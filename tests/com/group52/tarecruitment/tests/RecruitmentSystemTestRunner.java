package com.group52.tarecruitment.tests;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Notification;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.ApplicationRepository;
import com.group52.tarecruitment.repository.JobRepository;
import com.group52.tarecruitment.repository.NotificationRepository;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AiMatchingService;
import com.group52.tarecruitment.service.AiMatchingServiceAdapter;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.MoApplicantRankingService;
import com.group52.tarecruitment.service.NotificationService;
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
        runCase("MO pending application count uses only the MO's pending applications", this::testMoPendingApplicationCount);
        runCase("MO accept fills job when final position is accepted", this::testMoAcceptFillsJobWhenCapacityReached);
        runCase("Job deletion is blocked when applications exist", this::testDeleteJobGuard);
        runCase("TA notification filtering and unread count", this::testTaNotificationFilteringAndUnreadCount);
        runCase("TA notification status summary and closed-job message", this::testTaNotificationSummaryAndClosedMessage);
        runCase("End-to-end integration: TA profile data visible to MO and admin workload", this::testEndToEndIntegrationFlow);
        runCase("AI matching returns 100 for complete matches", this::testAiMatchingCompleteMatch);
        runCase("AI matching returns partial score with missing skills", this::testAiMatchingPartialMatch);
        runCase("AI matching handles empty and invalid input", this::testAiMatchingEmptyAndInvalidInput);
        runCase("MO ranking sorts by match score descending", this::testMoRankingSortsByMatchScoreDescending);
        runCase("MO ranking filters pending applications and minimum score", this::testMoRankingFiltersPendingAndMinimumScore);
        runCase("MO ranking needs-decision filter includes only reviewable applications", this::testMoRankingNeedsDecisionFilter);
        runCase("AdminService risk level respects TA availableHours, not hardcoded 20h", this::testAdminRiskLevelUsesAvailableHours);
        runCase("AdminService getRecruitmentSnapshot counts filled jobs and overloaded TAs", this::testRecruitmentSnapshot);
        runCase("AdminService searchTAWorkload filters by name and ID", this::testSearchTAWorkload);
        runCase("AdminService getWorkloadTrend returns correct label by job count", this::testWorkloadTrend);
        runCase("Job lifecycle: closing a job blocks new applications", this::testJobLifecycleCloseBlocksApplication);
        runCase("Job lifecycle: reopen restores apply, respects deadline and capacity", this::testJobLifecycleReopenAllowsApplication);
        runCase("Job lifecycle: expired deadline auto-closes the job and rejects applications", this::testJobLifecycleAutoCloseOnDeadline);
        runCase("Notification persistence survives repository restart", this::testNotificationPersistenceAfterRestart);
        runCase("Application and job lifecycle events create persistent notifications", this::testApplicationAndJobEventsCreateNotifications);
        runCase("Admin overload alert notification is persisted and de-duplicated", this::testAdminOverloadAlertNotification);

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

    private void testMoPendingApplicationCount() throws Exception {
        try (TestContext context = new TestContext()) {
            // Setup
            User mo = newMo("MO-PENDING-1", "MO Pending", "mo.pending@bupt.cn");
            User otherMo = newMo("MO-PENDING-2", "Other MO Pending", "other.pending@bupt.cn");
            User ta1 = newTa("TA-PENDING-1", "Iris Pending", "iris.pending@bupt.cn");
            User ta2 = newTa("TA-PENDING-2", "Jack Pending", "jack.pending@bupt.cn");
            User ta3 = newTa("TA-PENDING-3", "Kara Pending", "kara.pending@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(otherMo);
            context.userRepository.save(ta1);
            context.userRepository.save(ta2);
            context.userRepository.save(ta3);

            Job moJobOne = context.jobService.createJob(
                    "CS-MO-101",
                    "MO Pending One",
                    "desc",
                    "Java",
                    "6",
                    "2",
                    LocalDate.now().plusDays(7).toString(),
                    mo.getId());
            Job moJobTwo = context.jobService.createJob(
                    "CS-MO-102",
                    "MO Pending Two",
                    "desc",
                    "Python",
                    "4",
                    "1",
                    LocalDate.now().plusDays(7).toString(),
                    mo.getId());
            Job otherMoJob = context.jobService.createJob(
                    "CS-MO-103",
                    "Other MO Job",
                    "desc",
                    "Java",
                    "4",
                    "1",
                    LocalDate.now().plusDays(7).toString(),
                    otherMo.getId());

            context.applicationRepository.save(new Application(
                    "APP-MO-PENDING-1",
                    moJobOne.getId(),
                    ta1.getId(),
                    ApplicationStatus.PENDING,
                    LocalDate.now().toString()));
            context.applicationRepository.save(new Application(
                    "APP-MO-PENDING-2",
                    moJobTwo.getId(),
                    ta2.getId(),
                    ApplicationStatus.PENDING,
                    LocalDate.now().toString()));
            context.applicationRepository.save(new Application(
                    "APP-MO-REJECTED-1",
                    moJobOne.getId(),
                    ta3.getId(),
                    ApplicationStatus.REJECTED,
                    LocalDate.now().toString()));
            context.applicationRepository.save(new Application(
                    "APP-OTHER-MO-PENDING-1",
                    otherMoJob.getId(),
                    ta3.getId(),
                    ApplicationStatus.PENDING,
                    LocalDate.now().toString()));

            // Action
            int pendingCount = context.applicationService.getPendingApplicationCountForMo(mo.getId());

            // Expected result
            assertEquals(2, pendingCount, "MO pending count should include only pending applications for that MO's jobs.");

            context.applicationService.updateApplicationStatus(
                    "APP-MO-PENDING-2",
                    mo.getId(),
                    ApplicationStatus.REJECTED);
            assertEquals(
                    1,
                    context.applicationService.getPendingApplicationCountForMo(mo.getId()),
                    "Rejecting one application should immediately reduce the MO pending count.");
        }
    }

    private void testMoAcceptFillsJobWhenCapacityReached() throws Exception {
        try (TestContext context = new TestContext()) {
            // Setup
            User mo = newMo("MO-FILL-1", "MO Fill", "mo.fill@bupt.cn");
            User ta = newTa("TA-FILL-1", "Lena Fill", "lena.fill@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job job = context.jobService.createJob(
                    "CS-FILL-101",
                    "Capacity Lab",
                    "desc",
                    "Java",
                    "6",
                    "1",
                    LocalDate.now().plusDays(7).toString(),
                    mo.getId());
            Application application = context.applicationService.applyForJob(job.getId(), ta.getId());

            // Action
            context.applicationService.updateApplicationStatus(
                    application.getId(),
                    mo.getId(),
                    ApplicationStatus.ACCEPTED);

            // Expected result
            Job reloadedJob = context.jobRepository.findById(job.getId())
                    .orElseThrow(() -> new AssertionError("Job should still exist after accepting an applicant."));
            assertEquals(JobStatus.FILLED, reloadedJob.getStatus(), "Job should become FILLED when capacity is reached.");
            assertEquals(
                    0,
                    context.applicationService.getPendingApplicationCountForMo(mo.getId()),
                    "Pending count should refresh to zero after the only pending application is accepted.");
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

    private void testMoRankingNeedsDecisionFilter() throws Exception {
        try (TestContext context = new TestContext()) {
            Job targetJob = new Job(
                    "JOB-RANK-3",
                    "ECS7004",
                    "Review Queue",
                    "Support labs",
                    "Java",
                    4,
                    3,
                    LocalDate.now().plusDays(30).toString(),
                    "MO-RANK",
                    JobStatus.OPEN);
            context.jobRepository.save(targetJob);

            User applied = newTa("TA-NEEDS-APPLIED", "Applied Needs", "applied.needs@bupt.cn");
            User reviewing = newTa("TA-NEEDS-REVIEWING", "Reviewing Needs", "reviewing.needs@bupt.cn");
            User pending = newTa("TA-NEEDS-PENDING", "Pending Needs", "pending.needs@bupt.cn");
            User accepted = newTa("TA-NEEDS-ACCEPTED", "Accepted Done", "accepted.needs@bupt.cn");
            User rejected = newTa("TA-NEEDS-REJECTED", "Rejected Done", "rejected.needs@bupt.cn");
            context.userRepository.save(applied);
            context.userRepository.save(reviewing);
            context.userRepository.save(pending);
            context.userRepository.save(accepted);
            context.userRepository.save(rejected);

            context.applicationRepository.save(new Application(
                    "APP-NEEDS-APPLIED", targetJob.getId(), applied.getId(), ApplicationStatus.APPLIED, "2026-05-01"));
            context.applicationRepository.save(new Application(
                    "APP-NEEDS-REVIEWING", targetJob.getId(), reviewing.getId(), ApplicationStatus.REVIEWING, "2026-05-01"));
            context.applicationRepository.save(new Application(
                    "APP-NEEDS-PENDING", targetJob.getId(), pending.getId(), ApplicationStatus.PENDING, "2026-05-01"));
            context.applicationRepository.save(new Application(
                    "APP-NEEDS-ACCEPTED", targetJob.getId(), accepted.getId(), ApplicationStatus.ACCEPTED, "2026-05-01"));
            context.applicationRepository.save(new Application(
                    "APP-NEEDS-REJECTED", targetJob.getId(), rejected.getId(), ApplicationStatus.REJECTED, "2026-05-01"));

            MoApplicantRankingService rankingService = new MoApplicantRankingService(
                    context.applicationService, new AiMatchingServiceAdapter(new AiMatchingService()));
            List<MoApplicantRankingService.RankedApplicant> ranked = rankingService.rankApplicants(
                    targetJob,
                    context.applicationRepository.findByJobId(targetJob.getId()),
                    Map.of(
                            applied.getId(), applied,
                            reviewing.getId(), reviewing,
                            pending.getId(), pending,
                            accepted.getId(), accepted,
                            rejected.getId(), rejected),
                    new MoApplicantRankingService.RankingOptions(
                            false,
                            true,
                            0,
                            MoApplicantRankingService.SortMode.MATCH_SCORE_DESC));

            Set<ApplicationStatus> statuses = new HashSet<>();
            for (MoApplicantRankingService.RankedApplicant applicant : ranked) {
                statuses.add(applicant.getStatus());
            }
            assertEquals(3, ranked.size(), "Needs-decision filter should keep APPLIED, REVIEWING, and PENDING only.");
            assertTrue(statuses.contains(ApplicationStatus.APPLIED), "APPLIED applications should need a decision.");
            assertTrue(statuses.contains(ApplicationStatus.REVIEWING), "REVIEWING applications should need a decision.");
            assertTrue(statuses.contains(ApplicationStatus.PENDING), "PENDING applications should need a decision.");
            assertFalse(statuses.contains(ApplicationStatus.ACCEPTED), "ACCEPTED applications should not need a decision.");
            assertFalse(statuses.contains(ApplicationStatus.REJECTED), "REJECTED applications should not need a decision.");
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

    private void testJobLifecycleCloseBlocksApplication() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO-LC-1", "MO Lifecycle One", "mo.lc1@bupt.cn");
            User ta = newTa("TA-LC-1", "TA Lifecycle One", "ta.lc1@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job job = context.jobService.createJob(
                    "CS-LC-101",
                    "Lifecycle Lab",
                    "desc",
                    "Java",
                    "6",
                    "1",
                    LocalDate.now().plusDays(7).toString(),
                    mo.getId());
            assertEquals(JobStatus.OPEN, job.getStatus(), "Newly created job should be OPEN.");

            Job closed = context.jobService.closeJob(job.getId(), mo.getId());
            assertEquals(JobStatus.CLOSED, closed.getStatus(), "closeJob should move OPEN job to CLOSED.");
            assertEquals(
                    JobStatus.CLOSED,
                    context.jobRepository.findById(job.getId()).orElseThrow().getStatus(),
                    "CLOSED status must be persisted.");

            assertThrowsContains(
                    "closed and no longer accepts applications",
                    () -> context.applicationService.applyForJob(job.getId(), ta.getId()),
                    "TA must not be able to apply after the job is closed.");

            assertThrowsContains(
                    "already closed",
                    () -> context.jobService.closeJob(job.getId(), mo.getId()),
                    "Closing an already-closed job must be rejected.");

            User otherMo = newMo("MO-LC-OTHER", "Other MO", "mo.other@bupt.cn");
            context.userRepository.save(otherMo);
            assertThrowsContains(
                    "You can only edit jobs that you posted",
                    () -> context.jobService.closeJob(job.getId(), otherMo.getId()),
                    "Other MOs must not be able to close a job they did not post.");
        }
    }

    private void testJobLifecycleReopenAllowsApplication() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO-LC-2", "MO Lifecycle Two", "mo.lc2@bupt.cn");
            User ta = newTa("TA-LC-2", "TA Lifecycle Two", "ta.lc2@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job job = context.jobService.createJob(
                    "CS-LC-201",
                    "Reopen Lab",
                    "desc",
                    "Java",
                    "6",
                    "1",
                    LocalDate.now().plusDays(10).toString(),
                    mo.getId());
            context.jobService.closeJob(job.getId(), mo.getId());

            Job reopened = context.jobService.reopenJob(job.getId(), mo.getId());
            assertEquals(JobStatus.OPEN, reopened.getStatus(), "reopenJob should restore status to OPEN.");

            Application application = context.applicationService.applyForJob(job.getId(), ta.getId());
            assertEquals(
                    ApplicationStatus.PENDING,
                    application.getStatus(),
                    "TA should be able to apply once the job is reopened.");

            assertThrowsContains(
                    "already open",
                    () -> context.jobService.reopenJob(job.getId(), mo.getId()),
                    "Reopening an already-open job must be rejected.");

            // Reopening must respect deadline; rewrite the row with an expired deadline and try again.
            Job expired = context.jobRepository.findById(job.getId()).orElseThrow();
            expired.setStatus(JobStatus.CLOSED);
            expired.setDeadline(LocalDate.now().minusDays(1).toString());
            context.jobRepository.save(expired);
            assertThrowsContains(
                    "deadline has passed",
                    () -> context.jobService.reopenJob(job.getId(), mo.getId()),
                    "Reopening past-deadline jobs must be rejected.");

            // Reopening a job whose offers fill all positions should be rejected too.
            Job filledJob = context.jobService.createJob(
                    "CS-LC-202",
                    "Filled Lab",
                    "desc",
                    "Java",
                    "6",
                    "1",
                    LocalDate.now().plusDays(12).toString(),
                    mo.getId());
            context.applicationRepository.save(new Application(
                    "APP-LC-FILL",
                    filledJob.getId(),
                    ta.getId(),
                    ApplicationStatus.ACCEPTED,
                    LocalDate.now().toString()));
            Job persistedFilled = context.jobRepository.findById(filledJob.getId()).orElseThrow();
            persistedFilled.setStatus(JobStatus.CLOSED);
            context.jobRepository.save(persistedFilled);
            assertThrowsContains(
                    "filled",
                    () -> context.jobService.reopenJob(filledJob.getId(), mo.getId()),
                    "Reopening a fully-filled job must be rejected.");
        }
    }

    private void testJobLifecycleAutoCloseOnDeadline() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO-LC-3", "MO Lifecycle Three", "mo.lc3@bupt.cn");
            User ta = newTa("TA-LC-3", "TA Lifecycle Three", "ta.lc3@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            // Jobs created via the service must have a future deadline, so we plant the expired row directly.
            Job expired = new Job(
                    "JOB-LC-EXPIRED",
                    "CS-LC-301",
                    "Expired Lab",
                    "desc",
                    "Java",
                    6,
                    1,
                    LocalDate.now().minusDays(2).toString(),
                    mo.getId(),
                    JobStatus.OPEN);
            Job futureOpen = new Job(
                    "JOB-LC-FUTURE",
                    "CS-LC-302",
                    "Future Lab",
                    "desc",
                    "Java",
                    6,
                    1,
                    LocalDate.now().plusDays(5).toString(),
                    mo.getId(),
                    JobStatus.OPEN);
            context.jobRepository.save(expired);
            context.jobRepository.save(futureOpen);

            List<Job> swept = context.jobService.autoCloseExpiredJobs();
            assertEquals(1, swept.size(), "Only the expired job should be swept.");
            assertEquals(
                    JobStatus.CLOSED,
                    context.jobRepository.findById(expired.getId()).orElseThrow().getStatus(),
                    "Expired job must be persisted as CLOSED.");
            assertEquals(
                    JobStatus.OPEN,
                    context.jobRepository.findById(futureOpen.getId()).orElseThrow().getStatus(),
                    "Future-deadline jobs must remain OPEN.");

            // Sweeping again should be a no-op.
            assertEquals(0, context.jobService.autoCloseExpiredJobs().size(), "Second sweep should find nothing.");

            // End-to-end: an expired OPEN job must self-heal to CLOSED when a TA tries to apply.
            Job stale = new Job(
                    "JOB-LC-STALE",
                    "CS-LC-303",
                    "Stale Lab",
                    "desc",
                    "Java",
                    6,
                    1,
                    LocalDate.now().minusDays(1).toString(),
                    mo.getId(),
                    JobStatus.OPEN);
            context.jobRepository.save(stale);
            assertThrowsContains(
                    "passed its deadline",
                    () -> context.applicationService.applyForJob(stale.getId(), ta.getId()),
                    "Applying to an expired job should be rejected.");
            assertEquals(
                    JobStatus.CLOSED,
                    context.jobRepository.findById(stale.getId()).orElseThrow().getStatus(),
                    "Apply-time validation should self-heal stale OPEN jobs to CLOSED.");
        }
    }

    private void testNotificationPersistenceAfterRestart() throws Exception {
        try (TestContext context = new TestContext()) {
            context.notificationService.publish(
                    Role.TA,
                    NotificationType.APPLY,
                    "TA-NTF-01",
                    "Application submitted.",
                    "APP-NTF-01");
            context.notificationService.publish(
                    Role.TA,
                    NotificationType.ACCEPT,
                    "TA-NTF-01",
                    "Application accepted.",
                    "APP-NTF-01");

            NotificationRepository restartedRepository = new NotificationRepository(context.notificationsFilePath);
            NotificationService restartedService = new NotificationService(restartedRepository);
            List<Notification> restored = restartedService.getNotificationsForUser("TA-NTF-01");
            assertEquals(2, restored.size(), "Notifications should still exist after restarting repository.");

            boolean hasApply = restored.stream().anyMatch(n -> n.getType() == NotificationType.APPLY);
            boolean hasAccept = restored.stream().anyMatch(n -> n.getType() == NotificationType.ACCEPT);
            assertTrue(hasApply, "Restored notifications should include APPLY.");
            assertTrue(hasAccept, "Restored notifications should include ACCEPT.");
        }
    }

    private void testApplicationAndJobEventsCreateNotifications() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO-NTF-01", "MO NTF", "mo.ntf@bupt.cn");
            User ta = newTa("TA-NTF-02", "TA NTF", "ta.ntf@bupt.cn");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job job = context.jobService.createJob(
                    "CS-NTF-101",
                    "Notification Integration Lab",
                    "desc",
                    "Java",
                    "6",
                    "2",
                    LocalDate.now().plusDays(8).toString(),
                    mo.getId());

            Application application = context.applicationService.applyForJob(job.getId(), ta.getId());
            context.applicationService.updateApplicationStatus(application.getId(), mo.getId(), ApplicationStatus.ACCEPTED);
            context.jobService.closeJob(job.getId(), mo.getId());
            context.jobService.reopenJob(job.getId(), mo.getId());

            List<Notification> notifications = context.notificationService.getNotificationsForUser(ta.getId());
            Set<NotificationType> types = new HashSet<>();
            for (Notification notification : notifications) {
                types.add(notification.getType());
            }

            assertTrue(types.contains(NotificationType.APPLY), "TA should receive APPLY notification.");
            assertTrue(types.contains(NotificationType.ACCEPT), "TA should receive ACCEPT notification.");
            assertTrue(types.contains(NotificationType.JOB_CLOSE), "TA should receive JOB_CLOSE notification.");
            assertTrue(types.contains(NotificationType.JOB_REOPEN), "TA should receive JOB_REOPEN notification.");
        }
    }

    private void testAdminOverloadAlertNotification() throws Exception {
        try (TestContext context = new TestContext()) {
            User mo = newMo("MO-NTF-02", "MO Alert", "mo.alert@bupt.cn");
            User ta = new User(
                    "TA-NTF-03",
                    Role.TA,
                    "TA Alert",
                    "ta.alert@bupt.cn",
                    "password1",
                    "Computer Science",
                    2,
                    "Java",
                    4,
                    true,
                    "");
            context.userRepository.save(mo);
            context.userRepository.save(ta);

            Job heavyJob = context.jobService.createJob(
                    "CS-NTF-201",
                    "Heavy Lab",
                    "desc",
                    "Java",
                    "6",
                    "1",
                    LocalDate.now().plusDays(10).toString(),
                    mo.getId());
            Application application = context.applicationService.applyForJob(heavyJob.getId(), ta.getId());
            context.applicationService.updateApplicationStatus(application.getId(), mo.getId(), ApplicationStatus.ACCEPTED);

            int createdOnce = context.adminService.publishOverloadAlerts();
            int createdTwice = context.adminService.publishOverloadAlerts();
            assertEquals(1, createdOnce, "First overload publish should create exactly one alert.");
            assertEquals(0, createdTwice, "Second overload publish should be de-duplicated.");

            NotificationRepository restartedRepository = new NotificationRepository(context.notificationsFilePath);
            NotificationService restartedService = new NotificationService(restartedRepository);
            List<Notification> persisted = restartedService.getNotificationsForUser(ta.getId());
            long overloadCount = persisted.stream()
                    .filter(notification -> notification.getType() == NotificationType.OVERLOAD_ALERT)
                    .count();
            assertEquals(1L, overloadCount, "Overload alert should persist and remain unique after restart.");
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
        private final Path notificationsFilePath;
        private final UserRepository userRepository;
        private final JobRepository jobRepository;
        private final ApplicationRepository applicationRepository;
        private final NotificationRepository notificationRepository;
        private final AuthService authService;
        private final NotificationService notificationService;
        private final JobService jobService;
        private final ApplicationService applicationService;
        private final AdminService adminService;

        private TestContext() throws Exception {
            this.tempDirectory = Files.createTempDirectory("ta-recruitment-tests-");
            this.usersFilePath = tempDirectory.resolve("users.csv");
            this.jobsFilePath = tempDirectory.resolve("jobs.csv");
            this.applicationsFilePath = tempDirectory.resolve("applications.csv");
            this.notificationsFilePath = tempDirectory.resolve("notifications.csv");

            this.userRepository = new UserRepository(usersFilePath);
            this.jobRepository = new JobRepository(jobsFilePath);
            this.applicationRepository = new ApplicationRepository(applicationsFilePath);
            this.notificationRepository = new NotificationRepository(notificationsFilePath);
            this.authService = new AuthService(userRepository);
            this.notificationService = new NotificationService(notificationRepository);
            this.jobService = new JobService(jobRepository, applicationRepository, notificationService);
            this.applicationService = new ApplicationService(applicationRepository, jobRepository, null, notificationService);
            this.adminService = new AdminService(userRepository, jobRepository, applicationRepository, notificationService);
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
