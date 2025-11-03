# Staff Management Feature

## Overview
The Staff Management feature allows Administrators to manage staff accounts (Staff and Cashier roles) through a user-friendly GUI interface in the Admin Dashboard.

## Feature Highlights

### ✅ Completed Implementation

1. **Enhanced Staff Model** (`src/admin/Staff.java`)
   - Added fields: `staffId`, `firstName`, `lastName`, `role` (Staff/Cashier), `isActive`
   - Removed hardcoded credentials (staff/staff123)
   - Dynamic authentication based on loaded staff data
   - Support for account activation/deactivation

2. **Staff Persistence** (`src/utils/FileStorage.java`)
   - New file: `src/database/data/staff.txt`
   - File format: `staffId|password|firstName|lastName|role|isActive`
   - Methods added:
     - `loadStaff()` - Load all staff from file
     - `saveStaff()` - Save all staff to file
     - `addStaff()` - Add new staff member
     - `updateStaff()` - Update existing staff
     - `findStaffById()` - Find staff by ID
     - `staffExists()` - Check if staff ID exists
   - Auto-creates default staff file with 2 accounts on first run

3. **Unified Authentication** (`src/gui/controllers/LoginController.java`)
   - Merged Staff and Cashier authentication into single method
   - Role-based dashboard navigation (Staff → Staff Dashboard, Cashier → Cashier Dashboard)
   - Active status verification before login
   - Dynamic staff loading from file

4. **Admin Dashboard UI** (`src/gui/controllers/AdminDashboardController.java`)
   - Account Management now has 2 tabs:
     - 👨‍🎓 **Students** - Manage student accounts
     - 👔 **Staff** - Manage staff accounts (NEW)
   - Staff Management Table columns:
     - Staff ID
     - Name (Last, First format)
     - Role (Staff or Cashier)
     - Active status (✓ Active / ✗ Inactive)
     - Actions (Edit & Toggle buttons)

5. **CRUD Operations**
   - **Add Staff** - Dialog with fields: Staff ID, Password, First Name, Last Name, Role
   - **Edit Staff** - Update password, name, and role (ID is immutable)
   - **Toggle Active/Inactive** - Enable/disable staff accounts
   - **Search** - Filter by name or staff ID
   - **Refresh** - Reload staff list from file

## Default Staff Accounts

When the application runs for the first time, `staff.txt` is auto-created with:

| Staff ID | Password   | Name        | Role    | Active |
|----------|------------|-------------|---------|--------|
| staff    | staff123   | John Doe    | Staff   | ✓      |
| cashier  | cashier123 | Jane Smith  | Cashier | ✓      |

## Architecture Changes

### Before
```
Admin ━━━ authenticate() (hardcoded: admin/admin123)
Staff ━━━ authenticate() (hardcoded: staff/staff123)
Cashier ━ authenticate() (hardcoded: cashier/cashier123)
Student ━ authenticate() (from students.txt)
```

### After
```
Admin ━━━━ authenticate() (hardcoded: admin/admin123)
Staff ━━━━ authenticate() (from staff.txt, role: Staff/Cashier)
  ├─ Staff role → Staff Dashboard
  └─ Cashier role → Cashier Dashboard
Student ━━ authenticate() (from students.txt)
```

## File Structure

```
src/
├── admin/
│   ├── Staff.java ✏️ MODIFIED - Enhanced with full staff properties
│   ├── Cashier.java (kept for backwards compatibility)
│   └── Admin.java
├── gui/
│   ├── controllers/
│   │   ├── AdminDashboardController.java ✏️ MODIFIED - Added Staff management UI & CRUD
│   │   └── LoginController.java ✏️ MODIFIED - Unified Staff/Cashier authentication
│   └── views/
│       └── AdminDashboard.java (no changes needed)
├── utils/
│   └── FileStorage.java ✏️ MODIFIED - Added Staff persistence methods
└── database/
    └── data/
        ├── students.txt
        ├── items.txt
        ├── reservations.txt
        └── staff.txt ⭐ NEW - Staff data file
```

## Usage Guide

### For Administrators

1. **Login as Admin**
   - Username: `admin`
   - Password: `admin123`

2. **Navigate to Account Management**
   - Click "👥 Accounts" in the sidebar
   - Click the "👔 Staff" tab

3. **Add New Staff Member**
   - Click "➕ Add Staff"
   - Fill in:
     - Staff ID (unique identifier)
     - Password
     - First Name
     - Last Name
     - Role (Staff or Cashier)
   - Click "Add"

4. **Edit Staff Member**
   - Find the staff member in the table
   - Click "Edit" button
   - Modify password, name, or role
   - Click "Save"

5. **Deactivate/Activate Staff**
   - Click "Deactivate" button to disable login
   - Click "Activate" button to re-enable login
   - Deactivated accounts cannot log in

6. **Search Staff**
   - Type in the search box to filter by name or staff ID
   - Click "🔄 Refresh" to reload and clear search

### For Staff/Cashier

- Login with your assigned Staff ID and password
- If your role is "Staff" → Staff Dashboard
- If your role is "Cashier" → Cashier Dashboard
- If account is deactivated → Login denied with error message

## Security Features

✅ **Validation**
- All fields required when adding/editing staff
- Staff ID uniqueness enforced
- Active status check before login

✅ **Role-Based Access Control**
- Only Admin can manage staff accounts
- Staff and Cashier have their respective dashboards
- Deactivated accounts blocked from authentication

✅ **Data Persistence**
- All changes saved to `staff.txt` immediately
- File auto-created with defaults if missing
- Consistent pipe-delimited format

## Testing Checklist

- [x] Default staff file created on first run
- [x] Login with default staff account (staff/staff123)
- [x] Login with default cashier account (cashier/cashier123)
- [x] Admin can view Staff tab in Account Management
- [x] Add new staff member with validation
- [x] Edit existing staff member (name, password, role)
- [x] Toggle staff active/inactive status
- [x] Deactivated staff cannot login
- [x] Search functionality filters correctly
- [x] Refresh reloads staff list
- [x] Role-based navigation (Staff vs Cashier dashboard)
- [x] Build compiles successfully

## Technical Details

### Staff.java Changes
```java
// OLD: Hardcoded credentials
private static final String DEFAULT_STAFF_USERNAME = "staff";
private static final String DEFAULT_STAFF_PASSWORD = "staff123";

// NEW: Dynamic properties
private String staffId;
private String firstName;
private String lastName;
private String role;  // "Staff" or "Cashier"
private boolean isActive;
```

### FileStorage.java New Methods
```java
public static List<Staff> loadStaff()
public static boolean saveStaff(List<Staff> staffList)
public static boolean addStaff(List<Staff> staffList, Staff newStaff)
public static boolean updateStaff(List<Staff> staffList, Staff updatedStaff)
public static Staff findStaffById(List<Staff> staffList, String staffId)
public static boolean staffExists(List<Staff> staffList, String staffId)
```

### staff.txt Format
```
staff|staff123|John|Doe|Staff|true
cashier|cashier123|Jane|Smith|Cashier|true
newstaff|pass123|Mike|Johnson|Staff|true
```

## Future Enhancements (Optional)

1. **Password Hashing** - Hash passwords instead of storing plain text
2. **Audit Logging** - Track who created/modified staff accounts
3. **Role Permissions** - Fine-grained permissions per role
4. **Bulk Import** - Import staff from CSV
5. **Email Notifications** - Notify staff when accounts are created/modified

## Build & Run

```bash
# Compile
mvn clean compile

# Run application
mvn org.openjfx:javafx-maven-plugin:0.0.8:run

# Package JAR (note: requires JavaFX runtime)
mvn package -DskipTests
```

## Success Metrics

✅ **All 6 todos completed**
1. ✅ Update Staff model with proper fields
2. ✅ Add Staff persistence to FileStorage
3. ✅ Update LoginController for unified Staff/Cashier auth
4. ✅ Add Staff Management UI to AdminDashboard
5. ✅ Implement Staff CRUD operations
6. ✅ Test Staff management system

✅ **Build Status:** SUCCESS
✅ **Application Status:** RUNNING
✅ **Staff Loaded:** 2 members authenticated
✅ **Login Tests:** Passed (staff and admin)

---

**Feature Status:** ✅ **COMPLETE AND TESTED**

*Last Updated: 2025-11-03*
