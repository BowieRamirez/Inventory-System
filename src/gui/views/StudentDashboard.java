package gui.views;

import gui.controllers.StudentDashboardController;
import gui.utils.ThemeManager;
import student.Student;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
    private Button myReservationsBtn;
    private Button profileBtn;
    private Button logoutBtn;
    
    public StudentDashboard(Student student) {
        this.student = student;
        controller = new StudentDashboardController(student);
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setStyle("-fx-background-color: -color-bg-default;");
        
        // Create sidebar
        createSidebar();
        view.setLeft(sidebar);
        
        // Create top bar
        view.setTop(createTopBar());
        
        // Create content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: -color-bg-default;");
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
        topBar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        titleLabel = new Label("Shop");
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
        
        Label studentLabel = new Label("👤 " + student.getFullName());
        studentLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, themeBtn, studentLabel);
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
        
        Label subtitleLabel = new Label("Student Portal");
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        // Student info card
        VBox infoCard = new VBox(5);
        infoCard.setPadding(new Insets(15));
        infoCard.setStyle(
            "-fx-background-color: -color-accent-subtle;" +
            "-fx-background-radius: 8px;"
        );
        
        Label nameLabel = new Label(student.getFullName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label idLabel = new Label("ID: " + student.getStudentId());
        idLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        Label courseLabel = new Label("Course: " + student.getCourse());
        courseLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        infoCard.getChildren().addAll(nameLabel, idLabel, courseLabel);
        
        // Navigation buttons
        shopBtn = createNavButton("🛍️ Shop", true);
        myReservationsBtn = createNavButton("📋 My Reservations", false);
        profileBtn = createNavButton("👤 Profile", false);
        
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
        shopBtn.setOnAction(e -> {
            setActiveButton(shopBtn);
            showShop();
        });
        
        myReservationsBtn.setOnAction(e -> {
            setActiveButton(myReservationsBtn);
            showMyReservations();
        });
        
        profileBtn.setOnAction(e -> {
            setActiveButton(profileBtn);
            showProfile();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            infoCard,
            new Separator(),
            shopBtn,
            myReservationsBtn,
            profileBtn,
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
        Button[] buttons = {shopBtn, myReservationsBtn, profileBtn};
        
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
     * Show shop view
     */
    private void showShop() {
        titleLabel.setText("Shop");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createShopView());
    }
    
    /**
     * Show my reservations
     */
    private void showMyReservations() {
        titleLabel.setText("My Reservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createMyReservationsView());
    }
    
    /**
     * Show profile
     */
    private void showProfile() {
        titleLabel.setText("Profile");
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

