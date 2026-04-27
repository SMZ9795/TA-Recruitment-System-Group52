package com.group52.tarecruitment.model;

public class User {
    private String id;
    private Role role;
    private String name;
    private String email;
    private String password;
    private String programme;
    private int yearOfStudy;
    private String skills;
    private int availableHours;
    private boolean active;
    private String cvFilePath;
    private String avatarFilePath;

    public User() {
        this.cvFilePath = "";
        this.avatarFilePath = "";
    }

    public User(String id, Role role, String name, String email, String password, String programme,
            int yearOfStudy, String skills, int availableHours, boolean active) {
        this(id, role, name, email, password, programme, yearOfStudy, skills, availableHours, active, "", "");
    }

    public User(String id, Role role, String name, String email, String password, String programme,
            int yearOfStudy, String skills, int availableHours, boolean active, String cvFilePath) {
        this(id, role, name, email, password, programme, yearOfStudy, skills, availableHours, active, cvFilePath, "");
    }

    public User(String id, Role role, String name, String email, String password, String programme,
            int yearOfStudy, String skills, int availableHours, boolean active, String cvFilePath,
            String avatarFilePath) {
        this.id = id;
        this.role = role;
        this.name = name;
        this.email = email;
        this.password = password;
        this.programme = programme;
        this.yearOfStudy = yearOfStudy;
        this.skills = skills;
        this.availableHours = availableHours;
        this.active = active;
        this.cvFilePath = cvFilePath == null ? "" : cvFilePath;
        this.avatarFilePath = avatarFilePath == null ? "" : avatarFilePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProgramme() {
        return programme;
    }

    public void setProgramme(String programme) {
        this.programme = programme;
    }

    public int getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(int yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(int availableHours) {
        this.availableHours = availableHours;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCvFilePath() {
        return cvFilePath;
    }

    public void setCvFilePath(String cvFilePath) {
        this.cvFilePath = cvFilePath == null ? "" : cvFilePath;
    }

    public String getAvatarFilePath() {
        return avatarFilePath;
    }

    public void setAvatarFilePath(String avatarFilePath) {
        this.avatarFilePath = avatarFilePath == null ? "" : avatarFilePath;
    }
}
