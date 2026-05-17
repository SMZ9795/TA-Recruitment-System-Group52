package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.CsvUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Generates CSV exports of recruitment data for Admin and MO operators.
 *
 * <p>Output files are written to the configured export directory (defaults to
 * {@code data/exports/}). Every export filename embeds a timestamp so repeated
 * exports do not overwrite each other. If two exports happen in the same second
 * a numeric suffix is appended to keep filenames unique.
 *
 * <p>Empty datasets are still written as header-only CSVs so downstream users
 * receive a well-formed file. File I/O errors are wrapped as
 * {@link IllegalStateException} with a friendly, actionable message.
 */
public class ExportService {

    public static final String[] APPLICATIONS_HEADER = {
        "applicationId", "jobId", "moduleCode", "moduleName",
        "taUserId", "taName", "taEmail", "status", "appliedDate"
    };

    public static final String[] WORKLOAD_HEADER = {
        "taUserId", "taName", "availableHours", "acceptedJobCount",
        "totalAssignedHours", "remainingHours", "utilisationPercent", "riskLevel"
    };

    public static final String[] JOB_FILLING_HEADER = {
        "jobId", "moduleCode", "moduleName", "positions", "filledPositions",
        "remainingPositions", "fillRatio", "status", "deadline", "postedByMoId"
    };

    public static final String[] APPLICANT_LIST_HEADER = {
        "applicationId", "taUserId", "taName", "taEmail", "programme",
        "yearOfStudy", "skills", "availableHours", "status", "appliedDate"
    };

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final AdminService adminService;
    private final Path exportDirectory;
    private final Supplier<LocalDateTime> clock;

    public ExportService(UserRepository userRepository,
                         JobRepository jobRepository,
                         ApplicationRepository applicationRepository,
                         AdminService adminService,
                         Path exportDirectory) {
        this(userRepository, jobRepository, applicationRepository, adminService,
                exportDirectory, LocalDateTime::now);
    }

    /** Package-private constructor that allows tests to inject a deterministic clock. */
    ExportService(UserRepository userRepository,
                  JobRepository jobRepository,
                  ApplicationRepository applicationRepository,
                  AdminService adminService,
                  Path exportDirectory,
                  Supplier<LocalDateTime> clock) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository");
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.applicationRepository = Objects.requireNonNull(applicationRepository, "applicationRepository");
        this.adminService = Objects.requireNonNull(adminService, "adminService");
        this.exportDirectory = Objects.requireNonNull(exportDirectory, "exportDirectory");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Path getExportDirectory() {
        return exportDirectory;
    }

    /**
     * Exports every application in the system, joining each row with the related
     * job (module code/name) and TA (name/email) for human-friendly review.
     *
     * @return the path of the written CSV file (never {@code null}).
     */
    public Path exportAllApplications() {
        List<Application> applications = new ArrayList<>(applicationRepository.findAll());
        applications.sort(Comparator.comparing(Application::getId, Comparator.nullsLast(String::compareTo)));

        Map<String, Job> jobsById = indexJobsById();
        Map<String, User> usersById = indexUsersById();

        List<String[]> rows = new ArrayList<>();
        for (Application application : applications) {
            Job job = jobsById.get(normalizeKey(application.getJobId()));
            User ta = usersById.get(normalizeKey(application.getTaUserId()));
            rows.add(new String[] {
                safe(application.getId()),
                safe(application.getJobId()),
                job == null ? "" : safe(job.getModuleCode()),
                job == null ? "" : safe(job.getModuleName()),
                safe(application.getTaUserId()),
                ta == null ? "" : safe(ta.getName()),
                ta == null ? "" : safe(ta.getEmail()),
                application.getStatus() == null ? "" : application.getStatus().name(),
                safe(application.getAppliedDate())
            });
        }
        return writeCsv("all_applications", APPLICATIONS_HEADER, rows);
    }

    /**
     * Exports the TA workload summary (one row per active TA with at least one
     * accepted position) using {@link AdminService#getAllTAWorkloads()}.
     */
    public Path exportTaWorkloadSummary() {
        List<AdminService.TAWorkloadSummary> summaries = adminService.getAllTAWorkloads();
        List<String[]> rows = new ArrayList<>();
        for (AdminService.TAWorkloadSummary summary : summaries) {
            rows.add(new String[] {
                safe(summary.getTaUserId()),
                safe(summary.getTaName()),
                String.valueOf(summary.getAvailableHours()),
                String.valueOf(summary.getAcceptedJobCount()),
                String.valueOf(summary.getTotalAssignedHours()),
                String.valueOf(summary.getRemainingHours()),
                String.format(Locale.ROOT, "%.1f", summary.getUtilisationPercent()),
                summary.getRiskLevel().label()
            });
        }
        return writeCsv("ta_workload_summary", WORKLOAD_HEADER, rows);
    }

    /**
     * Exports the global job filling status: one row per job with its filled
     * vs. total positions ratio and current lifecycle status.
     */
    public Path exportJobFillingStatus() {
        List<Job> jobs = new ArrayList<>(jobRepository.findAll());
        jobs.sort(Comparator.comparing(Job::getModuleCode, Comparator.nullsLast(String::compareTo)));

        Map<String, Long> acceptedByJobId = new HashMap<>();
        for (Application application : applicationRepository.findAll()) {
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                continue;
            }
            acceptedByJobId.merge(normalizeKey(application.getJobId()), 1L, Long::sum);
        }

        List<String[]> rows = new ArrayList<>();
        for (Job job : jobs) {
            int positions = Math.max(0, job.getPositions());
            int filled = (int) (long) acceptedByJobId.getOrDefault(normalizeKey(job.getId()), 0L);
            int remaining = Math.max(0, positions - filled);
            rows.add(new String[] {
                safe(job.getId()),
                safe(job.getModuleCode()),
                safe(job.getModuleName()),
                String.valueOf(positions),
                String.valueOf(filled),
                String.valueOf(remaining),
                filled + "/" + positions,
                job.getStatus() == null ? "" : job.getStatus().name(),
                safe(job.getDeadline()),
                safe(job.getPostedByMoId())
            });
        }
        return writeCsv("job_filling_status", JOB_FILLING_HEADER, rows);
    }

    /**
     * Exports the applicant list for a single job. Used by MO operators who
     * want an offline copy of every TA that applied to one of their modules.
     *
     * @throws IllegalArgumentException if the job ID is blank or does not exist.
     */
    public Path exportApplicantsForJob(String jobId) {
        if (jobId == null || jobId.isBlank()) {
            throw new IllegalArgumentException("Job ID is required for applicant export.");
        }
        String normalizedJobId = jobId.trim();
        Optional<Job> jobOpt = jobRepository.findById(normalizedJobId);
        if (jobOpt.isEmpty()) {
            throw new IllegalArgumentException("Job not found for ID: " + normalizedJobId);
        }
        Job job = jobOpt.get();

        List<Application> applications = new ArrayList<>(
                applicationRepository.findByJobId(job.getId()));
        applications.sort(Comparator.comparing(Application::getAppliedDate,
                Comparator.nullsLast(String::compareTo)));

        Map<String, User> usersById = indexUsersById();
        List<String[]> rows = new ArrayList<>();
        for (Application application : applications) {
            User ta = usersById.get(normalizeKey(application.getTaUserId()));
            rows.add(new String[] {
                safe(application.getId()),
                safe(application.getTaUserId()),
                ta == null ? "" : safe(ta.getName()),
                ta == null ? "" : safe(ta.getEmail()),
                ta == null ? "" : safe(ta.getProgramme()),
                ta == null ? "" : String.valueOf(ta.getYearOfStudy()),
                ta == null ? "" : safe(ta.getSkills()),
                ta == null ? "" : String.valueOf(ta.getAvailableHours()),
                application.getStatus() == null ? "" : application.getStatus().name(),
                safe(application.getAppliedDate())
            });
        }

        String moduleCodeForFilename = sanitizeForFilename(
                safe(job.getModuleCode()).isBlank() ? job.getId() : job.getModuleCode());
        return writeCsv("applicants_" + moduleCodeForFilename, APPLICANT_LIST_HEADER, rows);
    }

    private Map<String, Job> indexJobsById() {
        Map<String, Job> result = new HashMap<>();
        for (Job job : jobRepository.findAll()) {
            if (job.getId() != null) {
                result.put(normalizeKey(job.getId()), job);
            }
        }
        return result;
    }

    private Map<String, User> indexUsersById() {
        Map<String, User> result = new HashMap<>();
        for (User user : userRepository.findAll()) {
            if (user.getId() != null) {
                result.put(normalizeKey(user.getId()), user);
            }
        }
        return result;
    }

    private Path writeCsv(String filePrefix, String[] header, List<String[]> rows) {
        try {
            Files.createDirectories(exportDirectory);
        } catch (IOException ioException) {
            throw new IllegalStateException(
                    "Unable to create export directory: " + exportDirectory, ioException);
        }

        Path target = resolveUniquePath(filePrefix);
        List<String> lines = new ArrayList<>(rows.size() + 1);
        lines.add(joinCsvRow(header));
        for (String[] row : rows) {
            lines.add(joinCsvRow(row));
        }

        try {
            Files.write(target, lines, StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new IllegalStateException(
                    "Failed to write export file " + target + ": " + ioException.getMessage(),
                    ioException);
        }
        return target;
    }

    private Path resolveUniquePath(String filePrefix) {
        String timestamp = TIMESTAMP_FORMAT.format(clock.get());
        Path candidate = exportDirectory.resolve(filePrefix + "_" + timestamp + ".csv");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = exportDirectory.resolve(
                    filePrefix + "_" + timestamp + "_" + suffix + ".csv");
            suffix++;
        }
        return candidate;
    }

    private static String joinCsvRow(String[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(CsvUtil.escape(values[i]));
        }
        return builder.toString();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    /** Removes characters that are unfriendly in filenames (slashes, colons, etc.). */
    private static String sanitizeForFilename(String value) {
        if (value == null || value.isBlank()) {
            return "job";
        }
        String cleaned = value.trim().replaceAll("[^A-Za-z0-9._-]+", "_");
        return cleaned.isBlank() ? "job" : cleaned;
    }
}
