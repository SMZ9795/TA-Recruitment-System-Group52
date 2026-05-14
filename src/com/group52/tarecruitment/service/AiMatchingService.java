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
            return new RecommendationResult(0, "Low Fit", "TA profile or job data is missing.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            return new RecommendationResult(0, "Unavailable", "This job is not open for applications.");
        }

        MatchResult matchResult = analyzeSkills(ta.getSkills(), job.getRequiredSkills());
        int remainingHours = Math.max(0, ta.getAvailableHours() - Math.max(0, acceptedHours));
        boolean hoursFit = job.getHoursPerWeek() <= remainingHours || ta.getAvailableHours() <= 0;

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
        String reason = matchResult.getReason()
                + " Remaining hours: " + remainingHours
                + "h/week; job requires " + job.getHoursPerWeek() + "h/week.";
        return new RecommendationResult(score, label, reason);
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

        public RecommendationResult(int score, String label, String reason) {
            this.score = ValidationUtil.parseIntInRange(String.valueOf(score), "Recommendation score", 0, 100);
            this.label = label == null ? "" : label;
            this.reason = reason == null ? "" : reason;
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
    }
}
