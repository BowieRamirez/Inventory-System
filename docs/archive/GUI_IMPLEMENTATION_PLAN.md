# GUI Implementation Plan for STI ProWear System

## 📋 Executive Summary

This document outlines the complete strategy for transitioning the **STI ProWear System** from a console-based application to a modern GUI application using **JavaFX + AtlantaFX**.

---

## 🏗️ Current Architecture Analysis

### **Existing Components**

#### **1. Entry Point & Main System**
- `MerchSystem.java` - Main entry point, handles authentication flow
- Console-based menu system with Scanner input

#### **2. User Roles & Authentication**
- **Admin** (`Admin.java`, `AdminInterface.java`) - Full system access
- **Staff** (`Staff.java`, `StaffInterface.java`) - Inventory & reservation management
- **Cashier** (`Cashier.java`, `CashierInterface.java`) - Payment processing
- **Student** (`Student.java`, `StudentInterface.java`) - Browse, reserve, pickup items
- **Base User** (`User.java`) - Abstract authentication base

#### **3. Business Logic (Core Managers)**
- `InventoryManager` - Item CRUD, stock management, search
- `ReservationManager` - Reservation lifecycle, approval workflow
- `ReceiptManager` - Receipt generation and tracking

#### **4. Data Models**
- `Item` - Product with code, name, course, size, quantity, price
- `Reservation` - Order with status tracking, payment info, return eligibility
- `Receipt` - Payment records with detailed formatting
- `Student` - User profile with course, active status

#### **5. Utilities**
- `FileStorage` - File-based persistence (CSV/TXT)
- `InputValidator` - Console input validation
- `TermsAndConditions` - T&C acceptance flow

---

## 🎯 GUI Architecture Design

### **Design Pattern: Model-View-Controller (MVC)**

```
┌─────────────────────────────────────────────────────────────┐
│                         GUI Layer                            │
│  (JavaFX Views + AtlantaFX Styling)                         │
│  - LoginView, DashboardView, InventoryView, etc.            │
└──────────────────┬──────────────────────────────────────────┘
                   │ User Actions
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    Controller Layer                          │
│  - LoginController, AdminController, StudentController       │
│  - Handles UI events, validates input, calls services        │
└──────────────────┬──────────────────────────────────────────┘
                   │ Business Logic Calls
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   Service/Manager Layer                      │
│  - InventoryManager, ReservationManager, ReceiptManager      │
│  - Business logic (UNCHANGED - reuse existing code)          │
└──────────────────┬──────────────────────────────────────────┘
                   │ Data Access
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                      Data Layer                              │
│  - FileStorage (existing), Models (Item, Reservation, etc.) │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Proposed Project Structure

```
src/
├── main/
│   ├── MerchSystem.java (KEEP - console version)
│   └── MerchSystemGUI.java (NEW - GUI entry point)
│
├── gui/
│   ├── MainApp.java (JavaFX Application entry)
│   │
│   ├── controllers/
│   │   ├── LoginController.java
│   │   ├── AdminController.java
│   │   ├── StaffController.java
│   │   ├── CashierController.java
│   │   └── StudentController.java
│   │
│   ├── views/
│   │   ├── LoginView.java
│   │   ├── admin/
│   │   │   ├── AdminDashboard.java
│   │   │   ├── InventoryPanel.java
│   │   │   ├── ReservationPanel.java
│   │   │   └── AccountManagementPanel.java
│   │   ├── student/
│   │   │   ├── StudentDashboard.java
│   │   │   ├── StockBrowserPanel.java
│   │   │   ├── ReservationStatusPanel.java
│   │   │   └── PaymentPanel.java
│   │   ├── cashier/
│   │   │   └── CashierDashboard.java
│   │   └── staff/
│   │       └── StaffDashboard.java
│   │
│   ├── components/ (Reusable UI components)
│   │   ├── ItemCard.java
│   │   ├── ItemTable.java
│   │   ├── ReservationCard.java
│   │   ├── ReceiptViewer.java
│   │   └── NavigationBar.java
│   │
│   ├── utils/
│   │   ├── GUIValidator.java (GUI input validation)
│   │   ├── AlertHelper.java (Dialogs & notifications)
│   │   ├── SceneManager.java (Scene navigation)
│   │   └── ThemeManager.java (AtlantaFX theme switching)
│   │
│   └── resources/
│       ├── fxml/ (Optional FXML files)
│       ├── css/ (Custom styles)
│       └── images/ (Icons, logos)
│
├── inventory/ (UNCHANGED)
├── admin/ (UNCHANGED)
├── student/ (UNCHANGED)
├── user/ (UNCHANGED)
└── utils/ (UNCHANGED - keep for backward compatibility)
```

---

## 🛠️ Technology Stack

### **Core Framework**
- **JavaFX 21+** - Modern UI framework
- **AtlantaFX 2.0.1** - Modern theme library (Primer, Nord, Dracula themes)

### **Build Tool**
- **Maven** or **Gradle** (recommended)

### **Dependencies**
```xml
<!-- pom.xml -->
<dependencies>
    <!-- JavaFX -->
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>21.0.1</version>
    </dependency>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-fxml</artifactId>
        <version>21.0.1</version>
    </dependency>
    
    <!-- AtlantaFX Theme -->
    <dependency>
        <groupId>io.github.mkpaz</groupId>
        <artifactId>atlantafx-base</artifactId>
        <version>2.0.1</version>
    </dependency>
</dependencies>
```

---

## 📝 Implementation Phases

### **Phase 1: Project Setup & Foundation** (2-3 days)
- [ ] Setup Maven/Gradle with JavaFX + AtlantaFX dependencies
- [ ] Create base GUI package structure
- [ ] Implement `MainApp.java` (JavaFX Application)
- [ ] Setup `SceneManager` for navigation
- [ ] Configure AtlantaFX theme (Primer Light/Dark)
- [ ] Create `AlertHelper` for dialogs
- [ ] Test basic window launch

### **Phase 2: Authentication & Login Screen** (2-3 days)
- [ ] Design `LoginView` with modern UI
  - Username/Student ID field
  - Password field (masked)
  - Role selector (Admin/Staff/Cashier/Student)
  - Login button
  - Signup button (for students)
- [ ] Implement `LoginController`
  - Validate credentials
  - Call existing authentication methods
  - Navigate to appropriate dashboard
- [ ] Create `SignupView` for student registration
- [ ] Implement form validation with visual feedback

### **Phase 3: Admin Dashboard** (4-5 days)
- [ ] Create `AdminDashboard` layout
  - Navigation sidebar
  - Main content area
  - Header with user info & logout
- [ ] Implement `InventoryPanel`
  - TableView for items (code, name, course, size, qty, price)
  - Add/Edit/Delete item dialogs
  - Search & filter functionality
  - Stock adjustment controls
- [ ] Implement `ReservationPanel`
  - TableView for all reservations
  - Approve/Reject buttons
  - Status filter (Pending, Approved, Completed, Cancelled)
  - Reservation details dialog
- [ ] Implement `AccountManagementPanel`
  - Student list TableView
  - Activate/Deactivate accounts
  - Password reset functionality
- [ ] Connect to existing managers (InventoryManager, ReservationManager)

### **Phase 4: Student Dashboard** (4-5 days)
- [ ] Create `StudentDashboard` layout
- [ ] Implement `StockBrowserPanel`
  - GridView/ListView of items (card-based)
  - Filter by course
  - Search by code/name
  - Item details dialog
  - "Reserve" button
- [ ] Implement `ReservationStatusPanel`
  - TableView of student's reservations
  - Status badges (color-coded)
  - Cancel reservation button
  - Return item button (with 10-day check)
- [ ] Implement `PaymentPanel`
  - Payment status overview
  - Receipt viewer
  - Pickup confirmation
- [ ] Create reservation flow dialog
  - Item selection
  - Size picker
  - Quantity input
  - Confirmation summary

### **Phase 5: Cashier Dashboard** (2-3 days)
- [ ] Create `CashierDashboard`
- [ ] Implement payment processing interface
  - Search reservation by ID
  - Display reservation details
  - Payment method selector (Cash, GCash, Card, Bank)
  - Process payment button
  - Receipt generation & display
- [ ] View all reservations table

### **Phase 6: Staff Dashboard** (2-3 days)
- [ ] Create `StaffDashboard`
- [ ] Implement inventory management (similar to Admin but limited)
- [ ] Implement reservation approval interface
- [ ] Receipt viewing functionality

### **Phase 7: Reusable Components** (2-3 days)
- [ ] Create `ItemCard` component
  - Image placeholder
  - Item name, course, price
  - Stock indicator
  - "Reserve" button
- [ ] Create `ItemTable` component
  - Sortable columns
  - Inline editing
  - Context menu (right-click actions)
- [ ] Create `ReservationCard` component
  - Status badge
  - Item details
  - Action buttons
- [ ] Create `ReceiptViewer` component
  - Formatted receipt display
  - Print functionality (optional)

### **Phase 8: Polish & UX Enhancements** (2-3 days)
- [ ] Add loading indicators for long operations
- [ ] Implement toast notifications (success/error messages)
- [ ] Add confirmation dialogs for destructive actions
- [ ] Implement keyboard shortcuts
- [ ] Add tooltips and help text
- [ ] Theme switcher (Light/Dark mode)
- [ ] Responsive layout adjustments
- [ ] Error handling & user-friendly messages

### **Phase 9: Testing & Bug Fixes** (3-4 days)
- [ ] Test all user flows
- [ ] Test data persistence
- [ ] Test edge cases (empty lists, invalid input)
- [ ] Test concurrent operations
- [ ] Fix identified bugs
- [ ] Performance optimization

### **Phase 10: Documentation & Deployment** (1-2 days)
- [ ] Update README with GUI instructions
- [ ] Create user manual
- [ ] Package application (JAR with dependencies)
- [ ] Create installer (optional - jpackage)

---

## 🎨 UI/UX Design Guidelines

### **Color Scheme (AtlantaFX Primer Theme)**
- **Primary**: Blue (#0969DA)
- **Success**: Green (#1A7F37)
- **Warning**: Orange (#BF8700)
- **Danger**: Red (#CF222E)
- **Background**: White/Dark based on theme

### **Typography**
- **Headers**: 18-24px, Bold
- **Body**: 14px, Regular
- **Labels**: 12px, Medium

### **Layout Principles**
- **Spacing**: 8px base unit (8, 16, 24, 32px)
- **Card Padding**: 16px
- **Button Height**: 36px
- **Input Height**: 40px

### **Component Standards**
- **Tables**: Striped rows, hover effects, sortable headers
- **Buttons**: Primary (filled), Secondary (outlined), Danger (red)
- **Forms**: Labels above inputs, validation feedback below
- **Dialogs**: Centered, max-width 600px, backdrop blur

---

## 🔄 Migration Strategy

### **Approach: Parallel Development**
1. **Keep console version intact** - No breaking changes
2. **Build GUI alongside** - New package structure
3. **Reuse business logic** - No duplication
4. **Gradual rollout** - Test GUI per role
5. **Fallback option** - Console remains available

### **Data Compatibility**
- ✅ No changes to file formats
- ✅ Same FileStorage class
- ✅ Same data models
- ✅ Seamless transition

---

## ⚠️ Key Considerations

### **Input Validation**
- Replace `InputValidator` (Scanner-based) with `GUIValidator`
- Use JavaFX validation framework
- Visual feedback (red borders, error labels)

### **Threading**
- Use `Task` and `Platform.runLater()` for long operations
- Prevent UI freezing during file I/O

### **Error Handling**
- Replace `System.out.println` with `AlertHelper.showError()`
- User-friendly error messages
- Logging for debugging

### **Navigation**
- Implement `SceneManager` for view switching
- Maintain navigation history
- Breadcrumb navigation for deep hierarchies

---

## 📊 Estimated Timeline

| Phase | Duration | Dependencies |
|-------|----------|--------------|
| Phase 1: Setup | 2-3 days | None |
| Phase 2: Login | 2-3 days | Phase 1 |
| Phase 3: Admin | 4-5 days | Phase 1, 2 |
| Phase 4: Student | 4-5 days | Phase 1, 2 |
| Phase 5: Cashier | 2-3 days | Phase 1, 2 |
| Phase 6: Staff | 2-3 days | Phase 1, 2 |
| Phase 7: Components | 2-3 days | Phase 3-6 |
| Phase 8: Polish | 2-3 days | Phase 3-7 |
| Phase 9: Testing | 3-4 days | All phases |
| Phase 10: Docs | 1-2 days | Phase 9 |
| **TOTAL** | **25-35 days** | |

---

## 🚀 Next Steps

1. **Review this plan** - Confirm approach and timeline
2. **Setup development environment** - Install JavaFX, configure IDE
3. **Create Maven/Gradle project** - Add dependencies
4. **Start Phase 1** - Build foundation
5. **Iterate and refine** - Adjust based on feedback

---

## 📚 Resources

- **JavaFX Documentation**: https://openjfx.io/
- **AtlantaFX GitHub**: https://github.com/mkpaz/atlantafx
- **AtlantaFX Sampler**: https://mkpaz.github.io/atlantafx/
- **JavaFX Tutorial**: https://jenkov.com/tutorials/javafx/index.html

---

**Document Version**: 1.0  
**Last Updated**: 2025-10-30  
**Author**: AI Assistant

