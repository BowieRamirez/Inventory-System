# 🖥️📜 CLI Logging Feature — STI ProWear System

## Overview

The **CLI Logging Feature** has been successfully implemented into the STI ProWear Inventory System (v2.0.0). This feature ensures **transparency and traceability** of all critical system actions by automatically printing formatted console logs in real-time, while the GUI handles the user interaction interface.

---

## 🎯 Feature Highlights

### ✅ What Gets Logged

1. **🔐 Authentication Events**
   - User login with role detection (Admin, Staff, Cashier, Student)
   - Failed login attempts with reasons
   - User logout

2. **🛒 Purchase Transactions**
   - Student purchases with item details, quantity, and total price
   - Payment method used (CASH, GCASH, CARD, BANK)

3. **📦 Inventory Stock Updates**
   - Automatic logging when stock is deducted after purchase
   - Shows item name, quantity sold, and remaining stock

4. **📊 Admin Stock Adjustments**
   - Stock additions or removals by admin
   - Shows adjustment amount, admin username, and new stock level
   - Supports multiple items simultaneously

5. **⚠️ System Errors & Warnings**
   - Failed operations logged with exception details
   - Easy debugging and audit trail

---

## 📋 Sample CLI Output Scenarios

### Scenario 1: Student Login
```
🔐 [2025-11-03 14:32:15] ✅ Login Successful — User: student001 | Role: Student
```

### Scenario 2: Failed Login
```
❌ [2025-11-03 14:33:42] 🔍 Authentication Failed — User: invaliduser | Reason: Invalid credentials
```

### Scenario 3: Cashier Processing Payment
```
🛒 [2025-11-03 14:35:20] ✅ Purchase Transaction — User: CS-2024-001 | Item: Polo Shirt (Large) | Qty: 2 | Total: ₱1,200.00
📦 [2025-11-03 14:35:20] Stock Updated: Polo Shirt | Sold: 2 | Remaining: 48
```

### Scenario 4: Admin Stock Adjustment (Adding Stock)
```
📊 [2025-11-03 14:40:05] Stock Adjustment: +10 units added to Polo Shirt (Large) by Admin: admin01 — Updated stock: 120
```

### Scenario 5: Admin Stock Adjustment (Removing Stock)
```
📊 [2025-11-03 14:42:30] Stock Adjustment: -5 units removed from PE Shirt (Small) by Admin: admin02 — Updated stock: 45
```

### Scenario 6: Multiple Items in a Bundle Purchase
```
🛒 [2025-11-03 14:45:10] ✅ Purchase Transaction — User: CS-2024-002 | Item: PE Shirt Bundle | Qty: 3 | Total: ₱2,850.00
📦 [2025-11-03 14:45:10] Stock Updated: PE Shirt | Sold: 3 | Remaining: 62
```

### Scenario 7: System Error
```
❌ [2025-11-03 14:50:33] 🔍 System Error: Payment processing failed for reservation: RES-12345 | Exception: Payment marking failed
```

---

## 🏗️ Implementation Details

### New Files Added

1. **`src/utils/SystemLogger.java`** — Centralized logging utility
   - Provides static methods for all log types
   - Formats logs with timestamps (yyyy-MM-dd HH:mm:ss)
   - Uses emojis for visual distinction and quick scanning
   - No external dependencies (uses Java's built-in `java.time`)

### Modified Files

1. **`src/gui/controllers/LoginController.java`**
   - Logs successful login with role
   - Logs authentication failures with reason

2. **`src/gui/controllers/CashierDashboardController.java`**
   - Logs purchase transactions
   - Logs stock updates when payment is processed
   - Logs payment processing errors

3. **`src/inventory/InventoryManager.java`**
   - Logs stock adjustments (additions/removals)
   - Logs when admin updates inventory quantities
   - Tracks old and new stock levels

---

## 🎨 Log Emoji Meanings

| Emoji | Meaning | When Used |
|-------|---------|-----------|
| 🔐 | Authentication/Security | User login events |
| 👋 | Logout | User logging out |
| 🛒 | Shopping/Purchase | Customer transactions |
| 📦 | Stock/Inventory | Stock updates |
| 📊 | Adjustment/Analytics | Admin adjustments |
| ❌ | Error | Failed operations |
| ✅ | Success | Confirmed operations |
| ⚠️ | Warning | Potential issues |
| 🔍 | Audit/Investigation | Audit trail entries |
| ⚙️ | Activity/System | General system activity |

---

## 🚀 How to Use

### Compilation & Build

The logging feature has been fully integrated and compiled. To rebuild:

```bash
cd /Users/karlfrias/Downloads/OOP/Inventory-System
mvn -DskipTests clean compile
```

### Running the Application

The GUI will run as usual, but now all critical actions are logged to the console:

```bash
mvn javafx:run
```

Or run the compiled JAR:
```bash
java -jar target/prowear-system-2.0.0.jar
```

**Console Output**: All logs will be displayed in the terminal where you ran the command.

### Capturing Logs to a File

To save all console output (including logs) to a file:

```bash
java -jar target/prowear-system-2.0.0.jar > system_logs.txt 2>&1 &
```

Then tail the file in real-time:
```bash
tail -f system_logs.txt
```

---

## 📊 Log Methods Available in `SystemLogger`

Public static methods:
- `logLogin(String username, String role)` — Log user login
- `logLogout(String username)` — Log user logout
- `logAuthenticationFailure(String username, String reason)` — Log auth failures
- `logPurchase(String username, String itemName, int quantity, double totalPrice)` — Log transactions
- `logStockUpdate(String itemName, int quantitySold, int remainingStock)` — Log inventory changes
- `logStockAdjustment(String adminUsername, String itemName, int adjustment, int newStock)` — Log admin adjustments
- `logReservation(String username, String itemName, int quantity)` — Log reservations
- `logReservationCancellation(String username, String itemName)` — Log cancellations
- `logError(String errorMessage, Exception exception)` — Log system errors
- `logActivity(String activity)` — Log general activities
- `logWarning(String warningMessage)` — Log warnings

---

## 🔍 Debugging & Audit Trail Benefits

1. **Real-time Visibility** — Immediately see all system actions as they happen
2. **Error Tracking** — Quickly identify and debug failed operations
3. **Accountability** — Every action is timestamped with user/admin identity
4. **Compliance** — Maintain detailed audit trail for inventory management
5. **Performance** — Monitor peak transaction times
6. **Security** — Track failed login attempts and suspicious activities
7. **Easy Integration** — Console logs can be piped to external logging systems (ELK, Splunk, etc.)

---

## 📌 Future Enhancements (Optional)

- Write logs to external file automatically
- Log rotation (daily/weekly backups)
- Integration with external logging frameworks (log4j, SLF4j)
- Database logging for long-term audit trail
- Log filtering and search capabilities
- Real-time log viewer in GUI

---

## ✨ Summary

The CLI Logging Feature is now **fully operational**. Every critical action (login, purchase, stock adjustment) is logged with:
- ✅ Timestamp (yyyy-MM-dd HH:mm:ss)
- ✅ User/Admin identity
- ✅ Action details (item, quantity, amount)
- ✅ Emoji indicators for quick visual scanning
- ✅ Error messages for failed operations

This ensures **transparency, traceability, and easy debugging** while the GUI continues to handle user interactions seamlessly! 🎉

---

**Build Status**: ✅ **SUCCESS**  
**Compiled**: 33 source files  
**JAR Location**: `/Users/karlfrias/Downloads/OOP/Inventory-System/target/prowear-system-2.0.0.jar`
