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
    
    // Getters
    public int getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCourse() {
        return course;
    }
    
    public String getSize() {
        return size;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getPrice() {
        return price;
    }
    
    // Setters
    public void setCode(int code) {
        this.code = code;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void reduceQuantity(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        }
    }
    
    public void addQuantity(int amount) {
        quantity += amount;
    }
    
    @Override
    public String toString() {
        return String.format("%-6d | %-25s | %-20s | %-8s | %-8d | ₱%.2f", 
                           code, name, course, size, quantity, price);
    }
}