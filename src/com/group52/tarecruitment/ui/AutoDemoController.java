package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.ExportService;
import com.group52.tarecruitment.service.JobService;
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
    private final JobService jobService;
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
                       JobService jobService,
                       ApplicationService applicationService,
                       AdminService adminService,
                       ExportService exportService,
                       Path dataDirectory) {
        this.app = app;
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.adminService = adminService;
        this.exportService = exportService;
        this.dataDirectory = dataDirectory;

        // Two safety nets that protect the real data files even if a
        // previous demo was killed mid-run (Force-Stop, power loss,
        // closing the window during playback):
        //
        // 1) On startup, restore any leftover *.demo-backup pair so the
        //    user always boots into a clean seed dataset.
        // 2) Register a JVM shutdown hook so a graceful close mid-demo
        //    still rolls the data files back.
        recoverLeftoverBackups();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownRestore,
                "auto-demo-shutdown-restore"));
    }

    /**
     * Restore any *.demo-backup files left over from a previous run
     * that did not complete (e.g. JVM was killed while the demo was
     * playing). Safe to call even when no backups exist.
     */
    private void recoverLeftoverBackups() {
        if (dataDirectory == null || !Files.isDirectory(dataDirectory)) {
            return;
        }
        for (String name : BACKUP_FILE_NAMES) {
            Path backup = dataDirectory.resolve(name + BACKUP_SUFFIX);
            if (!Files.exists(backup)) {
                continue;
            }
            Path src = dataDirectory.resolve(name);
            try {
                Files.copy(backup, src, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(backup);
                System.out.println("[AutoDemo] Restored leftover backup: " + name);
            } catch (Exception ex) {
                System.err.println("[AutoDemo] Failed to restore " + name
                        + ": " + ex.getMessage());
            }
        }
    }

    /** Shutdown hook: best-effort restore if a demo is running. */
    private void shutdownRestore() {
        if (!running) {
            return;
        }
        try {
            restoreData();
        } catch (Exception ignored) {
            // Best-effort; nothing else we can do during JVM shutdown.
        }
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

        // ============================================================
        // PHASE 0 — Opening (15s)
        // ============================================================
        steps.add(new Step(
                "Welcome to the BUPT TA Recruitment System — a complete TA hiring platform.",
                "欢迎使用 BUPT 助教招聘系统 —— 一个完整的助教招聘解决方案。",
                15000, null));

        // ============================================================
        // PHASE 1 — Login + Register + Error handling (60s)
        // ============================================================
        steps.add(new Step(
                "We start on the login screen — one entry point for TA, MO and Admin roles.",
                "登录页是 TA、MO、Admin 三种角色的统一入口。",
                12000, () -> app.demoReturnToLogin()));

        steps.add(new Step(
                "Error handling first: a wrong password is blocked by AuthService.",
                "先看错误处理：错误密码会被 AuthService 直接拦截。",
                10000, this::safeIntentionalBadLogin));

        steps.add(new Step(
                "Now the registration page — new TAs sign up here with email, password and skills.",
                "接下来是注册页 —— 新 TA 在这里填写邮箱、密码与技能完成注册。",
                14000, () -> app.demoShowRegisterPage()));

        steps.add(new Step(
                "Returning to login. All inputs are validated server-side; client UI just relays errors.",
                "回到登录页。所有输入都在服务端校验，前端只负责显示错误。",
                10000, () -> app.demoReturnToLogin()));

        // ============================================================
        // PHASE 2 — TA Experience (135s ~ 2:15)
        // ============================================================
        steps.add(new Step(
                "PART 1 / 3 — TA portal. Signing in as Carol Wu (a real seeded account).",
                "第一部分 / 共三部分 —— TA 端。以预置账号 Carol Wu 登录。",
                10000, () -> safeEnterAs(taEmail, taPassword)));

        steps.add(new Step(
                "TA Dashboard: at-a-glance counts of applied / pending / accepted jobs and AI tips.",
                "TA 工作台：一眼看到已申请 / 待审 / 已通过数量，并展示 AI 推荐提示。",
                20000, () -> app.demoSelectTaTab("dashboard")));

        steps.add(new Step(
                "Job Board: every open position with module, MO, hours, deadline and an AI Fit score.",
                "岗位看板：每个开放岗位的模块、MO、工时、截止日期与 AI 匹配分一目了然。",
                22000, () -> app.demoSelectTaTab("jobBoard")));

        steps.add(new Step(
                "Filters by skill, hours, MO, status — plus a 'Recommended only' toggle driven by AI.",
                "可按技能、工时、MO、状态筛选 —— 还可勾选 'Recommended only'，由 AI 推荐驱动。",
                18000, null));

        steps.add(new Step(
                "Notifications Centre: lifecycle events (status changes, new jobs) arrive here.",
                "通知中心：状态变更、新岗位等生命周期事件统一推送到这里。",
                18000, () -> app.demoSelectTaTab("notifications")));

        steps.add(new Step(
                "My Profile: name, programme, available hours, skills, CV upload & password change.",
                "个人资料：姓名、专业、可用工时、技能、CV 上传与修改密码全部在此完成。",
                20000, () -> app.demoSelectTaTab("profile")));

        steps.add(new Step(
                "Back to Dashboard — note the live count of Carol's pending application for CS101.",
                "回到工作台 —— 注意 Carol 在 CS101 上的待审申请实时计数。",
                15000, () -> app.demoSelectTaTab("dashboard")));

        // ============================================================
        // PHASE 3 — MO Experience (140s ~ 2:20)
        // ============================================================
        steps.add(new Step(
                "PART 2 / 3 — MO portal. Switching to Dr Smith, who posted CS101.",
                "第二部分 / 共三部分 —— MO 端。切换到 CS101 的发布人 Dr Smith。",
                10000, () -> safeEnterAs(moEmail, moPassword)));

        steps.add(new Step(
                "MO Dashboard: every job Dr Smith owns, with positions, filled count and status.",
                "MO 工作台：列出 Dr Smith 名下所有岗位的总坑位、已填补数量与状态。",
                18000, () -> app.demoSelectMoTab("dashboard")));

        steps.add(new Step(
                "Applicants List: each applicant ranked by AI match score AND current TA workload.",
                "申请人列表：综合 AI 匹配分与 TA 当前工作量进行排名。",
                22000, () -> app.demoSelectMoTab("applicants")));

        steps.add(new Step(
                "Click any row to see the AI explanation — overlapping skills and final reasoning.",
                "点击任意一行可看到 AI 解释 —— 重合技能与最终评分理由。",
                14000, null));

        steps.add(new Step(
                "Accepting Carol's pending application — service-layer call, all audit-logged.",
                "通过 Carol 的待审申请 —— 走 Service 层调用，全程审计留痕。",
                12000, () -> safeAcceptPendingApplication(demoTaId, demoJobId, moEmail, moPassword)));

        steps.add(new Step(
                "When all positions fill, the job auto-flips to FILLED. Self-healing state machine.",
                "所有坑位填满后，岗位自动切换到 FILLED 状态。这是自愈的状态机。",
                14000, () -> app.demoSelectMoTab("dashboard")));

        steps.add(new Step(
                "MO Notifications: accept / reject decisions push to TAs in real time.",
                "MO 通知：通过 / 拒绝操作实时推送给对应 TA。",
                14000, () -> app.demoSelectMoTab("notifications")));

        steps.add(new Step(
                "MO Profile: same self-service settings as TA, with role-scoped views.",
                "MO 个人资料：与 TA 类似的自助设置，但视图按角色隔离。",
                12000, () -> app.demoSelectMoTab("profile")));

        steps.add(new Step(
                "Error handling: trying to post an empty job is rejected by ValidationUtil.",
                "错误处理：提交空字段创建岗位会被 ValidationUtil 拒绝。",
                14000, this::safeIntentionalBadJobCreate));

        // ============================================================
        // PHASE 4 — Admin Experience (130s ~ 2:10)
        // ============================================================
        steps.add(new Step(
                "PART 3 / 3 — Admin portal. Signing in as the system administrator.",
                "第三部分 / 共三部分 —— Admin 端。以系统管理员身份登录。",
                10000, () -> safeEnterAs(adminEmail, adminPassword)));

        steps.add(new Step(
                "Workload Overview: every TA classified as Balanced / Overloaded / Underused.",
                "工作量总览：每个 TA 自动分类为 Balanced / Overloaded / Underused。",
                20000, () -> app.demoSelectAdminTab("workload")));

        steps.add(new Step(
                "Manage Accounts: search, reset password, disable — admin-grade controls.",
                "账号管理：搜索、重置密码、禁用账号 —— Admin 级权限管理。",
                18000, () -> app.demoSelectAdminTab("accounts")));

        steps.add(new Step(
                "Jobs Overview: system-wide pipeline of OPEN / FILLED / CLOSED jobs.",
                "岗位总览：全系统 OPEN / FILLED / CLOSED 三态岗位通览。",
                18000, () -> app.demoSelectAdminTab("jobs")));

        steps.add(new Step(
                "Applications: cross-MO view of every applicant, status and outcome.",
                "全部申请：跨 MO 视图查看每一份申请、状态与结果。",
                18000, () -> app.demoSelectAdminTab("applications")));

        steps.add(new Step(
                "Audit Log: every business event is immutably recorded — Carol's accept is here too.",
                "审计日志：所有业务事件不可篡改地记录在案 —— 刚才通过 Carol 的操作也在这里。",
                20000, () -> app.demoSelectAdminTab("audit")));

        steps.add(new Step(
                "Admin Notifications: high-priority system alerts (overload, expiry, security).",
                "Admin 通知：系统级高优告警（过载、过期、安全事件）集中在此。",
                14000, () -> app.demoSelectAdminTab("notifications")));

        // ============================================================
        // PHASE 5 — CSV Export + Wrap-up (50s)
        // ============================================================
        steps.add(new Step(
                "Exporting the TA Workload Summary CSV to data/exports/ ...",
                "正在导出 TA 工作量汇总 CSV 到 data/exports/ ...",
                12000, this::safeExportWorkload));

        steps.add(new Step(
                "Done. Filename embeds a timestamp; same-second exports get a numeric suffix.",
                "已完成。文件名包含时间戳；同秒重复导出会自动追加数字后缀。",
                12000, null));

        steps.add(new Step(
                "Three more CSVs are one click away: All Applications, Job Filling, Applicants per Job.",
                "另外三份 CSV 同样一键即得：全部申请、岗位填充率、单岗位申请人。",
                14000, null));

        steps.add(new Step(
                "Demo complete — restoring the seed data so you can replay this any time.",
                "演示结束 —— 正在恢复种子数据，方便随时重新播放。",
                14000, null));
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
                    .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
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

    private void safeExportWorkload() {
        try {
            Path exported = exportService.exportTaWorkloadSummary();
            app.demoAppendCaption("\u2192 " + exported.getFileName(),
                    "\u2192 " + exported.getFileName());
        } catch (Exception ex) {
            app.demoAppendCaption("(export failed: " + ex.getMessage() + ")",
                    "\uff08\u5bfc\u51fa\u5931\u8d25\uff1a" + ex.getMessage() + "\uff09");
        }
    }

    /** Trigger an intentional auth failure so the demo can show error handling. */
    private void safeIntentionalBadLogin() {
        try {
            authService.login("nobody@bupt.local", "wrongpassword");
            app.demoAppendCaption("(unexpected success)", "\uff08\u610f\u5916\u6210\u529f\uff09");
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "rejected" : ex.getMessage();
            app.demoAppendCaption("\u2192 blocked: " + msg,
                    "\u2192 \u5df2\u62e6\u622a\uff1a" + msg);
        }
    }

    /**
     * Trigger an intentional validation failure on JobService.createJob so the
     * demo can showcase the centralised ValidationUtil error pipeline.
     * We use a known invalid MO id and empty fields so the call never mutates state.
     */
    private void safeIntentionalBadJobCreate() {
        if (jobService == null) {
            app.demoAppendCaption("(jobService not wired)", "\uff08jobService \u672a\u63a5\u5165\uff09");
            return;
        }
        try {
            jobService.createJob("", "", "", "", "0", "0", "2020-01-01", "MO001");
            app.demoAppendCaption("(unexpected success)", "\uff08\u610f\u5916\u6210\u529f\uff09");
        } catch (Exception ex) {
            String msg = ex.getMessage() == null ? "rejected" : ex.getMessage();
            app.demoAppendCaption("\u2192 validation: " + msg,
                    "\u2192 \u6821\u9a8c\u62e6\u622a\uff1a" + msg);
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
