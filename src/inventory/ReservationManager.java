package inventory;

import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 1001;
    
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
    
    public boolean cancelReservation(int reservationId) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.getStatus().equals("COMPLETED")) {
            r.setStatus("CANCELLED");
            return true;
        }
        return false;
    }
    
    public boolean updateReservationStatus(int reservationId, String status) {
        Reservation r = findReservationById(reservationId);
        if (r != null) {
            r.setStatus(status);
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
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|------------------------------");
        for (Reservation r : reservations) {
            System.out.println(r);
        }
    }
    
    public void displayReservationsByStudent(String studentId) {
        List<Reservation> studentReservations = getReservationsByStudent(studentId);
        if (studentReservations.isEmpty()) {
            System.out.println("No reservations found for student ID: " + studentId);
            return;
        }
        System.out.println("\n=== YOUR RESERVATIONS ===");
        System.out.println("ID   | Student Name    | Student ID   | Course               | Item   | Item Name                 | Quantity | Reservation Time    | Status");
        System.out.println("-----|-----------------|--------------|----------------------|--------|---------------------------|----------|---------------------|------------------------------");
        for (Reservation r : studentReservations) {
            System.out.println(r);
        }
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
}