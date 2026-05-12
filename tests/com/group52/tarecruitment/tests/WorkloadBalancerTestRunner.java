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
import com.group52.tarecruitment.service.WorkloadBalancerService;
import com.group52.tarecruitment.service.WorkloadBalancerService.WorkloadStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public final class WorkloadBalancerTestRunner {
    public static void main(String[] args) throws Exception {
        WorkloadBalancerTestRunner runner = new WorkloadBalancerTestRunner();
        runner.run();
    }

    private void run() throws Exception {
        testBalancedWorkload();
        testOverloadedWorkload();
        testUnderusedWorkload();
        System.out.println("All workload balancer tests passed.");
    }

    private void testBalancedWorkload() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA-BAL-1", "Alice", "alice@bupt.cn");
            User mo = newMo("MO-BAL-1", "MO", "mo@bupt.cn");
            context.userRepository.save(ta);
            context.userRepository.save(mo);

            Job job1 = context.createJob(mo.getId(), "JOB-BAL-1", 4);
            Job job2 = context.createJob(mo.getId(), "JOB-BAL-2", 4);
            context.accept(job1.getId(), ta.getId());
            context.accept(job2.getId(), ta.getId());

            WorkloadBalancerService service = context.service();
            WorkloadBalancerService.WorkloadAnalysis analysis = service.analyzeWorkload().stream()
                    .filter(item -> item.getTaUserId().equals(ta.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(8, analysis.getWeeklyWorkloadHours(), "Balanced TA should have 8h/week.");
            assertEquals(WorkloadStatus.BALANCED, analysis.getStatus(), "TA should be classified as balanced.");
            assertEquals(
                    List.of("All TA workloads are balanced."),
                    service.generateSuggestions(),
                    "Balanced workloads should produce a no-action suggestion.");
        }
    }

    private void testOverloadedWorkload() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta1 = newTa("TA-OVR-1", "Alice", "alice@bupt.cn");
            User ta2 = newTa("TA-OVR-2", "Bob", "bob@bupt.cn");
            User mo = newMo("MO-OVR-1", "MO", "mo@bupt.cn");
            context.userRepository.save(ta1);
            context.userRepository.save(ta2);
            context.userRepository.save(mo);

            Job heavy1 = context.createJob(mo.getId(), "JOB-OVR-1", 10);
            Job heavy2 = context.createJob(mo.getId(), "JOB-OVR-2", 6);
            Job light = context.createJob(mo.getId(), "JOB-OVR-3", 2);
            context.accept(heavy1.getId(), ta1.getId());
            context.accept(heavy2.getId(), ta1.getId());
            context.accept(light.getId(), ta2.getId());

            WorkloadBalancerService service = context.service();
            WorkloadBalancerService.WorkloadAnalysis overloaded = service.analyzeWorkload().stream()
                    .filter(item -> item.getTaUserId().equals(ta1.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(16, overloaded.getWeeklyWorkloadHours(), "Overloaded TA should have 16h/week.");
            assertEquals(WorkloadStatus.HIGH_RISK, overloaded.getStatus(), "TA should be classified as high risk.");
            List<String> suggestions = service.generateSuggestions();
            assertTrue(
                    suggestions.stream().anyMatch(line -> line.contains("Move") && line.contains("Alice") && line.contains("Bob")),
                    "Suggestion should explain the deterministic transfer.");
            assertTrue(
                    suggestions.stream().anyMatch(line -> line.contains("exceeds their weekly capacity") || line.contains("overloaded by")),
                    "Suggestion should explain the overloaded TA.");
        }
    }

    private void testUnderusedWorkload() throws Exception {
        try (TestContext context = new TestContext()) {
            User ta = newTa("TA-UND-1", "Carol", "carol@bupt.cn");
            User mo = newMo("MO-UND-1", "MO", "mo@bupt.cn");
            context.userRepository.save(ta);
            context.userRepository.save(mo);

            Job small = context.createJob(mo.getId(), "JOB-UND-1", 2);
            context.accept(small.getId(), ta.getId());

            WorkloadBalancerService service = context.service();
            WorkloadBalancerService.WorkloadAnalysis analysis = service.analyzeWorkload().stream()
                    .filter(item -> item.getTaUserId().equals(ta.getId()))
                    .findFirst()
                    .orElseThrow();

            assertEquals(2, analysis.getWeeklyWorkloadHours(), "Underused TA should have 2h/week.");
            assertEquals(WorkloadStatus.UNDERUSED, analysis.getStatus(), "TA should be classified as underused.");
            assertTrue(
                    service.generateSuggestions().stream().anyMatch(line ->
                            line.contains("only has 2h/week")
                                    || line.contains("All TA workloads are balanced.")),
                    "Suggestion should mention the underused TA hours or confirm balanced rules.");
        }
    }

    private static User newTa(String id, String name, String email) {
        return new User(id, Role.TA, name, email, "password1", "Computer Science", 2, "Java", 10, true, "");
    }

    private static User newMo(String id, String name, String email) {
        return new User(id, Role.MO, name, email, "password1", "", 0, "", 0, true, "");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class TestContext implements AutoCloseable {
        private final Path tempDirectory;
        private final Path usersFilePath;
        private final Path jobsFilePath;
        private final Path applicationsFilePath;
        private final UserRepository userRepository;
        private final JobRepository jobRepository;
        private final ApplicationRepository applicationRepository;

        private TestContext() throws Exception {
            this.tempDirectory = Files.createTempDirectory("workload-balancer-tests-");
            this.usersFilePath = tempDirectory.resolve("users.csv");
            this.jobsFilePath = tempDirectory.resolve("jobs.csv");
            this.applicationsFilePath = tempDirectory.resolve("applications.csv");
            this.userRepository = new UserRepository(usersFilePath);
            this.jobRepository = new JobRepository(jobsFilePath);
            this.applicationRepository = new ApplicationRepository(applicationsFilePath);
        }

        private WorkloadBalancerService service() {
            return new WorkloadBalancerService(userRepository, jobRepository, applicationRepository);
        }

        private Job createJob(String moId, String jobId, int hoursPerWeek) {
            Job job = new Job(jobId, "CS101", jobId, "desc", "Java", hoursPerWeek, 1,
                    LocalDate.now().plusDays(7).toString(), moId, JobStatus.OPEN);
            jobRepository.save(job);
            return job;
        }

        private void accept(String jobId, String taId) {
            applicationRepository.save(new Application(
                    jobId + "-APP-" + taId,
                    jobId,
                    taId,
                    ApplicationStatus.ACCEPTED,
                    LocalDate.now().toString()));
        }

        @Override
        public void close() throws Exception {
            try (var walk = Files.walk(tempDirectory)) {
                walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }
}
