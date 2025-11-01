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
    private String bundleId;         // Identifier for bundle purchases (null for single items)
    
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
        this.bundleId = null; // Default to null for single items
    }
    
    // Constructor with bundleId
    public Receipt(int receiptId, String dateOrdered, String paymentStatus, 
                   int quantity, double amount, int itemCode, String itemName, String size, String buyerName, String bundleId) {
        this(receiptId, dateOrdered, paymentStatus, quantity, amount, itemCode, itemName, size, buyerName);
        this.bundleId = bundleId;
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
    public String getBundleId() { return bundleId; }
    
    public boolean isPartOfBundle() { return bundleId != null && !bundleId.isEmpty(); }
    
    // Setters
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    // Get current date/time formatted
    public static String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
    
    // Convert to string for file storage (pipe-delimited)
    public String toFileFormat() {
        String bundleIdStr = (bundleId != null) ? bundleId : "";
        return receiptId + "|" + dateOrdered + "|" + paymentStatus + "|" + 
               quantity + "|" + amount + "|" + itemCode + "|" + itemName + "|" + size + "|" + buyerName + "|" + bundleIdStr;
    }
    
    // Create Receipt from file format
    public static Receipt fromFileFormat(String line) {
        String[] parts = line.split("\\|", -1); // -1 to keep empty trailing strings
        if (parts.length < 9) return null;
        
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
            String bundleId = (parts.length > 9 && !parts[9].isEmpty()) ? parts[9] : null;
            
            return new Receipt(receiptId, dateOrdered, paymentStatus, quantity, amount, itemCode, itemName, size, buyerName, bundleId);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String toString() {
        String bundleInfo = isPartOfBundle() ? " [BUNDLE: " + bundleId + "]" : "";
        return String.format("%-10d | %-19s | %-25s | %-3d | ₱%-9.2f | %-6d | %-30s | %-10s | %-25s%s",
            receiptId, dateOrdered, paymentStatus, quantity, amount, itemCode, itemName, size, buyerName, bundleInfo);
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
        if (isPartOfBundle()) {
            sb.append(String.format("║  Bundle ID: %-55s ║\n", bundleId));
        }
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
        if (isPartOfBundle()) {
            sb.append("║  ** PART OF BUNDLE PURCHASE **                                     ║\n");
        }
        sb.append("╚════════════════════════════════════════════════════════════════════╝\n");
        
        return sb.toString();
    }
}
