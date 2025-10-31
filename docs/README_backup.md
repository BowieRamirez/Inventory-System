# 🎓 MerchSystem - University Merchandise Inventory Management System

A comprehensive Java-based console application designed for managing university merchandise inventory with role-based access control, multi-stage reservation workflows, and complete stock tracking transparency.

## 📋 Table of Contents
- [Features](#features)
- [System Architecture](#system-architecture)
- [User Roles](#user-roles)
- [Workflows](#workflows)
- [Database Structure](#database-structure)
- [Installation](#installation)
- [Usage Guide](#usage-guide)
- [Technologies Used](#technologies-used)

---

## ✨ Features

### Core Functionality
- **Multi-Role Access Control** - Admin, Staff, Cashier, and Student roles with specific permissions
- **Multi-Stage Reservation Workflow** - Structured approval process from reservation to completion
- **Comprehensive Stock Logging** - Complete transparency of all inventory movements
- **Course-Based Inventory** - Items organized by courses (BSIT, BSCS, BSIS)
- **Return Management** - Student-initiated returns with reason tracking
- **Payment Processing** - Cash-only payment system with receipt generation
- **Real-Time Inventory Updates** - Automatic stock adjustments on transactions

### Advanced Features
- **Reservation Approval System** - Admin/Staff must approve before payment
- **Stock Return with Reasons** - 6 different return reason categories tracked
- **Student Return Requests** - Students can request returns with condition reports
- **Stock Movement Logs** - Audit trail for all inventory changes
- **Receipt Management** - Digital receipt generation and tracking
- **User Authentication** - Secure login system for all user roles

---

## 🏗️ System Architecture

```
MerchSystem/
├── src/
│   ├── admin/              # Admin, Staff, and Cashier interfaces
│   ├── database/data/      # Text-based database files
│   ├── inventory/          # Core business logic
│   ├── main/              # Application entry point
│   ├── student/           # Student interface
│   ├── user/              # User base class
│   └── utils/             # Helper utilities and validators
```

### Database Files
- `students.txt` - Student accounts and information
- `items.txt` - Merchandise inventory (CSV format)
- `reservations.txt` - Reservation records
- `receipts.txt` - Transaction receipts
- `stock_logs.txt` - Comprehensive stock movement logs

---

## 👥 User Roles

### 1. 🔑 Admin
**Default Credentials:** `admin` / `admin123`

**Capabilities:**
- ✅ Full inventory management (Add items, Return stock)
- ✅ Approve student reservations
- ✅ Process returns and refunds
- ✅ View all reservations and receipts
- ✅ View comprehensive stock logs
- ✅ Register new students
- ✅ Manage student accounts (activate/deactivate)

### 2. 👔 Staff
**Default Credentials:** `staff` / `staff123`

**Capabilities:**
- ✅ Stock management (Add items, Return stock)
- ✅ Approve student reservations
- ✅ Process returns and refunds
- ✅ View all reservations and receipts
- ✅ View comprehensive stock logs
- ⛔ Cannot register or manage students

### 3. 💰 Cashier
**Default Credentials:** `cashier` / `cashier123`

**Capabilities:**
- ✅ Process payments for approved reservations
- ✅ Generate receipts
- ✅ View payment-ready reservations
- ⛔ Cannot manage inventory
- ⛔ Cannot approve reservations

### 4. 🎓 Student
**Registration Required**

**Capabilities:**
- ✅ Browse inventory by course
- ✅ Create reservations
- ✅ Pick up paid items
- ✅ Request returns with reasons
- ✅ View personal reservation history
- ✅ View reservation status

---

## 🔄 Workflows

### Reservation Workflow (Multi-Stage Process)

```
┌─────────────┐
│   STUDENT   │ Creates reservation
└──────┬──────┘
       │
       ▼
┌─────────────────────────┐
│  PENDING                │ ← Waiting for approval
└──────┬──────────────────┘
       │
       │ Admin/Staff approves
       ▼
┌─────────────────────────┐
│  APPROVED - WAITING     │ ← Ready for payment
│  FOR PAYMENT            │
└──────┬──────────────────┘
       │
       │ Cashier processes payment
       │ Stock deducted
       ▼
┌─────────────────────────┐
│  PAID - READY FOR       │ ← Ready for pickup
│  PICKUP                 │
└──────┬──────────────────┘
       │
       │ Student picks up
       │ Logged to stock_logs.txt
       ▼
┌─────────────────────────┐
│  COMPLETED              │ ← Transaction complete
└─────────────────────────┘
```

### Return Workflow

```
┌─────────────┐
│  COMPLETED  │ Student requests return
└──────┬──────┘
       │
       │ Student selects reason:
       │ [1] Damaged Item
       │ [2] Wrong Size
       │ [3] Defective Item
       │ [4] Changed Mind
       ▼
┌─────────────────────────┐
│  RETURN REQUESTED       │ ← Waiting for admin approval
└──────┬──────────────────┘
       │
       │ Admin/Staff approves
       │ Stock restored
       │ Logged to stock_logs.txt
       ▼
┌─────────────────────────┐
│  RETURNED - REFUNDED    │ ← Return complete
└─────────────────────────┘
```

### Stock Management Workflow

#### Add New Item
1. Admin/Staff accesses Stock Management
2. Selects "Add New Item"
3. Enters item details (code, name, course, size, quantity, price)
4. Confirms addition
5. Item added to inventory

#### Return Stock (Course-Based)
1. Admin/Staff accesses Stock Management
2. Selects "Return Stock"
3. **Chooses course (BSIT/BSCS/BSIS)**
4. **Views table of all items for that course**
5. Enters item code from the displayed table
6. System validates item belongs to selected course
7. Specifies quantity to return
8. Selects return reason:
   - [1] Defective
   - [2] Damaged
   - [3] Expired/Outdated
   - [4] Overstock
   - [5] Wrong Item
   - [6] Other
9. Confirms stock return
10. Stock reduced and logged to `stock_logs.txt`

---

## 🗄️ Database Structure

### students.txt
```
Format: studentId|password|course|firstName|lastName|gender|isActive
Example: 2024001|pass123|BSIT|John|Doe|Male|true
```

### items.txt (CSV)
```
Format: ItemCode,ItemName,Course,Size,Quantity,Price
Example: 1001,IT/Eng Gray 3/4 Polo (Male),BSIT,S,47,450.00
```

### reservations.txt
```
Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason
Example: 5001|Doe, John|2024001|BSIT|1001|IT/Eng Gray 3/4 Polo (Male)|2|900.00|M|COMPLETED|true|CASH ONLY|2024-10-30 14:30:00|2024-10-30 15:00:00|
```

### receipts.txt
```
Format: receiptId|dateOrdered|paymentStatus|quantity|amount|itemCode|itemName|size|buyerName
Example: 10000001|2024-10-30 14:35:00|PAID - READY FOR PICKUP|2|900.00|1001|IT/Eng Gray 3/4 Polo (Male)|M|Doe, John
```

### stock_logs.txt (Transparency Logs)
```
Format: Timestamp|PerformedBy|Code|ItemName|Size|StockChange|Action|Details
Example: 2024-10-30 15:00:00|Doe, John|1001|IT/Eng Gray 3/4 Polo (Male)|M|-2|USER_PICKUP|Student completed pickup

Actions:
- STAFF_RETURN: Admin/Staff returned stock (Reason logged)
- USER_PICKUP: Student picked up items (Stock decreased)
- USER_RETURN: Student return approved (Stock restored with reason)
```

---

## 🚀 Installation

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Windows PowerShell or any terminal
- Git (for cloning)

### Steps

1. **Clone the repository**
```powershell
git clone https://github.com/BowieRamirez/Inventory-System.git
cd Inventory-System
```

2. **Compile the project**
```powershell
javac -d . src/**/*.java
```

3. **Run the application**
```powershell
java src\main\MerchSystem.java
```

---

## 📖 Usage Guide

### For Admins/Staff

#### Approving Reservations
1. Login with admin/staff credentials
2. Select "View Reservations" → "Approve Reservation"
3. View pending reservations with student details
4. Enter reservation ID to approve
5. Reservation status changes to "APPROVED - WAITING FOR PAYMENT"

#### Managing Stock Returns
1. Login with admin/staff credentials
2. Select "Stock Management" → "Return Stock"
3. **Select course (BSIT, BSCS, or BSIS)**
4. **Review displayed table of available items**
5. Enter item code from the table
6. Specify quantity to return
7. Select return reason from 6 options
8. Confirm return
9. Stock reduced and logged with reason

#### Processing Student Returns
1. Select "View Reservations" → "Process Returns/Refunds"
2. View return requests with student-provided reasons
3. Enter reservation ID to approve return
4. Stock restored automatically
5. Logged to stock_logs.txt with USER_RETURN action

### For Cashiers

#### Processing Payments
1. Login with cashier credentials
2. Select "Process Payment"
3. View all "APPROVED - WAITING FOR PAYMENT" reservations
4. Enter reservation ID to process
5. Payment method: CASH ONLY
6. Receipt generated automatically
7. Status changes to "PAID - READY FOR PICKUP"
8. Stock deducted from inventory

### For Students

#### Making a Reservation
1. Login or register as student
2. Select "Shop Items"
3. Browse items by your course
4. Enter item code and size
5. Specify quantity
6. Confirm reservation
7. Wait for admin/staff approval

#### Picking Up Items
1. Login and select "Pickup Item"
2. View "PAID - READY FOR PICKUP" reservations
3. Enter reservation ID to pickup
4. Confirm pickup
5. Status changes to "COMPLETED"
6. Pickup logged to stock_logs.txt

#### Requesting Returns
1. Select "Return Item"
2. View completed orders
3. Enter reservation ID to return
4. Select return reason:
   - [1] Damaged Item
   - [2] Wrong Size
   - [3] Defective Item
   - [4] Changed Mind
5. Submit return request
6. Wait for admin/staff approval

---

## 🔍 Key Features Explained

### 1. Multi-Stage Approval System
**Why?** Prevents unauthorized purchases and ensures inventory control.

**How it works:**
- Students create reservations (PENDING status)
- Admin/Staff review and approve (APPROVED - WAITING FOR PAYMENT)
- Cashier processes payment and deducts stock (PAID - READY FOR PICKUP)
- Student picks up item (COMPLETED)

### 2. Comprehensive Stock Logging
**Why?** Complete transparency and audit trail for all inventory movements.

**What's logged:**
- Staff stock returns with reasons
- Student pickups (stock decrease)
- Student returns when approved (stock increase)
- Timestamp, performer, item details, and action type

### 3. Course-Based Return Stock Selection
**Why?** Easier navigation and prevents errors when returning stock.

**How it works:**
- Admin/Staff first selects the course
- System displays formatted table of all items for that course
- Shows code, name, size availability with quantities, and price
- Admin selects item code from the table
- System validates item belongs to selected course
- Prevents wrong course selections

### 4. Return Reason Tracking
**Why?** Understanding why items are returned helps improve inventory decisions.

**Student Reasons:**
- Damaged Item
- Wrong Size
- Defective Item
- Changed Mind

**Staff Reasons:**
- Defective
- Damaged
- Expired/Outdated
- Overstock
- Wrong Item
- Other

---

## 💻 Technologies Used

- **Language:** Java (JDK 17+)
- **Database:** Text-based file storage (Pipe-delimited)
- **Architecture:** MVC-inspired structure
- **Authentication:** Simple credential-based system
- **Data Format:** CSV and Pipe-delimited text files
- **Interface:** Console-based UI

---

## 📊 System Statistics

- **User Roles:** 4 (Admin, Staff, Cashier, Student)
- **Reservation Statuses:** 7 distinct states
- **Return Reasons:** 4 for students, 6 for staff
- **Stock Actions:** 3 types (STAFF_RETURN, USER_PICKUP, USER_RETURN)
- **Payment Methods:** CASH ONLY
- **Supported Courses:** BSIT, BSCS, BSIS

---

## 🔐 Security Features

- Role-based access control
- Password-protected accounts
- Active/Inactive student account management
- Input validation on all user entries
- Transaction logging for accountability
- Status validation at each workflow stage

---

## 📝 Future Enhancements

- [ ] Multiple payment methods (GCash, Card)
- [ ] Email notifications for status changes
- [ ] Generate PDF receipts
- [ ] Dashboard with analytics
- [ ] Database migration to MySQL/PostgreSQL
- [ ] Web-based interface
- [ ] Mobile application
- [ ] Barcode scanning integration
- [ ] Automated low-stock alerts

---

## 👨‍💻 Developer

**Bowie Ramirez**
- GitHub: [@BowieRamirez](https://github.com/BowieRamirez)
- Repository: [Inventory-System](https://github.com/BowieRamirez/Inventory-System)

---

## 📄 License

This project is created for educational purposes as part of university coursework.

---

## 🤝 Contributing

This is an academic project. For suggestions or improvements, please open an issue or contact the developer.

---

## 📞 Support

For issues or questions about the system:
1. Check the documentation above
2. Review the code comments
3. Open an issue on GitHub
4. Contact the developer

---

**Last Updated:** October 30, 2025  
**Version:** 2.0 (Multi-Stage Approval with Stock Logging)
