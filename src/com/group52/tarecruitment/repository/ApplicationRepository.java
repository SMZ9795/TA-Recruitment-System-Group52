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
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> values = CsvUtil.parseLine(line);
            if (values.size() != 5) {
                throw invalidRecord(i + 1, null);
            }
            try {
                applications.add(toApplication(values));
            } catch (RuntimeException e) {
                throw invalidRecord(i + 1, e);
            }
        }
        return applications;
    }

    public Optional<Application> findById(String applicationId) {
        if (applicationId == null || applicationId.isBlank()) {
            return Optional.empty();
        }
        String normalizedId = applicationId.trim();
        return findAll().stream()
                .filter(application -> application.getId().equalsIgnoreCase(normalizedId))
                .findFirst();
    }

    public List<Application> findByJobId(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            return new ArrayList<>();
        }
        String normalizedJobId = jobId.trim();
        return new ArrayList<>(findAll().stream()
                .filter(application -> application.getJobId().equalsIgnoreCase(normalizedJobId))
                .toList());
    }

    public List<Application> findByTaUserId(String taUserId) {
        if (taUserId == null || taUserId.isBlank()) {
            return new ArrayList<>();
        }
        String normalizedTaUserId = taUserId.trim();
        return new ArrayList<>(findAll().stream()
                .filter(application -> application.getTaUserId().equalsIgnoreCase(normalizedTaUserId))
                .toList());
    }

    public long countByJobIdAndStatus(String jobId, ApplicationStatus status) {
        if (jobId == null || jobId.isBlank() || status == null) {
            return 0;
        }
        String normalizedJobId = jobId.trim();
        return findAll().stream()
                .filter(application -> application.getJobId().equalsIgnoreCase(normalizedJobId))
                .filter(application -> application.getStatus() == status)
                .count();
    }

    public void save(Application application) {
        if (application == null || application.getId() == null || application.getId().isBlank()) {
            throw new IllegalArgumentException("Application ID is required.");
        }
        String normalizedId = application.getId().trim();
        application.setId(normalizedId);
        List<Application> applications = findAll();
        applications.removeIf(existing -> existing.getId().equalsIgnoreCase(normalizedId));
        applications.add(application);
        writeAll(applications);
    }

    public boolean existsByJobIdAndTaUserId(String jobId, String taUserId) {
        if (jobId == null || jobId.isBlank() || taUserId == null || taUserId.isBlank()) {
            return false;
        }
        String normalizedJobId = jobId.trim();
        String normalizedTaUserId = taUserId.trim();
        return findAll().stream().anyMatch(application ->
                application.getJobId().equalsIgnoreCase(normalizedJobId)
                        && application.getTaUserId().equalsIgnoreCase(normalizedTaUserId));
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
                ApplicationStatus.fromStorageValue(values.get(3)),
                values.get(4));
    }

    private IllegalStateException invalidRecord(int lineNumber, RuntimeException cause) {
        String message = "Invalid application data in " + filePath.getFileName() + " at line " + lineNumber + ".";
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
