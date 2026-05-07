package com.group52.tarecruitment.service;

import com.group52.tarecruitment.util.ValidationUtil;
import java.util.List;

public interface ApplicantMatchingService {
    MatchDetails match(String applicantSkillsRaw, String requiredSkillsRaw);

    final class MatchDetails {
        private final int score;
        private final List<String> matchedSkills;
        private final List<String> missingSkills;
        private final String reason;

        public MatchDetails(int score, List<String> matchedSkills, List<String> missingSkills, String reason) {
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
