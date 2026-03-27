package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public Application applyForJob(String jobId, String taUserId) {
        String normalizedJobId = requireText(jobId, "Job ID");
        String normalizedTaUserId = requireText(taUserId, "TA user ID");

        Job job = jobRepository.findById(normalizedJobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        validateJobIsOpen(job);

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
        return applicationRepository.findByTaUserId(requireText(taUserId, "TA user ID"));
    }

    public List<Application> getApplicationsForMo(String moId) {
        String normalizedMoId = requireText(moId, "MO ID");
        return jobRepository.findByPostedByMoId(normalizedMoId).stream()
                .flatMap(job -> applicationRepository.findByJobId(job.getId()).stream())
                .toList();
    }

    public Application updateApplicationStatus(String applicationId, String moId, ApplicationStatus newStatus) {
        String normalizedApplicationId = requireText(applicationId, "Application ID");
        String normalizedMoId = requireText(moId, "MO ID");
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

        application.setStatus(newStatus);
        applicationRepository.save(application);
        return application;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
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
}
