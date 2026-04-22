package com.group52.tarecruitment;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.UserProfileService;
import com.group52.tarecruitment.ui.SwingApp;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path dataDirectory = resolveDataDirectory();

        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository =
                new ApplicationRepository(dataDirectory.resolve("applications.csv"));
        ensureDefaultUsers(userRepository);

        AuthService authService = new AuthService(userRepository);
        JobService jobService = new JobService(jobRepository);
        ApplicationService applicationService = new ApplicationService(applicationRepository, jobRepository);
        UserProfileService userProfileService = new UserProfileService(userRepository);
        AdminService adminService = new AdminService(userRepository, jobRepository, applicationRepository);

        // 直接运行SwingApp，跳过ConsoleApp
        SwingApp app = new SwingApp(authService, jobService, applicationService, dataDirectory);
        app.start();
    }

    private static Path resolveDataDirectory() {
        Path current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (Path cursor = current; cursor != null; cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("data");
            if (Files.exists(candidate.resolve("users.csv"))
                    || Files.exists(candidate.resolve("jobs.csv"))
                    || Files.exists(candidate.resolve("applications.csv"))) {
                return candidate;
            }
            if (Files.exists(cursor.resolve(".git"))) {
                return candidate;
            }
        }
        return current.resolve("data");
    }

    private static void ensureDefaultUsers(UserRepository userRepository) {
        if (userRepository.findById("ADMIN001").isEmpty()) {
            userRepository.save(new User(
                    "ADMIN001",
                    Role.ADMIN,
                    "System Admin",
                    "admin@bupt.local",
                    "admin123",
                    "",
                    0,
                    "",
                    0,
                    true));
        }
        if (userRepository.findById("MO001").isEmpty()) {
            userRepository.save(new User(
                    "MO001",
                    Role.MO,
                    "Dr Smith",
                    "drsmith@bupt.local",
                    "mo123456",
                    "",
                    0,
                    "",
                    0,
                    true));
        }
    }
}
