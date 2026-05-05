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
import com.group52.tarecruitment.util.CvValidationUtil;
import com.group52.tarecruitment.util.JobFilterUtil;
import com.group52.tarecruitment.util.TaNotificationUtil;
import com.group52.tarecruitment.util.TaNotificationUtil.ApplicationStatusSummary;
import com.group52.tarecruitment.util.TaNotificationUtil.NotificationEntry;
import com.group52.tarecruitment.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Color;
import java.awt.Font;
import java.awt.Desktop;
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

    private void initAndShow() {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            javax.swing.UIManager.put("Table.rowHeight", 30);
            javax.swing.UIManager.put("Table.font", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            javax.swing.UIManager.put("TableHeader.font", new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
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
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new java.awt.Color(0, 80, 158));
        bar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(java.awt.Color.WHITE);
        titleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 22));
        bar.add(titleLabel, BorderLayout.WEST);
        
        if (logoutAction != null) {
            JButton logoutButton = new JButton("Logout");
            logoutButton.setFocusPainted(false);
            logoutButton.setBackground(new java.awt.Color(220, 53, 69));
            logoutButton.setForeground(java.awt.Color.WHITE);
            logoutButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            logoutButton.addActionListener(e -> logoutAction.run());
            bar.add(logoutButton, BorderLayout.EAST);
        }
        return bar;
    }

    private JPanel buildNavigationPanel(String[] labels, Runnable[] actions) {
        JPanel nav = new JPanel();
        nav.setBackground(new java.awt.Color(240, 242, 245));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(220, 0));

        JLabel menuLabel = new JLabel("Menu");
        menuLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 18));
        menuLabel.setForeground(java.awt.Color.DARK_GRAY);
        menuLabel.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        nav.add(menuLabel);
        nav.add(Box.createVerticalStrut(20));

        for (int i = 0; i < labels.length; i++) {
            JButton button = new JButton(labels[i]);
            button.setMaximumSize(new Dimension(190, 45));
            button.setPreferredSize(new Dimension(190, 45));
            button.setFocusPainted(false);
            button.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 15));
            button.setBackground(java.awt.Color.WHITE);
            button.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
            final int actionIndex = i;
            button.addActionListener(e -> actions[actionIndex].run());
            nav.add(button);
            nav.add(Box.createVerticalStrut(12));
        }
        return nav;
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

    private int calculateMatchScore(String taSkillsText, String requiredSkillsText) {
        Set<String> taSkills = normalizeSkills(taSkillsText);
        Set<String> requiredSkills = normalizeSkills(requiredSkillsText);
        if (requiredSkills.isEmpty()) {
            return 100;
        }
        int matches = 0;
        for (String required : requiredSkills) {
            if (taSkills.contains(required)) {
                matches++;
            }
        }
        return (int) Math.round((matches * 100.0) / requiredSkills.size());
    }

    private String listMissingSkills(String taSkillsText, String requiredSkillsText) {
        Set<String> taSkills = normalizeSkills(taSkillsText);
        List<String> missing = new ArrayList<>();
        for (String required : normalizeSkills(requiredSkillsText)) {
            if (!taSkills.contains(required)) {
                missing.add(required);
            }
        }
        return missing.isEmpty() ? "None" : String.join(", ", missing);
    }

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
                    BorderFactory.createLineBorder(new java.awt.Color(200, 200, 200), 1, true),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)
            ));
            form.setBackground(java.awt.Color.WHITE);
            form.setOpaque(true);

            JLabel roleLabel = new JLabel("Role:");
            roleLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            form.add(roleLabel);
            roleCombo = new JComboBox<>(new Role[] {Role.TA, Role.MO, Role.ADMIN});
            roleCombo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            form.add(roleCombo);

            JLabel emailLabel = new JLabel("Email:");
            emailLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            form.add(emailLabel);
            emailField = new JTextField();
            emailField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            form.add(emailField);

            JLabel pwdLabel = new JLabel("Password:");
            pwdLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            form.add(pwdLabel);
            passwordField = new JPasswordField();
            passwordField.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
            form.add(passwordField);

            loginButton = new JButton("Login");
            loginButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            loginButton.setBackground(new java.awt.Color(0, 123, 255));
            loginButton.setForeground(java.awt.Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.addActionListener(e -> login());
            form.add(loginButton);

            registerButton = new JButton("Register as TA");
            registerButton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
            registerButton.setBackground(new java.awt.Color(108, 117, 125));
            registerButton.setForeground(java.awt.Color.WHITE);
            registerButton.setFocusPainted(false);
            registerButton.addActionListener(e -> registerTa());
            form.add(registerButton);

            statusLabel = new JLabel(" ");
            statusLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
            statusLabel.setForeground(new java.awt.Color(100, 100, 100));

            JPanel centerContainer = new JPanel(new BorderLayout());
            centerContainer.setOpaque(false);
            centerContainer.add(form, BorderLayout.CENTER);
            centerContainer.add(statusLabel, BorderLayout.SOUTH);
            centerWrapper.add(centerContainer);

            add(buildTopBar("BUPT International School TA Recruitment System", null), BorderLayout.NORTH);
            add(centerWrapper, BorderLayout.CENTER);
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
                String email = emailField.getText().trim();
                String password = new String(passwordField.getPassword());
                if (email.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both email and password.");
                    statusLabel.setText("Please fill in email and password.");
                    return;
                }
                User loginUser = authService.login(email, password);
                roleCombo.setSelectedItem(loginUser.getRole());
                statusLabel.setText("Login success.");
                onLoginSuccess(loginUser);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                String detail = ex.getClass().getSimpleName();
                if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
                    detail += ": " + ex.getMessage();
                }
                JOptionPane.showMessageDialog(frame, "Login error: " + detail);
                statusLabel.setText("Login error.");
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
                User ta = authService.registerTa(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passwordField.getPassword()));
                JOptionPane.showMessageDialog(frame, "TA registered: " + ta.getId());
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
    }

    private class TaPanel extends JPanel {
        private static final String TAB_DASHBOARD = "dashboard";
        private static final String TAB_JOB_BOARD = "jobBoard";
        private static final String TAB_PROFILE = "profile";
        private static final String TAB_NOTIFICATIONS = "notifications";

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel applicationModel;
        private final DefaultTableModel jobModel;
        private final DefaultTableModel notificationModel;
        private final JTable applicationTable;
        private final JTable jobTable;
        private final JTable notificationTable;
        private final JTextField searchField;
        private final JTextField skillsFilterField;
        private final JTextField hoursFilterField;
        private final JTextField moFilterField;
        private final JComboBox<String> statusFilterBox;
        private final JComboBox<String> notificationFilterBox;
        private final JLabel unreadCountLabel;
        private final JLabel notificationEmptyLabel;
        private final JLabel applicationSummaryLabel;
        private final JLabel dashboardNotificationLabel;
        private final JLabel dashboardActionLabel;
        private final JTextField profileNameField;
        private final JTextField profileYearField;
        private final JTextField profileProgrammeField;
        private final JTextArea profileSkillsArea;
        private final JTextField profileHoursField;
        private final JLabel cvLabel;

        private User user;
        private String selectedCvPath = "";
        private String selectedCvName = "";
        private final Set<String> readNotificationIds = new HashSet<>();

        private TaPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("TA Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("TA Dashboard", SwingApp.this::showLoginPage);
            add(topBar, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);

            String[] navLabels = {"Dashboard", "Job Board", "Notifications", "My Profile"};
            Runnable[] navActions = {
                () -> {
                    refreshApplications();
                    refreshNotifications();
                    contentLayout.show(contentPanel, TAB_DASHBOARD);
                },
                () -> {
                    refreshJobs();
                    contentLayout.show(contentPanel, TAB_JOB_BOARD);
                },
                () -> {
                    refreshNotifications();
                    contentLayout.show(contentPanel, TAB_NOTIFICATIONS);
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
            JPanel dashboardHeader = new JPanel(new GridLayout(0, 1, 4, 4));
            dashboardHeader.add(new JLabel("My Application Status"));
            applicationSummaryLabel = new JLabel("Applications: 0 pending, 0 accepted, 0 rejected, 0 withdrawn.");
            dashboardHeader.add(applicationSummaryLabel);
            dashboardNotificationLabel = new JLabel("No notifications yet.");
            dashboardHeader.add(dashboardNotificationLabel);
            dashboardActionLabel = new JLabel(" ");
            dashboardHeader.add(dashboardActionLabel);
            dashboardPanel.add(dashboardHeader, BorderLayout.NORTH);
            dashboardPanel.add(new JScrollPane(applicationTable), BorderLayout.CENTER);
            JPanel dashboardActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton withdrawButton = new JButton("Withdraw Selected");
            withdrawButton.addActionListener(e -> withdrawSelected());
            JButton refreshAppsButton = new JButton("Refresh");
            refreshAppsButton.addActionListener(e -> refreshApplications());
            JButton viewNotificationsButton = new JButton("View Notifications");
            viewNotificationsButton.addActionListener(e -> {
                refreshNotifications();
                contentLayout.show(contentPanel, TAB_NOTIFICATIONS);
            });
            dashboardActions.add(withdrawButton);
            dashboardActions.add(refreshAppsButton);
            dashboardActions.add(viewNotificationsButton);
            dashboardPanel.add(dashboardActions, BorderLayout.SOUTH);

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
            JPanel searchPanel = new JPanel(new GridLayout(0, 4, 10, 8));
            JPanel keywordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            keywordPanel.add(new JLabel("Search"));
            searchField = new JTextField(24);
            keywordPanel.add(searchField);
            JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            skillsPanel.add(new JLabel("Skill"));
            skillsFilterField = new JTextField(14);
            skillsPanel.add(skillsFilterField);
            JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            hoursPanel.add(new JLabel("Max Hours"));
            hoursFilterField = new JTextField(8);
            hoursPanel.add(hoursFilterField);
            JPanel moPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            moPanel.add(new JLabel("MO"));
            moFilterField = new JTextField(14);
            moPanel.add(moFilterField);
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusPanel.add(new JLabel("Status"));
            statusFilterBox = new JComboBox<>(new String[] {"OPEN", "ALL", "CLOSED", "FILLED"});
            statusPanel.add(statusFilterBox);
            JButton searchButton = new JButton("Apply Filter");
            searchButton.addActionListener(e -> refreshJobs());
            JButton clearButton = new JButton("Clear");
            clearButton.addActionListener(e -> {
                searchField.setText("");
                skillsFilterField.setText("");
                hoursFilterField.setText("");
                moFilterField.setText("");
                statusFilterBox.setSelectedItem("OPEN");
                refreshJobs();
            });
            JButton refreshJobsButton = new JButton("Refresh");
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
            jobBoardPanel.add(new JScrollPane(jobTable), BorderLayout.CENTER);
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton detailButton = new JButton("View Details");
            detailButton.addActionListener(e -> showJobDetails());
            JButton applyButton = new JButton("Apply Now");
            applyButton.addActionListener(e -> applySelectedJob());
            jobActions.add(detailButton);
            jobActions.add(applyButton);
            jobBoardPanel.add(jobActions, BorderLayout.SOUTH);

            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
            profileNameField = new JTextField();
            profileYearField = new JTextField();
            profileProgrammeField = new JTextField();
            profileSkillsArea = new JTextArea(4, 20);
            profileHoursField = new JTextField();
            cvLabel = new JLabel("No CV uploaded");

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
            JPanel cvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton cvButton = new JButton("Choose File");
            cvButton.addActionListener(e -> chooseCvFile());
            JButton viewCvButton = new JButton("View CV");
            viewCvButton.addActionListener(e -> viewMyCv());
            cvPanel.add(cvButton);
            cvPanel.add(viewCvButton);
            cvPanel.add(cvLabel);
            form.add(cvPanel);

            profilePanel.add(form, BorderLayout.CENTER);
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton saveButton = new JButton("Save Profile");
            saveButton.addActionListener(e -> saveProfile());
            profileActions.add(saveButton);
            profilePanel.add(profileActions, BorderLayout.SOUTH);

            notificationModel = new DefaultTableModel(
                    new Object[] {"ID", "Read", "Type", "Message", "Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            notificationTable = new JTable(notificationModel);
            notificationTable.getColumnModel().getColumn(0).setMinWidth(0);
            notificationTable.getColumnModel().getColumn(0).setMaxWidth(0);
            notificationTable.getColumnModel().getColumn(0).setPreferredWidth(0);
            JPanel notificationsPanel = new JPanel(new BorderLayout(10, 10));
            notificationsPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JPanel notificationTopPanel = new JPanel(new BorderLayout(10, 10));
            notificationTopPanel.add(new JLabel("Notification Center"), BorderLayout.WEST);
            JPanel notificationControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            unreadCountLabel = new JLabel("Unread: 0");
            notificationFilterBox = new JComboBox<>(new String[] {"All", "Unread", "Read"});
            notificationFilterBox.addActionListener(e -> refreshNotifications());
            JButton refreshNotificationsButton = new JButton("Refresh");
            refreshNotificationsButton.addActionListener(e -> refreshNotifications());
            JButton markReadButton = new JButton("Mark Read");
            markReadButton.addActionListener(e -> setSelectedNotificationRead(true));
            JButton markUnreadButton = new JButton("Mark Unread");
            markUnreadButton.addActionListener(e -> setSelectedNotificationRead(false));
            notificationControls.add(unreadCountLabel);
            notificationControls.add(new JLabel("Show"));
            notificationControls.add(notificationFilterBox);
            notificationControls.add(refreshNotificationsButton);
            notificationControls.add(markReadButton);
            notificationControls.add(markUnreadButton);
            notificationTopPanel.add(notificationControls, BorderLayout.EAST);
            notificationEmptyLabel = new JLabel("No notifications to show for this filter.");
            notificationsPanel.add(notificationTopPanel, BorderLayout.NORTH);
            notificationsPanel.add(new JScrollPane(notificationTable), BorderLayout.CENTER);
            notificationsPanel.add(notificationEmptyLabel, BorderLayout.SOUTH);

            contentPanel.add(dashboardPanel, TAB_DASHBOARD);
            contentPanel.add(jobBoardPanel, TAB_JOB_BOARD);
            contentPanel.add(notificationsPanel, TAB_NOTIFICATIONS);
            contentPanel.add(profilePanel, TAB_PROFILE);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            this.user = user;
            refreshApplications();
            refreshJobs();
            refreshNotifications();
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
            updateApplicationStatusSummary(applications);
            refreshNotifications();
        }

        private void updateApplicationStatusSummary(List<Application> applications) {
            ApplicationStatusSummary summary = TaNotificationUtil.summarizeApplications(applications);
            applicationSummaryLabel.setText(summary.format());
        }

        private void refreshNotifications() {
            notificationModel.setRowCount(0);
            List<NotificationEntry> notifications = buildTaNotifications();
            int unreadCount = TaNotificationUtil.countUnread(notifications, readNotificationIds);
            unreadCountLabel.setText("Unread: " + unreadCount);
            updateDashboardNotificationSummary(notifications, unreadCount);

            String filter = String.valueOf(notificationFilterBox.getSelectedItem());
            for (NotificationEntry notification :
                    TaNotificationUtil.filterByReadState(notifications, readNotificationIds, filter)) {
                boolean isRead = readNotificationIds.contains(notification.getId());
                notificationModel.addRow(new Object[] {
                    notification.getId(),
                    isRead ? "Read" : "Unread",
                    notification.getType(),
                    notification.getMessage(),
                    notification.getDate()
                });
            }
            notificationEmptyLabel.setVisible(notificationModel.getRowCount() == 0);
        }

        private List<NotificationEntry> buildTaNotifications() {
            return TaNotificationUtil.buildNotifications(
                    applicationService.getApplicationsByTaUserId(user.getId()),
                    jobService.getAllJobs());
        }

        private void updateDashboardNotificationSummary(List<NotificationEntry> notifications, int unreadCount) {
            if (notifications.isEmpty()) {
                dashboardNotificationLabel.setText("No notifications yet.");
                return;
            }
            NotificationEntry latest = notifications.get(0);
            dashboardNotificationLabel.setText(
                    "Notifications: " + unreadCount + " unread. Latest update: " + latest.getType() + ".");
        }

        private void setSelectedNotificationRead(boolean read) {
            int selected = notificationTable.getSelectedRow();
            if (selected < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a notification first.");
                return;
            }
            String notificationId = String.valueOf(notificationModel.getValueAt(selected, 0));
            if (read) {
                readNotificationIds.add(notificationId);
            } else {
                readNotificationIds.remove(notificationId);
            }
            refreshNotifications();
        }

        private void refreshJobs() {
            jobModel.setRowCount(0);
            String query = searchField.getText();
            String skillQuery = skillsFilterField.getText();
            String moQuery = moFilterField.getText();
            String statusValue = String.valueOf(statusFilterBox.getSelectedItem());
            Integer maxHours = parseHoursFilter();
            if (maxHours != null && maxHours < 0) {
                return;
            }
            for (Job job : jobService.getAllJobs()) {
                String moduleCode = safeText(job.getModuleCode());
                String moduleName = safeText(job.getModuleName());
                String moName = safeText(moNameForJob(job));
                if (!JobFilterUtil.matches(job, query, skillQuery, maxHours, moQuery, statusValue, moName)) {
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
                showDashboardActionFeedback("Application submitted. Notification center has been refreshed.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
                String closedMessage = TaNotificationUtil.jobClosedApplyMessage(findJobById(jobId).orElse(null));
                if (!closedMessage.isBlank()) {
                    showDashboardActionFeedback(closedMessage);
                    refreshNotifications();
                }
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
            String message = "Module: " + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()) + "\n"
                    + "MO: " + moNameForJob(job) + "\n"
                    + "Required Skills: " + safeText(job.getRequiredSkills()) + "\n"
                    + "Weekly Hours: " + job.getHoursPerWeek() + "\n"
                    + "Deadline: " + safeText(job.getDeadline()) + "\n\n"
                    + "Description: " + safeText(job.getDescription()) + "\n\n"
                    + "Match Score: " + score + "%\n"
                    + "Missing Skills: " + missingSkills;
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
            if (!application.get().getTaUserId().equalsIgnoreCase(user.getId())) {
                JOptionPane.showMessageDialog(frame, "You can only withdraw your own applications.");
                return;
            }
            applicationService.updateStatus(applicationId, user.getId(), ApplicationStatus.WITHDRAWN);
            refreshApplications();
            showDashboardActionFeedback("Application withdrawn. Notification center has been refreshed.");
        }

        private void showDashboardActionFeedback(String message) {
            dashboardActionLabel.setText(message);
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
                user.setYearOfStudy(ValidationUtil.parseIntInRange(profileYearField.getText(), "Year of study", 1, 12));
                user.setProgramme(profileProgrammeField.getText().trim());
                user.setSkills(profileSkillsArea.getText().trim());
                user.setAvailableHours(ValidationUtil.parseIntInRange(profileHoursField.getText(), "Available hours", 1, 168));
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
                try {
                    CvValidationUtil.validate(selectedFile.getName(), selectedFile.length());
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
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
            try {
                Path cvsDir;
                if (dataDirectory != null) {
                    cvsDir = dataDirectory.resolve("cvs");
                } else {
                    cvsDir = Path.of(System.getProperty("user.dir")).resolve("data").resolve("cvs");
                }
                Files.createDirectories(cvsDir);
                String ext = sourceFile.getName().contains(".")
                        ? sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'))
                        : "";
                String destName = userId + "_" + System.currentTimeMillis() + ext;
                Path destPath = cvsDir.resolve(destName);
                Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                return destPath.toAbsolutePath().toString();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Failed to save CV file: " + ex.getMessage());
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
            JButton viewProfileButton = new JButton("View Applicant Details");
            viewProfileButton.addActionListener(e -> viewSelectedApplicantProfile());
            JButton viewCvButton = new JButton("View Applicant CV");
            viewCvButton.addActionListener(e -> viewSelectedApplicantCv());
            JButton refreshApplicantsButton = new JButton("Refresh");
            refreshApplicantsButton.addActionListener(e -> refreshApplicants());
            applicantActions.add(acceptButton);
            applicantActions.add(rejectButton);
            applicantActions.add(viewProfileButton);
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
            try {
                applicationService.updateApplicationStatus(appId, user.getId(), status);
                refreshApplicants();
                refreshJobs();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
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

        private void viewSelectedApplicantProfile() {
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

            String cvDisplay = safeText(ta.getCvFilePath()).isBlank()
                    ? "Not uploaded"
                    : new File(ta.getCvFilePath()).getName();
            String message = "Name: " + safeText(ta.getName()) + "\n"
                    + "Year of Study: " + ta.getYearOfStudy() + "\n"
                    + "Programme: " + safeText(ta.getProgramme()) + "\n"
                    + "Skills: " + safeText(ta.getSkills()) + "\n"
                    + "Available Hours/Week: " + ta.getAvailableHours() + "\n"
                    + "CV: " + cvDisplay;
            JOptionPane.showMessageDialog(frame, message, "Applicant Details", JOptionPane.INFORMATION_MESSAGE);
        }

        private void loadProfile() {
            profileNameField.setText(user.getName());
            profileProgrammeField.setText(user.getProgramme());
            profileEmailField.setText(user.getEmail());
            profileHoursField.setText(String.valueOf(user.getAvailableHours()));
        }

        private void saveProfile() {
            try {
                user.setName(profileNameField.getText().trim());
                user.setProgramme(profileProgrammeField.getText().trim());
                user.setEmail(profileEmailField.getText().trim());
                user.setAvailableHours(ValidationUtil.parseIntInRange(profileHoursField.getText(), "Available hours", 1, 168));
                authService.updateUser(user);
                JOptionPane.showMessageDialog(frame, "Profile saved.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
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
