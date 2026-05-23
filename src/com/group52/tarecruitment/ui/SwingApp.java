package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Notification;
import com.group52.tarecruitment.model.NotificationType;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
import com.group52.tarecruitment.service.WorkloadBalancerService;
import com.group52.tarecruitment.service.AiMatchingService;
import com.group52.tarecruitment.service.AiMatchingServiceAdapter;
import com.group52.tarecruitment.service.AuthService;
import com.group52.tarecruitment.service.JobService;
import com.group52.tarecruitment.service.MoApplicantRankingService;
import com.group52.tarecruitment.service.NotificationService;
import com.group52.tarecruitment.util.CvValidationUtil;
import com.group52.tarecruitment.util.JobFilterUtil;
import com.group52.tarecruitment.util.TaNotificationUtil;
import com.group52.tarecruitment.util.TaNotificationUtil.ApplicationStatusSummary;
import com.group52.tarecruitment.util.TaNotificationUtil.NotificationEntry;
import com.group52.tarecruitment.util.ValidationUtil;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.SpinnerNumberModel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class SwingApp {
    private static final String PAGE_LOGIN = "login";
    private static final String PAGE_REGISTER = "register";
    private static final String PAGE_TA = "ta";
    private static final String PAGE_MO = "mo";
    private static final String PAGE_ADMIN = "admin";
    private static final String BRAND_TAGLINE = "BUPT x QMUL TA Recruitment";
    private static final int DEFAULT_MO_MATCH_THRESHOLD = MoApplicantRankingService.DEFAULT_MINIMUM_MATCH_SCORE;
    private static final Color QMUL_PURPLE = new Color(75, 46, 131);
    private static final Color QMUL_PURPLE_DARK = new Color(58, 31, 107);
    private static final Color QMUL_PURPLE_LIGHT = new Color(123, 92, 240);
    private static final Color PRIMARY_BUTTON_COLOR = QMUL_PURPLE;
    private static final Color SECONDARY_BUTTON_COLOR = QMUL_PURPLE_DARK;
    private static final Color DANGER_BUTTON_COLOR = new Color(205, 67, 76);
    private static final Color SUCCESS_COLOR = new Color(28, 161, 96);
    private static final Color WARNING_COLOR = new Color(246, 173, 54);
    private static final Color MUTED_TEXT_COLOR = new Color(108, 117, 125);
    private static final Color SURFACE_BG = new Color(246, 247, 251);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color CARD_BORDER = new Color(229, 231, 235);
    private static final Color HEADER_TEXT = new Color(31, 41, 55);
    private static final Color TABLE_HEADER_BG = new Color(243, 244, 246);
    private static final Color TABLE_HEADER_TEXT = new Color(75, 85, 99);
    private static final Color BADGE_PURPLE = new Color(123, 92, 240);
    private static final Color BADGE_GREEN = new Color(28, 161, 96);
    private static final Color BADGE_RED = new Color(205, 67, 76);
    private static final Color BADGE_ORANGE = new Color(246, 173, 54);
    private static final Color SIDEBAR_BG = new Color(58, 31, 107);
    private static final Color SIDEBAR_BG_DARK = new Color(50, 24, 93);
    private static final Color SIDEBAR_BUTTON_BG = new Color(90, 46, 156);
    private static final Color SIDEBAR_BUTTON_ACTIVE_BG = new Color(123, 92, 240);
    private static final Color SIDEBAR_BUTTON_HOVER_BG = new Color(107, 73, 214);
    private static final String DEFAULT_AVATAR_PATH = "data/default-avatar.png";
    private static final String AVATAR_DIR = "data/avatars";
    private static final String[] AVATAR_FILES = {"touxiang1.jpg", "touxiang2.jpg", "touxiang3.jpg", "touxiang4.jpg"};
    private static final String[] PRESET_AVATAR_FILES = {
        "touxiang1.jpg",
        "touxiang2.jpg",
        "touxiang3.jpg",
        "touxiang4.jpg"
    };
    private static final String ICON_DIR = "data/icons";
    private static final String ICON_DASHBOARD = "dashboard.png";
    private static final String ICON_JOB = "job.png";
    private static final String ICON_NOTIFICATION = "notification.png";
    private static final String ICON_PROFILE = "my.png";
    private static final String ICON_APPLICANT = "applicant.png";
    private static final String ICON_WORKLOAD = "workload.png";
    private static final String ICON_ACCOUNTS = "accounts.png";
    private static final String ICON_APPLICATIONS = "applications.png";
    private static final String ICON_AUDIT = "audit.png";

    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final AiMatchingService aiMatchingService;
    private final MoApplicantRankingService moApplicantRankingService;
    private final AdminService adminService;
    private final NotificationService notificationService;
    private final Path dataDirectory;
    private com.group52.tarecruitment.service.ExportService exportService;
    private AutoDemoController autoDemoController;

    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private JLabel topBarAvatarLabel;
    private String topBarAvatarPath = "";
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private TaPanel taPanel;
    private MoPanel moPanel;
    private AdminPanel adminPanel;
    private JTextArea recommendationArea;
    private JLabel demoCaptionLabel;

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService) {
        this(authService, jobService, applicationService, null, null, null);
    }

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService, Path dataDirectory) {
        this(authService, jobService, applicationService, dataDirectory, null, null);
    }

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService,
                    Path dataDirectory, AdminService adminService) {
        this(authService, jobService, applicationService, dataDirectory, adminService, null);
    }

    public SwingApp(AuthService authService, JobService jobService, ApplicationService applicationService,
                    Path dataDirectory, AdminService adminService, NotificationService notificationService) {
        this.authService = authService;
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.aiMatchingService = new AiMatchingService();
        this.moApplicantRankingService = new MoApplicantRankingService(
                applicationService, new AiMatchingServiceAdapter(this.aiMatchingService));
        this.dataDirectory = dataDirectory;
        this.adminService = adminService;
        this.notificationService = notificationService;
    }

    /**
     * Inject the export service so that the auto-demo (and any future
     * UI-side export buttons) can run. Must be called before {@link #start()}.
     */
    public void setExportService(com.group52.tarecruitment.service.ExportService exportService) {
        this.exportService = exportService;
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
        rootPanel.setBackground(SURFACE_BG);

        loginPanel = new LoginPanel();
        registerPanel = new RegisterPanel();
        taPanel = new TaPanel();
        moPanel = new MoPanel();
        adminPanel = new AdminPanel();

        rootPanel.add(loginPanel, PAGE_LOGIN);
        rootPanel.add(registerPanel, PAGE_REGISTER);
        rootPanel.add(taPanel, PAGE_TA);
        rootPanel.add(moPanel, PAGE_MO);
        rootPanel.add(adminPanel, PAGE_ADMIN);

        frame.setContentPane(rootPanel);

        if (exportService != null && dataDirectory != null && adminService != null) {
            autoDemoController = new AutoDemoController(this, authService, jobService,
                    applicationService, adminService, exportService, dataDirectory);
            loginPanel.installDemoButton(autoDemoController);
        }

        showLoginPage();
        frame.setVisible(true);
    }

    private void showLoginPage() {
        loginPanel.reset();
        showPage(PAGE_LOGIN);
    }

    private void onLoginSuccess(User user) {
        updateTopBarAvatar(user.getAvatarFilePath());
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

    private void updateTopBarAvatar(String avatarPath) {
        topBarAvatarPath = avatarPath == null ? "" : avatarPath;
        if (topBarAvatarLabel != null) {
            topBarAvatarLabel.setIcon(loadAvatarIcon(topBarAvatarPath, 30));
        }
    }

    private JPanel buildTopBar(String title, Runnable logoutAction) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(true);
        bar.setBackground(QMUL_PURPLE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 28)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel rightArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightArea.setOpaque(false);
        if (logoutAction != null) {
            JButton logoutButton = new JButton("Logout");
            logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
            logoutButton.setForeground(Color.WHITE);
            logoutButton.setBackground(new Color(103, 78, 150));
            logoutButton.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            logoutButton.setFocusPainted(false);
            logoutButton.setOpaque(false);
            logoutButton.setContentAreaFilled(false);
            logoutButton.setBorderPainted(false);
            
            Color normalLogoutBg = new Color(103, 78, 150);
            Color hoverLogoutBg = new Color(117, 95, 160);
            logoutButton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
                @Override
                public void paint(Graphics g, JComponent c) {
                    javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(b.getModel().isRollover() ? hoverLogoutBg : normalLogoutBg);
                    g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 6, 6);
                    g2.dispose();
                    super.paint(g, c);
                }
            });
            logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            logoutButton.addActionListener(e -> logoutAction.run());
            rightArea.add(logoutButton);
        }
        bar.add(rightArea, BorderLayout.EAST);
        return bar;
    }

    private String shortFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return new File(path).getName();
    }

    private JPanel buildNavigationPanel(String[] labels, Runnable[] actions) {
        JPanel nav = new JPanel();
        nav.setBackground(SIDEBAR_BG_DARK);
        nav.setBorder(BorderFactory.createEmptyBorder(20, 16, 20, 16));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setPreferredSize(new Dimension(246, 0));

        JPanel brandPanel = new JPanel();
        brandPanel.setOpaque(false);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        JLabel systemTitle = new JLabel("QMUL TA Recruitment System");
        systemTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        systemTitle.setForeground(Color.WHITE);
        JLabel systemSubTitle = new JLabel(BRAND_TAGLINE);
        systemSubTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        systemSubTitle.setForeground(new Color(232, 220, 255));
        brandPanel.add(systemTitle);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(systemSubTitle);
        brandPanel.add(Box.createVerticalStrut(12));
        JLabel divider = new JLabel(" ");
        divider.setOpaque(true);
        divider.setBackground(new Color(255, 255, 255, 40));
        divider.setMaximumSize(new Dimension(214, 1));
        divider.setPreferredSize(new Dimension(214, 1));
        brandPanel.add(divider);
        nav.add(brandPanel);
        nav.add(Box.createVerticalStrut(20));

        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JLabel menuLabel = new JLabel("Menu");
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        menuLabel.setForeground(new Color(230, 219, 255));
        menuLabel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        menuPanel.add(menuLabel);
        menuPanel.add(Box.createVerticalStrut(10));

        List<JButton> navButtons = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            final int actionIndex = i;
            JButton button = createSidebarButton(labels[i], () -> {
                for (int j = 0; j < navButtons.size(); j++) {
                    setSidebarButtonState(navButtons.get(j), j == actionIndex);
                }
                actions[actionIndex].run();
            });
            navButtons.add(button);
            menuPanel.add(button);
            menuPanel.add(Box.createVerticalStrut(10));
        }
        if (!navButtons.isEmpty()) {
            setSidebarButtonState(navButtons.get(0), true);
        }
        nav.add(menuPanel);
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    private JButton createSidebarButton(String label, Runnable action) {
        JButton button = new JButton(iconTextForLabel(label));
        ImageIcon navIcon = getNavIcon(label);
        if (navIcon != null) {
            button.setIcon(navIcon);
            button.setHorizontalTextPosition(JButton.RIGHT);
            button.setIconTextGap(10);
        }
        button.setHorizontalAlignment(JButton.LEFT);
        button.setMaximumSize(new Dimension(214, 46));
        button.setPreferredSize(new Dimension(214, 46));
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_BUTTON_BG);
        button.putClientProperty("sidebar.active", Boolean.FALSE);
        button.addActionListener(e -> action.run());
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("sidebar.active"))) {
                    button.setBackground(SIDEBAR_BUTTON_HOVER_BG);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!Boolean.TRUE.equals(button.getClientProperty("sidebar.active"))) {
                    button.setBackground(SIDEBAR_BUTTON_BG);
                }
            }
        });
        return button;
    }

    private void setSidebarButtonState(JButton button, boolean active) {
        if (button == null) {
            return;
        }
        button.putClientProperty("sidebar.active", active);
        button.setBackground(active ? SIDEBAR_BUTTON_ACTIVE_BG : SIDEBAR_BUTTON_BG);
        button.setForeground(Color.WHITE);
    }

    private String navIconFor(String label) {
        String key = label == null ? "" : label.toLowerCase();
        if (key.contains("dashboard")) {
            return "⌂";
        }
        if (key.contains("job")) {
            return "▣";
        }
        if (key.contains("notification")) {
            return "◉";
        }
        if (key.contains("profile") || key.contains("my")) {
            return "◌";
        }
        if (key.contains("applicant")) {
            return "☰";
        }
        if (key.contains("workload")) {
            return "◍";
        }
        if (key.contains("account")) {
            return "◎";
        }
        return "•";
    }


    private ImageIcon loadNavIcon(String iconName) {
        File iconFile = new File(ICON_DIR, iconName);
        if (iconFile.exists()) {
            return new ImageIcon(iconFile.getAbsolutePath());
        }
        return null;
    }

    private String iconTextForLabel(String label) {
        String key = label == null ? "" : label.toLowerCase();
        if (key.contains("dashboard")) {
            return "Dashboard";
        }
        if (key.contains("job")) {
            return "Job Board";
        }
        if (key.contains("notification")) {
            return "Notifications";
        }
        if (key.contains("my") || key.contains("profile")) {
            return "My Profile";
        }
        if (key.contains("applicant")) {
            return "Applicants";
        }
        if (key.contains("workload")) {
            return "Workload";
        }
        if (key.contains("account")) {
            return "Accounts";
        }
        return label == null ? "" : label;
    }

    private ImageIcon getNavIcon(String label) {
        String key = label == null ? "" : label.toLowerCase();
        if (key.contains("dashboard")) {
            return loadNavIcon(ICON_DASHBOARD);
        }
        if (key.contains("job")) {
            return loadNavIcon(ICON_JOB);
        }
        if (key.contains("notification")) {
            return loadNavIcon(ICON_NOTIFICATION);
        }
        if (key.contains("my") || key.contains("profile")) {
            return loadNavIcon(ICON_PROFILE);
        }
        if (key.contains("applicant")) {
            return loadNavIcon(ICON_APPLICANT);
        }
        if (key.contains("workload")) {
            return loadNavIcon(ICON_WORKLOAD);
        }
        if (key.contains("account")) {
            return loadNavIcon(ICON_ACCOUNTS);
        }
        if (key.contains("application")) {
            return loadNavIcon(ICON_APPLICATIONS);
        }
        if (key.contains("audit")) {
            return loadNavIcon(ICON_AUDIT);
        }
        return null;
    }

    private String notificationIcon(String type) {
        if (type == null) {
            return "◈";
        }
        String key = type.toLowerCase();
        if (key.contains("submitted")) {
            return "✉";
        }
        if (key.contains("accepted")) {
            return "✔";
        }
        if (key.contains("rejected")) {
            return "✖";
        }
        if (key.contains("withdrawn")) {
            return "↩";
        }
        if (key.contains("closed")) {
            return "⚑";
        }
        return "◈";
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

    private int acceptedApplicantsForJob(String jobId) {
        int count = 0;
        for (Application application : applicationService.getApplicationsByJobId(jobId)) {
            if (application.getStatus() == ApplicationStatus.ACCEPTED) {
                count++;
            }
        }
        return count;
    }

    private void showNotificationDetails(JTable table) {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a notification first.", "Select Notification", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(selected);
        String message = "";
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            if ("Message".equals(table.getModel().getColumnName(i))) {
                Object val = table.getModel().getValueAt(modelRow, i);
                if (val != null) message = val.toString();
                break;
            }
        }
        
        JTextArea area = new JTextArea(message);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(400, 200));
        
        JOptionPane.showMessageDialog(frame, sp, "Notification Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private int acceptedHoursForTa(String taUserId) {
        int hours = 0;
        for (Application application : applicationService.getApplicationsByTaUserId(taUserId)) {
            if (application.getStatus() != ApplicationStatus.ACCEPTED) {
                continue;
            }
            Job job = findJobById(application.getJobId()).orElse(null);
            if (job != null) {
                hours += job.getHoursPerWeek();
            }
        }
        return hours;
    }

    private class RoundedLineBorder implements javax.swing.border.Border {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedLineBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new java.awt.BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

    private void styleRoundedInput(javax.swing.JComponent input) {
        input.setBackground(Color.WHITE);
        Color defaultBorderColor = new Color(209, 213, 219);
        Color focusBorderColor = new Color(75, 54, 130);
        
        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                new RoundedLineBorder(defaultBorderColor, 6, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12));
        javax.swing.border.Border focusBorder = BorderFactory.createCompoundBorder(
                new RoundedLineBorder(focusBorderColor, 6, 2),
                BorderFactory.createEmptyBorder(7, 11, 7, 11));
        
        input.setBorder(defaultBorder);
        input.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                input.setBorder(focusBorder);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                input.setBorder(defaultBorder);
            }
        });
    }

    private JPanel createRoundedComboWrapper(JComboBox<?> combo) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        wrapper.add(combo, BorderLayout.CENTER);
        
        Color defaultBorderColor = new Color(209, 213, 219);
        Color focusBorderColor = new Color(75, 54, 130);
        
        javax.swing.border.Border defaultBorder = new RoundedLineBorder(defaultBorderColor, 6, 1);
        javax.swing.border.Border focusBorder = new RoundedLineBorder(focusBorderColor, 6, 2);
        
        wrapper.setBorder(BorderFactory.createCompoundBorder(defaultBorder, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        
        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(focusBorder, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                wrapper.setBorder(BorderFactory.createCompoundBorder(defaultBorder, BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            }
        });
        return wrapper;
    }

    private JButton createRoundedButton(String text, boolean primary) {
        Color normalBg = primary ? new Color(75, 54, 130) : Color.WHITE;
        Color fg = primary ? Color.WHITE : new Color(75, 54, 130);
        Color borderColor = primary ? new Color(75, 54, 130) : new Color(209, 213, 219);
        Color hoverBg = primary ? new Color(58, 31, 107) : new Color(249, 250, 251);

        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hoverBg : normalBg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                
                super.paintComponent(g);
            }
        };
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setForeground(fg);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void stylePrimaryButton(JButton button) {
        styleUnifiedButton(button, true, false);
    }

    private void styleSecondaryButton(JButton button) {
        styleUnifiedButton(button, false, false);
    }

    private void styleGhostButton(JButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(Color.WHITE);
        button.setForeground(QMUL_PURPLE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(QMUL_PURPLE, 1, true),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(245, 240, 252));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(Color.WHITE);
                }
            }
        });
    }

    private void styleMaterialInput(javax.swing.JComponent input) {
        input.setBackground(Color.WHITE);
        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(6, 0, 6, 0));
        javax.swing.border.Border focusBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, QMUL_PURPLE),
                BorderFactory.createEmptyBorder(5, 0, 6, 0));
        input.setBorder(defaultBorder);
        input.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                input.setBorder(focusBorder);
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                input.setBorder(defaultBorder);
            }
        });
    }

    private void styleDangerButton(JButton button) {
        styleUnifiedButton(button, true, true);
    }

    private void styleUnifiedButton(JButton button, boolean bold, boolean danger) {
        Color normalBg = danger ? new Color(220, 53, 69) : QMUL_PURPLE;
        Color hoverBg = danger ? new Color(200, 35, 51) : new Color(107, 63, 160);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(normalBg);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                javax.swing.AbstractButton b = (javax.swing.AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color bg = b.isEnabled() ? b.getBackground() : (Color) b.getClientProperty("button.disabledBg");
                if (bg == null) bg = new Color(120, 105, 145);
                
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), 8, 8);
                g2.dispose();
                
                super.paint(g, c);
            }
        });

        button.putClientProperty("button.hover", Boolean.FALSE);
        button.putClientProperty("button.disabledBg", new Color(120, 105, 145));
        button.putClientProperty("button.disabledFg", Color.WHITE);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hoverBg);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(normalBg);
                }
            }
        });
        button.addPropertyChangeListener("enabled", evt -> {
            if (button.isEnabled()) {
                Color origBg = danger ? new Color(220, 53, 69) : QMUL_PURPLE;
                button.setBackground(origBg);
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground((Color) button.getClientProperty("button.disabledBg"));
                button.setForeground((Color) button.getClientProperty("button.disabledFg"));
            }
        });
        if (danger) {
            button.putClientProperty("button.variant", "danger");
        }
    }

    private JPanel createAuthBrandPanel(boolean includeTagline, boolean darkTheme) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("BUPT x QMUL");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 30));
        brand.setForeground(darkTheme ? Color.WHITE : QMUL_PURPLE);
        JLabel system = new JLabel("TA Recruitment System");
        system.setFont(new Font("Segoe UI", Font.BOLD, 24));
        system.setForeground(darkTheme ? Color.WHITE : new Color(36, 41, 56));
        JLabel tagline = new JLabel(BRAND_TAGLINE);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(darkTheme ? new Color(255, 255, 255, 180) : MUTED_TEXT_COLOR);
        panel.add(brand);
        panel.add(system);
        if (includeTagline) {
            panel.add(Box.createVerticalStrut(4));
            panel.add(tagline);
        }
        return panel;
    }

    private void showToast(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(frame, message, title, messageType);
    }

    private String scoreProgressBar(int score) {
        int normalized = Math.max(0, Math.min(score, 100));
        int filled = (int) Math.round(normalized / 10.0);
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            builder.append(i < filled ? "■" : "□");
        }
        builder.append("] ").append(normalized).append("%");
        return builder.toString();
    }

    private void styleDataTable(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(38);
        table.setIntercellSpacing(new Dimension(0, 8));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setGridColor(CARD_BORDER);
        table.setSelectionBackground(new Color(235, 228, 255));
        table.setSelectionForeground(HEADER_TEXT);
        table.setBackground(CARD_WHITE);
        table.setForeground(new Color(46, 52, 64));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (table.getTableHeader() != null) {
            table.getTableHeader().setReorderingAllowed(false);
            table.getTableHeader().setPreferredSize(new Dimension(0, 38));
            table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                    setOpaque(true);
                    setBackground(TABLE_HEADER_BG);
                    setForeground(TABLE_HEADER_TEXT);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    setHorizontalAlignment(LEFT);
                    return this;
                }
            });
        }
    }

    private void applyStatusRenderer(JTable table, int statusColumn) {
        table.getColumnModel().getColumn(statusColumn).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value == null ? "" : String.valueOf(value).trim().toUpperCase();
                setText(status);
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

                Color fg = Color.WHITE;
                Color bg = BADGE_PURPLE;
                if (status.contains("ACCEPTED") || status.contains("OPEN")) {
                    fg = new Color(6, 95, 70);
                    bg = new Color(209, 250, 229);
                } else if (status.contains("REJECTED") || status.contains("CLOSED")) {
                    fg = new Color(153, 27, 27);
                    bg = new Color(254, 226, 226);
                } else if (status.contains("PENDING") || status.contains("REVIEWING") || status.contains("APPLIED")) {
                    fg = new Color(146, 64, 14);
                    bg = new Color(254, 243, 199);
                } else if (status.contains("WITHDRAWN")) {
                    fg = new Color(71, 85, 105);
                    bg = new Color(241, 245, 249);
                } else if (status.contains("FILLED")) {
                    fg = new Color(91, 33, 182);
                    bg = new Color(237, 233, 254);
                }

                if (!isSelected) {
                    setForeground(fg);
                    setBackground(bg);
                }
                return this;
            }
        });
    }

    private void applyIntegerSuffixRenderer(JTable table, int columnIndex, String suffix) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(CENTER);
                setText(value == null ? "" : value + suffix);
                return this;
            }
        });
    }

    private void applyRecommendationRenderer(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String recommendation = value == null ? "" : String.valueOf(value).trim();
                setHorizontalAlignment(CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (!isSelected) {
                    if ("Recommended".equalsIgnoreCase(recommendation)) {
                        setForeground(new Color(31, 122, 78));
                    } else if ("Review".equalsIgnoreCase(recommendation)) {
                        setForeground(new Color(184, 112, 0));
                    } else if ("Unavailable".equalsIgnoreCase(recommendation)) {
                        setForeground(new Color(120, 120, 120));
                    } else {
                        setForeground(new Color(183, 77, 77));
                    }
                }
                return this;
            }
        });
    }

    private JPanel createCardPanel(JComponent content) {
        return createCardPanel(content, 18, 18, 18, 18);
    }

    private JPanel createCardPanel(JComponent content, int top, int left, int bottom, int right) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(top, left, bottom, right)));
        card.setOpaque(true);
        if (content != null) {
            card.add(content, BorderLayout.CENTER);
        }
        return card;
    }

    private JPanel createSectionTitle(String title, String subtitle) {
        JPanel holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 21));
        titleLabel.setForeground(HEADER_TEXT);
        holder.add(titleLabel);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitleLabel.setForeground(MUTED_TEXT_COLOR);
            holder.add(Box.createVerticalStrut(4));
            holder.add(subtitleLabel);
        }
        return holder;
    }

    private JLabel createStatGlyph(String text, Color background) {
        JLabel glyph = new JLabel(text, SwingConstants.CENTER);
        glyph.setOpaque(true);
        glyph.setPreferredSize(new Dimension(34, 34));
        glyph.setMaximumSize(new Dimension(34, 34));
        glyph.setBackground(background);
        glyph.setForeground(Color.WHITE);
        glyph.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return glyph;
    }

    private JPanel createGradientStatCard(JLabel valueLabel, String title, String subtitle, String glyphText, Color startColor, Color endColor) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                java.awt.GradientPaint gradient = new java.awt.GradientPaint(0, 0, new Color(startColor.getRed(), startColor.getGreen(), startColor.getBlue(), 18),
                        getWidth(), getHeight(), new Color(endColor.getRed(), endColor.getGreen(), endColor.getBlue(), 8));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        card.putClientProperty("gradientStart", startColor);
        card.putClientProperty("gradientEnd", endColor);
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(createStatGlyph(glyphText, startColor), BorderLayout.WEST);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED_TEXT_COLOR);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topRow.add(titleLabel, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);
        valueLabel.setForeground(HEADER_TEXT);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        card.add(valueLabel, BorderLayout.CENTER);
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(MUTED_TEXT_COLOR);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(subtitleLabel, BorderLayout.SOUTH);
        return card;
    }

    private void installTableRowHover(JTable table) {
        table.putClientProperty("hoverRow", -1);
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (table.getClientProperty("hoverRow") instanceof Integer oldRow && oldRow == row) {
                    return;
                }
                table.putClientProperty("hoverRow", row);
                table.repaint();
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                table.putClientProperty("hoverRow", -1);
                table.repaint();
            }
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                boolean hovered = table.getClientProperty("hoverRow") instanceof Integer hover && hover == row;
                boolean zebra = row % 2 == 1;
                if (!isSelected) {
                    setBackground(hovered ? new Color(239, 232, 255) : (zebra ? new Color(250, 250, 252) : Color.WHITE));
                    setForeground(new Color(46, 52, 64));
                }
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(236, 238, 242)));
                setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return this;
            }
        });
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

    private ImageIcon loadAvatarIcon(String avatarPath, int size) {
        File file = resolveAvatarFile(avatarPath);
        BufferedImage image = null;
        if (file != null && file.exists()) {
            try {
                image = javax.imageio.ImageIO.read(file);
            } catch (IOException ignored) {
                image = null;
            }
        }
        if (image == null) {
            image = createDefaultAvatarImage(size);
        }
        Image scaled = image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        BufferedImage clipped = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = clipped.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape circle = new Ellipse2D.Double(0, 0, size, size);
        g2.setClip(circle);
        g2.drawImage(scaled, 0, 0, null);
        g2.setClip(null);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.draw(circle);
        g2.dispose();
        return new ImageIcon(clipped);
    }

    private BufferedImage createDefaultAvatarImage(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(242, 244, 248));
        g2.fillRect(0, 0, size, size);
        g2.setColor(new Color(214, 219, 229));
        g2.fillOval((int) (size * 0.18), (int) (size * 0.12), (int) (size * 0.64), (int) (size * 0.64));
        g2.setColor(new Color(214, 219, 229));
        g2.fillOval((int) (size * 0.30), (int) (size * 0.30), (int) (size * 0.40), (int) (size * 0.40));
        g2.fillRoundRect((int) (size * 0.22), (int) (size * 0.60), (int) (size * 0.56), (int) (size * 0.23), size, size);
        g2.dispose();
        return image;
    }

    private File resolveAvatarFile(String avatarPath) {
        String candidate = avatarPath == null ? "" : avatarPath.trim();
        if (!candidate.isBlank()) {
            File avatarFile = new File(candidate);
            if (avatarFile.exists()) {
                return avatarFile;
            }
            File relativeFile = new File(AVATAR_DIR, candidate);
            if (relativeFile.exists()) {
                return relativeFile;
            }
        }
        File fallback = new File(DEFAULT_AVATAR_PATH);
        return fallback.exists() ? fallback : null;
    }

    private Path saveAvatarFileToDataDir(File sourceFile, String userId) {
        try {
            Path avatarsDir = dataDirectory != null
                    ? dataDirectory.resolve("avatars")
                    : Path.of(System.getProperty("user.dir")).resolve("data").resolve("avatars");
            Files.createDirectories(avatarsDir);
            String ext = sourceFile.getName().contains(".")
                    ? sourceFile.getName().substring(sourceFile.getName().lastIndexOf('.'))
                    : "";
            String destName = userId + "_avatar_" + System.currentTimeMillis() + ext;
            Path destPath = avatarsDir.resolve(destName);
            Files.copy(sourceFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath.toAbsolutePath();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to save avatar file: " + ex.getMessage());
            return null;
        }
    }

    private class LoginPanel extends JPanel {
        private final JTextField emailField;
        private final JPasswordField passwordField;
        private final JComboBox<Role> roleCombo;
        private final JButton loginButton;
        private final JButton registerButton;
        private final JLabel statusLabel;
        private final JPanel form;

        void installDemoButton(AutoDemoController controller) {
            if (controller == null || !controller.isAvailable()) {
                return;
            }
            JButton demo = new JButton("Play Demo");
            stylePrimaryButton(demo);
            demo.setToolTipText("Run the auto demo: TA \u2192 MO \u2192 Admin walkthrough (~9 min).");
            demo.addActionListener(e -> {
                int ok = JOptionPane.showConfirmDialog(frame,
                        "Start the full auto demo? Your data/ files will be backed up\n"
                                + "and restored when it finishes (or if you close the app).",
                        "Auto Demo", JOptionPane.OK_CANCEL_OPTION);
                if (ok == JOptionPane.OK_OPTION) {
                    controller.start();
                }
            });
            JLabel spacer = new JLabel(" ");
            form.add(spacer);
            form.add(demo);
            form.revalidate();
            form.repaint();
        }

        private LoginPanel() {
            setLayout(new BorderLayout());

            // LEFT PANEL (400px wide, beautiful solid system purple with abstract geometric accents)
            JPanel left = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw the exact solid system purple background (#4B3682)
                    g2.setColor(new Color(75, 54, 130));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Paint abstract geometric rings with low opacity to prevent a "bare" look
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillOval(-120, -120, 360, 360);
                    g2.fillOval(getWidth() - 160, getHeight() - 220, 320, 320);
                    
                    g2.setColor(new Color(255, 255, 255, 6));
                    g2.fillOval(80, getHeight() / 2 - 160, 260, 260);
                    
                    // Draw a subtle glowing arc intersecting the branding text
                    g2.setStroke(new java.awt.BasicStroke(2));
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.drawArc(-200, getHeight() / 2 - 250, 500, 500, 45, 270);
                    
                    g2.dispose();
                }
            };
            left.setPreferredSize(new Dimension(400, 0));
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(BorderFactory.createEmptyBorder(48, 32, 48, 32));
            
            // Top branding text
            JLabel topLabel = new JLabel("BU x QM JOINT PROGRAMME");
            topLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            topLabel.setForeground(new Color(255, 255, 255, 140));
            topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(topLabel);
            
            left.add(Box.createVerticalGlue());
            
            // Center main title and subtitle
            JLabel brandLabel = new JLabel("BUPT x QMUL");
            brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
            brandLabel.setForeground(Color.WHITE);
            brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel sysLabel = new JLabel("TA Recruitment System");
            sysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            sysLabel.setForeground(new Color(255, 255, 255, 220));
            sysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            left.add(brandLabel);
            left.add(Box.createVerticalStrut(10));
            left.add(sysLabel);
            
            left.add(Box.createVerticalGlue());
            
            // Bottom copyright text
            JLabel bottomLabel = new JLabel("© 2026 BUPT x QMUL. All rights reserved.");
            bottomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            bottomLabel.setForeground(new Color(255, 255, 255, 100));
            bottomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(bottomLabel);

            // RIGHT PANEL (pure white background filling the remaining width)
            JPanel right = new JPanel(new GridBagLayout());
            right.setBackground(Color.WHITE);
            right.setOpaque(true);

            // Centered form container inside the right column
            JPanel formContainer = new JPanel();
            formContainer.setOpaque(false);
            formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
            formContainer.setPreferredSize(new Dimension(380, 520));
            formContainer.setMaximumSize(new Dimension(380, 520));

            JLabel title = new JLabel("Welcome back");
            title.setFont(new Font("Segoe UI", Font.BOLD, 30));
            title.setForeground(new Color(31, 41, 55));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            formContainer.add(title);
            
            formContainer.add(Box.createVerticalStrut(8));
            
            JLabel subtitle = new JLabel("Sign in to continue to your dashboard.");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            subtitle.setForeground(new Color(107, 114, 128));
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            formContainer.add(subtitle);
            formContainer.add(Box.createVerticalStrut(40));

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            form.setAlignmentX(Component.LEFT_ALIGNMENT);
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridx = 0;

            Color labelColor = new Color(75, 85, 99);

            gbc.gridy = 0; 
            gbc.insets = new Insets(0, 0, 6, 0);
            JLabel roleLabel = new JLabel("Role");
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            roleLabel.setForeground(labelColor);
            form.add(roleLabel, gbc);

            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 24, 0);
            roleCombo = new JComboBox<>(new Role[] {Role.TA, Role.MO, Role.ADMIN});
            roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            roleCombo.setFocusable(false);
            form.add(createRoundedComboWrapper(roleCombo), gbc);

            gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 6, 0);
            JLabel emailLabel = new JLabel("Email");
            emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            emailLabel.setForeground(labelColor);
            form.add(emailLabel, gbc);

            gbc.gridy = 3;
            gbc.insets = new Insets(0, 0, 24, 0);
            emailField = new JTextField();
            emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            styleRoundedInput(emailField);
            form.add(emailField, gbc);

            gbc.gridy = 4;
            gbc.insets = new Insets(0, 0, 6, 0);
            JLabel pwdLabel = new JLabel("Password");
            pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            pwdLabel.setForeground(labelColor);
            form.add(pwdLabel, gbc);

            gbc.gridy = 5;
            gbc.insets = new Insets(0, 0, 32, 0);
            passwordField = new JPasswordField();
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            styleRoundedInput(passwordField);
            form.add(passwordField, gbc);

            gbc.gridy = 6;
            gbc.insets = new Insets(0, 0, 16, 0);
            loginButton = createRoundedButton("Sign in", true);
            loginButton.setPreferredSize(new Dimension(0, 42));
            loginButton.addActionListener(e -> login());
            form.add(loginButton, gbc);

            gbc.gridy = 7;
            gbc.insets = new Insets(0, 0, 10, 0);
            registerButton = createRoundedButton("Create a TA account", false);
            registerButton.setPreferredSize(new Dimension(0, 42));
            registerButton.addActionListener(e -> showRegisterPage());
            form.add(registerButton, gbc);

            formContainer.add(form);
            formContainer.add(Box.createVerticalGlue());
            
            statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(new Color(100, 100, 100));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            formContainer.add(statusLabel);

            // Center the form container in the right column
            GridBagConstraints containerGbc = new GridBagConstraints();
            containerGbc.gridx = 0;
            containerGbc.gridy = 0;
            containerGbc.anchor = GridBagConstraints.CENTER;
            right.add(formContainer, containerGbc);

            add(left, BorderLayout.WEST);
            add(right, BorderLayout.CENTER);
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

        private void showRegisterPage() {
            registerPanel.reset();
            showPage(PAGE_REGISTER);
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
            } catch (IllegalArgumentException ex) {
                String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                        ? ex.getMessage() : "Invalid credentials. Please try again.";
                JOptionPane.showMessageDialog(frame, msg, "Login Failed", JOptionPane.WARNING_MESSAGE);
                statusLabel.setText("Login failed.");
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "An unexpected error occurred. Please try again.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Login error.");
            }
        }

        private void registerTa() {
            showRegisterPage();
        }
    }

    private abstract class AvatarAwarePanel extends JPanel {
        protected final String[] presetAvatarFiles = AVATAR_FILES;

        protected JButton createAvatarChoiceButton(String fileName, String label, java.util.function.Consumer<String> onSelect) {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(76, 76));
            button.setMinimumSize(new Dimension(76, 76));
            button.setMaximumSize(new Dimension(76, 76));
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setBackground(Color.WHITE);
            button.setIcon(loadAvatarIcon(AVATAR_DIR + "/" + fileName, 68));
            button.setToolTipText(label);
            button.setBorder(BorderFactory.createLineBorder(new Color(214, 219, 229), 2, true));
            button.addActionListener(e -> onSelect.accept(fileName));
            return button;
        }
    }

    private class RegisterPanel extends AvatarAwarePanel {
        private final JLabel avatarPreview;
        private final JTextField studentIdField;
        private final JTextField nameField;
        private final JTextField emailField;
        private final JPasswordField passwordField;
        private final JTextField programmeField;
        private final JTextField yearField;
        private final JTextArea skillsArea;
        private final JTextField hoursField;
        private final JLabel avatarPathLabel;
        private final List<JButton> avatarChoiceButtons = new ArrayList<>();
        private String selectedAvatarPath = "";

        private RegisterPanel() {
            setLayout(new BorderLayout());

            // LEFT PANEL (400px wide, beautiful solid system purple with abstract geometric accents)
            JPanel left = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Draw the exact solid system purple background (#4B3682)
                    g2.setColor(new Color(75, 54, 130));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Paint abstract geometric rings with low opacity to prevent a "bare" look
                    g2.setColor(new Color(255, 255, 255, 12));
                    g2.fillOval(-120, -120, 360, 360);
                    g2.fillOval(getWidth() - 160, getHeight() - 220, 320, 320);
                    
                    g2.setColor(new Color(255, 255, 255, 6));
                    g2.fillOval(80, getHeight() / 2 - 160, 260, 260);
                    
                    // Draw a subtle glowing arc intersecting the branding text
                    g2.setStroke(new java.awt.BasicStroke(2));
                    g2.setColor(new Color(255, 255, 255, 18));
                    g2.drawArc(-200, getHeight() / 2 - 250, 500, 500, 45, 270);
                    
                    g2.dispose();
                }
            };
            left.setPreferredSize(new Dimension(400, 0));
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.setBorder(BorderFactory.createEmptyBorder(48, 32, 48, 32));
            
            // Top branding text
            JLabel topLabel = new JLabel("BU x QM JOINT PROGRAMME");
            topLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            topLabel.setForeground(new Color(255, 255, 255, 140));
            topLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(topLabel);
            
            left.add(Box.createVerticalGlue());
            
            // Center main title and subtitle
            JLabel brandLabel = new JLabel("BUPT x QMUL");
            brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
            brandLabel.setForeground(Color.WHITE);
            brandLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel sysLabel = new JLabel("TA Recruitment System");
            sysLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            sysLabel.setForeground(new Color(255, 255, 255, 220));
            sysLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            left.add(brandLabel);
            left.add(Box.createVerticalStrut(10));
            left.add(sysLabel);
            
            left.add(Box.createVerticalGlue());
            
            // Bottom copyright text
            JLabel bottomLabel = new JLabel("© 2026 BUPT x QMUL. All rights reserved.");
            bottomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            bottomLabel.setForeground(new Color(255, 255, 255, 100));
            bottomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            left.add(bottomLabel);

            // RIGHT PANEL (pure white background filling the remaining width)
            JPanel right = new JPanel(new GridBagLayout());
            right.setBackground(Color.WHITE);
            right.setOpaque(true);

            // Scrollable form container inside the right column
            JPanel formContainer = new JPanel();
            formContainer.setOpaque(false);
            formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
            formContainer.setBorder(BorderFactory.createEmptyBorder(32, 48, 32, 48));

            JLabel title = new JLabel("Create your TA profile");
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
            title.setForeground(new Color(31, 41, 55));
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            formContainer.add(title);
            
            formContainer.add(Box.createVerticalStrut(6));
            
            JLabel subtitle = new JLabel("Upload a photo and fill in your academic details to get started.");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            subtitle.setForeground(new Color(107, 114, 128));
            subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            formContainer.add(subtitle);
            
            formContainer.add(Box.createVerticalStrut(24));

            // Avatar Selection Panel
            JPanel avatarSec = new JPanel(new BorderLayout(20, 0));
            avatarSec.setOpaque(false);
            avatarSec.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            avatarPreview = new JLabel(loadAvatarIcon("", 88));
            avatarPreview.setPreferredSize(new Dimension(88, 88));
            avatarPreview.setMinimumSize(new Dimension(88, 88));
            avatarPreview.setMaximumSize(new Dimension(88, 88));
            avatarSec.add(avatarPreview, BorderLayout.WEST);
            
            JPanel avatarRight = new JPanel();
            avatarRight.setOpaque(false);
            avatarRight.setLayout(new BoxLayout(avatarRight, BoxLayout.Y_AXIS));
            
            JPanel wallPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            wallPanel.setOpaque(false);
            for (String fileName : presetAvatarFiles) {
                JButton choiceButton = createAvatarChoiceButton(fileName, fileName, this::selectPresetAvatar);
                choiceButton.setPreferredSize(new Dimension(46, 46));
                choiceButton.setMinimumSize(new Dimension(46, 46));
                choiceButton.setMaximumSize(new Dimension(46, 46));
                choiceButton.setIcon(loadAvatarIcon(AVATAR_DIR + "/" + fileName, 40));
                choiceButton.setBorder(BorderFactory.createLineBorder(new Color(214, 219, 229), 1, true));
                avatarChoiceButtons.add(choiceButton);
                wallPanel.add(choiceButton);
            }
            
            JPanel btnsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            btnsPanel.setOpaque(false);
            
            JButton uploadButton = new JButton("Upload Image");
            styleSecondaryButton(uploadButton);
            uploadButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            uploadButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            uploadButton.addActionListener(e -> chooseRegisterAvatar());
            
            JButton resetAvatarButton = new JButton("Use Default");
            styleSecondaryButton(resetAvatarButton);
            resetAvatarButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
            resetAvatarButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            resetAvatarButton.addActionListener(e -> resetRegisterAvatar());
            
            btnsPanel.add(uploadButton);
            btnsPanel.add(resetAvatarButton);
            
            avatarPathLabel = new JLabel("Using default avatar");
            avatarPathLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            avatarPathLabel.setForeground(new Color(120, 120, 120));
            avatarPathLabel.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 0));
            
            avatarRight.add(wallPanel);
            avatarRight.add(Box.createVerticalStrut(8));
            avatarRight.add(btnsPanel);
            avatarRight.add(avatarPathLabel);
            
            avatarSec.add(avatarRight, BorderLayout.CENTER);
            formContainer.add(avatarSec);
            
            formContainer.add(Box.createVerticalStrut(24));

            // Grid layout for 2-column fields
            JPanel fieldsGrid = new JPanel(new GridBagLayout());
            fieldsGrid.setOpaque(false);
            fieldsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            GridBagConstraints fgbc = new GridBagConstraints();
            fgbc.fill = GridBagConstraints.HORIZONTAL;
            fgbc.weightx = 0.5;
            
            Color fieldLabelColor = new Color(75, 85, 99);
            Font fieldLabelFont = new Font("Segoe UI", Font.BOLD, 12);
            
            studentIdField = new JTextField();
            styleRoundedInput(studentIdField);
            nameField = new JTextField();
            styleRoundedInput(nameField);
            emailField = new JTextField();
            styleRoundedInput(emailField);
            passwordField = new JPasswordField();
            styleRoundedInput(passwordField);
            programmeField = new JTextField();
            styleRoundedInput(programmeField);
            yearField = new JTextField();
            styleRoundedInput(yearField);
            skillsArea = new JTextArea(3, 20);
            styleRoundedInput(skillsArea);
            skillsArea.setLineWrap(true);
            skillsArea.setWrapStyleWord(true);
            hoursField = new JTextField();
            styleRoundedInput(hoursField);

            // Row 0: Student ID and Name
            JPanel sub0 = new JPanel(new BorderLayout(0, 4));
            sub0.setOpaque(false);
            JLabel l0 = new JLabel("Student ID"); l0.setFont(fieldLabelFont); l0.setForeground(fieldLabelColor);
            sub0.add(l0, BorderLayout.NORTH);
            sub0.add(studentIdField, BorderLayout.CENTER);
            
            JPanel sub1 = new JPanel(new BorderLayout(0, 4));
            sub1.setOpaque(false);
            JLabel l1 = new JLabel("Name"); l1.setFont(fieldLabelFont); l1.setForeground(fieldLabelColor);
            sub1.add(l1, BorderLayout.NORTH);
            sub1.add(nameField, BorderLayout.CENTER);
            
            fgbc.gridy = 0;
            fgbc.gridx = 0; fgbc.insets = new Insets(0, 0, 16, 12); fieldsGrid.add(sub0, fgbc);
            fgbc.gridx = 1; fgbc.insets = new Insets(0, 12, 16, 0); fieldsGrid.add(sub1, fgbc);
            
            // Row 1: Email and Password
            JPanel sub2 = new JPanel(new BorderLayout(0, 4));
            sub2.setOpaque(false);
            JLabel l2 = new JLabel("Email"); l2.setFont(fieldLabelFont); l2.setForeground(fieldLabelColor);
            sub2.add(l2, BorderLayout.NORTH);
            sub2.add(emailField, BorderLayout.CENTER);
            
            JPanel sub3 = new JPanel(new BorderLayout(0, 4));
            sub3.setOpaque(false);
            JLabel l3 = new JLabel("Password"); l3.setFont(fieldLabelFont); l3.setForeground(fieldLabelColor);
            sub3.add(l3, BorderLayout.NORTH);
            sub3.add(passwordField, BorderLayout.CENTER);
            
            fgbc.gridy = 1;
            fgbc.gridx = 0; fgbc.insets = new Insets(0, 0, 16, 12); fieldsGrid.add(sub2, fgbc);
            fgbc.gridx = 1; fgbc.insets = new Insets(0, 12, 16, 0); fieldsGrid.add(sub3, fgbc);
            
            // Row 2: Programme and Year of Study
            JPanel sub4 = new JPanel(new BorderLayout(0, 4));
            sub4.setOpaque(false);
            JLabel l4 = new JLabel("Programme"); l4.setFont(fieldLabelFont); l4.setForeground(fieldLabelColor);
            sub4.add(l4, BorderLayout.NORTH);
            sub4.add(programmeField, BorderLayout.CENTER);
            
            JPanel sub5 = new JPanel(new BorderLayout(0, 4));
            sub5.setOpaque(false);
            JLabel l5 = new JLabel("Year of Study"); l5.setFont(fieldLabelFont); l5.setForeground(fieldLabelColor);
            sub5.add(l5, BorderLayout.NORTH);
            sub5.add(yearField, BorderLayout.CENTER);
            
            fgbc.gridy = 2;
            fgbc.gridx = 0; fgbc.insets = new Insets(0, 0, 16, 12); fieldsGrid.add(sub4, fgbc);
            fgbc.gridx = 1; fgbc.insets = new Insets(0, 12, 16, 0); fieldsGrid.add(sub5, fgbc);
            
            // Row 3: Skills and Available Hours/Week
            JPanel sub6 = new JPanel(new BorderLayout(0, 4));
            sub6.setOpaque(false);
            JLabel l6 = new JLabel("Skills (comma-separated)"); l6.setFont(fieldLabelFont); l6.setForeground(fieldLabelColor);
            sub6.add(l6, BorderLayout.NORTH);
            JScrollPane skillsScroll = new JScrollPane(skillsArea);
            skillsScroll.setBorder(null);
            sub6.add(skillsScroll, BorderLayout.CENTER);
            
            JPanel sub7 = new JPanel(new BorderLayout(0, 4));
            sub7.setOpaque(false);
            JLabel l7 = new JLabel("Available Hours/Week"); l7.setFont(fieldLabelFont); l7.setForeground(fieldLabelColor);
            sub7.add(l7, BorderLayout.NORTH);
            sub7.add(hoursField, BorderLayout.CENTER);
            
            fgbc.gridy = 3;
            fgbc.gridx = 0; fgbc.insets = new Insets(0, 0, 16, 12); fieldsGrid.add(sub6, fgbc);
            fgbc.gridx = 1; fgbc.insets = new Insets(0, 12, 16, 0); fieldsGrid.add(sub7, fgbc);
            
            formContainer.add(fieldsGrid);

            // Action row buttons
            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            actionRow.setOpaque(false);
            actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JButton submitButton = new JButton("Register Now");
            stylePrimaryButton(submitButton);
            submitButton.setPreferredSize(new Dimension(160, 42));
            submitButton.addActionListener(e -> submitRegister());
            
            JButton backButton = new JButton("Back to Login");
            styleSecondaryButton(backButton);
            backButton.setPreferredSize(new Dimension(160, 42));
            backButton.addActionListener(e -> showLoginPage());
            
            actionRow.add(submitButton);
            actionRow.add(Box.createHorizontalStrut(12));
            actionRow.add(backButton);
            
            formContainer.add(Box.createVerticalStrut(16));
            formContainer.add(actionRow);

            // Wrapping form inside a JScrollPane for responsiveness
            JScrollPane scrollPane = new JScrollPane(formContainer);
            scrollPane.setBorder(null);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            
            GridBagConstraints containerGbc = new GridBagConstraints();
            containerGbc.gridx = 0;
            containerGbc.gridy = 0;
            containerGbc.fill = GridBagConstraints.BOTH;
            containerGbc.weightx = 1.0;
            containerGbc.weighty = 1.0;
            containerGbc.insets = new Insets(20, 20, 20, 20);
            right.add(scrollPane, containerGbc);
            
            add(left, BorderLayout.WEST);
            add(right, BorderLayout.CENTER);
        }

        private void reset() {
            studentIdField.setText("");
            nameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            programmeField.setText("");
            yearField.setText("");
            skillsArea.setText("");
            hoursField.setText("");
            selectedAvatarPath = "";
            avatarPreview.setIcon(loadAvatarIcon("", 88));
            avatarPathLabel.setText("Using default avatar");
            updateAvatarWallSelection("");
        }

        private void chooseRegisterAvatar() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Image files (*.png, *.jpg, *.jpeg, *.webp)", "png", "jpg", "jpeg", "webp"));
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                Path savedPath = saveAvatarFileToDataDir(chooser.getSelectedFile(), studentIdField.getText().trim().isBlank()
                        ? "TA"
                        : "TA" + studentIdField.getText().trim());
                if (savedPath != null) {
                    selectedAvatarPath = savedPath.toAbsolutePath().toString();
                    avatarPreview.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
                    avatarPathLabel.setText(new File(selectedAvatarPath).getName());
                    updateAvatarWallSelection("");
                }
            }
        }

        private void selectPresetAvatar(String fileName) {
            selectedAvatarPath = AVATAR_DIR + "/" + fileName;
            avatarPreview.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
            avatarPathLabel.setText(fileName);
            updateAvatarWallSelection(fileName);
        }

        private void updateAvatarWallSelection(String selectedFileName) {
            for (int i = 0; i < avatarChoiceButtons.size(); i++) {
                JButton button = avatarChoiceButtons.get(i);
                String fileName = presetAvatarFiles[i];
                boolean selected = fileName.equals(selectedFileName);
                button.setBorder(BorderFactory.createLineBorder(
                        selected ? QMUL_PURPLE : new Color(214, 219, 229),
                        selected ? 3 : 2,
                        true));
                button.setBackground(selected ? new Color(242, 235, 252) : Color.WHITE);
            }
        }

        private void resetRegisterAvatar() {
            selectedAvatarPath = "";
            avatarPreview.setIcon(loadAvatarIcon("", 88));
            avatarPathLabel.setText("Using default avatar");
            updateAvatarWallSelection("");
        }

        private void submitRegister() {
            try {
                User ta = authService.registerTa(
                        studentIdField.getText().trim(),
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passwordField.getPassword()));
                ta.setProgramme(programmeField.getText().trim());
                ta.setYearOfStudy(Integer.parseInt(yearField.getText().trim()));
                ta.setSkills(skillsArea.getText().trim());
                ta.setAvailableHours(Integer.parseInt(hoursField.getText().trim()));
                ta.setAvatarFilePath(selectedAvatarPath);
                authService.updateUser(ta);
                JOptionPane.showMessageDialog(frame, "TA registered: " + ta.getId());
                showLoginPage();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }
    }

    private class TaPanel extends AvatarAwarePanel {
        static final String TAB_DASHBOARD = "dashboard";
        static final String TAB_JOB_BOARD = "jobBoard";
        static final String TAB_PROFILE = "profile";
        static final String TAB_NOTIFICATIONS = "notifications";

        void demoShowTab(String tab) {
            switch (tab) {
                case TAB_DASHBOARD:
                    refreshApplications();
                    refreshNotifications();
                    break;
                case TAB_JOB_BOARD:
                    refreshJobs();
                    break;
                case TAB_NOTIFICATIONS:
                    refreshNotifications();
                    break;
                case TAB_PROFILE:
                    loadProfile();
                    break;
                default:
                    return;
            }
            contentLayout.show(contentPanel, tab);
        }

        private final JLabel titleLabel;
        private final JLabel profileAvatarLabel;
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
        private final JCheckBox recommendedOnlyBox;
        private final JComboBox<String> notificationFilterBox;
        private final JLabel jobRecommendationSummaryLabel;
        private final JTextArea jobRecommendationDetailsArea;
        private final JLabel unreadCountLabel;
        private final JLabel notificationEmptyLabel;
        private final JLabel applicationSummaryLabel;
        private final JLabel dashboardNotificationLabel;
        private final JLabel dashboardActionLabel;
        private final JLabel dashboardAppliedCountLabel;
        private final JLabel dashboardPendingCountLabel;
        private final JLabel dashboardAcceptedCountLabel;
        private final JTextField profileNameField;
        private final JTextField profileYearField;
        private final JTextField profileProgrammeField;
        private final JTextArea profileSkillsArea;
        private final JTextField profileHoursField;
        private final JLabel cvLabel;

        private User user;
        private String selectedCvPath = "";
        private String selectedCvName = "";
        private String selectedAvatarPath = "";
        private final Set<String> readNotificationIds = new HashSet<>();

        private TaPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("TA Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            profileAvatarLabel = new JLabel();
            profileAvatarLabel.setPreferredSize(new Dimension(48, 48));
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
            styleDataTable(applicationTable);
            applyStatusRenderer(applicationTable, 3);
            installTableRowHover(applicationTable);

            JPanel dashboardTitle = createSectionTitle(
                    "TA Dashboard",
                    "Track applications, review jobs, and monitor updates in one place.");
            JLabel dashboardSubtitle = new JLabel("A focused workspace for your teaching assistant workflow");
            dashboardSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dashboardSubtitle.setForeground(MUTED_TEXT_COLOR);
            JPanel dashboardHeaderText = new JPanel();
            dashboardHeaderText.setOpaque(false);
            dashboardHeaderText.setLayout(new BoxLayout(dashboardHeaderText, BoxLayout.Y_AXIS));
            dashboardHeaderText.add(dashboardTitle);
            dashboardHeaderText.add(Box.createVerticalStrut(6));
            dashboardHeaderText.add(dashboardSubtitle);

            dashboardAppliedCountLabel = new JLabel("0");
            dashboardPendingCountLabel = new JLabel("0");
            dashboardAcceptedCountLabel = new JLabel("0");
            JPanel statCards = new JPanel(new GridLayout(1, 3, 14, 0));
            statCards.setOpaque(false);
            statCards.add(createGradientStatCard(dashboardAppliedCountLabel, "Applications", "Total submitted", "⌘", QMUL_PURPLE, QMUL_PURPLE_LIGHT));
            statCards.add(createGradientStatCard(dashboardPendingCountLabel, "Pending", "Waiting review", "⏳", BADGE_ORANGE, new Color(255, 196, 105)));
            statCards.add(createGradientStatCard(dashboardAcceptedCountLabel, "Accepted", "Confirmed roles", "✓", BADGE_GREEN, new Color(120, 210, 162)));

            applicationSummaryLabel = new JLabel("Applications: 0 pending, 0 accepted, 0 rejected, 0 withdrawn.");
            applicationSummaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            applicationSummaryLabel.setForeground(MUTED_TEXT_COLOR);
            dashboardNotificationLabel = new JLabel("No notifications yet.");
            dashboardNotificationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            dashboardNotificationLabel.setForeground(MUTED_TEXT_COLOR);
            dashboardActionLabel = new JLabel(" ");
            dashboardActionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dashboardActionLabel.setForeground(QMUL_PURPLE);

            JPanel summaryPanel = new JPanel();
            summaryPanel.setOpaque(false);
            summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
            summaryPanel.add(applicationSummaryLabel);
            summaryPanel.add(Box.createVerticalStrut(4));
            summaryPanel.add(dashboardNotificationLabel);
            summaryPanel.add(Box.createVerticalStrut(4));
            summaryPanel.add(dashboardActionLabel);

            JPanel dashboardToolbar = new JPanel(new BorderLayout());
            dashboardToolbar.setOpaque(false);
            dashboardToolbar.add(dashboardHeaderText, BorderLayout.WEST);
            dashboardToolbar.add(summaryPanel, BorderLayout.EAST);

            JPanel dashboardBody = new JPanel(new BorderLayout(0, 16));
            dashboardBody.setOpaque(false);
            dashboardBody.add(statCards, BorderLayout.NORTH);
            dashboardBody.add(createCardPanel(new JScrollPane(applicationTable), 16, 16, 16, 16), BorderLayout.CENTER);

            JButton withdrawButton = new JButton("Withdraw Selected");
            styleDangerButton(withdrawButton);
            withdrawButton.addActionListener(e -> withdrawSelected());
            JButton refreshAppsButton = new JButton("Refresh");
            styleSecondaryButton(refreshAppsButton);
            refreshAppsButton.addActionListener(e -> refreshApplications());
            JButton viewNotificationsButton = new JButton("View Notifications");
            stylePrimaryButton(viewNotificationsButton);
            viewNotificationsButton.addActionListener(e -> {
                refreshNotifications();
                contentLayout.show(contentPanel, TAB_NOTIFICATIONS);
            });
            JPanel dashboardActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            dashboardActions.setOpaque(false);
            dashboardActions.add(withdrawButton);
            dashboardActions.add(refreshAppsButton);
            dashboardActions.add(viewNotificationsButton);

            JPanel dashboardPanel = new JPanel(new BorderLayout(0, 16));
            dashboardPanel.setOpaque(false);
            dashboardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            dashboardPanel.add(createCardPanel(dashboardToolbar, 18, 18, 18, 18), BorderLayout.NORTH);
            dashboardPanel.add(dashboardBody, BorderLayout.CENTER);
            dashboardPanel.add(dashboardActions, BorderLayout.SOUTH);

            jobModel = new DefaultTableModel(
                    new Object[] {"Job ID", "Module", "MO", "Hours/Week", "Deadline", "Status", "AI Fit", "Recommendation"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobTable = new JTable(jobModel);
            styleDataTable(jobTable);
            applyStatusRenderer(jobTable, 5);
            applyRecommendationRenderer(jobTable, 7);
            JPanel jobBoardPanel = new JPanel(new BorderLayout(0, 16));
            jobBoardPanel.setOpaque(false);
            jobBoardPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JPanel searchPanel = new JPanel(new GridLayout(0, 4, 10, 8));
            searchPanel.setOpaque(false);
            JPanel keywordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            keywordPanel.setOpaque(false);
            keywordPanel.add(new JLabel("Search"));
            searchField = new JTextField(24);
            keywordPanel.add(searchField);
            JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            skillsPanel.setOpaque(false);
            skillsPanel.add(new JLabel("Skill"));
            skillsFilterField = new JTextField(14);
            skillsPanel.add(skillsFilterField);
            JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            hoursPanel.setOpaque(false);
            hoursPanel.add(new JLabel("Max Hours"));
            hoursFilterField = new JTextField(8);
            hoursPanel.add(hoursFilterField);
            JPanel moPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            moPanel.setOpaque(false);
            moPanel.add(new JLabel("MO"));
            moFilterField = new JTextField(14);
            moPanel.add(moFilterField);
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            statusPanel.setOpaque(false);
            statusPanel.add(new JLabel("Status"));
            statusFilterBox = new JComboBox<>(new String[] {"OPEN", "ALL", "CLOSED", "FILLED"});
            statusPanel.add(statusFilterBox);
            JPanel aiFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            aiFilterPanel.setOpaque(false);
            recommendedOnlyBox = new JCheckBox("Recommended only");
            recommendedOnlyBox.setOpaque(false);
            recommendedOnlyBox.setToolTipText("Show jobs with strong skill match and enough available hours.");
            recommendedOnlyBox.addActionListener(e -> refreshJobs());
            aiFilterPanel.add(recommendedOnlyBox);
            JButton searchButton = new JButton("Apply Filter");
            stylePrimaryButton(searchButton);
            searchButton.addActionListener(e -> refreshJobs());
            JButton clearButton = new JButton("Clear");
            styleSecondaryButton(clearButton);
            clearButton.addActionListener(e -> {
                searchField.setText("");
                skillsFilterField.setText("");
                hoursFilterField.setText("");
                moFilterField.setText("");
                statusFilterBox.setSelectedItem("OPEN");
                recommendedOnlyBox.setSelected(false);
                refreshJobs();
            });
            JButton refreshJobsButton = new JButton("Refresh");
            styleSecondaryButton(refreshJobsButton);
            refreshJobsButton.addActionListener(e -> refreshJobs());
            JPanel filterActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            filterActionsPanel.setOpaque(false);
            filterActionsPanel.add(searchButton);
            filterActionsPanel.add(clearButton);
            filterActionsPanel.add(refreshJobsButton);
            searchPanel.add(keywordPanel);
            searchPanel.add(skillsPanel);
            searchPanel.add(hoursPanel);
            searchPanel.add(moPanel);
            searchPanel.add(statusPanel);
            searchPanel.add(aiFilterPanel);
            searchPanel.add(filterActionsPanel);
            JPanel jobControlsCard = createCardPanel(searchPanel, 18, 18, 18, 18);
            jobBoardPanel.add(jobControlsCard, BorderLayout.NORTH);
            JScrollPane jobScrollPane = new JScrollPane(jobTable);
            jobScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobScrollPane.getViewport().setBackground(Color.WHITE);
            jobRecommendationSummaryLabel = new JLabel("Select a job to view the AI recommendation explanation.");
            jobRecommendationSummaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            jobRecommendationSummaryLabel.setForeground(QMUL_PURPLE);
            jobRecommendationDetailsArea = new JTextArea(4, 20);
            jobRecommendationDetailsArea.setEditable(false);
            jobRecommendationDetailsArea.setLineWrap(true);
            jobRecommendationDetailsArea.setWrapStyleWord(true);
            jobRecommendationDetailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            jobRecommendationDetailsArea.setBackground(new Color(250, 246, 255));
            jobRecommendationDetailsArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            jobRecommendationDetailsArea.setText("Recommended jobs are ranked by skill overlap and weekly-hour fit.");
            JPanel recommendationPanel = new JPanel(new BorderLayout(0, 8));
            recommendationPanel.setOpaque(false);
            recommendationPanel.add(jobRecommendationSummaryLabel, BorderLayout.NORTH);
            recommendationPanel.add(new JScrollPane(jobRecommendationDetailsArea), BorderLayout.CENTER);
            JPanel jobCenterPanel = new JPanel(new BorderLayout(0, 12));
            jobCenterPanel.setOpaque(false);
            jobCenterPanel.add(createCardPanel(jobScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            jobCenterPanel.add(createCardPanel(recommendationPanel, 14, 16, 14, 16), BorderLayout.SOUTH);
            jobBoardPanel.add(jobCenterPanel, BorderLayout.CENTER);
            jobTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    updateSelectedJobRecommendation();
                }
            });
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            jobActions.setOpaque(false);
            JButton detailButton = new JButton("View Details");
            styleSecondaryButton(detailButton);
            detailButton.addActionListener(e -> showJobDetails());
            JButton applyButton = new JButton("Apply Now");
            stylePrimaryButton(applyButton);
            applyButton.addActionListener(e -> applySelectedJob());
            jobActions.add(detailButton);
            jobActions.add(applyButton);
            jobBoardPanel.add(jobActions, BorderLayout.SOUTH);

            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setOpaque(false);
            profilePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JPanel profileHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
            profileHeader.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)));
            profileHeader.setBackground(CARD_WHITE);
            JPanel profileHeaderCard = createCardPanel(profileHeader, 0, 0, 0, 0);
            profilePanel.add(profileHeaderCard, BorderLayout.NORTH);
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            profileNameField = new JTextField();
            profileYearField = new JTextField();
            profileProgrammeField = new JTextField();
            profileSkillsArea = new JTextArea(4, 20);
            profileHoursField = new JTextField();
            cvLabel = new JLabel("No CV uploaded");

            JPanel cvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            cvPanel.setOpaque(false);
            JButton cvButton = new JButton("Choose File");
            styleSecondaryButton(cvButton);
            cvButton.addActionListener(e -> chooseCvFile());
            JButton viewCvButton = new JButton("View CV");
            styleSecondaryButton(viewCvButton);
            viewCvButton.addActionListener(e -> viewMyCv());
            cvPanel.add(cvButton);
            cvPanel.add(Box.createHorizontalStrut(10));
            cvPanel.add(viewCvButton);
            cvPanel.add(Box.createHorizontalStrut(10));
            cvPanel.add(cvLabel);

            JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            avatarPanel.setOpaque(false);
            for (String fileName : presetAvatarFiles) {
                avatarPanel.add(createAvatarChoiceButton(fileName, fileName, chosenFileName -> {
                    selectedAvatarPath = AVATAR_DIR + "/" + chosenFileName;
                    profileAvatarLabel.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
                    updateTopBarAvatar(selectedAvatarPath);
                }));
            }
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 0, 10, 20);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.NONE;
            
            int row = 0;
            // Name
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            JLabel nameLabel = new JLabel("Name");
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(nameLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(profileNameField, gbc);
            row++;
            
            // Year of Study
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel yearLabel = new JLabel("Year of Study");
            yearLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(yearLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(profileYearField, gbc);
            row++;
            
            // Programme
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel progLabel = new JLabel("Programme");
            progLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(progLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(profileProgrammeField, gbc);
            row++;
            
            // Skills
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            JLabel skillsLabel = new JLabel("Skills (comma-separated)");
            skillsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(skillsLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(new JScrollPane(profileSkillsArea), gbc);
            row++;
            
            // Hours
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            JLabel hoursLabel = new JLabel("Available Hours/Week");
            hoursLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(hoursLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(profileHoursField, gbc);
            row++;
            
            // CV
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel cvTextLabel = new JLabel("Upload CV (.pdf/.txt)");
            cvTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(cvTextLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(cvPanel, gbc);
            row++;
            
            // Avatar
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
            JLabel avatarTextLabel = new JLabel("Avatar Photo");
            avatarTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            form.add(avatarTextLabel, gbc);
            gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(avatarPanel, gbc);
            row++;

            JTextField[] taFields = {profileNameField, profileYearField, profileProgrammeField, profileHoursField};
            for (JTextField f : taFields) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            profileSkillsArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            
            JPanel formContainer = new JPanel(new BorderLayout());
            formContainer.setOpaque(false);
            formContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            formContainer.add(form, BorderLayout.NORTH);
            
            JPanel cardForm = createCardPanel(formContainer, 0, 0, 0, 0);
            cardForm.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JPanel formWrapper = new JPanel();
            formWrapper.setLayout(new BoxLayout(formWrapper, BoxLayout.Y_AXIS));
            formWrapper.setOpaque(false);
            formWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20)); // add right margin so it doesn't touch the scrollbar
            formWrapper.add(cardForm);
            
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
            profileActions.setOpaque(false);
            profileActions.setAlignmentX(Component.LEFT_ALIGNMENT);
            JButton saveButton = new JButton("Save Profile");
            stylePrimaryButton(saveButton);
            saveButton.addActionListener(e -> saveProfile());
            profileActions.add(saveButton);
            formWrapper.add(profileActions);
            
            JPanel scrollWrapper = new JPanel(new BorderLayout());
            scrollWrapper.setOpaque(false);
            scrollWrapper.add(formWrapper, BorderLayout.NORTH);
            
            JScrollPane scrollPane = new JScrollPane(scrollWrapper);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            scrollPane.setOpaque(false);
            scrollPane.getViewport().setOpaque(false);
            
            profilePanel.add(scrollPane, BorderLayout.CENTER);

            notificationModel = new DefaultTableModel(
                    new Object[] {"ID", "Read", "Type", "Message", "Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            notificationTable = new JTable(notificationModel);
            styleDataTable(notificationTable);
            notificationTable.setRowHeight(62);
            notificationTable.setShowGrid(false);
            notificationTable.setIntercellSpacing(new Dimension(0, 0));
            notificationTable.getTableHeader().setVisible(false);
            notificationTable.getTableHeader().setPreferredSize(new Dimension(0, 0));
            notificationTable.setFillsViewportHeight(true);
            installTableRowHover(notificationTable);
            notificationTable.getColumnModel().getColumn(1).setMaxWidth(56);
            notificationTable.getColumnModel().getColumn(2).setPreferredWidth(180);
            notificationTable.getColumnModel().getColumn(3).setPreferredWidth(420);
            notificationTable.getColumnModel().getColumn(4).setPreferredWidth(90);
            notificationTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    String state = value == null ? "" : String.valueOf(value);
                    setHorizontalAlignment(CENTER);
                    setText("Unread".equalsIgnoreCase(state) ? "●" : "○");
                    setForeground("Unread".equalsIgnoreCase(state) ? QMUL_PURPLE : new Color(190, 190, 190));
                    setFont(getFont().deriveFont(Font.BOLD, 16f));
                }
            });
            notificationTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    String type = value == null ? "" : String.valueOf(value);
                    setText(notificationIcon(type) + "  " + type);
                    setFont(getFont().deriveFont(Font.BOLD, 13f));
                    setForeground(new Color(50, 50, 70));
                }
            });
            notificationTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    setText("<html><span style='color:#5f6368'>" + safeText(String.valueOf(value)) + "</span></html>");
                    setFont(getFont().deriveFont(Font.PLAIN, 12f));
                }
            });
            notificationTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                protected void setValue(Object value) {
                    setHorizontalAlignment(RIGHT);
                    setText(value == null ? "" : String.valueOf(value));
                    setForeground(MUTED_TEXT_COLOR);
                    setFont(getFont().deriveFont(Font.PLAIN, 12f));
                }
            });
            notificationTable.getColumnModel().getColumn(0).setMinWidth(0);
            notificationTable.getColumnModel().getColumn(0).setMaxWidth(0);
            notificationTable.getColumnModel().getColumn(0).setPreferredWidth(0);
            JPanel notificationsPanel = new JPanel(new BorderLayout(0, 16));
            notificationsPanel.setOpaque(false);
            notificationsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JPanel notificationTopPanel = new JPanel(new BorderLayout(10, 10));
            notificationTopPanel.setOpaque(false);
            JPanel titleWrap = new JPanel(new GridLayout(0, 1));
            titleWrap.setOpaque(false);
            JLabel notificationsTitleLabel = new JLabel("Notifications");
            notificationsTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
            JLabel notificationsSubtitleLabel = new JLabel("Stay updated with the latest news and updates.");
            notificationsSubtitleLabel.setForeground(MUTED_TEXT_COLOR);
            titleWrap.add(notificationsTitleLabel);
            titleWrap.add(notificationsSubtitleLabel);
            notificationTopPanel.add(titleWrap, BorderLayout.WEST);
            JPanel notificationControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            notificationControls.setOpaque(false);
            unreadCountLabel = new JLabel("Unread: 0");
            notificationFilterBox = new JComboBox<>(new String[] {"All", "Unread", "Read"});
            notificationFilterBox.addActionListener(e -> refreshNotifications());
            JButton refreshNotificationsButton = new JButton("Refresh");
            styleSecondaryButton(refreshNotificationsButton);
            refreshNotificationsButton.addActionListener(e -> refreshNotifications());
            JButton markReadButton = new JButton("Mark Read");
            stylePrimaryButton(markReadButton);
            markReadButton.addActionListener(e -> setSelectedNotificationRead(true));
            JButton markUnreadButton = new JButton("Mark Unread");
            styleSecondaryButton(markUnreadButton);
            markUnreadButton.addActionListener(e -> setSelectedNotificationRead(false));
            
            JButton viewDetailsButton = new JButton("View Details");
            styleSecondaryButton(viewDetailsButton);
            viewDetailsButton.addActionListener(e -> showNotificationDetails(notificationTable));

            notificationControls.add(unreadCountLabel);
            notificationControls.add(new JLabel("Show"));
            notificationControls.add(notificationFilterBox);
            notificationControls.add(refreshNotificationsButton);
            notificationControls.add(viewDetailsButton);
            notificationControls.add(markReadButton);
            notificationControls.add(markUnreadButton);
            notificationTopPanel.add(notificationControls, BorderLayout.EAST);
            notificationEmptyLabel = new JLabel("No notifications to show for this filter.");
            notificationEmptyLabel.setForeground(MUTED_TEXT_COLOR);

            JPanel notificationCard = createCardPanel(new JScrollPane(notificationTable), 0, 0, 0, 0);
            JScrollPane notificationScrollPane = (JScrollPane) notificationCard.getComponent(0);
            notificationScrollPane.setBorder(BorderFactory.createEmptyBorder());
            notificationScrollPane.getViewport().setBackground(Color.WHITE);
            notificationCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)));

            JPanel notificationHeaderCard = createCardPanel(notificationTopPanel, 18, 18, 18, 18);
            notificationsPanel.add(notificationHeaderCard, BorderLayout.NORTH);
            notificationsPanel.add(notificationCard, BorderLayout.CENTER);
            JPanel notificationFooterPanel = new JPanel(new BorderLayout());
            notificationFooterPanel.setOpaque(false);
            JLabel pagerSummaryLabel = new JLabel("Showing latest notifications");
            pagerSummaryLabel.setForeground(MUTED_TEXT_COLOR);
            JPanel pagerControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pagerControlsPanel.setOpaque(false);
            pagerControlsPanel.add(createPaginationChip("<", false));
            pagerControlsPanel.add(createPaginationChip("1", true));
            pagerControlsPanel.add(createPaginationChip("2", false));
            pagerControlsPanel.add(createPaginationChip("3", false));
            pagerControlsPanel.add(createPaginationChip(">", false));
            notificationFooterPanel.add(pagerSummaryLabel, BorderLayout.WEST);
            notificationFooterPanel.add(pagerControlsPanel, BorderLayout.EAST);

            JPanel notificationsBottom = new JPanel(new BorderLayout());
            notificationsBottom.setOpaque(false);
            notificationsBottom.add(notificationEmptyLabel, BorderLayout.NORTH);
            notificationsBottom.add(notificationFooterPanel, BorderLayout.SOUTH);
            notificationsPanel.add(notificationsBottom, BorderLayout.SOUTH);

            contentPanel.add(dashboardPanel, TAB_DASHBOARD);
            contentPanel.add(jobBoardPanel, TAB_JOB_BOARD);
            contentPanel.add(notificationsPanel, TAB_NOTIFICATIONS);
            contentPanel.add(profilePanel, TAB_PROFILE);
            add(contentPanel, BorderLayout.CENTER);
        }

        private JPanel createDashboardStatCard(String title, JLabel valueLabel) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                    BorderFactory.createEmptyBorder(10, 12, 10, 12)));
            JLabel titleLabel = new JLabel(title);
            titleLabel.setForeground(MUTED_TEXT_COLOR);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            valueLabel.setForeground(PRIMARY_BUTTON_COLOR);
            card.add(titleLabel, BorderLayout.NORTH);
            card.add(valueLabel, BorderLayout.CENTER);
            return card;
        }

        private JButton createPaginationChip(String text, boolean active) {
            JButton chip = new JButton(text);
            chip.setEnabled(false);
            chip.setFocusPainted(false);
            chip.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            chip.setFont(new Font("Segoe UI", Font.BOLD, 12));
            if (active) {
                chip.setBackground(QMUL_PURPLE);
                chip.setForeground(Color.WHITE);
            } else {
                chip.setBackground(Color.WHITE);
                chip.setForeground(MUTED_TEXT_COLOR);
            }
            return chip;
        }

        private void bindUser(User user) {
            this.user = user;
            refreshApplications();
            refreshJobs();
            refreshNotifications();
            loadProfile();
            updateTopBarAvatar(user == null ? "" : user.getAvatarFilePath());
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
            dashboardAppliedCountLabel.setText(String.valueOf(applications == null ? 0 : applications.size()));
            dashboardPendingCountLabel.setText(String.valueOf(summary.getPending()));
            dashboardAcceptedCountLabel.setText(String.valueOf(summary.getAccepted()));
        }

        private void refreshNotifications() {
            notificationModel.setRowCount(0);
            List<NotificationEntry> notifications = buildTaNotifications();
            int unreadCount = notificationService == null
                    ? TaNotificationUtil.countUnread(notifications, readNotificationIds)
                    : notificationService.countUnreadForUser(user.getId());
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
            if (notificationService != null) {
                List<Notification> persisted = notificationService.getNotificationsForUser(user.getId());
                readNotificationIds.clear();
                List<NotificationEntry> entries = new ArrayList<>();
                for (Notification notification : persisted) {
                    if (notification.isReadStatus()) {
                        readNotificationIds.add(notification.getId());
                    }
                    entries.add(new NotificationEntry(
                            notification.getId(),
                            notificationTypeLabel(notification),
                            safeText(notification.getMessage()),
                            safeText(notification.getCreatedAt())));
                }
                return entries;
            }
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
            if (notificationService != null) {
                try {
                    notificationService.setReadStatus(notificationId, read);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                    return;
                }
            } else {
                if (read) {
                    readNotificationIds.add(notificationId);
                } else {
                    readNotificationIds.remove(notificationId);
                }
            }
            refreshNotifications();
        }

        private String notificationTypeLabel(Notification notification) {
            if (notification == null || notification.getType() == null) {
                return "Notification";
            }
            return switch (notification.getType()) {
                case APPLY -> "Application Submitted";
                case WITHDRAW -> "Application Withdrawn";
                case ACCEPT -> "Application Accepted";
                case REJECT -> "Application Rejected";
                case JOB_CLOSE -> "Job Closed";
                case JOB_REOPEN -> "Job Reopened";
                case OVERLOAD_ALERT -> "Overload Alert";
                case ADMIN_ACCOUNT_CREATED -> "Account Created";
                case ADMIN_ACCOUNT_STATUS_CHANGED -> "Account Status Changed";
                case ADMIN_JOB_FORCE_CLOSED -> "Job Force-Closed";
                case ADMIN_JOBS_AUTO_CLOSED -> "Jobs Auto-Closed";
                case SYSTEM_ALERT -> "System Alert";
            };
        }

        private void refreshJobs() {
            jobService.autoCloseExpiredJobs();
            jobModel.setRowCount(0);
            String query = searchField.getText();
            String skillQuery = skillsFilterField.getText();
            String moQuery = moFilterField.getText();
            String statusValue = String.valueOf(statusFilterBox.getSelectedItem());
            Integer maxHours = parseHoursFilter();
            if (maxHours != null && maxHours < 0) {
                return;
            }
            List<Job> visibleJobs = new ArrayList<>();
            int acceptedHours = acceptedHoursForTa(user.getId());
            boolean recommendedOnly = recommendedOnlyBox.isSelected();
            for (Job job : jobService.getAllJobs()) {
                String moName = safeText(moNameForJob(job));
                if (!JobFilterUtil.matches(job, query, skillQuery, maxHours, moQuery, statusValue, moName)) {
                    continue;
                }
                AiMatchingService.RecommendationResult recommendation =
                        aiMatchingService.recommendJob(user, job, acceptedHours);
                if (recommendedOnly && !recommendation.isRecommended()) {
                    continue;
                }
                visibleJobs.add(job);
            }
            visibleJobs.sort(Comparator
                    .comparingInt((Job job) -> aiMatchingService.recommendJob(user, job, acceptedHours).getScore())
                    .reversed()
                    .thenComparing(job -> safeText(job.getDeadline())));
            for (Job job : visibleJobs) {
                String moduleCode = safeText(job.getModuleCode());
                String moduleName = safeText(job.getModuleName());
                String moName = safeText(moNameForJob(job));
                AiMatchingService.RecommendationResult recommendation =
                        aiMatchingService.recommendJob(user, job, acceptedHours);
                jobModel.addRow(new Object[] {
                    safeText(job.getId()),
                    moduleCode + " - " + moduleName,
                    moName,
                    job.getHoursPerWeek(),
                    safeText(job.getDeadline()),
                    job.getStatus().name(),
                    recommendation.getScore() + "%",
                    recommendation.getLabel()
                });
            }
            if (jobModel.getRowCount() > 0) {
                jobTable.setRowSelectionInterval(0, 0);
            } else {
                updateSelectedJobRecommendation();
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
                showToast("Application Submitted", "Application submitted successfully.", JOptionPane.INFORMATION_MESSAGE);
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
            AiMatchingService.MatchResult matchResult =
                    aiMatchingService.analyzeSkills(user.getSkills(), job.getRequiredSkills());
            AiMatchingService.RecommendationResult recommendation =
                    aiMatchingService.recommendJob(user, job, acceptedHoursForTa(user.getId()));
            String matchedSkills = matchResult.getMatchedSkills().isEmpty()
                    ? "None"
                    : String.join(", ", matchResult.getMatchedSkills());
            String missingSkills = matchResult.getMissingSkills().isEmpty()
                    ? "None"
                    : String.join(", ", matchResult.getMissingSkills());
            String message = "<html><div style='width:460px'>"
                    + "<h3>Job Overview</h3>"
                    + "<b>Module:</b> " + safeText(job.getModuleCode()) + " - " + safeText(job.getModuleName()) + "<br/>"
                    + "<b>MO:</b> " + moNameForJob(job) + "<br/>"
                    + "<b>Required Skills:</b> " + safeText(job.getRequiredSkills()) + "<br/>"
                    + "<b>Weekly Hours:</b> " + job.getHoursPerWeek() + "<br/>"
                    + "<b>Deadline:</b> " + safeText(job.getDeadline()) + "<br/><br/>"
                    + "<h3>Description</h3>"
                    + safeText(job.getDescription()) + "<br/><br/>"
                    + "<h3>AI Matching</h3>"
                    + "<b>Match Score:</b> " + matchResult.getScore() + "%<br/>"
                    + "<b>Recommendation:</b> " + recommendation.getLabel()
                    + " (" + recommendation.getScore() + "%)<br/>"
                    + "<b>Recommendation Progress:</b> " + scoreProgressBar(recommendation.getScore()) + "<br/>"
                    + "<b>Matched Skills:</b> " + matchedSkills + "<br/>"
                    + "<b>Missing Skills:</b> " + missingSkills + "<br/>"
                    + "<b>Hours Fit:</b> " + (recommendation.isHoursFit() ? "Yes" : "Needs review")
                    + " (" + recommendation.getRemainingHours() + "h/week remaining)<br/>"
                    + "<b>Reason:</b> " + recommendation.getReason() + "<br/><br/>"
                    + "<b>Next Step:</b> " + recommendation.getActionHint() + "<br/><br/>"
                    + "<span style='color:#5A2382'><b>" + BRAND_TAGLINE + "</b></span>"
                    + "</div></html>";
            JOptionPane.showMessageDialog(frame, message, "Job Details", JOptionPane.INFORMATION_MESSAGE);
        }

        private void updateSelectedJobRecommendation() {
            if (jobModel.getRowCount() == 0) {
                jobRecommendationSummaryLabel.setText("No jobs match the current filters.");
                jobRecommendationDetailsArea.setText(recommendedOnlyBox.isSelected()
                        ? "No strongly recommended jobs are available under the current filters. Try clearing "
                                + "'Recommended only' or broadening the skill/hour filters."
                        : "Adjust the search, skill, MO, status, or hour filters to find more jobs.");
                return;
            }
            int selected = jobTable.getSelectedRow();
            if (selected < 0) {
                jobRecommendationSummaryLabel.setText("Select a job to view the AI recommendation explanation.");
                jobRecommendationDetailsArea.setText(
                        "Recommended jobs are ranked by skill overlap and weekly-hour fit.");
                return;
            }

            String jobId = String.valueOf(jobModel.getValueAt(selected, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                jobRecommendationSummaryLabel.setText("Selected job could not be found.");
                jobRecommendationDetailsArea.setText("Refresh the job board and select the job again.");
                return;
            }

            AiMatchingService.MatchResult matchResult =
                    aiMatchingService.analyzeSkills(user.getSkills(), job.getRequiredSkills());
            AiMatchingService.RecommendationResult recommendation =
                    aiMatchingService.recommendJob(user, job, acceptedHoursForTa(user.getId()));
            jobRecommendationSummaryLabel.setText("AI Fit: " + recommendation.getLabel()
                    + " (" + recommendation.getScore() + "%)");
            String details = "Why this ranking?\n"
                    + "- " + recommendation.getReason() + "\n"
                    + "- Matched skills: " + formatSkillList(matchResult.getMatchedSkills()) + "\n"
                    + "- Missing skills: " + formatSkillList(matchResult.getMissingSkills()) + "\n"
                    + "- Hours check: " + (recommendation.isHoursFit() ? "fits current availability" : "needs review")
                    + " with " + recommendation.getRemainingHours() + "h/week remaining.\n"
                    + "- Suggested action: " + recommendation.getActionHint();
            jobRecommendationDetailsArea.setText(details);
            jobRecommendationDetailsArea.setCaretPosition(0);
        }

        private String formatSkillList(List<String> skills) {
            return skills == null || skills.isEmpty() ? "none" : String.join(", ", skills);
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
            showToast("Application Updated", "Application withdrawn successfully.", JOptionPane.INFORMATION_MESSAGE);
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
            selectedAvatarPath = safeText(user.getAvatarFilePath());
            cvLabel.setText(selectedCvName.isBlank() ? "No CV uploaded" : selectedCvName);
            profileAvatarLabel.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
            updateTopBarAvatar(selectedAvatarPath);
        }

        private void saveProfile() {
            try {
                user.setName(profileNameField.getText().trim());
                user.setYearOfStudy(ValidationUtil.parseIntInRange(profileYearField.getText(), "Year of study", 1, 12));
                user.setProgramme(profileProgrammeField.getText().trim());
                user.setSkills(profileSkillsArea.getText().trim());
                user.setAvailableHours(ValidationUtil.parseIntInRange(profileHoursField.getText(), "Available hours", 1, 168));
                user.setCvFilePath(selectedCvPath);
                user.setAvatarFilePath(selectedAvatarPath);
                authService.updateUser(user);
                updateTopBarAvatar(selectedAvatarPath);
                showToast("Profile Saved", "Your profile has been updated.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void chooseCvFile() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("CV files (*.pdf, *.txt)", "pdf", "txt"));
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

        private void chooseAvatarFile() {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image files (*.png, *.jpg, *.jpeg, *.webp)", "png", "jpg", "jpeg", "webp"));
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
                File selectedFile = chooser.getSelectedFile();
                try {
                    Path savedPath = saveAvatarFileToDataDir(selectedFile, user.getId());
                    if (savedPath == null) {
                        return;
                    }
                    selectedAvatarPath = savedPath.toAbsolutePath().toString();
                    profileAvatarLabel.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
                    updateTopBarAvatar(selectedAvatarPath);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frame, ex.getMessage());
                }
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
        static final String TAB_DASHBOARD = "dashboard";
        static final String TAB_APPLICANTS = "applicants";
        static final String TAB_PROFILE = "profile";
        static final String TAB_NOTIFICATIONS = "notifications";

        void demoShowTab(String tab) {
            switch (tab) {
                case TAB_DASHBOARD:
                    refreshJobs();
                    break;
                case TAB_APPLICANTS:
                    refreshApplicants();
                    break;
                case TAB_PROFILE:
                    loadProfile();
                    break;
                case TAB_NOTIFICATIONS:
                    refreshMoNotificationsTab();
                    break;
                default:
                    return;
            }
            contentLayout.show(contentPanel, tab);
        }

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel jobsModel;
        private final JTable jobsTable;
        private final DefaultTableModel applicantsModel;
        private final JTable applicantsTable;
        private final JLabel applicantsTitle;
        private final DefaultTableModel notificationsModel;
        private final JTable notificationsTable;
        private JCheckBox pendingOnlyCheckBox;
        private JCheckBox needsDecisionCheckBox;
        private final JSpinner matchThresholdSpinner;
        private final Map<String, MoApplicantRankingService.RankedApplicant> rankedApplicantsByApplicationId;
        private final JTextField profileNameField;
        private final JTextField profileProgrammeField;
        private final JTextField profileEmailField;
        private final JTextField profileHoursField;
        private User user;
        private String selectedJobId;
        private MoApplicantRankingService.SortMode applicantSortMode;

        private MoPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("MO Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("MO Dashboard", SwingApp.this::showLoginPage);
            add(topBar, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);

            String[] navLabels = {"Dashboard", "Applicants List", "My Profile", "Notifications"};
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
                },
                () -> {
                    refreshMoNotificationsTab();
                    contentLayout.show(contentPanel, TAB_NOTIFICATIONS);
                }
            };
            add(buildNavigationPanel(navLabels, navActions), BorderLayout.WEST);
            applicantSortMode = MoApplicantRankingService.SortMode.MATCH_SCORE_DESC;
            rankedApplicantsByApplicationId = new LinkedHashMap<>();

            jobsModel = new DefaultTableModel(
                    new Object[] {"Job ID", "Module", "Positions", "Filled", "Status", "Deadline"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobsTable = new JTable(jobsModel);
            jobsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
            jobsTable.getColumnModel().getColumn(1).setPreferredWidth(250);
            jobsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
            jobsTable.getColumnModel().getColumn(3).setPreferredWidth(60);
            jobsTable.getColumnModel().getColumn(4).setPreferredWidth(100);
            jobsTable.getColumnModel().getColumn(5).setPreferredWidth(90);
            styleDataTable(jobsTable);
            applyStatusRenderer(jobsTable, 4);
            installTableRowHover(jobsTable);
            JPanel dashboardPanel = new JPanel(new BorderLayout(0, 16));
            dashboardPanel.setOpaque(false);
            JPanel dashboardTopStack = new JPanel();
            dashboardTopStack.setOpaque(false);
            dashboardTopStack.setLayout(new BoxLayout(dashboardTopStack, BoxLayout.Y_AXIS));

            // ========================= Notifications Tab =========================
            notificationsModel = new DefaultTableModel(
                    new Object[] {"Type", "Related ID", "Message", "Time", "Read"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            notificationsTable = new JTable(notificationsModel);
            styleDataTable(notificationsTable);
            installTableRowHover(notificationsTable);
            
            JPanel notificationsPanel = new JPanel(new BorderLayout(0, 16));
            notificationsPanel.setOpaque(false);
            JPanel notifHeader = new JPanel(new BorderLayout());
            notifHeader.setOpaque(false);
            notifHeader.add(createSectionTitle("MO Notifications", "Updates on your posted jobs and applications."), BorderLayout.WEST);
            notificationsPanel.add(createCardPanel(notifHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            
            JScrollPane notifScrollPane = new JScrollPane(notificationsTable);
            notifScrollPane.setBorder(BorderFactory.createEmptyBorder());
            notifScrollPane.getViewport().setBackground(Color.WHITE);
            notificationsPanel.add(createCardPanel(notifScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            
            JPanel notifFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            notifFooter.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            notifFooter.setOpaque(false);
            
            JButton viewDetailsButton = new JButton("View Details");
            styleSecondaryButton(viewDetailsButton);
            viewDetailsButton.addActionListener(e -> showNotificationDetails(notificationsTable));
            notifFooter.add(viewDetailsButton);
            
            JButton sendNotifButton = new JButton("Send Notification");
            stylePrimaryButton(sendNotifButton);
            sendNotifButton.addActionListener(e -> showMoSendNotificationDialog());
            notifFooter.add(sendNotifButton);
            
            JButton markReadButton = new JButton("Mark All Read");
            styleSecondaryButton(markReadButton);
            markReadButton.addActionListener(e -> {
                if (user != null && notificationService != null) {
                    java.util.List<Notification> notifs = notificationService.getNotificationsForUser(user.getId());
                    for (Notification n : notifs) {
                        if (!n.isReadStatus()) {
                            notificationService.setReadStatus(n.getId(), true);
                        }
                    }
                    refreshMoNotificationsTab();
                }
            });
            notifFooter.add(markReadButton);
            notificationsPanel.add(notifFooter, BorderLayout.SOUTH);

            JPanel dashboardHeader = new JPanel(new BorderLayout());
            dashboardHeader.setOpaque(false);
            JPanel dashboardHeaderText = createSectionTitle("My Posted Jobs", "Overview of jobs, applicants, and statuses.");
            dashboardHeader.add(dashboardHeaderText, BorderLayout.WEST);
            dashboardTopStack.add(createCardPanel(dashboardHeader, 18, 18, 18, 18));
            dashboardPanel.add(dashboardTopStack, BorderLayout.NORTH);
            JScrollPane jobsScrollPane = new JScrollPane(jobsTable);
            jobsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobsScrollPane.getViewport().setBackground(Color.WHITE);
            dashboardPanel.add(createCardPanel(jobsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
            jobActions.setBorder(BorderFactory.createEmptyBorder(4, 0, 10, 0));
            jobActions.setOpaque(false);
            JButton postButton = new JButton("Post New Job");
            stylePrimaryButton(postButton);
            postButton.addActionListener(e -> createJob());
            JButton editButton = new JButton("Edit");
            styleSecondaryButton(editButton);
            editButton.addActionListener(e -> editSelectedJob());
            JButton closeButton = new JButton("Close Job");
            styleDangerButton(closeButton);
            closeButton.addActionListener(e -> closeSelectedJob());
            JButton reopenButton = new JButton("Reopen Job");
            styleSecondaryButton(reopenButton);
            reopenButton.addActionListener(e -> reopenSelectedJob());
            JButton deleteButton = new JButton("Delete");
            styleDangerButton(deleteButton);
            deleteButton.addActionListener(e -> deleteSelectedJob());
            JButton applicantsButton = new JButton("View Applicants");
            stylePrimaryButton(applicantsButton);
            applicantsButton.addActionListener(e -> openApplicantsForSelectedJob());
            JButton refreshButton = new JButton("Refresh");
            styleSecondaryButton(refreshButton);
            refreshButton.addActionListener(e -> refreshJobs());
            jobActions.add(postButton);
            jobActions.add(editButton);
            jobActions.add(closeButton);
            jobActions.add(reopenButton);
            jobActions.add(deleteButton);
            jobActions.add(applicantsButton);
            jobActions.add(refreshButton);
            dashboardPanel.add(jobActions, BorderLayout.SOUTH);

            applicantsModel = new DefaultTableModel(
                    new Object[] {
                        "App ID", "Job", "Applicant", "Year", "Match Score", "Missing Skills",
                        "Current Workload", "Recommendation", "Status"
                    }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 3:
                        case 4:
                        case 6:
                            return Integer.class;
                        default:
                            return String.class;
                    }
                }
            };
            applicantsTable = new JTable(applicantsModel);
            styleDataTable(applicantsTable);
            applyIntegerSuffixRenderer(applicantsTable, 4, "%");
            applyIntegerSuffixRenderer(applicantsTable, 6, "h/week");
            applyRecommendationRenderer(applicantsTable, 7);
            applyStatusRenderer(applicantsTable, 8);
            installTableRowHover(applicantsTable);
            JPanel applicantsPanel = new JPanel(new BorderLayout(0, 16));
            applicantsPanel.setOpaque(false);
            applicantsTitle = new JLabel("Applicants List");
            applicantsTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            applicantsTitle.setForeground(MUTED_TEXT_COLOR);
            JPanel applicantsHeader = new JPanel(new BorderLayout());
            applicantsHeader.setOpaque(false);
            JPanel applicantsHeaderText = new JPanel();
            applicantsHeaderText.setOpaque(false);
            applicantsHeaderText.setLayout(new BoxLayout(applicantsHeaderText, BoxLayout.Y_AXIS));
            applicantsHeaderText.add(createSectionTitle(
                    "Applicants", "Review pending candidates with match score, missing skills, and workload."));
            applicantsHeaderText.add(Box.createVerticalStrut(6));
            applicantsHeaderText.add(applicantsTitle);
            applicantsHeader.add(applicantsHeaderText, BorderLayout.WEST);
            JPanel applicantsControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            applicantsControls.setOpaque(false);
            pendingOnlyCheckBox = new JCheckBox("Pending only", true);
            pendingOnlyCheckBox.setOpaque(false);
            pendingOnlyCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            pendingOnlyCheckBox.addActionListener(e -> {
                if (pendingOnlyCheckBox.isSelected()) {
                    needsDecisionCheckBox.setSelected(false);
                }
                refreshApplicants();
            });
            applicantsControls.add(pendingOnlyCheckBox);
            needsDecisionCheckBox = new JCheckBox("Needs decision", false);
            needsDecisionCheckBox.setOpaque(false);
            needsDecisionCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            needsDecisionCheckBox.addActionListener(e -> {
                if (needsDecisionCheckBox.isSelected()) {
                    pendingOnlyCheckBox.setSelected(false);
                }
                refreshApplicants();
            });
            applicantsControls.add(needsDecisionCheckBox);
            applicantsControls.add(new JLabel("Min match score"));
            matchThresholdSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_MO_MATCH_THRESHOLD, 0, 100, 5));
            matchThresholdSpinner.setPreferredSize(new Dimension(70, 32));
            matchThresholdSpinner.addChangeListener(e -> refreshApplicants());
            applicantsControls.add(matchThresholdSpinner);
            applicantsHeader.add(applicantsControls, BorderLayout.EAST);
            applicantsPanel.add(createCardPanel(applicantsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane applicantsScrollPane = new JScrollPane(applicantsTable);
            applicantsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            applicantsScrollPane.getViewport().setBackground(Color.WHITE);
            applicantsPanel.add(createCardPanel(applicantsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel applicantActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            applicantActions.setOpaque(false);
            JButton acceptButton = new JButton("Accept");
            stylePrimaryButton(acceptButton);
            acceptButton.addActionListener(e -> updateApplicantStatus(ApplicationStatus.ACCEPTED));
            JButton rejectButton = new JButton("Reject");
            styleDangerButton(rejectButton);
            rejectButton.addActionListener(e -> updateApplicantStatus(ApplicationStatus.REJECTED));
            JButton viewProfileButton = new JButton("View Applicant Details");
            styleSecondaryButton(viewProfileButton);
            viewProfileButton.addActionListener(e -> viewSelectedApplicantProfile());
            JButton viewCvButton = new JButton("View Applicant CV");
            styleSecondaryButton(viewCvButton);
            viewCvButton.addActionListener(e -> viewSelectedApplicantCv());
            JButton explanationButton = new JButton("View Explanation");
            styleSecondaryButton(explanationButton);
            explanationButton.addActionListener(e -> viewSelectedApplicantExplanation());
            JButton refreshApplicantsButton = new JButton("Refresh");
            styleSecondaryButton(refreshApplicantsButton);
            refreshApplicantsButton.addActionListener(e -> refreshApplicants());
            JButton sortMatchButton = new JButton("High match first");
            stylePrimaryButton(sortMatchButton);
            sortMatchButton.addActionListener(e -> sortApplicantsByMatchScore());
            JButton sortWorkloadButton = new JButton("Sort by Workload");
            styleSecondaryButton(sortWorkloadButton);
            sortWorkloadButton.addActionListener(e -> sortApplicantsByWorkload());
            applicantActions.add(acceptButton);
            applicantActions.add(rejectButton);
            applicantActions.add(viewProfileButton);
            applicantActions.add(viewCvButton);
            applicantActions.add(explanationButton);
            applicantActions.add(sortMatchButton);
            applicantActions.add(sortWorkloadButton);
            applicantActions.add(refreshApplicantsButton);
            applicantsPanel.add(applicantActions, BorderLayout.SOUTH);

            JPanel profilePanel = new JPanel(new BorderLayout());
            profilePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            profilePanel.setOpaque(false);
            JPanel profileCard = new JPanel(new BorderLayout(0, 24));
            profileCard.setBackground(CARD_WHITE);
            profileCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CARD_BORDER),
                    BorderFactory.createEmptyBorder(30, 30, 30, 30)));
            profileCard.add(createSectionTitle("My Profile", "View and edit your personal information"), BorderLayout.NORTH);
            JPanel profileForm = new JPanel(new GridLayout(0, 2, 16, 20));
            profileForm.setOpaque(false);
            profileNameField = new JTextField();
            profileProgrammeField = new JTextField();
            profileEmailField = new JTextField();
            profileHoursField = new JTextField();
            JTextField[] moFields = {profileNameField, profileProgrammeField, profileEmailField, profileHoursField};
            for (JTextField f : moFields) {
                f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
            profileForm.add(new JLabel("Full Name"));
            profileForm.add(profileNameField);
            profileForm.add(new JLabel("Department/School"));
            profileForm.add(profileProgrammeField);
            profileForm.add(new JLabel("Official Email"));
            profileForm.add(profileEmailField);
            profileForm.add(new JLabel("Available Hours/Week"));
            profileForm.add(profileHoursField);
            JPanel formWrapper = new JPanel(new BorderLayout());
            formWrapper.setOpaque(false);
            formWrapper.add(profileForm, BorderLayout.NORTH);
            profileCard.add(formWrapper, BorderLayout.CENTER);
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            profileActions.setOpaque(false);
            JButton saveProfileButton = new JButton("Save Profile");
            stylePrimaryButton(saveProfileButton);
            saveProfileButton.addActionListener(e -> saveProfile());
            profileActions.add(saveProfileButton);
            profileCard.add(profileActions, BorderLayout.SOUTH);
            profilePanel.add(profileCard, BorderLayout.NORTH);

            contentPanel.add(dashboardPanel, TAB_DASHBOARD);
            contentPanel.add(applicantsPanel, TAB_APPLICANTS);
            contentPanel.add(profilePanel, TAB_PROFILE);
            contentPanel.add(notificationsPanel, TAB_NOTIFICATIONS);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            this.user = user;
            this.selectedJobId = null;
            refreshJobs();
            refreshApplicants();
            refreshMoNotificationsTab();
            loadProfile();
            updateTopBarAvatar(user == null ? "" : user.getAvatarFilePath());
            contentLayout.show(contentPanel, TAB_DASHBOARD);
        }

        private void refreshJobs() {
            try {
                List<Job> closed = jobService.autoCloseExpiredJobs();
                if (!closed.isEmpty()) {
                    showToast(
                            "Jobs Auto-Closed",
                            closed.size() + " job(s) past their deadline were closed automatically.",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (RuntimeException ignored) {
                // Don't block dashboard rendering if the sweep fails; the latest data still loads below.
            }
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

        private void refreshMoNotificationsTab() {
            notificationsModel.setRowCount(0);
            if (user == null || notificationService == null) return;
            List<Notification> notifications = notificationService.getNotificationsForUser(user.getId());
            for (Notification n : notifications) {
                notificationsModel.addRow(new Object[] {
                    n.getType().name(),
                    safeText(n.getRelatedId()),
                    safeText(n.getMessage()),
                    safeText(n.getCreatedAt()),
                    n.isReadStatus() ? "Yes" : "No"
                });
            }
        }

        private void createJob() {
            JobInput input = promptForJobInput(null);
            if (input == null) {
                return;
            }
            try {
                jobService.createJob(
                        input.moduleCode,
                        input.moduleName,
                        input.description,
                        input.requiredSkills,
                        input.hoursPerWeek,
                        input.positions,
                        input.deadline,
                        user.getId());
                refreshJobs();
                refreshMoNotificationsTab();
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
            int modelRow = jobsTable.convertRowIndexToModel(row);
            String jobId = String.valueOf(jobsModel.getValueAt(modelRow, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                JOptionPane.showMessageDialog(frame, "Job not found.");
                return;
            }
            JobInput input = promptForJobInput(job);
            if (input == null) {
                return;
            }
            try {
                jobService.updateJob(
                        job.getId(),
                        user.getId(),
                        input.moduleCode,
                        input.moduleName,
                        input.description,
                        input.requiredSkills,
                        String.valueOf(input.hoursPerWeek),
                        String.valueOf(input.positions),
                        input.deadline);
                refreshJobs();
                refreshApplicants();
                refreshMoNotificationsTab();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void closeSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            int modelRow = jobsTable.convertRowIndexToModel(row);
            String jobId = String.valueOf(jobsModel.getValueAt(modelRow, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                JOptionPane.showMessageDialog(frame, "Job not found.");
                return;
            }
            if (job.getStatus() == JobStatus.CLOSED) {
                JOptionPane.showMessageDialog(frame, "This job is already closed.");
                return;
            }
            String label = job.getModuleCode() + " - " + job.getModuleName();
            String prompt = "Close " + label + "?\n"
                    + "New TAs will not be able to apply.\n"
                    + "Pending applications keep their current status.";
            int confirm = JOptionPane.showConfirmDialog(
                    frame, prompt, "Confirm Close Job", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                jobService.closeJob(job.getId(), user.getId());
                showToast("Job Closed", label + " is now closed.", JOptionPane.INFORMATION_MESSAGE);
                refreshJobs();
                refreshApplicants();
                refreshMoNotificationsTab();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void reopenSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            int modelRow = jobsTable.convertRowIndexToModel(row);
            String jobId = String.valueOf(jobsModel.getValueAt(modelRow, 0));
            Job job = findJobById(jobId).orElse(null);
            if (job == null) {
                JOptionPane.showMessageDialog(frame, "Job not found.");
                return;
            }
            if (job.getStatus() == JobStatus.OPEN) {
                JOptionPane.showMessageDialog(frame, "This job is already open.");
                return;
            }
            String label = job.getModuleCode() + " - " + job.getModuleName();
            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Reopen " + label + "?\nTAs will be able to apply again until the deadline.",
                    "Confirm Reopen Job",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                Job reopened = jobService.reopenJob(job.getId(), user.getId());
                showToast(
                        "Job Reopened",
                        label + " is now " + reopened.getStatus().name() + ".",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshJobs();
                refreshApplicants();
                refreshMoNotificationsTab();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void deleteSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            int modelRow = jobsTable.convertRowIndexToModel(row);
            String jobId = String.valueOf(jobsModel.getValueAt(modelRow, 0));
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
            refreshMoNotificationsTab();
        }

        private void openApplicantsForSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            int modelRow = jobsTable.convertRowIndexToModel(row);
            selectedJobId = String.valueOf(jobsModel.getValueAt(modelRow, 0));
            refreshApplicants();
            contentLayout.show(contentPanel, TAB_APPLICANTS);
        }

        private void refreshApplicants() {
            String selectedApplicationId = selectedApplicantApplicationId();
            applicantsModel.setRowCount(0);
            rankedApplicantsByApplicationId.clear();
            
            if (user == null) {
                applicantsTitle.setText("Applicants List");
                return;
            }
            
            applicantsTitle.setText("All Applicants for Your Posted Jobs");
            
            List<Job> allJobs = jobService.getJobsByMoId(user.getId());
            MoApplicantRankingService.RankingOptions options = new MoApplicantRankingService.RankingOptions(
                    pendingOnlyCheckBox.isSelected(),
                    needsDecisionCheckBox.isSelected(),
                    (Integer) matchThresholdSpinner.getValue(),
                    applicantSortMode);
                    
            List<MoApplicantRankingService.RankedApplicant> allRanked = new ArrayList<>();
            Map<String, Job> rankedAppToJob = new HashMap<>();

            for (Job job : allJobs) {
                List<Application> applications = applicationService.getApplicationsByJobId(job.getId());
                Map<String, User> applicantsById = new LinkedHashMap<>();
                for (Application application : applications) {
                    findUserById(application.getTaUserId())
                            .ifPresent(u -> applicantsById.put(u.getId(), u));
                }
                List<MoApplicantRankingService.RankedApplicant> ranked = moApplicantRankingService.rankApplicants(
                        job, applications, applicantsById, options);
                allRanked.addAll(ranked);
                for (MoApplicantRankingService.RankedApplicant ra : ranked) {
                    rankedAppToJob.put(ra.getApplicationId(), job);
                }
            }
            
            // Re-sort the combined list across all jobs
            allRanked.sort(options.getSortMode().getComparator());

            for (MoApplicantRankingService.RankedApplicant applicant : allRanked) {
                rankedApplicantsByApplicationId.put(applicant.getApplicationId(), applicant);
                Job job = rankedAppToJob.get(applicant.getApplicationId());
                applicantsModel.addRow(new Object[] {
                    applicant.getApplicationId(),
                    job != null ? job.getModuleCode() : "Unknown",
                    applicant.getApplicantName(),
                    applicant.getYearOfStudy(),
                    applicant.getMatchScore(),
                    applicant.getMissingSkillsText(),
                    applicant.getCurrentWorkload(),
                    applicant.getRecommendationLabel(),
                    applicant.getStatus().name()
                });
            }
            selectApplicantByApplicationId(selectedApplicationId);
        }

        private void sortApplicantsByMatchScore() {
            applicantSortMode = MoApplicantRankingService.SortMode.MATCH_SCORE_DESC;
            clearApplicantsTableSortKeys();
            refreshApplicants();
        }

        private void sortApplicantsByWorkload() {
            applicantSortMode = MoApplicantRankingService.SortMode.WORKLOAD_ASC;
            clearApplicantsTableSortKeys();
            refreshApplicants();
        }

        private void clearApplicantsTableSortKeys() {
            if (applicantsTable.getRowSorter() != null) {
                applicantsTable.getRowSorter().setSortKeys(null);
            }
        }

        private void showMoSendNotificationDialog() {
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ALL", "TA", "ADMIN"});
            JTextField userIdField = new JTextField();
            JTextArea messageArea = new JTextArea(4, 20);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("Target Role:"));
            panel.add(roleCombo);
            panel.add(new JLabel("Target User ID (Optional):"));
            panel.add(userIdField);
            panel.add(new JLabel("Message:"));
            panel.add(new JScrollPane(messageArea));
            
            int result = JOptionPane.showConfirmDialog(frame, panel, "Send Notification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String roleStr = (String) roleCombo.getSelectedItem();
                Role targetRole = "ALL".equals(roleStr) ? null : Role.valueOf(roleStr);
                String userId = userIdField.getText().trim();
                String message = messageArea.getText().trim();
                
                if (message.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Message cannot be empty.");
                    return;
                }
                
                int count = 0;
                java.util.List<User> targets = new ArrayList<>();
                if (!userId.isEmpty()) {
                    findUserById(userId).ifPresent(u -> {
                        if (u.getRole() == Role.TA || u.getRole() == Role.ADMIN) {
                            targets.add(u);
                        } else {
                            JOptionPane.showMessageDialog(frame, "MO can only send notifications to TA or Admin users.");
                        }
                    });
                    if (targets.isEmpty() && findUserById(userId).isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "User not found: " + userId);
                        return;
                    }
                } else {
                    for (User u : authService.getAllUsers()) {
                        if (u.getRole() == Role.TA || u.getRole() == Role.ADMIN) {
                            if (targetRole == null || u.getRole() == targetRole) {
                                targets.add(u);
                            }
                        }
                    }
                }
                
                String broadcastId = "MO_BROADCAST:" + System.currentTimeMillis();
                for (User u : targets) {
                    notificationService.publish(u.getRole(), NotificationType.SYSTEM_ALERT, u.getId(), 
                            "From " + user.getName() + ": " + message, broadcastId);
                    count++;
                }
                
                JOptionPane.showMessageDialog(frame, "Sent notification to " + count + " users.");
            }
        }

        private String selectedApplicantApplicationId() {
            int row = applicantsTable.getSelectedRow();
            if (row < 0) {
                return null;
            }
            int modelRow = applicantsTable.convertRowIndexToModel(row);
            return String.valueOf(applicantsModel.getValueAt(modelRow, 0));
        }

        private void selectApplicantByApplicationId(String applicationId) {
            if (applicationId == null || applicationId.isBlank()) {
                return;
            }
            for (int modelRow = 0; modelRow < applicantsModel.getRowCount(); modelRow++) {
                if (!applicationId.equalsIgnoreCase(String.valueOf(applicantsModel.getValueAt(modelRow, 0)))) {
                    continue;
                }
                int viewRow = applicantsTable.convertRowIndexToView(modelRow);
                if (viewRow >= 0) {
                    applicantsTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
                    applicantsTable.scrollRectToVisible(applicantsTable.getCellRect(viewRow, 0, true));
                }
                return;
            }
        }

        private void updateApplicantStatus(ApplicationStatus status) {
            String appId = selectedApplicantApplicationId();
            if (appId == null) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
            String actionLabel = status == ApplicationStatus.ACCEPTED ? "accept" : "reject";
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to " + actionLabel + " this applicant?",
                    "Confirm " + actionLabel.substring(0, 1).toUpperCase() + actionLabel.substring(1),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                applicationService.updateApplicationStatus(appId, user.getId(), status);
                refreshApplicants();
                refreshJobs();
                refreshMoNotificationsTab();
                showToast(
                        "Application Reviewed",
                        "Application has been marked as " + status.name() + ".",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void viewSelectedApplicantCv() {
            String appId = selectedApplicantApplicationId();
            if (appId == null) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
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

        private void viewSelectedApplicantExplanation() {
            String appId = selectedApplicantApplicationId();
            if (appId == null) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
            Job selectedJob = selectedJobId == null ? null : findJobById(selectedJobId).orElse(null);
            MoApplicantRankingService.RankedApplicant applicant = rankedApplicantsByApplicationId.get(appId);
            if (applicant == null) {
                JOptionPane.showMessageDialog(frame, "Applicant ranking details are no longer available. Please refresh the list.");
                return;
            }

            String explanation = moApplicantRankingService.buildExplanation(
                    selectedJob, applicant, (Integer) matchThresholdSpinner.getValue());
            JTextArea explanationArea = new JTextArea(explanation, 14, 58);
            explanationArea.setEditable(false);
            explanationArea.setLineWrap(true);
            explanationArea.setWrapStyleWord(true);
            explanationArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            explanationArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JScrollPane scrollPane = new JScrollPane(explanationArea);
            scrollPane.setBorder(BorderFactory.createLineBorder(CARD_BORDER));
            JOptionPane.showMessageDialog(frame, scrollPane, "Applicant Match Explanation", JOptionPane.INFORMATION_MESSAGE);
        }

        private void viewSelectedApplicantProfile() {
            String appId = selectedApplicantApplicationId();
            if (appId == null) {
                JOptionPane.showMessageDialog(frame, "Please select an applicant first.");
                return;
            }
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
                showToast("Profile Saved", "Your profile has been updated.", JOptionPane.INFORMATION_MESSAGE);
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

            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 4, 4, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.gridx = 0;

            gbc.gridy = 0; panel.add(new JLabel("Module Code"), gbc);
            gbc.gridy = 1; panel.add(moduleCodeField, gbc);

            gbc.gridy = 2; panel.add(new JLabel("Module Name"), gbc);
            gbc.gridy = 3; panel.add(moduleNameField, gbc);

            gbc.gridy = 4; panel.add(new JLabel("Description"), gbc);
            gbc.gridy = 5; 
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            panel.add(new JScrollPane(descriptionArea), gbc);

            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weighty = 0.0;

            gbc.gridy = 6; panel.add(new JLabel("Required Skills"), gbc);
            gbc.gridy = 7; panel.add(requiredSkillsField, gbc);

            gbc.gridy = 8; panel.add(new JLabel("Hours per Week"), gbc);
            gbc.gridy = 9; panel.add(hoursField, gbc);

            gbc.gridy = 10; panel.add(new JLabel("Positions"), gbc);
            gbc.gridy = 11; panel.add(positionsField, gbc);

            gbc.gridy = 12; panel.add(new JLabel("Deadline (YYYY-MM-DD)"), gbc);
            gbc.gridy = 13; panel.add(deadlineField, gbc);

            if (existing != null) {
                JLabel statusHint = new JLabel(
                        "Status: " + existing.getStatus().name() + "  (use Close / Reopen to change)");
                statusHint.setForeground(MUTED_TEXT_COLOR);
                gbc.gridy = 14; panel.add(statusHint, gbc);
            }

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
                        deadlineField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Hours and positions must be numbers.");
                return null;
            }
        }
    }

    private class AdminPanel extends JPanel {
        static final String TAB_WORKLOAD = "workload";
        static final String TAB_ACCOUNTS = "accounts";
        static final String TAB_JOBS = "jobs";
        static final String TAB_APPLICATIONS = "applications";
        static final String TAB_AUDIT = "audit";
        static final String TAB_NOTIFICATIONS = "notifications";

        void demoShowTab(String tab) {
            switch (tab) {
                case TAB_WORKLOAD:
                    refreshWorkload();
                    break;
                case TAB_ACCOUNTS:
                    refreshAccounts();
                    break;
                case TAB_JOBS:
                    refreshJobs();
                    break;
                case TAB_APPLICATIONS:
                    refreshApplications();
                    break;
                case TAB_AUDIT:
                    refreshAuditLog();
                    break;
                case TAB_NOTIFICATIONS:
                    refreshAdminNotifications();
                    break;
                default:
                    return;
            }
            contentLayout.show(contentPanel, tab);
        }

        private final JLabel titleLabel;
        private final CardLayout contentLayout;
        private final JPanel contentPanel;
        private final DefaultTableModel workloadModel;
        private final JTable workloadTable;
        private final DefaultTableModel accountModel;
        private final JTable accountTable;
        private final DefaultTableModel jobsModel;
        private final JTable jobsTable;
        private final DefaultTableModel applicationsModel;
        private final JTable applicationsTable;
        private final DefaultTableModel auditModel;
        private final JTable auditTable;
        private final DefaultTableModel notificationsModel;
        private final JTable notificationsTable;
        private JComboBox<String> applicationStatusFilter;

        // Summary bar labels
        private final JLabel summaryTotalJobs = new JLabel("--");
        private final JLabel summaryFilledJobs = new JLabel("--");
        private final JLabel summaryOverloaded = new JLabel("--");
        private final JLabel summaryHighRisk = new JLabel("--");
        private final JLabel summaryApplications = new JLabel("--");
        private final JTextField workloadSearchField = new JTextField();

        // Notification bell
        private JLabel notificationBadge;

        // Current admin user
        private User user;

        // Cached job overview data for job actions
        private List<AdminService.JobOverview> cachedJobOverviews = new ArrayList<>();

        private AdminPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("Admin Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("Admin Dashboard", SwingApp.this::showLoginPage);
            addNotificationBellToTopBar(topBar);
            JPanel northArea = new JPanel(new BorderLayout());
            northArea.setOpaque(false);
            northArea.add(topBar, BorderLayout.NORTH);
            northArea.add(buildSummaryBar(), BorderLayout.SOUTH);
            add(northArea, BorderLayout.NORTH);

            contentLayout = new CardLayout();
            contentPanel = new JPanel(contentLayout);

            String[] navLabels = {"Workload Overview", "Manage Accounts", "Jobs Overview", "Applications", "Audit Log", "Notifications"};
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
                },
                () -> {
                    refreshApplications();
                    contentLayout.show(contentPanel, TAB_APPLICATIONS);
                },
                () -> {
                    refreshAuditLog();
                    contentLayout.show(contentPanel, TAB_AUDIT);
                },
                () -> {
                    refreshAdminNotifications();
                    contentLayout.show(contentPanel, TAB_NOTIFICATIONS);
                }
            };
            add(buildNavigationPanel(navLabels, navActions), BorderLayout.WEST);

            // ========================= Workload Tab =========================
            workloadModel = new DefaultTableModel(
                    new Object[] {"TA ID", "TA Name", "Available h/week", "Assigned h/week", "Remaining h", "Risk"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            workloadTable = new JTable(workloadModel);
            styleDataTable(workloadTable);
            applyRiskLevelRenderer(workloadTable, 5);
            installTableRowHover(workloadTable);
            workloadTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showTADetailDialog();
                    }
                }
            });
            JPanel workloadPanel = new JPanel(new BorderLayout(0, 16));
            workloadPanel.setOpaque(false);
            JPanel workloadHeader = new JPanel(new BorderLayout(12, 0));
            workloadHeader.setOpaque(false);
            workloadHeader.add(createSectionTitle("TA Weekly Workload", "Monitor accepted hours and overloaded TAs."), BorderLayout.WEST);
            workloadSearchField.setPreferredSize(new java.awt.Dimension(200, 30));
            workloadSearchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            workloadSearchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 223, 230), 1, true),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            workloadSearchField.putClientProperty("JTextField.placeholderText", "Search TA name or ID...");
            workloadSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filterWorkloadTable(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filterWorkloadTable(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filterWorkloadTable(); }
            });
            JPanel searchWrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
            searchWrapper.setOpaque(false);
            searchWrapper.add(workloadSearchField);
            workloadHeader.add(searchWrapper, BorderLayout.EAST);
            workloadPanel.add(createCardPanel(workloadHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane workloadScrollPane = new JScrollPane(workloadTable);
            workloadScrollPane.setBorder(BorderFactory.createEmptyBorder());
            workloadScrollPane.getViewport().setBackground(Color.WHITE);

            JPanel workloadCenter = new JPanel(new BorderLayout(0, 14));
            workloadCenter.setOpaque(false);
            workloadCenter.add(createCardPanel(workloadScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            workloadPanel.add(workloadCenter, BorderLayout.CENTER);

            JButton refreshWorkloadButton = new JButton("Refresh");
            styleSecondaryButton(refreshWorkloadButton);
            refreshWorkloadButton.addActionListener(e -> refreshWorkload());
            JButton showOverloadedButton = new JButton("Overloaded Only");
            styleDangerButton(showOverloadedButton);
            showOverloadedButton.addActionListener(e -> showOverloadedOnly());
            JButton exportReportButton = new JButton("View Report");
            styleSecondaryButton(exportReportButton);
            exportReportButton.addActionListener(e -> showWorkloadReport());
            JButton exportWorkloadCsvButton = new JButton("Export CSV");
            styleSecondaryButton(exportWorkloadCsvButton);
            exportWorkloadCsvButton.addActionListener(e -> exportWorkloadCsv());
            JButton aiAnalysisButton = new JButton("AI Analysis");
            stylePrimaryButton(aiAnalysisButton);
            aiAnalysisButton.addActionListener(e -> showAiAnalysisDialog());
            JPanel workloadActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            workloadActions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            workloadActions.setOpaque(false);
            workloadActions.add(refreshWorkloadButton);
            workloadActions.add(showOverloadedButton);
            workloadActions.add(aiAnalysisButton);
            workloadActions.add(exportReportButton);
            workloadActions.add(exportWorkloadCsvButton);
            workloadPanel.add(workloadActions, BorderLayout.SOUTH);

            // ========================= Accounts Tab =========================
            accountModel = new DefaultTableModel(
                    new Object[] {"User ID", "Name", "Email", "Role", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            accountTable = new JTable(accountModel);
            styleDataTable(accountTable);
            installTableRowHover(accountTable);
            accountTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showAccountDetailDialog();
                    }
                }
            });
            JPanel accountsPanel = new JPanel(new BorderLayout(0, 16));
            accountsPanel.setOpaque(false);
            JPanel accountsHeader = new JPanel(new BorderLayout());
            accountsHeader.setOpaque(false);
            accountsHeader.add(createSectionTitle("User Accounts", "Create, activate, or reset accounts in one place. Double-click for details."), BorderLayout.WEST);
            accountsPanel.add(createCardPanel(accountsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane accountsScrollPane = new JScrollPane(accountTable);
            accountsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            accountsScrollPane.getViewport().setBackground(Color.WHITE);
            accountsPanel.add(createCardPanel(accountsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel accountActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            accountActions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            accountActions.setOpaque(false);
            JButton createMoButton = new JButton("Create New MO");
            stylePrimaryButton(createMoButton);
            createMoButton.addActionListener(e -> createMoAccount());
            JButton toggleButton = new JButton("Activate/Deactivate");
            styleSecondaryButton(toggleButton);
            toggleButton.addActionListener(e -> toggleSelectedAccount());
            JButton resetPwdButton = new JButton("Reset Password");
            styleDangerButton(resetPwdButton);
            resetPwdButton.addActionListener(e -> resetPassword());
            JButton refreshAccountsButton = new JButton("Refresh");
            styleSecondaryButton(refreshAccountsButton);
            refreshAccountsButton.addActionListener(e -> refreshAccounts());
            accountActions.add(createMoButton);
            accountActions.add(toggleButton);
            accountActions.add(resetPwdButton);
            accountActions.add(refreshAccountsButton);
            accountsPanel.add(accountActions, BorderLayout.SOUTH);

            // ========================= Jobs Tab =========================
            jobsModel = new DefaultTableModel(
                    new Object[] {"Module", "MO", "Filled/Total", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobsTable = new JTable(jobsModel);
            styleDataTable(jobsTable);
            applyStatusRenderer(jobsTable, 3);
            applyFilledRatioRenderer(jobsTable, 2);
            installTableRowHover(jobsTable);
            jobsTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        showJobDetailDialog();
                    }
                }
            });
            JPanel jobsPanel = new JPanel(new BorderLayout(0, 16));
            jobsPanel.setOpaque(false);
            JPanel jobsHeader = new JPanel(new BorderLayout());
            jobsHeader.setOpaque(false);
            jobsHeader.add(createSectionTitle("Global Jobs", "A complete view of all modules and filling status. Double-click for details."), BorderLayout.WEST);
            jobsPanel.add(createCardPanel(jobsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane jobsScrollPane = new JScrollPane(jobsTable);
            jobsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobsScrollPane.getViewport().setBackground(Color.WHITE);
            jobsPanel.add(createCardPanel(jobsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel jobsActions = new JPanel(new BorderLayout());
            jobsActions.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            jobsActions.setOpaque(false);
            JPanel jobsButtonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            jobsButtonRow.setOpaque(false);
            JButton refreshJobsButton = new JButton("Refresh");
            styleSecondaryButton(refreshJobsButton);
            refreshJobsButton.addActionListener(e -> refreshJobs());
            JButton forceCloseButton = new JButton("Force Close");
            styleDangerButton(forceCloseButton);
            forceCloseButton.addActionListener(e -> forceCloseSelectedJob());
            JButton forceReopenButton = new JButton("Force Reopen");
            styleSecondaryButton(forceReopenButton);
            forceReopenButton.addActionListener(e -> forceReopenSelectedJob());
            JButton autoCloseButton = new JButton("Auto-Close Expired");
            styleDangerButton(autoCloseButton);
            autoCloseButton.addActionListener(e -> autoCloseExpiredJobs());
            JButton exportJobsCsvButton = new JButton("Export CSV");
            styleSecondaryButton(exportJobsCsvButton);
            exportJobsCsvButton.addActionListener(e -> exportJobsCsv());
            jobsButtonRow.add(refreshJobsButton);
            jobsButtonRow.add(forceCloseButton);
            jobsButtonRow.add(forceReopenButton);
            jobsButtonRow.add(autoCloseButton);
            jobsButtonRow.add(exportJobsCsvButton);
            jobsActions.add(jobsButtonRow, BorderLayout.WEST);
            JLabel jobsStatsLabel = new JLabel(" ");
            jobsStatsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            jobsStatsLabel.setForeground(new Color(107, 114, 128));
            jobsStatsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            jobsActions.add(jobsStatsLabel, BorderLayout.EAST);
            jobsPanel.add(jobsActions, BorderLayout.SOUTH);
            refreshJobsButton.addActionListener(e2 -> {
                AdminService.RecruitmentSnapshot snap = adminService.getRecruitmentSnapshot();
                jobsStatsLabel.setText("Total: " + snap.totalJobs + "  |  Filled: " + snap.filledJobs
                        + "  |  Open: " + snap.openJobs);
            });

            // ========================= Applications Tab =========================
            applicationsModel = new DefaultTableModel(
                    new Object[] {"App ID", "Module", "TA Name", "Status", "Applied Date"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            applicationsTable = new JTable(applicationsModel);
            styleDataTable(applicationsTable);
            applyApplicationStatusRenderer(applicationsTable, 3);
            installTableRowHover(applicationsTable);
            JPanel applicationsPanel = new JPanel(new BorderLayout(0, 16));
            applicationsPanel.setOpaque(false);
            JPanel appsHeader = new JPanel(new BorderLayout(12, 0));
            appsHeader.setOpaque(false);
            appsHeader.add(createSectionTitle("Applications Overview",
                    "Monitor all TA applications across the system (read-only)."), BorderLayout.WEST);
            applicationStatusFilter = new JComboBox<>(new String[] {
                    "All", "PENDING", "ACCEPTED", "REJECTED", "WITHDRAWN"});
            applicationStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            applicationStatusFilter.addActionListener(e -> refreshApplications());
            JPanel filterWrapper = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
            filterWrapper.setOpaque(false);
            filterWrapper.add(new JLabel("Status:"));
            filterWrapper.add(applicationStatusFilter);
            appsHeader.add(filterWrapper, BorderLayout.EAST);
            applicationsPanel.add(createCardPanel(appsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane appsScrollPane = new JScrollPane(applicationsTable);
            appsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            appsScrollPane.getViewport().setBackground(Color.WHITE);
            applicationsPanel.add(createCardPanel(appsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            // Stats footer
            JPanel appsFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            appsFooter.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            appsFooter.setOpaque(false);
            JButton refreshAppsButton = new JButton("Refresh");
            styleSecondaryButton(refreshAppsButton);
            refreshAppsButton.addActionListener(e -> refreshApplications());
            appsFooter.add(refreshAppsButton);
            JLabel appsStatsLabel = new JLabel(" ");
            appsStatsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            appsStatsLabel.setForeground(new Color(107, 114, 128));
            appsFooter.add(appsStatsLabel);
            refreshAppsButton.addActionListener(e2 -> {
                AdminService.ApplicationStats stats = adminService.getApplicationStatistics();
                appsStatsLabel.setText("Total: " + stats.total + "  |  Pending: " + stats.pending
                        + "  |  Accepted: " + stats.accepted + "  |  Rejected: " + stats.rejected
                        + "  |  Withdrawn: " + stats.withdrawn);
            });
            applicationsPanel.add(appsFooter, BorderLayout.SOUTH);

            // ========================= Audit Log Tab =========================
            auditModel = new DefaultTableModel(
                    new Object[] {"Timestamp", "Admin", "Action", "Target", "Details"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            auditTable = new JTable(auditModel);
            styleDataTable(auditTable);
            installTableRowHover(auditTable);
            JPanel auditPanel = new JPanel(new BorderLayout(0, 16));
            auditPanel.setOpaque(false);
            JPanel auditHeader = new JPanel(new BorderLayout());
            auditHeader.setOpaque(false);
            auditHeader.add(createSectionTitle("Audit Log",
                    "Session-level record of all admin operations."), BorderLayout.WEST);
            auditPanel.add(createCardPanel(auditHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane auditScrollPane = new JScrollPane(auditTable);
            auditScrollPane.setBorder(BorderFactory.createEmptyBorder());
            auditScrollPane.getViewport().setBackground(Color.WHITE);
            auditPanel.add(createCardPanel(auditScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel auditFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            auditFooter.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            auditFooter.setOpaque(false);
            JButton refreshAuditButton = new JButton("Refresh");
            styleSecondaryButton(refreshAuditButton);
            refreshAuditButton.addActionListener(e -> refreshAuditLog());
            auditFooter.add(refreshAuditButton);
            auditPanel.add(auditFooter, BorderLayout.SOUTH);

            // ========================= Notifications Tab =========================
            notificationsModel = new DefaultTableModel(
                    new Object[] {"Type", "Related ID", "Message", "Time", "Read"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) { return false; }
            };
            notificationsTable = new JTable(notificationsModel);
            styleDataTable(notificationsTable);
            installTableRowHover(notificationsTable);
            
            JPanel notificationsPanel = new JPanel(new BorderLayout(0, 16));
            notificationsPanel.setOpaque(false);
            JPanel notifHeader = new JPanel(new BorderLayout());
            notifHeader.setOpaque(false);
            notifHeader.add(createSectionTitle("System Notifications", "Admin alerts and broadcasts."), BorderLayout.WEST);
            notificationsPanel.add(createCardPanel(notifHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            
            JScrollPane notifScrollPane = new JScrollPane(notificationsTable);
            notifScrollPane.setBorder(BorderFactory.createEmptyBorder());
            notifScrollPane.getViewport().setBackground(Color.WHITE);
            notificationsPanel.add(createCardPanel(notifScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            
            JPanel notifFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
            notifFooter.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
            notifFooter.setOpaque(false);
            
            JButton markReadButton = new JButton("Mark All Read");
            styleSecondaryButton(markReadButton);
            markReadButton.addActionListener(e -> {
                java.util.List<com.group52.tarecruitment.model.Notification> notifs = notificationService.getNotificationsForUser(user.getId());
                for (com.group52.tarecruitment.model.Notification n : notifs) {
                    if (!n.isReadStatus()) {
                        notificationService.setReadStatus(n.getId(), true);
                    }
                }
                refreshAdminNotifications();
                updateNotificationBadge();
            });
            
            JButton sendNotifButton = new JButton("Send Notification");
            stylePrimaryButton(sendNotifButton);
            sendNotifButton.addActionListener(e -> showSendNotificationDialog());
            
            JButton broadcastAnomaliesButton = new JButton("Broadcast Anomalies");
            styleDangerButton(broadcastAnomaliesButton);
            broadcastAnomaliesButton.addActionListener(e -> handleBroadcastAnomalies());
            
            JButton viewDetailsButton = new JButton("View Details");
            styleSecondaryButton(viewDetailsButton);
            viewDetailsButton.addActionListener(e -> showNotificationDetails(notificationsTable));
            
            notifFooter.add(viewDetailsButton);
            notifFooter.add(markReadButton);
            notifFooter.add(sendNotifButton);
            notifFooter.add(broadcastAnomaliesButton);
            notificationsPanel.add(notifFooter, BorderLayout.SOUTH);

            contentPanel.add(workloadPanel, TAB_WORKLOAD);
            contentPanel.add(accountsPanel, TAB_ACCOUNTS);
            contentPanel.add(jobsPanel, TAB_JOBS);
            contentPanel.add(applicationsPanel, TAB_APPLICATIONS);
            contentPanel.add(auditPanel, TAB_AUDIT);
            contentPanel.add(notificationsPanel, TAB_NOTIFICATIONS);
            add(contentPanel, BorderLayout.CENTER);
        }

        // ========================= Notification bell =========================

        private void addNotificationBellToTopBar(JPanel topBar) {
            JPanel bellPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            bellPanel.setOpaque(false);
            JButton bellButton = new JButton("\uD83D\uDD14");
            bellButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            bellButton.setBorderPainted(false);
            bellButton.setContentAreaFilled(false);
            bellButton.setFocusPainted(false);
            bellButton.setToolTipText("Admin Notifications");
            bellButton.addActionListener(e -> showAdminNotifications());
            notificationBadge = new JLabel("");
            notificationBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
            notificationBadge.setForeground(new Color(239, 68, 68));
            bellPanel.add(bellButton);
            bellPanel.add(notificationBadge);
            topBar.add(bellPanel, BorderLayout.CENTER);
        }

        private void showAdminNotifications() {
            if (user == null || notificationService == null) {
                JOptionPane.showMessageDialog(frame, "No notifications available.");
                return;
            }
            List<com.group52.tarecruitment.model.Notification> notifications =
                    notificationService.getNotificationsForUser(user.getId());
            if (notifications.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "No notifications.", "Admin Notifications", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            int shown = 0;
            for (com.group52.tarecruitment.model.Notification n : notifications) {
                if (shown >= 20) { sb.append("\n... and more"); break; }
                String read = n.isReadStatus() ? "" : " [NEW]";
                sb.append(n.getCreatedAt() == null ? "" : n.getCreatedAt().substring(0, Math.min(19, n.getCreatedAt().length())))
                        .append(read).append("  ").append(n.getMessage()).append("\n");
                shown++;
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            area.setEditable(false);
            area.setRows(15);
            area.setColumns(55);
            JScrollPane sp = new JScrollPane(area);
            int choice = JOptionPane.showOptionDialog(frame, sp, "Admin Notifications (" + notifications.size() + ")",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                    new String[] {"Mark All Read", "Close"}, "Close");
            if (choice == 0) {
                for (com.group52.tarecruitment.model.Notification n : notifications) {
                    if (!n.isReadStatus()) {
                        notificationService.setReadStatus(n.getId(), true);
                    }
                }
                updateNotificationBadge();
            }
        }

        private void updateNotificationBadge() {
            if (notificationBadge == null || user == null || notificationService == null) return;
            int unread = notificationService.countUnreadForUser(user.getId());
            notificationBadge.setText(unread > 0 ? String.valueOf(unread) : "");
        }

        // ========================= Summary Bar =========================

        private JPanel buildSummaryBar() {
            JPanel bar = new JPanel(new java.awt.GridLayout(1, 5, 12, 0));
            bar.setBackground(new Color(245, 246, 250));
            bar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 223, 230)),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            bar.add(buildSummaryCard("Total Jobs", summaryTotalJobs, new Color(99, 102, 241)));
            bar.add(buildSummaryCard("Filled Jobs", summaryFilledJobs, new Color(16, 185, 129)));
            bar.add(buildSummaryCard("Overloaded TAs", summaryOverloaded, new Color(239, 68, 68)));
            bar.add(buildSummaryCard("High-Risk TAs", summaryHighRisk, new Color(245, 158, 11)));
            bar.add(buildSummaryCard("Applications", summaryApplications, new Color(59, 130, 246)));
            return bar;
        }

        private JPanel buildSummaryCard(String title, JLabel valueLabel, Color accent) {
            JPanel card = new JPanel(new BorderLayout(4, 4));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 223, 230), 1, true),
                    BorderFactory.createEmptyBorder(8, 14, 8, 14)));
            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            titleLbl.setForeground(new Color(107, 114, 128));
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valueLabel.setForeground(accent);
            card.add(titleLbl, BorderLayout.NORTH);
            card.add(valueLabel, BorderLayout.CENTER);
            return card;
        }

        private void refreshSummaryBar() {
            AdminService.RecruitmentSnapshot snap = adminService.getRecruitmentSnapshot();
            summaryTotalJobs.setText(String.valueOf(snap.totalJobs));
            summaryFilledJobs.setText(String.valueOf(snap.filledJobs));
            summaryOverloaded.setText(String.valueOf(snap.overloadedTAs));
            summaryHighRisk.setText(String.valueOf(snap.atRiskTAs));
            AdminService.ApplicationStats appStats = adminService.getApplicationStatistics();
            summaryApplications.setText(String.valueOf(appStats.total));
        }

        // ========================= Bind / Refresh =========================

        private void bindUser(User user) {
            this.user = user;
            refreshWorkload();
            refreshAccounts();
            refreshJobs();
            updateTopBarAvatar(user == null ? "" : user.getAvatarFilePath());
            updateNotificationBadge();
            contentLayout.show(contentPanel, TAB_WORKLOAD);
        }

        private void refreshWorkload() {
            filterWorkloadTable();
            adminService.publishOverloadAlerts();
            refreshSummaryBar();
            updateNotificationBadge();
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
            cachedJobOverviews = adminService.getJobsOverview();
            for (AdminService.JobOverview overview : cachedJobOverviews) {
                jobsModel.addRow(new Object[] {
                    overview.moduleCode + " - " + overview.moduleName,
                    overview.postedByMoName,
                    overview.filledRatio(),
                    overview.status.name()
                });
            }
        }

        private void refreshApplications() {
            applicationsModel.setRowCount(0);
            String selected = applicationStatusFilter == null ? "All"
                    : (String) applicationStatusFilter.getSelectedItem();
            ApplicationStatus filterStatus = null;
            if (selected != null && !"All".equals(selected)) {
                try { filterStatus = ApplicationStatus.valueOf(selected); } catch (Exception ignored) {}
            }
            List<AdminService.EnrichedApplication> apps = adminService.getApplicationsByStatus(filterStatus);
            for (AdminService.EnrichedApplication app : apps) {
                applicationsModel.addRow(new Object[] {
                    app.applicationId,
                    app.moduleCode + " - " + app.moduleName,
                    app.taName,
                    app.status.name(),
                    app.appliedDate
                });
            }
        }

        private void refreshAuditLog() {
            auditModel.setRowCount(0);
            for (AdminService.AuditLogEntry entry : adminService.getAuditLog()) {
                auditModel.addRow(new Object[] {
                    entry.timestamp,
                    entry.adminUserId,
                    entry.action,
                    entry.targetId,
                    entry.details
                });
            }
        }

        // ========================= Renderers =========================

        private void applyRiskLevelRenderer(JTable table, int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    String text = value == null ? "" : String.valueOf(value);
                    setText(text);
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    if (!isSelected) {
                        if (text.equals(AdminService.RiskLevel.OVERLOADED.label())) {
                            setForeground(Color.WHITE);
                            setBackground(new Color(239, 68, 68));
                        } else if (text.equals(AdminService.RiskLevel.AT_RISK.label())) {
                            setForeground(Color.WHITE);
                            setBackground(new Color(245, 158, 11));
                        } else {
                            setForeground(new Color(16, 185, 129));
                            setBackground(Color.WHITE);
                        }
                    }
                    return this;
                }
            });
        }

        private void applyFilledRatioRenderer(JTable table, int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    String text = value == null ? "" : String.valueOf(value);
                    setText(text);
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    if (!isSelected) {
                        boolean isFull = text.contains("/") && !text.startsWith("0/") &&
                                text.split("/").length == 2 &&
                                Integer.parseInt(text.split("/")[0]) >= Integer.parseInt(text.split("/")[1]);
                        if (isFull) {
                            setForeground(Color.WHITE);
                            setBackground(new Color(16, 185, 129));
                        } else {
                            setForeground(new Color(46, 52, 64));
                            setBackground(Color.WHITE);
                        }
                    }
                    return this;
                }
            });
        }

        private void applyApplicationStatusRenderer(JTable table, int col) {
            table.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(
                        JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    String text = value == null ? "" : String.valueOf(value);
                    setText(text);
                    setHorizontalAlignment(CENTER);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    if (!isSelected) {
                        switch (text) {
                            case "ACCEPTED" -> { setForeground(Color.WHITE); setBackground(new Color(16, 185, 129)); }
                            case "REJECTED" -> { setForeground(Color.WHITE); setBackground(new Color(239, 68, 68)); }
                            case "WITHDRAWN" -> { setForeground(Color.WHITE); setBackground(new Color(107, 114, 128)); }
                            case "PENDING", "APPLIED", "REVIEWING" -> { setForeground(Color.WHITE); setBackground(new Color(245, 158, 11)); }
                            default -> { setForeground(new Color(46, 52, 64)); setBackground(Color.WHITE); }
                        }
                    }
                    return this;
                }
            });
        }

        // ========================= Workload actions =========================

        private void showOverloadedOnly() {
            workloadModel.setRowCount(0);
            for (AdminService.TAWorkloadSummary s : adminService.getOverloadedTAs()) {
                workloadModel.addRow(new Object[] {
                    s.getTaUserId(),
                    s.getTaName(),
                    s.getAvailableHours(),
                    s.getTotalAssignedHours(),
                    s.getRemainingHours(),
                    s.getRiskLevel().label()
                });
            }
            if (workloadModel.getRowCount() == 0) {
                showToast("No Overloaded TAs", "All TAs are within their available hours.", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshSummaryBar();
        }


        private void showWorkloadReport() {
            String report = adminService.getWorkloadBalancingReport();
            JTextArea textArea = new JTextArea(report);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            textArea.setRows(22);
            textArea.setColumns(66);
            JScrollPane scrollPane = new JScrollPane(textArea);
            JOptionPane.showMessageDialog(frame, scrollPane, "Workload Report", JOptionPane.PLAIN_MESSAGE);
        }

        private void filterWorkloadTable() {
            String keyword = workloadSearchField.getText();
            workloadModel.setRowCount(0);
            List<AdminService.TAWorkloadSummary> source = keyword == null || keyword.isBlank()
                    ? adminService.getAllTAWorkloads()
                    : adminService.searchTAWorkload(keyword);
            for (AdminService.TAWorkloadSummary s : source) {
                workloadModel.addRow(new Object[] {
                    s.getTaUserId(),
                    s.getTaName(),
                    s.getAvailableHours(),
                    s.getTotalAssignedHours(),
                    s.getRemainingHours(),
                    s.getRiskLevel().label()
                });
            }
        }

        private void showAiAnalysisDialog() {
            String summary = adminService.getWorkloadBalancingSummary();
            List<WorkloadBalancerService.WorkloadRecommendation> recommendations = adminService.getWorkloadRecommendations();
            StringBuilder sb = new StringBuilder();
            sb.append(summary).append("\n\n");
            if (recommendations.isEmpty()) {
                sb.append("\u2022 All TA workloads are balanced.");
            } else {
                for (WorkloadBalancerService.WorkloadRecommendation recommendation : recommendations) {
                    sb.append("\u2022 ").append(recommendation.getOverloadedName())
                            .append(" exceeds their weekly capacity by ")
                            .append(recommendation.getOverloadHours()).append("h/week.\n");
                    sb.append("  - ").append(recommendation.getTargetName())
                            .append(" currently has ")
                            .append(recommendation.getTargetRemainingCapacityHours()).append("h/week remaining capacity.\n");
                    sb.append("  - Suggested Action: Move ")
                            .append(recommendation.getMoveHours()).append("h/week from ")
                            .append(recommendation.getOverloadedName()).append(" to ")
                            .append(recommendation.getTargetName()).append(".\n");
                    sb.append("  - ").append(recommendation.getPriority().label())
                            .append(" Redistribution Recommended.\n");
                    sb.append("  - Explainability: ")
                            .append(recommendation.getOverloadedName())
                            .append(" exceeds capacity by ")
                            .append(String.format("%.0f%%", Math.max(0, (recommendation.getOverloadHours() * 100.0) / Math.max(1, recommendation.getTargetRemainingCapacityHours() + recommendation.getOverloadHours()))))
                            .append(".\n\n");
                }
            }
            
            JTextArea textArea = new JTextArea(sb.toString().trim());
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textArea.setBackground(new Color(250, 246, 255));
            textArea.setForeground(new Color(58, 31, 107));
            textArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(199, 177, 255), 1, true));
            
            JOptionPane.showMessageDialog(frame, scrollPane, "AI Workload Analysis", JOptionPane.PLAIN_MESSAGE);
        }

        // ========================= TA Detail =========================

        private void showTADetailDialog() {
            int row = workloadTable.getSelectedRow();
            if (row < 0) return;
            String taId = String.valueOf(workloadModel.getValueAt(row, 0));
            AdminService.TAWorkloadSummary s;
            try {
                s = adminService.getTAWorkload(taId);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
                return;
            }
            AdminService.WorkloadTrend trend = adminService.getWorkloadTrend(s);
            StringBuilder sb = new StringBuilder();
            sb.append("TA ID:           ").append(s.getTaUserId()).append("\n");
            sb.append("Name:            ").append(s.getTaName()).append("\n");
            sb.append("Available h/wk:  ").append(s.getAvailableHours()).append("h\n");
            sb.append("Assigned h/wk:   ").append(s.getTotalAssignedHours()).append("h\n");
            sb.append("Remaining h/wk:  ").append(s.getRemainingHours()).append("h\n");
            sb.append("Utilisation:     ").append(String.format("%.0f%%", s.getUtilisationPercent())).append("\n");
            sb.append("Risk Level:      ").append(s.getRiskLevel().label()).append("\n");
            sb.append("Workload Trend:  ").append(trend.label()).append("\n");
            sb.append("\nAccepted Positions (").append(s.getAcceptedJobCount()).append("):\n");
            if (s.getAcceptedJobDescriptions().isEmpty()) {
                sb.append("  (none)\n");
            } else {
                for (String desc : s.getAcceptedJobDescriptions()) {
                    sb.append("  \u2022 ").append(desc).append("\n");
                }
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setEditable(false);
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JOptionPane.showMessageDialog(frame, new JScrollPane(area),
                    "TA Detail \u2014 " + s.getTaName(), JOptionPane.PLAIN_MESSAGE);
        }

        // ========================= Account actions =========================

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
                User newMo = authService.createMoAccount(
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passwordField.getPassword()));
                adminService.addAuditEntry(user == null ? "ADMIN" : user.getId(),
                        "CREATE_MO_ACCOUNT", newMo.getId(),
                        "Created MO account: " + newMo.getName() + " (" + newMo.getEmail() + ")");
                if (notificationService != null && user != null) {
                    notificationService.publish(Role.ADMIN, com.group52.tarecruitment.model.NotificationType.ADMIN_ACCOUNT_CREATED,
                            user.getId(), "Created MO account: " + newMo.getName(), newMo.getId());
                }
                refreshAccounts();
                updateNotificationBadge();
                showToast("MO Account Created", "New MO account created successfully.", JOptionPane.INFORMATION_MESSAGE);
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
            boolean newStatus = !target.get().isActive();
            authService.setUserActive(userId, newStatus);
            adminService.addAuditEntry(user == null ? "ADMIN" : user.getId(),
                    newStatus ? "ACTIVATE_ACCOUNT" : "DEACTIVATE_ACCOUNT", userId,
                    (newStatus ? "Activated" : "Deactivated") + " user: " + target.get().getName());
            if (notificationService != null && user != null) {
                notificationService.publish(Role.ADMIN, com.group52.tarecruitment.model.NotificationType.ADMIN_ACCOUNT_STATUS_CHANGED,
                        user.getId(), (newStatus ? "Activated" : "Deactivated") + " user: " + target.get().getName(), userId);
            }
            refreshAccounts();
            updateNotificationBadge();
            showToast("Account Updated", "Account status has been updated.", JOptionPane.INFORMATION_MESSAGE);
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
                adminService.addAuditEntry(user == null ? "ADMIN" : user.getId(),
                        "RESET_PASSWORD", userId, "Password reset for user: " + userId);
                showToast("Password Reset", "Password reset completed.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void showAccountDetailDialog() {
            int row = accountTable.getSelectedRow();
            if (row < 0) return;
            String userId = String.valueOf(accountModel.getValueAt(row, 0));
            String detail = adminService.getUserDetailSummary(userId);
            JTextArea area = new JTextArea(detail);
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setEditable(false);
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            area.setRows(16);
            area.setColumns(50);
            JOptionPane.showMessageDialog(frame, new JScrollPane(area),
                    "User Detail \u2014 " + accountModel.getValueAt(row, 1), JOptionPane.PLAIN_MESSAGE);
        }

        // ========================= Job actions =========================

        private void showJobDetailDialog() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) return;
            if (row >= cachedJobOverviews.size()) {
                JOptionPane.showMessageDialog(frame, "Please refresh the jobs list first.");
                return;
            }
            AdminService.JobOverview ov = cachedJobOverviews.get(row);
            StringBuilder sb = new StringBuilder();
            sb.append("Job ID:          ").append(ov.jobId).append("\n");
            sb.append("Module Code:     ").append(ov.moduleCode).append("\n");
            sb.append("Module Name:     ").append(ov.moduleName).append("\n");
            sb.append("Description:     ").append(ov.description == null ? "" : ov.description).append("\n");
            sb.append("Required Skills: ").append(ov.requiredSkills == null ? "" : ov.requiredSkills).append("\n");
            sb.append("Hours/Week:      ").append(ov.hoursPerWeek).append("\n");
            sb.append("Positions:       ").append(ov.positions).append("\n");
            sb.append("Filled:          ").append(ov.filled).append("\n");
            sb.append("Deadline:        ").append(ov.deadline == null ? "" : ov.deadline).append("\n");
            sb.append("Posted by MO:    ").append(ov.postedByMoName).append(" (").append(ov.postedByMoId).append(")\n");
            sb.append("Status:          ").append(ov.status.name()).append("\n");
            JTextArea area = new JTextArea(sb.toString());
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setEditable(false);
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JOptionPane.showMessageDialog(frame, new JScrollPane(area),
                    "Job Detail \u2014 " + ov.moduleCode + " " + ov.moduleName, JOptionPane.PLAIN_MESSAGE);
        }

        private void forceCloseSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            if (row >= cachedJobOverviews.size()) {
                JOptionPane.showMessageDialog(frame, "Please refresh the jobs list first.");
                return;
            }
            AdminService.JobOverview ov = cachedJobOverviews.get(row);
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Force-close job: " + ov.moduleCode + " - " + ov.moduleName + "?\nThis action cannot be undone by MO.",
                    "Confirm Force Close", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            try {
                adminService.forceCloseJob(ov.jobId, user == null ? "ADMIN" : user.getId());
                refreshJobs();
                refreshSummaryBar();
                updateNotificationBadge();
                showToast("Job Closed", "Job has been force-closed.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void forceReopenSelectedJob() {
            int row = jobsTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(frame, "Please select a job first.");
                return;
            }
            if (row >= cachedJobOverviews.size()) {
                JOptionPane.showMessageDialog(frame, "Please refresh the jobs list first.");
                return;
            }
            AdminService.JobOverview ov = cachedJobOverviews.get(row);
            try {
                adminService.forceReopenJob(ov.jobId, user == null ? "ADMIN" : user.getId());
                refreshJobs();
                refreshSummaryBar();
                showToast("Job Reopened", "Job has been force-reopened.", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        }

        private void autoCloseExpiredJobs() {
            int confirm = JOptionPane.showConfirmDialog(frame,
                    "Auto-close all OPEN jobs whose deadlines have passed?",
                    "Confirm Auto-Close", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;
            int count = adminService.triggerAutoCloseExpiredJobs(user == null ? "ADMIN" : user.getId());
            refreshJobs();
            refreshSummaryBar();
            updateNotificationBadge();
            if (count == 0) {
                showToast("No Expired Jobs", "All OPEN jobs are within their deadlines.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showToast("Jobs Closed", count + " expired job(s) have been closed.", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        // ========================= Export =========================

        private void exportWorkloadCsv() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export Workload CSV");
            chooser.setSelectedFile(new File("workload_export.csv"));
            if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
            try {
                adminService.exportWorkloadToCsv(chooser.getSelectedFile().toPath());
                adminService.addAuditEntry(user == null ? "ADMIN" : user.getId(),
                        "EXPORT_WORKLOAD_CSV", chooser.getSelectedFile().getName(), "Exported workload data to CSV.");
                showToast("Export Complete", "Workload data exported to:\n" + chooser.getSelectedFile().getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Export failed: " + ex.getMessage());
            }
        }

        private void exportJobsCsv() {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export Jobs CSV");
            chooser.setSelectedFile(new File("jobs_export.csv"));
            if (chooser.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
            try {
                adminService.exportJobsToCsv(chooser.getSelectedFile().toPath());
                adminService.addAuditEntry(user == null ? "ADMIN" : user.getId(),
                        "EXPORT_JOBS_CSV", chooser.getSelectedFile().getName(), "Exported jobs data to CSV.");
                showToast("Export Complete", "Jobs data exported to:\n" + chooser.getSelectedFile().getAbsolutePath(), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Export failed: " + ex.getMessage());
            }
        }

        // ========================= Notifications & Anomalies =========================
        
        private void refreshAdminNotifications() {
            notificationsModel.setRowCount(0);
            if (user == null || notificationService == null) return;
            List<com.group52.tarecruitment.model.Notification> notifs = notificationService.getNotificationsForUser(user.getId());
            for (com.group52.tarecruitment.model.Notification n : notifs) {
                notificationsModel.addRow(new Object[] {
                    n.getType().name(),
                    n.getRelatedId(),
                    n.getMessage(),
                    n.getCreatedAt(),
                    n.isReadStatus() ? "Yes" : "No"
                });
            }
        }

        private void handleBroadcastAnomalies() {
            int count = adminService.broadcastAnomalies(user == null ? "ADMIN" : user.getId());
            refreshAdminNotifications();
            updateNotificationBadge();
            if (count > 0) {
                showToast("Anomalies Broadcasted", count + " anomaly alert(s) sent successfully.", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showToast("No Anomalies", "System is healthy. No anomalies found.", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        private void showSendNotificationDialog() {
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ALL", "TA", "MO"});
            JTextField userIdField = new JTextField();
            JTextArea messageArea = new JTextArea(4, 20);
            messageArea.setLineWrap(true);
            messageArea.setWrapStyleWord(true);
            
            JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
            panel.add(new JLabel("Target Role:"));
            panel.add(roleCombo);
            panel.add(new JLabel("Target User ID (Optional):"));
            panel.add(userIdField);
            panel.add(new JLabel("Message:"));
            panel.add(new JScrollPane(messageArea));
            
            int result = JOptionPane.showConfirmDialog(frame, panel, "Send Notification", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String roleStr = (String) roleCombo.getSelectedItem();
                Role role = "ALL".equals(roleStr) ? null : Role.valueOf(roleStr);
                String userId = userIdField.getText().trim();
                String message = messageArea.getText().trim();
                
                if (message.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Message cannot be empty.");
                    return;
                }
                
                int count = adminService.sendCustomNotification(user == null ? "ADMIN" : user.getId(), role, userId, message);
                refreshAdminNotifications();
                updateNotificationBadge();
                showToast("Notification Sent", "Sent to " + count + " user(s).", JOptionPane.INFORMATION_MESSAGE);
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

        private JobInput(String moduleCode, String moduleName, String description, String requiredSkills,
                int hoursPerWeek, int positions, String deadline) {
            this.moduleCode = moduleCode;
            this.moduleName = moduleName;
            this.description = description;
            this.requiredSkills = requiredSkills;
            this.hoursPerWeek = hoursPerWeek;
            this.positions = positions;
            this.deadline = deadline;
        }
    }

    // ========================= Demo support methods =========================

    JFrame getMainFrame() {
        return frame;
    }

    void demoEnterAs(User user) {
        SwingUtilities.invokeLater(() -> onLoginSuccess(user));
    }

    void demoRefreshCurrentPanel() {
        SwingUtilities.invokeLater(() -> {
            if (taPanel != null) {
                taPanel.demoShowTab(TaPanel.TAB_DASHBOARD);
            }
            if (moPanel != null) {
                moPanel.demoShowTab(MoPanel.TAB_DASHBOARD);
            }
            if (adminPanel != null) {
                adminPanel.demoShowTab(AdminPanel.TAB_WORKLOAD);
            }
        });
    }

    void demoSelectTaTab(String tab) {
        SwingUtilities.invokeLater(() -> { if (taPanel != null) taPanel.demoShowTab(tab); });
    }

    void demoSelectMoTab(String tab) {
        SwingUtilities.invokeLater(() -> { if (moPanel != null) moPanel.demoShowTab(tab); });
    }

    void demoSelectAdminTab(String tab) {
        SwingUtilities.invokeLater(() -> { if (adminPanel != null) adminPanel.demoShowTab(tab); });
    }

    void demoShowRegisterPage() {
        SwingUtilities.invokeLater(() -> {
            if (registerPanel != null) {
                registerPanel.reset();
                showPage(PAGE_REGISTER);
            }
        });
    }

    void demoReturnToLogin() {
        SwingUtilities.invokeLater(this::showLoginPage);
    }

    void demoShowCaption(String captionEn, String captionCn) {
        SwingUtilities.invokeLater(() -> showDemoCaption(captionEn, captionCn, false));
    }

    void demoAppendCaption(String captionEn, String captionCn) {
        SwingUtilities.invokeLater(() -> showDemoCaption(captionEn, captionCn, true));
    }

    void demoHideCaption() {
        SwingUtilities.invokeLater(() -> {
            if (demoCaptionLabel != null) {
                demoCaptionLabel.setVisible(false);
            }
        });
    }

    private void showDemoCaption(String captionEn, String captionCn, boolean append) {
        if (frame == null) {
            return;
        }
        ensureDemoCaption();
        String existing = "";
        if (append && demoCaptionLabel.isVisible() && demoCaptionLabel.getText() != null) {
            existing = demoCaptionLabel.getText()
                    .replace("<html>", "")
                    .replace("</html>", "");
        }
        String html = "<html><div style='text-align:center;line-height:1.55'>"
                + (existing.isEmpty() ? "" : existing + "<br>")
                + "<span style='color:#FFFFFF;font-size:14px'><b>" + escapeHtml(captionEn) + "</b></span><br>"
                + "<span style='color:#E6DBFF;font-size:13px'>" + escapeHtml(captionCn) + "</span>"
                + "</div></html>";
        demoCaptionLabel.setText(html);
        demoCaptionLabel.setVisible(true);
        repositionDemoCaption();
    }

    private void ensureDemoCaption() {
        if (demoCaptionLabel != null) {
            return;
        }
        demoCaptionLabel = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(40, 19, 78, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(123, 92, 240, 180));
                g2.setStroke(new java.awt.BasicStroke(1.4f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        demoCaptionLabel.setOpaque(false);
        demoCaptionLabel.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        demoCaptionLabel.setVisible(false);

        javax.swing.JLayeredPane layeredPane = frame.getLayeredPane();
        layeredPane.add(demoCaptionLabel, javax.swing.JLayeredPane.POPUP_LAYER);

        frame.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                repositionDemoCaption();
            }
        });
    }

    private void repositionDemoCaption() {
        if (demoCaptionLabel == null || frame == null) {
            return;
        }
        int frameWidth = frame.getContentPane().getWidth();
        int frameHeight = frame.getContentPane().getHeight();
        Dimension pref = demoCaptionLabel.getPreferredSize();
        int width = Math.min(Math.max(pref.width, 540), frameWidth - 80);
        int height = pref.height;
        int x = (frameWidth - width) / 2;
        int y = frameHeight - height - 28;
        demoCaptionLabel.setBounds(x, y, width, height);
        demoCaptionLabel.revalidate();
        demoCaptionLabel.repaint();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

