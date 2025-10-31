package inventory;

import java.util.*;
import utils.FileStorage;

public class InventoryManager {
    private List<Item> inventory;
    private Map<Integer, Item> itemByCodeMap; // HashMap for quick lookup by code
    
    public InventoryManager() {
        inventory = new ArrayList<>();
        itemByCodeMap = new HashMap<>();
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
}