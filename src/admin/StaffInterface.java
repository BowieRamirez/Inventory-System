package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.Receipt;
import inventory.Item;
import utils.InputValidator;

import java.util.List;

public class StaffInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private InputValidator validator;

    public StaffInterface(InventoryManager inventoryManager, ReservationManager reservationManager, 
                          ReceiptManager receiptManager, InputValidator validator) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.receiptManager = receiptManager;
        this.validator = validator;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== STAFF HOMEPAGE ===");
            System.out.println("[1] Help");
            System.out.println("[2] User Reservations");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Add/Remove Item");
            System.out.println("[5] Process Returns/Refunds");
            System.out.println("[6] Logout");
            System.out.println("[0] Exit");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 6);
            
            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                case 1: showStaffHelp(); break;
                case 2: showUserReservations(); break;
                case 3: showStockPage(); break;
                case 4: showAddRemoveMenu(); break;
                case 5: processReturnsRefunds(); break;
                case 6:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showStaffHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          STAFF HELP GUIDE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nNAVIGATION BASICS:");
        System.out.println("   - Use number keys (0-9) to select menu options");
        System.out.println("   - Press [0] to go back to the previous menu");
        System.out.println("   - All actions require confirmation (y/n) for safety");
        
        System.out.println("\nUSER RESERVATIONS MANAGEMENT:");
    System.out.println("   [1] View All - See all student reservations");
    System.out.println("   [2] View Pending - Filter pending reservations");
    System.out.println("   [3] Cancel Reservation - Remove reservation");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View All - Display entire inventory");
        System.out.println("   [2] View by Course - Filter by course code");
        System.out.println("   [3] Search by Code - Find item by code");
        
        System.out.println("\nADD/REMOVE ITEM:");
        System.out.println("   [1] Add Item - Add new merchandise");
        System.out.println("   [2] Remove Item - Delete item");
        System.out.println("   [3] Update Quantity - Change stock levels");
        
        System.out.println("\nIMPORTANT NOTES:");
        System.out.println("   - Staff CANNOT access Account Management");
        System.out.println("   - Only Admin has access to view student accounts");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }

    private void showUserReservations() {
        while (true) {
            System.out.println("\n=== USER RESERVATIONS ===");
            System.out.println("[1] View All");
            System.out.println("[2] View Pending");
            System.out.println("[3] Cancel Reservation");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 4);
            
            switch (choice) {
                case 0: return;
                case 1: 
                    reservationManager.displayAllReservations();
                    System.out.println("\nPress [0] to go back...");
                    validator.getValidInteger("", 0, 0);
                    break;
                case 2: 
                    displayPending();
                    System.out.println("\nPress [0] to go back...");
                    validator.getValidInteger("", 0, 0);
                    break;
                case 3: cancelRes(); break;
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
        // Display all reservations in table format
        System.out.println("\n=== ALL RESERVATIONS ===");
        List<Reservation> allReservations = reservationManager.getAllReservations();
        
        if (allReservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|----------");
        
        for (Reservation r : allReservations) {
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-7.2f | %-8s | %-10s | %-10s\n",
                r.getReservationId(),
                truncate(r.getStudentName(), 15),
                r.getStudentId(),
                r.getItemCode(),
                truncate(r.getItemName(), 25),
                r.getQuantity(),
                r.getTotalPrice(),
                r.isPaid() ? "PAID" : "UNPAID",
                r.getPaymentMethod(),
                r.getStatus());
        }
        
        System.out.println("\nℹ Note: Only PAID reservations can be approved for pickup.");
        
        int id = validator.getValidInteger("Enter Reservation ID (or 0 to cancel): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Not found.");
            return;
        }
        
        // Check if reservation is COMPLETED - handle return/refund eligibility
        if (r.getStatus().equals("COMPLETED")) {
            if (r.isEligibleForReturn()) {
                long daysLeft = r.getDaysUntilReturnExpires();
                System.out.println("\n=== RETURN/REFUND OPTION AVAILABLE ===");
                System.out.println("This item was picked up and is eligible for return.");
                System.out.println("Days remaining for return: " + daysLeft + " days");
                System.out.println("\nWould you like to process a return and refund?");
                
                if (validator.getValidYesNo("Process return/refund?")) {
                    System.out.println("\n=== RETURN CONFIRMATION ===");
                    System.out.println("Student: " + r.getStudentName());
                    System.out.println("Item: " + r.getItemName());
                    System.out.println("Quantity: " + r.getQuantity());
                    System.out.println("Refund Amount: ₱" + r.getTotalPrice());
                    
                    if (validator.getValidYesNo("\nConfirm return and issue refund?")) {
                        // Change status to RETURNED
                        reservationManager.updateReservationStatus(id, "RETURNED - REFUNDED", "Item returned within 10 days");
                        
                        // Restock the item by size
                        boolean restocked = inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
                        if (restocked) {
                            System.out.println("✓ Item restocked: " + r.getQuantity() + " units added back to inventory");
                        } else {
                            System.out.println("⚠ Warning: Could not restock item to inventory");
                        }
                        
                        // Update receipt status to RETURNED
                        Receipt receipt = receiptManager.findReceiptByItemAndBuyer(r.getItemCode(), r.getStudentName());
                        if (receipt != null) {
                            receiptManager.updatePaymentStatus(receipt.getReceiptId(), "RETURNED - REFUNDED");
                            System.out.println("✓ Receipt updated to: RETURNED - REFUNDED");
                        }
                        
                        System.out.println("✓ Return processed successfully!");
                        System.out.println("✓ Status updated to: RETURNED - REFUNDED");
                        System.out.println("✓ Refund Amount: ₱" + r.getTotalPrice());
                        System.out.println("\nℹ Please process the refund payment to the student.");
                    }
                }
                return;
            } else {
                System.out.println("\n⚠ ERROR: Cannot modify this reservation!");
                System.out.println("Reason: This reservation is COMPLETED.");
                if (r.getCompletedDate() != null) {
                    System.out.println("The 10-day return period has expired.");
                }
                System.out.println("No changes can be made to this transaction.");
                return;
            }
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
        System.out.println("[1] CANCELLED");
        System.out.println("[0] Cancel");

        int status = validator.getValidInteger("Select status: ", 0, 1);
        if (status == 0) return;

        String newStatus = "CANCELLED";

        // Check if trying to cancel a paid reservation
        if (r.isPaid()) {
            System.out.println("\n⚠ ERROR: Cannot cancel paid reservation!");
            System.out.println("Reason: Student has already paid for this reservation.");
            System.out.println("Contact the student or process a refund if needed.");
            return;
        }

        if (validator.getValidYesNo("Confirm status change to: " + newStatus + "?")) {
            reservationManager.updateReservationStatus(id, newStatus, "");
            System.out.println("✓ Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        // Display all reservations in table format
        System.out.println("\n=== ALL RESERVATIONS ===");
        List<Reservation> allReservations = reservationManager.getAllReservations();
        
        if (allReservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|----------");
        
        for (Reservation r : allReservations) {
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-7.2f | %-8s | %-10s | %-10s\n",
                r.getReservationId(),
                truncate(r.getStudentName(), 15),
                r.getStudentId(),
                r.getItemCode(),
                truncate(r.getItemName(), 25),
                r.getQuantity(),
                r.getTotalPrice(),
                r.isPaid() ? "PAID" : "UNPAID",
                r.getPaymentMethod(),
                r.getStatus());
        }
        
        int id = validator.getValidInteger("Enter ID to cancel (or 0 to go back): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        // Check if reservation exists and if it's paid
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        // Check if user has already paid
        if (r.isPaid()) {
            System.out.println("\n ERROR: Cannot cancel paid reservation!");
            System.out.println("Reason: Student has already paid for this reservation.");
            System.out.println("Contact the student or process a refund if needed.");
            return;
        }
        
        if (validator.getValidYesNo("Confirm cancellation?")) {
            if (reservationManager.cancelReservation(id, "Cancelled by staff")) {
                System.out.println("Cancelled.");
            }
        }
    }

    private void processReturnsRefunds() {
        System.out.println("\n=== PROCESS RETURNS/REFUNDS ===");
        
        // Get all COMPLETED reservations that are eligible for return
        List<Reservation> allReservations = reservationManager.getAllReservations();
        List<Reservation> eligibleReturns = new java.util.ArrayList<>();
        
        for (Reservation r : allReservations) {
            if (r.getStatus().equals("COMPLETED") && r.isEligibleForReturn()) {
                eligibleReturns.add(r);
            }
        }
        
        if (eligibleReturns.isEmpty()) {
            System.out.println("\nNo items eligible for return/refund at this time.");
            System.out.println("\nℹ Items can be returned within 10 days of pickup.");
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
            return;
        }
        
        System.out.println("\n=== ITEMS ELIGIBLE FOR RETURN ===");
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Days Left");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------");
        
        for (Reservation r : eligibleReturns) {
            long daysLeft = r.getDaysUntilReturnExpires();
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-7.2f | %d days\n",
                r.getReservationId(),
                truncate(r.getStudentName(), 15),
                r.getStudentId(),
                r.getItemCode(),
                truncate(r.getItemName(), 25),
                r.getQuantity(),
                r.getTotalPrice(),
                daysLeft);
        }
        
        int id = validator.getValidInteger("\nEnter Reservation ID to process return (0 to cancel): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        if (!r.getStatus().equals("COMPLETED")) {
            System.out.println("\n⚠ ERROR: Only COMPLETED items can be returned.");
            return;
        }
        
        if (!r.isEligibleForReturn()) {
            System.out.println("\n⚠ ERROR: Return period has expired (10 days limit).");
            return;
        }
        
        System.out.println("\n=== RETURN CONFIRMATION ===");
        System.out.println("Student: " + r.getStudentName() + " (" + r.getStudentId() + ")");
        System.out.println("Item: " + r.getItemName() + " (Size: " + r.getSize() + ")");
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Refund Amount: ₱" + r.getTotalPrice());
        System.out.println("Payment Method: " + r.getPaymentMethod());
        
        if (validator.getValidYesNo("\nConfirm return and issue refund?")) {
            // Change status to RETURNED
            reservationManager.updateReservationStatus(id, "RETURNED - REFUNDED", "Item returned within 10 days");
            
            // Restock the item by size
            boolean restocked = inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
            if (restocked) {
                System.out.println("✓ Item restocked: " + r.getQuantity() + " units added back to inventory");
            } else {
                System.out.println("⚠ Warning: Could not restock item to inventory");
            }
            
            // Update receipt status to RETURNED
            Receipt receipt = receiptManager.findReceiptByItemAndBuyer(r.getItemCode(), r.getStudentName());
            if (receipt != null) {
                receiptManager.updatePaymentStatus(receipt.getReceiptId(), "RETURNED - REFUNDED");
                System.out.println("✓ Receipt updated to: RETURNED - REFUNDED");
            }
            
            System.out.println("✓ Return processed successfully!");
            System.out.println("✓ Refund Amount: ₱" + r.getTotalPrice());
            System.out.println("\n⚠ Please process the refund payment to the student.");
        } else {
            System.out.println("Return cancelled.");
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
        String course = validator.getValidCourse("Enter course code: ");
        inventoryManager.displayItemsByCourse(course);
    }

    private void searchByCode() {
        int code = validator.getValidInteger("Enter item code: ", 1000, 9999);
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            System.out.println("\n=== ITEM DETAILS ===");
            System.out.println("Code | Item Name                 | Course               | Size     | Quantity | Price");
            System.out.println("-----|---------------------------|----------------------|----------|----------|--------");
            System.out.println(item);
        } else {
            System.out.println("Item not found.");
        }
    }

    private void showAddRemoveMenu() {
        while (true) {
            System.out.println("\n=== ADD/REMOVE ITEM ===");
            System.out.println("[1] Add Item");
            System.out.println("[2] Remove Item");
            System.out.println("[3] Update Quantity");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: addItem(); break;
                case 2: removeItem(); break;
                case 3: updateQuantity(); break;
            }
        }
    }

    private void addItem() {
        System.out.println("\n--- Add New Item ---");
        int code = validator.getValidInteger("Enter item code: ", 1000, 9999);
        
        if (inventoryManager.findItemByCode(code) != null) {
            System.out.println("Item code already exists!");
            return;
        }
        
        String name = validator.getValidNonEmptyString("Enter item name: ", "Item name");
        String course = validator.getValidCourse("Enter course: ");
        String size = validator.getValidNonEmptyString("Enter size: ", "Size");
        int qty = validator.getValidInteger("Enter quantity: ", 0, 10000);
        double price = validator.getValidPrice("Enter price: ");
        
        if (validator.getValidYesNo("Confirm add item?")) {
            Item newItem = new Item(code, name, course, size, qty, price);
            inventoryManager.addItem(newItem);
            System.out.println("Item added successfully!");
        }
    }

    private void removeItem() {
        int code = validator.getValidInteger("Enter item code to remove: ", 1000, 9999);
        Item item = inventoryManager.findItemByCode(code);
        
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        System.out.println("Item to remove: " + item);
        if (validator.getValidYesNo("Confirm removal?")) {
            inventoryManager.removeItem(code);
            System.out.println("Item removed successfully!");
        }
    }

    private void updateQuantity() {
        int code = validator.getValidInteger("Enter item code: ", 1000, 9999);
        Item item = inventoryManager.findItemByCode(code);
        
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        System.out.println("Current item: " + item);
        System.out.println("Current quantity: " + item.getQuantity());
        
        int newQty = validator.getValidInteger("Enter new quantity: ", 0, 10000);
        
        if (validator.getValidYesNo("Confirm quantity update?")) {
            inventoryManager.updateItemQuantity(code, newQty);
            System.out.println("Quantity updated successfully!");
        }
    }
    
    // Helper method to truncate text to specified length
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
