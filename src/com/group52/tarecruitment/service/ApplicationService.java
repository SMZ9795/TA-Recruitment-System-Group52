package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getApplicationsByTaUserId(String taUserId) {
        return applicationRepository.findByTaUserId(taUserId);
    }

    public List<Application> getApplicationsByJobId(String jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public Optional<Application> getApplicationById(String applicationId) {
        return applicationRepository.findById(applicationId);
    }

    public List<Application> getApplicationsForMo(String moUserId) {
        String normalizedMoUserId = requireText(moUserId, "MO user ID");
        Set<String> jobIds = new HashSet<>();
        for (Job job : jobRepository.findByPostedByMoId(normalizedMoUserId)) {
            jobIds.add(job.getId());
        }
        return applicationRepository.findAll().stream()
                .filter(application -> jobIds.contains(application.getJobId()))
                .toList();
    }

    public void updateStatus(String applicationId, ApplicationStatus status) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalArgumentException("Application ID is required.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Application status is required.");
        }
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        application.setStatus(status);
        applicationRepository.save(application);
    }

    public Application updateApplicationStatus(String applicationId, String moUserId, ApplicationStatus status) {
        String normalizedApplicationId = requireText(applicationId, "Application ID");
        String normalizedMoUserId = requireText(moUserId, "MO user ID");
        if (status == null) {
            throw new IllegalArgumentException("Application status is required.");
        }

        Application application = applicationRepository.findById(normalizedApplicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        Job job = jobRepository.findById(application.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (!job.getPostedByMoId().equalsIgnoreCase(normalizedMoUserId)) {
            throw new IllegalArgumentException("You can only review applications for your own jobs.");
        }

        application.setStatus(status);
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
    }
}
