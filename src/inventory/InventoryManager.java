package inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import audit.StockAuditManager;
import utils.FileStorage;
import utils.SystemLogger;

public class InventoryManager {
    private final List<Item> inventory;
    private final Map<Integer, Item> itemByCodeMap; // HashMap for quick lookup by code
    private final StockAuditManager auditManager;
    
    public InventoryManager() {
        inventory = new ArrayList<>();
        itemByCodeMap = new HashMap<>();
        auditManager = new StockAuditManager();
        loadItemsFromFile();
    }

    /**
     * Load all items from file during initialization
     */
    private void loadItemsFromFile() {
        List<Item> loadedItems = FileStorage.loadItems();
        for (Item item : loadedItems) {
            loadItem(item);
        }
    }

    /**
     * Normalize course names so SHS variants map to a single canonical value `SHS`.
     * This keeps the rest of the codebase working without changing callers.
     */
    private String normalizeCourse(String course) {
        if (course == null) return "";
        String c = course.trim();
        String cu = c.toUpperCase();

        // Map senior high related course labels to canonical "SHS"
        if (cu.equals("ABM") || cu.equals("STEM") || cu.equals("HUMSS") || cu.equals("IT")
                || cu.equals("T.O") || cu.equals("TO")
                || cu.startsWith("TVL") || cu.startsWith("TVL-")) {
            return "SHS";
        }

        return c;
    }
    
    // Load items from file without saving (used during initialization)
    public void loadItem(Item item) {
        inventory.add(item);
        itemByCodeMap.put(item.getCode(), item);
    }
    
    // Add new item and save to file
    public void addItem(Item item) {
        inventory.add(item);
        itemByCodeMap.put(item.getCode(), item);
        // Save to file immediately after adding
        FileStorage.saveItems(inventory);
    }
    
    public boolean removeItem(int code) {
        boolean removed = inventory.removeIf(item -> item.getCode() == code);
        if (removed) {
            itemByCodeMap.remove(code);
            // Save to file immediately after removing
            FileStorage.saveItems(inventory);
        }
        return removed;
    }
    
    public Item findItemByCode(int code) {
        // Use HashMap for O(1) lookup instead of O(n) iteration
        return itemByCodeMap.get(code);
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(inventory);
    }

    /**
     * Find all size variants of an item by item code
     */
    public List<Item> findItemsByCode(int code) {
        List<Item> variants = new ArrayList<>();
        for (Item item : inventory) {
            if (item.getCode() == code) variants.add(item);
        }
        return variants;
    }
    
    public List<Item> getItemsByCourse(String course) {
        List<Item> result = new ArrayList<>();
        String rawSel = course == null ? "" : course.trim();
        String selUpper = rawSel.toUpperCase();
        String selNorm = normalizeCourse(rawSel);

        for (Item item : inventory) {
            if (item.getQuantity() <= 0) continue;

            String rawItemCourse = item.getCourse() == null ? "" : item.getCourse().trim();
            String itemNorm = normalizeCourse(rawItemCourse);
            String nameLower = item.getName() == null ? "" : item.getName().toLowerCase();

            boolean include = false;

            // Universal items (STI Special)
            if ("STI Special".equalsIgnoreCase(rawItemCourse) || "STI SPECIAL".equalsIgnoreCase(rawItemCourse)) {
                include = true;
            }

            // Selection == All: include most items but hide strand-restricted special items (lab coat, chef items)
            if (!include && (selUpper.isEmpty() || selUpper.equals("ALL"))) {
                if (nameLower.contains("lab coat")) {
                    include = false;
                } else if (nameLower.contains("chef") || nameLower.contains("apron") || nameLower.contains("tvl chef") || nameLower.contains("cul art") || nameLower.contains("culinary") || nameLower.contains("cap")) {
                    include = false;
                } else {
                    include = true;
                }
            }

            // Specific selection handling
            if (!include && !selUpper.isEmpty() && !selUpper.equals("ALL")) {
                // SHS-derived selection (HUMSS, ABM, STEM, IT, T.O, TVL-CA)
                if ("SHS".equalsIgnoreCase(selNorm)) {
                    if ("SHS".equalsIgnoreCase(itemNorm)) {
                        // Lab coat -> only for STEM
                        if (nameLower.contains("lab coat")) {
                            include = selUpper.contains("STEM");
                        }
                        // Culinary / TVL chef items -> only for TVL-CA
                        else if (nameLower.contains("chef") || nameLower.contains("apron") || nameLower.contains("tvl chef") || nameLower.contains("cul art") || nameLower.contains("culinary") || nameLower.contains("cap")) {
                            include = selUpper.equals("TVL-CA") || selUpper.equals("TVL CA") || selUpper.equals("CUL ART") || selUpper.equals("CULART");
                        } else {
                            // Regular SHS garments (vests, shirts, skirts, pants) should show for any SHS-derived selection.
                            // Accept variants such as HUMSS and other aliases by allowing the normalized SHS selection.
                            include = true;
                        }
                    } else {
                        // Item has non-SHS course label; include only if raw course matches selection
                        include = courseMatches(rawItemCourse, rawSel);
                    }
                } else {
                    // Non-SHS normalized selection: include when normalized or raw match
                    include = courseMatches(rawItemCourse, rawSel) || courseMatches(itemNorm, selNorm);
                }
            }

            // Final name-based special inclusion
            if (!include && isSpecialForCourse(item, rawSel)) include = true;

            if (include) result.add(item);
        }

        // Order results: show regular uniforms first, then special items (STI Special, lab coats, chef/apron, berets)
        java.util.List<Item> uniforms = new java.util.ArrayList<>();
        java.util.List<Item> specials = new java.util.ArrayList<>();
        for (Item it : result) {
            String nm = it.getName() == null ? "" : it.getName().toLowerCase();
            String rawCourse = it.getCourse() == null ? "" : it.getCourse();
            boolean isStiSpecial = "STI Special".equalsIgnoreCase(rawCourse) || "STI SPECIAL".equalsIgnoreCase(rawCourse);
            boolean isNameSpecial = nm.contains("lab coat") || nm.contains("chef") || nm.contains("apron") || nm.contains("beret") || nm.contains("cul art") || nm.contains("culinary") || nm.contains("cap");
            if (isStiSpecial || isNameSpecial || isSpecialForCourse(it, rawSel)) {
                specials.add(it);
            } else {
                uniforms.add(it);
            }
        }

        java.util.List<Item> ordered = new java.util.ArrayList<>();
        ordered.addAll(uniforms);
        ordered.addAll(specials);
        return ordered;
    }

    /**
     * Determine if an item should be considered special for a given course selection.
     * Current rules:
     * - Items whose name contains "Lab Coat" are considered special for STEM students
     * - Items whose name contains "Beret" are considered special for T.O (Tourism) students
     */
    private boolean isSpecialForCourse(Item item, String requestedCourse) {
        if (item == null || requestedCourse == null) return false;
        String name = item.getName() == null ? "" : item.getName().toLowerCase();
        String rc = requestedCourse.trim().toUpperCase();
        if (rc.contains("STEM") && name.contains("lab coat")) return true;
        if ((rc.equals("T.O") || rc.equals("TO") || rc.equals("TVL-TO") || rc.equals("TOURISM")) && name.contains("beret")) return true;
        return false;
    }

    /**
     * Helper to check whether an item's course label should be considered a match
     * for the selected course. This handles combined labels like "BSCS/BSIT/BSCpE"
     * and comma-separated values.
     */
    private boolean courseMatches(String itemCourseRaw, String selRaw) {
        if (itemCourseRaw == null || selRaw == null) return false;
        String a = itemCourseRaw.trim();
        String b = selRaw.trim();
        if (a.equalsIgnoreCase(b)) return true;
        // split on slash or comma
        if (a.contains("/")) {
            for (String p : a.split("/")) {
                if (p.trim().equalsIgnoreCase(b)) return true;
            }
        }
        if (a.contains(",")) {
            for (String p : a.split(",")) {
                if (p.trim().equalsIgnoreCase(b)) return true;
            }
        }
        // token match (fallback)
        for (String p : a.split("\\s+")) {
            if (p.trim().equalsIgnoreCase(b)) return true;
        }
        return false;
    }
    

    
    public Item findItemByCodeAndSize(int code, String size) {
        for (Item item : inventory) {
            if (item.getCode() == code && item.getSize().equalsIgnoreCase(size)) {
                return item;
            }
        }
        return null;
    }

    // ✅ Reservation should NOT deduct stock immediately
    public boolean reserveItem(int code, String size, int quantity) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null && item.getQuantity() >= quantity) {
            // Do not deduct yet — just confirm availability
            return true;
        }
        return false;
    }

    // ✅ Deduct stock ONLY upon approval
    public boolean deductStockOnApproval(int code, String size, int quantity) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null && item.getQuantity() >= quantity) {
            item.setQuantity(item.getQuantity() - quantity);
            // Save updated inventory to file
            FileStorage.saveItems(inventory);
            return true;
        }
        return false;
    }

    // ✅ Update item quantity
    public boolean updateItemQuantity(int code, int newQuantity) {
        for (Item item : inventory) {
            if (item.getCode() == code) {
                int oldQuantity = item.getQuantity();
                item.setQuantity(newQuantity);
                // Save updated inventory to file
                FileStorage.saveItems(inventory);
                // Log stock adjustment
                int adjustment = newQuantity - oldQuantity;
                SystemLogger.logStockAdjustment("Admin", item.getName(), adjustment, newQuantity);
                return true;
            }
        }
        return false;
    }
    
    // ✅ Update item quantity by code and size
    public boolean updateItemQuantityBySize(int code, String size, int newQuantity) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null) {
            int oldQuantity = item.getQuantity();
            item.setQuantity(newQuantity);
            // Save updated inventory to file
            FileStorage.saveItems(inventory);
            // Log stock adjustment
            int adjustment = newQuantity - oldQuantity;
            SystemLogger.logStockAdjustment("Admin", item.getName() + " (" + size + ")", adjustment, newQuantity);
            return true;
        }
        return false;
    }
    
    // ✅ Add new stock to existing item
    public boolean addStock(int code, String size, int quantity) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null) {
            item.addQuantity(quantity);
            // Save updated inventory to file
            FileStorage.saveItems(inventory);
            // Log stock addition
            SystemLogger.logStockAdjustment("Admin", item.getName() + " (" + size + ")", quantity, item.getQuantity());
            return true;
        }
        return false;
    }

    // ✅ Update item price by code and size
    public boolean updateItemPriceBySize(int code, String size, double newPrice) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null) {
            double oldPrice = item.getPrice();
            item.setPrice(newPrice);
            // Save updated inventory to file
            FileStorage.saveItems(inventory);
            // Log price update activity
            SystemLogger.logActivity("Price updated: " + item.getName() + " (" + size + ") " + String.format("₱%.2f -> ₱%.2f", oldPrice, newPrice));
            return true;
        }
        return false;
    }

    /**
     * Update item price for all size variants that share the same item code.
     * This will set the price for every variant with matching code and persist once.
     */
    public boolean updateItemPriceByCode(int code, double newPrice) {
        boolean changed = false;
        for (Item item : inventory) {
            if (item.getCode() == code) {
                double oldPrice = item.getPrice();
                if (Double.compare(oldPrice, newPrice) != 0) {
                    item.setPrice(newPrice);
                    // Log per-variant price update
                    SystemLogger.logActivity("Price updated: " + item.getName() + " (" + item.getSize() + ") " + String.format("₱%.2f -> ₱%.2f", oldPrice, newPrice));
                    // Also write to legacy stock logs per size for audit
                    utils.StockReturnLogger.logPriceChange("staff", item.getCode(), item.getName(), item.getSize(), oldPrice, newPrice);
                    changed = true;
                }
            }
        }
        if (changed) {
            FileStorage.saveItems(inventory);
        }
        return changed;
    }
    
    // ✅ Restock item (used for returns/refunds)
    public boolean restockItem(int code, String size, int quantity) {
        return addStock(code, size, quantity);
    }


    public List<String> getAvailableCourses() {
        Set<String> courses = new HashSet<>();
        for (Item item : inventory) {
            String raw = normalizeCourse(item.getCourse());
            // Special handling: SHS items map to multiple SHS-subcourses (HUMSS, ABM, STEM, IT, T.O)
                if ("SHS".equalsIgnoreCase(raw)) {
                courses.add("HUMSS");
                courses.add("ABM");
                courses.add("STEM");
                courses.add("IT");
                courses.add("T.O");
                courses.add("TVL-CA");
                continue;
            }

            // Keep combined entries (like "BSCS/BSIT/BSCpE") as-is but also expose individual parts
            if (raw.contains("/")) {
                courses.add(raw);
                for (String p : raw.split("/")) {
                    String t = p.trim();
                    if (!t.isEmpty()) courses.add(t);
                }
            } else {
                // Some backups or entries may contain multiple course codes separated by commas; tokenized parts
                String[] parts = raw.split(",|/");
                for (String p : parts) {
                    String t = p.trim();
                    if (!t.isEmpty()) courses.add(t);
                }
            }
        }
        // Keep both combined and individual business labels so majors like "BSBA" and "BSA"
        // remain selectable even when a combined entry "BSBA/BSA" exists in the data.

        return new ArrayList<>(courses);
    }
    
    // Find all size variants of an item by name and course
    public List<Item> findSizeVariants(String itemName, String course) {
        List<Item> variants = new ArrayList<>();
        String target = normalizeCourse(course);
        for (Item item : inventory) {
            String itemCourse = normalizeCourse(item.getCourse());
            if (item.getName().equalsIgnoreCase(itemName) && 
                itemCourse.equalsIgnoreCase(target) &&
                item.getQuantity() > 0) {
                variants.add(item);
            }
        }
        return variants;
    }
    
    // ============================================================================
    // 🔐 STAFF-ONLY METHODS - Stock modifications must be done by Staff with auditing
    // ============================================================================
    
    /**
     * STAFF-ONLY: Request a stock adjustment (with audit trail)
     * Only Staff role can modify stocks - all changes are logged and require Admin approval
     * Note: Role verification is handled at controller level before calling this method
     */
    public boolean requestStockAdjustment(String staffUsername, int itemCode, String itemSize, 
                                         int newQuantity, String reason) {
        Item item = findItemByCodeAndSize(itemCode, itemSize);
        if (item == null) {
            SystemLogger.logError("Stock adjustment failed: Item not found", new Exception("Item code: " + itemCode));
            return false;
        }
        
        // Validate adjustment
        if (newQuantity < 0) {
            SystemLogger.logWarning("Stock adjustment rejected: Negative quantity not allowed (" + newQuantity + ")");
            return false;
        }
        
        int oldQuantity = item.getQuantity();
        int diff = newQuantity - oldQuantity;
        
        // Create audit log entry (PENDING approval)
        auditManager.logStockChange(staffUsername, item.getName(), itemCode, itemSize, 
                                   oldQuantity, newQuantity, reason, 
                                   diff > 0 ? "ADD" : (diff < 0 ? "REMOVE" : "ADJUST"));
        
        // Log to CLI
        SystemLogger.logActivity("📝 Staff " + staffUsername + " REQUESTED stock adjustment for " + 
                               item.getName() + ": " + oldQuantity + " → " + newQuantity + " (Reason: " + reason + ")");
        
        return true;
    }
    
    /**
     * ADMIN-ONLY: Approve a pending stock change and apply it
     */
    public boolean approveAndApplyStockChange(String logId, String adminUsername) {
        // Approve in audit manager
        if (!auditManager.approveChange(logId, adminUsername)) {
            SystemLogger.logError("Failed to approve stock change", new Exception("Log not found: " + logId));
            return false;
        }
        
        // Note: In a complete system, we would apply the change here
        // For now, the audit log tracks the approval
        SystemLogger.logActivity("✅ Admin " + adminUsername + " approved stock change: " + logId);
        return true;
    }
    
    /**
     * ADMIN-ONLY: Reject a pending stock change
     */
    public boolean rejectStockChange(String logId, String adminUsername, String rejectionReason) {
        if (!auditManager.rejectChange(logId, adminUsername, rejectionReason)) {
            SystemLogger.logError("Failed to reject stock change", new Exception("Log not found: " + logId));
            return false;
        }
        
        SystemLogger.logActivity("❌ Admin " + adminUsername + " rejected stock change: " + logId);
        return true;
    }
    
    /**
     * Get pending stock change requests (for Admin Dashboard)
     */
    public List<audit.StockAuditLog> getPendingStockChanges() {
        return auditManager.getPendingChanges();
    }
    
    /**
     * Get audit trail for a specific staff member
     */
    public List<audit.StockAuditLog> getStaffAuditTrail(String staffUsername) {
        return auditManager.getChangesByStaff(staffUsername);
    }
    
    /**
     * Get complete audit trail
     */
    public List<audit.StockAuditLog> getCompleteAuditTrail() {
        return auditManager.getAllLogs();
    }
    
    /**
     * Export audit trail to CSV
     */
    public void exportAuditTrailToCSV(String filename) {
        auditManager.exportToCSV(filename);
    }
    
    /**
     * Print audit summary to console
     */
    public void printAuditSummary() {
        auditManager.printAuditSummary();
    }
    
    /**
     * Get the audit manager instance
     */
    public StockAuditManager getAuditManager() {
        return auditManager;
    }
}