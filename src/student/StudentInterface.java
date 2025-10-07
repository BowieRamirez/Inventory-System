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
            System.out.println("[5] Logout");
            System.out.println("[0] Exit System");
            
            int choice = validator.getValidInteger("Enter your choice: ", 0, 5);
            
            switch (choice) {
                case 0:
                    System.out.println("🔚 Exiting system...");
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
                    if (validator.getValidYesNo("Are you sure you want to logout?")) {
                        System.out.println("👋 Logged out successfully!");
                        return;
                    }
                    break;
            }
        }
    }

    private void showStudentHelp() {
        System.out.println("\n=== STUDENT HELP DESK ===");
        System.out.println("Welcome " + student.getFullName() + "!");
        System.out.println("Course: " + student.getCourse());
        System.out.println("\nYou can:");
        System.out.println("- Browse available items");
        System.out.println("- Reserve items for your course");
        System.out.println("- Track your reservations");
        System.out.println("\nPress Enter to continue...");
        try { System.in.read(); } catch (Exception e) { }
    }

    private void reserveItem() {
        while (true) {
            System.out.println("\n=== RESERVE ITEM ===");
            System.out.println("[0] Back to Menu");
            System.out.println("\n Available items for " + student.getCourse() + ":");
            inventoryManager.displayItemsByCourse(student.getCourse());
            System.out.println("\n STI Special Merchandise:");
            inventoryManager.displayItemsByCourse("STI Special");

            int code = validator.getValidInteger("\nEnter item code to reserve (0 to go back): ", 0, 9999);
            if (code == 0) return;

            Item item = inventoryManager.findItemByCode(code);
            if (item == null) {
                System.out.println(" Item not found.");
                continue;
            }

            if (!item.getCourse().equalsIgnoreCase(student.getCourse()) 
                && !item.getCourse().equalsIgnoreCase("STI Special")) {
                System.out.println(" You can only reserve items for your course or STI Special items.");
                continue;
            }

            System.out.println("\n Item: " + item.getName());
            System.out.println("Available stock: " + item.getQuantity());
            System.out.println("Price: ₱" + item.getPrice());

            String selectedSize = validator.getValidSize("Select size: ");
            
            int qty = validator.getValidInteger("Enter quantity (1-" + item.getQuantity() + "): ", 1, item.getQuantity());

            System.out.println("\n=== CONFIRMATION ===");
            System.out.println("Student: " + student.getFullName());
            System.out.println("Item: " + item.getName());
            System.out.println("Size: " + selectedSize);
            System.out.println("Quantity: " + qty);
            System.out.println("Total: ₱" + (item.getPrice() * qty));

            if (validator.getValidYesNo("\nConfirm reservation?")) {
                if (inventoryManager.reserveItem(code, qty)) {
                    Reservation res = reservationManager.createReservation(
                        student.getFullName(),
                        student.getStudentId(),
                        student.getCourse(),
                        code,
                        item.getName() + " (" + selectedSize + ")",
                        qty
                    );
                    System.out.println(" Reservation created!");
                    System.out.println("Reservation ID: " + res.getReservationId());
                    System.out.println("Status: " + res.getStatus());
                } else {
                    System.out.println(" Failed to create reservation.");
                }
            }
            
            if (!validator.getValidYesNo("Reserve another item?")) {
                return;
            }
        }
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
            System.out.println(" Item not found.");
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
                if (reservationManager.cancelReservation(id)) {
                    Item item = inventoryManager.findItemByCode(r.getItemCode());
                    if (item != null) item.addQuantity(r.getQuantity());
                    System.out.println(" Reservation cancelled.");
                }
            }
        } else {
            System.out.println(" Invalid reservation ID.");
        }
    }
}