# STI Merch System - Enhanced Version

A comprehensive console-based inventory management system for school uniforms and merchandise with advanced validation, user registration, and improved user experience.

## 🆕 Latest Features (Enhanced Version)

### ✨ **New Student Features**
- **Student Registration**: Create account with username, password, Student ID, and course
- **Terms & Conditions**: Required acceptance during signup
- **Password Confirmation**: Secure account creation
- **Enhanced Help**: Detailed step-by-step ordering guide for students
- **Logout Functionality**: Safe logout from student dashboard
- **Improved Reservation**: Confirmation dialogs and better validation
- **Enhanced Navigation**: Back options in all menus

### 🔧 **New Admin Features**
- **Comprehensive Admin Help**: Detailed administration guide
- **Stock Update Confirmations**: Confirm before updating quantities
- **Enhanced Item Management**: Better validation for adding items
- **Logout Functionality**: Safe logout from admin dashboard
- **Improved Navigation**: Back options and better menu flow

### 🛡️ **Input Validation & Security**
- **Smart Course Validation**: Only valid course codes accepted (BSIT, BSCS, STEM, etc.)
- **Item Code Validation**: Proper format enforcement (1000-9999)
- **Price Validation**: Realistic price ranges (₱0 - ₱10,000)
- **Quantity Validation**: Proper integer ranges (1-1000)
- **Size Validation**: Only valid sizes (XS, S, M, L, XL, XXL, One Size)
- **Student ID Validation**: 6-12 digit format required
- **Password Security**: Minimum 6 characters, maximum 20
- **Error Messages**: Clear, helpful error messages with format examples

### 📚 **Expanded Course Support**
- **Senior High School**: STEM, ABM, HUMSS, GAS, TVL-ICT, TVL-HE, TVL-IA, ARTS
- **Tertiary Programs**: BSCS, BSIT, BSCpE, BSBA, BSA, BSHM, BSTM, BMMA

## Features

### Admin Features
- **Enhanced Help Desk**: Comprehensive administration guide and best practices
- **Users Reservations**: View all student reservations, update status, and manage pending requests
- **Stock Management**: View all inventory, search items, and manage stock levels with confirmations
- **Add/Remove Items**: Full inventory management with proper validation and confirmation dialogs
- **Logout & Exit**: Safe logout option and system exit

### Student Features
- **Student Registration**: Create new account with course validation and password confirmation
- **Terms & Conditions**: Review and accept terms during registration
- **Enhanced Help**: Detailed step-by-step ordering guide specific to students
- **Reserve Items**: Browse and reserve uniforms and merchandise with confirmation dialogs
- **Stock View**: View available items for their course and STI special merchandise
- **Your Reservations**: Track reservation status and manage personal orders
- **Logout & Exit**: Safe logout option and system exit

## Supported Courses

### 🎓 Senior High School (SHS)
- **STEM** - Science, Technology, Engineering, and Mathematics
- **ABM** - Accountancy, Business, and Management
- **HUMSS** - Humanities and Social Sciences
- **GAS** - General Academic Strand
- **TVL-ICT** - Technical-Vocational-Livelihood (ICT)
- **TVL-HE** - Technical-Vocational-Livelihood (Home Economics)
- **TVL-IA** - Technical-Vocational-Livelihood (Industrial Arts)
- **ARTS** - Arts and Design

### 🎓 Tertiary Programs
- **BSCS** - Bachelor of Science in Computer Science
- **BSIT** - Bachelor of Science in Information Technology
- **BSCpE** - Bachelor of Science in Computer Engineering
- **BSBA** - Bachelor of Science in Business Administration
- **BSA** - Bachelor of Science in Accountancy
- **BSHM** - Bachelor of Science in Hotel Management
- **BSTM** - Bachelor of Science in Tourism Management
- **BMMA** - Bachelor of Multimedia Arts

## Available Items
- **Course-specific uniforms**: Polo shirts, Pants, ID Lace
- **STI Special merchandise**: Anniversary Clothes, PE Uniforms, Washday Shirts, NSTP Uniforms
- **Sizes**: XS, S, M, L, XL, XXL

## System Login Credentials

### Admin Login
- Username: `admin`
- Password: `admin123`

### Student Login
- Any username and password (system validates for non-empty fields)
- Requires Student ID and Course selection
- Example: Username: `john`, Password: `password`, Student ID: `2024001`, Course: `IT`

## Project Structure

```
MerchSystem/
├── src/                    # Source files
│   ├── MerchSystem.java   # Main application
│   ├── User.java          # Base user class
│   ├── Admin.java         # Admin user class
│   ├── Student.java       # Student user class
│   ├── Item.java          # Item model
│   ├── InventoryManager.java  # Inventory management
│   ├── Reservation.java   # Reservation model
│   ├── ReservationManager.java  # Reservation management
│   ├── AdminInterface.java    # Admin UI
│   └── StudentInterface.java  # Student UI
├── bin/                   # Compiled classes (auto-generated)
├── docs/                  # Additional documentation
├── README.md             # This file
├── compile.bat           # Windows compilation script
├── run.bat               # Windows run script
├── compile.sh            # Unix/Linux compilation script
└── run.sh                # Unix/Linux run script
```

## How to Run

### Option 1: Using Scripts (Recommended)
**Windows:**
```batch
compile.bat
run.bat
```

**Unix/Linux/Mac:**
```bash
chmod +x compile.sh run.sh
./compile.sh
./run.sh
```

### Option 2: Manual Compilation
1. Compile all Java files:
   ```
   javac -d bin src/*.java
   ```

2. Run the system:
   ```
   java -cp bin MerchSystem
   ```

## System Architecture

The system is built using Object-Oriented Programming principles with the following classes:

- `MerchSystem`: Main application class
- `User`, `Admin`, `Student`: Authentication system
- `Item`, `InventoryManager`: Inventory management
- `Reservation`, `ReservationManager`: Reservation system
- `AdminInterface`, `StudentInterface`: User interfaces

## Navigation

- Use number keys to select menu options
- Press `[0]` to go back to previous menu or exit
- All input is case-insensitive where applicable

## Reservation Process

1. Student logs in and browses available items
2. Student reserves items using item codes
3. Reservation is created with PENDING status
4. Admin can approve/reject reservations
5. Student can track reservation status

## Default Inventory

The system comes pre-loaded with:
- Uniforms for all supported courses
- STI special merchandise
- Various sizes and quantities
- Realistic pricing

## Status Types

- **PENDING**: Awaiting admin approval
- **APPROVED**: Admin has approved the reservation
- **COMPLETED**: Items are ready for pickup
- **CANCELLED**: Reservation has been cancelled