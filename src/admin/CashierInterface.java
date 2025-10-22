package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Reservation;
import utils.InputValidator;
import java.util.List;

public class CashierInterface {
    private ReservationManager reservationManager;
    private InputValidator validator;

    public CashierInterface(ReservationManager reservationManager, InputValidator validator) {
        this.reservationManager = reservationManager;
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
        System.out.println("   • Use number keys (0-3) to select menu options");
        System.out.println("   • Press [0] to go back to the previous menu");
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
        
        System.out.println("\nTIPS & BEST PRACTICES:");
        System.out.println("   • Regularly check pending reservations");
        System.out.println("   • Update status to 'APPROVED - READY FOR PICKUP' when ready");
        System.out.println("   • Mark as 'COMPLETED' only after student pickup");
        System.out.println("   • Use [0] anytime to safely go back or cancel operations");
        
        System.out.println("\nIMPORTANT NOTES:");
        System.out.println("   • Students can only reserve items for their course + STI Special");
        System.out.println("   • Payment is required during merchandise pickup");
        
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
            reservationManager.updateReservationStatus(id, newStatus, "");
            System.out.println("Status updated to: " + newStatus);
        }
    }

    private void cancelRes() {
        int id = validator.getValidInteger("Enter ID to cancel: ", 1000, 9999);
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
            System.out.println("No unpaid reservations found.");
            return;
        }
        
        System.out.println("\nUnpaid Reservations:");
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|------------------------------");
        for (Reservation r : unpaid) {
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
        
        System.out.println("\n=== SELECT PAYMENT METHOD ===");
        System.out.println("[1] Cash");
        System.out.println("[2] GCash");
        System.out.println("[3] Card");
        System.out.println("[4] Bank Transfer");
        System.out.println("[0] Cancel");
        
        int paymentChoice = validator.getValidInteger("Select payment method: ", 0, 4);
        if (paymentChoice == 0) return;
        
        String paymentMethod = switch (paymentChoice) {
            case 1 -> "CASH";
            case 2 -> "GCASH";
            case 3 -> "CARD";
            case 4 -> "BANK";
            default -> "CASH";
        };
        
        if (validator.getValidYesNo("\nConfirm payment of ₱" + r.getTotalPrice() + " via " + paymentMethod + "?")) {
            if (reservationManager.markAsPaid(reservationId, paymentMethod)) {
                // Generate and display receipt
                printReceipt(r, paymentMethod);
                System.out.println("\n⚠ Student can now wait for Admin/Staff approval for pickup.");
            } else {
                System.out.println("Failed to process payment.");
            }
        }
    }
    
    private void printReceipt(Reservation r, String paymentMethod) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                   OFFICIAL RECEIPT                      ");
        System.out.println("                  STI MERCH SYSTEM                       ");
        System.out.println("=".repeat(60));
        System.out.println("Date: " + java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
        System.out.println("  Method     : " + paymentMethod);
        System.out.println("  Status     : PAID");
        System.out.println("-".repeat(60));
        System.out.println("NEXT STEPS:");
        System.out.println("  1. Wait for Admin/Staff approval");
        System.out.println("  2. Check your reservation status regularly");
        System.out.println("  3. Pickup when status is 'APPROVED - READY FOR PICKUP'");
        System.out.println("-".repeat(60));
        System.out.println("           Thank you for your purchase!                  ");
        System.out.println("      Please keep this receipt for your records.         ");
        System.out.println("=".repeat(60));
        System.out.println("\n✓ Receipt printed successfully!");
        System.out.println("✓ Payment processed: ₱" + String.format("%.2f", r.getTotalPrice()));
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