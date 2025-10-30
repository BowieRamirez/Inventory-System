# STI ProWear System - GUI Transition Documentation

## 📖 Overview

This repository contains the **STI ProWear System**, an inventory management system for school merchandise. We are transitioning from a **console-based application** to a **modern GUI application** using **JavaFX + AtlantaFX**.

---

## 📂 Documentation Structure

This transition is documented across multiple files:

### **1. CODEBASE_ANALYSIS.md** 📊
**Purpose**: Complete analysis of the existing codebase  
**Contents**:
- System architecture overview
- Detailed breakdown of all components
- User roles and capabilities
- Business logic analysis
- Data models and workflows
- Code quality observations

**Read this first** to understand the current system.

### **2. GUI_IMPLEMENTATION_PLAN.md** 🗺️
**Purpose**: Comprehensive implementation roadmap  
**Contents**:
- GUI architecture design (MVC pattern)
- Proposed project structure
- Technology stack details
- 10-phase implementation plan
- UI/UX design guidelines
- Timeline estimates (25-35 days)
- Migration strategy

**Read this second** to understand the implementation approach.

### **3. QUICK_START_GUIDE.md** 🚀
**Purpose**: Hands-on setup instructions  
**Contents**:
- Prerequisites and requirements
- Maven/Gradle configuration
- Step-by-step setup guide
- First GUI window creation
- Testing AtlantaFX themes
- Troubleshooting tips

**Read this third** to start coding immediately.

---

## 🎯 Quick Summary

### **Current State**
- ✅ Fully functional console-based application
- ✅ Multi-role system (Admin, Staff, Cashier, Student)
- ✅ Complete inventory management
- ✅ Reservation workflow with approval
- ✅ Payment processing and receipts
- ✅ Return policy (10-day window)
- ✅ File-based persistence

### **Target State**
- 🎨 Modern JavaFX GUI with AtlantaFX themes
- 🎨 Intuitive user interface with visual feedback
- 🎨 Card-based item browsing
- 🎨 Interactive tables with sorting/filtering
- 🎨 Real-time status updates
- 🎨 Light/Dark theme support
- 🎨 Responsive layout design

### **Technology Choice**
**Selected**: **JavaFX + AtlantaFX**

**Why?**
- ✅ Modern, actively maintained
- ✅ Beautiful pre-built themes (Primer, Nord, Dracula)
- ✅ CSS-based styling (easy customization)
- ✅ Rich component library
- ✅ Excellent for data-heavy applications
- ✅ Cross-platform (Windows, Mac, Linux)
- ✅ Strong community support

**Alternatives Considered**:
- Swing + FlatLaf (older, but easier learning curve)
- SWT (native widgets, Eclipse-based)
- Vaadin (web-based, not desktop)

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              GUI Layer (NEW)                             │
│  JavaFX Views + AtlantaFX Styling                       │
│  - LoginView, Dashboards, Panels, Components            │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│           Controller Layer (NEW)                         │
│  - LoginController, AdminController, etc.                │
│  - Handles UI events, validates input                    │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│        Business Logic (REUSED - NO CHANGES)              │
│  - InventoryManager, ReservationManager, ReceiptManager  │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│         Data Layer (REUSED - NO CHANGES)                 │
│  - FileStorage, Models (Item, Reservation, etc.)         │
└─────────────────────────────────────────────────────────┘
```

**Key Principle**: **Reuse existing business logic, add GUI layer on top**

---

## 📋 Implementation Phases

| Phase | Focus | Duration | Status |
|-------|-------|----------|--------|
| **Phase 1** | Foundation & Utilities | 2-3 days | ⏳ Not Started |
| **Phase 2** | Authentication & Login | 2-3 days | ⏳ Not Started |
| **Phase 3** | Admin Dashboard | 4-5 days | ⏳ Not Started |
| **Phase 4** | Student Dashboard | 4-5 days | ⏳ Not Started |
| **Phase 5** | Cashier Dashboard | 2-3 days | ⏳ Not Started |
| **Phase 6** | Staff Dashboard | 2-3 days | ⏳ Not Started |
| **Phase 7** | Reusable Components | 2-3 days | ⏳ Not Started |
| **Phase 8** | Polish & UX | 2-3 days | ⏳ Not Started |
| **Phase 9** | Testing & Bug Fixes | 3-4 days | ⏳ Not Started |
| **Phase 10** | Documentation & Deploy | 1-2 days | ⏳ Not Started |

**Total Estimated Time**: 25-35 days

---

## 🚀 Getting Started

### **Step 1: Read Documentation**
1. Read `CODEBASE_ANALYSIS.md` (understand current system)
2. Read `GUI_IMPLEMENTATION_PLAN.md` (understand approach)
3. Read `QUICK_START_GUIDE.md` (setup environment)

### **Step 2: Setup Environment**
```bash
# Verify Java version (need 17+)
java -version

# Create pom.xml (see QUICK_START_GUIDE.md)
# Add JavaFX + AtlantaFX dependencies

# Install dependencies
mvn clean install
```

### **Step 3: Create Package Structure**
```
src/gui/
├── MainApp.java
├── controllers/
├── views/
├── components/
└── utils/
```

### **Step 4: Test Basic Setup**
```bash
# Run the test application
mvn javafx:run
```

### **Step 5: Start Phase 1**
- Create `SceneManager.java`
- Create `AlertHelper.java`
- Create `GUIValidator.java`
- Create `ThemeManager.java`

---

## 🎨 UI Design Preview

### **Login Screen**
```
┌─────────────────────────────────────────┐
│                                         │
│         STI ProWear System              │
│    Modern Inventory Management          │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Username / Student ID             │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Password              [👁]        │ │
│  └───────────────────────────────────┘ │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ Role: [Student ▼]                 │ │
│  └───────────────────────────────────┘ │
│                                         │
│     [    Login    ]  [   Signup   ]    │
│                                         │
│              Theme: [🌙 Dark]           │
└─────────────────────────────────────────┘
```

### **Student Dashboard**
```
┌─────────────────────────────────────────────────────────┐
│ STI ProWear | Welcome, Juan Dela Cruz (BSIT)  [Logout] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [📦 Browse Items] [📋 My Reservations] [💳 Payments]  │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐            │
│  │  Item 1  │  │  Item 2  │  │  Item 3  │            │
│  │  ₱450.00 │  │  ₱500.00 │  │  ₱480.00 │            │
│  │ [Reserve]│  │ [Reserve]│  │ [Reserve]│            │
│  └──────────┘  └──────────┘  └──────────┘            │
│                                                         │
│  Recent Reservations:                                   │
│  ┌─────────────────────────────────────────────────┐  │
│  │ ID: 5001 | Gray Polo | PENDING | ₱450.00       │  │
│  │ ID: 5002 | RTW Pants | PAID    | ₱500.00       │  │
│  └─────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔑 Key Features to Implement

### **For Students**
- ✅ Browse items by course (card-based view)
- ✅ Search and filter items
- ✅ Reserve items with size/quantity selection
- ✅ View reservation status with color-coded badges
- ✅ Cancel pending reservations
- ✅ View payment status
- ✅ Confirm item pickup
- ✅ Request returns (within 10 days)

### **For Admin**
- ✅ Full inventory CRUD operations
- ✅ Approve/reject reservations
- ✅ Manage student accounts (activate/deactivate)
- ✅ Reset passwords
- ✅ View all transactions
- ✅ Process returns and refunds
- ✅ Generate reports

### **For Cashier**
- ✅ Search reservations by ID
- ✅ Process payments (multiple methods)
- ✅ Generate receipts
- ✅ View payment history

### **For Staff**
- ✅ Manage inventory (add/edit items)
- ✅ Approve reservations
- ✅ View receipts
- ✅ Process returns

---

## ⚠️ Important Notes

### **What NOT to Change**
- ❌ Don't modify existing business logic classes
- ❌ Don't change file formats or data structures
- ❌ Don't remove console version (keep as fallback)
- ❌ Don't alter authentication logic

### **What to Add**
- ✅ New `gui` package (separate from existing code)
- ✅ Controllers for each view
- ✅ JavaFX views and components
- ✅ GUI-specific utilities

### **Migration Strategy**
- 🔄 Parallel development (GUI alongside console)
- 🔄 Reuse all business logic
- 🔄 No breaking changes to existing code
- 🔄 Gradual rollout by role
- 🔄 Console remains as fallback

---

## 📊 Progress Tracking

Track your progress using the task list:

```bash
# View current tasks
# (Use your IDE's task management or create a checklist)
```

Update this README as you complete phases!

---

## 🐛 Known Issues & Considerations

1. **Security**: Passwords currently stored in plain text
   - Consider implementing hashing (BCrypt, Argon2)

2. **Threading**: File I/O on main thread
   - Use JavaFX `Task` for async operations

3. **Validation**: Duplicated validation logic
   - Centralize in `GUIValidator`

4. **Error Handling**: Limited try-catch blocks
   - Add comprehensive error handling in GUI

5. **Testing**: No unit tests
   - Consider adding JUnit tests for business logic

---

## 📚 Resources

### **Official Documentation**
- JavaFX: https://openjfx.io/
- AtlantaFX: https://github.com/mkpaz/atlantafx
- Maven: https://maven.apache.org/

### **Tutorials**
- JavaFX Tutorial: https://jenkov.com/tutorials/javafx/
- AtlantaFX Sampler: https://mkpaz.github.io/atlantafx/

### **Tools**
- Scene Builder: https://gluonhq.com/products/scene-builder/
- IntelliJ IDEA: https://www.jetbrains.com/idea/

---

## 🤝 Contributing

When working on this project:

1. **Follow the implementation plan** in `GUI_IMPLEMENTATION_PLAN.md`
2. **Test frequently** - Run after each change
3. **Commit often** - Use meaningful commit messages
4. **Document changes** - Update this README
5. **Ask questions** - If stuck, refer to documentation

---

## 📝 Version History

| Version | Date | Description |
|---------|------|-------------|
| 1.0 | Current | Console-based application |
| 2.0 | In Progress | GUI implementation with JavaFX + AtlantaFX |

---

## 📧 Support

For questions or issues:
1. Check `QUICK_START_GUIDE.md` troubleshooting section
2. Review `GUI_IMPLEMENTATION_PLAN.md` for detailed guidance
3. Consult official JavaFX/AtlantaFX documentation

---

**Happy Coding! 🚀**

Let's build an amazing GUI for the STI ProWear System!

