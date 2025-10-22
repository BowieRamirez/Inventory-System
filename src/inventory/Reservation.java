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
    private String status;
    private String reason;
    private boolean isPaid;
    private String paymentMethod;
    private double totalPrice;
    
    public Reservation(int reservationId, String studentName, String studentId, String course,
                       int itemCode, String itemName, int quantity, double totalPrice) {
        this.reservationId = reservationId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.course = course;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.reservationTime = LocalDateTime.now();
        this.status = "PENDING";
        this.isPaid = false;
        this.paymentMethod = "UNPAID";
    }
    
    public int getReservationId() { return reservationId; }
    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getCourse() { return course; }
    public int getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getReservationTime() { return reservationTime; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }
    public boolean isPaid() { return isPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getTotalPrice() { return totalPrice; }
    
    public void setStatus(String status) { this.status = status; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setReason(String reason) { this.reason = reason; }
    public void setPaid(boolean paid) { this.isPaid = paid; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return reservationTime.format(formatter);
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