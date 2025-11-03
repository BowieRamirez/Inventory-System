package audit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * StockAuditLog - Records every stock change for complete traceability
 * Tracks: WHO changed WHAT, WHEN, WHY, and the before/after quantities
 */
public class StockAuditLog implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String logId;
    private final String staffUsername; // WHO made the change
    private final String itemName; // WHAT item was changed
    private final int itemCode;
    private final String itemSize;
    private final int quantityBefore; // Stock level BEFORE change
    private final int quantityAfter; // Stock level AFTER change
    private final int quantityChanged; // Difference (can be negative for reductions)
    private final String reason; // WHY the change was made (restock, return, correction, damage, inventory_count, etc.)
    private final String changeType; // ADD, REMOVE, ADJUST, RETURN
    private String status; // PENDING, APPROVED, REJECTED, EXECUTED
    private final LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private String notes; // Additional notes/justification
    
    // Constructor for creating new audit log entry
    public StockAuditLog(String staffUsername, String itemName, int itemCode, String itemSize,
                         int quantityBefore, int quantityAfter, String reason, String changeType) {
        this.logId = "LOG-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
        this.staffUsername = staffUsername;
        this.itemName = itemName;
        this.itemCode = itemCode;
        this.itemSize = itemSize;
        this.quantityBefore = quantityBefore;
        this.quantityAfter = quantityAfter;
        this.quantityChanged = quantityAfter - quantityBefore;
        this.reason = reason;
        this.changeType = changeType;
        this.status = "PENDING"; // Starts as pending approval
        this.createdAt = LocalDateTime.now();
    }
    
    // Approve the audit log entry
    public void approve(String approverUsername) {
        this.status = "APPROVED";
        this.approvedBy = approverUsername;
        this.approvedAt = LocalDateTime.now();
    }
    
    // Reject the audit log entry
    public void reject() {
        this.status = "REJECTED";
    }
    
    // Mark as executed (change has been applied)
    public void execute() {
        this.status = "EXECUTED";
    }
    
    // Getters
    public String getLogId() { return logId; }
    public String getStaffUsername() { return staffUsername; }
    public String getItemName() { return itemName; }
    public int getItemCode() { return itemCode; }
    public String getItemSize() { return itemSize; }
    public int getQuantityBefore() { return quantityBefore; }
    public int getQuantityAfter() { return quantityAfter; }
    public int getQuantityChanged() { return quantityChanged; }
    public String getReason() { return reason; }
    public String getChangeType() { return changeType; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public String getApprovedBy() { return approvedBy; }
    public String getNotes() { return notes; }
    
    // Setters
    public void setNotes(String notes) { this.notes = notes; }
    
    /**
     * Format audit log for console output
     */
    @Override
    public String toString() {
        return String.format(
            "[%s] %s | Item: %s (%s) | %s: %d → %d (Δ%d) | Reason: %s | Staff: %s | Status: %s | Time: %s",
            logId,
            changeType,
            itemName,
            itemSize,
            "Qty",
            quantityBefore,
            quantityAfter,
            quantityChanged,
            reason,
            staffUsername,
            status,
            createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
    
    /**
     * Format as CSV for export/reporting
     */
    public String toCSV() {
        return String.format(
            "%s,%s,%s,%d,%s,%s,%d,%d,%d,%s,%s,%s,%s,%s",
            logId,
            createdAt,
            staffUsername,
            itemCode,
            itemName,
            itemSize,
            quantityBefore,
            quantityAfter,
            quantityChanged,
            changeType,
            reason,
            status,
            approvedBy != null ? approvedBy : "N/A",
            approvedAt != null ? approvedAt : "N/A"
        );
    }
}
