package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.ValidationUtil;
import com.group52.tarecruitment.util.WorkloadRules;
import com.group52.tarecruitment.util.WorkloadRules.WorkloadStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Deterministic and explainable workload balancing service.
 *
 * It analyses accepted applications, classifies TAs using fixed rules, and
 * generates human-readable balancing recommendations with fairness notes.
 */
public class WorkloadBalancerService {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public WorkloadBalancerService(UserRepository userRepository,
                                   JobRepository jobRepository,
                                   ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<WorkloadAnalysis> analyzeWorkload() {
        List<WorkloadAnalysis> analyses = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            if (user.getRole() != Role.TA || !user.isActive()) {
                continue;
            }
            int weeklyHours = calculateWeeklyWorkload(user.getId());
            analyses.add(new WorkloadAnalysis(user.getId(), user.getName(), weeklyHours, user.getAvailableHours()));
        }
        analyses.sort(Comparator.comparing(WorkloadAnalysis::getTaName, String.CASE_INSENSITIVE_ORDER));
        return analyses;
    }

    public WorkloadStatus classifyTA(int weeklyWorkloadHours) {
        if (weeklyWorkloadHours <= 0) {
            return WorkloadStatus.UNDERUSED;
        }
        if (weeklyWorkloadHours > WorkloadRules.OVERLOADED_THRESHOLD_HOURS + 5) {
            return WorkloadStatus.HIGH_RISK;
        }
        if (weeklyWorkloadHours > WorkloadRules.OVERLOADED_THRESHOLD_HOURS) {
            return WorkloadStatus.OVERLOADED;
        }
        if (weeklyWorkloadHours < WorkloadRules.UNDERUSED_THRESHOLD_HOURS) {
            return WorkloadStatus.UNDERUSED;
        }
        if (weeklyWorkloadHours <= WorkloadRules.UNDERUSED_THRESHOLD_HOURS + 2) {
            return WorkloadStatus.BALANCED;
        }
        return WorkloadStatus.BALANCED;
    }

    public List<WorkloadRecommendation> generateRecommendations() {
        List<WorkloadAnalysis> analyses = analyzeWorkload();
        List<WorkloadAnalysis> overloaded = analyses.stream()
                .filter(a -> a.getStatus() == WorkloadStatus.OVERLOADED || a.getStatus() == WorkloadStatus.HIGH_RISK)
                .collect(Collectors.toList());
        List<WorkloadAnalysis> available = analyses.stream()
                .filter(a -> a.getRemainingCapacityHours() > WorkloadRules.UNDERUSED_THRESHOLD_HOURS)
                .collect(Collectors.toList());

        List<WorkloadRecommendation> recommendations = new ArrayList<>();
        if (overloaded.isEmpty()) {
            return recommendations;
        }

        available.sort(Comparator.comparingInt(WorkloadAnalysis::getRemainingCapacityHours).reversed());

        for (WorkloadAnalysis source : overloaded) {
            int overloadHours = Math.max(0, source.getWeeklyWorkloadHours() - source.getAvailableHours());
            if (overloadHours <= 0) {
                continue;
            }

            WorkloadAnalysis target = available.stream()
                    .filter(candidate -> !candidate.getTaUserId().equalsIgnoreCase(source.getTaUserId()))
                    .filter(candidate -> candidate.getRemainingCapacityHours() > 0)
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                continue;
            }

            int moveHours = Math.min(overloadHours, target.getRemainingCapacityHours());
            WorkloadRecommendationPriority priority = overloadHours > 5
                    ? WorkloadRecommendationPriority.HIGH
                    : overloadHours >= 3
                    ? WorkloadRecommendationPriority.MEDIUM
                    : WorkloadRecommendationPriority.LOW;

            recommendations.add(new WorkloadRecommendation(
                    source.getTaUserId(),
                    source.getTaName(),
                    target.getTaUserId(),
                    target.getTaName(),
                    moveHours,
                    overloadHours,
                    target.getRemainingCapacityHours(),
                    priority));
        }

        return recommendations;
    }

    public List<String> generateSuggestions() {
        List<WorkloadRecommendation> recommendations = generateRecommendations();
        if (recommendations.isEmpty()) {
            return List.of("All TA workloads are balanced.");
        }
        List<String> lines = new ArrayList<>();
        for (WorkloadRecommendation recommendation : recommendations) {
            lines.add(recommendation.getOverloadedName() + " is overloaded by "
                    + recommendation.getOverloadHours() + "h/week.");
            lines.add(recommendation.getTargetName() + " has "
                    + recommendation.getTargetRemainingCapacityHours() + "h/week remaining capacity.");
            lines.add("Suggested action: Move " + recommendation.getMoveHours() + "h/week from "
                    + recommendation.getOverloadedName() + " to " + recommendation.getTargetName() + ".");
            lines.add(recommendation.getPriority().label() + " Redistribution Recommended.");
        }
        return lines;
    }

    public String buildAutoSummary() {
        List<WorkloadAnalysis> analyses = analyzeWorkload();
        long overloaded = analyses.stream().filter(a -> a.getStatus() == WorkloadStatus.OVERLOADED || a.getStatus() == WorkloadStatus.HIGH_RISK).count();
        long available = analyses.stream().filter(a -> a.getRemainingCapacityHours() > 0).count();
        if (overloaded == 0) {
            return "All TA workloads are balanced.";
        }
        return overloaded + " overloaded TA detected.\n" + available + " TAs still have available capacity.\nRedistribution is recommended.";
    }

    public String buildReport() {
        List<WorkloadAnalysis> analyses = analyzeWorkload();
        List<WorkloadRecommendation> recommendations = generateRecommendations();
        long overloadedCount = analyses.stream().filter(a -> a.getStatus() == WorkloadStatus.OVERLOADED || a.getStatus() == WorkloadStatus.HIGH_RISK).count();
        long underusedCount = analyses.stream().filter(a -> a.getRemainingCapacityHours() > WorkloadRules.UNDERUSED_THRESHOLD_HOURS).count();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Workload Statistics ===\n");
        sb.append("Total TAs: ").append(analyses.size()).append("\n");
        sb.append("Overloaded TA count: ").append(overloadedCount).append("\n");
        sb.append("Underused TA count: ").append(underusedCount).append("\n\n");
        sb.append("=== Recommendations ===\n");
        if (recommendations.isEmpty()) {
            sb.append("All TA workloads are balanced.\n");
        } else {
            for (WorkloadRecommendation recommendation : recommendations) {
                sb.append("- ").append(recommendation.toReportLine()).append("\n");
            }
        }
        sb.append("\nOverall balancing status: ").append(recommendations.isEmpty() ? "Balanced" : "Redistribution Recommended");
        return sb.toString();
    }

    public int calculateWeeklyWorkload(String taUserId) {
        String normalizedTaId = ValidationUtil.requireText(taUserId, "TA user ID");
        return applicationRepository.findByTaUserId(normalizedTaId).stream()
                .filter(application -> application.getStatus() == ApplicationStatus.ACCEPTED)
                .map(Application::getJobId)
                .map(jobRepository::findById)
                .flatMap(Optional::stream)
                .mapToInt(Job::getHoursPerWeek)
                .sum();
    }

    public static final class WorkloadAnalysis {
        private final String taUserId;
        private final String taName;
        private final int weeklyWorkloadHours;
        private final int availableHours;
        private final WorkloadStatus status;

        public WorkloadAnalysis(String taUserId, String taName, int weeklyWorkloadHours, int availableHours) {
            this.taUserId = taUserId;
            this.taName = taName;
            this.weeklyWorkloadHours = weeklyWorkloadHours;
            this.availableHours = availableHours;
            this.status = classify(weeklyWorkloadHours, availableHours);
        }

        private WorkloadStatus classify(int assigned, int available) {
            if (assigned <= 0) {
                return WorkloadStatus.UNDERUSED;
            }
            if (assigned > available) {
                return (assigned - available) > 5 ? WorkloadStatus.HIGH_RISK : WorkloadStatus.OVERLOADED;
            }
            int remaining = available - assigned;
            if (remaining > 6) {
                return WorkloadStatus.UNDERUSED;
            }
            if (remaining == 0) {
                return WorkloadStatus.BALANCED;
            }
            return WorkloadStatus.BALANCED;
        }

        public String getTaUserId() {
            return taUserId;
        }

        public String getTaName() {
            return taName;
        }

        public int getWeeklyWorkloadHours() {
            return weeklyWorkloadHours;
        }

        public int getAvailableHours() {
            return availableHours;
        }

        public int getRemainingCapacityHours() {
            return Math.max(0, availableHours - weeklyWorkloadHours);
        }

        public WorkloadStatus getStatus() {
            return status;
        }

        public double getUtilisationRate() {
            if (availableHours <= 0) return weeklyWorkloadHours > 0 ? 1.0 : 0.0;
            return (double) weeklyWorkloadHours / availableHours;
        }

        public String getExplainabilityText() {
            return String.format("%s currently utilizes %.0f%% of available workload.", taName, getUtilisationRate() * 100.0);
        }
    }

    public enum WorkloadRecommendationPriority {
        LOW("Low Priority"), MEDIUM("Medium Priority"), HIGH("High Priority");

        private final String label;

        WorkloadRecommendationPriority(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static final class WorkloadRecommendation {
        private final String overloadedTaId;
        private final String overloadedName;
        private final String targetTaId;
        private final String targetName;
        private final int moveHours;
        private final int overloadHours;
        private final int targetRemainingCapacityHours;
        private final WorkloadRecommendationPriority priority;

        public WorkloadRecommendation(String overloadedTaId, String overloadedName, String targetTaId, String targetName,
                                      int moveHours, int overloadHours, int targetRemainingCapacityHours,
                                      WorkloadRecommendationPriority priority) {
            this.overloadedTaId = overloadedTaId;
            this.overloadedName = overloadedName;
            this.targetTaId = targetTaId;
            this.targetName = targetName;
            this.moveHours = moveHours;
            this.overloadHours = overloadHours;
            this.targetRemainingCapacityHours = targetRemainingCapacityHours;
            this.priority = priority;
        }

        public String getOverloadedName() { return overloadedName; }
        public String getTargetName() { return targetName; }
        public int getMoveHours() { return moveHours; }
        public int getOverloadHours() { return overloadHours; }
        public int getTargetRemainingCapacityHours() { return targetRemainingCapacityHours; }
        public WorkloadRecommendationPriority getPriority() { return priority; }
        public String toReportLine() { return priority.label() + ": Move " + moveHours + "h/week from " + overloadedName + " to " + targetName; }
    }

    public enum WorkloadStatus {
        BALANCED,
        UNDERUSED,
        OVERLOADED,
        HIGH_RISK
    }
}
