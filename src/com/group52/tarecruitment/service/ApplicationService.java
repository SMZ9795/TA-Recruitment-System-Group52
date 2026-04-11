package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
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

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public Application applyForJob(String jobId, String taUserId) {
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");
        String normalizedTaUserId = ValidationUtil.requireText(taUserId, "TA user ID");

        Job job = jobRepository.findById(normalizedJobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        validateJobIsOpen(job);
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
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending applications can be withdrawn.");
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);

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
        if (application.getStatus() != ApplicationStatus.PENDING) {
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
            long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);
            if (acceptedCount >= job.getPositions()) {
                job.setStatus(JobStatus.FILLED);
                jobRepository.save(job);
            }
        }

        return application;
    }

    private void validateJobIsOpen(Job job) {
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("This job is not open for applications.");
        }

        try {
            if (LocalDate.parse(job.getDeadline()).isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("This job has passed its deadline.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("The job deadline is invalid.");
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
}
