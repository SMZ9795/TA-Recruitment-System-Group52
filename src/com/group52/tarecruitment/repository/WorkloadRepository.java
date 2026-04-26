package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.Workload;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WorkloadRepository {
    private static final String HEADER = "applicantId,assignedJobs";
    private static final String JOB_DELIMITER = ";";

    private final Path filePath;

    public WorkloadRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<Workload> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<Workload> workloads = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> values = CsvUtil.parseLine(line);
            if (values.size() != 2) {
                throw invalidRecord(i + 1, null);
            }
            try {
                workloads.add(toWorkload(values));
            } catch (RuntimeException e) {
                throw invalidRecord(i + 1, e);
            }
        }
        return workloads;
    }

    public Optional<Workload> findByApplicantId(String applicantId) {
        if (applicantId == null || applicantId.isBlank()) {
            return Optional.empty();
        }
        String normalized = applicantId.trim();
        return findAll().stream()
                .filter(workload -> workload.getApplicantId().equalsIgnoreCase(normalized))
                .findFirst();
    }

    public void save(Workload workload) {
        if (workload == null || workload.getApplicantId() == null || workload.getApplicantId().isBlank()) {
            throw new IllegalArgumentException("Applicant ID is required.");
        }
        String normalizedApplicantId = workload.getApplicantId().trim();
        workload.setApplicantId(normalizedApplicantId);
        List<Workload> workloads = findAll();
        workloads.removeIf(existing -> existing.getApplicantId().equalsIgnoreCase(normalizedApplicantId));
        workloads.add(workload);
        writeAll(workloads);
    }

    private void writeAll(List<Workload> workloads) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Workload workload : workloads) {
            lines.add(String.join(",",
                    CsvUtil.escape(workload.getApplicantId()),
                    CsvUtil.escape(String.join(JOB_DELIMITER, workload.getAssignedJobs()))));
        }
        FileUtil.writeAllLines(filePath, lines);
    }

    private Workload toWorkload(List<String> values) {
        List<String> jobs = new ArrayList<>();
        if (values.get(1) != null && !values.get(1).isBlank()) {
            jobs = new ArrayList<>(Arrays.stream(values.get(1).split(JOB_DELIMITER))
                    .map(String::trim)
                    .filter(jobId -> !jobId.isBlank())
                    .toList());
        }
        return new Workload(values.get(0), jobs);
    }

    private IllegalStateException invalidRecord(int lineNumber, RuntimeException cause) {
        String message = "Invalid workload data in " + filePath.getFileName() + " at line " + lineNumber + ".";
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
