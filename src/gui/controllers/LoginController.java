package gui.controllers;

import admin.Admin;
import admin.Staff;
import admin.Cashier;
import student.Student;
import gui.utils.AlertHelper;
import gui.utils.GUIValidator;
import gui.utils.SceneManager;
import gui.views.SignupView;
import utils.FileStorage;
import javafx.scene.Scene;
import java.util.List;

/**
 * LoginController - Handles authentication logic for the login screen
 * 
 * Manages user authentication for all roles (Admin, Staff, Cashier, Student)
 * and navigates to appropriate dashboards upon successful login.
 */
public class LoginController {
    
    private List<Student> students;
    
    public LoginController() {
        // Load students from file
        loadStudents();
    }
    
    /**
     * Load students from file storage
     */
    private void loadStudents() {
        try {
            students = FileStorage.loadStudents();
            System.out.println("Loaded " + students.size() + " students for authentication.");
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load student data: " + e.getMessage());
            students = List.of(); // Empty list as fallback
        }
    }
    
    /**
     * Handle login attempt - Auto-detects role based on credentials
     *
     * @param username The username or student ID
     * @param password The password
     */
    public void handleLogin(String username, String password) {
        // Validate inputs
        if (!GUIValidator.isNotEmpty(username) || !GUIValidator.isNotEmpty(password)) {
            AlertHelper.showError("Login Failed", "Please enter both username and password.");
            return;
        }

        // Try to authenticate as Admin
        if (authenticateAdmin(username, password)) {
            navigateToAdminDashboard();
            return;
        }

        // Try to authenticate as Staff
        if (authenticateStaff(username, password)) {
            navigateToStaffDashboard();
            return;
        }

        // Try to authenticate as Cashier
        if (authenticateCashier(username, password)) {
            navigateToCashierDashboard();
            return;
        }

        // Try to authenticate as Student
        Student student = authenticateStudent(username, password);
        if (student != null) {
            navigateToStudentDashboard(student);
            return;
        }

        // If all authentication attempts fail
        AlertHelper.showError("Login Failed", "Invalid credentials. Please try again.");
    }
    
    /**
     * Authenticate admin user
     */
    private boolean authenticateAdmin(String username, String password) {
        Admin admin = new Admin(username, password);
        return admin.authenticate();
    }
    
    /**
     * Authenticate staff user
     */
    private boolean authenticateStaff(String username, String password) {
        Staff staff = new Staff(username, password);
        return staff.authenticate();
    }
    
    /**
     * Authenticate cashier user
     */
    private boolean authenticateCashier(String username, String password) {
        Cashier cashier = new Cashier(username, password);
        return cashier.authenticate();
    }
    
    /**
     * Authenticate student user
     * 
     * @return The authenticated Student object, or null if authentication fails
     */
    private Student authenticateStudent(String studentId, String password) {
        // Validate student ID format
        if (!GUIValidator.isValidStudentId(studentId)) {
            return null;
        }
        
        // Find student by ID
        for (Student student : students) {
            if (student.getStudentId().equals(studentId) && 
                student.getPassword().equals(password)) {
                
                // Check if account is active
                if (!student.isActive()) {
                    AlertHelper.showError("Account Deactivated", 
                        "Your account has been deactivated. Please contact the administrator.");
                    return null;
                }
                
                return student;
            }
        }
        
        return null;
    }
    
    /**
     * Navigate to Admin Dashboard
     */
    private void navigateToAdminDashboard() {
        gui.views.AdminDashboard dashboard = new gui.views.AdminDashboard();
        Scene scene = new Scene(dashboard.getView(), 1280, 800);
        SceneManager.setScene(scene);
        SceneManager.setTitle("STI ProWear System - Admin Dashboard");
    }
    
    /**
     * Navigate to Staff Dashboard
     */
    private void navigateToStaffDashboard() {
        gui.views.StaffDashboard dashboard = new gui.views.StaffDashboard();
        Scene scene = new Scene(dashboard.getView(), 1280, 800);
        SceneManager.setScene(scene);
        SceneManager.setTitle("STI ProWear System - Staff Dashboard");
    }
    
    /**
     * Navigate to Cashier Dashboard
     */
    private void navigateToCashierDashboard() {
        gui.views.CashierDashboard dashboard = new gui.views.CashierDashboard();
        Scene scene = new Scene(dashboard.getView(), 1280, 800);
        SceneManager.setScene(scene);
        SceneManager.setTitle("STI ProWear System - Cashier Dashboard");
    }
    
    /**
     * Navigate to Student Dashboard
     *
     * @param student The authenticated student
     */
    private void navigateToStudentDashboard(Student student) {
        gui.views.StudentDashboard dashboard = new gui.views.StudentDashboard(student);
        Scene scene = new Scene(dashboard.getView(), 1280, 800);
        SceneManager.setScene(scene);
        SceneManager.setTitle("STI ProWear System - Student Portal");
    }
    
    /**
     * Handle signup button click
     */
    public void handleSignup() {
        // Navigate to signup view
        SignupView signupView = new SignupView();
        Scene scene = new Scene(signupView.getView(), 1024, 768);
        SceneManager.setScene(scene);
    }
}

