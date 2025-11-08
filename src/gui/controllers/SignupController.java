package gui.controllers;

import student.Student;
import gui.utils.AlertHelper;
import gui.utils.GUIValidator;
import gui.utils.SceneManager;
import gui.views.LoginView;
import utils.FileStorage;
import utils.TermsAndConditions;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.List;
import java.util.Optional;

/**
 * SignupController - Handles student registration logic
 * 
 * Manages the creation of new student accounts with validation
 * and persistence to file storage.
 */
public class SignupController {
    
    private List<Student> students;
    
    public SignupController() {
        loadStudents();
    }
    
    /**
     * Load existing students from file
     */
    private void loadStudents() {
        try {
            students = FileStorage.loadStudents();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load student data: " + e.getMessage());
            students = List.of();
        }
    }
    
    /**
     * Handle signup attempt - shows Terms and Conditions first
     * 
     * @param studentId The student ID
     * @param firstName The first name
     * @param lastName The last name
     * @param course The course code
     * @param gender The gender
     * @param password The password
     * @param confirmPassword The password confirmation
     */
    public void handleSignup(String studentId, String firstName, String lastName, 
                            String course, String gender, String password, String confirmPassword) {
        
        // Validate all inputs first
        if (!validateInputs(studentId, firstName, lastName, course, gender, password, confirmPassword)) {
            return;
        }
        
        // Check if student ID already exists
        if (studentIdExists(studentId)) {
            AlertHelper.showError("Registration Failed", 
                "Student ID already exists. Please use a different ID or contact administrator.");
            return;
        }
        
        // Show Terms and Conditions dialog
        if (!showTermsAndConditions()) {
            // User declined Terms and Conditions
            return;
        }
        
        // Create new student
        Student newStudent = new Student(studentId, password, course, firstName, lastName, gender);
        
        // Save to file
        try {
            boolean success = FileStorage.addStudent(students, newStudent);
            
            if (success) {
                AlertHelper.showSuccess("Registration Successful", 
                    "Account created successfully!\n\n" +
                    "Student ID: " + studentId + "\n" +
                    "Name: " + newStudent.getFullName() + "\n" +
                    "Course: " + course + "\n\n" +
                    "You can now login with your credentials.");
                
                // Navigate back to login
                navigateToLogin();
            } else {
                AlertHelper.showError("Registration Failed", 
                    "Failed to save student data. Please try again.");
            }
        } catch (Exception e) {
            AlertHelper.showException("Registration Failed", 
                "An error occurred during registration.", e);
        }
    }
    
    /**
     * Show Terms and Conditions dialog and get user acceptance
     * 
     * @return true if user accepts, false otherwise
     */
    private boolean showTermsAndConditions() {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Terms and Conditions");
        dialog.setHeaderText("Please read and accept the Terms and Conditions");
        
        // Create scrollable text area for terms
        TextArea termsArea = new TextArea(TermsAndConditions.getTermsAndConditionsText());
        termsArea.setEditable(false);
        termsArea.setWrapText(true);
        termsArea.setPrefRowCount(20);
        termsArea.setPrefColumnCount(60);
        termsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        // Wrap in VBox with padding
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.getChildren().add(termsArea);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setPrefSize(700, 600);
        
        // Add buttons
        ButtonType acceptButton = new ButtonType("I Accept", ButtonBar.ButtonData.OK_DONE);
        ButtonType declineButton = new ButtonType("I Decline", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(acceptButton, declineButton);
        
        // Style the buttons
        Button acceptBtn = (Button) dialog.getDialogPane().lookupButton(acceptButton);
        acceptBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;"
        );
        
        Button declineBtn = (Button) dialog.getDialogPane().lookupButton(declineButton);
        declineBtn.setStyle(
            "-fx-background-color: #dc3545;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;"
        );
        
        // Show dialog and wait for response
        Optional<ButtonType> result = dialog.showAndWait();
        
        return result.isPresent() && result.get() == acceptButton;
    }
    
    /**
     * Validate all signup inputs
     */
    private boolean validateInputs(String studentId, String firstName, String lastName, 
                                   String course, String gender, String password, String confirmPassword) {
        
        // Validate student ID
        if (!GUIValidator.isValidStudentId(studentId)) {
            AlertHelper.showError("Invalid Input", 
                "Student ID must be 10-12 digits.");
            return false;
        }
        
        // Validate first name
        if (!GUIValidator.isNotEmpty(firstName)) {
            AlertHelper.showError("Invalid Input", 
                "First name is required.");
            return false;
        }
        
        // Validate last name
        if (!GUIValidator.isNotEmpty(lastName)) {
            AlertHelper.showError("Invalid Input", 
                "Last name is required.");
            return false;
        }
        
        // Validate course
        if (course == null || !GUIValidator.isValidCourse(course)) {
            AlertHelper.showError("Invalid Input", 
                "Please select a valid course.");
            return false;
        }
        
        // Validate gender
        if (gender == null || gender.isEmpty()) {
            AlertHelper.showError("Invalid Input", 
                "Please select a gender.");
            return false;
        }
        
        // Validate password
        if (!GUIValidator.isValidPassword(password)) {
            AlertHelper.showError("Invalid Input", 
                "Password must be at least 6 characters long.");
            return false;
        }
        
        // Validate password confirmation
        if (!GUIValidator.passwordsMatch(password, confirmPassword)) {
            AlertHelper.showError("Invalid Input", 
                "Passwords do not match.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if student ID already exists
     */
    private boolean studentIdExists(String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Navigate back to login screen
     */
    public void navigateToLogin() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1024, 768);
        SceneManager.setScene(scene);
    }
}
