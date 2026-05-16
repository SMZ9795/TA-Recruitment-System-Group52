package com.group52.tarecruitment.model;

public class ApplicationAuditLog {
    private String id;
    private String applicationId;
    private String taUserId;
    private String jobId;
    private String operatorUserId;
    private ApplicationStatus fromStatus;
    private ApplicationStatus toStatus;
    private String changedAt;

    public ApplicationAuditLog() {}

    public ApplicationAuditLog(String id, String applicationId, String taUserId, String jobId,
            String operatorUserId, ApplicationStatus fromStatus, ApplicationStatus toStatus, String changedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.taUserId = taUserId;
        this.jobId = jobId;
        this.operatorUserId = operatorUserId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedAt = changedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getTaUserId() { return taUserId; }
    public void setTaUserId(String taUserId) { this.taUserId = taUserId; }
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(String operatorUserId) { this.operatorUserId = operatorUserId; }
    public ApplicationStatus getFromStatus() { return fromStatus; }
    public void setFromStatus(ApplicationStatus fromStatus) { this.fromStatus = fromStatus; }
    public ApplicationStatus getToStatus() { return toStatus; }
    public void setToStatus(ApplicationStatus toStatus) { this.toStatus = toStatus; }
    public String getChangedAt() { return changedAt; }
    public void setChangedAt(String changedAt) { this.changedAt = changedAt; }
}
