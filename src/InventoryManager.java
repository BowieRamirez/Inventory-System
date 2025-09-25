import java.util.*;

public class InventoryManager {
    private List<Item> inventory;
    
    public InventoryManager() {
        inventory = new ArrayList<>();
    }
    
    public void addItem(Item item) {
        inventory.add(item);
    }
    
    public boolean removeItem(int code) {
        return inventory.removeIf(item -> item.getCode() == code);
    }
    
    public Item findItemByCode(int code) {
        for (Item item : inventory) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(inventory);
    }
    
    public List<Item> getItemsByCourse(String course) {
        List<Item> courseItems = new ArrayList<>();
        for (Item item : inventory) {
            if (item.getCourse().equalsIgnoreCase(course)) {
                courseItems.add(item);
            }
        }
        return courseItems;
    }
    
    public void displayAllItems() {
        if (inventory.isEmpty()) {
            System.out.println("No items in inventory.");
            return;
        }
        
        System.out.println("\n=== INVENTORY STOCK ===");
        System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
        System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
        
        for (Item item : inventory) {
            System.out.println(item);
        }
        System.out.println();
    }
    
    public void displayItemsByCourse(String course) {
        List<Item> courseItems = getItemsByCourse(course);
        
        if (courseItems.isEmpty()) {
            System.out.println("No items found for course: " + course);
            return;
        }
        
        System.out.println("\n=== " + course.toUpperCase() + " STOCK ===");
        System.out.println("Code   | Name                      | Course               | Size     | Quantity | Price");
        System.out.println("-------|---------------------------|----------------------|----------|----------|----------");
        
        for (Item item : courseItems) {
            System.out.println(item);
        }
        System.out.println();
    }
    
    public boolean updateItemQuantity(int code, int newQuantity) {
        Item item = findItemByCode(code);
        if (item != null) {
            item.setQuantity(newQuantity);
            return true;
        }
        return false;
    }
    
    public boolean reserveItem(int code, int quantity) {
        Item item = findItemByCode(code);
        if (item != null && item.getQuantity() >= quantity) {
            item.reduceQuantity(quantity);
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
}