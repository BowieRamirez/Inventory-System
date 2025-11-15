package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StockReturnLogger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Resolve the absolute path to stock_logs.txt reliably across run contexts
    public static String getLogFilePath() {
        try {
            // Prefer project root detected by presence of pom.xml
            java.nio.file.Path projectRoot = findProjectRoot();
            if (projectRoot != null) {
                // NEW stock logs file
                java.nio.file.Path canonical = projectRoot.resolve(java.nio.file.Paths.get("src","database","data","stock_logs_v2.txt"));
                if (ensureParentDir(canonical)) return canonical.toString();
            }

            java.nio.file.Path desiredRelative = java.nio.file.Paths.get("src", "database", "data", "stock_logs_v2.txt");

            // 1) Try from current working directory
            java.nio.file.Path base = java.nio.file.Paths.get("").toAbsolutePath();
            java.nio.file.Path candidate = base.resolve(desiredRelative);
            if (ensureParentDir(candidate)) return candidate.toString();

            // 2) If running from target/classes, walk up to project root
            java.net.URL codeSource = StockReturnLogger.class.getProtectionDomain().getCodeSource().getLocation();
            java.nio.file.Path classesDir = java.nio.file.Paths.get(codeSource.toURI());
            java.nio.file.Path maybeProjectRoot = classesDir.getParent() != null ? classesDir.getParent().getParent() : null; // target/classes -> project
            if (maybeProjectRoot != null) {
                candidate = maybeProjectRoot.resolve(desiredRelative);
                if (ensureParentDir(candidate)) return candidate.toString();
            }

            // 3) Walk a few parents from user.dir just in case
            java.nio.file.Path walk = base;
            for (int i = 0; i < 4 && walk != null; i++) {
                candidate = walk.resolve(desiredRelative);
                if (ensureParentDir(candidate)) return candidate.toString();
                walk = walk.getParent();
            }

            // 4) Fallback: create under current working dir
            candidate = base.resolve(desiredRelative);
            java.nio.file.Files.createDirectories(candidate.getParent());
            return candidate.toString();
        } catch (Exception ex) {
            // Last-resort fallback
            return "src/database/data/stock_logs_v2.txt";
        }
    }

    // Ensure parent directory exists; returns true if parent exists or created successfully
    private static boolean ensureParentDir(java.nio.file.Path path) {
        try {
            java.nio.file.Path parent = path.getParent();
            if (parent == null) return false;
            if (!java.nio.file.Files.exists(parent)) {
                java.nio.file.Files.createDirectories(parent);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Find project root by locating pom.xml upwards from classes location or CWD
    private static java.nio.file.Path findProjectRoot() {
        java.util.List<java.nio.file.Path> starts = new java.util.ArrayList<>();
        starts.add(java.nio.file.Paths.get("").toAbsolutePath());
        try {
            java.net.URL codeSource = StockReturnLogger.class.getProtectionDomain().getCodeSource().getLocation();
            starts.add(java.nio.file.Paths.get(codeSource.toURI()));
        } catch (Exception ignored) {}

        for (java.nio.file.Path start : starts) {
            java.nio.file.Path p = start;
            for (int i=0; i<6 && p!=null; i++) {
                java.nio.file.Path candidate = p.resolve("pom.xml");
                if (java.nio.file.Files.exists(candidate)) {
                    return p;
                }
                p = p.getParent();
            }
        }
        return null;
    }
    
    // Debug: Print the working directory and file path
    static {
        String logFilePath = getLogFilePath();
        System.out.println("[StockReturnLogger] Initialized. Log file: " + logFilePath);
        File logFile = new File(logFilePath);
        System.out.println("[StockReturnLogger] File path: " + logFile.getAbsolutePath());
        System.out.println("[StockReturnLogger] File exists: " + logFile.exists());
        System.out.println("[StockReturnLogger] File writable: " + (logFile.exists() && logFile.canWrite()));
        System.out.println("[StockReturnLogger] Parent dir exists: " + (logFile.getParentFile() != null && logFile.getParentFile().exists()));
    }
    
    // Test method to verify logging works
    public static void testLogging() {
        System.out.println("[StockReturnLogger] Testing log write...");
        logItemAdded("TEST_USER", 9999, "TEST_ITEM", "TEST", 1, 99.99);
        System.out.println("[StockReturnLogger] Test complete.");
    }

    // Ensure table exists (called once at startup)
    public static void ensureTableExists() {
        if (!DBManager.isConfigured()) {
            return;
        }
        String createTable = "CREATE TABLE IF NOT EXISTS stock_logs (" +
                "ts DATETIME, actor VARCHAR(200), item_code INT, item_name VARCHAR(255), size VARCHAR(20), " +
                "delta VARCHAR(20), action_type VARCHAR(50), details VARCHAR(400), INDEX idx_ts (ts))";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(createTable)) {
            ps.executeUpdate();
            System.out.println("[StockReturnLogger] Database table ensured.");
        } catch (SQLException e) {
            System.err.println("[StockReturnLogger] Failed to ensure table: " + e.getMessage());
        }
    }

    // Persist to database when DB is configured
    private static void writeToDatabase(String timestamp,
                                        String actor,
                                        int itemCode,
                                        String itemName,
                                        String size,
                                        String stockChange,
                                        String action,
                                        String details) {
        if (!DBManager.isConfigured()) {
            return; // DB not configured; skip silently
        }
        String insertSql = "INSERT INTO stock_logs(ts,actor,item_code,item_name,size,delta,action_type,details) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBManager.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setString(1, timestamp);
            ps.setString(2, actor);
            ps.setInt(3, itemCode);
            ps.setString(4, itemName);
            ps.setString(5, size);
            ps.setString(6, stockChange);
            ps.setString(7, action);
            ps.setString(8, details);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[StockReturnLogger] DB write failed: " + e.getMessage());
        }
    }
    
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
            java.nio.file.Path file = java.nio.file.Paths.get(getLogFilePath());
            writeHeaderIfMissing(file);

            String timestamp = LocalDateTime.now().format(formatter);
            String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                timestamp, performedBy, itemCode, itemName, size, stockChange, action, updateDetails);

            // Append atomically and flush to disk
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file.toFile(), true);
                 java.nio.channels.FileChannel ch = fos.getChannel();
                 java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(osw)) {
                bw.write(logEntry);
                bw.newLine();
                bw.flush();
                ch.force(true);
            }

            // Also persist to DB when available
            writeToDatabase(timestamp, performedBy, itemCode, itemName, size, stockChange, action, updateDetails);

        } catch (IOException e) {
            System.err.println("[StockReturnLogger] Error logging item updated: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Log price change
    public static void logPriceChange(String performedBy, int itemCode, String itemName,
                                      String size, double oldPrice, double newPrice) {
        String action = "PRICE_UPDATED";
        String details = String.format("Price changed: ₱%.2f -> ₱%.2f", oldPrice, newPrice);
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(getLogFilePath());
            writeHeaderIfMissing(file);

            String timestamp = LocalDateTime.now().format(formatter);
            String stockChange = ""; // price updates don't affect stock
            String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file.toFile(), true);
                 java.nio.channels.FileChannel ch = fos.getChannel();
                 java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(osw)) {
                bw.write(logEntry);
                bw.newLine();
                bw.flush();
                ch.force(true);
            }

            // Persist to DB (optional)
            writeToDatabase(timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);

        } catch (IOException e) {
            System.err.println("[StockReturnLogger] Error logging price change: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Generic log method
    private static void logStockChange(String performedBy, int itemCode, String itemName,
                                      String size, int quantity, int remainingStock, String action, String details) {
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(getLogFilePath());
            writeHeaderIfMissing(file);

            String timestamp = LocalDateTime.now().format(formatter);
            String stockChange = action.equals("USER_RETURN") || action.equals("STAFF_RETURN")
                               ? "+" + quantity + "/" + remainingStock
                               : "-" + quantity + "/" + remainingStock;

            String logEntry = String.format("%s|%s|%d|%s|%s|%s|%s|%s",
                timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file.toFile(), true);
                 java.nio.channels.FileChannel ch = fos.getChannel();
                 java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(osw)) {
                bw.write(logEntry);
                bw.newLine();
                bw.flush();
                ch.force(true);
            }

            // Persist to DB (optional)
            writeToDatabase(timestamp, performedBy, itemCode, itemName, size, stockChange, action, details);

        } catch (IOException e) {
            // Error logging stock change
            e.printStackTrace();
        }
    }

    // Create the file if missing and write header once
    private static void writeHeaderIfMissing(java.nio.file.Path path) throws IOException {
        if (!java.nio.file.Files.exists(path)) {
            java.nio.file.Files.createDirectories(path.getParent());
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(path.toFile(), false);
                 java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
                 java.io.BufferedWriter bw = new java.io.BufferedWriter(osw)) {
                bw.write("Timestamp|PerformedBy|Code|ItemName|Size|StockChange|Action|Details");
                bw.newLine();
                bw.flush();
                fos.getChannel().force(true);
            }
        }
    }
}
