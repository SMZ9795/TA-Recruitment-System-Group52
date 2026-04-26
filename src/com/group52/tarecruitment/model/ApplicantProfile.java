package com.group52.tarecruitment.model;

public class ApplicantProfile {
    private String userId;
    private String skills;
    private String experience;
    private String cvFilePath;

    public ApplicantProfile() {
    }

    public ApplicantProfile(String userId, String skills, String experience, String cvFilePath) {
        this.userId = userId;
        this.skills = skills;
        this.experience = experience;
        this.cvFilePath = cvFilePath;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getCvFilePath() {
        return cvFilePath;
    }

    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath;
    }
}
