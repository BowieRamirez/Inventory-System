package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Reservation;
import inventory.Item;
import utils.InputValidator;
import student.Student;

import java.util.List;

public class AdminInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private InputValidator validator;
    private List<Student> registeredStudents;

    public AdminInterface(InventoryManager inventoryManager, ReservationManager reservationManager, 
                          InputValidator validator, List<Student> registeredStudents) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.validator = validator;
        this.registeredStudents = registeredStudents;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== ADMIN HOMEPAGE ===");
            System.out.println("[1] Help");
            System.out.println("[2] User Reservations");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Add/Remove Item");
            System.out.println("[5] Account Management");
            System.out.println("[6] Logout");
            System.out.println("[0] Exit");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 6);
            
            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                case 1: showAdminHelp(); break;
                case 2: showUserReservations(); break;
                case 3: showStockPage(); break;
                case 4: showAddRemoveMenu(); break;
                case 5: showAccountManagement(); break;
                case 6:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showAccountManagement() {
        System.out.println("\n=== ACCOUNT MANAGEMENT (ADMIN ONLY) ===");
        System.out.println("Total Registered Students: " + registeredStudents.size());
        
        if (registeredStudents.isEmpty()) {
            System.out.println("No registered students found.");
            return;
        }
        
        System.out.println("\nID   | Student ID    | Name and section               | Password");
        System.out.println("-----|---------------|---------------------------|------------------");

        int count = 1;
        for (Student student : registeredStudents) {
            System.out.printf("%-4d | %-13s | %-25s | %s\n",
                count++,
                student.getStudentId(),
                student.getFullName(),
                "********");
        }
        
        System.out.println("\nPress [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }

    private void showAdminHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          ADMIN HELP GUIDE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nNAVIGATION BASICS:");
        System.out.println("   - Use number keys (0-9) to select menu options");
        System.out.println("   - Press [0] to go back to the previous menu");
        System.out.println("   - All actions require confirmation (y/n) for safety");
        
        System.out.println("\nUSER RESERVATIONS MANAGEMENT:");
        System.out.println("   [1] View All - See all student reservations");
        System.out.println("   [2] View Pending - Filter pending reservations");
        System.out.println("   [3] Update Status - Change reservation status");
        System.out.println("   [4] Cancel Reservation - Remove reservation");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View All - Display entire inventory");
        System.out.println("   [2] View by Course - Filter by course code");
        System.out.println("   [3] Search by Code - Find item by code");
        
        System.out.println("\nADD/REMOVE ITEM:");
        System.out.println("   [1] Add Item - Add new merchandise");
        System.out.println("   [2] Remove Item - Delete item");
        System.out.println("   [3] Update Quantity - Change stock levels");
        
        System.out.println("\nACCOUNT MANAGEMENT:");
        System.out.println("   - View all registered student accounts");
        System.out.println("   - Staff and Cashier CANNOT access this");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }

    private void showUserReservations() {
        while (true) {
            System.out.println("\n=== USER RESERVATIONS ===");
            System.out.println("[1] View All");
            System.out.println("[2] View Pending");
            System.out.println("[3] Update Status");
            System.out.println("[4] Cancel Reservation");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 4);
            
            switch (choice) {
                case 0: return;
                case 1: reservationManager.displayAllReservations(); break;
                case 2: displayPending(); break;
                case 3: updateStatus(); break;
                case 4: cancelRes(); break;
            }
        }
    }

    private void displayPending() {
        System.out.println("\n=== PENDING RESERVATIONS ===");
        List<Reservation> pending = reservationManager.getPendingReservations();
        
        if (pending.isEmpty()) {
            System.out.println("No pending reservations.");
            return;
        }
        
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|------------------------------");
        for (Reservation r : pending) {
            System.out.println(r);
        }
        
        System.out.println("\nℹ Note: Only PAID reservations can be approved for pickup.");
    }

    private void updateStatus() {
        int id = validator.getValidInteger("Enter Reservation ID: ", 1000, 9999);
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Not found.");
            return;
        }
        
        // Check if reservation is already COMPLETED
        if (r.getStatus().equals("COMPLETED")) {
            System.out.println("\n⚠ ERROR: Cannot update status!");
            System.out.println("Reason: This reservation is already COMPLETED.");
            System.out.println("Completed reservations are final and cannot be modified.");
            return;
        }
        
        System.out.println("\n=== CURRENT RESERVATION ===");
        System.out.println("ID: " + r.getReservationId());
        System.out.println("Student: " + r.getStudentName() + " (" + r.getStudentId() + ")");
        System.out.println("Item: " + r.getItemName());
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Total: ₱" + r.getTotalPrice());
        System.out.println("Payment Status: " + r.getPaymentStatus());
        System.out.println("Current Status: " + r.getStatus());
        
        System.out.println("\n=== SELECT NEW STATUS ===");
        System.out.println("[1] PENDING");
        System.out.println("[2] APPROVED - READY FOR PICKUP");
        System.out.println("[4] CANCELLED");
        System.out.println("[0] Cancel");
        System.out.println("\nNote: Status automatically changes to COMPLETED when student picks up.");
        
        int status = validator.getValidInteger("Select status: ", 0, 4);
        if (status == 0) return;
        
        if (status == 3) {
            System.out.println("\n⚠ Cannot manually set to COMPLETED.");
            System.out.println("Status automatically changes to COMPLETED when student picks up the item.");
            return;
        }
        
        String newStatus = switch (status) {
            case 1 -> "PENDING";
            case 2 -> "APPROVED - READY FOR PICKUP";
            case 4 -> "CANCELLED";
            default -> r.getStatus();
        };
        
        // Check if trying to approve for pickup without payment
        if (newStatus.equals("APPROVED - READY FOR PICKUP") && !r.isPaid()) {
            System.out.println("\n⚠ ERROR: Cannot approve for pickup!");
            System.out.println("Reason: Student has not paid yet.");
            System.out.println("Student must pay at CASHIER first.");
            return;
        }
        
        if (validator.getValidYesNo("Confirm status change to: " + newStatus + "?")) {
            reservationManager.updateReservationStatus(id, newStatus, "");
            System.out.println("✓ Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        int id = validator.getValidInteger("Enter ID to cancel: ", 1000, 9999);
        if (validator.getValidYesNo("Confirm cancellation?")) {
            if (reservationManager.cancelReservation(id, "Cancelled by admin")) {
                System.out.println("Cancelled.");
            }
        }
    }

    private void showStockPage() {
        while (true) {
            System.out.println("\n=== STOCK PAGE ===");
            System.out.println("[1] View All");
            System.out.println("[2] View by Course");
            System.out.println("[3] Search by Code");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: inventoryManager.displayAllItems(); break;
                case 2: viewByCourse(); break;
                case 3: searchByCode(); break;
            }
        }
    }

    private void viewByCourse() {
        for (String course : inventoryManager.getAvailableCourses()) {
            System.out.println("- " + course);
        }
        String course = validator.getValidNonEmptyString("Enter course: ", "Course");
        inventoryManager.displayItemsByCourse(course);
    }

    private void searchByCode() {
        System.out.println("\n=== SEARCH BY CODE ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Enter code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println("Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            System.out.println("\n=== ITEM FOUND ===");
            System.out.println("Code   | Name                           | Course                    | Size       | Quantity | Price");
            System.out.println("-------|--------------------------------|---------------------------|------------|----------|----------");
            System.out.println(item);
        } else {
            System.out.println("Not found.");
        }
    }

    private void showAddRemoveMenu() {
        while (true) {
            System.out.println("\n=== ADD/REMOVE/UPDATE ===");
            System.out.println("[1] Add Item");
            System.out.println("[2] Remove Item");
            System.out.println("[3] Update Quantity");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: addItem(); break;
                case 2: removeItem(); break;
                case 3: updateQty(); break;
            }
        }
    }

    private void addItem() {
        System.out.println("\n=== ADD ITEM ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Item code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println("Returning to menu...");
            return;
        }
        
        if (inventoryManager.findItemByCode(code) != null) {
            System.out.println("Code exists!");
            return;
        }
        
        String name = validator.getValidNonEmptyString("Item name: ", "Name");
        String course = validator.getValidCourse("Course code: ");
        String size = validator.getValidSize("Size: ");
        int qty = validator.getValidInteger("Quantity: ", 1, 1000);
        double price = validator.getValidPrice("Price: ");
        
        Item item = new Item(code, name, course, size, qty, price);
        
        if (validator.getValidYesNo("Add this item?")) {
            inventoryManager.addItem(item);
            System.out.println("Item added!");
        }
    }

    private void removeItem() {
        System.out.println("\n=== REMOVE ITEM ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Item code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println("Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("Not found.");
            return;
        }
        
        System.out.println(item);
        if (validator.getValidYesNo("Remove this item?")) {
            inventoryManager.removeItem(code);
            System.out.println("Removed!");
        }
    }

    private void updateQty() {
        System.out.println("\n=== UPDATE QUANTITY ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Item code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println("Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("Not found.");
            return;
        }
        
        System.out.println("Current: " + item);
        int newQty = validator.getValidInteger("New quantity: ", 0, 1000);
        
        if (validator.getValidYesNo("Update quantity?")) {
            inventoryManager.updateItemQuantity(code, newQty);
            System.out.println("Updated!");
        }
    }
}