package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized CLI Logging utility for the ProWear Inventory System.
 * Provides formatted console logs with timestamps and emojis for transparency and traceability.
 */
public class SystemLogger {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Log levels / prefixes with emojis
    private static final String EMOJI_LOGIN = "🔐";
    private static final String EMOJI_LOGOUT = "👋";
    private static final String EMOJI_PURCHASE = "🛒";
    private static final String EMOJI_STOCK = "📦";
    private static final String EMOJI_ADJUST = "📊";
    private static final String EMOJI_ERROR = "❌";
    private static final String EMOJI_SUCCESS = "✅";
    private static final String EMOJI_WARNING = "⚠️";
    private static final String EMOJI_AUDIT = "🔍";
    private static final String EMOJI_ACTIVITY = "⚙️";
    
    /**
     * Log a user login event
     */
    public static void logLogin(String username, String role) {
        String message = String.format("%s [%s] %s Login Successful — User: %s | Role: %s",
                EMOJI_LOGIN, getTimestamp(), EMOJI_SUCCESS, username, role);
        printLog(message);
    }
    
    /**
     * Log a user logout event
     */
    public static void logLogout(String username) {
        String message = String.format("%s [%s] User Logged Out: %s",
                EMOJI_LOGOUT, getTimestamp(), username);
        printLog(message);
    }
    
    /**
     * Log a failed authentication attempt
     */
    public static void logAuthenticationFailure(String username, String reason) {
        String message = String.format("%s [%s] %s Authentication Failed — User: %s | Reason: %s",
                EMOJI_ERROR, getTimestamp(), EMOJI_AUDIT, username, reason);
        printLog(message);
    }
    
    /**
     * Log a purchase transaction
     */
    public static void logPurchase(String username, String itemName, int quantity, double totalPrice) {
        String message = String.format("%s [%s] %s Purchase Transaction — User: %s | Item: %s | Qty: %d | Total: ₱%.2f",
                EMOJI_PURCHASE, getTimestamp(), EMOJI_SUCCESS, username, itemName, quantity, totalPrice);
        printLog(message);
    }
    
    /**
     * Log stock update after a purchase
     */
    public static void logStockUpdate(String itemName, int quantitySold, int remainingStock) {
        String message = String.format("%s [%s] Stock Updated: %s | Sold: %d | Remaining: %d",
                EMOJI_STOCK, getTimestamp(), itemName, quantitySold, remainingStock);
        printLog(message);
    }
    
    /**
     * Log a stock adjustment by admin (add/remove items)
     */
    public static void logStockAdjustment(String adminUsername, String itemName, int adjustment, int newStock) {
        String action = adjustment > 0 ? "added" : "removed";
        String message = String.format("%s [%s] Stock Adjustment: %s units %s to %s by Admin: %s — Updated stock: %d",
                EMOJI_ADJUST, getTimestamp(), Math.abs(adjustment), action, itemName, adminUsername, newStock);
        printLog(message);
    }
    
    /**
     * Log a reservation
     */
    public static void logReservation(String username, String itemName, int quantity) {
        String message = String.format("%s [%s] %s Reservation Created — User: %s | Item: %s | Qty: %d",
                EMOJI_ACTIVITY, getTimestamp(), EMOJI_SUCCESS, username, itemName, quantity);
        printLog(message);
    }
    
    /**
     * Log a reservation cancellation
     */
    public static void logReservationCancellation(String username, String itemName) {
        String message = String.format("%s [%s] Reservation Cancelled — User: %s | Item: %s",
                EMOJI_ACTIVITY, getTimestamp(), username, itemName);
        printLog(message);
    }
    
    /**
     * Log a system error
     */
    public static void logError(String errorMessage, Exception exception) {
        String message = String.format("%s [%s] %s System Error: %s | Exception: %s",
                EMOJI_ERROR, getTimestamp(), EMOJI_AUDIT, errorMessage, exception.getMessage());
        printLog(message);
    }
    
    /**
     * Log a general system activity
     */
    public static void logActivity(String activity) {
        String message = String.format("%s [%s] %s %s",
                EMOJI_ACTIVITY, getTimestamp(), EMOJI_SUCCESS, activity);
        printLog(message);
    }
    
    /**
     * Log a warning
     */
    public static void logWarning(String warningMessage) {
        String message = String.format("%s [%s] %s Warning: %s",
                EMOJI_WARNING, getTimestamp(), EMOJI_AUDIT, warningMessage);
        printLog(message);
    }
    
    /**
     * Internal method to print formatted log to console
     */
    private static void printLog(String message) {
        System.out.println(message);
    }
    
    /**
     * Get current timestamp as formatted string
     */
    private static String getTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
}
