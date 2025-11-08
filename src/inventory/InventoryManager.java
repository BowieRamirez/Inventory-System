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
    
    public List<Item> getItemsByCourse(String course) {
        List<Item> result = new ArrayList<>();
        for (Item item : inventory) {
            // Show items that match the student's course OR are "STI Special" (universal items)
            if ((item.getCourse().equalsIgnoreCase(course) || item.getCourse().equalsIgnoreCase("STI Special"))
                && item.getQuantity() > 0) {
                result.add(item);
            }
        }
        return result;
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
    
    // ✅ Restock item (used for returns/refunds)
    public boolean restockItem(int code, String size, int quantity) {
        return addStock(code, size, quantity);
    }


    public List<String> getAvailableCourses() {
        Set<String> courses = new HashSet<>();
        for (Item item : inventory) {
            courses.add(item.getCourse());
        }
        return new ArrayList<>(courses);
    }
    
    // Find all size variants of an item by name and course
    public List<Item> findSizeVariants(String itemName, String course) {
        List<Item> variants = new ArrayList<>();
        for (Item item : inventory) {
            if (item.getName().equalsIgnoreCase(itemName) && 
                item.getCourse().equalsIgnoreCase(course) &&
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