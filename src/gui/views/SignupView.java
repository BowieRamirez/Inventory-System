package gui.views;

import gui.controllers.SignupController;
import gui.utils.GUIValidator;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import utils.SystemConfigManager;

/**
 * SignupView - Student registration screen
 * 
 * Provides a form for new students to create an account
 */
public class SignupView {
    
    private BorderPane view;
    private TextField studentIdField;
    private TextField firstNameField;
    private TextField lastNameField;
    private ComboBox<String> courseComboBox;
    private ComboBox<String> genderComboBox;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private Button signupButton;
    private Button backButton;
    private SignupController controller;
    
    public SignupView() {
        controller = new SignupController();
        initializeView();
    }
    
    private void initializeView() {
        // Main container
        view = new BorderPane();
        String bgColor = ThemeManager.isDarkMode() ? "-color-bg-default" : "#0969DA";
        view.setStyle("-fx-background-color: " + bgColor + ";");
        
        // Check if system is under maintenance
        SystemConfigManager configManager = SystemConfigManager.getInstance();
        boolean isMaintenanceMode = configManager.isMaintenanceModeActive();
        
        // Signup card
        VBox signupCard = new VBox(20);
        signupCard.setAlignment(Pos.CENTER_LEFT);
        signupCard.setPadding(new Insets(40));
        signupCard.setMaxWidth(500);
        String cardBg = ThemeManager.isDarkMode() ? "-color-bg-subtle" : "white";
        signupCard.setStyle(
            "-fx-background-color: " + cardBg + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);"
        );
        
        // Title
        Label titleLabel = new Label("Create Student Account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        String titleColor = ThemeManager.isDarkMode() ? "-color-fg-default" : "#0969DA";
        titleLabel.setStyle("-fx-text-fill: " + titleColor + ";");
        
        // Maintenance warning banner (if active)
        VBox maintenanceBanner = null;
        if (isMaintenanceMode) {
            maintenanceBanner = new VBox(10);
            maintenanceBanner.setAlignment(Pos.CENTER);
            maintenanceBanner.setPadding(new Insets(15));
            maintenanceBanner.setMaxWidth(Double.MAX_VALUE);
            maintenanceBanner.setStyle(
                "-fx-background-color: #FFF3CD;" +
                "-fx-border-color: #FFC107;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;"
            );
            
            Label warningIcon = new Label("⚠️");
            warningIcon.setFont(Font.font(24));
            warningIcon.setAlignment(Pos.CENTER);
            
            Label warningTitle = new Label("System Under Maintenance");
            warningTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            warningTitle.setStyle("-fx-text-fill: #664d03;");
            warningTitle.setWrapText(true);
            warningTitle.setMaxWidth(450);
            warningTitle.setAlignment(Pos.CENTER);
            
            Label warningMessage = new Label("Account creation is temporarily unavailable.\nPlease try again after maintenance.");
            warningMessage.setWrapText(true);
            warningMessage.setMaxWidth(450);
            warningMessage.setAlignment(Pos.CENTER);
            warningMessage.setStyle("-fx-text-fill: #664d03; -fx-font-size: 13px; -fx-text-alignment: center;");
            
            maintenanceBanner.getChildren().addAll(warningIcon, warningTitle, warningMessage);
        }
        
        // Student ID field
        VBox studentIdBox = createFieldBox("Student ID (10-12 digits)", 
            studentIdField = new TextField(), "Enter your student ID");
        
        // First Name field
        VBox firstNameBox = createFieldBox("First Name", 
            firstNameField = new TextField(), "Enter your first name");
        
        // Last Name field
        VBox lastNameBox = createFieldBox("Last Name", 
            lastNameField = new TextField(), "Enter your last name");
        
        // Course selection
        VBox courseBox = new VBox(8);
        Label courseLabel = new Label("Course");
        courseLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        courseComboBox = new ComboBox<>();
        courseComboBox.getItems().addAll(GUIValidator.getAllCourses());
        courseComboBox.setPromptText("Select your course");
        courseComboBox.setPrefHeight(40);
        courseComboBox.setMaxWidth(Double.MAX_VALUE);
        courseBox.getChildren().addAll(courseLabel, courseComboBox);
        
        // Gender selection
        VBox genderBox = new VBox(8);
        Label genderLabel = new Label("Gender");
        genderLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        genderComboBox = new ComboBox<>();
        genderComboBox.getItems().addAll("Male", "Female");
        genderComboBox.setPromptText("Select gender");
        genderComboBox.setPrefHeight(40);
        genderComboBox.setMaxWidth(Double.MAX_VALUE);
        genderBox.getChildren().addAll(genderLabel, genderComboBox);
        
        // Password field
        VBox passwordBox = createFieldBox("Password (min. 6 characters)", 
            passwordField = new PasswordField(), "Enter password");
        
        // Confirm Password field
        VBox confirmPasswordBox = createFieldBox("Confirm Password", 
            confirmPasswordField = new PasswordField(), "Re-enter password");
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        signupButton = new Button("Create Account");
        signupButton.setPrefWidth(180);
        signupButton.setPrefHeight(40);
        
        // Disable signup button if maintenance mode is active
        if (isMaintenanceMode) {
            signupButton.setDisable(true);
            signupButton.setStyle(
                "-fx-background-color: #6E7781;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.6;"
            );
        } else {
            signupButton.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        }
        signupButton.setOnAction(e -> handleSignup());
        
        backButton = new Button("Back to Login");
        backButton.setPrefWidth(180);
        backButton.setPrefHeight(40);
        backButton.setStyle(
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
        backButton.setOnAction(e -> controller.navigateToLogin());
        
        buttonBox.getChildren().addAll(signupButton, backButton);
        
        // Add all to signup card
        signupCard.getChildren().add(titleLabel);
        
        // Add maintenance banner if system is under maintenance
        if (isMaintenanceMode && maintenanceBanner != null) {
            signupCard.getChildren().add(maintenanceBanner);
        }
        
        signupCard.getChildren().addAll(
            new Separator(),
            studentIdBox,
            firstNameBox,
            lastNameBox,
            courseBox,
            genderBox,
            passwordBox,
            confirmPasswordBox,
            buttonBox
        );
        
        // Add extra spacing at the bottom for better layout
        Region bottomSpacer = new Region();
        bottomSpacer.setPrefHeight(30);
        signupCard.getChildren().add(bottomSpacer);
        
        // Wrap signup card in a centered container
        StackPane centerWrapper = new StackPane();
        centerWrapper.setAlignment(Pos.CENTER);
        centerWrapper.setPadding(new Insets(20));
        
        // Wrap signup card in ScrollPane for better scrolling
        ScrollPane scrollPane = new ScrollPane(signupCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setMaxWidth(550);
        
        centerWrapper.getChildren().add(scrollPane);
        view.setCenter(centerWrapper);
    }
    
    /**
     * Create a labeled field box
     */
    private VBox createFieldBox(String labelText, TextField field, String promptText) {
        VBox box = new VBox(8);
        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        field.setPromptText(promptText);
        field.setPrefHeight(40);
        field.setStyle("-fx-font-size: 14px;");
        box.getChildren().addAll(label, field);
        return box;
    }
    
    /**
     * Handle signup button click
     */
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
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

