package main;

import student.Student;
import student.StudentInterface;
import admin.Admin;
import admin.AdminInterface;
import admin.Staff;
import admin.StaffInterface;
import admin.Cashier;
import admin.CashierInterface;
import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Item;
import utils.InputValidator;
import utils.TermsAndConditions;
import utils.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MerchSystem {
    private Scanner scanner;
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private InputValidator validator;
    private List<Student> registeredStudents;
    
    public MerchSystem() {
        this.scanner = new Scanner(System.in);
        this.inventoryManager = new InventoryManager();
        this.reservationManager = new ReservationManager(inventoryManager);
        this.receiptManager = new ReceiptManager();
        this.validator = new InputValidator(this.scanner);
        this.registeredStudents = new ArrayList<>();
        // Load students from database
        loadStudentsFromDatabase();
        loadInventoryFromFile();
    }
    
    private void loadStudentsFromDatabase() {
        this.registeredStudents = FileStorage.loadStudents();
    }
    
    private void loadInventoryFromFile() {
        List<Item> loadedItems = FileStorage.loadItems();
        for (Item item : loadedItems) {
            inventoryManager.loadItem(item); // Use loadItem() to avoid unnecessary file writes during initialization
        }
    }
    
    public void start() {
        System.out.println("=================================");
        System.out.println("     STI ProWear System");
        System.out.println("=================================");
        
        while (true) {
            showMainLogin();
            int choice = validator.getValidInteger("Enter your choice: ", 0, 2);

            switch (choice) {
                case 0:
                    System.out.println("Thank you for using STI ProWear System!");
                    System.exit(0);
                    break;
                case 1:
                    // Single login entry (student or role-based quick-login)
                    handleStudentLogin();
                    break;
                case 2:
                    handleStudentSignup();
                    break;
            }
        }
    }
    
    private void showMainLogin() {
        System.out.println("\n=================================");
        System.out.println("           LOGIN");
        System.out.println("=================================");
        System.out.println("[1] Login");
        System.out.println("[2] Signup");
        System.out.println("[0] Exit");
    }
    
    private void handleMaintenanceLogin() {
        while (true) {
            System.out.println("\n=== MAINTENANCE LOGIN ===");
            System.out.println("Admin access for system maintenance");
            System.out.println("Enter [0] at any time to return to main menu.");
            
            String username = validator.getValidNonEmptyString("Username: ", "Username");
            if (username.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            String password = validator.getValidNonEmptyString("Password: ", "Password");
            if (password.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            Admin admin = new Admin(username, password);
            if (admin.authenticate()) {
                System.out.println("Login successful! Welcome Admin");
                AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, receiptManager, validator, registeredStudents);
                adminInterface.showMenu();
                return; // Exit after successful login and logout
            } else {
                System.out.println("Invalid credentials.");
                System.out.println("\nPress Enter to try again or Press [0] to return to main menu...");
                
                String choice = scanner.nextLine().trim();
                if (choice.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }
                // Loop continues to retry login
            }
        }
    }
    
    private void handleStaffLogin() {
        while (true) {
            System.out.println("\n=== PURCHASING & ASSET MANAGEMENT OFFICER LOGIN ===");
            System.out.println("Staff access for purchasing and asset management");
            System.out.println("Enter [0] at any time to return to main menu.");
            
            String username = validator.getValidNonEmptyString("Username: ", "Username");
            if (username.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            String password = validator.getValidNonEmptyString("Password: ", "Password");
            if (password.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            Staff staff = new Staff(username, password);
            if (staff.authenticate()) {
                System.out.println("Login successful! Welcome Staff");
                StaffInterface staffInterface = new StaffInterface(inventoryManager, reservationManager, receiptManager, validator);
                staffInterface.showMenu();
                return; // Exit after successful login and logout
            } else {
                System.out.println("Invalid credentials.");
                System.out.println("\nPress Enter to try again or Press [0] to return to main menu...");
                
                String choice = scanner.nextLine().trim();
                if (choice.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }
                // Loop continues to retry login
            }
        }
    }
    
    private void handleCashierLogin() {
        while (true) {
            System.out.println("\n=== CASHIER LOGIN ===");
            System.out.println("Cashier access for transaction management");
            System.out.println("Enter [0] at any time to return to main menu.");
            
            String username = validator.getValidNonEmptyString("Username: ", "Username");
            if (username.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            String password = validator.getValidNonEmptyString("Password: ", "Password");
            if (password.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }
            
            Cashier cashier = new Cashier(username, password);
            if (cashier.authenticate()) {
                System.out.println("Login successful! Welcome Cashier");
                CashierInterface cashierInterface = new CashierInterface(reservationManager, receiptManager, validator);
                cashierInterface.showMenu();
                return; // Exit after successful login and logout
            } else {
                System.out.println("Invalid credentials.");
                System.out.println("\nPress Enter to try again or Press [0] to return to main menu...");
                
                String choice = scanner.nextLine().trim();
                if (choice.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }
                // Loop continues to retry login
            }
        }
    }
    
    private void handleStudentLogin() {
        while (true) {
            System.out.println("\n--- Student Login ---");
            System.out.println("Enter [0] at any time to return to main menu.");
            // Accept either a student ID (numeric) or role keywords: admin, staff, cashier
            String idInput = validator.getValidNonEmptyString("Student ID: ", "Student ID");
            if (idInput.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }

            String lower = idInput.trim().toLowerCase();

            // Role-based quick login from this prompt
            if (lower.equals("admin") || lower.equals("staff") || lower.equals("cashier")) {
                String password = validator.getValidNonEmptyString("Password: ", "Password");
                if (password.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }

                if (lower.equals("admin")) {
                    Admin admin = new Admin("admin", password);
                    if (admin.authenticate()) {
                        System.out.println("Login successful! Welcome Admin");
                        AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, receiptManager, validator, registeredStudents);
                        adminInterface.showMenu();
                        return;
                    }
                } else if (lower.equals("staff")) {
                    Staff staff = new Staff("staff", password);
                    if (staff.authenticate()) {
                        System.out.println("Login successful! Welcome Staff");
                        StaffInterface staffInterface = new StaffInterface(inventoryManager, reservationManager, receiptManager, validator);
                        staffInterface.showMenu();
                        return;
                    }
                } else { // cashier
                    Cashier cashier = new Cashier("cashier", password);
                    if (cashier.authenticate()) {
                        System.out.println("Login successful! Welcome Cashier");
                        CashierInterface cashierInterface = new CashierInterface(reservationManager, receiptManager, validator);
                        cashierInterface.showMenu();
                        return;
                    }
                }

                System.out.println("Invalid credentials for role: " + idInput + ".");
                System.out.println("Press Enter to try again or Press [0] to return to main menu...");
                String choice = scanner.nextLine().trim();
                if (choice.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }
                continue; // retry
            }

            // Otherwise treat as student ID
            String studentId = idInput.trim();
            // Basic validation: must be digits and length between 10 and 12
            if (!studentId.matches("\\d+") || studentId.length() < 10 || studentId.length() > 12) {
                System.out.println("Invalid Student ID format. Please enter a valid numeric Student ID (10-12 digits).");
                continue;
            }

            String password = validator.getValidNonEmptyString("Password: ", "Password");
            if (password.equals("0")) {
                System.out.println("Returning to main menu...");
                return;
            }

            Student student = findStudentByCredentials(studentId, password);
            if (student != null) {
                // Check if account is active
                if (!student.isActive()) {
                    System.out.println("⚠ Your account has been deactivated.");
                    System.out.println("This may be because you are not currently enrolled.");
                    System.out.println("Please contact the admin to reactivate your account.");
                    validator.waitForEnter("Press Enter to return to main menu...");
                    return;
                }

                System.out.println("Login successful! Welcome " + student.getFullName());
                StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, receiptManager, validator, student);
                studentInterface.showMenu();
                return; // Exit after successful login and logout
            } else {
                System.out.println("Invalid credentials or student ID. Please check your information.");
                System.out.println("If you don't have an account, please sign up first.");
                System.out.println("\nPress Enter to try again or Press [0] to return to main menu...");

                String choice = scanner.nextLine().trim();
                if (choice.equals("0")) {
                    System.out.println("Returning to main menu...");
                    return;
                }
                // Loop continues to retry login
            }
        }
    }
    
    private void handleStudentSignup() {
        System.out.println("\n--- Student Sign Up ---");
        System.out.println("Enter [0] at any time to cancel and return to main menu.");
        
        if (!TermsAndConditions.acceptTerms(validator)) {
            System.out.println("You must accept terms and conditions to sign up.");
            return;
        }
        
        String lastName = validator.getValidNonEmptyString("Enter last name: ", "Last name");
        if (lastName.equals("0")) {
            System.out.println("Signup cancelled. Returning to main menu...");
            return;
        }
        
        String firstName = validator.getValidNonEmptyString("Enter first name: ", "First name");
        if (firstName.equals("0")) {
            System.out.println("Signup cancelled. Returning to main menu...");
            return;
        }
    // Username is no longer required; login uses student ID + password.

        // Student ID: ensure uniqueness (in-memory only)
        String studentId;
        while (true) {
            studentId = validator.getValidStudentId("Enter student ID (10-12 digits): ");
            if (studentId.equals("0")) {
                System.out.println("Signup cancelled. Returning to main menu...");
                return;
            }
            if (isStudentIdExists(studentId)) {
                System.out.println("Student ID already registered! If this is your ID, please login or use a different ID.");
                continue;
            }
            if(studentId.length() < 11 || studentId.length() > 12) {
                System.out.println("Error: Student ID must be 10-12 digits.");
                continue;
            }
            if(!studentId.matches("\\d+")) {
                System.out.println("Error: Student ID must contain only digits.");
                continue;
            }   
            break;
        }
        
        String password;
        while(true) {
            password = validator.getValidNonEmptyString("Enter password (8-20 chars): ", "Password");
            if (password.equals("0")) {
                System.out.println("Signup cancelled. Returning to main menu...");
                return;
            }
            if(password.length() < 8) {
                System.out.println("Error: Password must be at least 8-20 characters long.");
                continue;
            }
            break;
        }

        String course = validator.getValidCourse("Enter course code: ");
        if (course.equals("0")) {
            System.out.println("Signup cancelled. Returning to main menu...");
            return;
        }
        
        // Gender selection
        String gender = null;
        while (true) {
            String g = validator.getValidNonEmptyString("Enter gender (Male/Female): ", "Gender");
            if (g.equals("0")) {
                System.out.println("Signup cancelled. Returning to main menu...");
                return;
            }
            g = g.trim();
            if (g.equalsIgnoreCase("male") || g.equalsIgnoreCase("m")) { gender = "Male"; break; }
            if (g.equalsIgnoreCase("female") || g.equalsIgnoreCase("f")) { gender = "Female"; break; }
            System.out.println("Invalid gender. Please enter 'Male' or 'Female'.");
        }

    // Create Student with username left blank (studentId is used for login)
    Student newStudent = new Student(studentId, password, course, firstName, lastName, gender);
        FileStorage.addStudent(registeredStudents, newStudent);
        
        System.out.println("Account created successfully!");
        System.out.println("✓ Your account has been saved to the database.");
        System.out.println("Welcome, " + newStudent.getFullName() + "!");
        System.out.println("Student ID: " + studentId);
        System.out.println("Course: " + course);
        System.out.println("\nPlease remember your credentials:");
        System.out.println("   - Student ID: " + studentId);
        System.out.println("You can now login with your credentials.");
    }
    
    private Student findStudentByCredentials(String studentId, String password) {
        for (Student s : registeredStudents) {
            if (s.getPassword().equals(password) && 
                s.getStudentId().equals(studentId)) {
                return s;
            }
        }
        return null;
    }
    
    private boolean isStudentIdExists(String studentId) {
        for (Student s : registeredStudents) {
            if (s.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        MerchSystem system = new MerchSystem();
        system.start();
    }
}