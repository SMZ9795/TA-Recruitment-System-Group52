package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.util.IdGenerator;
import com.group52.tarecruitment.util.ValidationUtil;
import java.util.List;
import java.util.Optional;

public class JobService {
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(String jobId) {
        return jobRepository.findById(jobId);
    }

    public List<Job> getJobsByMoId(String moId) {
        return jobRepository.findByPostedByMoId(ValidationUtil.requireText(moId, "MO ID"));
    }

    public Job createJob(String moduleCode, String moduleName, String description, String requiredSkills,
            String hoursPerWeek, String positions, String deadline, String postedByMoId) {
        String normalizedModuleCode = ValidationUtil.requireText(moduleCode, "Module code");
        String normalizedModuleName = ValidationUtil.requireText(moduleName, "Module name");
        String normalizedDescription = ValidationUtil.requireText(description, "Description");
        String normalizedRequiredSkills = ValidationUtil.requireText(requiredSkills, "Required skills");
        int normalizedHoursPerWeek = ValidationUtil.parseIntInRange(hoursPerWeek, "Hours per week", 1, 168);
        int normalizedPositions = ValidationUtil.parsePositiveInt(positions, "Positions");
        String normalizedDeadline = ValidationUtil.requireTodayOrFutureDate(deadline, "Deadline");
        String normalizedPostedByMoId = ValidationUtil.requireText(postedByMoId, "Posted by MO ID");

        Job job = new Job(
                IdGenerator.nextId("JOB"),
                normalizedModuleCode,
                normalizedModuleName,
                normalizedDescription,
                normalizedRequiredSkills,
                normalizedHoursPerWeek,
                normalizedPositions,
                normalizedDeadline,
                normalizedPostedByMoId,
                JobStatus.OPEN);
        jobRepository.save(job);
        return job;
    }

    // Compatibility overload for Swing UI flows.
    public Job createJob(String moduleCode, String moduleName, String description, String requiredSkills,
            int hoursPerWeek, int positions, String deadline, String postedByMoId) {
        return createJob(
                moduleCode,
                moduleName,
                description,
                requiredSkills,
                String.valueOf(hoursPerWeek),
                String.valueOf(positions),
                deadline,
                postedByMoId);
    }

    public Job getJobForMo(String jobId, String moId) {
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");
        String normalizedMoId = ValidationUtil.requireText(moId, "MO ID");

        Job job = jobRepository.findById(normalizedJobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (!job.getPostedByMoId().equalsIgnoreCase(normalizedMoId)) {
            throw new IllegalArgumentException("You can only edit jobs that you posted.");
        }
        return job;
    }

    public Job updateJob(String jobId, String moId, String moduleCode, String moduleName, String description,
            String requiredSkills, String hoursPerWeek, String positions, String deadline) {
        Job job = getJobForMo(jobId, moId);
        long acceptedCount = applicationRepository.countByJobIdAndStatus(job.getId(), ApplicationStatus.ACCEPTED);

        int normalizedPositions = ValidationUtil.parsePositiveInt(positions, "Positions");
        if (normalizedPositions < acceptedCount) {
            throw new IllegalArgumentException(
                    "Positions cannot be less than the number of accepted applications (" + acceptedCount + ").");
        }

        job.setModuleCode(ValidationUtil.requireText(moduleCode, "Module code"));
        job.setModuleName(ValidationUtil.requireText(moduleName, "Module name"));
        job.setDescription(ValidationUtil.requireText(description, "Description"));
        job.setRequiredSkills(ValidationUtil.requireText(requiredSkills, "Required skills"));
        job.setHoursPerWeek(ValidationUtil.parseIntInRange(hoursPerWeek, "Hours per week", 1, 168));
        job.setPositions(normalizedPositions);
        job.setDeadline(ValidationUtil.requireTodayOrFutureDate(deadline, "Deadline"));
        job.setStatus(acceptedCount >= normalizedPositions ? JobStatus.FILLED : JobStatus.OPEN);
        jobRepository.save(job);
        return job;
    }

    // Compatibility overload for Swing UI flows.
    public Job updateJob(Job updatedJob) {
        if (updatedJob == null) {
            throw new IllegalArgumentException("Job is required.");
        }
        return updateJob(
                updatedJob.getId(),
                updatedJob.getPostedByMoId(),
                updatedJob.getModuleCode(),
                updatedJob.getModuleName(),
                updatedJob.getDescription(),
                updatedJob.getRequiredSkills(),
                String.valueOf(updatedJob.getHoursPerWeek()),
                String.valueOf(updatedJob.getPositions()),
                updatedJob.getDeadline());
    }

    public void deleteJob(String jobId) {
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");
        if (!applicationRepository.findByJobId(normalizedJobId).isEmpty()) {
            throw new IllegalArgumentException("Cannot delete a job that has related applications.");
        }
        jobRepository.deleteById(normalizedJobId);
    }
}
