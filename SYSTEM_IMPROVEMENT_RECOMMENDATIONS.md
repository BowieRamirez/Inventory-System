# STI ProWear Inventory System - Improvement Recommendations
**Date:** November 10, 2025  
**System Version:** 2.0.0  
**Branch:** beta-gui

---

## 🔴 CRITICAL MISSING FEATURES (Priority 1)

### 1. Actual Payment Deadline Implementation
**Current Issue:** You display the deadline message, but there's NO actual enforcement

**What's Missing:**
- No date/timestamp tracking for when orders were approved
- No automated job to check and cancel expired orders
- No countdown timer showing days remaining
- No filtering for "overdue" orders
- No database field to store `approvalDate` and `deadlineDate`

**How to Implement:**
```java
// Add to Reservation class
private LocalDateTime approvalDate;
private LocalDateTime paymentDeadline;

// When approving an order:
reservation.setApprovalDate(LocalDateTime.now());
reservation.setPaymentDeadline(LocalDateTime.now().plusDays(3));

// Scheduled task to check expired orders:
public void checkExpiredOrders() {
    reservationManager.getAllReservations().stream()
        .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()))
        .filter(r -> LocalDateTime.now().isAfter(r.getPaymentDeadline()))
        .forEach(r -> {
            r.setStatus("CANCELLED - PAYMENT DEADLINE EXPIRED");
            // Return stock to inventory
            inventoryManager.returnStock(r.getItemCode(), r.getQuantity());
            // Notify student
            notifyStudent(r.getStudentId(), "Order expired");
        });
}
```

**Estimated Effort:** 8-16 hours

---

### 2. Real-time Stock Validation
**Current Issue:** Multiple students could reserve the same item simultaneously

**What's Missing:**
- Concurrent reservation handling
- Stock locking mechanism during checkout
- Real-time inventory updates across all sessions
- Transaction rollback on failure

**How to Implement:**
```java
// Add synchronized stock reservation
public synchronized boolean reserveStock(int itemCode, int quantity) {
    Item item = getItem(itemCode);
    if (item.getQuantity() >= quantity) {
        item.setQuantity(item.getQuantity() - quantity);
        saveItems(); // Persist immediately
        return true;
    }
    return false;
}

// Add optimistic locking with version number
private int version; // In Item class
```

**Estimated Effort:** 4-8 hours

---

### 3. Audit Trail & Activity Logging
**Current Status:** Partial implementation via SystemLogger

**What's Missing:**
- Who approved/rejected what and when
- Payment transaction history with timestamps
- Item price change history
- Failed login attempts tracking
- Session management logs
- Data modification audit trail

**How to Implement:**
```java
// Enhanced audit log structure
public class AuditLog {
    private String timestamp;
    private String userId;
    private String userRole;
    private String action; // CREATE, UPDATE, DELETE, APPROVE, REJECT, etc.
    private String entityType; // RESERVATION, ITEM, STUDENT, etc.
    private String entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String reason;
}

// Usage:
auditLogger.log(
    currentUser.getId(),
    "APPROVE_RESERVATION",
    "RESERVATION",
    reservation.getId(),
    "Status: PENDING",
    "Status: APPROVED",
    "Complete uniform set approved"
);
```

**Estimated Effort:** 12-20 hours

---

### 4. Data Backup & Recovery
**Critical Gap:** All data in `.txt` files with no backup mechanism

**What's Missing:**
- Automated daily backups
- Database migration (SQLite or PostgreSQL)
- Data export/import functionality
- Disaster recovery plan
- Backup verification
- Point-in-time recovery

**How to Implement:**
```java
// Automated backup service
public class BackupService {
    private static final String BACKUP_DIR = "backups/";
    
    public void performDailyBackup() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );
        String backupFolder = BACKUP_DIR + "backup_" + timestamp + "/";
        
        // Copy all data files
        Files.copy(
            Paths.get("database/data/items.txt"),
            Paths.get(backupFolder + "items.txt")
        );
        // Repeat for all data files
        
        // Compress backup
        zipBackup(backupFolder);
        
        // Keep only last 30 days
        cleanOldBackups(30);
    }
}

// Schedule using Timer or ScheduledExecutorService
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(
    () -> backupService.performDailyBackup(),
    0, 24, TimeUnit.HOURS
);
```

**Recommended:** Migrate to SQLite first, then implement backups
```sql
-- SQLite auto-backup
PRAGMA auto_vacuum = FULL;
-- Or use .backup command
```

**Estimated Effort:** 20-40 hours (including DB migration)

---

## 🟡 IMPORTANT ENHANCEMENTS (Priority 2)

### 5. Advanced Search & Filtering
**Current:** Basic text search only

**What to Add:**
- Advanced filters (date range, price range, course, status)
- Sort by multiple columns
- Saved search queries
- Export filtered results to CSV/PDF
- Filter by size availability
- Filter by course/program

**How to Implement:**
```java
public class AdvancedSearchFilter {
    private String itemName;
    private String course;
    private String size;
    private Double minPrice;
    private Double maxPrice;
    private Integer minStock;
    private Integer maxStock;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<String> statuses;
    
    public List<Reservation> applyFilter(List<Reservation> reservations) {
        return reservations.stream()
            .filter(r -> matchesFilter(r))
            .collect(Collectors.toList());
    }
}

// UI: Add filter panel with ComboBoxes, DatePickers, TextFields
```

**Estimated Effort:** 8-12 hours

---

### 6. Reporting & Analytics Dashboard
**Missing Completely**

**What to Add:**
- Sales reports (daily/weekly/monthly)
- Popular items analysis
- Revenue tracking by course/period
- Student purchase history
- Inventory turnover rates
- Low stock alerts with reorder suggestions
- Seasonal trend analysis
- Export reports to PDF/Excel

**How to Implement:**
```java
public class SalesReport {
    public Map<String, Double> getDailyRevenue(LocalDate date) {
        return receipts.stream()
            .filter(r -> r.getDate().equals(date))
            .collect(Collectors.groupingBy(
                Receipt::getPaymentMethod,
                Collectors.summingDouble(Receipt::getAmount)
            ));
    }
    
    public List<ItemSalesData> getTopSellingItems(int limit) {
        return receipts.stream()
            .collect(Collectors.groupingBy(
                Receipt::getItemCode,
                Collectors.counting()
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(limit)
            .map(e -> new ItemSalesData(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
    }
}

// Create Charts using JavaFX Charts API
LineChart<String, Number> salesChart = new LineChart<>(xAxis, yAxis);
PieChart categoryDistribution = new PieChart();
BarChart<String, Number> monthlyRevenue = new BarChart<>(xAxis, yAxis);
```

**Estimated Effort:** 20-30 hours

---

### 7. Payment System Integration
**Current:** Manual payment method selection only

**What to Add:**
- QR code generation for GCash payments
- Payment verification/proof upload
- Automated receipt generation with QR codes
- Payment status webhooks
- Multiple payment gateways
- Partial payments support

**How to Implement:**
```java
// Use ZXing library for QR code generation
import com.google.zxing.*;
import com.google.zxing.qrcode.QRCodeWriter;

public class PaymentQRGenerator {
    public BufferedImage generateGCashQR(double amount, String referenceNo) {
        String qrContent = String.format(
            "gcash://pay?amount=%.2f&reference=%s&merchant=STI_PROWEAR",
            amount, referenceNo
        );
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(
            qrContent, 
            BarcodeFormat.QR_CODE, 
            300, 300
        );
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}

// Add payment proof upload
public class PaymentProof {
    private String receiptId;
    private String paymentMethod;
    private byte[] proofImage;
    private LocalDateTime uploadDate;
    private String verificationStatus; // PENDING, VERIFIED, REJECTED
}
```

**Add to pom.xml:**
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

**Estimated Effort:** 16-24 hours

---

### 8. Notification System
**Missing Completely**

**What to Add:**
- Email notifications for:
  - Order approval
  - Payment deadline reminders (24hrs before expiry)
  - Payment confirmation
  - Item ready for pickup
  - Stock availability alerts
  - Password reset
- SMS notifications (optional, via API)
- In-app notifications

**How to Implement:**
```java
// Using JavaMail API
import javax.mail.*;
import javax.mail.internet.*;

public class EmailNotificationService {
    private final String SMTP_HOST = "smtp.gmail.com";
    private final String SMTP_PORT = "587";
    private final String EMAIL = "stiprowear@gmail.com";
    private final String PASSWORD = "your_app_password";
    
    public void sendPaymentDeadlineReminder(Student student, Reservation reservation) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL, PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(student.getEmail())
            );
            message.setSubject("Payment Deadline Reminder - Order #" + reservation.getId());
            message.setText(
                "Dear " + student.getName() + ",\n\n" +
                "This is a reminder that your order payment is due in 24 hours.\n" +
                "Order ID: " + reservation.getId() + "\n" +
                "Total Amount: ₱" + reservation.getTotalPrice() + "\n" +
                "Deadline: " + reservation.getPaymentDeadline() + "\n\n" +
                "Please complete your payment to avoid cancellation.\n\n" +
                "Thank you,\nSTI ProWear Team"
            );
            
            Transport.send(message);
            SystemLogger.log("Email sent to " + student.getEmail());
        } catch (MessagingException e) {
            SystemLogger.logError("Failed to send email: " + e.getMessage());
        }
    }
}

// Schedule daily check
public void checkAndSendReminders() {
    LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
    reservationManager.getAllReservations().stream()
        .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()))
        .filter(r -> r.getPaymentDeadline().toLocalDate().equals(tomorrow.toLocalDate()))
        .forEach(r -> {
            Student student = getStudent(r.getStudentId());
            emailService.sendPaymentDeadlineReminder(student, r);
        });
}
```

**Add to pom.xml:**
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

**Estimated Effort:** 12-16 hours

---

### 9. Enhanced Student Features
**Current:** Students can only view and reserve

**What to Add:**
- Order history tracking
- Wishlist functionality
- Size recommendation based on previous orders
- Reorder previous purchases
- Cancel pending orders (before approval)
- Rate/review items
- Track order status in real-time
- Save favorite items

**How to Implement:**
```java
// Add to Student class
public class Student {
    // ... existing fields
    private List<Integer> wishlist; // Item codes
    private Map<String, String> sizePreferences; // itemType -> preferredSize
    
    public void addToWishlist(int itemCode) {
        if (!wishlist.contains(itemCode)) {
            wishlist.add(itemCode);
            saveWishlist();
        }
    }
    
    public List<Reservation> getOrderHistory() {
        return reservationManager.getAllReservations().stream()
            .filter(r -> r.getStudentId().equals(this.id))
            .sorted(Comparator.comparing(Reservation::getDateOrdered).reversed())
            .collect(Collectors.toList());
    }
    
    public String recommendSize(String itemType) {
        // Check previous orders for this item type
        return orderHistory.stream()
            .filter(r -> r.getItemType().equals(itemType))
            .map(Reservation::getSize)
            .findFirst()
            .orElse("M"); // Default
    }
}

// Student Dashboard additions
public Node createOrderHistoryView() {
    // Table showing: Date, Items, Status, Total, Actions (Reorder/Review)
}

public Node createWishlistView() {
    // Grid showing wishlist items with "Add to Cart" button
}
```

**Estimated Effort:** 16-24 hours

---

### 10. Advanced Inventory Management
**Current:** Basic add/edit/delete only

**What to Add:**
- Barcode/QR scanning for items
- Bulk import/export (CSV/Excel)
- Category/subcategory management
- Seasonal inventory planning
- Supplier management
- Purchase order tracking
- Stock transfer between locations
- Inventory valuation reports
- Dead stock identification

**How to Implement:**
```java
// Category Management
public class Category {
    private int id;
    private String name;
    private String description;
    private Category parent; // For subcategories
}

// Bulk Import from CSV
public void importItemsFromCSV(File csvFile) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            Item item = new Item();
            item.setCode(Integer.parseInt(values[0]));
            item.setName(values[1]);
            item.setCourse(values[2]);
            item.setSize(values[3]);
            item.setQuantity(Integer.parseInt(values[4]));
            item.setPrice(Double.parseDouble(values[5]));
            inventoryManager.addItem(item);
        }
    }
}

// Supplier Management
public class Supplier {
    private int id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private List<Integer> suppliedItemCodes;
}

// Purchase Order
public class PurchaseOrder {
    private String poNumber;
    private int supplierId;
    private LocalDate orderDate;
    private LocalDate expectedDelivery;
    private String status; // PENDING, CONFIRMED, DELIVERED, CANCELLED
    private List<PurchaseOrderItem> items;
    private double totalCost;
}
```

**Estimated Effort:** 24-40 hours

---

## 🟢 NICE-TO-HAVE FEATURES (Priority 3)

### 11. User Experience Enhancements

**What to Add:**
- Dark/light theme toggle (you already have ThemeManager!)
- Keyboard shortcuts (Ctrl+S to save, Esc to close, etc.)
- Drag-and-drop for reordering items
- Image upload for items
- Print receipts directly
- Multi-language support (English/Filipino)
- Customizable dashboard widgets
- Recently viewed items
- Quick actions menu

**How to Implement:**
```java
// Activate existing ThemeManager
public void initializeThemeToggle() {
    ToggleButton themeToggle = new ToggleButton("🌙");
    themeToggle.setOnAction(e -> {
        if (themeToggle.isSelected()) {
            ThemeManager.applyTheme(scene, ThemeManager.Theme.DARK);
            themeToggle.setText("☀️");
        } else {
            ThemeManager.applyTheme(scene, ThemeManager.Theme.LIGHT);
            themeToggle.setText("🌙");
        }
    });
}

// Keyboard shortcuts
scene.setOnKeyPressed(e -> {
    if (e.getCode() == KeyCode.S && e.isControlDown()) {
        handleSave();
        e.consume();
    } else if (e.getCode() == KeyCode.ESCAPE) {
        handleCancel();
        e.consume();
    } else if (e.getCode() == KeyCode.F && e.isControlDown()) {
        searchField.requestFocus();
        e.consume();
    }
});

// Print receipt
import javafx.print.*;

public void printReceipt(Receipt receipt) {
    PrinterJob job = PrinterJob.createPrinterJob();
    if (job != null && job.showPrintDialog(stage)) {
        Node receiptNode = createReceiptNode(receipt);
        boolean success = job.printPage(receiptNode);
        if (success) {
            job.endJob();
        }
    }
}
```

**Estimated Effort:** 12-20 hours

---

### 12. Security Enhancements
**Current:** Basic password authentication

**What to Add:**
- Password complexity requirements (min length, special chars, numbers)
- Password expiry & forced reset every 90 days
- Two-factor authentication (2FA) via email/SMS
- Session timeout after inactivity
- IP whitelist for admin access
- Encryption for sensitive data (passwords, payment info)
- Role-based permissions (granular - not just Admin/Staff/Cashier)
- Security audit logs
- Prevent SQL injection (when migrating to DB)
- CAPTCHA for login after failed attempts

**How to Implement:**
```java
// Password complexity
public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final String SPECIAL_CHARS = "!@#$%^&*()";
    
    public static boolean isValid(String password) {
        if (password.length() < MIN_LENGTH) return false;
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars()
            .anyMatch(c -> SPECIAL_CHARS.indexOf(c) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}

// Password hashing (use BCrypt instead of plain text!)
import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }
    
    public static boolean checkPassword(String plainPassword, String hashed) {
        return BCrypt.checkpw(plainPassword, hashed);
    }
}

// Session management
public class SessionManager {
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes
    private Map<String, UserSession> activeSessions = new HashMap<>();
    
    public void startSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        UserSession session = new UserSession(
            sessionId, 
            user, 
            System.currentTimeMillis()
        );
        activeSessions.put(sessionId, session);
        
        // Start timeout checker
        scheduleSessionTimeout(sessionId);
    }
    
    public void updateActivity(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session != null) {
            session.setLastActivity(System.currentTimeMillis());
        }
    }
    
    public boolean isSessionValid(String sessionId) {
        UserSession session = activeSessions.get(sessionId);
        if (session == null) return false;
        
        long elapsed = System.currentTimeMillis() - session.getLastActivity();
        return elapsed < SESSION_TIMEOUT;
    }
}

// 2FA via Email
public class TwoFactorAuth {
    public String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
    
    public void sendOTP(String email, String otp) {
        emailService.send(email, "Your OTP", "Your code is: " + otp);
    }
    
    public boolean verifyOTP(String userOTP, String storedOTP) {
        return userOTP.equals(storedOTP);
    }
}
```

**Add to pom.xml:**
```xml
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>
```

**Estimated Effort:** 20-30 hours

---

### 13. Performance Optimization
**Current:** Loading all data at once

**What to Add:**
- Pagination for large tables (load 50 rows at a time)
- Lazy loading of images
- Caching frequently accessed data
- Background tasks for heavy operations
- Database indexing (when migrated)
- Connection pooling
- Async loading with progress indicators

**How to Implement:**
```java
// Pagination
public class PaginatedTableView<T> {
    private int itemsPerPage = 50;
    private int currentPage = 0;
    private List<T> allData;
    
    public ObservableList<T> getPageData() {
        int fromIndex = currentPage * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allData.size());
        return FXCollections.observableArrayList(
            allData.subList(fromIndex, toIndex)
        );
    }
    
    public void nextPage() {
        if ((currentPage + 1) * itemsPerPage < allData.size()) {
            currentPage++;
            table.setItems(getPageData());
        }
    }
    
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            table.setItems(getPageData());
        }
    }
}

// Caching
public class CacheManager {
    private Map<String, CachedData> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes
    
    public <T> T get(String key, Supplier<T> dataLoader) {
        CachedData cached = cache.get(key);
        
        if (cached != null && !cached.isExpired()) {
            return (T) cached.getData();
        }
        
        T data = dataLoader.get();
        cache.put(key, new CachedData(data, System.currentTimeMillis()));
        return data;
    }
}

// Async loading
public void loadDataAsync() {
    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setVisible(true);
    
    Task<List<Item>> loadTask = new Task<>() {
        @Override
        protected List<Item> call() throws Exception {
            return inventoryManager.getAllItems();
        }
    };
    
    loadTask.setOnSucceeded(e -> {
        table.setItems(FXCollections.observableArrayList(loadTask.getValue()));
        progressIndicator.setVisible(false);
    });
    
    loadTask.setOnFailed(e -> {
        progressIndicator.setVisible(false);
        AlertHelper.showError("Failed to load data", loadTask.getException().getMessage());
    });
    
    new Thread(loadTask).start();
}
```

**Estimated Effort:** 12-20 hours

---

### 14. Enhanced Data Validation
**Current:** Basic validation via InputValidator

**What to Add:**
- Real-time form validation with visual feedback
- Duplicate detection (same student ordering same item)
- Size availability checking before reservation
- Price range validation
- Quantity limits per student (e.g., max 5 items per order)
- Email format validation
- Phone number format validation
- Student ID format validation

**How to Implement:**
```java
// Real-time validation
public void setupFormValidation(TextField textField, Predicate<String> validator) {
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (validator.test(newVal)) {
            textField.setStyle("-fx-border-color: green;");
        } else {
            textField.setStyle("-fx-border-color: red;");
        }
    });
}

// Duplicate order detection
public boolean hasDuplicateOrder(String studentId, int itemCode) {
    return reservationManager.getAllReservations().stream()
        .filter(r -> r.getStudentId().equals(studentId))
        .filter(r -> r.getItemCode() == itemCode)
        .filter(r -> !r.getStatus().equals("COMPLETED"))
        .filter(r -> !r.getStatus().equals("CANCELLED"))
        .findAny()
        .isPresent();
}

// Quantity limits
public class StudentOrderValidator {
    private static final int MAX_ITEMS_PER_ORDER = 5;
    private static final int MAX_PENDING_ORDERS = 3;
    
    public boolean canPlaceOrder(String studentId, int quantity) {
        long pendingOrders = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStudentId().equals(studentId))
            .filter(r -> "PENDING".equals(r.getStatus()))
            .count();
        
        if (pendingOrders >= MAX_PENDING_ORDERS) {
            return false;
        }
        
        if (quantity > MAX_ITEMS_PER_ORDER) {
            return false;
        }
        
        return true;
    }
}

// Email validation
public boolean isValidEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    Pattern pattern = Pattern.compile(emailRegex);
    return pattern.matcher(email).matches();
}
```

**Estimated Effort:** 8-12 hours

---

### 15. Advanced Bundle Features
**Current:** Basic bundle support

**What to Add:**
- Pre-defined bundle packages (e.g., "Complete PE Uniform")
- Bundle discounts (10% off when buying 3+ items)
- Minimum/maximum items per bundle
- Bundle templates by course/year level
- Custom bundle creation by students
- Bundle popularity tracking
- Seasonal bundle promotions

**How to Implement:**
```java
public class BundleTemplate {
    private String id;
    private String name;
    private String description;
    private String course;
    private int yearLevel;
    private List<BundleItem> requiredItems;
    private double discountPercentage;
    private boolean isActive;
    
    public double calculateBundlePrice() {
        double originalPrice = requiredItems.stream()
            .mapToDouble(bi -> bi.getItem().getPrice() * bi.getQuantity())
            .sum();
        return originalPrice * (1 - discountPercentage / 100);
    }
}

public class BundleItem {
    private Item item;
    private int quantity;
    private boolean isOptional;
}

// Pre-defined bundles
public void createDefaultBundles() {
    BundleTemplate peUniform = new BundleTemplate();
    peUniform.setName("Complete PE Uniform Set");
    peUniform.setCourse("ALL");
    peUniform.setDiscountPercentage(10);
    peUniform.addRequiredItem(getItem("PE White Shirt"), 1);
    peUniform.addRequiredItem(getItem("PE Pants"), 1);
    peUniform.addRequiredItem(getItem("STI Pin"), 1);
    
    bundleTemplateManager.save(peUniform);
}

// Apply bundle discount
public double calculateDiscount(List<Reservation> bundleItems) {
    if (bundleItems.size() >= 3) {
        double total = bundleItems.stream()
            .mapToDouble(Reservation::getTotalPrice)
            .sum();
        return total * 0.10; // 10% discount
    }
    return 0;
}
```

**Estimated Effort:** 12-16 hours

---

## 📊 SPECIFIC CODE IMPROVEMENTS

### 16. Error Handling
**Current:** Basic try-catch blocks

**What to Improve:**
```java
// Create custom exceptions
public class InsufficientStockException extends Exception {
    private int itemCode;
    private int requested;
    private int available;
    
    public InsufficientStockException(int itemCode, int requested, int available) {
        super(String.format(
            "Insufficient stock for item %d. Requested: %d, Available: %d",
            itemCode, requested, available
        ));
        this.itemCode = itemCode;
        this.requested = requested;
        this.available = available;
    }
}

public class PaymentDeadlineExpiredException extends Exception {
    public PaymentDeadlineExpiredException(String orderId) {
        super("Payment deadline has expired for order: " + orderId);
    }
}

public class DuplicateReservationException extends Exception {
    public DuplicateReservationException(String studentId, int itemCode) {
        super(String.format(
            "Student %s already has a pending order for item %d",
            studentId, itemCode
        ));
    }
}

// Global exception handler
public class GlobalExceptionHandler {
    public static void handle(Exception e) {
        if (e instanceof InsufficientStockException) {
            InsufficientStockException ise = (InsufficientStockException) e;
            AlertHelper.showWarning(
                "Stock Unavailable",
                String.format("Only %d units available", ise.getAvailable())
            );
        } else if (e instanceof PaymentDeadlineExpiredException) {
            AlertHelper.showError("Order Expired", e.getMessage());
        } else {
            SystemLogger.logError("Unexpected error: " + e.getMessage());
            AlertHelper.showError("System Error", "Please contact administrator");
        }
    }
}

// Retry mechanism for critical operations
public <T> T retryOperation(Supplier<T> operation, int maxRetries) {
    int attempts = 0;
    Exception lastException = null;
    
    while (attempts < maxRetries) {
        try {
            return operation.get();
        } catch (Exception e) {
            lastException = e;
            attempts++;
            try {
                Thread.sleep(1000 * attempts); // Exponential backoff
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    throw new RuntimeException("Operation failed after " + maxRetries + " attempts", lastException);
}
```

**Estimated Effort:** 6-8 hours

---

### 17. Code Structure Refactoring
**Current:** UI logic mixed with business logic

**What to Improve:**

**Service Layer Pattern:**
```java
// Business logic layer
public class ReservationService {
    private ReservationManager reservationManager;
    private InventoryManager inventoryManager;
    private EmailNotificationService emailService;
    
    public Reservation createReservation(CreateReservationRequest request) 
            throws InsufficientStockException, DuplicateReservationException {
        
        // Validation
        if (hasDuplicateOrder(request.getStudentId(), request.getItemCode())) {
            throw new DuplicateReservationException(
                request.getStudentId(), 
                request.getItemCode()
            );
        }
        
        // Reserve stock
        boolean stockReserved = inventoryManager.reserveStock(
            request.getItemCode(), 
            request.getQuantity()
        );
        
        if (!stockReserved) {
            throw new InsufficientStockException(
                request.getItemCode(),
                request.getQuantity(),
                inventoryManager.getAvailableStock(request.getItemCode())
            );
        }
        
        // Create reservation
        Reservation reservation = reservationManager.createReservation(request);
        
        // Send notification
        emailService.sendOrderConfirmation(request.getStudentId(), reservation);
        
        return reservation;
    }
}

// Controllers only handle UI
public class ReservationController {
    private ReservationService reservationService;
    
    private void handleCreateReservation() {
        CreateReservationRequest request = buildRequestFromForm();
        
        try {
            Reservation reservation = reservationService.createReservation(request);
            AlertHelper.showSuccess("Order Created", "Order ID: " + reservation.getId());
            refreshTable();
        } catch (InsufficientStockException e) {
            AlertHelper.showWarning("Stock Unavailable", e.getMessage());
        } catch (DuplicateReservationException e) {
            AlertHelper.showWarning("Duplicate Order", e.getMessage());
        }
    }
}
```

**Repository Pattern:**
```java
public interface ReservationRepository {
    List<Reservation> findAll();
    Optional<Reservation> findById(int id);
    List<Reservation> findByStudentId(String studentId);
    List<Reservation> findByStatus(String status);
    Reservation save(Reservation reservation);
    void delete(int id);
}

public class FileReservationRepository implements ReservationRepository {
    private static final String FILE_PATH = "database/data/reservations.txt";
    
    @Override
    public List<Reservation> findAll() {
        // Read from file
    }
    
    @Override
    public Reservation save(Reservation reservation) {
        // Save to file
    }
}

// Future: SQLite implementation
public class SQLiteReservationRepository implements ReservationRepository {
    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservations";
        // Execute query
    }
}
```

**Dependency Injection:**
```java
// Instead of: new ReservationManager()
// Use:
public class ServiceLocator {
    private static Map<Class<?>, Object> services = new HashMap<>();
    
    public static <T> void register(Class<T> serviceClass, T implementation) {
        services.put(serviceClass, implementation);
    }
    
    public static <T> T get(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }
}

// Initialize services at startup
public class Application {
    public void initialize() {
        ReservationRepository repo = new FileReservationRepository();
        ReservationManager manager = new ReservationManager(repo);
        ReservationService service = new ReservationService(manager);
        
        ServiceLocator.register(ReservationService.class, service);
    }
}

// Use in controllers
public class DashboardController {
    private ReservationService reservationService = 
        ServiceLocator.get(ReservationService.class);
}
```

**Estimated Effort:** 40-60 hours (major refactoring)

---

### 18. Database Migration Plan
**Current:** Text files (.txt)

**Recommended:** Migrate to SQLite

**Why SQLite:**
- No server needed (embedded database)
- Single file database
- ACID compliant
- Better performance than text files
- Built-in transactions
- SQL queries for complex filtering
- Concurrent access handling

**Migration Steps:**

**Step 1: Add Dependencies**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.0.0</version>
</dependency>
```

**Step 2: Create Database Schema**
```sql
-- schema.sql
CREATE TABLE IF NOT EXISTS items (
    code INTEGER PRIMARY KEY,
    name TEXT NOT NULL,
    course TEXT NOT NULL,
    size TEXT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    price REAL NOT NULL,
    category TEXT,
    supplier_id INTEGER,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    course TEXT,
    year_level INTEGER,
    phone TEXT,
    address TEXT,
    is_active BOOLEAN DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reservations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    student_id TEXT NOT NULL,
    student_name TEXT NOT NULL,
    item_code INTEGER NOT NULL,
    item_name TEXT NOT NULL,
    size TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    total_price REAL NOT NULL,
    status TEXT NOT NULL,
    bundle_id TEXT,
    is_part_of_bundle BOOLEAN DEFAULT 0,
    date_ordered TEXT NOT NULL,
    approval_date TEXT,
    payment_deadline TEXT,
    paid_date TEXT,
    completed_date TEXT,
    reason TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id),
    FOREIGN KEY (item_code) REFERENCES items(code)
);

CREATE TABLE IF NOT EXISTS receipts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    reservation_id INTEGER NOT NULL,
    buyer_name TEXT NOT NULL,
    item_code INTEGER NOT NULL,
    item_name TEXT NOT NULL,
    size TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    amount REAL NOT NULL,
    payment_method TEXT NOT NULL,
    payment_status TEXT NOT NULL,
    bundle_id TEXT,
    date_ordered TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

CREATE TABLE IF NOT EXISTS staff (
    username TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    password TEXT NOT NULL,
    role TEXT NOT NULL,
    email TEXT UNIQUE,
    is_active BOOLEAN DEFAULT 1,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    timestamp TEXT NOT NULL,
    user_id TEXT NOT NULL,
    user_role TEXT NOT NULL,
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    ip_address TEXT
);

-- Indexes for better performance
CREATE INDEX idx_reservations_student ON reservations(student_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_bundle ON reservations(bundle_id);
CREATE INDEX idx_receipts_date ON receipts(date_ordered);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
```

**Step 3: Database Connection Manager**
```java
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:database/prowear.db";
    private static Connection connection;
    
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(false); // Enable transactions
        }
        return connection;
    }
    
    public static void initializeDatabase() {
        try {
            Connection conn = getConnection();
            String schema = new String(Files.readAllBytes(
                Paths.get("database/schema.sql")
            ));
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(schema);
            conn.commit();
            
            SystemLogger.log("Database initialized successfully");
        } catch (Exception e) {
            SystemLogger.logError("Failed to initialize database: " + e.getMessage());
        }
    }
}
```

**Step 4: Migrate Existing Data**
```java
public class DataMigration {
    public void migrateFromTextFiles() {
        try {
            migrateItems();
            migrateStudents();
            migrateStaff();
            migrateReservations();
            migrateReceipts();
            
            SystemLogger.log("Data migration completed successfully");
        } catch (Exception e) {
            SystemLogger.logError("Migration failed: " + e.getMessage());
        }
    }
    
    private void migrateItems() throws Exception {
        List<String> lines = Files.readAllLines(
            Paths.get("database/data/items.txt")
        );
        
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO items (code, name, course, size, quantity, price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        
        for (String line : lines) {
            String[] parts = line.split(",");
            pstmt.setInt(1, Integer.parseInt(parts[0]));
            pstmt.setString(2, parts[1]);
            pstmt.setString(3, parts[2]);
            pstmt.setString(4, parts[3]);
            pstmt.setInt(5, Integer.parseInt(parts[4]));
            pstmt.setDouble(6, Double.parseDouble(parts[5]));
            pstmt.addBatch();
        }
        
        pstmt.executeBatch();
        conn.commit();
    }
    
    // Similar methods for other tables...
}
```

**Step 5: Update Repository Implementations**
```java
public class SQLiteReservationRepository implements ReservationRepository {
    
    @Override
    public List<Reservation> findAll() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations ORDER BY date_ordered DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            SystemLogger.logError("Failed to fetch reservations: " + e.getMessage());
        }
        
        return reservations;
    }
    
    @Override
    public Reservation save(Reservation reservation) {
        String sql = reservation.getReservationId() == 0 ?
            "INSERT INTO reservations (student_id, student_name, item_code, ...) VALUES (?, ?, ?, ...)" :
            "UPDATE reservations SET student_id=?, student_name=?, ... WHERE id=?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, reservation.getStudentId());
            pstmt.setString(2, reservation.getStudentName());
            // ... set all fields
            
            pstmt.executeUpdate();
            conn.commit();
            
            if (reservation.getReservationId() == 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    reservation.setReservationId(generatedKeys.getInt(1));
                }
            }
            
            return reservation;
        } catch (SQLException e) {
            SystemLogger.logError("Failed to save reservation: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public List<Reservation> findByStatus(String status) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE status = ? ORDER BY date_ordered DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            SystemLogger.logError("Failed to fetch reservations by status: " + e.getMessage());
        }
        
        return reservations;
    }
}
```

**Estimated Effort:** 40-60 hours

---

## 🎯 PRIORITY IMPLEMENTATION ROADMAP

### Phase 1 (Weeks 1-2): Critical Foundation
**Total: ~60-80 hours**

1. **Database Migration** (40-60 hours)
   - Set up SQLite
   - Create schema
   - Migrate existing data
   - Update all repositories
   - Test thoroughly

2. **Automated Backups** (8-12 hours)
   - Implement backup service
   - Schedule daily backups
   - Test restore process

3. **Payment Deadline Enforcement** (8-16 hours)
   - Add date fields to reservations
   - Implement deadline checker
   - Add scheduled task
   - Test cancellation logic

### Phase 2 (Weeks 3-4): Security & Reliability
**Total: ~50-70 hours**

1. **Enhanced Security** (20-30 hours)
   - Password hashing with BCrypt
   - Password complexity validation
   - Session management
   - Security audit logs

2. **Comprehensive Audit Trail** (12-20 hours)
   - Enhanced logging system
   - Action tracking
   - Report generation

3. **Stock Validation** (4-8 hours)
   - Synchronized reservations
   - Real-time stock checking

4. **Error Handling** (6-8 hours)
   - Custom exceptions
   - Global error handler
   - User-friendly messages

5. **Unit Testing** (8-12 hours)
   - Set up JUnit
   - Test critical functions
   - Aim for 50% coverage

### Phase 3 (Weeks 5-6): User Experience
**Total: ~60-80 hours**

1. **Notification System** (12-16 hours)
   - Email integration
   - Payment reminders
   - Status updates

2. **Reporting Dashboard** (20-30 hours)
   - Sales reports
   - Analytics charts
   - Export functionality

3. **Advanced Search** (8-12 hours)
   - Multi-field filtering
   - Saved queries
   - Export results

4. **Enhanced Student Features** (16-24 hours)
   - Order history
   - Wishlist
   - Size recommendations
   - Reorder function

5. **UI Improvements** (4-8 hours)
   - Theme toggle
   - Keyboard shortcuts
   - Print receipts

### Phase 4 (Weeks 7-8): Advanced Features
**Total: ~40-60 hours**

1. **Payment Integration** (16-24 hours)
   - QR code generation
   - Payment proof upload
   - Verification system

2. **Advanced Inventory** (16-24 hours)
   - Bulk import/export
   - Category management
   - Supplier tracking

3. **Bundle Enhancements** (8-12 hours)
   - Pre-defined bundles
   - Discount system
   - Templates

### Phase 5 (Ongoing): Optimization & Testing
**Total: ~30-40 hours**

1. **Performance Optimization** (12-20 hours)
   - Pagination
   - Caching
   - Async loading

2. **Code Refactoring** (12-16 hours)
   - Service layer
   - Dependency injection
   - Clean architecture

3. **Comprehensive Testing** (6-10 hours)
   - Integration tests
   - User acceptance testing
   - Bug fixes

---

## 📝 TESTING RECOMMENDATIONS

### Unit Tests (Create these!)
```java
// Example test structure
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationServiceTest {
    
    @Test
    public void testCreateReservation_Success() {
        // Arrange
        ReservationService service = new ReservationService();
        CreateReservationRequest request = new CreateReservationRequest();
        request.setStudentId("2000296625");
        request.setItemCode(101);
        request.setQuantity(1);
        
        // Act
        Reservation result = service.createReservation(request);
        
        // Assert
        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
    }
    
    @Test
    public void testCreateReservation_InsufficientStock() {
        // Test that exception is thrown when stock unavailable
        assertThrows(InsufficientStockException.class, () -> {
            service.createReservation(requestWithTooMuchQuantity);
        });
    }
}
```

**Add to pom.xml:**
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>
```

---

## 🚀 QUICK WINS TO IMPLEMENT TODAY

These can be done in 1-2 hours each:

1. **Add Confirmation Dialogs** - Before deleting items/cancelling orders
2. **Show Version Number** - Display in footer "v2.0.0"
3. **Add Tooltips** - Explain what each button does
4. **Keyboard Shortcuts** - Esc to close dialogs
5. **Export to CSV** - Add button on tables
6. **Loading Indicators** - Show spinner during operations
7. **Print Receipt** - Direct print from receipt view
8. **Recently Viewed** - Track last 5 items viewed
9. **Quick Search** - Ctrl+F to focus search field
10. **Dark Theme Toggle** - Activate your ThemeManager!

---

## 📚 RECOMMENDED LIBRARIES TO ADD

```xml
<!-- Add these to pom.xml -->

<!-- For QR Code Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>

<!-- For Email Notifications -->
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>

<!-- For Password Hashing -->
<dependency>
    <groupId>org.mindrot</groupId>
    <artifactId>jbcrypt</artifactId>
    <version>0.4</version>
</dependency>

<!-- For SQLite Database -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.43.0.0</version>
</dependency>

<!-- For Excel Export -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>

<!-- For PDF Generation -->
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>8.0.2</version>
    <type>pom</type>
</dependency>

<!-- For Unit Testing -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<!-- For Mocking in Tests -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.5.0</version>
    <scope>test</scope>
</dependency>
```

---

## 💡 FINAL RECOMMENDATIONS

**Immediate Actions (This Week):**
1. Implement actual payment deadline enforcement
2. Set up automated backups
3. Add unit tests for critical functions
4. Hash passwords with BCrypt
5. Add confirmation dialogs everywhere

**Short Term (This Month):**
1. Migrate to SQLite database
2. Implement email notifications
3. Add comprehensive audit logging
4. Create reporting dashboard
5. Improve error handling

**Long Term (Next 3 Months):**
1. Payment gateway integration
2. Mobile app development
3. Advanced analytics
4. Multi-location support
5. API for integrations

**Remember:**
- **Test everything thoroughly**
- **Keep backups before major changes**
- **Document all new features**
- **Get user feedback early**
- **Deploy incrementally**

---

**Total Estimated Development Time:**
- Phase 1-2 (Critical): 110-150 hours (3-4 months part-time)
- Phase 3-4 (Enhanced): 100-140 hours (2-3 months part-time)
- Phase 5 (Polish): 30-40 hours (ongoing)

**Grand Total: 240-330 hours** of development work

---

## 📧 SUPPORT & QUESTIONS

If you need help implementing any of these features, I can:
- Provide detailed code examples
- Help debug issues
- Review your implementations
- Suggest best practices
- Create documentation

Just ask! 🚀

---

**Document Version:** 1.0  
**Last Updated:** November 10, 2025  
**Prepared for:** STI ProWear Inventory System Development Team
