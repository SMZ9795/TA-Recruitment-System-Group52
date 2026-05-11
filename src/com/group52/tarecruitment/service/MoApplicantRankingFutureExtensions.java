package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.JobStatus;
import java.util.List;
import java.util.Objects;

/**
 * Currently unused backup/future extension logic for the MO applicant ranking feature.
 *
 * <p>This class is intentionally not wired into Swing, services, repositories, tests, or main
 * methods. It is reserved for a later iteration or demo extension and must not change current
 * runtime behavior.
 */
public final class MoApplicantRankingFutureExtensions {
    private static final int STRONG_MATCH_SCORE = 80;
    private static final int MODERATE_MATCH_SCORE = 60;
    private static final int HIGH_MATCH_NOTIFICATION_SCORE = 85;
    private static final int MEDIUM_WORKLOAD_HOURS = 10;
    private static final int HIGH_WORKLOAD_HOURS = 15;
    private static final int MANY_PENDING_APPLICATIONS = 5;
    private static final int PRIORITY_MATCH_WEIGHT = 50;
    private static final int PRIORITY_PENDING_STATUS_BONUS = 25;
    private static final int PRIORITY_REVIEWING_STATUS_BONUS = 20;
    private static final int PRIORITY_APPLIED_STATUS_BONUS = 18;
    private static final int PRIORITY_STRONG_MATCH_BONUS = 10;
    private static final int PRIORITY_ALMOST_FILLED_JOB_BONUS = 12;
    private static final int PRIORITY_FILLED_JOB_PENALTY = 30;
    private static final int PRIORITY_HIGH_WORKLOAD_PENALTY = 8;

    private MoApplicantRankingFutureExtensions() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String buildRichExplanation(
            MoApplicantRankingService.RankedApplicant applicant,
            FutureFilterConfig filterConfig) {
        if (applicant == null) {
            return "No applicant selected.";
        }

        FutureFilterConfig safeConfig = filterConfig == null
                ? FutureFilterConfig.defaultConfig()
                : filterConfig;
        StringBuilder explanation = new StringBuilder();
        explanation.append("Applicant: ")
                .append(applicant.getApplicantName())
                .append(" (")
                .append(applicant.getApplicantId())
                .append(")\n");
        explanation.append("Application ID: ").append(applicant.getApplicationId()).append("\n");
        explanation.append("Status: ").append(applicant.getStatus()).append("\n");
        explanation.append("Match Score: ").append(applicant.getMatchScore()).append("%\n");
        explanation.append("Match Tier: ").append(categorizeMatch(applicant).getLabel()).append("\n");
        explanation.append("Workload Risk: ").append(getWorkloadRiskLabel(applicant.getCurrentWorkload())).append("\n");
        explanation.append("Matched Skills: ").append(applicant.getMatchedSkillsText()).append("\n");
        explanation.append("Missing Skills: ").append(applicant.getMissingSkillsText()).append("\n");
        explanation.append("Recommendation: ").append(applicant.getRecommendationLabel()).append("\n");
        explanation.append("Future Filters: ").append(safeConfig.toSummaryText()).append("\n");

        if (!applicant.getMatchReason().isBlank()) {
            explanation.append("Score Reason: ").append(applicant.getMatchReason()).append("\n");
        }

        explanation.append("\nDemo note: ")
                .append(buildVivaSummaryText(List.of(applicant), safeConfig));
        return explanation.toString();
    }

    public static String buildRecommendationSummary(
            List<MoApplicantRankingService.RankedApplicant> applicants) {
        List<MoApplicantRankingService.RankedApplicant> safeApplicants = safeApplicants(applicants);
        int total = safeApplicants.size();
        int recommended = (int) safeApplicants.stream()
                .filter(MoApplicantRankingService.RankedApplicant::isRecommended)
                .count();
        int strong = (int) safeApplicants.stream()
                .filter(applicant -> categorizeMatch(applicant) == MatchTier.STRONG_MATCH)
                .count();
        int moderate = (int) safeApplicants.stream()
                .filter(applicant -> categorizeMatch(applicant) == MatchTier.MODERATE_MATCH)
                .count();
        int weak = total - strong - moderate;

        return "Total applicants: " + total
                + "\nRecommended: " + recommended
                + "\nNot recommended: " + (total - recommended)
                + "\nStrong match: " + strong
                + "\nModerate match: " + moderate
                + "\nWeak match: " + weak;
    }

    /**
     * Future-use priority score for sorting the MO review queue.
     *
     * <p>This method is intentionally not called by the current application. It accepts job filling
     * values as parameters because the current ranked applicant object does not own job capacity
     * information.
     */
    public static int calculateReviewPriorityScore(
            MoApplicantRankingService.RankedApplicant applicant,
            int acceptedApplicantsForJob,
            int jobPositions) {
        if (applicant == null) {
            return 0;
        }
        return calculateReviewPriorityScore(
                applicant.getMatchScore(),
                applicant.getStatus(),
                applicant.getCurrentWorkload(),
                acceptedApplicantsForJob,
                jobPositions);
    }

    /**
     * Future-use priority score using only primitive values and enum state.
     *
     * <p>The score is capped to 0..100 so a future caller can sort or display it safely without
     * extra normalization.
     */
    public static int calculateReviewPriorityScore(
            int matchScore,
            ApplicationStatus status,
            int currentWorkloadHours,
            int acceptedApplicantsForJob,
            int jobPositions) {
        int safeMatchScore = normalizeMatchScore(matchScore);
        int score = Math.round(safeMatchScore * (PRIORITY_MATCH_WEIGHT / 100.0f));

        if (status == ApplicationStatus.PENDING) {
            score += PRIORITY_PENDING_STATUS_BONUS;
        } else if (status == ApplicationStatus.REVIEWING) {
            score += PRIORITY_REVIEWING_STATUS_BONUS;
        } else if (status == ApplicationStatus.APPLIED) {
            score += PRIORITY_APPLIED_STATUS_BONUS;
        }

        if (isStrongMatch(safeMatchScore)) {
            score += PRIORITY_STRONG_MATCH_BONUS;
        }
        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)) {
            score += PRIORITY_ALMOST_FILLED_JOB_BONUS;
        }
        if (isJobFilled(acceptedApplicantsForJob, jobPositions)) {
            score -= PRIORITY_FILLED_JOB_PENALTY;
        }
        if (currentWorkloadHours >= HIGH_WORKLOAD_HOURS) {
            score -= PRIORITY_HIGH_WORKLOAD_PENALTY;
        }

        return clampToRange(score, 0, 100);
    }

    public static NotificationSeverity classifyNotificationSeverity(
            int pendingApplications,
            int highestPendingMatchScore,
            int acceptedApplicantsForJob,
            int jobPositions,
            JobStatus jobStatus) {
        int safePendingApplications = Math.max(0, pendingApplications);
        int safeHighestPendingMatchScore = normalizeMatchScore(highestPendingMatchScore);

        if (isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicantsForJob, jobPositions)) {
            return NotificationSeverity.CRITICAL;
        }
        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)) {
            return NotificationSeverity.HIGH;
        }
        if (safePendingApplications >= MANY_PENDING_APPLICATIONS) {
            return NotificationSeverity.HIGH;
        }
        if (safePendingApplications > 0 && safeHighestPendingMatchScore >= HIGH_MATCH_NOTIFICATION_SCORE) {
            return NotificationSeverity.HIGH;
        }
        if (safePendingApplications > 0) {
            return NotificationSeverity.MEDIUM;
        }
        return NotificationSeverity.LOW;
    }

    public static String buildApplicantRecommendationMessage(
            int matchScore,
            ApplicationStatus status,
            int currentWorkloadHours) {
        int safeMatchScore = normalizeMatchScore(matchScore);
        if (!needsDecision(status)) {
            return "Low priority: this application has already been reviewed.";
        }
        if (isStrongMatch(safeMatchScore) && currentWorkloadHours < HIGH_WORKLOAD_HOURS) {
            return "High priority: this applicant has a strong match and still needs a decision.";
        }
        if (safeMatchScore >= MODERATE_MATCH_SCORE) {
            return "Medium priority: this applicant is pending but the match score is moderate.";
        }
        if (currentWorkloadHours >= HIGH_WORKLOAD_HOURS) {
            return "Low priority: this applicant needs review, but their current workload may be high.";
        }
        return "Medium priority: this applicant still needs an MO decision.";
    }

    public static String buildJobRecommendationMessage(
            int pendingApplications,
            int acceptedApplicantsForJob,
            int jobPositions,
            JobStatus jobStatus) {
        if (isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicantsForJob, jobPositions)) {
            return "Filled: this job already has enough accepted applicants.";
        }
        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)) {
            return "Urgent: this job is close to being filled.";
        }
        if (pendingApplications >= MANY_PENDING_APPLICATIONS) {
            return "High priority: this job has many pending applications waiting for review.";
        }
        if (pendingApplications > 0) {
            return "Medium priority: this job has pending applications waiting for review.";
        }
        return "No action needed: this job has no pending applications right now.";
    }

    public static ReviewSummary summarizeFutureReviewItems(List<FutureReviewItem> items) {
        List<FutureReviewItem> safeItems = safeFutureReviewItems(items);
        int pendingApplications = 0;
        int acceptedApplications = 0;
        int rejectedApplications = 0;
        int reviewableApplications = 0;
        int filledJobs = 0;
        int highMatchPendingApplications = 0;

        for (FutureReviewItem item : safeItems) {
            if (item.getStatus() == ApplicationStatus.PENDING) {
                pendingApplications++;
                if (item.getMatchScore() >= STRONG_MATCH_SCORE) {
                    highMatchPendingApplications++;
                }
            }
            if (item.getStatus() == ApplicationStatus.ACCEPTED) {
                acceptedApplications++;
            }
            if (item.getStatus() == ApplicationStatus.REJECTED) {
                rejectedApplications++;
            }
            if (needsDecision(item.getStatus())) {
                reviewableApplications++;
            }
            if (item.isFilledJobReminder()) {
                filledJobs++;
            }
        }

        return new ReviewSummary(
                safeItems.size(),
                pendingApplications,
                acceptedApplications,
                rejectedApplications,
                reviewableApplications,
                filledJobs,
                highMatchPendingApplications);
    }

    public static MatchTier categorizeMatch(MoApplicantRankingService.RankedApplicant applicant) {
        if (applicant == null) {
            return MatchTier.WEAK_MATCH;
        }
        int score = normalizeMatchScore(applicant.getMatchScore());
        if (score >= STRONG_MATCH_SCORE) {
            return MatchTier.STRONG_MATCH;
        }
        if (score >= MODERATE_MATCH_SCORE) {
            return MatchTier.MODERATE_MATCH;
        }
        return MatchTier.WEAK_MATCH;
    }

    public static String toCsvExport(List<MoApplicantRankingService.RankedApplicant> applicants) {
        StringBuilder csv = new StringBuilder();
        csv.append("Application ID,Applicant ID,Applicant Name,Year,Match Score,Match Tier,")
                .append("Matched Skills,Missing Skills,Current Workload,Workload Risk,Recommendation,Status\n");

        for (MoApplicantRankingService.RankedApplicant applicant : safeApplicants(applicants)) {
            csv.append(csvValue(applicant.getApplicationId())).append(",");
            csv.append(csvValue(applicant.getApplicantId())).append(",");
            csv.append(csvValue(applicant.getApplicantName())).append(",");
            csv.append(applicant.getYearOfStudy()).append(",");
            csv.append(applicant.getMatchScore()).append(",");
            csv.append(csvValue(categorizeMatch(applicant).getLabel())).append(",");
            csv.append(csvValue(applicant.getMatchedSkillsText())).append(",");
            csv.append(csvValue(applicant.getMissingSkillsText())).append(",");
            csv.append(applicant.getCurrentWorkload()).append(",");
            csv.append(csvValue(getWorkloadRiskLabel(applicant.getCurrentWorkload()))).append(",");
            csv.append(csvValue(applicant.getRecommendationLabel())).append(",");
            csv.append(csvValue(String.valueOf(applicant.getStatus()))).append("\n");
        }

        return csv.toString();
    }

    public static String getWorkloadRiskLabel(int currentWorkloadHours) {
        int safeHours = Math.max(0, currentWorkloadHours);
        if (safeHours >= HIGH_WORKLOAD_HOURS) {
            return WorkloadRisk.HIGH.getLabel();
        }
        if (safeHours >= MEDIUM_WORKLOAD_HOURS) {
            return WorkloadRisk.MEDIUM.getLabel();
        }
        return WorkloadRisk.LOW.getLabel();
    }

    public static FutureFilterConfig createFutureFilterConfig(
            boolean pendingOnly,
            int minimumMatchScore,
            MoApplicantRankingService.SortMode sortMode,
            boolean hideHighWorkloadApplicants,
            MatchTier minimumTier) {
        return new FutureFilterConfig(
                new MoApplicantRankingService.RankingOptions(pendingOnly, minimumMatchScore, sortMode),
                hideHighWorkloadApplicants,
                minimumTier == null ? MatchTier.WEAK_MATCH : minimumTier);
    }

    public static String buildVivaSummaryText(
            List<MoApplicantRankingService.RankedApplicant> applicants,
            FutureFilterConfig filterConfig) {
        List<MoApplicantRankingService.RankedApplicant> safeApplicants = safeApplicants(applicants);
        FutureFilterConfig safeConfig = filterConfig == null
                ? FutureFilterConfig.defaultConfig()
                : filterConfig;
        long recommendedCount = safeApplicants.stream()
                .filter(MoApplicantRankingService.RankedApplicant::isRecommended)
                .count();
        long highWorkloadCount = safeApplicants.stream()
                .filter(applicant -> WorkloadRisk.HIGH.getLabel().equals(
                        getWorkloadRiskLabel(applicant.getCurrentWorkload())))
                .count();

        return "The future ranking extension can explain "
                + safeApplicants.size()
                + " ranked applicants, highlight "
                + recommendedCount
                + " recommended candidates, flag "
                + highWorkloadCount
                + " high-workload risks, and describe the active future filter plan as: "
                + safeConfig.toSummaryText();
    }

    private static List<MoApplicantRankingService.RankedApplicant> safeApplicants(
            List<MoApplicantRankingService.RankedApplicant> applicants) {
        if (applicants == null || applicants.isEmpty()) {
            return List.of();
        }
        return applicants.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<FutureReviewItem> safeFutureReviewItems(List<FutureReviewItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static int normalizeMatchScore(int matchScore) {
        return clampToRange(matchScore, 0, 100);
    }

    private static int clampToRange(int value, int minimum, int maximum) {
        if (value < minimum) {
            return minimum;
        }
        if (value > maximum) {
            return maximum;
        }
        return value;
    }

    private static boolean isStrongMatch(int matchScore) {
        return normalizeMatchScore(matchScore) >= STRONG_MATCH_SCORE;
    }

    private static boolean needsDecision(ApplicationStatus status) {
        return status == ApplicationStatus.APPLIED
                || status == ApplicationStatus.REVIEWING
                || status == ApplicationStatus.PENDING;
    }

    private static boolean isJobFilledStatus(JobStatus status) {
        return status == JobStatus.FILLED;
    }

    private static boolean isJobFilled(int acceptedApplicantsForJob, int jobPositions) {
        int safePositions = Math.max(0, jobPositions);
        if (safePositions == 0) {
            return false;
        }
        return Math.max(0, acceptedApplicantsForJob) >= safePositions;
    }

    private static boolean isJobAlmostFilled(int acceptedApplicantsForJob, int jobPositions) {
        int safePositions = Math.max(0, jobPositions);
        int safeAccepted = Math.max(0, acceptedApplicantsForJob);
        if (safePositions <= 1) {
            return safePositions == 1 && safeAccepted == 0;
        }
        return safeAccepted >= safePositions - 1 && safeAccepted < safePositions;
    }

    private static String readableStatusLabel(ApplicationStatus status) {
        if (status == null) {
            return "Unknown";
        }
        String raw = status.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private static String csvValue(String value) {
        String safeValue = value == null ? "" : value;
        boolean mustQuote = safeValue.contains(",")
                || safeValue.contains("\"")
                || safeValue.contains("\n")
                || safeValue.contains("\r");
        String escaped = safeValue.replace("\"", "\"\"");
        return mustQuote ? "\"" + escaped + "\"" : escaped;
    }

    public enum MatchTier {
        STRONG_MATCH("Strong Match"),
        MODERATE_MATCH("Moderate Match"),
        WEAK_MATCH("Weak Match");

        private final String label;

        MatchTier(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum WorkloadRisk {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High");

        private final String label;

        WorkloadRisk(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum NotificationSeverity {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        CRITICAL("Critical");

        private final String label;

        NotificationSeverity(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Future-use lightweight item for summarising MO review queues without depending on UI tables.
     */
    public static final class FutureReviewItem {
        private final ApplicationStatus status;
        private final int matchScore;
        private final int currentWorkloadHours;
        private final boolean filledJobReminder;

        public FutureReviewItem(
                ApplicationStatus status,
                int matchScore,
                int currentWorkloadHours,
                boolean filledJobReminder) {
            this.status = status;
            this.matchScore = normalizeMatchScore(matchScore);
            this.currentWorkloadHours = Math.max(0, currentWorkloadHours);
            this.filledJobReminder = filledJobReminder;
        }

        public ApplicationStatus getStatus() {
            return status;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public int getCurrentWorkloadHours() {
            return currentWorkloadHours;
        }

        public boolean isFilledJobReminder() {
            return filledJobReminder;
        }

        public String toSummaryText() {
            return "status=" + readableStatusLabel(status)
                    + ", matchScore=" + matchScore
                    + "%, workload=" + currentWorkloadHours
                    + "h/week, filledJobReminder=" + filledJobReminder;
        }
    }

    public static final class ReviewSummary {
        private final int totalApplications;
        private final int pendingApplications;
        private final int acceptedApplications;
        private final int rejectedApplications;
        private final int reviewableApplications;
        private final int filledJobs;
        private final int highMatchPendingApplications;

        private ReviewSummary(
                int totalApplications,
                int pendingApplications,
                int acceptedApplications,
                int rejectedApplications,
                int reviewableApplications,
                int filledJobs,
                int highMatchPendingApplications) {
            this.totalApplications = Math.max(0, totalApplications);
            this.pendingApplications = Math.max(0, pendingApplications);
            this.acceptedApplications = Math.max(0, acceptedApplications);
            this.rejectedApplications = Math.max(0, rejectedApplications);
            this.reviewableApplications = Math.max(0, reviewableApplications);
            this.filledJobs = Math.max(0, filledJobs);
            this.highMatchPendingApplications = Math.max(0, highMatchPendingApplications);
        }

        public int getTotalApplications() {
            return totalApplications;
        }

        public int getPendingApplications() {
            return pendingApplications;
        }

        public int getAcceptedApplications() {
            return acceptedApplications;
        }

        public int getRejectedApplications() {
            return rejectedApplications;
        }

        public int getReviewableApplications() {
            return reviewableApplications;
        }

        public int getFilledJobs() {
            return filledJobs;
        }

        public int getHighMatchPendingApplications() {
            return highMatchPendingApplications;
        }

        public String toReadableText() {
            return "Total applications: " + totalApplications
                    + "\nPending applications: " + pendingApplications
                    + "\nAccepted applications: " + acceptedApplications
                    + "\nRejected applications: " + rejectedApplications
                    + "\nReviewable applications: " + reviewableApplications
                    + "\nFilled jobs: " + filledJobs
                    + "\nHigh-match pending applications: " + highMatchPendingApplications;
        }
    }

    public static final class FutureFilterConfig {
        private final MoApplicantRankingService.RankingOptions rankingOptions;
        private final boolean hideHighWorkloadApplicants;
        private final MatchTier minimumTier;
        private final boolean needsDecisionOnly;
        private final boolean highMatchFirst;
        private final boolean includeReviewedApplications;
        private final boolean includeFilledJobs;

        private FutureFilterConfig(
                MoApplicantRankingService.RankingOptions rankingOptions,
                boolean hideHighWorkloadApplicants,
                MatchTier minimumTier) {
            this(rankingOptions, hideHighWorkloadApplicants, minimumTier, false, true, true, true);
        }

        private FutureFilterConfig(
                MoApplicantRankingService.RankingOptions rankingOptions,
                boolean hideHighWorkloadApplicants,
                MatchTier minimumTier,
                boolean needsDecisionOnly,
                boolean highMatchFirst,
                boolean includeReviewedApplications,
                boolean includeFilledJobs) {
            this.rankingOptions = rankingOptions == null
                    ? MoApplicantRankingService.RankingOptions.defaultOptions()
                    : rankingOptions;
            this.hideHighWorkloadApplicants = hideHighWorkloadApplicants;
            this.minimumTier = minimumTier == null ? MatchTier.WEAK_MATCH : minimumTier;
            this.needsDecisionOnly = needsDecisionOnly;
            this.highMatchFirst = highMatchFirst;
            this.includeReviewedApplications = includeReviewedApplications;
            this.includeFilledJobs = includeFilledJobs;
        }

        public static FutureFilterConfig defaultConfig() {
            return new FutureFilterConfig(
                    MoApplicantRankingService.RankingOptions.defaultOptions(),
                    false,
                    MatchTier.WEAK_MATCH,
                    false,
                    true,
                    true,
                    true);
        }

        public static FutureFilterConfig forFutureReviewFilters(
                boolean pendingOnly,
                boolean needsDecisionOnly,
                boolean highMatchFirst,
                int minimumMatchScore,
                boolean includeReviewedApplications,
                boolean includeFilledJobs) {
            MoApplicantRankingService.SortMode sortMode = highMatchFirst
                    ? MoApplicantRankingService.SortMode.MATCH_SCORE_DESC
                    : MoApplicantRankingService.SortMode.WORKLOAD_ASC;
            MoApplicantRankingService.RankingOptions rankingOptions =
                    new MoApplicantRankingService.RankingOptions(pendingOnly, minimumMatchScore, sortMode);
            return new FutureFilterConfig(
                    rankingOptions,
                    false,
                    MatchTier.WEAK_MATCH,
                    needsDecisionOnly,
                    highMatchFirst,
                    includeReviewedApplications,
                    includeFilledJobs);
        }

        public MoApplicantRankingService.RankingOptions getRankingOptions() {
            return rankingOptions;
        }

        public boolean isHideHighWorkloadApplicants() {
            return hideHighWorkloadApplicants;
        }

        public MatchTier getMinimumTier() {
            return minimumTier;
        }

        public boolean isPendingOnly() {
            return rankingOptions.isPendingOnly();
        }

        public boolean isNeedsDecisionOnly() {
            return needsDecisionOnly;
        }

        public boolean isHighMatchFirst() {
            return highMatchFirst;
        }

        public int getMinimumMatchScore() {
            return rankingOptions.getMinimumMatchScore();
        }

        public boolean isIncludeReviewedApplications() {
            return includeReviewedApplications;
        }

        public boolean isIncludeFilledJobs() {
            return includeFilledJobs;
        }

        public String toSummaryText() {
            return "pendingOnly=" + rankingOptions.isPendingOnly()
                    + ", needsDecisionOnly=" + needsDecisionOnly
                    + ", highMatchFirst=" + highMatchFirst
                    + ", minimumMatchScore=" + rankingOptions.getMinimumMatchScore()
                    + ", sortMode=" + rankingOptions.getSortMode()
                    + ", hideHighWorkloadApplicants=" + hideHighWorkloadApplicants
                    + ", minimumTier=" + minimumTier.getLabel()
                    + ", includeReviewedApplications=" + includeReviewedApplications
                    + ", includeFilledJobs=" + includeFilledJobs;
        }
    }
}
