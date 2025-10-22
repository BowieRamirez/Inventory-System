package student;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Reservation;
import inventory.Item;
import utils.InputValidator;

import java.util.List;

public class StudentInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private InputValidator validator;
    private Student student;

    public StudentInterface(InventoryManager inventoryManager, ReservationManager reservationManager,
                            InputValidator validator, Student student) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.validator = validator;
        this.student = student;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("       STUDENT HOMEPAGE");
            System.out.println("   Welcome, " + student.getFullName() + "!");
            System.out.println("   Course: " + student.getCourse());
            System.out.println("=================================");
            System.out.println("[1] Help");
            System.out.println("[2] Reserve a Item");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Your Reservations");
            System.out.println("[5] Payment Status");
            System.out.println("[6] View Receipts");
            System.out.println("[7] Pickup Item");
            System.out.println("[8] Logout");
            System.out.println("[0] Exit System");
            
            int choice = validator.getValidInteger("Enter your choice: ", 0, 8);
            
            switch (choice) {
                case 0:
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;
                case 1:
                    showStudentHelp();
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
                case 5:
                    showPaymentStatus();
                    break;
                case 6:
                    viewReceipts();
                    break;
                case 7:
                    pickupItem();
                    break;
                case 8:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showStudentHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          STUDENT HELP GUIDE");
        System.out.println("=".repeat(80));
        System.out.println("Welcome, " + student.getFullName() + "!");
        System.out.println("Course: " + student.getCourse());
        System.out.println("Student ID: " + student.getStudentId());
        
        System.out.println("\nNAVIGATION BASICS:");
        System.out.println("   • Use number keys (0-9) to select menu options");
        System.out.println("   • Press [0] to go back to the previous menu");
        System.out.println("   • Press [0] when entering codes to cancel and return");
        System.out.println("   • All reservations require confirmation (y/n)");
        
        System.out.println("\nHOW TO RESERVE ITEMS:");
        System.out.println("   Step 1: Select [2] Reserve a Item from homepage");
        System.out.println("   Step 2: Browse available items for " + student.getCourse());
        System.out.println("           • You can reserve items for YOUR COURSE");
        System.out.println("           • You can also reserve STI SPECIAL merchandise");
        System.out.println("   Step 3: Note the item code (e.g., 1001, 2002)");
        System.out.println("   Step 4: Enter item code or [0] to go back");
        System.out.println("   Step 5: Select your size (XS, S, M, L, XL, XXL, One Size)");
        System.out.println("   Step 6: Enter quantity (must not exceed available stock)");
        System.out.println("   Step 7: Review details and confirm (y/n)");
        System.out.println("   Step 8: Receive your Reservation ID");
        System.out.println("           • Save this ID to track your order!");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View Your Course Items - See all items for " + student.getCourse());
        System.out.println("   [2] View STI Special Items - Browse special merchandise");
        System.out.println("       • Anniversary Shirts, PE Uniforms, Washday Shirts, etc.");
        System.out.println("   [3] Search by Code - Find specific item using code (1000-9999)");
        System.out.println("       • Enter item code or [0] to go back");
        System.out.println("   [0] Back - Return to homepage");
        
        System.out.println("\nYOUR RESERVATIONS:");
        System.out.println("   [1] View All Reservations - See complete history");
        System.out.println("   [2] View Pending - See reservations awaiting approval");
        System.out.println("   [3] Cancel Reservation - Cancel a pending reservation");
        System.out.println("       • You can only cancel PENDING reservations");
        System.out.println("       • Stock will be returned to inventory");
        System.out.println("   [0] Back - Return to homepage");
        
        System.out.println("\nRESERVATION STATUS MEANINGS:");
        System.out.println("   PENDING - Your reservation is awaiting admin approval");
        System.out.println("   APPROVED - READY FOR PICKUP - Your items are ready! Go pickup");
        System.out.println("   COMPLETED - You have successfully picked up your items");
        System.out.println("   CANCELLED - Reservation was cancelled");
        
        System.out.println("\n TIPS FOR SUCCESSFUL ORDERING:");
        System.out.println("   ✓ Check available stock before reserving");
        System.out.println("   ✓ Double-check your size selection");
        System.out.println("   ✓ Save your Reservation ID for tracking");
        System.out.println("   ✓ Check 'Your Reservations' regularly for status updates");
        System.out.println("   ✓ When status is 'APPROVED - READY FOR PICKUP', go to pickup location");
        System.out.println("   ✓ Bring your Student ID and Reservation ID for pickup");
        System.out.println("   ✓ Payment is required during merchandise pickup");
        
        System.out.println("\n IMPORTANT NOTES:");
        System.out.println("   • You can only reserve items for YOUR course (" + student.getCourse() + ")");
        System.out.println("   • STI Special items are available to ALL students");
        System.out.println("   • Sizes available: XS, S, M, L, XL, XXL, One Size");
        System.out.println("   • Items must be collected within 7 days of approval");
        System.out.println("   • You can cancel PENDING reservations anytime");
        System.out.println("   • Cannot cancel APPROVED or COMPLETED reservations");
        
        System.out.println("\n NEED HELP?");
        System.out.println("   • Contact your admin if you have questions");
        System.out.println("   • Check stock availability before reserving");
        System.out.println("   • Use [0] anytime to safely go back");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }

    private void reserveItem() {
        while (true) {
            System.out.println("\n=== RESERVE ITEM ===");
            System.out.println("[0] Back to Menu");
            System.out.println("\nAvailable items for " + student.getCourse() + ":");
            inventoryManager.displayItemsByCourse(student.getCourse());
            System.out.println("\n🎉 STI Special Merchandise:");
            inventoryManager.displayItemsByCourse("STI Special");

            int code = validator.getValidInteger("\nEnter item code to reserve (0 to go back): ", 0, 9999);
            if (code == 0) return;

            Item item = inventoryManager.findItemByCode(code);
            if (item == null) {
                System.out.println("Item not found.");
                continue;
            }

            if (!item.getCourse().equalsIgnoreCase(student.getCourse()) 
                && !item.getCourse().equalsIgnoreCase("STI Special")) {
                System.out.println("You can only reserve items for your course or STI Special items.");
                continue;
            }

            // Find all size variants of this item
            List<Item> sizeVariants = inventoryManager.findSizeVariants(item.getName(), item.getCourse());
            
            if (sizeVariants.isEmpty()) {
                System.out.println("No available stock for this item.");
                continue;
            }
            
            System.out.println("\nItem: " + item.getName());
            System.out.println("Available stock: " + item.getQuantity());
            System.out.println("Price: ₱" + item.getPrice());
            
            // Display available sizes
            System.out.println("\n Valid Sizes: " + getSizesList(sizeVariants));
            String selectedSize = validator.getValidSize("Select size: ");
            
            // Find the item with the selected size
            Item selectedItem = null;
            for (Item variant : sizeVariants) {
                if (variant.getSize().equalsIgnoreCase(selectedSize)) {
                    selectedItem = variant;
                    break;
                }
            }
            
            if (selectedItem == null) {
                System.out.println("Selected size not available for this item.");
                continue;
            }
            
            System.out.println("Available stock for size " + selectedSize + ": " + selectedItem.getQuantity());
            
            int qty = validator.getValidInteger("Enter quantity (1-" + selectedItem.getQuantity() + "): ", 1, selectedItem.getQuantity());

            double totalPrice = selectedItem.getPrice() * qty;
            
            System.out.println("\n=== CONFIRMATION ===");
            System.out.println("Student: " + student.getFullName());
            System.out.println("Item: " + selectedItem.getName());
            System.out.println("Size: " + selectedItem.getSize());
            System.out.println("Quantity: " + qty);
            System.out.println("Total: ₱" + totalPrice);

            if (validator.getValidYesNo("\nConfirm reservation?")) {
                if (inventoryManager.reserveItem(selectedItem.getCode(), selectedItem.getSize(), qty)) {
                    Reservation res = reservationManager.createReservation(
                        student.getFullName(),
                        student.getStudentId(),
                        student.getCourse(),
                        selectedItem.getCode(),
                        selectedItem.getName(),
                        selectedItem.getSize(),
                        qty,
                        totalPrice
                    );
                    System.out.println("\n✓ Reservation created successfully!");
                    System.out.println("Reservation ID: " + res.getReservationId());
                    System.out.println("Total Amount: ₱" + totalPrice);
                    System.out.println("Payment Status: UNPAID");
                    System.out.println("\n⚠ IMPORTANT: Please proceed to CASHIER to make payment!");
                    System.out.println("Status: " + res.getStatus());
                } else {
                    System.out.println("Failed to create reservation.");
                }
            }
            
            if (!validator.getValidYesNo("Reserve another item?")) {
                return;
            }
        }
    }
    
    // Helper method to get a formatted list of available sizes with quantities
    private String getSizesList(List<Item> items) {
        StringBuilder sizes = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            sizes.append(item.getSize()).append(" (").append(item.getQuantity()).append(")");
            if (i < items.size() - 1) {
                sizes.append(", ");
            }
        }
        return sizes.toString();
    }

    private void showStockPage() {
        while (true) {
            System.out.println("\n=== STOCK PAGE ===");
            System.out.println("[1] View Your Course Items");
            System.out.println("[2] View STI Special Items");
            System.out.println("[3] Search by Code");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: inventoryManager.displayItemsByCourse(student.getCourse()); break;
                case 2: inventoryManager.displayItemsByCourse("STI Special"); break;
                case 3: searchItem(); break;
            }
        }
    }

    private void searchItem() {
        System.out.println("\n=== SEARCH BY CODE ===");
        System.out.println("[0] Back to previous menu");
        int code = validator.getValidInteger("Enter item code (1000-9999, 0 to go back): ", 0, 9999);
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
            System.out.println("Item not found.");
        }
    }

    private void showYourReservations() {
        while (true) {
            System.out.println("\n=== YOUR RESERVATIONS ===");
            System.out.println("[1] View All Reservations");
            System.out.println("[2] View Pending");
            System.out.println("[3] Cancel Reservation");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: reservationManager.displayReservationsByStudent(student.getStudentId()); break;
                case 2: displayPendingReservations(); break;
                case 3: cancelReservation(); break;
            }
        }
    }

    private void displayPendingReservations() {
        List<Reservation> all = reservationManager.getReservationsByStudent(student.getStudentId());
        System.out.println("\n=== PENDING RESERVATIONS ===");
        boolean found = false;
        for (Reservation r : all) {
            if (r.getStatus().equals("PENDING")) {
                System.out.println(r);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No pending reservations.");
        }
    }

    private void cancelReservation() {
        reservationManager.displayReservationsByStudent(student.getStudentId());
        int id = validator.getValidInteger("Enter Reservation ID to cancel (0 to go back): ", 0, 9999);
        if (id == 0) return;

        Reservation r = reservationManager.findReservationById(id);
        if (r != null && r.getStudentId().equals(student.getStudentId())) {
            if (validator.getValidYesNo("Cancel this reservation?")) {
                if (reservationManager.cancelReservation(id, "Cancelled by student")) {
                    Item item = inventoryManager.findItemByCode(r.getItemCode());
                    if (item != null) item.addQuantity(r.getQuantity());
                    System.out.println("Reservation cancelled.");
                }
            }
        } else {
            System.out.println("Invalid reservation ID.");
        }
    }
    
    private void showPaymentStatus() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          PAYMENT STATUS");
        System.out.println("=".repeat(80));
        
        List<Reservation> myReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        
        if (myReservations.isEmpty()) {
            System.out.println("\nYou have no reservations yet.");
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
            return;
        }
        
        double totalUnpaid = 0;
        double totalPaid = 0;
        int unpaidCount = 0;
        int paidCount = 0;
        
        System.out.println("\n=== YOUR RESERVATIONS WITH PAYMENT STATUS ===");
        System.out.println("ID   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|---------------------------|-----|----------|----------|------------|------------------------------");
        
        for (Reservation r : myReservations) {
            System.out.printf("%-4d | %-25s | %-3d | ₱%-8.2f | %-8s | %-10s | %s\n",
                r.getReservationId(),
                r.getItemName(),
                r.getQuantity(),
                r.getTotalPrice(),
                r.isPaid() ? "PAID" : "UNPAID",
                r.getPaymentMethod(),
                r.getStatus());
            
            if (r.isPaid()) {
                totalPaid += r.getTotalPrice();
                paidCount++;
            } else if (!r.getStatus().equals("CANCELLED")) {
                totalUnpaid += r.getTotalPrice();
                unpaidCount++;
            }
        }
        
        System.out.println("=".repeat(120));
        System.out.println("\n=== PAYMENT SUMMARY ===");
        System.out.println("Total Reservations: " + myReservations.size());
        System.out.println("Paid Reservations: " + paidCount + " (₱" + String.format("%.2f", totalPaid) + ")");
        System.out.println("Unpaid Reservations: " + unpaidCount + " (₱" + String.format("%.2f", totalUnpaid) + ")");
        
        if (unpaidCount > 0) {
            System.out.println("\n⚠ IMPORTANT: You have " + unpaidCount + " UNPAID reservation(s)!");
            System.out.println("   Total amount to pay: ₱" + String.format("%.2f", totalUnpaid));
            System.out.println("   Please proceed to the CASHIER to make payment.");
            System.out.println("   Your items cannot be approved for pickup until payment is completed.");
        } else {
            System.out.println("\n✓ All your reservations are paid!");
        }
        
        System.out.println("\n=== PAYMENT METHODS AVAILABLE ===");
        System.out.println("   • CASH - Pay at cashier counter");
        System.out.println("   • GCASH - Mobile payment");
        System.out.println("   • CARD - Credit/Debit card payment");
        System.out.println("   • BANK - Bank transfer");
        
        System.out.println("\n=== PAYMENT PROCESS ===");
        System.out.println("   1. Go to the CASHIER");
        System.out.println("   2. Provide your Reservation ID and Student ID");
        System.out.println("   3. Choose your payment method");
        System.out.println("   4. Complete the payment");
        System.out.println("   5. Wait for Admin/Staff to approve your order for pickup");
        System.out.println("   6. Pickup your items when status shows 'APPROVED - READY FOR PICKUP'");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }
    
    private void pickupItem() {
        System.out.println("\n=== PICKUP ITEM ===");
        
        List<Reservation> myReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        List<Reservation> readyForPickup = new java.util.ArrayList<>();
        
        // Filter only approved and paid reservations
        for (Reservation r : myReservations) {
            if (r.getStatus().equals("APPROVED - READY FOR PICKUP") && r.isPaid()) {
                readyForPickup.add(r);
            }
        }
        
        if (readyForPickup.isEmpty()) {
            System.out.println("No items ready for pickup.");
            System.out.println("\nPossible reasons:");
            System.out.println("   • Your reservations are not yet approved by Admin/Staff");
            System.out.println("   • You haven't paid yet (pay at CASHIER first)");
            System.out.println("   • You've already picked up all your items");
            return;
        }
        
        System.out.println("\n=== ITEMS READY FOR PICKUP ===");
        System.out.println("ID   | Item Code | Item Name                 | Qty | Total    | Payment Method");
        System.out.println("-----|-----------|---------------------------|-----|----------|---------------");
        for (Reservation r : readyForPickup) {
            System.out.printf("%-4d | %-9d | %-25s | %-3d | ₱%-7.2f | %s\n",
                r.getReservationId(), r.getItemCode(), r.getItemName(), 
                r.getQuantity(), r.getTotalPrice(), r.getPaymentMethod());
        }
        
        int reservationId = validator.getValidInteger("\nEnter Reservation ID to pickup (0 to cancel): ", 0, 9999);
        if (reservationId == 0) return;
        
        Reservation r = reservationManager.findReservationById(reservationId);
        
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        // Verify it's the student's reservation
        if (!r.getStudentId().equals(student.getStudentId())) {
            System.out.println("This reservation does not belong to you!");
            return;
        }
        
        // Verify it's approved and paid
        if (!r.getStatus().equals("APPROVED - READY FOR PICKUP")) {
            System.out.println("This item is not yet approved for pickup.");
            System.out.println("Current status: " + r.getStatus());
            return;
        }
        
        if (!r.isPaid()) {
            System.out.println("This reservation is not paid yet. Please pay at CASHIER first.");
            return;
        }
        
        System.out.println("\n=== PICKUP CONFIRMATION ===");
        System.out.println("Reservation ID: " + r.getReservationId());
        System.out.println("Item: " + r.getItemName());
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Total Paid: ₱" + r.getTotalPrice());
        System.out.println("Payment Method: " + r.getPaymentMethod());
        
        System.out.println("\n⚠ By confirming pickup, you acknowledge:");
        System.out.println("   • You have received all items in good condition");
        System.out.println("   • The transaction will be marked as COMPLETED");
        System.out.println("   • Items were already removed from inventory during approval");
        
        if (validator.getValidYesNo("\nConfirm that you have picked up these items?")) {
            // Mark as completed (stock was already deducted during staff approval)
            reservationManager.updateReservationStatus(reservationId, "COMPLETED", "Picked up by student");
            
            System.out.println("\n✓ Pickup confirmed successfully!");
            System.out.println("✓ Reservation marked as COMPLETED");
            System.out.println("\nThank you for using STI Merch System!");
            System.out.println("Enjoy your items! 🎉");
        } else {
            System.out.println("Pickup cancelled.");
        }
    }
    
    private void viewReceipts() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                         YOUR RECEIPTS                         ");
        System.out.println("=".repeat(70));
        
        List<Reservation> paidReservations = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStudentId().equals(student.getStudentId()) && r.isPaid())
            .toList();
        
        if (paidReservations.isEmpty()) {
            System.out.println("\nNo receipts found. You haven't made any payments yet.");
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
            return;
        }
        
        System.out.println("\n[1] View All Receipts");
        System.out.println("[2] View Specific Receipt");
        System.out.println("[0] Back");
        
        int choice = validator.getValidInteger("Enter choice: ", 0, 2);
        
        if (choice == 0) return;
        
        if (choice == 1) {
            System.out.println("\n=== ALL YOUR RECEIPTS ===");
            System.out.println("Receipt# | Item Name                 | Qty | Amount    | Payment   | Status");
            System.out.println("---------|---------------------------|-----|-----------|-----------|---------------------------");
            for (Reservation r : paidReservations) {
                System.out.printf("%-8d | %-25s | %-3d | ₱%-8.2f | %-9s | %s\n",
                    r.getReservationId(), r.getItemName(), r.getQuantity(), 
                    r.getTotalPrice(), r.getPaymentMethod(), r.getStatus());
            }
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
        } else if (choice == 2) {
            int receiptId = validator.getValidInteger("\nEnter Receipt/Reservation ID: ", 1000, 9999);
            Reservation r = reservationManager.findReservationById(receiptId);
            if (r == null || !r.getStudentId().equals(student.getStudentId()) || !r.isPaid()) {
                System.out.println("Receipt not found or invalid.");
                return;
            }
            printDetailedReceipt(r);
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
        }
    }
    
    private void printDetailedReceipt(Reservation r) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   OFFICIAL RECEIPT                      ");
        System.out.println("                  STI MERCH SYSTEM                       ");
        System.out.println("=".repeat(60));
        System.out.println("Receipt #: " + r.getReservationId());
        System.out.println("-".repeat(60));
        System.out.println("CUSTOMER INFORMATION:");
        System.out.println("  Name       : " + r.getStudentName());
        System.out.println("  Student ID : " + r.getStudentId());
        System.out.println("  Course     : " + r.getCourse());
        System.out.println("-".repeat(60));
        System.out.println("ITEM DETAILS:");
        System.out.println("  Item Code  : " + r.getItemCode());
        System.out.println("  Item Name  : " + r.getItemName());
        System.out.println("  Quantity   : " + r.getQuantity());
        System.out.println("  Unit Price : ₱" + String.format("%.2f", r.getTotalPrice() / r.getQuantity()));
        System.out.println("-".repeat(60));
        System.out.println("PAYMENT DETAILS:");
        System.out.println("  Subtotal   : ₱" + String.format("%.2f", r.getTotalPrice()));
        System.out.println("  TOTAL      : ₱" + String.format("%.2f", r.getTotalPrice()));
        System.out.println("  Method     : " + r.getPaymentMethod());
        System.out.println("  Status     : PAID");
        System.out.println("-".repeat(60));
        System.out.println("ORDER STATUS:");
        System.out.println("  Current Status: " + r.getStatus());
        if (r.getStatus().equals("PENDING")) {
            System.out.println("  Next Step: Wait for Admin/Staff approval");
        } else if (r.getStatus().equals("APPROVED - READY FOR PICKUP")) {
            System.out.println("  Next Step: Go to [7] Pickup Item to collect your order");
        } else if (r.getStatus().equals("COMPLETED")) {
            System.out.println("  Order Complete: Item has been picked up");
        }
        System.out.println("-".repeat(60));
        System.out.println("           Thank you for your purchase!                  ");
        System.out.println("      Please keep this receipt for your records.         ");
        System.out.println("=".repeat(60));
    }
}