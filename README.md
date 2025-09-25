# STI Merch System

A comprehensive console-based inventory management system for school uniforms and merchandise.

## Features

### Admin Features
- **Help**: Step-by-step ordering guide and system navigation help
- **Users Reservations**: View all student reservations, update status, and manage pending requests
- **Stock Management**: View all inventory, search items, and manage stock levels
- **Add/Remove Items**: Full inventory management with ability to add new items and remove existing ones

### Student Features
- **Help**: Ordering instructions and system guidance
- **Reserve Items**: Browse and reserve uniforms and merchandise
- **Stock View**: View available items for their course and STI special merchandise
- **Your Reservations**: Track reservation status and manage personal orders

## Supported Courses
- IT (Information Technology)
- CS (Computer Science)
- Tourism Management
- Multi Media Arts
- HRM (Hotel & Restaurant Management)
- Accountancy
- Business Administration
- Comp E (Computer Engineering)

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