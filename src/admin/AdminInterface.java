package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Reservation;
import inventory.Item;
import utils.InputValidator;

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
            System.out.println("\n=== ADMIN HOMEPAGE ===");
            System.out.println("[1] Help");
            System.out.println("[2] User Reservations");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Add/Remove Item");
            System.out.println("[5] Logout");
            System.out.println("[0] Exit");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 5);
            
            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                case 1: showAdminHelp(); break;
                case 2: showUserReservations(); break;
                case 3: showStockPage(); break;
                case 4: showAddRemoveMenu(); break;
                case 5:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("👋 Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showAdminHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          ADMIN HELP GUIDE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nNAVIGATION BASICS:");
        System.out.println("   • Use number keys (0-9) to select menu options");
        System.out.println("   • Press [0] to go back to the previous menu");
        System.out.println("   • Press [0] when entering codes/IDs to cancel and return");
        System.out.println("   • All actions require confirmation (y/n) for safety");
        
        System.out.println("\nUSER RESERVATIONS MANAGEMENT:");
        System.out.println("   [1] View All - See all student reservations with full details");
        System.out.println("   [2] View Pending - Filter and view only pending reservations");
        System.out.println("   [3] Update Status - Change reservation status:");
        System.out.println("       • PENDING - Initial status when student reserves");
        System.out.println("       • APPROVED - READY FOR PICKUP - Notify student to pickup");
        System.out.println("       • COMPLETED - Mark when student has picked up items");
        System.out.println("       • CANCELLED - Cancel the reservation");
        System.out.println("   [4] Cancel Reservation - Remove a reservation from the system");
        System.out.println("   [0] Back - Return to main menu");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View All - Display entire inventory with all details");
        System.out.println("   [2] View by Course - Filter items by specific course code");
        System.out.println("       Available courses: BSIT, BSCS, STEM, ABM, HUMSS, etc.");
        System.out.println("   [3] Search by Code - Find specific item using item code (1000-9999)");
        System.out.println("       • Enter [0] to go back without searching");
        System.out.println("   [0] Back - Return to main menu");
        
        System.out.println("\nADD/REMOVE ITEM:");
        System.out.println("   [1] Add Item - Add new merchandise to inventory:");
        System.out.println("       • Enter item code (1000-9999) or [0] to cancel");
        System.out.println("       • Provide item name, course, size, quantity, and price");
        System.out.println("       • System checks for duplicate codes");
        System.out.println("   [2] Remove Item - Delete item from inventory:");
        System.out.println("       • Enter item code or [0] to cancel");
        System.out.println("       • Confirm before deletion");
        System.out.println("   [3] Update Quantity - Change stock levels:");
        System.out.println("       • Enter item code or [0] to cancel");
        System.out.println("       • Set new quantity (0-1000)");
        System.out.println("       • Confirm before updating");
        System.out.println("   [0] Back - Return to main menu");
        
        System.out.println("\nTIPS & BEST PRACTICES:");
        System.out.println("   ✓ Regularly check pending reservations");
        System.out.println("   ✓ Update status to 'APPROVED - READY FOR PICKUP' when ready");
        System.out.println("   ✓ Mark as 'COMPLETED' only after student pickup");
        System.out.println("   ✓ Monitor inventory levels to prevent stockouts");
        System.out.println("   ✓ Use [0] anytime to safely go back or cancel operations");
        System.out.println("   ✓ All deletion/modification actions require confirmation");
        
        System.out.println("\nIMPORTANT NOTES:");
        System.out.println("   • Item codes must be unique (1000-9999)");
        System.out.println("   • Course codes must be valid (14 courses available)");
        System.out.println("   • Valid sizes: XS, S, M, L, XL, XXL, One Size");
        System.out.println("   • Price range: ₱0 - ₱10,000");
        System.out.println("   • Quantity range: 0 - 1000");
        System.out.println("   • Students can only reserve items for their course + STI Special");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press Enter to return to Admin Homepage...");
        try { System.in.read(); } catch (Exception e) { }
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
        for (Reservation r : reservationManager.getPendingReservations()) {
            System.out.println(r);
        }
    }

    private void updateStatus() {
        int id = validator.getValidInteger("Enter Reservation ID: ", 1000, 9999);
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Not found.");
            return;
        }
        
        System.out.println("Current: " + r);
        System.out.println("\n[1] PENDING");
        System.out.println("[2] APPROVED - READY FOR PICKUP");
        System.out.println("[3] COMPLETED");
        System.out.println("[4] CANCELLED");
        
        int status = validator.getValidInteger("Select status: ", 1, 4);
        String newStatus = switch (status) {
            case 1 -> "PENDING";
            case 2 -> "APPROVED - READY FOR PICKUP";
            case 3 -> "COMPLETED";
            case 4 -> "CANCELLED";
            default -> r.getStatus();
        };
        
        if (validator.getValidYesNo("Confirm status change?")) {
            reservationManager.updateReservationStatus(id, newStatus);
            System.out.println("Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        int id = validator.getValidInteger("Enter ID to cancel: ", 1000, 9999);
        if (validator.getValidYesNo("Confirm cancellation?")) {
            if (reservationManager.cancelReservation(id)) {
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
            System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
            System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
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
            System.out.println("📝 Returning to menu...");
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