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
        System.out.println("\n=== ADMIN HELP ===");
        System.out.println("Manage inventory and reservations");
        System.out.println("Press Enter...");
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
            System.out.println(" Not found.");
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
            System.out.println(" Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        int id = validator.getValidInteger("Enter ID to cancel: ", 1000, 9999);
        if (validator.getValidYesNo("Confirm cancellation?")) {
            if (reservationManager.cancelReservation(id)) {
                System.out.println(" Cancelled.");
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
            System.out.println(" Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            System.out.println("\n=== ITEM FOUND ===");
            System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
            System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
            System.out.println(item);
        } else {
            System.out.println(" Not found.");
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
            System.out.println(" Returning to menu...");
            return;
        }
        
        if (inventoryManager.findItemByCode(code) != null) {
            System.out.println(" Code exists!");
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
            System.out.println(" Item added!");
        }
    }

    private void removeItem() {
        System.out.println("\n=== REMOVE ITEM ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Item code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println(" Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println(" Not found.");
            return;
        }
        
        System.out.println(item);
        if (validator.getValidYesNo("Remove this item?")) {
            inventoryManager.removeItem(code);
            System.out.println(" Removed!");
        }
    }

    private void updateQty() {
        System.out.println("\n=== UPDATE QUANTITY ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Item code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println(" Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println(" Not found.");
            return;
        }
        
        System.out.println("Current: " + item);
        int newQty = validator.getValidInteger("New quantity: ", 0, 1000);
        
        if (validator.getValidYesNo("Update quantity?")) {
            inventoryManager.updateItemQuantity(code, newQty);
            System.out.println(" Updated!");
        }
    }
}