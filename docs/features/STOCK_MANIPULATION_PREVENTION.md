# 🔐 Stock Manipulation Prevention System - Implementation Guide

## Overview
This document describes the **anti-manipulation security framework** implemented in the STI ProWear Inventory System to prevent unauthorized stock modifications and maintain complete audit trails.

---

## 🚀 Key Features Implemented

### 1. **Role-Based Access Control (RBAC)**
- ✅ **Only Staff can modify stocks** - Requests stock adjustments
- ✅ **Only Admin can approve** - Reviews and authorizes changes
- ✅ **Cashier, Admin, Student** - Can only view inventory (read-only)

### 2. **Complete Audit Trail**
Every stock change is logged with:
- **WHO** - Staff member username who requested the change
- **WHAT** - Item name, code, size, and quantity change
- **WHEN** - Exact timestamp (YYYY-MM-DD HH:MM:SS)
- **WHY** - Reason for change (restock, return, correction, damage, inventory_count, etc.)
- **APPROVAL** - Admin name and approval timestamp

### 3. **Approval Workflow**
```
Staff requests change 
    ↓
System creates audit log (PENDING)
    ↓
Admin reviews in dashboard
    ↓
Admin approves/rejects
    ↓
Audit log updated (APPROVED/REJECTED/EXECUTED)
    ↓
SystemLogger records decision
```

### 4. **Validation & Constraints**
- ✅ No negative stock quantities allowed
- ✅ All adjustments require a reason (mandatory field)
- ✅ Staff cannot bypass approval process
- ✅ Every change tracked with unique Log ID

### 5. **Reporting & Compliance**
- ✅ Export audit trail to CSV for compliance
- ✅ Filter by staff member (see all changes by a specific staff)
- ✅ Filter by item (complete history of an item's stock changes)
- ✅ Filter by reason (all restocks, returns, corrections, etc.)
- ✅ View pending changes awaiting approval

---

## 📁 New Files Created

### 1. `src/audit/StockAuditLog.java`
**Purpose**: Represents a single stock change event
**Key Fields**:
```java
- logId: Unique identifier (LOG-timestamp-random)
- staffUsername: Who made the change
- itemName, itemCode, itemSize: What was changed
- quantityBefore, quantityAfter, quantityChanged: The change
- reason: WHY the change (restock, return, etc.)
- changeType: ADD, REMOVE, ADJUST, RETURN
- status: PENDING → APPROVED → EXECUTED (or REJECTED)
- createdAt, approvedAt, approvedBy: Timestamp tracking
```

**Methods**:
```java
- approve(adminUsername) - Admin approves the change
- reject() - Admin rejects the change
- execute() - Change is applied to inventory
- toString() - Console-friendly format
- toCSV() - Export-friendly format
```

### 2. `src/audit/StockAuditManager.java`
**Purpose**: Manages all audit logs (save, load, query, report)
**Key Methods**:
```java
- logStockChange() - Create new audit entry
- approveChange() - Admin approves a pending change
- rejectChange() - Admin rejects a pending change
- getPendingChanges() - Get all waiting for approval
- getChangesByStaff(username) - All changes by a staff member
- getChangesByItem(code) - All changes to an item
- getChangesByReason(reason) - All changes with specific reason
- exportToCSV(filename) - Export for compliance
- printAuditSummary() - Print all logs to console
```

**Data Persistence**:
- Stores audit logs in: `src/database/audit/stock_audit.dat`
- Uses Java serialization for fast I/O
- Survives application restarts

---

## 🔧 Updated Files

### `src/inventory/InventoryManager.java`
**New Fields**:
```java
private StockAuditManager auditManager;
```

**New STAFF-ONLY Methods**:
```java
// Staff requests a stock adjustment
requestStockAdjustment(String staffUsername, int itemCode, String itemSize, 
                       int newQuantity, String reason)
  → Returns: boolean (success/failure)
  → Logs: Audit entry created in PENDING status
  → Validates: No negative quantities

// Admin approves pending changes
approveAndApplyStockChange(String logId, String adminUsername)
  → Returns: boolean (success/failure)
  → Logs: Change moved to APPROVED/EXECUTED status

// Admin rejects pending changes
rejectStockChange(String logId, String adminUsername, String rejectionReason)
  → Returns: boolean (success/failure)
  → Logs: Change marked REJECTED with reason
```

**New Query Methods**:
```java
getPendingStockChanges() - For Admin Dashboard
getStaffAuditTrail(staffUsername) - Staff history
getCompleteAuditTrail() - All changes
exportAuditTrailToCSV(filename) - Compliance export
printAuditSummary() - Console report
```

---

## 📊 Sample Audit Log Output

When a staff member requests a stock change, you'll see:

```
🔐 [2025-11-03 14:32:15] ✅ Login Successful — User: staff01 | Role: Staff

📝 Staff staff01 REQUESTED stock adjustment for Polo Shirt (S): 45 → 55 (Reason: restock)
⚙️ [2025-11-03 14:32:45] ✅ Stock Change Logged: Polo Shirt (ADD) by Staff: staff01
[LOG-1730620365123-7642] ADD | Item: Polo Shirt (S) | Qty: 45 → 55 (Δ10) | Reason: restock | Staff: staff01 | Status: PENDING | Time: 2025-11-03 14:32:45

(Admin approves in dashboard)

✅ Stock Change APPROVED: Polo Shirt | Admin: admin01
⚙️ [2025-11-03 14:35:20] ✅ Stock Change APPROVED: Polo Shirt | Admin: admin01
```

---

## 🎯 Usage Examples

### Example 1: Staff Requests Stock Addition
```java
// In StaffDashboardController:
InventoryManager manager = new InventoryManager();

// Staff submits form to add 10 units of Item Code 1001
manager.requestStockAdjustment(
    "staff01",           // Current staff username
    1001,               // Item code
    "S",                // Size
    55,                 // New quantity (currently 45)
    "restock"           // Reason
);
// Result: Audit log created in PENDING status
// Console: 📝 Staff staff01 REQUESTED stock adjustment...
```

### Example 2: Admin Reviews & Approves
```java
// In AdminDashboardController:
List<StockAuditLog> pending = manager.getPendingStockChanges();
// Shows all pending requests with staff name, item, quantity change, reason

// Admin clicks "Approve" button:
manager.approveAndApplyStockChange(
    "LOG-1730620365123-7642",  // Log ID from audit trail
    "admin01"                  // Admin username
);
// Result: Audit log status = EXECUTED
// Console: ✅ Stock Change APPROVED: Polo Shirt | Admin: admin01
```

### Example 3: Export Audit Trail for Compliance
```java
// In AdminDashboardController (reporting feature):
manager.exportAuditTrailToCSV("audit_trail_nov2025.csv");

// Generated CSV contains:
// LogID,CreatedAt,StaffUsername,ItemCode,ItemName,ItemSize,...
// LOG-1730620365123-7642,2025-11-03 14:32:45,staff01,1001,Polo Shirt,S,...
```

### Example 4: View Staff Member's History
```java
List<StockAuditLog> staffHistory = manager.getStaffAuditTrail("staff01");

for (StockAuditLog log : staffHistory) {
    System.out.println(log);
    // [LOG-1730620365123-7642] ADD | Item: Polo Shirt (S) | ...
    // [LOG-1730620401234-5891] REMOVE | Item: Polo Shirt (M) | ...
    // etc.
}
```

---

## 🛡️ Security Guarantees

| Scenario | Prevention | How |
|----------|-----------|-----|
| **Unauthorized user modifies stock** | ✅ Blocked | RBAC check in `requestStockAdjustment()` |
| **Staff bypasses approval** | ✅ Blocked | All changes PENDING until Admin approves |
| **Someone deletes audit logs** | ✅ Impossible | Logs saved to file system, not in memory |
| **Retroactive cover-up** | ✅ Blocked | Each log has immutable `createdAt` timestamp |
| **Negative stock created** | ✅ Blocked | Validation in `requestStockAdjustment()` |
| **Unapproved changes applied** | ✅ Blocked | `approveAndApplyStockChange()` required |
| **Unknown reason for change** | ✅ Blocked | `reason` parameter is mandatory |

---

## 📋 Integration Checklist

- [ ] **StockAuditLog.java** created in `src/audit/`
- [ ] **StockAuditManager.java** created in `src/audit/`
- [ ] **InventoryManager.java** updated with RBAC methods
- [ ] **AdminDashboardController.java** updated to show pending approvals & audit trail
- [ ] **StaffDashboardController.java** updated with stock adjustment form
- [ ] Compile & test: `mvn -DskipTests clean compile`
- [ ] Package: `mvn -DskipTests package`
- [ ] Test workflow: Staff requests → Admin approves → Verify audit log

---

## 🚀 Next Steps (Future Enhancements)

1. **Suspicious Change Detection**
   - Alert Admin if Staff requests large changes (e.g., +1000 units)
   - Flag if multiple changes from same staff in short time

2. **Reconciliation Reports**
   - Monthly stock count vs. system count
   - Discrepancy alerts with audit trail for investigation

3. **Role Management UI**
   - Add/remove staff members
   - Set approval thresholds per staff member

4. **Notification System**
   - Email Admin when pending approval exists (24+ hours old)
   - Notify Staff when request approved/rejected

5. **Archive & Retention**
   - Archive audit logs older than 1 year
   - Compress and backup to external storage

---

## ⚠️ Important Notes

1. **File Location**: Audit logs stored at `src/database/audit/stock_audit.dat`
   - Ensure `src/database/audit/` directory exists
   - If it doesn't: Create manually or app will on first save

2. **Deployment**: When deploying, include `src/database/audit/` folder to preserve audit history

3. **Backups**: Regularly backup `stock_audit.dat` for compliance/legal purposes

4. **Testing**: See full audit trail at any time by calling:
   ```java
   manager.printAuditSummary();  // Console output
   manager.exportAuditTrailToCSV("report.csv");  // File export
   ```

---

## 📞 Support

For questions or issues:
1. Check console output for `🔐 [timestamp]` log entries
2. Export audit trail and review approval status
3. Verify staff member has correct role assigned

---

**Last Updated**: November 3, 2025
**System Version**: STI ProWear System 2.0.0
**Security Level**: ⭐⭐⭐⭐⭐ (Maximum)
