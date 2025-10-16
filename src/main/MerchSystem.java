package main;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final Path USERS_DIR = Path.of("src", "database", "data");
    private static final File USERS_FILE = USERS_DIR.resolve("users.txt").toFile();
    public MerchSystem() {
        this.scanner = new Scanner(System.in);
        this.inventoryManager = new InventoryManager();
        this.reservationManager = new ReservationManager();
        this.validator = new InputValidator(this.scanner);
        this.registeredStudents = new ArrayList<>();
        initializeDefaultInventory();
    }
    
    private void initializeDefaultInventory() {
        int itemCode = 1000;
        
        // ===== INFORMATION TECHNOLOGY & ENGINEERING (BSIT, BSCS, BSCpE) =====
        // Male Uniform
        itemCode++;
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSIT", "S", 50, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSIT", "M", 75, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSIT", "L", 60, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSIT", "XL", 40, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSIT", "S", 40, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSIT", "M", 55, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSIT", "L", 45, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSIT", "XL", 35, 500.00));
        
        // Female Uniform
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Blouse (Female)", "BSIT", "S", 45, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Blouse (Female)", "BSIT", "M", 70, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Blouse (Female)", "BSIT", "L", 55, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Skirt (Female)", "BSIT", "S", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Skirt (Female)", "BSIT", "M", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Skirt (Female)", "BSIT", "L", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Female)", "BSIT", "S", 35, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Female)", "BSIT", "M", 45, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Female)", "BSIT", "L", 35, 500.00));
        
        // BSCS Uniforms (same as BSIT)
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSCS", "S", 45, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSCS", "M", 65, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSCS", "L", 55, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSCS", "M", 50, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Blouse (Female)", "BSCS", "M", 60, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Skirt (Female)", "BSCS", "M", 45, 480.00));
        
        // BSCpE Uniforms
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSCpE", "M", 60, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Polo (Male)", "BSCpE", "L", 50, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng RTW Pants (Male)", "BSCpE", "M", 45, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "IT/Eng Gray 3/4 Blouse (Female)", "BSCpE", "M", 55, 450.00));
        
        // ===== ARTS & SCIENCES (BMMA) =====
        inventoryManager.addItem(new Item(itemCode++, "Arts Gray 3/4 Polo (Male)", "BMMA", "M", 50, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "Arts Gray 3/4 Polo (Male)", "BMMA", "L", 45, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "Arts RTW Pants (Male)", "BMMA", "M", 40, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "Arts Gray 3/4 Blouse (Female)", "BMMA", "M", 55, 450.00));
        inventoryManager.addItem(new Item(itemCode++, "Arts RTW Skirt (Female)", "BMMA", "M", 45, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Arts RTW Pants (Female)", "BMMA", "M", 40, 500.00));
        
        // ===== BUSINESS & MANAGEMENT (BSBA, BSA) =====
        // Male Uniform
        inventoryManager.addItem(new Item(itemCode++, "Business White Polo (Male)", "BSBA", "S", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Polo (Male)", "BSBA", "M", 70, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Polo (Male)", "BSBA", "L", 60, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Slacks (Male)", "BSBA", "S", 45, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Slacks (Male)", "BSBA", "M", 65, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Slacks (Male)", "BSBA", "L", 55, 550.00));
        
        // Female Uniform
        inventoryManager.addItem(new Item(itemCode++, "Business White Blouse (Female)", "BSBA", "S", 45, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Blouse (Female)", "BSBA", "M", 65, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Blouse (Female)", "BSBA", "L", 55, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Vest (Female)", "BSBA", "S", 40, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Vest (Female)", "BSBA", "M", 60, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Vest (Female)", "BSBA", "L", 50, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Pencil Skirt (Female)", "BSBA", "S", 40, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Pencil Skirt (Female)", "BSBA", "M", 55, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Pencil Skirt (Female)", "BSBA", "L", 45, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Slacks (Female)", "BSBA", "M", 50, 550.00));
        
        // Blazer (Optional/Required for presentations)
        inventoryManager.addItem(new Item(itemCode++, "Business Blazer", "BSBA", "S", 30, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Blazer", "BSBA", "M", 45, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Blazer", "BSBA", "L", 35, 650.00));
        
        // BSA Uniforms (same as BSBA)
        inventoryManager.addItem(new Item(itemCode++, "Business White Polo (Male)", "BSA", "M", 60, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Polo (Male)", "BSA", "L", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Slacks (Male)", "BSA", "M", 55, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Business White Blouse (Female)", "BSA", "M", 60, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Vest (Female)", "BSA", "M", 50, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Pencil Skirt (Female)", "BSA", "M", 50, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Business Blazer", "BSA", "M", 40, 650.00));
        
        // ===== HOSPITALITY MANAGEMENT (BSHM) =====
        // Daily Kitchen Attire - Female
        inventoryManager.addItem(new Item(itemCode++, "BSHM White Blouse (Female)", "BSHM", "S", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM White Blouse (Female)", "BSHM", "M", 60, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM White Blouse (Female)", "BSHM", "L", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Blazer (Female)", "BSHM", "S", 35, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Blazer (Female)", "BSHM", "M", 50, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Blazer (Female)", "BSHM", "L", 40, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM RTW Skirt (Female)", "BSHM", "S", 35, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM RTW Skirt (Female)", "BSHM", "M", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM RTW Skirt (Female)", "BSHM", "L", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Yellow Scarf", "BSHM", "One Size", 80, 120.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Beret", "BSHM", "One Size", 70, 150.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Pin", "BSHM", "One Size", 100, 80.00));
        
        // Daily Kitchen Attire - Male
        inventoryManager.addItem(new Item(itemCode++, "BSHM White Polo (Male)", "BSHM", "M", 55, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM White Polo (Male)", "BSHM", "L", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Blazer (Male)", "BSHM", "M", 45, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Blazer (Male)", "BSHM", "L", 40, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Pants (Male)", "BSHM", "M", 50, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Pants (Male)", "BSHM", "L", 45, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "BSHM Necktie", "BSHM", "One Size", 75, 180.00));
        
        // Chef's Laboratory Uniform (Practicum)
        inventoryManager.addItem(new Item(itemCode++, "Chef White Polo", "BSHM", "M", 45, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef White Polo", "BSHM", "L", 40, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef White Polo", "BSHM", "XL", 35, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef White Pants", "BSHM", "M", 45, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef White Pants", "BSHM", "L", 40, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef White Pants", "BSHM", "XL", 35, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef Apron", "BSHM", "One Size", 60, 250.00));
        inventoryManager.addItem(new Item(itemCode++, "Chef Cap", "BSHM", "One Size", 70, 180.00));
        
        // ===== TOURISM MANAGEMENT (BSTM) =====
        // Female Uniform
        inventoryManager.addItem(new Item(itemCode++, "Tourism White Blouse (Female)", "BSTM", "S", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism White Blouse (Female)", "BSTM", "M", 60, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism White Blouse (Female)", "BSTM", "L", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blazer (Female)", "BSTM", "S", 35, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blazer (Female)", "BSTM", "M", 50, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blazer (Female)", "BSTM", "L", 40, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blue Skirt (Female)", "BSTM", "S", 35, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blue Skirt (Female)", "BSTM", "M", 50, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blue Skirt (Female)", "BSTM", "L", 40, 480.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blue Slacks (Female)", "BSTM", "M", 45, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Yellow Scarf", "BSTM", "One Size", 80, 120.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Pin", "BSTM", "One Size", 100, 80.00));
        
        // Male Uniform
        inventoryManager.addItem(new Item(itemCode++, "Tourism White Long Polo (Male)", "BSTM", "M", 55, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism White Long Polo (Male)", "BSTM", "L", 50, 500.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blazer (Male)", "BSTM", "M", 45, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Blazer (Male)", "BSTM", "L", 40, 650.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Trousers (Male)", "BSTM", "M", 50, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Trousers (Male)", "BSTM", "L", 45, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "Tourism Necktie", "BSTM", "One Size", 75, 180.00));
        
        // ===== SENIOR HIGH SCHOOL - DAILY UNIFORM =====
        // ABM, STEM, HUMSS (Standard SHS Uniform)
        String[] shsCourses = {"ABM", "STEM", "HUMSS", "TVL-ICT", "TVL-TO", "TVL-CA"};
        
        for (String course : shsCourses) {
            // Female Daily Uniform
            inventoryManager.addItem(new Item(itemCode++, "SHS White Blouse (Female)", course, "S", 55, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS White Blouse (Female)", course, "M", 80, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS White Blouse (Female)", course, "L", 65, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Female)", course, "S", 50, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Female)", course, "M", 70, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Female)", course, "L", 60, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Skirt", course, "S", 50, 450.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Skirt", course, "M", 70, 450.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Skirt", course, "L", 60, 450.00));
            
            // Male Daily Uniform
            inventoryManager.addItem(new Item(itemCode++, "SHS White Polo (Male)", course, "S", 55, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS White Polo (Male)", course, "M", 80, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS White Polo (Male)", course, "L", 65, 420.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Male)", course, "S", 50, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Male)", course, "M", 70, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Vest (Male)", course, "L", 60, 300.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Pants", course, "S", 50, 480.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Pants", course, "M", 70, 480.00));
            inventoryManager.addItem(new Item(itemCode++, "SHS Checkered Pants", course, "L", 60, 480.00));
        }
        
        // ===== TVL-CA CULINARY CHEF UNIFORM =====
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef White Polo", "TVL-CA", "M", 40, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef White Polo", "TVL-CA", "L", 35, 520.00));
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef White Pants", "TVL-CA", "M", 40, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef White Pants", "TVL-CA", "L", 35, 550.00));
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef Apron", "TVL-CA", "One Size", 55, 250.00));
        inventoryManager.addItem(new Item(itemCode++, "TVL Chef Cap", "TVL-CA", "One Size", 60, 180.00));
        
        // ===== STI SPECIAL ITEMS (Available to ALL students) =====
        // PE Uniform
        inventoryManager.addItem(new Item(itemCode++, "PE White Shirt", "STI Special", "S", 80, 300.00));
        inventoryManager.addItem(new Item(itemCode++, "PE White Shirt", "STI Special", "M", 120, 300.00));
        inventoryManager.addItem(new Item(itemCode++, "PE White Shirt", "STI Special", "L", 100, 300.00));
        inventoryManager.addItem(new Item(itemCode++, "PE White Shirt", "STI Special", "XL", 80, 300.00));
        inventoryManager.addItem(new Item(itemCode++, "PE Blue Jogging Pants", "STI Special", "S", 70, 400.00));
        inventoryManager.addItem(new Item(itemCode++, "PE Blue Jogging Pants", "STI Special", "M", 100, 400.00));
        inventoryManager.addItem(new Item(itemCode++, "PE Blue Jogging Pants", "STI Special", "L", 90, 400.00));
        inventoryManager.addItem(new Item(itemCode++, "PE Blue Jogging Pants", "STI Special", "XL", 70, 400.00));
        
        // NSTP Shirts
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Male)", "STI Special", "S", 60, 320.00));
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Male)", "STI Special", "M", 90, 320.00));
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Male)", "STI Special", "L", 75, 320.00));
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Female)", "STI Special", "S", 60, 320.00));
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Female)", "STI Special", "M", 90, 320.00));
        inventoryManager.addItem(new Item(itemCode++, "NSTP Gray Shirt (Female)", "STI Special", "L", 75, 320.00));
        
        // Special Events
        inventoryManager.addItem(new Item(itemCode++, "Anniversary Shirt", "STI Special", "M", 100, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Anniversary Shirt", "STI Special", "L", 90, 350.00));
        inventoryManager.addItem(new Item(itemCode++, "Washday Shirt", "STI Special", "M", 120, 300.00));
        inventoryManager.addItem(new Item(itemCode++, "Washday Shirt", "STI Special", "L", 100, 300.00));
        
        // Accessories
        inventoryManager.addItem(new Item(itemCode++, "ID Lace", "STI Special", "One Size", 200, 50.00));
        inventoryManager.addItem(new Item(itemCode++, "STI Pin", "STI Special", "One Size", 150, 80.00));
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
            System.out.println("Login successful!");
            AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, validator);
            adminInterface.showMenu();
        } else {
            System.out.println("Invalid credentials.");
            System.out.println("Hint: username='admin', password='admin123'");
        }
    }
    
    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        String username = validator.getValidNonEmptyString("Username: ", "Username");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
        String studentId = validator.getValidStudentId("Student ID: ");
        
        // First try to authenticate against persisted users file
        Student student = findStudentInFile(username, password, studentId);
        // If not found in file, try in-memory registeredStudents (recent signups)
        if (student == null) {
            student = findStudentByCredentials(username, password, studentId);
        }
        if (student != null) {
            System.out.println("Login successful! Welcome " + student.getFullName());
            StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, validator, student);
            studentInterface.showMenu();
        } else {
            System.out.println("Invalid credentials or student ID. Please check your information.");
            System.out.println("If you don't have an account, please sign up first.");
        }
    }

    /**
     * Try to find a matching student record in the users.txt file.
     * Expected line format produced by FileStorage: 
     * "User: <username>, Password: <password>, ID: <studentId>, Course: <course>, Name: <first> <last>"
     */
    private Student findStudentInFile(String username, String password, String studentId) {
        try {
            if (!USERS_FILE.exists()) return null;
            List<String> lines = Files.readAllLines(USERS_FILE.toPath());
            for (String line : lines) {
                if (line == null || line.trim().isEmpty()) continue;
                String u = null, p = null, id = null, course = null, first = null, last = null;
                if (line.contains(":")) {
                    // labeled format: Key: Value, Key: Value, ...
                    String[] parts = line.split(",");
                    for (String part : parts) {
                        String[] kv = part.split(":", 2);
                        if (kv.length < 2) continue;
                        String key = kv[0].trim();
                        String val = kv[1].trim();
                        switch (key) {
                            case "User": u = val; break;
                            case "Password": p = val; break;
                            case "ID": id = val; break;
                            case "Student ID": id = val; break;
                            case "Course": course = val; break;
                            case "Name": {
                                String[] names = val.split(" ", 2);
                                first = names.length > 0 ? names[0].trim() : "";
                                last = names.length > 1 ? names[1].trim() : "";
                                break;
                            }
                            default: break;
                        }
                    }
                } else {
                    // CSV format: username,password,studentId,course,first,last
                    String[] cols = line.split(",");
                    if (cols.length >= 6) {
                        u = cols[0].trim();
                        p = cols[1].trim();
                        id = cols[2].trim();
                        course = cols[3].trim();
                        first = cols[4].trim();
                        last = cols[5].trim();
                    }
                }
                if (u == null || p == null || id == null) continue;
                if (u.equals(username) && p.equals(password) && id.equals(studentId)) {
                    // construct Student and return
                    Student s = new Student(u, p, id, (course != null ? course : ""), (first != null ? first : ""), (last != null ? last : ""));
                    // add to in-memory list so future lookups are faster
                    registeredStudents.add(s);
                    return s;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read users file: " + e.getMessage());
        }
        return null;
    }
    
    private void handleStudentSignup() {
        System.out.println("\n--- Student Sign Up ---");
        
        if (!TermsAndConditions.acceptTerms(validator)) {
            System.out.println("You must accept terms and conditions to sign up.");
            return;
        }
        
        String lastName = validator.getValidNonEmptyString("Enter last name: ", "Last name");
        String firstName = validator.getValidNonEmptyString("Enter first name: ", "First name");
        String username = validator.getValidNonEmptyString("Enter username: ", "Username");
        
        if (isUsernameExists(username)) {
            System.out.println("Username already exists!");
            return;
        }
        
        String studentId = validator.getValidStudentId("Enter student ID (6-12 digits): ");
        
        if (isStudentIdExists(studentId)) {
            System.out.println("Student ID already registered!");
            return;
        }
        
        String password = validator.getValidNonEmptyString("Enter password (6-20 chars): ", "Password");
        String course = validator.getValidCourse("Enter course code: ");
        
        Student newStudent = new Student(username, password, studentId, course, firstName, lastName);
        registeredStudents.add(newStudent);
        // persist to simple file storage
        boolean saved = utils.FileStorage.saveStudent(newStudent);
        if (!saved) {
            System.out.println("Warning: failed to persist new user to file.");
        }
        
        System.out.println("Account created successfully!");
        System.out.println("Welcome, " + newStudent.getFullName() + "!");
        System.out.println("Student ID: " + studentId);
        System.out.println("Course: " + course);
        System.out.println("\nPlease remember your credentials:");
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