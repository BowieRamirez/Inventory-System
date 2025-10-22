package inventory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Receipt {
    private int receiptId;          // Unique receipt ID (starts at 10000000)
    private String dateOrdered;      // Date when order was placed
    private String paymentStatus;    // Completed, Approved, or Waiting for Payment
    private int quantity;
    private double amount;           // Total amount
    private int itemCode;
    private String itemName;         // Item name
    private String size;             // Item size
    private String buyerName;
    
    // Constructor for creating new receipt
    public Receipt(int receiptId, String dateOrdered, String paymentStatus, 
                   int quantity, double amount, int itemCode, String itemName, String size, String buyerName) {
        this.receiptId = receiptId;
        this.dateOrdered = dateOrdered;
        this.paymentStatus = paymentStatus;
        this.quantity = quantity;
        this.amount = amount;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.size = size;
        this.buyerName = buyerName;
    }
    
    // Getters
    public int getReceiptId() { return receiptId; }
    public String getDateOrdered() { return dateOrdered; }
    public String getPaymentStatus() { return paymentStatus; }
    public int getQuantity() { return quantity; }
    public double getAmount() { return amount; }
    public int getItemCode() { return itemCode; }
    public String getItemName() { return itemName; }
    public String getSize() { return size; }
    public String getBuyerName() { return buyerName; }
    
    // Setters
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setAmount(double amount) { this.amount = amount; }
    
    // Get current date/time formatted
    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
    
    // Convert to string for file storage (pipe-delimited)
    public String toFileFormat() {
        return receiptId + "|" + dateOrdered + "|" + paymentStatus + "|" + 
               quantity + "|" + amount + "|" + itemCode + "|" + itemName + "|" + size + "|" + buyerName;
    }
    
    // Create Receipt from file format
    public static Receipt fromFileFormat(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 9) return null;
        
        try {
            int receiptId = Integer.parseInt(parts[0]);
            String dateOrdered = parts[1];
            String paymentStatus = parts[2];
            int quantity = Integer.parseInt(parts[3]);
            double amount = Double.parseDouble(parts[4]);
            int itemCode = Integer.parseInt(parts[5]);
            String itemName = parts[6];
            String size = parts[7];
            String buyerName = parts[8];
            
            return new Receipt(receiptId, dateOrdered, paymentStatus, quantity, amount, itemCode, itemName, size, buyerName);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%-10d | %-19s | %-25s | %-3d | ₱%-9.2f | %-6d | %-30s | %-10s | %-25s",
            receiptId, dateOrdered, paymentStatus, quantity, amount, itemCode, itemName, size, buyerName);
    }
    
    // Detailed receipt format
    public String toDetailedFormat() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                        OFFICIAL RECEIPT                            ║\n");
        sb.append("║                      STI ProWear System                            ║\n");
        sb.append("╠════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Receipt ID: %-53d ║\n", receiptId));
        sb.append(String.format("║  Date: %-59s ║\n", dateOrdered));
        sb.append("╠════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Buyer: %-58s ║\n", buyerName));
        sb.append("╠════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Item Code: %-54d ║\n", itemCode));
        sb.append(String.format("║  Item Name: %-54s ║\n", itemName));
        sb.append(String.format("║  Size: %-59s ║\n", size));
        sb.append(String.format("║  Quantity: %-55d ║\n", quantity));
        sb.append(String.format("║  Amount: ₱%-54.2f ║\n", amount));
        sb.append("╠════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Payment Status: %-49s ║\n", paymentStatus));
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n");
        
        return sb.toString();
    }
}
