package inventory;

import java.util.ArrayList;
import java.util.List;

public class ReservationManager {
    private List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 1001;
    
    public Reservation createReservation(String studentName, String studentId, String course,
                                         int itemCode, String itemName, int quantity, double totalPrice) {
        Reservation reservation = new Reservation(nextReservationId++, studentName, studentId, 
                                                   course, itemCode, itemName, quantity, totalPrice);
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
    
    public boolean cancelReservation(int reservationId, String reason) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.getStatus().equals("COMPLETED")) {
            r.setStatus("CANCELLED");
            r.setReason(reason);
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
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|------------------------------");
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
        System.out.println("ID   | Student Name    | Student ID   | Item   | Item Name                 | Qty | Total    | Payment  | Method     | Status");
        System.out.println("-----|-----------------|--------------|--------|---------------------------|-----|----------|----------|------------|------------------------------");
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
    
    public boolean markAsPaid(int reservationId, String paymentMethod) {
        Reservation r = findReservationById(reservationId);
        if (r != null && !r.isPaid()) {
            r.setPaid(true);
            r.setPaymentMethod(paymentMethod);
            return true;
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
}