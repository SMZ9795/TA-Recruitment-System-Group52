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
import com.group52.tarecruitment.service.AiMatchingService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.util.CvValidationUtil;
import com.group52.tarecruitment.util.FileUtil;
import com.group52.tarecruitment.util.JobFilterUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
        runCase("End-to-end integration: TA profile data visible to MO and admin workload", this::testEndToEndIntegrationFlow);
        runCase("AI matching returns 100 for complete matches", this::testAiMatchingCompleteMatch);
        runCase("AI matching returns partial score with missing skills", this::testAiMatchingPartialMatch);
        runCase("AI matching handles empty and invalid input", this::testAiMatchingEmptyAndInvalidInput);
        runCase("AdminService risk level respects TA availableHours, not hardcoded 20h", this::testAdminRiskLevelUsesAvailableHours);
        runCase("AdminService getRecruitmentSnapshot counts filled jobs and overloaded TAs", this::testRecruitmentSnapshot);
        runCase("AdminService searchTAWorkload filters by name and ID", this::testSearchTAWorkload);
        runCase("AdminService getWorkloadTrend returns correct label by job count", this::testWorkloadTrend);

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
