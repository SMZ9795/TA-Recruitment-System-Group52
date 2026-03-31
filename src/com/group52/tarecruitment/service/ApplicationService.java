package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application applyForJob(String jobId, String taUserId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID is required.");
        }
        if (taUserId == null || taUserId.isBlank()) {
            throw new IllegalArgumentException("TA user ID is required.");
        }
        if (applicationRepository.existsByJobIdAndTaUserId(jobId, taUserId)) {
            throw new IllegalArgumentException("This TA has already applied for the job.");
        }

        Application application = new Application(
                IdGenerator.nextId("APP"),
                jobId,
                taUserId,
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
}
