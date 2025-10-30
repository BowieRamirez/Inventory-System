package student;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.Receipt;
import inventory.Item;
import utils.InputValidator;

import java.util.List;
import java.util.ArrayList;

public class StudentInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private InputValidator validator;
    private Student student;

    public StudentInterface(InventoryManager inventoryManager, ReservationManager reservationManager,
                            ReceiptManager receiptManager, InputValidator validator, Student student) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.receiptManager = receiptManager;
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
            System.out.println("[1] Reservation Status");
            System.out.println("[2] Stock Page");
            System.out.println("[3] Next (Go to Payments and Receipts)");
            System.out.println("[4] Logout");
            System.out.println("[5] Exit System");
            
            int choice = validator.getValidInteger("Enter your choice: ", 1, 5);
            
            switch (choice) {
                case 1:
                    showYourReservations();
                    break;
                case 2:
                    showStockPage();
                    break;
                case 3:
                    showPaymentsAndReceipts();
                    break;
                case 4:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    break;
                case 5:
                    System.out.println("Exiting system...");
                    System.exit(0);
                    break;
            }
        }
    }

    private void showPaymentsAndReceipts() {
        while (true) {
            System.out.println("\n=================================");
            System.out.println("    PAYMENTS AND RECEIPTS");
            System.out.println("   Welcome, " + student.getFullName() + "!");
            System.out.println("   Course: " + student.getCourse());
            System.out.println("=================================");
            System.out.println("[1] Payment Status");
            System.out.println("[2] View Receipts");
            System.out.println("[3] Pickup Item");
            System.out.println("[4] Back (Return to Main Homepage)");
            
            int choice = validator.getValidInteger("Enter your choice: ", 1, 4);
            
            switch (choice) {
                case 1:
                    showPaymentStatus();
                    break;
                case 2:
                    viewReceipts();
                    break;
                case 3:
                    pickupItem();
                    break;
                case 4:
                    return; // Return to main homepage
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
        System.out.println("   • Use number keys (1-5) to select menu options");
        System.out.println("   • Press [0] to go back to the previous menu");
        System.out.println("   • Press [0] when entering codes to cancel and return");
        System.out.println("   • All reservations require confirmation (y/n)");
        
        System.out.println("\nHOW TO RESERVE ITEMS:");
        System.out.println("   Step 1: Select [2] Stock Page from homepage");
        System.out.println("   Step 2: Select [4] Reserve Item");
        System.out.println("   Step 3: View available items for " + student.getCourse());
        System.out.println("           • You can reserve items for YOUR COURSE");
        System.out.println("           • You can also reserve STI SPECIAL merchandise");
        System.out.println("   Step 4: Note the item code (e.g., 1001, 2002)");
        System.out.println("   Step 5: Enter item code or [0] to go back");
        System.out.println("   Step 6: Select your size (XS, S, M, L, XL, XXL, One Size)");
        System.out.println("   Step 7: Enter quantity (must not exceed available stock)");
        System.out.println("   Step 8: Review details and confirm (y/n)");
        System.out.println("   Step 9: Receive your Reservation ID");
        System.out.println("           • Save this ID to track your order!");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View Your Course Items - See all items for " + student.getCourse());
        System.out.println("   [2] View STI Special Items - Browse special merchandise");
        System.out.println("       • Anniversary Shirts, PE Uniforms, Washday Shirts, etc.");
        System.out.println("   [3] Search by Code - Find specific item using code (1000-9999)");
        System.out.println("       • Enter item code or [0] to go back");
        System.out.println("   [4] Reserve Item - Make a new reservation");
        System.out.println("   [0] Back - Return to homepage");
        
        System.out.println("\nRESERVATION STATUS (Option [1] from Homepage):");
        System.out.println("   [1] View All Reservations - See complete history");
        System.out.println("   [2] View Pending - See reservations awaiting approval");
        System.out.println("   [3] Cancel Reservation - Cancel a pending reservation");
        System.out.println("       • You can only cancel PENDING and UNPAID reservations");
        System.out.println("       • Stock will be returned to inventory");
        System.out.println("   [4] Return Item (Refund) - Return completed items within 10 days");
        System.out.println("       • Items must be in original condition");
        System.out.println("       • Full refund will be processed");
        System.out.println("   [0] Back - Return to homepage");
        
        System.out.println("\nPAYMENTS AND RECEIPTS (Option [3] from Homepage):");
        System.out.println("   [1] Payment Status - Check payment status of your reservations");
        System.out.println("   [2] View Receipts - See your payment receipts");
        System.out.println("   [3] Pickup Item - Confirm item pickup after payment");
        System.out.println("   [4] Back - Return to main homepage");
        
        System.out.println("\nRESERVATION STATUS MEANINGS:");
        System.out.println("   PENDING - Your reservation is awaiting admin approval");
        System.out.println("   APPROVED - WAITING FOR PAYMENT - Approved! Go to cashier to pay");
        System.out.println("   PAID - READY FOR PICKUP - Your items are ready! Go pickup");
        System.out.println("   COMPLETED - You have successfully picked up your items");
        System.out.println("   RETURN REQUESTED - You submitted a return request, waiting for admin approval");
        System.out.println("   RETURNED - REFUNDED - Item returned and refund processed");
        System.out.println("   CANCELLED - Reservation was cancelled");
        
        System.out.println("\n✓ RETURN POLICY (NEW!):");
        System.out.println("    You have 10 DAYS from pickup to return items for a full refund");
        System.out.println("    Items must be in ORIGINAL CONDITION (unworn, tags attached)");
        System.out.println("    Return requests must be made through Admin/Staff");
        System.out.println("    After 10 days, returns will NOT be accepted");
        System.out.println("    Stock will be restored to inventory upon return");
        
        System.out.println("\n✓ RETURN POLICY (NEW!):");
        System.out.println("    You have 10 DAYS from pickup to return items for a full refund");
        System.out.println("    Items must be in ORIGINAL CONDITION (unworn, tags attached)");
        System.out.println("    Return requests must be made through Admin/Staff");
        System.out.println("    After 10 days, returns will NOT be accepted");
        System.out.println("    Stock will be restored to inventory upon return");
        
        System.out.println("\n TIPS FOR SUCCESSFUL ORDERING:");
        System.out.println("    Check available stock before reserving");
        System.out.println("    Double-check your size selection");
        System.out.println("    Save your Reservation ID for tracking");
        System.out.println("    Check 'Reservation Status' regularly for updates");
        System.out.println("    When status is 'APPROVED - WAITING FOR PAYMENT', go to cashier");
        System.out.println("    Bring your Student ID and Reservation ID for payment");
        System.out.println("    After payment, status changes to 'PAID - READY FOR PICKUP'");
        System.out.println("    Keep items in original condition for 10-day return period");
        
        System.out.println("\n IMPORTANT NOTES:");
        System.out.println("    You can only reserve items for YOUR course (" + student.getCourse() + ")");
        System.out.println("    STI Special items are available to ALL students");
        System.out.println("    Sizes available: XS, S, M, L, XL, XXL, One Size");
        System.out.println("    Items must be collected within 7 days of approval");
        System.out.println("    You can cancel PENDING and UNPAID reservations anytime");
        System.out.println("    Cannot cancel PAID or COMPLETED reservations");
        System.out.println("    Returns accepted within 10 days of pickup (COMPLETED status)");
        
        System.out.println("\n NEED HELP?");
        System.out.println("    Contact your admin if you have questions");
        System.out.println("    Check stock availability before reserving");
        System.out.println("    Use [0] anytime to safely go back");
        
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
                    System.out.println("\n Reservation created successfully!");
                    System.out.println("Reservation ID: " + res.getReservationId());
                    System.out.println("Total Amount: ₱" + totalPrice);
                    System.out.println("Payment Status: UNPAID");
                    System.out.println("\n IMPORTANT!!: Please proceed to CASHIER to make payment!");
                    System.out.println("Status: " + res.getStatus());
                    
                    // Ask if user wants to reserve another item after successful reservation
                    if (!validator.getValidYesNo("Reserve another item?")) {
                        return;
                    }
                } else {
                    System.out.println("Failed to create reservation.");
                    return;
                }
            } else {
                // User cancelled the reservation
                System.out.println("Reservation cancelled. Returning to homepage...");
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
            System.out.println("[4] Reserve Item");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 4);
            
            switch (choice) {
                case 0: return;
                case 1: inventoryManager.displayItemsByCourse(student.getCourse()); break;
                case 2: inventoryManager.displayItemsByCourse("STI Special"); break;
                case 3: searchItem(); break;
                case 4: reserveItem(); break;
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
            // Get student's course
            String studentCourse = student.getCourse();
            String itemCourse = item.getCourse();
            
            // Check if the item belongs to the student's course or is a STI Special item
            boolean isStiSpecial = itemCourse.equalsIgnoreCase("STI SPECIAL");
            boolean isSameCourse = studentCourse.equalsIgnoreCase(itemCourse);
            
            if (!isSameCourse && !isStiSpecial) {
                System.out.println("\n ERROR!: You can only search for items within your own course.");
                System.out.println("Your course: " + studentCourse);
                System.out.println("Item course: " + itemCourse);
                System.out.println("\nPlease search for items belonging to your course or STI SPECIAL items.");
                return;
            }
            
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
            System.out.println("[4] Return Item (Refund)");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 4);
            
            switch (choice) {
                case 0: return;
                case 1: 
                    reservationManager.displayReservationsByStudent(student.getStudentId());
                    System.out.println("\nPress [0] to go back...");
                    validator.getValidInteger("", 0, 0);
                    break;
                case 2: 
                    displayPendingReservations();
                    System.out.println("\nPress [0] to go back...");
                    validator.getValidInteger("", 0, 0);
                    break;
                case 3: cancelReservation(); break;
                case 4: returnItem(); break;
            }
        }
    }

    private void displayPendingReservations() {
        List<Reservation> all = reservationManager.getReservationsByStudent(student.getStudentId());
        System.out.println("\n=== PENDING RESERVATIONS ===");
        
        List<Reservation> pendingList = new ArrayList<>();
        for (Reservation r : all) {
            if (r.getStatus().equals("PENDING")) {
                pendingList.add(r);
            }
        }
        
        if (pendingList.isEmpty()) {
            System.out.println("No pending reservations.");
            return;
        }
        
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|------------------------------");
        for (Reservation r : pendingList) {
            System.out.println(r);
        }
    }

    private void cancelReservation() {
        reservationManager.displayReservationsByStudent(student.getStudentId());
        int id = validator.getValidInteger("\nEnter Reservation ID to cancel (0 to go back): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }

        Reservation r = reservationManager.findReservationById(id);
        if (r != null && r.getStudentId().equals(student.getStudentId())) {
            // Check if reservation is already paid
            if (r.isPaid()) {
                System.out.println("\n⚠ ERROR: Cannot cancel paid reservation!");
                System.out.println("Reason: You have already paid for this reservation.");
                System.out.println("Please contact the cashier or admin for assistance.");
                return;
            }
            
            if (validator.getValidYesNo("Cancel this reservation?")) {
                if (reservationManager.cancelReservation(id, "Cancelled by student")) {
                    Item item = inventoryManager.findItemByCode(r.getItemCode());
                    if (item != null) item.addQuantity(r.getQuantity());
                    System.out.println("✓ Reservation cancelled.");
                }
            }
        } else {
            System.out.println("Invalid reservation ID.");
        }
    }
    
    private void returnItem() {
        System.out.println("\n=== RETURN ITEM (REFUND) ===");
        
        List<Reservation> myReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        List<Reservation> eligibleForReturn = new ArrayList<>();
        
        // Filter completed items eligible for return
        for (Reservation r : myReservations) {
            if (r.getStatus().equals("COMPLETED") && r.isEligibleForReturn()) {
                eligibleForReturn.add(r);
            }
        }
        
        if (eligibleForReturn.isEmpty()) {
            System.out.println("No items eligible for return.");
            System.out.println("\nℹ Note: Only completed items within 10 days of pickup can be returned.");
            return;
        }
        
        System.out.println("\n=== ITEMS ELIGIBLE FOR RETURN ===");
        System.out.println("ID   | Item Name                 | Size | Qty | Total    | Days Left | Completed Date");
        System.out.println("-----|---------------------------|------|-----|----------|-----------|-------------------");
        for (Reservation r : eligibleForReturn) {
            System.out.printf("%-4d | %-25s | %-4s | %-3d | ₱%-7.2f | %-9d | %s\n",
                r.getReservationId(), 
                r.getItemName(),
                r.getSize(),
                r.getQuantity(), 
                r.getTotalPrice(),
                r.getDaysUntilReturnExpires(),
                r.getCompletedDate() != null ? r.getCompletedDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "N/A");
        }
        
        int id = validator.getValidInteger("\nEnter Reservation ID to return (0 to cancel): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Reservation r = reservationManager.findReservationById(id);
        
        if (r == null || !r.getStudentId().equals(student.getStudentId())) {
            System.out.println("Invalid reservation ID.");
            return;
        }
        
        if (!r.getStatus().equals("COMPLETED")) {
            System.out.println("⚠ This item cannot be returned - it's not yet completed.");
            return;
        }
        
        if (!r.isEligibleForReturn()) {
            System.out.println("⚠ This item is no longer eligible for return.");
            System.out.println("The 10-day return period has expired.");
            return;
        }
        
        System.out.println("\n=== RETURN DETAILS ===");
        System.out.println("Item: " + r.getItemName());
        System.out.println("Size: " + r.getSize());
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Refund Amount: ₱" + r.getTotalPrice());
        System.out.println("Days Remaining: " + r.getDaysUntilReturnExpires() + " days");
        
        System.out.println("\n⚠ RETURN REQUIREMENTS:");
        System.out.println("  • Item must be in original condition");
        System.out.println("  • All tags and packaging must be intact");
        System.out.println("  • Item must not have been worn or used");
        
        // Ask for return reason
        System.out.println("\n=== REASON FOR RETURN ===");
        System.out.println("Please select the reason for your return:");
        System.out.println("[1] Damaged Item - Item received with defects or damage");
        System.out.println("[2] Wrong Size - Size does not fit properly");
        System.out.println("[3] Changed Mind - No longer want the item");
        System.out.println("[4] Other - Different reason");
        System.out.println("[0] Cancel - Go back");
        
        int reasonChoice = validator.getValidInteger("Enter reason (0-4): ", 0, 4);
        
        if (reasonChoice == 0) {
            System.out.println("Return cancelled.");
            return;
        }
        
        String returnReason = switch (reasonChoice) {
            case 1 -> "Damaged Item";
            case 2 -> "Wrong Size";
            case 3 -> "Changed Mind";
            case 4 -> "Other";
            default -> "Not specified";
        };
        
        System.out.println("\nYou selected: " + returnReason);
        
        if (validator.getValidYesNo("\nDo you want to proceed with the return?")) {
            // Update status to RETURN REQUESTED with reason
            String returnMessage = "Return requested - Reason: " + returnReason;
            reservationManager.updateReservationStatus(id, "RETURN REQUESTED", returnMessage);
            
            System.out.println("\n✓ Return request submitted!");
            System.out.println("✓ Return Reason: " + returnReason);
            System.out.println("✓ Status changed to: RETURN REQUESTED");
            System.out.println("✓ Please bring the item to the Admin or Staff for inspection.");
            System.out.println("✓ Once approved, you will receive a refund of ₱" + r.getTotalPrice());
            System.out.println("\nℹ Note: Go to Admin/Staff to complete the return process.");
            System.out.println("ℹ Note: They will verify your return reason and item condition.");
        } else {
            System.out.println("Return cancelled.");
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
            System.out.println("\n IMPORTANT: You have " + unpaidCount + " UNPAID reservation(s)!");
            System.out.println("   Total amount to pay: ₱" + String.format("%.2f", totalUnpaid));
            System.out.println("   Please proceed to the CASHIER to make payment.");
            System.out.println("   Your items cannot be approved for pickup until payment is completed.");
        } else {
            System.out.println("\n All your reservations are paid!");
        }
        
        System.out.println("\n=== PAYMENT METHODS AVAILABLE ===");
        System.out.println("    CASH - Pay at cashier counter");
        System.out.println("    GCASH - Mobile payment");
        System.out.println("    CARD - Credit/Debit card payment");
        System.out.println("    BANK - Bank transfer");
        
        System.out.println("\n=== PAYMENT PROCESS ===");
        System.out.println("   1. Wait for Admin/Staff to approve your reservation");
        System.out.println("   2. Once approved, status changes to 'APPROVED - WAITING FOR PAYMENT'");
        System.out.println("   3. Go to the CASHIER with your Reservation ID and Student ID");
        System.out.println("   4. Complete the payment (CASH only)");
        System.out.println("   5. Status changes to 'PAID - READY FOR PICKUP'");
        System.out.println("   6. Go to [3] Pickup Item to collect your order");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }
    
    private void pickupItem() {
        System.out.println("\n=== PICKUP ITEM ===");
        
        List<Reservation> myReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        List<Reservation> readyForPickup = new java.util.ArrayList<>();
        
        // Filter only paid and ready for pickup reservations
        for (Reservation r : myReservations) {
            if (r.getStatus().equals("PAID - READY FOR PICKUP") && r.isPaid()) {
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
        
        // Verify it's paid and ready for pickup
        if (!r.getStatus().equals("PAID - READY FOR PICKUP")) {
            System.out.println("This item is not yet ready for pickup.");
            System.out.println("Current status: " + r.getStatus());
            if (r.getStatus().equals("APPROVED - WAITING FOR PAYMENT")) {
                System.out.println("Please pay at CASHIER first!");
            }
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
        
        System.out.println("\n By confirming pickup, you acknowledge:");
        System.out.println("   • You have received all items in good condition");
        System.out.println("   • The transaction will be marked as COMPLETED");
        System.out.println("   • Items were already removed from inventory during approval");
        
        if (validator.getValidYesNo("\nConfirm that you have picked up these items?")) {
            // Mark as completed (stock was already deducted during payment)
            reservationManager.updateReservationStatus(reservationId, "COMPLETED", "Picked up by student");
            
            // Set the completed date for return eligibility tracking
            r.setCompletedDate(java.time.LocalDateTime.now());
            
            // Save the reservation with updated completed date
            reservationManager.saveToDatabase();
            
            // Log the user pickup (stock decrease already happened at payment)
            utils.StockReturnLogger.logUserCompletion(
                r.getStudentId(),
                r.getStudentName(),
                r.getItemCode(),
                r.getItemName(),
                r.getSize(),
                r.getQuantity()
            );
            
            // Find and update existing receipt instead of creating a new one
            Receipt existingReceipt = receiptManager.findPendingReceiptByItemAndBuyer(
                r.getItemCode(), 
                r.getStudentName()
            );
            
            if (existingReceipt != null) {
                // Update the existing receipt status to COMPLETED
                receiptManager.updatePaymentStatus(existingReceipt.getReceiptId(), "COMPLETED");
                System.out.println("\n✓ Pickup confirmed successfully!");
                System.out.println("✓ Reservation marked as COMPLETED");
                System.out.println("✓ Receipt updated - ID: " + existingReceipt.getReceiptId());
            } else {
                System.out.println("\n✓ Pickup confirmed successfully!");
                System.out.println("✓ Reservation marked as COMPLETED");
                System.out.println("⚠ Warning: No pending receipt found for this reservation");
            }
            
            System.out.println("\nℹ RETURN POLICY:");
            System.out.println("  • You have 10 days from today to return this item for a full refund");
            System.out.println("  • After 10 days, returns will not be accepted");
            System.out.println("  • Items must be in original condition for return");
            
            System.out.println("\nThank you for using STI ProWear System!");
            System.out.println("Enjoy your items! ");
        } else {
            System.out.println("Pickup cancelled.");
        }
    }
    
    private void viewReceipts() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("                         YOUR RECEIPTS                         ");
        System.out.println("=".repeat(70));
        
        // Get receipts from ReceiptManager
        List<Receipt> receipts = receiptManager.getReceiptsByBuyer(student.getFullName());
        
        if (receipts.isEmpty()) {
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
            System.out.println("Receipt ID | Date Ordered        | Payment Status            | Qty | Amount    | Item Code | Item Name                      | Size       | Buyer Name");
            System.out.println("-".repeat(170));
            for (Receipt receipt : receipts) {
                System.out.println(receipt);
            }
            System.out.println("\nTotal receipts: " + receipts.size());
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
        } else if (choice == 2) {
            int receiptId = validator.getValidInteger("\nEnter Receipt ID (starts with 1000000): ", 10000000, 99999999);
            Receipt receipt = receiptManager.findReceiptById(receiptId);
            if (receipt == null || !receipt.getBuyerName().equalsIgnoreCase(student.getFullName())) {
                System.out.println("Receipt not found or does not belong to you.");
                return;
            }
            
            System.out.println(receipt.toDetailedFormat());
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
        }
    }
    
    private void printDetailedReceipt(Reservation r) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   OFFICIAL RECEIPT                      ");
        System.out.println("                  STI ProWear System                     ");
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
        } else if (r.getStatus().equals("APPROVED - WAITING FOR PAYMENT")) {
            System.out.println("  Next Step: Go to CASHIER to pay");
        } else if (r.getStatus().equals("PAID - READY FOR PICKUP")) {
            System.out.println("  Next Step: Go to [3] Pickup Item to collect your order");
        } else if (r.getStatus().equals("COMPLETED")) {
            System.out.println("  Order Complete: Item has been picked up");
        }
        System.out.println("-".repeat(60));
        System.out.println("           Thank you for your purchase!                  ");
        System.out.println("      Please keep this receipt for your records.         ");
        System.out.println("=".repeat(60));
    }
}