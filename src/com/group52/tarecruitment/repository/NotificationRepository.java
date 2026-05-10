package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.Notification;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NotificationRepository {
    private static final String HEADER =
            "id,recipientUserId,role,type,message,relatedId,createdAt,readStatus";

    private final Path filePath;

    public NotificationRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<Notification> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<Notification> notifications = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> values = CsvUtil.parseLine(line);
            if (values.size() != 8) {
                throw invalidRecord(i + 1, null);
            }
            try {
                notifications.add(toNotification(values));
            } catch (RuntimeException e) {
                throw invalidRecord(i + 1, e);
            }
        }
        return notifications;
    }

    public Optional<Notification> findById(String notificationId) {
        if (notificationId == null || notificationId.isBlank()) {
            return Optional.empty();
        }
        String normalizedId = notificationId.trim();
        return findAll().stream()
                .filter(notification -> normalizedId.equalsIgnoreCase(notification.getId()))
                .findFirst();
    }

    public List<Notification> findByRecipientUserId(String recipientUserId) {
        if (recipientUserId == null || recipientUserId.isBlank()) {
            return new ArrayList<>();
        }
        String normalizedUserId = recipientUserId.trim();
        return new ArrayList<>(findAll().stream()
                .filter(notification -> normalizedUserId.equalsIgnoreCase(notification.getRecipientUserId()))
                .toList());
    }

    public void save(Notification notification) {
        if (notification == null || notification.getId() == null || notification.getId().isBlank()) {
            throw new IllegalArgumentException("Notification ID is required.");
        }
        String normalizedId = notification.getId().trim();
        notification.setId(normalizedId);
        List<Notification> notifications = findAll();
        notifications.removeIf(existing -> normalizedId.equalsIgnoreCase(existing.getId()));
        notifications.add(notification);
        writeAll(notifications);
    }

    public void saveAll(List<Notification> notifications) {
        writeAll(notifications == null ? List.of() : notifications);
    }

    private void writeAll(List<Notification> notifications) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Notification notification : notifications) {
            lines.add(String.join(",",
                    CsvUtil.escape(notification.getId()),
                    CsvUtil.escape(notification.getRecipientUserId()),
                    CsvUtil.escape(notification.getRole().name()),
                    CsvUtil.escape(notification.getType().name()),
                    CsvUtil.escape(notification.getMessage()),
                    CsvUtil.escape(notification.getRelatedId()),
                    CsvUtil.escape(notification.getCreatedAt()),
                    CsvUtil.escape(String.valueOf(notification.isReadStatus()))));
        }
        FileUtil.writeAllLines(filePath, lines);
    }

    private Notification toNotification(List<String> values) {
        return new Notification(
                values.get(0),
                values.get(1),
                Role.valueOf(values.get(2)),
                NotificationType.valueOf(values.get(3)),
                values.get(4),
                values.get(5),
                values.get(6),
                Boolean.parseBoolean(values.get(7)));
    }

    private IllegalStateException invalidRecord(int lineNumber, RuntimeException cause) {
        String message = "Invalid notification data in " + filePath.getFileName() + " at line " + lineNumber + ".";
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
