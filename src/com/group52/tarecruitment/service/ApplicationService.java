package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import com.group52.tarecruitment.util.ValidationUtil;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final WorkloadService workloadService;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this(applicationRepository, jobRepository, null, null);
    }

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
            WorkloadService workloadService) {
        this(applicationRepository, jobRepository, workloadService, null);
    }

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
            WorkloadService workloadService, NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.workloadService = workloadService;
        this.notificationService = notificationService;
    }

    public Application applyForJob(String jobId, String taUserId) {
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");
        String normalizedTaUserId = ValidationUtil.requireText(taUserId, "TA user ID");

        Job job = jobRepository.findById(normalizedJobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        try {
            validateJobIsOpen(job);
        } catch (IllegalArgumentException ex) {
            maybePublishJobClosedApplyBlockedNotification(job, normalizedTaUserId);
            throw ex;
        }
        validateJobHasCapacity(job);

        if (applicationRepository.existsByJobIdAndTaUserId(normalizedJobId, normalizedTaUserId)) {
            throw new IllegalArgumentException("This TA has already applied for the job.");
        }

        Application application = new Application(
                IdGenerator.nextId("APP"),
                normalizedJobId,
                normalizedTaUserId,
                ApplicationStatus.PENDING,
                LocalDate.now().toString());
        applicationRepository.save(application);
        publishTaNotification(
                NotificationType.APPLY,
                normalizedTaUserId,
                application.getId(),
                "Application submitted for "
                        + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()) + ".");
        return application;
    }

    public List<Application> getApplicationsByTaUserId(String taUserId) {
        return new ArrayList<>(applicationRepository.findByTaUserId(
                ValidationUtil.requireText(taUserId, "TA user ID")));
    }

    public List<Application> getApplicationsForMo(String moId) {
        String normalizedMoId = ValidationUtil.requireText(moId, "MO ID");
        return jobRepository.findByPostedByMoId(normalizedMoId).stream()
                .flatMap(job -> applicationRepository.findByJobId(job.getId()).stream())
                .toList();
    }

    public int getPendingApplicationCountForMo(String moId) {
        return countApplicationsForMoByStatus(moId, ApplicationStatus.PENDING);
    }

    public int getReviewableApplicationCountForMo(String moId) {
        int count = 0;
        for (Application application : getApplicationsForMo(moId)) {
            if (isReviewableStatus(application.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // Compatibility APIs for Swing UI flows.
    public Optional<Application> getApplicationById(String applicationId) {
        if (applicationId == null || applicationId.isBlank()) {
            return Optional.empty();
        }
        return applicationRepository.findById(applicationId.trim());
    }

    public List<Application> getApplicationsByJobId(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return List.of();
        }
        return new ArrayList<>(applicationRepository.findByJobId(jobId.trim()));
    }

    public int getAcceptedWorkloadHoursForTa(String taUserId) {
        if (taUserId == null || taUserId.isBlank()) {
            return 0;
        }
        int totalHours = 0;
        for (Application application : applicationRepository.findByTaUserId(taUserId.trim())) {
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                continue;
            }
            totalHours += jobRepository.findById(application.getJobId())
                    .map(Job::getHoursPerWeek)
                    .orElse(0);
        }
        return totalHours;
    }

    public Application updateStatus(String applicationId, ApplicationStatus newStatus) {
        throw new IllegalArgumentException("Use updateStatus(applicationId, operatorUserId, newStatus).");
    }

    public Application updateStatus(String applicationId, String operatorUserId, ApplicationStatus newStatus) {
        String normalizedApplicationId = ValidationUtil.requireText(applicationId, "Application ID");
        String normalizedOperatorUserId = ValidationUtil.requireText(operatorUserId, "Operator user ID");
        if (newStatus == null) {
            throw new IllegalArgumentException("Application status is required.");
        }

        Application application = applicationRepository.findById(normalizedApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found for the application."));
        if (newStatus != ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("Use MO review flow to set this status.");
        }
        if (!application.getTaUserId().equalsIgnoreCase(normalizedOperatorUserId)) {
            throw new IllegalArgumentException("You can only withdraw your own application.");
        }
        if (!isReviewableStatus(application.getStatus())) {
            throw new IllegalArgumentException("Only pending applications can be withdrawn.");
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        publishTaNotification(
                NotificationType.WITHDRAW,
                application.getTaUserId(),
                application.getId(),
                "Application withdrawn successfully for "
                        + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()) + ".");

        long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);
        if (acceptedCount >= job.getPositions()) {
            job.setStatus(JobStatus.FILLED);
        } else if (job.getStatus() == JobStatus.FILLED) {
            job.setStatus(JobStatus.OPEN);
        }
        jobRepository.save(job);
        return application;
    }

    public Application updateApplicationStatus(String applicationId, String moId, ApplicationStatus newStatus) {
        String normalizedApplicationId = ValidationUtil.requireText(applicationId, "Application ID");
        String normalizedMoId = ValidationUtil.requireText(moId, "MO ID");
        if (newStatus == null) {
            throw new IllegalArgumentException("Application status is required.");
        }
        if (newStatus == ApplicationStatus.WITHDRAWN) {
            throw new IllegalArgumentException("MO cannot set status to WITHDRAWN.");
        }

        Application application = applicationRepository.findById(normalizedApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found for the application."));

        if (!job.getPostedByMoId().equalsIgnoreCase(normalizedMoId)) {
            throw new IllegalArgumentException("You can only review applications for your own jobs.");
        }
        if (!isReviewableStatus(application.getStatus())) {
            throw new IllegalArgumentException("Only pending applications can be reviewed.");
        }

        if (newStatus == ApplicationStatus.ACCEPTED) {
            long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);
            if (acceptedCount >= job.getPositions()) {
                job.setStatus(JobStatus.FILLED);
                jobRepository.save(job);
                throw new IllegalArgumentException("This job has already reached its positions limit.");
            }
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        if (newStatus == ApplicationStatus.ACCEPTED) {
            publishTaNotification(
                    NotificationType.ACCEPT,
                    application.getTaUserId(),
                    application.getId(),
                    "Your application for "
                            + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName())
                            + " has been accepted.");
        } else if (newStatus == ApplicationStatus.REJECTED) {
            publishTaNotification(
                    NotificationType.REJECT,
                    application.getTaUserId(),
                    application.getId(),
                    "Your application for "
                            + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName())
                            + " has been rejected.");
        }
        if (workloadService != null) {
            if (newStatus == ApplicationStatus.ACCEPTED) {
                workloadService.assignJob(application.getTaUserId(), application.getJobId());
            } else if (newStatus == ApplicationStatus.REJECTED) {
                workloadService.unassignJob(application.getTaUserId(), application.getJobId());
            }
        }

        if (newStatus == ApplicationStatus.ACCEPTED) {
            refreshJobFilledStatus(job);
        }

        return application;
    }

    private int countApplicationsForMoByStatus(String moId, ApplicationStatus status) {
        if (status == null) {
            return 0;
        }
        int count = 0;
        for (Application application : getApplicationsForMo(moId)) {
            if (application.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    private void refreshJobFilledStatus(Job job) {
        if (job == null || job.getStatus() == JobStatus.CLOSED) {
            return;
        }
        long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);
        JobStatus refreshedStatus = acceptedCount >= job.getPositions() ? JobStatus.FILLED : JobStatus.OPEN;
        if (job.getStatus() != refreshedStatus) {
            job.setStatus(refreshedStatus);
            jobRepository.save(job);
        }
    }

    private void validateJobIsOpen(Job job) {
        if (job.getStatus() == JobStatus.CLOSED) {
            throw new IllegalArgumentException("This job is closed and no longer accepts applications.");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("This job is not open for applications.");
        }

        final LocalDate deadlineDate;
        try {
            deadlineDate = LocalDate.parse(job.getDeadline());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("The job deadline is invalid.");
        }
        if (deadlineDate.isBefore(LocalDate.now())) {
            // Self-heal: an OPEN job past its deadline should be CLOSED before we reject the apply.
            job.setStatus(JobStatus.CLOSED);
            jobRepository.save(job);
            throw new IllegalArgumentException("This job has passed its deadline.");
        }
    }

    private void validateJobHasCapacity(Job job) {
        long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);
        if (acceptedCount >= job.getPositions()) {
            if (job.getStatus() != JobStatus.FILLED) {
                job.setStatus(JobStatus.FILLED);
                jobRepository.save(job);
            }
            throw new IllegalArgumentException("This job has already reached its positions limit.");
        }
    }

    private boolean isReviewableStatus(ApplicationStatus status) {
        return status == ApplicationStatus.APPLIED
                || status == ApplicationStatus.REVIEWING
                || status == ApplicationStatus.PENDING;
    }

    private void maybePublishJobClosedApplyBlockedNotification(Job job, String taUserId) {
        if (notificationService == null || job == null || taUserId == null || taUserId.isBlank()) {
            return;
        }
        if (job.getStatus() != JobStatus.CLOSED) {
            return;
        }
        String message = safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName())
                + " is closed and no longer accepts applications.";
        notificationService.publishIfNotExists(
                Role.TA,
                NotificationType.JOB_CLOSE,
                taUserId,
                message,
                "JOB_CLOSE_APPLY_BLOCKED:" + safeText(job.getId()));
    }

    private void publishTaNotification(NotificationType type, String taUserId, String relatedId, String message) {
        if (notificationService == null || taUserId == null || taUserId.isBlank()) {
            return;
        }
        notificationService.publish(Role.TA, type, taUserId, message, safeText(relatedId));
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }
}
