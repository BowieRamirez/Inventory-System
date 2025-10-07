package main;

import student.Student;
import student.StudentInterface;
import admin.Admin;
import admin.AdminInterface;
import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Item;
import inventory.Reservation;
import utils.InputValidator;
import utils.TermsAndConditions;

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
        initializeDefaultInventory();
    }
    
    private void initializeDefaultInventory() {
        inventoryManager.addItem(new Item(1001, "BSIT Polo Shirt", "BSIT", "S", 50, 450.00));
        inventoryManager.addItem(new Item(1002, "BSIT Polo Shirt", "BSIT", "M", 75, 450.00));
        inventoryManager.addItem(new Item(1003, "BSIT Polo Shirt", "BSIT", "L", 60, 450.00));
        inventoryManager.addItem(new Item(1004, "BSIT Pants", "BSIT", "M", 40, 500.00));
        inventoryManager.addItem(new Item(1005, "BSIT ID Lace", "BSIT", "One Size", 100, 50.00));
        
        inventoryManager.addItem(new Item(2001, "BSCS Polo Shirt", "BSCS", "S", 45, 450.00));
        inventoryManager.addItem(new Item(2002, "BSCS Polo Shirt", "BSCS", "M", 65, 450.00));
        inventoryManager.addItem(new Item(2003, "BSCS Pants", "BSCS", "L", 35, 500.00));
        
        inventoryManager.addItem(new Item(3001, "STEM Polo Shirt", "STEM", "S", 55, 420.00));
        inventoryManager.addItem(new Item(3002, "STEM Polo Shirt", "STEM", "M", 80, 420.00));
        inventoryManager.addItem(new Item(3003, "STEM Pants", "STEM", "M", 50, 480.00));
        
        inventoryManager.addItem(new Item(9001, "Anniversary Shirt", "STI Special", "M", 100, 350.00));
        inventoryManager.addItem(new Item(9002, "PE Uniform", "STI Special", "L", 90, 400.00));
        inventoryManager.addItem(new Item(9003, "Washday Shirt", "STI Special", "M", 120, 300.00));
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
        
        Admin admin = new Admin(username, password);
        if (admin.authenticate()) {
            System.out.println(" Login successful!");
            AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, validator);
            adminInterface.showMenu();
        } else {
            System.out.println(" Invalid credentials.");
            System.out.println(" Hint: username='admin', password='admin123'");
        }
    }
    
    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        String username = validator.getValidNonEmptyString("Username: ", "Username");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
        String studentId = validator.getValidStudentId("Student ID: ");
        
        Student student = findStudentByCredentials(username, password, studentId);
        if (student != null) {
            System.out.println(" Login successful! Welcome " + student.getFullName());
            StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, validator, student);
            studentInterface.showMenu();
        } else {
            System.out.println(" Invalid credentials or student ID. Please check your information.");
            System.out.println(" If you don't have an account, please sign up first.");
        }
    }
    
    private void handleStudentSignup() {
        System.out.println("\n--- Student Sign Up ---");
        
        if (!TermsAndConditions.acceptTerms(validator)) {
            System.out.println(" You must accept terms and conditions to sign up.");
            return;
        }
        
        String lastName = validator.getValidNonEmptyString("Enter last name: ", "Last name");
        String firstName = validator.getValidNonEmptyString("Enter first name: ", "First name");
        String username = validator.getValidNonEmptyString("Enter username: ", "Username");
        
        if (isUsernameExists(username)) {
            System.out.println(" Username already exists!");
            return;
        }
        
        String studentId = validator.getValidStudentId("Enter student ID (6-12 digits): ");
        
        if (isStudentIdExists(studentId)) {
            System.out.println(" Student ID already registered!");
            return;
        }
        
        String password = validator.getValidNonEmptyString("Enter password (6-20 chars): ", "Password");
        String course = validator.getValidCourse("Enter course code: ");
        
        Student newStudent = new Student(username, password, studentId, course, firstName, lastName);
        registeredStudents.add(newStudent);
        
        System.out.println(" Account created successfully!");
        System.out.println("Welcome, " + newStudent.getFullName() + "!");
        System.out.println("Student ID: " + studentId);
        System.out.println("Course: " + course);
        System.out.println("\n Please remember your credentials:");
        System.out.println("   - Username: " + username);
        System.out.println("   - Student ID: " + studentId);
        System.out.println("You can now login with your credentials.");
    }
    
    private Student findStudentByCredentials(String username, String password, String studentId) {
        for (Student s : registeredStudents) {
            if (s.getUsername().equals(username) && 
                s.getPassword().equals(password) && 
                s.getStudentId().equals(studentId)) {
                return s;
            }
        }
        return null;
    }
    
    private boolean isUsernameExists(String username) {
        for (Student s : registeredStudents) {
            if (s.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
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