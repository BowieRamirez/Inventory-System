import java.util.Scanner;
import java.util.List;

public class AdminInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private InputValidator validator;
    
    public AdminInterface(InventoryManager inventoryManager, ReservationManager reservationManager, InputValidator validator) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.validator = validator;
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
            System.out.println("[5] Logout");
            System.out.println("[0] Exit System");
            System.out.print("Enter your choice: ");
            
            int choice = validator.getValidInteger("", 0, 5);
            
            switch (choice) {
                case 1:
                    showAdminHelp();
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
                case 5:
                    System.out.println("👋 Logged out successfully!");
                    return;
                case 0:
                    System.out.println("🔚 Exiting system...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }
    
    private void showAdminHelp() {
        System.out.println("\n=================================");
        System.out.println("         ADMIN HELP DESK");
        System.out.println("=================================");
        System.out.println("\n--- SYSTEM ADMINISTRATION GUIDE ---");
        System.out.println("Welcome to the STI Merch System Admin Panel!");
        System.out.println("This system manages school uniform and merchandise inventory.");
        System.out.println("\n🎓 SUPPORTED PROGRAMS:");
        System.out.println("Senior High School:");
        String[] shsCourses = InputValidator.getSHSCourses();
        for (String course : shsCourses) {
            System.out.println("  • " + course);
        }
        System.out.println("\nTertiary Programs:");
        String[] tertiaryCourses = InputValidator.getTertiaryCourses();
        for (String course : tertiaryCourses) {
            System.out.println("  • " + course);
        }
        System.out.println("\n📦 STI SPECIAL MERCHANDISE:");
        System.out.println("  • Anniversary Clothes");
        System.out.println("  • PE Uniforms");
        System.out.println("  • Washday Shirts");
        System.out.println("  • NSTP Uniforms");
        
        System.out.println("\n--- ADMIN RESPONSIBILITIES ---");
        System.out.println("1. 👥 RESERVATION MANAGEMENT");
        System.out.println("   • Review pending student reservations");
        System.out.println("   • Approve or reject reservation requests");
        System.out.println("   • Update reservation status (PENDING → APPROVED → COMPLETED)");
        System.out.println("   • Monitor reservation timeline and pickup schedules");
        
        System.out.println("\n2. 📋 INVENTORY CONTROL");
        System.out.println("   • Monitor stock levels across all courses");
        System.out.println("   • Add new items when restocking");
        System.out.println("   • Remove discontinued or damaged items");
        System.out.println("   • Update quantities and pricing");
        
        System.out.println("\n3. 🔍 REPORTING AND MONITORING");
        System.out.println("   • View detailed inventory reports");
        System.out.println("   • Track reservation patterns by course");
        System.out.println("   • Monitor popular items and stock turnover");
        
        System.out.println("\n--- NAVIGATION GUIDE ---");
        System.out.println("🔢 Use number keys [1-5] to navigate menus");
        System.out.println("🔙 Press [0] to go back to previous menu");
        System.out.println("🚪 Press [5] to logout safely");
        System.out.println("🛑 Emergency exit: Press [0] in main menu");
        
        System.out.println("\n--- BEST PRACTICES ---");
        System.out.println("✅ Regularly review pending reservations");
        System.out.println("✅ Maintain accurate stock quantities");
        System.out.println("✅ Approve/reject reservations promptly");
        System.out.println("✅ Keep course codes consistent (e.g., BSIT, BSCS)");
        System.out.println("✅ Logout when finished for security");
        
        System.out.println("\nPress Enter to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Handle input exception
        }
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
            System.out.println("[0] Back to Admin Menu");
            
            int choice = validator.getValidInteger("Enter your choice: ", 0, 4);
            
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
                    System.out.println("❌ Invalid choice. Please try again.");
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
        int reservationId = validator.getValidInteger("Enter Reservation ID: ", 1000, 9999);
        
        Reservation reservation = reservationManager.findReservationById(reservationId);
        if (reservation == null) {
            System.out.println("❌ Reservation not found.");
            return;
        }
        
        System.out.println("Current reservation: " + reservation);
        System.out.println("\nAvailable statuses:");
        System.out.println("[1] PENDING");
        System.out.println("[2] APPROVED");
        System.out.println("[3] COMPLETED");
        System.out.println("[4] CANCELLED");
        
        int statusChoice = validator.getValidInteger("Choose new status: ", 1, 4);
        String newStatus = "";
        
        switch (statusChoice) {
            case 1: newStatus = "PENDING"; break;
            case 2: newStatus = "APPROVED"; break;
            case 3: newStatus = "COMPLETED"; break;
            case 4: newStatus = "CANCELLED"; break;
        }
        
        // Add confirmation
        System.out.println("\n⚠️ CONFIRMATION REQUIRED");
        System.out.println("Change status from '" + reservation.getStatus() + "' to '" + newStatus + "'?");
        if (validator.getValidYesNo("Confirm status update")) {
            if (reservationManager.updateReservationStatus(reservationId, newStatus)) {
                System.out.println("✅ Reservation status updated to: " + newStatus);
            } else {
                System.out.println("❌ Failed to update reservation status.");
            }
        } else {
            System.out.println("📝 Status update cancelled.");
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
            System.out.println("[0] Back to Admin Menu");
            
            int choice = validator.getValidInteger("Enter your choice: ", 0, 3);
            
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
                    System.out.println("❌ Invalid choice. Please try again.");
            }
        }
    }
    
    private void viewStockByCourse() {
        while (true) {
            System.out.println("\n📚 Available courses:");
            List<String> courses = inventoryManager.getAvailableCourses();
            for (int i = 0; i < courses.size(); i++) {
                System.out.println("[" + (i + 1) + "] " + courses.get(i));
            }
            System.out.println("[0] Back to Stock Menu");
            
            int courseChoice = validator.getValidInteger("Enter course number: ", 0, courses.size());
            if (courseChoice == 0) {
                return;
            }
            
            String selectedCourse = courses.get(courseChoice - 1);
            inventoryManager.displayItemsByCourse(selectedCourse);
        }
    }
    
    private void searchItemByCode() {
        int code = validator.getValidInteger("Enter item code: ", 1000, 9999);
        
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
            
            int choice = validator.getValidInteger("Enter your choice: ", 0, 3);
            
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
        int code = validator.getValidInteger("Enter item code (1000-9999): ", 1000, 9999);
        
        // Check if item code already exists
        if (inventoryManager.findItemByCode(code) != null) {
            System.out.println("❌ Item with code " + code + " already exists.");
            return;
        }
        
        String name = validator.getValidNonEmptyString("Enter item name: ", "Item name");
        String course = validator.getValidCourse("Enter course code (e.g., BSIT, STEM): ");
        String size = validator.getValidSize("Enter size: ");
        int quantity = validator.getValidInteger("Enter quantity (1-1000): ", 1, 1000);
        double price = validator.getValidPrice("Enter price (₱): ");
        
        Item newItem = new Item(code, name, course, size, quantity, price);
        
        // Show confirmation
        System.out.println("\n⚠️ CONFIRMATION REQUIRED");
        System.out.println("Add this item to inventory?");
        System.out.println(newItem);
        
        if (validator.getValidYesNo("Confirm addition")) {
            inventoryManager.addItem(newItem);
            System.out.println("✅ Item added successfully!");
        } else {
            System.out.println("📝 Item addition cancelled.");
        }
    }
    
    private void removeItem() {
        int code = validator.getValidInteger("Enter item code to remove: ", 1000, 9999);
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("❌ Item not found.");
            return;
        }
        
        System.out.println("Item to be removed:");
        System.out.println(item);
        
        if (validator.getValidYesNo("Are you sure you want to remove this item")) {
            if (inventoryManager.removeItem(code)) {
                System.out.println("✅ Item removed successfully.");
            } else {
                System.out.println("❌ Failed to remove item.");
            }
        } else {
            System.out.println("📝 Removal cancelled.");
        }
    }
    
    private void updateItemQuantity() {
        int code = validator.getValidInteger("Enter item code: ", 1000, 9999);
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("❌ Item not found.");
            return;
        }
        
        System.out.println("Current item: " + item);
        int newQuantity = validator.getValidInteger("Enter new quantity (0-1000): ", 0, 1000);
        
        // Show confirmation
        System.out.println("\n⚠️ STOCK UPDATE CONFIRMATION");
        System.out.println("Change quantity from " + item.getQuantity() + " to " + newQuantity + "?");
        System.out.println("Item: " + item.getName() + " (" + item.getSize() + ")");
        
        if (validator.getValidYesNo("Confirm stock update")) {
            if (inventoryManager.updateItemQuantity(code, newQuantity)) {
                System.out.println("✅ Item quantity updated successfully.");
                System.out.println("New stock: " + newQuantity + " units");
            } else {
                System.out.println("❌ Failed to update item quantity.");
            }
        } else {
            System.out.println("📝 Stock update cancelled.");
        }
    }
    
    private void cancelReservation() {
        int reservationId = validator.getValidInteger("Enter Reservation ID to cancel: ", 1000, 9999);
        
        if (reservationManager.cancelReservation(reservationId)) {
            System.out.println("✅ Reservation cancelled successfully.");
        } else {
            System.out.println("❌ Failed to cancel reservation. It may not exist or already be completed.");
        }
    }
}