package com.group52.tarecruitment.tests;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.util.CvValidationUtil;
import com.group52.tarecruitment.util.FileUtil;
import com.group52.tarecruitment.util.JobFilterUtil;
import com.group52.tarecruitment.util.TaNotificationUtil;
import com.group52.tarecruitment.util.TaNotificationUtil.ApplicationStatusSummary;
import com.group52.tarecruitment.util.TaNotificationUtil.NotificationEntry;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class RecruitmentSystemTestRunner {
    private int passedCount;
    private int failedCount;

    public static void main(String[] args) throws Exception {
        RecruitmentSystemTestRunner runner = new RecruitmentSystemTestRunner();
        runner.run();
    }

    private void run() throws Exception {
        runCase("CV persistence survives repository restart", this::testCvPersistenceSurvivesRestart);
        runCase("Legacy users.csv rows without cvFilePath are still readable", this::testLegacyUsersCsvCompatibility);
        runCase("TA profile update persists CV and enforces validation", this::testTaProfileUpdatePersistsCvAndValidates);
        runCase("CV format and file-size validation rules", this::testCvValidationRules);
        runCase("Job board multi-filter matching", this::testJobFilterRules);
        runCase("Auth register/login success and validation failures", this::testAuthFlows);
        runCase("Job creation/update validation includes deadline and capacity", this::testJobValidationFlows);
        runCase("Application authorization and state transition rules", this::testApplicationAuthorizationAndTransitions);
        runCase("Job deletion is blocked when applications exist", this::testDeleteJobGuard);
        runCase("TA notification filtering and unread count", this::testTaNotificationFilteringAndUnreadCount);
        runCase("TA notification status summary and closed-job message", this::testTaNotificationSummaryAndClosedMessage);
        runCase("End-to-end integration: TA profile data visible to MO and admin workload", this::testEndToEndIntegrationFlow);

        System.out.println();
        System.out.println("==== TEST SUMMARY ====");
        System.out.println("Passed: " + passedCount);
        System.out.println("Failed: " + failedCount);
        if (failedCount > 0) {
            throw new IllegalStateException("Some tests failed.");
        }
    }

    private void testCvPersistenceSurvivesRestart() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA100000001", "Alice", "alice@bupt.cn");
            ta.setCvFilePath("C:\\cv\\alice_resume.pdf");
            context.userRepository.save(ta);

            UserRepository restartedRepository = new UserRepository(context.usersFilePath);
            User loaded = restartedRepository.findById(ta.getId())
                    .orElseThrow(() -> new AssertionError("TA should exist after repository restart."));
            assertEquals("C:\\cv\\alice_resume.pdf", loaded.getCvFilePath(), "CV path must persist in CSV.");
        }
    }

    private void testLegacyUsersCsvCompatibility() throws Exception {
        try (TestContext context = new TestContext()) {
            List<String> lines = List.of(
                    "id,role,name,email,password,programme,yearOfStudy,skills,availableHours,active",
                    "\"TALEGACY01\",\"TA\",\"Legacy TA\",\"legacy@bupt.cn\",\"password1\","
                            + "\"Computer Science\",\"2\",\"Java;Python\",\"10\",\"true\"");
            FileUtil.writeAllLines(context.usersFilePath, lines);

            UserRepository repository = new UserRepository(context.usersFilePath);
            User loaded = repository.findById("TALEGACY01")
                    .orElseThrow(() -> new AssertionError("Legacy TA row should be readable."));
            assertEquals("", loaded.getCvFilePath(), "Legacy row should default CV path to empty string.");
        }
    }

    private void testTaProfileUpdatePersistsCvAndValidates() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA100000002", "Brenda", "brenda@bupt.cn");
            context.userRepository.save(ta);

            User existing = context.authService.findById(ta.getId())
                    .orElseThrow(() -> new AssertionError("TA should exist before update."));
            existing.setProgramme("Software Engineering");
            existing.setYearOfStudy(3);
            existing.setSkills("Java;Testing");
            existing.setAvailableHours(14);
            existing.setCvFilePath("C:\\cv\\brenda_cv.txt");
            context.authService.updateUser(existing);

            User reloaded = context.userRepository.findById(ta.getId())
                    .orElseThrow(() -> new AssertionError("Updated TA should still exist."));
            assertEquals("C:\\cv\\brenda_cv.txt", reloaded.getCvFilePath(), "Updated CV path should be persisted.");
            assertEquals(14, reloaded.getAvailableHours(), "Available hours should persist.");

            existing.setYearOfStudy(0);
            assertThrowsContains(
                    "Year of study must be between 1 and 12.",
                    () -> context.authService.updateUser(existing),
                    "TA profile should reject invalid year.");
        }
    }

    private void testCvValidationRules() {
        CvValidationUtil.validate("resume.pdf", 1024);
        CvValidationUtil.validate("resume.TXT", CvValidationUtil.MAX_CV_SIZE_BYTES);

        assertThrowsContains(
                "Only .pdf or .txt CV files are supported.",
                () -> CvValidationUtil.validate("resume.docx", 200),
                "Unsupported extension should be rejected.");
        assertThrowsContains(
                "CV file is too large. Please choose a file <= 5 MB.",
                () -> CvValidationUtil.validate("resume.pdf", CvValidationUtil.MAX_CV_SIZE_BYTES + 1),
                "Oversized CV should be rejected.");
    }

    private void testJobFilterRules() {
        Job javaJob = new Job(
                "JOB001",
                "CS101",
                "Intro Programming",
                "Assist lab",
                "Java;Tutor",
                6,
                2,
                "2026-12-01",
                "MO001",
                JobStatus.OPEN);
        Job mlJob = new Job(
                "JOB002",
                "AI301",
                "Machine Learning",
                "Supervise lab",
                "Python;ML",
                10,
                1,
                "2026-11-10",
                "MO002",
                JobStatus.OPEN);

        assertTrue(
                JobFilterUtil.matches(mlJob, "machine", "python", 10, "li", "OPEN", "Prof Li"),
                "Combined filter should match ML job.");
        assertFalse(
                JobFilterUtil.matches(javaJob, "machine", "python", 10, "li", "OPEN", "Prof Li"),
                "Combined filter should not match Java job.");
        assertTrue(
                JobFilterUtil.matches(javaJob, "", "", null, "", "ALL", "Dr Smith"),
                "ALL status with empty filters should match.");
        assertFalse(
                JobFilterUtil.matches(mlJob, "", "", 8, "", "OPEN", "Prof Li"),
                "Hours filter should exclude jobs over max hours.");
    }

    private void testAuthFlows() throws Exception {
        try (TestContext context = new TestContext()) {
            User registered = context.authService.registerTa(
                    "231226111",
                    "Clara",
                    "clara@bupt.cn",
                    "password1");
            assertEquals("TA231226111", registered.getId(), "TA ID should be student-ID based.");

            User loginById = context.authService.login("TA231226111", "password1");
            assertEquals("clara@bupt.cn", loginById.getEmail(), "Login by user ID should work.");

            User loginByEmail = context.authService.login("clara@bupt.cn", "password1");
            assertEquals("TA231226111", loginByEmail.getId(), "Login by email should work.");

            assertThrowsContains(
                    "Incorrect password.",
                    () -> context.authService.login("clara@bupt.cn", "wrong-pass"),
                    "Wrong password should be rejected.");
            assertThrowsContains(
                    "Email is already registered.",
                    () -> context.authService.registerTa("231226112", "Other", "clara@bupt.cn", "password1"),
                    "Duplicate email should be rejected.");

            context.authService.setUserActive("TA231226111", false);
            assertThrowsContains(
                    "This account is inactive.",
                    () -> context.authService.login("TA231226111", "password1"),
                    "Inactive user should not be able to login.");
        }
    }

    private void testJobValidationFlows() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO1001", "Dr Green", "dr.green@bupt.cn");
            User ta1 = newTa("TA100000003", "Derek", "derek@bupt.cn");
            User ta2 = newTa("TA100000004", "Emily", "emily@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta1);
            context.userRepository.save(ta2);

            String pastDate = LocalDate.now().minusDays(1).toString();
            assertThrowsContains(
                    "Deadline must be today or later.",
                    () -> context.jobService.createJob(
                            "CS900",
                            "Invalid Job",
                            "desc",
                            "Java",
                            "6",
                            "1",
                            pastDate,
                            mo.getId()),
                    "Past deadline job creation must be rejected.");

            Job job = context.jobService.createJob(
                    "CS901",
                    "Valid Job",
                    "desc",
                    "Java",
                    "6",
                    "3",
                    LocalDate.now().plusDays(5).toString(),
                    mo.getId());

            context.applicationRepository.save(new Application(
                    "APP1001",
                    job.getId(),
                    ta1.getId(),
                    ApplicationStatus.ACCEPTED,
                    LocalDate.now().toString()));
            context.applicationRepository.save(new Application(
                    "APP1002",
                    job.getId(),
                    ta2.getId(),
                    ApplicationStatus.ACCEPTED,
                    LocalDate.now().toString()));

            assertThrowsContains(
                    "Positions cannot be less than the number of accepted applications",
                    () -> context.jobService.updateJob(
                            job.getId(),
                            mo.getId(),
                            "CS901",
                            "Valid Job",
                            "desc",
                            "Java",
                            "6",
                            "1",
                            LocalDate.now().plusDays(10).toString()),
                    "Job update must respect accepted capacity.");
        }
    }

    private void testApplicationAuthorizationAndTransitions() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo1 = newMo("MO2001", "MO One", "mo.one@bupt.cn");
            User mo2 = newMo("MO2002", "MO Two", "mo.two@bupt.cn");
            User ta1 = newTa("TA100000005", "Fiona", "fiona@bupt.cn");
            User ta2 = newTa("TA100000006", "George", "george@bupt.cn");
            context.userRepository.save(mo1);
            context.userRepository.save(mo2);
            context.userRepository.save(ta1);
            context.userRepository.save(ta2);

            Job job = context.jobService.createJob(
                    "CS920",
                    "Distributed Systems",
                    "desc",
                    "Java",
                    "8",
                    "2",
                    LocalDate.now().plusDays(7).toString(),
                    mo1.getId());

            Application ta1App = context.applicationService.applyForJob(job.getId(), ta1.getId());
            assertEquals(ApplicationStatus.PENDING, ta1App.getStatus(), "New application should be pending.");

            assertThrowsContains(
                    "You can only review applications for your own jobs.",
                    () -> context.applicationService.updateApplicationStatus(
                            ta1App.getId(), mo2.getId(), ApplicationStatus.ACCEPTED),
                    "Other MO cannot review this application.");

            assertThrowsContains(
                    "You can only withdraw your own application.",
                    () -> context.applicationService.updateStatus(
                            ta1App.getId(), ta2.getId(), ApplicationStatus.WITHDRAWN),
                    "Other TA cannot withdraw this application.");

            context.applicationService.updateApplicationStatus(ta1App.getId(), mo1.getId(), ApplicationStatus.ACCEPTED);
            Application accepted = context.applicationService.getApplicationById(ta1App.getId())
                    .orElseThrow(() -> new AssertionError("Application should exist after accept."));
            assertEquals(ApplicationStatus.ACCEPTED, accepted.getStatus(), "MO should be able to accept pending app.");

            assertThrowsContains(
                    "Only pending applications can be withdrawn.",
                    () -> context.applicationService.updateStatus(
                            ta1App.getId(), ta1.getId(), ApplicationStatus.WITHDRAWN),
                    "Accepted application cannot be withdrawn.");

            Application ta2App = context.applicationService.applyForJob(job.getId(), ta2.getId());
            context.applicationService.updateStatus(ta2App.getId(), ta2.getId(), ApplicationStatus.WITHDRAWN);
            Application withdrawn = context.applicationService.getApplicationById(ta2App.getId())
                    .orElseThrow(() -> new AssertionError("Withdrawn application should still exist."));
            assertEquals(ApplicationStatus.WITHDRAWN, withdrawn.getStatus(), "Pending application can be withdrawn.");
        }
    }

    private void testDeleteJobGuard() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO3001", "MO Delete", "mo.delete@bupt.cn");
            User ta = newTa("TA100000007", "Helen", "helen@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job withApplication = context.jobService.createJob(
                    "CS930",
                    "Operating Systems",
                    "desc",
                    "C;Linux",
                    "6",
                    "1",
                    LocalDate.now().plusDays(4).toString(),
                    mo.getId());
            context.applicationService.applyForJob(withApplication.getId(), ta.getId());

            assertThrowsContains(
                    "Cannot delete a job that has related applications.",
                    () -> context.jobService.deleteJob(withApplication.getId()),
                    "Job with applications must not be deleted.");

            Job emptyJob = context.jobService.createJob(
                    "CS931",
                    "No Applicants Job",
                    "desc",
                    "Java",
                    "4",
                    "1",
                    LocalDate.now().plusDays(8).toString(),
                    mo.getId());
            context.jobService.deleteJob(emptyJob.getId());
            assertTrue(
                    context.jobService.getJobById(emptyJob.getId()).isEmpty(),
                    "Job without applications should be deletable.");
        }
    }

    private void testTaNotificationFilteringAndUnreadCount() {
        Job openJob = new Job(
                "JOB-NOTIFY-1",
                "CS901",
                "Notification Lab",
                "Assist lab",
                "Java",
                6,
                1,
                "2026-12-01",
                "MO001",
                JobStatus.OPEN);
        Job closedJob = new Job(
                "JOB-NOTIFY-2",
                "CS902",
                "Closed Lab",
                "Assist closed lab",
                "Python",
                4,
                1,
                "2026-12-02",
                "MO001",
                JobStatus.CLOSED);
        Application pendingOpen = new Application(
                "APP-NOTIFY-1",
                openJob.getId(),
                "TA100",
                ApplicationStatus.PENDING,
                "2026-04-20");
        Application accepted = new Application(
                "APP-NOTIFY-2",
                openJob.getId(),
                "TA100",
                ApplicationStatus.ACCEPTED,
                "2026-04-21");
        Application pendingClosed = new Application(
                "APP-NOTIFY-3",
                closedJob.getId(),
                "TA100",
                ApplicationStatus.PENDING,
                "2026-04-22");

        List<NotificationEntry> notifications = TaNotificationUtil.buildNotifications(
                List.of(pendingOpen, accepted, pendingClosed),
                List.of(openJob, closedJob));
        assertEquals(4, notifications.size(), "Three applications plus one closed-job alert should be shown.");
        assertTrue(
                notifications.stream().anyMatch(notification ->
                        "Job Closed".equals(notification.getType())
                                && notification.getMessage().contains("CS902")),
                "Closed pending jobs should create a visible TA notification.");

        Set<String> readIds = new HashSet<>();
        readIds.add(notifications.get(0).getId());
        assertEquals(3, TaNotificationUtil.countUnread(notifications, readIds), "Unread count should exclude read IDs.");
        assertEquals(
                3,
                TaNotificationUtil.filterByReadState(notifications, readIds, "Unread").size(),
                "Unread filter should hide read notifications.");
        assertEquals(
                1,
                TaNotificationUtil.filterByReadState(notifications, readIds, "Read").size(),
                "Read filter should only show read notifications.");
    }

    private void testTaNotificationSummaryAndClosedMessage() {
        ApplicationStatusSummary summary = TaNotificationUtil.summarizeApplications(List.of(
                new Application("APP-SUM-1", "JOB1", "TA100", ApplicationStatus.PENDING, "2026-04-20"),
                new Application("APP-SUM-2", "JOB2", "TA100", ApplicationStatus.ACCEPTED, "2026-04-21"),
                new Application("APP-SUM-3", "JOB3", "TA100", ApplicationStatus.REJECTED, "2026-04-22"),
                new Application("APP-SUM-4", "JOB4", "TA100", ApplicationStatus.WITHDRAWN, "2026-04-23")));

        assertEquals(1, summary.getPending(), "Pending count should be summarized.");
        assertEquals(1, summary.getAccepted(), "Accepted count should be summarized.");
        assertEquals(1, summary.getRejected(), "Rejected count should be summarized.");
        assertEquals(1, summary.getWithdrawn(), "Withdrawn count should be summarized.");
        assertEquals(
                "Applications: 1 pending, 1 accepted, 1 rejected, 1 withdrawn.",
                summary.format(),
                "Dashboard summary text should match TA status counts.");

        Job closedJob = new Job(
                "JOB-CLOSED-MSG",
                "AI401",
                "Closed AI Lab",
                "No longer accepting",
                "Python",
                5,
                1,
                "2026-12-03",
                "MO001",
                JobStatus.CLOSED);
        Job openJob = new Job(
                "JOB-OPEN-MSG",
                "AI402",
                "Open AI Lab",
                "Accepting applications",
                "Python",
                5,
                1,
                "2026-12-04",
                "MO001",
                JobStatus.OPEN);

        assertTrue(
                TaNotificationUtil.jobClosedApplyMessage(closedJob).contains("no longer accepts applications"),
                "Closed jobs should produce a clear apply-blocked message.");
        assertEquals(
                "",
                TaNotificationUtil.jobClosedApplyMessage(openJob),
                "Open jobs should not produce a closed-job apply warning.");
    }

    private void testEndToEndIntegrationFlow() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO4001", "Prof Zhao", "prof.zhao@bupt.cn");
            context.userRepository.save(mo);

            User ta = context.authService.registerTa("231226999", "Iris", "iris@bupt.cn", "password1");
            User taForProfile = context.authService.findById(ta.getId())
                    .orElseThrow(() -> new AssertionError("Registered TA should be queryable."));
            taForProfile.setProgramme("Data Science");
            taForProfile.setYearOfStudy(2);
            taForProfile.setSkills("Python;ML;SQL");
            taForProfile.setAvailableHours(12);
            taForProfile.setCvFilePath("C:\\cv\\iris_resume.pdf");
            context.authService.updateUser(taForProfile);

            Job job = context.jobService.createJob(
                    "DS500",
                    "Data Mining",
                    "Support labs",
                    "Python;ML",
                    "10",
                    "1",
                    LocalDate.now().plusDays(10).toString(),
                    mo.getId());
            Application app = context.applicationService.applyForJob(job.getId(), ta.getId());
            context.applicationService.updateApplicationStatus(app.getId(), mo.getId(), ApplicationStatus.ACCEPTED);

            List<Application> appsForMo = context.applicationService.getApplicationsForMo(mo.getId());
            assertEquals(1, appsForMo.size(), "MO should see one application for own job.");

            User taAsSeenByMo = context.authService.findById(appsForMo.get(0).getTaUserId())
                    .orElseThrow(() -> new AssertionError("MO should be able to resolve applicant user profile."));
            assertEquals("Data Science", taAsSeenByMo.getProgramme(), "MO should see TA programme.");
            assertEquals("Python;ML;SQL", taAsSeenByMo.getSkills(), "MO should see TA skills.");
            assertEquals(12, taAsSeenByMo.getAvailableHours(), "MO should see TA available hours.");
            assertEquals("C:\\cv\\iris_resume.pdf", taAsSeenByMo.getCvFilePath(), "MO should see TA CV info.");

            AdminService.TAWorkloadSummary summary = context.adminService.getTAWorkload(ta.getId());
            assertEquals(1, summary.getAcceptedJobCount(), "Admin workload should include accepted position.");
            assertEquals(10, summary.getTotalAssignedHours(), "Assigned hours should match accepted job hours.");
        }
    }

    private void runCase(String caseName, ThrowingRunnable testCase) throws Exception {
        try {
            testCase.run();
            passedCount++;
            System.out.println("[PASS] " + caseName);
        } catch (Throwable throwable) {
            failedCount++;
            System.out.println("[FAIL] " + caseName);
            System.out.println("       " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private static User newTa(String id, String name, String email) {
        return new User(
                id,
                Role.TA,
                name,
                email,
                "password1",
                "Computer Science",
                2,
                "Java",
                10,
                true,
                "");
    }

    private static User newMo(String id, String name, String email) {
        return new User(
                id,
                Role.MO,
                name,
                email,
                "password1",
                "",
                0,
                "",
                0,
                true,
                "");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertThrowsContains(String expectedMessagePart, Runnable runnable, String message) {
        try {
            runnable.run();
        } catch (RuntimeException runtimeException) {
            if (runtimeException.getMessage() != null
                    && runtimeException.getMessage().contains(expectedMessagePart)) {
                return;
            }
            throw new AssertionError(
                    message + " Wrong error message: " + runtimeException.getMessage(), runtimeException);
        }
        throw new AssertionError(message + " Expected exception was not thrown.");
    }

    private static final class TestContext implements AutoCloseable {
        private final Path tempDirectory;
        private final Path usersFilePath;
        private final Path jobsFilePath;
        private final Path applicationsFilePath;
        private final UserRepository userRepository;
        private final JobRepository jobRepository;
        private final ApplicationRepository applicationRepository;
        private final AuthService authService;
        private final JobService jobService;
        private final ApplicationService applicationService;
        private final AdminService adminService;

        private TestContext() throws Exception {
            this.tempDirectory = Files.createTempDirectory("ta-recruitment-tests-");
            this.usersFilePath = tempDirectory.resolve("users.csv");
            this.jobsFilePath = tempDirectory.resolve("jobs.csv");
            this.applicationsFilePath = tempDirectory.resolve("applications.csv");

            this.userRepository = new UserRepository(usersFilePath);
            this.jobRepository = new JobRepository(jobsFilePath);
            this.applicationRepository = new ApplicationRepository(applicationsFilePath);
            this.authService = new AuthService(userRepository);
            this.jobService = new JobService(jobRepository, applicationRepository);
            this.applicationService = new ApplicationService(applicationRepository, jobRepository);
            this.adminService = new AdminService(userRepository, jobRepository, applicationRepository);
        }

        @Override
        public void close() throws Exception {
            if (!Files.exists(tempDirectory)) {
                return;
            }
            try (var walk = Files.walk(tempDirectory)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                        // Ignore cleanup failures in tests.
                    }
                });
            }
        }
    }
}
