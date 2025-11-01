package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockReturnLogger {
    private static final String LOG_FILE = "src/database/data/stock_logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Log admin/staff stock returns
    public static void logStockReturn(String performedBy, int itemCode, String itemName,
                                     String size, int quantity, int remainingStock, String reason) {
        logStockChange(performedBy, itemCode, itemName, size, quantity, remainingStock,
                      "STAFF_RETURN", reason);
    }

    // Log user item completion (stock decrease)
    public static void logUserCompletion(String studentId, String studentName, int itemCode,
                                        String itemName, String size, int quantity, int remainingStock) {
        String action = "USER_PICKUP";
        String details = String.format("Student %s (%s) picked up item", studentName, studentId);
        logStockChange(studentName, itemCode, itemName, size, quantity, remainingStock, action, details);
    }

    // Log user return (customer return)
    public static void logUserReturn(String studentId, String studentName, int itemCode,
                                    String itemName, String size, int quantity, int remainingStock, String reason) {
        String action = "USER_RETURN";
        String details = String.format("Student %s (%s) returned - Reason: %s",
                                      studentName, studentId, reason);
        logStockChange(studentName, itemCode, itemName, size, quantity, remainingStock, action, details);
    }

    // Log admin/staff item addition
    public static void logItemAdded(String performedBy, int itemCode, String itemName,
                                   String size, int quantity, double price) {
        String action = "ITEM_ADDED";
        String details = String.format("New item added - Price: ₱%.2f", price);
        logStockChange(performedBy, itemCode, itemName, size, quantity, quantity, action, details);
    }

    // Log admin/staff item deletion
    public static void logItemDeleted(String performedBy, int itemCode, String itemName,
                                     String size, int remainingQuantity) {
        String action = "ITEM_DELETED";
        String details = String.format("Item removed from inventory");
        logStockChange(performedBy, itemCode, itemName, size, remainingQuantity, 0, action, details);
    }

    // Log admin/staff item update
    public static void logItemUpdated(String performedBy, int itemCode, String itemName,
                                     String size, int oldQuantity, int newQuantity, String updateDetails) {
        String action = "ITEM_UPDATED";
        int quantityChange = newQuantity - oldQuantity;
        String stockChange = (quantityChange >= 0 ? "+" : "") + quantityChange + "/" + newQuantity;
        
        try {
            File file = new File(LOG_FILE);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
                try (FileWriter fw = new FileWriter(file, true);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter out = new PrintWriter(bw)) {
                    out.println("Timestamp|PerformedBy|Code|ItemName|Size|StockChange|Action|Details");
                }
            }

            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                String timestamp = LocalDateTime.now().format(formatter);
                String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                    timestamp, performedBy, itemCode, itemName, size, stockChange, action, updateDetails);
                out.println(logEntry);
            }
        } catch (IOException e) {
            // Error logging
        }
    }
    
    // Generic log method
    private static void logStockChange(String performedBy, int itemCode, String itemName,
                                      String size, int quantity, int remainingStock, String action, String details) {
        try {
            File file = new File(LOG_FILE);

            // Create file if it doesn't exist
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();

                // Write header if new file
                try (FileWriter fw = new FileWriter(file, true);
                     BufferedWriter bw = new BufferedWriter(fw);
                     PrintWriter out = new PrintWriter(bw)) {
                    out.println("Timestamp|PerformedBy|Code|ItemName|Size|StockChange|Action|Details");
                }
            }

            // Append log entry
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                String timestamp = LocalDateTime.now().format(formatter);
                // Format: -1/50 or +1/51 (change/remaining)
                String stockChange = action.equals("USER_RETURN") || action.equals("STAFF_RETURN")
                                   ? "+" + quantity + "/" + remainingStock
                                   : "-" + quantity + "/" + remainingStock;

                String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                    timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);

                out.println(logEntry);
            }

        } catch (IOException e) {
            // Error logging stock change
        }
    }
}
