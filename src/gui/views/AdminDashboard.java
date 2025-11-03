package gui.views;

import gui.controllers.AdminDashboardController;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    private Button stockApprovalsBtn;
    private Button logoutBtn;
    
    // Sidebar labels for theme updates
    private Label logoLabel;
    private Label subtitleLabel;
    
    public AdminDashboard() {
        controller = new AdminDashboardController();
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Create sidebar
        createSidebar();
        view.setLeft(sidebar);
        
        // Create top bar
        createTopBar();
        view.setTop(createTopBar());
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
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
        Button approvePendingBtn = controller.getApprovePendingBtn();
        Button manageAccountsBtn = controller.getManageAccountsBtn();
        
        // Wire up actions if buttons exist
        if (approvePendingBtn != null) {
            approvePendingBtn.setOnAction(e -> {
                setActiveButton(stockApprovalsBtn);
                showStockApprovals();
            });
        }
        
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
        String topBarBg = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "#0969DA";
        String borderColor = ThemeManager.isDarkMode() ? "-color-border-default" : "#0550AE";
        topBar.setStyle(
            "-fx-background-color: " + topBarBg + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 0 0 2 0;"
        );
        
        titleLabel = new Label("Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle button
        Button themeBtn = new Button(ThemeManager.isDarkMode() ? "☀" : "🌙");
        String themeBtnColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        themeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + themeBtnColor + ";" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;"
        );
        themeBtn.setOnAction(e -> {
            ThemeManager.toggleLightDark();
            themeBtn.setText(ThemeManager.isDarkMode() ? "☀" : "🌙");
            updateSidebarTheme(); // Update sidebar colors when theme changes
        });
        
        Label adminLabel = new Label("👤 Admin");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        adminLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, themeBtn, adminLabel);
        return topBar;
    }
    
    /**
     * Create sidebar navigation
     */
    private void createSidebar() {
        sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20));
        
        // Blue sidebar in light mode, subtle background in dark mode
        String sidebarColor = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "#0969DA";
        sidebar.setStyle(
            "-fx-background-color: " + sidebarColor + ";" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        // Logo/Title
        logoLabel = new Label("STI ProWear");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        subtitleLabel = new Label("Admin Panel");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        dashboardBtn = createNavButton("📊 Dashboard", true);
        accountsBtn = createNavButton("👥 Accounts", false);
        stockApprovalsBtn = createNavButton("✅ Stock Approvals", false);
        stockLogsBtn = createNavButton("📝 Stock Logs", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        String logoutColor = ThemeManager.isDarkMode() ? "#CF222E" : "rgba(255,255,255,0.9)";
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + logoutColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 12px;" +
            "-fx-cursor: hand;"
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
        
        stockApprovalsBtn.setOnAction(e -> {
            setActiveButton(stockApprovalsBtn);
            showStockApprovals();
        });
        
        stockLogsBtn.setOnAction(e -> {
            setActiveButton(stockLogsBtn);
            showStockLogs();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            dashboardBtn,
            accountsBtn,
            stockApprovalsBtn,
            stockLogsBtn,
            spacer,
            new Separator(),
            logoutBtn
        );
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
        Button[] buttons = {dashboardBtn, accountsBtn, stockApprovalsBtn, stockLogsBtn};
        
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
     * Show stock approvals (pending stock adjustments)
     */
    private void showStockApprovals() {
        titleLabel.setText("Stock Approvals");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createStockApprovalsView());
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
        String topBarBg = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "#0969DA";
        String borderColor = ThemeManager.isDarkMode() ? "-color-border-default" : "#0550AE";
        topBar.setStyle(
            "-fx-background-color: " + topBarBg + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 0 0 2 0;"
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
        String sidebarColor = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "#0969DA";
        sidebar.setStyle(
            "-fx-background-color: " + sidebarColor + ";" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        // Update logo and subtitle colors
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        // Update navigation buttons
        Button[] buttons = {dashboardBtn, accountsBtn, stockApprovalsBtn, stockLogsBtn};
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
        
        // Update logout button
        String logoutColor = ThemeManager.isDarkMode() ? "#CF222E" : "rgba(255,255,255,0.9)";
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + logoutColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 12px;" +
            "-fx-cursor: hand;"
        );
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

