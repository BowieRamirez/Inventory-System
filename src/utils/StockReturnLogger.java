package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StockReturnLogger {
    private static final String LOG_FILE = "src/database/data/stock_logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Log admin/staff stock returns
    public static void logStockReturn(String performedBy, int itemCode, String itemName, 
                                     String size, int quantity, String reason) {
        logStockChange(performedBy, itemCode, itemName, size, quantity, 
                      "STAFF_RETURN", reason);
    }
    
    // Log user item completion (stock decrease)
    public static void logUserCompletion(String studentId, String studentName, int itemCode, 
                                        String itemName, String size, int quantity) {
        String action = "USER_PICKUP";
        String details = String.format("Student %s (%s) picked up item", studentName, studentId);
        logStockChange(studentName, itemCode, itemName, size, quantity, action, details);
    }
    
    // Log user return (customer return)
    public static void logUserReturn(String studentId, String studentName, int itemCode,
                                    String itemName, String size, int quantity, String reason) {
        String action = "USER_RETURN";
        String details = String.format("Student %s (%s) returned - Reason: %s", 
                                      studentName, studentId, reason);
        logStockChange(studentName, itemCode, itemName, size, quantity, action, details);
    }
    
    // Generic log method
    private static void logStockChange(String performedBy, int itemCode, String itemName,
                                      String size, int quantity, String action, String details) {
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
                String stockChange = action.equals("USER_RETURN") || action.equals("STAFF_RETURN") 
                                   ? "+" + quantity 
                                   : "-" + quantity;
                
                String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                    timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);
                
                out.println(logEntry);
            }
            
        } catch (IOException e) {
            System.err.println("Error logging stock change: " + e.getMessage());
        }
    }
    
    public static void displayStockReturnLogs() {
        File file = new File(LOG_FILE);
        
        if (!file.exists()) {
            System.out.println("\nNo stock logs found.");
            return;
        }
        
        System.out.println("\n=== STOCK CHANGE LOGS ===");
        System.out.println("Timestamp           | Performed By       | Code | Item Name                 | Size | Change | Action        | Details");
        System.out.println("--------------------|-----------------------|------|---------------------------|------|--------|---------------|--------------------------------");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            
            while ((line = br.readLine()) != null) {
                // Skip header line
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    System.out.printf("%-19s | %-21s | %-4s | %-25s | %-4s | %-6s | %-13s | %-30s\n",
                        parts[0],  // Timestamp
                        truncate(parts[1], 21),  // Performed By
                        parts[2],  // Item Code
                        truncate(parts[3], 25),  // Item Name
                        parts[4],  // Size
                        parts[5],  // Stock Change
                        parts[6],  // Action
                        truncate(parts[7], 30)); // Details
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading stock logs: " + e.getMessage());
        }
    }
    
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
