package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.util.ValidationUtil;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AiMatchingService {
    public MatchResult analyzeSkills(String applicantSkillsRaw, String requiredSkillsRaw) {
        Set<String> applicantSkills = tokenizeSkills(applicantSkillsRaw);
        Set<String> requiredSkills = tokenizeSkills(requiredSkillsRaw);

        if (requiredSkills.isEmpty()) {
            return new MatchResult(
                    100,
                    List.of(),
                    List.of(),
                    "No required skills were specified for this job.");
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String required : requiredSkills) {
            if (applicantSkills.contains(required)) {
                matched.add(required);
            } else {
                missing.add(required);
            }
        }

        int score = (int) Math.round((matched.size() * 100.0) / requiredSkills.size());
        String reason = "Matched " + matched.size() + " of " + requiredSkills.size()
                + " required skills based on normalized keyword overlap.";
        return new MatchResult(score, matched, missing, reason);
    }

    public RecommendationResult recommendJob(User ta, Job job, int acceptedHours) {
        if (ta == null || job == null) {
            return new RecommendationResult(
                    0,
                    "Low Fit",
                    "TA profile or job data is missing.",
                    false,
                    0,
                    "Review profile and job data before applying.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            return new RecommendationResult(
                    0,
                    "Unavailable",
                    "This job is not open for applications.",
                    false,
                    0,
                    "Choose another open job.");
        }

        MatchResult matchResult = analyzeSkills(ta.getSkills(), job.getRequiredSkills());
        int remainingHours = Math.max(0, ta.getAvailableHours() - Math.max(0, acceptedHours));
        boolean availabilityNotSet = ta.getAvailableHours() <= 0;
        boolean hoursFit = availabilityNotSet || job.getHoursPerWeek() <= remainingHours;

        int score = matchResult.getScore();
        if (hoursFit) {
            score = Math.min(100, score + 10);
        } else {
            score = Math.max(0, score - 20);
        }

        String label;
        if (score >= 80 && hoursFit) {
            label = "Recommended";
        } else if (score >= 50) {
            label = "Review";
        } else {
            label = "Low Fit";
        }
        String matched = matchResult.getMatchedSkills().isEmpty()
                ? "none"
                : String.join(", ", matchResult.getMatchedSkills());
        String missing = matchResult.getMissingSkills().isEmpty()
                ? "none"
                : String.join(", ", matchResult.getMissingSkills());
        String hoursDetail = availabilityNotSet
                ? "Availability is not set, so this is treated as flexible but should be confirmed."
                : hoursFit
                        ? "Hours fit: " + remainingHours + "h/week remaining for a "
                                + job.getHoursPerWeek() + "h/week job."
                        : "Hours risk: only " + remainingHours + "h/week remains, but this job needs "
                                + job.getHoursPerWeek() + "h/week.";
        String actionHint;
        if ("Recommended".equals(label)) {
            actionHint = "Strong match: apply first if the module is interesting.";
        } else if ("Review".equals(label)) {
            actionHint = "Review before applying: check the missing skills or weekly hours.";
        } else {
            actionHint = "Lower priority: consider improving the profile match or choosing another job.";
        }
        String reason = matchResult.getReason()
                + " Matched skills: " + matched + "."
                + " Missing skills: " + missing + "."
                + " " + hoursDetail;
        return new RecommendationResult(score, label, reason, hoursFit, remainingHours, actionHint);
    }

    private Set<String> tokenizeSkills(String rawSkills) {
        Set<String> skills = new LinkedHashSet<>();
        if (rawSkills == null || rawSkills.isBlank()) {
            return skills;
        }
        String[] parts = rawSkills.split("[,;|/\\n\\r\\t]");
        for (String part : parts) {
            String cleaned = part.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                skills.add(cleaned);
            }
        }
        return skills;
    }

    public static final class MatchResult {
        private final int score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final String reason;

        public MatchResult(int score, List<String> matchedSkills, List<String> missingSkills, String reason) {
            this.score = ValidationUtil.parseIntInRange(String.valueOf(score), "Match score", 0, 100);
            this.matchedSkills = List.copyOf(matchedSkills == null ? List.of() : matchedSkills);
            this.missingSkills = List.copyOf(missingSkills == null ? List.of() : missingSkills);
            this.reason = reason == null ? "" : reason;
        }

        public int getScore() {
            return score;
        }

        public List<String> getMatchedSkills() {
            return matchedSkills;
        }

        public List<String> getMissingSkills() {
            return missingSkills;
        }

        public String getReason() {
            return reason;
        }
    }

    public static final class RecommendationResult {
        private final int score;
        private final String label;
        private final String reason;
        private final boolean hoursFit;
        private final int remainingHours;
        private final String actionHint;

        public RecommendationResult(int score, String label, String reason) {
            this(score, label, reason, false, 0, "");
        }

        public RecommendationResult(
                int score, String label, String reason, boolean hoursFit, int remainingHours, String actionHint) {
            this.score = ValidationUtil.parseIntInRange(String.valueOf(score), "Recommendation score", 0, 100);
            this.label = label == null ? "" : label;
            this.reason = reason == null ? "" : reason;
            this.hoursFit = hoursFit;
            this.remainingHours = Math.max(0, remainingHours);
            this.actionHint = actionHint == null ? "" : actionHint;
        }

        public int getScore() {
            return score;
        }

        public String getLabel() {
            return label;
        }

        public String getReason() {
            return reason;
        }

        public boolean isRecommended() {
            return "Recommended".equalsIgnoreCase(label);
        }

        public boolean isHoursFit() {
            return hoursFit;
        }

        public int getRemainingHours() {
            return remainingHours;
        }

        public String getActionHint() {
            return actionHint;
        }
    }
}
