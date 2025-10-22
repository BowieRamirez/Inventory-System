package inventory;

import java.util.*;
import utils.FileStorage;

public class InventoryManager {
    private List<Item> inventory;
    private Map<Integer, Item> itemByCodeMap; // HashMap for quick lookup by code
    
    public InventoryManager() {
        inventory = new ArrayList<>();
        itemByCodeMap = new HashMap<>();
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
            if (item.getCourse().equalsIgnoreCase(course)) {
                result.add(item);
            }
        }
        return result;
    }
    
    public void displayAllItems() {
        if (inventory.isEmpty()) {
            System.out.println("No items in inventory.");
            return;
        }
        
        // Group items by name and course
        Map<String, List<Item>> groupedItems = new LinkedHashMap<>();
        for (Item item : inventory) {
            String key = item.getName() + "|" + item.getCourse();
            groupedItems.putIfAbsent(key, new ArrayList<>());
            groupedItems.get(key).add(item);
        }
        
        System.out.println("\n=== ALL ITEMS ===");
        System.out.println("Code     | Name                                    | Course                     | Size                                                      | Price");
        System.out.println("---------|------------------------------------------|----------------------------|-----------------------------------------------------------|------------");
        
        for (Map.Entry<String, List<Item>> entry : groupedItems.entrySet()) {
            List<Item> variants = entry.getValue();
            Item firstItem = variants.get(0);
            
            // Build size string with quantities
            StringBuilder sizeInfo = new StringBuilder();
            for (int i = 0; i < variants.size(); i++) {
                Item variant = variants.get(i);
                sizeInfo.append(variant.getSize()).append(" (").append(variant.getQuantity()).append(")");
                if (i < variants.size() - 1) {
                    sizeInfo.append(", ");
                }
            }
            
            // Display one row per item group
            System.out.printf("%-8d | %-40s | %-26s | %-57s | ₱%-10.2f\n",
                firstItem.getCode(),
                firstItem.getName(),
                firstItem.getCourse(),
                sizeInfo.toString(),
                firstItem.getPrice());
        }
    }
    
    public void displayItemsByCourse(String course) {
        List<Item> items = getItemsByCourse(course);
        if (items.isEmpty()) {
            System.out.println("No items found for course: " + course);
            return;
        }
        
        // Group items by name
        Map<String, List<Item>> groupedItems = new LinkedHashMap<>();
        for (Item item : items) {
            groupedItems.putIfAbsent(item.getName(), new ArrayList<>());
            groupedItems.get(item.getName()).add(item);
        }
        
        System.out.println("\n=== ITEMS FOR " + course + " ===");
        System.out.println("Code     | Name                                    | Course                     | Size                                                      | Price");
        System.out.println("---------|------------------------------------------|----------------------------|-----------------------------------------------------------|------------");
        
        for (Map.Entry<String, List<Item>> entry : groupedItems.entrySet()) {
            List<Item> variants = entry.getValue();
            Item firstItem = variants.get(0);
            
            // Build size string with quantities
            StringBuilder sizeInfo = new StringBuilder();
            for (int i = 0; i < variants.size(); i++) {
                Item variant = variants.get(i);
                sizeInfo.append(variant.getSize()).append(" (").append(variant.getQuantity()).append(")");
                if (i < variants.size() - 1) {
                    sizeInfo.append(", ");
                }
            }
            
            // Display one row per item group
            System.out.printf("%-8d | %-40s | %-26s | %-57s | ₱%-10.2f\n",
                firstItem.getCode(),
                firstItem.getName(),
                firstItem.getCourse(),
                sizeInfo.toString(),
                firstItem.getPrice());
        }
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
                item.setQuantity(newQuantity);
                // Save updated inventory to file
                FileStorage.saveItems(inventory);
                return true;
            }
        }
        return false;
    }
    
    // ✅ Update item quantity by code and size
    public boolean updateItemQuantityBySize(int code, String size, int newQuantity) {
        Item item = findItemByCodeAndSize(code, size);
        if (item != null) {
            item.setQuantity(newQuantity);
            // Save updated inventory to file
            FileStorage.saveItems(inventory);
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
            return true;
        }
        return false;
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
    
    // Rebuild HashMap from inventory list (useful for data integrity)
    public void rebuildHashMap() {
        itemByCodeMap.clear();
        for (Item item : inventory) {
            itemByCodeMap.put(item.getCode(), item);
        }
    }
    
    // Get inventory size for debugging
    public int getInventorySize() {
        return inventory.size();
    }
    
    // Get HashMap size for debugging
    public int getHashMapSize() {
        return itemByCodeMap.size();
    }
}