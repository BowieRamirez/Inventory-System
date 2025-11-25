package gui.views;

import gui.controllers.StaffDashboardController;
import gui.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
 * StaffDashboard - Main dashboard for staff users
 * 
 * Features:
 * - Approve reservations
 * - Manage inventory
 * - View stock logs
 */
public class StaffDashboard {
    
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private Label titleLabel;
    private StaffDashboardController controller;
    
    private Button reservationsBtn;
    private Button inventoryBtn;
    private Button stockLogsBtn;
    private Button reportsBtn;
    private Button helpBtn;
    private Button logoutBtn;
    private Button dashboardBtn;
    
    // Sidebar labels for theme updates
    private Label logoLabel;
    private Label subtitleLabel;
    
    // Theme toggle
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    
    // Track currently active button for theme refresh
    private Button activeButton;
    
    public StaffDashboard() {
        controller = new StaffDashboardController();
        initializeView();
        // Listen for theme changes and reapply the sidebar styles to avoid stale styles
        javafx.application.Platform.runLater(() -> ThemeManager.addThemeChangeListener(() -> javafx.application.Platform.runLater(this::updateSidebarTheme)));
        // Reload dashboard data when the window gains focus (useful if files were edited externally)
        view.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((winObs, oldWin, newWin) -> {
                    if (newWin != null) {
                        newWin.focusedProperty().addListener((fObs, wasFocused, isNowFocused) -> {
                            if (isNowFocused) {
                                // Reload inventory from disk and refresh the dashboard
                                try {
                                    controller.reloadInventory();
                                    // Rebuild the dashboard view to pick up new data
                                    javafx.application.Platform.runLater(this::showDashboard);
                                } catch (Exception ignored) {}
                            }
                        });
                    }
                });
            }
        });
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        createSidebar();
        view.setLeft(sidebar);
        view.setTop(createTopBar());
        
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        setActiveButton(dashboardBtn);
        showDashboard();
    }
    
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
        
        titleLabel = new Label("Reservations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle switch (reuse LoginView implementation)
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
            "-fx-background-radius: 17px;" +
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
        
        Label staffLabel = new Label("👤 Staff");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        staffLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, toggleSwitch, staffLabel);
        return topBar;
    }
    
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
        
        logoLabel = new Label("STI ProWear Novaliches");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        subtitleLabel = new Label("Staff Panel");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(10, logoImage, logoLabel, subtitleLabel);
        header.setAlignment(Pos.TOP_CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        dashboardBtn = createNavButton("📊 Dashboard", true);
        reservationsBtn = createNavButton("📋 Reservations", false);
        inventoryBtn = createNavButton("📦 Inventory", false);
        stockLogsBtn = createNavButton("📝 Stock Logs", false);
        reportsBtn = createNavButton("📈 Reports", false);
        helpBtn = createNavButton("❓ Help", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        // In dark mode the logout button should show a danger background with white text
        // (previously used a red text color which caused it to appear pure red on some setups)
        String logoutColor = ThemeManager.isDarkMode() ? "white" : "rgba(255,255,255,0.9)";
        // Use explicit danger color to avoid relying on CSS variable resolution
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
        
        dashboardBtn.setOnAction(e -> {
            setActiveButton(dashboardBtn);
            showDashboard();
        });
        
        reservationsBtn.setOnAction(e -> {
            setActiveButton(reservationsBtn);
            showReservations();
        });
        
        inventoryBtn.setOnAction(e -> {
            setActiveButton(inventoryBtn);
            showInventory();
        });
        
        stockLogsBtn.setOnAction(e -> {
            setActiveButton(stockLogsBtn);
            showStockLogs();
        });
        
        reportsBtn.setOnAction(e -> {
            setActiveButton(reportsBtn);
            showReports();
        });

        helpBtn.setOnAction(e -> {
            setActiveButton(helpBtn);
            showHelp();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            dashboardBtn,
            reservationsBtn,
            inventoryBtn,
            stockLogsBtn,
            reportsBtn,
            helpBtn,
            spacer,
            new Separator(),
            logoutBtn
        );
    }

    private void showHelp() {
        titleLabel.setText("Help & Documentation");
        contentArea.getChildren().clear();
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-control-inner-background: transparent; -fx-padding: 10;");
        
        VBox mainBox = new VBox(20);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-border-color: transparent;");
        mainBox.setId("help-content");
        
        // ====== WELCOME SECTION ======
        VBox welcomeSection = createHelpCard(
            "👋 Welcome to Staff Dashboard",
            "STI ProWear System - Merchandise Support Team",
            "As staff, you manage reservations, inventory, and customer service. This guide covers all your responsibilities and how to use the system effectively."
        );
        
        // ====== NAVIGATION BASICS ======
        VBox navSection = createHelpCard(
            "🧭 Navigation Basics",
            "",
            ""
        );
        navSection.getChildren().add(createBulletPoint("Dashboard: View pending requests and system overview"));
        navSection.getChildren().add(createBulletPoint("Reservations: Handle student merchandise requests"));
        navSection.getChildren().add(createBulletPoint("Inventory: Update stock and manage items"));
        navSection.getChildren().add(createBulletPoint("Stock Logs: Track all inventory changes for auditing"));
        
        // ====== RESERVATION MANAGEMENT ======
        VBox reservationSection = createHelpCard(
            "📋 Reservation Management",
            "Handling Student Requests",
            ""
        );
        reservationSection.getChildren().add(createBulletPoint("View all pending student reservations from the dashboard"));
        reservationSection.getChildren().add(createBulletPoint("Review request details: Student name, course, items, sizes, quantity"));
        reservationSection.getChildren().add(createSubBullet("✓ Approve: After verifying payment or identity"));
        reservationSection.getChildren().add(createSubBullet("✗ Reject: With reason if items unavailable or request invalid"));
        reservationSection.getChildren().add(createBulletPoint("Track status: PENDING → APPROVED → COMPLETED"));
        reservationSection.getChildren().add(createBulletPoint("Handle returns: Process customer return requests professionally"));
        
        // ====== INVENTORY MANAGEMENT ======
        VBox inventorySection = createHelpCard(
            "📦 Inventory Management",
            "Stock Control & Organization",
            ""
        );
        inventorySection.getChildren().add(createBulletPoint("View all items: See current stock levels by course/category"));
        inventorySection.getChildren().add(createBulletPoint("Add items: Create new merchandise entries with all details"));
        inventorySection.getChildren().add(createBulletPoint("Update quantities: Adjust stock when items arrive or are sold"));
        inventorySection.getChildren().add(createBulletPoint("Modify prices: Change item prices with admin confirmation"));
        inventorySection.getChildren().add(createBulletPoint("Remove items: Delete discontinued or damaged items"));
        inventorySection.getChildren().add(createBulletPoint("Available sizes: XS, S, M, L, XL, XXL, One Size"));
        
        // ====== CUSTOMER SERVICE ======
        VBox csSection = createHelpCard(
            "🤝 Customer Service Best Practices",
            "Creating Great Experiences",
            ""
        );
        csSection.getChildren().add(createBulletPoint("Always greet customers professionally and helpfully"));
        csSection.getChildren().add(createBulletPoint("Verify student ID before processing transactions"));
        csSection.getChildren().add(createBulletPoint("Explain reservation status clearly to students"));
        csSection.getChildren().add(createBulletPoint("Handle complaints with patience and empathy"));
        csSection.getChildren().add(createBulletPoint("Escalate complex issues to admin/management"));
        csSection.getChildren().add(createBulletPoint("Keep merchandise display organized and clean"));
        
        // ====== STOCK LOGS & AUDITING ======
        VBox auditSection = createHelpCard(
            "📊 Stock Logs & Auditing",
            "Track Changes & Accountability",
            ""
        );
        auditSection.getChildren().add(createBulletPoint("View complete history of all inventory changes"));
        auditSection.getChildren().add(createBulletPoint("See who made changes, what changed, and when"));
        auditSection.getChildren().add(createBulletPoint("Filter logs by staff member, date, or item code"));
        auditSection.getChildren().add(createBulletPoint("Export logs for reports and audits"));
        auditSection.getChildren().add(createBulletPoint("Identify discrepancies or suspicious activities"));
        
        // ====== REPLACEMENT REQUESTS ======
        VBox replacementSection = createHelpCard(
            "🔄 Handling Replacement Requests",
            "Processing Returns and Swaps",
            ""
        );
        replacementSection.getChildren().add(createBulletPoint("Review replacement request: Reason, original item, replacement item"));
        replacementSection.getChildren().add(createBulletPoint("Verify original item condition if applicable"));
        replacementSection.getChildren().add(createBulletPoint("Check replacement stock availability"));
        replacementSection.getChildren().add(createSubBullet("✓ Approve: Swap items and update inventory"));
        replacementSection.getChildren().add(createSubBullet("✗ Reject: With clear reason if unable to fulfill"));
        replacementSection.getChildren().add(createBulletPoint("Update reservation status to reflect change"));
        
        // ====== TIPS & BEST PRACTICES ======
        VBox tipsSection = createHelpCard(
            "💡 Tips & Best Practices",
            "Work Smarter, Serve Better",
            ""
        );
        tipsSection.getChildren().add(createTipBullet("Process reservations quickly - students are waiting"));
        tipsSection.getChildren().add(createTipBullet("Verify payment before approving high-value orders"));
        tipsSection.getChildren().add(createTipBullet("Keep accurate notes when rejecting requests"));
        tipsSection.getChildren().add(createTipBullet("Alert management when stock levels are low"));
        tipsSection.getChildren().add(createTipBullet("Double-check item codes to avoid wrong items"));
        tipsSection.getChildren().add(createTipBullet("Maintain professional conduct with all customers"));
        
        // ====== TROUBLESHOOTING ======
        VBox troubleshootSection = createHelpCard(
            "🔧 Troubleshooting",
            "Common Issues & Solutions",
            ""
        );
        troubleshootSection.getChildren().add(createFAQItem("Q: Can't approve a reservation?", "A: Check if payment is verified and stock is available. Ensure all details are correct."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Item showing wrong quantity?", "A: Refresh the inventory view or check Stock Logs to see if quantity was recently changed."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Student requesting return?", "A: Check the return/replacement request feature. Ensure original receipt is available."));
        troubleshootSection.getChildren().add(createFAQItem("Q: Size not available?", "A: Contact admin to restock or mark size as temporarily unavailable."));
        
        // ====== KEYBOARD SHORTCUTS ======
        VBox shortcutsSection = createHelpCard(
            "⌨️ Keyboard Shortcuts",
            "Speed Up Your Work",
            ""
        );
        shortcutsSection.getChildren().add(createBulletPoint("Tab - Navigate between fields"));
        shortcutsSection.getChildren().add(createBulletPoint("Enter - Confirm actions"));
        shortcutsSection.getChildren().add(createBulletPoint("Esc - Cancel current operation"));
        shortcutsSection.getChildren().add(createBulletPoint("Ctrl+F - Search inventory"));
        
        // ====== POLICIES & PROCEDURES ======
        VBox policiesSection = createHelpCard(
            "📜 Policies & Procedures",
            "Important Guidelines",
            ""
        );
        policiesSection.getChildren().add(createBulletPoint("Only approve payment after physical verification"));
        policiesSection.getChildren().add(createBulletPoint("Never process requests without valid student ID"));
        policiesSection.getChildren().add(createBulletPoint("Always maintain accurate records in the system"));
        policiesSection.getChildren().add(createBulletPoint("Report system errors or suspicious activity immediately"));
        policiesSection.getChildren().add(createBulletPoint("Keep shift operations log updated and detailed"));
        
        mainBox.getChildren().addAll(
            welcomeSection, navSection, reservationSection, inventorySection, 
            csSection, auditSection, replacementSection, tipsSection, 
            troubleshootSection, shortcutsSection, policiesSection
        );
        
        scrollPane.setContent(mainBox);
        contentArea.getChildren().add(scrollPane);
    }
    
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
    
    private void setActiveButton(Button activeBtn) {
        activeButton = activeBtn;
        Button[] buttons = {dashboardBtn, reservationsBtn, inventoryBtn, stockLogsBtn, reportsBtn, helpBtn};
        
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
    
    private void showDashboard() {
        titleLabel.setText("Overview");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createStaffDashboardView());
    }

    private void showReservations() {
        titleLabel.setText("Reservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReservationsView());
    }
    
    private void showInventory() {
        titleLabel.setText("Inventory");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createInventoryView());
    }
    
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
     * Toggle between light and dark theme
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
        
        updateSidebarTheme();
    }
    
    private void updateSidebarTheme() {
        boolean isDark = ThemeManager.isDarkMode();
        
        // Update main background
        String bgColor = isDark ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update content area background
        String contentBg = isDark ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + "; -fx-padding: 20;");
        
        // Update top bar
        HBox topBar = (HBox) view.getTop();
        String bgGradient = isDark
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topBar.setStyle(
            "-fx-background-color: " + bgGradient + ";"
        );
        
        // Update title color
        String titleColor = isDark ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        
        // Update top bar buttons and labels
        for (javafx.scene.Node node : topBar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String themeBtnColor = isDark ? "-color-fg-default" : "white";
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: " + themeBtnColor + ";" +
                    "-fx-font-size: 18px;" +
                    "-fx-cursor: hand;"
                );
            } else if (node instanceof Label && !node.equals(titleLabel)) {
                Label lbl = (Label) node;
                String labelColor = isDark ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
                lbl.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
            }
        }
        
        // Update sidebar background
        String sidebarGradient = isDark
            ? "linear-gradient(to bottom, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to bottom, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        sidebar.setStyle("-fx-background-color: " + sidebarGradient + "; -fx-padding: 20;");
        
        // Update logo and subtitle
        String logoColor = isDark ? "-color-accent-fg" : "white";
        String subtitleColor = isDark ? "-color-fg-muted" : "rgba(255, 255, 255, 0.8)";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        // Update navigation buttons
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String currentStyle = btn.getStyle();
                
                // Check if button is currently active (bold text)
                boolean isActive = currentStyle.contains("-fx-font-weight: bold");
                
                if (isDark) {
                    // Dark mode styling - use darker subtle color for active state
                    if (isActive) {
                        btn.setStyle("-fx-background-color: -color-accent-subtle; -fx-text-fill: -color-accent-fg; " +
                                   "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; " +
                                   "-fx-cursor: hand; -fx-alignment: center-left; -fx-font-size: 14px;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: -color-fg-default; " +
                                   "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                                   "-fx-alignment: center-left; -fx-font-size: 14px;");
                    }
                } else {
                    // Light mode styling (blue sidebar)
                    if (isActive) {
                        btn.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); -fx-text-fill: white; " +
                                   "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; " +
                                   "-fx-cursor: hand; -fx-alignment: center-left; -fx-font-size: 14px;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255, 255, 255, 0.9); " +
                                   "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                                   "-fx-alignment: center-left; -fx-font-size: 14px;");
                    }
                }
            }
        }
        
        // Update logout button: keep it as a nav-style text button in light mode
        if (isDark) {
            // Use explicit danger color for dark mode
            logoutBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; " +
                             "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                             "-fx-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            // In light mode, show logout as a transparent nav item with white text
            logoutBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15);" +
                "-fx-text-fill: rgba(255,255,255,0.9);" +
                "-fx-font-size: 14px;" +
                "-fx-alignment: center;" +
                "-fx-padding: 12px;" +
                "-fx-cursor: hand;"
            );
        }
        
        // Refresh Help content if currently displayed
        if (contentArea.lookup("#help-content") != null) {
            showHelp();
        }
        
        // Reapply active button styling for current theme
        if (activeButton != null) {
            setActiveButton(activeButton);
        }
    }
    
    public BorderPane getView() {
        return view;
    }
    
    // ===== HELP SECTION HELPER METHODS =====
    
    private VBox createHelpCard(String title, String subtitle, String description) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle(
            "-fx-background-color: " + (ThemeManager.isDarkMode() ? "#2a2a3e" : "#f5f5f5") + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "#404054" : "#e0e0e0") + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 1);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#e0e0ff" : "#1e3c72") + ";");
        card.getChildren().add(titleLabel);
        
        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 12));
            subtitleLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#a0a0c0" : "#4a5f8f") + ";");
            card.getChildren().add(subtitleLabel);
        }
        
        if (description != null && !description.isEmpty()) {
            Label descLabel = new Label(description);
            descLabel.setWrapText(true);
            descLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + ";");
            card.getChildren().add(descLabel);
        }
        
        return card;
    }
    
    private VBox createBulletPoint(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("• " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#d0d0e0" : "#333333") + "; -fx-font-size: 11px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createSubBullet(String text) {
        VBox box = new VBox(2);
        box.setPadding(new Insets(2, 0, 2, 40));
        Label bullet = new Label("  ◦ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#444444") + "; -fx-font-size: 10px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private VBox createTipBullet(String text) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(4, 0, 4, 20));
        Label bullet = new Label("✓ " + text);
        bullet.setWrapText(true);
        bullet.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#90ee90" : "#2d6a2d") + "; -fx-font-size: 11px;");
        box.getChildren().add(bullet);
        return box;
    }
    
    private HBox createFAQItem(String question, String answer) {
        HBox faqBox = new HBox(12);
        faqBox.setPadding(new Insets(8, 0, 8, 0));
        
        VBox qnaBox = new VBox(4);
        Label qLabel = new Label(question);
        qLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 11));
        qLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0d0ff" : "#1e3c72") + ";");
        qLabel.setWrapText(true);
        
        Label aLabel = new Label(answer);
        aLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#c0c0d0" : "#555555") + "; -fx-font-size: 10px;");
        aLabel.setWrapText(true);
        
        qnaBox.getChildren().addAll(qLabel, aLabel);
        faqBox.getChildren().add(qnaBox);
        HBox.setHgrow(qnaBox, Priority.ALWAYS);
        
        return faqBox;
    }
}

