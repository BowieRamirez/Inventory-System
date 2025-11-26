package inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.FileStorage;
import utils.StockReturnLogger;

public class ReservationManager {
    private List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 5001; // Start at 5001 to avoid conflict with item IDs
    private InventoryManager inventoryManager; // link inventory
    private ReceiptManager receiptManager; // link receipts

    public ReservationManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        loadReservations();
    }

    private List<Reservation> copySorted(Collection<Reservation> source) {
        return source.stream()
            .sorted(Reservation.newestFirstComparator())
            .collect(Collectors.toList());
    }

    private List<Reservation> filterSorted(Predicate<Reservation> predicate) {
        return reservations.stream()
            .filter(predicate)
            .sorted(Reservation.newestFirstComparator())
            .collect(Collectors.toList());
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
            
            // Set payment deadline to 48 hours from now
            r.setPaymentDeadline(java.time.LocalDateTime.now().plusHours(48));
            
            saveReservations();
            return true;
        }
        return false;
    }
    
    public List<Reservation> getAllReservations() {
        return copySorted(reservations);
    }

    /**
     * Refresh in-memory reservations from persistent storage (FileStorage).
     * Call this when external changes may have been made by other actors (e.g., staff).
     */
    public void refresh() {
        loadReservations();
    }
    
    public List<Reservation> getReservationsByStudent(String studentId) {
        if (studentId == null) {
            return Collections.emptyList();
        }
        return filterSorted(r -> studentId.equals(r.getStudentId()));
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
        return filterSorted(r -> "PENDING".equalsIgnoreCase(r.getStatus()));
    }
    
    public boolean markAsPaid(int reservationId, String paymentMethod) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.isPaid() && "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
            r.setPaid(true);
            r.setPaymentMethod(paymentMethod);

            // Deduct stock from inventory when payment is processed
            boolean deducted = inventoryManager.deductStockOnApproval(r.getItemCode(), r.getSize(), r.getQuantity());
            if (deducted) {
                // After payment, mark reservation as paid but WAITING for the student
                // to request pickup. Staff should NOT see this as a pickup approval yet.
                r.setStatus("AWAITING PICKUP REQUEST");
                saveReservations();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    public List<Reservation> getUnpaidReservations() {
        return filterSorted(r -> !r.isPaid() && !"CANCELLED".equalsIgnoreCase(r.getStatus()));
    }
    
    public List<Reservation> getPaidPendingReservations() {
        return filterSorted(r -> r.isPaid() && "PENDING".equalsIgnoreCase(r.getStatus()));
    }
    
    // Save reservations when external modifications are made
    public void saveToDatabase() {
        saveReservations();
    }

    /**
     * Student requests pickup - changes status to awaiting staff approval
     * Changes status from "AWAITING PICKUP REQUEST" to "PICKUP REQUESTED - AWAITING STAFF APPROVAL"
     */
    public boolean requestPickup(int reservationId) {
        return requestPickup(reservationId, null);
    }
    
    /**
     * Student requests pickup with preferred time note
     */
    public boolean requestPickup(int reservationId, String preferredTimeNote) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "AWAITING PICKUP REQUEST".equals(r.getStatus())) {
            r.setStatus("PICKUP REQUESTED - AWAITING STAFF APPROVAL");
            if (preferredTimeNote != null && !preferredTimeNote.trim().isEmpty()) {
                r.setReason("Preferred pickup time: " + preferredTimeNote.trim());
            }
            saveReservations();
            return true;
        }
        return false;
    }
    
    /**
     * Staff approves pickup request
     * Changes status from "PICKUP REQUESTED - AWAITING STAFF APPROVAL" to "APPROVED FOR PICKUP"
     */
    public boolean approvePickupRequest(int reservationId, java.time.LocalDateTime scheduledPickup) {
        return approvePickupRequest(reservationId, scheduledPickup, null);
    }
    
    /**
     * Staff approves pickup request with time range
     * Changes status from "PICKUP REQUESTED - AWAITING STAFF APPROVAL" to "APPROVED FOR PICKUP"
     */
    public boolean approvePickupRequest(int reservationId, java.time.LocalDateTime scheduledPickupStart, java.time.LocalDateTime scheduledPickupEnd) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus())) {
            // Mark as approved for pickup and store scheduled pickup datetime range
            r.setStatus("APPROVED FOR PICKUP");
            r.setScheduledPickupDateTime(scheduledPickupStart);
            r.setScheduledPickupEndDateTime(scheduledPickupEnd);
            saveReservations();

            // Do NOT auto-complete here — the student must still claim the item,
            // which should call `markAsPickedUp(...)` to set COMPLETED and
            // completedDate.
            return true;
        }
        return false;
    }
    
    /**
     * Get all pickup requests awaiting staff approval
     */
    public List<Reservation> getPickupRequestsAwaitingApproval() {
        return filterSorted(r -> "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equalsIgnoreCase(r.getStatus()));
    }
    
    /**
     * Student requests reschedule after missing pickup time
     * Changes status from "APPROVED FOR PICKUP" back to "PICKUP REQUESTED - AWAITING STAFF APPROVAL"
     * Clears the previous scheduled pickup datetime so staff can set a new one
     */
    public boolean requestReschedule(int reservationId, String note) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "APPROVED FOR PICKUP".equals(r.getStatus())) {
            r.setStatus("PICKUP REQUESTED - AWAITING STAFF APPROVAL");
            r.setScheduledPickupDateTime(null); // Clear old schedule
            if (note != null && !note.trim().isEmpty()) {
                r.setRescheduleNote(note.trim());
            }
            saveReservations();
            return true;
        }
        return false;
    }

    /**
     * Mark reservation as picked up (student confirms pickup)
     * Changes status from "APPROVED FOR PICKUP" to "COMPLETED"
     * Also updates receipt status and logs to stock_logs.txt
     */
    public boolean markAsPickedUp(int reservationId, String claimProofImagePath) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "APPROVED FOR PICKUP".equals(r.getStatus())) {
            r.setStatus("COMPLETED");
            r.setCompletedDate(java.time.LocalDateTime.now());
            if (claimProofImagePath != null && !claimProofImagePath.isEmpty()) {
                r.setClaimProofImagePath(claimProofImagePath);
            }
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
            r.setStatus("REPLACEMENT REQUESTED");
            r.setReason("Replacement requested - Reason: " + reason);
            saveReservations();
            return true;
        }
        return false;
    }

    /**
     * Request partial return for a completed reservation (student returns fewer items than reserved)
     * Can only be done within 10 days of completion
     * @param reservationId the reservation ID
     * @param quantityToReturn the number of items to return (must be less than total quantity)
     * @param reason the reason for return
     * @return true if successful, false otherwise
     */
    public boolean requestPartialReturn(int reservationId, int quantityToReturn, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r == null || !r.isEligibleForReturn()) {
            return false;
        }
        
        int originalQty = r.getQuantity();
        if (quantityToReturn <= 0 || quantityToReturn > originalQty) {
            return false; // Invalid quantity
        }
        
        // If returning all items, use regular return
        if (quantityToReturn == originalQty) {
            return requestReturn(reservationId, reason);
        }
        
        // For partial returns, create a new "virtual" return request
        // The original reservation keeps its full quantity, but the return request
        // will specify how many items are being returned
        String partialReturnReason = "Partial Replacement (" + quantityToReturn + " of " + originalQty + " items) - Reason: " + reason;
        r.setStatus("REPLACEMENT REQUESTED");
        r.setReason(partialReturnReason);
        saveReservations();
        
        return true;
    }

    /**
     * Approve return request (admin/staff approves)
     * Restocks the item, marks as refunded, updates receipt, and logs to stock_logs.txt
     */
    public boolean approveReturn(int reservationId) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "REPLACEMENT REQUESTED".equals(r.getStatus())) {
            // Check if this is a partial return
            int quantityToReturn = r.getQuantity();
            String reasonText = r.getReason() != null ? r.getReason() : "";
            
            // Parse partial return quantity if present
            if (reasonText.startsWith("Partial Return (")) {
                try {
                    int start = reasonText.indexOf("(") + 1;
                    int end = reasonText.indexOf(" of ");
                    String qtyStr = reasonText.substring(start, end);
                    quantityToReturn = Integer.parseInt(qtyStr);
                } catch (Exception e) {
                    // If parsing fails, use full quantity
                    quantityToReturn = r.getQuantity();
                }
            }
            
            // Restock the item
            boolean restocked = inventoryManager.restockItem(r.getItemCode(), r.getSize(), quantityToReturn);
            if (restocked) {
                r.setStatus("REPLACED");
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

                String returnReason = r.getReason() != null ? 
                    r.getReason().replace("Return requested - Reason: ", "").replace("Partial Return ", "Partial return ") : 
                    "Item returned";
                StockReturnLogger.logUserReturn(
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemCode(),
                    r.getItemName(),
                    r.getSize(),
                    quantityToReturn,
                    remainingStock,
                    returnReason
                );

                return true;
            }
        }
        return false;
    }

    /**
     * Approve replacement with specific replacement item (does NOT restock - item is replaced with new one)
     */
    public boolean approveReplacementWithItem(int reservationId, int replacementItemCode, String replacementItemName, String replacementSize, String replacementNote) {
        return approveReplacementWithItem(reservationId, replacementItemCode, replacementItemName, replacementSize, replacementNote, null, null);
    }
    
    /**
     * Approve replacement with specific replacement item and scheduled pickup time range.
     * This sets status to "APPROVED FOR REPLACEMENT" - student must claim to complete.
     * Stock is NOT deducted until student claims the replacement.
     */
    public boolean approveReplacementWithItem(int reservationId, int replacementItemCode, String replacementItemName, String replacementSize, String replacementNote, java.time.LocalDateTime scheduledPickupStart, java.time.LocalDateTime scheduledPickupEnd) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "REPLACEMENT REQUESTED".equals(r.getStatus())) {
            // Check replacement item has enough stock (but don't deduct yet)
            Item replacementItem = inventoryManager.findItemByCodeAndSize(replacementItemCode, replacementSize);
            if (replacementItem == null || replacementItem.getQuantity() < r.getQuantity()) {
                return false; // Not enough stock for replacement
            }

            // Track the replacement item and optional note
            r.setReplacementItem(replacementItemCode, replacementItemName, replacementSize);
            if (replacementNote != null && !replacementNote.trim().isEmpty()) {
                r.setReplacementNote(replacementNote.trim());
            }
            
            // Set scheduled pickup time range (required for replacement pickup)
            if (scheduledPickupStart != null) {
                r.setScheduledPickupDateTime(scheduledPickupStart);
            }
            if (scheduledPickupEnd != null) {
                r.setScheduledPickupEndDateTime(scheduledPickupEnd);
            }
            
            // Set status to APPROVED FOR REPLACEMENT - student must claim to complete
            r.setStatus("APPROVED FOR REPLACEMENT");
            r.setReason(r.getReason() != null ? r.getReason() : "Item replacement approved - awaiting student pickup");
            saveReservations();

            return true;
        }
        return false;
    }
    
    /**
     * Complete replacement claim - called when student picks up the replacement item.
     * This restocks the original item, deducts the replacement item, and sets status to REPLACED.
     */
    public boolean completeReplacementClaim(int reservationId, String claimProofImagePath) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "APPROVED FOR REPLACEMENT".equals(r.getStatus())) {
            // Restock the original item (student returned it)
            inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());

            // Destock the replacement item
            Item replacementItem = inventoryManager.findItemByCodeAndSize(r.getReplacementItemCode(), r.getReplacementSize());
            if (replacementItem != null && replacementItem.getQuantity() >= r.getQuantity()) {
                // Remove the replacement item from stock
                inventoryManager.updateItemQuantityBySize(r.getReplacementItemCode(), r.getReplacementSize(), 
                    replacementItem.getQuantity() - r.getQuantity());
            } else {
                return false; // Not enough stock for replacement (shouldn't happen if properly reserved)
            }

            // Set claim proof image if provided
            if (claimProofImagePath != null && !claimProofImagePath.isEmpty()) {
                r.setClaimProofImagePath(claimProofImagePath);
            }
            
            r.setStatus("REPLACED");
            r.setCompletedDate(java.time.LocalDateTime.now());
            saveReservations();

            // Update receipt status to indicate replacement
            if (receiptManager != null) {
                Receipt receipt = receiptManager.findReceiptByItemAndBuyer(r.getItemCode(), r.getStudentName());
                if (receipt != null) {
                    receiptManager.updatePaymentStatus(receipt.getReceiptId(), "REPLACED");
                }
            }

            // Log replacement to stock_logs.txt
            StringBuilder replacementInfo = new StringBuilder();
            replacementInfo.append("Replacement claimed: ").append(r.getReplacementItemName())
                          .append(" (Size: ").append(r.getReplacementSize()).append(")");
            if (r.getReplacementNote() != null && !r.getReplacementNote().isEmpty()) {
                replacementInfo.append("; Note: ").append(r.getReplacementNote());
            }
            StockReturnLogger.logUserReturn(
                r.getStudentId(),
                r.getStudentName(),
                r.getItemCode(),
                r.getItemName(),
                r.getSize(),
                r.getQuantity(),
                r.getQuantity(), // Original item restocked
                replacementInfo.toString()
            );

            return true;
        }
        return false;
    }

    /**
     * Reject return request (admin/staff rejects)
     */
    public boolean rejectReturn(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "REPLACEMENT REQUESTED".equals(r.getStatus())) {
            r.setStatus("COMPLETED");
            r.setReason("Replacement rejected - Reason: " + reason);
            saveReservations();
            return true;
        }
        return false;
    }
    
    /**
     * Cancel an approved replacement (APPROVED FOR REPLACEMENT -> REPLACEMENT REQUESTED)
     * This allows staff to cancel and re-process the replacement request
     */
    public boolean cancelApprovedReplacement(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && "APPROVED FOR REPLACEMENT".equals(r.getStatus())) {
            r.setStatus("REPLACEMENT REQUESTED");
            r.setReason(r.getReason() + " | Cancelled: " + reason);
            // Clear replacement item info so it can be re-selected
            r.setReplacementItem(0, null, null);
            r.setReplacementNote(null);
            r.setScheduledPickupDateTime(null);
            r.setScheduledPickupEndDateTime(null);
            saveReservations();
            return true;
        }
        return false;
    }

    /**
     * Get all return requests (for admin/staff view)
     */
    public List<Reservation> getReturnRequests() {
        return filterSorted(r -> "REPLACEMENT REQUESTED".equalsIgnoreCase(r.getStatus()));
    }
    
    /**
     * Auto-expire reservations that have passed their payment deadline
     * Returns the number of reservations that were expired
     */
    public int expireOverduePayments() {
        int expiredCount = 0;
        for (Reservation r : reservations) {
            if (r.isPaymentOverdue()) {
                // Restock the items
                inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
                
                // Mark as expired/cancelled
                r.setStatus("EXPIRED - PAYMENT DEADLINE PASSED");
                r.setReason("Payment not received within 48 hours of approval");
                expiredCount++;
            }
        }
        
        if (expiredCount > 0) {
            saveReservations();
        }
        
        return expiredCount;
    }
    
    /**
     * Get all overdue reservations (for admin/staff tracking)
     */
    public List<Reservation> getOverduePayments() {
        return filterSorted(Reservation::isPaymentOverdue);
    }
}
