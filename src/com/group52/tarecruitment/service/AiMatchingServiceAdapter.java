package com.group52.tarecruitment.service;

public class AiMatchingServiceAdapter implements ApplicantMatchingService {
    private final AiMatchingService delegate;

    public AiMatchingServiceAdapter() {
        this(new AiMatchingService());
    }

    public AiMatchingServiceAdapter(AiMatchingService delegate) {
        this.delegate = delegate;
    }

    @Override
    public MatchDetails match(String applicantSkillsRaw, String requiredSkillsRaw) {
        AiMatchingService.MatchResult result = delegate.analyzeSkills(applicantSkillsRaw, requiredSkillsRaw);
        return new MatchDetails(
                result.getScore(),
                result.getMatchedSkills(),
                result.getMissingSkills(),
                result.getReason());
    }
}
