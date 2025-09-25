import java.util.Scanner;

public class MerchSystem {
    private Scanner scanner;
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    
    public MerchSystem() {
        scanner = new Scanner(System.in);
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager();
        initializeDefaultInventory();
    }
    
    public void start() {
        System.out.println("=================================");
        System.out.println("       MERCH SYSTEM");
        System.out.println("=================================");
        
        while (true) {
            showMainLogin();
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    handleAdminLogin();
                    break;
                case 2:
                    handleStudentLogin();
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
        System.out.println("[1] Admin");
        System.out.println("[2] Student");
        System.out.println("[0] Exit");
        System.out.print("Enter your choice: ");
    }
    
    private void handleAdminLogin() {
        System.out.println("\n--- Admin Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        Admin admin = new Admin(username, password);
        if (admin.authenticate()) {
            AdminInterface adminInterface = new AdminInterface(inventoryManager, reservationManager, scanner);
            adminInterface.showMenu();
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
    }
    
    private void handleStudentLogin() {
        System.out.println("\n--- Student Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Student ID: ");
        String studentId = scanner.nextLine();
        System.out.print("Course: ");
        String course = scanner.nextLine();
        
        Student student = new Student(username, password, studentId, course);
        if (student.authenticate()) {
            StudentInterface studentInterface = new StudentInterface(inventoryManager, reservationManager, scanner, student);
            studentInterface.showMenu();
        } else {
            System.out.println("Invalid credentials. Please try again.");
        }
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
        String[] courses = {"IT", "CS", "Tourism Management", "Multi Media Arts", 
                           "HRM", "Accountancy", "Business Administration", "Comp E"};
        String[] sizes = {"XS", "S", "M", "L", "XL", "XXL"};
        
        int itemCode = 1001;
        
        // Add uniforms for each course
        for (String course : courses) {
            for (String size : sizes) {
                inventoryManager.addItem(new Item(itemCode++, course + " Polo Shirt", course, size, 15, 450.0));
                inventoryManager.addItem(new Item(itemCode++, course + " Pants", course, size, 10, 350.0));
                inventoryManager.addItem(new Item(itemCode++, course + " ID Lace", course, "One Size", 50, 25.0));
            }
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