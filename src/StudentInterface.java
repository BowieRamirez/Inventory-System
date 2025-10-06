import java.util.Scanner;
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
            System.out.println("   Welcome, " + student.getUsername() + "!");
            System.out.println("   Course: " + student.getCourse());
            System.out.println("=================================");
            System.out.println("[1] Help");
            System.out.println("[2] Reserve a Item");
            System.out.println("[3] Stock Page");
            System.out.println("[4] Your Reservations");
            System.out.println("[5] Logout");
            System.out.println("[0] Exit System");
            System.out.print("Enter your choice: ");
            
            int choice = validator.getValidInteger("", 0, 5);
            
            switch (choice) {
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
    
    private void showStudentHelp() {
        System.out.println("\n=================================");
        System.out.println("        STUDENT HELP DESK");
        System.out.println("=================================");
        System.out.println("\n--- WELCOME TO STI MERCH SYSTEM ---");
        System.out.println("Hello " + student.getUsername() + "! 👋");
        System.out.println("This system helps you browse and reserve school uniforms and merchandise.");
        System.out.println("📚 Your course: " + student.getCourse());
        
        System.out.println("\n--- STEP-BY-STEP ORDERING GUIDE ---");
        System.out.println("1. 📋 BROWSE ITEMS");
        System.out.println("   • Go to 'Stock Page' → 'View Your Course Items'");
        System.out.println("   • Check available uniforms for " + student.getCourse());
        System.out.println("   • Browse 'STI Special Merchandise' for general items");
        System.out.println("   • Note down the ITEM CODE of what you want");
        
        System.out.println("\n2. 🛒 MAKE RESERVATION");
        System.out.println("   • Go to 'Reserve a Item' menu");
        System.out.println("   • Enter the exact ITEM CODE you noted");
        System.out.println("   • Enter the quantity you need");
        System.out.println("   • Confirm your reservation details");
        System.out.println("   • Your reservation will be created with PENDING status");
        
        System.out.println("\n3. 📞 TRACK YOUR ORDER");
        System.out.println("   • Go to 'Your Reservations'");
        System.out.println("   • Check the status of your orders");
        System.out.println("   • Cancel if needed (before approval)");
        
        System.out.println("\n--- RESERVATION STATUS GUIDE ---");
        System.out.println("🟡 PENDING - Waiting for admin approval");
        System.out.println("🟢 APPROVED - Admin approved, prepare for pickup");
        System.out.println("🔵 COMPLETED - Items ready for pickup");
        System.out.println("🔴 CANCELLED - Reservation cancelled");
        
        System.out.println("\n--- WHAT YOU CAN ORDER ---");
        System.out.println("📘 " + student.getCourse() + " Course Items:");
        System.out.println("   • " + student.getCourse() + " Polo Shirts (All sizes)");
        System.out.println("   • " + student.getCourse() + " Pants (All sizes)");
        System.out.println("   • " + student.getCourse() + " ID Lace");
        
        System.out.println("\n🎉 STI Special Merchandise (All students):");
        System.out.println("   • Anniversary Clothes");
        System.out.println("   • PE Uniforms");
        System.out.println("   • Washday Shirts");
        System.out.println("   • NSTP Uniforms");
        
        System.out.println("\n--- IMPORTANT REMINDERS ---");
        System.out.println("⚠️ You can only order items for your course (" + student.getCourse() + ")");
        System.out.println("⚠️ All reservations need admin approval");
        System.out.println("⚠️ Use exact item codes when reserving");
        System.out.println("⚠️ Payment required during pickup");
        System.out.println("⚠️ Items must be picked up within 7 days");
        
        System.out.println("\n--- NAVIGATION TIPS ---");
        System.out.println("🔢 Use number keys [1-5] to navigate");
        System.out.println("🔙 Press [0] to go back in any menu");
        System.out.println("🚪 Press [5] to logout safely");
        
        System.out.println("\nPress Enter to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Handle input exception
        }
    }
    
    private void reserveItem() {
        System.out.println("\n=================================");
        System.out.println("         RESERVE ITEM");
        System.out.println("=================================");
        
        // Show available items for student's course first
        System.out.println("📚 Available items for your course (" + student.getCourse() + "):");
        inventoryManager.displayItemsByCourse(student.getCourse());
        
        // Also show STI Special items
        System.out.println("🎉 STI Special Merchandise:");
        inventoryManager.displayItemsByCourse("STI Special");
        
        int itemCode = validator.getValidInteger("Enter item code to reserve: ", 1000, 9999);
        
        Item item = inventoryManager.findItemByCode(itemCode);
        if (item == null) {
            System.out.println("❌ Item with code " + itemCode + " not found.");
            System.out.println("💡 Please check the item code from the inventory list above.");
            return;
        }
        
        // Check if item belongs to student's course or is STI Special
        if (!item.getCourse().equalsIgnoreCase(student.getCourse()) && 
            !item.getCourse().equalsIgnoreCase("STI Special")) {
            System.out.println("❌ You can only reserve items for your course (" + student.getCourse() + ") or STI Special merchandise.");
            System.out.println("💡 This item belongs to: " + item.getCourse());
            return;
        }
        
        System.out.println("\n✅ Item found: " + item.getName() + " (" + item.getSize() + ")");
        System.out.println("Available quantity: " + item.getQuantity());
        System.out.println("Price: ₱" + item.getPrice() + " per item");
        
        int quantity = validator.getValidInteger("Enter quantity to reserve (1-" + item.getQuantity() + "): ", 1, item.getQuantity());
        
        if (item.getQuantity() < quantity) {
            System.out.println("❌ Insufficient stock. Available quantity: " + item.getQuantity());
            return;
        }
        
        // Show reservation summary
        System.out.println("\n=== RESERVATION CONFIRMATION ===");
        System.out.println("Student Name: " + student.getUsername());
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Course: " + student.getCourse());
        System.out.println("Item: " + item.getName() + " (" + item.getSize() + ")");
        System.out.println("Item Code: " + item.getCode());
        System.out.println("Quantity: " + quantity);
        System.out.println("Price per item: ₱" + item.getPrice());
        System.out.println("Total Amount: ₱" + (item.getPrice() * quantity));
        
        System.out.println("\n⚠️ IMPORTANT REMINDERS:");
        System.out.println("• Reservation requires admin approval");
        System.out.println("• Payment due during pickup");
        System.out.println("• Items must be collected within 7 days");
        System.out.println("• You can cancel before approval");
        
        if (validator.getValidYesNo("\nConfirm this reservation")) {
            if (inventoryManager.reserveItem(itemCode, quantity)) {
                Reservation reservation = reservationManager.createReservation(
                    student.getUsername(), 
                    student.getStudentId(), 
                    student.getCourse(), 
                    itemCode, 
                    item.getName() + " (" + item.getSize() + ")", 
                    quantity
                );
                
                System.out.println("\n✅ Reservation created successfully!");
                System.out.println("📧 Reservation ID: " + reservation.getReservationId());
                System.out.println("📊 Status: " + reservation.getStatus());
                System.out.println("⏰ Created: " + reservation.getFormattedTime());
                System.out.println("🔔 Please wait for admin approval.");
            } else {
                System.out.println("❌ Failed to create reservation. Please try again.");
            }
        } else {
            System.out.println("📝 Reservation cancelled.");
        }
    }
    
    private void showStockPage() {
        System.out.println("\n=================================");
        System.out.println("           STOCK PAGE");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View Your Course Items (" + student.getCourse() + ")");
            System.out.println("[2] View STI Special Merchandise");
            System.out.println("[3] Search Item by Code");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = validator.getValidInteger("", 0, 3);
            
            switch (choice) {
                case 1:
                    inventoryManager.displayItemsByCourse(student.getCourse());
                    break;
                case 2:
                    inventoryManager.displayItemsByCourse("STI Special");
                    break;
                case 3:
                    searchItemByCode();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void searchItemByCode() {
        System.out.print("Enter item code: ");
        int code = validator.getValidInteger("", 1000, 9999);
        
        Item item = inventoryManager.findItemByCode(code);
        if (item != null) {
            // Check if student can view this item
            if (item.getCourse().equalsIgnoreCase(student.getCourse()) || 
                item.getCourse().equalsIgnoreCase("STI Special")) {
                System.out.println("\nItem found:");
                System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
                System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
                System.out.println(item);
            } else {
                System.out.println("You can only view items for your course or STI Special merchandise.");
            }
        } else {
            System.out.println("Item not found.");
        }
    }
    
    private void showYourReservations() {
        System.out.println("\n=================================");
        System.out.println("       YOUR RESERVATIONS");
        System.out.println("=================================");
        
        while (true) {
            System.out.println("\n[1] View All Your Reservations");
            System.out.println("[2] View Pending Reservations");
            System.out.println("[3] Cancel Reservation");
            System.out.println("[0] Back");
            System.out.print("Enter your choice: ");
            
            int choice = validator.getValidInteger("", 0, 3);
            
            switch (choice) {
                case 1:
                    reservationManager.displayReservationsByStudent(student.getStudentId());
                    break;
                case 2:
                    displayPendingReservations();
                    break;
                case 3:
                    cancelReservation();
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private void displayPendingReservations() {
        List<Reservation> studentReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        List<Reservation> pending = studentReservations.stream()
                                  .filter(r -> r.getStatus().equals("PENDING"))
                                  .toList();
        
        if (pending.isEmpty()) {
            System.out.println("No pending reservations.");
            return;
        }
        
        System.out.println("\n=== YOUR PENDING RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|----------");
        
        for (Reservation reservation : pending) {
            System.out.println(reservation);
        }
    }
    
    private void cancelReservation() {
        List<Reservation> studentReservations = reservationManager.getReservationsByStudent(student.getStudentId());
        
        if (studentReservations.isEmpty()) {
            System.out.println("You have no reservations to cancel.");
            return;
        }
        
        // Show student's reservations
        System.out.println("\nYour reservations:");
        reservationManager.displayReservationsByStudent(student.getStudentId());
        
        System.out.print("Enter Reservation ID to cancel (0 to go back): ");
        int reservationId = validator.getValidInteger("", 0, Integer.MAX_VALUE);
        
        if (reservationId == 0) {
            return;
        }
        
        // Check if reservation belongs to this student
        Reservation reservation = reservationManager.findReservationById(reservationId);
        if (reservation == null || !reservation.getStudentId().equals(student.getStudentId())) {
            System.out.println("Invalid reservation ID or you don't have permission to cancel this reservation.");
            return;
        }
        
        if (reservation.getStatus().equals("COMPLETED")) {
            System.out.println("Cannot cancel a completed reservation.");
            return;
        }
        
        System.out.println("Reservation to cancel: " + reservation);
        boolean confirm = validator.getValidYesNo("Are you sure you want to cancel this reservation? (y/n): ");
        
        if (confirm) {
            if (reservationManager.cancelReservation(reservationId)) {
                // Return the reserved quantity back to inventory
                Item item = inventoryManager.findItemByCode(reservation.getItemCode());
                if (item != null) {
                    item.addQuantity(reservation.getQuantity());
                }
                System.out.println("Reservation cancelled successfully.");
            } else {
                System.out.println("Failed to cancel reservation.");
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }
}