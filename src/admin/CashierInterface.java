package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.Receipt;
import utils.InputValidator;
import java.util.ArrayList;
import java.util.List;

public class CashierInterface {
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private InputValidator validator;

    public CashierInterface(ReservationManager reservationManager, ReceiptManager receiptManager, InputValidator validator) {
        this.reservationManager = reservationManager;
        this.receiptManager = receiptManager;
        this.validator = validator;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== CASHIER HOMEPAGE ===");
            System.out.println("[1] Help");
            System.out.println("[2] Process Payment");
            System.out.println("[3] View All Reservations");
            System.out.println("[4] Logout");
            System.out.println("[0] Exit");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 4);
            
            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                case 1:
                    showCashierHelp();
                    break;
                case 2:
                    processPayment();
                    break;
                case 3:
                    reservationManager.displayAllReservations();
                    break;
                case 4:
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showCashierHelp() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                          CASHIER HELP GUIDE");
        System.out.println("=".repeat(80));
        
        System.out.println("\nNAVIGATION BASICS:");
        System.out.println("    Use number keys (0-4) to select menu options");
        System.out.println("    Press [0] to go back to the previous menu");
        System.out.println("    All actions require confirmation (y/n) for safety");
        
        System.out.println("\nPROCESS PAYMENT:");
        System.out.println("    Enter student's reservation ID to process payment");
        System.out.println("    Verify student information and order details");
        System.out.println("    Process payment and generate receipt");
        System.out.println("    After payment, status changes to PAID - READY FOR PICKUP");
        System.out.println("    Student must have APPROVED - WAITING FOR PAYMENT status to pay");
        
        System.out.println("\nVIEW ALL RESERVATIONS:");
        System.out.println("    See all student reservations with full details");
        System.out.println("    Check reservation status (PENDING, APPROVED, COMPLETED, CANCELLED)");
        System.out.println("    Find reservation IDs for payment processing");
        
        System.out.println("\nPAYMENT PROCESS WORKFLOW:");
        System.out.println("   Step 1: Student arrives with Reservation ID");
        System.out.println("   Step 2: Select [2] Process Payment from menu");
        System.out.println("   Step 3: Enter the student's Reservation ID");
        System.out.println("   Step 4: Verify the student's information and items");
        System.out.println("   Step 5: Collect payment from student");
        System.out.println("   Step 6: System generates receipt automatically");
        System.out.println("   Step 7: Reservation marked as COMPLETED");
        
        System.out.println("\nTIPS & BEST PRACTICES:");
        System.out.println("    Verify student ID before processing payment");
        System.out.println("    Double-check order details with student");
        System.out.println("    Ensure correct amount is collected");
        System.out.println("    Print or provide receipt to student");
        System.out.println("    Use [0] anytime to safely go back or cancel operations");
        
        System.out.println("\nIMPORTANT NOTES:");
        System.out.println("    Only APPROVED reservations can be processed for payment");
        System.out.println("    Students can only reserve items for their course + STI Special");
        System.out.println("    Payment is required during merchandise pickup");
        System.out.println("    Cashier CANNOT access Account Management or modify inventory");
        
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
        for (Reservation r : reservationManager.getPendingReservations()) {
            System.out.println(r);
        }
    }

    private void updateStatus() {
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
        
        System.out.println("Current: " + r);
        System.out.println("\n[1] PENDING");
        System.out.println("[2] APPROVED - WAITING FOR PAYMENT");
        System.out.println("[3] PAID - READY FOR PICKUP");
        System.out.println("[4] COMPLETED");
        System.out.println("[5] CANCELLED");
        
        int status = validator.getValidInteger("Select status: ", 1, 5);
        String newStatus = switch (status) {
            case 1 -> "PENDING";
            case 2 -> "APPROVED - WAITING FOR PAYMENT";
            case 3 -> "PAID - READY FOR PICKUP";
            case 4 -> "COMPLETED";
            case 5 -> "CANCELLED";
            default -> r.getStatus();
        };
        
        if (validator.getValidYesNo("Confirm status change?")) {
            reservationManager.updateReservationStatus(id, newStatus, "");
            System.out.println("Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        int id = validator.getValidInteger("Enter ID to cancel (or 0 to cancel): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        if (validator.getValidYesNo("Confirm cancellation?")) {
            if (reservationManager.cancelReservation(id, "Cancelled by cashier")) {
                System.out.println("Cancelled.");
            }
        }
    }
    
    private void processPayment() {
        System.out.println("\n=== PROCESS PAYMENT ===");
        
        List<Reservation> unpaid = reservationManager.getUnpaidReservations();
        if (unpaid.isEmpty()) {
            System.out.println("No reservations waiting for payment.");
            return;
        }
        
        // Filter only APPROVED - WAITING FOR PAYMENT status
        List<Reservation> awaitingPayment = new ArrayList<>();
        for (Reservation r : unpaid) {
            if ("APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
                awaitingPayment.add(r);
            }
        }
        
        if (awaitingPayment.isEmpty()) {
            System.out.println("No reservations approved and waiting for payment.");
            System.out.println("Note: Admin/Staff must approve reservations before payment can be processed.");
            return;
        }
        
        System.out.println("\nReservations Approved and Waiting for Payment:");
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|------------------------------");
        for (Reservation r : awaitingPayment) {
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-8.2f | %s\n",
                r.getReservationId(), r.getStudentName(), r.getStudentId(), 
                r.getItemCode(), r.getItemName(), r.getQuantity(), r.getTotalPrice(), r.getStatus());
        }
        
        int reservationId = validator.getValidInteger("\nEnter Reservation ID to process payment (0 to cancel): ", 0, 9999);
        if (reservationId == 0) return;
        
        Reservation r = reservationManager.findReservationById(reservationId);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        if (!r.getStatus().equals("APPROVED - WAITING FOR PAYMENT")) {
            System.out.println("Error: This reservation must be approved by Admin/Staff before payment.");
            return;
        }
        
        if (r.isPaid()) {
            System.out.println("This reservation is already paid!");
            return;
        }
        
        System.out.println("\n=== PAYMENT DETAILS ===");
        System.out.println("Reservation ID: " + r.getReservationId());
        System.out.println("Student: " + r.getStudentName() + " (" + r.getStudentId() + ")");
        System.out.println("Item: " + r.getItemName());
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Total Amount: ₱" + r.getTotalPrice());
        
        System.out.println("\n=== PAYMENT METHOD ===");
        System.out.println("Payment Method: CASH ONLY");
        
        String paymentMethod = "CASH";
        
        if (validator.getValidYesNo("\nConfirm payment of ₱" + r.getTotalPrice() + " via CASH?")) {
            if (reservationManager.markAsPaid(reservationId, paymentMethod)) {
                // Get updated reservation to check status
                r = reservationManager.findReservationById(reservationId);
                
                // Create receipt in database with updated status
                String receiptStatus = r.getStatus().equals("PAID - READY FOR PICKUP") 
                    ? "PAID - READY FOR PICKUP" 
                    : "Waiting for Approval";
                    
                Receipt receipt = receiptManager.createReceipt(
                    receiptStatus, 
                    r.getQuantity(), 
                    r.getTotalPrice(), 
                    r.getItemCode(),
                    r.getItemName(),
                    r.getSize(),
                    r.getStudentName()
                );
                
                // Generate and display receipt
                printReceipt(r, paymentMethod, receipt.getReceiptId());
                System.out.println("\n✓ Receipt ID: " + receipt.getReceiptId() + " has been saved to database.");
                
                if (r.getStatus().equals("PAID - READY FOR PICKUP")) {
                    System.out.println("✓ Payment successful!");
                    System.out.println("✓ Status: PAID - READY FOR PICKUP");
                    System.out.println("✓ Stock deducted from inventory.");
                    System.out.println("✓ Student can now pickup the item.");
                } else {
                    System.out.println("⚠ Payment processed but status update failed.");
                }
            } else {
                System.out.println("Failed to process payment.");
            }
        }
    }
    
    private void printReceipt(Reservation r, String paymentMethod, int receiptId) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   OFFICIAL RECEIPT                      ");
        System.out.println("                  STI ProWear System                     ");
        System.out.println("=".repeat(60));
        System.out.println("Date: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Receipt ID: " + receiptId);
        System.out.println("Reservation ID: " + r.getReservationId());
        System.out.println("-".repeat(60));
        System.out.println("CUSTOMER INFORMATION:");
        System.out.println("  Name       : " + r.getStudentName());
        System.out.println("  Student ID : " + r.getStudentId());
        System.out.println("  Course     : " + r.getCourse());
        System.out.println("-".repeat(60));
        System.out.println("ITEM DETAILS:");
        System.out.println("  Item Code  : " + r.getItemCode());
        System.out.println("  Item Name  : " + r.getItemName());
        System.out.println("  Size       : " + r.getSize());
        System.out.println("  Quantity   : " + r.getQuantity());
        System.out.println("  Unit Price : ₱" + String.format("%.2f", r.getTotalPrice() / r.getQuantity()));
        System.out.println("-".repeat(60));
        System.out.println("PAYMENT DETAILS:");
        System.out.println("  Subtotal   : ₱" + String.format("%.2f", r.getTotalPrice()));
        System.out.println("  TOTAL      : ₱" + String.format("%.2f", r.getTotalPrice()));
        System.out.println("  Method     : " + paymentMethod);
        System.out.println("  Status     : PAID - READY FOR PICKUP");
        System.out.println("-".repeat(60));
        System.out.println("NEXT STEPS:");
        System.out.println("  1. Your item is now READY FOR PICKUP!");
        System.out.println("  2. Go to Student Interface > [3] Pickup Item");
        System.out.println("  3. Provide your Reservation ID to complete pickup");
        System.out.println("-".repeat(60));
        System.out.println("           Thank you for your purchase!                  ");
        System.out.println("      Please keep this receipt for your records.         ");
        System.out.println("=".repeat(60));
        System.out.println("\n Receipt printed successfully!");
        System.out.println(" Payment processed: ₱" + String.format("%.2f", r.getTotalPrice()));
    }
    
    private void viewUnpaidReservations() {
        System.out.println("\n=== UNPAID RESERVATIONS ===");
        List<Reservation> unpaid = reservationManager.getUnpaidReservations();
        
        if (unpaid.isEmpty()) {
            System.out.println("No unpaid reservations found.");
            return;
        }
        
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|------------------------------");
        for (Reservation r : unpaid) {
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-8.2f | %s\n",
                r.getReservationId(), r.getStudentName(), r.getStudentId(), 
                r.getItemCode(), r.getItemName(), r.getQuantity(), r.getTotalPrice(), r.getStatus());
        }
    }
}