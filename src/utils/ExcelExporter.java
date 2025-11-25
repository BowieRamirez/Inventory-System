package utils;

import utils.ReportGenerator.*;
import java.time.LocalDate;
import java.util.*;

/**
 * ExcelExporter - Exports reports to Excel format
 * Note: Requires Apache POI library for full Excel support
 * This version creates CSV which can be opened as Excel files
 */
public class ExcelExporter {
    
    private static final String REPORTS_DIR = "reports/";
    
    static {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(REPORTS_DIR));
        } catch (Exception e) {
            System.err.println("Failed to create reports directory: " + e.getMessage());
        }
    }
    
    /**
     * Export stock report to Excel (CSV format)
     */
    public static String exportStockReportToExcel(List<StockReport> reports, String filename) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Stock Availability Report\n");
            csv.append("Generated:,").append(LocalDate.now()).append("\n\n");
            
            csv.append("Category,Quantity\n");
            for (StockReport report : reports) {
                csv.append("\"").append(report.getCategory()).append("\",");
                csv.append(report.getQuantity()).append("\n");
            }
            
            String filepath = REPORTS_DIR + filename.replace(".xlsx", "") + ".csv";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                csv.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export stock report to Excel: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Export sales report to Excel (CSV format)
     */
    public static String exportSalesReportToExcel(SalesSummaryReport summary, String filename) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Sales & Transaction Report\n");
            csv.append("Generated:,").append(LocalDate.now()).append("\n\n");
            
            csv.append("Metric,Value\n");
            csv.append("Total Revenue,").append(String.format("%.2f", summary.getTotalRevenue())).append("\n");
            csv.append("Total Orders,").append(summary.getTotalOrders()).append("\n");
            csv.append("Average Order Value,").append(String.format("%.2f", summary.getAverageOrderValue())).append("\n");
            
            String filepath = REPORTS_DIR + filename.replace(".xlsx", "") + ".csv";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                csv.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export sales report to Excel: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Export orders report to Excel (CSV format)
     */
    public static String exportOrdersToExcel(List<ReservationReport> orders, String filename) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Orders Report\n");
            csv.append("Generated:,").append(LocalDate.now()).append("\n\n");
            
            csv.append("Order ID,Student ID,Student Name,Item Name,Total Price,Status\n");
            for (ReservationReport order : orders) {
                csv.append(order.getReservationId()).append(",");
                csv.append("\"").append(order.getStudentId()).append("\",");
                csv.append("\"").append(order.getStudentName()).append("\",");
                csv.append("\"").append(order.getItemName()).append("\",");
                csv.append(String.format("%.2f", order.getTotalPrice())).append(",");
                csv.append("\"").append(order.getStatus()).append("\"\n");
            }
            
            String filepath = REPORTS_DIR + filename.replace(".xlsx", "") + ".csv";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                csv.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export orders to Excel: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Export student activity report to Excel (CSV format)
     */
    public static String exportStudentActivityToExcel(List<StudentActivityReport> reports, String filename) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Student Activity Report\n");
            csv.append("Generated:,").append(LocalDate.now()).append("\n\n");
            
            csv.append("Student ID,Student Name,Order Count,Total Spent\n");
            for (StudentActivityReport report : reports) {
                csv.append("\"").append(report.getStudentId()).append("\",");
                csv.append("\"").append(report.getStudentName()).append("\",");
                csv.append(report.getOrderCount()).append(",");
                csv.append(String.format("%.2f", report.getTotalSpent())).append("\n");
            }
            
            String filepath = REPORTS_DIR + filename.replace(".xlsx", "") + ".csv";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                csv.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export student activity to Excel: " + e.getMessage());
            return null;
        }
    }
}
