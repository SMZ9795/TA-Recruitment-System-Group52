package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.ExportService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Drives an end-to-end demo (TA → MO → Admin → CSV export) on top of the
 * real Swing application, without simulating mouse clicks. Each step
 * either calls a service method that mutates state and refreshes the
 * panel, or just lets the user read the bilingual caption.
 *
 * The controller backs the contents of {@code data/} up before it runs
 * and restores them when it finishes (or fails), so the demo can be
 * replayed any number of times without polluting the seed data.
 */
final class AutoDemoController {

    /**
     * One demo step. {@code action} can be null (caption-only pause).
     */
    private static final class Step {
        final String captionEn;
        final String captionCn;
        final int durationMs;
        final Runnable action;

        Step(String en, String cn, int durationMs, Runnable action) {
            this.captionEn = en;
            this.captionCn = cn;
            this.durationMs = durationMs;
            this.action = action;
        }
    }

    /** Files that the demo may overwrite. Backed up before run. */
    private static final String[] BACKUP_FILE_NAMES = {
            "users.csv",
            "jobs.csv",
            "applications.csv",
            "audit_log.csv",
            "notifications.csv",
            "workloads.json"
    };

    private static final String BACKUP_SUFFIX = ".demo-backup";

    private final SwingApp app;
    private final AuthService authService;
    private final ApplicationService applicationService;
    private final AdminService adminService;
    private final ExportService exportService;
    private final Path dataDirectory;

    private final List<Step> steps = new ArrayList<>();
    private final List<Path> backedUpFiles = new ArrayList<>();

    private int currentStep;
    private boolean running;
    private Timer scheduler;

    AutoDemoController(SwingApp app,
                       AuthService authService,
                       ApplicationService applicationService,
                       AdminService adminService,
                       ExportService exportService,
                       Path dataDirectory) {
        this.app = app;
        this.authService = authService;
        this.applicationService = applicationService;
        this.adminService = adminService;
        this.exportService = exportService;
        this.dataDirectory = dataDirectory;
    }

    /**
     * Whether the wiring is complete enough to actually run.
     * If anything required is missing we just disable the button.
     */
    boolean isAvailable() {
        return authService != null
                && applicationService != null
                && adminService != null
                && exportService != null
                && dataDirectory != null;
    }

    void start() {
        if (running || !isAvailable()) {
            return;
        }
        running = true;
        try {
            backupData();
        } catch (Exception ex) {
            running = false;
            JOptionPane.showMessageDialog(app.getMainFrame(),
                    "Failed to back up data before demo:\n" + ex.getMessage(),
                    "Auto Demo", JOptionPane.ERROR_MESSAGE);
            return;
        }
        buildScript();
        currentStep = 0;
        scheduleNext();
    }

    // ------------------------------------------------------------------
    // Script
    // ------------------------------------------------------------------

    private void buildScript() {
        steps.clear();

        // Pre-resolved fixtures from seeded data.
        // - Carol Wu  (TA231226945) has a PENDING application on CS101 (JOB100001).
        // - Dr Smith  (MO001) posted CS101.
        // - System Admin can export workload summary.
        final String taEmail = "carol.wu@bupt.local";
        final String taPassword = "ta345678";
        final String moEmail = "drsmith@bupt.local";
        final String moPassword = "mo123456";
        final String adminEmail = "admin@bupt.local";
        final String adminPassword = "admin123";
        final String demoJobId = "JOB100001";
        final String demoTaId = "TA231226945";

        steps.add(new Step(
                "Welcome — we'll walk through TA → MO → Admin in one pass.",
                "欢迎 — 我们将一次完整走过 TA → MO → Admin 三个角色流程。",
                7000,
                null));

        steps.add(new Step(
                "Step 1 / 3 — signing in as TA Carol Wu.",
                "步骤 1 / 3 — 以 TA Carol Wu 身份登录。",
                3500,
                () -> safeEnterAs(taEmail, taPassword)));

        steps.add(new Step(
                "Carol's dashboard shows her jobs, applications and AI match scores.",
                "Carol 的工作台展示岗位、申请记录和 AI 匹配分数。",
                12000,
                null));

        steps.add(new Step(
                "Step 2 / 3 — switching to MO Dr Smith to review applications.",
                "步骤 2 / 3 — 切换到 MO Dr Smith 进行评审。",
                3500,
                () -> safeEnterAs(moEmail, moPassword)));

        steps.add(new Step(
                "MO sees applicants ranked by AI match score AND current TA workload.",
                "MO 看到的排名同时考虑 AI 匹配分数 与 TA 当前工作量。",
                10000,
                null));

        steps.add(new Step(
                "Accepting Carol's pending application for CS101.",
                "通过 Carol 在 CS101 上的待审申请。",
                3500,
                () -> safeAcceptPendingApplication(demoTaId, demoJobId, moEmail, moPassword)));

        steps.add(new Step(
                "Audit log records the change; the job auto-marks FILLED when all positions are taken.",
                "审计日志自动记录变更；岗位填满后自动标记为 FILLED。",
                10000,
                () -> safeRefreshCurrentPanel()));

        steps.add(new Step(
                "Step 3 / 3 — switching to Admin for the system-wide view.",
                "步骤 3 / 3 — 切换到 Admin 查看系统总览。",
                3500,
                () -> safeEnterAs(adminEmail, adminPassword)));

        steps.add(new Step(
                "Admin dashboard: filled jobs, TA workload (Balanced / Overloaded / Underused), alerts.",
                "Admin 工作台：已填补岗位、TA 工作量分类（Balanced / Overloaded / Underused）、告警。",
                12000,
                null));

        steps.add(new Step(
                "Exporting the TA workload summary CSV to data/exports/ ...",
                "正在导出 TA 工作量汇总 CSV 到 data/exports/ ...",
                3500,
                this::safeExportWorkload));

        steps.add(new Step(
                "Report generated. Filename is timestamped so repeated exports never collide.",
                "报表已生成。文件名带时间戳，重复导出不会冲突。",
                7000,
                null));

        steps.add(new Step(
                "Demo finished — restoring original data so you can replay it.",
                "演示结束 — 正在恢复原始数据，方便您再次播放。",
                4500,
                null));
    }

    // ------------------------------------------------------------------
    // Step actions (all swallow exceptions and forward to the caption so
    // a partial environment never crashes the whole demo).
    // ------------------------------------------------------------------

    private void safeEnterAs(String email, String password) {
        try {
            User user = authService.login(email, password);
            app.demoEnterAs(user);
        } catch (Exception ex) {
            // Best-effort: stay where we are and surface to the caption.
            app.demoAppendCaption("(login failed: " + ex.getMessage() + ")",
                    "(登录失败：" + ex.getMessage() + ")");
        }
    }

    private void safeAcceptPendingApplication(String taUserId, String jobId,
                                              String moEmail, String moPassword) {
        try {
            User mo = authService.login(moEmail, moPassword);
            Optional<Application> pending = applicationService.getApplicationsByJobId(jobId).stream()
                    .filter(a -> taUserId.equalsIgnoreCase(a.getTaUserId()))
                    .filter(a -> a.getStatus() == ApplicationStatus.PENDING
                            || a.getStatus() == ApplicationStatus.APPLIED
                            || a.getStatus() == ApplicationStatus.REVIEWING)
                    .findFirst();
            if (pending.isPresent()) {
                applicationService.updateApplicationStatus(
                        pending.get().getId(), mo.getId(), ApplicationStatus.ACCEPTED);
            } else {
                app.demoAppendCaption("(no pending application to accept)",
                        "（没有待审申请可以通过）");
            }
        } catch (Exception ex) {
            app.demoAppendCaption("(accept failed: " + ex.getMessage() + ")",
                    "（通过失败：" + ex.getMessage() + "）");
        }
    }

    private void safeRefreshCurrentPanel() {
        try {
            app.demoRefreshCurrentPanel();
        } catch (Exception ignored) {
            // Best-effort refresh.
        }
    }

    private void safeExportWorkload() {
        try {
            Path exported = exportService.exportTaWorkloadSummary();
            app.demoAppendCaption("→ " + exported.getFileName(),
                    "→ " + exported.getFileName());
        } catch (Exception ex) {
            app.demoAppendCaption("(export failed: " + ex.getMessage() + ")",
                    "（导出失败：" + ex.getMessage() + "）");
        }
    }

    // ------------------------------------------------------------------
    // Driver
    // ------------------------------------------------------------------

    private void scheduleNext() {
        if (!running || currentStep >= steps.size()) {
            finish();
            return;
        }
        Step step = steps.get(currentStep++);
        app.demoShowCaption(step.captionEn, step.captionCn);
        if (step.action != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    step.action.run();
                } catch (Exception ignored) {
                    // Best-effort: caption already surfaces failures.
                }
            });
        }
        scheduler = new Timer(step.durationMs, e -> scheduleNext());
        scheduler.setRepeats(false);
        scheduler.start();
    }

    private void finish() {
        if (scheduler != null) {
            scheduler.stop();
            scheduler = null;
        }
        try {
            restoreData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(app.getMainFrame(),
                    "Demo finished but data restore failed:\n" + ex.getMessage()
                            + "\n\nPlease restore data/ from git if needed.",
                    "Auto Demo", JOptionPane.WARNING_MESSAGE);
        }
        SwingUtilities.invokeLater(() -> {
            app.demoHideCaption();
            app.demoReturnToLogin();
            JOptionPane.showMessageDialog(app.getMainFrame(),
                    "Auto demo finished.\nOriginal data files have been restored.\n\n"
                            + "演示完毕。原始数据文件已自动恢复。",
                    "Auto Demo", JOptionPane.INFORMATION_MESSAGE);
            running = false;
        });
    }

    // ------------------------------------------------------------------
    // Data backup / restore
    // ------------------------------------------------------------------

    private void backupData() throws Exception {
        backedUpFiles.clear();
        if (dataDirectory == null || !Files.isDirectory(dataDirectory)) {
            return;
        }
        for (String name : BACKUP_FILE_NAMES) {
            Path src = dataDirectory.resolve(name);
            if (!Files.exists(src)) {
                continue;
            }
            Path backup = dataDirectory.resolve(name + BACKUP_SUFFIX);
            Files.copy(src, backup, StandardCopyOption.REPLACE_EXISTING);
            backedUpFiles.add(src);
        }
    }

    private void restoreData() throws Exception {
        for (Path src : backedUpFiles) {
            Path backup = src.resolveSibling(src.getFileName() + BACKUP_SUFFIX);
            if (Files.exists(backup)) {
                Files.copy(backup, src, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backup);
            }
        }
        backedUpFiles.clear();
    }
}
