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
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#0969DA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Login card container
        VBox loginCard = new VBox(20);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setPadding(new Insets(40));
        loginCard.setMaxWidth(450);
        String cardBg = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "white";
        loginCard.setStyle(
            "-fx-background-color: " + cardBg + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);"
        );
        
        // Title
        Label titleLabel = new Label("STI ProWear System");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "#0969DA";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        // Subtitle
        Label subtitleLabel = new Label("Modern Inventory Management");
        subtitleLabel.setFont(Font.font("System", 14));
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "#656D76";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + ";");
        
        // Username/Student ID field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Username / Student ID");
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "#1F2328";
        usernameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-weight: bold;");
        usernameField = new TextField();
        usernameField.setPromptText("Enter username or student ID");
        usernameField.setPrefHeight(40);
        String fieldBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "white";
        String fieldText = ThemeManager.isDarkMode() ? "-color-fg-default" : "#1F2328";
        String fieldBorder = ThemeManager.isDarkMode() ? "-color-border-default" : "#D0D7DE";
        usernameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;"
        );
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-weight: bold;");
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;"
        );
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
        updateTheme();
    }
    
    /**
     * Update all theme-dependent colors
     */
    private void updateTheme() {
        // Update main background
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#0969DA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Update login card
        VBox loginCard = (VBox) view.getChildren().get(0);
        String cardBg = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "white";
        loginCard.setStyle(
            "-fx-background-color: " + cardBg + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);"
        );
        
        // Update title
        Label titleLabel = (Label) loginCard.getChildren().get(0);
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "#0969DA";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + "; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        // Update subtitle
        Label subtitleLabel = (Label) loginCard.getChildren().get(1);
        String subtitleColor = ThemeManager.isDarkMode() ? "-color-fg-muted" : "#656D76";
        subtitleLabel.setStyle("-fx-text-fill: " + subtitleColor + "; -fx-font-size: 14px;");
        
        // Update labels
        String labelColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "#1F2328";
        VBox usernameBox = (VBox) loginCard.getChildren().get(3);
        Label usernameLabel = (Label) usernameBox.getChildren().get(0);
        usernameLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-weight: bold;");
        
        VBox passwordBox = (VBox) loginCard.getChildren().get(4);
        Label passwordLabel = (Label) passwordBox.getChildren().get(0);
        passwordLabel.setStyle("-fx-text-fill: " + labelColor + "; -fx-font-weight: bold;");
        
        // Update text fields
        String fieldBg = ThemeManager.isDarkMode() ? "-color-bg-default" : "white";
        String fieldText = ThemeManager.isDarkMode() ? "-color-fg-default" : "#1F2328";
        String fieldBorder = ThemeManager.isDarkMode() ? "-color-border-default" : "#D0D7DE";
        usernameField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;"
        );
        passwordField.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;"
        );
        
        // Update signup button
        String signupBorder = ThemeManager.isDarkMode() ? "-color-accent-fg" : "#0969DA";
        String signupText = ThemeManager.isDarkMode() ? "-color-accent-fg" : "#0969DA";
        signupButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: " + signupBorder + ";" +
            "-fx-border-width: 2px;" +
            "-fx-text-fill: " + signupText + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-border-radius: 6px;" +
            "-fx-cursor: hand;"
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

