package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Notification;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.repository.NotificationRepository;
import com.group52.tarecruitment.util.IdGenerator;
import com.group52.tarecruitment.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification publish(Role role, NotificationType type, String recipientUserId,
            String message, String relatedId) {
        String normalizedRecipientUserId = ValidationUtil.requireText(recipientUserId, "Recipient user ID");
        String normalizedMessage = ValidationUtil.requireText(message, "Notification message");
        String normalizedRelatedId = relatedId == null ? "" : relatedId.trim();
        if (role == null) {
            throw new IllegalArgumentException("Recipient role is required.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required.");
        }

        Notification notification = new Notification(
                IdGenerator.nextId("NTF"),
                normalizedRecipientUserId,
                role,
                type,
                normalizedMessage,
                normalizedRelatedId,
                LocalDateTime.now().toString(),
                false);
        notificationRepository.save(notification);
        return notification;
    }

    public Notification publishIfNotExists(Role role, NotificationType type, String recipientUserId,
            String message, String relatedId) {
        String normalizedRecipientUserId = ValidationUtil.requireText(recipientUserId, "Recipient user ID");
        String normalizedRelatedId = relatedId == null ? "" : relatedId.trim();
        Optional<Notification> existing = findByRecipientTypeAndRelatedId(
                normalizedRecipientUserId, type, normalizedRelatedId);
        if (existing.isPresent()) {
            return existing.get();
        }
        return publish(role, type, normalizedRecipientUserId, message, normalizedRelatedId);
    }

    public List<Notification> getNotificationsForUser(String userId) {
        String normalizedUserId = ValidationUtil.requireText(userId, "User ID");
        List<Notification> notifications = new ArrayList<>(notificationRepository.findByRecipientUserId(normalizedUserId));
        notifications.sort(Comparator.comparing(
                        Notification::getCreatedAt,
                        Comparator.nullsLast(String::compareTo))
                .reversed());
        return notifications;
    }

    public int countUnreadForUser(String userId) {
        return (int) getNotificationsForUser(userId).stream()
                .filter(notification -> !notification.isReadStatus())
                .count();
    }

    public Notification setReadStatus(String notificationId, boolean readStatus) {
        String normalizedNotificationId = ValidationUtil.requireText(notificationId, "Notification ID");
        Notification notification = notificationRepository.findById(normalizedNotificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found."));
        notification.setReadStatus(readStatus);
        notificationRepository.save(notification);
        return notification;
    }

    private Optional<Notification> findByRecipientTypeAndRelatedId(
            String recipientUserId, NotificationType type, String relatedId) {
        if (type == null) {
            return Optional.empty();
        }
        String normalizedRelatedId = relatedId == null ? "" : relatedId.trim();
        return notificationRepository.findByRecipientUserId(recipientUserId).stream()
                .filter(notification -> notification.getType() == type)
                .filter(notification -> normalizedRelatedId.equalsIgnoreCase(notification.getRelatedId()))
                .findFirst();
    }
}
