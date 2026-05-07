package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.util.ValidationUtil;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MoApplicantRankingService {
    private static final int RECOMMENDED_MATCH_SCORE = 60;
    private final ApplicationService applicationService;
    private final ApplicantMatchingService matchingService;

    public MoApplicantRankingService(ApplicationService applicationService, ApplicantMatchingService matchingService) {
        this.applicationService = applicationService;
        this.matchingService = matchingService;
    }

    public List<RankedApplicant> rankApplicants(
            Job job,
            List<Application> applications,
            Map<String, User> applicantsById,
            RankingOptions options) {
        if (job == null) {
            return List.of();
        }
        RankingOptions safeOptions = options == null ? RankingOptions.defaultOptions() : options;
        Map<String, User> safeApplicants = applicantsById == null ? Map.of() : applicantsById;

        return applications.stream()
                .filter(Objects::nonNull)
                .filter(application -> !safeOptions.isPendingOnly()
                        || application.getStatus() == ApplicationStatus.PENDING)
                .map(application -> buildRankedApplicant(job, application, safeApplicants.get(application.getTaUserId()),
                        safeOptions.getMinimumMatchScore()))
                .filter(Objects::nonNull)
                .filter(applicant -> applicant.getMatchScore() >= safeOptions.getMinimumMatchScore())
                .sorted(safeOptions.getSortMode().getComparator())
                .toList();
    }

    private RankedApplicant buildRankedApplicant(
            Job job,
            Application application,
            User applicant,
            int minimumMatchScore) {
        if (application == null || applicant == null) {
            return null;
        }

        ApplicantMatchingService.MatchDetails matchDetails =
                matchingService.match(applicant.getSkills(), job.getRequiredSkills());
        int currentWorkload = applicationService.getAcceptedWorkloadHoursForTa(applicant.getId());
        boolean exceedsAvailability = applicant.getAvailableHours() > 0
                && currentWorkload + job.getHoursPerWeek() > applicant.getAvailableHours();
        boolean recommended = matchDetails.getScore() >= Math.max(minimumMatchScore, RECOMMENDED_MATCH_SCORE)
                && !exceedsAvailability;

        return new RankedApplicant(
                application.getId(),
                applicant.getId(),
                applicant.getName(),
                applicant.getYearOfStudy(),
                matchDetails.getScore(),
                matchDetails.getMissingSkills(),
                currentWorkload,
                recommended,
                application.getStatus());
    }

    public enum SortMode {
        MATCH_SCORE_DESC(Comparator.comparingInt(RankedApplicant::getMatchScore).reversed()
                .thenComparingInt(RankedApplicant::getCurrentWorkload)
                .thenComparing(RankedApplicant::getApplicantName, String.CASE_INSENSITIVE_ORDER)),
        WORKLOAD_ASC(Comparator.comparingInt(RankedApplicant::getCurrentWorkload)
                .thenComparing(Comparator.comparingInt(RankedApplicant::getMatchScore).reversed())
                .thenComparing(RankedApplicant::getApplicantName, String.CASE_INSENSITIVE_ORDER));

        private final Comparator<RankedApplicant> comparator;

        SortMode(Comparator<RankedApplicant> comparator) {
            this.comparator = comparator;
        }

        public Comparator<RankedApplicant> getComparator() {
            return comparator;
        }
    }

    public static final class RankingOptions {
        private final boolean pendingOnly;
        private final int minimumMatchScore;
        private final SortMode sortMode;

        public RankingOptions(boolean pendingOnly, int minimumMatchScore, SortMode sortMode) {
            this.pendingOnly = pendingOnly;
            this.minimumMatchScore = ValidationUtil.parseIntInRange(
                    String.valueOf(minimumMatchScore), "Minimum match score", 0, 100);
            this.sortMode = sortMode == null ? SortMode.MATCH_SCORE_DESC : sortMode;
        }

        public static RankingOptions defaultOptions() {
            return new RankingOptions(true, 0, SortMode.MATCH_SCORE_DESC);
        }

        public boolean isPendingOnly() {
            return pendingOnly;
        }

        public int getMinimumMatchScore() {
            return minimumMatchScore;
        }

        public SortMode getSortMode() {
            return sortMode;
        }
    }

    public static final class RankedApplicant {
        private final String applicationId;
        private final String applicantId;
        private final String applicantName;
        private final int yearOfStudy;
        private final int matchScore;
        private final List<String> missingSkills;
        private final int currentWorkload;
        private final boolean recommended;
        private final ApplicationStatus status;

        public RankedApplicant(
                String applicationId,
                String applicantId,
                String applicantName,
                int yearOfStudy,
                int matchScore,
                List<String> missingSkills,
                int currentWorkload,
                boolean recommended,
                ApplicationStatus status) {
            this.applicationId = ValidationUtil.requireText(applicationId, "Application ID");
            this.applicantId = ValidationUtil.requireText(applicantId, "Applicant ID");
            this.applicantName = ValidationUtil.requireText(applicantName, "Applicant name");
            this.yearOfStudy = yearOfStudy;
            this.matchScore = ValidationUtil.parseIntInRange(String.valueOf(matchScore), "Match score", 0, 100);
            this.missingSkills = List.copyOf(missingSkills == null ? List.of() : missingSkills);
            this.currentWorkload = Math.max(0, currentWorkload);
            this.recommended = recommended;
            this.status = status == null ? ApplicationStatus.PENDING : status;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getApplicantId() {
            return applicantId;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public int getYearOfStudy() {
            return yearOfStudy;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public String getMissingSkillsText() {
            return missingSkills.isEmpty() ? "None" : String.join(", ", missingSkills);
        }

        public int getCurrentWorkload() {
            return currentWorkload;
        }

        public boolean isRecommended() {
            return recommended;
        }

        public String getRecommendationLabel() {
            return recommended ? "Recommended" : "Not Recommended";
        }

        public ApplicationStatus getStatus() {
            return status;
        }
    }
}
