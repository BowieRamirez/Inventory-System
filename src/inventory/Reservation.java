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
    
    public Reservation(int reservationId, String studentName, String studentId, String course,
                       int itemCode, String itemName, int quantity) {
        this.reservationId = reservationId;
        this.studentName = studentName;
        this.studentId = studentId;
        this.course = course;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.quantity = quantity;
        this.reservationTime = LocalDateTime.now();
        this.status = "PENDING";
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
    
    public void setStatus(String status) { this.status = status; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getFormattedTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return reservationTime.format(formatter);
    }
    
    @Override
    public String toString() {
        return String.format("%-4d | %-15s | %-12s | %-20s | %-6d | %-25s | %-8d | %-19s | %-28s",
            reservationId, studentName, studentId, course, itemCode, itemName, quantity, getFormattedTime(), status);
    }
}