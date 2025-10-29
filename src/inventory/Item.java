package inventory;

public class Item {
    private int code;
    private String name;
    private String course;
    private String size;
    private int quantity;
    private double price;
    
    public Item(int code, String name, String course, String size, int quantity, double price) {
        this.code = code;
        this.name = name;
        this.course = course;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
    }
    
    public int getCode() { return code; }
    public String getName() { return name; }
    public String getCourse() { return course; }
    public String getSize() { return size; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public void addQuantity(int amount) {
        this.quantity += amount;
    }
    
    @Override
    public String toString() {
        return String.format("%-1d | %-40s  | %-35s  | %-20s  | %-9d  | ₱%9.2f",
            code, name, course, size, quantity, price);
    }
}