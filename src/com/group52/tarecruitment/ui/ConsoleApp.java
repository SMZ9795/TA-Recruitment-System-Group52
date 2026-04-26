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
            try {
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
            } catch (RuntimeException e) {
                printUnexpectedFailure(e);
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
            String studentId = scanner.nextLine();
            System.out.print("Name: ");
            String name = scanner.nextLine();
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Password (min 8 characters): ");
            String password = scanner.nextLine();
            User user = authService.registerTa(studentId, name, email, password);
            System.out.println("TA account created. Your login ID: " + user.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Registration failed", e);
        }
    }

    private void login() {
        try {
            System.out.print("User ID or Email: ");
            String identifier = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            User user = authService.login(identifier, password);

            System.out.println("Login successful. Role: " + user.getRole());
            if (user.getRole() == Role.TA) {
                showTaMenu(user);
            } else if (user.getRole() == Role.MO) {
                showMoMenu(user);
            } else {
                showAdminMenu(user);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Login failed", e);
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
            try {
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
            } catch (RuntimeException e) {
                printUnexpectedFailure(e);
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
            System.out.println("2. Edit a job");
            System.out.println("3. View my jobs");
            System.out.println("4. View applications for my jobs");
            System.out.println("5. Review an application");
            System.out.println("6. View all jobs");
            System.out.println("7. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        createJob(user);
                        break;
                    case "2":
                        editJob(user);
                        break;
                    case "3":
                        listJobsByMo(user);
                        break;
                    case "4":
                        listApplicationsByMo(user);
                        break;
                    case "5":
                        reviewApplication(user);
                        break;
                    case "6":
                        listJobs();
                        break;
                    case "7":
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (RuntimeException e) {
                printUnexpectedFailure(e);
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
            try {
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
            } catch (RuntimeException e) {
                printUnexpectedFailure(e);
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
            System.out.println(formatJob(job));
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
            String programme = scanner.nextLine();
            System.out.print("Year of study: ");
            String yearOfStudy = scanner.nextLine();
            System.out.print("Skills: ");
            String skills = scanner.nextLine();
            System.out.print("Available hours: ");
            String availableHours = scanner.nextLine();
            System.out.print("CV file path (optional): ");
            String cvFilePath = scanner.nextLine();

            User updatedUser = userProfileService.updateTaProfile(
                    user.getId(), programme, yearOfStudy, skills, availableHours, cvFilePath);
            syncUserProfile(user, updatedUser);
            System.out.println("Profile updated.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Profile update failed", e);
        }
    }

    private void createJob(User user) {
        try {
            System.out.print("Module code: ");
            String moduleCode = scanner.nextLine();
            System.out.print("Module name: ");
            String moduleName = scanner.nextLine();
            System.out.print("Description: ");
            String description = scanner.nextLine();
            System.out.print("Required skills: ");
            String requiredSkills = scanner.nextLine();
            System.out.print("Hours per week: ");
            String hours = scanner.nextLine();
            System.out.print("Positions: ");
            String positions = scanner.nextLine();
            System.out.print("Deadline (YYYY-MM-DD): ");
            String deadline = scanner.nextLine();

            Job job = jobService.createJob(
                    moduleCode, moduleName, description, requiredSkills, hours, positions, deadline, user.getId());
            System.out.println("Job created: " + job.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Job creation failed", e);
        }
    }

    private void editJob(User user) {
        try {
            System.out.print("Enter job ID to edit: ");
            String jobId = scanner.nextLine();
            Job currentJob = jobService.getJobForMo(jobId, user.getId());

            System.out.println("Current job: " + formatJob(currentJob));
            System.out.print("New module code: ");
            String moduleCode = scanner.nextLine();
            System.out.print("New module name: ");
            String moduleName = scanner.nextLine();
            System.out.print("New description: ");
            String description = scanner.nextLine();
            System.out.print("New required skills: ");
            String requiredSkills = scanner.nextLine();
            System.out.print("New hours per week: ");
            String hours = scanner.nextLine();
            System.out.print("New positions: ");
            String positions = scanner.nextLine();
            System.out.print("New deadline (YYYY-MM-DD): ");
            String deadline = scanner.nextLine();

            Job updatedJob = jobService.updateJob(
                    jobId, user.getId(), moduleCode, moduleName, description, requiredSkills, hours, positions, deadline);
            System.out.println("Job updated: " + updatedJob.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Job update failed", e);
        }
    }

    private void applyForJob(User user) {
        try {
            System.out.print("Enter job ID: ");
            String jobId = scanner.nextLine();
            Application application = applicationService.applyForJob(jobId, user.getId());
            System.out.println("Application submitted: " + application.getId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Application failed", e);
        }
    }

    private void reviewApplication(User user) {
        try {
            System.out.print("Enter application ID: ");
            String applicationId = scanner.nextLine();
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Review failed", e);
        }
    }

    private void syncUserProfile(User currentUser, User updatedUser) {
        currentUser.setProgramme(updatedUser.getProgramme());
        currentUser.setYearOfStudy(updatedUser.getYearOfStudy());
        currentUser.setSkills(updatedUser.getSkills());
        currentUser.setAvailableHours(updatedUser.getAvailableHours());
        currentUser.setCvFilePath(updatedUser.getCvFilePath());
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            printOperationFailure("Error", e);
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

    private String formatJob(Job job) {
        return job.getId()
                + " | " + job.getModuleCode()
                + " | " + job.getModuleName()
                + " | hours=" + job.getHoursPerWeek()
                + " | positions=" + job.getPositions()
                + " | deadline=" + job.getDeadline()
                + " | status=" + job.getStatus();
    }

    private void printOperationFailure(String prefix, Exception e) {
        System.out.println(prefix + ": " + e.getMessage());
    }

    private void printUnexpectedFailure(RuntimeException e) {
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            System.out.println("Operation failed: " + e.getMessage());
            return;
        }
        System.out.println("Operation failed due to an unexpected error. Please try again.");
    }
}
