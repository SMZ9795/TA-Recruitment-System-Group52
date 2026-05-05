package com.group52.tarecruitment.model;

import java.util.ArrayList;
import java.util.List;

public class Workload {
    private String applicantId;
    private List<String> assignedJobs;

    public Workload() {
        this.assignedJobs = new ArrayList<>();
    }

    public Workload(String applicantId, List<String> assignedJobs) {
        this.applicantId = applicantId;
        this.assignedJobs = assignedJobs == null ? new ArrayList<>() : new ArrayList<>(assignedJobs);
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public List<String> getAssignedJobs() {
        return new ArrayList<>(assignedJobs);
    }

    public void setAssignedJobs(List<String> assignedJobs) {
        this.assignedJobs = assignedJobs == null ? new ArrayList<>() : new ArrayList<>(assignedJobs);
    }
}
