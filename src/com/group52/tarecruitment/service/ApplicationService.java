package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;

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
}
