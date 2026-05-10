package com.group52.tarecruitment.ui;

import com.group52.tarecruitment.model.Application;
import com.group52.tarecruitment.model.ApplicationStatus;
import com.group52.tarecruitment.model.Job;
import com.group52.tarecruitment.model.JobStatus;
import com.group52.tarecruitment.model.Notification;
import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.service.AdminService;
import com.group52.tarecruitment.service.ApplicationService;
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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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

    private final AuthService authService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final AiMatchingService aiMatchingService;
    private final MoApplicantRankingService moApplicantRankingService;
    private final AdminService adminService;
    private final NotificationService notificationService;
    private final Path dataDirectory;

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
            stylePrimaryButton(logoutButton);
            logoutButton.setBackground(QMUL_PURPLE);
            logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (logoutButton.isEnabled()) {
                        logoutButton.setBackground(new Color(107, 63, 160));
                    }
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    if (logoutButton.isEnabled()) {
                        logoutButton.setBackground(QMUL_PURPLE);
                    }
                }
            });
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

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private void stylePrimaryButton(JButton button) {
        styleUnifiedButton(button, true, false);
    }

    private void styleSecondaryButton(JButton button) {
        styleUnifiedButton(button, false, false);
    }

    private void styleDangerButton(JButton button) {
        styleUnifiedButton(button, true, true);
    }

    private void styleUnifiedButton(JButton button, boolean bold, boolean danger) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(QMUL_PURPLE);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, 14));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.putClientProperty("button.hover", Boolean.FALSE);
        button.putClientProperty("button.disabledBg", new Color(120, 105, 145));
        button.putClientProperty("button.disabledFg", Color.WHITE);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(new Color(107, 63, 160));
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(QMUL_PURPLE);
                }
            }
        });
        button.addPropertyChangeListener("enabled", evt -> {
            if (button.isEnabled()) {
                button.setBackground(QMUL_PURPLE);
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

    private JPanel createAuthBrandPanel(boolean includeTagline) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("BUPT x QMUL");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 30));
        brand.setForeground(QMUL_PURPLE);
        JLabel system = new JLabel("TA Recruitment System");
        system.setFont(new Font("Segoe UI", Font.BOLD, 24));
        system.setForeground(new Color(36, 41, 56));
        JLabel tagline = new JLabel(BRAND_TAGLINE);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(MUTED_TEXT_COLOR);
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
                    fg = Color.WHITE;
                    bg = BADGE_GREEN;
                } else if (status.contains("REJECTED") || status.contains("CLOSED")) {
                    fg = Color.WHITE;
                    bg = BADGE_RED;
                } else if (status.contains("PENDING") || status.contains("REVIEWING") || status.contains("APPLIED")) {
                    fg = Color.WHITE;
                    bg = BADGE_ORANGE;
                } else if (status.contains("WITHDRAWN")) {
                    fg = Color.WHITE;
                    bg = new Color(124, 133, 150);
                } else if (status.contains("FILLED")) {
                    fg = Color.WHITE;
                    bg = new Color(123, 92, 240);
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

        private LoginPanel() {
            setLayout(new BorderLayout());
            JPanel centerWrapper = new JPanel(new GridBagLayout());
            centerWrapper.setOpaque(false);

            JPanel card = new JPanel(new BorderLayout(24, 0));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 229, 236), 1, true),
                    BorderFactory.createEmptyBorder(24, 24, 24, 24)));
            card.setPreferredSize(new Dimension(940, 520));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setPreferredSize(new Dimension(340, 0));
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(createAuthBrandPanel(true));
            left.add(Box.createVerticalStrut(20));
            JLabel loginHint = new JLabel("Sign in to manage jobs, applications, and notifications.");
            loginHint.setForeground(MUTED_TEXT_COLOR);
            loginHint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            left.add(loginHint);
            left.add(Box.createVerticalStrut(18));
            JLabel loginCardBadge = new JLabel("Fast access to TA, MO, and Admin portals");
            loginCardBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
            loginCardBadge.setForeground(QMUL_PURPLE);
            left.add(loginCardBadge);

            JPanel form = new JPanel(new GridLayout(0, 2, 15, 14));
            form.setPreferredSize(new Dimension(450, 250));
            form.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 225, 233), 1, true),
                    BorderFactory.createEmptyBorder(28, 28, 28, 28)));
            form.setBackground(Color.WHITE);
            form.setOpaque(true);

            JLabel roleLabel = new JLabel("Role");
            roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            form.add(roleLabel);
            roleCombo = new JComboBox<>(new Role[] {Role.TA, Role.MO, Role.ADMIN});
            roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(roleCombo);

            JLabel emailLabel = new JLabel("Email");
            emailLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            form.add(emailLabel);
            emailField = new JTextField();
            emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(emailField);

            JLabel pwdLabel = new JLabel("Password");
            pwdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            form.add(pwdLabel);
            passwordField = new JPasswordField();
            passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            form.add(passwordField);

            loginButton = new JButton("Login");
            stylePrimaryButton(loginButton);
            loginButton.addActionListener(e -> login());
            form.add(loginButton);

            registerButton = new JButton("Register as TA");
            styleSecondaryButton(registerButton);
            registerButton.addActionListener(e -> showRegisterPage());
            form.add(registerButton);

            statusLabel = new JLabel(" ");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(new Color(100, 100, 100));

            JPanel centerContainer = new JPanel(new BorderLayout());
            centerContainer.setOpaque(false);
            centerContainer.add(form, BorderLayout.CENTER);
            centerContainer.add(statusLabel, BorderLayout.SOUTH);

            card.add(left, BorderLayout.WEST);
            card.add(centerContainer, BorderLayout.CENTER);
            centerWrapper.add(card);

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
            showRegisterPage();
        }
    }

    private abstract class AvatarAwarePanel extends JPanel {
        protected final String[] presetAvatarFiles = AVATAR_FILES;

        protected JButton createAvatarChoiceButton(String fileName, String label, java.util.function.Consumer<String> onSelect) {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(76, 76));
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
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                    0, 0, new Color(248, 250, 255),
                    getWidth(), getHeight(), new Color(233, 239, 255));
            g2.setPaint(gradient);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(90, 35, 130, 18));
            g2.fillOval(getWidth() - 240, 40, 180, 180);
            g2.fillOval(30, getHeight() - 220, 160, 160);
            g2.dispose();
        }

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
        private final JPanel avatarWallPanel;
        private final List<JButton> avatarChoiceButtons = new ArrayList<>();
        private String selectedAvatarPath = "";

        private RegisterPanel() {
            setLayout(new BorderLayout());
            setOpaque(false);

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            JPanel card = new JPanel(new BorderLayout(24, 0));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 229, 236), 1, true),
                    BorderFactory.createEmptyBorder(24, 24, 24, 24)));
            card.setPreferredSize(new Dimension(980, 560));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setPreferredSize(new Dimension(350, 0));
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(createAuthBrandPanel(true));
            left.add(Box.createVerticalStrut(18));
            JLabel heading = new JLabel("Create your TA profile");
            heading.setFont(new Font("Segoe UI", Font.BOLD, 24));
            heading.setForeground(new Color(36, 41, 56));
            JLabel subheading = new JLabel("Upload a photo, fill in your details, and register in one place.");
            subheading.setForeground(MUTED_TEXT_COLOR);
            subheading.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            JLabel avatarSectionLabel = new JLabel("Avatar Preview");
            avatarSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            avatarSectionLabel.setForeground(new Color(36, 41, 56));
            avatarPreview = new JLabel(loadAvatarIcon("", 220));
            avatarPreview.setAlignmentX(CENTER_ALIGNMENT);
            avatarPreview.setPreferredSize(new Dimension(220, 220));
            avatarPreview.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 219, 229), 1, true),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            avatarPathLabel = new JLabel("Using default avatar");
            avatarPathLabel.setForeground(MUTED_TEXT_COLOR);
            avatarPathLabel.setAlignmentX(CENTER_ALIGNMENT);
            avatarWallPanel = new JPanel(new GridLayout(2, 2, 10, 10));
            avatarWallPanel.setOpaque(false);
            for (String fileName : presetAvatarFiles) {
                JButton choiceButton = createAvatarChoiceButton(fileName, fileName, this::selectPresetAvatar);
                avatarChoiceButtons.add(choiceButton);
                avatarWallPanel.add(choiceButton);
            }
            JButton resetAvatarButton = new JButton("Use Default Avatar");
            styleSecondaryButton(resetAvatarButton);
            resetAvatarButton.setAlignmentX(CENTER_ALIGNMENT);
            resetAvatarButton.addActionListener(e -> resetRegisterAvatar());
            left.add(heading);
            left.add(Box.createVerticalStrut(8));
            left.add(subheading);
            left.add(Box.createVerticalStrut(18));
            left.add(avatarSectionLabel);
            left.add(Box.createVerticalStrut(10));
            left.add(avatarPreview);
            left.add(Box.createVerticalStrut(10));
            left.add(avatarPathLabel);
            left.add(Box.createVerticalStrut(14));
            left.add(avatarWallPanel);
            left.add(Box.createVerticalStrut(12));
            left.add(resetAvatarButton);

            JPanel right = new JPanel(new GridLayout(0, 2, 12, 12));
            right.setOpaque(false);
            right.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            studentIdField = new JTextField();
            nameField = new JTextField();
            emailField = new JTextField();
            passwordField = new JPasswordField();
            programmeField = new JTextField();
            yearField = new JTextField();
            skillsArea = new JTextArea(4, 20);
            hoursField = new JTextField();
            right.add(createFieldLabel("Student ID"));
            right.add(studentIdField);
            right.add(createFieldLabel("Name"));
            right.add(nameField);
            right.add(createFieldLabel("Email"));
            right.add(emailField);
            right.add(createFieldLabel("Password"));
            right.add(passwordField);
            right.add(createFieldLabel("Programme"));
            right.add(programmeField);
            right.add(createFieldLabel("Year of Study"));
            right.add(yearField);
            right.add(createFieldLabel("Skills"));
            right.add(new JScrollPane(skillsArea));
            right.add(createFieldLabel("Available Hours/Week"));
            right.add(hoursField);

            JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            actionRow.setOpaque(false);
            JButton submitButton = new JButton("Register Now");
            stylePrimaryButton(submitButton);
            submitButton.addActionListener(e -> submitRegister());
            JButton backButton = new JButton("Back to Login");
            styleSecondaryButton(backButton);
            backButton.addActionListener(e -> showLoginPage());
            actionRow.add(submitButton);
            actionRow.add(backButton);

            JPanel rightWrapper = new JPanel(new BorderLayout(0, 18));
            rightWrapper.setOpaque(false);
            rightWrapper.add(right, BorderLayout.CENTER);
            rightWrapper.add(actionRow, BorderLayout.SOUTH);

            card.add(left, BorderLayout.WEST);
            card.add(rightWrapper, BorderLayout.CENTER);
            wrapper.add(card);
            add(wrapper, BorderLayout.CENTER);
        }

        private JLabel createFieldLabel(String text) {
            JLabel label = new JLabel(text);
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            return label;
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
            avatarPreview.setIcon(loadAvatarIcon("", 180));
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
                    avatarPreview.setIcon(loadAvatarIcon(selectedAvatarPath, 180));
                    avatarPathLabel.setText(new File(selectedAvatarPath).getName());
                }
            }
        }

        private void selectPresetAvatar(String fileName) {
            selectedAvatarPath = AVATAR_DIR + "/" + fileName;
            avatarPreview.setIcon(loadAvatarIcon(selectedAvatarPath, 180));
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
            avatarPreview.setIcon(loadAvatarIcon("", 180));
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
        private static final String TAB_DASHBOARD = "dashboard";
        private static final String TAB_JOB_BOARD = "jobBoard";
        private static final String TAB_PROFILE = "profile";
        private static final String TAB_NOTIFICATIONS = "notifications";

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
        private final JComboBox<String> notificationFilterBox;
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
                    new Object[] {"Job ID", "Module", "MO", "Hours/Week", "Deadline", "Status"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            jobTable = new JTable(jobModel);
            styleDataTable(jobTable);
            applyStatusRenderer(jobTable, 5);
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
            searchPanel.add(filterActionsPanel);
            JPanel jobControlsCard = createCardPanel(searchPanel, 18, 18, 18, 18);
            jobBoardPanel.add(jobControlsCard, BorderLayout.NORTH);
            JScrollPane jobScrollPane = new JScrollPane(jobTable);
            jobScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobScrollPane.getViewport().setBackground(Color.WHITE);
            jobBoardPanel.add(createCardPanel(jobScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
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

            form.add(new JLabel("Avatar Photo"));
            JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            avatarPanel.setOpaque(false);
            for (String fileName : presetAvatarFiles) {
                avatarPanel.add(createAvatarChoiceButton(fileName, fileName, chosenFileName -> {
                    selectedAvatarPath = AVATAR_DIR + "/" + chosenFileName;
                    profileAvatarLabel.setIcon(loadAvatarIcon(selectedAvatarPath, 88));
                    updateTopBarAvatar(selectedAvatarPath);
                }));
            }
            form.add(avatarPanel);

            profilePanel.add(form, BorderLayout.CENTER);
            JPanel profileActions = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton saveButton = new JButton("Save Profile");
            stylePrimaryButton(saveButton);
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
            notificationControls.add(unreadCountLabel);
            notificationControls.add(new JLabel("Show"));
            notificationControls.add(notificationFilterBox);
            notificationControls.add(refreshNotificationsButton);
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
            };
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
                    + "<b>Progress:</b> " + scoreProgressBar(matchResult.getScore()) + "<br/>"
                    + "<b>Matched Skills:</b> " + matchedSkills + "<br/>"
                    + "<b>Missing Skills:</b> " + missingSkills + "<br/>"
                    + "<b>Reason:</b> " + matchResult.getReason() + "<br/><br/>"
                    + "<span style='color:#5A2382'><b>" + BRAND_TAGLINE + "</b></span>"
                    + "</div></html>";
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
        private final JCheckBox pendingOnlyCheckBox;
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
            styleDataTable(jobsTable);
            applyStatusRenderer(jobsTable, 4);
            installTableRowHover(jobsTable);
            JPanel dashboardPanel = new JPanel(new BorderLayout(0, 16));
            dashboardPanel.setOpaque(false);
            JPanel dashboardHeader = new JPanel(new BorderLayout());
            dashboardHeader.setOpaque(false);
            JPanel dashboardHeaderText = createSectionTitle("My Posted Jobs", "Overview of jobs, applicants, and statuses.");
            dashboardHeader.add(dashboardHeaderText, BorderLayout.WEST);
            dashboardPanel.add(createCardPanel(dashboardHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane jobsScrollPane = new JScrollPane(jobsTable);
            jobsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobsScrollPane.getViewport().setBackground(Color.WHITE);
            dashboardPanel.add(createCardPanel(jobsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel jobActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
                        "App ID", "Applicant", "Year", "Match Score", "Missing Skills",
                        "Current Workload", "Recommendation", "Status"
                    }, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    switch (columnIndex) {
                        case 2:
                        case 3:
                        case 5:
                            return Integer.class;
                        default:
                            return String.class;
                    }
                }
            };
            applicantsTable = new JTable(applicantsModel);
            styleDataTable(applicantsTable);
            applyIntegerSuffixRenderer(applicantsTable, 3, "%");
            applyIntegerSuffixRenderer(applicantsTable, 5, "h/week");
            applyRecommendationRenderer(applicantsTable, 6);
            applyStatusRenderer(applicantsTable, 7);
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
            pendingOnlyCheckBox.addActionListener(e -> refreshApplicants());
            applicantsControls.add(pendingOnlyCheckBox);
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
            JButton sortMatchButton = new JButton("Sort by Match Score");
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
            stylePrimaryButton(saveProfileButton);
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
            List<Application> applications = applicationService.getApplicationsByJobId(selectedJobId);
            Map<String, User> applicantsById = new LinkedHashMap<>();
            for (Application application : applications) {
                findUserById(application.getTaUserId())
                        .ifPresent(user -> applicantsById.put(user.getId(), user));
            }
            MoApplicantRankingService.RankingOptions options = new MoApplicantRankingService.RankingOptions(
                    pendingOnlyCheckBox.isSelected(),
                    (Integer) matchThresholdSpinner.getValue(),
                    applicantSortMode);
            for (MoApplicantRankingService.RankedApplicant applicant : moApplicantRankingService.rankApplicants(
                    selectedJob, applications, applicantsById, options)) {
                rankedApplicantsByApplicationId.put(applicant.getApplicationId(), applicant);
                applicantsModel.addRow(new Object[] {
                    applicant.getApplicationId(),
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
                applicantsTable.getRowSorter().setSortKeys(List.of());
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
            try {
                applicationService.updateApplicationStatus(appId, user.getId(), status);
                refreshApplicants();
                refreshJobs();
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
            if (existing != null) {
                JLabel statusHint = new JLabel(
                        "Status: " + existing.getStatus().name() + "  (use Close / Reopen to change)");
                statusHint.setForeground(MUTED_TEXT_COLOR);
                panel.add(statusHint);
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

        // Summary bar labels
        private final JLabel summaryTotalJobs = new JLabel("--");
        private final JLabel summaryFilledJobs = new JLabel("--");
        private final JLabel summaryOverloaded = new JLabel("--");
        private final JLabel summaryHighRisk = new JLabel("--");
        private final JTextField workloadSearchField = new JTextField();

        private AdminPanel() {
            setLayout(new BorderLayout());
            titleLabel = new JLabel("Admin Dashboard");
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            JPanel topBar = buildTopBar("Admin Dashboard", SwingApp.this::showLoginPage);
            JPanel northArea = new JPanel(new BorderLayout());
            northArea.setOpaque(false);
            northArea.add(topBar, BorderLayout.NORTH);
            northArea.add(buildSummaryBar(), BorderLayout.SOUTH);
            add(northArea, BorderLayout.NORTH);

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
            workloadPanel.add(createCardPanel(workloadScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JButton refreshWorkloadButton = new JButton("Refresh");
            styleSecondaryButton(refreshWorkloadButton);
            refreshWorkloadButton.addActionListener(e -> refreshWorkload());
            JButton showOverloadedButton = new JButton("Overloaded Only");
            styleDangerButton(showOverloadedButton);
            showOverloadedButton.addActionListener(e -> showOverloadedOnly());
            JButton exportReportButton = new JButton("View Report");
            styleSecondaryButton(exportReportButton);
            exportReportButton.addActionListener(e -> showWorkloadReport());
            JPanel workloadActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            workloadActions.setOpaque(false);
            workloadActions.add(refreshWorkloadButton);
            workloadActions.add(showOverloadedButton);
            workloadActions.add(exportReportButton);
            workloadPanel.add(workloadActions, BorderLayout.SOUTH);

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
            JPanel accountsPanel = new JPanel(new BorderLayout(0, 16));
            accountsPanel.setOpaque(false);
            JPanel accountsHeader = new JPanel(new BorderLayout());
            accountsHeader.setOpaque(false);
            accountsHeader.add(createSectionTitle("User Accounts", "Create, activate, or reset accounts in one place."), BorderLayout.WEST);
            accountsPanel.add(createCardPanel(accountsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane accountsScrollPane = new JScrollPane(accountTable);
            accountsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            accountsScrollPane.getViewport().setBackground(Color.WHITE);
            accountsPanel.add(createCardPanel(accountsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel accountActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
            JPanel jobsPanel = new JPanel(new BorderLayout(0, 16));
            jobsPanel.setOpaque(false);
            JPanel jobsHeader = new JPanel(new BorderLayout());
            jobsHeader.setOpaque(false);
            jobsHeader.add(createSectionTitle("Global Jobs", "A complete view of all modules and filling status."), BorderLayout.WEST);
            jobsPanel.add(createCardPanel(jobsHeader, 18, 18, 18, 18), BorderLayout.NORTH);
            JScrollPane jobsScrollPane = new JScrollPane(jobsTable);
            jobsScrollPane.setBorder(BorderFactory.createEmptyBorder());
            jobsScrollPane.getViewport().setBackground(Color.WHITE);
            jobsPanel.add(createCardPanel(jobsScrollPane, 0, 0, 0, 0), BorderLayout.CENTER);
            JPanel jobsActions = new JPanel(new BorderLayout());
            jobsActions.setOpaque(false);
            JPanel jobsButtonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            jobsButtonRow.setOpaque(false);
            JButton refreshJobsButton = new JButton("Refresh");
            styleSecondaryButton(refreshJobsButton);
            refreshJobsButton.addActionListener(e -> refreshJobs());
            jobsButtonRow.add(refreshJobsButton);
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

            contentPanel.add(workloadPanel, TAB_WORKLOAD);
            contentPanel.add(accountsPanel, TAB_ACCOUNTS);
            contentPanel.add(jobsPanel, TAB_JOBS);
            add(contentPanel, BorderLayout.CENTER);
        }

        private void bindUser(User user) {
            refreshWorkload();
            refreshAccounts();
            refreshJobs();
            updateTopBarAvatar(user == null ? "" : user.getAvatarFilePath());
            contentLayout.show(contentPanel, TAB_WORKLOAD);
        }

        private void refreshWorkload() {
            workloadModel.setRowCount(0);
            for (AdminService.TAWorkloadSummary s : adminService.getAllTAWorkloads()) {
                workloadModel.addRow(new Object[] {
                    s.getTaUserId(),
                    s.getTaName(),
                    s.getAvailableHours(),
                    s.getTotalAssignedHours(),
                    s.getRemainingHours(),
                    s.getRiskLevel().label()
                });
            }
            adminService.publishOverloadAlerts();
            refreshSummaryBar();
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
            for (AdminService.JobOverview overview : adminService.getJobsOverview()) {
                jobsModel.addRow(new Object[] {
                    overview.moduleCode + " - " + overview.moduleName,
                    overview.filledRatio(),
                    overview.status.name()
                });
            }
        }

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
                    setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
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
                    setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                    if (!isSelected) {
                        boolean isFull = text.contains("/") && !text.startsWith("0/") &&
                                text.split("/").length == 2 &&
                                Integer.parseInt(text.split("/")[0]) >= Integer.parseInt(text.split("/")[1]);
                        if (isFull) {
                            setForeground(Color.WHITE);
                            setBackground(new Color(239, 68, 68));
                        } else {
                            setForeground(new Color(46, 52, 64));
                            setBackground(Color.WHITE);
                        }
                    }
                    return this;
                }
            });
        }

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
        }


        private void showWorkloadReport() {
            String report = adminService.getWorkloadReport();
            JTextArea textArea = new JTextArea(report);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setEditable(false);
            textArea.setRows(20);
            textArea.setColumns(60);
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

        private JPanel buildSummaryBar() {
            JPanel bar = new JPanel(new java.awt.GridLayout(1, 4, 12, 0));
            bar.setBackground(new Color(245, 246, 250));
            bar.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 223, 230)),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)));
            bar.add(buildSummaryCard("Total Jobs", summaryTotalJobs, new Color(99, 102, 241)));
            bar.add(buildSummaryCard("Filled Jobs", summaryFilledJobs, new Color(16, 185, 129)));
            bar.add(buildSummaryCard("Overloaded TAs", summaryOverloaded, new Color(239, 68, 68)));
            bar.add(buildSummaryCard("High-Risk TAs", summaryHighRisk, new Color(245, 158, 11)));
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
        }

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
                    sb.append("  • ").append(desc).append("\n");
                }
            }
            JTextArea area = new JTextArea(sb.toString());
            area.setFont(new Font("Monospaced", Font.PLAIN, 13));
            area.setEditable(false);
            area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            JOptionPane.showMessageDialog(frame, new JScrollPane(area),
                    "TA Detail — " + s.getTaName(), JOptionPane.PLAIN_MESSAGE);
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
            authService.setUserActive(userId, !target.get().isActive());
            refreshAccounts();
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
                showToast("Password Reset", "Password reset completed.", JOptionPane.INFORMATION_MESSAGE);
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

}
