import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class MerchSystem {
    private Scanner scanner;
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private InputValidator validator;
    private List<Student> registeredStudents;
    
    public MerchSystem() {
        scanner = new Scanner(System.in);
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager();
        validator = new InputValidator(scanner);
        registeredStudents = new ArrayList<>();
        initializeDefaultInventory();
    }
    
    public void start() {
        System.out.println("=================================");
        System.out.println("       MERCH SYSTEM");
        System.out.println("=================================");
        
        while (true) {
            showMainLogin();
            int choice = validator.getValidInteger("", 0, 3);
            
            switch (choice) {
                case 1:
                    handleAdminLogin();
                    break;
                case 2:
                    handleStudentLogin();
                    break;
                case 3:
                    handleStudentSignup();
                    break;
                case 0:
                    System.out.println("Thank you for using Merch System!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void showMainLogin() {
        System.out.println("\n=================================");
        System.out.println("           LOGIN");
        System.out.println("=================================");
        System.out.println("[1] Admin Login");
        System.out.println("[2] Student Login");
        System.out.println("[3] Student Sign Up");
        System.out.println("[0] Exit");
        System.out.print("Enter your choice: ");
    }
    
    private void handleAdminLogin() {
        System.out.println("\n--- Admin Login ---");
        String username = validator.getValidNonEmptyString("Username: ", "Username");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
        
        Admin admin = new Admin(username, password);
        if (admin.authenticate()) {
            AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, validator);
            adminInterface.showMenu();
        } else {
            System.out.println("❌ Invalid credentials. Please try again.");
            System.out.println("💡 Hint: Default admin credentials are username: 'admin', password: 'admin123'");
        }
    }
    
    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        String username = validator.getValidNonEmptyString("Username: ", "Username");
        String password = validator.getValidNonEmptyString("Password: ", "Password");
        String studentId = validator.getValidStudentId("Student ID: ");
        String course = validator.getValidCourse("Course (e.g., BSIT, BSCS, STEM): ");
        
        // Check if student is registered
        Student foundStudent = findRegisteredStudent(username, studentId);
        if (foundStudent != null && foundStudent.getPassword().equals(password)) {
            StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, validator, foundStudent);
            studentInterface.showMenu();
        } else {
            Student student = new Student(username, password, studentId, course);
            if (student.authenticate()) {
                StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, validator, student);
                studentInterface.showMenu();
            } else {
                System.out.println("❌ Invalid credentials. Please check your information and try again.");
            }
        }
    }
    
    private void handleStudentSignup() {
        System.out.println("\n--- Student Sign Up ---");
        System.out.println("📝 Create a new student account");
        
        // Display and accept terms and conditions
        if (!TermsAndConditions.acceptTerms(validator)) {
            System.out.println("⚠️ You must accept the Terms and Conditions to create an account.");
            return;
        }
        
        String username = validator.getValidNonEmptyString("Choose a username: ", "Username");
        
        // Check if username already exists
        if (isUsernameExists(username)) {
            System.out.println("❌ Username already exists. Please choose a different username.");
            return;
        }
        
        String password;
        while (true) {
            password = validator.getValidPassword("Create a password (6-20 characters): ");
            if (validator.confirmPassword(password)) {
                break;
            }
        }
        
        String studentId = validator.getValidStudentId("Student ID (6-12 digits): ");
        
        // Check if student ID already exists
        if (isStudentIdExists(studentId)) {
            System.out.println("❌ Student ID already registered. Please contact admin if this is an error.");
            return;
        }
        
        String course = validator.getValidCourse("Course (e.g., BSIT, BSCS, STEM): ");
        
        Student newStudent = new Student(username, password, studentId, course);
        registeredStudents.add(newStudent);
        
        System.out.println("✅ Account created successfully!");
        System.out.println("📧 Welcome to STI Merch System, " + username + "!");
        System.out.println("🎓 Course: " + course);
        System.out.println("🆔 Student ID: " + studentId);
        System.out.println("You can now login with your credentials.");
    }
    
    private Student findRegisteredStudent(String username, String studentId) {
        for (Student student : registeredStudents) {
            if (student.getUsername().equals(username) && student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }
    
    private boolean isUsernameExists(String username) {
        for (Student student : registeredStudents) {
            if (student.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isStudentIdExists(String studentId) {
        for (Student student : registeredStudents) {
            if (student.getStudentId().equals(studentId)) {
                return true;
            }
        }
        return false;
    }
    
    private int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private void initializeDefaultInventory() {
        // Initialize with default uniforms and merchandise
        String[] shsCourses = InputValidator.getSHSCourses();
        String[] tertiaryCourses = InputValidator.getTertiaryCourses();
        String[] sizes = {"XS", "S", "M", "L", "XL", "XXL"};
        
        int itemCode = 1001;
        
        // Add uniforms for SHS courses
        for (String course : shsCourses) {
            for (String size : sizes) {
                inventoryManager.addItem(new Item(itemCode++, course + " Polo Shirt", course, size, 15, 450.0));
                inventoryManager.addItem(new Item(itemCode++, course + " Pants", course, size, 10, 350.0));
            }
            inventoryManager.addItem(new Item(itemCode++, course + " ID Lace", course, "One Size", 50, 25.0));
        }
        
        // Add uniforms for Tertiary courses
        for (String course : tertiaryCourses) {
            for (String size : sizes) {
                inventoryManager.addItem(new Item(itemCode++, course + " Polo Shirt", course, size, 15, 450.0));
                inventoryManager.addItem(new Item(itemCode++, course + " Pants", course, size, 10, 350.0));
            }
            inventoryManager.addItem(new Item(itemCode++, course + " ID Lace", course, "One Size", 50, 25.0));
        }
        
        // Add STI special merchandise
        String[] specialMerch = {"Anniversary Clothes", "PE Uniform", "Washday Shirt", "NSTP Uniform"};
        for (String merch : specialMerch) {
            for (String size : sizes) {
                inventoryManager.addItem(new Item(itemCode++, "STI " + merch, "STI Special", size, 20, 300.0));
            }
        }
    }
    
    public static void main(String[] args) {
        MerchSystem system = new MerchSystem();
        system.start();
    }
}