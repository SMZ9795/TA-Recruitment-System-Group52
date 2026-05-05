package com.group52.tarecruitment.util;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class TaNotificationUtil {
    private TaNotificationUtil() {
    }

    public static List<NotificationEntry> buildNotifications(List<Application> applications, List<Job> jobs) {
        List<Application> sortedApplications = new ArrayList<>(applications == null ? List.of() : applications);
        sortedApplications.sort(Comparator.comparing(
                        Application::getAppliedDate,
                        Comparator.nullsLast(String::compareTo))
                .reversed());

        List<NotificationEntry> notifications = new ArrayList<>();
        for (Application application : sortedApplications) {
            Job job = findJob(jobs, application.getJobId());
            String jobName = job == null
                    ? "Job " + safeText(application.getJobId())
                    : safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName());

            notifications.add(notificationForApplication(application, jobName));
            if (job != null
                    && job.getStatus() == JobStatus.CLOSED
                    && application.getStatus() == ApplicationStatus.PENDING) {
                notifications.add(new NotificationEntry(
                        "JOB_CLOSED:" + job.getId() + ":" + application.getId(),
                        "Job Closed",
                        jobName + " is now closed. This pending application may no longer move forward.",
                        safeText(application.getAppliedDate())));
            }
        }
        return notifications;
    }

    public static ApplicationStatusSummary summarizeApplications(List<Application> applications) {
        int pending = 0;
        int accepted = 0;
        int rejected = 0;
        int withdrawn = 0;
        for (Application application : applications == null ? List.<Application>of() : applications) {
            if (application.getStatus() == ApplicationStatus.PENDING) {
                pending++;
            } else if (application.getStatus() == ApplicationStatus.ACCEPTED) {
                accepted++;
            } else if (application.getStatus() == ApplicationStatus.REJECTED) {
                rejected++;
            } else if (application.getStatus() == ApplicationStatus.WITHDRAWN) {
                withdrawn++;
            }
        }
        return new ApplicationStatusSummary(pending, accepted, rejected, withdrawn);
    }

    public static int countUnread(List<NotificationEntry> notifications, Set<String> readNotificationIds) {
        int unreadCount = 0;
        for (NotificationEntry notification : notifications == null ? List.<NotificationEntry>of() : notifications) {
            if (readNotificationIds == null || !readNotificationIds.contains(notification.getId())) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    public static List<NotificationEntry> filterByReadState(
            List<NotificationEntry> notifications, Set<String> readNotificationIds, String filter) {
        String normalizedFilter = filter == null ? "All" : filter;
        List<NotificationEntry> filtered = new ArrayList<>();
        for (NotificationEntry notification : notifications == null ? List.<NotificationEntry>of() : notifications) {
            boolean isRead = readNotificationIds != null && readNotificationIds.contains(notification.getId());
            if ("Unread".equals(normalizedFilter) && isRead) {
                continue;
            }
            if ("Read".equals(normalizedFilter) && !isRead) {
                continue;
            }
            filtered.add(notification);
        }
        return filtered;
    }

    public static String jobClosedApplyMessage(Job job) {
        if (job == null || job.getStatus() != JobStatus.CLOSED) {
            return "";
        }
        return safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName())
                + " is closed and no longer accepts applications.";
    }

    private static NotificationEntry notificationForApplication(Application application, String jobName) {
        ApplicationStatus status = application.getStatus();
        String type = "Application Update";
        String message = "Your application for " + jobName + " has been updated.";
        if (status == ApplicationStatus.PENDING) {
            type = "Application Submitted";
            message = "Your application for " + jobName + " is waiting for MO review.";
        } else if (status == ApplicationStatus.ACCEPTED) {
            type = "Application Accepted";
            message = "Your application for " + jobName + " has been accepted.";
        } else if (status == ApplicationStatus.REJECTED) {
            type = "Application Rejected";
            message = "Your application for " + jobName + " has been rejected.";
        } else if (status == ApplicationStatus.WITHDRAWN) {
            type = "Application Withdrawn";
            message = "Your application for " + jobName + " was withdrawn successfully.";
        }
        return new NotificationEntry(
                "APP:" + application.getId() + ":" + (status == null ? "UNKNOWN" : status.name()),
                type,
                message,
                safeText(application.getAppliedDate()));
    }

    private static Job findJob(List<Job> jobs, String jobId) {
        if (jobs == null || jobId == null || jobId.isBlank()) {
            return null;
        }
        for (Job job : jobs) {
            if (job.getId() != null && job.getId().equalsIgnoreCase(jobId)) {
                return job;
            }
        }
        return null;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    public static final class NotificationEntry {
        private final String id;
        private final String type;
        private final String message;
        private final String date;

        public NotificationEntry(String id, String type, String message, String date) {
            this.id = id;
            this.type = type;
            this.message = message;
            this.date = date;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        public String getDate() {
            return date;
        }
    }

    public static final class ApplicationStatusSummary {
        private final int pending;
        private final int accepted;
        private final int rejected;
        private final int withdrawn;

        private ApplicationStatusSummary(int pending, int accepted, int rejected, int withdrawn) {
            this.pending = pending;
            this.accepted = accepted;
            this.rejected = rejected;
            this.withdrawn = withdrawn;
        }

        public int getPending() {
            return pending;
        }

        public int getAccepted() {
            return accepted;
        }

        public int getRejected() {
            return rejected;
        }

        public int getWithdrawn() {
            return withdrawn;
        }

        public String format() {
            return "Applications: "
                    + pending + " pending, "
                    + accepted + " accepted, "
                    + rejected + " rejected, "
                    + withdrawn + " withdrawn.";
        }
    }
}
