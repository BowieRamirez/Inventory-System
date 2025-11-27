package gui.controllers;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gui.utils.ThemeManager;
import inventory.InventoryManager;
import inventory.ReceiptManager;
import inventory.ReservationManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import utils.DamagedStockTracker;
import utils.DamagedStockTracker.DamagedStockRecord;
import utils.ReplacementTracker;
import utils.ReplacementTracker.ReplacementReason;
import utils.ReplacementTracker.ReplacementRecord;
import utils.ReplacementTracker.ReplacementSummary;
import utils.ReportGenerator;
import utils.ReportGenerator.ReservationReport;
import utils.ReportGenerator.SalesSummaryReport;
import utils.ReportGenerator.StockReport;
import utils.ReportGenerator.StockValuationReport;
import utils.ReportGenerator.StudentActivityReport;

/**
 * ReportController - Manages report generation and display
 */
public class ReportController {
    
    private ReportGenerator reportGenerator;
    private ReservationManager reservationManager;
    
    public ReportController(InventoryManager inventoryManager,
                           ReservationManager reservationManager,
                           ReceiptManager receiptManager) {
        this.reportGenerator = new ReportGenerator(inventoryManager, reservationManager, receiptManager);
        this.reservationManager = reservationManager;
    }
    
    // Theme-aware color methods
    private String getReportTitleColor() {
        return ThemeManager.isDarkMode() ? "#d0d0e0" : "#1e3c72";
    }
    
    private String getSearchBoxBgColor() {
        return ThemeManager.isDarkMode() ? "rgba(255,255,255,0.08)" : "#f5f5f5";
    }
    
    private String getSearchBoxBorderColor() {
        return ThemeManager.isDarkMode() ? "rgba(255,255,255,0.15)" : "#e0e0e0";
    }
    
    private String getDescLabelColor() {
        return ThemeManager.isDarkMode() ? "#a0a0c0" : "#666666";
    }
    
    @SuppressWarnings("unused")
    private String getStatBoxBgColor() {
        return ThemeManager.isDarkMode() ? "rgba(255,255,255,0.05)" : "#f5f5f5";
    }
    
    @SuppressWarnings("unused")
    private String getStatBoxBorderColor() {
        return ThemeManager.isDarkMode() ? "rgba(255,255,255,0.1)" : "#e0e0e0";
    }
    
    private String getButtonBgColor() {
        return "#1e3c72";
    }

    
    
    @SuppressWarnings("unused")
    private String getButtonHoverBgColor() {
        return "#0d1f3c";
    }
    
    /**
     * Create stock availability report view
     */
    public VBox createStockAvailabilityReport() {
        VBox reportBox = new VBox(15);
        reportBox.setPadding(new Insets(20));
        
        // Header with title and export buttons
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("📦 Stock Availability Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Button exportPdfBtn = createExportButton("📄 Export as PDF");
        Button exportExcelBtn = createExportButton("📊 Export as Excel");
        
        exportPdfBtn.setOnAction(e -> exportStockReport("PDF"));
        exportExcelBtn.setOnAction(e -> exportStockReport("EXCEL"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportPdfBtn, exportExcelBtn);
        reportBox.getChildren().add(headerBox);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab stockByCoursTab = new Tab("Stock by Course", createStockByCourseTab());
        Tab lowStockTab = new Tab("Low Stock Items", createLowStockTab());
        Tab outOfStockTab = new Tab("Out of Stock", createOutOfStockTab());
        Tab valuationTab = new Tab("Stock Valuation", createStockValuationTab());
        Tab damagedStockTab = new Tab("Damaged Stock", createDamagedStockTab());
        
        tabPane.getTabs().addAll(stockByCoursTab, lowStockTab, outOfStockTab, valuationTab, damagedStockTab);
        reportBox.getChildren().add(tabPane);
        
        return reportBox;
    }
    
    private Button createExportButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 12px; -fx-padding: 8 15; -fx-background-color: #1e3c72; -fx-text-fill: white; -fx-border-radius: 5;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-font-size: 12px; -fx-padding: 8 15; -fx-background-color: #0d1f3c; -fx-text-fill: white; -fx-border-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-font-size: 12px; -fx-padding: 8 15; -fx-background-color: #1e3c72; -fx-text-fill: white; -fx-border-radius: 5;"));
        return btn;
    }
    
    private void exportStockReport(String format) {
        try {
            String filename = "stock_report_" + LocalDate.now() + "." + (format.equals("PDF") ? "pdf" : "csv");
            String filepath = "reports/" + filename;
            
            // Use detailed stock report with sizes and courses for both formats
            Map<String, List<ReportGenerator.DetailedItemReport>> detailedData = reportGenerator.getDetailedItemsByCourse();
            
            if (format.equals("PDF")) {
                utils.PDFExporter.exportDetailedStockReportToPDF(detailedData, filename);
            } else {
                utils.ExcelExporter.exportDetailedStockReportToExcel(detailedData, filename);
            }
            
            showExportSuccess(filepath);
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private VBox createStockByCourseTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        List<StockReport> stockByCourse = reportGenerator.getStockByCourse();
        
        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));
        searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by course name...");
        searchField.setPrefWidth(300);
        
        Label descLabel = new Label("Stock levels grouped by course:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getDescLabelColor() + ";");
        
        TableView<StockReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<StockReport, String> categoryCol = new TableColumn<>("Course/Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(200);
        categoryCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StockReport, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);
        qtyCol.setStyle("-fx-alignment: CENTER;");
        
        table.getColumns().add(categoryCol);
        table.getColumns().add(qtyCol);
        
        // Live search filter
        javafx.collections.ObservableList<StockReport> observableList = FXCollections.observableArrayList(stockByCourse);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(observableList);
            } else {
                String searchLower = newVal.toLowerCase();
                List<StockReport> filtered = new ArrayList<>();
                for (StockReport report : stockByCourse) {
                    if (report.getCategory().toLowerCase().contains(searchLower)) {
                        filtered.add(report);
                    }
                }
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });
        
        table.setItems(observableList);
        
        searchBox.getChildren().addAll(new Label("Search:"), searchField);
        box.getChildren().addAll(descLabel, searchBox, table);
        
        return box;
    }
    private VBox createLowStockTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        List<StockReport> lowStockItems = reportGenerator.getLowStockItems(10);
        
        // Controls bar with search and filter
        HBox controlsBox = new HBox(15);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        controlsBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by item name...");
        searchField.setPrefWidth(300);
        
        Spinner<Integer> thresholdSpinner = new Spinner<>(1, 100, 10);
        thresholdSpinner.setPrefWidth(120);
        thresholdSpinner.setStyle("-fx-font-size: 12px;");
        
        TableView<StockReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<StockReport, String> itemCol = new TableColumn<>("Item Name");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        itemCol.setPrefWidth(200);
        itemCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StockReport, Integer> quantityCol = new TableColumn<>("Stock Level");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        quantityCol.setStyle("-fx-alignment: CENTER;");
        
        table.getColumns().add(itemCol);
        table.getColumns().add(quantityCol);
        
        // Search and filter logic
        javafx.collections.ObservableList<StockReport> observableList = FXCollections.observableArrayList(lowStockItems);
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateLowStockTable(table, searchField.getText(), thresholdSpinner.getValue(), lowStockItems));
        thresholdSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateLowStockTable(table, searchField.getText(), newVal, lowStockItems));
        
        table.setItems(observableList);
        
        controlsBox.getChildren().addAll(
            new Label("Search:"), searchField,
            new Label("Threshold:"), thresholdSpinner
        );
        
        Label descLabel = new Label("Items with stock below threshold:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getDescLabelColor() + ";");
        
        box.getChildren().addAll(descLabel, controlsBox, table);
        
        return box;
    }
    
    private void updateLowStockTable(TableView<StockReport> table, String searchText, int threshold, List<StockReport> allItems) {
        List<StockReport> filtered = new ArrayList<>();
        String searchLower = searchText == null ? "" : searchText.toLowerCase();
        
        for (StockReport item : allItems) {
            boolean matchesSearch = item.getCategory().toLowerCase().contains(searchLower);
            boolean matchesThreshold = item.getQuantity() <= threshold;
            
            if (matchesSearch && matchesThreshold) {
                filtered.add(item);
            }
        }
        
        table.setItems(FXCollections.observableArrayList(filtered));
    }
    
    private VBox createOutOfStockTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
            List<ReportGenerator.DetailedItemReport> detailed = reportGenerator.getAllDetailedItems();

            // Per-variant out-of-stock rows (quantity == 0)
            List<ReportGenerator.DetailedItemReport> oosVariants = detailed.stream()
                    .filter(d -> d.getQuantity() == 0)
                    .collect(Collectors.toList());

            TableView<ReportGenerator.DetailedItemReport> table = new TableView<>();
            table.setPrefHeight(360);

            // Single descriptive column in the format: "ItemName Size x Qty OOS"
            TableColumn<ReportGenerator.DetailedItemReport, String> descCol = new TableColumn<>("Out of Stock Items");
            descCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("%s %s x %d OOS",
                    c.getValue().getItemName(),
                    c.getValue().getSize() != null ? c.getValue().getSize() : "",
                    c.getValue().getQuantity())
            ));
            descCol.setPrefWidth(820);
            descCol.setStyle("-fx-alignment: CENTER-LEFT;");

            table.getColumns().add(descCol);
            table.setItems(FXCollections.observableArrayList(oosVariants));

            Label countLabel = new Label("Out of Stock Rows: " + oosVariants.size());
            countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #d32f2f;");

            box.getChildren().addAll(countLabel, table);

            return box;
    }
    
    private VBox createStockValuationTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        List<StockValuationReport> valuations = reportGenerator.getStockValuation();
        
        TableView<StockValuationReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<StockValuationReport, String> itemCol = new TableColumn<>("Item Name");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(150);
        itemCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StockValuationReport, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        quantityCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StockValuationReport, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(100);
        priceCol.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StockValuationReport, Double> valueCol = new TableColumn<>("Total Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        valueCol.setPrefWidth(100);
        valueCol.setStyle("-fx-alignment: CENTER;");
        
        table.getColumns().add(itemCol);
        table.getColumns().add(quantityCol);
        table.getColumns().add(priceCol);
        table.getColumns().add(valueCol);
        table.setItems(FXCollections.observableArrayList(valuations));
        
        double totalValue = valuations.stream().mapToDouble(StockValuationReport::getTotalValue).sum();
        Label summaryLabel = new Label(String.format("Total Inventory Value: ₱%.2f", totalValue));
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0d6a1e;");
        
        box.getChildren().addAll(table, summaryLabel);
        
        return box;
    }
    
    /**
     * Create transaction/sales report view (Admin version - without Returns)
     */
    public VBox createTransactionReport() {
        VBox reportBox = new VBox(15);
        reportBox.setPadding(new Insets(20));
        
        // Header with title and export buttons
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("💳 Transaction & Sales Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Button exportPdfBtn = createExportButton("📄 Export as PDF");
        Button exportExcelBtn = createExportButton("📊 Export as Excel");
        
        exportPdfBtn.setOnAction(e -> exportTransactionReport("PDF"));
        exportExcelBtn.setOnAction(e -> exportTransactionReport("EXCEL"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportPdfBtn, exportExcelBtn);
        reportBox.getChildren().add(headerBox);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab summaryTab = new Tab("Sales Summary", createSalesSummaryTab());
        Tab completedTab = new Tab("Completed Orders", createCompletedOrdersTab());
        Tab cancelledTab = new Tab("Cancelled Orders", createCancelledOrdersTab());
        
        // NOTE: Returns tab removed - staff handles returns now
        tabPane.getTabs().addAll(summaryTab, completedTab, cancelledTab);
        reportBox.getChildren().add(tabPane);
        
        return reportBox;
    }
    
    /**
     * Create transaction/sales report view for STAFF (without Returns tab)
     * Staff should not see Returns as it's handled differently
     */
    public VBox createTransactionReportStaff() {
        VBox reportBox = new VBox(15);
        reportBox.setPadding(new Insets(20));
        
        // Header with title and export buttons
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("💳 Transaction & Sales Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Button exportPdfBtn = createExportButton("📄 Export as PDF");
        Button exportExcelBtn = createExportButton("📊 Export as Excel");
        
        exportPdfBtn.setOnAction(e -> exportTransactionReport("PDF"));
        exportExcelBtn.setOnAction(e -> exportTransactionReport("EXCEL"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportPdfBtn, exportExcelBtn);
        reportBox.getChildren().add(headerBox);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab summaryTab = new Tab("Sales Summary", createSalesSummaryTab());
        Tab completedTab = new Tab("Completed Orders", createCompletedOrdersTab());
        Tab cancelledTab = new Tab("Cancelled Orders", createCancelledOrdersTab());
        
        // NOTE: No Returns tab for staff - returns are handled separately
        tabPane.getTabs().addAll(summaryTab, completedTab, cancelledTab);
        reportBox.getChildren().add(tabPane);
        
        return reportBox;
    }
    
    private void exportTransactionReport(String format) {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(1);
            LocalDate endDate = LocalDate.now();
            SalesSummaryReport summary = reportGenerator.getSalesSummary(startDate, endDate);
            String filename = "transaction_report_" + LocalDate.now() + "." + (format.equals("PDF") ? "txt" : "csv");
            
            if (format.equals("PDF")) {
                utils.PDFExporter.exportSalesReportToPDF(summary, filename);
            } else {
                utils.ExcelExporter.exportSalesReportToExcel(summary, filename);
            }
            
            showExportSuccess("reports/" + filename);
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    private VBox createSalesSummaryTab() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));
        
        // Filter buttons
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Button todayBtn = new Button("Today");
        Button monthBtn = new Button("This Month");
        Button overallBtn = new Button("Overall");
        
        // Active filter indicator
        Label activeFilterLabel = new Label("(Showing: Overall)");
        activeFilterLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getReportTitleColor() + "; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Stats container that will be updated
        VBox statsContainer = new VBox(10);
        statsContainer.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-padding: 15;");
        
        // Label container that will be updated
        VBox labelContainer = new VBox(5);
        
        // Initial load - Overall (set to active immediately)
        setButtonActive(overallBtn, todayBtn, monthBtn);
        updateSalesStats(statsContainer, labelContainer, LocalDate.now().minusYears(10), LocalDate.now());
        
        // Button actions with proper state management
        todayBtn.setOnAction(e -> {
            setButtonActive(todayBtn, monthBtn, overallBtn);
            activeFilterLabel.setText("(Showing: Today)");
            updateSalesStats(statsContainer, labelContainer, LocalDate.now(), LocalDate.now());
        });
        
        monthBtn.setOnAction(e -> {
            setButtonActive(monthBtn, todayBtn, overallBtn);
            activeFilterLabel.setText("(Showing: This Month)");
            updateSalesStats(statsContainer, labelContainer, LocalDate.now().withDayOfMonth(1), LocalDate.now());
        });
        
        overallBtn.setOnAction(e -> {
            setButtonActive(overallBtn, todayBtn, monthBtn);
            activeFilterLabel.setText("(Showing: Overall)");
            updateSalesStats(statsContainer, labelContainer, LocalDate.now().minusYears(10), LocalDate.now());
        });
        
        filterBox.getChildren().addAll(filterLabel, todayBtn, monthBtn, overallBtn, spacer, activeFilterLabel);
        
        box.getChildren().addAll(filterBox, labelContainer, statsContainer);
        
        return box;
    }
    
    private void setButtonActive(Button active, Button... inactive) {
        // Remove mouse handlers from all buttons first
        for (Button btn : inactive) {
            btn.setOnMouseEntered(null);
            btn.setOnMouseExited(null);
        }
        active.setOnMouseEntered(null);
        active.setOnMouseExited(null);
        
        // Set active button style
        active.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-padding: 6 12; " +
            "-fx-background-color: " + getButtonBgColor() + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-color: " + getButtonBgColor() + "; " +
            "-fx-border-radius: 3;"
        );
        
        // Set inactive button styles with hover effects
        for (Button btn : inactive) {
            String inactiveBg = ThemeManager.isDarkMode() ? "#333333" : "#ffffff";
            String inactiveText = ThemeManager.isDarkMode() ? "#e0e0e0" : "#1e3c72";
            String inactiveBorder = ThemeManager.isDarkMode() ? "#555555" : "#d0d0d0";
            String hoverBg = ThemeManager.isDarkMode() ? "#404040" : "#f0f0f0";
            String hoverBorder = ThemeManager.isDarkMode() ? "#666666" : "#b0b0b0";
            
            btn.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-padding: 6 12; " +
                "-fx-background-color: " + inactiveBg + "; " +
                "-fx-text-fill: " + inactiveText + "; " +
                "-fx-border-color: " + inactiveBorder + "; " +
                "-fx-border-radius: 3;"
            );
            
            btn.setOnMouseEntered(evt -> btn.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-padding: 6 12; " +
                "-fx-background-color: " + hoverBg + "; " +
                "-fx-text-fill: " + inactiveText + "; " +
                "-fx-border-color: " + hoverBorder + "; " +
                "-fx-border-radius: 3;"
            ));
            
            btn.setOnMouseExited(evt -> btn.setStyle(
                "-fx-font-size: 11px; " +
                "-fx-padding: 6 12; " +
                "-fx-background-color: " + inactiveBg + "; " +
                "-fx-text-fill: " + inactiveText + "; " +
                "-fx-border-color: " + inactiveBorder + "; " +
                "-fx-border-radius: 3;"
            ));
        }
    }
    
    private void updateSalesStats(VBox statsContainer, VBox labelContainer, LocalDate startDate, LocalDate endDate) {
        statsContainer.getChildren().clear();
        labelContainer.getChildren().clear();
        
        SalesSummaryReport summary = reportGenerator.getSalesSummary(startDate, endDate);
        
        Label totalRevenueLabel = new Label(String.format("Total Revenue: ₱%.2f", summary.getTotalRevenue()));
        totalRevenueLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0d6a1e;");
        
        Label totalOrdersLabel = new Label("Total Orders: " + summary.getTotalOrders());
        totalOrdersLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Label averageLabel = new Label(String.format("Average Order Value: ₱%.2f", summary.getAverageOrderValue()));
        averageLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        statsContainer.getChildren().addAll(totalRevenueLabel, totalOrdersLabel, averageLabel);
        
        Label periodLabel = new Label("Period: " + startDate + " to " + endDate);
        periodLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getDescLabelColor() + ";");
        
        labelContainer.getChildren().add(periodLabel);
    }
    
    private VBox createCompletedOrdersTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        // Add filter buttons for date range
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Button todayBtn = new Button("Today");
        Button monthBtn = new Button("This Month");
        Button overallBtn = new Button("Overall");
        
        Label activeFilterLabel = new Label("(Showing: This Month)");
        activeFilterLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getReportTitleColor() + "; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Count label and table
        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #388e3c;");
        
        TableView<ReservationReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<ReservationReport, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        idCol.setPrefWidth(80);
        
        TableColumn<ReservationReport, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(100);
        
        table.getColumns().add(idCol);
        table.getColumns().add(studentCol);
        table.getColumns().add(itemCol);
        table.getColumns().add(priceCol);
        
        // Load data function
        Runnable loadData = () -> {
            LocalDate startDate = LocalDate.now().minusMonths(1);
            LocalDate endDate = LocalDate.now();
            List<ReservationReport> completedOrders = reportGenerator.getCompletedOrders(startDate, endDate);
            countLabel.setText("Completed Orders: " + completedOrders.size());
            table.setItems(FXCollections.observableArrayList(completedOrders));
        };
        
        // Initial load - This Month
        setButtonActive(monthBtn, todayBtn, overallBtn);
        loadData.run();
        
        // Button actions
        todayBtn.setOnAction(e -> {
            setButtonActive(todayBtn, monthBtn, overallBtn);
            activeFilterLabel.setText("(Showing: Today)");
            LocalDate now = LocalDate.now();
            List<ReservationReport> orders = reportGenerator.getCompletedOrders(now, now);
            countLabel.setText("Completed Orders: " + orders.size());
            table.setItems(FXCollections.observableArrayList(orders));
        });
        
        monthBtn.setOnAction(e -> {
            setButtonActive(monthBtn, todayBtn, overallBtn);
            activeFilterLabel.setText("(Showing: This Month)");
            LocalDate startDate = LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = LocalDate.now();
            List<ReservationReport> orders = reportGenerator.getCompletedOrders(startDate, endDate);
            countLabel.setText("Completed Orders: " + orders.size());
            table.setItems(FXCollections.observableArrayList(orders));
        });
        
        overallBtn.setOnAction(e -> {
            setButtonActive(overallBtn, todayBtn, monthBtn);
            activeFilterLabel.setText("(Showing: Overall)");
            LocalDate startDate = LocalDate.now().minusYears(10);
            LocalDate endDate = LocalDate.now();
            List<ReservationReport> orders = reportGenerator.getCompletedOrders(startDate, endDate);
            countLabel.setText("Completed Orders: " + orders.size());
            table.setItems(FXCollections.observableArrayList(orders));
        });
        
        filterBox.getChildren().addAll(filterLabel, todayBtn, monthBtn, overallBtn, spacer, activeFilterLabel);
        box.getChildren().addAll(filterBox, countLabel, table);
        
        return box;
    }
    
    private VBox createCancelledOrdersTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        // Add filter buttons for date range
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Button todayBtn = new Button("Today");
        Button monthBtn = new Button("This Month");
        Button overallBtn = new Button("Overall");
        
        Label activeFilterLabel = new Label("(Showing: This Month)");
        activeFilterLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + getReportTitleColor() + "; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Count label and table
        Label countLabel = new Label();
        countLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
        
        TableView<ReservationReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<ReservationReport, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        idCol.setPrefWidth(80);
        
        TableColumn<ReservationReport, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(100);
        
        table.getColumns().add(idCol);
        table.getColumns().add(studentCol);
        table.getColumns().add(itemCol);
        table.getColumns().add(priceCol);
        
        // Function to update display
        java.util.function.BiConsumer<LocalDate, LocalDate> updateDisplay = (startDate, endDate) -> {
            List<ReservationReport> cancelledOrders = reportGenerator.getCancelledOrders(startDate, endDate);
            countLabel.setText("Cancelled Orders: " + cancelledOrders.size());
            table.setItems(FXCollections.observableArrayList(cancelledOrders));
        };
        
        // Initial load - This Month
        setButtonActive(monthBtn, todayBtn, overallBtn);
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        updateDisplay.accept(startMonth, LocalDate.now());
        
        // Button actions
        todayBtn.setOnAction(e -> {
            setButtonActive(todayBtn, monthBtn, overallBtn);
            activeFilterLabel.setText("(Showing: Today)");
            LocalDate now = LocalDate.now();
            updateDisplay.accept(now, now);
        });
        
        monthBtn.setOnAction(e -> {
            setButtonActive(monthBtn, todayBtn, overallBtn);
            activeFilterLabel.setText("(Showing: This Month)");
            LocalDate start = LocalDate.now().withDayOfMonth(1);
            updateDisplay.accept(start, LocalDate.now());
        });
        
        overallBtn.setOnAction(e -> {
            setButtonActive(overallBtn, todayBtn, monthBtn);
            activeFilterLabel.setText("(Showing: Overall)");
            updateDisplay.accept(LocalDate.now().minusYears(10), LocalDate.now());
        });
        
        filterBox.getChildren().addAll(filterLabel, todayBtn, monthBtn, overallBtn, spacer, activeFilterLabel);
        box.getChildren().addAll(filterBox, countLabel, table);
        
        return box;
    }
    
    private VBox createReturnTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        List<ReservationReport> returns = reportGenerator.getReturnReport();
        
        if (returns.isEmpty()) {
            Label emptyLabel = new Label("No replaced records");
            return new VBox(emptyLabel);
        }
        
        TableView<ReservationReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<ReservationReport, Integer> idCol = new TableColumn<>("Return ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        idCol.setPrefWidth(80);
        
        TableColumn<ReservationReport, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        studentCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemCol.setPrefWidth(150);
        
        TableColumn<ReservationReport, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        table.getColumns().add(idCol);
        table.getColumns().add(studentCol);
        table.getColumns().add(itemCol);
        table.getColumns().add(statusCol);
        table.setItems(FXCollections.observableArrayList(returns));
        
        box.getChildren().add(table);
        
        return box;
    }
    
    /**
     * Create student activity report view
     */
    public VBox createStudentActivityReport() {
        VBox reportBox = new VBox(15);
        reportBox.setPadding(new Insets(20));
        
        // Header with title and export buttons
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("👥 Student Activity Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Button exportPdfBtn = createExportButton("📄 Export as PDF");
        Button exportExcelBtn = createExportButton("📊 Export as Excel");
        
        exportPdfBtn.setOnAction(e -> exportStudentReport("PDF"));
        exportExcelBtn.setOnAction(e -> exportStudentReport("EXCEL"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportPdfBtn, exportExcelBtn);
        reportBox.getChildren().add(headerBox);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab topStudentsTab = new Tab("Top Students", createTopStudentsTab());
        Tab accountStatusTab = new Tab("Account Status", createAccountStatusTab());
        Tab distributionTab = new Tab("Student Distribution", createDistributionTab());
        
        tabPane.getTabs().addAll(topStudentsTab, accountStatusTab, distributionTab);
        reportBox.getChildren().add(tabPane);
        
        return reportBox;
    }
    
    private void exportStudentReport(String format) {
        try {
            List<StudentActivityReport> data = reportGenerator.getTopStudents(100);
            String filename = "student_activity_report_" + LocalDate.now() + "." + (format.equals("PDF") ? "txt" : "csv");
            
            if (format.equals("PDF")) {
                utils.PDFExporter.exportStudentActivityToPDF(data, filename);
            } else {
                utils.ExcelExporter.exportStudentActivityToExcel(data, filename);
            }
            
            showExportSuccess("reports/" + filename);
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
        }
    }
    
    /**
     * Create staff report view (includes Returns + Stock Management + Transaction Summary)
     */
    public VBox createStaffReport() {
        VBox reportBox = new VBox(15);
        reportBox.setPadding(new Insets(20));
        
        // Header with title and export buttons
        HBox headerBox = new HBox(15);
        headerBox.setPadding(new Insets(10));
        
        Label titleLabel = new Label("👔 Staff Report");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + getReportTitleColor() + ";");
        
        Button exportPdfBtn = createExportButton("📄 Export as PDF");
        Button exportExcelBtn = createExportButton("📊 Export as Excel");
        
        exportPdfBtn.setOnAction(e -> exportStaffReport("PDF"));
        exportExcelBtn.setOnAction(e -> exportStaffReport("EXCEL"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportPdfBtn, exportExcelBtn);
        reportBox.getChildren().add(headerBox);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Replaced Management (primary for staff)
        Tab returnsTab = new Tab("Replaced", createReturnTab());
        
        // Stock Management (staff manages inventory)
        Tab stockByCoursTab = new Tab("Stock by Course", createStockByCourseTab());
        Tab lowStockTab = new Tab("Low Stock Items", createLowStockTab());
        Tab outOfStockTab = new Tab("Out of Stock", createOutOfStockTab());
        Tab allReplacementsTab = new Tab("All Replacements", createAllReplacementsTab());
        
        // Transaction Summary (staff awareness)
        Tab transactionTab = new Tab("Sales Summary", createSalesSummaryTab());
        Tab completedTab = new Tab("Completed Orders", createCompletedOrdersTab());
        
        tabPane.getTabs().addAll(returnsTab, stockByCoursTab, lowStockTab, outOfStockTab, 
                                 allReplacementsTab, transactionTab, completedTab);
        reportBox.getChildren().add(tabPane);
        
        return reportBox;
    }
    
    private void exportStaffReport(String format) {
        try {
            LocalDate startDate = LocalDate.now().minusMonths(1);
            LocalDate endDate = LocalDate.now();
            
            // Gather all staff report data
            List<ReservationReport> returns = reportGenerator.getReturnReport();
            List<StockReport> stockByCourse = reportGenerator.getStockByCourse();
            List<StockReport> lowStockItems = reportGenerator.getLowStockItems(10);
            List<StockReport> outOfStockItems = reportGenerator.getOutOfStockItems();
            List<DamagedStockTracker.DamagedStockRecord> damagedStock = DamagedStockTracker.getAllDamagedRecords();
            SalesSummaryReport summary = reportGenerator.getSalesSummary(startDate, endDate);
            List<ReservationReport> completedOrders = reportGenerator.getCompletedOrders(startDate, endDate);
            
            String filename = "staff_report_" + LocalDate.now();
            
            if (format.equals("PDF")) {
                utils.PDFExporter.exportStaffReportToPDF(returns, stockByCourse, lowStockItems, 
                    outOfStockItems, damagedStock, summary, completedOrders, filename);
                showExportSuccess("reports/staff_report_" + LocalDate.now() + ".pdf");
            } else {
                utils.ExcelExporter.exportStaffReportToExcel(returns, stockByCourse, lowStockItems, 
                    outOfStockItems, damagedStock, summary, completedOrders, filename);
                showExportSuccess("reports/staff_report_" + LocalDate.now() + ".csv");
            }
        } catch (Exception e) {
            showError("Export failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createTopStudentsTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        List<StudentActivityReport> topStudents = reportGenerator.getTopStudents(10);
        
        TableView<StudentActivityReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<StudentActivityReport, String> nameCol = new TableColumn<>("Student Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        nameCol.setPrefWidth(200);
        
        TableColumn<StudentActivityReport, Integer> purchaseCol = new TableColumn<>("Order Count");
        purchaseCol.setCellValueFactory(new PropertyValueFactory<>("orderCount"));
        purchaseCol.setPrefWidth(100);
        
        TableColumn<StudentActivityReport, Double> spentCol = new TableColumn<>("Total Spent");
        spentCol.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        spentCol.setPrefWidth(150);
        
        table.getColumns().add(nameCol);
        table.getColumns().add(purchaseCol);
        table.getColumns().add(spentCol);
        table.setItems(FXCollections.observableArrayList(topStudents));
        
        box.getChildren().add(table);
        
        return box;
    }
    
    private VBox createAccountStatusTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        Map<String, Integer> accountStatus = reportGenerator.getAccountActivityStatus();
        
        VBox statsBox = new VBox(10);
        statsBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-padding: 15;");
        
        Label activeLabel = new Label("Active Accounts: " + accountStatus.getOrDefault("Active", 0));
        activeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0d6a1e;");
        
        Label inactiveLabel = new Label("Inactive Accounts: " + accountStatus.getOrDefault("Inactive", 0));
        inactiveLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #b71c1c;");
        
        statsBox.getChildren().addAll(activeLabel, inactiveLabel);
        
        box.getChildren().add(statsBox);
        
        return box;
    }
    
    private VBox createDistributionTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        Map<String, Integer> distribution = reportGenerator.getStudentDistributionByCourse();
        
        TableView<Map.Entry<String, Integer>> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<Map.Entry<String, Integer>, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        courseCol.setPrefWidth(200);
        
        TableColumn<Map.Entry<String, Integer>, Integer> countCol = new TableColumn<>("Student Count");
        countCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getValue()).asObject());
        countCol.setPrefWidth(150);
        
        table.getColumns().add(courseCol);
        table.getColumns().add(countCol);
        table.setItems(FXCollections.observableArrayList(distribution.entrySet()));
        
        box.getChildren().add(table);
        
        return box;
    }
    
    /**
     * Create Damaged Stock tab - shows items that were replaced due to damage
     */
    private VBox createDamagedStockTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        // Description label
        Label descLabel = new Label("Items replaced due to damage with optional image proof:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getDescLabelColor() + ";");
        
        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));
        searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by student or item name...");
        searchField.setPrefWidth(300);
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12; -fx-background-color: " + getButtonBgColor() + "; -fx-text-fill: white;");
        
        searchBox.getChildren().addAll(new Label("Search:"), searchField, refreshBtn);
        
        // Stats label
        List<DamagedStockRecord> allRecords = DamagedStockTracker.getAllDamagedRecords();
        Label statsLabel = new Label("Total damaged items tracked: " + allRecords.size());
        statsLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #b71c1c;");
        
        // Table for damaged stock records
        TableView<DamagedStockRecord> table = new TableView<>();
        table.setPrefHeight(350);
        
        TableColumn<DamagedStockRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTimestamp()));
        dateCol.setPrefWidth(130);
        
        TableColumn<DamagedStockRecord, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getStudentName() + " (" + data.getValue().getStudentId() + ")"
        ));
        studentCol.setPrefWidth(150);
        
        TableColumn<DamagedStockRecord, String> originalCol = new TableColumn<>("Original Item");
        originalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getOriginalItemName() + " (" + data.getValue().getOriginalSize() + ")"
        ));
        originalCol.setPrefWidth(180);
        
        TableColumn<DamagedStockRecord, String> replacementCol = new TableColumn<>("Replacement");
        replacementCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getReplacementItemName() + " (" + data.getValue().getReplacementSize() + ")"
        ));
        replacementCol.setPrefWidth(180);
        
        TableColumn<DamagedStockRecord, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getReason() != null ? data.getValue().getReason() : "N/A"
        ));
        reasonCol.setPrefWidth(120);
        
        TableColumn<DamagedStockRecord, Void> imageCol = new TableColumn<>("Image");
        imageCol.setCellFactory(col -> new javafx.scene.control.TableCell<DamagedStockRecord, Void>() {
            private final Button viewBtn = new Button("📷 View");
            
            {
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    DamagedStockRecord record = getTableView().getItems().get(getIndex());
                    showDamageImage(record);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DamagedStockRecord record = getTableView().getItems().get(getIndex());
                    if (record.hasImage()) {
                        setGraphic(viewBtn);
                    } else {
                        Label noImg = new Label("No image");
                        noImg.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                        setGraphic(noImg);
                    }
                }
            }
        });
        imageCol.setPrefWidth(90);
        
        table.getColumns().add(dateCol);
        table.getColumns().add(studentCol);
        table.getColumns().add(originalCol);
        table.getColumns().add(replacementCol);
        table.getColumns().add(reasonCol);
        table.getColumns().add(imageCol);
        
        // Initial load
        table.setItems(FXCollections.observableArrayList(allRecords));
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<DamagedStockRecord> filtered = DamagedStockTracker.searchRecords(newVal);
            table.setItems(FXCollections.observableArrayList(filtered));
        });
        
        // Refresh button
        refreshBtn.setOnAction(e -> {
            List<DamagedStockRecord> refreshed = DamagedStockTracker.getAllDamagedRecords();
            table.setItems(FXCollections.observableArrayList(refreshed));
            statsLabel.setText("Total damaged items tracked: " + refreshed.size());
            searchField.clear();
        });
        
        // Placeholder when no records
        table.setPlaceholder(new Label("No damaged stock records found."));
        
        box.getChildren().addAll(descLabel, statsLabel, searchBox, table);
        
        return box;
    }
    
    /**
     * Show damage image in a dialog
     */
    private void showDamageImage(DamagedStockRecord record) {
        if (!record.hasImage()) {
            showError("No image available for this record.");
            return;
        }
        
        try {
            File imageFile = new File(record.getImagePath());
            if (!imageFile.exists()) {
                showError("Image file not found: " + record.getImagePath());
                return;
            }
            
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Damage Proof Image");
            dialog.setHeaderText("Damaged Item: " + record.getOriginalItemName() + " (" + record.getOriginalSize() + ")");
            
            javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            
            // Scale image to fit dialog while maintaining aspect ratio
            double maxWidth = 600;
            double maxHeight = 500;
            if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
                imageView.setFitWidth(maxWidth);
                imageView.setFitHeight(maxHeight);
                imageView.setPreserveRatio(true);
            }
            
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Info labels
            Label studentLabel = new Label("Student: " + record.getStudentName() + " (" + record.getStudentId() + ")");
            studentLabel.setStyle("-fx-font-size: 13px;");
            
            Label reasonLabel = new Label("Reason: " + (record.getReason() != null ? record.getReason() : "Not specified"));
            reasonLabel.setStyle("-fx-font-size: 13px;");
            
            Label dateLabel = new Label("Date: " + record.getTimestamp());
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            Label processedLabel = new Label("Processed by: " + (record.getProcessedBy() != null ? record.getProcessedBy() : "Unknown"));
            processedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            content.getChildren().addAll(studentLabel, reasonLabel, dateLabel, processedLabel, imageView);
            
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(650, 600);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error loading image: " + e.getMessage());
        }
    }
    
    private void showExportSuccess(String filepath) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export Successful");
        alert.setHeaderText("Report Exported");
        alert.setContentText("Report saved to: " + filepath);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Create All Replacements tab - shows all replacement requests with reason categorization
     * Tracks: Wrong Size, Damaged/Defective, Wrong Item, Poor Quality, Color/Design, Size Fit, Other
     */
    private VBox createAllReplacementsTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        
        // Description label
        Label descLabel = new Label("All replacement requests with categorized reasons:");
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + getDescLabelColor() + ";");
        
        // Get all reservations for complete replacement data
        java.util.List<inventory.Reservation> allReservations = reservationManager.getAllReservations();
        
        // Get replacement summary for stats (including reservation data)
        ReplacementSummary summary = ReplacementTracker.getSummaryWithReservations(allReservations);
        
        // Stats display - summary cards showing counts by reason
        HBox statsRow = new HBox(10);
        statsRow.setPadding(new Insets(10));
        statsRow.setStyle("-fx-background-color: " + getSearchBoxBgColor() + "; -fx-background-radius: 5;");
        
        // Total replacements card
        VBox totalCard = createReasonCard("📊 Total", summary.getTotalReplacements(), "#0969DA");
        
        // Create cards for each reason
        VBox wrongSizeCard = createReasonCard(ReplacementReason.WRONG_SIZE.getIcon() + " Wrong Size", 
            summary.getCount(ReplacementReason.WRONG_SIZE), ReplacementReason.WRONG_SIZE.getColor());
        VBox damagedCard = createReasonCard(ReplacementReason.DAMAGED_DEFECTIVE.getIcon() + " Damaged", 
            summary.getCount(ReplacementReason.DAMAGED_DEFECTIVE), ReplacementReason.DAMAGED_DEFECTIVE.getColor());
        VBox wrongItemCard = createReasonCard(ReplacementReason.WRONG_ITEM.getIcon() + " Wrong Item", 
            summary.getCount(ReplacementReason.WRONG_ITEM), ReplacementReason.WRONG_ITEM.getColor());
        VBox qualityCard = createReasonCard(ReplacementReason.POOR_QUALITY.getIcon() + " Poor Quality", 
            summary.getCount(ReplacementReason.POOR_QUALITY), ReplacementReason.POOR_QUALITY.getColor());
        VBox colorCard = createReasonCard(ReplacementReason.COLOR_DESIGN.getIcon() + " Color/Design", 
            summary.getCount(ReplacementReason.COLOR_DESIGN), ReplacementReason.COLOR_DESIGN.getColor());
        VBox sizeFitCard = createReasonCard(ReplacementReason.SIZE_FIT.getIcon() + " Size Fit", 
            summary.getCount(ReplacementReason.SIZE_FIT), ReplacementReason.SIZE_FIT.getColor());
        VBox otherCard = createReasonCard(ReplacementReason.OTHER.getIcon() + " Other", 
            summary.getCount(ReplacementReason.OTHER), ReplacementReason.OTHER.getColor());
        
        statsRow.getChildren().addAll(totalCard, wrongSizeCard, damagedCard, wrongItemCard, 
                                      qualityCard, colorCard, sizeFitCard, otherCard);
        
        // Search and filter bar
        HBox filterBox = new HBox(10);
        filterBox.setPadding(new Insets(10));
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        filterBox.setStyle("-fx-border-color: " + getSearchBoxBorderColor() + "; -fx-border-radius: 5; -fx-background-color: " + getSearchBoxBgColor() + ";");
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by student or item name...");
        searchField.setPrefWidth(250);
        
        // Filter dropdown for reason
        ComboBox<String> reasonFilter = new ComboBox<>();
        reasonFilter.getItems().add("All Reasons");
        for (ReplacementReason reason : ReplacementReason.values()) {
            reasonFilter.getItems().add(reason.getIcon() + " " + reason.getDisplayName());
        }
        reasonFilter.setValue("All Reasons");
        reasonFilter.setPrefWidth(180);
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle("-fx-font-size: 12px; -fx-padding: 6 12; -fx-background-color: " + getButtonBgColor() + "; -fx-text-fill: white;");
        
        filterBox.getChildren().addAll(new Label("Search:"), searchField, new Label("Filter:"), reasonFilter, refreshBtn);
        
        // Table for replacement records
        TableView<ReplacementRecord> table = new TableView<>();
        table.setPrefHeight(350);
        
        TableColumn<ReplacementRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTimestamp()));
        dateCol.setPrefWidth(130);
        
        TableColumn<ReplacementRecord, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getStudentName() + " (" + data.getValue().getStudentId() + ")"
        ));
        studentCol.setPrefWidth(150);
        
        TableColumn<ReplacementRecord, String> originalCol = new TableColumn<>("Original Item");
        originalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getOriginalItemName() + " (" + data.getValue().getOriginalSize() + ")"
        ));
        originalCol.setPrefWidth(160);
        
        TableColumn<ReplacementRecord, String> replacementCol = new TableColumn<>("Replacement");
        replacementCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getReplacementItemName() + " (" + data.getValue().getReplacementSize() + ")"
        ));
        replacementCol.setPrefWidth(160);
        
        TableColumn<ReplacementRecord, String> reasonsCol = new TableColumn<>("Reasons");
        reasonsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getFormattedReasons()
        ));
        reasonsCol.setPrefWidth(180);
        
        TableColumn<ReplacementRecord, Void> imageCol = new TableColumn<>("Image");
        imageCol.setCellFactory(col -> new javafx.scene.control.TableCell<ReplacementRecord, Void>() {
            private final Button viewBtn = new Button("📷 View");
            
            {
                viewBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8; -fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    ReplacementRecord record = getTableView().getItems().get(getIndex());
                    showReplacementImage(record);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReplacementRecord record = getTableView().getItems().get(getIndex());
                    if (record.hasImage()) {
                        setGraphic(viewBtn);
                    } else {
                        Label noImg = new Label("No image");
                        noImg.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                        setGraphic(noImg);
                    }
                }
            }
        });
        imageCol.setPrefWidth(90);
        
        table.getColumns().add(dateCol);
        table.getColumns().add(studentCol);
        table.getColumns().add(originalCol);
        table.getColumns().add(replacementCol);
        table.getColumns().add(reasonsCol);
        table.getColumns().add(imageCol);
        
        // Initial load (including reservation data)
        List<ReplacementRecord> allRecords = ReplacementTracker.getAllRecordsWithReservations(allReservations);
        table.setItems(FXCollections.observableArrayList(allRecords));
        
        // Filter logic - capture allReservations for use in lambda
        final java.util.List<inventory.Reservation> reservationsForFilter = allReservations;
        Runnable applyFilters = () -> {
            String searchText = searchField.getText().toLowerCase().trim();
            String reasonValue = reasonFilter.getValue();
            
            // Get all records including from reservations
            List<ReplacementRecord> filtered = ReplacementTracker.getAllRecordsWithReservations(reservationsForFilter);
            
            // Apply reason filter
            if (!"All Reasons".equals(reasonValue)) {
                // Find the matching reason enum
                for (ReplacementReason reason : ReplacementReason.values()) {
                    if ((reason.getIcon() + " " + reason.getDisplayName()).equals(reasonValue)) {
                        final ReplacementReason matchedReason = reason;
                        filtered = filtered.stream()
                            .filter(r -> r.hasReason(matchedReason))
                            .collect(java.util.stream.Collectors.toList());
                        break;
                    }
                }
            }
            
            // Apply search filter
            if (!searchText.isEmpty()) {
                final List<ReplacementRecord> reasonFiltered = filtered;
                filtered = reasonFiltered.stream()
                    .filter(r -> r.getStudentName().toLowerCase().contains(searchText) ||
                                r.getStudentId().toLowerCase().contains(searchText) ||
                                r.getOriginalItemName().toLowerCase().contains(searchText) ||
                                r.getReplacementItemName().toLowerCase().contains(searchText))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            table.setItems(FXCollections.observableArrayList(filtered));
        };
        
        // Event handlers
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        reasonFilter.setOnAction(e -> applyFilters.run());
        refreshBtn.setOnAction(e -> {
            searchField.clear();
            reasonFilter.setValue("All Reasons");
            table.setItems(FXCollections.observableArrayList(
                ReplacementTracker.getAllRecordsWithReservations(reservationsForFilter)));
        });
        
        // Placeholder when no records
        table.setPlaceholder(new Label("No replacement records found."));
        
        box.getChildren().addAll(descLabel, statsRow, filterBox, table);
        
        return box;
    }
    
    /**
     * Helper to create a small stats card for reason counts
     */
    private VBox createReasonCard(String title, int count, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: " + color + "15; -fx-border-color: " + color + "; " +
                     "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");
        card.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
        
        Label countLabel = new Label(String.valueOf(count));
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, countLabel);
        return card;
    }
    
    /**
     * Show replacement image in a dialog
     */
    private void showReplacementImage(ReplacementRecord record) {
        if (!record.hasImage()) {
            showError("No image available for this record.");
            return;
        }
        
        try {
            File imageFile = new File(record.getImagePath());
            if (!imageFile.exists()) {
                showError("Image file not found: " + record.getImagePath());
                return;
            }
            
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Replacement Proof Image");
            dialog.setHeaderText("Item: " + record.getOriginalItemName() + " (" + record.getOriginalSize() + ")");
            
            javafx.scene.image.Image image = new javafx.scene.image.Image(imageFile.toURI().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            
            // Scale image to fit dialog while maintaining aspect ratio
            double maxWidth = 600;
            double maxHeight = 500;
            if (image.getWidth() > maxWidth || image.getHeight() > maxHeight) {
                imageView.setFitWidth(maxWidth);
                imageView.setFitHeight(maxHeight);
                imageView.setPreserveRatio(true);
            }
            
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setAlignment(javafx.geometry.Pos.CENTER);
            
            // Info labels
            Label studentLabel = new Label("Student: " + record.getStudentName() + " (" + record.getStudentId() + ")");
            studentLabel.setStyle("-fx-font-size: 13px;");
            
            Label reasonsLabel = new Label("Reasons: " + record.getFormattedReasons());
            reasonsLabel.setStyle("-fx-font-size: 13px;");
            
            Label dateLabel = new Label("Date: " + record.getTimestamp());
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            Label processedLabel = new Label("Processed by: " + (record.getProcessedBy() != null ? record.getProcessedBy() : "Unknown"));
            processedLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            
            content.getChildren().addAll(studentLabel, reasonsLabel, dateLabel, processedLabel, imageView);
            
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(650, 600);
            
            dialog.getDialogPane().setContent(scrollPane);
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            dialog.showAndWait();
            
        } catch (Exception e) {
            showError("Error loading image: " + e.getMessage());
        }
    }
}
