package inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private int reservationId;
    private String studentName;
    private String studentId;
    private String course;
    private int itemCode;
    private String itemName;
    private int quantity;
    private LocalDateTime reservationTime;
    private LocalDateTime completedDate;
    private LocalDateTime paymentDeadline; // Deadline for payment (48 hours after approval)
    private String status;
    private String reason;
    private boolean isPaid;
    private String paymentMethod;
    private double totalPrice;
    private String size;
    private String bundleId; // Identifier for bundle purchases (null for single items)
    public Reservation(int reservationId, String studentName, String studentId, String course,
                       int itemCode, String itemName, int quantity, double totalPrice, String size) {
        this.reservationId = reservationId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.course = course;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.size = size;
        this.totalPrice = totalPrice;
        this.reservationTime = LocalDateTime.now();
        this.status = "PENDING";
        this.isPaid = false;
        this.paymentMethod = "UNPAID";
        this.bundleId = null; // Default to null for single items
    }
    
    // Constructor with bundleId
    public Reservation(int reservationId, String studentName, String studentId, String course,
                       int itemCode, String itemName, int quantity, double totalPrice, String size, String bundleId) {
        this(reservationId, studentName, studentId, course, itemCode, itemName, quantity, totalPrice, size);
        this.bundleId = bundleId;
    }
    public String getSize() { return size; }
    public int getReservationId() { return reservationId; }
    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getCourse() { return course; }
    public int getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public boolean isPaid() { return isPaid; }
    public String getBundleId() { return bundleId; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalPrice() { return totalPrice; }
    
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }
    public void setPaid(boolean paid) { this.isPaid = paid; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    public void setPaymentDeadline(LocalDateTime paymentDeadline) { this.paymentDeadline = paymentDeadline; }
    public void setBundleId(String bundleId) { this.bundleId = bundleId; }
    
    public boolean isPartOfBundle() { return bundleId != null && !bundleId.isEmpty(); }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return reservationTime.format(formatter);
    }
    
    public boolean isEligibleForReturn() {
        if (completedDate == null) {
            return false;
        }
        // Allow returns for COMPLETED or RETURN REQUESTED status
        if (!status.equals("COMPLETED") && !status.equals("RETURN REQUESTED")) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return completedDate.plusDays(10).isAfter(now);
    }
    
    public long getDaysUntilReturnExpires() {
        if (completedDate == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = completedDate.plusDays(10);
        return java.time.temporal.ChronoUnit.DAYS.between(now, expiryDate);
    }
    
    /**
     * Check if payment deadline has passed
     */
    public boolean isPaymentOverdue() {
        if (paymentDeadline == null) {
            return false;
        }
        // Only consider overdue if still waiting for payment
        if (!"APPROVED - WAITING FOR PAYMENT".equals(status)) {
            return false;
        }
        return LocalDateTime.now().isAfter(paymentDeadline);
    }
    
    /**
     * Get hours remaining until payment deadline
     */
    public long getHoursUntilPaymentDeadline() {
        if (paymentDeadline == null) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(paymentDeadline)) {
            return 0; // Deadline passed
        }
        return java.time.temporal.ChronoUnit.HOURS.between(now, paymentDeadline);
    }
    
    /**
     * Get formatted payment deadline string
     */
    public String getFormattedPaymentDeadline() {
        if (paymentDeadline == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        return paymentDeadline.format(formatter);
    }
    
    @Override
    public String toString() {
        String paymentStatus = isPaid ? "PAID" : "UNPAID";
        return String.format("%-4d | %-15s | %-12s | %-6d | %-25s | %-3d | ₱%-8.2f | %-8s | %-10s | %-28s",
            reservationId, studentName, studentId, itemCode, itemName, quantity, totalPrice, paymentStatus, paymentMethod, status);
    }
    
    public String getPaymentStatus() {
        return isPaid ? "PAID (" + paymentMethod + ")" : "UNPAID";
    }
}