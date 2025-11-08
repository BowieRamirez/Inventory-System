package gui.views;

import gui.controllers.SignupController;
import gui.utils.GUIValidator;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

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
        signupButton.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
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
        signupCard.getChildren().addAll(
            titleLabel,
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
        
        // Wrap signup card in ScrollPane for better alignment
        ScrollPane scrollPane = new ScrollPane(signupCard);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPadding(new Insets(30));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Center the scroll pane content
        StackPane centerWrapper = new StackPane(scrollPane);
        centerWrapper.setAlignment(Pos.CENTER);
        
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
        String studentId = studentIdField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String course = courseComboBox.getValue();
        String gender = genderComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        controller.handleSignup(studentId, firstName, lastName, course, gender, password, confirmPassword);
    }
    
    /**
     * Get the view node
     */
    public BorderPane getView() {
        return view;
    }
}

