package audit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.SystemLogger;

/**
 * StockAuditManager - Manages audit trail for all stock changes
 * Handles: saving, loading, querying, and reporting on audit logs
 */
public class StockAuditManager {
    private List<StockAuditLog> auditLogs;
    private static final String AUDIT_FILE = "src/database/audit/stock_audit.dat";
    
    public StockAuditManager() {
        auditLogs = new ArrayList<>();
        loadAuditLogs();
    }
    
    /**
     * Create a new audit log entry for a stock change
     */
    public void logStockChange(String staffUsername, String itemName, int itemCode, String itemSize,
                              int quantityBefore, int quantityAfter, String reason, String changeType) {
        StockAuditLog log = new StockAuditLog(staffUsername, itemName, itemCode, itemSize,
                                             quantityBefore, quantityAfter, reason, changeType);
        auditLogs.add(log);
        saveAuditLogs();
        
        // Also log to system logger for real-time visibility
        SystemLogger.logActivity("📋 Stock Change Logged: " + log.getItemName() + 
                               " (" + log.getChangeType() + ") by Staff: " + staffUsername);
    }
    
    /**
     * Approve a pending stock change
     */
    public boolean approveChange(String logId, String adminUsername) {
        for (StockAuditLog log : auditLogs) {
            if (log.getLogId().equals(logId)) {
                log.approve(adminUsername);
                log.execute();
                saveAuditLogs();
                
                SystemLogger.logActivity("✅ Stock Change APPROVED: " + log.getItemName() + 
                                       " | Admin: " + adminUsername);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Reject a pending stock change
     */
    public boolean rejectChange(String logId, String adminUsername, String reason) {
        for (StockAuditLog log : auditLogs) {
            if (log.getLogId().equals(logId)) {
                log.reject();
                log.setNotes("Rejected by " + adminUsername + ": " + reason);
                saveAuditLogs();
                
                SystemLogger.logError("❌ Stock Change REJECTED: " + log.getItemName() + 
                                     " | Reason: " + reason, new Exception("Change rejected"));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all pending changes waiting for approval
     */
    public List<StockAuditLog> getPendingChanges() {
        List<StockAuditLog> pending = new ArrayList<>();
        for (StockAuditLog log : auditLogs) {
            if ("PENDING".equals(log.getStatus())) {
                pending.add(log);
            }
        }
        return pending;
    }
    
    /**
     * Get all approved changes
     */
    public List<StockAuditLog> getApprovedChanges() {
        List<StockAuditLog> approved = new ArrayList<>();
        for (StockAuditLog log : auditLogs) {
            if ("APPROVED".equals(log.getStatus())) {
                approved.add(log);
            }
        }
        return approved;
    }
    
    /**
     * Get changes by staff member
     */
    public List<StockAuditLog> getChangesByStaff(String staffUsername) {
        List<StockAuditLog> staffChanges = new ArrayList<>();
        for (StockAuditLog log : auditLogs) {
            if (staffUsername.equalsIgnoreCase(log.getStaffUsername())) {
                staffChanges.add(log);
            }
        }
        return staffChanges;
    }
    
    /**
     * Get changes by item
     */
    public List<StockAuditLog> getChangesByItem(int itemCode) {
        List<StockAuditLog> itemChanges = new ArrayList<>();
        for (StockAuditLog log : auditLogs) {
            if (log.getItemCode() == itemCode) {
                itemChanges.add(log);
            }
        }
        return itemChanges;
    }
    
    /**
     * Get changes by reason (restock, return, correction, etc.)
     */
    public List<StockAuditLog> getChangesByReason(String reason) {
        List<StockAuditLog> reasonChanges = new ArrayList<>();
        for (StockAuditLog log : auditLogs) {
            if (reason.equalsIgnoreCase(log.getReason())) {
                reasonChanges.add(log);
            }
        }
        return reasonChanges;
    }
    
    /**
     * Get all audit logs (full history)
     */
    public List<StockAuditLog> getAllLogs() {
        return new ArrayList<>(auditLogs);
    }
    
    /**
     * Save audit logs to file
     */
    private void saveAuditLogs() {
        try {
            // Ensure parent directory exists
            File file = new File(AUDIT_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(auditLogs);
            }
        } catch (IOException e) {
            SystemLogger.logError("Failed to save audit logs", e);
        }
    }
    
    /**
     * Load audit logs from file
     */
    @SuppressWarnings("unchecked")
    private void loadAuditLogs() {
        File file = new File(AUDIT_FILE);
        if (!file.exists()) {
            auditLogs = new ArrayList<>();
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            auditLogs = (List<StockAuditLog>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            SystemLogger.logError("Failed to load audit logs", e);
            auditLogs = new ArrayList<>();
        }
    }
    
    /**
     * Export audit logs as CSV (for reporting/compliance)
     */
    public void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("LogID,CreatedAt,StaffUsername,ItemCode,ItemName,ItemSize,QtyBefore,QtyAfter,QtyChanged,ChangeType,Reason,Status,ApprovedBy,ApprovedAt");
            
            // Write all logs
            for (StockAuditLog log : auditLogs) {
                writer.println(log.toCSV());
            }
            
            SystemLogger.logActivity("✅ Audit logs exported to: " + filename);
        } catch (IOException e) {
            SystemLogger.logError("Failed to export audit logs to CSV", e);
        }
    }
    
    /**
     * Generate a summary report of all changes
     */
    public void printAuditSummary() {
        System.out.println("\n" + "=".repeat(120));
        System.out.println("📊 STOCK AUDIT TRAIL SUMMARY");
        System.out.println("=".repeat(120));
        
        if (auditLogs.isEmpty()) {
            System.out.println("No audit logs found.");
            return;
        }
        
        // Summary statistics
        int totalChanges = auditLogs.size();
        int pending = (int) auditLogs.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        int approved = (int) auditLogs.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        int rejected = (int) auditLogs.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();
        int executed = (int) auditLogs.stream().filter(l -> "EXECUTED".equals(l.getStatus())).count();
        
        System.out.println("Total Changes: " + totalChanges + " | Pending: " + pending + " | Approved: " + approved + 
                          " | Rejected: " + rejected + " | Executed: " + executed);
        System.out.println("=".repeat(120));
        
        // List all changes
        for (StockAuditLog log : auditLogs) {
            System.out.println(log);
        }
        
        System.out.println("=".repeat(120) + "\n");
    }
}
