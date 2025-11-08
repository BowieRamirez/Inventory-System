# Stock Logs Differentiation Feature

## Overview
Implemented role-based filtering for stock logs to display relevant information to each user type:
- **Admin Dashboard**: Shows staff/admin activities (inventory management)
- **Staff Dashboard**: Shows user/customer activities (pickups and returns)

## Changes Made

### 1. AdminDashboardController.java
**Modified Method**: `loadStockLogs()`

**Filter Logic**:
- Shows only admin-relevant actions:
  - `STAFF_RETURN` - Staff/admin returning items to inventory
  - `ITEM_ADDED` - New items added to inventory
  - `ITEM_DELETED` - Items removed from inventory
  - `ITEM_UPDATED` - Item details updated

**Purpose**: 
- Admins need to see inventory management actions performed by staff
- Helps track staff changes to stock levels
- Monitors inventory corrections and adjustments

**UI Changes**:
- Column header changed from "Stock Change Requested By" to "Staff/Admin"
- Clearly indicates these are administrative actions

### 2. StaffDashboardController.java
**Modified Method**: `loadStockLogs()`

**Filter Logic**:
- Shows only staff-relevant actions:
  - `USER_PICKUP` - Students/users picking up completed orders
  - `USER_RETURN` - Students/users returning items

**Purpose**:
- Staff need to see customer-facing activities
- Helps track user pickups and returns
- Monitors customer service interactions

**UI Changes**:
- Column header changed from "Performed By" to "Student/User"
- Clearly indicates these are customer activities
- Added sorting by timestamp (newest first)

## Technical Details

### Action Types (StockReturnLogger.java)
The system uses the following action types to categorize stock changes:

**Admin Actions**:
- `STAFF_RETURN` - Staff returning items
- `ITEM_ADDED` - Adding new items
- `ITEM_DELETED` - Removing items
- `ITEM_UPDATED` - Updating item details

**User Actions**:
- `USER_PICKUP` - Customer claiming completed orders
- `USER_RETURN` - Customer returning items

### Filtering Implementation
Both controllers now use a whitelist approach:
```java
// Admin View
List<String> adminRelevantActions = Arrays.asList(
    "STAFF_RETURN", "ITEM_ADDED", "ITEM_DELETED", "ITEM_UPDATED"
);

// Staff View
List<String> staffRelevantActions = Arrays.asList(
    "USER_PICKUP", "USER_RETURN"
);
```

### Data Source
- Primary: `src/database/data/stock_logs.txt`
- Secondary (Admin only): `StockAuditManager` (for audit logs with approval tracking)

## Benefits

### For Admins
- ✅ Focused view of inventory management activities
- ✅ Track staff changes to stock levels
- ✅ Monitor inventory corrections and adjustments
- ✅ Audit staff actions for accountability

### For Staff
- ✅ Focused view of customer activities
- ✅ Track user pickups and returns
- ✅ Monitor customer service interactions
- ✅ Better visibility into user behavior

## Testing Checklist
- [x] Code compiles successfully
- [ ] Admin dashboard shows only STAFF_RETURN, ITEM_ADDED, ITEM_DELETED, ITEM_UPDATED
- [ ] Staff dashboard shows only USER_PICKUP, USER_RETURN
- [ ] Search functionality works with filtered logs
- [ ] Refresh button reloads filtered logs correctly
- [ ] Column headers are clear and descriptive

## Usage

### Admin Dashboard
1. Navigate to Stock Logs section
2. View shows only staff/admin inventory changes
3. Use search to filter by item name or staff member
4. Click refresh to reload latest logs

### Staff Dashboard
1. Navigate to Stock Logs section
2. View shows only customer pickup/return activities
3. Use search to filter by item or student name
4. Click refresh to reload latest logs

## Future Enhancements
- Add date range filtering
- Export filtered logs to CSV
- Add summary statistics by action type
- Real-time log updates via file watcher
