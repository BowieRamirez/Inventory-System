package gui.views;

import gui.controllers.LoginController;
import gui.utils.GUIValidator;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * LoginView - Login screen for the STI ProWear System
 * 
 * Provides a modern login interface with username/student ID input,
 * password field, role selection, and theme toggle.
 */
public class LoginView {
    
    private VBox view;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button signupButton;
    private Button themeToggleButton;
    private LoginController controller;
    
    public LoginView() {
        controller = new LoginController();
        initializeView();
    }
    
    private void initializeView() {
        // Main container
        view = new VBox(20);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(50));
        view.setStyle("-fx-background-color: -color-bg-default;");
        
        // Login card container
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setMaxWidth(450);
        loginCard.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);"
        );
        
        // Title
        Label titleLabel = new Label("STI ProWear System");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        // Subtitle
        Label subtitleLabel = new Label("Modern Inventory Management");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        
        // Username/Student ID field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Username / Student ID");
        usernameLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username or student ID");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px;");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px;");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Buttons container
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Login button
        loginButton = new Button("Login");
        loginButton.setPrefWidth(150);
        loginButton.setPrefHeight(40);
        loginButton.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        loginButton.setOnAction(e -> handleLogin());
        
        // Signup button
        signupButton = new Button("Sign Up");
        signupButton.setPrefWidth(150);
        signupButton.setPrefHeight(40);
        signupButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: #0969DA;" +
            "-fx-border-width: 2px;" +
            "-fx-text-fill: #0969DA;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        signupButton.setOnAction(e -> handleSignup());
        
        buttonBox.getChildren().addAll(loginButton, signupButton);
        
        // Theme toggle button
        themeToggleButton = new Button(ThemeManager.isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode");
        themeToggleButton.setPrefHeight(35);
        themeToggleButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: -color-fg-muted;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;"
        );
        themeToggleButton.setOnAction(e -> toggleTheme());
        
        // Add Enter key support for login
        passwordField.setOnAction(e -> handleLogin());
        
        // Add all components to login card
        loginCard.getChildren().addAll(
            titleLabel,
            subtitleLabel,
            new Separator(),
            usernameBox,
            passwordBox,
            buttonBox,
            themeToggleButton
        );
        
        // Add login card to main view
        view.getChildren().add(loginCard);
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
        themeToggleButton.setText(ThemeManager.isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode");
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

