package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final Scanner scanner;

    public ConsoleApp(AuthService authService, JobService jobService, ApplicationService applicationService) {
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    registerTa();
                    break;
                case "2":
                    login();
                    break;
                case "3":
                    running = false;
                    System.out.println("Application closed.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("=== BUPT TA Recruitment System ===");
        System.out.println("1. Register as TA");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");
    }

    private void registerTa() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            User user = authService.registerTa(name, email, password);
            System.out.println("TA account created: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        Optional<User> user = authService.login(email, password);
        if (user.isEmpty()) {
            System.out.println("Login failed.");
            return;
        }

        if (user.get().getRole() == Role.TA) {
            showTaMenu(user.get());
        } else if (user.get().getRole() == Role.MO) {
            showMoMenu(user.get());
        } else {
            showAdminMenu(user.get());
        }
    }

    private void showTaMenu(User user) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== TA Dashboard ===");
            System.out.println("Welcome, " + user.getName());
            System.out.println("1. Browse jobs");
            System.out.println("2. Apply for a job");
            System.out.println("3. View my applications");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    listJobs();
                    break;
                case "2":
                    applyForJob(user);
                    break;
                case "3":
                    listApplicationsByTa(user);
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void showMoMenu(User user) {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== MO Dashboard ===");
            System.out.println("Welcome, " + user.getName());
            System.out.println("1. Post a job");
            System.out.println("2. View my jobs");
            System.out.println("3. View applications for my jobs");
            System.out.println("4. Review an application");
            System.out.println("5. View all jobs");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    createJob(user);
                    break;
                case "2":
                    listJobsByMo(user);
                    break;
                case "3":
                    listApplicationsByMo(user);
                    break;
                case "4":
                    reviewApplication(user);
                    break;
                case "5":
                    listJobs();
                    break;
                case "6":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void showAdminMenu(User user) {
        System.out.println();
        System.out.println("=== Admin Dashboard ===");
        System.out.println("Welcome, " + user.getName());
        System.out.println("This area is a placeholder for workload monitoring and account management.");
    }

    private void listJobs() {
        List<Job> jobs = jobService.getAllJobs();
        if (jobs.isEmpty()) {
            System.out.println("No jobs available.");
            return;
        }
        printJobs(jobs);
    }

    private void listJobsByMo(User user) {
        List<Job> jobs = jobService.getJobsByMoId(user.getId());
        if (jobs.isEmpty()) {
            System.out.println("You have not posted any jobs.");
            return;
        }
        printJobs(jobs);
    }

    private void printJobs(List<Job> jobs) {
        System.out.println();
        for (Job job : jobs) {
            System.out.println(job.getId() + " | " + job.getModuleCode() + " | " + job.getModuleName()
                    + " | " + job.getStatus());
        }
    }

    private void listApplicationsByTa(User user) {
        List<Application> applications = applicationService.getApplicationsByTaUserId(user.getId());
        if (applications.isEmpty()) {
            System.out.println("You have not submitted any applications.");
            return;
        }
        printApplications(applications);
    }

    private void listApplicationsByMo(User user) {
        List<Application> applications = applicationService.getApplicationsForMo(user.getId());
        if (applications.isEmpty()) {
            System.out.println("There are no applications for your jobs yet.");
            return;
        }
        printApplications(applications);
    }

    private void printApplications(List<Application> applications) {
        System.out.println();
        for (Application application : applications) {
            System.out.println(application.getId() + " | job=" + application.getJobId()
                    + " | ta=" + application.getTaUserId()
                    + " | " + application.getStatus()
                    + " | applied=" + application.getAppliedDate());
        }
    }

    private void createJob(User user) {
        try {
            System.out.print("Module code: ");
            String moduleCode = scanner.nextLine().trim();
            System.out.print("Module name: ");
            String moduleName = scanner.nextLine().trim();
            System.out.print("Description: ");
            String description = scanner.nextLine().trim();
            System.out.print("Required skills: ");
            String requiredSkills = scanner.nextLine().trim();
            System.out.print("Hours per week: ");
            int hours = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Positions: ");
            int positions = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Deadline (YYYY-MM-DD): ");
            String deadline = scanner.nextLine().trim();

            Job job = jobService.createJob(
                    moduleCode, moduleName, description, requiredSkills, hours, positions, deadline, user.getId());
            System.out.println("Job created: " + job.getId());
        } catch (NumberFormatException e) {
            System.out.println("Job creation failed: Hours per week and positions must be whole numbers.");
        } catch (IllegalArgumentException e) {
            System.out.println("Job creation failed: " + e.getMessage());
        }
    }

    private void applyForJob(User user) {
        try {
            System.out.print("Enter job ID: ");
            String jobId = scanner.nextLine().trim();
            applicationService.applyForJob(jobId, user.getId());
            System.out.println("Application submitted.");
        } catch (IllegalArgumentException e) {
            System.out.println("Application failed: " + e.getMessage());
        }
    }

    private void reviewApplication(User user) {
        try {
            System.out.print("Enter application ID: ");
            String applicationId = scanner.nextLine().trim();
            System.out.print("Action (A=accept, R=reject): ");
            String action = scanner.nextLine().trim();

            ApplicationStatus newStatus;
            if ("A".equalsIgnoreCase(action)) {
                newStatus = ApplicationStatus.ACCEPTED;
            } else if ("R".equalsIgnoreCase(action)) {
                newStatus = ApplicationStatus.REJECTED;
            } else {
                System.out.println("Invalid action.");
                return;
            }

            Application application = applicationService.updateApplicationStatus(applicationId, user.getId(), newStatus);
            System.out.println("Application updated: " + application.getId() + " -> " + application.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Review failed: " + e.getMessage());
        }
    }
}
