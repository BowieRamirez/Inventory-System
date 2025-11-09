package gui.controllers;

import java.util.List;

import admin.Admin;
import admin.Staff;
import gui.utils.AlertHelper;
import gui.utils.GUIValidator;
import gui.utils.SceneManager;
import gui.views.SignupView;
import javafx.scene.Scene;
import student.Student;
import utils.FileStorage;
import utils.SystemConfigManager;
import utils.SystemLogger;

/**
 * LoginController - Handles authentication logic for the login screen
 * 
 * Manages user authentication for all roles (Admin, Staff, Cashier, Student)
 * and navigates to appropriate dashboards upon successful login.
 */
public class LoginController {
    
    private List<Student> students;
    private List<Staff> staffList;
    
    public LoginController() {
        // Load students and staff from file
        loadStudents();
        loadStaff();
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
     * Load staff from file storage
     */
    private void loadStaff() {
        try {
            staffList = FileStorage.loadStaff();
            System.out.println("Loaded " + staffList.size() + " staff members for authentication.");
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load staff data: " + e.getMessage());
            staffList = List.of(); // Empty list as fallback
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
            SystemLogger.logAuthenticationFailure(username, "Empty username or password");
            return;
        }

        // Try to authenticate as Admin first (admin can always login)
        if (authenticateAdmin(username, password)) {
            SystemLogger.logLogin(username, "Admin");
            navigateToAdminDashboard();
            return;
        }
        
        // Check maintenance mode for non-admin users
        SystemConfigManager configManager = SystemConfigManager.getInstance();
        if (configManager.isMaintenanceModeActive()) {
            AlertHelper.showWarning("System Maintenance", 
                "⚠️ " + configManager.getMaintenanceMessage() + "\n\n" +
                "Please contact the administrator for more information.");
            SystemLogger.logAuthenticationFailure(username, "System under maintenance");
            return;
        }

        // Try to authenticate as Staff or Cashier
        Staff staff = authenticateStaff(username, password);
        if (staff != null) {
            SystemLogger.logLogin(username, staff.getRole());
            
            // Navigate to appropriate dashboard based on role
            if ("Cashier".equals(staff.getRole())) {
                navigateToCashierDashboard();
            } else {
                navigateToStaffDashboard();
            }
            return;
        }

        // Try to authenticate as Student
        Student student = authenticateStudent(username, password);
        if (student != null) {
            SystemLogger.logLogin(username, "Student");
            navigateToStudentDashboard(student);
            return;
        }

        // If all authentication attempts fail
        AlertHelper.showError("Login Failed", "Invalid credentials. Please try again.");
        SystemLogger.logAuthenticationFailure(username, "Invalid credentials");
    }
    
    /**
     * Authenticate admin user
     */
    private boolean authenticateAdmin(String username, String password) {
        Admin admin = new Admin(username, password);
        return admin.authenticate();
    }
    
    /**
     * Authenticate staff user (includes both Staff and Cashier roles)
     * 
     * @return The authenticated Staff object, or null if authentication fails
     */
    private Staff authenticateStaff(String staffId, String password) {
        // Find staff by ID
        for (Staff staff : staffList) {
            if (staff.getStaffId().equals(staffId) && 
                staff.getPassword().equals(password)) {
                
                // Check if account is active
                if (!staff.isActive()) {
                    AlertHelper.showError("Account Deactivated", 
                        "Your account has been deactivated. Please contact the administrator.");
                    return null;
                }
                
                return staff;
            }
        }
        
        return null;
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

