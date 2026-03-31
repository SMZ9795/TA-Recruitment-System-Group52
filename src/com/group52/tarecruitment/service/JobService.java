package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public List<Job> getJobsByMoId(String moId) {
        return jobRepository.findByPostedByMoId(moId);
    }

    public Optional<Job> getJobById(String jobId) {
        return jobRepository.findById(jobId);
    }

    public List<Job> getJobsByMoId(String moId) {
        if (moId == null || moId.isBlank()) {
            throw new IllegalArgumentException("MO ID is required.");
        }
        return jobRepository.findByPostedByMoId(moId.trim());
    }

    public Job createJob(String moduleCode, String moduleName, String description, String requiredSkills,
            int hoursPerWeek, int positions, String deadline, String postedByMoId) {
        String normalizedModuleCode = requireText(moduleCode, "Module code");
        String normalizedModuleName = requireText(moduleName, "Module name");
        String normalizedDescription = requireText(description, "Description");
        String normalizedRequiredSkills = requireText(requiredSkills, "Required skills");
        String normalizedDeadline = requireText(deadline, "Deadline");
        String normalizedPostedByMoId = requireText(postedByMoId, "Posted by MO ID");

        if (hoursPerWeek <= 0) {
            throw new IllegalArgumentException("Hours per week must be greater than 0.");
        }
        if (positions <= 0) {
            throw new IllegalArgumentException("Positions must be greater than 0.");
        }

        validateDeadline(normalizedDeadline);

        Job job = new Job(
                IdGenerator.nextId("JOB"),
                normalizedModuleCode,
                normalizedModuleName,
                normalizedDescription,
                normalizedRequiredSkills,
                hoursPerWeek,
                positions,
                normalizedDeadline,
                normalizedPostedByMoId,
                JobStatus.OPEN);
        jobRepository.save(job);
        return job;
    }

    public void updateJob(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job is required.");
        }
        jobRepository.save(job);
    }

    public void deleteJob(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID is required.");
        }
        jobRepository.deleteById(jobId);
    }
}
