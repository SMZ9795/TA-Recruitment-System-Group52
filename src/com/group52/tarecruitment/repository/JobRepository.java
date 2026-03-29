package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobRepository {
    private static final String HEADER =
            "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status";

    private final Path filePath;

    public JobRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<Job> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<Job> jobs = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> values = CsvUtil.parseLine(lines.get(i));
            if (values.size() < 10) {
                continue;
            }
            jobs.add(toJob(values));
        }
        return jobs;
    }

    public Optional<Job> findById(String jobId) {
        return findAll().stream().filter(job -> job.getId().equalsIgnoreCase(jobId)).findFirst();
    }

    public List<Job> findByPostedByMoId(String moId) {
        return findAll().stream()
                .filter(job -> job.getPostedByMoId() != null && job.getPostedByMoId().equalsIgnoreCase(moId))
                .toList();
    }

    public void save(Job job) {
        List<Job> jobs = findAll();
        jobs.removeIf(existing -> existing.getId().equalsIgnoreCase(job.getId()));
        jobs.add(job);
        writeAll(jobs);
    }

    private void writeAll(List<Job> jobs) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Job job : jobs) {
            lines.add(String.join(",",
                    CsvUtil.escape(job.getId()),
                    CsvUtil.escape(job.getModuleCode()),
                    CsvUtil.escape(job.getModuleName()),
                    CsvUtil.escape(job.getDescription()),
                    CsvUtil.escape(job.getRequiredSkills()),
                    CsvUtil.escape(String.valueOf(job.getHoursPerWeek())),
                    CsvUtil.escape(String.valueOf(job.getPositions())),
                    CsvUtil.escape(job.getDeadline()),
                    CsvUtil.escape(job.getPostedByMoId()),
                    CsvUtil.escape(job.getStatus().name())));
        }
        FileUtil.writeAllLines(filePath, lines);
    }

    private Job toJob(List<String> values) {
        return new Job(
                values.get(0),
                values.get(1),
                values.get(2),
                values.get(3),
                values.get(4),
                parseInt(values.get(5)),
                parseInt(values.get(6)),
                values.get(7),
                values.get(8),
                JobStatus.valueOf(values.get(9)));
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }
}
