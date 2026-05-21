package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.WorkloadBalancerService.WorkloadRecommendation;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for Admin functions: TA workload monitoring and recruitment summary.
 */
public class AdminService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationService notificationService;
    private final WorkloadBalancerService workloadBalancerService;
    private final JobService jobService;

    /** In-memory audit log for the current session. */
    private final List<AuditLogEntry> auditLog = Collections.synchronizedList(new ArrayList<>());

    public AdminService(UserRepository userRepository, JobRepository jobRepository,
                        ApplicationRepository applicationRepository) {
        this(userRepository, jobRepository, applicationRepository, null, null);
    }

    public AdminService(UserRepository userRepository, JobRepository jobRepository,
                        ApplicationRepository applicationRepository, NotificationService notificationService) {
        this(userRepository, jobRepository, applicationRepository, notificationService, null);
    }

    public AdminService(UserRepository userRepository, JobRepository jobRepository,
                        ApplicationRepository applicationRepository, NotificationService notificationService,
                        JobService jobService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.notificationService = notificationService;
        this.jobService = jobService;
        this.workloadBalancerService = new WorkloadBalancerService(userRepository, jobRepository, applicationRepository);
    }

    public enum RiskLevel {
        OK, AT_RISK, OVERLOADED;

        public String label() {
            return switch (this) {
                case OK -> "OK";
                case AT_RISK -> "At Risk";
                case OVERLOADED -> "Overloaded";
            };
        }
    }

    public static class TAWorkloadSummary {
        private final String taUserId;
        private final String taName;
        private final int availableHours;
        private final int acceptedJobCount;
        private final int totalAssignedHours;
        private final List<String> acceptedJobDescriptions;

        public TAWorkloadSummary(String taUserId, String taName, int availableHours,
                                 int acceptedJobCount, int totalAssignedHours,
                                 List<String> acceptedJobDescriptions) {
            this.taUserId = taUserId;
            this.taName = taName;
            this.availableHours = availableHours;
            this.acceptedJobCount = acceptedJobCount;
            this.totalAssignedHours = totalAssignedHours;
            this.acceptedJobDescriptions = acceptedJobDescriptions;
        }

        public String getTaUserId() { return taUserId; }
        public String getTaName() { return taName; }
        public int getAvailableHours() { return availableHours; }
        public int getAcceptedJobCount() { return acceptedJobCount; }
        public int getTotalAssignedHours() { return totalAssignedHours; }
        public List<String> getAcceptedJobDescriptions() { return acceptedJobDescriptions; }

        public int getRemainingHours() {
            return Math.max(0, availableHours - totalAssignedHours);
        }

        public double getUtilisationPercent() {
            if (availableHours <= 0) return totalAssignedHours > 0 ? 100.0 : 0.0;
            return Math.min(100.0, (double) totalAssignedHours / availableHours * 100.0);
        }

        /** True when assigned hours exceed the TA's own declared availableHours. */
        public boolean isOverloaded() {
            return availableHours > 0 && totalAssignedHours > availableHours;
        }

        /**
         * Risk classification based on percentage of availableHours consumed.
         * AT_RISK: >= 80% utilisation; OVERLOADED: > 100%.
         */
        public RiskLevel getRiskLevel() {
            if (availableHours <= 0) {
                return totalAssignedHours > 0 ? RiskLevel.OVERLOADED : RiskLevel.OK;
            }
            double utilisation = (double) totalAssignedHours / availableHours;
            if (utilisation > 1.0) return RiskLevel.OVERLOADED;
            if (utilisation >= 0.8) return RiskLevel.AT_RISK;
            return RiskLevel.OK;
        }
    }

    /** Structured snapshot returned by {@link #getRecruitmentSnapshot()}. */
    public static class RecruitmentSnapshot {
        public final int totalJobs;
        public final int filledJobs;
        public final int openJobs;
        public final int totalActiveTAs;
        public final int overloadedTAs;
        public final int atRiskTAs;

        public RecruitmentSnapshot(int totalJobs, int filledJobs, int openJobs,
                                   int totalActiveTAs, int overloadedTAs, int atRiskTAs) {
            this.totalJobs = totalJobs;
            this.filledJobs = filledJobs;
            this.openJobs = openJobs;
            this.totalActiveTAs = totalActiveTAs;
            this.overloadedTAs = overloadedTAs;
            this.atRiskTAs = atRiskTAs;
        }
    }

    /** Per-job overview entry used in the Jobs Overview panel. */
    public static class JobOverview {
        public final String jobId;
        public final String moduleCode;
        public final String moduleName;
        public final String description;
        public final String requiredSkills;
        public final int hoursPerWeek;
        public final int positions;
        public final int filled;
        public final String deadline;
        public final String postedByMoId;
        public final String postedByMoName;
        public final JobStatus status;

        public JobOverview(String jobId, String moduleCode, String moduleName,
                           String description, String requiredSkills, int hoursPerWeek,
                           int positions, int filled, String deadline,
                           String postedByMoId, String postedByMoName, JobStatus status) {
            this.jobId = jobId;
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.description = description;
            this.requiredSkills = requiredSkills;
            this.hoursPerWeek = hoursPerWeek;
            this.positions = positions;
            this.filled = filled;
            this.deadline = deadline;
            this.postedByMoId = postedByMoId;
            this.postedByMoName = postedByMoName;
            this.status = status;
        }

        public boolean isFull() {
            return filled >= positions && positions > 0;
        }

        public String filledRatio() {
            return filled + "/" + positions;
        }
    }

    // ========================= Application overview types =========================

    /** Enriched application entry for Admin's Applications Overview panel. */
    public static class EnrichedApplication {
        public final String applicationId;
        public final String jobId;
        public final String moduleCode;
        public final String moduleName;
        public final String taUserId;
        public final String taName;
        public final ApplicationStatus status;
        public final String appliedDate;

        public EnrichedApplication(String applicationId, String jobId, String moduleCode,
                                   String moduleName, String taUserId, String taName,
                                   ApplicationStatus status, String appliedDate) {
            this.applicationId = applicationId;
            this.jobId = jobId;
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.taUserId = taUserId;
            this.taName = taName;
            this.status = status;
            this.appliedDate = appliedDate;
        }
    }

    /** Application statistics summary. */
    public static class ApplicationStats {
        public final int total;
        public final int pending;
        public final int accepted;
        public final int rejected;
        public final int withdrawn;

        public ApplicationStats(int total, int pending, int accepted, int rejected, int withdrawn) {
            this.total = total;
            this.pending = pending;
            this.accepted = accepted;
            this.rejected = rejected;
            this.withdrawn = withdrawn;
        }
    }

    // ========================= Audit log types =========================

    /** In-memory audit log entry for admin operations. */
    public static class AuditLogEntry {
        public final String timestamp;
        public final String adminUserId;
        public final String action;
        public final String targetId;
        public final String details;

        public AuditLogEntry(String adminUserId, String action, String targetId, String details) {
            this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.adminUserId = adminUserId;
            this.action = action;
            this.targetId = targetId;
            this.details = details;
        }
    }

    /**
     * Workload summaries for all active TAs that have at least one accepted application,
     * sorted by risk level (OVERLOADED first) then by utilisation descending.
     */
    public List<TAWorkloadSummary> getAllTAWorkloads() {
        List<Application> allApplications = applicationRepository.findAll();
        List<User> taUsers = userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.TA && user.isActive())
                .toList();

        List<TAWorkloadSummary> summaries = new ArrayList<>();
        for (User ta : taUsers) {
            List<Application> acceptedApps = allApplications.stream()
                    .filter(app -> app.getTaUserId().equalsIgnoreCase(ta.getId()))
                    .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                    .toList();
            if (acceptedApps.isEmpty()) continue;
            summaries.add(buildSummary(ta, acceptedApps));
        }

        summaries.sort(Comparator
                .comparingInt((TAWorkloadSummary s) -> -s.getRiskLevel().ordinal())
                .thenComparingDouble(s -> -s.getUtilisationPercent()));
        return summaries;
    }

    /** Workload summary for a specific TA. */
    public TAWorkloadSummary getTAWorkload(String taUserId) {
        if (taUserId == null || taUserId.isBlank()) {
            throw new IllegalArgumentException("TA user ID is required.");
        }
        String normalizedId = taUserId.trim();
        User ta = userRepository.findById(normalizedId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (ta.getRole() != Role.TA) {
            throw new IllegalArgumentException("User is not a TA.");
        }
        List<Application> acceptedApps = applicationRepository.findByTaUserId(normalizedId).stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .toList();
        return buildSummary(ta, acceptedApps);
    }

    /** TAs whose assigned hours exceed their own availableHours. */
    public List<TAWorkloadSummary> getOverloadedTAs() {
        return getAllTAWorkloads().stream()
                .filter(TAWorkloadSummary::isOverloaded)
                .toList();
    }

    /**
     * Deterministic redistribution recommendations for the workload page.
     *
     * The method pairs each overloaded TA with the underused TA that has the
     * largest remaining capacity at that moment. This keeps the logic easy to
     * explain in viva/demo settings and avoids any ML/AI dependency.
     */
    public List<String> getWorkloadBalancingRecommendations() {
        List<TAWorkloadSummary> workloads = new ArrayList<>(getAllTAWorkloads());
        List<TAWorkloadSummary> overloaded = workloads.stream()
                .filter(TAWorkloadSummary::isOverloaded)
                .toList();
        List<TAWorkloadSummary> available = workloads.stream()
                .filter(s -> !s.isOverloaded())
                .filter(s -> s.getRemainingHours() > 0)
                .toList();

        List<String> recommendations = new ArrayList<>();
        if (overloaded.isEmpty()) {
            recommendations.add("All TA workloads are balanced.");
            return recommendations;
        }

        List<TAWorkloadSummary> mutableAvailable = new ArrayList<>(available);
        for (TAWorkloadSummary source : overloaded) {
            int overloadHours = source.getTotalAssignedHours() - source.getAvailableHours();
            if (overloadHours <= 0) {
                continue;
            }

            TAWorkloadSummary target = mutableAvailable.stream()
                    .filter(candidate -> candidate.getRemainingHours() > 0)
                    .max(Comparator
                            .comparingInt(TAWorkloadSummary::getRemainingHours)
                            .thenComparing(TAWorkloadSummary::getTaName, String.CASE_INSENSITIVE_ORDER))
                    .orElse(null);

            if (target == null) {
                recommendations.add(String.format("%s is overloaded by %dh/week.",
                        source.getTaName(), overloadHours));
                recommendations.add("No available TA has remaining capacity for redistribution.");
                continue;
            }

            int moveHours = Math.min(overloadHours, target.getRemainingHours());
            recommendations.add(String.format("%s is overloaded by %dh/week.",
                    source.getTaName(), overloadHours));
            recommendations.add(String.format("%s has %dh/week remaining capacity.",
                    target.getTaName(), target.getRemainingHours()));
            recommendations.add(String.format("Suggested action: Move %dh/week from %s to %s.",
                    moveHours, source.getTaName(), target.getTaName()));

            int updatedRemaining = target.getRemainingHours() - moveHours;
            if (updatedRemaining <= 0) {
                mutableAvailable.remove(target);
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("All TA workloads are balanced.");
        }
        return recommendations;
    }

    public int publishOverloadAlerts() {
        if (notificationService == null) {
            return 0;
        }
        int createdCount = 0;
        for (TAWorkloadSummary summary : getOverloadedTAs()) {
            String message = "Overload alert: you are assigned "
                    + summary.getTotalAssignedHours() + "h/week, exceeding your available "
                    + summary.getAvailableHours() + "h/week.";
            String relatedId = "OVERLOAD:" + summary.getTaUserId();
            int before = notificationService.countUnreadForUser(summary.getTaUserId());
            notificationService.publishIfNotExists(
                    Role.TA,
                    NotificationType.OVERLOAD_ALERT,
                    summary.getTaUserId(),
                    message,
                    relatedId);
            int after = notificationService.countUnreadForUser(summary.getTaUserId());
            if (after > before) {
                createdCount++;
            }
        }
        return createdCount;
    }

    /** TAs at risk (>= 80% utilisation) or already overloaded. */
    public List<TAWorkloadSummary> getHighRiskTAs() {
        return getAllTAWorkloads().stream()
                .filter(s -> s.getRiskLevel() != RiskLevel.OK)
                .toList();
    }

    public List<WorkloadBalancerService.WorkloadRecommendation> getWorkloadRecommendations() {
        return workloadBalancerService.generateRecommendations();
    }

    public String getWorkloadBalancingSummary() {
        return workloadBalancerService.buildAutoSummary();
    }

    public String getWorkloadBalancingReport() {
        return workloadBalancerService.buildReport();
    }

    /** Per-job overview list sorted: full jobs last so open slots appear first. */
    public List<JobOverview> getJobsOverview() {
        List<Application> allApplications = applicationRepository.findAll();
        List<JobOverview> result = new ArrayList<>();
        for (Job job : jobRepository.findAll()) {
            int filled = (int) allApplications.stream()
                    .filter(a -> a.getJobId().equals(job.getId())
                            && a.getStatus() == ApplicationStatus.ACCEPTED)
                    .count();
            String moName = resolveMoName(job.getPostedByMoId());
            result.add(new JobOverview(
                    job.getId(), job.getModuleCode(), job.getModuleName(),
                    job.getDescription(), job.getRequiredSkills(), job.getHoursPerWeek(),
                    job.getPositions(), filled, job.getDeadline(),
                    job.getPostedByMoId(), moName, job.getStatus()));
        }
        result.sort(Comparator.comparingInt(o -> (o.isFull() ? 1 : 0)));
        return result;
    }

    /** Structured recruitment snapshot for the Admin summary bar. */
    public RecruitmentSnapshot getRecruitmentSnapshot() {
        List<Job> allJobs = jobRepository.findAll();
        List<JobOverview> overviews = getJobsOverview();
        int filledJobs = (int) overviews.stream().filter(JobOverview::isFull).count();
        int openJobs = (int) allJobs.stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN).count();

        List<User> activeTAs = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TA && u.isActive())
                .toList();
        List<TAWorkloadSummary> workloads = getAllTAWorkloads();
        int overloaded = (int) workloads.stream().filter(TAWorkloadSummary::isOverloaded).count();
        int atRisk = (int) workloads.stream()
                .filter(s -> s.getRiskLevel() == RiskLevel.AT_RISK).count();

        return new RecruitmentSnapshot(
                allJobs.size(), filledJobs, openJobs,
                activeTAs.size(), overloaded, atRisk);
    }

    /**
     * Detailed plain-text workload report listing every TA's hours and risk status,
     * with a section for overloaded TAs highlighted at the top.
     */
    public String getWorkloadReport() {
        List<TAWorkloadSummary> workloads = getAllTAWorkloads();
        StringBuilder sb = new StringBuilder();
        sb.append("=== TA Workload Report ===\n\n");

        List<TAWorkloadSummary> overloaded = workloads.stream()
                .filter(TAWorkloadSummary::isOverloaded).toList();
        List<TAWorkloadSummary> atRisk = workloads.stream()
                .filter(s -> s.getRiskLevel() == RiskLevel.AT_RISK).toList();

        if (!overloaded.isEmpty()) {
            sb.append("!! OVERLOADED TAs (immediate attention required):\n");
            for (TAWorkloadSummary s : overloaded) {
                sb.append(String.format("   %-20s assigned=%dh  available=%dh  over=%dh\n",
                        s.getTaName(), s.getTotalAssignedHours(),
                        s.getAvailableHours(), s.getTotalAssignedHours() - s.getAvailableHours()));
                for (String desc : s.getAcceptedJobDescriptions()) {
                    sb.append("      - ").append(desc).append("\n");
                }
            }
            sb.append("\n");
        }

        if (!atRisk.isEmpty()) {
            sb.append("! AT-RISK TAs (>= 80% utilisation):\n");
            for (TAWorkloadSummary s : atRisk) {
                sb.append(String.format("   %-20s assigned=%dh  available=%dh  (%.0f%%)\n",
                        s.getTaName(), s.getTotalAssignedHours(),
                        s.getAvailableHours(), s.getUtilisationPercent()));
            }
            sb.append("\n");
        }

        sb.append("All active TAs with accepted positions:\n");
        for (TAWorkloadSummary s : workloads) {
            sb.append(String.format("   %-20s [%s] assigned=%dh  remaining=%dh\n",
                    s.getTaName(), s.getRiskLevel().label(),
                    s.getTotalAssignedHours(), s.getRemainingHours()));
        }
        return sb.toString();
    }

    /** Human-readable recruitment summary string. */
    public String getRecruitmentSummary() {
        RecruitmentSnapshot s = getRecruitmentSnapshot();
        List<TAWorkloadSummary> workloads = getAllTAWorkloads();
        int totalPositions = workloads.stream().mapToInt(TAWorkloadSummary::getAcceptedJobCount).sum();
        int totalHours = workloads.stream().mapToInt(TAWorkloadSummary::getTotalAssignedHours).sum();
        return String.format(
                "=== Recruitment Summary ===%n"
                + "  Total jobs posted:           %d%n"
                + "  Filled jobs:                 %d%n"
                + "  Open jobs:                   %d%n"
                + "  Active TAs:                  %d%n"
                + "  TAs with accepted positions: %d%n"
                + "  Total accepted positions:    %d%n"
                + "  Total assigned hours/week:   %d%n"
                + "  Overloaded TAs:              %d%n"
                + "  At-risk TAs:                 %d",
                s.totalJobs, s.filledJobs, s.openJobs, s.totalActiveTAs,
                workloads.size(), totalPositions, totalHours, s.overloadedTAs, s.atRiskTAs);
    }

    /** All active TA users (for admin to browse). */
    public List<User> getAllTAs() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.TA && user.isActive())
                .toList();
    }

    /**
     * Search workload summaries by TA name or ID (case-insensitive substring match).
     * Returns an empty list when keyword is blank.
     */
    public List<TAWorkloadSummary> searchTAWorkload(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new ArrayList<>();
        }
        String lower = keyword.trim().toLowerCase();
        return getAllTAWorkloads().stream()
                .filter(s -> s.getTaName().toLowerCase().contains(lower)
                        || s.getTaUserId().toLowerCase().contains(lower))
                .toList();
    }

    /**
     * Workload trend label for a TA based on accepted job count.
     * New: 1 accepted job; Growing: 2; Established: 3+.
     */
    public enum WorkloadTrend {
        NEW, GROWING, ESTABLISHED;

        public String label() {
            return switch (this) {
                case NEW -> "New";
                case GROWING -> "Growing";
                case ESTABLISHED -> "Established";
            };
        }
    }

    /** Derives a WorkloadTrend label for the given workload summary. */
    public WorkloadTrend getWorkloadTrend(TAWorkloadSummary summary) {
        int count = summary.getAcceptedJobCount();
        if (count >= 3) return WorkloadTrend.ESTABLISHED;
        if (count == 2) return WorkloadTrend.GROWING;
        return WorkloadTrend.NEW;
    }

    // ========================= Job management =========================

    /**
     * Force-close a job regardless of MO ownership.
     * Only Admin should call this method.
     */
    public Job forceCloseJob(String jobId, String adminUserId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID is required.");
        }
        Job job = jobRepository.findById(jobId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("This job is already closed.");
        }
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
        addAuditEntry(adminUserId, "FORCE_CLOSE_JOB", jobId,
                "Closed job " + job.getModuleCode() + " - " + job.getModuleName());
        publishAdminNotification(adminUserId, NotificationType.ADMIN_JOB_FORCE_CLOSED,
                "Force-closed job: " + job.getModuleCode() + " - " + job.getModuleName(), jobId);
        return job;
    }

    /**
     * Force-reopen a closed or filled job regardless of MO ownership.
     * Only Admin should call this method.
     */
    public Job forceReopenJob(String jobId, String adminUserId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID is required.");
        }
        Job job = jobRepository.findById(jobId.trim())
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (job.getStatus() == JobStatus.OPEN) {
            throw new IllegalArgumentException("This job is already open.");
        }
        // Check deadline
        if (job.getDeadline() != null && !job.getDeadline().isBlank()) {
            try {
                if (LocalDate.parse(job.getDeadline().trim()).isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException(
                            "Cannot reopen a job whose deadline has passed (" + job.getDeadline() + ").");
                }
            } catch (DateTimeParseException ignored) {
                // If deadline is unparseable, allow reopen
            }
        }
        job.setStatus(JobStatus.OPEN);
        jobRepository.save(job);
        addAuditEntry(adminUserId, "FORCE_REOPEN_JOB", jobId,
                "Reopened job " + job.getModuleCode() + " - " + job.getModuleName());
        return job;
    }

    /**
     * Auto-close all expired (past-deadline) OPEN jobs.
     * Returns the number of jobs closed.
     */
    public int triggerAutoCloseExpiredJobs(String adminUserId) {
        int closedCount = 0;
        for (Job job : jobRepository.findAll()) {
            if (job.getStatus() != JobStatus.OPEN) continue;
            if (job.getDeadline() == null || job.getDeadline().isBlank()) continue;
            try {
                if (LocalDate.parse(job.getDeadline().trim()).isBefore(LocalDate.now())) {
                    job.setStatus(JobStatus.CLOSED);
                    jobRepository.save(job);
                    closedCount++;
                }
            } catch (DateTimeParseException ignored) {
                // skip unparseable deadlines
            }
        }
        if (closedCount > 0) {
            addAuditEntry(adminUserId, "AUTO_CLOSE_EXPIRED", "-",
                    "Auto-closed " + closedCount + " expired job(s).");
            publishAdminNotification(adminUserId, NotificationType.ADMIN_JOBS_AUTO_CLOSED,
                    "Auto-closed " + closedCount + " expired job(s).", "AUTO_CLOSE");
        }
        return closedCount;
    }

    // ========================= Application overview =========================

    /** All applications enriched with Job and TA display names. */
    public List<EnrichedApplication> getAllApplicationsEnriched() {
        List<EnrichedApplication> result = new ArrayList<>();
        for (Application app : applicationRepository.findAll()) {
            String moduleCode = "";
            String moduleName = "";
            Optional<Job> jobOpt = jobRepository.findById(app.getJobId());
            if (jobOpt.isPresent()) {
                moduleCode = jobOpt.get().getModuleCode();
                moduleName = jobOpt.get().getModuleName();
            }
            String taName = userRepository.findById(app.getTaUserId())
                    .map(User::getName).orElse(app.getTaUserId());
            result.add(new EnrichedApplication(
                    app.getId(), app.getJobId(), moduleCode, moduleName,
                    app.getTaUserId(), taName, app.getStatus(), app.getAppliedDate()));
        }
        result.sort(Comparator.comparing((EnrichedApplication e) -> e.appliedDate == null ? "" : e.appliedDate).reversed());
        return result;
    }

    /** Applications filtered by status. Pass null to get all. */
    public List<EnrichedApplication> getApplicationsByStatus(ApplicationStatus status) {
        if (status == null) {
            return getAllApplicationsEnriched();
        }
        return getAllApplicationsEnriched().stream()
                .filter(e -> e.status == status)
                .toList();
    }

    /** Application count statistics by status. */
    public ApplicationStats getApplicationStatistics() {
        List<Application> all = applicationRepository.findAll();
        int pending = 0, accepted = 0, rejected = 0, withdrawn = 0;
        for (Application app : all) {
            switch (app.getStatus()) {
                case PENDING -> pending++;
                case ACCEPTED -> accepted++;
                case REJECTED -> rejected++;
                case WITHDRAWN -> withdrawn++;
            }
        }
        return new ApplicationStats(all.size(), pending, accepted, rejected, withdrawn);
    }

    // ========================= User detail =========================

    /** Detailed user summary for admin inspection. */
    public String getUserDetailSummary(String userId) {
        if (userId == null || userId.isBlank()) {
            return "User ID is required.";
        }
        User user = userRepository.findById(userId.trim()).orElse(null);
        if (user == null) {
            return "User not found.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("User ID:         ").append(user.getId()).append("\n");
        sb.append("Name:            ").append(safe(user.getName())).append("\n");
        sb.append("Email:           ").append(safe(user.getEmail())).append("\n");
        sb.append("Role:            ").append(user.getRole()).append("\n");
        sb.append("Status:          ").append(user.isActive() ? "Active" : "Deactivated").append("\n");

        if (user.getRole() == Role.TA) {
            sb.append("Programme:       ").append(safe(user.getProgramme())).append("\n");
            sb.append("Year of Study:   ").append(user.getYearOfStudy()).append("\n");
            sb.append("Skills:          ").append(safe(user.getSkills())).append("\n");
            sb.append("Available h/wk:  ").append(user.getAvailableHours()).append("\n");
            sb.append("CV File:         ").append(safe(user.getCvFilePath()).isBlank() ? "Not uploaded" : user.getCvFilePath()).append("\n");

            // Application stats for this TA
            List<Application> taApps = applicationRepository.findByTaUserId(userId.trim());
            long acceptedCount = taApps.stream().filter(a -> a.getStatus() == ApplicationStatus.ACCEPTED).count();
            long pendingCount = taApps.stream().filter(a -> a.getStatus() == ApplicationStatus.PENDING).count();
            sb.append("\nApplication Summary:\n");
            sb.append("  Total applied:   ").append(taApps.size()).append("\n");
            sb.append("  Accepted:        ").append(acceptedCount).append("\n");
            sb.append("  Pending/Review:  ").append(pendingCount).append("\n");

            // Workload info if there are accepted apps
            if (acceptedCount > 0 && user.isActive()) {
                try {
                    TAWorkloadSummary workload = getTAWorkload(userId.trim());
                    sb.append("\nWorkload:\n");
                    sb.append("  Assigned h/wk:   ").append(workload.getTotalAssignedHours()).append("\n");
                    sb.append("  Remaining h/wk:  ").append(workload.getRemainingHours()).append("\n");
                    sb.append("  Risk Level:      ").append(workload.getRiskLevel().label()).append("\n");
                    sb.append("  Utilisation:     ").append(String.format("%.0f%%", workload.getUtilisationPercent())).append("\n");
                } catch (IllegalArgumentException ignored) {
                    // TA may not have accepted apps matching criteria
                }
            }
        } else if (user.getRole() == Role.MO) {
            // Show jobs posted by this MO
            List<Job> moJobs = jobRepository.findByPostedByMoId(userId.trim());
            sb.append("\nPosted Jobs: ").append(moJobs.size()).append("\n");
            for (Job job : moJobs) {
                sb.append("  • ").append(job.getModuleCode()).append(" - ").append(job.getModuleName())
                        .append(" [").append(job.getStatus().name()).append("]\n");
            }
        }
        return sb.toString();
    }

    // ========================= Notifications & Alerts =========================

    public int sendCustomNotification(String adminUserId, Role targetRole, String userId, String message) {
        if (notificationService == null) return 0;
        int count = 0;
        
        List<User> targets = new ArrayList<>();
        if (userId != null && !userId.isBlank()) {
            Optional<User> u = userRepository.findById(userId);
            u.ifPresent(targets::add);
        } else {
            for (User u : userRepository.findAll()) {
                if (targetRole == null || u.getRole() == targetRole) {
                    targets.add(u);
                }
            }
        }
        
        String broadcastId = "ADMIN_BROADCAST:" + System.currentTimeMillis();
        for (User u : targets) {
            notificationService.publish(u.getRole(), NotificationType.SYSTEM_ALERT, u.getId(), message, broadcastId);
            count++;
        }
        
        addAuditEntry(adminUserId, "SEND_NOTIFICATION", targetRole == null ? "ALL" : targetRole.name(), "Sent to " + count + " users. Message: " + message);
        
        publishAdminNotification(adminUserId, NotificationType.SYSTEM_ALERT, 
                "You sent a notification to " + count + " users: " + message, broadcastId);
                
        return count;
    }

    public int broadcastAnomalies(String adminUserId) {
        if (notificationService == null) return 0;
        int count = publishOverloadAlerts(); 

        List<Application> allApps = applicationRepository.findAll();
        for (Job job : jobRepository.findAll()) {
            boolean isClosedOrExpired = job.getStatus() == JobStatus.CLOSED;
            if (!isClosedOrExpired && job.getDeadline() != null && !job.getDeadline().isBlank()) {
                try {
                    isClosedOrExpired = LocalDate.now().isAfter(LocalDate.parse(job.getDeadline()));
                } catch (DateTimeParseException ignored) {
                }
            }
            
            if (isClosedOrExpired) {
                long filled = allApps.stream()
                        .filter(a -> a.getJobId().equals(job.getId()) && a.getStatus() == ApplicationStatus.ACCEPTED)
                        .count();
                if (filled < job.getPositions()) {
                    String msg = "Anomaly Alert: Job '" + job.getModuleCode() + "' is closed/expired but not fully staffed (" + filled + "/" + job.getPositions() + ").";
                    notificationService.publishIfNotExists(
                            Role.MO, 
                            NotificationType.SYSTEM_ALERT, 
                            job.getPostedByMoId(), 
                            msg, 
                            "ANOMALY_UNFILLED:" + job.getId());
                    count++;
                }
            }
        }
        
        addAuditEntry(adminUserId, "BROADCAST_ANOMALIES", "SYSTEM", "Sent " + count + " anomaly alerts.");
        return count;
    }

    // ========================= Audit log =========================

    /** Record an audit log entry. */
    public void addAuditEntry(String adminUserId, String action, String targetId, String details) {
        auditLog.add(new AuditLogEntry(
                adminUserId == null ? "SYSTEM" : adminUserId,
                action, targetId == null ? "" : targetId,
                details == null ? "" : details));
    }

    /** Returns all audit log entries (newest first). */
    public List<AuditLogEntry> getAuditLog() {
        List<AuditLogEntry> copy = new ArrayList<>(auditLog);
        Collections.reverse(copy);
        return copy;
    }

    // ========================= Data export =========================

    /**
     * Export all TA workload data to a CSV file.
     * Returns the path of the written file.
     */
    public Path exportWorkloadToCsv(Path outputPath) throws IOException {
        List<TAWorkloadSummary> workloads = getAllTAWorkloads();
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("TA ID,TA Name,Available h/week,Assigned h/week,Remaining h,Utilisation %,Risk Level");
            writer.newLine();
            for (TAWorkloadSummary s : workloads) {
                writer.write(String.format("\"%s\",\"%s\",%d,%d,%d,%.0f,\"%s\"",
                        s.getTaUserId(), s.getTaName(), s.getAvailableHours(),
                        s.getTotalAssignedHours(), s.getRemainingHours(),
                        s.getUtilisationPercent(), s.getRiskLevel().label()));
                writer.newLine();
            }
        }
        return outputPath;
    }

    /**
     * Export all job overview data to a CSV file.
     */
    public Path exportJobsToCsv(Path outputPath) throws IOException {
        List<JobOverview> jobs = getJobsOverview();
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("Job ID,Module Code,Module Name,MO,Filled,Positions,Status,Deadline");
            writer.newLine();
            for (JobOverview j : jobs) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,\"%s\",\"%s\"",
                        j.jobId, j.moduleCode, j.moduleName, j.postedByMoName,
                        j.filled, j.positions, j.status.name(), j.deadline));
                writer.newLine();
            }
        }
        return outputPath;
    }

    // ========================= Internal helpers =========================

    private String resolveMoName(String moId) {
        if (moId == null || moId.isBlank()) return "Unknown";
        return userRepository.findById(moId.trim())
                .map(User::getName)
                .orElse(moId);
    }

    private void publishAdminNotification(String adminUserId, NotificationType type,
                                          String message, String relatedId) {
        if (notificationService == null || adminUserId == null || adminUserId.isBlank()) return;
        notificationService.publish(Role.ADMIN, type, adminUserId, message,
                relatedId == null ? "" : relatedId);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    /**
     * Optimised buildSummary: resolves each job only once per accepted application
     * instead of calling findById twice per app.
     */
    private TAWorkloadSummary buildSummary(User ta, List<Application> acceptedApps) {
        int totalHours = 0;
        List<String> jobDescriptions = new ArrayList<>();
        for (Application app : acceptedApps) {
            Optional<Job> jobOpt = jobRepository.findById(app.getJobId());
            if (jobOpt.isPresent()) {
                Job job = jobOpt.get();
                jobDescriptions.add(job.getModuleCode() + " - " + job.getModuleName()
                        + " (" + job.getHoursPerWeek() + "h/week)");
                totalHours += job.getHoursPerWeek();
            }
        }
        return new TAWorkloadSummary(
                ta.getId(), ta.getName(), ta.getAvailableHours(),
                acceptedApps.size(), totalHours, jobDescriptions);
    }
}
