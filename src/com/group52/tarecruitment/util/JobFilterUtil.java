package com.group52.tarecruitment.util;

import com.group52.tarecruitment.model.Job;

public final class JobFilterUtil {
    private JobFilterUtil() {
    }

    public static boolean matches(Job job, String keyword, String skillQuery, Integer maxHours,
            String moQuery, String statusValue, String moDisplayName) {
        if (job == null) {
            return false;
        }

        String normalizedKeyword = normalize(keyword);
        String normalizedSkillQuery = normalize(skillQuery);
        String normalizedMoQuery = normalize(moQuery);
        String normalizedStatus = normalize(statusValue);

        String moduleCode = normalize(job.getModuleCode());
        String moduleName = normalize(job.getModuleName());
        String requiredSkills = normalize(job.getRequiredSkills());
        String postedByMoId = normalize(job.getPostedByMoId());
        String moName = normalize(moDisplayName);
        String jobStatus = job.getStatus() == null ? "" : job.getStatus().name();

        boolean matchedKeyword = normalizedKeyword.isBlank()
                || moduleCode.contains(normalizedKeyword)
                || moduleName.contains(normalizedKeyword)
                || requiredSkills.contains(normalizedKeyword)
                || moName.contains(normalizedKeyword);
        boolean matchedSkill = normalizedSkillQuery.isBlank() || requiredSkills.contains(normalizedSkillQuery);
        boolean matchedMo = normalizedMoQuery.isBlank()
                || moName.contains(normalizedMoQuery)
                || postedByMoId.contains(normalizedMoQuery);
        boolean matchedStatus = normalizedStatus.isBlank()
                || "all".equals(normalizedStatus)
                || jobStatus.equalsIgnoreCase(normalizedStatus);
        boolean matchedHours = maxHours == null || job.getHoursPerWeek() <= maxHours;

        return matchedKeyword && matchedSkill && matchedMo && matchedStatus && matchedHours;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
