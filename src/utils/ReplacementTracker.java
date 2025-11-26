package utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReplacementTracker - Tracks all replacement requests with detailed reason categorization.
 * Separates tracking from DamagedStockTracker to distinguish between:
 * - Damaged/Defective items (actual damage)
 * - Other replacement reasons (wrong size, wrong item, quality issues, etc.)
 * 
 * Replacement Reason Categories:
 * 1. WRONG_SIZE - Wrong size received
 * 2. DAMAGED_DEFECTIVE - Item is damaged or defective
 * 3. WRONG_ITEM - Wrong item received
 * 4. POOR_QUALITY - Poor quality or material issue
 * 5. COLOR_DESIGN - Color/design not as expected
 * 6. SIZE_FIT - Size doesn't fit properly
 * 7. OTHER - Other reason
 */
public class ReplacementTracker {
    
    private static final String REPLACEMENT_FILE = "database/data/replacements.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Enum for replacement reason categories
     */
    public enum ReplacementReason {
        WRONG_SIZE("Wrong Size", "❌", "#E67E22"),
        DAMAGED_DEFECTIVE("Damaged/Defective", "🔨", "#CF222E"),
        WRONG_ITEM("Wrong Item", "📦", "#9B59B6"),
        POOR_QUALITY("Poor Quality", "⚠️", "#F39C12"),
        COLOR_DESIGN("Color/Design Issue", "🎨", "#3498DB"),
        SIZE_FIT("Size Fit Issue", "📏", "#1ABC9C"),
        OTHER("Other", "✏️", "#6C757D");
        
        private final String displayName;
        private final String icon;
        private final String color;
        
        ReplacementReason(String displayName, String icon, String color) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String getColor() { return color; }
        
        /**
         * Parse reason string from student submission to determine categories
         */
        public static Set<ReplacementReason> parseFromReasonString(String reasonText) {
            Set<ReplacementReason> reasons = new HashSet<>();
            if (reasonText == null) return reasons;
            
            String lower = reasonText.toLowerCase();
            
            if (lower.contains("[wrong size]") || lower.contains("wrong size")) {
                reasons.add(WRONG_SIZE);
            }
            if (lower.contains("[damaged") || lower.contains("[defective]") || 
                lower.contains("damaged") || lower.contains("defective")) {
                reasons.add(DAMAGED_DEFECTIVE);
            }
            if (lower.contains("[wrong item]") || lower.contains("wrong item")) {
                reasons.add(WRONG_ITEM);
            }
            if (lower.contains("[poor quality]") || lower.contains("poor quality") || 
                lower.contains("material issue")) {
                reasons.add(POOR_QUALITY);
            }
            if (lower.contains("[color") || lower.contains("[design") || 
                lower.contains("color/design") || lower.contains("not as expected")) {
                reasons.add(COLOR_DESIGN);
            }
            if (lower.contains("[size fit") || lower.contains("doesn't fit") || 
                lower.contains("size fit")) {
                reasons.add(SIZE_FIT);
            }
            if (lower.contains("[other]")) {
                reasons.add(OTHER);
            }
            
            // If no specific reason detected, mark as OTHER
            if (reasons.isEmpty()) {
                reasons.add(OTHER);
            }
            
            return reasons;
        }
    }
    
    /**
     * Represents a replacement record with categorized reasons
     */
    public static class ReplacementRecord {
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
        private String reasonText; // Original reason text from student
        private Set<ReplacementReason> reasonCategories; // Parsed categories
        private String imagePath;
        private String processedBy;
        
        public ReplacementRecord(String timestamp, int reservationId, String studentName, String studentId,
                                 int originalItemCode, String originalItemName, String originalSize,
                                 int replacementItemCode, String replacementItemName, String replacementSize,
                                 String reasonText, String imagePath, String processedBy) {
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
            this.reasonText = reasonText;
            this.reasonCategories = ReplacementReason.parseFromReasonString(reasonText);
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
        public String getReasonText() { return reasonText; }
        public Set<ReplacementReason> getReasonCategories() { return reasonCategories; }
        public String getImagePath() { return imagePath; }
        public String getProcessedBy() { return processedBy; }
        
        /**
         * Check if this replacement was due to damage
         */
        public boolean isDamageRelated() {
            return reasonCategories.contains(ReplacementReason.DAMAGED_DEFECTIVE);
        }
        
        /**
         * Check if this replacement has a specific reason
         */
        public boolean hasReason(ReplacementReason reason) {
            return reasonCategories.contains(reason);
        }
        
        /**
         * Get formatted reason categories for display
         */
        public String getFormattedReasons() {
            return reasonCategories.stream()
                .map(r -> r.getIcon() + " " + r.getDisplayName())
                .collect(Collectors.joining(", "));
        }
        
        /**
         * Get primary reason (first one if multiple)
         */
        public ReplacementReason getPrimaryReason() {
            // Priority: Damaged > Wrong Item > Wrong Size > Size Fit > Quality > Color > Other
            if (reasonCategories.contains(ReplacementReason.DAMAGED_DEFECTIVE)) return ReplacementReason.DAMAGED_DEFECTIVE;
            if (reasonCategories.contains(ReplacementReason.WRONG_ITEM)) return ReplacementReason.WRONG_ITEM;
            if (reasonCategories.contains(ReplacementReason.WRONG_SIZE)) return ReplacementReason.WRONG_SIZE;
            if (reasonCategories.contains(ReplacementReason.SIZE_FIT)) return ReplacementReason.SIZE_FIT;
            if (reasonCategories.contains(ReplacementReason.POOR_QUALITY)) return ReplacementReason.POOR_QUALITY;
            if (reasonCategories.contains(ReplacementReason.COLOR_DESIGN)) return ReplacementReason.COLOR_DESIGN;
            return ReplacementReason.OTHER;
        }
        
        public boolean hasImage() {
            return imagePath != null && !imagePath.isEmpty() && new File(imagePath).exists();
        }
        
        @Override
        public String toString() {
            return String.format("%s|%d|%s|%s|%d|%s|%s|%d|%s|%s|%s|%s|%s",
                timestamp, reservationId, studentName, studentId,
                originalItemCode, originalItemName, originalSize,
                replacementItemCode, replacementItemName, replacementSize,
                reasonText != null ? reasonText.replace("|", ";") : "",
                imagePath != null ? imagePath : "",
                processedBy != null ? processedBy : "");
        }
        
        public static ReplacementRecord fromString(String line) {
            String[] parts = line.split("\\|", -1);
            if (parts.length < 13) return null;
            
            try {
                return new ReplacementRecord(
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
                System.err.println("[ReplacementTracker] Error parsing record: " + e.getMessage());
                return null;
            }
        }
    }
    
    /**
     * Log a replacement
     */
    public static void logReplacement(int reservationId, String studentName, String studentId,
                                      int originalItemCode, String originalItemName, String originalSize,
                                      int replacementItemCode, String replacementItemName, String replacementSize,
                                      String reason, String imagePath, String processedBy) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        
        ReplacementRecord record = new ReplacementRecord(
            timestamp, reservationId, studentName, studentId,
            originalItemCode, originalItemName, originalSize,
            replacementItemCode, replacementItemName, replacementSize,
            reason, imagePath, processedBy
        );
        
        try {
            File file = new File(REPLACEMENT_FILE);
            file.getParentFile().mkdirs();
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
                writer.println(record.toString());
            }
            
            System.out.println("[ReplacementTracker] Logged replacement: " + originalItemName + " -> " + replacementItemName + 
                               " | Reasons: " + record.getFormattedReasons());
        } catch (IOException e) {
            System.err.println("[ReplacementTracker] Error logging replacement: " + e.getMessage());
        }
    }
    
    /**
     * Log a replacement with image file
     */
    public static void logReplacementWithImage(int reservationId, String studentName, String studentId,
                                               int originalItemCode, String originalItemName, String originalSize,
                                               int replacementItemCode, String replacementItemName, String replacementSize,
                                               String reason, File imageFile, String processedBy) {
        String imagePath = "";
        
        if (imageFile != null && imageFile.exists()) {
            try {
                File imagesDir = new File("database/data/replacement_images");
                imagesDir.mkdirs();
                
                String extension = getFileExtension(imageFile.getName());
                String newFileName = "replacement_" + reservationId + "_" + System.currentTimeMillis() + extension;
                File destFile = new File(imagesDir, newFileName);
                
                copyFile(imageFile, destFile);
                imagePath = destFile.getAbsolutePath();
                
                System.out.println("[ReplacementTracker] Saved replacement image: " + imagePath);
            } catch (IOException e) {
                System.err.println("[ReplacementTracker] Error saving image: " + e.getMessage());
            }
        }
        
        logReplacement(reservationId, studentName, studentId,
                      originalItemCode, originalItemName, originalSize,
                      replacementItemCode, replacementItemName, replacementSize,
                      reason, imagePath, processedBy);
    }
    
    /**
     * Get all replacement records
     */
    public static List<ReplacementRecord> getAllRecords() {
        List<ReplacementRecord> records = new ArrayList<>();
        File file = new File(REPLACEMENT_FILE);
        
        if (!file.exists()) {
            // Also check legacy damaged_stock.txt for migration
            return migrateLegacyRecords();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                ReplacementRecord record = ReplacementRecord.fromString(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("[ReplacementTracker] Error reading records: " + e.getMessage());
        }
        
        // Also include legacy records
        records.addAll(migrateLegacyRecords());
        
        // Remove duplicates based on reservationId and timestamp
        Set<String> seen = new HashSet<>();
        records = records.stream()
            .filter(r -> seen.add(r.getReservationId() + "_" + r.getTimestamp()))
            .collect(Collectors.toList());
        
        // Sort by timestamp descending (newest first)
        records.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return records;
    }
    
    /**
     * Migrate legacy DamagedStockTracker records
     */
    private static List<ReplacementRecord> migrateLegacyRecords() {
        List<ReplacementRecord> records = new ArrayList<>();
        File legacyFile = new File("database/data/damaged_stock.txt");
        
        if (!legacyFile.exists()) {
            return records;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(legacyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                ReplacementRecord record = ReplacementRecord.fromString(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("[ReplacementTracker] Error reading legacy records: " + e.getMessage());
        }
        
        return records;
    }
    
    /**
     * Get total replacement count
     */
    public static int getTotalCount() {
        return getAllRecords().size();
    }
    
    /**
     * Get count by specific reason
     */
    public static int getCountByReason(ReplacementReason reason) {
        return (int) getAllRecords().stream()
            .filter(r -> r.hasReason(reason))
            .count();
    }
    
    /**
     * Get counts for all reasons (for dashboard display)
     */
    public static Map<ReplacementReason, Integer> getCountsByReason() {
        Map<ReplacementReason, Integer> counts = new EnumMap<>(ReplacementReason.class);
        for (ReplacementReason reason : ReplacementReason.values()) {
            counts.put(reason, 0);
        }
        
        for (ReplacementRecord record : getAllRecords()) {
            for (ReplacementReason reason : record.getReasonCategories()) {
                counts.merge(reason, 1, Integer::sum);
            }
        }
        
        return counts;
    }
    
    /**
     * Get records filtered by reason
     */
    public static List<ReplacementRecord> getRecordsByReason(ReplacementReason reason) {
        return getAllRecords().stream()
            .filter(r -> r.hasReason(reason))
            .collect(Collectors.toList());
    }
    
    /**
     * Get only damage-related replacements
     */
    public static List<ReplacementRecord> getDamagedRecords() {
        return getRecordsByReason(ReplacementReason.DAMAGED_DEFECTIVE);
    }
    
    /**
     * Get damage-related count (for backward compatibility with DamagedStockTracker)
     */
    public static int getDamagedCount() {
        return getCountByReason(ReplacementReason.DAMAGED_DEFECTIVE);
    }
    
    /**
     * Search records by student name or item name
     */
    public static List<ReplacementRecord> searchRecords(String query) {
        List<ReplacementRecord> all = getAllRecords();
        if (query == null || query.trim().isEmpty()) {
            return all;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        return all.stream()
            .filter(r -> r.getStudentName().toLowerCase().contains(lowerQuery) ||
                        r.getStudentId().toLowerCase().contains(lowerQuery) ||
                        r.getOriginalItemName().toLowerCase().contains(lowerQuery) ||
                        r.getReplacementItemName().toLowerCase().contains(lowerQuery) ||
                        r.getReasonText().toLowerCase().contains(lowerQuery))
            .collect(Collectors.toList());
    }
    
    /**
     * Get replacement summary for dashboard display
     */
    public static ReplacementSummary getSummary() {
        return new ReplacementSummary(getAllRecords());
    }
    
    /**
     * Summary class for dashboard display
     */
    public static class ReplacementSummary {
        private final int totalReplacements;
        private final Map<ReplacementReason, Integer> countsByReason;
        private final int damagedCount;
        private final int nonDamagedCount;
        
        public ReplacementSummary(List<ReplacementRecord> records) {
            this.totalReplacements = records.size();
            this.countsByReason = new EnumMap<>(ReplacementReason.class);
            
            for (ReplacementReason reason : ReplacementReason.values()) {
                countsByReason.put(reason, 0);
            }
            
            int damaged = 0;
            for (ReplacementRecord record : records) {
                for (ReplacementReason reason : record.getReasonCategories()) {
                    countsByReason.merge(reason, 1, Integer::sum);
                }
                if (record.isDamageRelated()) {
                    damaged++;
                }
            }
            
            this.damagedCount = damaged;
            this.nonDamagedCount = totalReplacements - damaged;
        }
        
        public int getTotalReplacements() { return totalReplacements; }
        public Map<ReplacementReason, Integer> getCountsByReason() { return countsByReason; }
        public int getDamagedCount() { return damagedCount; }
        public int getNonDamagedCount() { return nonDamagedCount; }
        
        public int getCount(ReplacementReason reason) {
            return countsByReason.getOrDefault(reason, 0);
        }
    }
    
    /**
     * Create a ReplacementRecord from a Reservation object.
     * Used to sync replacement records from the reservation system.
     * 
     * @param reservation The reservation with status REPLACED
     * @return ReplacementRecord created from reservation data
     */
    public static ReplacementRecord fromReservation(inventory.Reservation reservation) {
        if (reservation == null || !reservation.getStatus().equals("REPLACED")) {
            return null;
        }
        
        // Format timestamp from reservation's completed date or reservation time
        String timestamp;
        if (reservation.getCompletedDate() != null) {
            timestamp = reservation.getCompletedDate().format(FORMATTER);
        } else if (reservation.getReservationTime() != null) {
            timestamp = reservation.getReservationTime().format(FORMATTER);
        } else {
            timestamp = LocalDateTime.now().format(FORMATTER);
        }
        
        // Get reason text from reservation (this is the replacement note/reason)
        String reasonText = reservation.getReplacementNote();
        if (reasonText == null || reasonText.isEmpty()) {
            reasonText = reservation.getReason();
        }
        if (reasonText == null || reasonText.isEmpty()) {
            reasonText = "[Other] - Replacement processed";
        }
        
        // Get replacement item info (may be same as original if not tracked)
        int replacementItemCode = reservation.getReplacementItemCode() > 0 
            ? reservation.getReplacementItemCode() : reservation.getItemCode();
        String replacementItemName = reservation.getReplacementItemName() != null 
            ? reservation.getReplacementItemName() : reservation.getItemName();
        String replacementSize = reservation.getReplacementSize() != null 
            ? reservation.getReplacementSize() : reservation.getSize();
        
        return new ReplacementRecord(
            timestamp,
            reservation.getReservationId(),
            reservation.getStudentName(),
            reservation.getStudentId(),
            reservation.getItemCode(),
            reservation.getItemName(),
            reservation.getSize() != null ? reservation.getSize() : "",
            replacementItemCode,
            replacementItemName,
            replacementSize != null ? replacementSize : "",
            reasonText,
            reservation.getClaimProofImagePath() != null ? reservation.getClaimProofImagePath() : "",
            "System" // Processed by - from existing reservations
        );
    }
    
    /**
     * Get all replacement records including those from reservation system.
     * This combines file-based records with reservation data.
     * 
     * @param reservations List of reservations to include (filter to REPLACED status)
     * @return Combined list of all replacement records
     */
    public static List<ReplacementRecord> getAllRecordsWithReservations(java.util.List<inventory.Reservation> reservations) {
        List<ReplacementRecord> records = new ArrayList<>();
        
        // Get file-based records
        records.addAll(getAllRecords());
        
        // Get set of existing reservation IDs to avoid duplicates
        Set<Integer> existingIds = records.stream()
            .map(ReplacementRecord::getReservationId)
            .collect(Collectors.toSet());
        
        // Add records from reservations with REPLACED status
        if (reservations != null) {
            for (inventory.Reservation r : reservations) {
                if ("REPLACED".equals(r.getStatus()) && !existingIds.contains(r.getReservationId())) {
                    ReplacementRecord record = fromReservation(r);
                    if (record != null) {
                        records.add(record);
                        existingIds.add(r.getReservationId());
                    }
                }
            }
        }
        
        // Sort by timestamp descending (newest first)
        records.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
        return records;
    }
    
    /**
     * Get replacement summary including reservation data.
     * 
     * @param reservations List of reservations to include
     * @return Summary with complete replacement statistics
     */
    public static ReplacementSummary getSummaryWithReservations(java.util.List<inventory.Reservation> reservations) {
        return new ReplacementSummary(getAllRecordsWithReservations(reservations));
    }
    
    // Helper methods
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".jpg";
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
