package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import utils.ReportGenerator.DetailedItemReport;
import utils.ReportGenerator.ReservationReport;
import utils.ReportGenerator.SalesSummaryReport;
import utils.ReportGenerator.StockReport;
import utils.ReportGenerator.StudentActivityReport;

/**
 * ExcelExporter - Utility class for exporting CSV-based reports.
 * Creates CSV files that can be opened as Excel files.
 */
public class ExcelExporter {
    
    private static final String REPORTS_DIR = "reports/";
    
    static {
        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to create reports directory: " + e.getMessage());
        }
    }
    
    // ==================== HELPER METHOD ====================
    
    /**
     * Writes the CSV content to a file inside the REPORTS_DIR.
     *
     * @param filename Target filename (.csv will be appended automatically)
     * @param csv      The CSV content
     * @return Full path of the exported file
     * @throws IOException When file writing fails
     */
    private static String writeCsvToFile(String filename, StringBuilder csv) throws IOException {
        String sanitizedName = filename
                .replace(".xlsx", "")
                .replace(".csv", "")
                .trim() + ".csv";

        String filePath = REPORTS_DIR + sanitizedName;

        Files.write(Paths.get(filePath), csv.toString().getBytes());
        return filePath;
    }
    
    // ==================== STOCK REPORTS ====================
    
    /**
     * Export stock summary report (Category + Quantity).
     */
    public static String exportStockReportToExcel(List<StockReport> reports, String filename) {
        try {
            StringBuilder csv = new StringBuilder();

            // Header
            csv.append("Stock Availability Report").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

            // Table headers
            csv.append("Category,Quantity").append(System.lineSeparator());

            // Data rows
            for (StockReport report : reports) {
                csv.append("\"").append(report.getCategory()).append("\",")
                   .append(report.getQuantity())
                   .append(System.lineSeparator());
            }

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Stock Report: " + e.getMessage());
            return null;
        }
    }

    /**
     * Export Detailed Stock Inventory Report with all items grouped by course.
     */
    public static String exportDetailedStockReportToExcel(
            Map<String, List<DetailedItemReport>> itemsByCourse,
            String filename) {

        try {
            StringBuilder csv = new StringBuilder();

            // Header
            csv.append("Detailed Stock Inventory Report").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

            // Summary calculations
            int totalItems = itemsByCourse.values().stream().mapToInt(List::size).sum();
            int totalQuantity = itemsByCourse.values().stream()
                    .flatMap(List::stream)
                    .mapToInt(DetailedItemReport::getQuantity)
                    .sum();
            double totalValue = itemsByCourse.values().stream()
                    .flatMap(List::stream)
                    .mapToDouble(i -> i.getQuantity() * i.getPrice())
                    .sum();

            // Summary Section
            csv.append("Summary").append(System.lineSeparator());
            csv.append("Total Courses,").append(itemsByCourse.size()).append(System.lineSeparator());
            csv.append("Total Unique Items,").append(totalItems).append(System.lineSeparator());
            csv.append("Total Stock Quantity,").append(totalQuantity).append(System.lineSeparator());
            csv.append("Total Inventory Value,").append(String.format("%.2f", totalValue))
               .append(System.lineSeparator()).append(System.lineSeparator());

            // Table Header
            csv.append("Course,Item Code,Item Name,Size,Quantity,Price,Status")
               .append(System.lineSeparator());

            // Data Rows
            for (var entry : itemsByCourse.entrySet()) {
                String course = entry.getKey();
                List<DetailedItemReport> itemList = entry.getValue();

                for (DetailedItemReport item : itemList) {
                    csv.append("\"").append(course).append("\",")
                       .append(item.getItemCode()).append(",")
                       .append("\"").append(item.getItemName()).append("\",")
                       .append("\"").append(item.getSize()).append("\",")
                       .append(item.getQuantity()).append(",")
                       .append(String.format("%.2f", item.getPrice())).append(",")
                       .append("\"").append(item.getStatus()).append("\"")
                       .append(System.lineSeparator());
                }
            }

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Detailed Stock Report: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== SALES REPORTS ====================

    /**
     * Export summarized Sales Report (Revenue, Orders, AOV).
     */
    public static String exportSalesReportToExcel(SalesSummaryReport summary, String filename) {
        try {
            StringBuilder csv = new StringBuilder();

            csv.append("Sales & Transaction Report").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

            csv.append("Metric,Value").append(System.lineSeparator());
            csv.append("Total Revenue,").append(String.format("%.2f", summary.getTotalRevenue()))
               .append(System.lineSeparator());
            csv.append("Total Orders,").append(summary.getTotalOrders())
               .append(System.lineSeparator());
            csv.append("Average Order Value,").append(String.format("%.2f", summary.getAverageOrderValue()))
               .append(System.lineSeparator());

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Sales Report: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== ORDER REPORTS ====================

    /**
     * Export Orders / Reservations Report.
     */
    public static String exportOrdersToExcel(List<ReservationReport> orders, String filename) {
        try {
            StringBuilder csv = new StringBuilder();

            csv.append("Orders Report").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

            csv.append("Order ID,Student ID,Student Name,Item Name,Total Price,Status")
               .append(System.lineSeparator());

            for (ReservationReport order : orders) {
                csv.append(order.getReservationId()).append(",")
                   .append("\"").append(order.getStudentId()).append("\",")
                   .append("\"").append(order.getStudentName()).append("\",")
                   .append("\"").append(order.getItemName()).append("\",")
                   .append(String.format("%.2f", order.getTotalPrice())).append(",")
                   .append("\"").append(order.getStatus()).append("\"")
                   .append(System.lineSeparator());
            }

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Orders Report: " + e.getMessage());
            return null;
        }
    }
    
    // ==================== STUDENT REPORTS ====================

    /**
     * Export Student Activity Report.
     */
    public static String exportStudentActivityToExcel(List<StudentActivityReport> reports, String filename) {
        try {
            StringBuilder csv = new StringBuilder();

            csv.append("Student Activity Report").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

            csv.append("Student ID,Student Name,Order Count,Total Spent")
               .append(System.lineSeparator());

            for (StudentActivityReport report : reports) {
                csv.append("\"").append(report.getStudentId()).append("\",")
                   .append("\"").append(report.getStudentName()).append("\",")
                   .append(report.getOrderCount()).append(",")
                   .append(String.format("%.2f", report.getTotalSpent()))
                   .append(System.lineSeparator());
            }

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Student Activity Report: " + e.getMessage());
            return null;
        }
    }

    // ==================== STAFF REPORT ====================

    /**
     * Export comprehensive Staff Report with all sections:
     * Returns, Stock by Course, Low Stock, Out of Stock, Damaged Stock, Sales Summary, Completed Orders
     */
    public static String exportStaffReportToExcel(List<ReservationReport> returns, 
                                                   List<StockReport> stockByCourse,
                                                   List<StockReport> lowStockItems,
                                                   List<StockReport> outOfStockItems,
                                                   List<utils.DamagedStockTracker.DamagedStockRecord> damagedStock,
                                                   SalesSummaryReport salesSummary,
                                                   List<ReservationReport> completedOrders,
                                                   String filename) {
        try {
            StringBuilder csv = new StringBuilder();

            csv.append("STAFF REPORT").append(System.lineSeparator());
            csv.append("Generated:,").append(LocalDate.now())
               .append(System.lineSeparator()).append(System.lineSeparator());

                // ===== REPLACED SECTION =====
                csv.append("=== REPLACED ===").append(System.lineSeparator());
                csv.append("Replaced ID,Student Name,Item Name,Status")
               .append(System.lineSeparator());

            if (returns != null && !returns.isEmpty()) {
                for (ReservationReport ret : returns) {
                    csv.append(ret.getReservationId()).append(",")
                       .append("\"").append(ret.getStudentName()).append("\",")
                       .append("\"").append(ret.getItemName()).append("\",")
                       .append("\"").append(ret.getStatus()).append("\"")
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No replaced records").append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== STOCK BY COURSE SECTION =====
            csv.append("=== STOCK BY COURSE ===").append(System.lineSeparator());
            csv.append("Course,Total Quantity")
               .append(System.lineSeparator());

            if (stockByCourse != null && !stockByCourse.isEmpty()) {
                for (StockReport stock : stockByCourse) {
                    csv.append("\"").append(stock.getCategory()).append("\",")
                       .append(stock.getQuantity())
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No stock data available").append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== LOW STOCK ITEMS SECTION =====
            csv.append("=== LOW STOCK ITEMS (Below 10) ===").append(System.lineSeparator());
            csv.append("Course,Quantity")
               .append(System.lineSeparator());

            if (lowStockItems != null && !lowStockItems.isEmpty()) {
                for (StockReport stock : lowStockItems) {
                    csv.append("\"").append(stock.getCategory()).append("\",")
                       .append(stock.getQuantity())
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No low stock items").append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== OUT OF STOCK SECTION =====
            csv.append("=== OUT OF STOCK ===").append(System.lineSeparator());
            csv.append("Course,Quantity")
               .append(System.lineSeparator());

            if (outOfStockItems != null && !outOfStockItems.isEmpty()) {
                for (StockReport stock : outOfStockItems) {
                    csv.append("\"").append(stock.getCategory()).append("\",")
                       .append(stock.getQuantity())
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No out of stock items").append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== DAMAGED STOCK SECTION =====
            csv.append("=== DAMAGED STOCK ===").append(System.lineSeparator());
            csv.append("Item Code,Item Name,Reason,Date,Staff")
               .append(System.lineSeparator());

            if (damagedStock != null && !damagedStock.isEmpty()) {
                for (utils.DamagedStockTracker.DamagedStockRecord record : damagedStock) {
                    csv.append(record.getOriginalItemCode()).append(",")
                       .append("\"").append(record.getOriginalItemName()).append("\",")
                       .append("\"").append(record.getReason()).append("\",")
                       .append("\"").append(record.getTimestamp()).append("\",")
                       .append("\"").append(record.getProcessedBy()).append("\"")
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No damaged stock records").append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== SALES SUMMARY SECTION =====
            csv.append("=== SALES SUMMARY ===").append(System.lineSeparator());
            csv.append("Metric,Value").append(System.lineSeparator());
            if (salesSummary != null) {
                csv.append("Total Revenue,").append(String.format("%.2f", salesSummary.getTotalRevenue()))
                   .append(System.lineSeparator());
                csv.append("Total Orders,").append(salesSummary.getTotalOrders())
                   .append(System.lineSeparator());
                csv.append("Average Order Value,").append(String.format("%.2f", salesSummary.getAverageOrderValue()))
                   .append(System.lineSeparator());
            }

            csv.append(System.lineSeparator());

            // ===== COMPLETED ORDERS SECTION =====
            csv.append("=== COMPLETED ORDERS ===").append(System.lineSeparator());
            csv.append("Order ID,Student,Item,Price,Status")
               .append(System.lineSeparator());

            if (completedOrders != null && !completedOrders.isEmpty()) {
                for (ReservationReport order : completedOrders) {
                    csv.append(order.getReservationId()).append(",")
                       .append("\"").append(order.getStudentName()).append("\",")
                       .append("\"").append(order.getItemName()).append("\",")
                       .append(String.format("%.2f", order.getTotalPrice())).append(",")
                       .append("\"").append(order.getStatus()).append("\"")
                       .append(System.lineSeparator());
                }
            } else {
                csv.append("No completed orders").append(System.lineSeparator());
            }

            return writeCsvToFile(filename, csv);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to export Staff Report: " + e.getMessage());
            return null;
        }
    }
}
