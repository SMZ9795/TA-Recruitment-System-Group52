package com.group52.tarecruitment.model;

public enum ApplicationStatus {
    APPLIED,
    REVIEWING,
    PENDING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    public static ApplicationStatus fromStorageValue(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Application status is missing.");
        }
        String normalized = rawValue.trim().toUpperCase();
        if ("PENDING".equals(normalized)) {
            return PENDING;
        }
        return ApplicationStatus.valueOf(normalized);
    }
}
