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
