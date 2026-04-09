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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class GuiApp {
    private static final String CARD_AUTH = "AUTH";
    private static final String CARD_DASHBOARD = "DASHBOARD";

    private static final Color APP_BG = new Color(236, 236, 236);
    private static final Color TOP_BAR = new Color(210, 210, 210);
    private static final Color SIDEBAR = new Color(229, 229, 229);
    private static final Color SURFACE = new Color(242, 242, 242);
    private static final Color CARD = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(20, 122, 255);
    private static final Color SUCCESS = new Color(32, 145, 89);
    private static final Color DANGER = new Color(185, 53, 53);
    private static final Color TEXT_MAIN = new Color(48, 48, 54);
    private static final Color TEXT_SUB = new Color(100, 100, 108);
    private static final Color BORDER = new Color(196, 196, 196);
    private static final Color SIDEBAR_SELECTED = new Color(248, 251, 255);

    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserProfileService userProfileService;
    private final AdminService adminService;

    private JFrame frame;
    private JPanel rootPanel;
    private CardLayout cardLayout;
    private JPanel dashboardPanel;
    private User currentUser;

    public GuiApp(AuthService authService, JobService jobService, ApplicationService applicationService,
            UserProfileService userProfileService, AdminService adminService) {
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userProfileService = userProfileService;
        this.adminService = adminService;
    }

    public void start() {
        SwingUtilities.invokeLater(this::createAndShowUi);
    }

    private void createAndShowUi() {
        configureLookAndFeel();

        frame = new JFrame("BUPT TA Recruitment System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1180, 760));
        frame.setMinimumSize(new Dimension(1024, 680));
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);
        rootPanel.add(buildAuthPage(), CARD_AUTH);

        frame.setContentPane(rootPanel);
        frame.setVisible(true);
    }

    private JPanel buildAuthPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(APP_BG);
        page.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(SURFACE);
        shell.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(TOP_BAR);
        topBar.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel brand = new JLabel("BUPT International School TA Recruitment System");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 15));
        brand.setForeground(TEXT_MAIN);
        topBar.add(brand, BorderLayout.WEST);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel authCard = new JPanel(new BorderLayout(0, 14));
        authCard.setBackground(SURFACE);
        authCard.setBorder(new EmptyBorder(14, 14, 14, 14));
        authCard.setPreferredSize(new Dimension(380, 420));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Welcome back");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 38));
        JLabel subtitle = new JLabel("Use your account to continue");
        subtitle.setForeground(TEXT_SUB);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.addTab("Login", buildLoginTab());
        tabbedPane.addTab("Register TA", buildRegisterTab());

        authCard.add(titlePanel, BorderLayout.NORTH);
        authCard.add(tabbedPane, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(16, 16, 16, 16);
        gbc.anchor = GridBagConstraints.CENTER;
        center.add(authCard, gbc);

        shell.add(topBar, BorderLayout.NORTH);
        shell.add(center, BorderLayout.CENTER);
        page.add(shell, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JTextField identifierField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        styleTextField(identifierField);
        styleTextField(passwordField);

        JButton loginButton = createButton("Login", PRIMARY);
        JButton clearButton = createGhostButton("Clear");

        loginButton.addActionListener(e -> {
            String identifier = identifierField.getText();
            String password = new String(passwordField.getPassword());
            handleLogin(identifier, password);
        });

        clearButton.addActionListener(e -> {
            identifierField.setText("");
            passwordField.setText("");
        });

        int row = 0;
        addFormLabel(panel, "User ID or Email", row++);
        addFormField(panel, identifierField, row++);
        addFormLabel(panel, "Password", row++);
        addFormField(panel, passwordField, row++);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(loginButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(clearButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 14, 0);
        panel.add(actions, gbc);

        JLabel hint = new JLabel("Default: ADMIN001/admin123 | MO001/mo123456");
        hint.setForeground(TEXT_SUB);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel buildRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JTextField studentIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        styleTextField(studentIdField);
        styleTextField(nameField);
        styleTextField(emailField);
        styleTextField(passwordField);

        JButton registerButton = createButton("Create TA Account", PRIMARY);
        JButton clearButton = createGhostButton("Clear");

        registerButton.addActionListener(e -> {
            try {
                User newUser = authService.registerTa(
                        studentIdField.getText(),
                        nameField.getText(),
                        emailField.getText(),
                        new String(passwordField.getPassword()));
                showInfo("Registration succeeded. Your TA ID is: " + newUser.getId());
                studentIdField.setText("");
                nameField.setText("");
                emailField.setText("");
                passwordField.setText("");
            } catch (IllegalArgumentException ex) {
                showError("Registration failed: " + ex.getMessage());
            }
        });

        clearButton.addActionListener(e -> {
            studentIdField.setText("");
            nameField.setText("");
            emailField.setText("");
            passwordField.setText("");
        });

        int row = 0;
        addFormLabel(panel, "Student ID (9-12 digits)", row++);
        addFormField(panel, studentIdField, row++);
        addFormLabel(panel, "Name", row++);
        addFormField(panel, nameField, row++);
        addFormLabel(panel, "Email", row++);
        addFormField(panel, emailField, row++);
        addFormLabel(panel, "Password (min 8 chars)", row++);
        addFormField(panel, passwordField, row++);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(registerButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(clearButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);
        panel.add(actions, gbc);

        return panel;
    }

    private void handleLogin(String identifier, String password) {
        try {
            currentUser = authService.login(identifier, password);
            renderDashboard();
            cardLayout.show(rootPanel, CARD_DASHBOARD);
        } catch (IllegalArgumentException ex) {
            showError("Login failed: " + ex.getMessage());
        }
    }

    private void renderDashboard() {
        if (dashboardPanel != null) {
            rootPanel.remove(dashboardPanel);
        }

        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(APP_BG);
        dashboardPanel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(SURFACE);
        shell.setBorder(BorderFactory.createLineBorder(BORDER));
        shell.add(buildDashboardHeader(), BorderLayout.NORTH);
        shell.add(buildDashboardTabs(), BorderLayout.CENTER);

        dashboardPanel.add(shell, BorderLayout.CENTER);

        rootPanel.add(dashboardPanel, CARD_DASHBOARD);
        rootPanel.revalidate();
        rootPanel.repaint();
    }

    private JPanel buildDashboardHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TOP_BAR);
        header.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel title = new JLabel("BUPT TA Recruitment System    Welcome, " + currentUser.getName() + "!");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_MAIN);

        JLabel bellLabel = new JLabel("🔔");
        bellLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        bellLabel.setForeground(PRIMARY);
        bellLabel.setBorder(new EmptyBorder(0, 0, 0, 8));

        JButton logoutButton = createGhostButton("Logout");
        logoutButton.setForeground(PRIMARY);
        logoutButton.setBorder(new EmptyBorder(4, 8, 4, 8));
        logoutButton.setBackground(TOP_BAR);
        logoutButton.addActionListener(e -> {
            currentUser = null;
            cardLayout.show(rootPanel, CARD_AUTH);
        });

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.add(bellLabel);
        right.add(logoutButton);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildDashboardTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.setBackground(SIDEBAR);
        tabs.setOpaque(true);
        tabs.setBorder(new EmptyBorder(0, 0, 0, 0));
        tabs.setPreferredSize(new Dimension(210, 0));

        if (currentUser.getRole() == Role.TA) {
            buildTaTabs(tabs);
        } else if (currentUser.getRole() == Role.MO) {
            buildMoTabs(tabs);
        } else {
            buildAdminTabs(tabs);
        }
        styleSidebarTabs(tabs);
        return tabs;
    }

    private void buildTaTabs(JTabbedPane tabs) {
        DefaultTableModel jobsModel = createModel(
                "Job ID", "Module Code", "Module Name", "Hours/Week", "Positions", "Deadline", "Status", "MO");
        JTable jobsTable = createTable(jobsModel);
        Runnable refreshJobs = () -> fillJobs(jobsModel, jobService.getAllJobs());

        JButton refreshJobsButton = createGhostButton("Refresh Jobs");
        refreshJobsButton.addActionListener(e -> refreshJobs.run());
        JButton applyButton = createButton("Apply Selected Job", SUCCESS);
        applyButton.addActionListener(e -> {
            int selected = jobsTable.getSelectedRow();
            if (selected < 0) {
                showError("Please select a job first.");
                return;
            }
            String jobId = jobsModel.getValueAt(selected, 0).toString();
            try {
                applicationService.applyForJob(jobId, currentUser.getId());
                showInfo("Application submitted for " + jobId + ".");
                refreshJobs.run();
            } catch (IllegalArgumentException ex) {
                showError("Apply failed: " + ex.getMessage());
            }
        });

        JPanel jobsPanel = buildSection("Job Board", new JScrollPane(jobsTable), refreshJobsButton, applyButton);

        DefaultTableModel myAppsModel = createModel("Application ID", "Job ID", "Status", "Applied Date");
        JTable myAppsTable = createTable(myAppsModel);
        Runnable refreshApps = () -> {
            myAppsModel.setRowCount(0);
            List<Application> apps = applicationService.getApplicationsByTaUserId(currentUser.getId());
            for (Application app : apps) {
                myAppsModel.addRow(new Object[]{
                        app.getId(), app.getJobId(), app.getStatus(), app.getAppliedDate()
                });
            }
        };
        JButton refreshMyAppsButton = createGhostButton("Refresh Applications");
        refreshMyAppsButton.addActionListener(e -> refreshApps.run());
        JPanel appsPanel = buildSection("My Application Status", new JScrollPane(myAppsTable), refreshMyAppsButton);
        tabs.addTab("Dashboard", appsPanel);
        tabs.addTab("Job Board", jobsPanel);
        tabs.addTab("My Profile", buildTaProfilePanel());

        refreshJobs.run();
        refreshApps.run();
    }

    private JPanel buildTaProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(20, 22, 20, 22)));

        JTextField nameField = new JTextField(currentUser.getName());
        JTextField emailField = new JTextField(currentUser.getEmail());
        JTextField programmeField = new JTextField(valueOrEmpty(currentUser.getProgramme()));
        JTextField yearField = new JTextField(currentUser.getYearOfStudy() > 0 ? String.valueOf(currentUser.getYearOfStudy()) : "");
        JTextField skillsField = new JTextField(valueOrEmpty(currentUser.getSkills()));
        JTextField hoursField =
                new JTextField(currentUser.getAvailableHours() > 0 ? String.valueOf(currentUser.getAvailableHours()) : "");

        styleTextField(nameField);
        styleTextField(emailField);
        styleTextField(programmeField);
        styleTextField(yearField);
        styleTextField(skillsField);
        styleTextField(hoursField);

        nameField.setEnabled(false);
        emailField.setEnabled(false);

        int row = 0;
        addFormLabel(card, "Name", row++);
        addFormField(card, nameField, row++);
        addFormLabel(card, "Email", row++);
        addFormField(card, emailField, row++);
        addFormLabel(card, "Programme", row++);
        addFormField(card, programmeField, row++);
        addFormLabel(card, "Year of Study", row++);
        addFormField(card, yearField, row++);
        addFormLabel(card, "Skills", row++);
        addFormField(card, skillsField, row++);
        addFormLabel(card, "Available Hours/Week", row++);
        addFormField(card, hoursField, row++);

        JButton saveButton = createButton("Save Profile", PRIMARY);
        saveButton.addActionListener(e -> {
            try {
                int year = Integer.parseInt(yearField.getText().trim());
                int hours = Integer.parseInt(hoursField.getText().trim());
                User updated = userProfileService.updateTaProfile(
                        currentUser.getId(),
                        programmeField.getText(),
                        String.valueOf(year),
                        skillsField.getText(),
                        String.valueOf(hours));
                currentUser = updated;
                showInfo("Profile updated.");
            } catch (NumberFormatException ex) {
                showError("Year of study and available hours must be numbers.");
            } catch (IllegalArgumentException ex) {
                showError("Profile update failed: " + ex.getMessage());
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);
        card.add(saveButton, gbc);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private void buildMoTabs(JTabbedPane tabs) {
        DefaultTableModel myJobsModel = createModel(
                "Job ID", "Module Code", "Module Name", "Hours/Week", "Positions", "Deadline", "Status");
        JTable myJobsTable = createTable(myJobsModel);
        Runnable refreshMyJobs = () -> fillJobs(myJobsModel, jobService.getJobsByMoId(currentUser.getId()));
        JButton refreshMyJobsButton = createGhostButton("Refresh My Jobs");
        refreshMyJobsButton.addActionListener(e -> refreshMyJobs.run());
        JPanel myJobsPanel = buildSection("My Posted Jobs Overview", new JScrollPane(myJobsTable), refreshMyJobsButton);

        DefaultTableModel appsModel = createModel("Application ID", "Job ID", "TA User ID", "Status", "Applied Date");
        JTable appsTable = createTable(appsModel);
        Runnable refreshApps = () -> {
            appsModel.setRowCount(0);
            List<Application> apps = applicationService.getApplicationsForMo(currentUser.getId());
            for (Application app : apps) {
                appsModel.addRow(new Object[]{
                        app.getId(), app.getJobId(), app.getTaUserId(), app.getStatus(), app.getAppliedDate()
                });
            }
        };
        JButton refreshAppsButton = createGhostButton("Refresh Applications");
        refreshAppsButton.addActionListener(e -> refreshApps.run());

        JButton acceptButton = createButton("Accept Selected", SUCCESS);
        acceptButton.addActionListener(e -> reviewSelectedApplication(appsModel, appsTable, ApplicationStatus.ACCEPTED, refreshApps));
        JButton rejectButton = createButton("Reject Selected", DANGER);
        rejectButton.addActionListener(e -> reviewSelectedApplication(appsModel, appsTable, ApplicationStatus.REJECTED, refreshApps));

        JPanel applicationsPanel = buildSection(
                "Applicants List & Review",
                new JScrollPane(appsTable),
                refreshAppsButton,
                acceptButton,
                rejectButton);

        DefaultTableModel allJobsModel = createModel(
                "Job ID", "Module Code", "Module Name", "Hours/Week", "Positions", "Deadline", "Status", "MO");
        JTable allJobsTable = createTable(allJobsModel);
        Runnable refreshAllJobs = () -> fillJobs(allJobsModel, jobService.getAllJobs());
        JButton refreshAllJobsButton = createGhostButton("Refresh All Jobs");
        refreshAllJobsButton.addActionListener(e -> refreshAllJobs.run());
        JPanel allJobsPanel = buildSection("All Jobs in System", new JScrollPane(allJobsTable), refreshAllJobsButton);

        tabs.addTab("Dashboard", myJobsPanel);
        tabs.addTab("Applicants List", applicationsPanel);
        tabs.addTab("Post Job", buildMoPostJobPanel());
        tabs.addTab("All Jobs", allJobsPanel);

        refreshMyJobs.run();
        refreshApps.run();
        refreshAllJobs.run();
    }

    private JPanel buildMoPostJobPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(20, 22, 20, 22)));

        JTextField moduleCodeField = new JTextField();
        JTextField moduleNameField = new JTextField();
        JTextArea descriptionArea = new JTextArea(4, 20);
        JTextField requiredSkillsField = new JTextField();
        JTextField hoursField = new JTextField();
        JTextField positionsField = new JTextField();
        JTextField deadlineField = new JTextField();

        styleTextField(moduleCodeField);
        styleTextField(moduleNameField);
        styleTextField(requiredSkillsField);
        styleTextField(hoursField);
        styleTextField(positionsField);
        styleTextField(deadlineField);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 10, 8, 10)));

        int row = 0;
        addFormLabel(card, "Module Code", row++);
        addFormField(card, moduleCodeField, row++);
        addFormLabel(card, "Module Name", row++);
        addFormField(card, moduleNameField, row++);
        addFormLabel(card, "Description", row++);
        addFormField(card, new JScrollPane(descriptionArea), row++);
        addFormLabel(card, "Required Skills", row++);
        addFormField(card, requiredSkillsField, row++);
        addFormLabel(card, "Hours per Week", row++);
        addFormField(card, hoursField, row++);
        addFormLabel(card, "Positions", row++);
        addFormField(card, positionsField, row++);
        addFormLabel(card, "Deadline (YYYY-MM-DD)", row++);
        addFormField(card, deadlineField, row++);

        JButton createButton = createButton("Post Job", PRIMARY);
        createButton.addActionListener(e -> {
            try {
                int hours = Integer.parseInt(hoursField.getText().trim());
                int positions = Integer.parseInt(positionsField.getText().trim());
                Job job = jobService.createJob(
                        moduleCodeField.getText(),
                        moduleNameField.getText(),
                        descriptionArea.getText(),
                        requiredSkillsField.getText(),
                        String.valueOf(hours),
                        String.valueOf(positions),
                        deadlineField.getText(),
                        currentUser.getId());
                showInfo("Job created: " + job.getId());
                moduleCodeField.setText("");
                moduleNameField.setText("");
                descriptionArea.setText("");
                requiredSkillsField.setText("");
                hoursField.setText("");
                positionsField.setText("");
                deadlineField.setText("");
            } catch (NumberFormatException ex) {
                showError("Hours and positions must be numbers.");
            } catch (IllegalArgumentException ex) {
                showError("Job creation failed: " + ex.getMessage());
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(16, 0, 0, 0);
        card.add(createButton, gbc);

        panel.add(card, BorderLayout.NORTH);
        return panel;
    }

    private void reviewSelectedApplication(
            DefaultTableModel model, JTable table, ApplicationStatus status, Runnable refreshCallback) {
        int selected = table.getSelectedRow();
        if (selected < 0) {
            showError("Please select an application first.");
            return;
        }
        String appId = model.getValueAt(selected, 0).toString();
        try {
            applicationService.updateApplicationStatus(appId, currentUser.getId(), status);
            showInfo("Application updated: " + appId + " -> " + status);
            refreshCallback.run();
        } catch (IllegalArgumentException ex) {
            showError("Review failed: " + ex.getMessage());
        }
    }

    private void buildAdminTabs(JTabbedPane tabs) {
        tabs.addTab("Workload Overview", buildAdminSummaryPanel());

        DefaultTableModel workloadsModel = createModel(
                "TA User ID", "TA Name", "Available h/w", "Assigned h/w", "Remaining h/w",
                "Accepted Jobs", "Overloaded", "Accepted Job Details");
        JTable workloadsTable = createTable(workloadsModel);
        Runnable refreshWorkloads = () -> {
            workloadsModel.setRowCount(0);
            List<AdminService.TAWorkloadSummary> workloads = adminService.getAllTAWorkloads();
            for (AdminService.TAWorkloadSummary workload : workloads) {
                workloadsModel.addRow(new Object[]{
                        workload.getTaUserId(),
                        workload.getTaName(),
                        workload.getAvailableHours(),
                        workload.getTotalAssignedHours(),
                        workload.getRemainingHours(),
                        workload.getAcceptedJobCount(),
                        workload.isOverloaded() ? "YES" : "NO",
                        String.join(" | ", workload.getAcceptedJobDescriptions())
                });
            }
        };

        JButton refreshWorkloadsButton = createGhostButton("Refresh Workloads");
        refreshWorkloadsButton.addActionListener(e -> refreshWorkloads.run());
        tabs.addTab("Workload Details",
                buildSection("All TA Workloads", new JScrollPane(workloadsTable), refreshWorkloadsButton));

        tabs.addTab("Jobs Overview", buildAdminJobsPanel());
        tabs.addTab("Specific TA", buildAdminSpecificTaPanel());

        DefaultTableModel allTAsModel = createModel("TA ID", "Name", "Email", "Programme", "Year", "Hours/Week");
        JTable allTAsTable = createTable(allTAsModel);
        Runnable refreshTAs = () -> {
            allTAsModel.setRowCount(0);
            List<User> tas = adminService.getAllTAs();
            for (User ta : tas) {
                allTAsModel.addRow(new Object[]{
                        ta.getId(),
                        ta.getName(),
                        ta.getEmail(),
                        display(ta.getProgramme()),
                        ta.getYearOfStudy() > 0 ? ta.getYearOfStudy() : "N/A",
                        ta.getAvailableHours()
                });
            }
        };
        JButton refreshTAsButton = createGhostButton("Refresh TA List");
        refreshTAsButton.addActionListener(e -> refreshTAs.run());
        tabs.addTab("Manage Accounts", buildSection("All TA Accounts", new JScrollPane(allTAsTable), refreshTAsButton));

        refreshWorkloads.run();
        refreshTAs.run();
    }

    private JPanel buildAdminSummaryPanel() {
        JTextArea summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        summaryArea.setBackground(CARD);
        summaryArea.setForeground(TEXT_MAIN);
        summaryArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        Runnable refreshSummary = () -> summaryArea.setText(adminService.getRecruitmentSummary());
        JButton refreshButton = createGhostButton("Refresh Summary");
        refreshButton.addActionListener(e -> refreshSummary.run());

        JPanel panel = buildSection("Recruitment Overview", new JScrollPane(summaryArea), refreshButton);
        refreshSummary.run();
        return panel;
    }

    private JPanel buildAdminJobsPanel() {
        DefaultTableModel jobsModel = createModel(
                "Job ID", "Module Code", "Module Name", "MO", "Positions", "Status", "Deadline");
        JTable jobsTable = createTable(jobsModel);

        Runnable refreshJobs = () -> {
            jobsModel.setRowCount(0);
            List<Job> jobs = jobService.getAllJobs();
            for (Job job : jobs) {
                jobsModel.addRow(new Object[]{
                        job.getId(),
                        job.getModuleCode(),
                        job.getModuleName(),
                        job.getPostedByMoId(),
                        job.getPositions(),
                        job.getStatus(),
                        job.getDeadline()
                });
            }
        };

        JButton refreshButton = createGhostButton("Refresh Jobs");
        refreshButton.addActionListener(e -> refreshJobs.run());
        JPanel panel = buildSection("Global Jobs Overview", new JScrollPane(jobsTable), refreshButton);
        refreshJobs.run();
        return panel;
    }

    private JPanel buildAdminSpecificTaPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SURFACE);

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBackground(SURFACE);

        JTextField taIdField = new JTextField();
        styleTextField(taIdField);
        JButton loadButton = createButton("Load Workload", PRIMARY);

        top.add(taIdField, BorderLayout.CENTER);
        top.add(loadButton, BorderLayout.EAST);

        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        detailArea.setBackground(CARD);
        detailArea.setForeground(TEXT_MAIN);
        detailArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        loadButton.addActionListener(e -> {
            try {
                AdminService.TAWorkloadSummary w = adminService.getTAWorkload(taIdField.getText());
                StringBuilder sb = new StringBuilder();
                sb.append("TA ID: ").append(w.getTaUserId()).append(System.lineSeparator());
                sb.append("Name: ").append(w.getTaName()).append(System.lineSeparator());
                sb.append("Available Hours: ").append(w.getAvailableHours()).append(" h/week").append(System.lineSeparator());
                sb.append("Assigned Hours: ").append(w.getTotalAssignedHours()).append(" h/week").append(System.lineSeparator());
                sb.append("Remaining Hours: ").append(w.getRemainingHours()).append(" h/week").append(System.lineSeparator());
                sb.append("Accepted Positions: ").append(w.getAcceptedJobCount()).append(System.lineSeparator());
                sb.append("Overloaded: ").append(w.isOverloaded() ? "YES" : "NO").append(System.lineSeparator());
                if (!w.getAcceptedJobDescriptions().isEmpty()) {
                    sb.append(System.lineSeparator()).append("Accepted Jobs:").append(System.lineSeparator());
                    for (String desc : w.getAcceptedJobDescriptions()) {
                        sb.append(" - ").append(desc).append(System.lineSeparator());
                    }
                }
                detailArea.setText(sb.toString());
            } catch (IllegalArgumentException ex) {
                showError("Load failed: " + ex.getMessage());
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(detailArea), BorderLayout.CENTER);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private void styleSidebarTabs(JTabbedPane tabs) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setTabComponentAt(i, createSidebarTab(tabs.getTitleAt(i)));
        }
        ChangeListener listener = e -> refreshSidebarTabStyles(tabs);
        tabs.addChangeListener(listener);
        refreshSidebarTabStyles(tabs);
    }

    private JPanel createSidebarTab(String title) {
        JPanel tab = new JPanel(new BorderLayout());
        tab.setOpaque(true);
        tab.setBorder(new MatteBorder(0, 4, 0, 0, SIDEBAR));
        tab.setPreferredSize(new Dimension(180, 42));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(TEXT_MAIN);
        label.setBorder(new EmptyBorder(0, 10, 0, 8));

        tab.add(label, BorderLayout.CENTER);
        return tab;
    }

    private void refreshSidebarTabStyles(JTabbedPane tabs) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component component = tabs.getTabComponentAt(i);
            if (!(component instanceof JPanel panel)) {
                continue;
            }
            boolean selected = i == tabs.getSelectedIndex();
            panel.setBackground(selected ? SIDEBAR_SELECTED : SIDEBAR);
            panel.setBorder(new MatteBorder(0, 4, 0, 0, selected ? PRIMARY : SIDEBAR));
            if (panel.getComponentCount() > 0 && panel.getComponent(0) instanceof JLabel label) {
                label.setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 15));
                label.setForeground(selected ? new Color(25, 25, 30) : TEXT_MAIN);
            }
        }
    }

    private JPanel buildSection(String title, JComponent mainContent, JComponent... actions) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(SURFACE);
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(TEXT_MAIN);

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        for (int i = 0; i < actions.length; i++) {
            actionPanel.add(actions[i]);
            if (i < actions.length - 1) {
                actionPanel.add(Box.createHorizontalStrut(8));
            }
        }

        head.add(label, BorderLayout.WEST);
        head.add(actionPanel, BorderLayout.EAST);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 8, 8, 8)));
        card.add(mainContent, BorderLayout.CENTER);

        panel.add(head, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void fillJobs(DefaultTableModel model, List<Job> jobs) {
        model.setRowCount(0);
        for (Job job : jobs) {
            if (model.getColumnCount() == 7) {
                model.addRow(new Object[]{
                        job.getId(),
                        job.getModuleCode(),
                        job.getModuleName(),
                        job.getHoursPerWeek(),
                        job.getPositions(),
                        job.getDeadline(),
                        job.getStatus()
                });
            } else {
                model.addRow(new Object[]{
                        job.getId(),
                        job.getModuleCode(),
                        job.getModuleName(),
                        job.getHoursPerWeek(),
                        job.getPositions(),
                        job.getDeadline(),
                        job.getStatus(),
                        job.getPostedByMoId()
                });
            }
        }
    }

    private void addFormLabel(JPanel panel, String text, int row) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_MAIN);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 4, 0);
        panel.add(label, gbc);
    }

    private void addFormField(JPanel panel, JComponent component, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);
        panel.add(component, gbc);
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(100, 36));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)));
        field.setBackground(CARD);
        field.setForeground(TEXT_MAIN);
    }

    private DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(223, 223, 223));
        table.getTableHeader().setForeground(TEXT_MAIN);
        table.setGridColor(BORDER);
        table.setBackground(CARD);
        table.setSelectionBackground(new Color(211, 229, 255));
        table.setSelectionForeground(TEXT_MAIN);
        applyStatusBadgeRenderers(table);
        return table;
    }

    private void applyStatusBadgeRenderers(JTable table) {
        DefaultTableCellRenderer renderer = new StatusBadgeRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            String name = table.getColumnName(i);
            if ("Status".equalsIgnoreCase(name)
                    || "Overloaded".equalsIgnoreCase(name)
                    || "Account Status".equalsIgnoreCase(name)) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }
        }
    }

    private JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker()),
                new EmptyBorder(8, 14, 8, 14)));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(CARD);
        button.setForeground(TEXT_MAIN);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(8, 12, 8, 12)));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default look and feel.
        }
        UIManager.put("TabbedPane.selected", CARD);
        UIManager.put("TabbedPane.contentAreaColor", SURFACE);
        UIManager.put("TabbedPane.background", SIDEBAR);
        UIManager.put("TabbedPane.foreground", TEXT_MAIN);
        UIManager.put("TabbedPane.focus", SURFACE);
        UIManager.put("TabbedPane.borderHightlightColor", BORDER);
        UIManager.put("TabbedPane.darkShadow", BORDER);
        UIManager.put("TabbedPane.light", BORDER);
        UIManager.put("TabbedPane.selectHighlight", BORDER);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String display(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value;
    }

    private static final class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, false, hasFocus, row, column);
            label.setHorizontalAlignment(CENTER);
            label.setBorder(new EmptyBorder(2, 6, 2, 6));
            label.setFont(new Font("Segoe UI", Font.BOLD, 12));
            label.setOpaque(true);

            String text = value == null ? "" : value.toString().trim().toUpperCase();
            Color bg = new Color(235, 235, 235);
            Color fg = new Color(70, 70, 70);

            if ("OPEN".equals(text) || "ACCEPTED".equals(text) || "ACTIVE".equals(text)) {
                bg = new Color(215, 242, 224);
                fg = new Color(25, 120, 70);
            } else if ("FILLED".equals(text) || "PENDING".equals(text)) {
                bg = new Color(220, 235, 255);
                fg = new Color(23, 83, 160);
            } else if ("CLOSED".equals(text) || "REJECTED".equals(text)
                    || "WITHDRAWN".equals(text) || "DEACTIVATED".equals(text) || "YES".equals(text)) {
                bg = new Color(255, 225, 225);
                fg = new Color(160, 45, 45);
            } else if ("NO".equals(text)) {
                bg = new Color(230, 236, 245);
                fg = new Color(75, 85, 100);
            }

            label.setBackground(bg);
            label.setForeground(fg);
            return label;
        }
    }

}
