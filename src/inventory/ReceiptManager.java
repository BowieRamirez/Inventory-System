package inventory;

import java.io.*;
import java.util.*;

public class ReceiptManager {
    private Map<Integer, Receipt> receipts; // HashMap for efficient lookup
    private int nextReceiptId;
    private static final String RECEIPTS_FILE = getReceiptsFilePath();
    
    private static String getReceiptsFilePath() {
        // Try src/database/data first (when running from project root)
        File file = new File("src/database/data/receipts.txt");
        if (file.getParentFile().exists()) {
            return "src/database/data/receipts.txt";
        }
        // Otherwise use database/data (when running from src directory)
        return "database/data/receipts.txt";
    }
    
    public ReceiptManager() {
        this.receipts = new HashMap<>();
        this.nextReceiptId = 10000000; // Start at 10000000 as per specification
        loadReceipts();
    }
    
    // Load receipts from file
    private void loadReceipts() {
        File file = new File(RECEIPTS_FILE);
        System.out.println("DEBUG: Loading receipts from: " + file.getAbsolutePath());
        if (!file.exists()) {
            System.out.println("No existing receipts database. Starting fresh.");
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines
                Receipt receipt = Receipt.fromFileFormat(line);
                if (receipt != null) {
                    receipts.put(receipt.getReceiptId(), receipt);
                    count++;
                    // Update nextReceiptId to be one more than the highest ID
                    if (receipt.getReceiptId() >= nextReceiptId) {
                        nextReceiptId = receipt.getReceiptId() + 1;
                    }
                }
            }
            System.out.println("Loaded " + count + " receipts from database.");
        } catch (IOException e) {
            System.out.println("Error loading receipts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Save all receipts to file
    private void saveReceipts() {
        try {
            // Create directories if they don't exist
            File file = new File(RECEIPTS_FILE);
            file.getParentFile().mkdirs();
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Receipt receipt : receipts.values()) {
                    writer.write(receipt.toFileFormat());
                    writer.newLine();
                }
                writer.flush(); // Ensure data is written to disk
            }
            System.out.println("DEBUG: Receipts saved successfully to " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error saving receipts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Create a new receipt
    public Receipt createReceipt(String paymentStatus, int quantity, double amount, 
                                 int itemCode, String itemName, String size, String buyerName) {
        int receiptId = nextReceiptId++;
        String dateOrdered = Receipt.getCurrentDateTime();
        Receipt receipt = new Receipt(receiptId, dateOrdered, paymentStatus, 
                                      quantity, amount, itemCode, itemName, size, buyerName);
        receipts.put(receiptId, receipt);
        System.out.println("DEBUG: Creating receipt ID " + receiptId + " for " + buyerName);
        saveReceipts();
        System.out.println("DEBUG: Receipt " + receiptId + " saved. Total receipts in memory: " + receipts.size());
        return receipt;
    }
    
    // Find receipt by ID
    public Receipt findReceiptById(int receiptId) {
        return receipts.get(receiptId);
    }
    
    // Get all receipts for a buyer
    public List<Receipt> getReceiptsByBuyer(String buyerName) {
        List<Receipt> buyerReceipts = new ArrayList<>();
        for (Receipt receipt : receipts.values()) {
            if (receipt.getBuyerName().equalsIgnoreCase(buyerName)) {
                buyerReceipts.add(receipt);
            }
        }
        // Sort by receipt ID (newest first)
        buyerReceipts.sort((r1, r2) -> Integer.compare(r2.getReceiptId(), r1.getReceiptId()));
        return buyerReceipts;
    }
    
    // Get all receipts
    public List<Receipt> getAllReceipts() {
        List<Receipt> allReceipts = new ArrayList<>(receipts.values());
        allReceipts.sort((r1, r2) -> Integer.compare(r2.getReceiptId(), r1.getReceiptId()));
        return allReceipts;
    }
    
    // Update payment status
    public boolean updatePaymentStatus(int receiptId, String newStatus) {
        Receipt receipt = receipts.get(receiptId);
        if (receipt != null) {
            receipt.setPaymentStatus(newStatus);
            saveReceipts();
            return true;
        }
        return false;
    }
    
    // Find receipt by item code and buyer name (returns the most recent one)
    public Receipt findReceiptByItemAndBuyer(int itemCode, String buyerName) {
        Receipt mostRecent = null;
        for (Receipt receipt : receipts.values()) {
            if (receipt.getItemCode() == itemCode && 
                receipt.getBuyerName().equalsIgnoreCase(buyerName)) {
                if (mostRecent == null || receipt.getReceiptId() > mostRecent.getReceiptId()) {
                    mostRecent = receipt;
                }
            }
        }
        return mostRecent;
    }
    
    // Find receipt with "Waiting for Approval" or "APPROVED - READY FOR PICKUP" status by item code and buyer
    public Receipt findPendingReceiptByItemAndBuyer(int itemCode, String buyerName) {
        for (Receipt receipt : receipts.values()) {
            if (receipt.getItemCode() == itemCode && 
                receipt.getBuyerName().equalsIgnoreCase(buyerName) &&
                (receipt.getPaymentStatus().equals("Waiting for Approval") ||
                 receipt.getPaymentStatus().equals("APPROVED - READY FOR PICKUP"))) {
                return receipt;
            }
        }
        return null;
    }
    
    // Display all receipts
    public void displayAllReceipts() {
        if (receipts.isEmpty()) {
            System.out.println("\nNo receipts in the database.");
            return;
        }
        
        System.out.println("\n=== ALL RECEIPTS ===");
        System.out.println("Receipt ID | Date Ordered        | Payment Status            | Qty | Amount    | Item Code | Buyer Name");
        System.out.println("-".repeat(130));
        
        List<Receipt> sortedReceipts = getAllReceipts();
        for (Receipt receipt : sortedReceipts) {
            System.out.println(receipt);
        }
        System.out.println("Total receipts: " + receipts.size());
    }
    
    // Display receipts by buyer
    public void displayReceiptsByBuyer(String buyerName) {
        List<Receipt> buyerReceipts = getReceiptsByBuyer(buyerName);
        
        if (buyerReceipts.isEmpty()) {
            System.out.println("\nNo receipts found for: " + buyerName);
            return;
        }
        
        System.out.println("\n=== RECEIPTS FOR: " + buyerName + " ===");
        System.out.println("Receipt ID | Date Ordered        | Payment Status            | Qty | Amount    | Item Code");
        System.out.println("-".repeat(110));
        
        for (Receipt receipt : buyerReceipts) {
            System.out.println(receipt);
        }
        System.out.println("Total receipts: " + buyerReceipts.size());
    }
    
    // Get receipt count
    public int getReceiptCount() {
        return receipts.size();
    }
}
