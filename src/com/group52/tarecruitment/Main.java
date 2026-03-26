package com.group52.tarecruitment;

import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.ui.ConsoleApp;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");

        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository =
                new ApplicationRepository(dataDirectory.resolve("applications.csv"));

        AuthService authService = new AuthService(userRepository);
        JobService jobService = new JobService(jobRepository);
        ApplicationService applicationService = new ApplicationService(applicationRepository);

        ConsoleApp app = new ConsoleApp(authService, jobService, applicationService);
        app.start();
    }
}
