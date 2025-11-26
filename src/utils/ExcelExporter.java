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
}
