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
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines
                Receipt receipt = Receipt.fromFileFormat(line);
                if (receipt != null) {
                    receipts.put(receipt.getReceiptId(), receipt);
                    // Update nextReceiptId to be one more than the highest ID
                    if (receipt.getReceiptId() >= nextReceiptId) {
                        nextReceiptId = receipt.getReceiptId() + 1;
                    }
                }
            }
        } catch (IOException e) {
            // Error loading receipts
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
        } catch (IOException e) {
            // Error saving receipts
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
        saveReceipts();
        return receipt;
    }
    
    // Create a new receipt with bundleId
    public Receipt createReceipt(String paymentStatus, int quantity, double amount,
                                 int itemCode, String itemName, String size, String buyerName, String bundleId) {
        int receiptId = nextReceiptId++;
        String dateOrdered = Receipt.getCurrentDateTime();
        Receipt receipt = new Receipt(receiptId, dateOrdered, paymentStatus,
                                      quantity, amount, itemCode, itemName, size, buyerName, bundleId);
        receipts.put(receiptId, receipt);
        saveReceipts();
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
    

    
    // Get receipt count
    public int getReceiptCount() {
        return receipts.size();
    }
}
