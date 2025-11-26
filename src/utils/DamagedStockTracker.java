package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DamagedStockTracker - Tracks items that were replaced due to damage.
 * Records the original item, replacement item, reason, date, and optional image proof.
 */
public class DamagedStockTracker {
    
    private static final String DAMAGED_STOCK_FILE = "database/data/damaged_stock.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Represents a damaged stock record
     */
    public static class DamagedStockRecord {
        private String timestamp;
        private int reservationId;
        private String studentName;
        private String studentId;
        private int originalItemCode;
        private String originalItemName;
        private String originalSize;
        private int replacementItemCode;
        private String replacementItemName;
        private String replacementSize;
        private String reason;
        private String imagePath; // Optional image proof of damage
        private String processedBy; // Staff/Admin who processed the replacement
        
        public DamagedStockRecord(String timestamp, int reservationId, String studentName, String studentId,
                                  int originalItemCode, String originalItemName, String originalSize,
                                  int replacementItemCode, String replacementItemName, String replacementSize,
                                  String reason, String imagePath, String processedBy) {
            this.timestamp = timestamp;
            this.reservationId = reservationId;
            this.studentName = studentName;
            this.studentId = studentId;
            this.originalItemCode = originalItemCode;
            this.originalItemName = originalItemName;
            this.originalSize = originalSize;
            this.replacementItemCode = replacementItemCode;
            this.replacementItemName = replacementItemName;
            this.replacementSize = replacementSize;
            this.reason = reason;
            this.imagePath = imagePath;
            this.processedBy = processedBy;
        }
        
        // Getters
        public String getTimestamp() { return timestamp; }
        public int getReservationId() { return reservationId; }
        public String getStudentName() { return studentName; }
        public String getStudentId() { return studentId; }
        public int getOriginalItemCode() { return originalItemCode; }
        public String getOriginalItemName() { return originalItemName; }
        public String getOriginalSize() { return originalSize; }
        public int getReplacementItemCode() { return replacementItemCode; }
        public String getReplacementItemName() { return replacementItemName; }
        public String getReplacementSize() { return replacementSize; }
        public String getReason() { return reason; }
        public String getImagePath() { return imagePath; }
        public String getProcessedBy() { return processedBy; }
        
        public boolean hasImage() {
            return imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists();
        }
        
        @Override
        public String toString() {
            return String.format("%s|%d|%s|%s|%d|%s|%s|%d|%s|%s|%s|%s|%s",
                timestamp, reservationId, studentName, studentId,
                originalItemCode, originalItemName, originalSize,
                replacementItemCode, replacementItemName, replacementSize,
                reason != null ? reason.replace("|", ";") : "",
                imagePath != null ? imagePath : "",
                processedBy != null ? processedBy : "");
        }
        
        public static DamagedStockRecord fromString(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 13) return null;
            
            try {
                return new DamagedStockRecord(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    parts[2],
                    parts[3],
                    Integer.parseInt(parts[4]),
                    parts[5],
                    parts[6],
                    Integer.parseInt(parts[7]),
                    parts[8],
                    parts[9],
                    parts[10],
                    parts[11],
                    parts[12]
                );
            } catch (Exception e) {
                System.err.println("Error parsing damaged stock record: " + e.getMessage());
                return null;
            }
        }
    }
    
    /**
     * Log a damaged stock replacement
     */
    public static void logDamagedReplacement(int reservationId, String studentName, String studentId,
                                              int originalItemCode, String originalItemName, String originalSize,
                                              int replacementItemCode, String replacementItemName, String replacementSize,
                                              String reason, String imagePath, String processedBy) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        
        DamagedStockRecord record = new DamagedStockRecord(
            timestamp, reservationId, studentName, studentId,
            originalItemCode, originalItemName, originalSize,
            replacementItemCode, replacementItemName, replacementSize,
            reason, imagePath, processedBy
        );
        
        try {
            File file = new File(DAMAGED_STOCK_FILE);
            file.getParentFile().mkdirs();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(record.toString());
            }
            
            System.out.println("[DamagedStockTracker] Logged damaged replacement: " + originalItemName + " -> " + replacementItemName);
        } catch (IOException e) {
            System.err.println("[DamagedStockTracker] Error logging damaged stock: " + e.getMessage());
        }
    }
    
    /**
     * Log a damaged stock replacement with image
     */
    public static void logDamagedReplacementWithImage(int reservationId, String studentName, String studentId,
                                                       int originalItemCode, String originalItemName, String originalSize,
                                                       int replacementItemCode, String replacementItemName, String replacementSize,
                                                       String reason, File imageFile, String processedBy) {
        String imagePath = "";
        
        // Copy image to damaged_images folder if provided
        if (imageFile != null && imageFile.exists()) {
            try {
                File imagesDir = new File("database/data/damaged_images");
                imagesDir.mkdirs();
                
                String extension = getFileExtension(imageFile.getName());
                String newFileName = "damaged_" + reservationId + "_" + System.currentTimeMillis() + extension;
                File destFile = new File(imagesDir, newFileName);
                
                copyFile(imageFile, destFile);
                imagePath = destFile.getAbsolutePath();
                
                System.out.println("[DamagedStockTracker] Saved damage image: " + imagePath);
            } catch (IOException e) {
                System.err.println("[DamagedStockTracker] Error saving damage image: " + e.getMessage());
            }
        }
        
        logDamagedReplacement(reservationId, studentName, studentId,
                             originalItemCode, originalItemName, originalSize,
                             replacementItemCode, replacementItemName, replacementSize,
                             reason, imagePath, processedBy);
    }
    
    /**
     * Get all damaged stock records
     */
    public static List<DamagedStockRecord> getAllDamagedRecords() {
        List<DamagedStockRecord> records = new ArrayList<>();
        File file = new File(DAMAGED_STOCK_FILE);
        
        if (!file.exists()) {
            return records;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                DamagedStockRecord record = DamagedStockRecord.fromString(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("[DamagedStockTracker] Error reading damaged stock records: " + e.getMessage());
        }
        
        // Return in reverse order (newest first)
        java.util.Collections.reverse(records);
        return records;
    }
    
    /**
     * Get damaged stock records count
     */
    public static int getDamagedCount() {
        return getAllDamagedRecords().size();
    }
    
    /**
     * Search damaged records by student name or item name
     */
    public static List<DamagedStockRecord> searchRecords(String query) {
        List<DamagedStockRecord> all = getAllDamagedRecords();
        if (query == null || query.trim().isEmpty()) {
            return all;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        List<DamagedStockRecord> filtered = new ArrayList<>();
        
        for (DamagedStockRecord record : all) {
            if (record.getStudentName().toLowerCase().contains(lowerQuery) ||
                record.getStudentId().toLowerCase().contains(lowerQuery) ||
                record.getOriginalItemName().toLowerCase().contains(lowerQuery) ||
                record.getReplacementItemName().toLowerCase().contains(lowerQuery)) {
                filtered.add(record);
            }
        }
        
        return filtered;
    }
    
    // Helper methods
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".jpg"; // default
    }
    
    private static void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source);
             OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}
