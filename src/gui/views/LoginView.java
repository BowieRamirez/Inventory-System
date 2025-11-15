package gui.views;

import java.io.File;

import gui.controllers.LoginController;
import gui.utils.GUIValidator;
import gui.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
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

/**
 * LoginView - Login screen for the STI ProWear System
 * 

        // We'll layer a blurred image behind the HBox so it extends behind the login card
        StackPane layeredSplit = new StackPane();
        layeredSplit.setMaxWidth(Double.MAX_VALUE);
        layeredSplit.setMaxHeight(Double.MAX_VALUE);
        layeredSplit.setAlignment(Pos.CENTER_LEFT);
 * Provides a modern login interface with username/student ID input,
 * password field, role selection, and theme toggle.
 */
public class LoginView {
    
    private VBox view;
    private VBox rightContainer; // card container on the right
    private VBox loginCard;      // inner form stack
    private Label titleLabel;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button signupButton;
    private StackPane toggleSwitch;
    private StackPane toggleCircle;
    private Label toggleIcon;
    private LoginController controller;
    
    public LoginView() {
        controller = new LoginController();
        initializeView();
    }
    
    private void initializeView() {
        // Root container (keeps entire scene centered)
        view = new VBox(0);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(30));
        view.setMaxWidth(Double.MAX_VALUE);
        view.setMaxHeight(Double.MAX_VALUE);

        // Subtle app background (not the shirt image)
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
    leftPane.setStyle("-fx-background-color: " + leftBg + "; -fx-background-radius: 20 0 0 20;");

        try {
            File sideImageFile = new File("src/database/data/images/NewSides2.png");
            if (sideImageFile.exists()) {
                Image sideImage = new Image(sideImageFile.toURI().toString(), true);
                ImageView shirt = new ImageView(sideImage);
                shirt.setPreserveRatio(false);
                shirt.setSmooth(true);
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
            "-fx-background-color: " + cardBg + ";" +
            "-fx-background-radius: 0 20 20 0;"
        );

        // Internal login form content
        loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(10, 45, 10, 45));
        loginCard.setMaxWidth(Double.MAX_VALUE);
        loginCard.setMinWidth(350);
        
        // Logo Section - Load STI ProWear Logo with rounded corners
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
        
        // Login Title
    titleLabel = new Label("Login");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        String titleColor = ThemeManager.isDarkMode() ? "#ffffff" : "#1e3c72";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        // Student ID field (changed from Email)
        VBox usernameBox = new VBox(10);
        Label usernameLabel = new Label("Student ID - Username");
        String labelColor = ThemeManager.isDarkMode() ? "#e0e0e0" : "#555555";
        usernameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        usernameField = new TextField();
        usernameField.setPromptText("Type your Student ID or Username");
        usernameField.setPrefHeight(45);
        String fieldBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.9)";
        String fieldText = ThemeManager.isDarkMode() ? "#ffffff" : "#333333";
        String fieldBorder = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.4)" : "rgba(46, 92, 152, 0.3)";
        usernameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.5)" : "#999999") + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordBox = new VBox(10);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Type your password");
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
            "-fx-prompt-text-fill: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.5)" : "#999999") + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Sign in button (full width, yellow theme with glass effect)
        loginButton = new Button("Sign in");
        loginButton.setPrefWidth(330);
        loginButton.setPrefHeight(50);
        loginButton.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #f5c542 0%, #d4a229 100%);" +
            "-fx-text-fill: #1e3c72;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(245, 197, 66, 0.4), 15, 0, 0, 4);"
        );
        loginButton.setOnAction(e -> handleLogin());
        
        // Sign up prompt
        HBox signupPrompt = new HBox(5);
        signupPrompt.setAlignment(Pos.CENTER);
        Label promptLabel = new Label("Don't have an account yet?");
        promptLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0b0b0" : "#666666") + "; -fx-font-size: 12px;");
        
        signupButton = new Button("Sign up");
        signupButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#6fb1fc" : "#2a5298") + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;" +
            "-fx-padding: 0;"
        );
        signupButton.setOnAction(e -> handleSignup());
        
        signupPrompt.getChildren().addAll(promptLabel, signupButton);
        
    // Theme toggle switch (placed at top-right inside the right container)
    // Toggle switch button (pill shape with sliding circle) - smaller
        toggleSwitch = new StackPane();
        toggleSwitch.setPrefWidth(70);
        toggleSwitch.setPrefHeight(32);
        toggleSwitch.setMaxWidth(70);
        toggleSwitch.setMaxHeight(32);
        
        // Background pill - smaller
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
        
        // Sliding circle with icon - smaller
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
        
        // Icon inside circle - smaller
        toggleIcon = new Label(ThemeManager.isDarkMode() ? "🌙" : "☀");
        toggleIcon.setFont(Font.font("System", FontWeight.BOLD, 12));
        toggleIcon.setStyle("-fx-text-fill: #000000;");
        toggleCircle.getChildren().add(toggleIcon);
        
        // Position circle based on theme
        StackPane.setAlignment(toggleCircle, ThemeManager.isDarkMode() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        StackPane.setMargin(toggleCircle, new Insets(0, 2, 0, 2));
        
        toggleSwitch.getChildren().addAll(toggleBg, toggleCircle);
        toggleSwitch.setOnMouseClicked(e -> toggleTheme());
        toggleSwitch.setStyle("-fx-cursor: hand;");
        
    // Place toggle inside a header row to keep VBox structure
    HBox toggleRow = new HBox();
    toggleRow.setAlignment(Pos.TOP_RIGHT);
    toggleRow.getChildren().add(toggleSwitch);
    toggleRow.setPadding(new Insets(4, 6, 0, 6));
        
        // Add Enter key support for login
        passwordField.setOnAction(e -> handleLogin());
        
        // Assemble form
        loginCard.getChildren().addAll(
            logoContainer,
            titleLabel,
            usernameBox,
            passwordBox,
            loginButton,
            signupPrompt
        );

        rightContainer.getChildren().addAll(toggleRow, loginCard);

        // Add both containers to the HBox
        split.getChildren().addAll(leftPane, rightContainer);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox.setHgrow(rightContainer, Priority.ALWAYS);

        // Add split to root view (kept centered by view alignment)
        view.getChildren().add(split);
    }
    
    /**
     * Handle login button click
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (!GUIValidator.isNotEmpty(username)) {
            GUIValidator.applyValidationStyle(usernameField, false);
            return;
        }

        if (!GUIValidator.isNotEmpty(password)) {
            GUIValidator.applyValidationStyle(usernameField, true);
            passwordField.setStyle("-fx-border-color: #CF222E; -fx-border-width: 2px;");
            return;
        }

        // Clear validation styles
        GUIValidator.clearValidationStyle(usernameField);
        passwordField.setStyle("");

        // Call controller to handle authentication (auto-detect role)
        controller.handleLogin(username, password);
    }
    
    /**
     * Handle signup button click
     */
    private void handleSignup() {
        controller.handleSignup();
    }
    
    /**
     * Toggle between light and dark theme
     */
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
            "-fx-background-radius: 17px;" +
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
    
    /**
     * Update all theme-dependent colors
     */
    private void updateTheme() {
        // Update main background gradient - JavaFX style
        String bgGradient = ThemeManager.isDarkMode() 
            ? "linear-gradient(from 0% 0% to 100% 100%, #1a2a6c 0%, #0d1b4d 50%, #1a2a6c 100%)"
            : "linear-gradient(from 0% 0% to 100% 100%, #1e3c72 0%, #2a5298 50%, #1e3c72 100%)";
        view.setStyle("-fx-background-color: " + bgGradient + ";");
        
        // Update right container background and card look
        String cardBg = ThemeManager.isDarkMode() ? "rgba(30, 40, 70, 0.9)" : "#ffffff";
        if (rightContainer != null) {
            rightContainer.setStyle(
                "-fx-background-color: " + cardBg + ";"
            );
        }
        
        // Update title
        String titleColor = ThemeManager.isDarkMode() ? "#ffffff" : "#1e3c72";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 32px; -fx-font-weight: bold;");
        
        // Update labels
        String labelColor = ThemeManager.isDarkMode() ? "#e0e0e0" : "#555555";
        VBox usernameBox = (VBox) loginCard.getChildren().get(2);
        Label usernameLabel = (Label) usernameBox.getChildren().get(0);
        usernameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        
        VBox passwordBox = (VBox) loginCard.getChildren().get(3);
        Label passwordLabel = (Label) passwordBox.getChildren().get(0);
        passwordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-size: 13px;");
        
        // Update text fields with glass effect
        String fieldBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.9)";
        String fieldText = ThemeManager.isDarkMode() ? "#ffffff" : "#333333";
        String fieldBorder = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.4)" : "rgba(46, 92, 152, 0.3)";
        usernameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.5)" : "#999999") + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        passwordField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1.5px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 12px;" +
            "-fx-prompt-text-fill: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.5)" : "#999999") + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        // Update signup prompt (index 5 - after login button)
        HBox signupPrompt = (HBox) loginCard.getChildren().get(5);
        Label promptLabel = (Label) signupPrompt.getChildren().get(0);
        promptLabel.setStyle("-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#b0b0b0" : "#666666") + "; -fx-font-size: 12px;");
        
        signupButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + (ThemeManager.isDarkMode() ? "#6fb1fc" : "#2a5298") + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;" +
            "-fx-padding: 0;"
        );
    }
    
    /**
     * Get the view node
     * 
     * @return The root VBox of this view
     */
    public VBox getView() {
        return view;
    }
}

