# Stock Logs Clickable Feature

## Overview
Added clickable row functionality to stock logs tables in both Admin and Staff dashboards. When a log entry is clicked, a detailed dialog shows comprehensive information about the stock change.

## Changes Made

### 1. AdminDashboardController.java
**Added Features**:
- ✅ Row click handler for stock logs table
- ✅ `showStockLogDetailsDialog()` method for detailed view

**Dialog Sections**:
1. **📅 Timestamp** - When the change occurred
2. **👤 Staff/Admin** - Who performed the action (with approval info)
3. **📦 Item Details** - Code, name, and size
4. **📊 Stock Change** - Visual change indicator (green for increase, red for decrease)
5. **📝 Details** - Full description and notes

### 2. StaffDashboardController.java
**Added Features**:
- ✅ Row click handler for stock logs table
- ✅ `showStockLogDetailsDialog()` method for user activities

**Dialog Sections**:
1. **📅 Timestamp** - When the activity occurred
2. **👤 Student/User** - Who performed the action
3. **📦 Item Details** - Code, name, and size
4. **📊 Stock Change** - Visual change indicator (green for returns, red for pickups)
5. **📝 Details** - Full activity description

## Implementation Details

### Row Click Handler
```java
table.setRowFactory(tv -> {
    javafx.scene.control.TableRow<String[]> row = new javafx.scene.control.TableRow<>();
    row.setOnMouseClicked(event -> {
        if (!row.isEmpty() && event.getClickCount() == 1) {
            String[] clickedLog = row.getItem();
            showStockLogDetailsDialog(clickedLog);
        }
    });
    return row;
});
```

### Dialog Features
- **Responsive Layout**: 600px minimum width
- **Sectioned Information**: Each piece of info in its own styled container
- **Color Coding**: Green (+) for stock increases, Red (-) for stock decreases
- **Icon Indicators**: Emojis for visual clarity
- **Wrapped Text**: Long details wrap properly
- **Theme Aware**: Uses theme colors for consistency

## Visual Examples

### Admin Stock Log Dialog
```
┌─────────────────────────────────────────────────┐
│           Stock Change Information              │
├─────────────────────────────────────────────────┤
│ 📅 TIMESTAMP                                    │
│ 2025-01-08 15:30:25                            │
├─────────────────────────────────────────────────┤
│ 👤 STAFF/ADMIN                                  │
│ admin_john                                      │
│ Approved By: admin_supervisor                   │
├─────────────────────────────────────────────────┤
│ 📦 ITEM DETAILS                                 │
│ Code: 101                                       │
│ Item: BSIT Shirt                               │
│ Size: M                                         │
├─────────────────────────────────────────────────┤
│ 📊 STOCK CHANGE                                 │
│ +5/50 (green, bold)                            │
│ Action: STAFF_RETURN                           │
├─────────────────────────────────────────────────┤
│ 📝 DETAILS                                      │
│ Staff returned 5 units - Item damaged in       │
│ display, returned to stock                     │
└─────────────────────────────────────────────────┘
```

### Staff Stock Log Dialog
```
┌─────────────────────────────────────────────────┐
│          User Activity Information              │
├─────────────────────────────────────────────────┤
│ 📅 TIMESTAMP                                    │
│ 2025-01-08 16:45:12                            │
├─────────────────────────────────────────────────┤
│ 👤 STUDENT/USER                                 │
│ Juan Dela Cruz                                  │
├─────────────────────────────────────────────────┤
│ 📦 ITEM DETAILS                                 │
│ Code: 101                                       │
│ Item: BSIT Shirt                               │
│ Size: M                                         │
├─────────────────────────────────────────────────┤
│ 📊 STOCK CHANGE                                 │
│ -1/49 (red, bold)                              │
│ Action: USER_PICKUP                            │
├─────────────────────────────────────────────────┤
│ 📝 DETAILS                                      │
│ Student Juan Dela Cruz (2021-12345) picked up │
│ completed order                                │
└─────────────────────────────────────────────────┘
```

## User Experience

### For Admins
1. Navigate to Stock Logs section
2. Click any log entry in the table
3. See detailed dialog with:
   - Staff member who made the change
   - Approval information
   - Complete item details
   - Stock change visualization
   - Full action description

### For Staff
1. Navigate to Stock Logs section
2. Click any log entry in the table
3. See detailed dialog with:
   - Student/user who performed action
   - Complete item details
   - Stock change visualization
   - Full activity description

## Benefits

### Enhanced Visibility
- ✅ Quick access to full log details
- ✅ No need to scroll horizontally
- ✅ Better readability with formatted sections
- ✅ Visual indicators for stock changes

### Improved UX
- ✅ Intuitive click interaction
- ✅ Clean, organized information display
- ✅ Consistent with other table views (orders, receipts)
- ✅ Professional dialog presentation

### Better Auditing
- ✅ Easy verification of log entries
- ✅ Complete approval chain visible
- ✅ Full context in one view
- ✅ Clear action categorization

## Technical Notes

### Data Structure
- Admin logs: 9 fields (includes "Approved By" column)
- Staff logs: 8 fields (no approval tracking needed)
- Both use pipe-delimited format from `stock_logs.txt`

### Performance
- Click handlers are lightweight
- Dialog creation is on-demand (no pre-loading)
- Table rendering unaffected by click handlers

### Compatibility
- Works with existing role-based filtering
- Maintains theme consistency
- Responsive to window resizing
- Compatible with search functionality

## Testing Checklist
- [x] Code compiles successfully
- [ ] Admin stock logs are clickable
- [ ] Staff stock logs are clickable
- [ ] Dialog shows correct information
- [ ] Stock changes color-coded correctly
- [ ] Long details wrap properly
- [ ] Dialog respects theme colors
- [ ] Close button works

## Future Enhancements
- Add "Export to PDF" button in dialog
- Add "View Related Logs" for same item/user
- Add timestamp filtering from dialog
- Add quick action buttons (e.g., "Find Similar Changes")
