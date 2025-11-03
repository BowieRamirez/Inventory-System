package gui.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Set<String> seenBundleIds = new HashSet<>();
        List<Reservation> deduplicated = new ArrayList<>();
        
        for (Reservation r : reservations) {
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                if (!seenBundleIds.contains(bundleId)) {
                    seenBundleIds.add(bundleId);
                    deduplicated.add(r);
                }
            } else {
                deduplicated.add(r);
            }
        }
        
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
