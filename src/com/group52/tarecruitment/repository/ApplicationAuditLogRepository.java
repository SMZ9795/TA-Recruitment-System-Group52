package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.ApplicationAuditLog;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ApplicationAuditLogRepository {
    private static final String HEADER =
            "id,applicationId,taUserId,jobId,operatorUserId,fromStatus,toStatus,changedAt";

    private final Path filePath;

    public ApplicationAuditLogRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<ApplicationAuditLog> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<ApplicationAuditLog> logs = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) continue;
            List<String> v = CsvUtil.parseLine(line);
            if (v.size() != 8) continue;
            logs.add(new ApplicationAuditLog(
                    v.get(0), v.get(1), v.get(2), v.get(3), v.get(4),
                    parseStatus(v.get(5)), parseStatus(v.get(6)), v.get(7)));
        }
        return logs;
    }

    public List<ApplicationAuditLog> findByTaUserId(String taUserId) {
        return findAll().stream()
                .filter(l -> taUserId.equalsIgnoreCase(l.getTaUserId()))
                .toList();
    }

    public List<ApplicationAuditLog> findByJobId(String jobId) {
        return findAll().stream()
                .filter(l -> jobId.equalsIgnoreCase(l.getJobId()))
                .toList();
    }

    public void save(ApplicationAuditLog log) {
        List<String> lines = FileUtil.readAllLines(filePath);
        if (lines.isEmpty()) lines.add(HEADER);
        lines.add(String.join(",",
                CsvUtil.escape(log.getId()),
                CsvUtil.escape(log.getApplicationId()),
                CsvUtil.escape(log.getTaUserId()),
                CsvUtil.escape(log.getJobId()),
                CsvUtil.escape(log.getOperatorUserId()),
                CsvUtil.escape(log.getFromStatus() == null ? "" : log.getFromStatus().name()),
                CsvUtil.escape(log.getToStatus().name()),
                CsvUtil.escape(log.getChangedAt())));
        FileUtil.writeAllLines(filePath, lines);
    }

    private ApplicationStatus parseStatus(String v) {
        if (v == null || v.isBlank()) return null;
        try { return ApplicationStatus.valueOf(v.trim().toUpperCase()); } catch (Exception e) { return null; }
    }
}
