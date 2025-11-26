package inventory;

public class Item {
    private int code;
    private String name;
    private String course;
    private String size;
    private int quantity;      // This represents AVAILABLE stock (what students can purchase)
    private int damagedStock;  // Damaged items tracked separately per size
    private double price;
    
    public Item(int code, String name, String course, String size, int quantity, double price) {
        this.code = code;
        this.name = name;
        this.course = course;
        this.size = size;
        this.quantity = quantity;
        this.damagedStock = 0;
        this.price = price;
    }
    
    // Constructor with damagedStock for loading from file
    public Item(int code, String name, String course, String size, int quantity, int damagedStock, double price) {
        this.code = code;
        this.name = name;
        this.course = course;
        this.size = size;
        this.quantity = quantity;
        this.damagedStock = damagedStock;
        this.price = price;
    }
    
    public int getCode() { return code; }
    public String getName() { return name; }
    public String getCourse() { return course; }
    public String getSize() { return size; }
    
    /**
     * Get available stock quantity (what students can purchase).
     * Does NOT include damaged stock.
     */
    public int getQuantity() { return quantity; }
    
    /**
     * Get available stock - alias for getQuantity() for clarity
     */
    public int getAvailableStock() { return quantity; }
    
    /**
     * Get damaged stock quantity (items marked as damaged, not available for sale)
     */
    public int getDamagedStock() { return damagedStock; }
    
    /**
     * Get total stock (available + damaged) for staff inventory view
     */
    public int getTotalStock() { return quantity + damagedStock; }
    
    public double getPrice() { return price; }
    
    /**
     * Set available stock quantity
     */
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    /**
     * Set damaged stock quantity
     */
    public void setDamagedStock(int damagedStock) { this.damagedStock = damagedStock; }
    
    /**
     * Add to available stock quantity
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    /**
     * Mark items as damaged - moves from available to damaged stock.
     * availableStock -= qty
     * damagedStock += qty
     * @param qty Number of items to mark as damaged
     * @return true if successful, false if not enough available stock
     */
    public boolean markAsDamaged(int qty) {
        if (qty <= 0) return false;
        if (this.quantity < qty) return false;
        
        this.quantity -= qty;
        this.damagedStock += qty;
        return true;
    }
    
    /**
     * Restore damaged items back to available stock.
     * @param qty Number of items to restore
     * @return true if successful, false if not enough damaged stock
     */
    public boolean restoreFromDamaged(int qty) {
        if (qty <= 0) return false;
        if (this.damagedStock < qty) return false;
        
        this.damagedStock -= qty;
        this.quantity += qty;
        return true;
    }
    
    /**
     * Remove damaged items from inventory completely (dispose/write-off)
     * @param qty Number of damaged items to remove
     * @return true if successful, false if not enough damaged stock
     */
    public boolean disposeDamaged(int qty) {
        if (qty <= 0) return false;
        if (this.damagedStock < qty) return false;
        
        this.damagedStock -= qty;
        return true;
    }

    public void setPrice(double price) { this.price = price; }
    
    @Override
    public String toString() {
        return String.format("%-1d | %-40s  | %-35s  | %-20s  | %-9d  | ₱%9.2f",
            code, name, course, size, quantity, price);
    }
    
    /**
     * String representation with damaged stock info for staff view
     */
    public String toStringWithDamaged() {
        return String.format("%-1d | %-40s  | %-35s  | %-20s  | Avail: %-5d  Damaged: %-5d  | ₱%9.2f",
            code, name, course, size, quantity, damagedStock, price);
    }
}