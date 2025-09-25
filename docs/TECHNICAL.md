# STI Merch System - Technical Documentation

## Class Hierarchy

### Core Classes
- **MerchSystem**: Main application controller
- **User**: Abstract base class for authentication
  - **Admin**: Administrative user with full system access
  - **Student**: Student user with limited access

### Data Models
- **Item**: Represents inventory items (uniforms, merchandise)
- **Reservation**: Represents student reservation requests

### Managers
- **InventoryManager**: Handles all inventory operations
- **ReservationManager**: Manages reservation lifecycle

### User Interfaces
- **AdminInterface**: Administrative menu system
- **StudentInterface**: Student menu system

## Design Patterns Used

1. **Template Method Pattern**: User authentication
2. **Manager Pattern**: Separation of business logic
3. **Interface Segregation**: Separate admin/student interfaces

## Key Features Implementation

### Authentication
- Simple credential validation for admin
- Student authentication with course validation
- Role-based access control

### Inventory Management
- Real-time stock tracking
- Course-specific item filtering
- Automatic quantity updates on reservations

### Reservation System
- Status workflow: PENDING → APPROVED → COMPLETED → CANCELLED
- Timestamp tracking
- Student-specific reservation views

## Database Simulation
The system simulates a database using in-memory collections:
- `List<Item>` for inventory
- `List<Reservation>` for reservations
- Pre-populated with realistic test data

## Error Handling
- Input validation for all user inputs
- Graceful handling of invalid menu choices
- Proper error messages for business rule violations

## Future Enhancements
1. File-based persistence
2. Database integration
3. Email notifications
4. Receipt generation
5. Reporting system