import java.util.Scanner;
import java.util.List;

public class StudentInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private Scanner scanner;
    private Student student;
    
    public StudentInterface(InventoryManager inventoryManager, ReservationManager reservationManager, 
                           Scanner scanner, Student student) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.scanner = scanner;
        this.student = student;
    }
    
    public void showMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("       STUDENT HOMEPAGE");
            System.out.println("   Welcome, " + student.getUsername() + "!");
            System.out.println("=================================");
            System.out.println("[1] Help");
            System.out.println("[2] Reserve a Item");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Your Reservations");
            System.out.println("[0] Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    showHelp();
                    break;
                case 2:
                    reserveItem();
                    break;
                case 3:
                    showStockPage();
                    break;
                case 4:
                    showYourReservations();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void showHelp() {
        System.out.println("\n=================================");
        System.out.println("             HELP");
        System.out.println("=================================");
        System.out.println("\n--- SYSTEM DESCRIPTION ---");
        System.out.println("Welcome to the STI Merch System!");
        System.out.println("This system allows you to browse and reserve school uniforms and merchandise.");
        
        System.out.println("\n--- HOW TO ORDER ---");
        System.out.println("1. Go to 'Stock Page' to browse available items for your course");
        System.out.println("2. Note down the item code of what you want to reserve");
        System.out.println("3. Go to 'Reserve a Item' menu");
        System.out.println("4. Enter the item code and quantity you want");
        System.out.println("5. Confirm your reservation details");
        System.out.println("6. Your reservation will be created with PENDING status");
        System.out.println("7. Check 'Your Reservations' to see the status");
        
        System.out.println("\n--- RESERVATION STATUS ---");
        System.out.println("• PENDING - Your reservation is waiting for admin approval");
        System.out.println("• APPROVED - Your reservation has been approved");
        System.out.println("• COMPLETED - Your items are ready for pickup");
        System.out.println("• CANCELLED - Your reservation has been cancelled");
        
        System.out.println("\n--- NAVIGATION HELP ---");
        System.out.println("• Use number keys to navigate menus");
        System.out.println("• Press [0] to go back to previous menu");
        System.out.println("• You can only view items for your course: " + student.getCourse());
        System.out.println("• All reservations require admin approval");
        
        System.out.println("\n--- AVAILABLE ITEMS ---");
        System.out.println("Uniforms: Polo Shirts, Pants, ID Lace");
        System.out.println("STI Special: Anniversary Clothes, PE Uniforms, Washday Shirts, NSTP Uniforms");
        System.out.println("Sizes: XS, S, M, L, XL, XXL");
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void reserveItem() {
        System.out.println("\n=================================");
        System.out.println("         RESERVE ITEM");
        System.out.println("=================================");
        
        // Show available items for student's course first
        System.out.println("Available items for your course (" + student.getCourse() + "):");
        inventoryManager.displayItemsByCourse(student.getCourse());
        
        // Also show STI Special items
        System.out.println("STI Special Merchandise:");
        inventoryManager.displayItemsByCourse("STI Special");
        
        System.out.print("Enter item code to reserve: ");
        int itemCode = getChoice();
        
        Item item = inventoryManager.findItemByCode(itemCode);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        // Check if item belongs to student's course or is STI Special
        if (!item.getCourse().equalsIgnoreCase(student.getCourse()) && 
            !item.getCourse().equalsIgnoreCase("STI Special")) {
            System.out.println("You can only reserve items for your course or STI Special merchandise.");
            return;
        }
        
        System.out.println("\nSelected item: " + item);
        System.out.print("Enter quantity to reserve: ");
        int quantity = getChoice();
        
        if (quantity <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }
        
        if (item.getQuantity() < quantity) {
            System.out.println("Insufficient stock. Available quantity: " + item.getQuantity());
            return;
        }
        
        // Show reservation summary
        System.out.println("\n--- RESERVATION SUMMARY ---");
        System.out.println("Student Name: " + student.getUsername());
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Course: " + student.getCourse());
        System.out.println("Item: " + item.getName() + " (" + item.getSize() + ")");
        System.out.println("Item Code: " + item.getCode());
        System.out.println("Quantity: " + quantity);
        System.out.println("Price per item: ₱" + item.getPrice());
        System.out.println("Total: ₱" + (item.getPrice() * quantity));
        
        System.out.print("\nConfirm reservation? (y/n): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            if (inventoryManager.reserveItem(itemCode, quantity)) {
                Reservation reservation = reservationManager.createReservation(
                    student.getUsername(), 
                    student.getStudentId(), 
                    student.getCourse(), 
                    itemCode, 
                    item.getName() + " (" + item.getSize() + ")", 
                    quantity
                );
                
                System.out.println("Reservation created successfully!");
                System.out.println("Reservation ID: " + reservation.getReservationId());
                System.out.println("Status: " + reservation.getStatus());
                System.out.println("Wait for admin approval.");
            } else {
                System.out.println("Failed to create reservation. Please try again.");
            }
        } else {
            System.out.println("Reservation cancelled.");
        }
    }
    
    private void showStockPage() {
        System.out.println("\n=================================");
        System.out.println("           STOCK PAGE");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View Your Course Items (" + student.getCourse() + ")");
            System.out.println("[2] View STI Special Merchandise");
            System.out.println("[3] Search Item by Code");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    inventoryManager.displayItemsByCourse(student.getCourse());
                    break;
                case 2:
                    inventoryManager.displayItemsByCourse("STI Special");
                    break;
                case 3:
                    searchItemByCode();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void searchItemByCode() {
        System.out.print("Enter item code: ");
        int code = getChoice();
        
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            // Check if student can view this item
            if (item.getCourse().equalsIgnoreCase(student.getCourse()) || 
                item.getCourse().equalsIgnoreCase("STI Special")) {
                System.out.println("\nItem found:");
                System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
                System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
                System.out.println(item);
            } else {
                System.out.println("You can only view items for your course or STI Special merchandise.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }
    
    private void showYourReservations() {
        System.out.println("\n=================================");
        System.out.println("       YOUR RESERVATIONS");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View All Your Reservations");
            System.out.println("[2] View Pending Reservations");
            System.out.println("[3] Cancel Reservation");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    reservationManager.displayReservationsByStudent(student.getStudentId());
                    break;
                case 2:
                    displayPendingReservations();
                    break;
                case 3:
                    cancelReservation();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void displayPendingReservations() {
        List<Reservation> studentReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        List<Reservation> pending = studentReservations.stream()
                                  .filter(r -> r.getStatus().equals("PENDING"))
                                  .toList();
        
        if (pending.isEmpty()) {
            System.out.println("No pending reservations.");
            return;
        }
        
        System.out.println("\n=== YOUR PENDING RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|----------");
        
        for (Reservation reservation : pending) {
            System.out.println(reservation);
        }
    }
    
    private void cancelReservation() {
        List<Reservation> studentReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        
        if (studentReservations.isEmpty()) {
            System.out.println("You have no reservations to cancel.");
            return;
        }
        
        // Show student's reservations
        System.out.println("\nYour reservations:");
        reservationManager.displayReservationsByStudent(student.getStudentId());
        
        System.out.print("Enter Reservation ID to cancel (0 to go back): ");
        int reservationId = getChoice();
        
        if (reservationId == 0) {
            return;
        }
        
        // Check if reservation belongs to this student
        Reservation reservation = reservationManager.findReservationById(reservationId);
        if (reservation == null || !reservation.getStudentId().equals(student.getStudentId())) {
            System.out.println("Invalid reservation ID or you don't have permission to cancel this reservation.");
            return;
        }
        
        if (reservation.getStatus().equals("COMPLETED")) {
            System.out.println("Cannot cancel a completed reservation.");
            return;
        }
        
        System.out.println("Reservation to cancel: " + reservation);
        System.out.print("Are you sure you want to cancel this reservation? (y/n): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            if (reservationManager.cancelReservation(reservationId)) {
                // Return the reserved quantity back to inventory
                Item item = inventoryManager.findItemByCode(reservation.getItemCode());
                if (item != null) {
                    item.addQuantity(reservation.getQuantity());
                }
                System.out.println("Reservation cancelled successfully.");
            } else {
                System.out.println("Failed to cancel reservation.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }
    
    private int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}