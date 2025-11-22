package gui.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import inventory.Reservation;

/**
 * ControllerUtils - Common utility methods shared across controllers
 * Eliminates duplicate code and provides reusable functions
 */
public class ControllerUtils {

    /**
     * Deduplicate bundle reservations - show only one entry per bundle
     * For non-bundle items, show them as-is
     * 
     * @param reservations List of reservations (may contain bundles with multiple items)
     * @return Deduplicated list with one entry per bundle
     */
    public static List<Reservation> getDeduplicatedReservations(List<Reservation> reservations) {
        // For bundles, pick a representative reservation per bundle using a status-priority rule
        // so the table shows the most-relevant status for the bundle (e.g., COMPLETED/APPROVED over PENDING)
        List<Reservation> deduplicated = new ArrayList<>();

        // Map bundleId -> list of reservations in that bundle
        Map<String, List<Reservation>> bundles = new LinkedHashMap<>();

        for (Reservation r : reservations) {
            if (r.isPartOfBundle()) {
                bundles.computeIfAbsent(r.getBundleId(), k -> new ArrayList<>()).add(r);
            } else {
                deduplicated.add(r);
            }
        }

        // Helper to compute priority of a reservation status
        java.util.function.Function<Reservation, Integer> statusPriority = (res) -> {
            String s = res.getStatus();
            if (s == null) return 0;
            s = s.toUpperCase();
            if (s.contains("REPLACED")) return 6;
            if (s.contains("COMPLETED")) return 5;
            if (s.contains("PAID")) return 4;
            if (s.contains("APPROVED")) return 3;
            if (s.contains("PICKUP")) return 2;
            if (s.contains("REPLACEMENT")) return 1;
            if (s.contains("PENDING")) return 0;
            return 0;
        };

        // For each bundle pick the reservation with highest priority
        for (Map.Entry<String, List<Reservation>> e : bundles.entrySet()) {
            List<Reservation> list = e.getValue();
            Reservation best = null;
            int bestPriority = Integer.MIN_VALUE;
            for (Reservation r : list) {
                int p = statusPriority.apply(r);
                if (best == null || p > bestPriority) {
                    best = r;
                    bestPriority = p;
                }
            }
            if (best != null) deduplicated.add(best);
        }

            deduplicated.sort(Reservation.newestFirstComparator());
            return deduplicated;
    }
    
    /**
     * Calculate total price for a bundle by summing all items with the same bundle ID
     * 
     * @param bundleId The bundle ID to calculate total for
     * @param allReservations All reservations to search through
     * @return Total price of all items in the bundle
     */
    public static double calculateBundleTotal(String bundleId, List<Reservation> allReservations) {
        return allReservations.stream()
            .filter(res -> bundleId.equals(res.getBundleId()))
            .mapToDouble(Reservation::getTotalPrice)
            .sum();
    }
    
    /**
     * Calculate total quantity for a bundle by summing all items with the same bundle ID
     * 
     * @param bundleId The bundle ID to calculate total for
     * @param allReservations All reservations to search through
     * @return Total quantity of all items in the bundle
     */
    public static int calculateBundleQuantity(String bundleId, List<Reservation> allReservations) {
        return allReservations.stream()
            .filter(res -> bundleId.equals(res.getBundleId()))
            .mapToInt(Reservation::getQuantity)
            .sum();
    }
    
    /**
     * Count items in a bundle
     * 
     * @param bundleId The bundle ID to count items for
     * @param allReservations All reservations to search through
     * @return Number of items in the bundle
     */
    public static long countBundleItems(String bundleId, List<Reservation> allReservations) {
        return allReservations.stream()
            .filter(res -> bundleId.equals(res.getBundleId()))
            .count();
    }
}
