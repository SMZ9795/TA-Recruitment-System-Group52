package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * A summary record for one TA's workload.
     */
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

        public boolean isOverloaded() {
            return totalAssignedHours > availableHours && availableHours > 0;
        }
    }

    /**
     * Get workload summaries for all TAs that have at least one accepted application.
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

            if (acceptedApps.isEmpty()) {
                continue;
            }

            summaries.add(buildSummary(ta, acceptedApps));
        }
        return summaries;
    }

    /**
     * Get workload summary for a specific TA.
     */
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

    /**
     * Get a summary overview: total TAs, total accepted positions, total hours assigned.
     */
    public String getRecruitmentSummary() {
        List<TAWorkloadSummary> workloads = getAllTAWorkloads();
        int totalTAs = workloads.size();
        int totalPositions = workloads.stream().mapToInt(TAWorkloadSummary::getAcceptedJobCount).sum();
        int totalHours = workloads.stream().mapToInt(TAWorkloadSummary::getTotalAssignedHours).sum();
        int overloadedCount = (int) workloads.stream().filter(TAWorkloadSummary::isOverloaded).count();

        long allJobsCount = jobRepository.findAll().size();
        long openJobsCount = jobRepository.findAll().stream()
                .filter(job -> job.getStatus() == com.group52.tarecruitment.model.JobStatus.OPEN)
                .count();

        return String.format(
                "=== Recruitment Summary ===%n"
                + "  Total jobs posted:           %d%n"
                + "  Open jobs:                   %d%n"
                + "  TAs with accepted positions: %d%n"
                + "  Total accepted positions:    %d%n"
                + "  Total assigned hours/week:   %d%n"
                + "  Overloaded TAs:              %d",
                allJobsCount, openJobsCount, totalTAs, totalPositions, totalHours, overloadedCount);
    }

    /**
     * Get all TA users (for admin to browse).
     */
    public List<User> getAllTAs() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.TA && user.isActive())
                .toList();
    }

    private TAWorkloadSummary buildSummary(User ta, List<Application> acceptedApps) {
        int totalHours = 0;
        List<String> jobDescriptions = new ArrayList<>();
        for (Application app : acceptedApps) {
            jobRepository.findById(app.getJobId()).ifPresent(job -> {
                jobDescriptions.add(job.getModuleCode() + " - " + job.getModuleName()
                        + " (" + job.getHoursPerWeek() + "h/week)");
            });
            totalHours += jobRepository.findById(app.getJobId())
                    .map(Job::getHoursPerWeek)
                    .orElse(0);
        }

        return new TAWorkloadSummary(
                ta.getId(),
                ta.getName(),
                ta.getAvailableHours(),
                acceptedApps.size(),
                totalHours,
                jobDescriptions);
    }
}