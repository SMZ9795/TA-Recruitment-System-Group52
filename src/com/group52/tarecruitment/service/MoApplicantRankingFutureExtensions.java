package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.JobStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    private static final int LOW_MATCH_SCORE = 40;
    private static final int HIGH_MATCH_NOTIFICATION_SCORE = 85;
    private static final int MEDIUM_WORKLOAD_HOURS = 10;
    private static final int HIGH_WORKLOAD_HOURS = 15;
    private static final int DEFAULT_WAITING_DAYS_ATTENTION_THRESHOLD = 7;
    private static final int LONG_WAITING_DAYS_THRESHOLD = 14;
    private static final int CLOSE_TO_FILLED_PERCENT = 80;
    private static final int MANY_PENDING_APPLICATIONS = 5;
    private static final int MANY_REVIEWABLE_APPLICATIONS = 8;
    private static final int PRIORITY_MATCH_WEIGHT = 50;
    private static final int PRIORITY_PENDING_STATUS_BONUS = 25;
    private static final int PRIORITY_REVIEWING_STATUS_BONUS = 20;
    private static final int PRIORITY_APPLIED_STATUS_BONUS = 18;
    private static final int PRIORITY_STRONG_MATCH_BONUS = 10;
    private static final int PRIORITY_ALMOST_FILLED_JOB_BONUS = 12;
    private static final int PRIORITY_FILLED_JOB_PENALTY = 30;
    private static final int PRIORITY_HIGH_WORKLOAD_PENALTY = 8;
    private static final int DECISION_STRONGLY_REVIEW_SCORE = 75;
    private static final int DECISION_REVIEW_SCORE = 45;
    private static final int DECISION_LOW_PRIORITY_SCORE = 20;
    private static final int JOB_URGENCY_FILLED_POINTS = 35;
    private static final int JOB_URGENCY_CLOSE_TO_FILLED_POINTS = 25;
    private static final int JOB_URGENCY_PENDING_POINTS = 4;
    private static final int JOB_URGENCY_REVIEWABLE_POINTS = 3;
    private static final int JOB_URGENCY_HIGH_MATCH_POINTS = 15;
    private static final int RANKING_TREND_STABLE_DELTA = 2;
    private static final int SOME_MATCHED_SKILLS = 2;
    private static final int MANY_MISSING_SKILLS = 3;
    private static final int CRITICAL_MISSING_SKILLS = 5;
    private static final int EXTREME_WORKLOAD_HOURS = 22;
    private static final int URGENT_REVIEW_SCORE = 75;
    private static final int EXCELLENT_QUALITY_SCORE = 85;
    private static final int GOOD_QUALITY_SCORE = 70;
    private static final int FAIR_QUALITY_SCORE = 50;
    private static final int WEAK_QUALITY_SCORE = 30;
    private static final int LOW_CONFIDENCE_SCORE = 45;
    private static final int HIGH_CONFIDENCE_SCORE = 75;

    private MoApplicantRankingFutureExtensions() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Future-use helper for comparing ranking scores between two ranking runs.
     *
     * <p>This is not currently integrated into the MO workflow. A later dashboard could use it to
     * show whether a candidate moved up, moved down, or stayed broadly stable after job criteria or
     * applicant details changed.
     */
    public static RankingTrend classifyRankingTrend(Integer previousScore, Integer currentScore) {
        if (previousScore == null || currentScore == null || previousScore < 0 || currentScore < 0) {
            return RankingTrend.UNKNOWN;
        }

        int difference = normalizeMatchScore(currentScore) - normalizeMatchScore(previousScore);
        if (difference > RANKING_TREND_STABLE_DELTA) {
            return RankingTrend.IMPROVED;
        }
        if (difference < -RANKING_TREND_STABLE_DELTA) {
            return RankingTrend.DECLINED;
        }
        return RankingTrend.STABLE;
    }

    /**
     * Primitive overload for future callers that already have validated score values.
     */
    public static RankingTrend classifyRankingTrend(int previousScore, int currentScore) {
        return classifyRankingTrend(Integer.valueOf(previousScore), Integer.valueOf(currentScore));
    }

    /**
     * Future-use review priority recommendation based on a compact candidate snapshot.
     *
     * <p>The result is intentionally advisory and deterministic. It is not connected to current
     * services, repositories, Swing screens, or hiring decisions.
     */
    public static ReviewPriority recommendReviewPriority(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return ReviewPriority.SKIP_FOR_NOW;
        }
        return recommendReviewPriority(
                candidate.getMatchScore(),
                candidate.getMissingSkillsCount(),
                candidate.getCurrentWorkloadHours(),
                candidate.getStatus());
    }

    /**
     * Future-use review priority recommendation using primitive values.
     */
    public static ReviewPriority recommendReviewPriority(
            int matchScore,
            int missingSkillsCount,
            int currentWorkloadHours,
            ApplicationStatus status) {
        if (!needsDecision(status)) {
            return ReviewPriority.SKIP_FOR_NOW;
        }

        int safeMatchScore = normalizeMatchScore(matchScore);
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        int safeWorkload = Math.max(0, currentWorkloadHours);

        if (safeMatchScore >= STRONG_MATCH_SCORE
                && safeMissingSkills <= 1
                && safeWorkload < HIGH_WORKLOAD_HOURS) {
            return ReviewPriority.URGENT;
        }
        if (safeWorkload >= HIGH_WORKLOAD_HOURS
                || safeMissingSkills >= CRITICAL_MISSING_SKILLS
                || safeMatchScore < LOW_MATCH_SCORE) {
            return ReviewPriority.LATER;
        }
        if (safeMatchScore >= MODERATE_MATCH_SCORE || safeMissingSkills <= MANY_MISSING_SKILLS) {
            return ReviewPriority.NORMAL;
        }
        return ReviewPriority.LATER;
    }

    /**
     * Future-use risk label generator for an MO review screen.
     *
     * <p>These flags are deliberately human-readable strings so a later UI can display them without
     * having to know this helper's enum details. This method is currently not wired into production
     * code.
     */
    public static List<String> identifyApplicantRiskFlags(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return List.of("Incomplete applicant information");
        }
        return identifyApplicantRiskFlags(
                candidate.getApplicationId(),
                candidate.getApplicantName(),
                candidate.getMatchScore(),
                candidate.getMissingSkillsCount(),
                candidate.getCurrentWorkloadHours(),
                candidate.getStatus());
    }

    /**
     * Future-use risk label generator using primitive values.
     */
    public static List<String> identifyApplicantRiskFlags(
            String applicationId,
            String applicantName,
            int matchScore,
            int missingSkillsCount,
            int currentWorkloadHours,
            ApplicationStatus status) {
        List<String> flags = new ArrayList<>();
        int safeMatchScore = normalizeMatchScore(matchScore);
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        int safeWorkload = Math.max(0, currentWorkloadHours);

        if (applicationId == null || applicationId.isBlank()
                || applicantName == null || applicantName.isBlank()) {
            flags.add("Incomplete applicant information");
        }
        if (safeMatchScore < LOW_MATCH_SCORE) {
            flags.add("Low match score");
        }
        if (safeMissingSkills >= MANY_MISSING_SKILLS) {
            flags.add("Many missing skills");
        }
        if (safeWorkload >= HIGH_WORKLOAD_HOURS) {
            flags.add("High current workload");
        }
        if (status == null) {
            flags.add("Unknown application status");
        } else if (!needsDecision(status)) {
            flags.add("Status not pending or reviewable: " + readableStatusLabel(status));
        }
        if (flags.isEmpty()) {
            flags.add("No major future risk flags");
        }
        return List.copyOf(flags);
    }

    /**
     * Future-use risk level summary that groups the human-readable flags into a coarse severity.
     */
    public static CandidateRiskLevel classifyCandidateRiskLevel(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return CandidateRiskLevel.HIGH;
        }
        if (!needsDecision(candidate.getStatus())) {
            return CandidateRiskLevel.NOT_APPLICABLE;
        }
        int safeMatchScore = candidate.getMatchScore();
        int safeMissingSkills = candidate.getMissingSkillsCount();
        int safeWorkload = candidate.getCurrentWorkloadHours();

        if (safeMatchScore < LOW_MATCH_SCORE
                || safeMissingSkills >= CRITICAL_MISSING_SKILLS
                || safeWorkload >= HIGH_WORKLOAD_HOURS) {
            return CandidateRiskLevel.HIGH;
        }
        if (safeMatchScore < MODERATE_MATCH_SCORE
                || safeMissingSkills >= MANY_MISSING_SKILLS
                || safeWorkload >= MEDIUM_WORKLOAD_HOURS) {
            return CandidateRiskLevel.MEDIUM;
        }
        return CandidateRiskLevel.LOW;
    }

    /**
     * Future-use interview recommendation text for MO-facing review notes.
     *
     * <p>This method intentionally returns short deterministic text and is not used by the current
     * ranking workflow.
     */
    public static String buildInterviewRecommendation(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "Not applicable - applicant information is incomplete";
        }
        return buildInterviewRecommendation(
                candidate.getMatchScore(),
                candidate.getMissingSkillsCount(),
                candidate.getCurrentWorkloadHours(),
                candidate.getStatus());
    }

    /**
     * Future-use interview recommendation text using primitive values.
     */
    public static String buildInterviewRecommendation(
            int matchScore,
            int missingSkillsCount,
            int currentWorkloadHours,
            ApplicationStatus status) {
        if (!needsDecision(status)) {
            return "Not applicable - application is not pending";
        }

        int safeMatchScore = normalizeMatchScore(matchScore);
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        int safeWorkload = Math.max(0, currentWorkloadHours);

        if (safeMatchScore >= STRONG_MATCH_SCORE
                && safeMissingSkills <= 1
                && safeWorkload < HIGH_WORKLOAD_HOURS) {
            return "Strong candidate - schedule interview";
        }
        if (safeMatchScore >= MODERATE_MATCH_SCORE && safeMissingSkills <= MANY_MISSING_SKILLS) {
            return "Potential candidate - review missing skills";
        }
        return "Weak fit - consider rejection";
    }

    /**
     * Future-use batch summary for candidate-like records.
     *
     * <p>The current application does not call this method. It is reserved for a later MO dashboard
     * or export panel that needs aggregate statistics without touching repositories.
     */
    public static FutureRankingSummary summarizeFutureCandidates(List<FutureCandidateSnapshot> candidates) {
        List<FutureCandidateSnapshot> safeCandidates = safeFutureCandidateSnapshots(candidates);
        if (safeCandidates.isEmpty()) {
            return new FutureRankingSummary(0, 0, 0, 0, 0.0, 0, 0);
        }

        int strongCandidates = 0;
        int mediumCandidates = 0;
        int weakCandidates = 0;
        int totalScore = 0;
        int highestScore = 0;
        int lowestScore = 100;

        for (FutureCandidateSnapshot candidate : safeCandidates) {
            int score = candidate.getMatchScore();
            MatchTier tier = matchTierFromScore(score);
            if (tier == MatchTier.STRONG_MATCH) {
                strongCandidates++;
            } else if (tier == MatchTier.MODERATE_MATCH) {
                mediumCandidates++;
            } else {
                weakCandidates++;
            }
            totalScore += score;
            highestScore = Math.max(highestScore, score);
            lowestScore = Math.min(lowestScore, score);
        }

        double averageScore = totalScore / (double) safeCandidates.size();
        return new FutureRankingSummary(
                safeCandidates.size(),
                strongCandidates,
                mediumCandidates,
                weakCandidates,
                averageScore,
                highestScore,
                lowestScore);
    }

    /**
     * Future-use explanation template for MO users.
     *
     * <p>This combines score level, skill quality, missing-skill concern, workload concern, status
     * concern, risk level, and a final recommendation. It is intentionally kept out of the current
     * workflow until the team decides to expose richer explanations.
     */
    public static String buildFutureMoExplanation(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "No future explanation can be built because candidate information is missing.";
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Candidate: ")
                .append(safeLabel(candidate.getApplicantName(), "Unknown applicant"))
                .append("\n");
        explanation.append("Application ID: ")
                .append(safeLabel(candidate.getApplicationId(), "Unknown application"))
                .append("\n");
        explanation.append("Score level: ")
                .append(buildFutureScoreLevelText(candidate.getMatchScore()))
                .append("\n");
        explanation.append("Matched skill quality: ")
                .append(buildFutureMatchedSkillQualityText(candidate.getMatchedSkillsCount()))
                .append("\n");
        explanation.append("Missing skill concern: ")
                .append(buildFutureMissingSkillConcernText(candidate.getMissingSkillsCount()))
                .append("\n");
        explanation.append("Workload concern: ")
                .append(buildFutureWorkloadConcernText(candidate.getCurrentWorkloadHours()))
                .append("\n");
        explanation.append("Status concern: ")
                .append(buildFutureStatusConcernText(candidate.getStatus()))
                .append("\n");
        explanation.append("Risk level: ")
                .append(classifyCandidateRiskLevel(candidate).getLabel())
                .append("\n");
        explanation.append("Final recommendation: ")
                .append(buildInterviewRecommendation(candidate));
        return explanation.toString();
    }

    /**
     * Future-use score-level text for richer MO explanations.
     */
    public static String buildFutureScoreLevelText(int matchScore) {
        MatchTier tier = matchTierFromScore(matchScore);
        if (tier == MatchTier.STRONG_MATCH) {
            return "Strong score; applicant appears to match most ranking criteria.";
        }
        if (tier == MatchTier.MODERATE_MATCH) {
            return "Medium score; applicant may be suitable after closer MO review.";
        }
        return "Weak score; applicant may not fit the current job requirements.";
    }

    /**
     * Future-use matched-skill text for richer MO explanations.
     */
    public static String buildFutureMatchedSkillQualityText(int matchedSkillsCount) {
        int safeMatchedSkills = Math.max(0, matchedSkillsCount);
        if (safeMatchedSkills >= SOME_MATCHED_SKILLS) {
            return "Matched skills are present and should be checked for relevance.";
        }
        if (safeMatchedSkills == 1) {
            return "Only one matched skill is visible, so the match may be narrow.";
        }
        return "No matched skills are visible in the future snapshot.";
    }

    /**
     * Future-use missing-skill text for richer MO explanations.
     */
    public static String buildFutureMissingSkillConcernText(int missingSkillsCount) {
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        if (safeMissingSkills >= CRITICAL_MISSING_SKILLS) {
            return "Critical concern; many required skills appear to be missing.";
        }
        if (safeMissingSkills >= MANY_MISSING_SKILLS) {
            return "Moderate concern; several required skills need review.";
        }
        if (safeMissingSkills > 0) {
            return "Low concern; a small number of skills may need follow-up.";
        }
        return "No missing skills are recorded in the future snapshot.";
    }

    /**
     * Future-use workload text for richer MO explanations.
     */
    public static String buildFutureWorkloadConcernText(int currentWorkloadHours) {
        int safeWorkload = Math.max(0, currentWorkloadHours);
        if (safeWorkload >= HIGH_WORKLOAD_HOURS) {
            return "High concern; applicant may be overloaded.";
        }
        if (safeWorkload >= MEDIUM_WORKLOAD_HOURS) {
            return "Medium concern; workload should be checked before assignment.";
        }
        return "Low concern; workload does not currently block review.";
    }

    /**
     * Future-use status text for richer MO explanations.
     */
    public static String buildFutureStatusConcernText(ApplicationStatus status) {
        if (status == null) {
            return "Unknown status; MO should verify the application state first.";
        }
        if (needsDecision(status)) {
            return readableStatusLabel(status) + " status can still be reviewed by an MO.";
        }
        return readableStatusLabel(status) + " status is not pending review.";
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

    /**
     * Future-use attention flag detector for a single applicant row.
     *
     * <p>The current production UI builds its own simple reminders. This richer result is left here
     * so a later dashboard can explain why an applicant row deserves MO attention without adding
     * more logic to Swing components.
     */
    public static List<ApplicantAttentionFlag> identifyApplicantAttentionFlags(
            FutureApplicantSignal signal) {
        if (signal == null) {
            return List.of(ApplicantAttentionFlag.NO_SIGNAL_DATA);
        }
        return identifyApplicantAttentionFlags(
                signal.getMatchScore(),
                signal.getStatus(),
                signal.getCurrentWorkloadHours(),
                signal.getAcceptedApplicantsForJob(),
                signal.getJobPositions(),
                signal.getWaitingDays(),
                DEFAULT_WAITING_DAYS_ATTENTION_THRESHOLD,
                signal.getJobStatus());
    }

    /**
     * Primitive-value overload for future callers that do not have a FutureApplicantSignal object.
     */
    public static List<ApplicantAttentionFlag> identifyApplicantAttentionFlags(
            int matchScore,
            ApplicationStatus status,
            int currentWorkloadHours,
            int acceptedApplicantsForJob,
            int jobPositions,
            int waitingDays,
            int waitingDaysAttentionThreshold,
            JobStatus jobStatus) {
        List<ApplicantAttentionFlag> flags = new ArrayList<>();
        int safeMatchScore = normalizeMatchScore(matchScore);
        int safeWorkload = Math.max(0, currentWorkloadHours);
        int safeWaitingDays = Math.max(0, waitingDays);
        int safeWaitingThreshold = Math.max(1, waitingDaysAttentionThreshold);
        boolean needsDecision = needsDecision(status);
        boolean jobFilled = isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicantsForJob, jobPositions);

        if (needsDecision && safeMatchScore >= STRONG_MATCH_SCORE) {
            flags.add(ApplicantAttentionFlag.HIGH_MATCH_STILL_PENDING);
        }
        if (needsDecision && safeMatchScore < LOW_MATCH_SCORE) {
            flags.add(ApplicantAttentionFlag.LOW_MATCH_STILL_PENDING);
        }
        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)) {
            flags.add(ApplicantAttentionFlag.JOB_ALMOST_FILLED);
        }
        if (jobFilled) {
            flags.add(ApplicantAttentionFlag.JOB_ALREADY_FILLED);
        }
        if (safeWorkload >= HIGH_WORKLOAD_HOURS) {
            flags.add(ApplicantAttentionFlag.APPLICANT_POSSIBLY_OVERLOADED);
        }
        if (needsDecision && safeWaitingDays >= safeWaitingThreshold) {
            flags.add(ApplicantAttentionFlag.APPLICATION_WAITING_TOO_LONG);
        }
        if (!needsDecision) {
            flags.add(ApplicantAttentionFlag.APPLICATION_DOES_NOT_NEED_ACTION);
        }
        if (flags.isEmpty()) {
            flags.add(ApplicantAttentionFlag.NORMAL_REVIEW_ITEM);
        }
        return List.copyOf(flags);
    }

    public static DecisionSuggestion suggestFutureDecision(FutureApplicantSignal signal) {
        if (signal == null) {
            return DecisionSuggestion.NO_ACTION_NEEDED;
        }
        return suggestFutureDecision(
                signal.getMatchScore(),
                signal.getStatus(),
                signal.getCurrentWorkloadHours(),
                signal.getAcceptedApplicantsForJob(),
                signal.getJobPositions(),
                signal.getWaitingDays(),
                signal.getJobStatus());
    }

    /**
     * Future-use decision suggestion that stays deliberately simple and auditable.
     *
     * <p>This is not a hiring decision engine. It produces a coarse queue suggestion so a future MO
     * dashboard could sort or label applicants while leaving the actual decision to the MO.
     */
    public static DecisionSuggestion suggestFutureDecision(
            int matchScore,
            ApplicationStatus status,
            int currentWorkloadHours,
            int acceptedApplicantsForJob,
            int jobPositions,
            int waitingDays,
            JobStatus jobStatus) {
        if (!needsDecision(status)) {
            return DecisionSuggestion.NO_ACTION_NEEDED;
        }
        if (isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicantsForJob, jobPositions)) {
            return DecisionSuggestion.NO_ACTION_NEEDED;
        }

        int score = 0;
        int safeMatchScore = normalizeMatchScore(matchScore);
        int safeWorkload = Math.max(0, currentWorkloadHours);
        int safeWaitingDays = Math.max(0, waitingDays);

        if (safeMatchScore >= STRONG_MATCH_SCORE) {
            score += 45;
        } else if (safeMatchScore >= MODERATE_MATCH_SCORE) {
            score += 30;
        } else if (safeMatchScore >= LOW_MATCH_SCORE) {
            score += 15;
        } else {
            score += 5;
        }

        if (status == ApplicationStatus.PENDING) {
            score += 20;
        } else if (status == ApplicationStatus.REVIEWING) {
            score += 18;
        } else if (status == ApplicationStatus.APPLIED) {
            score += 15;
        }

        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)) {
            score += 12;
        }
        if (safeWaitingDays >= LONG_WAITING_DAYS_THRESHOLD) {
            score += 12;
        } else if (safeWaitingDays >= DEFAULT_WAITING_DAYS_ATTENTION_THRESHOLD) {
            score += 6;
        }
        if (safeWorkload >= HIGH_WORKLOAD_HOURS) {
            score -= 15;
        } else if (safeWorkload >= MEDIUM_WORKLOAD_HOURS) {
            score -= 5;
        }

        if (score >= DECISION_STRONGLY_REVIEW_SCORE) {
            return DecisionSuggestion.STRONGLY_REVIEW;
        }
        if (score >= DECISION_REVIEW_SCORE) {
            return DecisionSuggestion.REVIEW;
        }
        if (score >= DECISION_LOW_PRIORITY_SCORE) {
            return DecisionSuggestion.LOW_PRIORITY;
        }
        return DecisionSuggestion.NO_ACTION_NEEDED;
    }

    public static String buildApplicantPriorityExplanation(FutureApplicantSignal signal) {
        if (signal == null) {
            return "No applicant signal is available, so no future priority explanation can be built.";
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Applicant: ").append(safeLabel(signal.getApplicantName(), "Unknown applicant")).append("\n");
        explanation.append("Application ID: ").append(safeLabel(signal.getApplicationId(), "Unknown application")).append("\n");
        explanation.append("Decision state: ").append(readableApplicantDecisionState(signal.getStatus())).append("\n");
        explanation.append("Review suggestion: ").append(suggestFutureDecision(signal).getLabel()).append("\n");
        explanation.append("Match quality: ").append(readableMatchQualityLevel(signal.getMatchScore())).append("\n");
        explanation.append("Workload: ").append(buildWorkloadExplanation(signal.getCurrentWorkloadHours())).append("\n");
        explanation.append("Job fill state: ")
                .append(readableJobFillState(signal.getAcceptedApplicantsForJob(), signal.getJobPositions(),
                        signal.getJobStatus()))
                .append("\n");
        explanation.append("Waiting time: ").append(Math.max(0, signal.getWaitingDays())).append(" day(s).\n");
        explanation.append("Attention flags: ")
                .append(readableAttentionFlags(identifyApplicantAttentionFlags(signal)))
                .append("\n\n");
        explanation.append(buildMatchScoreExplanation(signal.getMatchScore()));
        return explanation.toString();
    }

    public static String buildJobAttentionExplanation(FutureJobDashboardItem item) {
        if (item == null) {
            return "No job summary is available, so no future job attention explanation can be built.";
        }

        StringBuilder explanation = new StringBuilder();
        explanation.append("Job: ").append(item.getJobLabel()).append("\n");
        explanation.append("Fill state: ")
                .append(readableJobFillState(item.getAcceptedApplicants(), item.getPositions(), item.getJobStatus()))
                .append("\n");
        explanation.append("Pending applications: ").append(item.getPendingApplications()).append("\n");
        explanation.append("Applicants needing decision: ").append(item.getReviewableApplications()).append("\n");
        explanation.append("Highest pending match score: ").append(item.getHighestPendingMatchScore()).append("%\n");
        explanation.append("Job urgency score: ").append(calculateJobUrgencyScore(item)).append("/100\n");

        if (item.needsAttention()) {
            explanation.append("Why it needs attention: ");
            if (item.isFilled()) {
                explanation.append("the job is already filled and may need status visibility.");
            } else if (item.isCloseToFilled()) {
                explanation.append("the job is close to capacity, so each decision has a stronger effect.");
            } else if (item.getReviewableApplications() >= MANY_REVIEWABLE_APPLICATIONS) {
                explanation.append("many applications still need review.");
            } else {
                explanation.append("there are pending applications waiting for an MO decision.");
            }
        } else {
            explanation.append(buildNoActionNeededExplanation(item));
        }
        return explanation.toString();
    }

    public static String buildNoActionNeededExplanation(FutureJobDashboardItem item) {
        if (item == null) {
            return "No action needed because no job information was provided.";
        }
        if (item.isFilled()) {
            return "No applicant decision is needed because the job is already filled.";
        }
        if (item.getReviewableApplications() == 0 && item.getPendingApplications() == 0) {
            return "No action needed because this job currently has no pending or reviewable applications.";
        }
        return "No immediate action is needed because this job is not close to capacity and has a small review queue.";
    }

    public static String buildMatchScoreExplanation(int matchScore) {
        int safeMatchScore = normalizeMatchScore(matchScore);
        MatchTier tier = matchTierFromScore(safeMatchScore);
        if (tier == MatchTier.STRONG_MATCH) {
            return "Match score matters because a strong score suggests the applicant covers most required skills.";
        }
        if (tier == MatchTier.MODERATE_MATCH) {
            return "Match score matters because this applicant covers some required skills but may need closer review.";
        }
        return "Match score matters because a weak score highlights missing skills that the MO may want to inspect.";
    }

    public static String buildWorkloadExplanation(int currentWorkloadHours) {
        int safeWorkload = Math.max(0, currentWorkloadHours);
        WorkloadRisk risk = workloadRiskFromHours(safeWorkload);
        if (risk == WorkloadRisk.HIGH) {
            return safeWorkload + "h/week, high risk; accepting more work may overload the applicant.";
        }
        if (risk == WorkloadRisk.MEDIUM) {
            return safeWorkload + "h/week, medium risk; workload should be checked before accepting.";
        }
        return safeWorkload + "h/week, low risk; workload alone does not raise a concern.";
    }

    public static FutureDashboardSummary summarizeFutureDashboard(List<FutureJobDashboardItem> jobs) {
        List<FutureJobDashboardItem> safeJobs = safeFutureJobDashboardItems(jobs);
        int jobsNeedingAttention = 0;
        int jobsAlreadyFilled = 0;
        int jobsCloseToFilled = 0;
        int highMatchPendingApplicants = 0;
        int applicantsNeedingDecision = 0;

        for (FutureJobDashboardItem job : safeJobs) {
            if (job.needsAttention()) {
                jobsNeedingAttention++;
            }
            if (job.isFilled()) {
                jobsAlreadyFilled++;
            }
            if (job.isCloseToFilled()) {
                jobsCloseToFilled++;
            }
            if (job.getHighestPendingMatchScore() >= STRONG_MATCH_SCORE) {
                highMatchPendingApplicants += Math.max(0, job.getPendingApplications());
            }
            applicantsNeedingDecision += Math.max(0, job.getReviewableApplications());
        }

        return new FutureDashboardSummary(
                safeJobs.size(),
                jobsNeedingAttention,
                jobsAlreadyFilled,
                jobsCloseToFilled,
                highMatchPendingApplicants,
                applicantsNeedingDecision);
    }

    public static int calculateJobUrgencyScore(FutureJobDashboardItem item) {
        if (item == null) {
            return 0;
        }

        int score = 0;
        if (item.isFilled()) {
            score += JOB_URGENCY_FILLED_POINTS;
        }
        if (item.isCloseToFilled()) {
            score += JOB_URGENCY_CLOSE_TO_FILLED_POINTS;
        }
        score += Math.min(25, Math.max(0, item.getPendingApplications()) * JOB_URGENCY_PENDING_POINTS);
        score += Math.min(25, Math.max(0, item.getReviewableApplications()) * JOB_URGENCY_REVIEWABLE_POINTS);
        if (item.getHighestPendingMatchScore() >= STRONG_MATCH_SCORE) {
            score += JOB_URGENCY_HIGH_MATCH_POINTS;
        }
        return clampToRange(score, 0, 100);
    }

    public static int calculateFillRatioPercent(int acceptedApplicantsForJob, int jobPositions) {
        int safePositions = Math.max(0, jobPositions);
        if (safePositions == 0) {
            return 0;
        }
        int safeAccepted = Math.max(0, acceptedApplicantsForJob);
        return clampToRange((int) Math.round((safeAccepted * 100.0) / safePositions), 0, 100);
    }

    public static JobFillState classifyJobFillState(
            int acceptedApplicantsForJob,
            int jobPositions,
            JobStatus jobStatus) {
        if (isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicantsForJob, jobPositions)) {
            return JobFillState.FILLED;
        }
        if (isJobAlmostFilled(acceptedApplicantsForJob, jobPositions)
                || calculateFillRatioPercent(acceptedApplicantsForJob, jobPositions) >= CLOSE_TO_FILLED_PERCENT) {
            return JobFillState.CLOSE_TO_FILLED;
        }
        if (Math.max(0, acceptedApplicantsForJob) == 0) {
            return JobFillState.EMPTY;
        }
        return JobFillState.IN_PROGRESS;
    }

    public static String readableNotificationSeverity(NotificationSeverity severity) {
        return severity == null ? NotificationSeverity.LOW.getLabel() : severity.getLabel();
    }

    public static String readableReviewPriority(DecisionSuggestion suggestion) {
        return suggestion == null ? DecisionSuggestion.NO_ACTION_NEEDED.getLabel() : suggestion.getLabel();
    }

    public static String readableJobFillState(
            int acceptedApplicantsForJob,
            int jobPositions,
            JobStatus jobStatus) {
        return classifyJobFillState(acceptedApplicantsForJob, jobPositions, jobStatus).getLabel();
    }

    public static String readableApplicantDecisionState(ApplicationStatus status) {
        return decisionStateFromStatus(status).getLabel();
    }

    public static String readableMatchQualityLevel(int matchScore) {
        return matchTierFromScore(matchScore).getLabel();
    }

    public static Comparator<FutureApplicantSignal> reviewPriorityComparator() {
        return MoApplicantRankingFutureExtensions::compareByReviewPriority;
    }

    public static int compareByReviewPriority(FutureApplicantSignal left, FutureApplicantSignal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }

        int leftPriority = calculateReviewPriorityScore(
                left.getMatchScore(),
                left.getStatus(),
                left.getCurrentWorkloadHours(),
                left.getAcceptedApplicantsForJob(),
                left.getJobPositions());
        int rightPriority = calculateReviewPriorityScore(
                right.getMatchScore(),
                right.getStatus(),
                right.getCurrentWorkloadHours(),
                right.getAcceptedApplicantsForJob(),
                right.getJobPositions());

        int priorityCompare = Integer.compare(rightPriority, leftPriority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        int matchCompare = compareByMatchScore(left, right);
        if (matchCompare != 0) {
            return matchCompare;
        }

        int workloadCompare = compareByWorkload(left, right);
        if (workloadCompare != 0) {
            return workloadCompare;
        }

        return Integer.compare(right.getWaitingDays(), left.getWaitingDays());
    }

    public static Comparator<FutureApplicantSignal> matchScoreComparator() {
        return MoApplicantRankingFutureExtensions::compareByMatchScore;
    }

    public static int compareByMatchScore(FutureApplicantSignal left, FutureApplicantSignal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return Integer.compare(right.getMatchScore(), left.getMatchScore());
    }

    public static Comparator<FutureApplicantSignal> workloadComparator() {
        return MoApplicantRankingFutureExtensions::compareByWorkload;
    }

    public static int compareByWorkload(FutureApplicantSignal left, FutureApplicantSignal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return Integer.compare(left.getCurrentWorkloadHours(), right.getCurrentWorkloadHours());
    }

    public static Comparator<FutureJobDashboardItem> jobUrgencyComparator() {
        return MoApplicantRankingFutureExtensions::compareByJobUrgency;
    }

    public static int compareByJobUrgency(FutureJobDashboardItem left, FutureJobDashboardItem right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return Integer.compare(calculateJobUrgencyScore(right), calculateJobUrgencyScore(left));
    }

    public static Comparator<FutureApplicantSignal> decisionNeedComparator() {
        return MoApplicantRankingFutureExtensions::compareByDecisionNeed;
    }

    public static int compareByDecisionNeed(FutureApplicantSignal left, FutureApplicantSignal right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        int leftNeed = decisionNeedWeight(left.getStatus());
        int rightNeed = decisionNeedWeight(right.getStatus());
        if (leftNeed != rightNeed) {
            return Integer.compare(rightNeed, leftNeed);
        }
        return compareByReviewPriority(left, right);
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

    /**
     * Future-only MO review urgency score for a candidate snapshot.
     *
     * <p>This helper is deliberately not wired into production ranking. It combines match score,
     * workload, missing skills, status, risk, and ranking trend into a deterministic 0..100 score
     * that a later review queue could use for display or sorting.
     */
    public static int calculateFutureReviewUrgencyScore(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return 0;
        }
        return calculateFutureReviewUrgencyScore(
                candidate.getMatchScore(),
                candidate.getCurrentWorkloadHours(),
                candidate.getMissingSkillsCount(),
                candidate.getStatus(),
                candidate.getRiskLevel(),
                candidate.getRankingTrend());
    }

    /**
     * Future-only primitive overload for calculating review urgency without needing a snapshot.
     */
    public static int calculateFutureReviewUrgencyScore(
            int matchScore,
            int currentWorkloadHours,
            int missingSkillsCount,
            ApplicationStatus status,
            CandidateRiskLevel riskLevel,
            RankingTrend rankingTrend) {
        int safeScore = normalizeMatchScore(matchScore);
        int safeWorkload = Math.max(0, currentWorkloadHours);
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        int urgency = Math.round(safeScore * 0.45f);

        if (status == ApplicationStatus.PENDING) {
            urgency += 22;
        } else if (status == ApplicationStatus.REVIEWING) {
            urgency += 18;
        } else if (status == ApplicationStatus.APPLIED) {
            urgency += 14;
        } else if (status == null) {
            urgency -= 8;
        } else {
            urgency -= 25;
        }

        urgency -= Math.min(22, safeMissingSkills * 5);

        WorkloadSeverity workloadSeverity = classifyWorkloadSeverity(safeWorkload);
        if (workloadSeverity == WorkloadSeverity.NONE || workloadSeverity == WorkloadSeverity.LOW) {
            urgency += 6;
        } else if (workloadSeverity == WorkloadSeverity.MEDIUM) {
            urgency += 2;
        } else if (workloadSeverity == WorkloadSeverity.HIGH) {
            urgency -= 8;
        } else if (workloadSeverity == WorkloadSeverity.EXTREME) {
            urgency -= 18;
        }

        if (riskLevel == CandidateRiskLevel.LOW) {
            urgency += 8;
        } else if (riskLevel == CandidateRiskLevel.MEDIUM) {
            urgency -= 8;
        } else if (riskLevel == CandidateRiskLevel.HIGH) {
            urgency -= 24;
        } else if (riskLevel == CandidateRiskLevel.NOT_APPLICABLE) {
            urgency -= 18;
        }

        if (rankingTrend == RankingTrend.IMPROVED) {
            urgency += 8;
        } else if (rankingTrend == RankingTrend.DECLINED) {
            urgency -= 10;
        } else if (rankingTrend == RankingTrend.STABLE) {
            urgency += 2;
        }

        if (!needsDecision(status)) {
            urgency = Math.min(urgency, 30);
        }
        return clampToRange(urgency, 0, 100);
    }

    /**
     * Future-only grouping helper for candidate snapshots.
     */
    public static CandidateGroup groupCandidateSnapshot(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return CandidateGroup.UNKNOWN;
        }
        if (!needsDecision(candidate.getStatus())) {
            return CandidateGroup.NOT_PENDING;
        }
        if (candidate.getRiskLevel() == CandidateRiskLevel.HIGH) {
            return CandidateGroup.HIGH_RISK;
        }
        if (candidate.getMatchScore() >= STRONG_MATCH_SCORE
                && candidate.getMissingSkillsCount() <= 1) {
            return CandidateGroup.STRONG_FIT;
        }
        if (candidate.getMatchScore() >= MODERATE_MATCH_SCORE
                && candidate.getMissingSkillsCount() <= MANY_MISSING_SKILLS) {
            return CandidateGroup.POSSIBLE_FIT;
        }
        if (candidate.getMatchScore() < MODERATE_MATCH_SCORE
                || candidate.getMissingSkillsCount() >= CRITICAL_MISSING_SKILLS) {
            return CandidateGroup.WEAK_FIT;
        }
        return CandidateGroup.UNKNOWN;
    }

    /**
     * Future-only batch grouping helper. The returned map and lists are new objects.
     */
    public static Map<CandidateGroup, List<FutureCandidateSnapshot>> groupCandidateSnapshots(
            List<FutureCandidateSnapshot> candidates) {
        Map<CandidateGroup, List<FutureCandidateSnapshot>> grouped = new EnumMap<>(CandidateGroup.class);
        for (CandidateGroup group : CandidateGroup.values()) {
            grouped.put(group, new ArrayList<>());
        }
        for (FutureCandidateSnapshot candidate : safeFutureCandidateSnapshots(candidates)) {
            grouped.get(groupCandidateSnapshot(candidate)).add(candidate);
        }

        Map<CandidateGroup, List<FutureCandidateSnapshot>> copy = new EnumMap<>(CandidateGroup.class);
        for (Map.Entry<CandidateGroup, List<FutureCandidateSnapshot>> entry : grouped.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    /**
     * Future-only readable group label for dashboards or CSV previews.
     */
    public static String readableCandidateGroup(CandidateGroup group) {
        return group == null ? CandidateGroup.UNKNOWN.getLabel() : group.getLabel();
    }

    /**
     * Future-only comparator for resolving ranking ties without mutating candidate snapshots.
     */
    public static Comparator<FutureCandidateSnapshot> candidateTieBreakerComparator() {
        return MoApplicantRankingFutureExtensions::compareCandidateTieBreakers;
    }

    /**
     * Future-only tie breaker ordered by score, workload, missing skills, status, risk, then name/id.
     */
    public static int compareCandidateTieBreakers(
            FutureCandidateSnapshot left,
            FutureCandidateSnapshot right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }

        int scoreComparison = Integer.compare(right.getMatchScore(), left.getMatchScore());
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        int workloadComparison = Integer.compare(left.getCurrentWorkloadHours(), right.getCurrentWorkloadHours());
        if (workloadComparison != 0) {
            return workloadComparison;
        }
        int missingSkillComparison = Integer.compare(left.getMissingSkillsCount(), right.getMissingSkillsCount());
        if (missingSkillComparison != 0) {
            return missingSkillComparison;
        }
        int statusComparison = Integer.compare(statusTieBreakerWeight(left.getStatus()), statusTieBreakerWeight(right.getStatus()));
        if (statusComparison != 0) {
            return statusComparison;
        }
        int riskComparison = Integer.compare(riskTieBreakerWeight(left.getRiskLevel()), riskTieBreakerWeight(right.getRiskLevel()));
        if (riskComparison != 0) {
            return riskComparison;
        }

        int nameComparison = normalizedSortText(left.getApplicantName())
                .compareTo(normalizedSortText(right.getApplicantName()));
        if (nameComparison != 0) {
            return nameComparison;
        }
        return normalizedSortText(left.getApplicationId()).compareTo(normalizedSortText(right.getApplicationId()));
    }

    /**
     * Future-only workload severity classifier.
     */
    public static WorkloadSeverity classifyWorkloadSeverity(int currentWorkloadHours) {
        if (currentWorkloadHours < 0) {
            return WorkloadSeverity.UNKNOWN;
        }
        if (currentWorkloadHours == 0) {
            return WorkloadSeverity.NONE;
        }
        if (currentWorkloadHours < MEDIUM_WORKLOAD_HOURS) {
            return WorkloadSeverity.LOW;
        }
        if (currentWorkloadHours < HIGH_WORKLOAD_HOURS) {
            return WorkloadSeverity.MEDIUM;
        }
        if (currentWorkloadHours < EXTREME_WORKLOAD_HOURS) {
            return WorkloadSeverity.HIGH;
        }
        return WorkloadSeverity.EXTREME;
    }

    /**
     * Future-only workload severity text for MO review explanations.
     */
    public static String explainWorkloadSeverity(WorkloadSeverity severity) {
        if (severity == WorkloadSeverity.NONE) {
            return "No recorded workload; the candidate appears available in this future snapshot.";
        }
        if (severity == WorkloadSeverity.LOW) {
            return "Low workload; the candidate is unlikely to be overloaded.";
        }
        if (severity == WorkloadSeverity.MEDIUM) {
            return "Medium workload; confirm availability before assigning additional TA work.";
        }
        if (severity == WorkloadSeverity.HIGH) {
            return "High workload; review carefully before adding more responsibilities.";
        }
        if (severity == WorkloadSeverity.EXTREME) {
            return "Extreme workload; the candidate should usually be treated as overloaded.";
        }
        return "Unknown workload; verify workload data before making a decision.";
    }

    /**
     * Future-only overload detector based on the snapshot workload.
     */
    public static boolean isCandidateOverloaded(FutureCandidateSnapshot candidate) {
        return candidate != null && isCandidateOverloaded(candidate.getCurrentWorkloadHours());
    }

    /**
     * Future-only overload detector using workload hours.
     */
    public static boolean isCandidateOverloaded(int currentWorkloadHours) {
        WorkloadSeverity severity = classifyWorkloadSeverity(currentWorkloadHours);
        return severity == WorkloadSeverity.HIGH || severity == WorkloadSeverity.EXTREME;
    }

    /**
     * Future-only shortlist helper. The original list and snapshots are never modified.
     */
    public static List<FutureCandidateSnapshot> shortlistTopCandidateSnapshots(
            List<FutureCandidateSnapshot> candidates,
            int maximumShortlistSize,
            int minimumScoreThreshold) {
        int safeLimit = Math.max(0, maximumShortlistSize);
        if (safeLimit == 0) {
            return List.of();
        }

        int safeThreshold = normalizeMatchScore(minimumScoreThreshold);
        List<FutureCandidateSnapshot> safeCandidates = safeFutureCandidateSnapshots(candidates);
        List<FutureCandidateSnapshot> qualified = safeCandidates.stream()
                .filter(candidate -> candidate.getMatchScore() >= safeThreshold)
                .filter(candidate -> needsDecision(candidate.getStatus()))
                .filter(candidate -> candidate.getRiskLevel() != CandidateRiskLevel.HIGH)
                .sorted(candidateTieBreakerComparator())
                .toList();

        if (qualified.isEmpty()) {
            qualified = safeCandidates.stream()
                    .filter(candidate -> candidate.getMatchScore() >= safeThreshold)
                    .sorted(candidateTieBreakerComparator())
                    .toList();
        }
        if (qualified.isEmpty()) {
            qualified = safeCandidates.stream()
                    .sorted(candidateTieBreakerComparator())
                    .toList();
        }
        return qualified.stream()
                .limit(safeLimit)
                .toList();
    }

    /**
     * Future-only plain-text digest builder. It does not connect to the real notification system.
     */
    public static String buildFutureMoNotificationDigest(List<FutureCandidateSnapshot> candidates) {
        List<FutureCandidateSnapshot> safeCandidates = safeFutureCandidateSnapshots(candidates);
        int pendingCandidates = 0;
        int urgentCandidates = 0;
        int strongCandidates = 0;
        int riskyCandidates = 0;

        for (FutureCandidateSnapshot candidate : safeCandidates) {
            if (needsDecision(candidate.getStatus())) {
                pendingCandidates++;
            }
            if (calculateFutureReviewUrgencyScore(candidate) >= URGENT_REVIEW_SCORE) {
                urgentCandidates++;
            }
            if (groupCandidateSnapshot(candidate) == CandidateGroup.STRONG_FIT) {
                strongCandidates++;
            }
            if (candidate.getRiskLevel() == CandidateRiskLevel.HIGH) {
                riskyCandidates++;
            }
        }

        FutureCandidateSnapshot topCandidate = shortlistTopCandidateSnapshots(safeCandidates, 1, 0)
                .stream()
                .findFirst()
                .orElse(null);
        String suggestedAction;
        if (urgentCandidates > 0) {
            suggestedAction = "Review urgent candidates first.";
        } else if (strongCandidates > 0) {
            suggestedAction = "Start with the strongest pending fit.";
        } else if (pendingCandidates > 0) {
            suggestedAction = "Continue normal pending review.";
        } else {
            suggestedAction = "No pending candidate action is suggested.";
        }

        return "Future MO applicant digest"
                + "\nTotal pending candidates: " + pendingCandidates
                + "\nUrgent candidates: " + urgentCandidates
                + "\nStrong candidates: " + strongCandidates
                + "\nRisky candidates: " + riskyCandidates
                + "\nTop recommended candidate: " + candidateDigestName(topCandidate)
                + "\nSuggested next action: " + suggestedAction;
    }

    /**
     * Future-only one-sentence candidate explanation.
     */
    public static String buildShortCandidateExplanation(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "No candidate explanation is available because the future snapshot is missing.";
        }
        return candidateDisplayName(candidate)
                + " has a " + candidate.getMatchScore() + "% score, "
                + readableStatusLabel(candidate.getStatus()).toLowerCase()
                + " status, " + candidate.getRiskLevel().getLabel().toLowerCase()
                + ", and " + recommendReviewPriority(candidate).getLabel().toLowerCase() + ".";
    }

    /**
     * Future-only medium candidate explanation in three to five sentences.
     */
    public static String buildMediumCandidateExplanation(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "No candidate explanation is available. The future snapshot is missing. Verify candidate data before review.";
        }
        String recommendation = buildInterviewRecommendation(candidate);
        return candidateDisplayName(candidate) + " currently has a "
                + candidate.getMatchScore() + "% match score and "
                + readableStatusLabel(candidate.getStatus()).toLowerCase() + " status. "
                + "Risk is " + candidate.getRiskLevel().getLabel().toLowerCase()
                + ", workload is " + classifyWorkloadSeverity(candidate.getCurrentWorkloadHours()).getLabel().toLowerCase()
                + ", and " + candidate.getMissingSkillsCount() + " missing skills are recorded. "
                + "The ranking trend is " + candidate.getRankingTrend().getLabel().toLowerCase() + ". "
                + "Recommendation: " + recommendation + ".";
    }

    /**
     * Future-only detailed candidate explanation as plain multi-line text.
     */
    public static String buildDetailedCandidateExplanation(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "Candidate explanation\nStatus: Missing future snapshot\nRecommendation: Verify candidate data.";
        }
        WorkloadSeverity severity = classifyWorkloadSeverity(candidate.getCurrentWorkloadHours());
        return "Candidate explanation"
                + "\nCandidate: " + candidateDisplayName(candidate)
                + "\nApplication ID: " + safeLabel(candidate.getApplicationId(), "Unknown application")
                + "\nScore: " + candidate.getMatchScore() + "%"
                + "\nStatus: " + readableStatusLabel(candidate.getStatus())
                + "\nRisk: " + candidate.getRiskLevel().getLabel()
                + "\nWorkload: " + candidate.getCurrentWorkloadHours() + "h/week (" + severity.getLabel() + ")"
                + "\nMissing skills: " + candidate.getMissingSkillsCount()
                + "\nMatched skills: " + candidate.getMatchedSkillsCount()
                + "\nTrend: " + candidate.getRankingTrend().getLabel()
                + "\nGroup: " + readableCandidateGroup(groupCandidateSnapshot(candidate))
                + "\nUrgency score: " + calculateFutureReviewUrgencyScore(candidate)
                + "\nWorkload note: " + explainWorkloadSeverity(severity)
                + "\nRecommendation: " + buildInterviewRecommendation(candidate);
    }

    /**
     * Future-only audit entry factory. This method only returns an object; it never writes files or
     * databases.
     */
    public static FutureReviewAuditEntry createFutureReviewAuditEntry(
            String candidateId,
            String actionType,
            ReviewPriority previousPriority,
            ReviewPriority newPriority,
            String reason,
            String timestampText) {
        return new FutureReviewAuditEntry(
                safeLabel(candidateId, "Unknown candidate"),
                safeLabel(actionType, "Unspecified action"),
                previousPriority == null ? ReviewPriority.SKIP_FOR_NOW : previousPriority,
                newPriority == null ? ReviewPriority.SKIP_FOR_NOW : newPriority,
                safeLabel(reason, "No reason recorded"),
                safeLabel(timestampText, "Unknown timestamp"));
    }

    /**
     * Future-only plain-text formatter for audit entries.
     */
    public static String formatFutureReviewAuditEntry(FutureReviewAuditEntry entry) {
        if (entry == null) {
            return "Future review audit entry: unavailable";
        }
        return "Future review audit entry"
                + "\nCandidate ID: " + entry.getCandidateId()
                + "\nAction: " + entry.getActionType()
                + "\nPrevious priority: " + entry.getPreviousPriority().getLabel()
                + "\nNew priority: " + entry.getNewPriority().getLabel()
                + "\nReason: " + entry.getReason()
                + "\nTimestamp: " + entry.getTimestampText();
    }

    /**
     * Future-only score parser. Invalid values safely return 0.
     */
    public static int parseFutureScore(String rawScore) {
        Integer parsed = parseFirstInteger(rawScore);
        return parsed == null ? 0 : normalizeMatchScore(parsed);
    }

    /**
     * Future-only workload parser. Invalid values safely return 0.
     */
    public static int parseFutureWorkload(String rawWorkload) {
        Integer parsed = parseFirstInteger(rawWorkload);
        return parsed == null ? 0 : Math.max(0, parsed);
    }

    /**
     * Future-only missing-skill counter for lists of skill names.
     */
    public static int parseFutureMissingSkillCount(List<String> missingSkills) {
        if (missingSkills == null || missingSkills.isEmpty()) {
            return 0;
        }
        return (int) missingSkills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .count();
    }

    /**
     * Future-only missing-skill parser. A number is used directly; otherwise comma-like separators
     * are counted.
     */
    public static int parseFutureMissingSkillCount(String rawMissingSkills) {
        if (rawMissingSkills == null || rawMissingSkills.isBlank()) {
            return 0;
        }
        String trimmed = rawMissingSkills.trim();
        Integer parsed = parseFirstInteger(trimmed);
        if (parsed != null && trimmed.matches(".*\\d.*") && !trimmed.contains(",")) {
            return Math.max(0, parsed);
        }
        String normalized = trimmed.toLowerCase();
        if ("none".equals(normalized)
                || "n/a".equals(normalized)
                || "not applicable".equals(normalized)
                || normalized.contains("no missing")) {
            return 0;
        }
        String[] parts = trimmed.split("[,;|/]+");
        int count = 0;
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                count++;
            }
        }
        return Math.max(0, count);
    }

    /**
     * Future-only application status parser. Invalid values safely return null.
     */
    public static ApplicationStatus parseFutureApplicationStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return null;
        }
        String normalized = rawStatus.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        for (ApplicationStatus status : ApplicationStatus.values()) {
            if (status.name().equals(normalized)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Future-only consistency checker for a candidate snapshot.
     */
    public static List<String> findFutureCandidateConsistencyWarnings(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return List.of("Candidate snapshot is missing.");
        }
        return findFutureCandidateConsistencyWarnings(
                candidate.getApplicationId(),
                candidate.getMatchScore(),
                candidate.getMissingSkillsCount(),
                candidate.getCurrentWorkloadHours(),
                candidate.getStatus(),
                candidate.getReviewPriority(),
                buildInterviewRecommendation(candidate));
    }

    /**
     * Future-only primitive consistency checker for candidate-like values.
     */
    public static List<String> findFutureCandidateConsistencyWarnings(
            String candidateId,
            int matchScore,
            int missingSkillsCount,
            int currentWorkloadHours,
            ApplicationStatus status,
            ReviewPriority reviewPriority,
            String recommendationText) {
        List<String> warnings = new ArrayList<>();
        int safeScore = normalizeMatchScore(matchScore);
        int safeMissing = Math.max(0, missingSkillsCount);
        String safeRecommendation = recommendationText == null ? "" : recommendationText.toLowerCase();

        if (candidateId == null || candidateId.isBlank()) {
            warnings.add("Candidate ID is empty.");
        }
        if (matchScore < 0 || matchScore > 100) {
            warnings.add("Match score was outside the expected 0 to 100 range.");
        }
        if (safeScore >= STRONG_MATCH_SCORE && safeMissing >= CRITICAL_MISSING_SKILLS) {
            warnings.add("High score is inconsistent with many missing skills.");
        }
        if (currentWorkloadHours < 0) {
            warnings.add("Current workload is negative.");
        }
        if (!needsDecision(status) && reviewPriority == ReviewPriority.URGENT) {
            warnings.add("Non-pending status should not have urgent review priority.");
        }
        if ((status == ApplicationStatus.ACCEPTED || status == ApplicationStatus.REJECTED)
                && (safeRecommendation.contains("pending")
                || safeRecommendation.contains("schedule interview")
                || safeRecommendation.contains("review missing"))) {
            warnings.add("Accepted or rejected status conflicts with a pending-review recommendation.");
        }
        if (status == null) {
            warnings.add("Application status is unknown.");
        }
        return List.copyOf(warnings);
    }

    /**
     * Future-only rich profile factory for later MO dashboards.
     *
     * <p>This method deliberately accepts primitive and plain Java values. It does not read models,
     * repositories, services, Swing state, files, or any production workflow. Invalid values are
     * normalized into safe defaults so a future screen can render partial data safely.
     */
    public static FutureCandidateProfile createFutureCandidateProfile(
            String candidateId,
            String applicantName,
            String applicationId,
            String jobId,
            String jobTitle,
            String rawStatus,
            double matchScore,
            Integer previousMatchScore,
            int workload,
            List<String> matchedSkills,
            List<String> missingSkills,
            List<String> riskFlags,
            String recommendationText,
            String notes) {
        int safeScore = normalizeFuturePercentage(matchScore);
        int safePreviousScore = normalizeFuturePreviousScore(previousMatchScore);
        int safeWorkload = Math.max(0, workload);
        ApplicationStatus parsedStatus = parseFutureApplicationStatus(rawStatus);
        List<String> safeMatchedSkills = normalizeFutureTextList(matchedSkills);
        List<String> safeMissingSkills = normalizeFutureTextList(missingSkills);
        List<String> safeRiskFlags = normalizeFutureTextList(riskFlags);

        RankingTrend trend = safePreviousScore < 0
                ? RankingTrend.UNKNOWN
                : classifyRankingTrend(safePreviousScore, safeScore);
        ReviewPriority priority = recommendReviewPriority(
                safeScore,
                safeMissingSkills.size(),
                safeWorkload,
                parsedStatus);
        CandidateGroup group = groupFutureProfileValues(safeScore, safeMissingSkills.size(), safeWorkload, parsedStatus);
        WorkloadSeverity workloadSeverity = classifyWorkloadSeverity(safeWorkload);

        if (safeRiskFlags.isEmpty()) {
            safeRiskFlags = identifyApplicantRiskFlags(
                    applicationId,
                    applicantName,
                    safeScore,
                    safeMissingSkills.size(),
                    safeWorkload,
                    parsedStatus);
        }

        String recommendation = recommendationText == null || recommendationText.isBlank()
                ? buildInterviewRecommendation(safeScore, safeMissingSkills.size(), safeWorkload, parsedStatus)
                : recommendationText.trim();

        return new FutureCandidateProfile(
                safeLabel(candidateId, "UNKNOWN"),
                safeLabel(applicantName, "Unknown applicant"),
                safeLabel(applicationId, "UNKNOWN"),
                safeLabel(jobId, "UNKNOWN"),
                safeLabel(jobTitle, "Unknown job"),
                normalizeFutureStatusLabel(rawStatus),
                safeScore,
                safePreviousScore,
                safeWorkload,
                safeMatchedSkills,
                safeMissingSkills,
                safeRiskFlags,
                trend,
                priority,
                group,
                workloadSeverity,
                recommendation,
                safeLabel(notes, ""));
    }

    /**
     * Future-only convenience factory for code paths that already use an ApplicationStatus enum.
     */
    public static FutureCandidateProfile createFutureCandidateProfile(
            String candidateId,
            String applicantName,
            String applicationId,
            String jobId,
            String jobTitle,
            ApplicationStatus status,
            int matchScore,
            Integer previousMatchScore,
            int workload,
            List<String> matchedSkills,
            List<String> missingSkills,
            List<String> riskFlags,
            String recommendationText,
            String notes) {
        return createFutureCandidateProfile(
                candidateId,
                applicantName,
                applicationId,
                jobId,
                jobTitle,
                status == null ? null : status.name(),
                matchScore,
                previousMatchScore,
                workload,
                matchedSkills,
                missingSkills,
                riskFlags,
                recommendationText,
                notes);
    }

    /**
     * Future-only copier that returns a normalized independent profile object.
     */
    public static FutureCandidateProfile copyFutureCandidateProfile(FutureCandidateProfile profile) {
        if (profile == null) {
            return createFutureCandidateProfile(
                    "", "", "", "", "", (String) null, 0, null, 0, List.of(), List.of(), List.of(), "", "");
        }
        return createFutureCandidateProfile(
                profile.getCandidateId(),
                profile.getApplicantName(),
                profile.getApplicationId(),
                profile.getJobId(),
                profile.getJobTitle(),
                profile.getNormalizedStatus(),
                profile.getMatchScore(),
                profile.getPreviousMatchScore() < 0 ? null : Integer.valueOf(profile.getPreviousMatchScore()),
                profile.getWorkload(),
                profile.getMatchedSkills(),
                profile.getMissingSkills(),
                profile.getRiskFlags(),
                profile.getRecommendationText(),
                profile.getNotes());
    }

    /**
     * Future-only compact display label for possible MO lists or exports.
     */
    public static String buildFutureProfileCompactLabel(FutureCandidateProfile profile) {
        if (profile == null) {
            return "Unknown applicant | UNKNOWN | 0% | Unknown";
        }
        return profile.getApplicantName()
                + " | " + profile.getApplicationId()
                + " | " + profile.getMatchScore() + "%"
                + " | " + profile.getReviewPriority().getLabel();
    }

    /**
     * Future-only detailed display label for a richer candidate profile.
     */
    public static String buildFutureProfileDetailedLabel(FutureCandidateProfile profile) {
        if (profile == null) {
            return "Future candidate profile\nStatus: UNKNOWN\nRecommendation: Not applicable";
        }
        return "Future candidate profile"
                + "\nCandidate: " + profile.getApplicantName() + " (" + profile.getCandidateId() + ")"
                + "\nApplication: " + profile.getApplicationId()
                + "\nJob: " + profile.getJobTitle() + " (" + profile.getJobId() + ")"
                + "\nStatus: " + profile.getNormalizedStatus()
                + "\nScore: " + profile.getMatchScore() + "%"
                + "\nPrevious score: " + formatFuturePreviousScore(profile.getPreviousMatchScore())
                + "\nTrend: " + profile.getRankingTrend().getLabel()
                + "\nWorkload: " + profile.getWorkload() + "h/week (" + profile.getWorkloadSeverity().getLabel() + ")"
                + "\nMatched skills: " + formatFutureList(profile.getMatchedSkills())
                + "\nMissing skills: " + formatFutureList(profile.getMissingSkills())
                + "\nRisk flags: " + formatFutureList(profile.getRiskFlags())
                + "\nGroup: " + profile.getCandidateGroup().getLabel()
                + "\nReview priority: " + profile.getReviewPriority().getLabel()
                + "\nRecommendation: " + profile.getRecommendationText()
                + "\nNotes: " + safeLabel(profile.getNotes(), "No notes");
    }

    /**
     * Future-only reviewability check. Unknown or completed statuses safely return false.
     */
    public static boolean isFutureProfileReviewable(FutureCandidateProfile profile) {
        return profile != null && needsDecision(parseFutureApplicationStatus(profile.getNormalizedStatus()));
    }

    /**
     * Future-only strong-profile check based on score, missing skills, workload, and status.
     */
    public static boolean isFutureProfileStrong(FutureCandidateProfile profile) {
        return profile != null
                && isFutureProfileReviewable(profile)
                && profile.getMatchScore() >= STRONG_MATCH_SCORE
                && profile.getMissingSkills().size() <= 1
                && profile.getWorkloadSeverity() != WorkloadSeverity.HIGH
                && profile.getWorkloadSeverity() != WorkloadSeverity.EXTREME;
    }

    /**
     * Future-only risk check for a richer profile.
     */
    public static boolean isFutureProfileRisky(FutureCandidateProfile profile) {
        if (profile == null) {
            return true;
        }
        return profile.getCandidateGroup() == CandidateGroup.HIGH_RISK
                || profile.getRiskFlags().stream().anyMatch(flag -> !"No major future risk flags".equalsIgnoreCase(flag))
                || profile.getMatchScore() < LOW_MATCH_SCORE
                || profile.getMissingSkills().size() >= CRITICAL_MISSING_SKILLS
                || profile.getWorkloadSeverity() == WorkloadSeverity.HIGH
                || profile.getWorkloadSeverity() == WorkloadSeverity.EXTREME;
    }

    /**
     * Future-only completeness check for possible dashboard warnings.
     */
    public static boolean hasFutureProfileIncompleteInformation(FutureCandidateProfile profile) {
        if (profile == null) {
            return true;
        }
        return isUnknownLabel(profile.getCandidateId())
                || isUnknownLabel(profile.getApplicantName())
                || isUnknownLabel(profile.getApplicationId())
                || isUnknownLabel(profile.getJobId())
                || isUnknownLabel(profile.getJobTitle())
                || "UNKNOWN".equalsIgnoreCase(profile.getNormalizedStatus())
                || (profile.getMatchedSkills().isEmpty() && profile.getMissingSkills().isEmpty());
    }

    /**
     * Future-only stage suggestion. It never mutates workflow state.
     */
    public static FutureReviewStage suggestNextReviewStage(
            FutureCandidateProfile profile,
            FutureReviewStage currentStage) {
        FutureReviewStage safeStage = currentStage == null ? FutureReviewStage.UNKNOWN : currentStage;
        if (profile == null) {
            return FutureReviewStage.BLOCKED;
        }
        if (!isFutureProfileReviewable(profile)) {
            return FutureReviewStage.COMPLETED;
        }
        if (hasFutureProfileIncompleteInformation(profile)) {
            return FutureReviewStage.BLOCKED;
        }
        if (safeStage == FutureReviewStage.NOT_STARTED || safeStage == FutureReviewStage.UNKNOWN) {
            return FutureReviewStage.INITIAL_SCREENING;
        }
        if (safeStage == FutureReviewStage.INITIAL_SCREENING) {
            return profile.getMissingSkills().isEmpty()
                    ? FutureReviewStage.WORKLOAD_REVIEW
                    : FutureReviewStage.SKILL_REVIEW;
        }
        if (safeStage == FutureReviewStage.SKILL_REVIEW) {
            return FutureReviewStage.WORKLOAD_REVIEW;
        }
        if (safeStage == FutureReviewStage.WORKLOAD_REVIEW) {
            return FutureReviewStage.FINAL_DECISION;
        }
        if (safeStage == FutureReviewStage.FINAL_DECISION) {
            return FutureReviewStage.COMPLETED;
        }
        return safeStage;
    }

    /**
     * Future-only action suggestion for a profile and review stage.
     */
    public static FutureReviewAction suggestNextReviewAction(
            FutureCandidateProfile profile,
            FutureReviewStage currentStage) {
        if (profile == null) {
            return FutureReviewAction.NO_ACTION;
        }
        FutureReviewStage safeStage = currentStage == null ? FutureReviewStage.UNKNOWN : currentStage;
        if (!isFutureProfileReviewable(profile)) {
            return FutureReviewAction.NO_ACTION;
        }
        if (hasFutureProfileIncompleteInformation(profile)) {
            return FutureReviewAction.REQUEST_MORE_INFO;
        }
        if (profile.getWorkloadSeverity() == WorkloadSeverity.EXTREME) {
            return FutureReviewAction.ESCALATE_TO_ADMIN;
        }
        if (safeStage == FutureReviewStage.NOT_STARTED || safeStage == FutureReviewStage.UNKNOWN) {
            return FutureReviewAction.START_REVIEW;
        }
        if (isFutureProfileStrong(profile)) {
            return FutureReviewAction.MARK_FOR_INTERVIEW;
        }
        if (profile.getCandidateGroup() == CandidateGroup.POSSIBLE_FIT
                || calculateFutureCompositeQualityScore(profile) >= FAIR_QUALITY_SCORE) {
            return FutureReviewAction.MARK_FOR_WAITLIST;
        }
        if (isFutureProfileRisky(profile) || profile.getCandidateGroup() == CandidateGroup.WEAK_FIT) {
            return FutureReviewAction.MARK_FOR_REJECTION;
        }
        if (safeStage == FutureReviewStage.FINAL_DECISION) {
            return FutureReviewAction.COMPLETE_REVIEW;
        }
        return FutureReviewAction.DEFER_REVIEW;
    }

    /**
     * Future-only action allowance check for enum statuses.
     */
    public static boolean isFutureReviewActionAllowed(
            FutureReviewAction action,
            ApplicationStatus status) {
        FutureReviewAction safeAction = action == null ? FutureReviewAction.NO_ACTION : action;
        if (safeAction == FutureReviewAction.NO_ACTION) {
            return true;
        }
        if (status == null) {
            return safeAction == FutureReviewAction.REQUEST_MORE_INFO
                    || safeAction == FutureReviewAction.DEFER_REVIEW;
        }
        if (needsDecision(status)) {
            return true;
        }
        return safeAction == FutureReviewAction.NO_ACTION
                || safeAction == FutureReviewAction.COMPLETE_REVIEW;
    }

    /**
     * Future-only action allowance check for raw status labels.
     */
    public static boolean isFutureReviewActionAllowed(
            FutureReviewAction action,
            String rawStatus) {
        return isFutureReviewActionAllowed(action, parseFutureApplicationStatus(rawStatus));
    }

    /**
     * Future-only plain-English explanation for action allowance.
     */
    public static String explainFutureReviewActionAllowance(
            FutureReviewAction action,
            String rawStatus) {
        FutureReviewAction safeAction = action == null ? FutureReviewAction.NO_ACTION : action;
        ApplicationStatus status = parseFutureApplicationStatus(rawStatus);
        boolean allowed = isFutureReviewActionAllowed(safeAction, status);
        if (allowed && safeAction == FutureReviewAction.NO_ACTION) {
            return "No action is always allowed because it does not change future review state.";
        }
        if (allowed && needsDecision(status)) {
            return safeAction.getLabel() + " is allowed because "
                    + readableStatusLabel(status).toLowerCase()
                    + " applications still need MO review.";
        }
        if (allowed && status == null) {
            return safeAction.getLabel()
                    + " is allowed as a safe fallback while the status remains unknown.";
        }
        if (allowed) {
            return safeAction.getLabel()
                    + " is allowed because it only closes or records a completed future review.";
        }
        return safeAction.getLabel()
                + " is not allowed because "
                + normalizeFutureStatusLabel(rawStatus).toLowerCase()
                + " is not a reviewable future status.";
    }

    /**
     * Future-only action-to-outcome mapper. It does not persist any decision.
     */
    public static FutureReviewOutcome mapFutureReviewActionToOutcome(FutureReviewAction action) {
        if (action == FutureReviewAction.MARK_FOR_INTERVIEW) {
            return FutureReviewOutcome.INTERVIEW_RECOMMENDED;
        }
        if (action == FutureReviewAction.MARK_FOR_WAITLIST) {
            return FutureReviewOutcome.WAITLIST_RECOMMENDED;
        }
        if (action == FutureReviewAction.MARK_FOR_REJECTION) {
            return FutureReviewOutcome.REJECTION_RECOMMENDED;
        }
        if (action == FutureReviewAction.REQUEST_MORE_INFO || action == FutureReviewAction.DEFER_REVIEW) {
            return FutureReviewOutcome.NEEDS_MORE_REVIEW;
        }
        if (action == FutureReviewAction.ESCALATE_TO_ADMIN) {
            return FutureReviewOutcome.BLOCKED;
        }
        if (action == FutureReviewAction.NO_ACTION || action == null) {
            return FutureReviewOutcome.NOT_APPLICABLE;
        }
        if (action == FutureReviewAction.START_REVIEW || action == FutureReviewAction.COMPLETE_REVIEW) {
            return FutureReviewOutcome.NEEDS_MORE_REVIEW;
        }
        return FutureReviewOutcome.UNKNOWN;
    }

    /**
     * Future-only transition summary for possible review workflow previews.
     */
    public static String generateFutureWorkflowTransitionSummary(
            FutureCandidateProfile profile,
            FutureReviewStage currentStage,
            FutureReviewAction action) {
        FutureReviewStage safeCurrentStage = currentStage == null ? FutureReviewStage.UNKNOWN : currentStage;
        FutureReviewAction safeAction = action == null ? FutureReviewAction.NO_ACTION : action;
        FutureReviewStage nextStage = suggestNextReviewStage(profile, safeCurrentStage);
        FutureReviewOutcome outcome = mapFutureReviewActionToOutcome(safeAction);
        String candidateLabel = profile == null ? "Unknown applicant" : profile.getApplicantName();

        return "Future workflow transition"
                + "\nCandidate: " + candidateLabel
                + "\nCurrent stage: " + safeCurrentStage.getLabel()
                + "\nSuggested stage: " + nextStage.getLabel()
                + "\nAction: " + safeAction.getLabel()
                + "\nAction allowed: " + isFutureReviewActionAllowed(
                        safeAction,
                        profile == null ? null : profile.getNormalizedStatus())
                + "\nOutcome: " + outcome.getLabel()
                + "\nExplanation: " + explainFutureReviewActionAllowance(
                        safeAction,
                        profile == null ? null : profile.getNormalizedStatus());
    }

    /**
     * Future-only block reason detector for richer workflow previews.
     */
    public static List<FutureReviewBlockReason> determineFutureReviewBlockReasons(FutureCandidateProfile profile) {
        if (profile == null) {
            return List.of(FutureReviewBlockReason.UNKNOWN);
        }
        List<FutureReviewBlockReason> reasons = new ArrayList<>();
        if (isUnknownLabel(profile.getCandidateId())) {
            reasons.add(FutureReviewBlockReason.MISSING_CANDIDATE_ID);
        }
        if (isUnknownLabel(profile.getApplicationId())) {
            reasons.add(FutureReviewBlockReason.MISSING_APPLICATION_ID);
        }
        if (isUnknownLabel(profile.getJobId())) {
            reasons.add(FutureReviewBlockReason.MISSING_JOB_ID);
        }
        ApplicationStatus status = parseFutureApplicationStatus(profile.getNormalizedStatus());
        if (status == null) {
            reasons.add(FutureReviewBlockReason.UNKNOWN_STATUS);
        } else if (!needsDecision(status)) {
            reasons.add(FutureReviewBlockReason.NON_REVIEWABLE_STATUS);
        }
        if (profile.getWorkloadSeverity() == WorkloadSeverity.EXTREME) {
            reasons.add(FutureReviewBlockReason.EXTREME_WORKLOAD);
        }
        if (profile.getMatchedSkills().isEmpty() && profile.getMissingSkills().isEmpty()) {
            reasons.add(FutureReviewBlockReason.INCOMPLETE_SKILL_DATA);
        }
        if (reasons.isEmpty()) {
            reasons.add(FutureReviewBlockReason.NO_BLOCK);
        }
        return List.copyOf(reasons);
    }

    /**
     * Future-only readiness summary for a single profile. This is plain text only and is not shown
     * in the current application unless a later workflow explicitly calls it.
     */
    public static String buildFutureReviewReadinessSummary(FutureCandidateProfile profile) {
        if (profile == null) {
            return "Future review readiness: blocked\nReasons: Unknown";
        }
        List<FutureReviewBlockReason> reasons = determineFutureReviewBlockReasons(profile);
        boolean blocked = reasons.stream().anyMatch(reason -> reason != FutureReviewBlockReason.NO_BLOCK);
        return "Future review readiness"
                + "\nCandidate: " + profile.getApplicantName()
                + "\nReady: " + !blocked
                + "\nBlock reasons: " + formatFutureBlockReasons(reasons)
                + "\nSuggested stage: " + suggestNextReviewStage(profile, FutureReviewStage.NOT_STARTED).getLabel()
                + "\nSuggested action: " + suggestNextReviewAction(profile, FutureReviewStage.NOT_STARTED).getLabel()
                + "\nQuality: " + classifyFutureQualityBand(profile).getLabel()
                + "\nConfidence: " + calculateFutureReviewConfidenceScore(profile) + "/100";
    }

    /**
     * Future-only bulk review plan formatter. It sorts copies of the supplied profiles and returns
     * deterministic plain text instead of changing application state.
     */
    public static String buildFutureBulkReviewPlan(List<FutureCandidateProfile> profiles, int maximumItems) {
        int safeLimit = Math.max(0, maximumItems);
        if (safeLimit == 0) {
            return "Future bulk review plan\nNo items requested.";
        }
        List<FutureCandidateProfile> sorted = safeFutureCandidateProfiles(profiles).stream()
                .sorted(MoApplicantRankingFutureExtensions::compareFutureQualityScores)
                .limit(safeLimit)
                .toList();
        if (sorted.isEmpty()) {
            return "Future bulk review plan\nNo candidate profiles are available.";
        }
        StringBuilder builder = new StringBuilder("Future bulk review plan");
        int index = 1;
        for (FutureCandidateProfile profile : sorted) {
            builder.append("\n")
                    .append(index)
                    .append(". ")
                    .append(buildFutureProfileCompactLabel(profile))
                    .append(" | quality=")
                    .append(classifyFutureQualityBand(profile).getLabel())
                    .append(" | action=")
                    .append(suggestNextReviewAction(profile, FutureReviewStage.NOT_STARTED).getLabel());
            index++;
        }
        return builder.toString();
    }

    /**
     * Future-only sample profiles for demos or manual exploration. These samples are not loaded by
     * production code and do not create files, database records, notifications, or UI rows.
     */
    public static List<FutureCandidateProfile> createSampleFutureCandidateProfiles() {
        List<FutureCandidateProfile> samples = new ArrayList<>();
        samples.add(createFutureCandidateProfile(
                "CAND-001",
                "Sample Strong Applicant",
                "APP-001",
                "JOB-101",
                "Software Engineering TA",
                ApplicationStatus.PENDING,
                92,
                87,
                6,
                List.of("Java", "Testing", "OO Design"),
                List.of(),
                List.of(),
                "",
                "Future-only sample strong profile."));
        samples.add(createFutureCandidateProfile(
                "CAND-002",
                "Sample Workload Risk",
                "APP-002",
                "JOB-101",
                "Software Engineering TA",
                ApplicationStatus.REVIEWING,
                74,
                79,
                23,
                List.of("Java"),
                List.of("JUnit", "Teaching experience"),
                List.of("High current workload"),
                "",
                "Future-only sample workload risk profile."));
        samples.add(createFutureCandidateProfile(
                "",
                "",
                "APP-003",
                "",
                "",
                (String) null,
                Double.NaN,
                null,
                -5,
                List.of(),
                List.of(),
                List.of(),
                "",
                "Future-only sample incomplete profile."));
        return List.copyOf(samples);
    }

    /**
     * Future-only quality band classifier for a rich profile.
     */
    public static FutureQualityBand classifyFutureQualityBand(FutureCandidateProfile profile) {
        if (profile == null) {
            return FutureQualityBand.UNKNOWN;
        }
        return classifyFutureQualityBand(
                profile.getMatchScore(),
                profile.getMissingSkills().size(),
                profile.getWorkload(),
                profile.getNormalizedStatus(),
                isFutureProfileRisky(profile));
    }

    /**
     * Future-only quality band classifier using primitive values.
     */
    public static FutureQualityBand classifyFutureQualityBand(
            double matchScore,
            int missingSkillCount,
            int workload,
            String rawStatus,
            boolean risky) {
        ApplicationStatus status = parseFutureApplicationStatus(rawStatus);
        if (status == null) {
            return FutureQualityBand.UNKNOWN;
        }
        if (!needsDecision(status)) {
            return FutureQualityBand.UNSUITABLE;
        }
        int score = calculateFutureCompositeQualityScore(
                matchScore,
                missingSkillCount,
                workload,
                rawStatus,
                risky);
        if (score >= EXCELLENT_QUALITY_SCORE) {
            return FutureQualityBand.EXCELLENT;
        }
        if (score >= GOOD_QUALITY_SCORE) {
            return FutureQualityBand.GOOD;
        }
        if (score >= FAIR_QUALITY_SCORE) {
            return FutureQualityBand.FAIR;
        }
        if (score >= WEAK_QUALITY_SCORE) {
            return FutureQualityBand.WEAK;
        }
        return FutureQualityBand.UNSUITABLE;
    }

    /**
     * Future-only quality signal builder for explainable review summaries.
     */
    public static List<FutureQualitySignal> generateFutureQualitySignals(FutureCandidateProfile profile) {
        if (profile == null) {
            return List.of(FutureQualitySignal.NO_SIGNAL_DATA);
        }
        List<FutureQualitySignal> signals = new ArrayList<>();
        if (profile.getMatchScore() >= STRONG_MATCH_SCORE) {
            signals.add(FutureQualitySignal.STRONG_MATCH_SCORE);
        }
        if (profile.getMatchedSkills().size() >= SOME_MATCHED_SKILLS) {
            signals.add(FutureQualitySignal.SKILL_ALIGNMENT);
        }
        if (profile.getMissingSkills().isEmpty()) {
            signals.add(FutureQualitySignal.NO_MISSING_SKILLS);
        }
        if (profile.getWorkloadSeverity() == WorkloadSeverity.NONE
                || profile.getWorkloadSeverity() == WorkloadSeverity.LOW) {
            signals.add(FutureQualitySignal.MANAGEABLE_WORKLOAD);
        }
        if (profile.getRankingTrend() == RankingTrend.IMPROVED) {
            signals.add(FutureQualitySignal.IMPROVING_TREND);
        } else if (profile.getRankingTrend() == RankingTrend.STABLE) {
            signals.add(FutureQualitySignal.STABLE_TREND);
        }
        if (isFutureProfileReviewable(profile)) {
            signals.add(FutureQualitySignal.REVIEWABLE_STATUS);
        }
        if (signals.isEmpty()) {
            signals.add(FutureQualitySignal.NO_SIGNAL_DATA);
        }
        return List.copyOf(signals);
    }

    /**
     * Future-only quality warning builder for explainable review summaries.
     */
    public static List<FutureQualityWarning> generateFutureQualityWarnings(FutureCandidateProfile profile) {
        if (profile == null) {
            return List.of(FutureQualityWarning.INCOMPLETE_PROFILE);
        }
        List<FutureQualityWarning> warnings = new ArrayList<>();
        if (hasFutureProfileIncompleteInformation(profile)) {
            warnings.add(FutureQualityWarning.INCOMPLETE_PROFILE);
        }
        if (!isFutureProfileReviewable(profile)) {
            warnings.add(FutureQualityWarning.NON_REVIEWABLE_STATUS);
        }
        if (profile.getMatchScore() < LOW_MATCH_SCORE) {
            warnings.add(FutureQualityWarning.LOW_MATCH_SCORE);
        }
        if (profile.getMissingSkills().size() >= MANY_MISSING_SKILLS) {
            warnings.add(FutureQualityWarning.MANY_MISSING_SKILLS);
        }
        if (profile.getWorkloadSeverity() == WorkloadSeverity.HIGH
                || profile.getWorkloadSeverity() == WorkloadSeverity.EXTREME) {
            warnings.add(FutureQualityWarning.HIGH_WORKLOAD);
        }
        if (profile.getRankingTrend() == RankingTrend.DECLINED) {
            warnings.add(FutureQualityWarning.DECLINING_TREND);
        }
        if (isFutureProfileRisky(profile)) {
            warnings.add(FutureQualityWarning.RISK_FLAGS_PRESENT);
        }
        if (warnings.isEmpty()) {
            warnings.add(FutureQualityWarning.NO_MAJOR_WARNING);
        }
        return List.copyOf(warnings);
    }

    /**
     * Future-only composite quality score from 0 to 100 for a rich profile.
     */
    public static int calculateFutureCompositeQualityScore(FutureCandidateProfile profile) {
        if (profile == null) {
            return 0;
        }
        return calculateFutureCompositeQualityScore(
                profile.getMatchScore(),
                profile.getMissingSkills().size(),
                profile.getWorkload(),
                profile.getNormalizedStatus(),
                isFutureProfileRisky(profile));
    }

    /**
     * Future-only composite quality score from 0 to 100 using primitive values.
     */
    public static int calculateFutureCompositeQualityScore(
            double matchScore,
            int missingSkillCount,
            int workload,
            String rawStatus,
            boolean risky) {
        int score = normalizeFuturePercentage(matchScore);
        int safeMissing = Math.max(0, missingSkillCount);
        int safeWorkload = Math.max(0, workload);
        ApplicationStatus status = parseFutureApplicationStatus(rawStatus);
        int composite = Math.round(score * 0.65f);

        composite += Math.max(0, 20 - (safeMissing * 5));

        WorkloadSeverity severity = classifyWorkloadSeverity(safeWorkload);
        if (severity == WorkloadSeverity.NONE || severity == WorkloadSeverity.LOW) {
            composite += 10;
        } else if (severity == WorkloadSeverity.MEDIUM) {
            composite += 4;
        } else if (severity == WorkloadSeverity.HIGH) {
            composite -= 8;
        } else if (severity == WorkloadSeverity.EXTREME) {
            composite -= 18;
        }

        if (status == ApplicationStatus.PENDING || status == ApplicationStatus.REVIEWING) {
            composite += 5;
        } else if (status == ApplicationStatus.APPLIED) {
            composite += 3;
        } else if (status == null) {
            composite -= 15;
        } else {
            composite -= 25;
        }

        if (risky) {
            composite -= 12;
        }
        return clampToRange(composite, 0, 100);
    }

    /**
     * Future-only review confidence score from 0 to 100. Higher means the helper has enough clean
     * profile data to make its advisory quality score less tentative.
     */
    public static int calculateFutureReviewConfidenceScore(FutureCandidateProfile profile) {
        if (profile == null) {
            return 0;
        }
        int confidence = 35;
        if (!isUnknownLabel(profile.getCandidateId())) {
            confidence += 8;
        }
        if (!isUnknownLabel(profile.getApplicantName())) {
            confidence += 8;
        }
        if (!isUnknownLabel(profile.getApplicationId())) {
            confidence += 8;
        }
        if (!isUnknownLabel(profile.getJobId()) && !isUnknownLabel(profile.getJobTitle())) {
            confidence += 8;
        }
        if (parseFutureApplicationStatus(profile.getNormalizedStatus()) != null) {
            confidence += 8;
        }
        if (!profile.getMatchedSkills().isEmpty() || !profile.getMissingSkills().isEmpty()) {
            confidence += 10;
        }
        if (profile.getPreviousMatchScore() >= 0) {
            confidence += 5;
        }
        if (!profile.getRiskFlags().isEmpty()) {
            confidence += 5;
        }
        if (profile.getWorkloadSeverity() != WorkloadSeverity.UNKNOWN) {
            confidence += 5;
        }
        if (hasFutureProfileIncompleteInformation(profile)) {
            confidence -= 20;
        }
        return clampToRange(confidence, 0, 100);
    }

    /**
     * Future-only explanation of how the quality score was produced.
     */
    public static String explainFutureQualityScore(FutureCandidateProfile profile) {
        if (profile == null) {
            return "Future quality score is 0 because the profile is missing.";
        }
        int score = calculateFutureCompositeQualityScore(profile);
        int confidence = calculateFutureReviewConfidenceScore(profile);
        return "Future quality score"
                + "\nCandidate: " + profile.getApplicantName()
                + "\nComposite score: " + score + "/100"
                + "\nQuality band: " + classifyFutureQualityBand(profile).getLabel()
                + "\nReview confidence: " + confidence + "/100"
                + "\nScore basis: match score contributes most, missing skills reduce the score, manageable workload adds a small bonus, non-reviewable or unknown status reduces the score, and risk flags apply a fixed penalty."
                + "\nSignals: " + formatFutureQualitySignals(generateFutureQualitySignals(profile))
                + "\nWarnings: " + formatFutureQualityWarnings(generateFutureQualityWarnings(profile));
    }

    /**
     * Future-only comparator for two richer candidate profiles by quality score, confidence, then
     * readable labels.
     */
    public static int compareFutureQualityScores(
            FutureCandidateProfile left,
            FutureCandidateProfile right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        int qualityComparison = Integer.compare(
                calculateFutureCompositeQualityScore(right),
                calculateFutureCompositeQualityScore(left));
        if (qualityComparison != 0) {
            return qualityComparison;
        }
        int confidenceComparison = Integer.compare(
                calculateFutureReviewConfidenceScore(right),
                calculateFutureReviewConfidenceScore(left));
        if (confidenceComparison != 0) {
            return confidenceComparison;
        }
        int nameComparison = normalizedSortText(left.getApplicantName())
                .compareTo(normalizedSortText(right.getApplicantName()));
        if (nameComparison != 0) {
            return nameComparison;
        }
        return normalizedSortText(left.getApplicationId())
                .compareTo(normalizedSortText(right.getApplicationId()));
    }

    /**
     * Future-only dashboard card for pending candidate count.
     */
    public static FutureDashboardCard buildPendingCandidatesCard(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        int pending = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            if (isFutureProfileReviewable(profile)) {
                pending++;
            }
        }
        NotificationSeverity severity = pending >= MANY_REVIEWABLE_APPLICATIONS
                ? NotificationSeverity.HIGH
                : pending > 0 ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW;
        return new FutureDashboardCard(
                "Pending candidates",
                String.valueOf(pending),
                safeProfiles.size() + " total future profiles",
                severity,
                pending > 0 ? "Open the review queue." : "No pending candidate action.");
    }

    /**
     * Future-only dashboard card for urgent review count.
     */
    public static FutureDashboardCard buildUrgentReviewCountCard(List<FutureCandidateProfile> profiles) {
        int urgent = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (calculateFutureCompositeQualityScore(profile) >= HIGH_CONFIDENCE_SCORE
                    && isFutureProfileReviewable(profile)) {
                urgent++;
            }
        }
        return new FutureDashboardCard(
                "Urgent reviews",
                String.valueOf(urgent),
                "Profiles with strong advisory quality",
                urgent > 0 ? NotificationSeverity.HIGH : NotificationSeverity.LOW,
                urgent > 0 ? "Review strongest candidates first." : "No urgent future reviews.");
    }

    /**
     * Future-only dashboard card for strong candidates.
     */
    public static FutureDashboardCard buildStrongCandidatesCard(List<FutureCandidateProfile> profiles) {
        int strong = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (isFutureProfileStrong(profile)) {
                strong++;
            }
        }
        return new FutureDashboardCard(
                "Strong candidates",
                String.valueOf(strong),
                "High score, low missing-skill pressure",
                strong > 0 ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW,
                strong > 0 ? "Prepare interview shortlist." : "Continue normal screening.");
    }

    /**
     * Future-only dashboard card for risky candidates.
     */
    public static FutureDashboardCard buildRiskyCandidatesCard(List<FutureCandidateProfile> profiles) {
        int risky = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (isFutureProfileRisky(profile)) {
                risky++;
            }
        }
        return new FutureDashboardCard(
                "Risky candidates",
                String.valueOf(risky),
                "Profiles with workload, skill, status, or data warnings",
                risky > 0 ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW,
                risky > 0 ? "Inspect warnings before decisions." : "No major risk flags.");
    }

    /**
     * Future-only dashboard card for average match score.
     */
    public static FutureDashboardCard buildAverageMatchScoreCard(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        if (safeProfiles.isEmpty()) {
            return new FutureDashboardCard("Average match score", "0.0%", "No future profiles", NotificationSeverity.LOW, "No score review needed.");
        }
        int total = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            total += profile.getMatchScore();
        }
        double average = total / (double) safeProfiles.size();
        NotificationSeverity severity = average >= STRONG_MATCH_SCORE
                ? NotificationSeverity.LOW
                : average >= MODERATE_MATCH_SCORE ? NotificationSeverity.MEDIUM : NotificationSeverity.HIGH;
        return new FutureDashboardCard(
                "Average match score",
                formatOneDecimal(average) + "%",
                safeProfiles.size() + " future profiles scored",
                severity,
                average >= MODERATE_MATCH_SCORE ? "Maintain normal review pace." : "Check job-skill alignment.");
    }

    /**
     * Future-only dashboard card for average workload.
     */
    public static FutureDashboardCard buildAverageWorkloadCard(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        if (safeProfiles.isEmpty()) {
            return new FutureDashboardCard("Average workload", "0.0h", "No future profiles", NotificationSeverity.LOW, "No workload review needed.");
        }
        int total = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            total += profile.getWorkload();
        }
        double average = total / (double) safeProfiles.size();
        NotificationSeverity severity = average >= HIGH_WORKLOAD_HOURS
                ? NotificationSeverity.HIGH
                : average >= MEDIUM_WORKLOAD_HOURS ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW;
        return new FutureDashboardCard(
                "Average workload",
                formatOneDecimal(average) + "h",
                "Weekly workload across future profiles",
                severity,
                average >= HIGH_WORKLOAD_HOURS ? "Review overload risk." : "Workload looks manageable.");
    }

    /**
     * Future-only dashboard card for missing skill pressure.
     */
    public static FutureDashboardCard buildMissingSkillPressureCard(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        int missingTotal = 0;
        int highPressure = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            int missing = profile.getMissingSkills().size();
            missingTotal += missing;
            if (missing >= MANY_MISSING_SKILLS) {
                highPressure++;
            }
        }
        double average = safeProfiles.isEmpty() ? 0.0 : missingTotal / (double) safeProfiles.size();
        NotificationSeverity severity = highPressure > 0 ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW;
        return new FutureDashboardCard(
                "Missing skill pressure",
                formatOneDecimal(average),
                highPressure + " profile(s) with several missing skills",
                severity,
                highPressure > 0 ? "Review requirement fit." : "No broad skill pressure.");
    }

    /**
     * Future-only dashboard card for review completion progress.
     */
    public static FutureDashboardCard buildReviewCompletionProgressCard(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        int completed = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            if (!isFutureProfileReviewable(profile)) {
                completed++;
            }
        }
        int percent = safeProfiles.isEmpty() ? 0 : Math.round((completed * 100.0f) / safeProfiles.size());
        return new FutureDashboardCard(
                "Review completion",
                percent + "%",
                completed + " of " + safeProfiles.size() + " profiles not pending review",
                percent >= CLOSE_TO_FILLED_PERCENT ? NotificationSeverity.LOW : NotificationSeverity.MEDIUM,
                percent >= CLOSE_TO_FILLED_PERCENT ? "Most future reviews are complete." : "Continue pending review.");
    }

    /**
     * Future-only dashboard card for candidate quality distribution.
     */
    public static FutureDashboardCard buildCandidateQualityDistributionCard(List<FutureCandidateProfile> profiles) {
        Map<FutureQualityBand, Integer> counts = countFutureQualityBands(profiles);
        String value = "Excellent " + counts.get(FutureQualityBand.EXCELLENT)
                + ", Good " + counts.get(FutureQualityBand.GOOD)
                + ", Fair " + counts.get(FutureQualityBand.FAIR)
                + ", Weak " + counts.get(FutureQualityBand.WEAK)
                + ", Unsuitable " + counts.get(FutureQualityBand.UNSUITABLE);
        int weakOrWorse = counts.get(FutureQualityBand.WEAK) + counts.get(FutureQualityBand.UNSUITABLE);
        return new FutureDashboardCard(
                "Quality distribution",
                value,
                counts.get(FutureQualityBand.UNKNOWN) + " unknown profile(s)",
                weakOrWorse > 0 ? NotificationSeverity.MEDIUM : NotificationSeverity.LOW,
                weakOrWorse > 0 ? "Inspect weak or unsuitable profiles." : "Quality distribution looks healthy.");
    }

    /**
     * Future-only dashboard card for a notification digest.
     */
    public static FutureDashboardCard buildNotificationDigestCard(List<FutureCandidateProfile> profiles) {
        String digest = generateCompactFutureDashboardDigest(profiles);
        NotificationSeverity severity = digest.contains("urgent=0") ? NotificationSeverity.LOW : NotificationSeverity.HIGH;
        return new FutureDashboardCard(
                "Notification digest",
                digest,
                "Plain-text digest only; not connected to NotificationService",
                severity,
                severity == NotificationSeverity.HIGH ? "Review digest alerts." : "No digest alert.");
    }

    /**
     * Future-only full dashboard snapshot builder. This returns plain Java objects only.
     */
    public static FutureDashboardSnapshot buildFullFutureDashboardSnapshot(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        List<FutureDashboardCard> overviewCards = List.of(
                buildPendingCandidatesCard(safeProfiles),
                buildUrgentReviewCountCard(safeProfiles),
                buildStrongCandidatesCard(safeProfiles),
                buildRiskyCandidatesCard(safeProfiles));
        List<FutureDashboardCard> qualityCards = List.of(
                buildAverageMatchScoreCard(safeProfiles),
                buildAverageWorkloadCard(safeProfiles),
                buildMissingSkillPressureCard(safeProfiles),
                buildCandidateQualityDistributionCard(safeProfiles));
        List<FutureDashboardCard> workflowCards = List.of(
                buildReviewCompletionProgressCard(safeProfiles),
                buildNotificationDigestCard(safeProfiles));

        List<FutureDashboardSection> sections = List.of(
                new FutureDashboardSection("Future MO overview", overviewCards),
                new FutureDashboardSection("Future quality signals", qualityCards),
                new FutureDashboardSection("Future workflow", workflowCards));

        return new FutureDashboardSnapshot(
                safeProfiles.size(),
                countFutureReviewableProfiles(safeProfiles),
                countFutureStrongProfiles(safeProfiles),
                countFutureRiskyProfiles(safeProfiles),
                sections,
                generateCompactFutureDashboardDigest(safeProfiles));
    }

    /**
     * Future-only card formatter for plain-text reports.
     */
    public static String formatFutureDashboardCardAsPlainText(FutureDashboardCard card) {
        if (card == null) {
            return "Dashboard card: unavailable";
        }
        return card.getCardTitle()
                + ": " + card.getCardValue()
                + "\nSeverity: " + card.getSeverity().getLabel()
                + "\nSubtitle: " + card.getCardSubtitle()
                + "\nRecommended action: " + card.getRecommendedAction();
    }

    /**
     * Future-only section formatter for plain-text reports.
     */
    public static String formatFutureDashboardSectionAsPlainText(FutureDashboardSection section) {
        if (section == null) {
            return "Dashboard section: unavailable";
        }
        StringBuilder builder = new StringBuilder(section.getSectionTitle());
        for (FutureDashboardCard card : section.getCards()) {
            builder.append("\n\n").append(formatFutureDashboardCardAsPlainText(card));
        }
        return builder.toString();
    }

    /**
     * Future-only snapshot formatter for plain-text reports.
     */
    public static String formatFutureDashboardSnapshotAsPlainText(FutureDashboardSnapshot snapshot) {
        if (snapshot == null) {
            return "Future dashboard snapshot: unavailable";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Future dashboard snapshot")
                .append("\nTotal profiles: ").append(snapshot.getTotalProfiles())
                .append("\nReviewable profiles: ").append(snapshot.getReviewableProfiles())
                .append("\nStrong profiles: ").append(snapshot.getStrongProfiles())
                .append("\nRisky profiles: ").append(snapshot.getRiskyProfiles())
                .append("\nDigest: ").append(snapshot.getCompactDigest());
        for (FutureDashboardSection section : snapshot.getSections()) {
            builder.append("\n\n").append(formatFutureDashboardSectionAsPlainText(section));
        }
        return builder.toString();
    }

    /**
     * Future-only compact dashboard digest for MO users. This does not send notifications.
     */
    public static String generateCompactFutureDashboardDigest(List<FutureCandidateProfile> profiles) {
        List<FutureCandidateProfile> safeProfiles = safeFutureCandidateProfiles(profiles);
        int reviewable = countFutureReviewableProfiles(safeProfiles);
        int urgent = 0;
        int strong = 0;
        int risky = 0;
        int incomplete = 0;
        for (FutureCandidateProfile profile : safeProfiles) {
            if (calculateFutureCompositeQualityScore(profile) >= HIGH_CONFIDENCE_SCORE
                    && isFutureProfileReviewable(profile)) {
                urgent++;
            }
            if (isFutureProfileStrong(profile)) {
                strong++;
            }
            if (isFutureProfileRisky(profile)) {
                risky++;
            }
            if (hasFutureProfileIncompleteInformation(profile)) {
                incomplete++;
            }
        }
        return "profiles=" + safeProfiles.size()
                + ", reviewable=" + reviewable
                + ", urgent=" + urgent
                + ", strong=" + strong
                + ", risky=" + risky
                + ", incomplete=" + incomplete;
    }

    private static int normalizeFuturePercentage(double rawScore) {
        if (Double.isNaN(rawScore) || Double.isInfinite(rawScore)) {
            return 0;
        }
        return clampToRange((int) Math.round(rawScore), 0, 100);
    }

    private static int normalizeFuturePreviousScore(Integer previousMatchScore) {
        if (previousMatchScore == null || previousMatchScore < 0) {
            return -1;
        }
        return normalizeMatchScore(previousMatchScore);
    }

    private static List<String> normalizeFutureTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                String trimmed = value.trim();
                if (!normalized.contains(trimmed)) {
                    normalized.add(trimmed);
                }
            }
        }
        return List.copyOf(normalized);
    }

    private static String normalizeFutureStatusLabel(String rawStatus) {
        ApplicationStatus status = parseFutureApplicationStatus(rawStatus);
        if (status != null) {
            return status.name();
        }
        return "UNKNOWN";
    }

    private static CandidateGroup groupFutureProfileValues(
            int matchScore,
            int missingSkillCount,
            int workload,
            ApplicationStatus status) {
        if (status == null) {
            return CandidateGroup.UNKNOWN;
        }
        if (!needsDecision(status)) {
            return CandidateGroup.NOT_PENDING;
        }
        int safeScore = normalizeMatchScore(matchScore);
        int safeMissing = Math.max(0, missingSkillCount);
        WorkloadSeverity severity = classifyWorkloadSeverity(workload);
        if (safeScore < LOW_MATCH_SCORE
                || safeMissing >= CRITICAL_MISSING_SKILLS
                || severity == WorkloadSeverity.HIGH
                || severity == WorkloadSeverity.EXTREME) {
            return CandidateGroup.HIGH_RISK;
        }
        if (safeScore >= STRONG_MATCH_SCORE && safeMissing <= 1) {
            return CandidateGroup.STRONG_FIT;
        }
        if (safeScore >= MODERATE_MATCH_SCORE && safeMissing <= MANY_MISSING_SKILLS) {
            return CandidateGroup.POSSIBLE_FIT;
        }
        return CandidateGroup.WEAK_FIT;
    }

    private static String formatFuturePreviousScore(int previousMatchScore) {
        return previousMatchScore < 0 ? "Not recorded" : previousMatchScore + "%";
    }

    private static String formatFutureList(List<String> values) {
        List<String> safeValues = normalizeFutureTextList(values);
        return safeValues.isEmpty() ? "None" : String.join(", ", safeValues);
    }

    private static boolean isUnknownLabel(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        String normalized = value.trim().toUpperCase();
        return "UNKNOWN".equals(normalized)
                || "UNKNOWN APPLICANT".equals(normalized)
                || "UNKNOWN APPLICATION".equals(normalized)
                || "UNKNOWN JOB".equals(normalized);
    }

    private static String formatFutureQualitySignals(List<FutureQualitySignal> signals) {
        if (signals == null || signals.isEmpty()) {
            return FutureQualitySignal.NO_SIGNAL_DATA.getLabel();
        }
        List<String> labels = new ArrayList<>();
        for (FutureQualitySignal signal : signals) {
            labels.add(signal == null
                    ? FutureQualitySignal.NO_SIGNAL_DATA.getLabel()
                    : signal.getLabel());
        }
        return String.join(", ", labels);
    }

    private static String formatFutureQualityWarnings(List<FutureQualityWarning> warnings) {
        if (warnings == null || warnings.isEmpty()) {
            return FutureQualityWarning.NO_MAJOR_WARNING.getLabel();
        }
        List<String> labels = new ArrayList<>();
        for (FutureQualityWarning warning : warnings) {
            labels.add(warning == null
                    ? FutureQualityWarning.INCOMPLETE_PROFILE.getLabel()
                    : warning.getLabel());
        }
        return String.join(", ", labels);
    }

    private static String formatFutureBlockReasons(List<FutureReviewBlockReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return FutureReviewBlockReason.UNKNOWN.getLabel();
        }
        List<String> labels = new ArrayList<>();
        for (FutureReviewBlockReason reason : reasons) {
            labels.add(reason == null
                    ? FutureReviewBlockReason.UNKNOWN.getLabel()
                    : reason.getLabel());
        }
        return String.join(", ", labels);
    }

    private static List<FutureCandidateProfile> safeFutureCandidateProfiles(
            List<FutureCandidateProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return List.of();
        }
        return profiles.stream()
                .filter(Objects::nonNull)
                .map(MoApplicantRankingFutureExtensions::copyFutureCandidateProfile)
                .toList();
    }

    private static int countFutureReviewableProfiles(List<FutureCandidateProfile> profiles) {
        int count = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (isFutureProfileReviewable(profile)) {
                count++;
            }
        }
        return count;
    }

    private static int countFutureStrongProfiles(List<FutureCandidateProfile> profiles) {
        int count = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (isFutureProfileStrong(profile)) {
                count++;
            }
        }
        return count;
    }

    private static int countFutureRiskyProfiles(List<FutureCandidateProfile> profiles) {
        int count = 0;
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            if (isFutureProfileRisky(profile)) {
                count++;
            }
        }
        return count;
    }

    private static Map<FutureQualityBand, Integer> countFutureQualityBands(
            List<FutureCandidateProfile> profiles) {
        Map<FutureQualityBand, Integer> counts = new EnumMap<>(FutureQualityBand.class);
        for (FutureQualityBand band : FutureQualityBand.values()) {
            counts.put(band, 0);
        }
        for (FutureCandidateProfile profile : safeFutureCandidateProfiles(profiles)) {
            FutureQualityBand band = classifyFutureQualityBand(profile);
            counts.put(band, counts.get(band) + 1);
        }
        return counts;
    }

    private static int statusTieBreakerWeight(ApplicationStatus status) {
        if (status == ApplicationStatus.PENDING) {
            return 0;
        }
        if (status == ApplicationStatus.REVIEWING) {
            return 1;
        }
        if (status == ApplicationStatus.APPLIED) {
            return 2;
        }
        if (status == ApplicationStatus.ACCEPTED) {
            return 3;
        }
        if (status == ApplicationStatus.REJECTED) {
            return 4;
        }
        if (status == ApplicationStatus.WITHDRAWN) {
            return 5;
        }
        return 6;
    }

    private static int riskTieBreakerWeight(CandidateRiskLevel riskLevel) {
        if (riskLevel == CandidateRiskLevel.LOW) {
            return 0;
        }
        if (riskLevel == CandidateRiskLevel.MEDIUM) {
            return 1;
        }
        if (riskLevel == CandidateRiskLevel.HIGH) {
            return 2;
        }
        if (riskLevel == CandidateRiskLevel.NOT_APPLICABLE) {
            return 3;
        }
        return 4;
    }

    private static String normalizedSortText(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private static String candidateDisplayName(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "Unknown candidate";
        }
        String name = safeLabel(candidate.getApplicantName(), "");
        if (!name.isBlank()) {
            return name;
        }
        return safeLabel(candidate.getApplicationId(), "Unknown candidate");
    }

    private static String candidateDigestName(FutureCandidateSnapshot candidate) {
        if (candidate == null) {
            return "None";
        }
        return candidateDisplayName(candidate)
                + " (" + candidate.getMatchScore() + "%, "
                + readableCandidateGroup(groupCandidateSnapshot(candidate)) + ")";
    }

    private static Integer parseFirstInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        String trimmed = rawValue.trim();
        StringBuilder digits = new StringBuilder();
        boolean started = false;

        for (int index = 0; index < trimmed.length(); index++) {
            char character = trimmed.charAt(index);
            if (!started && (character == '-' || Character.isDigit(character))) {
                digits.append(character);
                started = true;
            } else if (started && Character.isDigit(character)) {
                digits.append(character);
            } else if (started) {
                break;
            }
        }

        if (digits.length() == 0 || "-".contentEquals(digits)) {
            return null;
        }
        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
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

    private static List<FutureJobDashboardItem> safeFutureJobDashboardItems(List<FutureJobDashboardItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static List<FutureCandidateSnapshot> safeFutureCandidateSnapshots(
            List<FutureCandidateSnapshot> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return candidates.stream()
                .filter(Objects::nonNull)
                .toList();
    }

    private static String readableAttentionFlags(List<ApplicantAttentionFlag> flags) {
        if (flags == null || flags.isEmpty()) {
            return ApplicantAttentionFlag.NORMAL_REVIEW_ITEM.getLabel();
        }
        return flags.stream()
                .filter(Objects::nonNull)
                .map(ApplicantAttentionFlag::getLabel)
                .toList()
                .stream()
                .reduce((left, right) -> left + ", " + right)
                .orElse(ApplicantAttentionFlag.NORMAL_REVIEW_ITEM.getLabel());
    }

    private static MatchTier matchTierFromScore(int matchScore) {
        int safeMatchScore = normalizeMatchScore(matchScore);
        if (safeMatchScore >= STRONG_MATCH_SCORE) {
            return MatchTier.STRONG_MATCH;
        }
        if (safeMatchScore >= MODERATE_MATCH_SCORE) {
            return MatchTier.MODERATE_MATCH;
        }
        return MatchTier.WEAK_MATCH;
    }

    private static WorkloadRisk workloadRiskFromHours(int currentWorkloadHours) {
        int safeHours = Math.max(0, currentWorkloadHours);
        if (safeHours >= HIGH_WORKLOAD_HOURS) {
            return WorkloadRisk.HIGH;
        }
        if (safeHours >= MEDIUM_WORKLOAD_HOURS) {
            return WorkloadRisk.MEDIUM;
        }
        return WorkloadRisk.LOW;
    }

    private static DecisionState decisionStateFromStatus(ApplicationStatus status) {
        if (status == ApplicationStatus.APPLIED) {
            return DecisionState.APPLIED_NEEDS_REVIEW;
        }
        if (status == ApplicationStatus.REVIEWING) {
            return DecisionState.IN_REVIEW;
        }
        if (status == ApplicationStatus.PENDING) {
            return DecisionState.PENDING_DECISION;
        }
        if (status == ApplicationStatus.ACCEPTED) {
            return DecisionState.ACCEPTED_DONE;
        }
        if (status == ApplicationStatus.REJECTED) {
            return DecisionState.REJECTED_DONE;
        }
        if (status == ApplicationStatus.WITHDRAWN) {
            return DecisionState.WITHDRAWN_DONE;
        }
        return DecisionState.UNKNOWN;
    }

    private static int decisionNeedWeight(ApplicationStatus status) {
        if (status == ApplicationStatus.PENDING) {
            return 30;
        }
        if (status == ApplicationStatus.REVIEWING) {
            return 25;
        }
        if (status == ApplicationStatus.APPLIED) {
            return 20;
        }
        return 0;
    }

    private static String safeLabel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
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

    private static String formatOneDecimal(double value) {
        int scaled = (int) Math.round(Math.max(0.0, value) * 10.0);
        return (scaled / 10) + "." + (scaled % 10);
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

    public enum ApplicantAttentionFlag {
        HIGH_MATCH_STILL_PENDING("High match still waiting for an MO decision"),
        LOW_MATCH_STILL_PENDING("Low match still waiting for an MO decision"),
        JOB_ALMOST_FILLED("Job is close to filled"),
        JOB_ALREADY_FILLED("Job is already filled"),
        APPLICANT_POSSIBLY_OVERLOADED("Applicant may be overloaded"),
        APPLICATION_WAITING_TOO_LONG("Application has waited longer than the future threshold"),
        APPLICATION_DOES_NOT_NEED_ACTION("Application no longer needs an MO decision"),
        NORMAL_REVIEW_ITEM("Normal review item"),
        NO_SIGNAL_DATA("No applicant signal data");

        private final String label;

        ApplicantAttentionFlag(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum DecisionSuggestion {
        STRONGLY_REVIEW("Strongly Review"),
        REVIEW("Review"),
        LOW_PRIORITY("Low Priority"),
        NO_ACTION_NEEDED("No Action Needed");

        private final String label;

        DecisionSuggestion(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum JobFillState {
        EMPTY("No accepted applicants yet"),
        IN_PROGRESS("Partially filled"),
        CLOSE_TO_FILLED("Close to filled"),
        FILLED("Filled");

        private final String label;

        JobFillState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum DecisionState {
        APPLIED_NEEDS_REVIEW("Applied - needs review"),
        IN_REVIEW("Reviewing - needs decision"),
        PENDING_DECISION("Pending - needs decision"),
        ACCEPTED_DONE("Accepted - completed"),
        REJECTED_DONE("Rejected - completed"),
        WITHDRAWN_DONE("Withdrawn - completed"),
        UNKNOWN("Unknown");

        private final String label;

        DecisionState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum RankingTrend {
        IMPROVED("Improved"),
        DECLINED("Declined"),
        STABLE("Stable"),
        UNKNOWN("Unknown");

        private final String label;

        RankingTrend(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum ReviewPriority {
        URGENT("Review urgently"),
        NORMAL("Review normally"),
        LATER("Review later"),
        SKIP_FOR_NOW("Skip for now");

        private final String label;

        ReviewPriority(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum CandidateRiskLevel {
        LOW("Low risk"),
        MEDIUM("Medium risk"),
        HIGH("High risk"),
        NOT_APPLICABLE("Not applicable");

        private final String label;

        CandidateRiskLevel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum CandidateGroup {
        STRONG_FIT("Strong fit"),
        POSSIBLE_FIT("Possible fit"),
        WEAK_FIT("Weak fit"),
        HIGH_RISK("High risk"),
        NOT_PENDING("Not pending"),
        UNKNOWN("Unknown");

        private final String label;

        CandidateGroup(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum WorkloadSeverity {
        NONE("None"),
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        EXTREME("Extreme"),
        UNKNOWN("Unknown");

        private final String label;

        WorkloadSeverity(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureReviewStage {
        NOT_STARTED("Not started"),
        INITIAL_SCREENING("Initial screening"),
        SKILL_REVIEW("Skill review"),
        WORKLOAD_REVIEW("Workload review"),
        FINAL_DECISION("Final decision"),
        COMPLETED("Completed"),
        BLOCKED("Blocked"),
        UNKNOWN("Unknown");

        private final String label;

        FutureReviewStage(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureReviewAction {
        START_REVIEW("Start review"),
        REQUEST_MORE_INFO("Request more information"),
        MARK_FOR_INTERVIEW("Mark for interview"),
        MARK_FOR_WAITLIST("Mark for waitlist"),
        MARK_FOR_REJECTION("Mark for rejection"),
        ESCALATE_TO_ADMIN("Escalate to admin"),
        DEFER_REVIEW("Defer review"),
        COMPLETE_REVIEW("Complete review"),
        NO_ACTION("No action");

        private final String label;

        FutureReviewAction(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureReviewOutcome {
        INTERVIEW_RECOMMENDED("Interview recommended"),
        WAITLIST_RECOMMENDED("Waitlist recommended"),
        REJECTION_RECOMMENDED("Rejection recommended"),
        NEEDS_MORE_REVIEW("Needs more review"),
        BLOCKED("Blocked"),
        NOT_APPLICABLE("Not applicable"),
        UNKNOWN("Unknown");

        private final String label;

        FutureReviewOutcome(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureReviewBlockReason {
        MISSING_CANDIDATE_ID("Missing candidate ID"),
        MISSING_APPLICATION_ID("Missing application ID"),
        MISSING_JOB_ID("Missing job ID"),
        UNKNOWN_STATUS("Unknown status"),
        NON_REVIEWABLE_STATUS("Non-reviewable status"),
        EXTREME_WORKLOAD("Extreme workload"),
        INCOMPLETE_SKILL_DATA("Incomplete skill data"),
        NO_BLOCK("No block"),
        UNKNOWN("Unknown");

        private final String label;

        FutureReviewBlockReason(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureQualityBand {
        EXCELLENT("Excellent"),
        GOOD("Good"),
        FAIR("Fair"),
        WEAK("Weak"),
        UNSUITABLE("Unsuitable"),
        UNKNOWN("Unknown");

        private final String label;

        FutureQualityBand(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureQualitySignal {
        STRONG_MATCH_SCORE("Strong match score"),
        SKILL_ALIGNMENT("Visible skill alignment"),
        NO_MISSING_SKILLS("No missing skills recorded"),
        MANAGEABLE_WORKLOAD("Manageable workload"),
        IMPROVING_TREND("Improving ranking trend"),
        STABLE_TREND("Stable ranking trend"),
        REVIEWABLE_STATUS("Reviewable status"),
        NO_SIGNAL_DATA("No signal data");

        private final String label;

        FutureQualitySignal(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FutureQualityWarning {
        LOW_MATCH_SCORE("Low match score"),
        MANY_MISSING_SKILLS("Many missing skills"),
        HIGH_WORKLOAD("High workload"),
        NON_REVIEWABLE_STATUS("Non-reviewable status"),
        INCOMPLETE_PROFILE("Incomplete profile"),
        DECLINING_TREND("Declining ranking trend"),
        RISK_FLAGS_PRESENT("Risk flags present"),
        NO_MAJOR_WARNING("No major warning");

        private final String label;

        FutureQualityWarning(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /**
     * Future-only enriched candidate profile for later MO ranking dashboards.
     *
     * <p>This object is intentionally not connected to current models, repositories, services, UI
     * screens, or workflows. It exists only as a safe, immutable shape for future helper methods.
     */
    public static final class FutureCandidateProfile {
        private final String candidateId;
        private final String applicantName;
        private final String applicationId;
        private final String jobId;
        private final String jobTitle;
        private final String normalizedStatus;
        private final int matchScore;
        private final int previousMatchScore;
        private final int workload;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final List<String> riskFlags;
        private final RankingTrend rankingTrend;
        private final ReviewPriority reviewPriority;
        private final CandidateGroup candidateGroup;
        private final WorkloadSeverity workloadSeverity;
        private final String recommendationText;
        private final String notes;

        private FutureCandidateProfile(
                String candidateId,
                String applicantName,
                String applicationId,
                String jobId,
                String jobTitle,
                String normalizedStatus,
                int matchScore,
                int previousMatchScore,
                int workload,
                List<String> matchedSkills,
                List<String> missingSkills,
                List<String> riskFlags,
                RankingTrend rankingTrend,
                ReviewPriority reviewPriority,
                CandidateGroup candidateGroup,
                WorkloadSeverity workloadSeverity,
                String recommendationText,
                String notes) {
            this.candidateId = safeLabel(candidateId, "UNKNOWN");
            this.applicantName = safeLabel(applicantName, "Unknown applicant");
            this.applicationId = safeLabel(applicationId, "UNKNOWN");
            this.jobId = safeLabel(jobId, "UNKNOWN");
            this.jobTitle = safeLabel(jobTitle, "Unknown job");
            this.normalizedStatus = safeLabel(normalizedStatus, "UNKNOWN");
            this.matchScore = normalizeMatchScore(matchScore);
            this.previousMatchScore = previousMatchScore < 0 ? -1 : normalizeMatchScore(previousMatchScore);
            this.workload = Math.max(0, workload);
            this.matchedSkills = normalizeFutureTextList(matchedSkills);
            this.missingSkills = normalizeFutureTextList(missingSkills);
            this.riskFlags = normalizeFutureTextList(riskFlags);
            this.rankingTrend = rankingTrend == null ? RankingTrend.UNKNOWN : rankingTrend;
            this.reviewPriority = reviewPriority == null ? ReviewPriority.SKIP_FOR_NOW : reviewPriority;
            this.candidateGroup = candidateGroup == null ? CandidateGroup.UNKNOWN : candidateGroup;
            this.workloadSeverity = workloadSeverity == null ? WorkloadSeverity.UNKNOWN : workloadSeverity;
            this.recommendationText = safeLabel(recommendationText, "Not applicable");
            this.notes = notes == null ? "" : notes.trim();
        }

        public String getCandidateId() {
            return candidateId;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getJobId() {
            return jobId;
        }

        public String getJobTitle() {
            return jobTitle;
        }

        public String getNormalizedStatus() {
            return normalizedStatus;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public int getPreviousMatchScore() {
            return previousMatchScore;
        }

        public int getWorkload() {
            return workload;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public List<String> getRiskFlags() {
            return riskFlags;
        }

        public RankingTrend getRankingTrend() {
            return rankingTrend;
        }

        public ReviewPriority getReviewPriority() {
            return reviewPriority;
        }

        public CandidateGroup getCandidateGroup() {
            return candidateGroup;
        }

        public WorkloadSeverity getWorkloadSeverity() {
            return workloadSeverity;
        }

        public String getRecommendationText() {
            return recommendationText;
        }

        public String getNotes() {
            return notes;
        }

        public boolean isReviewable() {
            return isFutureProfileReviewable(this);
        }

        public boolean isStrong() {
            return isFutureProfileStrong(this);
        }

        public boolean isRisky() {
            return isFutureProfileRisky(this);
        }

        public boolean hasIncompleteInformation() {
            return hasFutureProfileIncompleteInformation(this);
        }

        public String toCompactLabel() {
            return buildFutureProfileCompactLabel(this);
        }

        public String toDetailedLabel() {
            return buildFutureProfileDetailedLabel(this);
        }
    }

    /**
     * Future-only dashboard card. It is plain data and is not wired into Swing or services.
     */
    public static final class FutureDashboardCard {
        private final String cardTitle;
        private final String cardValue;
        private final String cardSubtitle;
        private final NotificationSeverity severity;
        private final String recommendedAction;

        public FutureDashboardCard(
                String cardTitle,
                String cardValue,
                String cardSubtitle,
                NotificationSeverity severity,
                String recommendedAction) {
            this.cardTitle = safeLabel(cardTitle, "Untitled card");
            this.cardValue = safeLabel(cardValue, "");
            this.cardSubtitle = safeLabel(cardSubtitle, "");
            this.severity = severity == null ? NotificationSeverity.LOW : severity;
            this.recommendedAction = safeLabel(recommendedAction, "No action suggested.");
        }

        public String getCardTitle() {
            return cardTitle;
        }

        public String getCardValue() {
            return cardValue;
        }

        public String getCardSubtitle() {
            return cardSubtitle;
        }

        public NotificationSeverity getSeverity() {
            return severity;
        }

        public String getRecommendedAction() {
            return recommendedAction;
        }

        public String toPlainText() {
            return formatFutureDashboardCardAsPlainText(this);
        }
    }

    /**
     * Future-only dashboard section for grouping dashboard cards without depending on UI widgets.
     */
    public static final class FutureDashboardSection {
        private final String sectionTitle;
        private final List<FutureDashboardCard> cards;

        public FutureDashboardSection(String sectionTitle, List<FutureDashboardCard> cards) {
            this.sectionTitle = safeLabel(sectionTitle, "Future dashboard section");
            if (cards == null || cards.isEmpty()) {
                this.cards = List.of();
            } else {
                this.cards = cards.stream()
                        .filter(Objects::nonNull)
                        .toList();
            }
        }

        public String getSectionTitle() {
            return sectionTitle;
        }

        public List<FutureDashboardCard> getCards() {
            return cards;
        }

        public String toPlainText() {
            return formatFutureDashboardSectionAsPlainText(this);
        }
    }

    /**
     * Future-only dashboard snapshot for MO summary previews.
     */
    public static final class FutureDashboardSnapshot {
        private final int totalProfiles;
        private final int reviewableProfiles;
        private final int strongProfiles;
        private final int riskyProfiles;
        private final List<FutureDashboardSection> sections;
        private final String compactDigest;

        public FutureDashboardSnapshot(
                int totalProfiles,
                int reviewableProfiles,
                int strongProfiles,
                int riskyProfiles,
                List<FutureDashboardSection> sections,
                String compactDigest) {
            this.totalProfiles = Math.max(0, totalProfiles);
            this.reviewableProfiles = Math.max(0, reviewableProfiles);
            this.strongProfiles = Math.max(0, strongProfiles);
            this.riskyProfiles = Math.max(0, riskyProfiles);
            if (sections == null || sections.isEmpty()) {
                this.sections = List.of();
            } else {
                this.sections = sections.stream()
                        .filter(Objects::nonNull)
                        .toList();
            }
            this.compactDigest = safeLabel(compactDigest, "");
        }

        public int getTotalProfiles() {
            return totalProfiles;
        }

        public int getReviewableProfiles() {
            return reviewableProfiles;
        }

        public int getStrongProfiles() {
            return strongProfiles;
        }

        public int getRiskyProfiles() {
            return riskyProfiles;
        }

        public List<FutureDashboardSection> getSections() {
            return sections;
        }

        public String getCompactDigest() {
            return compactDigest;
        }

        public String toPlainText() {
            return formatFutureDashboardSnapshotAsPlainText(this);
        }
    }

    /**
     * Future-use applicant signal object built from primitive values.
     *
     * <p>This avoids depending on UI table columns or adding fields to the current model classes.
     * A later integration can assemble this object from real services when it is ready.
     */
    public static final class FutureApplicantSignal {
        private final String applicationId;
        private final String applicantName;
        private final ApplicationStatus status;
        private final int matchScore;
        private final int currentWorkloadHours;
        private final int acceptedApplicantsForJob;
        private final int jobPositions;
        private final int waitingDays;
        private final JobStatus jobStatus;

        public FutureApplicantSignal(
                String applicationId,
                String applicantName,
                ApplicationStatus status,
                int matchScore,
                int currentWorkloadHours,
                int acceptedApplicantsForJob,
                int jobPositions,
                int waitingDays,
                JobStatus jobStatus) {
            this.applicationId = applicationId == null ? "" : applicationId.trim();
            this.applicantName = applicantName == null ? "" : applicantName.trim();
            this.status = status;
            this.matchScore = normalizeMatchScore(matchScore);
            this.currentWorkloadHours = Math.max(0, currentWorkloadHours);
            this.acceptedApplicantsForJob = Math.max(0, acceptedApplicantsForJob);
            this.jobPositions = Math.max(0, jobPositions);
            this.waitingDays = Math.max(0, waitingDays);
            this.jobStatus = jobStatus;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getApplicantName() {
            return applicantName;
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

        public int getAcceptedApplicantsForJob() {
            return acceptedApplicantsForJob;
        }

        public int getJobPositions() {
            return jobPositions;
        }

        public int getWaitingDays() {
            return waitingDays;
        }

        public JobStatus getJobStatus() {
            return jobStatus;
        }

        public int getFillRatioPercent() {
            return calculateFillRatioPercent(acceptedApplicantsForJob, jobPositions);
        }

        public DecisionSuggestion getDecisionSuggestion() {
            return suggestFutureDecision(this);
        }

        public List<ApplicantAttentionFlag> getAttentionFlags() {
            return identifyApplicantAttentionFlags(this);
        }

        public String toReadableLine() {
            return safeLabel(applicantName, "Unknown applicant")
                    + " | status=" + readableApplicantDecisionState(status)
                    + " | match=" + matchScore + "%"
                    + " | workload=" + currentWorkloadHours + "h/week"
                    + " | fill=" + getFillRatioPercent() + "%"
                    + " | suggestion=" + getDecisionSuggestion().getLabel();
        }
    }

    /**
     * Future-use candidate snapshot for optional MO ranking review helpers.
     *
     * <p>This class is intentionally small and self-contained. It is not a replacement for current
     * domain models and is not integrated with the active workflow.
     */
    public static final class FutureCandidateSnapshot {
        private final String applicationId;
        private final String applicantName;
        private final ApplicationStatus status;
        private final int matchScore;
        private final int previousMatchScore;
        private final int matchedSkillsCount;
        private final int missingSkillsCount;
        private final int currentWorkloadHours;

        public FutureCandidateSnapshot(
                String applicationId,
                String applicantName,
                ApplicationStatus status,
                int matchScore,
                int previousMatchScore,
                int matchedSkillsCount,
                int missingSkillsCount,
                int currentWorkloadHours) {
            this.applicationId = applicationId == null ? "" : applicationId.trim();
            this.applicantName = applicantName == null ? "" : applicantName.trim();
            this.status = status;
            this.matchScore = normalizeMatchScore(matchScore);
            this.previousMatchScore = previousMatchScore < 0 ? -1 : normalizeMatchScore(previousMatchScore);
            this.matchedSkillsCount = Math.max(0, matchedSkillsCount);
            this.missingSkillsCount = Math.max(0, missingSkillsCount);
            this.currentWorkloadHours = Math.max(0, currentWorkloadHours);
        }

        public String getApplicationId() {
            return applicationId;
        }

        public String getApplicantName() {
            return applicantName;
        }

        public ApplicationStatus getStatus() {
            return status;
        }

        public int getMatchScore() {
            return matchScore;
        }

        public int getPreviousMatchScore() {
            return previousMatchScore;
        }

        public int getMatchedSkillsCount() {
            return matchedSkillsCount;
        }

        public int getMissingSkillsCount() {
            return missingSkillsCount;
        }

        public int getCurrentWorkloadHours() {
            return currentWorkloadHours;
        }

        public RankingTrend getRankingTrend() {
            if (previousMatchScore < 0) {
                return RankingTrend.UNKNOWN;
            }
            return classifyRankingTrend(previousMatchScore, matchScore);
        }

        public ReviewPriority getReviewPriority() {
            return recommendReviewPriority(this);
        }

        public CandidateRiskLevel getRiskLevel() {
            return classifyCandidateRiskLevel(this);
        }

        public List<String> getRiskFlags() {
            return identifyApplicantRiskFlags(this);
        }

        public String toReadableLine() {
            return safeLabel(applicantName, "Unknown applicant")
                    + " | status=" + readableStatusLabel(status)
                    + " | match=" + matchScore + "%"
                    + " | trend=" + getRankingTrend().getLabel()
                    + " | missingSkills=" + missingSkillsCount
                    + " | workload=" + currentWorkloadHours + "h/week"
                    + " | priority=" + getReviewPriority().getLabel();
        }
    }

    /**
     * Future-only immutable audit record for possible MO review actions.
     *
     * <p>This class is only a formatting/data helper. It does not write audit records to files,
     * databases, services, repositories, or the current production workflow.
     */
    public static final class FutureReviewAuditEntry {
        private final String candidateId;
        private final String actionType;
        private final ReviewPriority previousPriority;
        private final ReviewPriority newPriority;
        private final String reason;
        private final String timestampText;

        private FutureReviewAuditEntry(
                String candidateId,
                String actionType,
                ReviewPriority previousPriority,
                ReviewPriority newPriority,
                String reason,
                String timestampText) {
            this.candidateId = candidateId;
            this.actionType = actionType;
            this.previousPriority = previousPriority;
            this.newPriority = newPriority;
            this.reason = reason;
            this.timestampText = timestampText;
        }

        public String getCandidateId() {
            return candidateId;
        }

        public String getActionType() {
            return actionType;
        }

        public ReviewPriority getPreviousPriority() {
            return previousPriority;
        }

        public ReviewPriority getNewPriority() {
            return newPriority;
        }

        public String getReason() {
            return reason;
        }

        public String getTimestampText() {
            return timestampText;
        }

        public String toPlainText() {
            return formatFutureReviewAuditEntry(this);
        }
    }

    /**
     * Future-use immutable summary for batches of candidate-like records.
     */
    public static final class FutureRankingSummary {
        private final int totalCandidates;
        private final int strongCandidates;
        private final int mediumCandidates;
        private final int weakCandidates;
        private final double averageScore;
        private final int highestScore;
        private final int lowestScore;

        private FutureRankingSummary(
                int totalCandidates,
                int strongCandidates,
                int mediumCandidates,
                int weakCandidates,
                double averageScore,
                int highestScore,
                int lowestScore) {
            this.totalCandidates = Math.max(0, totalCandidates);
            this.strongCandidates = Math.max(0, strongCandidates);
            this.mediumCandidates = Math.max(0, mediumCandidates);
            this.weakCandidates = Math.max(0, weakCandidates);
            this.averageScore = averageScore < 0 ? 0.0 : averageScore;
            this.highestScore = normalizeMatchScore(highestScore);
            this.lowestScore = normalizeMatchScore(lowestScore);
        }

        public int getTotalCandidates() {
            return totalCandidates;
        }

        public int getStrongCandidates() {
            return strongCandidates;
        }

        public int getMediumCandidates() {
            return mediumCandidates;
        }

        public int getWeakCandidates() {
            return weakCandidates;
        }

        public double getAverageScore() {
            return averageScore;
        }

        public int getHighestScore() {
            return highestScore;
        }

        public int getLowestScore() {
            return lowestScore;
        }

        public String toReadableText() {
            return "Total candidates: " + totalCandidates
                    + "\nStrong candidates: " + strongCandidates
                    + "\nMedium candidates: " + mediumCandidates
                    + "\nWeak candidates: " + weakCandidates
                    + "\nAverage score: " + formatOneDecimal(averageScore)
                    + "\nHighest score: " + highestScore
                    + "\nLowest score: " + lowestScore;
        }
    }

    /**
     * Future-use job dashboard item. It intentionally accepts counts as constructor values because
     * this helper must not reach into repositories or services.
     */
    public static final class FutureJobDashboardItem {
        private final String jobId;
        private final String jobLabel;
        private final int pendingApplications;
        private final int reviewableApplications;
        private final int acceptedApplicants;
        private final int positions;
        private final int highestPendingMatchScore;
        private final JobStatus jobStatus;

        public FutureJobDashboardItem(
                String jobId,
                String jobLabel,
                int pendingApplications,
                int reviewableApplications,
                int acceptedApplicants,
                int positions,
                int highestPendingMatchScore,
                JobStatus jobStatus) {
            this.jobId = jobId == null ? "" : jobId.trim();
            this.jobLabel = safeLabel(jobLabel, "Unknown job");
            this.pendingApplications = Math.max(0, pendingApplications);
            this.reviewableApplications = Math.max(0, reviewableApplications);
            this.acceptedApplicants = Math.max(0, acceptedApplicants);
            this.positions = Math.max(0, positions);
            this.highestPendingMatchScore = normalizeMatchScore(highestPendingMatchScore);
            this.jobStatus = jobStatus;
        }

        public String getJobId() {
            return jobId;
        }

        public String getJobLabel() {
            return jobLabel;
        }

        public int getPendingApplications() {
            return pendingApplications;
        }

        public int getReviewableApplications() {
            return reviewableApplications;
        }

        public int getAcceptedApplicants() {
            return acceptedApplicants;
        }

        public int getPositions() {
            return positions;
        }

        public int getHighestPendingMatchScore() {
            return highestPendingMatchScore;
        }

        public JobStatus getJobStatus() {
            return jobStatus;
        }

        public boolean isFilled() {
            return isJobFilledStatus(jobStatus) || isJobFilled(acceptedApplicants, positions);
        }

        public boolean isCloseToFilled() {
            return !isFilled()
                    && (isJobAlmostFilled(acceptedApplicants, positions)
                    || calculateFillRatioPercent(acceptedApplicants, positions) >= CLOSE_TO_FILLED_PERCENT);
        }

        public boolean needsAttention() {
            return isFilled()
                    || isCloseToFilled()
                    || pendingApplications > 0
                    || reviewableApplications >= MANY_REVIEWABLE_APPLICATIONS;
        }

        public String toReadableLine() {
            return jobLabel
                    + " | pending=" + pendingApplications
                    + " | needsDecision=" + reviewableApplications
                    + " | fill=" + acceptedApplicants + "/" + positions
                    + " | highestPendingMatch=" + highestPendingMatchScore + "%"
                    + " | urgency=" + calculateJobUrgencyScore(this);
        }
    }

    public static final class FutureDashboardSummary {
        private final int totalJobs;
        private final int jobsNeedingAttention;
        private final int jobsAlreadyFilled;
        private final int jobsCloseToFilled;
        private final int highMatchPendingApplicants;
        private final int applicantsNeedingDecision;

        private FutureDashboardSummary(
                int totalJobs,
                int jobsNeedingAttention,
                int jobsAlreadyFilled,
                int jobsCloseToFilled,
                int highMatchPendingApplicants,
                int applicantsNeedingDecision) {
            this.totalJobs = Math.max(0, totalJobs);
            this.jobsNeedingAttention = Math.max(0, jobsNeedingAttention);
            this.jobsAlreadyFilled = Math.max(0, jobsAlreadyFilled);
            this.jobsCloseToFilled = Math.max(0, jobsCloseToFilled);
            this.highMatchPendingApplicants = Math.max(0, highMatchPendingApplicants);
            this.applicantsNeedingDecision = Math.max(0, applicantsNeedingDecision);
        }

        public int getTotalJobs() {
            return totalJobs;
        }

        public int getJobsNeedingAttention() {
            return jobsNeedingAttention;
        }

        public int getJobsAlreadyFilled() {
            return jobsAlreadyFilled;
        }

        public int getJobsCloseToFilled() {
            return jobsCloseToFilled;
        }

        public int getHighMatchPendingApplicants() {
            return highMatchPendingApplicants;
        }

        public int getApplicantsNeedingDecision() {
            return applicantsNeedingDecision;
        }

        public String toReadableText() {
            return "Total jobs: " + totalJobs
                    + "\nJobs needing attention: " + jobsNeedingAttention
                    + "\nJobs already filled: " + jobsAlreadyFilled
                    + "\nJobs close to filled: " + jobsCloseToFilled
                    + "\nHigh-match pending applicants: " + highMatchPendingApplicants
                    + "\nApplicants needing decision: " + applicantsNeedingDecision;
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
