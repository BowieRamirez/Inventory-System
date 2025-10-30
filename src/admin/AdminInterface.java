package admin;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.Receipt;
import inventory.Item;
import utils.InputValidator;
import student.Student;

import java.util.List;

public class AdminInterface {
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private InputValidator validator;
    private List<Student> registeredStudents;

    public AdminInterface(InventoryManager inventoryManager, ReservationManager reservationManager, 
                          ReceiptManager receiptManager, InputValidator validator, List<Student> registeredStudents) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.receiptManager = receiptManager;
        this.validator = validator;
        this.registeredStudents = registeredStudents;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n=== ADMIN HOMEPAGE ===");
            System.out.println("[1] Help");
            System.out.println("[2] User Reservations");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Stock Management");
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
        while (true) {
            System.out.println("\n=== ACCOUNT MANAGEMENT ===");
            System.out.println("[1] Change User Password");
            System.out.println("[2] Activate/Deactivate Account");
            System.out.println("[3] View All Accounts");
            System.out.println("[0] Back to Admin Main Page");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: changeUserPassword(); break;
                case 2: manageAccountStatus(); break;
                case 3: viewAllAccounts(); break;
            }
        }
    }
    
    private void viewAllAccounts() {
        System.out.println("\n=== ALL REGISTERED ACCOUNTS ===");
        System.out.println("Total Registered Students: " + registeredStudents.size());
        
        if (registeredStudents.isEmpty()) {
            System.out.println("No registered students found.");
            return;
        }
        
        System.out.println("\nID   | Student ID    | Name                           | Course    | Gender | Status");
        System.out.println("-------|---------------|--------------------------------|-----------|--------|-------------");

        int count = 1;
        for (Student student : registeredStudents) {
            System.out.printf("%-4d | %-13s | %-30s | %-9s | %-6s | %-12s\n",
                count++,
                student.getStudentId(),
                student.getFullName(),
                student.getCourse(),
                student.getGender(),
                student.getAccountStatus());
        }
        
        System.out.println("\nPress Enter to go back...");
        validator.waitForEnter("");
    }
    
    private void changeUserPassword() {
        System.out.println("\n=== CHANGE USER PASSWORD ===");
        
        if (registeredStudents.isEmpty()) {
            System.out.println("No registered students found.");
            return;
        }
        
        String studentId = validator.getValidStudentId("Enter Student ID (or 0 to cancel): ");
        if (studentId.equals("0")) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Student student = findStudentById(studentId);
        if (student == null) {
            System.out.println("Student ID not found.");
            return;
        }
        
        System.out.println("\nStudent Found:");
        System.out.println("Name: " + student.getFullName());
        System.out.println("Course: " + student.getCourse());
        
        String newPassword = validator.getValidNonEmptyString("\nEnter new password (8-20 chars): ", "Password");
        if (newPassword.equals("0")) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        if (newPassword.length() < 8 || newPassword.length() > 20) {
            System.out.println("Error: Password must be 8-20 characters long.");
            return;
        }
        
        if (validator.getValidYesNo("Confirm password change for " + student.getFullName() + "?")) {
            student.setPassword(newPassword);
            utils.FileStorage.updateStudent(registeredStudents, student);
            System.out.println("✓ Password changed successfully!");
            System.out.println("✓ Changes saved to database.");
        } else {
            System.out.println("Password change cancelled.");
        }
    }
    
    private void manageAccountStatus() {
        System.out.println("\n=== ACTIVATE/DEACTIVATE ACCOUNT ===");
        
        if (registeredStudents.isEmpty()) {
            System.out.println("No registered students found.");
            return;
        }
        
        String studentId = validator.getValidStudentId("Enter Student ID (or 0 to cancel): ");
        if (studentId.equals("0")) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Student student = findStudentById(studentId);
        if (student == null) {
            System.out.println("Student ID not found.");
            return;
        }
        
        System.out.println("\nAccount Details:");
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Name: " + student.getFullName());
        System.out.println("Course: " + student.getCourse());
        System.out.println("Current Status: " + student.getAccountStatus());
        
        // Determine action based on current status
        if (student.isActive()) {
            // Account is active, offer to deactivate
            System.out.println("\n⚠ DEACTIVATE ACCOUNT");
            System.out.println("This will prevent the student from logging in.");
            System.out.println("Use this when a student is not enrolled anymore.");
            
            if (validator.getValidYesNo("\nAre you sure you want to deactivate this account?")) {
                student.setActive(false);
                utils.FileStorage.updateStudent(registeredStudents, student);
                System.out.println("✓ Account deactivated successfully!");
                System.out.println("✓ Changes saved to database.");
                System.out.println("The student can no longer log in until reactivated.");
            } else {
                System.out.println("Account deactivation cancelled.");
            }
        } else {
            // Account is deactivated, offer to reactivate
            System.out.println("\n✓ REACTIVATE ACCOUNT");
            System.out.println("This will allow the student to log in again.");
            System.out.println("Use this when a student enrolls again.");
            
            if (validator.getValidYesNo("\nAre you sure you want to reactivate this account?")) {
                student.setActive(true);
                utils.FileStorage.updateStudent(registeredStudents, student);
                System.out.println("✓ Account reactivated successfully!");
                System.out.println("✓ Changes saved to database.");
                System.out.println("The student can now log in again.");
            } else {
                System.out.println("Account reactivation cancelled.");
            }
        }
    }
    
    private Student findStudentById(String studentId) {
        for (Student student : registeredStudents) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
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
    System.out.println("   [3] Cancel Reservation - Remove reservation");
        
        System.out.println("\nSTOCK PAGE:");
        System.out.println("   [1] View All - Display entire inventory");
        System.out.println("   [2] View by Course - Filter by course code");
        System.out.println("   [3] Search by Code - Find item by code");
        
        System.out.println("\nADD/REMOVE ITEM:");
        System.out.println("   [1] Add Item - Add new merchandise");
        System.out.println("   [2] Remove Item - Delete item");
        System.out.println("   [3] Update Quantity - Change stock levels");
        
        System.out.println("\nACCOUNT MANAGEMENT:");
        System.out.println("   [1] Change User Password - Reset student account passwords");
        System.out.println("   [2] Activate/Deactivate Account - Manage student account access");
        System.out.println("       • Deactivate when student is not enrolled (prevents login)");
        System.out.println("       • Reactivate when student enrolls again (restores access)");
        System.out.println("   [3] View All Accounts - See all registered students and their status");
        System.out.println("   • Staff and Cashier CANNOT access this feature");
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("Press [0] to go back...");
        validator.getValidInteger("", 0, 0);
    }

    private void showUserReservations() {
        while (true) {
            System.out.println("\n=== USER RESERVATIONS ===");
            System.out.println("[1] View All");
            System.out.println("[2] View Pending");
            System.out.println("[3] Approve Reservation");
            System.out.println("[4] Cancel Reservation");
            System.out.println("[5] Process Returns/Refunds");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 5);
            
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
                case 3: approveReservation(); break;
                case 4: cancelRes(); break;
                case 5: processReturnsRefunds(); break;
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
        
        System.out.println("\nℹ Note: Use [3] Approve Reservation to approve pending reservations.");
    }

    private void approveReservation() {
        System.out.println("\n=== APPROVE RESERVATION ===");
        
        // Display pending reservations
        List<Reservation> pending = reservationManager.getPendingReservations();
        
        if (pending.isEmpty()) {
            System.out.println("No pending reservations to approve.");
            return;
        }
        
        System.out.println("\nPENDING RESERVATIONS:");
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------");
        for (Reservation r : pending) {
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-7.2f | %-10s\n",
                r.getReservationId(),
                truncate(r.getStudentName(), 15),
                r.getStudentId(),
                r.getItemCode(),
                truncate(r.getItemName(), 25),
                r.getQuantity(),
                r.getTotalPrice(),
                r.getStatus());
        }
        
        int reservationId = validator.getValidInteger("\nEnter Reservation ID to approve (or 0 to cancel): ", 0, 9999);
        
        if (reservationId == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Reservation r = reservationManager.findReservationById(reservationId);
        
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        if (!r.getStatus().equals("PENDING")) {
            System.out.println("This reservation is not pending. Current status: " + r.getStatus());
            return;
        }
        
        // Display reservation details
        System.out.println("\n=== RESERVATION DETAILS ===");
        System.out.println("Reservation ID: " + r.getReservationId());
        System.out.println("Student: " + r.getStudentName() + " (" + r.getStudentId() + ")");
        System.out.println("Course: " + r.getCourse());
        System.out.println("Item: " + r.getItemName() + " (Code: " + r.getItemCode() + ")");
        System.out.println("Size: " + r.getSize());
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Total Price: ₱" + r.getTotalPrice());
        System.out.println("Current Status: " + r.getStatus());
        
        if (validator.getValidYesNo("\nApprove this reservation for payment?")) {
            if (reservationManager.approveReservation(reservationId, r.getSize())) {
                System.out.println("\n✓ Reservation approved!");
                System.out.println("✓ Status changed to: APPROVED - WAITING FOR PAYMENT");
                System.out.println("✓ Student can now go to CASHIER to pay");
            } else {
                System.out.println("\n✗ Failed to approve reservation.");
            }
        } else {
            System.out.println("Approval cancelled.");
        }
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
            if (reservationManager.cancelReservation(id, "Cancelled by admin")) {
                System.out.println("Cancelled.");
            }
        }
    }

    private void processReturnsRefunds() {
        System.out.println("\n=== PROCESS RETURNS/REFUNDS ===");
        
        // Get all RETURN REQUESTED reservations
        List<Reservation> allReservations = reservationManager.getAllReservations();
        List<Reservation> returnRequests = new java.util.ArrayList<>();
        
        for (Reservation r : allReservations) {
            if (r.getStatus().equals("RETURN REQUESTED") && r.isEligibleForReturn()) {
                returnRequests.add(r);
            }
        }
        
        if (returnRequests.isEmpty()) {
            System.out.println("\nNo return requests pending approval.");
            System.out.println("\nℹ Students must first submit a return request from their interface.");
            System.out.println("\nPress [0] to go back...");
            validator.getValidInteger("", 0, 0);
            return;
        }
        
        System.out.println("\n=== PENDING RETURN REQUESTS ===");
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Return Reason");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|---------------------------");
        
        for (Reservation r : returnRequests) {
            // Extract return reason from the reason field
            String reason = r.getReason();
            if (reason != null && reason.contains("Reason: ")) {
                reason = reason.substring(reason.indexOf("Reason: ") + 8);
            } else {
                reason = "Not specified";
            }
            
            System.out.printf("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-7.2f | %-25s\n",
                r.getReservationId(),
                truncate(r.getStudentName(), 15),
                r.getStudentId(),
                r.getItemCode(),
                truncate(r.getItemName(), 25),
                r.getQuantity(),
                r.getTotalPrice(),
                truncate(reason, 25));
        }
        
        int id = validator.getValidInteger("\nEnter Reservation ID to approve return (0 to cancel): ", 0, 9999);
        if (id == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        Reservation r = reservationManager.findReservationById(id);
        if (r == null) {
            System.out.println("Reservation not found.");
            return;
        }
        
        if (!r.getStatus().equals("RETURN REQUESTED")) {
            System.out.println("\n⚠ ERROR: This item does not have a return request.");
            System.out.println("Current status: " + r.getStatus());
            return;
        }
        
        if (!r.isEligibleForReturn()) {
            System.out.println("\n⚠ ERROR: Return period has expired (10 days limit).");
            return;
        }
        
        // Extract return reason
        String returnReason = r.getReason();
        if (returnReason != null && returnReason.contains("Reason: ")) {
            returnReason = returnReason.substring(returnReason.indexOf("Reason: ") + 8);
        } else {
            returnReason = "Not specified";
        }
        
        System.out.println("\n=== RETURN APPROVAL ===");
        System.out.println("Student: " + r.getStudentName() + " (" + r.getStudentId() + ")");
        System.out.println("Item: " + r.getItemName() + " (Size: " + r.getSize() + ")");
        System.out.println("Quantity: " + r.getQuantity());
        System.out.println("Refund Amount: ₱" + r.getTotalPrice());
        System.out.println("Payment Method: " + r.getPaymentMethod());
        System.out.println("Return Reason: " + returnReason);
        System.out.println("Days Since Pickup: " + (10 - r.getDaysUntilReturnExpires()) + " days");
        
        if (validator.getValidYesNo("\nApprove return and issue refund?")) {
            // Change status to RETURNED with reason
            String returnMessage = "Item returned within 10 days - Reason: " + returnReason;
            reservationManager.updateReservationStatus(id, "RETURNED - REFUNDED", returnMessage);
            
            // Restock the item by size
            boolean restocked = inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
            if (restocked) {
                System.out.println("✓ Item restocked: " + r.getQuantity() + " units added back to inventory");
                
                // Log the user return
                utils.StockReturnLogger.logUserReturn(
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemCode(),
                    r.getItemName(),
                    r.getSize(),
                    r.getQuantity(),
                    returnReason
                );
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
            System.out.println("\n=== STOCK MANAGEMENT ===");
            System.out.println("[1] Add New Item - Add completely new item to inventory");
            System.out.println("[2] Return Stock - Remove stock with reason (logged)");
            System.out.println("[3] View Stock Logs - See all stock changes (transparency)");
            System.out.println("[0] Back");
            
            int choice = validator.getValidInteger("Enter choice: ", 0, 3);
            
            switch (choice) {
                case 0: return;
                case 1: addItem(); break;
                case 2: returnStock(); break;
                case 3: utils.StockReturnLogger.displayStockReturnLogs(); 
                        System.out.println("\nPress [0] to go back...");
                        validator.getValidInteger("", 0, 0);
                        break;
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

    private void returnStock() {
        System.out.println("\n=== RETURN STOCK ===");
        System.out.println("Remove stock items from inventory with reason logging");
        System.out.println("[0] Back to previous menu");
        
        // First, select course
        String course = validator.getValidCourse("Select course (BSIT, BSCS, BSIS, 0 to go back): ");
        if (course.equals("0")) {
            System.out.println("Returning to menu...");
            return;
        }
        
        // Display items for selected course
        inventoryManager.displayItemsByCourse(course);
        
        List<Item> courseItems = inventoryManager.getItemsByCourse(course);
        if (courseItems.isEmpty()) {
            System.out.println("No items available for this course.");
            return;
        }
        
        int code = validator.getValidInteger("\nItem code (1000-9999, 0 to go back): ", 0, 9999);
        if (code == 0) {
            System.out.println("Returning to menu...");
            return;
        }
        
        Item item = inventoryManager.findItemByCode(code);
        if (item == null) {
            System.out.println("Item not found.");
            return;
        }
        
        // Validate that the item belongs to the selected course
        if (!item.getCourse().equalsIgnoreCase(course)) {
            System.out.println("Error: This item does not belong to the selected course (" + course + ").");
            return;
        }
        
        System.out.println("\n=== ITEM DETAILS ===");
        System.out.println("Code: " + item.getCode());
        System.out.println("Name: " + item.getName());
        System.out.println("Course: " + item.getCourse());
        System.out.println("Size: " + item.getSize());
        System.out.println("Current Quantity: " + item.getQuantity());
        System.out.println("Price: ₱" + item.getPrice());
        
        int returnQty = validator.getValidInteger("\nQuantity to return (0 to cancel): ", 0, item.getQuantity());
        if (returnQty == 0) {
            System.out.println("Operation cancelled.");
            return;
        }
        
        // Ask for return reason
        System.out.println("\n=== RETURN REASON ===");
        System.out.println("Please select the reason for returning stock:");
        System.out.println("[1] Defective - Item has defects or quality issues");
        System.out.println("[2] Damaged - Item was damaged during storage/handling");
        System.out.println("[3] Expired/Outdated - Item is no longer current");
        System.out.println("[4] Overstock - Excess inventory being returned");
        System.out.println("[5] Wrong Item - Item ordered/received in error");
        System.out.println("[6] Other - Different reason");
        System.out.println("[0] Cancel - Go back");
        
        int reasonChoice = validator.getValidInteger("Enter reason (0-6): ", 0, 6);
        
        if (reasonChoice == 0) {
            System.out.println("Stock return cancelled.");
            return;
        }
        
        String returnReason = switch (reasonChoice) {
            case 1 -> "Defective";
            case 2 -> "Damaged";
            case 3 -> "Expired/Outdated";
            case 4 -> "Overstock";
            case 5 -> "Wrong Item";
            case 6 -> "Other";
            default -> "Not specified";
        };
        
        System.out.println("\n=== CONFIRMATION ===");
        System.out.println("Item: " + item.getName() + " (" + item.getSize() + ")");
        System.out.println("Quantity to return: " + returnQty);
        System.out.println("Reason: " + returnReason);
        System.out.println("New quantity will be: " + (item.getQuantity() - returnQty));
        
        if (validator.getValidYesNo("\nConfirm stock return?")) {
            // Update inventory
            int newQty = item.getQuantity() - returnQty;
            inventoryManager.updateItemQuantity(code, newQty);
            
            // Log the return
            utils.StockReturnLogger.logStockReturn(
                "Admin",
                item.getCode(),
                item.getName(),
                item.getSize(),
                returnQty,
                returnReason
            );
            
            System.out.println("\n✓ Stock returned successfully!");
            System.out.println("✓ Quantity reduced from " + item.getQuantity() + " to " + newQty);
            System.out.println("✓ Return logged for transparency");
        } else {
            System.out.println("Stock return cancelled.");
        }
    }

    // Helper method to truncate text to specified length
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
