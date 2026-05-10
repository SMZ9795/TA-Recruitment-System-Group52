package com.group52.tarecruitment.service;

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
    private static final int MEDIUM_WORKLOAD_HOURS = 10;
    private static final int HIGH_WORKLOAD_HOURS = 15;

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

    public static MatchTier categorizeMatch(MoApplicantRankingService.RankedApplicant applicant) {
        if (applicant == null) {
            return MatchTier.WEAK_MATCH;
        }
        int score = applicant.getMatchScore();
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

    public static final class FutureFilterConfig {
        private final MoApplicantRankingService.RankingOptions rankingOptions;
        private final boolean hideHighWorkloadApplicants;
        private final MatchTier minimumTier;

        private FutureFilterConfig(
                MoApplicantRankingService.RankingOptions rankingOptions,
                boolean hideHighWorkloadApplicants,
                MatchTier minimumTier) {
            this.rankingOptions = rankingOptions == null
                    ? MoApplicantRankingService.RankingOptions.defaultOptions()
                    : rankingOptions;
            this.hideHighWorkloadApplicants = hideHighWorkloadApplicants;
            this.minimumTier = minimumTier == null ? MatchTier.WEAK_MATCH : minimumTier;
        }

        public static FutureFilterConfig defaultConfig() {
            return new FutureFilterConfig(
                    MoApplicantRankingService.RankingOptions.defaultOptions(),
                    false,
                    MatchTier.WEAK_MATCH);
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

        public String toSummaryText() {
            return "pendingOnly=" + rankingOptions.isPendingOnly()
                    + ", minimumMatchScore=" + rankingOptions.getMinimumMatchScore()
                    + ", sortMode=" + rankingOptions.getSortMode()
                    + ", hideHighWorkloadApplicants=" + hideHighWorkloadApplicants
                    + ", minimumTier=" + minimumTier.getLabel();
        }
    }
}
