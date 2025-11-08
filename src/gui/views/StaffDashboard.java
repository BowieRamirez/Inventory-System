package gui.views;

import gui.controllers.StaffDashboardController;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    private Button completedBtn;
    private Button returnedBtn;
    private Button cancelledBtn;
    private Button inventoryBtn;
    private Button stockLogsBtn;
    private Button logoutBtn;
    
    // Sidebar labels for theme updates
    private Label logoLabel;
    private Label subtitleLabel;
    
    public StaffDashboard() {
        controller = new StaffDashboardController();
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        createSidebar();
        view.setLeft(sidebar);
        view.setTop(createTopBar());
        
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        showReservations();
    }
    
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
        
        titleLabel = new Label("Reservations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "white";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
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
            updateSidebarTheme();
        });
        
        Label staffLabel = new Label("👤 Staff");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        staffLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, themeBtn, staffLabel);
        return topBar;
    }
    
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
        
        logoLabel = new Label("STI ProWear");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        String logoColor = ThemeManager.isDarkMode() ? "-color-accent-fg" : "white";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
        
        subtitleLabel = new Label("Staff Panel");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        reservationsBtn = createNavButton("📋 Reservations", true);
        completedBtn = createNavButton("✅ Completed", false);
        returnedBtn = createNavButton("↩️ Returned", false);
        cancelledBtn = createNavButton("❌ Cancelled", false);
        inventoryBtn = createNavButton("📦 Inventory", false);
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
        
        reservationsBtn.setOnAction(e -> {
            setActiveButton(reservationsBtn);
            showReservations();
        });
        
        completedBtn.setOnAction(e -> {
            setActiveButton(completedBtn);
            showCompleted();
        });
        
        returnedBtn.setOnAction(e -> {
            setActiveButton(returnedBtn);
            showReturned();
        });
        
        cancelledBtn.setOnAction(e -> {
            setActiveButton(cancelledBtn);
            showCancelled();
        });
        
        inventoryBtn.setOnAction(e -> {
            setActiveButton(inventoryBtn);
            showInventory();
        });
        
        stockLogsBtn.setOnAction(e -> {
            setActiveButton(stockLogsBtn);
            showStockLogs();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            reservationsBtn,
            completedBtn,
            returnedBtn,
            cancelledBtn,
            inventoryBtn,
            stockLogsBtn,
            spacer,
            new Separator(),
            logoutBtn
        );
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
        Button[] buttons = {reservationsBtn, completedBtn, returnedBtn, cancelledBtn, inventoryBtn, stockLogsBtn};
        
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
    
    private void showCompleted() {
        titleLabel.setText("Completed Orders");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createCompletedView());
    }
    
    private void showReturned() {
        titleLabel.setText("Returned Orders");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReturnedView());
    }
    
    private void showCancelled() {
        titleLabel.setText("Cancelled Orders");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createCancelledView());
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
        String topBarBg = isDark ? "-color-bg-subtle" : "#0969DA";
        String borderColor = isDark ? "-color-border-default" : "#0550AE";
        topBar.setStyle(
            "-fx-background-color: " + topBarBg + ";" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-width: 0 0 2 0;"
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
        String sidebarColor = isDark ? "-color-bg-subtle" : "#0969DA";
        sidebar.setStyle("-fx-background-color: " + sidebarColor + "; -fx-padding: 20;");
        
        // Update logo and subtitle
        String logoColor = isDark ? "-color-accent-fg" : "white";
        String subtitleColor = isDark ? "-color-fg-muted" : "rgba(255, 255, 255, 0.8)";
        logoLabel.setStyle("-fx-text-fill: " + logoColor + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        // Update navigation buttons
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String currentStyle = btn.getStyle();
                
                // Check if button is currently active (bold text)
                boolean isActive = currentStyle.contains("-fx-font-weight: bold");
                
                if (isDark) {
                    // Dark mode styling
                    if (isActive) {
                        btn.setStyle("-fx-background-color: -color-accent-emphasis; -fx-text-fill: white; " +
                                   "-fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 6; " +
                                   "-fx-cursor: hand; -fx-alignment: center-left; -fx-font-size: 14px;");
                    } else {
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: -color-fg-muted; " +
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
        
        // Update logout button
        if (isDark) {
            logoutBtn.setStyle("-fx-background-color: -color-danger-emphasis; -fx-text-fill: white; " +
                             "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                             "-fx-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            logoutBtn.setStyle("-fx-background-color: rgba(220, 53, 69, 0.9); -fx-text-fill: white; " +
                             "-fx-padding: 12; -fx-background-radius: 6; -fx-cursor: hand; " +
                             "-fx-alignment: center; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }
    
    public BorderPane getView() {
        return view;
    }
}

