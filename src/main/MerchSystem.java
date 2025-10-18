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
    private InputValidator validator;
    private List<Student> registeredStudents;
    
    public MerchSystem() {
        this.scanner = new Scanner(System.in);
        this.inventoryManager = new InventoryManager();
        this.reservationManager = new ReservationManager();
        this.validator = new InputValidator(this.scanner);
        this.registeredStudents = new ArrayList<>();
        // Removed student loading from file - accounts only exist in memory now
        loadInventoryFromFile();
    }
    
    private void loadInventoryFromFile() {
        List<Item> loadedItems = FileStorage.loadItems();
        for (Item item : loadedItems) {
            inventoryManager.addItem(item);
        }
    }
    
    public void start() {
        System.out.println("=================================");
        System.out.println("     STI MERCH SYSTEM");
        System.out.println("=================================");
        
        while (true) {
            showMainLogin();
            int choice = validator.getValidInteger("Enter your choice: ", 0, 3);
            
            switch (choice) {
                case 0:
                    System.out.println("Thank you for using STI Merch System!");
                    System.exit(0);
                    break;
                case 1:
                    handleAdminLogin();
                    break;
                case 2:
                    handleStudentLogin();
                    break;
                case 3:
                    handleStudentSignup();
                    break;
            }
        }
    }
    
    private void showMainLogin() {
        System.out.println("\n=================================");
        System.out.println("           MAIN MENU");
        System.out.println("=================================");
        System.out.println("[1] Admin Login");
        System.out.println("[2] Student Login");
        System.out.println("[3] Student Sign Up");
        System.out.println("[0] Exit");
    }
    
    private void handleAdminLogin() {
        System.out.println("\n--- Admin Login ---");
        String username = validator.getValidNonEmptyString("Username: ", "Username");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
        
        // Check Admin
        Admin admin = new Admin(username, password);
        if (admin.authenticate()) {
            System.out.println("Login successful!");
            AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, validator, registeredStudents);
            adminInterface.showMenu();
            return;
        }
        
        // Check Staff
        Staff staff = new Staff(username, password);
        if (staff.authenticate()) {
            System.out.println("Login successful!");
            StaffInterface staffInterface = new StaffInterface(inventoryManager, reservationManager, validator);
            staffInterface.showMenu();
            return;
        }
        
        // Check Cashier
        Cashier cashier = new Cashier(username, password);
        if (cashier.authenticate()) {
            System.out.println("Login successful!");
            CashierInterface cashierInterface = new CashierInterface(reservationManager, validator);
            cashierInterface.showMenu();
            return;
        }
        
        System.out.println("Invalid credentials.");
    }
    
    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        String studentId = validator.getValidStudentId("Student ID: ");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
       
        // Authenticate against in-memory registered students
        Student student = findStudentByCredentials(studentId, password);
        if (student != null) {
            System.out.println("Login successful! Welcome " + student.getFullName());
            StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, validator, student);
            studentInterface.showMenu();
        } else {
            System.out.println("Invalid credentials or student ID. Please check your information.");
            System.out.println("If you don't have an account, please sign up first.");
        }
    }
    
    private void handleStudentSignup() {
        System.out.println("\n--- Student Sign Up ---");
        
        if (!TermsAndConditions.acceptTerms(validator)) {
            System.out.println("You must accept terms and conditions to sign up.");
            return;
        }
        
        String lastName = validator.getValidNonEmptyString("Enter last name: ", "Last name");
        String firstName = validator.getValidNonEmptyString("Enter first name: ", "First name");
    // Username is no longer required; login uses student ID + password.

        // Student ID: ensure uniqueness (in-memory only)
        String studentId;
        while (true) {
            studentId = validator.getValidStudentId("Enter student ID (6-12 digits): ");
            if (isStudentIdExists(studentId)) {
                System.out.println("Student ID already registered! If this is your ID, please login or use a different ID.");
                continue;
            }
            break;
        }
        
        String password = validator.getValidNonEmptyString("Enter password (6-20 chars): ", "Password");
        String course = validator.getValidCourse("Enter course code: ");
        
        // Gender selection
        String gender = null;
        while (true) {
            String g = validator.getValidNonEmptyString("Enter gender (Male/Female): ", "Gender");
            g = g.trim();
            if (g.equalsIgnoreCase("male") || g.equalsIgnoreCase("m")) { gender = "Male"; break; }
            if (g.equalsIgnoreCase("female") || g.equalsIgnoreCase("f")) { gender = "Female"; break; }
            System.out.println("Invalid gender. Please enter 'Male' or 'Female'.");
        }

    // Create Student with username left blank (studentId is used for login)
    Student newStudent = new Student(studentId, password, course, firstName, lastName, gender);
        registeredStudents.add(newStudent);
        
        System.out.println("Account created successfully!");
        System.out.println("NOTE: Your account is stored in memory only and will be lost when the system exits.");
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