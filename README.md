# 🛍️ STI ProWear System - Modern Inventory Management

A comprehensive **JavaFX-based inventory management system** for STI ProWear, featuring modern UI design, role-based access control, and complete reservation workflow management.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-blue?style=flat-square)
![AtlantaFX](https://img.shields.io/badge/AtlantaFX-2.0.1-purple?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.9.5-red?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

> **📌 Note:** This is a **GUI-only** application. The legacy console/CLI version has been removed. All functionality is now accessible through the modern JavaFX interface.

---

## 📋 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#-tech-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Running the Application](#-running-the-application)
- [User Accounts](#-user-accounts)
- [Project Structure](#-project-structure)
- [Architecture](#-architecture)
- [Development](#-development)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### 🎨 **Modern UI/UX**
- Clean, professional interface with **AtlantaFX** theming
- **Light/Dark mode** toggle
- Responsive design with smooth animations
- Intuitive navigation with sidebar menus

### 👥 **Role-Based Access Control**
Four distinct user roles with specialized dashboards:

#### 👨‍💼 **Admin Dashboard**
- 📊 Real-time statistics (Total Items, Pending Reservations, Students, Low Stock)
- 📦 Complete inventory management (Add, Edit, Delete items)
- 📋 Reservation approval and management
- 👥 Student account management (Activate/Deactivate, Password reset)
- 📝 Stock logs and audit trail
- Quick action buttons for common tasks

#### 🎓 **Student Dashboard**
- 🛍️ Browse available items by course
- 📦 View item details (Name, Code, Size, Price, Stock)
- 🛒 Create reservations (single items or bundles)
- 📋 Track reservation status
- ↩️ **Selective bundle returns** - Choose specific items to return
- � Request returns within 10-day window
- �👤 Profile management
- Filter items by course and size

#### 💰 **Cashier Dashboard**
- 💳 Process payments for approved reservations
- 📋 View all reservations (deduplicated bundles)
- 🧾 Generate and print receipts with full itemization
- Payment method support (Cash, GCash, Card, Bank)
- Bundle receipt management

#### 👔 **Staff Dashboard**
- 📋 Approve pending reservations (single & bundle)
- 📦 Manage inventory stock
- 📝 View stock logs
- ↩️ Process return requests
- Stock return processing

### 🔐 **Authentication & Security**
- Auto-detection of user role (no manual selection needed)
- Secure password authentication
- Account activation/deactivation
- Session management

### 📦 **Inventory Management**
- Add, edit, and delete items
- Course-specific inventory (SHS & Tertiary)
- Size management (XS, S, M, L, XL, XXL, One Size)
- Stock tracking and low-stock alerts
- Item code validation (1000-9999)
- Bundle order support with unique IDs

### 📋 **Reservation System**
- Complete reservation lifecycle:
  - `PENDING` → `APPROVED - WAITING FOR PAYMENT` → `PAID - READY FOR PICKUP` → `COMPLETED`
- **Bundle orders** with multiple items linked by Bundle ID
- **Selective return** feature - Choose specific items from bundles
- Return policy (10-day window from completion)
- Cancellation support
- Payment tracking
- Individual item status within bundles

### 🎓 **Student Management**
- Student registration with validation
- Course assignment (ABM, STEM, HUMSS, TVL, BSCS, BSIT, etc.)
- Account status management
- Password management

### 🆕 **Latest Features**
- ✨ **Selective Bundle Returns** (Nov 2025)
  - Students can choose which items from a bundle to return
  - Checkbox interface for easy selection
  - Select All / Deselect All buttons
  - Keep good items while returning problematic ones
  - See [Selective Bundle Return Documentation](docs/features/SELECTIVE_BUNDLE_RETURN.md)

- 📊 **Advanced Reporting System** (Latest)
  - **Live Search**: Real-time filtering across all reports
  - **Smart Filtering**: Threshold controls and dynamic updates
  - **Multi-Format Export**: PDF and Excel export capabilities
  - **11 Comprehensive Reports**: Stock, Transaction, and Student Activity reports
  - See [Advanced Reporting Documentation](docs/features/ADVANCED_REPORTING_FEATURES.md)

---

## 📸 Screenshots

### Login Screen
Modern login interface with auto-role detection and theme toggle.

### Admin Dashboard
Comprehensive dashboard with statistics, inventory management, and quick actions.

### Student Dashboard
Browse items, create reservations, and track orders.

---

## 🛠️ Tech Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 (LTS) | Core programming language |
| **JavaFX** | 21.0.1 | GUI framework |
| **AtlantaFX** | 2.0.1 | Modern theme library |
| **Maven** | 3.9.11+ | Build tool & dependency management |
| **File Storage** | CSV/TXT | Data persistence |

---

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

### 1. **Java Development Kit (JDK) 21**
- **Download:** [Eclipse Temurin 21 (LTS)](https://adoptium.net/)
- **Verify installation:**
  ```powershell
  java -version
  ```
  Should output: `openjdk version "21.x.x"`

### 2. **Maven 3.9.5+**
- **Option A:** Use an IDE (IntelliJ IDEA, Eclipse, VSCode) - Maven is included
- **Option B:** Manual installation:
  - Download from [Apache Maven](https://maven.apache.org/download.cgi)
  - Extract to `C:\Program Files\Apache\maven`
  - Add `C:\Program Files\Apache\maven\bin` to PATH
  - **Verify:**
    ```powershell
    mvn -version
    ```

### 3. **IDE (Recommended)**
- **IntelliJ IDEA Community Edition** (Recommended) - [Download](https://www.jetbrains.com/idea/download/)
- **Eclipse IDE** - [Download](https://www.eclipse.org/downloads/)
- **Visual Studio Code** with Java extensions - [Download](https://code.visualstudio.com/)

---

## 🚀 Installation

### 1. **Clone the Repository**
```bash
git clone https://github.com/yourusername/Inventory-System.git
cd Inventory-System
```

### 2. **Install Dependencies**
```powershell
mvn clean install
```

This will:
- Download JavaFX 21.0.1
- Download AtlantaFX 2.0.1
- Compile the project
- Run tests (if any)

### 3. **Verify Project Structure**
Ensure the following directories exist:
```
src/database/data/
├── items.txt
├── students.txt
├── reservations.txt
└── receipts.txt
```

---

## ▶️ Running the Application

### **Option 1: Using Maven (Command Line)**
```powershell
mvn javafx:run
```

### **Option 2: Using IntelliJ IDEA**
1. Open the project in IntelliJ IDEA
2. Wait for Maven to download dependencies
3. Right-click `src/gui/MainApp.java`
4. Select **"Run 'MainApp.main()'"**

### **Option 3: Using Eclipse**
1. Import as Maven project
2. Right-click `src/gui/MainApp.java`
3. Select **"Run As" → "Java Application"**

### **Option 4: Using VSCode**
1. Install "Extension Pack for Java"
2. Open the project folder
3. Press `F5` or click "Run" on `MainApp.java`

---

## 🔑 User Accounts

The system automatically detects the user role based on credentials. No manual role selection is required.

### **Default Accounts**

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| **Admin** | `admin` | `admin123` | Full system access |
| **Staff** | `staff` | `staff123` | Inventory & reservations |
| **Cashier** | `cashier` | `cashier123` | Payment processing |
| **Student** | *Student ID* | *Set during registration* | Browse & reserve items |

### **Creating a Student Account**
1. Click **"Sign Up"** on the login screen
2. Fill in the registration form:
   - **Student ID:** 10-12 digits (e.g., `202312345678`)
   - **First Name:** Your first name
   - **Last Name:** Your last name
   - **Course:** Select from dropdown (ABM, STEM, BSCS, etc.)
   - **Gender:** Male or Female
   - **Password:** Minimum 6 characters
3. Click **"Sign Up"**
4. Login with your Student ID and password

---

## 📁 Project Structure

```
Inventory-System/
├── src/
│   ├── admin/                    # Admin, Staff, Cashier authentication models
│   │   ├── Admin.java
│   │   ├── Cashier.java
│   │   └── Staff.java
│   ├── database/                 # Data storage (text file database)
│   │   └── data/
│   │       ├── items.txt
│   │       ├── students.txt
│   │       ├── reservations.txt
│   │       ├── receipts.txt
│   │       └── stock_logs.txt
│   ├── gui/                      # JavaFX GUI layer
│   │   ├── controllers/          # Business logic controllers
│   │   │   ├── AdminDashboardController.java
│   │   │   ├── CashierDashboardController.java
│   │   │   ├── LoginController.java
│   │   │   ├── SignupController.java
│   │   │   ├── StaffDashboardController.java
│   │   │   └── StudentDashboardController.java
│   │   ├── utils/                # GUI utilities
│   │   │   ├── AlertHelper.java
│   │   │   ├── GUIValidator.java
│   │   │   ├── SceneManager.java
│   │   │   └── ThemeManager.java
│   │   ├── views/                # UI views
│   │   │   ├── AdminDashboard.java
│   │   │   ├── CashierDashboard.java
│   │   │   ├── LoginView.java
│   │   │   ├── SignupView.java
│   │   │   ├── StaffDashboard.java
│   │   │   └── StudentDashboard.java
│   │   └── MainApp.java          # Application entry point
│   ├── inventory/                # Inventory management
│   │   ├── InventoryManager.java
│   │   ├── Item.java
│   │   ├── Receipt.java
│   │   ├── ReceiptManager.java
│   │   ├── Reservation.java
│   │   └── ReservationManager.java
│   ├── student/                  # Student domain model
│   │   └── Student.java
│   ├── utils/                    # Core utilities
│   │   ├── FileStorage.java      # Text file database layer
│   │   ├── InputValidator.java
│   │   ├── StockReturnLogger.java
│   │   └── TermsAndConditions.java
│   └── user/                     # User base classes
│       └── User.java
├── pom.xml                       # Maven configuration
├── README.md                     # This file
└── .gitignore                    # Git ignore rules
```

---

## 🏗️ Architecture

### **Design Pattern: MVC (Model-View-Controller)**

```
┌─────────────────────────────────────────────────────────┐
│                     GUI Layer (View)                     │
│  LoginView, AdminDashboard, StudentDashboard, etc.      │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                Controller Layer                          │
│  LoginController, AdminDashboardController, etc.        │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│              Business Logic Layer (Model)                │
│  InventoryManager, ReservationManager, Student, etc.    │
└────────────────────┬────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────┐
│                  Data Layer                              │
│  FileStorage (CSV/TXT files)                            │
└─────────────────────────────────────────────────────────┘
```

### **Key Principles**
- ✅ **Separation of Concerns** - GUI, logic, and data are separated
- ✅ **100% Business Logic Reuse** - No modifications to existing console code
- ✅ **Single Responsibility** - Each class has one clear purpose
- ✅ **DRY (Don't Repeat Yourself)** - Utility classes for common operations

---

## 🔧 Development

### **Building the Project**
```powershell
mvn clean compile
```

### **Running Tests**
```powershell
mvn test
```

### **Creating a JAR**
```powershell
mvn clean package
```

The JAR will be created in `target/inventory-system-1.0.jar`

### **Running the JAR**
```powershell
java -jar target/inventory-system-1.0.jar
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

**Made with ❤️ for STI ProWear**

