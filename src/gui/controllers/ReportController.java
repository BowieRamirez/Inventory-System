package gui.controllers;

import gui.utils.ThemeManager;
import utils.ReportGenerator;
import utils.ReportGenerator.*;
import inventory.InventoryManager;
import inventory.ReceiptManager;
import inventory.ReservationManager;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import java.util.*;

/**
 * ReportController - Manages report generation and display
 */
public class ReportController {
    
    private ReportGenerator reportGenerator;
    
    public ReportController(InventoryManager inventoryManager,
                           ReservationManager reservationManager,
                           ReceiptManager receiptManager) {
        this.reportGenerator = new ReportGenerator(inventoryManager, reservationManager, receiptManager);
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
        
        tabPane.getTabs().addAll(stockByCoursTab, lowStockTab, outOfStockTab, valuationTab);
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
            List<StockReport> data = reportGenerator.getStockByCourse();
            String filename = "stock_report_" + LocalDate.now() + "." + (format.equals("PDF") ? "txt" : "csv");
            String filepath = "reports/" + filename;
            
            if (format.equals("PDF")) {
                utils.PDFExporter.exportStockReportToPDF(data, filename);
            } else {
                utils.ExcelExporter.exportStockReportToExcel(data, filename);
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
        
        TableColumn<StockReport, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);
        
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
        
        TableColumn<StockReport, Integer> quantityCol = new TableColumn<>("Stock Level");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        
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
        
        List<StockReport> outOfStockItems = reportGenerator.getOutOfStockItems();
        
        TableView<StockReport> table = new TableView<>();
        table.setPrefHeight(300);
        
        TableColumn<StockReport, String> itemCol = new TableColumn<>("Item Name");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        itemCol.setPrefWidth(300);
        
        table.getColumns().add(itemCol);
        table.setItems(FXCollections.observableArrayList(outOfStockItems));
        
        Label countLabel = new Label("Out of Stock Items: " + outOfStockItems.size());
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
        
        TableColumn<StockValuationReport, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityCol.setPrefWidth(100);
        
        TableColumn<StockValuationReport, Double> priceCol = new TableColumn<>("Unit Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        priceCol.setPrefWidth(100);
        
        TableColumn<StockValuationReport, Double> valueCol = new TableColumn<>("Total Value");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("totalValue"));
        valueCol.setPrefWidth(100);
        
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
     * Create transaction/sales report view
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
        Tab returnTab = new Tab("Returns", createReturnTab());
        
        tabPane.getTabs().addAll(summaryTab, completedTab, cancelledTab, returnTab);
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
            Label emptyLabel = new Label("No returns on record");
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
}
