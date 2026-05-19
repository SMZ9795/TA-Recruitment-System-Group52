package com.group52.tarecruitment;

import com.group52.tarecruitment.repository.ApplicationAuditLogRepository;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.NotificationRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.repository.WorkloadRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.ExportService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.NotificationService;
import com.group52.tarecruitment.service.WorkloadService;
import com.group52.tarecruitment.ui.SwingApp;
import java.nio.file.Path;

public class SwingMain {
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");

        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository =
                new ApplicationRepository(dataDirectory.resolve("applications.csv"));
        NotificationRepository notificationRepository =
                new NotificationRepository(dataDirectory.resolve("notifications.csv"));
        WorkloadRepository workloadRepository =
                new WorkloadRepository(dataDirectory.resolve("workloads.json"));
        ApplicationAuditLogRepository auditLogRepository =
                new ApplicationAuditLogRepository(dataDirectory.resolve("audit_log.csv"));

        AuthService authService = new AuthService(userRepository);
        NotificationService notificationService = new NotificationService(notificationRepository);
        JobService jobService = new JobService(jobRepository, applicationRepository, notificationService);
        WorkloadService workloadService = new WorkloadService(workloadRepository);
        ApplicationService applicationService =
                new ApplicationService(applicationRepository, jobRepository, workloadService, notificationService);
        applicationService.setAuditLogRepository(auditLogRepository);
        AdminService adminService =
                new AdminService(userRepository, jobRepository, applicationRepository, notificationService);
        ExportService exportService = new ExportService(
                userRepository, jobRepository, applicationRepository,
                adminService, dataDirectory.resolve("exports"));

        SwingApp app = new SwingApp(
                authService,
                jobService,
                applicationService,
                dataDirectory,
                adminService,
                notificationService,
                exportService);
        app.start();
    }
}
