package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * PDFExporter - Exports reports to professional PDF format using Apache PDFBox
 * Features: Header with STI branding, tables, summaries, page numbers, footer
 */
public class PDFExporter {

    private static final String REPORTS_DIR = "reports";
    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 14f;
    private static final float TABLE_ROW_HEIGHT = 18f;
    
    // Colors (RGB values 0-1)
    private static final float[] HEADER_COLOR = {0.118f, 0.235f, 0.447f}; // Dark blue (#1e3c72)

    static {
        try {
            Files.createDirectories(Path.of(REPORTS_DIR));
        } catch (Exception e) {
            System.err.println("Failed to create reports directory: " + e.getMessage());
        }
    }

    // ==================== STOCK REPORT ====================
    
    public static String exportStockReportToPDF(List<ReportGenerator.StockReport> reports, String filename) {
        try {
            String baseName = filename.replaceAll("\\.pdf$", "");
            Path outPath = Path.of(REPORTS_DIR, baseName + ".pdf");

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);
                
                PDPageContentStream cs = new PDPageContentStream(doc, page);
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float y = pageHeight - MARGIN;
                
                // Header
                y = drawHeader(cs, pageWidth, y, "STOCK AVAILABILITY REPORT");
                
                // Summary box
                int totalQty = reports.stream().mapToInt(ReportGenerator.StockReport::getQuantity).sum();
                y = drawSummaryBox(cs, y, new String[][]{
                    {"Total Categories", String.valueOf(reports.size())},
                    {"Total Stock Quantity", String.valueOf(totalQty)},
                    {"Report Date", LocalDate.now().toString()}
                });
                
                // Table header
                y -= 20;
                String[] headers = {"Category/Course", "Quantity", "Status"};
                float[] colWidths = {250, 100, 150};
                y = drawTableHeader(cs, y, headers, colWidths);
                
                // Table rows
                for (ReportGenerator.StockReport r : reports) {
                    if (y < MARGIN + 50) {
                        // Add page number and close
                        drawFooter(cs, pageWidth, doc.getNumberOfPages());
                        cs.close();
                        
                        // New page
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = pageHeight - MARGIN;
                        y = drawTableHeader(cs, y, headers, colWidths);
                    }
                    
                    String status = r.getQuantity() == 0 ? "OUT OF STOCK" : 
                                   r.getQuantity() <= 10 ? "LOW STOCK" : "IN STOCK";
                    String[] row = {r.getCategory(), String.valueOf(r.getQuantity()), status};
                    y = drawTableRow(cs, y, row, colWidths, r.getQuantity() <= 10);
                }
                
                // Footer
                drawFooter(cs, pageWidth, doc.getNumberOfPages());
                cs.close();
                doc.save(outPath.toFile());
            }
            
            SystemLogger.logActivity("📄 Stock report exported to PDF: " + outPath);
            return outPath.toString();
        } catch (Exception e) {
            System.err.println("Failed to export stock report to PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ==================== DETAILED STOCK REPORT (By Course with Items & Sizes) ====================
    
    public static String exportDetailedStockReportToPDF(java.util.Map<String, java.util.List<ReportGenerator.DetailedItemReport>> itemsByCourse, String filename) {
        try {
            String baseName = filename.replaceAll("\\.pdf$", "");
            Path outPath = Path.of(REPORTS_DIR, baseName + ".pdf");

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);
                
                PDPageContentStream cs = new PDPageContentStream(doc, page);
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float y = pageHeight - MARGIN;
                
                // Header
                y = drawHeader(cs, pageWidth, y, "STI UNIFORMS INVENTORY REPORT");
                
                // Calculate totals
                int totalItems = itemsByCourse.values().stream().mapToInt(java.util.List::size).sum();
                int totalQty = itemsByCourse.values().stream()
                        .flatMap(java.util.List::stream)
                        .mapToInt(ReportGenerator.DetailedItemReport::getQuantity)
                        .sum();
                double totalValue = itemsByCourse.values().stream()
                        .flatMap(java.util.List::stream)
                        .mapToDouble(r -> r.getQuantity() * r.getPrice())
                        .sum();
                
                y = drawSummaryBox(cs, y, new String[][]{
                    {"Total Courses", String.valueOf(itemsByCourse.size())},
                    {"Total Items", String.valueOf(totalItems)},
                    {"Total Stock Quantity", String.valueOf(totalQty)},
                    {"Total Inventory Value", String.format("P%.2f", totalValue)},
                    {"Report Date", LocalDate.now().toString()}
                });
                
                // Table headers
                String[] headers = {"Code", "Item Name", "Size", "Qty", "Price", "Status"};
                float[] colWidths = {50, 180, 60, 50, 70, 90};
                
                // Loop through each course
                for (java.util.Map.Entry<String, java.util.List<ReportGenerator.DetailedItemReport>> entry : itemsByCourse.entrySet()) {
                    String course = entry.getKey();
                    java.util.List<ReportGenerator.DetailedItemReport> items = entry.getValue();
                    
                    // Check if we need a new page for course header
                    if (y < MARGIN + 100) {
                        drawFooter(cs, pageWidth, doc.getNumberOfPages());
                        cs.close();
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = pageHeight - MARGIN;
                    }
                    
                    // Course header
                    y -= 25;
                    y = drawCourseHeader(cs, y, course, items.size());
                    y -= 10;
                    
                    // Table header
                    y = drawTableHeader(cs, y, headers, colWidths);
                    
                    // Items for this course
                    for (ReportGenerator.DetailedItemReport item : items) {
                        if (y < MARGIN + 50) {
                            drawFooter(cs, pageWidth, doc.getNumberOfPages());
                            cs.close();
                            page = new PDPage(PDRectangle.LETTER);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);
                            y = pageHeight - MARGIN;
                            
                            // Repeat course and table header on new page
                            y = drawCourseHeader(cs, y, course + "", 0);
                            y -= 10;
                            y = drawTableHeader(cs, y, headers, colWidths);
                        }
                        
                        boolean isLowStock = item.getQuantity() <= 10;
                        String[] row = {
                            String.valueOf(item.getItemCode()),
                            truncate(item.getItemName(), 28),
                            item.getSize(),
                            String.valueOf(item.getQuantity()),
                            String.format("P%.0f", item.getPrice()),
                            item.getStatus()
                        };
                        y = drawTableRow(cs, y, row, colWidths, isLowStock);
                    }
                }
                
                // Footer
                drawFooter(cs, pageWidth, doc.getNumberOfPages());
                cs.close();
                doc.save(outPath.toFile());
            }
            
            SystemLogger.logActivity("📄 Detailed stock report exported to PDF: " + outPath);
            return outPath.toString();
        } catch (Exception e) {
            System.err.println("Failed to export detailed stock report to PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static float drawCourseHeader(PDPageContentStream cs, float y, String courseName, int itemCount) throws IOException {
        // Course header background
        cs.setNonStrokingColor(0.2f, 0.4f, 0.6f); // Blue-ish
        cs.addRect(MARGIN, y - 20, 500, 22);
        cs.fill();
        
        // Course name
        cs.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 11);
        cs.beginText();
        cs.newLineAtOffset(MARGIN + 10, y - 14);
        String headerText = itemCount > 0 ? courseName + " (" + itemCount + " items)" : courseName;
        cs.showText(headerText);
        cs.endText();
        
        cs.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        return y - 22;
    }

    // ==================== SALES REPORT ====================
    
    public static String exportSalesReportToPDF(ReportGenerator.SalesSummaryReport summary, String filename) {
        try {
            String baseName = filename.replaceAll("\\.pdf$", "");
            Path outPath = Path.of(REPORTS_DIR, baseName + ".pdf");

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);
                
                PDPageContentStream cs = new PDPageContentStream(doc, page);
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float y = pageHeight - MARGIN;
                
                // Header
                y = drawHeader(cs, pageWidth, y, "SALES & TRANSACTION REPORT");
                
                // Summary box
                y = drawSummaryBox(cs, y, new String[][]{
                    {"Total Orders", String.valueOf(summary.getTotalOrders())},
                    {"Total Revenue", String.format("P%.2f", summary.getTotalRevenue())},
                    {"Average Order Value", String.format("P%.2f", summary.getAverageOrderValue())},
                    {"Report Period", "Last 30 Days"},
                    {"Generated", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}
                });
                
                // Revenue breakdown section
                y -= 30;
                cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Revenue Analysis");
                cs.endText();
                
                y -= 20;
                cs.setFont(PDType1Font.HELVETICA, 10);
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("This report summarizes all completed transactions within the specified period.");
                cs.endText();
                
                y -= LINE_HEIGHT;
                cs.beginText();
                cs.newLineAtOffset(MARGIN, y);
                cs.showText("Payment methods accepted: CASH, GCASH, CARD, BANK TRANSFER");
                cs.endText();
                
                // Footer
                drawFooter(cs, pageWidth, doc.getNumberOfPages());
                cs.close();
                doc.save(outPath.toFile());
            }
            
            SystemLogger.logActivity("📄 Sales report exported to PDF: " + outPath);
            return outPath.toString();
        } catch (Exception e) {
            System.err.println("Failed to export sales report to PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ==================== STUDENT ACTIVITY REPORT ====================
    
    public static String exportStudentActivityToPDF(List<ReportGenerator.StudentActivityReport> reports, String filename) {
        try {
            String baseName = filename.replaceAll("\\.pdf$", "");
            Path outPath = Path.of(REPORTS_DIR, baseName + ".pdf");

            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);
                
                PDPageContentStream cs = new PDPageContentStream(doc, page);
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();
                float y = pageHeight - MARGIN;
                
                // Header
                y = drawHeader(cs, pageWidth, y, "STUDENT ACTIVITY REPORT");
                
                // Summary
                double totalSpent = reports.stream().mapToDouble(ReportGenerator.StudentActivityReport::getTotalSpent).sum();
                int totalOrders = reports.stream().mapToInt(ReportGenerator.StudentActivityReport::getOrderCount).sum();
                
                y = drawSummaryBox(cs, y, new String[][]{
                    {"Total Students", String.valueOf(reports.size())},
                    {"Total Orders", String.valueOf(totalOrders)},
                    {"Total Revenue", String.format("P%.2f", totalSpent)},
                    {"Report Date", LocalDate.now().toString()}
                });
                
                // Table header
                y -= 20;
                String[] headers = {"Student ID", "Student Name", "Orders", "Total Spent"};
                float[] colWidths = {100, 180, 70, 120};
                y = drawTableHeader(cs, y, headers, colWidths);
                
                // Table rows
                for (ReportGenerator.StudentActivityReport r : reports) {
                    if (y < MARGIN + 50) {
                        drawFooter(cs, pageWidth, doc.getNumberOfPages());
                        cs.close();
                        
                        page = new PDPage(PDRectangle.LETTER);
                        doc.addPage(page);
                        cs = new PDPageContentStream(doc, page);
                        y = pageHeight - MARGIN;
                        y = drawTableHeader(cs, y, headers, colWidths);
                    }
                    
                    String[] row = {
                        r.getStudentId(),
                        truncate(r.getStudentName(), 25),
                        String.valueOf(r.getOrderCount()),
                        String.format("P%.2f", r.getTotalSpent())
                    };
                    y = drawTableRow(cs, y, row, colWidths, false);
                }
                
                // Footer
                drawFooter(cs, pageWidth, doc.getNumberOfPages());
                cs.close();
                doc.save(outPath.toFile());
            }
            
            SystemLogger.logActivity("📄 Student activity report exported to PDF: " + outPath);
            return outPath.toString();
        } catch (Exception e) {
            System.err.println("Failed to export student activity report to PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // ==================== HELPER METHODS ====================
    
    private static float drawHeader(PDPageContentStream cs, float pageWidth, float y, String title) throws IOException {
        // Draw header background
        cs.setNonStrokingColor(HEADER_COLOR[0], HEADER_COLOR[1], HEADER_COLOR[2]);
        cs.addRect(0, y - 10, pageWidth, 60);
        cs.fill();
        
        // Title
        cs.setNonStrokingColor(1.0f, 1.0f, 1.0f); // White
        cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y + 20);
        cs.showText(title);
        cs.endText();
        
        // Subtitle
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.beginText();
        cs.newLineAtOffset(MARGIN, y + 5);
        cs.showText("STI Novaliches ProWear System");
        cs.endText();
        
        // Reset color
        cs.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        
        return y - 80;
    }
    
    private static float drawSummaryBox(PDPageContentStream cs, float y, String[][] items) throws IOException {
        // Box background
        cs.setNonStrokingColor(0.96f, 0.96f, 0.96f);
        cs.addRect(MARGIN, y - (items.length * 20 + 20), 500, items.length * 20 + 20);
        cs.fill();
        
        // Border
        cs.setStrokingColor(0.8f, 0.8f, 0.8f);
        cs.addRect(MARGIN, y - (items.length * 20 + 20), 500, items.length * 20 + 20);
        cs.stroke();
        
        cs.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        y -= 15;
        
        for (String[] item : items) {
            // Label
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText();
            cs.newLineAtOffset(MARGIN + 10, y);
            cs.showText(item[0] + ":");
            cs.endText();
            
            // Value
            cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
            cs.beginText();
            cs.newLineAtOffset(MARGIN + 150, y);
            cs.showText(item[1]);
            cs.endText();
            
            y -= 18;
        }
        
        return y - 10;
    }
    
    private static float drawTableHeader(PDPageContentStream cs, float y, String[] headers, float[] colWidths) throws IOException {
        // Header background
        cs.setNonStrokingColor(HEADER_COLOR[0], HEADER_COLOR[1], HEADER_COLOR[2]);
        cs.addRect(MARGIN, y - TABLE_ROW_HEIGHT, sumArray(colWidths), TABLE_ROW_HEIGHT);
        cs.fill();
        
        // Header text
        cs.setNonStrokingColor(1.0f, 1.0f, 1.0f);
        cs.setFont(PDType1Font.HELVETICA_BOLD, 10);
        
        float x = MARGIN + 5;
        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x, y - 13);
            cs.showText(headers[i]);
            cs.endText();
            x += colWidths[i];
        }
        
        cs.setNonStrokingColor(0.0f, 0.0f, 0.0f);
        return y - TABLE_ROW_HEIGHT;
    }
    
    private static float drawTableRow(PDPageContentStream cs, float y, String[] values, float[] colWidths, boolean highlight) throws IOException {
        // Row background (alternating or highlight)
        if (highlight) {
            cs.setNonStrokingColor(1.0f, 0.9f, 0.9f); // Light red for low stock
        } else {
            cs.setNonStrokingColor(1.0f, 1.0f, 1.0f); // White background
        }
        cs.addRect(MARGIN, y - TABLE_ROW_HEIGHT, sumArray(colWidths), TABLE_ROW_HEIGHT);
        cs.fill();
        
        // Border
        cs.setStrokingColor(0.85f, 0.85f, 0.85f);
        cs.addRect(MARGIN, y - TABLE_ROW_HEIGHT, sumArray(colWidths), TABLE_ROW_HEIGHT);
        cs.stroke();
        
        // Row text - dark color for readability
        cs.setNonStrokingColor(0.1f, 0.1f, 0.1f);
        cs.setFont(PDType1Font.HELVETICA, 9);
        
        float x = MARGIN + 5;
        for (int i = 0; i < values.length; i++) {
            cs.beginText();
            cs.newLineAtOffset(x, y - 13);
            cs.showText(values[i] != null ? values[i] : "");
            cs.endText();
            x += colWidths[i];
        }
        
        return y - TABLE_ROW_HEIGHT;
    }
    
    private static void drawFooter(PDPageContentStream cs, float pageWidth, int pageNum) throws IOException {
        float footerY = 30;
        
        // Line
        cs.setStrokingColor(0.8f, 0.8f, 0.8f);
        cs.moveTo(MARGIN, footerY + 10);
        cs.lineTo(pageWidth - MARGIN, footerY + 10);
        cs.stroke();
        
        // Footer text
        cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
        cs.setFont(PDType1Font.HELVETICA, 8);
        
        cs.beginText();
        cs.newLineAtOffset(MARGIN, footerY);
        cs.showText("Generated by STI Novaliches ProWear System | Confidential");
        cs.endText();
        
        cs.beginText();
        cs.newLineAtOffset(pageWidth - MARGIN - 50, footerY);
        cs.showText("Page " + pageNum);
        cs.endText();
        
        cs.setNonStrokingColor(0.0f, 0.0f, 0.0f);
    }
    
    private static float sumArray(float[] arr) {
        float sum = 0;
        for (float v : arr) sum += v;
        return sum;
    }
    
    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }
}
