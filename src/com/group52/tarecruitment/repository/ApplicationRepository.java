package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationRepository {
    private static final String HEADER = "id,jobId,taUserId,status,appliedDate";

    private final Path filePath;

    public ApplicationRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<Application> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<Application> applications = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> values = CsvUtil.parseLine(lines.get(i));
            if (values.size() < 5) {
                continue;
            }
            applications.add(toApplication(values));
        }
        return applications;
    }

    public Optional<Application> findById(String applicationId) {
        return findAll().stream()
                .filter(application -> application.getId().equalsIgnoreCase(applicationId))
                .findFirst();
    }

    public List<Application> findByJobId(String jobId) {
        return findAll().stream()
                .filter(application -> application.getJobId().equalsIgnoreCase(jobId))
                .toList();
    }

    public List<Application> findByTaUserId(String taUserId) {
        return findAll().stream()
                .filter(application -> application.getTaUserId().equalsIgnoreCase(taUserId))
                .toList();
    }

    public long countByJobIdAndStatus(String jobId, ApplicationStatus status) {
        return findAll().stream()
                .filter(application -> application.getJobId().equalsIgnoreCase(jobId))
                .filter(application -> application.getStatus() == status)
                .count();
    }

    public void save(Application application) {
        List<Application> applications = findAll();
        applications.removeIf(existing -> existing.getId().equalsIgnoreCase(application.getId()));
        applications.add(application);
        writeAll(applications);
    }



    public boolean existsByJobIdAndTaUserId(String jobId, String taUserId) {
        return findAll().stream().anyMatch(application ->
                application.getJobId().equalsIgnoreCase(jobId)
                        && application.getTaUserId().equalsIgnoreCase(taUserId));
    }

    private void writeAll(List<Application> applications) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Application application : applications) {
            lines.add(String.join(",",
                    CsvUtil.escape(application.getId()),
                    CsvUtil.escape(application.getJobId()),
                    CsvUtil.escape(application.getTaUserId()),
                    CsvUtil.escape(application.getStatus().name()),
                    CsvUtil.escape(application.getAppliedDate())));
        }
        FileUtil.writeAllLines(filePath, lines);
    }

    private Application toApplication(List<String> values) {
        return new Application(
                values.get(0),
                values.get(1),
                values.get(2),
                ApplicationStatus.valueOf(values.get(3)),
                values.get(4));
    }
}
