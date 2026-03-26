package com.group52.tarecruitment.model;

public class Job {
    private String id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private String requiredSkills;
    private int hoursPerWeek;
    private int positions;
    private String deadline;
    private String postedByMoId;
    private JobStatus status;

    public Job() {
    }

    public Job(String id, String moduleCode, String moduleName, String description, String requiredSkills,
            int hoursPerWeek, int positions, String deadline, String postedByMoId, JobStatus status) {
        this.id = id;
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.description = description;
        this.requiredSkills = requiredSkills;
        this.hoursPerWeek = hoursPerWeek;
        this.positions = positions;
        this.deadline = deadline;
        this.postedByMoId = postedByMoId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(String requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public int getHoursPerWeek() {
        return hoursPerWeek;
    }

    public void setHoursPerWeek(int hoursPerWeek) {
        this.hoursPerWeek = hoursPerWeek;
    }

    public int getPositions() {
        return positions;
    }

    public void setPositions(int positions) {
        this.positions = positions;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPostedByMoId() {
        return postedByMoId;
    }

    public void setPostedByMoId(String postedByMoId) {
        this.postedByMoId = postedByMoId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }
}
