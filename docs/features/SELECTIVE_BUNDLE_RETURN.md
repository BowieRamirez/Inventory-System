# Selective Bundle Return Feature

## Overview
This feature allows students to select which specific items from a bundle order they want to return, rather than being forced to return the entire bundle at once.

## Implementation Date
November 9, 2025

## Feature Description

### Problem Solved
Previously, when a student wanted to return items from a bundle order, they had to return **ALL** items in the bundle together. This was inconvenient if they only wanted to return specific items (e.g., wrong size, defective item) while keeping the rest.

### Solution
Added a checkbox-based selection interface in the return request dialog that allows students to:
- View all items in their bundle order
- Select which specific items they want to return
- Keep items they don't want to return
- Use "Select All" and "Deselect All" buttons for convenience

## How It Works

### For Bundle Orders
1. **Return Button Click**: When a student clicks "Request Return" on a completed bundle order
2. **Item Selection Dialog**: A dialog appears showing:
   - Header: "Request Return for: Bundle Order (X items)"
   - Checkbox list of all items in the bundle with:
     - Item name
     - Size
     - Quantity
     - Price
   - "Select All" and "Deselect All" buttons
   - Reason text area
3. **Validation**: System checks that at least one item is selected
4. **Submission**: Only selected items are marked as "RETURN REQUESTED"
5. **Feedback**: Success message indicates how many items were submitted for return

### For Single Orders
Single item orders remain unchanged - clicking return directly processes the return request without checkboxes.

## Technical Details

### Modified Files
- `src/gui/controllers/StudentDashboardController.java`
  - Updated `handleReturnRequest()` method
  - Added imports: `ArrayList`, `HashMap`, `Map`, `CheckBox`

### Key Code Changes

#### Data Structure
```java
Map<CheckBox, Reservation> itemCheckBoxMap = new HashMap<>();
```
This maps each checkbox to its corresponding reservation item.

#### UI Components
- **CheckBox**: One per bundle item, showing item details
- **Select All/Deselect All**: Convenience buttons for bulk selection
- **Styled VBox**: Container with border and padding for clean layout

#### Selection Logic
```java
for (Map.Entry<CheckBox, Reservation> entry : finalItemCheckBoxMap.entrySet()) {
    if (entry.getKey().isSelected()) {
        selectedItemsToReturn.add(entry.getValue());
    }
}
```

#### Validation
```java
if (selectedItemsToReturn.isEmpty()) {
    AlertHelper.showError("Error", "Please select at least one item to return.");
    return;
}
```

## User Experience Flow

### Before (Old Behavior)
1. Click "Request Return" on bundle
2. See list of ALL items
3. Enter reason
4. Submit - ALL items marked for return
5. ❌ No way to return only some items

### After (New Behavior)
1. Click "Request Return" on bundle
2. See checkboxes for each item (all selected by default)
3. ✅ **Uncheck items you want to keep**
4. ✅ **Use Select/Deselect All for convenience**
5. Enter reason
6. Submit - ONLY selected items marked for return
7. ✅ Remaining items stay as COMPLETED

## Success Messages

### All Items Selected
```
Return request submitted successfully for all 5 items in the bundle!

Please wait for admin/staff approval.
```

### Partial Selection
```
Return request submitted successfully for 2 selected item(s)!

Please wait for admin/staff approval.
```

### Partial Success (Some Expired)
```
Return request submitted for 3 out of 5 items.
Some items may have exceeded the return period (10 days limit).
```

## Business Rules

### Eligibility
- Only items with status "COMPLETED" can be returned
- 10-day return window from completion date
- Must provide a reason for return

### Constraints
- At least ONE item must be selected
- Cannot submit empty selection
- Expired items filtered out automatically

## Visual Design

### Dialog Layout
```
┌─────────────────────────────────────────┐
│  Request Return for: Bundle Order (5)  │
├─────────────────────────────────────────┤
│ Select items to return:                 │
│ ┌─────────────────────────────────────┐ │
│ │ [Select All] [Deselect All]         │ │
│ │                                     │ │
│ │ ☑ IT/Eng RTW Pants (Male) - L (1x) │ │
│ │ ☑ PE White Shirt - L (1x)          │ │
│ │ ☑ NSTP Gray Shirt (Male) - L (1x)  │ │
│ │ ☐ PE Blue Jogging Pants - M (1x)   │ │
│ │ ☑ IT/Eng Gray 3/4 Polo (Male) - L  │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Please provide a reason for the return: │
│ ┌─────────────────────────────────────┐ │
│ │ Wrong size for some items           │ │
│ │                                     │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ Note: Return requests must be approved │
│                                         │
│        [Submit Request] [Cancel]        │
└─────────────────────────────────────────┘
```

## Admin/Staff Perspective

### Impact on Approval Process
- Admin/Staff will see individual return requests for each selected item
- Each item appears separately in the "Return Requests" table
- Can approve/reject items independently
- Bundle ID links items together for reference

## Testing Scenarios

### Test Case 1: Return All Items
1. Create bundle order with 5 items
2. Complete order (mark as paid and picked up)
3. Click "Request Return"
4. Leave all checkboxes selected
5. Enter reason and submit
6. ✅ All 5 items should be marked "RETURN REQUESTED"

### Test Case 2: Partial Return
1. Create bundle order with 5 items
2. Complete order
3. Click "Request Return"
4. Uncheck 2 items
5. Enter reason and submit
6. ✅ 3 items should be "RETURN REQUESTED"
7. ✅ 2 items should remain "COMPLETED"

### Test Case 3: Validation
1. Create bundle order
2. Complete order
3. Click "Request Return"
4. Click "Deselect All"
5. Enter reason and submit
6. ✅ Error: "Please select at least one item to return."

### Test Case 4: Single Item Return
1. Create single item order (not bundle)
2. Complete order
3. Click "Request Return"
4. ✅ No checkboxes shown (direct return)
5. Enter reason and submit
6. ✅ Item marked "RETURN REQUESTED"

## Benefits

### For Students
- ✅ **Flexibility**: Return only problematic items
- ✅ **Efficiency**: Keep good items, return bad ones
- ✅ **Control**: Choose what to return
- ✅ **User-Friendly**: Clear checkbox interface

### For Business
- ✅ **Reduced Returns**: Students keep good items
- ✅ **Better Tracking**: Individual item return reasons
- ✅ **Improved Satisfaction**: Students have more control
- ✅ **Accurate Inventory**: Know exactly what's being returned

## Future Enhancements

### Potential Improvements
1. **Quantity Selection**: Allow returning partial quantities (e.g., 2 out of 5)
2. **Different Reasons**: Allow different reasons per item
3. **Preview**: Show estimated refund amount for selected items
4. **History**: Show which items were returned vs kept
5. **Bulk Actions**: "Return all damaged", "Return all wrong size"

## Related Features
- [Bundle Orders](STAFF_STOCK_MANAGEMENT.md)
- [Receipt System](../QUICK_START_GUIDE.md#receipts)
- [Return Policy](../QUICK_START_GUIDE.md#returns)

## Support
If you encounter issues with the selective return feature, please check:
1. Items must be marked "COMPLETED" to be returnable
2. Return period is 10 days from completion date
3. At least one item must be selected
4. All selected items must still be within return window
