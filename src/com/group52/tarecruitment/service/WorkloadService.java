package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Workload;
import com.group52.tarecruitment.repository.WorkloadRepository;
import com.group52.tarecruitment.util.ValidationUtil;
import java.util.ArrayList;
import java.util.List;

public class WorkloadService {
    private final WorkloadRepository workloadRepository;

    public WorkloadService(WorkloadRepository workloadRepository) {
        this.workloadRepository = workloadRepository;
    }

    public void assignJob(String applicantId, String jobId) {
        String normalizedApplicantId = ValidationUtil.requireText(applicantId, "Applicant ID");
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");

        Workload workload = workloadRepository.findByApplicantId(normalizedApplicantId)
                .orElse(new Workload(normalizedApplicantId, new ArrayList<>()));
        List<String> assignedJobs = workload.getAssignedJobs();
        if (!assignedJobs.contains(normalizedJobId)) {
            assignedJobs.add(normalizedJobId);
            workload.setAssignedJobs(assignedJobs);
            workloadRepository.save(workload);
        }
    }

    public void unassignJob(String applicantId, String jobId) {
        String normalizedApplicantId = ValidationUtil.requireText(applicantId, "Applicant ID");
        String normalizedJobId = ValidationUtil.requireText(jobId, "Job ID");
        Workload workload = workloadRepository.findByApplicantId(normalizedApplicantId).orElse(null);
        if (workload == null) {
            return;
        }
        List<String> assignedJobs = workload.getAssignedJobs();
        if (assignedJobs.removeIf(job -> job.equalsIgnoreCase(normalizedJobId))) {
            workload.setAssignedJobs(assignedJobs);
            workloadRepository.save(workload);
        }
    }

    public List<Workload> getAllWorkloads() {
        return workloadRepository.findAll();
    }
}
