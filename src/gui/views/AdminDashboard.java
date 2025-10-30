package gui.views;

import gui.controllers.AdminDashboardController;
import gui.utils.SceneManager;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
    private Button inventoryBtn;
    private Button reservationsBtn;
    private Button accountsBtn;
    private Button stockLogsBtn;
    private Button logoutBtn;
    
    public AdminDashboard() {
        controller = new AdminDashboardController();
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setStyle("-fx-background-color: -color-bg-default;");
        
        // Create sidebar
        createSidebar();
        view.setLeft(sidebar);
        
        // Create top bar
        createTopBar();
        view.setTop(createTopBar());
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: -color-bg-default;");
        view.setCenter(contentArea);
        
        // Show dashboard by default
        showDashboard();
    }
    
    /**
     * Create top navigation bar
     */
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        titleLabel = new Label("Dashboard");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle button
        Button themeBtn = new Button(ThemeManager.isDarkMode() ? "☀" : "🌙");
        themeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: -color-fg-default;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;"
        );
        themeBtn.setOnAction(e -> {
            ThemeManager.toggleLightDark();
            themeBtn.setText(ThemeManager.isDarkMode() ? "☀" : "🌙");
        });
        
        Label adminLabel = new Label("👤 Admin");
        adminLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
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
        sidebar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        // Logo/Title
        Label logoLabel = new Label("STI ProWear");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        logoLabel.setStyle("-fx-text-fill: -color-accent-fg;");
        
        Label subtitleLabel = new Label("Admin Panel");
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        dashboardBtn = createNavButton("📊 Dashboard", true);
        inventoryBtn = createNavButton("📦 Inventory", false);
        reservationsBtn = createNavButton("📋 Reservations", false);
        accountsBtn = createNavButton("👥 Accounts", false);
        stockLogsBtn = createNavButton("📝 Stock Logs", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #CF222E;" +
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
        
        inventoryBtn.setOnAction(e -> {
            setActiveButton(inventoryBtn);
            showInventory();
        });
        
        reservationsBtn.setOnAction(e -> {
            setActiveButton(reservationsBtn);
            showReservations();
        });
        
        accountsBtn.setOnAction(e -> {
            setActiveButton(accountsBtn);
            showAccounts();
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
            inventoryBtn,
            reservationsBtn,
            accountsBtn,
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
            btn.setStyle(
                "-fx-background-color: -color-accent-subtle;" +
                "-fx-text-fill: -color-accent-fg;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: -color-fg-default;" +
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
        Button[] buttons = {dashboardBtn, inventoryBtn, reservationsBtn, accountsBtn, stockLogsBtn};
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setStyle(
                    "-fx-background-color: -color-accent-subtle;" +
                    "-fx-text-fill: -color-accent-fg;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: -color-fg-default;" +
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
    }
    
    /**
     * Show inventory management
     */
    private void showInventory() {
        titleLabel.setText("Inventory Management");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createInventoryView());
    }
    
    /**
     * Show reservations management
     */
    private void showReservations() {
        titleLabel.setText("Reservations Management");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReservationsView());
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
     * Show stock logs
     */
    private void showStockLogs() {
        titleLabel.setText("Stock Logs");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createStockLogsView());
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

