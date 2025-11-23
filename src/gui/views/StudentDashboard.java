package gui.views;

import java.io.File;

import gui.controllers.StudentDashboardController;
import gui.utils.ThemeManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
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
    private StackPane contentArea;
    private StudentDashboardController controller;
    private Student student;
    
    // Navigation buttons
    private Button homeBtn;
    private Button shopBtn;
    private Button cartBtn;
    private Button requestPickupBtn;
    private Button claimItemsBtn;
    private Button myReservationsBtn;
    
    // Top bar elements
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    private Button accountBtn;
    private Button notificationBtn;
    
    // Track current view
    private Runnable currentViewRefresher;
    // Track active tab name so recreating the top bar preserves selection
    private String activeTabName;
    
    public StudentDashboard(Student student) {
        this.student = student;
        controller = new StudentDashboardController(student);
        controller.setNotificationUpdateCallback(() -> updateNotificationBadge());
        controller.setRefreshCallback(() -> refreshCurrentView());
        controller.setNavigateToShopCallback(() -> {
            setActiveTab(shopBtn);
            showShop();
        });
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Create top bar
        view.setTop(createTopBar());

        // Initialize notification badge
        updateNotificationBadge();
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20, 20, 20, 20));
        contentArea.setMaxWidth(Double.MAX_VALUE);
        contentArea.setMaxHeight(Double.MAX_VALUE);
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + ";");
        view.setCenter(contentArea);
        
        // Show home by default
        showHome();
    }
    
    /**
     * Create top navigation bar with logo and tabs
     */
    private VBox createTopBar() {
        VBox topContainer = new VBox(0);
        String bgGradient = ThemeManager.isDarkMode()
            ? "linear-gradient(to right, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        topContainer.setStyle("-fx-background-color: " + bgGradient + ";");
        
        // Single unified row: Logo, Navigation (centered), and Account controls
        HBox headerRow = new HBox(20);
        headerRow.setPadding(new Insets(15, 20, 15, 20));
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setStyle("-fx-background-color: transparent;");
        
        // Logo container with name
        HBox logoBox = new HBox(10);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPrefWidth(280);
        logoBox.setStyle("-fx-background-color: transparent;");
        
        // Try to load logo image
        try {
            File logoFile = new File("src/database/data/images/NewLogo.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                ImageView logoView = new ImageView(logoImage);
                logoView.setFitHeight(45);
                logoView.setFitWidth(80);
                logoView.setPreserveRatio(true);
                logoBox.getChildren().add(logoView);
            } else {
                // Fallback to text logo
                Label logoText = new Label("STI ProWear");
                logoText.setFont(Font.font("System", FontWeight.BOLD, 18));
                logoText.setStyle("-fx-text-fill: #1a1a1a;");
                logoBox.getChildren().add(logoText);
            }
        } catch (Exception e) {
            // Fallback to text logo
            Label logoText = new Label("STI ProWear");
            logoText.setFont(Font.font("System", FontWeight.BOLD, 18));
            logoText.setStyle("-fx-text-fill: #1a1a1a;");
            logoBox.getChildren().add(logoText);
        }
        
        // Add company name and student name
        VBox namesBox = new VBox(2);
        namesBox.setAlignment(Pos.CENTER_LEFT);
        
        Label companyNameLabel = new Label("STI ProWare Novaliches");
        String textColor = "#ffffff";
        companyNameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        companyNameLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        Label studentNameLabel = new Label(student.getFirstName() + " " + student.getLastName());
        studentNameLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        studentNameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        namesBox.getChildren().addAll(companyNameLabel, studentNameLabel);
        logoBox.getChildren().add(namesBox);
        
        // Create navigation buttons/tabs
        homeBtn = createNavTab("Home", false);
        shopBtn = createNavTab("Shop", false);
        cartBtn = createNavTab("Cart", false);
        requestPickupBtn = createNavTab("Request Pickup", false);
        claimItemsBtn = createNavTab("Claim Items", false);
        myReservationsBtn = createNavTab("My Reservations", false);
        
        // Preserve previously active tab if set, otherwise default to Home
        if (activeTabName == null) {
            activeTabName = "Home";
        }
        
        // Button actions
        homeBtn.setOnAction(e -> {
            setActiveTab(homeBtn);
            showHome();
        });
        
        shopBtn.setOnAction(e -> {
            setActiveTab(shopBtn);
            showShop();
        });
        
        cartBtn.setOnAction(e -> {
            setActiveTab(cartBtn);
            showCart();
        });
        
        requestPickupBtn.setOnAction(e -> {
            setActiveTab(requestPickupBtn);
            showRequestPickup();
        });
        
        claimItemsBtn.setOnAction(e -> {
            setActiveTab(claimItemsBtn);
            showClaimItems();
        });
        
        myReservationsBtn.setOnAction(e -> {
            setActiveTab(myReservationsBtn);
            showMyReservations();
        });
        
        // Set up cart update callback
        controller.setCartUpdateCallback(this::updateCartBadge);
        
        // Navigation container (centered)
        HBox navContainer = new HBox(30);
        navContainer.setAlignment(Pos.CENTER);
        navContainer.getChildren().addAll(homeBtn, shopBtn, cartBtn, requestPickupBtn, claimItemsBtn, myReservationsBtn);
        HBox.setHgrow(navContainer, Priority.ALWAYS);

        // Apply the active tab based on last selection
        switch (activeTabName) {
            case "Shop": setActiveTab(shopBtn); break;
            case "Cart": setActiveTab(cartBtn); break;
            case "Request Pickup": setActiveTab(requestPickupBtn); break;
            case "Claim Items": setActiveTab(claimItemsBtn); break;
            case "My Reservations": setActiveTab(myReservationsBtn); break;
            default: setActiveTab(homeBtn); break;
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Theme toggle switch - reuse LoginView implementation
        // Toggle switch button (pill shape with sliding circle)
        toggleSwitch = new StackPane();
        toggleSwitch.setPrefWidth(70);
        toggleSwitch.setPrefHeight(32);
        toggleSwitch.setMaxWidth(70);
        toggleSwitch.setMaxHeight(32);
        
        // Background pill
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
        
        // Sliding circle with icon
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
        
        // Icon inside circle
        toggleIcon = new Label(ThemeManager.isDarkMode() ? "🌙" : "☀");
        toggleIcon.setFont(Font.font("System", FontWeight.BOLD, 12));
        toggleIcon.setStyle("-fx-text-fill: #000000;");
        toggleCircle.getChildren().add(toggleIcon);
        
        // Position circle based on theme using translateX (for smooth animation)
        StackPane.setAlignment(toggleCircle, Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        double initialX = ThemeManager.isDarkMode() ? 42 : 2; // 70 - 26 - 2 = 42
        toggleCircle.setTranslateX(initialX);
        
        toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
        toggleSwitch.setOnMouseClicked(e -> toggleTheme());
        toggleSwitch.setStyle("-fx-cursor: hand;");
        
        // My account dropdown button
        accountBtn = new Button("👤 My account ▼");
        accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        accountBtn.setOnMouseEntered(e -> accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.3);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        ));
        accountBtn.setOnMouseExited(e -> accountBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 12;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        ));
        
        // Create context menu for account dropdown
        ContextMenu accountMenu = new ContextMenu();
        
        // Account Info menu item
        MenuItem accountInfoItem = new MenuItem("Account Info");
        accountInfoItem.setStyle("-fx-font-size: 13px;");
        accountInfoItem.setOnAction(e -> showAccountDetails());

        // Change Password menu item
        MenuItem changePasswordItem = new MenuItem("Change Password");
        changePasswordItem.setStyle("-fx-font-size: 13px;");
        changePasswordItem.setOnAction(e -> controller.openChangePasswordDialog());

        // Logout menu item
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setStyle("-fx-font-size: 13px; -fx-text-fill: #CF222E;");
        logoutItem.setOnAction(e -> controller.handleLogout());
        
        accountMenu.getItems().addAll(accountInfoItem, changePasswordItem, logoutItem);
        
        // Show menu when account button is clicked
        accountBtn.setOnAction(e -> {
            accountMenu.show(accountBtn, Side.BOTTOM, 0, 5);
        });
        
        // Notification button (bell) - clickable to open notifications dialog
        notificationBtn = new Button("🔔");
        notificationBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.12);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-padding: 8 10;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;"
        );
        notificationBtn.setOnAction(e -> {
            controller.showNotificationsDialog();
            // update badge after user views
            updateNotificationBadge();
        });

        headerRow.getChildren().addAll(logoBox, navContainer, toggleSwitch, notificationBtn, accountBtn);
        
        // Add thicker separator line under header for stronger visual weight
        Separator separator = new Separator();
        separator.setPrefHeight(2);
        separator.setMinHeight(2);
        separator.setMaxHeight(2);
        separator.setStyle(
            "-fx-background-color: rgba(0,0,0,0.35);" +
            "-fx-padding: 0;"
        );
        
        topContainer.getChildren().addAll(headerRow, separator);
        topContainer.setPadding(new Insets(0));
        return topContainer;
    }
    
    /**
     * Create navigation tab button
     */
    private Button createNavTab(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPadding(new Insets(12, 16, 12, 16));
        btn.setAlignment(Pos.CENTER);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: rgba(255,255,255,0.9);" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: 600;" +
            "-fx-border-width: 0;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: System;"
        );
        
        // Hover effect
        // Remove hover-based font weight changes to avoid sticky bold effect.
        // Nav buttons remain consistently styled; only active state is bolded.
        btn.setOnMouseEntered(e -> { /* no-op to keep style stable on hover */ });
        btn.setOnMouseExited(e -> { /* no-op to keep style stable on hover */ });
        
        return btn;
    }
    
    /**
     * Set active navigation tab
     */
    private void setActiveTab(Button activeBtn) {
        // remember active tab name so recreating top bar keeps selection
        if (activeBtn != null) {
            activeTabName = activeBtn.getText();
        }
        Button[] buttons = {homeBtn, shopBtn, cartBtn, requestPickupBtn, claimItemsBtn, myReservationsBtn};
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                // Active button - white, solid
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: 700;" +
                    "-fx-border-width: 0;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: System;"
                );
            } else {
                // Inactive button - semi-transparent white
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: rgba(255,255,255,0.9);" +
                    "-fx-font-size: 16px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-border-width: 0;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-family: System;"
                );
            }
        }
    }
    
    /**
     * Show home view
     */
    private void showHome() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createHomeView());
        currentViewRefresher = this::showHome;
    }
    
    /**
     * Show shop view
     */
    private void showShop() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createShopView());
        currentViewRefresher = this::showShop;
    }
    
    /**
     * Show cart view
     */
    private void showCart() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createCartView());
        currentViewRefresher = this::showCart;
    }
    
    /**
     * Update cart badge with current item count
     */
    private void updateCartBadge() {
        int cartSize = controller.getCartSize();
        cartBtn.setText("Cart (" + cartSize + ")");
    }

    /**
     * Update notification badge with unread count
     */
    private void updateNotificationBadge() {
        if (notificationBtn == null) return;
        int count = controller.getUnreadNotificationCount();
        if (count > 0) {
            notificationBtn.setText("🔔 (" + count + ")");
        } else {
            notificationBtn.setText("🔔");
        }
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
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createMyReservationsView());
        currentViewRefresher = this::showMyReservations;
    }
    
    /**
     * Show request pickup view
     */
    private void showRequestPickup() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createRequestPickupView());
        currentViewRefresher = this::showRequestPickup;
    }
    
    /**
     * Show claim items view
     */
    private void showClaimItems() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createClaimItemsView());
        currentViewRefresher = this::showClaimItems;
    }
    
    /**
     * Toggle between light and dark theme
     */
    private void toggleTheme() {
        // Prevent double-clicks during animation
        toggleSwitch.setDisable(true);

        boolean willBeDark = !ThemeManager.isDarkMode();
        double toX = willBeDark ? 42 : 2; // slide right for dark, left for light

        Timeline slideAnimation = new Timeline(
            new KeyFrame(
                Duration.millis(220),
                new KeyValue(toggleCircle.translateXProperty(), toX, Interpolator.EASE_BOTH)
            )
        );

        slideAnimation.setOnFinished(evt -> {
            // Apply theme after the slider finishes for non-instant feel
            ThemeManager.toggleLightDark();

            // Update icon
            toggleIcon.setText(ThemeManager.isDarkMode() ? "🌙" : "☀");

            // Update circle style based on final theme
            String circleColor = ThemeManager.isDarkMode()
                ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
                : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
            toggleCircle.setStyle(
                "-fx-background-color: " + circleColor + ";" +
                "-fx-background-radius: 13px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
            );

            // Update background style
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

            // Refresh the themed UI last
            updateTheme();

            toggleSwitch.setDisable(false);
        });

        slideAnimation.play();
    }
    
    /**
     * Update theme colors when theme changes
     */
    private void updateTheme() {
        // Update main background
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update content area background
        String contentBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "#F8F9FA";
        contentArea.setStyle("-fx-background-color: " + contentBg + "; -fx-padding: 20;");
        
        // Recreate top bar to update all colors
        view.setTop(createTopBar());
        // Re-apply notification badge after recreating top bar
        updateNotificationBadge();
        
        // Re-apply active tab
        if (currentViewRefresher != null) {
            currentViewRefresher.run();
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
        nameLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label idLabel = new Label("Student ID: " + student.getStudentId());
        idLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label courseLabel = new Label("Course: " + student.getCourse());
        courseLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label genderLabel = new Label("Gender: " + student.getGender());
        genderLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        Label statusLabel = new Label("Status: " + student.getAccountStatus());
        statusLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: -color-fg-default;");
        
        infoBox.getChildren().addAll(nameLabel, idLabel, courseLabel, genderLabel, statusLabel);
        
        content.getChildren().addAll(infoBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

