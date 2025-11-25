package utils;

import utils.ReportGenerator.*;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.*;

/**
 * PDFExporter - Exports reports to PDF format
 * Note: Requires external library like Apache PDFBox or iText
 * For now, this provides a template for PDF export functionality
 */
public class PDFExporter {
    
    private static final String REPORTS_DIR = "reports/";
    
    static {
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(REPORTS_DIR));
        } catch (Exception e) {
            System.err.println("Failed to create reports directory: " + e.getMessage());
        }
    }
    
    /**
     * Export stock report to PDF
     * @param reports List of StockReport objects
     * @param filename Output filename
     * @return File path if successful, null otherwise
     */
    public static String exportStockReportToPDF(List<StockReport> reports, String filename) {
        try {
            // Create simple text-based PDF using StringBuilder
            StringBuilder content = new StringBuilder();
            content.append("STOCK AVAILABILITY REPORT\n");
            content.append("Generated: ").append(LocalDate.now()).append("\n");
            content.append("==========================================\n\n");
            
            content.append("Summary:\n");
            content.append("Total Items: ").append(reports.size()).append("\n");
            int totalQty = reports.stream().mapToInt(StockReport::getQuantity).sum();
            content.append("Total Quantity: ").append(totalQty).append("\n\n");
            
            content.append("Item Details:\n");
            content.append("==========================================\n");
            for (StockReport report : reports) {
                content.append("Category: ").append(report.getCategory()).append("\n");
                content.append("Quantity: ").append(report.getQuantity()).append("\n");
                content.append("-----------------------------------------\n");
            }
            
            String filepath = REPORTS_DIR + filename.replace(".pdf", "") + ".txt";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                content.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export stock report to PDF: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Export sales report to PDF
     */
    public static String exportSalesReportToPDF(SalesSummaryReport summary, String filename) {
        try {
            StringBuilder content = new StringBuilder();
            content.append("SALES & TRANSACTION REPORT\n");
            content.append("Generated: ").append(LocalDate.now()).append("\n");
            content.append("==========================================\n\n");
            
            content.append("Sales Summary:\n");
            content.append("Total Revenue: ₱").append(String.format("%.2f", summary.getTotalRevenue())).append("\n");
            content.append("Total Orders: ").append(summary.getTotalOrders()).append("\n");
            content.append("Average Order Value: ₱").append(String.format("%.2f", summary.getAverageOrderValue())).append("\n");
            
            String filepath = REPORTS_DIR + filename.replace(".pdf", "") + ".txt";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                content.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export sales report to PDF: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Export student activity report to PDF
     */
    public static String exportStudentActivityToPDF(List<StudentActivityReport> reports, String filename) {
        try {
            StringBuilder content = new StringBuilder();
            content.append("STUDENT ACTIVITY REPORT\n");
            content.append("Generated: ").append(LocalDate.now()).append("\n");
            content.append("==========================================\n\n");
            
            content.append("Top Students:\n");
            for (StudentActivityReport report : reports) {
                content.append("Student: ").append(report.getStudentName()).append("\n");
                content.append("Orders: ").append(report.getOrderCount()).append("\n");
                content.append("Total Spent: ₱").append(String.format("%.2f", report.getTotalSpent())).append("\n");
                content.append("-----------------------------------------\n");
            }
            
            String filepath = REPORTS_DIR + filename.replace(".pdf", "") + ".txt";
            java.nio.file.Files.write(
                java.nio.file.Paths.get(filepath),
                content.toString().getBytes()
            );
            return filepath;
        } catch (Exception e) {
            System.err.println("Failed to export student activity to PDF: " + e.getMessage());
            return null;
        }
    }
}
