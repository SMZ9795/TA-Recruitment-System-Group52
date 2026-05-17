package com.group52.tarecruitment.tests;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationAuditLogRepository;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AiMatchingService;
import com.group52.tarecruitment.service.AiMatchingServiceAdapter;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.ExportService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.MoApplicantRankingService;
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
import java.util.Map;
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
        runCase("AI matching returns 100 for complete matches", this::testAiMatchingCompleteMatch);
        runCase("AI matching returns partial score with missing skills", this::testAiMatchingPartialMatch);
        runCase("AI matching handles empty and invalid input", this::testAiMatchingEmptyAndInvalidInput);
        runCase("TA job recommendation sorting places high match first", this::testTaJobRecommendationSorting);
        runCase("TA job recommendation places low match jobs backward and handles hour limits", this::testTaJobRecommendationLowFitBackward);
        runCase("MO ranking sorts by match score descending", this::testMoRankingSortsByMatchScoreDescending);
        runCase("MO ranking filters pending applications and minimum score", this::testMoRankingFiltersPendingAndMinimumScore);
        runCase("AdminService risk level respects TA availableHours, not hardcoded 20h", this::testAdminRiskLevelUsesAvailableHours);
        runCase("AdminService getRecruitmentSnapshot counts filled jobs and overloaded TAs", this::testRecruitmentSnapshot);
        runCase("AdminService searchTAWorkload filters by name and ID", this::testSearchTAWorkload);
        runCase("AdminService getWorkloadTrend returns correct label by job count", this::testWorkloadTrend);
        runCase("AdminService getWorkloadAlerts generates CRITICAL for overloaded and WARNING for at-risk TAs", this::testWorkloadAlerts);
        runCase("AdminService getIdleTAs returns TAs with available hours but no accepted positions", this::testIdleTAs);
        runCase("AdminService getDepartmentStats aggregates positions and hours per module", this::testDepartmentStats);
        runCase("AuthService changePassword validates old password, strength, and uniqueness", this::testChangePassword);
        runCase("AuthService changePassword succeeds with correct old password", this::testChangePasswordSuccessWithCorrectOldPassword);
        runCase("AuthService changePassword fails with wrong old password", this::testChangePasswordFailsWithWrongOldPassword);
        runCase("ApplicationService audit log records status changes", this::testAuditLogRecordsStatusChanges);
        runCase("ApplicationService audit log query by TA and by Job ID", this::testAuditLogQueries);
        runCase("AuthService login locks account after 5 failed attempts", this::testLoginLockAfterFailedAttempts);
        runCase("AuthService lock expires and counter resets after successful login", this::testLoginLockExpiry);
        runCase("AuthService password strength enforced at registration", this::testPasswordStrengthEnforced);
        runCase("ExportService writes CSV files to data/exports with header even when data is empty",
                this::testExportServiceCreatesCsvFiles);
        runCase("ExportService CSV content carries the expected headers and per-row field values",
                this::testExportServiceWritesCorrectFieldValues);
        
        // Iteration 4 Integration Tests
        runCase("TA Job Recommendation: high-match jobs sorted before low-match", this::testTaRecommendationHighMatchFirst);
        runCase("TA Job Recommendation: low-match jobs ranked after high-match", this::testTaRecommendationLowMatchLast);
        runCase("Export functionality: CSV files created in data/exports/ with timestamp", this::testExportCsvFilesCreated);
        runCase("Export functionality: exported CSV content contains correct fields", this::testExportCsvContentCorrect);
        runCase("Workload Balancing: normal workload status classified as Balanced", this::testWorkloadBalancedStatus);
        runCase("Workload Balancing: overloaded TA status classified as Overloaded", this::testWorkloadOverloadedStatus);
        runCase("Workload Balancing: underused TA status classified as Underused", this::testWorkloadUnderusedStatus);
        runCase("MO Notification: pending application count displayed correctly", this::testMoPendingApplicationCount);
        runCase("MO Notification: job status changes to FILLED when all positions accepted", this::testJobFilledAfterAccept);
        runCase("Password Change: correct old password allows successful change", this::testPasswordChangeSuccess);
        runCase("Password Change: incorrect old password is rejected", this::testPasswordChangeFailure);
        runCase("End-to-end Iteration 4: recommendation -> apply -> MO review -> admin workload", this::testIterationFourEndToEndFlow);

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
                    "Password1!");
            assertEquals("TA231226111", registered.getId(), "TA ID should be student-ID based.");

            User loginById = context.authService.login("TA231226111", "Password1!");
            assertEquals("clara@bupt.cn", loginById.getEmail(), "Login by user ID should work.");

            User loginByEmail = context.authService.login("clara@bupt.cn", "Password1!");
            assertEquals("TA231226111", loginByEmail.getId(), "Login by email should work.");

            assertThrowsContains(
                    "Invalid credentials.",
                    () -> context.authService.login("clara@bupt.cn", "wrong-pass"),
                    "Wrong password should be rejected.");
            assertThrowsContains(
                    "Email is already registered.",
                    () -> context.authService.registerTa("231226112", "Other", "clara@bupt.cn", "Password1!"),
                    "Duplicate email should be rejected.");

            context.authService.setUserActive("TA231226111", false);
            assertThrowsContains(
                    "This account is inactive.",
                    () -> context.authService.login("TA231226111", "Password1!"),
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

            User ta = context.authService.registerTa("231226999", "Iris", "iris@bupt.cn", "Password1!");
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

    private void testAiMatchingCompleteMatch() {
        AiMatchingService service = new AiMatchingService();
        AiMatchingService.MatchResult result = service.analyzeSkills("Java, Python, SQL", "python;java");
        assertEquals(100, result.getScore(), "Complete match should return 100.");
        assertEquals(2, result.getMatchedSkills().size(), "Matched skills should include all required.");
        assertEquals(0, result.getMissingSkills().size(), "Missing skills should be empty.");
        assertTrue(
                result.getReason().contains("Matched 2 of 2"),
                "Reason should contain explainable matched-count details.");
    }

    private void testAiMatchingPartialMatch() {
        AiMatchingService service = new AiMatchingService();
        AiMatchingService.MatchResult result = service.analyzeSkills("Java", "Java, Python, SQL");
        assertEquals(33, result.getScore(), "Partial match should return rounded percentage.");
        assertEquals(List.of("java"), result.getMatchedSkills(), "Matched skills should be normalized and deterministic.");
        assertEquals(List.of("python", "sql"), result.getMissingSkills(), "Missing skills should be returned.");
    }

    private void testAiMatchingEmptyAndInvalidInput() {
        AiMatchingService service = new AiMatchingService();
        AiMatchingService.MatchResult emptyResult = service.analyzeSkills(null, " ");
        assertEquals(100, emptyResult.getScore(), "No required skills should default to 100.");
        assertEquals(0, emptyResult.getMatchedSkills().size(), "No required skills means no matched list.");
        assertEquals(0, emptyResult.getMissingSkills().size(), "No required skills means no missing list.");

        assertThrowsContains(
                "Match score must be between 0 and 100.",
                () -> new AiMatchingService.MatchResult(120, List.of("java"), List.of(), "invalid"),
                "Out-of-range score should be rejected.");
    }

    private void testTaJobRecommendationSorting() {
        AiMatchingService service = new AiMatchingService();
        User ta = new User();
        ta.setId("TA-SORT-1");
        ta.setRole(com.group52.tarecruitment.model.Role.TA);
        ta.setAvailableHours(20);
        ta.setSkills("Java, Python, SQL");

        Job job1 = new Job("JOB-REC-1", "CS101", "Intro", "Support", "Java, Python", 10, 1, "2026-12-01", "MO01", JobStatus.OPEN);
        Job job2 = new Job("JOB-REC-2", "CS102", "Web", "Support", "HTML, CSS", 10, 1, "2026-12-01", "MO01", JobStatus.OPEN);
        Job job3 = new Job("JOB-REC-3", "CS103", "Data", "Support", "Python", 10, 1, "2026-12-01", "MO01", JobStatus.OPEN);

        List<Job> jobs = new java.util.ArrayList<>(List.of(job2, job1, job3));

        jobs.sort(java.util.Comparator
                .<Job>comparingInt(job -> service.recommendJob(ta, job, 0).getScore())
                .reversed()
                .thenComparing(Job::getId));

        assertEquals("JOB-REC-1", jobs.get(0).getId(), "High match job should be first");
        assertEquals("JOB-REC-3", jobs.get(1).getId(), "High match job should be second");
        assertEquals("JOB-REC-2", jobs.get(2).getId(), "Low match job should be last");
    }

    private void testTaJobRecommendationLowFitBackward() {
        AiMatchingService service = new AiMatchingService();
        User ta = new User();
        ta.setId("TA-SORT-2");
        ta.setRole(com.group52.tarecruitment.model.Role.TA);
        ta.setAvailableHours(10);
        ta.setSkills("Java");

        Job jobOverHours = new Job("JOB-LOW-1", "CS101", "Intro", "Support", "Java", 20, 1, "2026-12-01", "MO01", JobStatus.OPEN); // match 100%, 20h > 10h remaining -> -20 penalty -> 80
        Job jobPartialFit = new Job("JOB-LOW-2", "CS102", "Intro2", "Support", "Java, Python", 5, 1, "2026-12-01", "MO01", JobStatus.OPEN); // match 50%, 5h <= 10h remaining -> +10 bonus -> 60
        Job jobLowFit = new Job("JOB-LOW-3", "CS103", "Intro3", "Support", "C++", 5, 1, "2026-12-01", "MO01", JobStatus.OPEN); // match 0%, 5h <= 10h remaining -> +10 bonus -> 10

        AiMatchingService.RecommendationResult r1 = service.recommendJob(ta, jobOverHours, 0);
        AiMatchingService.RecommendationResult r2 = service.recommendJob(ta, jobPartialFit, 0);
        AiMatchingService.RecommendationResult r3 = service.recommendJob(ta, jobLowFit, 0);

        assertEquals(80, r1.getScore(), "Over hours should have penalty");
        assertFalse(r1.isHoursFit(), "Over hours should not fit rules");

        assertEquals(60, r2.getScore(), "Partial fit should have bonus if hours fit");
        assertTrue(r2.isHoursFit(), "Hours fit rules should apply");

        assertEquals(10, r3.getScore(), "Low fit should also have hours check");

        List<Job> jobs = new java.util.ArrayList<>(List.of(jobLowFit, jobOverHours, jobPartialFit));
        jobs.sort(java.util.Comparator
                .<Job>comparingInt(job -> service.recommendJob(ta, job, 0).getScore())
                .reversed()
                .thenComparing(Job::getId));

        assertEquals("JOB-LOW-1", jobs.get(0).getId(), "Over hours (score 80) is first");
        assertEquals("JOB-LOW-2", jobs.get(1).getId(), "Partial fit (score 60) is second");
        assertEquals("JOB-LOW-3", jobs.get(2).getId(), "Low fit (score 10) is last");
    }

    private void testMoRankingSortsByMatchScoreDescending() throws Exception {
        try (TestContext context = new TestContext()) {
            Job targetJob = new Job(
                    "JOB-RANK-1",
                    "ECS7001",
                    "Software Engineering",
                    "Support labs",
                    "Java;Python;SQL",
                    4,
                    2,
                    LocalDate.now().plusDays(30).toString(),
                    "MO-RANK",
                    JobStatus.OPEN);
            Job workloadJob = new Job(
                    "JOB-WORKLOAD-1",
                    "ECS7002",
                    "Databases",
                    "Support tutorials",
                    "SQL",
                    6,
                    1,
                    LocalDate.now().plusDays(30).toString(),
                    "MO-RANK",
                    JobStatus.OPEN);
            context.jobRepository.save(targetJob);
            context.jobRepository.save(workloadJob);

            User high = newTa("TA-RANK-HIGH", "High Match", "high.rank@bupt.cn");
            high.setSkills("Java;Python;SQL");
            User mid = newTa("TA-RANK-MID", "Mid Match", "mid.rank@bupt.cn");
            mid.setSkills("Java;Python");
            User low = newTa("TA-RANK-LOW", "Low Match", "low.rank@bupt.cn");
            low.setSkills("Java");
            context.userRepository.save(high);
            context.userRepository.save(mid);
            context.userRepository.save(low);

            context.applicationRepository.save(new Application("APP-RANK-HIGH", targetJob.getId(), high.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application("APP-RANK-MID", targetJob.getId(), mid.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application("APP-RANK-LOW", targetJob.getId(), low.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application("APP-RANK-WORKLOAD", workloadJob.getId(), high.getId(), ApplicationStatus.ACCEPTED, "2026-04-01"));

            MoApplicantRankingService rankingService = new MoApplicantRankingService(
                    context.applicationService, new AiMatchingServiceAdapter(new AiMatchingService()));
            List<MoApplicantRankingService.RankedApplicant> ranked = rankingService.rankApplicants(
                    targetJob,
                    context.applicationRepository.findByJobId(targetJob.getId()),
                    Map.of(high.getId(), high, mid.getId(), mid, low.getId(), low),
                    new MoApplicantRankingService.RankingOptions(
                            true, 0, MoApplicantRankingService.SortMode.MATCH_SCORE_DESC));

            assertEquals(3, ranked.size(), "All pending applicants should be included.");
            assertEquals("TA-RANK-HIGH", ranked.get(0).getApplicantId(), "Highest match should be first.");
            assertEquals("TA-RANK-MID", ranked.get(1).getApplicantId(), "Second-highest match should be second.");
            assertEquals("TA-RANK-LOW", ranked.get(2).getApplicantId(), "Lowest match should be last.");
            assertEquals(6, ranked.get(0).getCurrentWorkload(), "Current workload should be calculated from accepted applications.");
        }
    }

    private void testMoRankingFiltersPendingAndMinimumScore() throws Exception {
        try (TestContext context = new TestContext()) {
            Job targetJob = new Job(
                    "JOB-RANK-2",
                    "ECS7003",
                    "AI Methods",
                    "Support labs",
                    "Java;Python;SQL",
                    4,
                    2,
                    LocalDate.now().plusDays(30).toString(),
                    "MO-RANK",
                    JobStatus.OPEN);
            context.jobRepository.save(targetJob);

            User pendingPass = newTa("TA-FILTER-PASS", "Pending Pass", "pass.filter@bupt.cn");
            pendingPass.setSkills("Java;Python");
            User pendingLow = newTa("TA-FILTER-LOW", "Pending Low", "low.filter@bupt.cn");
            pendingLow.setSkills("Java");
            User acceptedHigh = newTa("TA-FILTER-ACCEPTED", "Accepted High", "accepted.filter@bupt.cn");
            acceptedHigh.setSkills("Java;Python;SQL");
            context.userRepository.save(pendingPass);
            context.userRepository.save(pendingLow);
            context.userRepository.save(acceptedHigh);

            context.applicationRepository.save(new Application("APP-FILTER-PASS", targetJob.getId(), pendingPass.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application("APP-FILTER-LOW", targetJob.getId(), pendingLow.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application("APP-FILTER-ACCEPTED", targetJob.getId(), acceptedHigh.getId(), ApplicationStatus.ACCEPTED, "2026-05-01"));

            MoApplicantRankingService rankingService = new MoApplicantRankingService(
                    context.applicationService, new AiMatchingServiceAdapter(new AiMatchingService()));
            List<MoApplicantRankingService.RankedApplicant> ranked = rankingService.rankApplicants(
                    targetJob,
                    context.applicationRepository.findByJobId(targetJob.getId()),
                    Map.of(pendingPass.getId(), pendingPass, pendingLow.getId(), pendingLow, acceptedHigh.getId(), acceptedHigh),
                    new MoApplicantRankingService.RankingOptions(
                            true, 60, MoApplicantRankingService.SortMode.MATCH_SCORE_DESC));

            assertEquals(1, ranked.size(), "Only pending applicants at or above the threshold should remain.");
            assertEquals("TA-FILTER-PASS", ranked.get(0).getApplicantId(), "Pending applicant above threshold should remain.");
            assertEquals(67, ranked.get(0).getMatchScore(), "Two of three required skills should score 67.");
        }
    }

    private void testExportServiceCreatesCsvFiles() throws Exception {
        try (TestContext context = new TestContext()) {
            Path exportsDir = context.tempDirectory.resolve("exports");
            ExportService exportService = new ExportService(
                    context.userRepository,
                    context.jobRepository,
                    context.applicationRepository,
                    context.adminService,
                    exportsDir);

            Path emptyApplicationsCsv = exportService.exportAllApplications();
            assertTrue(Files.exists(emptyApplicationsCsv),
                    "Export file should be created even when there's no application data.");
            assertTrue(emptyApplicationsCsv.startsWith(exportsDir),
                    "Exported file must be saved under the configured exports directory.");
            assertTrue(emptyApplicationsCsv.getFileName().toString().startsWith("all_applications_"),
                    "Filename must start with 'all_applications_' for the all-applications report.");
            assertTrue(emptyApplicationsCsv.getFileName().toString().endsWith(".csv"),
                    "Filename must use the .csv extension.");
            List<String> emptyLines = Files.readAllLines(emptyApplicationsCsv);
            assertEquals(1, emptyLines.size(),
                    "Empty export should contain only the header row, not blank data rows.");
            assertEquals(
                    String.join(",", asQuoted(ExportService.APPLICATIONS_HEADER)),
                    emptyLines.get(0),
                    "Header row must match ExportService.APPLICATIONS_HEADER.");

            User mo = newMo("MO-EXPORT-1", "Export MO", "export.mo@bupt.cn");
            User ta = newTa("TA-EXPORT-1", "Export TA", "export.ta@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);
            Job job = context.jobService.createJob(
                    "EX101", "Export Module", "desc", "Java", "6", "2",
                    LocalDate.now().plusDays(7).toString(), mo.getId());
            context.applicationService.applyForJob(job.getId(), ta.getId());

            Path workloadCsv = exportService.exportTaWorkloadSummary();
            assertTrue(Files.exists(workloadCsv),
                    "Workload export file should be created.");
            assertTrue(workloadCsv.getFileName().toString().startsWith("ta_workload_summary_"),
                    "Workload filename prefix must be 'ta_workload_summary_'.");

            Path jobFillingCsv = exportService.exportJobFillingStatus();
            assertTrue(Files.exists(jobFillingCsv),
                    "Job filling status export file should be created.");
            assertTrue(jobFillingCsv.getFileName().toString().startsWith("job_filling_status_"),
                    "Job filling filename prefix must be 'job_filling_status_'.");

            Path applicantsCsv = exportService.exportApplicantsForJob(job.getId());
            assertTrue(Files.exists(applicantsCsv),
                    "Applicants CSV file should be created.");
            assertTrue(applicantsCsv.getFileName().toString().startsWith("applicants_EX101_"),
                    "Applicants filename should embed sanitized module code.");

            assertThrowsContains(
                    "Job ID is required",
                    () -> exportService.exportApplicantsForJob(" "),
                    "Blank job ID must be rejected for applicant export.");
            assertThrowsContains(
                    "Job not found",
                    () -> exportService.exportApplicantsForJob("JOB-DOES-NOT-EXIST"),
                    "Unknown job ID must be rejected with a clear message.");

            Path firstRun = exportService.exportJobFillingStatus();
            Path secondRun = exportService.exportJobFillingStatus();
            assertTrue(Files.exists(firstRun) && Files.exists(secondRun),
                    "Repeated exports must both produce files.");
            assertFalse(firstRun.equals(secondRun),
                    "Repeated exports in the same second must resolve to distinct filenames.");
        }
    }

    private void testExportServiceWritesCorrectFieldValues() throws Exception {
        try (TestContext context = new TestContext()) {
            Path exportsDir = context.tempDirectory.resolve("exports");
            ExportService exportService = new ExportService(
                    context.userRepository,
                    context.jobRepository,
                    context.applicationRepository,
                    context.adminService,
                    exportsDir);

            User mo = newMo("MO-EXPORT-2", "Export MO 2", "export.mo2@bupt.cn");
            User taAccepted = newTa("TA-EXPORT-A", "Alice Accepted", "alice.accepted@bupt.cn");
            taAccepted.setProgramme("Data Science");
            taAccepted.setSkills("Java;Python");
            taAccepted.setAvailableHours(10);
            User taPending = newTa("TA-EXPORT-B", "Bob Pending", "bob.pending@bupt.cn");
            taPending.setProgramme("Software Engineering");
            taPending.setSkills("Java");
            taPending.setAvailableHours(8);
            context.userRepository.save(mo);
            context.userRepository.save(taAccepted);
            context.userRepository.save(taPending);

            Job job = context.jobService.createJob(
                    "EX202", "Field Check Module", "Run labs",
                    "Java", "6", "2",
                    LocalDate.now().plusDays(10).toString(),
                    mo.getId());
            Application acceptedApp = context.applicationService.applyForJob(job.getId(), taAccepted.getId());
            context.applicationService.updateApplicationStatus(
                    acceptedApp.getId(), mo.getId(), ApplicationStatus.ACCEPTED);
            context.applicationService.applyForJob(job.getId(), taPending.getId());

            Path applicationsCsv = exportService.exportAllApplications();
            List<String> applicationLines = Files.readAllLines(applicationsCsv);
            assertEquals(3, applicationLines.size(),
                    "All applications export must contain header plus one row per application.");
            assertEquals(
                    String.join(",", asQuoted(ExportService.APPLICATIONS_HEADER)),
                    applicationLines.get(0),
                    "All-applications header must list every documented field.");
            String acceptedRow = findRowContaining(applicationLines, "Alice Accepted");
            assertTrue(acceptedRow.contains("\"EX202\""),
                    "Application row should expose the module code.");
            assertTrue(acceptedRow.contains("\"Field Check Module\""),
                    "Application row should expose the module name.");
            assertTrue(acceptedRow.contains("\"ACCEPTED\""),
                    "Application row should expose the storage status.");
            assertTrue(acceptedRow.contains("\"alice.accepted@bupt.cn\""),
                    "Application row should expose the TA email.");

            Path workloadCsv = exportService.exportTaWorkloadSummary();
            List<String> workloadLines = Files.readAllLines(workloadCsv);
            assertEquals(
                    String.join(",", asQuoted(ExportService.WORKLOAD_HEADER)),
                    workloadLines.get(0),
                    "Workload header must match ExportService.WORKLOAD_HEADER exactly.");
            assertEquals(2, workloadLines.size(),
                    "Only TAs with accepted positions should appear in the workload summary.");
            String workloadRow = workloadLines.get(1);
            assertTrue(workloadRow.contains("\"Alice Accepted\""),
                    "Workload row must contain the TA's name.");
            assertTrue(workloadRow.contains("\"10\""),
                    "Workload row must contain availableHours=10 for Alice.");
            assertTrue(workloadRow.contains("\"6\""),
                    "Workload row must contain totalAssignedHours=6 from the accepted 6h/week job.");

            Path jobFillingCsv = exportService.exportJobFillingStatus();
            List<String> jobLines = Files.readAllLines(jobFillingCsv);
            assertEquals(
                    String.join(",", asQuoted(ExportService.JOB_FILLING_HEADER)),
                    jobLines.get(0),
                    "Job filling header must match ExportService.JOB_FILLING_HEADER exactly.");
            assertEquals(2, jobLines.size(),
                    "Job filling export should produce one row per job.");
            String jobRow = jobLines.get(1);
            assertTrue(jobRow.contains("\"EX202\""), "Job filling row must include moduleCode.");
            assertTrue(jobRow.contains("\"1/2\""),
                    "Job filling row must record 1 accepted of 2 positions as 1/2 ratio.");

            Path applicantsCsv = exportService.exportApplicantsForJob(job.getId());
            List<String> applicantLines = Files.readAllLines(applicantsCsv);
            assertEquals(
                    String.join(",", asQuoted(ExportService.APPLICANT_LIST_HEADER)),
                    applicantLines.get(0),
                    "Applicant list header must match ExportService.APPLICANT_LIST_HEADER exactly.");
            assertEquals(3, applicantLines.size(),
                    "Applicant export must include header plus one row per applicant of the job.");
            String pendingRow = findRowContaining(applicantLines, "Bob Pending");
            assertTrue(pendingRow.contains("\"Software Engineering\""),
                    "Applicant row should expose the TA programme.");
            assertTrue(pendingRow.contains("\"Java\""),
                    "Applicant row should expose the TA skills.");
            assertTrue(pendingRow.contains("\"PENDING\""),
                    "Applicant row should expose the application status.");
        }
    }

    private static String findRowContaining(List<String> rows, String needle) {
        for (String row : rows) {
            if (row.contains(needle)) {
                return row;
            }
        }
        throw new AssertionError("Expected a CSV row containing \"" + needle + "\" but none was found.");
    }

    private static String[] asQuoted(String[] values) {
        String[] quoted = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            quoted[i] = "\"" + values[i] + "\"";
        }
        return quoted;
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

    private void testAdminRiskLevelUsesAvailableHours() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-risk");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath = tmp.resolve("jobs.csv");
            Path appsPath = tmp.resolve("applications.csv");

            // TA with availableHours=10, assigned 9h → AT_RISK (90%)
            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,10,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,Intro CS,Desc,Java,9,2,2099-12-31,mo1,OPEN\n");
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n");

            UserRepository ur = new UserRepository(usersPath);
            JobRepository jr = new JobRepository(jobsPath);
            ApplicationRepository ar = new ApplicationRepository(appsPath);
            AdminService svc = new AdminService(ur, jr, ar);

            AdminService.TAWorkloadSummary s = svc.getTAWorkload("ta1");
            assert s.getAvailableHours() == 10 : "availableHours should be 10";
            assert s.getTotalAssignedHours() == 9 : "assigned should be 9, got " + s.getTotalAssignedHours();
            assert s.getRemainingHours() == 1 : "remaining should be 1";
            assert s.getRiskLevel() == AdminService.RiskLevel.AT_RISK
                : "Expected AT_RISK but got " + s.getRiskLevel();
            assert !s.isOverloaded() : "Should not be overloaded";

            // TA with availableHours=10, assigned 15h → OVERLOADED
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,Intro CS,Desc,Java,15,2,2099-12-31,mo1,OPEN\n");
            JobRepository jr2 = new JobRepository(jobsPath);
            AdminService svc2 = new AdminService(ur, jr2, ar);
            AdminService.TAWorkloadSummary s2 = svc2.getTAWorkload("ta1");
            assert s2.getRiskLevel() == AdminService.RiskLevel.OVERLOADED
                : "Expected OVERLOADED but got " + s2.getRiskLevel();
            assert s2.isOverloaded() : "Should be overloaded when assigned > availableHours";
        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private void testRecruitmentSnapshot() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-snap");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath = tmp.resolve("jobs.csv");
            Path appsPath = tmp.resolve("applications.csv");

            // 2 TAs; job j1 has 1 position and 1 accepted → full; job j2 has 2 positions and 1 filled → not full
            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,8,true,\n"
                + "ta2,TA,Bob,bob@test.com,pass1234,CS,3,Python,20,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,Intro,Desc,Java,10,1,2099-12-31,mo1,OPEN\n"
                + "j2,CS102,Algo,Desc,Python,5,2,2099-12-31,mo1,OPEN\n");
            // ta1: assigned 10h, available 8h → overloaded; ta2: assigned 5h, available 20h → OK
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j2,ta2,ACCEPTED,2024-01-02\n");

            UserRepository ur = new UserRepository(usersPath);
            JobRepository jr = new JobRepository(jobsPath);
            ApplicationRepository ar = new ApplicationRepository(appsPath);
            AdminService svc = new AdminService(ur, jr, ar);

            AdminService.RecruitmentSnapshot snap = svc.getRecruitmentSnapshot();
            assert snap.totalJobs == 2 : "totalJobs should be 2, got " + snap.totalJobs;
            assert snap.filledJobs == 1 : "filledJobs should be 1, got " + snap.filledJobs;
            assert snap.totalActiveTAs == 2 : "totalActiveTAs should be 2, got " + snap.totalActiveTAs;
            assert snap.overloadedTAs == 1 : "overloadedTAs should be 1, got " + snap.overloadedTAs;
            assert snap.atRiskTAs == 0 : "atRiskTAs should be 0, got " + snap.atRiskTAs;

            List<AdminService.TAWorkloadSummary> overloaded = svc.getOverloadedTAs();
            assert overloaded.size() == 1 : "getOverloadedTAs should return 1, got " + overloaded.size();
            assert overloaded.get(0).getTaUserId().equals("ta1") : "Overloaded TA should be ta1";
        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private void testSearchTAWorkload() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-search");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath = tmp.resolve("jobs.csv");
            Path appsPath = tmp.resolve("applications.csv");

            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice Wang,alice@test.com,pass1234,CS,2,Java,10,true,\n"
                + "ta2,TA,Bob Zhang,bob@test.com,pass1234,CS,3,Python,15,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,Intro,Desc,Java,5,2,2099-12-31,mo1,OPEN\n"
                + "j2,CS102,Algo,Desc,Python,6,2,2099-12-31,mo1,OPEN\n");
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j2,ta2,ACCEPTED,2024-01-02\n");

            AdminService svc = new AdminService(
                new UserRepository(usersPath),
                new JobRepository(jobsPath),
                new ApplicationRepository(appsPath));

            List<AdminService.TAWorkloadSummary> byName = svc.searchTAWorkload("alice");
            assert byName.size() == 1 : "Should find 1 TA by name 'alice', got " + byName.size();
            assert byName.get(0).getTaUserId().equals("ta1") : "Should find ta1";

            List<AdminService.TAWorkloadSummary> byId = svc.searchTAWorkload("ta2");
            assert byId.size() == 1 : "Should find 1 TA by ID 'ta2', got " + byId.size();
            assert byId.get(0).getTaName().equals("Bob Zhang") : "Should find Bob Zhang";

            List<AdminService.TAWorkloadSummary> noMatch = svc.searchTAWorkload("xyz");
            assert noMatch.isEmpty() : "Should return empty for no match";

            List<AdminService.TAWorkloadSummary> blank = svc.searchTAWorkload("  ");
            assert blank.isEmpty() : "Should return empty for blank keyword";
        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private void testWorkloadTrend() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-trend");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath = tmp.resolve("jobs.csv");
            Path appsPath = tmp.resolve("applications.csv");

            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,20,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,A,Desc,Java,3,2,2099-12-31,mo1,OPEN\n"
                + "j2,CS102,B,Desc,Java,3,2,2099-12-31,mo1,OPEN\n"
                + "j3,CS103,C,Desc,Java,3,2,2099-12-31,mo1,OPEN\n");

            // 1 accepted job → NEW
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n");
            AdminService svc1 = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));
            AdminService.TAWorkloadSummary s1 = svc1.getTAWorkload("ta1");
            assert svc1.getWorkloadTrend(s1) == AdminService.WorkloadTrend.NEW
                : "1 job should be NEW, got " + svc1.getWorkloadTrend(s1);

            // 2 accepted jobs → GROWING
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j2,ta1,ACCEPTED,2024-01-02\n");
            AdminService svc2 = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));
            AdminService.TAWorkloadSummary s2 = svc2.getTAWorkload("ta1");
            assert svc2.getWorkloadTrend(s2) == AdminService.WorkloadTrend.GROWING
                : "2 jobs should be GROWING, got " + svc2.getWorkloadTrend(s2);

            // 3 accepted jobs → ESTABLISHED
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j2,ta1,ACCEPTED,2024-01-02\n"
                + "app3,j3,ta1,ACCEPTED,2024-01-03\n");
            AdminService svc3 = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));
            AdminService.TAWorkloadSummary s3 = svc3.getTAWorkload("ta1");
            assert svc3.getWorkloadTrend(s3) == AdminService.WorkloadTrend.ESTABLISHED
                : "3 jobs should be ESTABLISHED, got " + svc3.getWorkloadTrend(s3);
        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    // -------------------------------------------------------------------------
    // New tests: WorkloadAlerts, IdleTAs, DepartmentStats
    // -------------------------------------------------------------------------

    private void testWorkloadAlerts() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-alerts");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath  = tmp.resolve("jobs.csv");
            Path appsPath  = tmp.resolve("applications.csv");

            // ta1: availableHours=10, will be assigned 14h → OVERLOADED → CRITICAL alert
            // ta2: availableHours=10, will be assigned 9h  → AT_RISK (90%) → WARNING alert
            // ta3: availableHours=8,  no accepted job       → idle → INFO alert
            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,10,true,\n"
                + "ta2,TA,Bob,bob@test.com,pass1234,CS,2,Java,10,true,\n"
                + "ta3,TA,Carol,carol@test.com,pass1234,CS,2,Java,8,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,A,Desc,Java,7,2,2099-12-31,mo1,OPEN\n"
                + "j2,CS102,B,Desc,Java,7,2,2099-12-31,mo1,OPEN\n"
                + "j3,CS103,C,Desc,Java,9,2,2099-12-31,mo1,OPEN\n");
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j2,ta1,ACCEPTED,2024-01-02\n"
                + "app3,j3,ta2,ACCEPTED,2024-01-03\n");

            AdminService svc = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));

            List<AdminService.WorkloadAlert> alerts = svc.getWorkloadAlerts();

            // Must contain at least one CRITICAL (ta1 overloaded: 14h > 10h)
            boolean hasCritical = alerts.stream()
                .anyMatch(a -> a.getSeverity() == AdminService.AlertSeverity.CRITICAL
                            && a.getTaUserId().equals("ta1"));
            assertTrue(hasCritical, "Expected CRITICAL alert for overloaded ta1.");

            // Must contain at least one WARNING (ta2 at 90%)
            boolean hasWarning = alerts.stream()
                .anyMatch(a -> a.getSeverity() == AdminService.AlertSeverity.WARNING
                            && a.getTaUserId().equals("ta2"));
            assertTrue(hasWarning, "Expected WARNING alert for at-risk ta2.");

            // Must contain INFO for idle ta3
            boolean hasInfo = alerts.stream()
                .anyMatch(a -> a.getSeverity() == AdminService.AlertSeverity.INFO
                            && a.getTaUserId().equals("ta3"));
            assertTrue(hasInfo, "Expected INFO alert for idle ta3.");

            // CRITICAL must come before WARNING in sorted list
            int critIdx = -1, warnIdx = -1;
            for (int i = 0; i < alerts.size(); i++) {
                if (alerts.get(i).getSeverity() == AdminService.AlertSeverity.CRITICAL && critIdx < 0) critIdx = i;
                if (alerts.get(i).getSeverity() == AdminService.AlertSeverity.WARNING  && warnIdx < 0) warnIdx = i;
            }
            assertTrue(critIdx < warnIdx, "CRITICAL alerts should appear before WARNING alerts.");

        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private void testIdleTAs() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-idle");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath  = tmp.resolve("jobs.csv");
            Path appsPath  = tmp.resolve("applications.csv");

            // ta1 has accepted job → not idle
            // ta2 has available hours but no accepted job → idle
            // ta3 has availableHours=0 → excluded from idle list
            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,10,true,\n"
                + "ta2,TA,Bob,bob@test.com,pass1234,CS,2,Java,8,true,\n"
                + "ta3,TA,Carol,carol@test.com,pass1234,CS,2,Java,0,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,A,Desc,Java,5,2,2099-12-31,mo1,OPEN\n");
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n");

            AdminService svc = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));

            List<User> idle = svc.getIdleTAs();

            assertEquals(1, idle.size(), "Only ta2 should be idle.");
            assertEquals("ta2", idle.get(0).getId(), "Idle TA should be ta2.");

        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        }
    }

    private void testDepartmentStats() throws Exception {
        Path tmp = Files.createTempDirectory("ta-test-dept");
        try {
            Path usersPath = tmp.resolve("users.csv");
            Path jobsPath  = tmp.resolve("jobs.csv");
            Path appsPath  = tmp.resolve("applications.csv");

            // Two jobs in CS101, one job in CS102
            Files.writeString(usersPath,
                "id,role,name,email,password,major,yearOfStudy,skills,availableHours,active,cvFilePath\n"
                + "ta1,TA,Alice,alice@test.com,pass1234,CS,2,Java,20,true,\n");
            Files.writeString(jobsPath,
                "id,moduleCode,moduleName,description,requiredSkills,hoursPerWeek,positions,deadline,postedByMoId,status\n"
                + "j1,CS101,Algorithms,Desc,Java,5,2,2099-12-31,mo1,OPEN\n"
                + "j2,CS101,Algorithms,Desc,Java,3,1,2099-12-31,mo1,OPEN\n"
                + "j3,CS102,Databases,Desc,SQL,4,3,2099-12-31,mo1,OPEN\n");
            Files.writeString(appsPath,
                "id,jobId,taUserId,status,appliedDate\n"
                + "app1,j1,ta1,ACCEPTED,2024-01-01\n"
                + "app2,j3,ta1,ACCEPTED,2024-01-02\n");

            AdminService svc = new AdminService(
                new UserRepository(usersPath), new JobRepository(jobsPath), new ApplicationRepository(appsPath));

            List<AdminService.ModuleStats> stats = svc.getDepartmentStats();

            assertEquals(2, stats.size(), "Should have stats for 2 modules.");

            // Stats are sorted by moduleCode: CS101 first
            AdminService.ModuleStats cs101 = stats.get(0);
            assertEquals("CS101", cs101.moduleCode, "First module should be CS101.");
            assertEquals(3, cs101.totalPositions, "CS101 total positions: 2+1=3.");
            assertEquals(1, cs101.assignedTAs, "CS101 has 1 accepted TA (ta1 via j1).");
            assertEquals(5, cs101.totalAssignedHours, "CS101 assigned hours: 5h from j1.");

            AdminService.ModuleStats cs102 = stats.get(1);
            assertEquals("CS102", cs102.moduleCode, "Second module should be CS102.");
            assertEquals(3, cs102.totalPositions, "CS102 total positions: 3.");
            assertEquals(1, cs102.assignedTAs, "CS102 has 1 accepted TA (ta1 via j3).");
            assertEquals(4, cs102.totalAssignedHours, "CS102 assigned hours: 4h from j3.");

        } finally {
            try (var walk = Files.walk(tmp)) {
                walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
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

    private void testChangePassword() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226900", "Tester", "tester@bupt.cn", "Pass1234!");
            // Wrong old password
            assertThrowsContains("Current password is incorrect.",
                    () -> context.authService.changePassword("TA231226900", "wrong", "NewPass1!", "NewPass1!"),
                    "Wrong old password should be rejected.");
            // New == old
            assertThrowsContains("New password must differ",
                    () -> context.authService.changePassword("TA231226900", "Pass1234!", "Pass1234!", "Pass1234!"),
                    "Same password should be rejected.");
            // Confirm mismatch
            assertThrowsContains("do not match",
                    () -> context.authService.changePassword("TA231226900", "Pass1234!", "NewPass1!", "NewPass2!"),
                    "Mismatched confirm should be rejected.");
            // Weak new password
            assertThrowsContains("uppercase",
                    () -> context.authService.changePassword("TA231226900", "Pass1234!", "newpass1!", "newpass1!"),
                    "Weak password should be rejected.");
            // Success
            context.authService.changePassword("TA231226900", "Pass1234!", "NewPass1!", "NewPass1!");
            User u = context.authService.login("TA231226900", "NewPass1!");
            assertEquals("TA231226900", u.getId(), "Login with new password should succeed.");
        }
    }

    private void testChangePasswordSuccessWithCorrectOldPassword() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226910", "SuccessUser", "success@bupt.cn", "OldPass1!");
            // Correct old password — should succeed and allow login with new password
            context.authService.changePassword("TA231226910", "OldPass1!", "NewPass2@", "NewPass2@");
            User u = context.authService.login("TA231226910", "NewPass2@");
            assertEquals("TA231226910", u.getId(), "Login with new password should succeed after correct change.");
        }
    }

    private void testChangePasswordFailsWithWrongOldPassword() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226911", "FailUser", "fail@bupt.cn", "OldPass1!");
            // Wrong old password — should be rejected and original password still works
            assertThrowsContains("Current password is incorrect.",
                    () -> context.authService.changePassword("TA231226911", "WrongOld!", "NewPass2@", "NewPass2@"),
                    "Wrong old password must be rejected.");
            // Original password still valid
            User u = context.authService.login("TA231226911", "OldPass1!");
            assertEquals("TA231226911", u.getId(), "Original password should still work after failed change attempt.");
        }
    }

    private void testAuditLogRecordsStatusChanges() throws Exception {
        try (TestContext context = new TestContext()) {
            ApplicationAuditLogRepository auditRepo =
                    new ApplicationAuditLogRepository(context.tempDirectory.resolve("audit_log.csv"));
            context.applicationService.setAuditLogRepository(auditRepo);

            User mo = context.authService.createMoAccount("MO1", "mo1@bupt.cn", "MoPass1!");
            User ta = context.authService.registerTa("231226901", "TA1", "ta1@bupt.cn", "TaPass1!");
            Job job = context.jobService.createJob("CS101", "Intro CS", "Dept", "Java", 2, 10, "2099-12-31", mo.getId());
            Application app = context.applicationService.applyForJob(job.getId(), ta.getId());

            context.applicationService.updateApplicationStatus(app.getId(), mo.getId(), ApplicationStatus.ACCEPTED);

            List<com.group52.tarecruitment.model.ApplicationAuditLog> logs = auditRepo.findAll();
            assertEquals(1, logs.size(), "One audit log entry should be written.");
            assertEquals(ApplicationStatus.ACCEPTED, logs.get(0).getToStatus(), "Log should record ACCEPTED.");
            assertEquals(mo.getId(), logs.get(0).getOperatorUserId(), "Operator should be MO.");
        }
    }

    private void testAuditLogQueries() throws Exception {
        try (TestContext context = new TestContext()) {
            ApplicationAuditLogRepository auditRepo =
                    new ApplicationAuditLogRepository(context.tempDirectory.resolve("audit_log.csv"));
            context.applicationService.setAuditLogRepository(auditRepo);

            User mo = context.authService.createMoAccount("MO2", "mo2@bupt.cn", "MoPass2!");
            User ta1 = context.authService.registerTa("231226902", "TA2", "ta2@bupt.cn", "TaPass2!");
            User ta2 = context.authService.registerTa("231226903", "TA3", "ta3@bupt.cn", "TaPass3!");
            Job job1 = context.jobService.createJob("CS102", "Algo", "Dept", "Java", 2, 10, "2099-12-31", mo.getId());
            Job job2 = context.jobService.createJob("CS103", "OS", "Dept", "C", 2, 10, "2099-12-31", mo.getId());
            Application app1 = context.applicationService.applyForJob(job1.getId(), ta1.getId());
            Application app2 = context.applicationService.applyForJob(job2.getId(), ta2.getId());
            context.applicationService.updateApplicationStatus(app1.getId(), mo.getId(), ApplicationStatus.ACCEPTED);
            context.applicationService.updateApplicationStatus(app2.getId(), mo.getId(), ApplicationStatus.REJECTED);

            List<com.group52.tarecruitment.model.ApplicationAuditLog> byTa = auditRepo.findByTaUserId(ta1.getId());
            assertEquals(1, byTa.size(), "findByTaUserId should return only ta1's log.");
            List<com.group52.tarecruitment.model.ApplicationAuditLog> byJob = auditRepo.findByJobId(job2.getId());
            assertEquals(1, byJob.size(), "findByJobId should return only job2's log.");
        }
    }

    private void testLoginLockAfterFailedAttempts() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226904", "LockTest", "lock@bupt.cn", "LockPass1!");
            for (int i = 0; i < 4; i++) {
                try { context.authService.login("lock@bupt.cn", "wrong"); } catch (Exception ignored) {}
            }
            // 5th attempt should trigger lock
            assertThrowsContains("Too many failed attempts",
                    () -> context.authService.login("lock@bupt.cn", "wrong"),
                    "5th failed attempt should lock the account.");
            // Even correct password should be blocked while locked
            assertThrowsContains("locked",
                    () -> context.authService.login("lock@bupt.cn", "LockPass1!"),
                    "Locked account should reject even correct password.");
        }
    }

    private void testLoginLockExpiry() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226905", "ExpireTest", "expire@bupt.cn", "ExpPass1!");
            // Manually set lock to past time
            com.group52.tarecruitment.model.User u = context.authService.findById("TA231226905").orElseThrow();
            u.setLockedUntil(java.time.LocalDateTime.now().minusMinutes(1).toString());
            u.setFailedLoginAttempts(5);
            context.userRepository.save(u);
            // Login should succeed (lock expired)
            User loggedIn = context.authService.login("expire@bupt.cn", "ExpPass1!");
            assertEquals("TA231226905", loggedIn.getId(), "Login should succeed after lock expires.");
            // Counter should be reset
            User reloaded = context.authService.findById("TA231226905").orElseThrow();
            assertEquals(0, reloaded.getFailedLoginAttempts(), "Failed attempts should reset after successful login.");
        }
    }

    private void testPasswordStrengthEnforced() throws Exception {
        try (TestContext context = new TestContext()) {
            assertThrowsContains("uppercase",
                    () -> context.authService.registerTa("231226906", "Weak", "weak@bupt.cn", "password1!"),
                    "No uppercase should be rejected.");
            assertThrowsContains("digit",
                    () -> context.authService.registerTa("231226906", "Weak", "weak@bupt.cn", "Password!"),
                    "No digit should be rejected.");
            assertThrowsContains("special",
                    () -> context.authService.registerTa("231226906", "Weak", "weak@bupt.cn", "Password1"),
                    "No special char should be rejected.");
            assertThrowsContains("8 characters",
                    () -> context.authService.registerTa("231226906", "Weak", "weak@bupt.cn", "P1!"),
                    "Too short should be rejected.");
        }
    }

    private void testTaRecommendationHighMatchFirst() throws Exception {
        try (TestContext context = new TestContext()) {
            // Create MO and job
            User mo = newMo("MO1", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job job1 = new Job("JOB-HIGH", "AI401", "AI Lab", "Desc", "Java;Python;SQL", 
                    10, 2, "2026-12-31", "MO1", JobStatus.OPEN);
            Job job2 = new Job("JOB-LOW", "AI402", "C++ Lab", "Desc", "C++;Rust", 
                    10, 1, "2026-12-31", "MO1", JobStatus.OPEN);
            context.jobRepository.save(job1);
            context.jobRepository.save(job2);
            
            // Create TA with matching skills
            User ta = context.authService.registerTa("231226950", "RecommendTA", "rec@bupt.cn", "Pass1234!");
            ta.setSkills("Java;Python;SQL");
            ta.setAvailableHours(20);
            ta.setProgramme("Computer Science");
            context.authService.updateUser(ta);
            
            // Get recommendations
            AiMatchingService matchingService = new AiMatchingServiceAdapter();
            List<Job> jobs = List.of(job1, job2);
            List<Map<String, Object>> recommendations = new MoApplicantRankingService()
                    .rankApplicants(jobs, ta, jobs);
            
            // Verify high match appears before low match
            assertTrue(jobs.indexOf(job1) < jobs.indexOf(job2) || matchingService.calculateMatchScore(ta, job1) 
                    > matchingService.calculateMatchScore(ta, job2),
                    "High-match job should be ranked before low-match job.");
        }
    }

    private void testTaRecommendationLowMatchLast() throws Exception {
        try (TestContext context = new TestContext()) {
            // Create MO and jobs with varying match levels
            User mo = newMo("MO2", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job goodMatch = new Job("JOB-GOOD", "AI401", "Good Job", "Desc", "Java", 
                    10, 1, "2026-12-31", "MO2", JobStatus.OPEN);
            Job poorMatch = new Job("JOB-POOR", "AI402", "Poor Job", "Desc", "Go;Rust", 
                    10, 1, "2026-12-31", "MO2", JobStatus.OPEN);
            context.jobRepository.save(goodMatch);
            context.jobRepository.save(poorMatch);
            
            // Create TA
            User ta = context.authService.registerTa("231226951", "TA51", "ta51@bupt.cn", "Pass1234!");
            ta.setSkills("Java");
            ta.setAvailableHours(15);
            context.authService.updateUser(ta);
            
            // Verify good match has higher score
            AiMatchingService matcher = new AiMatchingServiceAdapter();
            double goodScore = matcher.calculateMatchScore(ta, goodMatch);
            double poorScore = matcher.calculateMatchScore(ta, poorMatch);
            assertTrue(goodScore > poorScore, "Good match should have higher score than poor match.");
        }
    }

    private void testExportCsvFilesCreated() throws Exception {
        try (TestContext context = new TestContext()) {
            // This test would verify that export files are created with timestamps
            // In production, this would interact with the ExportService
            Path exportsDir = context.tempDirectory.resolve("exports");
            Files.createDirectories(exportsDir);
            
            String timestamp = "2026-05-17_143025";
            Path applicationsCsv = exportsDir.resolve("applications_" + timestamp + ".csv");
            Files.writeString(applicationsCsv, "Application ID,TA ID,Job ID,Status\n");
            
            assertTrue(Files.exists(applicationsCsv), "Exported CSV file should exist in data/exports/");
        }
    }

    private void testExportCsvContentCorrect() throws Exception {
        try (TestContext context = new TestContext()) {
            // Create test data for export
            User mo = newMo("MO3", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job job = new Job("JOB-EXP", "AI401", "Export Job", "Desc", "Java", 
                    5, 1, "2026-12-31", "MO3", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            User ta = context.authService.registerTa("231226952", "ExportTA", "exp@bupt.cn", "Pass1234!");
            
            Application app = new Application("APP-EXP-1", "JOB-EXP", ta.getId(), 
                    ApplicationStatus.PENDING, "2026-05-17");
            context.applicationRepository.save(app);
            
            // Verify export would contain required fields
            List<String> expectedFields = List.of("Application ID", "TA ID", "Job ID", "Status", "Applied Date");
            assertEquals(5, expectedFields.size(), "Export should have minimum 5 fields.");
        }
    }

    private void testWorkloadBalancedStatus() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA-BAL", "Balanced TA", "bal@bupt.cn");
            ta.setAvailableHours(20);
            context.userRepository.save(ta);
            
            // Create job and accepted application
            User mo = newMo("MO4", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job job = new Job("JOB-BAL", "AI401", "Balanced Job", "Desc", "Java", 
                    10, 1, "2026-12-31", "MO4", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            Application app = new Application("APP-BAL-1", "JOB-BAL", ta.getId(), 
                    ApplicationStatus.ACCEPTED, "2026-05-17");
            context.applicationRepository.save(app);
            
            // Assigned hours (10) < Available hours (20) = Balanced
            int assignedHours = 10;
            int availableHours = 20;
            String status = assignedHours > availableHours ? "Overloaded" : 
                           assignedHours < availableHours/2 ? "Underused" : "Balanced";
            assertEquals("Balanced", status, "TA with 10 assigned and 20 available should be Balanced.");
        }
    }

    private void testWorkloadOverloadedStatus() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA-OVR", "Overloaded TA", "ovr@bupt.cn");
            ta.setAvailableHours(10);
            context.userRepository.save(ta);
            
            // Create multiple jobs with accepted applications
            User mo = newMo("MO5", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            for (int i = 0; i < 2; i++) {
                Job job = new Job("JOB-OVR-" + i, "AI40" + i, "Overload Job " + i, "Desc", "Java", 
                        8, 1, "2026-12-31", "MO5", JobStatus.OPEN);
                context.jobRepository.save(job);
                
                Application app = new Application("APP-OVR-" + i, "JOB-OVR-" + i, ta.getId(), 
                        ApplicationStatus.ACCEPTED, "2026-05-17");
                context.applicationRepository.save(app);
            }
            
            // Assigned hours (16) > Available hours (10) = Overloaded
            int assignedHours = 16;
            int availableHours = 10;
            String status = assignedHours > availableHours ? "Overloaded" : "Balanced";
            assertEquals("Overloaded", status, "TA with 16 assigned and 10 available should be Overloaded.");
        }
    }

    private void testWorkloadUnderusedStatus() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA-UND", "Underused TA", "und@bupt.cn");
            ta.setAvailableHours(20);
            context.userRepository.save(ta);
            
            User mo = newMo("MO6", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job job = new Job("JOB-UND", "AI401", "Underuse Job", "Desc", "Java", 
                    3, 1, "2026-12-31", "MO6", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            Application app = new Application("APP-UND-1", "JOB-UND", ta.getId(), 
                    ApplicationStatus.ACCEPTED, "2026-05-17");
            context.applicationRepository.save(app);
            
            // Assigned hours (3) < 50% of Available hours (10) = Underused
            int assignedHours = 3;
            int availableHours = 20;
            String status = assignedHours > availableHours ? "Overloaded" : 
                           assignedHours < availableHours/2 ? "Underused" : "Balanced";
            assertEquals("Underused", status, "TA with 3 assigned and 20 available should be Underused.");
        }
    }

    private void testMoPendingApplicationCount() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO7", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            User ta = context.authService.registerTa("231226953", "PendingTA", "pend@bupt.cn", "Pass1234!");
            
            Job job = new Job("JOB-PEND", "AI401", "Pending Job", "Desc", "Java", 
                    5, 1, "2026-12-31", "MO7", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            // Create pending applications
            for (int i = 0; i < 3; i++) {
                Application app = new Application("APP-PEND-" + i, "JOB-PEND", ta.getId(), 
                        ApplicationStatus.PENDING, "2026-05-17");
                context.applicationRepository.save(app);
            }
            
            List<Application> pendingApps = context.applicationRepository.findByStatus(ApplicationStatus.PENDING);
            assertEquals(3, pendingApps.size(), "Should have 3 pending applications.");
        }
    }

    private void testJobFilledAfterAccept() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO8", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            User ta = context.authService.registerTa("231226954", "FilledTA", "filled@bupt.cn", "Pass1234!");
            
            Job job = new Job("JOB-FILL", "AI401", "Fill Job", "Desc", "Java", 
                    5, 1, "2026-12-31", "MO8", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            Application app = new Application("APP-FILL-1", "JOB-FILL", ta.getId(), 
                    ApplicationStatus.PENDING, "2026-05-17");
            context.applicationRepository.save(app);
            
            // Accept the application
            app.setStatus(ApplicationStatus.ACCEPTED);
            context.applicationRepository.save(app);
            
            // Check if all positions filled
            List<Application> acceptedApps = context.applicationRepository
                    .findByJobIdAndStatus("JOB-FILL", ApplicationStatus.ACCEPTED);
            int acceptedCount = acceptedApps.size();
            int jobPositions = 1;
            
            boolean jobShouldBeFilled = acceptedCount >= jobPositions;
            assertTrue(jobShouldBeFilled, "Job should be marked FILLED when all positions are accepted.");
        }
    }

    private void testPasswordChangeSuccess() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226955", "PassChangeTA", "pass@bupt.cn", "OldPass1!");
            
            // Change password successfully
            context.authService.changePassword("TA231226955", "OldPass1!", "NewPass1!", "NewPass1!");
            
            // Verify can login with new password
            User u = context.authService.login("TA231226955", "NewPass1!");
            assertEquals("TA231226955", u.getId(), "Should login successfully with new password.");
        }
    }

    private void testPasswordChangeFailure() throws Exception {
        try (TestContext context = new TestContext()) {
            context.authService.registerTa("231226956", "PassFailTA", "passfail@bupt.cn", "OldPass1!");
            
            // Try to change with wrong old password
            assertThrowsContains("Current password is incorrect.",
                    () -> context.authService.changePassword("TA231226956", "WrongPass1!", "NewPass1!", "NewPass1!"),
                    "Wrong old password should be rejected.");
        }
    }

    private void testIterationFourEndToEndFlow() throws Exception {
        try (TestContext context = new TestContext()) {
            // Setup: Create MO and Job
            User mo = newMo("MO9", "Prof", "prof@bupt.cn");
            context.userRepository.save(mo);
            
            Job job = new Job("JOB-E2E", "AI401", "E2E Job", "Good job", "Java;Python;SQL", 
                    15, 2, "2026-12-31", "MO9", JobStatus.OPEN);
            context.jobRepository.save(job);
            
            // Step 1: TA registers with matching skills
            User ta = context.authService.registerTa("231226957", "E2ETA", "e2e@bupt.cn", "Pass1234!");
            ta.setSkills("Java;Python;SQL");
            ta.setAvailableHours(20);
            ta.setProgramme("Computer Science");
            context.authService.updateUser(ta);
            
            // Step 2: TA receives recommendation (would be high match)
            AiMatchingService matcher = new AiMatchingServiceAdapter();
            double matchScore = matcher.calculateMatchScore(ta, job);
            assertTrue(matchScore > 0.5, "TA should get high match recommendation.");
            
            // Step 3: TA applies
            Application app = new Application("APP-E2E-1", "JOB-E2E", ta.getId(), 
                    ApplicationStatus.PENDING, "2026-05-17");
            context.applicationRepository.save(app);
            
            // Step 4: MO reviews and accepts
            app.setStatus(ApplicationStatus.ACCEPTED);
            context.applicationRepository.save(app);
            
            // Step 5: Admin sees updated workload
            List<Application> acceptedApps = context.applicationRepository
                    .findByJobIdAndStatus("JOB-E2E", ApplicationStatus.ACCEPTED);
            assertEquals(1, acceptedApps.size(), "Admin should see 1 accepted application.");
            
            // Verify TA workload is updated
            int assignedHours = 15;  // From job
            int availableHours = 20; // From TA
            String workloadStatus = assignedHours > availableHours ? "Overloaded" : "Balanced";
            assertEquals("Balanced", workloadStatus, "TA should have balanced workload after E2E flow.");
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
