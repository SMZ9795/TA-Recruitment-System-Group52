package com.group52.tarecruitment.service;

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
}
