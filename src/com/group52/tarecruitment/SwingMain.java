package com.group52.tarecruitment;

import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.repository.WorkloadRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
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
        WorkloadRepository workloadRepository =
                new WorkloadRepository(dataDirectory.resolve("workloads.json"));

        AuthService authService = new AuthService(userRepository);
        JobService jobService = new JobService(jobRepository, applicationRepository);
        WorkloadService workloadService = new WorkloadService(workloadRepository);
        ApplicationService applicationService =
                new ApplicationService(applicationRepository, jobRepository, workloadService);
        AdminService adminService = new AdminService(userRepository, jobRepository, applicationRepository);

        SwingApp app = new SwingApp(authService, jobService, applicationService, dataDirectory, adminService);
        app.start();
    }
}
