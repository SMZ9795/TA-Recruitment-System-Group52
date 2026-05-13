package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for Admin functions: TA workload monitoring and recruitment summary.
 */
public class AdminService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public AdminService(UserRepository userRepository, JobRepository jobRepository,
                        ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
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
        public final String moduleCode;
        public final String moduleName;
        public final int positions;
        public final int filled;
        public final JobStatus status;

        public JobOverview(String moduleCode, String moduleName,
                           int positions, int filled, JobStatus status) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.positions = positions;
            this.filled = filled;
            this.status = status;
        }

        public boolean isFull() {
            return filled >= positions && positions > 0;
        }

        public String filledRatio() {
            return filled + "/" + positions;
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

    /** TAs at risk (>= 80% utilisation) or already overloaded. */
    public List<TAWorkloadSummary> getHighRiskTAs() {
        return getAllTAWorkloads().stream()
                .filter(s -> s.getRiskLevel() != RiskLevel.OK)
                .toList();
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
            result.add(new JobOverview(
                    job.getModuleCode(), job.getModuleName(),
                    job.getPositions(), filled, job.getStatus()));
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

    // -------------------------------------------------------------------------
    // WorkloadAlert: structured alert system
    // -------------------------------------------------------------------------

    /** Severity of a workload alert. */
    public enum AlertSeverity {
        CRITICAL, WARNING, INFO;

        public String label() {
            return switch (this) {
                case CRITICAL -> "Critical";
                case WARNING  -> "Warning";
                case INFO     -> "Info";
            };
        }
    }

    /** A single workload alert entry. */
    public static class WorkloadAlert {
        private final AlertSeverity severity;
        private final String taUserId;
        private final String taName;
        private final String message;
        private final String suggestedAction;

        public WorkloadAlert(AlertSeverity severity, String taUserId, String taName,
                             String message, String suggestedAction) {
            this.severity        = severity;
            this.taUserId        = taUserId;
            this.taName          = taName;
            this.message         = message;
            this.suggestedAction = suggestedAction;
        }

        public AlertSeverity getSeverity()      { return severity; }
        public String getTaUserId()             { return taUserId; }
        public String getTaName()               { return taName; }
        public String getMessage()              { return message; }
        public String getSuggestedAction()      { return suggestedAction; }
    }

    /**
     * Generates a list of workload alerts for all active TAs.
     * <ul>
     *   <li>CRITICAL — TA is overloaded (assigned > availableHours)</li>
     *   <li>WARNING  — TA is at risk (>= 80 % utilisation)</li>
     *   <li>INFO     — TA has declared availableHours but has no accepted job yet (idle capacity)</li>
     * </ul>
     * Alerts are sorted: CRITICAL first, then WARNING, then INFO.
     */
    public List<WorkloadAlert> getWorkloadAlerts() {
        List<WorkloadAlert> alerts = new ArrayList<>();

        // CRITICAL and WARNING from TAs with accepted jobs
        for (TAWorkloadSummary s : getAllTAWorkloads()) {
            if (s.isOverloaded()) {
                int excess = s.getTotalAssignedHours() - s.getAvailableHours();
                alerts.add(new WorkloadAlert(
                        AlertSeverity.CRITICAL,
                        s.getTaUserId(), s.getTaName(),
                        String.format("Assigned %dh/week exceeds declared capacity of %dh/week (overloaded by %dh).",
                                s.getTotalAssignedHours(), s.getAvailableHours(), excess),
                        "Review accepted applications and consider redistributing workload."));
            } else if (s.getRiskLevel() == RiskLevel.AT_RISK) {
                alerts.add(new WorkloadAlert(
                        AlertSeverity.WARNING,
                        s.getTaUserId(), s.getTaName(),
                        String.format("Utilisation at %.0f%% (%dh assigned of %dh available).",
                                s.getUtilisationPercent(), s.getTotalAssignedHours(), s.getAvailableHours()),
                        "Monitor closely before assigning additional positions."));
            }
        }

        // INFO — idle TAs (available hours declared but no accepted job)
        for (WorkloadAlert a : getIdleTAAlerts()) {
            alerts.add(a);
        }

        alerts.sort(Comparator.comparingInt(a -> a.getSeverity().ordinal()));
        return alerts;
    }

    /** INFO-level alerts for TAs with declared capacity but no accepted positions. */
    private List<WorkloadAlert> getIdleTAAlerts() {
        List<Application> allApps = applicationRepository.findAll();
        List<WorkloadAlert> idle = new ArrayList<>();
        for (User ta : userRepository.findAll()) {
            if (ta.getRole() != Role.TA || !ta.isActive() || ta.getAvailableHours() <= 0) continue;
            boolean hasAccepted = allApps.stream()
                    .anyMatch(app -> app.getTaUserId().equalsIgnoreCase(ta.getId())
                            && app.getStatus() == ApplicationStatus.ACCEPTED);
            if (!hasAccepted) {
                idle.add(new WorkloadAlert(
                        AlertSeverity.INFO,
                        ta.getId(), ta.getName(),
                        String.format("Has %dh/week available but no accepted positions yet.", ta.getAvailableHours()),
                        "Consider this TA for open positions that match their skills."));
            }
        }
        return idle;
    }

    /**
     * Returns active TAs who have declared available hours but hold no accepted positions.
     * Useful for identifying untapped capacity.
     */
    public List<User> getIdleTAs() {
        List<Application> allApps = applicationRepository.findAll();
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TA && u.isActive() && u.getAvailableHours() > 0)
                .filter(ta -> allApps.stream()
                        .noneMatch(app -> app.getTaUserId().equalsIgnoreCase(ta.getId())
                                && app.getStatus() == ApplicationStatus.ACCEPTED))
                .toList();
    }

    // -------------------------------------------------------------------------
    // Department / module-level statistics
    // -------------------------------------------------------------------------

    /** Aggregated statistics for a single module (department). */
    public static class ModuleStats {
        public final String moduleCode;
        public final String moduleName;
        public final int totalPositions;
        public final int filledPositions;
        public final int assignedTAs;
        public final int totalAssignedHours;

        public ModuleStats(String moduleCode, String moduleName,
                           int totalPositions, int filledPositions,
                           int assignedTAs, int totalAssignedHours) {
            this.moduleCode          = moduleCode;
            this.moduleName          = moduleName;
            this.totalPositions      = totalPositions;
            this.filledPositions     = filledPositions;
            this.assignedTAs         = assignedTAs;
            this.totalAssignedHours  = totalAssignedHours;
        }

        /** Percentage of positions filled (0–100). */
        public double fillRate() {
            if (totalPositions <= 0) return 0.0;
            return Math.min(100.0, (double) filledPositions / totalPositions * 100.0);
        }

        public String filledRatio() {
            return filledPositions + "/" + totalPositions;
        }
    }

    /**
     * Returns per-module statistics: how many positions exist, how many are filled,
     * how many distinct TAs are assigned, and total assigned hours for that module.
     * Only includes OPEN or CLOSED jobs (excludes DRAFT).
     */
    public List<ModuleStats> getDepartmentStats() {
        List<Application> allApps = applicationRepository.findAll();
        List<Job> jobs = jobRepository.findAll().stream()
                .filter(j -> j.getStatus() == JobStatus.OPEN || j.getStatus() == JobStatus.FILLED)
                .toList();

        // Group by moduleCode, accumulating positions, filled count, assigned TAs, and hours
        java.util.Map<String, int[]> totals = new java.util.LinkedHashMap<>(); // [positions, filled, hours]
        java.util.Map<String, String> names = new java.util.LinkedHashMap<>();
        java.util.Map<String, java.util.Set<String>> tasByModule = new java.util.LinkedHashMap<>();

        for (Job job : jobs) {
            String code = job.getModuleCode();
            names.putIfAbsent(code, job.getModuleName());
            totals.putIfAbsent(code, new int[3]);
            tasByModule.putIfAbsent(code, new java.util.HashSet<>());

            List<Application> accepted = allApps.stream()
                    .filter(a -> a.getJobId().equals(job.getId())
                            && a.getStatus() == ApplicationStatus.ACCEPTED)
                    .toList();
            totals.get(code)[0] += job.getPositions();
            totals.get(code)[1] += accepted.size();
            totals.get(code)[2] += accepted.size() * job.getHoursPerWeek();
            for (Application a : accepted) tasByModule.get(code).add(a.getTaUserId());
        }

        List<ModuleStats> result = new ArrayList<>();
        for (String code : totals.keySet()) {
            int[] t = totals.get(code);
            result.add(new ModuleStats(code, names.get(code), t[0], t[1],
                    tasByModule.get(code).size(), t[2]));
        }
        result.sort(Comparator.comparing(s -> s.moduleCode));
        return result;
    }

    // -------------------------------------------------------------------------
    // Capacity planning helpers
    // -------------------------------------------------------------------------

    /**
     * Total declared available hours across all active TAs.
     * Represents the system's maximum weekly TA capacity.
     */
    public int getTotalAvailableCapacity() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.TA && u.isActive())
                .mapToInt(User::getAvailableHours)
                .sum();
    }

    /**
     * Total hours currently assigned (accepted) across all active TAs.
     */
    public int getTotalAssignedHours() {
        return getAllTAWorkloads().stream()
                .mapToInt(TAWorkloadSummary::getTotalAssignedHours)
                .sum();
    }

    /**
     * System-wide utilisation percentage: totalAssigned / totalAvailable * 100.
     * Returns 0 if no capacity is declared.
     */
    public double getSystemUtilisation() {
        int capacity = getTotalAvailableCapacity();
        if (capacity <= 0) return 0.0;
        return Math.min(100.0, (double) getTotalAssignedHours() / capacity * 100.0);
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
