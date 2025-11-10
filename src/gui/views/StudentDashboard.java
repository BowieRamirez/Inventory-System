package gui.views;

import gui.controllers.StudentDashboardController;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import student.Student;

/**
 * StudentDashboard - Main dashboard for student users
 * 
 * Features:
 * - Browse available items
 * - Create reservations
 * - View my reservations
 * - Track order status
 * - Request returns
 */
public class StudentDashboard {
    
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private Label titleLabel;
    private StudentDashboardController controller;
    private Student student;
    
    // Sidebar buttons
    private Button shopBtn;
    private Button cartBtn;
    private Button claimItemsBtn;
    private Button myReservationsBtn;
    
    // Sidebar labels for theme updates
    private Label logoLabel;
    private Label subtitleLabel;
    
    // Track current view
    private Runnable currentViewRefresher;
    
    public StudentDashboard(Student student) {
        this.student = student;
        controller = new StudentDashboardController(student);
        controller.setRefreshCallback(() -> refreshCurrentView());
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Create sidebar
        createSidebar();
        view.setLeft(sidebar);
        
        // Create top bar
        view.setTop(createTopBar());
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        // Show shop by default
        showShop();
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
        
        titleLabel = new Label("Shop");
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
        
        // Profile dropdown button
        Button profileBtn = new Button("👤 " + student.getFullName() + " ▼");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.9)";
        profileBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + labelColor + ";" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8px 12px;"
        );
        
        // Add hover effect
        profileBtn.setOnMouseEntered(e -> {
            profileBtn.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-padding: 8px 12px;" +
                "-fx-background-radius: 4px;"
            );
        });
        
        profileBtn.setOnMouseExited(e -> {
            profileBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + labelColor + ";" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-padding: 8px 12px;"
            );
        });
        
        // Create context menu for profile dropdown
        ContextMenu profileMenu = new ContextMenu();
        
        // Account Details menu item
        MenuItem accountDetailsItem = new MenuItem("👤 Account Details");
        accountDetailsItem.setStyle("-fx-font-size: 13px;");
        accountDetailsItem.setOnAction(e -> showAccountDetails());
        
        // Change Password menu item
        MenuItem changePasswordItem = new MenuItem("🔑 Change Password");
        changePasswordItem.setStyle("-fx-font-size: 13px;");
        changePasswordItem.setOnAction(e -> showChangePassword());
        
        // Separator
        javafx.scene.control.SeparatorMenuItem separator = new javafx.scene.control.SeparatorMenuItem();
        
        // Logout menu item
        MenuItem logoutItem = new MenuItem("🚪 Logout");
        logoutItem.setStyle("-fx-font-size: 13px; -fx-text-fill: #CF222E;");
        logoutItem.setOnAction(e -> controller.handleLogout());
        
        profileMenu.getItems().addAll(accountDetailsItem, changePasswordItem, separator, logoutItem);
        
        // Show menu when profile button is clicked
        profileBtn.setOnAction(e -> {
            profileMenu.show(profileBtn, Side.BOTTOM, 0, 5);
        });
        
        topBar.getChildren().addAll(titleLabel, spacer, themeBtn, profileBtn);
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
        
        subtitleLabel = new Label("Student Portal");
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "rgba(255,255,255,0.8)";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        shopBtn = createNavButton("🛍️ Shop", true);
        cartBtn = createNavButton("🛒 Cart (0)", false);
        claimItemsBtn = createNavButton("📦 Claim Items", false);
        myReservationsBtn = createNavButton("📋 My Reservations", false);
        
        // Button actions
        shopBtn.setOnAction(e -> {
            setActiveButton(shopBtn);
            showShop();
        });
        
        cartBtn.setOnAction(e -> {
            setActiveButton(cartBtn);
            showCart();
        });
        
        claimItemsBtn.setOnAction(e -> {
            setActiveButton(claimItemsBtn);
            showClaimItems();
        });
        
        myReservationsBtn.setOnAction(e -> {
            setActiveButton(myReservationsBtn);
            showMyReservations();
        });
        
        // Set up cart update callback
        controller.setCartUpdateCallback(this::updateCartBadge);
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            shopBtn,
            cartBtn,
            claimItemsBtn,
            myReservationsBtn
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
        Button[] buttons = {shopBtn, cartBtn, claimItemsBtn, myReservationsBtn};
        
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
     * Show shop view
     */
    private void showShop() {
        titleLabel.setText("Shop");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createShopView());
        currentViewRefresher = this::showShop;
    }
    
    /**
     * Show cart view
     */
    private void showCart() {
        titleLabel.setText("Shopping Cart");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createCartView());
        currentViewRefresher = this::showCart;
    }
    
    /**
     * Update cart badge with current item count
     */
    private void updateCartBadge() {
        int cartSize = controller.getCartSize();
        cartBtn.setText("🛒 Cart (" + cartSize + ")");
    }
    
    /**
     * Refresh the current view
     */
    private void refreshCurrentView() {
        if (currentViewRefresher != null) {
            currentViewRefresher.run();
        }
    }
    
    /**
     * Show my reservations view
     */
    private void showMyReservations() {
        titleLabel.setText("My Reservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createMyReservationsView());
        currentViewRefresher = this::showMyReservations;
    }
    
    /**
     * Show claim items view
     */
    private void showClaimItems() {
        titleLabel.setText("Claim Items");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createClaimItemsView());
        currentViewRefresher = this::showClaimItems;
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
        Button[] buttons = {shopBtn, cartBtn, claimItemsBtn, myReservationsBtn};
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
    }
    
    /**
     * Show account details dialog
     */
    private void showAccountDetails() {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Account Details");
        dialog.setHeaderText("Your Account Information");
        
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-min-width: 400px;");
        
        // Account info
        VBox infoBox = new VBox(10);
        infoBox.setPadding(new Insets(15));
        infoBox.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;"
        );
        
        Label nameLabel = new Label("Full Name: " + student.getFullName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-default;");
        
        Label idLabel = new Label("Student ID: " + student.getStudentId());
        idLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-default;");
        
        Label courseLabel = new Label("Course: " + student.getCourse());
        courseLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-default;");
        
        Label genderLabel = new Label("Gender: " + student.getGender());
        genderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-default;");
        
        Label statusLabel = new Label("Status: " + student.getAccountStatus());
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-default;");
        
        infoBox.getChildren().addAll(nameLabel, idLabel, courseLabel, genderLabel, statusLabel);
        
        content.getChildren().addAll(infoBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Show change password dialog
     */
    private void showChangePassword() {
        titleLabel.setText("Settings");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createProfileView());
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

