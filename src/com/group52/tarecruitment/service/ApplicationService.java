package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;
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
