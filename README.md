# STI Merch System - Enhanced Version

A comprehensive console-based inventory management system for school uniforms and merchandise with advanced validation, user registration, and improved user experience.

## 🆕 Latest Features (Enhanced Version)

### ✨ **New Student Features**
- **Student Registration**: Create account with first name, last name, username, password, Student ID, and course
- **Terms & Conditions**: Required acceptance during signup
- **Student ID Login**: Required Student ID for login authentication
- **Enhanced Help**: Detailed step-by-step ordering guide for students
- **Logout Confirmation**: Confirmation dialog before logging out
- **Size Selection**: Choose size when reserving items
- **Improved Reservation**: Confirmation dialogs and better validation
- **Enhanced Navigation**: Back options (0) in all menus including reserve and stock pages

### 🔧 **New Admin Features**
- **Comprehensive Admin Help**: Detailed administration guide
- **Approval Status**: "APPROVED - READY FOR PICKUP" status for approved reservations
- **Stock Update Confirmations**: Confirm before updating quantities
- **Enhanced Item Management**: Better validation for adding items with back button support
- **Logout Confirmation**: Confirmation dialog before logging out
- **Improved Navigation**: Back options (0) in all menus including add/remove/update and stock search

### 🛡️ **Input Validation & Security**
- **Smart Course Validation**: Only valid course codes accepted (14 courses total)
- **Item Code Validation**: Proper format enforcement (1000-9999)
- **Price Validation**: Realistic price ranges (₱0 - ₱10,000)
- **Quantity Validation**: Proper integer ranges (1-1000)
- **Size Validation**: XS, S, M, L, XL, XXL, One Size
- **Student ID Validation**: 6-12 digit format required for signup and login
- **Duplicate Prevention**: Username and Student ID uniqueness checks
- **Password Security**: 6-20 characters
- **Error Messages**: Clear, helpful error messages with format examples

### 📚 **Expanded Course Support**

#### 🏫 **Senior High School (6 courses)**
- **ABM** - Accountancy, Business, and Management
- **STEM** - Science, Technology, Engineering, and Mathematics
- **HUMSS** - Humanities and Social Sciences
- **TVL-ICT** - IT in Mobile App and Web Development
- **TVL-TO** - Tourism Operations
- **TVL-CA** - Culinary Arts

#### 🎓 **Tertiary Programs (8 courses)**
- **BSCS** - Bachelor of Science in Computer Science
- **BSIT** - Bachelor of Science in Information Technology
- **BSCpE** - Bachelor of Science in Computer Engineering
- **BSBA** - Bachelor of Science in Business Administration
- **BSA** - Bachelor of Science in Accountancy
- **BSHM** - Bachelor of Science in Hospitality Management
- **BMMA** - Bachelor of Multimedia Arts
- **BSTM** - Bachelor of Science in Tourism Management

## Features

### Admin Features
- **Enhanced Help Desk**: Comprehensive administration guide and best practices
- **Users Reservations**: View all student reservations, update status (including "APPROVED - READY FOR PICKUP"), and manage pending requests
- **Stock Management**: View all inventory, search items with back button, and manage stock levels
- **Add/Remove Items**: Full inventory management with back button (0) support in all operations
- **Logout Confirmation**: Safe logout with confirmation prompt

### Student Features
- **Student Registration**: Create new account with last name, first name, username, password, Student ID, and course
- **Student ID Login**: Login requires username, password, AND Student ID
- **Terms & Conditions**: Review and accept terms during registration
- **Enhanced Help**: Detailed step-by-step ordering guide specific to students
- **Reserve Items**: Browse and reserve uniforms with size selection and back button (0) support
- **Stock View**: View available items with search capability and back button
- **Your Reservations**: Track reservation status including "APPROVED - READY FOR PICKUP"
- **Logout Confirmation**: Safe logout with confirmation prompt

## Available Items
- **Course-specific uniforms**: Polo shirts, Pants, ID Lace
- **STI Special merchandise**: Anniversary Clothes, PE Uniforms, Washday Shirts
- **Sizes**: XS, S, M, L, XL, XXL, One Size

## System Login Credentials

### Admin Login
- Username: `admin`
- Password: `admin123`

### Student Login
- Must create account first through Student Sign Up
- Login requires:
  - Username (created during signup)
  - Password (created during signup)
  - Student ID (6-12 digits, registered during signup)
- Example: Username: `john`, Password: `password123`, Student ID: `2024001`

## Project Structure

```
MerchSystem/
├── .gitignore
├── README.md             # This file
└── src/                  # Source files organized by package
    ├── admin/            # Admin-related classes
    │   ├── Admin.java
    │   └── AdminInterface.java
    ├── inventory/        # Inventory and reservation management
    │   ├── Item.java
    │   ├── InventoryManager.java
    │   ├── Reservation.java
    │   └── ReservationManager.java
    ├── main/             # Main application entry point
    │   └── MerchSystem.java
    ├── student/          # Student-related classes
    │   ├── Student.java
    │   └── StudentInterface.java
    ├── user/             # Base user authentication
    │   └── User.java
    └── utils/            # Utility classes
        ├── InputValidator.java
        └── TermsAndConditions.java
```

## How to Run

### Manual Compilation and Execution

1. **Navigate to project directory:**
   ```bash
   cd "c:\Users\bowie ramirez\OneDrive\Desktop\Java coding Projects\MerchSystem"
   ```

2. **Compile all Java files:**
   ```bash
   javac src\admin\*.java src\inventory\*.java src\main\*.java src\student\*.java src\user\*.java src\utils\*.java
   ```

3. **Run the system:**

   @echo off
   cd /d "%~dp0"
   echo Compiling...
   javac src\admin\*.java src\inventory\*.java src\main\*.java src\student\*.java src\user\*.java src\utils\*.java
   echo Running...
   java -cp src main.MerchSystem
   echo Cleaning up...
   del /S *.class
   pause

   ```bash  
   java src\main\MerchSystem.java
   ```

### Notes
- The system runs directly from source files (.java)
- No bin folder is used - .class files are generated in the same directories as .java files
- All Java files are organized in package structure for better maintainability

## System Architecture

The system is built using Object-Oriented Programming principles with package organization:

### Packages
- **main**: Entry point (`MerchSystem.java`)
- **user**: Base authentication (`User.java`)
- **admin**: Admin functionality (`Admin.java`, `AdminInterface.java`)
- **student**: Student functionality (`Student.java`, `StudentInterface.java`)
- **inventory**: Inventory and reservations (`Item.java`, `InventoryManager.java`, `Reservation.java`, `ReservationManager.java`)
- **utils**: Validation and utilities (`InputValidator.java`, `TermsAndConditions.java`)

## Navigation

- Use number keys to select menu options
- Press `[0]` to go back to previous menu
- Press `[0]` in login prompts and item code inputs to cancel/go back
- All input validation includes helpful error messages
- Logout requires confirmation (y/n)

## Reservation Process

1. Student creates account (Sign Up) with last name, first name, username, password, Student ID, and course
2. Student logs in with username, password, AND Student ID
3. Student browses available items and selects size
4. Student reserves items using item codes with quantity selection
5. Reservation is created with **PENDING** status
6. Admin can update status to **APPROVED - READY FOR PICKUP**
7. Admin can mark as **COMPLETED** when picked up
8. Student can track all reservation statuses

## Default Inventory

The system comes pre-loaded with:
- BSIT uniforms (Polo Shirts S/M/L, Pants M, ID Lace)
- BSCS uniforms (Polo Shirts S/M, Pants L)
- STEM uniforms (Polo Shirts S/M, Pants M)
- STI Special merchandise (Anniversary Shirt, PE Uniform, Washday Shirt)
- Realistic pricing (₱50 - ₱500)
- Various sizes and quantities

## Reservation Status Types

- **PENDING**: Awaiting admin approval
- **APPROVED - READY FOR PICKUP**: Admin has approved, ready for student pickup
- **COMPLETED**: Items have been picked up by student
- **CANCELLED**: Reservation has been cancelled

## Input Validation Features

### Course Codes
- Must match one of 14 valid courses (6 SHS + 8 Tertiary)
- Case-insensitive input
- Full course list displayed during selection

### Student ID
- 6-12 digits only
- Required for both signup and login
- Checked for duplicates during signup

### Item Codes
- Range: 1000-9999
- Must exist in inventory
- Students can only reserve items for their course or STI Special items

### Sizes
- Valid sizes: XS, S, M, L, XL, XXL, One Size
- Case-insensitive input
- Displayed before selection

### Quantities
- Range: 1 to available stock
- Cannot exceed current inventory quantity
- Validated in real-time

### Prices
- Range: ₱0 - ₱10,000
- Admin use only
- Two decimal places for cents

## User Experience Enhancements

✅ **Back Button Support**: Press `[0]` to go back in all menus
✅ **Confirmation Dialogs**: Required for logout and critical operations
✅ **Size Selection**: Required when reserving clothing items
✅ **Clear Status Messages**: "APPROVED - READY FOR PICKUP" instead of just "APPROVED"
✅ **Input Validation**: Helpful error messages with examples
✅ **Course-Specific Items**: Students only see items for their course + STI Special
✅ **Real-time Stock Updates**: Quantities update immediately after reservation
✅ **Duplicate Prevention**: Username and Student ID uniqueness checks
✅ **Full Name Support**: Last name, First name format during signup

## Developer Notes

- Built with Java SE
- Uses console-based I/O (Scanner)
- Object-oriented design with inheritance and polymorphism
- Package structure for better organization
- No external dependencies required
- Cross-platform compatible