package gui.views;

import gui.controllers.AdminDashboardController;
import gui.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * AdminDashboard - Main dashboard for admin users
 * 
 * Features:
 * - Inventory Management (View, Add, Remove, Update)
 * - Reservation Management (View, Approve, Cancel)
 * - Account Management (View, Activate/Deactivate, Change Password)
 * - Stock Logs
 */
public class AdminDashboard {
    
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private Label titleLabel;
    private AdminDashboardController controller;
    
    // Sidebar buttons
    private Button dashboardBtn;
    private Button accountsBtn;
    private Button stockLogsBtn;
    private Button reportsBtn;
    private Button systemSettingsBtn;
    private Button helpBtn;
    private Button logoutBtn;
    
    // Sidebar labels for theme updates
    private Label logoLabel;
    private Label subtitleLabel;
    
    // Theme toggle components
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    
    // Track currently active button for theme refresh
    private Button activeButton;
    // Preserve help expanded pane title across theme toggles
    private String helpExpandedTitle;
    
    public AdminDashboard() {
        controller = new AdminDashboardController();
        initializeView();
        // Ensure dashboard listens for theme changes so styles are reapplied correctly
        javafx.application.Platform.runLater(() -> ThemeManager.addThemeChangeListener(() -> javafx.application.Platform.runLater(this::updateSidebarTheme)));
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.getBackgroundColor();
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Create sidebar
        createSidebar();
        view.setLeft(sidebar);
        
        // Create top bar
        createTopBar();
        view.setTop(createTopBar());
        
        // Create content area - fills remaining space
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.getBackgroundColor();
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        // Show dashboard by default (this will also wire up quick actions)
        showDashboard();
    }
    
    /**
     * Wire up quick action buttons from the dashboard view
     */
    private void wireQuickActions() {
        // Get the buttons from controller (they're created when createDashboardView is called)
        Button manageAccountsBtn = controller.getManageAccountsBtn();
        
        // Wire up actions if buttons exist
        if (manageAccountsBtn != null) {
            manageAccountsBtn.setOnAction(e -> {
                setActiveButton(accountsBtn);
                showAccounts();
            });
        }
    }
    
    /**
     * Create top navigation bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topBar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        titleLabel = new Label("Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle switch (smooth animation)
        toggleSwitch = new StackPane();
        toggleSwitch.setPrefWidth(70);
        toggleSwitch.setPrefHeight(32);
        toggleSwitch.setMaxWidth(70);
        toggleSwitch.setMaxHeight(32);
        
        Region toggleBg = new Region();
        toggleBg.setPrefWidth(70);
        toggleBg.setPrefHeight(32);
        String toggleBgColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(to right, #1e3a5f 0%, #0f2a4a 50%, #1e3a5f 100%)"
            : "linear-gradient(to right, #fff9e6 0%, #ffefb3 50%, #fff9e6 100%)";
        toggleBg.setStyle(
            "-fx-background-color: " + toggleBgColor + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "rgba(77, 163, 255, 0.3)" : "rgba(245, 197, 66, 0.3)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);"
        );
        
        toggleCircle = new StackPane();
        toggleCircle.setPrefWidth(26);
        toggleCircle.setPrefHeight(26);
        toggleCircle.setMaxWidth(26);
        toggleCircle.setMaxHeight(26);
        
        String circleColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
        toggleCircle.setStyle(
            "-fx-background-color: " + circleColor + ";" +
            "-fx-background-radius: 13px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        toggleIcon = new Label(ThemeManager.isDarkMode() ? "🌙" : "☀");
        toggleIcon.setFont(Font.font("System", FontWeight.BOLD, 12));
        toggleIcon.setStyle("-fx-text-fill: #000000;");
        toggleCircle.getChildren().add(toggleIcon);
        
        StackPane.setAlignment(toggleCircle, ThemeManager.isDarkMode() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        
        toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
        toggleSwitch.setOnMouseClicked(e -> toggleTheme());
        toggleSwitch.setStyle("-fx-cursor: hand;");
        
        Label adminLabel = new Label("👤 Admin");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        adminLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, toggleSwitch, adminLabel);
        return topBar;
    }
    
    /**
     * Create sidebar navigation
     */
    private void createSidebar() {
        sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20));
        
        // Gradient blue sidebar in light mode, gradient dark blue in dark mode
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to bottom, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to bottom, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        sidebar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        // Logo image
        ImageView logoImage = new ImageView();
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(
                new java.io.FileInputStream("src/database/data/images/NewLogo.png")
            );
            logoImage.setImage(img);
            logoImage.setFitWidth(50);
            logoImage.setFitHeight(50);
            logoImage.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Failed to load logo: " + e.getMessage());
        }
        
        // Logo/Title
        logoLabel = new Label("STI ProWear Novaliches");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        subtitleLabel = new Label("Admin Panel");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(10, logoImage, logoLabel, subtitleLabel);
        header.setAlignment(Pos.TOP_CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        dashboardBtn = createNavButton("📊 Dashboard", true);
        accountsBtn = createNavButton("👥 Accounts", false);
        stockLogsBtn = createNavButton("📋 Stock Logs", false);
        reportsBtn = createNavButton("📈 Reports", false);
        systemSettingsBtn = createNavButton("⚙️ System Settings", false);
        helpBtn = createNavButton("❓ Help", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        String logoutColor = ThemeManager.isDarkMode() ? "white" : "rgba(255,255,255,0.9)";
        // Explicit danger color to avoid depending on theme CSS variable resolution
        String logoutBg = ThemeManager.isDarkMode() ? "#CF222E" : "rgba(255,255,255,0.15)";
        logoutBtn.setStyle(
            "-fx-background-color: " + logoutBg + ";" +
            "-fx-text-fill: " + logoutColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: center;" +
            "-fx-padding: 12px;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            (ThemeManager.isDarkMode() ? "-fx-font-weight: bold;" : "")
        );
        
        // Button actions
        dashboardBtn.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });
        
        accountsBtn.setOnAction(e -> {
            setActiveButton(accountsBtn);
            showAccounts();
        });
        
        stockLogsBtn.setOnAction(e -> {
            setActiveButton(stockLogsBtn);
            showStockLogs();
        });
        
        reportsBtn.setOnAction(e -> {
            setActiveButton(reportsBtn);
            showReports();
        });
        
        systemSettingsBtn.setOnAction(e -> {
            setActiveButton(systemSettingsBtn);
            showSystemSettings();
        });

        helpBtn.setOnAction(e -> {
            setActiveButton(helpBtn);
            showHelp();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        // Replace Separator with a 1px high Region to avoid default Separator rendering
        Region sepTop = new Region();
        sepTop.setPrefHeight(1);
        sepTop.setMaxHeight(1);
        sepTop.setStyle("-fx-background-color: #FFFFFF; -fx-opacity: 1;");
        Region sepBottom = new Region();
        sepBottom.setPrefHeight(1);
        sepBottom.setMaxHeight(1);
        sepBottom.setStyle("-fx-background-color: #FFFFFF; -fx-opacity: 1;");

        sidebar.getChildren().addAll(
            header,
            sepTop,
            dashboardBtn,
            accountsBtn,
            stockLogsBtn,
            reportsBtn,
            systemSettingsBtn,
            helpBtn,
            spacer,
            sepBottom,
            logoutBtn
        );
    }

    private void showHelp() {
        titleLabel.setText("Help & Documentation");
        contentArea.getChildren().clear();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 10;");

        VBox mainBox = new VBox(12);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-border-color: transparent;");
        mainBox.setId("help-content");

        javafx.scene.control.Accordion accordion = new javafx.scene.control.Accordion();

        // Welcome
        VBox welcomeContent = new VBox(8);
        welcomeContent.setPadding(new Insets(12));
        Label welcomeDesc = new Label("As an administrator, you have full control over the inventory system, user accounts, reservations, and system operations. This guide will help you master all features.");
        welcomeDesc.setWrapText(true);
        welcomeDesc.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#bcd6ee" : "#555555") + ";");
        welcomeContent.getChildren().add(welcomeDesc);
        javafx.scene.control.TitledPane welcomePane = createAccordionPane("👋 Welcome to Admin Dashboard", welcomeContent);

        // Navigation Basics
        VBox navContent = new VBox(6);
        navContent.setPadding(new Insets(12));
        navContent.getChildren().addAll(
            createBulletPoint("Use the sidebar to switch between Accounts, Stock Logs, System Settings, and Help"),
            createBulletPoint("Each section provides specialized tools for managing different aspects of the system"),
            createBulletPoint("Click any tab to expand or access detailed functionality"),
            createBulletPoint("The dashboard displays real-time statistics and important metrics")
        );
        javafx.scene.control.TitledPane navPane = createAccordionPane("🧭 Navigation Basics", navContent);

        // Reservation Management
        VBox reservationContent = new VBox(6);
        reservationContent.setPadding(new Insets(12));
        reservationContent.getChildren().addAll(
            createBulletPoint("View all pending student reservations from the dashboard"),
            createBulletPoint("Review student details, requested items, sizes, and quantities before approval"),
            createSubBullet("✓ Approve: Verify payment and identity before marking as ready for pickup"),
            createSubBullet("✗ Reject: Decline with optional reason; items return to inventory"),
            createBulletPoint("Update reservation status: PENDING → APPROVED → COMPLETED"),
            createBulletPoint("Track when students collect their items and mark as completed")
        );
        javafx.scene.control.TitledPane reservationPane = createAccordionPane("📋 Reservation Management", reservationContent);

        // Inventory Management
        VBox inventoryContent = new VBox(6);
        inventoryContent.setPadding(new Insets(12));
        inventoryContent.getChildren().addAll(
            createBulletPoint("Add new items with: Code (1000-9999), Name, Course, Available Sizes, Price"),
            createBulletPoint("Update quantities: Increase stock when new shipments arrive"),
            createBulletPoint("Modify prices: Adjust individual item prices or run bulk updates"),
            createBulletPoint("Remove items: Delete discontinued products from inventory"),
            createBulletPoint("Supported sizes: XS, S, M, L, XL, XXL, One Size"),
            createBulletPoint("Valid price range: ₱0 - ₱10,000 per item")
        );
        javafx.scene.control.TitledPane inventoryPane = createAccordionPane("📦 Inventory Management", inventoryContent);

        // Account Management
        VBox accountsContent = new VBox(6);
        accountsContent.setPadding(new Insets(12));
        accountsContent.getChildren().addAll(
            createBulletPoint("Create new staff accounts with username, email, and initial password"),
            createBulletPoint("Activate or deactivate user accounts as needed"),
            createBulletPoint("Reset passwords for staff members who forgot their credentials"),
            createBulletPoint("View all active and inactive users in the system"),
            createBulletPoint("Monitor user activity and login history")
        );
        javafx.scene.control.TitledPane accountsPane = createAccordionPane("👥 Account Management", accountsContent);

        // Stock Logs & Auditing
        VBox auditContent = new VBox(6);
        auditContent.setPadding(new Insets(12));
        auditContent.getChildren().addAll(
            createBulletPoint("View complete history of all inventory changes"),
            createBulletPoint("See who made changes, what changed, and when it happened"),
            createBulletPoint("Filter logs by staff member, item, or date range for quick lookup"),
            createBulletPoint("Export logs for compliance and audit purposes"),
            createBulletPoint("Identify suspicious activity or unauthorized modifications")
        );
        javafx.scene.control.TitledPane auditPane = createAccordionPane("📊 Stock Logs & Auditing", auditContent);

        // System Settings
        VBox settingsContent = new VBox(6);
        settingsContent.setPadding(new Insets(12));
        settingsContent.getChildren().addAll(
            createBulletPoint("Configure business hours and operating parameters"),
            createBulletPoint("Set default values for reservations and pickups"),
            createBulletPoint("Manage email notifications and alerts"),
            createBulletPoint("Backup and restore system data"),
            createBulletPoint("View system logs and error reports")
        );
        javafx.scene.control.TitledPane settingsPane = createAccordionPane("⚙️ System Settings", settingsContent);

        // Tips & Best Practices
        VBox tipsContent = new VBox(6);
        tipsContent.setPadding(new Insets(12));
        tipsContent.getChildren().addAll(
            createTipBullet("Regularly check pending reservations - don't leave students waiting"),
            createTipBullet("Update inventory levels after each restock to prevent overselling"),
            createTipBullet("Use stock logs monthly to audit and reconcile inventory"),
            createTipBullet("Set reasonable reservation approval windows for better planning"),
            createTipBullet("Document any manual inventory adjustments with clear reasons"),
            createTipBullet("Backup system data regularly to prevent data loss")
        );
        javafx.scene.control.TitledPane tipsPane = createAccordionPane("💡 Tips & Best Practices", tipsContent);

        // Troubleshooting
        VBox troubleshootContent = new VBox(6);
        troubleshootContent.setPadding(new Insets(12));
        troubleshootContent.getChildren().addAll(
            createFAQItem("Q: Student says reservation is lost?", "A: Check Stock Logs to see if it was manually cancelled. Reapprove if necessary."),
            createFAQItem("Q: Item code already exists?", "A: Each item needs a unique code (1000-9999). Choose a different code."),
            createFAQItem("Q: Can't approve a reservation?", "A: Verify sufficient stock, valid sizes selected, and student payment."),
            createFAQItem("Q: Price update not working?", "A: Ensure price is between ₱0-₱10,000. Try again after confirmation.")
        );
        javafx.scene.control.TitledPane troubleshootPane = createAccordionPane("🔧 Troubleshooting", troubleshootContent);

        // Shortcuts
        VBox shortcutsContent = new VBox(6);
        shortcutsContent.setPadding(new Insets(12));
        shortcutsContent.getChildren().addAll(
            createBulletPoint("Tab - Navigate between fields"),
            createBulletPoint("Enter - Confirm actions"),
            createBulletPoint("Esc - Cancel current operation or close dialog"),
            createBulletPoint("Ctrl+S - Save changes (in applicable sections)")
        );
        javafx.scene.control.TitledPane shortcutsPane = createAccordionPane("⌨️ Keyboard Shortcuts", shortcutsContent);

        // Support
        VBox supportContent = new VBox(6);
        supportContent.setPadding(new Insets(12));
        supportContent.getChildren().addAll(
            createBulletPoint("Email: admin-support@sti.edu.ph"),
            createBulletPoint("Phone: 1-800-STI-HELP"),
            createBulletPoint("Support Hours: Monday-Friday, 9 AM - 5 PM"),
            createBulletPoint("Emergency: Contact IT department directly")
        );
        javafx.scene.control.TitledPane supportPane = createAccordionPane("📞 Contact & Support", supportContent);

        accordion.getPanes().addAll(
            welcomePane, navPane, reservationPane, inventoryPane,
            accountsPane, auditPane, settingsPane, tipsPane,
            troubleshootPane, shortcutsPane, supportPane
        );

        // Restore previously-expanded pane if available, otherwise default to welcome
        boolean restored = false;
        if (helpExpandedTitle != null) {
            for (javafx.scene.control.TitledPane p : accordion.getPanes()) {
                if (helpExpandedTitle.equals(p.getText())) {
                    accordion.setExpandedPane(p);
                    restored = true;
                    break;
                }
            }
        }
        if (!restored) {
            accordion.setExpandedPane(welcomePane);
        }

        mainBox.getChildren().addAll(accordion);
        scrollPane.setContent(mainBox);
        contentArea.getChildren().add(scrollPane);
    }
    
    /**
     * Create navigation button
     */
    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(40);
        
        if (active) {
            String activeBg = ThemeManager.isDarkMode() ? "-color-accent-subtle" : "rgba(255,255,255,0.2)";
            String activeText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
            btn.setStyle(
                "-fx-background-color: " + activeBg + ";" +
                "-fx-text-fill: " + activeText + ";" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            String inactiveText = ThemeManager.isDarkMode() ? "-color-fg-default" : "rgba(255,255,255,0.9)";
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + inactiveText + ";" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;"
            );
        }
        
        return btn;
    }
    
    /**
     * Set active navigation button
     */
    private void setActiveButton(Button activeBtn) {
        activeButton = activeBtn;
        Button[] buttons = {dashboardBtn, accountsBtn, stockLogsBtn, reportsBtn, systemSettingsBtn, helpBtn};
        
        String activeBg = ThemeManager.isDarkMode() ? "-color-accent-subtle" : "rgba(255,255,255,0.2)";
        String activeText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        String inactiveText = ThemeManager.isDarkMode() ? "-color-fg-default" : "rgba(255,255,255,0.9)";
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setStyle(
                    "-fx-background-color: " + activeBg + ";" +
                    "-fx-text-fill: " + activeText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + inactiveText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
    }
    
    /**
     * Show dashboard overview
     */
    private void showDashboard() {
        titleLabel.setText("Dashboard");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createDashboardView());
        
        // Wire up quick actions every time dashboard is shown (buttons are recreated)
        wireQuickActions();
    }
    
    /**
     * Show accounts management
     */
    private void showAccounts() {
        titleLabel.setText("Account Management");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createAccountsView());
    }
    
    
    /**
     * Show system settings
     */
    private void showSystemSettings() {
        titleLabel.setText("System Settings");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createSystemSettingsView());
    }
    
    /**
     * Show stock logs
     */
    private void showStockLogs() {
        titleLabel.setText("Stock Logs");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createStockLogsView());
    }
    
    /**
     * Show reports and analytics
     */
    private void showReports() {
        titleLabel.setText("Reports & Analytics");
        contentArea.getChildren().clear();
        try {
            ReportView reportView = new ReportView(
                controller.getInventoryManager(),
                controller.getReservationManager(),
                controller.getReceiptManager()
            );
            contentArea.getChildren().add(reportView.getView());
        } catch (Exception e) {
            Label errorLabel = new Label("Failed to load reports: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red;");
            contentArea.getChildren().add(errorLabel);
        }
    }
    
    /**
     * Update sidebar theme colors when theme changes
     */
    private void updateSidebarTheme() {
        // Update main background
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update content area background
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + "; -fx-padding: 20;");
        
        // Update top bar
        HBox topBar = (HBox) view.getTop();
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topBar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        // Update title color
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Update top bar buttons and labels
        for (javafx.scene.Node node : topBar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String themeBtnColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + themeBtnColor + ";" +
                    "-fx-font-size: 18px;" +
                    "-fx-cursor: hand;"
                );
            } else if (node instanceof Label && !node.equals(titleLabel)) {
                Label lbl = (Label) node;
                String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
                lbl.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
            }
        }
        
        // Update sidebar background
        String sidebarGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to bottom, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to bottom, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        sidebar.setStyle(
            "-fx-background-color: " + sidebarGradient + ";"
        );
        
        // Update logo and subtitle colors
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        // Update navigation buttons
        Button[] buttons = {dashboardBtn, accountsBtn, stockLogsBtn, systemSettingsBtn, helpBtn};
        String activeBg = ThemeManager.isDarkMode() ? "-color-accent-subtle" : "rgba(255,255,255,0.2)";
        String activeText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        String inactiveText = ThemeManager.isDarkMode() ? "-color-fg-default" : "rgba(255,255,255,0.9)";
        
        for (Button btn : buttons) {
            String currentStyle = btn.getStyle();
            boolean isActive = currentStyle.contains("-fx-font-weight: bold");
            
            if (isActive) {
                btn.setStyle(
                    "-fx-background-color: " + activeBg + ";" +
                    "-fx-text-fill: " + activeText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + inactiveText + ";" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
        
        // Update logout button: red background in dark mode, transparent in light mode
        if (ThemeManager.isDarkMode()) {
            // Use explicit danger color to avoid variable resolution issues
            logoutBtn.setStyle(
                "-fx-background-color: #CF222E;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-alignment: center;" +
                "-fx-padding: 12;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-weight: bold;"
            );
        } else {
            logoutBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-text-fill: rgba(255,255,255,0.9);" +
                "-fx-font-size: 14px;" +
                "-fx-alignment: center;" +
                "-fx-padding: 12px;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
        }
        
        // Refresh Help content if currently displayed, preserving expanded pane
        if (contentArea.lookup("#help-content") != null) {
            helpExpandedTitle = getCurrentHelpExpandedTitle();
            showHelp();
        }

        // Reapply active button styling for current theme
        if (activeButton != null) {
            setActiveButton(activeButton);
        }
    }

    // Helper: find currently-expanded titled pane's title inside the help accordion
    private String getCurrentHelpExpandedTitle() {
        for (javafx.scene.Node node : contentArea.getChildren()) {
            if (node instanceof ScrollPane) {
                ScrollPane sp = (ScrollPane) node;
                javafx.scene.Node content = sp.getContent();
                if (content instanceof VBox) {
                    for (javafx.scene.Node child : ((VBox) content).getChildren()) {
                        if (child instanceof javafx.scene.control.Accordion) {
                            javafx.scene.control.Accordion acc = (javafx.scene.control.Accordion) child;
                            javafx.scene.control.TitledPane expanded = acc.getExpandedPane();
                            if (expanded != null) return expanded.getText();
                        }
                    }
                } else if (content instanceof javafx.scene.control.Accordion) {
                    javafx.scene.control.Accordion acc = (javafx.scene.control.Accordion) content;
                    javafx.scene.control.TitledPane expanded = acc.getExpandedPane();
                    if (expanded != null) return expanded.getText();
                }
            } else if (node instanceof javafx.scene.control.Accordion) {
                javafx.scene.control.Accordion acc = (javafx.scene.control.Accordion) node;
                javafx.scene.control.TitledPane expanded = acc.getExpandedPane();
                if (expanded != null) return expanded.getText();
            }
        }
        return null;
    }
    
    /**
     * Toggle theme with smooth animation
     */
    private void toggleTheme() {
        ThemeManager.toggleLightDark();
        
        // Update toggle switch appearance with smooth animation
        toggleIcon.setText(ThemeManager.isDarkMode() ? "🌙" : "☀");
        
        // Animate circle position smoothly
        double targetX = ThemeManager.isDarkMode() ? 44 : 2; // Right: 44, Left: 2
        Timeline slideAnimation = new Timeline(
            new KeyFrame(Duration.millis(400), 
                new KeyValue(toggleCircle.translateXProperty(), targetX - toggleCircle.getLayoutX())
            )
        );
        slideAnimation.setCycleCount(1);
        slideAnimation.play();
        
        // Update circle color
        String circleColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
        toggleCircle.setStyle(
            "-fx-background-color: " + circleColor + ";" +
            "-fx-background-radius: 17px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        // Update background color
        Region toggleBg = (Region) toggleSwitch.getChildren().get(0);
        String toggleBgColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(to right, #1e3a5f 0%, #0f2a4a 50%, #1e3a5f 100%)"
            : "linear-gradient(to right, #fff9e6 0%, #ffefb3 50%, #fff9e6 100%)";
        toggleBg.setStyle(
            "-fx-background-color: " + toggleBgColor + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "rgba(77, 163, 255, 0.3)" : "rgba(245, 197, 66, 0.3)") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 8, 0, 0, 2);"
        );
        
        // Update the full UI theme
        updateSidebarTheme();
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
    
    // ===== HELP SECTION HELPER METHODS =====
    
    @SuppressWarnings("unused")
    private VBox createHelpCard(String title, String subtitle, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: " + (ThemeManager.isDarkMode() ? "#1f2a3a" : "#f5f5f5") + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "#24364f" : "#e0e0e0") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#9fc5ff" : "#1e3c72") + ";");
        card.getChildren().add(titleLabel);
        
        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
            subtitleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#8fb6d6" : "#4a5f8f") + ";");
            card.getChildren().add(subtitleLabel);
        }
        
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#bcd6ee" : "#555555") + ";");
            card.getChildren().add(descLabel);
        }
        
        return card;
    }
    
    private VBox createBulletPoint(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("• " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#d0d0e0" : "#333333") + "; -fx-font-size: 14px; -fx-line-spacing: 2px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createSubBullet(String text) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(2, 0, 2, 40));
        Label bullet = new Label("  ◦ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#444444") + "; -fx-font-size: 12px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createTipBullet(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("✓ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#90ee90" : "#2d6a2d") + "; -fx-font-size: 14px; -fx-font-weight: 600;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private HBox createFAQItem(String question, String answer) {
        HBox faqBox = new HBox(12);
        faqBox.setPadding(new Insets(8, 0, 8, 0));
        
        VBox qnaBox = new VBox(4);
        Label qLabel = new Label(question);
        qLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        qLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0d0ff" : "#1e3c72") + "; -fx-font-size: 14px;");
        qLabel.setWrapText(true);
        
        Label aLabel = new Label(answer);
        aLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 13px;");
        aLabel.setWrapText(true);
        
        qnaBox.getChildren().addAll(qLabel, aLabel);
        faqBox.getChildren().add(qnaBox);
        HBox.setHgrow(qnaBox, Priority.ALWAYS);
        
        return faqBox;
    }

    /**
     * Create a styled TitledPane for accordions
     */
    private javafx.scene.control.TitledPane createAccordionPane(String title, VBox content) {
        javafx.scene.control.TitledPane pane = new javafx.scene.control.TitledPane(title, content);
        pane.setAnimated(false);
        pane.setCollapsible(true);

        String textColor = ThemeManager.isDarkMode() ? "#9fc5ff" : "#1e3c72";
        pane.setStyle(
            "-fx-font-weight: bold; " +
            "-fx-font-size: 15px; " +
            "-fx-text-fill: " + textColor + ";"
        );

        content.setStyle("-fx-background-color: transparent;");
        return pane;
    }
}

