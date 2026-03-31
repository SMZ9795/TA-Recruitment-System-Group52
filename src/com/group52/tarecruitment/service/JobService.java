package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
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

    public Job createJob(String moduleCode, String moduleName, String description, String requiredSkills,
            int hoursPerWeek, int positions, String deadline, String postedByMoId) {
        if (moduleCode == null || moduleCode.isBlank()) {
            throw new IllegalArgumentException("Module code is required.");
        }
        if (moduleName == null || moduleName.isBlank()) {
            throw new IllegalArgumentException("Module name is required.");
        }

        Job job = new Job(
                IdGenerator.nextId("JOB"),
                moduleCode,
                moduleName,
                description,
                requiredSkills,
                hoursPerWeek,
                positions,
                deadline,
                postedByMoId,
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
