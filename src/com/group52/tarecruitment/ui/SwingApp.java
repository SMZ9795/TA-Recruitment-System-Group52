package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.GradientPaint;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class SwingApp {
    private static final String PAGE_LOGIN = "login";
    private static final String PAGE_TA = "ta";
    private static final String PAGE_MO = "mo";
    private static final String PAGE_ADMIN = "admin";

    // 现代配色方案
    private static final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color LIGHT_BACKGROUND = new Color(248, 249, 250);
    private static final Color CARD_BACKGROUND = new Color(255, 255, 255);
    private static final Color TEXT_PRIMARY = new Color(51, 51, 51);
    private static final Color TEXT_SECONDARY = new Color(102, 102, 102);
    private static final Color BORDER_COLOR = new Color(204, 204, 204);
    private static final Color PRIMARY_DARK = new Color(30, 41, 59);
    private static final Color SHADOW_COLOR = new Color(0, 0, 0, 20);

    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final Path dataDirectory;

    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private LoginPanel loginPanel;
    private TaPanel taPanel;
    private MoPanel moPanel;
    private AdminPanel adminPanel;

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService) {
        this(authService, jobService, applicationService, null);
    }

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService, Path dataDirectory) {
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.dataDirectory = dataDirectory;
    }

    public void start() {
        SwingUtilities.invokeLater(this::initAndShow);
    }

    // 添加阴影效果
    private void addShadow(JPanel panel) {
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createLineBorder(SHADOW_COLOR, 1)
        ));
        panel.setBackground(CARD_BACKGROUND);
    }

    // 渐变面板
    private class GradientPanel extends JPanel {
        private final Color startColor;
        private final Color endColor;

        public GradientPanel(Color startColor, Color endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            int width = getWidth();
            int height = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, startColor, width, height, endColor);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, width, height);
        }
    }

    private void initAndShow() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            // 全局字体设置
            javax.swing.UIManager.put("Table.rowHeight", 30);
            javax.swing.UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
            javax.swing.UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));
            javax.swing.UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
            javax.swing.UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
            javax.swing.UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
            javax.swing.UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
            javax.swing.UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        } catch (Exception e) {
            // Ignore if L&F fails
        }

        frame = new JFrame("BUPT TA Recruitment System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 720);
        frame.setLocationRelativeTo(null);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);

        loginPanel = new LoginPanel();
        taPanel = new TaPanel();
        moPanel = new MoPanel();
        adminPanel = new AdminPanel();

        rootPanel.add(loginPanel, PAGE_LOGIN);
        rootPanel.add(taPanel, PAGE_TA);
        rootPanel.add(moPanel, PAGE_MO);
        rootPanel.add(adminPanel, PAGE_ADMIN);

        frame.setContentPane(rootPanel);
        showLoginPage();
        frame.setVisible(true);
    }

    private void showLoginPage() {
        loginPanel.reset();
        showPage(PAGE_LOGIN);
    }

    private void onLoginSuccess(User user) {
        if (user.getRole() == Role.TA) {
            taPanel.bindUser(user);
            showPage(PAGE_TA);
            return;
        }
        if (user.getRole() == Role.MO) {
            moPanel.bindUser(user);
            showPage(PAGE_MO);
            return;
        }
        adminPanel.bindUser(user);
        showPage(PAGE_ADMIN);
    }

    private void showPage(String pageName) {
        rootLayout.show(rootPanel, pageName);
        frame.setContentPane(rootPanel);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel buildTopBar(String title, Runnable logoutAction) {
        JPanel bar = new GradientPanel(PRIMARY_COLOR, PRIMARY_DARK);
        bar.setLayout(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        bar.add(titleLabel, BorderLayout.WEST);
        
        if (logoutAction != null) {
            JButton logoutButton = new JButton("Logout");
            logoutButton.setFocusPainted(false);
            logoutButton.setBackground(ACCENT_COLOR);
            logoutButton.setForeground(Color.WHITE);
            logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            logoutButton.addActionListener(e -> logoutAction.run());
            bar.add(logoutButton, BorderLayout.EAST);
        }
        return bar;
    }

    private JPanel buildNavigationPanel(String[] labels, Runnable[] actions) {
        JPanel nav = new JPanel();
        nav.setBackground(LIGHT_BACKGROUND);
        nav.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(220, 0));

        JLabel menuLabel = new JLabel("Menu");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        menuLabel.setForeground(TEXT_PRIMARY);
        menuLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nav.add(menuLabel);
        nav.add(Box.createVerticalStrut(20));

        for (int i = 0; i < labels.length; i++) {
            JButton button = new JButton(labels[i]);
            button.setMaximumSize(new Dimension(190, 45));
            button.setPreferredSize(new Dimension(190, 45));
            button.setFocusPainted(false);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            button.setBackground(CARD_BACKGROUND);
            button.setForeground(TEXT_PRIMARY);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            final int actionIndex = i;
            button.addActionListener(e -> actions[actionIndex].run());
            nav.add(button);
            nav.add(Box.createVerticalStrut(12));
        }
        return nav;
    }

    private int parseIntOrZero(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Optional<User> findUserById(String userId) {
        return authService.findById(userId);
    }

    private Optional<Job> findJobById(String jobId) {
        return jobService.getJobById(jobId);
    }

    private String moNameForJob(Job job) {
        return findUserById(job.getPostedByMoId()).map(User::getName).orElse("Unknown MO");
    }

    private int acceptedHoursForTa(String taUserId) {
        int total = 0;
        for (Application application : applicationService.getApplicationsByTaUserId(taUserId)) {
            if (application.getStatus() == ApplicationStatus.ACCEPTED) {
                total += findJobById(application.getJobId()).map(Job::getHoursPerWeek).orElse(0);
            }
        }
        return total;
    }

    private int acceptedApplicantsForJob(String jobId) {
        int count = 0;
        for (Application application : applicationService.getApplicationsByJobId(jobId)) {
            if (application.getStatus() == ApplicationStatus.ACCEPTED) {
                count++;
            }
        }
        return count;
    }

    // 技能相关性映射，用于更智能的技能匹配
    private static final java.util.Map<String, java.util.Set<String>> SKILL_SYNONYMS = new java.util.HashMap<>();
    static {
        // 编程语言相关
        SKILL_SYNONYMS.put("java", java.util.Set.of("jvm", "spring", "maven", "gradle"));
        SKILL_SYNONYMS.put("python", java.util.Set.of("django", "flask", "pandas", "numpy"));
        SKILL_SYNONYMS.put("c++", java.util.Set.of("cpp", "cplusplus"));
        SKILL_SYNONYMS.put("javascript", java.util.Set.of("js", "node", "react", "angular", "vue"));
        // 数据库相关
        SKILL_SYNONYMS.put("sql", java.util.Set.of("mysql", "postgres", "oracle", "sqlserver"));
        SKILL_SYNONYMS.put("nosql", java.util.Set.of("mongodb", "redis", "cassandra"));
        // 其他技能
        SKILL_SYNONYMS.put("linux", java.util.Set.of("unix", "bash", "shell"));
        SKILL_SYNONYMS.put("git", java.util.Set.of("github", "gitlab"));
        SKILL_SYNONYMS.put("algorithm", java.util.Set.of("data structures", "leetcode"));
    }

    // 增强的技能匹配算法，考虑技能相关性和权重
    private int calculateMatchScore(String taSkillsText, String requiredSkillsText) {
        Set<String> taSkills = normalizeSkills(taSkillsText);
        Set<String> requiredSkills = normalizeSkills(requiredSkillsText);
        if (requiredSkills.isEmpty()) {
            return 100;
        }
        
        int totalScore = 0;
        int maxPossibleScore = requiredSkills.size() * 100;
        
        for (String required : requiredSkills) {
            // 完全匹配得100分
            if (taSkills.contains(required)) {
                totalScore += 100;
            } else {
                // 部分匹配或相关技能得50分
                for (String taSkill : taSkills) {
                    if (isSkillRelated(taSkill, required)) {
                        totalScore += 50;
                        break;
                    }
                }
            }
        }
        
        return Math.min(100, (int) Math.round((totalScore * 100.0) / maxPossibleScore));
    }

    // 检查技能是否相关
    private boolean isSkillRelated(String skill1, String skill2) {
        // 检查直接同义词
        if (SKILL_SYNONYMS.containsKey(skill1) && SKILL_SYNONYMS.get(skill1).contains(skill2)) {
            return true;
        }
        if (SKILL_SYNONYMS.containsKey(skill2) && SKILL_SYNONYMS.get(skill2).contains(skill1)) {
            return true;
        }
        // 检查技能名称的部分匹配
        return skill1.contains(skill2) || skill2.contains(skill1);
    }

    // 增强的技能缺失识别，提供更具体的建议
    private String listMissingSkills(String taSkillsText, String requiredSkillsText) {
        Set<String> taSkills = normalizeSkills(taSkillsText);
        Set<String> requiredSkills = normalizeSkills(requiredSkillsText);
        List<String> missing = new ArrayList<>();
        List<String> related = new ArrayList<>();
        
        for (String required : requiredSkills) {
            boolean found = taSkills.contains(required);
            if (!found) {
                // 检查是否有相关技能
                boolean hasRelated = false;
                for (String taSkill : taSkills) {
                    if (isSkillRelated(taSkill, required)) {
                        related.add(required + " (related: " + taSkill + ")");
                        hasRelated = true;
                        break;
                    }
                }
                if (!hasRelated) {
                    missing.add(required);
                }
            }
        }
        
        StringBuilder result = new StringBuilder();
        if (!missing.isEmpty()) {
            result.append("Missing: " + String.join(", ", missing));
        }
        if (!related.isEmpty()) {
            if (result.length() > 0) {
                result.append("; ");
            }
            result.append("Related: " + String.join(", ", related));
        }
        return result.length() > 0 ? result.toString() : "None";
    }

    // 标准化技能名称
    private Set<String> normalizeSkills(String rawSkills) {
        Set<String> skills = new HashSet<>();
        if (rawSkills == null || rawSkills.isBlank()) {
            return skills;
        }
        String[] parts = rawSkills.split("[,;|/]");
        for (String part : parts) {
            String cleaned = part.trim().toLowerCase();
            if (!cleaned.isEmpty()) {
                skills.add(cleaned);
            }
        }
        return skills;
    }

    // 工作量平衡建议
    private String getWorkloadBalanceSuggestion(String taUserId) {
        int currentHours = acceptedHoursForTa(taUserId);
        User ta = findUserById(taUserId).orElse(null);
        if (ta == null) {
            return "User not found";
        }
        int availableHours = ta.getAvailableHours();
        int remainingHours = availableHours - currentHours;
        
        if (remainingHours < 0) {
            return "Warning: You are overloaded by " + Math.abs(remainingHours) + " hours per week. Consider reducing your workload.";
        } else if (remainingHours == 0) {
            return "You are fully allocated. No additional hours available.";
        } else if (remainingHours < 5) {
            return "You have " + remainingHours + " hours remaining. Consider part-time opportunities.";
        } else {
            return "You have " + remainingHours + " hours available. You can take on additional responsibilities.";
        }
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void openCvFile(String cvPath) {
        if (cvPath == null || cvPath.isBlank()) {
            JOptionPane.showMessageDialog(frame, "No CV uploaded yet.");
            return;
        }
        File file = new File(cvPath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(frame, "CV file not found:\n" + cvPath);
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            JOptionPane.showMessageDialog(frame, "Cannot open file automatically on this system.\nFile path: " + cvPath);
            return;
        }
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to open CV: " + ex.getMessage());
        }
    }

    private class LoginPanel extends JPanel {
        private final JTextField emailField;
        private final JPasswordField passwordField;
        private final JComboBox<Role> roleCombo;
        private final JButton loginButton;
        private final JButton registerButton;
        private final JLabel statusLabel;

        private LoginPanel() {
            setLayout(new BorderLayout());
            JPanel centerWrapper = new JPanel(new GridBagLayout());

            JPanel form = new JPanel(new GridLayout(0, 2, 15, 14));
            form.setPreferredSize(new Dimension(450, 250));
            form.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));
            form.setBackground(CARD_BACKGROUND);
            form.setOpaque(true);
            addShadow(form);

            JLabel roleLabel = new JLabel("Role:");
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            roleLabel.setForeground(TEXT_PRIMARY);
            form.add(roleLabel);
            roleCombo = new JComboBox<>(new Role[] {Role.TA, Role.MO, Role.ADMIN});
            roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(roleCombo);

            JLabel emailLabel = new JLabel("Email:");
            emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            emailLabel.setForeground(TEXT_PRIMARY);
            form.add(emailLabel);
            emailField = new JTextField();
            emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(emailField);

            JLabel pwdLabel = new JLabel("Password:");
            pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            pwdLabel.setForeground(TEXT_PRIMARY);
            form.add(pwdLabel);
            passwordField = new JPasswordField();
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(passwordField);

            loginButton = new JButton("Login");
            loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            loginButton.setBackground(SECONDARY_COLOR);
            loginButton.setForeground(Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            loginButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            loginButton.addActionListener(e -> login());
            form.add(loginButton);

            registerButton = new JButton("Register as TA");
            registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            registerButton.setBackground(TEXT_SECONDARY);
            registerButton.setForeground(Color.WHITE);
            registerButton.setFocusPainted(false);
            registerButton.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            registerButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            registerButton.addActionListener(e -> registerTa());
            form.add(registerButton);

            statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(TEXT_SECONDARY);

            JPanel centerContainer = new JPanel(new BorderLayout());
            centerContainer.setOpaque(false);
            centerContainer.add(form, BorderLayout.CENTER);
            centerContainer.add(statusLabel, BorderLayout.SOUTH);
            centerWrapper.add(centerContainer);

            // 添加渐变背景
            GradientPanel backgroundPanel = new GradientPanel(LIGHT_BACKGROUND, CARD_BACKGROUND);
            backgroundPanel.setLayout(new BorderLayout());
            backgroundPanel.add(buildTopBar("BUPT International School TA Recruitment System", null), BorderLayout.NORTH);
            backgroundPanel.add(centerWrapper, BorderLayout.CENTER);
            add(backgroundPanel, BorderLayout.CENTER);
            frameSetDefaultButton();
        }

        private void reset() {
            emailField.setText("");
            passwordField.setText("");
            roleCombo.setSelectedItem(Role.TA);
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);
            statusLabel.setText(" ");
            frameSetDefaultButton();
        }

        private void frameSetDefaultButton() {
            if (frame != null && frame.getRootPane() != null) {
                frame.getRootPane().setDefaultButton(loginButton);
            }
        }

        private void login() {
            try {
                statusLabel.setText("Logging in...");
                loginButton.setEnabled(false);
                registerButton.setEnabled(false);
                
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                
                if (email.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both email and password.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("Please fill in email and password.");
                    return;
                }
                
                Optional<User> loginUser = authService.login(email, password);
                if (loginUser.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Login failed. Check credentials or account status.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Login failed.");
                    return;
                }
                
                roleCombo.setSelectedItem(loginUser.get().getRole());
                statusLabel.setText("Login success.");
                JOptionPane.showMessageDialog(frame, "Welcome back, " + loginUser.get().getName() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                onLoginSuccess(loginUser.get());
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                String detail = ex.getClass().getSimpleName();
                if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                    detail += ": " + ex.getMessage();
                }
                JOptionPane.showMessageDialog(frame, "Login error: " + detail, "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Login error.");
            } finally {
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }
        }

        private void registerTa() {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("Name"));
            panel.add(nameField);
            panel.add(new JLabel("Email"));
            panel.add(emailField);
            panel.add(new JLabel("Password (>= 8 chars)"));
            panel.add(passwordField);
            int option = JOptionPane.showConfirmDialog(
                    frame, panel, "Register New TA", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                statusLabel.setText("Registering...");
                loginButton.setEnabled(false);
                registerButton.setEnabled(false);
                
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("Registration failed: missing fields.");
                    return;
                }
                
                if (password.length() < 8) {
                    JOptionPane.showMessageDialog(frame, "Password must be at least 8 characters long.", "Input Error", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("Registration failed: password too short.");
                    return;
                }
                
                User ta = authService.registerTa(name, email, password);
                JOptionPane.showMessageDialog(frame, "TA registered: " + ta.getId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                statusLabel.setText("Registration successful.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Registration error: " + ex.getMessage());
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                String detail = ex.getClass().getSimpleName();
                if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                    detail += ": " + ex.getMessage();
                }
                JOptionPane.showMessageDialog(frame, "Registration error: " + detail, "Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Registration error.");
            } finally {
                statusLabel.setText("Registration completed.");
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
            }
        }
    }

    private class TaPanel extends JPanel {
        private static final String TAB_DASHBOARD = "dashboard";
        private static final String TAB_JOB_BOARD = "jobBoard";
        private static final String TAB_PROFILE = "profile";

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel applicationModel;
        private final DefaultTableModel jobModel;
        private final JTable applicationTable;
        private final JTable jobTable;
        private final JTextField searchField;
        private final JTextField skillsFilterField;
        private final JTextField hoursFilterField;
        private final JTextField moFilterField;
        private final JComboBox<String> statusFilterBox;
        private final JTextField profileNameField;
        private final JTextField profileYearField;
        private final JTextField profileProgrammeField;
        private final JTextArea profileSkillsArea;
        private final JTextField profileHoursField;
        private final JLabel cvLabel;

        private User user;
        private String selectedCvPath = "";
        private String selectedCvName = "";

        private TaPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("TA Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("TA Dashboard", SwingApp.this::showLoginPage);
            add(topBar, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);
            contentPanel.setBackground(LIGHT_BACKGROUND);

            String[] navLabels = {"Dashboard", "Job Board", "My Profile"};
            Runnable[] navActions = {
                () -> {
                    refreshApplications();
                    contentLayout.show(contentPanel, TAB_DASHBOARD);
                },
                () -> {
                    refreshJobs();
                    contentLayout.show(contentPanel, TAB_JOB_BOARD);
                },
                () -> {
                    loadProfile();
                    contentLayout.show(contentPanel, TAB_PROFILE);
                }
            };
            add(buildNavigationPanel(navLabels, navActions), BorderLayout.WEST);

            applicationModel = new DefaultTableModel(
                    new Object[] {"App ID", "Job", "MO", "Status", "Applied Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            applicationTable = new JTable(applicationModel);
            JPanel dashboardPanel = new JPanel(new BorderLayout(10, 10));
            dashboardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JLabel dashboardTitle = new JLabel("My Application Status");
            dashboardTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            dashboardTitle.setForeground(TEXT_PRIMARY);
            dashboardPanel.add(dashboardTitle, BorderLayout.NORTH);
            JScrollPane scrollPane = new JScrollPane(applicationTable);
            scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            dashboardPanel.add(scrollPane, BorderLayout.CENTER);
            JPanel dashboardActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton withdrawButton = new JButton("Withdraw Selected");
            withdrawButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            withdrawButton.setBackground(ACCENT_COLOR);
            withdrawButton.setForeground(Color.WHITE);
            withdrawButton.setFocusPainted(false);
            withdrawButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            withdrawButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            withdrawButton.addActionListener(e -> withdrawSelected());
            
            JButton refreshAppsButton = new JButton("Refresh");
            refreshAppsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            refreshAppsButton.setBackground(SECONDARY_COLOR);
            refreshAppsButton.setForeground(Color.WHITE);
            refreshAppsButton.setFocusPainted(false);
            refreshAppsButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            refreshAppsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            refreshAppsButton.addActionListener(e -> refreshApplications());
            
            dashboardActions.add(withdrawButton);
            dashboardActions.add(refreshAppsButton);
            dashboardPanel.add(dashboardActions, BorderLayout.SOUTH);
            addShadow(dashboardPanel);

            jobModel = new DefaultTableModel(
                    new Object[] {"Job ID", "Module", "MO", "Hours/Week", "Deadline", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobTable = new JTable(jobModel);
            JPanel jobBoardPanel = new JPanel(new BorderLayout(10, 10));
            jobBoardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            // 搜索和筛选面板
            JPanel searchPanel = new JPanel(new GridLayout(0, 4, 10, 8));
            searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            JPanel keywordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            keywordPanel.add(new JLabel("Search"));
            searchField = new JTextField(24);
            searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            keywordPanel.add(searchField);
            
            JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            skillsPanel.add(new JLabel("Skill"));
            skillsFilterField = new JTextField(14);
            skillsFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            skillsPanel.add(skillsFilterField);
            
            JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            hoursPanel.add(new JLabel("Max Hours"));
            hoursFilterField = new JTextField(8);
            hoursFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            hoursPanel.add(hoursFilterField);
            
            JPanel moPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            moPanel.add(new JLabel("MO"));
            moFilterField = new JTextField(14);
            moFilterField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            moPanel.add(moFilterField);
            
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(new JLabel("Status"));
            statusFilterBox = new JComboBox<>(new String[] {"OPEN", "ALL", "CLOSED", "FILLED"});
            statusFilterBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            statusPanel.add(statusFilterBox);
            
            JButton searchButton = new JButton("Apply Filter");
            searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            searchButton.setBackground(SECONDARY_COLOR);
            searchButton.setForeground(Color.WHITE);
            searchButton.setFocusPainted(false);
            searchButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            searchButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            searchButton.addActionListener(e -> refreshJobs());
            
            JButton clearButton = new JButton("Clear");
            clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            clearButton.setBackground(TEXT_SECONDARY);
            clearButton.setForeground(Color.WHITE);
            clearButton.setFocusPainted(false);
            clearButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            clearButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            clearButton.addActionListener(e -> {
                searchField.setText("");
                skillsFilterField.setText("");
                hoursFilterField.setText("");
                moFilterField.setText("");
                statusFilterBox.setSelectedItem("OPEN");
                refreshJobs();
            });
            
            JButton refreshJobsButton = new JButton("Refresh");
            refreshJobsButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            refreshJobsButton.setBackground(TEXT_SECONDARY);
            refreshJobsButton.setForeground(Color.WHITE);
            refreshJobsButton.setFocusPainted(false);
            refreshJobsButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            refreshJobsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            refreshJobsButton.addActionListener(e -> refreshJobs());
            
            JPanel filterActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterActionsPanel.add(searchButton);
            filterActionsPanel.add(clearButton);
            filterActionsPanel.add(refreshJobsButton);
            
            searchPanel.add(keywordPanel);
            searchPanel.add(skillsPanel);
            searchPanel.add(hoursPanel);
            searchPanel.add(moPanel);
            searchPanel.add(statusPanel);
            searchPanel.add(filterActionsPanel);
            jobBoardPanel.add(searchPanel, BorderLayout.NORTH);
            
            JScrollPane jobScrollPane = new JScrollPane(jobTable);
            jobScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            jobBoardPanel.add(jobScrollPane, BorderLayout.CENTER);
            
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton detailButton = new JButton("View Details");
            detailButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            detailButton.setBackground(SECONDARY_COLOR);
            detailButton.setForeground(Color.WHITE);
            detailButton.setFocusPainted(false);
            detailButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            detailButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            detailButton.addActionListener(e -> showJobDetails());
            
            JButton applyButton = new JButton("Apply Now");
            applyButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            applyButton.setBackground(SUCCESS_COLOR);
            applyButton.setForeground(Color.WHITE);
            applyButton.setFocusPainted(false);
            applyButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            applyButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            applyButton.addActionListener(e -> applySelectedJob());
            
            jobActions.add(detailButton);
            jobActions.add(applyButton);
            jobBoardPanel.add(jobActions, BorderLayout.SOUTH);
            addShadow(jobBoardPanel);

            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            
            JLabel profileTitle = new JLabel("My Profile");
            profileTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
            profileTitle.setForeground(TEXT_PRIMARY);
            profilePanel.add(profileTitle, BorderLayout.NORTH);
            
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            form.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            
            profileNameField = new JTextField();
            profileNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            profileYearField = new JTextField();
            profileYearField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            profileProgrammeField = new JTextField();
            profileProgrammeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            profileSkillsArea = new JTextArea(4, 20);
            profileSkillsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            profileSkillsArea.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
            profileHoursField = new JTextField();
            profileHoursField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cvLabel = new JLabel("No CV uploaded");
            cvLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cvLabel.setForeground(TEXT_SECONDARY);

            form.add(new JLabel("Name"));
            form.add(profileNameField);
            form.add(new JLabel("Year of Study"));
            form.add(profileYearField);
            form.add(new JLabel("Programme"));
            form.add(profileProgrammeField);
            form.add(new JLabel("Skills (comma-separated)"));
            form.add(new JScrollPane(profileSkillsArea));
            form.add(new JLabel("Available Hours/Week"));
            form.add(profileHoursField);
            form.add(new JLabel("Upload CV (.pdf/.txt)"));
            
            JPanel cvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
            JButton cvButton = new JButton("Choose File");
            cvButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            cvButton.setBackground(SECONDARY_COLOR);
            cvButton.setForeground(Color.WHITE);
            cvButton.setFocusPainted(false);
            cvButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            cvButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            cvButton.addActionListener(e -> chooseCvFile());
            
            JButton viewCvButton = new JButton("View CV");
            viewCvButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            viewCvButton.setBackground(TEXT_SECONDARY);
            viewCvButton.setForeground(Color.WHITE);
            viewCvButton.setFocusPainted(false);
            viewCvButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            viewCvButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            viewCvButton.addActionListener(e -> viewMyCv());
            
            cvPanel.add(cvButton);
            cvPanel.add(viewCvButton);
            cvPanel.add(cvLabel);
            form.add(cvPanel);

            profilePanel.add(form, BorderLayout.CENTER);
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton saveButton = new JButton("Save Profile");
            saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            saveButton.setBackground(SUCCESS_COLOR);
            saveButton.setForeground(Color.WHITE);
            saveButton.setFocusPainted(false);
            saveButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            saveButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            saveButton.addActionListener(e -> saveProfile());
            profileActions.add(saveButton);
            profilePanel.add(profileActions, BorderLayout.SOUTH);
            
            addShadow(profilePanel);

            contentPanel.add(dashboardPanel, TAB_DASHBOARD);
            contentPanel.add(jobBoardPanel, TAB_JOB_BOARD);
            contentPanel.add(profilePanel, TAB_PROFILE);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            this.user = user;
            refreshApplications();
            refreshJobs();
            loadProfile();
            contentLayout.show(contentPanel, TAB_DASHBOARD);
        }

        private void refreshApplications() {
            applicationModel.setRowCount(0);
            List<Application> applications = applicationService.getApplicationsByTaUserId(user.getId());
            applications.sort(Comparator.comparing(
                            Application::getAppliedDate,
                            Comparator.nullsLast(String::compareTo))
                    .reversed());
            for (Application application : applications) {
                Job job = findJobById(application.getJobId()).orElse(null);
                if (job == null) {
                    continue;
                }
                applicationModel.addRow(new Object[] {
                    safeText(application.getId()),
                    safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()),
                    moNameForJob(job),
                    application.getStatus() == null ? "" : application.getStatus().name(),
                    safeText(application.getAppliedDate())
                });
            }
        }

        private void refreshJobs() {
            jobModel.setRowCount(0);
            String query = searchField.getText().trim().toLowerCase();
            String skillQuery = skillsFilterField.getText().trim().toLowerCase();
            String moQuery = moFilterField.getText().trim().toLowerCase();
            String statusValue = String.valueOf(statusFilterBox.getSelectedItem());
            Integer maxHours = parseHoursFilter();
            if (maxHours != null && maxHours < 0) {
                return;
            }
            for (Job job : jobService.getAllJobs()) {
                String moduleCode = safeText(job.getModuleCode());
                String moduleName = safeText(job.getModuleName());
                String requiredSkills = safeText(job.getRequiredSkills());
                String moName = safeText(moNameForJob(job));
                boolean matchedKeyword = query.isBlank()
                        || moduleCode.toLowerCase().contains(query)
                        || moduleName.toLowerCase().contains(query)
                        || requiredSkills.toLowerCase().contains(query)
                        || moName.toLowerCase().contains(query);
                boolean matchedSkill = skillQuery.isBlank() || requiredSkills.toLowerCase().contains(skillQuery);
                boolean matchedMo = moQuery.isBlank()
                        || moName.toLowerCase().contains(moQuery)
                        || safeText(job.getPostedByMoId()).toLowerCase().contains(moQuery);
                boolean matchedStatus = "ALL".equalsIgnoreCase(statusValue)
                        || job.getStatus().name().equalsIgnoreCase(statusValue);
                boolean matchedHours = maxHours == null || job.getHoursPerWeek() <= maxHours;
                if (!matchedKeyword || !matchedSkill || !matchedMo || !matchedStatus || !matchedHours) {
                    continue;
                }
                jobModel.addRow(new Object[] {
                    safeText(job.getId()),
                    moduleCode + " - " + moduleName,
                    moName,
                    job.getHoursPerWeek(),
                    safeText(job.getDeadline()),
                    job.getStatus().name()
                });
            }
        }

        private Integer parseHoursFilter() {
            String hoursText = hoursFilterField.getText().trim();
            if (hoursText.isBlank()) {
                return null;
            }
            try {
                int maxHours = Integer.parseInt(hoursText);
                if (maxHours <= 0) {
                    JOptionPane.showMessageDialog(frame, "Max hours filter must be greater than 0.");
                    return -1;
                }
                return maxHours;
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Max hours filter must be a whole number.");
                return -1;
            }
        }

        private void applySelectedJob() {
            int selected = jobTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            String jobId = String.valueOf(jobModel.getValueAt(selected, 0));
            try {
                applicationService.applyForJob(jobId, user.getId());
                JOptionPane.showMessageDialog(frame, "Application submitted.");
                refreshApplications();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void showJobDetails() {
            int selected = jobTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            String jobId = String.valueOf(jobModel.getValueAt(selected, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                JOptionPane.showMessageDialog(frame, "Job not found.");
                return;
            }
            int score = calculateMatchScore(user.getSkills(), job.getRequiredSkills());
            String missingSkills = listMissingSkills(user.getSkills(), job.getRequiredSkills());
            String workloadSuggestion = getWorkloadBalanceSuggestion(user.getId());
            String message = "Module: " + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()) + "\n"
                    + "MO: " + moNameForJob(job) + "\n"
                    + "Required Skills: " + safeText(job.getRequiredSkills()) + "\n"
                    + "Weekly Hours: " + job.getHoursPerWeek() + "\n"
                    + "Deadline: " + safeText(job.getDeadline()) + "\n\n"
                    + "Description: " + safeText(job.getDescription()) + "\n\n"
                    + "Match Score: " + score + "%\n"
                    + "Missing Skills: " + missingSkills + "\n\n"
                    + "Workload Balance: " + workloadSuggestion;
            JOptionPane.showMessageDialog(frame, message, "Job Details", JOptionPane.INFORMATION_MESSAGE);
        }

        private void withdrawSelected() {
            int selected = applicationTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(frame, "Please select an application first.");
                return;
            }
            String applicationId = String.valueOf(applicationModel.getValueAt(selected, 0));
            Optional<Application> application = applicationService.getApplicationById(applicationId);
            if (application.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Application not found.");
                return;
            }
            if (application.get().getStatus() != ApplicationStatus.PENDING) {
                JOptionPane.showMessageDialog(frame, "Only pending applications can be withdrawn.");
                return;
            }
            applicationService.updateStatus(applicationId, ApplicationStatus.WITHDRAWN);
            refreshApplications();
        }

        private void loadProfile() {
            profileNameField.setText(safeText(user.getName()));
            profileYearField.setText(String.valueOf(user.getYearOfStudy()));
            profileProgrammeField.setText(safeText(user.getProgramme()));
            profileSkillsArea.setText(safeText(user.getSkills()));
            profileHoursField.setText(String.valueOf(user.getAvailableHours()));
            selectedCvPath = safeText(user.getCvFilePath());
            selectedCvName = selectedCvPath.isBlank() ? "" : new File(selectedCvPath).getName();
            cvLabel.setText(selectedCvName.isBlank() ? "No CV uploaded" : selectedCvName);
        }

        private void saveProfile() {
            try {
                user.setName(profileNameField.getText().trim());
                user.setYearOfStudy(parseIntOrZero(profileYearField.getText()));
                user.setProgramme(profileProgrammeField.getText().trim());
                user.setSkills(profileSkillsArea.getText().trim());
                user.setAvailableHours(parseIntOrZero(profileHoursField.getText()));
                user.setCvFilePath(selectedCvPath);
                authService.updateUser(user);
                JOptionPane.showMessageDialog(frame, "Profile saved.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void chooseCvFile() {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                File selectedFile = chooser.getSelectedFile();
                String lowerName = selectedFile.getName().toLowerCase();
                if (!lowerName.endsWith(".pdf") && !lowerName.endsWith(".txt")) {
                    JOptionPane.showMessageDialog(frame, "Only .pdf or .txt CV files are supported.");
                    return;
                }
                long maxSizeBytes = 5L * 1024 * 1024;
                if (selectedFile.length() > maxSizeBytes) {
                    JOptionPane.showMessageDialog(frame, "CV file is too large. Please choose a file <= 5 MB.");
                    return;
                }
                String savedPath = saveCvFile(selectedFile, user.getId());
                if (savedPath == null) {
                    return;
                }
                selectedCvPath = savedPath;
                selectedCvName = selectedFile.getName();
                cvLabel.setText(selectedCvName);
            }
        }

        private String saveCvFile(File sourceFile, String userId) {
            if (sourceFile == null || !sourceFile.exists()) {
                JOptionPane.showMessageDialog(frame, "Selected file does not exist.");
                return null;
            }
            if (userId == null || userId.isBlank()) {
                JOptionPane.showMessageDialog(frame, "User ID is required.");
                return null;
            }
            try {
                Path cvsDir;
                if (dataDirectory != null) {
                    cvsDir = dataDirectory.resolve("cvs");
                } else {
                    cvsDir = Path.of(System.getProperty("user.dir")).resolve("data").resolve("cvs");
                }
                // 确保目录存在
                Files.createDirectories(cvsDir);
                // 提取文件扩展名
                String ext = sourceFile.getName().contains(".")
                        ? sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'))
                        : "";
                // 生成唯一文件名
                String destName = userId + "_" + System.currentTimeMillis() + ext;
                Path destPath = cvsDir.resolve(destName);
                // 复制文件
                Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                return destPath.toAbsolutePath().toString();
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Failed to save CV file: " + ex.getMessage());
                return null;
            } catch (SecurityException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Permission denied: Cannot save CV file.");
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error saving CV file: " + ex.getMessage());
                return null;
            }
        }

        private void viewMyCv() {
            String cvPath = selectedCvPath.isBlank() ? safeText(user.getCvFilePath()) : selectedCvPath;
            openCvFile(cvPath);
        }
    }

    private class MoPanel extends JPanel {
        private static final String TAB_DASHBOARD = "dashboard";
        private static final String TAB_APPLICANTS = "applicants";
        private static final String TAB_PROFILE = "profile";

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel jobsModel;
        private final JTable jobsTable;
        private final DefaultTableModel applicantsModel;
        private final JTable applicantsTable;
        private final JLabel applicantsTitle;
        private final JTextField profileNameField;
        private final JTextField profileProgrammeField;
        private final JTextField profileEmailField;
        private final JTextField profileHoursField;
        private User user;
        private String selectedJobId;

        private MoPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("MO Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("MO Dashboard", SwingApp.this::showLoginPage);
            add(topBar, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);

            String[] navLabels = {"Dashboard", "Applicants List", "My Profile"};
            Runnable[] navActions = {
                () -> {
                    refreshJobs();
                    contentLayout.show(contentPanel, TAB_DASHBOARD);
                },
                () -> {
                    refreshApplicants();
                    contentLayout.show(contentPanel, TAB_APPLICANTS);
                },
                () -> {
                    loadProfile();
                    contentLayout.show(contentPanel, TAB_PROFILE);
                }
            };
            add(buildNavigationPanel(navLabels, navActions), BorderLayout.WEST);

            jobsModel = new DefaultTableModel(
                    new Object[] {"Job ID", "Module", "Positions", "Filled", "Status", "Deadline"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobsTable = new JTable(jobsModel);
            JPanel dashboardPanel = new JPanel(new BorderLayout(10, 10));
            dashboardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            dashboardPanel.add(new JLabel("My Posted Jobs Overview"), BorderLayout.NORTH);
            dashboardPanel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton postButton = new JButton("Post New Job");
            postButton.addActionListener(e -> createJob());
            JButton editButton = new JButton("Edit");
            editButton.addActionListener(e -> editSelectedJob());
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(e -> deleteSelectedJob());
            JButton applicantsButton = new JButton("View Applicants");
            applicantsButton.addActionListener(e -> openApplicantsForSelectedJob());
            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(e -> refreshJobs());
            jobActions.add(postButton);
            jobActions.add(editButton);
            jobActions.add(deleteButton);
            jobActions.add(applicantsButton);
            jobActions.add(refreshButton);
            dashboardPanel.add(jobActions, BorderLayout.SOUTH);

            applicantsModel = new DefaultTableModel(
                    new Object[] {"App ID", "Applicant", "Year", "AI Match", "Workload", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            applicantsTable = new JTable(applicantsModel);
            JPanel applicantsPanel = new JPanel(new BorderLayout(10, 10));
            applicantsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            applicantsTitle = new JLabel("Applicants List");
            applicantsPanel.add(applicantsTitle, BorderLayout.NORTH);
            applicantsPanel.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);
            JPanel applicantActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton acceptButton = new JButton("Accept");
            acceptButton.addActionListener(e -> updateApplicantStatus(ApplicationStatus.ACCEPTED));
            JButton rejectButton = new JButton("Reject");
            rejectButton.addActionListener(e -> updateApplicantStatus(ApplicationStatus.REJECTED));
            JButton viewCvButton = new JButton("View Applicant CV");
            viewCvButton.addActionListener(e -> viewSelectedApplicantCv());
            JButton refreshApplicantsButton = new JButton("Refresh");
            refreshApplicantsButton.addActionListener(e -> refreshApplicants());
            applicantActions.add(acceptButton);
            applicantActions.add(rejectButton);
            applicantActions.add(viewCvButton);
            applicantActions.add(refreshApplicantsButton);
            applicantsPanel.add(applicantActions, BorderLayout.SOUTH);

            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel profileForm = new JPanel(new GridLayout(0, 2, 10, 10));
            profileNameField = new JTextField();
            profileProgrammeField = new JTextField();
            profileEmailField = new JTextField();
            profileHoursField = new JTextField();
            profileForm.add(new JLabel("Full Name"));
            profileForm.add(profileNameField);
            profileForm.add(new JLabel("Department/School"));
            profileForm.add(profileProgrammeField);
            profileForm.add(new JLabel("Official Email"));
            profileForm.add(profileEmailField);
            profileForm.add(new JLabel("Available Hours/Week"));
            profileForm.add(profileHoursField);
            profilePanel.add(profileForm, BorderLayout.CENTER);
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton saveProfileButton = new JButton("Save Profile");
            saveProfileButton.addActionListener(e -> saveProfile());
            profileActions.add(saveProfileButton);
            profilePanel.add(profileActions, BorderLayout.SOUTH);

            contentPanel.add(dashboardPanel, TAB_DASHBOARD);
            contentPanel.add(applicantsPanel, TAB_APPLICANTS);
            contentPanel.add(profilePanel, TAB_PROFILE);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            this.user = user;
            this.selectedJobId = null;
            refreshJobs();
            refreshApplicants();
            loadProfile();
            contentLayout.show(contentPanel, TAB_DASHBOARD);
        }

        private void refreshJobs() {
            jobsModel.setRowCount(0);
            for (Job job : jobService.getJobsByMoId(user.getId())) {
                jobsModel.addRow(new Object[] {
                    job.getId(),
                    job.getModuleCode() + " - " + job.getModuleName(),
                    job.getPositions(),
                    acceptedApplicantsForJob(job.getId()),
                    job.getStatus().name(),
                    job.getDeadline()
                });
            }
        }

        private void createJob() {
            JobInput input = promptForJobInput(null);
            if (input == null) {
                return;
            }
            try {
                Job job = jobService.createJob(
                        input.moduleCode,
                        input.moduleName,
                        input.description,
                        input.requiredSkills,
                        input.hoursPerWeek,
                        input.positions,
                        input.deadline,
                        user.getId());
                job.setStatus(input.status);
                jobService.updateJob(job);
                refreshJobs();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void editSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            String jobId = String.valueOf(jobsModel.getValueAt(row, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                JOptionPane.showMessageDialog(frame, "Job not found.");
                return;
            }
            JobInput input = promptForJobInput(job);
            if (input == null) {
                return;
            }
            job.setModuleCode(input.moduleCode);
            job.setModuleName(input.moduleName);
            job.setDescription(input.description);
            job.setRequiredSkills(input.requiredSkills);
            job.setHoursPerWeek(input.hoursPerWeek);
            job.setPositions(input.positions);
            job.setDeadline(input.deadline);
            job.setStatus(input.status);
            jobService.updateJob(job);
            refreshJobs();
            refreshApplicants();
        }

        private void deleteSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            String jobId = String.valueOf(jobsModel.getValueAt(row, 0));
            int confirm = JOptionPane.showConfirmDialog(
                    frame, "Delete selected job?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            jobService.deleteJob(jobId);
            if (jobId.equalsIgnoreCase(selectedJobId)) {
                selectedJobId = null;
            }
            refreshJobs();
            refreshApplicants();
        }

        private void openApplicantsForSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            selectedJobId = String.valueOf(jobsModel.getValueAt(row, 0));
            refreshApplicants();
            contentLayout.show(contentPanel, TAB_APPLICANTS);
        }

        private void refreshApplicants() {
            applicantsModel.setRowCount(0);
            if (selectedJobId == null || selectedJobId.isBlank()) {
                applicantsTitle.setText("Applicants List (Select a job in Dashboard first)");
                return;
            }
            Job selectedJob = findJobById(selectedJobId).orElse(null);
            if (selectedJob == null) {
                applicantsTitle.setText("Applicants List");
                return;
            }
            applicantsTitle.setText("Applicants for " + selectedJob.getModuleCode() + " - " + selectedJob.getModuleName());
            for (Application application : applicationService.getApplicationsByJobId(selectedJobId)) {
                User ta = findUserById(application.getTaUserId()).orElse(null);
                if (ta == null) {
                    continue;
                }
                int score = calculateMatchScore(ta.getSkills(), selectedJob.getRequiredSkills());
                applicantsModel.addRow(new Object[] {
                    application.getId(),
                    ta.getName(),
                    ta.getYearOfStudy(),
                    score + "%",
                    acceptedHoursForTa(ta.getId()) + "h/week",
                    application.getStatus().name()
                });
            }
        }

        private void updateApplicantStatus(ApplicationStatus status) {
            int row = applicantsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
            String appId = String.valueOf(applicantsModel.getValueAt(row, 0));
            applicationService.updateStatus(appId, status);
            refreshApplicants();
            refreshJobs();
        }

        private void viewSelectedApplicantCv() {
            int row = applicantsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
            String appId = String.valueOf(applicantsModel.getValueAt(row, 0));
            Optional<Application> application = applicationService.getApplicationById(appId);
            if (application.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Application not found.");
                return;
            }
            User ta = findUserById(application.get().getTaUserId()).orElse(null);
            if (ta == null) {
                JOptionPane.showMessageDialog(frame, "Applicant not found.");
                return;
            }
            openCvFile(ta.getCvFilePath());
        }

        private void loadProfile() {
            profileNameField.setText(user.getName());
            profileProgrammeField.setText(user.getProgramme());
            profileEmailField.setText(user.getEmail());
            profileHoursField.setText(String.valueOf(user.getAvailableHours()));
        }

        private void saveProfile() {
            user.setName(profileNameField.getText().trim());
            user.setProgramme(profileProgrammeField.getText().trim());
            user.setEmail(profileEmailField.getText().trim());
            user.setAvailableHours(parseIntOrZero(profileHoursField.getText()));
            authService.updateUser(user);
            JOptionPane.showMessageDialog(frame, "Profile saved.");
        }

        private JobInput promptForJobInput(Job existing) {
            JTextField moduleCodeField = new JTextField(existing == null ? "" : existing.getModuleCode());
            JTextField moduleNameField = new JTextField(existing == null ? "" : existing.getModuleName());
            JTextArea descriptionArea = new JTextArea(existing == null ? "" : existing.getDescription(), 4, 20);
            JTextField requiredSkillsField = new JTextField(existing == null ? "" : existing.getRequiredSkills());
            JTextField hoursField = new JTextField(existing == null ? "" : String.valueOf(existing.getHoursPerWeek()));
            JTextField positionsField = new JTextField(existing == null ? "" : String.valueOf(existing.getPositions()));
            JTextField deadlineField = new JTextField(existing == null ? "" : existing.getDeadline());
            JComboBox<JobStatus> statusBox =
                    new JComboBox<>(new JobStatus[] {JobStatus.OPEN, JobStatus.CLOSED, JobStatus.FILLED});
            if (existing != null) {
                statusBox.setSelectedItem(existing.getStatus());
            }

            JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
            panel.add(new JLabel("Module Code"));
            panel.add(moduleCodeField);
            panel.add(new JLabel("Module Name"));
            panel.add(moduleNameField);
            panel.add(new JLabel("Description"));
            panel.add(new JScrollPane(descriptionArea));
            panel.add(new JLabel("Required Skills"));
            panel.add(requiredSkillsField);
            panel.add(new JLabel("Hours per Week"));
            panel.add(hoursField);
            panel.add(new JLabel("Positions"));
            panel.add(positionsField);
            panel.add(new JLabel("Deadline (YYYY-MM-DD)"));
            panel.add(deadlineField);
            panel.add(new JLabel("Status"));
            panel.add(statusBox);

            int option = JOptionPane.showConfirmDialog(
                    frame,
                    panel,
                    existing == null ? "Post New Job" : "Edit Job",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                return new JobInput(
                        moduleCodeField.getText().trim(),
                        moduleNameField.getText().trim(),
                        descriptionArea.getText().trim(),
                        requiredSkillsField.getText().trim(),
                        Integer.parseInt(hoursField.getText().trim()),
                        Integer.parseInt(positionsField.getText().trim()),
                        deadlineField.getText().trim(),
                        (JobStatus) statusBox.getSelectedItem());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Hours and positions must be numbers.");
                return null;
            }
        }
    }

    private class AdminPanel extends JPanel {
        private static final String TAB_WORKLOAD = "workload";
        private static final String TAB_ACCOUNTS = "accounts";
        private static final String TAB_JOBS = "jobs";

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel workloadModel;
        private final JTable workloadTable;
        private final DefaultTableModel accountModel;
        private final JTable accountTable;
        private final DefaultTableModel jobsModel;
        private final JTable jobsTable;

        private AdminPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("Admin Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("Admin Dashboard", SwingApp.this::showLoginPage);
            add(topBar, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);

            String[] navLabels = {"Workload Overview", "Manage Accounts", "Jobs Overview"};
            Runnable[] navActions = {
                () -> {
                    refreshWorkload();
                    contentLayout.show(contentPanel, TAB_WORKLOAD);
                },
                () -> {
                    refreshAccounts();
                    contentLayout.show(contentPanel, TAB_ACCOUNTS);
                },
                () -> {
                    refreshJobs();
                    contentLayout.show(contentPanel, TAB_JOBS);
                }
            };
            add(buildNavigationPanel(navLabels, navActions), BorderLayout.WEST);

            workloadModel = new DefaultTableModel(new Object[] {"TA ID", "TA Name", "Accepted Hours/Week", "Alert"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            workloadTable = new JTable(workloadModel);
            JPanel workloadPanel = new JPanel(new BorderLayout(10, 10));
            workloadPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            workloadPanel.add(new JLabel("TA Weekly Workload Monitor"), BorderLayout.NORTH);
            workloadPanel.add(new JScrollPane(workloadTable), BorderLayout.CENTER);
            JButton refreshWorkloadButton = new JButton("Refresh");
            refreshWorkloadButton.addActionListener(e -> refreshWorkload());
            JPanel workloadActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            workloadActions.add(refreshWorkloadButton);
            workloadPanel.add(workloadActions, BorderLayout.SOUTH);

            accountModel = new DefaultTableModel(
                    new Object[] {"User ID", "Name", "Email", "Role", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            accountTable = new JTable(accountModel);
            JPanel accountsPanel = new JPanel(new BorderLayout(10, 10));
            accountsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            accountsPanel.add(new JLabel("User Account Management"), BorderLayout.NORTH);
            accountsPanel.add(new JScrollPane(accountTable), BorderLayout.CENTER);
            JPanel accountActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton createMoButton = new JButton("Create New MO");
            createMoButton.addActionListener(e -> createMoAccount());
            JButton toggleButton = new JButton("Activate/Deactivate");
            toggleButton.addActionListener(e -> toggleSelectedAccount());
            JButton resetPwdButton = new JButton("Reset Password");
            resetPwdButton.addActionListener(e -> resetPassword());
            JButton refreshAccountsButton = new JButton("Refresh");
            refreshAccountsButton.addActionListener(e -> refreshAccounts());
            accountActions.add(createMoButton);
            accountActions.add(toggleButton);
            accountActions.add(resetPwdButton);
            accountActions.add(refreshAccountsButton);
            accountsPanel.add(accountActions, BorderLayout.SOUTH);

            jobsModel = new DefaultTableModel(
                    new Object[] {"Module", "MO", "Positions", "Filled", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobsTable = new JTable(jobsModel);
            JPanel jobsPanel = new JPanel(new BorderLayout(10, 10));
            jobsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            jobsPanel.add(new JLabel("Global Jobs Overview"), BorderLayout.NORTH);
            jobsPanel.add(new JScrollPane(jobsTable), BorderLayout.CENTER);
            JPanel jobsActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton refreshJobsButton = new JButton("Refresh");
            refreshJobsButton.addActionListener(e -> refreshJobs());
            jobsActions.add(refreshJobsButton);
            jobsPanel.add(jobsActions, BorderLayout.SOUTH);

            contentPanel.add(workloadPanel, TAB_WORKLOAD);
            contentPanel.add(accountsPanel, TAB_ACCOUNTS);
            contentPanel.add(jobsPanel, TAB_JOBS);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            refreshWorkload();
            refreshAccounts();
            refreshJobs();
            contentLayout.show(contentPanel, TAB_WORKLOAD);
        }

        private void refreshWorkload() {
            workloadModel.setRowCount(0);
            for (User candidate : authService.getAllUsers()) {
                if (candidate.getRole() != Role.TA) {
                    continue;
                }
                int hours = acceptedHoursForTa(candidate.getId());
                String alert = hours > 20 ? "Overloaded" : "";
                workloadModel.addRow(new Object[] {candidate.getId(), candidate.getName(), hours, alert});
            }
        }

        private void refreshAccounts() {
            accountModel.setRowCount(0);
            for (User candidate : authService.getAllUsers()) {
                accountModel.addRow(new Object[] {
                    candidate.getId(),
                    candidate.getName(),
                    candidate.getEmail(),
                    candidate.getRole().name(),
                    candidate.isActive() ? "Active" : "Deactivated"
                });
            }
        }

        private void refreshJobs() {
            jobsModel.setRowCount(0);
            for (Job job : jobService.getAllJobs()) {
                jobsModel.addRow(new Object[] {
                    job.getModuleCode() + " - " + job.getModuleName(),
                    moNameForJob(job),
                    job.getPositions(),
                    acceptedApplicantsForJob(job.getId()),
                    job.getStatus().name()
                });
            }
        }

        private void createMoAccount() {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("MO Name"));
            panel.add(nameField);
            panel.add(new JLabel("MO Email"));
            panel.add(emailField);
            panel.add(new JLabel("Password (>= 8 chars)"));
            panel.add(passwordField);
            int option = JOptionPane.showConfirmDialog(
                    frame, panel, "Create New MO Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                authService.createMoAccount(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passwordField.getPassword()));
                refreshAccounts();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void toggleSelectedAccount() {
            int row = accountTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a user first.");
                return;
            }
            String userId = String.valueOf(accountModel.getValueAt(row, 0));
            Optional<User> target = authService.findById(userId);
            if (target.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "User not found.");
                return;
            }
            if (target.get().getRole() == Role.ADMIN) {
                JOptionPane.showMessageDialog(frame, "Admin account cannot be deactivated here.");
                return;
            }
            authService.setUserActive(userId, !target.get().isActive());
            refreshAccounts();
        }

        private void resetPassword() {
            int row = accountTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a user first.");
                return;
            }
            String userId = String.valueOf(accountModel.getValueAt(row, 0));
            String newPassword = JOptionPane.showInputDialog(frame, "Enter new password (>=8 chars):");
            if (newPassword == null) {
                return;
            }
            try {
                authService.updatePassword(userId, newPassword.trim());
                JOptionPane.showMessageDialog(frame, "Password reset completed.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
    }

    private static class JobInput {
        private final String moduleCode;
        private final String moduleName;
        private final String description;
        private final String requiredSkills;
        private final int hoursPerWeek;
        private final int positions;
        private final String deadline;
        private final JobStatus status;

        private JobInput(String moduleCode, String moduleName, String description, String requiredSkills,
                int hoursPerWeek, int positions, String deadline, JobStatus status) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.description = description;
            this.requiredSkills = requiredSkills;
            this.hoursPerWeek = hoursPerWeek;
            this.positions = positions;
            this.deadline = deadline;
            this.status = status;
        }
    }
}
