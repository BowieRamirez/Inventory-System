# Stock Logs Role-Based Views - Quick Reference

## Admin Dashboard - Stock Logs View

### What Admin Sees
```
╔══════════════════════════════════════════════════════════════════════════════════╗
║                            STOCK LOGS (Admin View)                               ║
╠══════════════════════════════════════════════════════════════════════════════════╣
║ Timestamp          │ Staff/Admin    │ Code │ Item           │ Size │ Change     ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 15:30   │ admin_john     │ 101  │ BSIT Shirt     │ M    │ +5/50      ║
║                    │ STAFF_RETURN                                                ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 14:20   │ staff_mary     │ 202  │ CAS Hoodie     │ L    │ +10/100    ║
║                    │ ITEM_ADDED                                                  ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 13:15   │ admin_john     │ 303  │ SHS Pin        │ OS   │ -20/30     ║
║                    │ ITEM_UPDATED                                                ║
╚══════════════════════════════════════════════════════════════════════════════════╝
```

### Admin View Includes:
- ✅ **STAFF_RETURN** - Staff returning items to inventory
- ✅ **ITEM_ADDED** - New items added by staff/admin
- ✅ **ITEM_DELETED** - Items removed by staff/admin
- ✅ **ITEM_UPDATED** - Items updated by staff/admin

### Admin View Excludes:
- ❌ **USER_PICKUP** - Customer pickups (not relevant for inventory management)
- ❌ **USER_RETURN** - Customer returns (handled by staff)

---

## Staff Dashboard - Stock Logs View

### What Staff Sees
```
╔══════════════════════════════════════════════════════════════════════════════════╗
║                            STOCK LOGS (Staff View)                               ║
╠══════════════════════════════════════════════════════════════════════════════════╣
║ Timestamp          │ Student/User   │ Code │ Item           │ Size │ Change     ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 16:45   │ Juan Dela Cruz │ 101  │ BSIT Shirt     │ M    │ -1/49      ║
║                    │ USER_PICKUP - Student picked up completed order             ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 15:30   │ Maria Santos   │ 202  │ CAS Hoodie     │ L    │ +1/100     ║
║                    │ USER_RETURN - Wrong size returned                          ║
║────────────────────┼────────────────┼──────┼────────────────┼──────┼────────────║
║ 2025-01-08 14:20   │ Pedro Garcia   │ 303  │ SHS Pin        │ OS   │ -2/98      ║
║                    │ USER_PICKUP - Student picked up items                      ║
╚══════════════════════════════════════════════════════════════════════════════════╝
```

### Staff View Includes:
- ✅ **USER_PICKUP** - Students picking up completed orders
- ✅ **USER_RETURN** - Students returning items

### Staff View Excludes:
- ❌ **STAFF_RETURN** - Staff inventory actions (admin responsibility)
- ❌ **ITEM_ADDED** - Inventory additions (admin responsibility)
- ❌ **ITEM_DELETED** - Inventory removals (admin responsibility)
- ❌ **ITEM_UPDATED** - Inventory updates (admin responsibility)

---

## Key Differences

| Aspect                | Admin View                          | Staff View                       |
|-----------------------|-------------------------------------|----------------------------------|
| **Focus**             | Inventory Management                | Customer Service                 |
| **Primary Actions**   | Staff changes to stock              | User pickups and returns         |
| **Column Header**     | "Staff/Admin"                       | "Student/User"                   |
| **Use Case**          | Track staff activities              | Track customer activities        |
| **Audit Purpose**     | Staff accountability                | Customer service monitoring      |

---

## Benefits of Separation

### Admin Benefits
1. **Focused Auditing**: Only see staff inventory changes
2. **Clear Accountability**: Track which staff modified inventory
3. **Inventory Control**: Monitor stock adjustments and corrections
4. **Less Clutter**: No customer pickup/return noise

### Staff Benefits
1. **Customer Focus**: Only see user-related activities
2. **Service Monitoring**: Track pickup and return trends
3. **Better Context**: Understand customer behavior patterns
4. **Less Clutter**: No admin inventory management noise

---

## Example Scenarios

### Admin Scenario
**Task**: "Which staff member added 100 BSIT shirts yesterday?"
- Admin opens Stock Logs
- Sees only ITEM_ADDED, ITEM_UPDATED, etc.
- Quickly finds the staff member who added the items
- Can verify quantity and details

### Staff Scenario
**Task**: "Did Juan Dela Cruz pick up his order?"
- Staff opens Stock Logs
- Sees only USER_PICKUP and USER_RETURN
- Quickly finds Juan's pickup activity
- Can confirm pickup time and items

---

## Summary

This separation ensures each role sees only relevant information:
- **Admin**: Inventory management and staff activities
- **Staff**: Customer service and user activities

This reduces information overload and improves efficiency for both roles.
