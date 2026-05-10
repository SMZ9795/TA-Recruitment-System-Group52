package com.group52.tarecruitment.model;

public class Notification {
    private String id;
    private String recipientUserId;
    private Role role;
    private NotificationType type;
    private String message;
    private String relatedId;
    private String createdAt;
    private boolean readStatus;

    public Notification() {
    }

    public Notification(String id, String recipientUserId, Role role, NotificationType type, String message,
            String relatedId, String createdAt, boolean readStatus) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.role = role;
        this.type = type;
        this.message = message;
        this.relatedId = relatedId;
        this.createdAt = createdAt;
        this.readStatus = readStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }
}
