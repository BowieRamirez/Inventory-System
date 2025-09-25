import java.util.*;

public class ReservationManager {
    private List<Reservation> reservations;
    private int nextReservationId;
    
    public ReservationManager() {
        reservations = new ArrayList<>();
        nextReservationId = 1001;
    }
    
    public Reservation createReservation(String studentName, String studentId, String course, 
                                       int itemCode, String itemName, int quantity) {
        Reservation reservation = new Reservation(nextReservationId++, studentName, studentId, 
                                                course, itemCode, itemName, quantity);
        reservations.add(reservation);
        return reservation;
    }
    
    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }
    
    public List<Reservation> getReservationsByStudent(String studentId) {
        List<Reservation> studentReservations = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStudentId().equals(studentId)) {
                studentReservations.add(reservation);
            }
        }
        return studentReservations;
    }
    
    public Reservation findReservationById(int reservationId) {
        for (Reservation reservation : reservations) {
            if (reservation.getReservationId() == reservationId) {
                return reservation;
            }
        }
        return null;
    }
    
    public boolean cancelReservation(int reservationId) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation != null && !reservation.getStatus().equals("COMPLETED")) {
            reservation.setStatus("CANCELLED");
            return true;
        }
        return false;
    }
    
    public boolean updateReservationStatus(int reservationId, String status) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation != null) {
            reservation.setStatus(status);
            return true;
        }
        return false;
    }
    
    public void displayAllReservations() {
        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }
        
        System.out.println("\n=== ALL RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|----------");
        
        for (Reservation reservation : reservations) {
            System.out.println(reservation);
        }
        System.out.println();
    }
    
    public void displayReservationsByStudent(String studentId) {
        List<Reservation> studentReservations = getReservationsByStudent(studentId);
        
        if (studentReservations.isEmpty()) {
            System.out.println("No reservations found for student ID: " + studentId);
            return;
        }
        
        System.out.println("\n=== YOUR RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|----------");
        
        for (Reservation reservation : studentReservations) {
            System.out.println(reservation);
        }
        System.out.println();
    }
    
    public List<Reservation> getPendingReservations() {
        List<Reservation> pending = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.getStatus().equals("PENDING")) {
                pending.add(reservation);
            }
        }
        return pending;
    }
}