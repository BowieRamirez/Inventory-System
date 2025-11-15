package gui.views;

import java.io.File;

import gui.controllers.SignupController;
import gui.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import utils.SystemConfigManager;

/**
 * SignupView - Student registration screen
 * Modern split-screen design matching LoginView
 */
public class SignupView {
    
    private VBox view;
    private VBox rightContainer;
    private TextField studentIdField;
    private TextField firstNameField;
    private TextField lastNameField;
    private ComboBox<String> courseComboBox;
    private ComboBox<String> genderComboBox;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button signupButton;
    private Button backButton;
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    private SignupController controller;
    
    public SignupView() {
        controller = new SignupController();
        initializeView();
    }
    
    private void initializeView() {
    // Root container (centered background gradient)
    view = new VBox(0);
    view.setAlignment(Pos.CENTER);
    view.setPadding(new Insets(30));
    view.setMaxWidth(Double.MAX_VALUE);
    view.setMaxHeight(Double.MAX_VALUE);
        
        // Blue gradient background - JavaFX style (matches login)
        String bgGradient = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        view.setStyle("-fx-background-color: " + bgGradient + ";");
        
        // Main horizontal split (two containers)
    HBox split = new HBox();
        split.setAlignment(Pos.CENTER);
        split.setSpacing(0);
        split.setMaxWidth(1400);
        split.setMaxHeight(850);
    // Unified shadow and rounded outer corners
    split.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 40, 0, 0, 10);");
    Rectangle outerClip = new Rectangle();
    outerClip.setArcWidth(20);
    outerClip.setArcHeight(20);
    outerClip.widthProperty().bind(split.widthProperty());
    outerClip.heightProperty().bind(split.heightProperty());
    split.setClip(outerClip);

        // LEFT: shirt image inside its own container (with blue gradient background)
    StackPane leftPane = new StackPane();
        leftPane.setPrefWidth(700);
        leftPane.setMinHeight(720);
        leftPane.setMaxHeight(850);
        HBox.setHgrow(leftPane, Priority.SOMETIMES);
    String leftBg = "linear-gradient(from 0% 0% to 100% 100%, #163764 0%, #1f4c86 100%)";
    leftPane.setStyle("-fx-background-color: " + leftBg + ";");
        try {
            File sideImageFile = new File("src/database/data/images/NewSides2.png");
            if (sideImageFile.exists()) {
                Image sideImage = new Image(sideImageFile.toURI().toString(), true);
                ImageView shirt = new ImageView(sideImage);
                shirt.setPreserveRatio(false);
                shirt.fitWidthProperty().bind(leftPane.widthProperty());
                shirt.fitHeightProperty().bind(leftPane.heightProperty());
                leftPane.getChildren().add(shirt);
                StackPane.setAlignment(shirt, Pos.CENTER);
            }
        } catch (Exception e) {
            System.err.println("Error loading side image: " + e.getMessage());
        }

        // RIGHT: form card container (white background, rounded corners, soft shadow)
        rightContainer = new VBox();
        rightContainer.setAlignment(Pos.TOP_CENTER);
        rightContainer.setSpacing(20);
        rightContainer.setPadding(new Insets(24, 24, 36, 24));
        rightContainer.setPrefWidth(700);
        rightContainer.setMinHeight(720);
        rightContainer.setMaxHeight(850);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);
        String cardBg = ThemeManager.isDarkMode() ? "rgba(30, 40, 70, 0.9)" : "#ffffff";
        rightContainer.setStyle(
            "-fx-background-color: " + cardBg + ";"
        );
        
        // Create signup form with scrollable content
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    // Always show scrollbar to prevent layout shift
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    scrollPane.setMaxWidth(Double.MAX_VALUE);
    scrollPane.setMinWidth(350);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
    VBox signupCard = new VBox(12);
    signupCard.setAlignment(Pos.CENTER);
    signupCard.setPadding(new Insets(10, 45, 10, 45));
    signupCard.setMaxWidth(Double.MAX_VALUE);
    signupCard.setMinWidth(350);
    signupCard.setPrefWidth(500);
        
        // Maintenance Mode Banner (if enabled)
        SystemConfigManager configManager = SystemConfigManager.getInstance();
        if (configManager.isMaintenanceModeActive()) {
            HBox maintenanceBanner = new HBox(10);
            maintenanceBanner.setAlignment(Pos.CENTER);
            maintenanceBanner.setPadding(new Insets(8));
            maintenanceBanner.setStyle(
                "-fx-background-color: rgba(255, 193, 7, 0.2); " +
                "-fx-border-color: #ffc107; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px;"
            );
            
            Label warningIcon = new Label("⚠");
            warningIcon.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffc107;");
            
            Label maintenanceLabel = new Label("Signup currently in maintenance mode");
            maintenanceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffc107;");
            
            maintenanceBanner.getChildren().addAll(warningIcon, maintenanceLabel);
            signupCard.getChildren().add(maintenanceBanner);
        }
        
        // Logo Section - Load STI ProWear Logo with rounded corners (MATCHES LOGIN)
        StackPane logoContainer = new StackPane();
        try {
            File logoFile = new File("src/database/data/images/newLOGO.png");
            if (logoFile.exists()) {
                Image logoImage = new Image(logoFile.toURI().toString());
                ImageView logoImageView = new ImageView(logoImage);
                logoImageView.setFitWidth(160);
                logoImageView.setFitHeight(160);
                logoImageView.setPreserveRatio(true);
                
                // Apply rounded corners using clip
                Rectangle clip = new Rectangle(160, 160);
                clip.setArcWidth(20);
                clip.setArcHeight(20);
                logoImageView.setClip(clip);
                
                logoContainer.getChildren().add(logoImageView);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            // Fallback text logo
            Label logoLabel = new Label("STI ProWear");
            logoLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            String logoColor = ThemeManager.isDarkMode() ? "#ffffff" : "#2a5298";
            logoLabel.setStyle("-fx-text-fill: " + logoColor + ";");
            logoContainer.getChildren().add(logoLabel);
        }
        
        // Create Account Title (MATCHES LOGIN STYLE)
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        String titleColor = ThemeManager.isDarkMode() ? "#ffffff" : "#1e3c72";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        // Shared styling variables (MATCHES LOGIN)
        String labelColor = ThemeManager.isDarkMode() ? "#e0e0e0" : "#555555";
        String fieldBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.9)";
        String fieldText = ThemeManager.isDarkMode() ? "#ffffff" : "#333333";
        String fieldBorder = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.4)" : "rgba(46, 92, 152, 0.3)";
        String promptTextColor = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.5)" : "#999999";
        
        // Student ID field
        VBox studentIdBox = new VBox(5);
        Label studentIdLabel = new Label("Student ID");
        studentIdLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        studentIdField = new TextField();
        studentIdField.setPromptText("Student ID");
        studentIdField.setPrefHeight(45);
        studentIdField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        studentIdBox.getChildren().addAll(studentIdLabel, studentIdField);
        
        // First Name field
        VBox firstNameBox = new VBox(5);
        Label firstNameLabel = new Label("First Name");
        firstNameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.setPrefHeight(45);
        firstNameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        firstNameBox.getChildren().addAll(firstNameLabel, firstNameField);
        
        // Last Name field
        VBox lastNameBox = new VBox(5);
        Label lastNameLabel = new Label("Last Name");
        lastNameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.setPrefHeight(45);
        lastNameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        lastNameBox.getChildren().addAll(lastNameLabel, lastNameField);
        
        // Course ComboBox - simple dropdown (no search)
        VBox courseBox = new VBox(5);
        Label courseLabel = new Label("Course");
        courseLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        
        // Create observable list
        ObservableList<String> courseItems = FXCollections.observableArrayList(
            // Senior High School (SHS)
            "Accountancy, Business, and Management",
            "Science, Technology, Engineering, and Mathematics",
            "Humanities and Social Sciences",
            "IT in Mobile App and Web Development",
            "Tourism Operations",
            "Culinary Arts",
            // Tertiary Courses
            "Bachelor of Science in Computer Science (BSCS)",
            "Bachelor of Science in Information Technology (BSIT)",
            "Bachelor of Science in Computer Engineering (BSCpE)",
            "Bachelor of Science in Business Administration (BSBA)",
            "Bachelor of Science in Accountancy (BSA)",
            "Bachelor of Science in Hospitality Management (BSHM)",
            "Bachelor of Multimedia Arts (BMMA)",
            "Bachelor of Science in Tourism Management (BSTM)"
        );
        
        // Non-editable ComboBox (dropdown only)
        courseComboBox = new ComboBox<>(courseItems);
        courseComboBox.setPromptText("Select course...");
        courseComboBox.setEditable(false);
        courseComboBox.setMaxWidth(Double.MAX_VALUE);
        courseComboBox.setPrefHeight(45);
        
        // Enhanced styling for course ComboBox
        String comboBoxStyle = 
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 0px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
            "-fx-popup-hide-animation-duration: 0ms;" +
            "-fx-control-inner-background: " + fieldBg + ";" +
            "-fx-cell-padding: 4;" +
            "-fx-focus-color: " + (ThemeManager.isDarkMode() ? "rgba(107, 182, 255, 0.3)" : "rgba(42, 82, 152, 0.2)") + ";" +
            "-fx-faint-focus-color: " + (ThemeManager.isDarkMode() ? "rgba(107, 182, 255, 0.15)" : "rgba(42, 82, 152, 0.1)") + ";"
        ;
        courseComboBox.setStyle(comboBoxStyle);
        
        courseBox.getChildren().addAll(courseLabel, courseComboBox);
        
        // Gender ComboBox - Editable with search
        VBox genderBox = new VBox(5);
        Label genderLabel = new Label("Gender");
        genderLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        
        // Create observable list
        ObservableList<String> genderItems = FXCollections.observableArrayList("Male", "Female", "Other");
        
        // Non-editable ComboBox (no search)
        genderComboBox = new ComboBox<>(genderItems);
        genderComboBox.setPromptText("Select gender...");
        genderComboBox.setEditable(false);
        genderComboBox.setMaxWidth(Double.MAX_VALUE);
        genderComboBox.setPrefHeight(45);
        
        // Enhanced styling for gender ComboBox
        String genderComboBoxStyle = 
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 0px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);" +
            "-fx-popup-hide-animation-duration: 0ms;" +
            "-fx-control-inner-background: " + fieldBg + ";" +
            "-fx-cell-padding: 4;" +
            "-fx-focus-color: " + (ThemeManager.isDarkMode() ? "rgba(107, 182, 255, 0.3)" : "rgba(42, 82, 152, 0.2)") + ";" +
            "-fx-faint-focus-color: " + (ThemeManager.isDarkMode() ? "rgba(107, 182, 255, 0.15)" : "rgba(42, 82, 152, 0.1)") + ";"
        ;
        genderComboBox.setStyle(genderComboBoxStyle);
        
        // No editor styling or search behavior for gender
        
        genderBox.getChildren().addAll(genderLabel, genderComboBox);
        
        // Password field
        VBox passwordBox = new VBox(5);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(45);
        passwordField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Confirm Password field
        VBox confirmPasswordBox = new VBox(5);
        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(45);
        confirmPasswordField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + promptTextColor + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        confirmPasswordBox.getChildren().addAll(confirmPasswordLabel, confirmPasswordField);
        
        // Create Account button (MATCHES LOGIN BUTTON STYLE)
        signupButton = new Button("Create Account");
        signupButton.setPrefWidth(330);
        signupButton.setPrefHeight(50);
        signupButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f5c542 0%, #d4a229 100%);" +
            "-fx-text-fill: #1e3c72;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(245, 197, 66, 0.4), 15, 0, 0, 4);"
        );
        signupButton.setOnAction(e -> handleSignup());
        
        // Back to Login button
        backButton = new Button("Back to Login");
        backButton.setPrefWidth(330);
        backButton.setPrefHeight(45);
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.4)" : "rgba(30, 60, 114, 0.3)") + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#ffffff" : "#1e3c72") + ";" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> controller.navigateToLogin());
        
        // Add all components to signup card (WITH LOGO - MATCHES LOGIN)
        signupCard.getChildren().addAll(
            logoContainer,
            titleLabel,
            studentIdBox,
            firstNameBox,
            lastNameBox,
            courseBox,
            genderBox,
            passwordBox,
            confirmPasswordBox,
            signupButton,
            backButton
        );
        
        scrollPane.setContent(signupCard);
        
        // Wrap scrollPane in a StackPane to maintain fixed width while allowing scroll
        // Add 17px to width to account for scrollbar (scrollbar is always visible)
        StackPane scrollWrapper = new StackPane(scrollPane);
        scrollWrapper.setPrefWidth(497);
        scrollWrapper.setMaxWidth(497);
        scrollWrapper.setMinWidth(397);
        scrollWrapper.setPrefHeight(600);
        scrollWrapper.setMaxHeight(600);
        scrollWrapper.setAlignment(Pos.CENTER);
        
        // Theme Toggle Switch (Icon Only, Top Right) - MATCHES LOGIN
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
        
        // Position circle based on theme (MATCHES LOGIN)
        StackPane.setAlignment(toggleCircle, ThemeManager.isDarkMode() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        
    toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
    toggleSwitch.setOnMouseClicked(e -> toggleTheme());
    toggleSwitch.setStyle("-fx-cursor: hand;");
    HBox toggleRow = new HBox();
    toggleRow.setAlignment(Pos.TOP_RIGHT);
    toggleRow.setPrefHeight(40);
    toggleRow.setMaxHeight(40);
    toggleRow.getChildren().add(toggleSwitch);
    toggleRow.setPadding(new Insets(4, 6, 0, 6));
    
    rightContainer.getChildren().addAll(toggleRow, scrollWrapper);
        
        // Ensure toggle is always on top with higher z-index
        toggleSwitch.setMouseTransparent(false);
        toggleSwitch.setPickOnBounds(true);
        toggleSwitch.toFront();  // Bring to front to ensure it's above everything
        
        // Assemble split – enforce equal growth like LoginView so both sides stay the same size
        split.getChildren().addAll(leftPane, rightContainer);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);
        view.getChildren().add(split);
    }
    
    private void handleSignup() {
        System.out.println("=== SIGNUP BUTTON CLICKED ===");
        String studentId = studentIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String course = courseComboBox.getValue();
        String gender = genderComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        System.out.println("Calling controller.handleSignup()...");
        controller.handleSignup(studentId, firstName, lastName, course, gender, password, confirmPassword);
    }
    
    private void toggleTheme() {
        ThemeManager.toggleLightDark();
        
        // Update toggle switch appearance with smooth animation
        toggleIcon.setText(ThemeManager.isDarkMode() ? "🌙" : "☀");
        
        // Animate circle position smoothly with ease-out effect
        double targetX = ThemeManager.isDarkMode() ? 44 : 2; // Right: 44, Left: 2
        Timeline slideAnimation = new Timeline(
            new KeyFrame(Duration.millis(400), 
                new KeyValue(toggleCircle.translateXProperty(), targetX - toggleCircle.getLayoutX())
            )
        );
        slideAnimation.setCycleCount(1);
        slideAnimation.play();
        
        // Update circle color with improved gradient
        String circleColor = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #6bb6ff 0%, #2a7fd9 50%, #1a5fa0 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #ffd700 0%, #ffed4e 50%, #f5b542 100%)";
        toggleCircle.setStyle(
            "-fx-background-color: " + circleColor + ";" +
            "-fx-background-radius: 13px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 3);"
        );
        
        // Update background color with better styling
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
        
        updateTheme();
    }
    
    private void updateTheme() {
        boolean isDark = ThemeManager.isDarkMode();
        
        // Update main background gradient (MATCHES LOGIN)
        String bgGradient = isDark 
            ? "linear-gradient(from 0% 0% to 100% 100%, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        view.setStyle("-fx-background-color: " + bgGradient + ";");
        
        // Update right container background (MATCHES LOGIN)
        String cardBg = isDark ? "rgba(30, 40, 70, 0.9)" : "#ffffff";
        if (rightContainer != null) {
            rightContainer.setStyle(
                "-fx-background-color: " + cardBg + ";"
            );
        }
        
        // Get signupCard from scrollPane (which is inside scrollWrapper)
        StackPane scrollWrapper = (StackPane) rightContainer.getChildren().get(1);
        ScrollPane scrollPane = (ScrollPane) scrollWrapper.getChildren().get(0);
        VBox signupCard = (VBox) scrollPane.getContent();
        
        // Update colors
        String titleColor = isDark ? "#ffffff" : "#1e3c72";
        String labelColor = isDark ? "#e0e0e0" : "#555555";
        String fieldBg = isDark ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.9)";
        String fieldText = isDark ? "#ffffff" : "#333333";
        String fieldBorder = isDark ? "rgba(255,255,255,0.4)" : "rgba(46, 92, 152, 0.3)";
        String promptTextColor = isDark ? "rgba(255,255,255,0.5)" : "#999999";
        
        // Update title section
        for (int i = 0; i < signupCard.getChildren().size(); i++) {
            if (signupCard.getChildren().get(i) instanceof Label) {
                Label label = (Label) signupCard.getChildren().get(i);
                if (label.getText().equals("Create Account")) {
                    label.setStyle("-fx-text-fill: " + titleColor + ";");
                    break;
                }
            }
        }
        
        // Update all VBox containers (field groups) (MATCHES LOGIN)
        for (int i = 0; i < signupCard.getChildren().size(); i++) {
            if (signupCard.getChildren().get(i) instanceof VBox) {
                VBox fieldBox = (VBox) signupCard.getChildren().get(i);
                if (fieldBox.getChildren().size() == 2) {
                    // This is a field group with label and input
                    if (fieldBox.getChildren().get(0) instanceof Label) {
                        Label label = (Label) fieldBox.getChildren().get(0);
                        // Skip title/subtitle labels
                        if (!label.getText().equals("Create Account") && 
                            !label.getText().equals("Join our community today")) {
                            label.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 12px;");
                        }
                    }
                    
                    if (fieldBox.getChildren().get(1) instanceof TextField || 
                        fieldBox.getChildren().get(1) instanceof PasswordField) {
                        fieldBox.getChildren().get(1).setStyle(
                            "-fx-font-size: 13px;" +
                            "-fx-background-color: " + fieldBg + ";" +
                            "-fx-text-fill: " + fieldText + ";" +
                            "-fx-border-color: " + fieldBorder + ";" +
                            "-fx-border-width: 1.5px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 10px;" +
                            "-fx-prompt-text-fill: " + promptTextColor + ";" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                        );
                    } else if (fieldBox.getChildren().get(1) instanceof ComboBox) {
                        fieldBox.getChildren().get(1).setStyle(
                            "-fx-font-size: 13px;" +
                            "-fx-background-color: " + fieldBg + ";" +
                            "-fx-text-fill: " + fieldText + ";" +
                            "-fx-border-color: " + fieldBorder + ";" +
                            "-fx-border-width: 1.5px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-padding: 10px;" +
                            "-fx-prompt-text-fill: " + promptTextColor + ";" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
                        );
                    }
                }
            }
        }
        
        // Update back button
        String backButtonText = isDark ? "#ffffff" : "#1e3c72";
        String backButtonBorder = isDark ? "rgba(255,255,255,0.4)" : "rgba(30, 60, 114, 0.3)";
        backButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + backButtonBorder + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-text-fill: " + backButtonText + ";" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;"
        );
    }
    
    public VBox getView() {
        return view;
    }

    // (Removed search helpers; simple dropdown implementation retained.)
}

