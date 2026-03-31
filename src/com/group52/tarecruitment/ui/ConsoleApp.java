package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.UserProfileService;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserProfileService userProfileService;
    private final AdminService adminService;
    private final Scanner scanner;

    public ConsoleApp(AuthService authService, JobService jobService, ApplicationService applicationService,
            UserProfileService userProfileService, AdminService adminService) {
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userProfileService = userProfileService;
        this.adminService = adminService;
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
            System.out.print("Student ID (9-12 digits): ");
            String studentId = scanner.nextLine().trim();
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Password (min 8 characters): ");
            String password = scanner.nextLine().trim();
            User user = authService.registerTa(studentId, name, email, password);
            System.out.println("TA account created. Your login ID: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private void login() {
        System.out.print("User ID or Email: ");
        String identifier = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        Optional<User> user = authService.login(identifier, password);
        if (user.isEmpty()) {
            System.out.println("Login failed. Please check your ID/email and password.");
            return;
        }

        System.out.println("Login successful. Role: " + user.get().getRole());
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
            System.out.println("4. View my profile");
            System.out.println("5. Edit my profile");
            System.out.println("6. Logout");
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
                    showTaProfile(user);
                    break;
                case "5":
                    editTaProfile(user);
                    break;
                case "6":
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
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Admin Dashboard ===");
            System.out.println("Welcome, " + user.getName());
            System.out.println("1. View all TA workloads");
            System.out.println("2. View specific TA workload");
            System.out.println("3. Recruitment summary");
            System.out.println("4. List all TAs");
            System.out.println("5. View all jobs");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    viewAllTAWorkloads();
                    break;
                case "2":
                    viewSpecificTAWorkload();
                    break;
                case "3":
                    viewRecruitmentSummary();
                    break;
                case "4":
                    listAllTAs();
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
        System.out.println();
        System.out.println("=== Applications For My Jobs ===");
        printApplications(applications);
    }

    private void printApplications(List<Application> applications) {
        System.out.println();
        for (Application application : applications) {
            System.out.println("Application " + application.getId()
                    + " | Job " + application.getJobId()
                    + " | Applicant " + application.getTaUserId()
                    + " | " + application.getStatus()
                    + " | applied=" + application.getAppliedDate());
        }
    }

    private void showTaProfile(User user) {
        System.out.println();
        System.out.println("=== TA Profile ===");
        System.out.println("Name: " + user.getName());
        System.out.println("Email: " + user.getEmail());
        System.out.println(userProfileService.formatProfile(user));
    }

    private void editTaProfile(User user) {
        try {
            System.out.print("Programme: ");
            String programme = scanner.nextLine().trim();
            System.out.print("Year of study: ");
            int yearOfStudy = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Skills: ");
            String skills = scanner.nextLine().trim();
            System.out.print("Available hours: ");
            int availableHours = Integer.parseInt(scanner.nextLine().trim());

            User updatedUser = userProfileService.updateTaProfile(
                    user.getId(), programme, yearOfStudy, skills, availableHours);
            syncUserProfile(user, updatedUser);
            System.out.println("Profile updated.");
        } catch (NumberFormatException e) {
            System.out.println("Profile update failed: Year of study and available hours must be whole numbers.");
        } catch (IllegalArgumentException e) {
            System.out.println("Profile update failed: " + e.getMessage());
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

    private void syncUserProfile(User currentUser, User updatedUser) {
        currentUser.setProgramme(updatedUser.getProgramme());
        currentUser.setYearOfStudy(updatedUser.getYearOfStudy());
        currentUser.setSkills(updatedUser.getSkills());
        currentUser.setAvailableHours(updatedUser.getAvailableHours());
    }

    // ==================== Admin Methods ====================

    private void viewAllTAWorkloads() {
        List<AdminService.TAWorkloadSummary> workloads = adminService.getAllTAWorkloads();
        if (workloads.isEmpty()) {
            System.out.println("No TAs have accepted positions yet.");
            return;
        }
        System.out.println();
        System.out.println("=== TA Workload Overview ===");
        for (AdminService.TAWorkloadSummary w : workloads) {
            System.out.println();
            System.out.println(w.getTaUserId() + " | " + w.getTaName()
                    + " | jobs=" + w.getAcceptedJobCount()
                    + " | assigned=" + w.getTotalAssignedHours() + "h/week"
                    + " | available=" + w.getAvailableHours() + "h/week"
                    + (w.isOverloaded() ? " [OVERLOADED]" : ""));
            for (String desc : w.getAcceptedJobDescriptions()) {
                System.out.println("    -> " + desc);
            }
        }
    }

    private void viewSpecificTAWorkload() {
        System.out.print("Enter TA user ID: ");
        String taId = scanner.nextLine().trim();
        try {
            AdminService.TAWorkloadSummary w = adminService.getTAWorkload(taId);
            System.out.println();
            System.out.println("=== Workload for " + w.getTaName() + " ===");
            System.out.println("TA ID:              " + w.getTaUserId());
            System.out.println("Available hours:    " + w.getAvailableHours() + "h/week");
            System.out.println("Assigned hours:     " + w.getTotalAssignedHours() + "h/week");
            System.out.println("Remaining capacity: " + w.getRemainingHours() + "h/week");
            System.out.println("Accepted positions: " + w.getAcceptedJobCount());
            if (w.isOverloaded()) {
                System.out.println("[WARNING] This TA is overloaded!");
            }
            if (!w.getAcceptedJobDescriptions().isEmpty()) {
                System.out.println("Accepted jobs:");
                for (String desc : w.getAcceptedJobDescriptions()) {
                    System.out.println("  -> " + desc);
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void viewRecruitmentSummary() {
        System.out.println();
        System.out.println(adminService.getRecruitmentSummary());
    }

    private void listAllTAs() {
        List<User> tas = adminService.getAllTAs();
        if (tas.isEmpty()) {
            System.out.println("No TA accounts registered.");
            return;
        }
        System.out.println();
        System.out.println("=== All TA Accounts ===");
        for (User ta : tas) {
            System.out.println(ta.getId() + " | " + ta.getName()
                    + " | " + ta.getEmail()
                    + " | programme=" + (ta.getProgramme() == null || ta.getProgramme().isBlank() ? "N/A" : ta.getProgramme())
                    + " | year=" + (ta.getYearOfStudy() == 0 ? "N/A" : ta.getYearOfStudy())
                    + " | hours=" + ta.getAvailableHours());
        }
    }
}
