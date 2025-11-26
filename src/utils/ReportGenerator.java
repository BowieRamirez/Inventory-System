package utils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import inventory.InventoryManager;
import inventory.Item;
import inventory.Receipt;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.ReservationManager;
import student.Student;

/**
 * ReportGenerator - Aggregates data for reporting and analytics
 */
public class ReportGenerator {
    
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    
    public ReportGenerator(InventoryManager inventoryManager,
                          ReservationManager reservationManager,
                          ReceiptManager receiptManager) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
        this.receiptManager = receiptManager;
    }
    
    // ==================== STOCK REPORTS ====================
    
    /**
     * Get stock levels by course
     */
    public List<StockReport> getStockByCourse() {
        Map<String, Integer> stockByCourse = new HashMap<>();
        
        for (Item item : inventoryManager.getAllItems()) {
            String course = item.getCourse();
            int stock = item.getQuantity();
            stockByCourse.put(course, stockByCourse.getOrDefault(course, 0) + stock);
        }
        
        return stockByCourse.entrySet().stream()
                .map(e -> new StockReport(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get detailed item list grouped by course (for detailed PDF export)
     */
    public Map<String, List<DetailedItemReport>> getDetailedItemsByCourse() {
        Map<String, List<DetailedItemReport>> itemsByCourse = new LinkedHashMap<>();
        
        for (Item item : inventoryManager.getAllItems()) {
            String course = item.getCourse();
            DetailedItemReport report = new DetailedItemReport(
                item.getCode(),
                item.getName(),
                item.getCourse(),
                item.getSize(),
                item.getQuantity(),
                item.getPrice()
            );
            
            itemsByCourse.computeIfAbsent(course, k -> new ArrayList<>()).add(report);
        }
        
        return itemsByCourse;
    }
    
    /**
     * Get all items as detailed reports
     */
    public List<DetailedItemReport> getAllDetailedItems() {
        return inventoryManager.getAllItems().stream()
                .map(item -> new DetailedItemReport(
                    item.getCode(),
                    item.getName(),
                    item.getCourse(),
                    item.getSize(),
                    item.getQuantity(),
                    item.getPrice()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get low stock items (below threshold)
     */
    public List<StockReport> getLowStockItems(int threshold) {
        return inventoryManager.getAllItems().stream()
                .filter(item -> item.getQuantity() > 0 && item.getQuantity() <= threshold)
                .map(item -> new StockReport(item.getName(), item.getQuantity()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get out of stock items
     */
    public List<StockReport> getOutOfStockItems() {
        return inventoryManager.getAllItems().stream()
                .filter(item -> item.getQuantity() == 0)
                .map(item -> new StockReport(item.getName(), 0))
                .collect(Collectors.toList());
    }
    
    /**
     * Get stock valuation report
     */
    public List<StockValuationReport> getStockValuation() {
        return inventoryManager.getAllItems().stream()
                .map(item -> new StockValuationReport(
                    item.getName(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getQuantity() * item.getPrice()
                ))
                .collect(Collectors.toList());
    }
    
    // ==================== SALES REPORTS ====================
    
    /**
     * Get sales summary for date range
     */
    public SalesSummaryReport getSalesSummary(LocalDate startDate, LocalDate endDate) {
        // Get completed reservations within the date range
        List<Reservation> completedReservations = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().equals("COMPLETED"))
                .filter(r -> {
                    LocalDate resDate = r.getCompletedDate() != null ? r.getCompletedDate().toLocalDate() : LocalDate.now();
                    return !resDate.isBefore(startDate) && !resDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
        
        int totalOrders = completedReservations.size();
        double totalRevenue = completedReservations.stream()
                .mapToDouble(Reservation::getTotalPrice)
                .sum();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;
        
        return new SalesSummaryReport(totalOrders, totalRevenue, averageOrderValue);
    }
    
    /**
     * Get revenue by item
     */
    public Map<String, Double> getRevenueByItem(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> revenueByItem = new HashMap<>();
        
        for (Receipt receipt : receiptManager.getAllReceipts()) {
            if (receipt.getPaymentStatus().equals("Completed")) {
                String itemName = receipt.getItemName();
                double revenue = receipt.getAmount();
                revenueByItem.put(itemName, revenueByItem.getOrDefault(itemName, 0.0) + revenue);
            }
        }
        
        return revenueByItem;
    }
    
    /**
     * Get completed orders
     */
    public List<ReservationReport> getCompletedOrders(LocalDate startDate, LocalDate endDate) {
        return reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().equals("COMPLETED"))
                .filter(r -> {
                    LocalDate resDate = r.getCompletedDate() != null ? r.getCompletedDate().toLocalDate() : LocalDate.now();
                    return !resDate.isBefore(startDate) && !resDate.isAfter(endDate);
                })
                .map(r -> new ReservationReport(
                    r.getReservationId(),
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemName(),
                    r.getTotalPrice(),
                    r.getStatus()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get cancelled orders
     */
    public List<ReservationReport> getCancelledOrders(LocalDate startDate, LocalDate endDate) {
        return reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().equals("CANCELLED"))
                .filter(r -> {
                    LocalDate resDate = r.getCompletedDate() != null ? r.getCompletedDate().toLocalDate() : LocalDate.now();
                    return !resDate.isBefore(startDate) && !resDate.isAfter(endDate);
                })
                .map(r -> new ReservationReport(
                    r.getReservationId(),
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemName(),
                    r.getTotalPrice(),
                    r.getStatus()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Get return/refund report
     */
    public List<ReservationReport> getReturnReport() {
        return reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().equals("REPLACED") || r.getStatus().equals("RETURNED"))
                .map(r -> new ReservationReport(
                    r.getReservationId(),
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getItemName(),
                    r.getTotalPrice(),
                    r.getStatus()
                ))
                .collect(Collectors.toList());
    }
    
    // ==================== STUDENT REPORTS ====================
    
    /**
     * Get top students by purchase volume
     */
    public List<StudentActivityReport> getTopStudents(int limit) {
        Map<String, StudentActivityReport> studentActivity = new HashMap<>();
        
        for (Reservation r : reservationManager.getAllReservations()) {
            String studentId = r.getStudentId();
            String studentName = r.getStudentName();
            double amount = r.getTotalPrice();
            
            StudentActivityReport report = studentActivity.getOrDefault(studentId,
                    new StudentActivityReport(studentId, studentName, 0, 0.0));
            report.setOrderCount(report.getOrderCount() + 1);
            report.setTotalSpent(report.getTotalSpent() + amount);
            studentActivity.put(studentId, report);
        }
        
        return studentActivity.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Get account activity status
     */
    public Map<String, Integer> getAccountActivityStatus() {
        Map<String, Integer> statusMap = new HashMap<>();
        statusMap.put("Active", 0);
        statusMap.put("Inactive", 0);
        
        try {
            List<Student> students = FileStorage.loadStudents();
            for (Student student : students) {
                boolean hasRecent = reservationManager.getAllReservations().stream()
                        .anyMatch(r -> r.getStudentId().equals(student.getStudentId()));
                
                if (hasRecent) {
                    statusMap.put("Active", statusMap.get("Active") + 1);
                } else {
                    statusMap.put("Inactive", statusMap.get("Inactive") + 1);
                }
            }
        } catch (Exception e) {
            statusMap.put("Active", 0);
            statusMap.put("Inactive", 0);
        }
        
        return statusMap;
    }
    
    /**
     * Get student distribution by course
     */
    public Map<String, Integer> getStudentDistributionByCourse() {
        Map<String, Integer> courseDistribution = new HashMap<>();
        
        try {
            List<Student> students = FileStorage.loadStudents();
            for (Student student : students) {
                String course = student.getCourse();
                courseDistribution.put(course, courseDistribution.getOrDefault(course, 0) + 1);
            }
        } catch (Exception e) {
            // Return empty map if students cannot be loaded
        }
        
        return courseDistribution;
    }
    
    // ==================== INNER DATA CLASSES ====================
    
    /**
     * Detailed item report data class - includes all item details
     */
    public static class DetailedItemReport {
        private int itemCode;
        private String itemName;
        private String course;
        private String size;
        private int quantity;
        private double price;
        private String status;
        
        public DetailedItemReport(int itemCode, String itemName, String course, String size, int quantity, double price) {
            this.itemCode = itemCode;
            this.itemName = itemName;
            this.course = course;
            this.size = size;
            this.quantity = quantity;
            this.price = price;
            this.status = quantity == 0 ? "OUT OF STOCK" : quantity <= 10 ? "LOW STOCK" : "IN STOCK";
        }
        
        public int getItemCode() { return itemCode; }
        public String getItemName() { return itemName; }
        public String getCourse() { return course; }
        public String getSize() { return size; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
        public String getStatus() { return status; }
    }
    
    /**
     * Stock report data class
     */
    public static class StockReport {
        private String category;
        private int quantity;
        
        public StockReport(String category, int quantity) {
            this.category = category;
            this.quantity = quantity;
        }
        
        public String getCategory() { return category; }
        public int getQuantity() { return quantity; }
    }
    
    /**
     * Stock valuation data class
     */
    public static class StockValuationReport {
        private String itemName;
        private int quantity;
        private double unitPrice;
        private double totalValue;
        
        public StockValuationReport(String itemName, int quantity, double unitPrice, double totalValue) {
            this.itemName = itemName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalValue = totalValue;
        }
        
        public String getItemName() { return itemName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getTotalValue() { return totalValue; }
    }
    
    /**
     * Sales summary data class
     */
    public static class SalesSummaryReport {
        private int totalOrders;
        private double totalRevenue;
        private double averageOrderValue;
        
        public SalesSummaryReport(int totalOrders, double totalRevenue, double averageOrderValue) {
            this.totalOrders = totalOrders;
            this.totalRevenue = totalRevenue;
            this.averageOrderValue = averageOrderValue;
        }
        
        public int getTotalOrders() { return totalOrders; }
        public double getTotalRevenue() { return totalRevenue; }
        public double getAverageOrderValue() { return averageOrderValue; }
    }
    
    /**
     * Reservation/Order report data class
     */
    public static class ReservationReport {
        private int reservationId;
        private String studentId;
        private String studentName;
        private String itemName;
        private double totalPrice;
        private String status;
        
        public ReservationReport(int reservationId, String studentId, String studentName,
                               String itemName, double totalPrice, String status) {
            this.reservationId = reservationId;
            this.studentId = studentId;
            this.studentName = studentName;
            this.itemName = itemName;
            this.totalPrice = totalPrice;
            this.status = status;
        }
        
        public int getReservationId() { return reservationId; }
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public String getItemName() { return itemName; }
        public double getTotalPrice() { return totalPrice; }
        public String getStatus() { return status; }
    }
    
    /**
     * Student activity report data class
     */
    public static class StudentActivityReport {
        private String studentId;
        private String studentName;
        private int orderCount;
        private double totalSpent;
        
        public StudentActivityReport(String studentId, String studentName, int orderCount, double totalSpent) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
        }
        
        public String getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
        public int getOrderCount() { return orderCount; }
        public void setOrderCount(int count) { this.orderCount = count; }
        public double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(double amount) { this.totalSpent = amount; }
    }
}
