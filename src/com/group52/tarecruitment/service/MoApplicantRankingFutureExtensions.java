package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.JobStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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

    /** Future-only confidence label for a possible MO ranking decision. */
    public static FutureDecisionConfidence estimateDecisionConfidence(
            int matchScore, int missingSkillsCount, int currentWorkloadHours,
            int tieBreakerSignals, ApplicationStatus status) {
        if (!needsDecision(status)) {
            return FutureDecisionConfidence.NEEDS_REVIEW;
        }

        int safeScore = normalizeMatchScore(matchScore);
        int safeMissingSkills = Math.max(0, missingSkillsCount);
        int safeWorkload = Math.max(0, currentWorkloadHours);
        int safeTieBreakers = Math.max(0, tieBreakerSignals);

        int confidenceScore = safeScore
                - (safeMissingSkills * 8)
                - Math.max(0, safeWorkload - MEDIUM_WORKLOAD_HOURS) * 2
                + Math.min(12, safeTieBreakers * 3);

        if (safeMissingSkills >= CRITICAL_MISSING_SKILLS || safeWorkload >= HIGH_WORKLOAD_HOURS + 5) {
            return FutureDecisionConfidence.LOW;
        }
        if (confidenceScore >= 78) {
            return FutureDecisionConfidence.HIGH;
        }
        if (confidenceScore >= 55) {
            return FutureDecisionConfidence.MEDIUM;
        }
        return FutureDecisionConfidence.LOW;
    }

    /** Future-only workload forecast for MO planning and queue sizing. */
    public static FutureWorkloadForecast buildWorkloadForecast(
            int reviewableApplications, int reviewerCount, int minutesPerReview,
            int urgentApplications, int daysAvailable) {
        int safeReviewable = Math.max(0, reviewableApplications);
        int safeReviewers = Math.max(1, reviewerCount);
        int safeMinutes = Math.max(1, minutesPerReview);
        int safeUrgent = Math.max(0, urgentApplications);
        int safeDays = Math.max(1, daysAvailable);
        int totalMinutes = safeReviewable * safeMinutes;
        int dailyCapacityMinutes = safeReviewers * 240;
        int estimatedDays = Math.max(1, (int) Math.ceil(totalMinutes / (double) dailyCapacityMinutes));
        int backlogAfterWindow = Math.max(0, safeReviewable - ((dailyCapacityMinutes * safeDays) / safeMinutes));
        boolean overloaded = estimatedDays > safeDays || safeUrgent > safeReviewers * 3;

        return new FutureWorkloadForecast(safeReviewable, safeReviewers, safeMinutes, safeUrgent,
                safeDays, totalMinutes, estimatedDays, backlogAfterWindow, overloaded);
    }

    /** Future-only notification digest builder for an MO review inbox. */
    public static FutureNotificationDigest buildFutureMoDigest(
            List<FutureCandidateSnapshot> candidates,
            FutureWorkloadForecast forecast,
            FutureDigestTone tone) {
        List<FutureCandidateSnapshot> safeCandidates = safeFutureCandidateSnapshots(candidates);
        FutureDigestTone safeTone = tone == null ? FutureDigestTone.NEUTRAL : tone;
        int urgent = 0;
        int highRisk = 0;
        int highConfidence = 0;

        for (FutureCandidateSnapshot candidate : safeCandidates) {
            if (candidate.getReviewPriority() == ReviewPriority.URGENT) {
                urgent++;
            }
            if (candidate.getRiskLevel() == CandidateRiskLevel.HIGH) {
                highRisk++;
            }
            FutureDecisionConfidence confidence = estimateDecisionConfidence(
                    candidate.getMatchScore(),
                    candidate.getMissingSkillsCount(),
                    candidate.getCurrentWorkloadHours(),
                    candidate.getMatchedSkillsCount(),
                    candidate.getStatus());
            if (confidence == FutureDecisionConfidence.HIGH) {
                highConfidence++;
            }
        }

        String title = safeTone == FutureDigestTone.CONCISE
                ? "MO ranking digest"
                : "Future MO applicant ranking digest";
        String body = "Reviewable candidates: " + safeCandidates.size()
                + "\nUrgent review suggestions: " + urgent
                + "\nHigh-risk applicants: " + highRisk
                + "\nHigh-confidence recommendations: " + highConfidence
                + "\nWorkload: " + (forecast == null ? "No forecast available" : forecast.toReadableText());
        return new FutureNotificationDigest(title, body, urgent, highRisk, highConfidence, safeTone);
    }

    /** Future-only audit summary formatter for explanation and export previews. */
    public static String summarizeAuditEntries(List<String> auditEntries, int maxVisibleEntries) {
        List<String> visibleEntries = new ArrayList<>();
        int safeMax = Math.max(1, maxVisibleEntries);
        int skippedBlankEntries = 0;

        if (auditEntries != null) {
            for (String entry : auditEntries) {
                if (entry == null || entry.isBlank()) {
                    skippedBlankEntries++;
                } else if (visibleEntries.size() < safeMax) {
                    visibleEntries.add(entry.trim());
                }
            }
        }

        int totalEntries = auditEntries == null ? 0 : auditEntries.size();
        int hiddenEntries = Math.max(0, totalEntries - skippedBlankEntries - visibleEntries.size());
        return "Total audit entries: " + totalEntries
                + "\nVisible entries: " + visibleEntries
                + "\nHidden entries: " + hiddenEntries
                + "\nSkipped blank entries: " + skippedBlankEntries;
    }

    /** Future-only normalizer for skill lists used by preview ranking utilities. */
    public static List<String> normalizeSkillList(List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String skill : skills) {
            if (skill != null && !skill.isBlank()) {
                normalized.add(skill.trim().toLowerCase());
            }
        }
        return List.copyOf(normalized);
    }

    /** Future-only required-skill overlap ratio. Returns {@code 0.0} when no requirements exist. */
    public static double calculateSkillOverlapRatio(List<String> requiredSkills, List<String> applicantSkills) {
        List<String> required = normalizeSkillList(requiredSkills);
        List<String> applicant = normalizeSkillList(applicantSkills);
        if (required.isEmpty() || applicant.isEmpty()) {
            return 0.0;
        }
        int matches = 0;
        for (String requiredSkill : required) {
            if (applicant.contains(requiredSkill)) {
                matches++;
            }
        }
        return matches / (double) required.size();
    }

    /** Future-only consistency checks for ranking explanations. */
    public static List<FutureApplicantRiskFlag> detectPotentialRankingWarnings(
            FutureCandidateSnapshot candidate,
            List<String> requiredSkills,
            List<String> applicantSkills) {
        List<FutureApplicantRiskFlag> warnings = new ArrayList<>();
        if (candidate == null) {
            warnings.add(FutureApplicantRiskFlag.INCOMPLETE_PROFILE);
            return List.copyOf(warnings);
        }
        if (candidate.getApplicationId().isBlank() || candidate.getApplicantName().isBlank()) {
            warnings.add(FutureApplicantRiskFlag.INCOMPLETE_PROFILE);
        }
        if (calculateSkillOverlapRatio(requiredSkills, applicantSkills) < 0.35) {
            warnings.add(FutureApplicantRiskFlag.LOW_SKILL_OVERLAP);
        }
        if (candidate.getCurrentWorkloadHours() >= HIGH_WORKLOAD_HOURS) {
            warnings.add(FutureApplicantRiskFlag.WORKLOAD_PRESSURE);
        }
        if (!needsDecision(candidate.getStatus())) {
            warnings.add(FutureApplicantRiskFlag.STATUS_ALREADY_FINAL);
        }
        if (candidate.getRankingTrend() == RankingTrend.DECLINED) {
            warnings.add(FutureApplicantRiskFlag.RANKING_DROP);
        }
        if (warnings.isEmpty()) {
            warnings.add(FutureApplicantRiskFlag.NO_WARNING);
        }
        return List.copyOf(warnings);
    }

    /** Future-only CSV preview builder for candidate-like snapshots. */
    public static String formatFutureCsvPreview(List<FutureCandidateSnapshot> candidates, int maxRows) {
        StringBuilder csv = new StringBuilder();
        csv.append("Application ID,Applicant Name,Status,Match Score,Trend,Risk Level,Priority\n");
        int rows = 0;
        int safeMaxRows = Math.max(1, maxRows);
        for (FutureCandidateSnapshot candidate : safeFutureCandidateSnapshots(candidates)) {
            if (rows >= safeMaxRows) {
                break;
            }
            csv.append(csvValue(candidate.getApplicationId())).append(",");
            csv.append(csvValue(candidate.getApplicantName())).append(",");
            csv.append(csvValue(readableStatusLabel(candidate.getStatus()))).append(",");
            csv.append(candidate.getMatchScore()).append(",");
            csv.append(csvValue(candidate.getRankingTrend().getLabel())).append(",");
            csv.append(csvValue(candidate.getRiskLevel().getLabel())).append(",");
            csv.append(csvValue(candidate.getReviewPriority().getLabel())).append("\n");
            rows++;
        }
        return csv.toString();
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

    /** Future-only confidence labels for ranking recommendation explanations. */
    public enum FutureDecisionConfidence {
        HIGH("High confidence"),
        MEDIUM("Medium confidence"),
        LOW("Low confidence"),
        NEEDS_REVIEW("Needs manual review");

        private final String label;

        FutureDecisionConfidence(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** Future-only warning labels for ranking consistency checks. */
    public enum FutureApplicantRiskFlag {
        INCOMPLETE_PROFILE("Incomplete profile"),
        LOW_SKILL_OVERLAP("Low skill overlap"),
        WORKLOAD_PRESSURE("Workload pressure"),
        STATUS_ALREADY_FINAL("Status already final"),
        RANKING_DROP("Ranking score dropped"),
        NO_WARNING("No warning");

        private final String label;

        FutureApplicantRiskFlag(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    /** Future-only tone option for MO notification digest text. */
    public enum FutureDigestTone {
        CONCISE("Concise"),
        NEUTRAL("Neutral"),
        DETAILED("Detailed");

        private final String label;

        FutureDigestTone(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
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

    /** Future-use immutable workload estimate for MO review planning. */
    public static final class FutureWorkloadForecast {
        public final int reviewableApplications;
        public final int reviewerCount;
        public final int minutesPerReview;
        public final int urgentApplications;
        public final int daysAvailable;
        public final int totalEstimatedMinutes;
        public final int estimatedDaysToClear;
        public final int backlogAfterWindow;
        public final boolean overloaded;

        private FutureWorkloadForecast(
                int reviewableApplications, int reviewerCount, int minutesPerReview,
                int urgentApplications, int daysAvailable, int totalEstimatedMinutes,
                int estimatedDaysToClear, int backlogAfterWindow, boolean overloaded) {
            this.reviewableApplications = reviewableApplications;
            this.reviewerCount = reviewerCount;
            this.minutesPerReview = minutesPerReview;
            this.urgentApplications = urgentApplications;
            this.daysAvailable = daysAvailable;
            this.totalEstimatedMinutes = totalEstimatedMinutes;
            this.estimatedDaysToClear = estimatedDaysToClear;
            this.backlogAfterWindow = backlogAfterWindow;
            this.overloaded = overloaded;
        }

        public String toReadableText() {
            return reviewableApplications + " applications, " + reviewerCount + " reviewers, "
                    + estimatedDaysToClear + " estimated days, " + backlogAfterWindow
                    + " possible backlog, overloaded=" + overloaded;
        }
    }

    /** Future-use immutable notification digest for MO ranking review summaries. */
    public static final class FutureNotificationDigest {
        public final String title;
        public final String body;
        public final int urgentSuggestions;
        public final int highRiskApplicants;
        public final int highConfidenceRecommendations;
        public final FutureDigestTone tone;

        private FutureNotificationDigest(
                String title, String body, int urgentSuggestions, int highRiskApplicants,
                int highConfidenceRecommendations, FutureDigestTone tone) {
            this.title = title;
            this.body = body;
            this.urgentSuggestions = Math.max(0, urgentSuggestions);
            this.highRiskApplicants = Math.max(0, highRiskApplicants);
            this.highConfidenceRecommendations = Math.max(0, highConfidenceRecommendations);
            this.tone = tone == null ? FutureDigestTone.NEUTRAL : tone;
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
