package inventory;

import utils.FileStorage;
import utils.StockReturnLogger;
import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 5001; // Start at 5001 to avoid conflict with item IDs
    private InventoryManager inventoryManager; // link inventory
    private ReceiptManager receiptManager; // link receipts

    public ReservationManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        loadReservations();
    }

    /**
     * Set receipt manager for receipt synchronization
     */
    public void setReceiptManager(ReceiptManager receiptManager) {
        this.receiptManager = receiptManager;
    }
    
    private void loadReservations() {
        this.reservations = FileStorage.loadReservations();
        if (!reservations.isEmpty()) {
            this.nextReservationId = FileStorage.getNextReservationId(reservations);
        }
    }
    
    private void saveReservations() {
        FileStorage.saveReservations(reservations);
    }
    
    public Reservation createReservation(String studentName, String studentId, String course,
                                         int itemCode, String itemName, String size,
                                         int quantity, double totalPrice) {
        boolean available = inventoryManager.reserveItem(itemCode, size, quantity);
        if (!available) {
            return null;
        }

        Reservation reservation = new Reservation(nextReservationId++, studentName, studentId,
                                                   course, itemCode, itemName, quantity, totalPrice, size);
        reservations.add(reservation);
        saveReservations();
        return reservation;
    }
    
    // Create reservation with bundleId
    public Reservation createReservation(String studentName, String studentId, String course,
                                         int itemCode, String itemName, String size,
                                         int quantity, double totalPrice, String bundleId) {
        boolean available = inventoryManager.reserveItem(itemCode, size, quantity);
        if (!available) {
            return null;
        }

        Reservation reservation = new Reservation(nextReservationId++, studentName, studentId,
                                                   course, itemCode, itemName, quantity, totalPrice, size, bundleId);
        reservations.add(reservation);
        saveReservations();
        return reservation;
    }

    public boolean approveReservation(int reservationId, String size) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "PENDING".equals(r.getStatus())) {
            // Don't deduct stock yet - only mark as approved for payment
            // Stock will be deducted when cashier processes payment
            r.setStatus("APPROVED - WAITING FOR PAYMENT");
            saveReservations();
            return true;
        }
        return false;
    }
    
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }
    
    public List<Reservation> getReservationsByStudent(String studentId) {
        List<Reservation> result = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.getStudentId().equals(studentId)) {
                result.add(r);
            }
        }
        return result;
    }
    
    public Reservation findReservationById(int reservationId) {
        for (Reservation r : reservations) {
            if (r.getReservationId() == reservationId) {
                return r;
            }
        }
        return null;
    }
    
    public boolean cancelReservation(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.getStatus().equals("COMPLETED")) {
            r.setStatus("CANCELLED");
            r.setReason(reason);
            saveReservations();
            return true;
        }
        return false;
    }
    
    public boolean updateReservationStatus(int reservationId, String status, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null) {
            r.setStatus(status);
            if (reason != null && !reason.isEmpty()) {
                r.setReason(reason);
            }
            saveReservations();
            return true;
        }
        return false;
    }
    

    
    public List<Reservation> getPendingReservations() {
        List<Reservation> pending = new ArrayList<>();
        for (Reservation r : reservations) {
            if ("PENDING".equals(r.getStatus())) {
                pending.add(r);
            }
        }
        return pending;
    }
    
    public boolean markAsPaid(int reservationId, String paymentMethod) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.isPaid() && "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
            r.setPaid(true);
            r.setPaymentMethod(paymentMethod);
            
            // Deduct stock from inventory when payment is processed
            boolean deducted = inventoryManager.deductStockOnApproval(r.getItemCode(), r.getSize(), r.getQuantity());
            if (deducted) {
                r.setStatus("PAID - READY FOR PICKUP");
                saveReservations();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    public List<Reservation> getUnpaidReservations() {
        List<Reservation> unpaid = new ArrayList<>();
        for (Reservation r : reservations) {
            if (!r.isPaid() && !"CANCELLED".equals(r.getStatus())) {
                unpaid.add(r);
            }
        }
        return unpaid;
    }
    
    public List<Reservation> getPaidPendingReservations() {
        List<Reservation> paidPending = new ArrayList<>();
        for (Reservation r : reservations) {
            if (r.isPaid() && "PENDING".equals(r.getStatus())) {
                paidPending.add(r);
            }
        }
        return paidPending;
    }
    
    // Save reservations when external modifications are made
    public void saveToDatabase() {
        saveReservations();
    }

    /**
     * Mark reservation as picked up (student confirms pickup)
     * Changes status from "PAID - READY FOR PICKUP" to "COMPLETED"
     * Also updates receipt status and logs to stock_logs.txt
     */
    public boolean markAsPickedUp(int reservationId) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "PAID - READY FOR PICKUP".equals(r.getStatus())) {
            r.setStatus("COMPLETED");
            r.setCompletedDate(java.time.LocalDateTime.now());
            saveReservations();

            // Update receipt status from "PAID" to "COMPLETED"
            if (receiptManager != null) {
                Receipt receipt = receiptManager.findReceiptByItemAndBuyer(r.getItemCode(), r.getStudentName());
                if (receipt != null && "PAID".equals(receipt.getPaymentStatus())) {
                    receiptManager.updatePaymentStatus(receipt.getReceiptId(), "COMPLETED");
                }
            }

            // Log pickup to stock_logs.txt (stock decrease)
            // Get remaining stock after pickup (stock was already deducted during payment)
            Item item = inventoryManager.findItemByCodeAndSize(r.getItemCode(), r.getSize());
            int remainingStock = (item != null) ? item.getQuantity() : 0;

            StockReturnLogger.logUserCompletion(
                r.getStudentId(),
                r.getStudentName(),
                r.getItemCode(),
                r.getItemName(),
                r.getSize(),
                r.getQuantity(),
                remainingStock
            );

            return true;
        }
        return false;
    }

    /**
     * Request return for a completed reservation (student initiates)
     * Can only be done within 10 days of completion
     */
    public boolean requestReturn(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && r.isEligibleForReturn()) {
            r.setStatus("RETURN REQUESTED");
            r.setReason("Return requested - Reason: " + reason);
            saveReservations();
            return true;
        }
        return false;
    }

    /**
     * Approve return request (admin/staff approves)
     * Restocks the item, marks as refunded, updates receipt, and logs to stock_logs.txt
     */
    public boolean approveReturn(int reservationId) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "RETURN REQUESTED".equals(r.getStatus())) {
            // Restock the item
            boolean restocked = inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
            if (restocked) {
                r.setStatus("RETURNED - REFUNDED");
                r.setReason(r.getReason() != null ? r.getReason() : "Item returned within 10 days");
                saveReservations();

                // Update receipt status from "COMPLETED" to "RETURNED - REFUNDED"
                if (receiptManager != null) {
                    Receipt receipt = receiptManager.findReceiptByItemAndBuyer(r.getItemCode(), r.getStudentName());
                    if (receipt != null) {
                        receiptManager.updatePaymentStatus(receipt.getReceiptId(), "RETURNED - REFUNDED");
                    }
                }

                // Log return to stock_logs.txt (stock increase)
                // Get remaining stock after return (stock was already restocked)
                Item item = inventoryManager.findItemByCodeAndSize(r.getItemCode(), r.getSize());
                int remainingStock = (item != null) ? item.getQuantity() : 0;

                String returnReason = r.getReason() != null ? r.getReason().replace("Return requested - Reason: ", "") : "Item returned";
                StockReturnLogger.logUserReturn(
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemCode(),
                    r.getItemName(),
                    r.getSize(),
                    r.getQuantity(),
                    remainingStock,
                    returnReason
                );

                return true;
            }
        }
        return false;
    }

    /**
     * Reject return request (admin/staff rejects)
     */
    public boolean rejectReturn(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "RETURN REQUESTED".equals(r.getStatus())) {
            r.setStatus("COMPLETED");
            r.setReason("Return rejected - Reason: " + reason);
            saveReservations();
            return true;
        }
        return false;
    }

    /**
     * Get all return requests (for admin/staff view)
     */
    public List<Reservation> getReturnRequests() {
        List<Reservation> returnRequests = new ArrayList<>();
        for (Reservation r : reservations) {
            if ("RETURN REQUESTED".equals(r.getStatus())) {
                returnRequests.add(r);
            }
        }
        return returnRequests;
    }
}