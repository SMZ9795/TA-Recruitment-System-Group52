package com.group52.tarecruitment.model;

public class Application {
    private String id;
    private String jobId;
    private String taUserId;
    private ApplicationStatus status;
    private String appliedDate;

    public Application() {
    }

    public Application(String id, String jobId, String taUserId, ApplicationStatus status, String appliedDate) {
        this.id = id;
        this.jobId = jobId;
        this.taUserId = taUserId;
        this.status = status;
        this.appliedDate = appliedDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getTaUserId() {
        return taUserId;
    }

    public void setTaUserId(String taUserId) {
        this.taUserId = taUserId;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public String getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(String appliedDate) {
        this.appliedDate = appliedDate;
    }
}
