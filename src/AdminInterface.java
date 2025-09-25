import java.util.Scanner;
import java.util.List;

public class AdminInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private Scanner scanner;
    
    public AdminInterface(InventoryManager inventoryManager, ReservationManager reservationManager, Scanner scanner) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.scanner = scanner;
    }
    
    public void showMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("        ADMIN HOMEPAGE");
            System.out.println("=================================");
            System.out.println("[1] Help");
            System.out.println("[2] Users Reservations");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Add or Remove Item");
            System.out.println("[0] Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    showHelp();
                    break;
                case 2:
                    showUserReservations();
                    break;
                case 3:
                    showStockPage();
                    break;
                case 4:
                    showAddRemoveMenu();
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
        System.out.println("This is the STI Merch System for managing school uniforms and merchandise.");
        System.out.println("The system handles inventory management and student reservations.");
        System.out.println("\nCourses supported:");
        System.out.println("• IT (Information Technology)");
        System.out.println("• CS (Computer Science)");
        System.out.println("• Tourism Management");
        System.out.println("• Multi Media Arts");
        System.out.println("• HRM (Hotel & Restaurant Management)");
        System.out.println("• Accountancy");
        System.out.println("• Business Administration");
        System.out.println("• Comp E (Computer Engineering)");
        System.out.println("\nSTI Special Merchandise:");
        System.out.println("• Anniversary Clothes");
        System.out.println("• PE Uniforms");
        System.out.println("• Washday Shirts");
        System.out.println("• NSTP Uniforms");
        
        System.out.println("\n--- HOW TO ORDER (For Students) ---");
        System.out.println("1. Login as Student");
        System.out.println("2. Go to 'Reserve a Item' menu");
        System.out.println("3. Enter item code, quantity, and personal details");
        System.out.println("4. Confirm reservation");
        System.out.println("5. Check reservation status in 'Your Reservations'");
        
        System.out.println("\n--- NAVIGATION HELP ---");
        System.out.println("• Use number keys to navigate menus");
        System.out.println("• Press [0] to go back to previous menu");
        System.out.println("• Admin can manage inventory and view all reservations");
        System.out.println("• Students can only view their course items and make reservations");
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
    
    private void showUserReservations() {
        System.out.println("\n=================================");
        System.out.println("       USER RESERVATIONS");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View All Reservations");
            System.out.println("[2] View Pending Reservations");
            System.out.println("[3] Update Reservation Status");
            System.out.println("[4] Cancel Reservation");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    reservationManager.displayAllReservations();
                    break;
                case 2:
                    displayPendingReservations();
                    break;
                case 3:
                    updateReservationStatus();
                    break;
                case 4:
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
        List<Reservation> pending = reservationManager.getPendingReservations();
        if (pending.isEmpty()) {
            System.out.println("No pending reservations.");
            return;
        }
        
        System.out.println("\n=== PENDING RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|----------");
        
        for (Reservation reservation : pending) {
            System.out.println(reservation);
        }
    }
    
    private void updateReservationStatus() {
        System.out.print("Enter Reservation ID: ");
        int reservationId = getChoice();
        
        Reservation reservation = reservationManager.findReservationById(reservationId);
        if (reservation == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        System.out.println("Current reservation: " + reservation);
        System.out.println("\nAvailable statuses:");
        System.out.println("[1] PENDING");
        System.out.println("[2] APPROVED");
        System.out.println("[3] COMPLETED");
        System.out.println("[4] CANCELLED");
        System.out.print("Choose new status: ");
        
        int statusChoice = getChoice();
        String newStatus = "";
        
        switch (statusChoice) {
            case 1: newStatus = "PENDING"; break;
            case 2: newStatus = "APPROVED"; break;
            case 3: newStatus = "COMPLETED"; break;
            case 4: newStatus = "CANCELLED"; break;
            default:
                System.out.println("Invalid status choice.");
                return;
        }
        
        if (reservationManager.updateReservationStatus(reservationId, newStatus)) {
            System.out.println("Reservation status updated to: " + newStatus);
        } else {
            System.out.println("Failed to update reservation status.");
        }
    }
    
    private void cancelReservation() {
        System.out.print("Enter Reservation ID to cancel: ");
        int reservationId = getChoice();
        
        if (reservationManager.cancelReservation(reservationId)) {
            System.out.println("Reservation cancelled successfully.");
        } else {
            System.out.println("Failed to cancel reservation. It may not exist or already be completed.");
        }
    }
    
    private void showStockPage() {
        System.out.println("\n=================================");
        System.out.println("           STOCK PAGE");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View All Stock");
            System.out.println("[2] View Stock by Course");
            System.out.println("[3] Search Item by Code");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    inventoryManager.displayAllItems();
                    break;
                case 2:
                    viewStockByCourse();
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
    
    private void viewStockByCourse() {
        System.out.println("\nAvailable courses:");
        List<String> courses = inventoryManager.getAvailableCourses();
        for (int i = 0; i < courses.size(); i++) {
            System.out.println("[" + (i + 1) + "] " + courses.get(i));
        }
        System.out.print("Enter course number: ");
        
        int courseChoice = getChoice();
        if (courseChoice > 0 && courseChoice <= courses.size()) {
            String selectedCourse = courses.get(courseChoice - 1);
            inventoryManager.displayItemsByCourse(selectedCourse);
        } else {
            System.out.println("Invalid course choice.");
        }
    }
    
    private void searchItemByCode() {
        System.out.print("Enter item code: ");
        int code = getChoice();
        
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            System.out.println("\nItem found:");
            System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
            System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
            System.out.println(item);
        } else {
            System.out.println("Item not found.");
        }
    }
    
    private void showAddRemoveMenu() {
        System.out.println("\n=================================");
        System.out.println("      ADD OR REMOVE ITEM");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] Add New Item");
            System.out.println("[2] Remove Item");
            System.out.println("[3] Update Item Quantity");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = getChoice();
            
            switch (choice) {
                case 1:
                    addNewItem();
                    break;
                case 2:
                    removeItem();
                    break;
                case 3:
                    updateItemQuantity();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void addNewItem() {
        System.out.println("\n--- ADD NEW ITEM ---");
        System.out.print("Enter item code: ");
        int code = getChoice();
        
        // Check if item code already exists
        if (inventoryManager.findItemByCode(code) != null) {
            System.out.println("Item with this code already exists.");
            return;
        }
        
        System.out.print("Enter item name: ");
        String name = scanner.nextLine();
        System.out.print("Enter course: ");
        String course = scanner.nextLine();
        System.out.print("Enter size: ");
        String size = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = getChoice();
        System.out.print("Enter price: ");
        double price = getDoubleInput();
        
        Item newItem = new Item(code, name, course, size, quantity, price);
        inventoryManager.addItem(newItem);
        
        System.out.println("Item added successfully!");
        System.out.println(newItem);
    }
    
    private void removeItem() {
        System.out.print("Enter item code to remove: ");
        int code = getChoice();
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        System.out.println("Item to be removed:");
        System.out.println(item);
        System.out.print("Are you sure? (y/n): ");
        String confirm = scanner.nextLine().toLowerCase();
        
        if (confirm.equals("y") || confirm.equals("yes")) {
            if (inventoryManager.removeItem(code)) {
                System.out.println("Item removed successfully.");
            } else {
                System.out.println("Failed to remove item.");
            }
        } else {
            System.out.println("Removal cancelled.");
        }
    }
    
    private void updateItemQuantity() {
        System.out.print("Enter item code: ");
        int code = getChoice();
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        System.out.println("Current item: " + item);
        System.out.print("Enter new quantity: ");
        int newQuantity = getChoice();
        
        if (inventoryManager.updateItemQuantity(code, newQuantity)) {
            System.out.println("Item quantity updated successfully.");
        } else {
            System.out.println("Failed to update item quantity.");
        }
    }
    
    private int getChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}