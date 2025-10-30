# STI ProWear System - Codebase Analysis

## 📊 System Overview

**STI ProWear System** is a comprehensive inventory management system for school merchandise (uniforms, apparel) with multi-role access control, reservation workflow, and payment processing.

**Current State**: Console-based application  
**Target State**: Modern GUI application with JavaFX + AtlantaFX

---

## 🏗️ Architecture Analysis

### **1. Entry Point**
- **File**: `src/main/MerchSystem.java`
- **Purpose**: Main application entry, authentication orchestration
- **Key Features**:
  - Unified login screen (supports Student ID or role keywords: admin/staff/cashier)
  - Student signup flow with terms & conditions
  - Role-based navigation to appropriate interfaces
  - Data loading on startup (students, inventory, reservations)

### **2. User Roles & Interfaces**

#### **Admin** (`admin/Admin.java`, `admin/AdminInterface.java`)
- **Credentials**: username="admin", password="admin123"
- **Capabilities**:
  - Full inventory management (CRUD operations)
  - Reservation approval/rejection
  - Student account management (activate/deactivate, password reset)
  - View all receipts and transactions
  - Process returns and refunds
  - Stock adjustment and reporting

#### **Staff** (`admin/Staff.java`, `admin/StaffInterface.java`)
- **Credentials**: username="staff", password="staff123"
- **Capabilities**:
  - Inventory management (add/edit items, adjust stock)
  - Reservation approval
  - View receipts
  - Process returns
  - Limited compared to Admin (no account management)

#### **Cashier** (`admin/Cashier.java`, `admin/CashierInterface.java`)
- **Credentials**: username="cashier", password="cashier123"
- **Capabilities**:
  - Process payments for approved reservations
  - Generate receipts
  - View all reservations
  - Payment methods: CASH, GCASH, CARD, BANK

#### **Student** (`student/Student.java`, `student/StudentInterface.java`)
- **Authentication**: Student ID (10-12 digits) + password
- **Capabilities**:
  - Browse items by course (own course + STI Special)
  - Reserve items with size and quantity selection
  - View reservation status
  - Cancel pending/unpaid reservations
  - View payment status
  - Pickup paid items
  - Return items within 10 days (with reason)
  - View receipts

### **3. Business Logic Layer**

#### **InventoryManager** (`inventory/InventoryManager.java`)
**Responsibilities**:
- Manage item collection (List + HashMap for O(1) lookup)
- CRUD operations for items
- Stock reservation (temporary hold)
- Stock deduction on approval
- Search by code, filter by course
- Display items with formatting

**Key Methods**:
- `addItem(Item)` - Add new item and save to file
- `loadItem(Item)` - Load item without saving (initialization)
- `findItemByCode(int)` - O(1) lookup using HashMap
- `reserveItem(code, size, qty)` - Temporary stock hold
- `deductStockOnApproval(code, size, qty)` - Permanent deduction
- `displayItemsByCourse(String)` - Filter and display

#### **ReservationManager** (`inventory/ReservationManager.java`)
**Responsibilities**:
- Manage reservation lifecycle
- Generate unique reservation IDs (starts at 5001)
- Approval workflow
- Payment tracking
- Return eligibility checking (10-day window)

**Key Methods**:
- `createReservation(...)` - Create new reservation, reserve stock
- `approveReservation(id, size)` - Approve and deduct stock
- `cancelReservation(id, reason)` - Cancel and restore stock
- `markAsPaid(id, paymentMethod)` - Process payment
- `getReservationsByStudent(studentId)` - Filter by student

**Reservation States**:
1. `PENDING` - Awaiting admin/staff approval
2. `APPROVED - WAITING FOR PAYMENT` - Approved, needs payment
3. `PAID - READY FOR PICKUP` - Paid, ready to collect
4. `COMPLETED` - Picked up by student
5. `RETURN REQUESTED` - Student requested return
6. `RETURNED - REFUNDED` - Return processed
7. `CANCELLED` - Cancelled by student or admin

#### **ReceiptManager** (`inventory/ReceiptManager.java`)
**Responsibilities**:
- Generate unique receipt IDs (starts at 10000000)
- Store and retrieve receipts
- Update payment status
- Filter receipts by buyer

**Key Methods**:
- `createReceipt(...)` - Generate new receipt
- `findReceiptById(int)` - Lookup receipt
- `getReceiptsByBuyer(String)` - Filter by buyer name
- `updatePaymentStatus(id, status)` - Update status

### **4. Data Models**

#### **Item** (`inventory/Item.java`)
```java
- int code          // Unique item code (1001-9999)
- String name       // Item name
- String course     // Course code or "STI Special"
- String size       // XS, S, M, L, XL, XXL, One Size
- int quantity      // Available stock
- double price      // Price in PHP
```

#### **Reservation** (`inventory/Reservation.java`)
```java
- int reservationId           // Unique ID (5001+)
- String studentName          // Full name
- String studentId            // Student ID
- String course               // Student's course
- int itemCode                // Item code
- String itemName             // Item name
- String size                 // Selected size
- int quantity                // Quantity ordered
- double totalPrice           // Total amount
- LocalDateTime reservationTime
- LocalDateTime completedDate // For return eligibility
- String status               // Reservation state
- boolean isPaid              // Payment flag
- String paymentMethod        // CASH, GCASH, CARD, BANK
```

**Special Features**:
- `isEligibleForReturn()` - Checks if within 10-day window
- `getDaysUntilReturnExpires()` - Calculates remaining days

#### **Receipt** (`inventory/Receipt.java`)
```java
- int receiptId         // Unique ID (10000000+)
- String dateOrdered    // Timestamp
- String paymentStatus  // PENDING, COMPLETED
- int quantity
- double amount
- int itemCode
- String itemName
- String size
- String buyerName
```

#### **Student** (`student/Student.java`)
```java
- String studentId      // 10-12 digit ID
- String password       // Plain text (consider hashing)
- String course         // Course code
- String firstName
- String lastName
- String gender         // Male/Female
- boolean isActive      // Account status
```

### **5. Utilities**

#### **FileStorage** (`utils/FileStorage.java`)
**Purpose**: File-based persistence (CSV format)

**Files**:
- `src/database/data/items.txt` - Inventory items
- `src/database/data/students.txt` - Student accounts
- `src/database/data/reservations.txt` - Reservations
- `src/database/data/receipts.txt` - Receipts

**Key Methods**:
- `loadItems()` - Parse items.txt
- `saveItems(List<Item>)` - Write items.txt
- `loadStudents()` - Parse students.txt
- `addStudent(List, Student)` - Append student
- `loadReservations()` - Parse reservations.txt
- `saveReservations(List)` - Write reservations.txt

**Format**: Comma-separated (items) or pipe-separated (reservations, receipts)

#### **InputValidator** (`utils/InputValidator.java`)
**Purpose**: Console input validation

**Key Methods**:
- `getValidInteger(prompt, min, max)` - Validate integer range
- `getValidPrice(prompt)` - Validate price (0-10000)
- `getValidCourse(prompt)` - Validate course code
- `getValidSize(prompt)` - Validate size (XS-XXL, One Size)
- `getValidStudentId(prompt)` - Validate 10-12 digit ID
- `getValidYesNo(prompt)` - Yes/No confirmation

**Supported Courses**:
- **SHS**: ABM, STEM, HUMSS, TVL-ICT, TVL-TO, TVL-CA
- **Tertiary**: BSCS, BSIT, BSCpE, BSBA, BSA, BSHM, BMMA, BSTM

#### **TermsAndConditions** (`utils/TermsAndConditions.java`)
**Purpose**: Display and accept T&C during signup

---

## 🔄 Key Workflows

### **1. Student Reservation Flow**
```
1. Student logs in with Student ID + password
2. Browse items (own course + STI Special)
3. Select item, choose size, enter quantity
4. Confirm reservation → Status: PENDING
5. Admin/Staff approves → Status: APPROVED - WAITING FOR PAYMENT
6. Student goes to Cashier
7. Cashier processes payment → Status: PAID - READY FOR PICKUP
8. Student confirms pickup → Status: COMPLETED
9. (Optional) Student returns within 10 days → Status: RETURN REQUESTED
10. Admin/Staff processes return → Status: RETURNED - REFUNDED
```

### **2. Inventory Management Flow**
```
1. Admin/Staff logs in
2. Navigate to Stock Management
3. Add/Edit/Delete items
4. Adjust stock quantities
5. Changes saved to items.txt immediately
```

### **3. Payment Processing Flow**
```
1. Cashier logs in
2. Student provides Reservation ID
3. Cashier searches reservation
4. Verifies details (student, item, amount)
5. Collects payment (CASH only in current implementation)
6. System generates receipt
7. Reservation status → PAID - READY FOR PICKUP
```

---

## 🎯 GUI Transition Strategy

### **What to Keep (Reuse)**
✅ All business logic (InventoryManager, ReservationManager, ReceiptManager)  
✅ All data models (Item, Reservation, Receipt, Student)  
✅ FileStorage for persistence  
✅ Authentication logic (Admin, Staff, Cashier, Student classes)  
✅ Data validation rules  

### **What to Replace**
❌ Console I/O (Scanner, System.out.println)  
❌ InputValidator (Scanner-based) → GUIValidator  
❌ Menu loops (while + switch) → Event-driven GUI  
❌ Text-based tables → JavaFX TableView  
❌ Console prompts → JavaFX dialogs and forms  

### **What to Add (New)**
➕ JavaFX views (LoginView, AdminDashboard, StudentDashboard, etc.)  
➕ Controllers (LoginController, AdminController, etc.)  
➕ GUI utilities (SceneManager, AlertHelper, ThemeManager)  
➕ Reusable components (ItemCard, ItemTable, ReservationCard)  
➕ AtlantaFX theme integration  

---

## 📈 Code Quality Observations

### **Strengths**
✅ Clear separation of concerns (managers, models, interfaces)  
✅ Consistent naming conventions  
✅ File-based persistence (simple, no DB dependency)  
✅ Comprehensive validation  
✅ Detailed user help guides  
✅ Return policy implementation (10-day window)  

### **Areas for Improvement**
⚠️ **Security**: Passwords stored in plain text (consider hashing)  
⚠️ **Error Handling**: Limited try-catch blocks  
⚠️ **Logging**: Uses System.out instead of proper logging framework  
⚠️ **Threading**: File I/O on main thread (will block GUI)  
⚠️ **Validation**: Duplicated validation logic across interfaces  
⚠️ **Testing**: No unit tests present  

---

## 🚀 Recommended Next Steps

1. **Review GUI_IMPLEMENTATION_PLAN.md** for detailed roadmap
2. **Setup Maven/Gradle** with JavaFX + AtlantaFX dependencies
3. **Create GUI package structure** (controllers, views, components, utils)
4. **Start with Phase 1**: Foundation (MainApp, SceneManager, AlertHelper)
5. **Build Phase 2**: Login screen (most critical for testing)
6. **Iterate through phases** 3-8 (dashboards, components, polish)
7. **Test thoroughly** before deployment

---

**Analysis Date**: 2025-10-30  
**Codebase Version**: Current  
**Analyzed By**: AI Assistant

