package gui.views;

import gui.controllers.ReportController;
import gui.utils.ThemeManager;
import inventory.InventoryManager;
import inventory.ReceiptManager;
import inventory.ReservationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * ReportView - Main reports interface for dashboards
 */
public class ReportView {
    
    private VBox view;
    private ReportController controller;
    private StackPane contentArea;
    private Runnable themeRefreshListener;
    private boolean staffMode; // If true, show only Stock report directly (no selection)
    
    // Track current report type
    private enum CurrentReport {
        SELECTION, STOCK, TRANSACTION, STUDENT
    }
    private CurrentReport currentReport = CurrentReport.SELECTION;
    
    /**
     * Constructor for Admin mode - shows all report types with selection screen
     */
    public ReportView(InventoryManager inventoryManager,
                     ReservationManager reservationManager,
                     ReceiptManager receiptManager) {
        this(inventoryManager, reservationManager, receiptManager, false);
    }
    
    /**
     * Constructor with mode selection
     * @param staffMode if true, shows Stock Availability report directly (for Staff)
     */
    public ReportView(InventoryManager inventoryManager,
                     ReservationManager reservationManager,
                     ReceiptManager receiptManager,
                     boolean staffMode) {
        this.controller = new ReportController(inventoryManager, reservationManager, receiptManager);
        this.staffMode = staffMode;
        initializeView();
        registerThemeListener();
    }
    
    private void initializeView() {
        view = new VBox(0);
        updateViewStyles();
        
        // Header
        VBox header = createHeader();
        VBox.setVgrow(header, Priority.NEVER);
        
        // Content area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        updateContentAreaStyles();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // In staffMode, show Stock report directly; otherwise show selection
        if (staffMode) {
            showStockReportDirect();
        } else {
            showReportSelection();
        }
        
        view.getChildren().addAll(header, new Separator(), contentArea);
    }
    
    private void updateViewStyles() {
        view.setStyle("-fx-background-color: " + ThemeManager.getBackgroundColor() + ";");
    }
    
    private void updateContentAreaStyles() {
        contentArea.setStyle("-fx-background-color: " + ThemeManager.getBackgroundColor() + ";");
    }
    
    private void registerThemeListener() {
        themeRefreshListener = () -> {
            updateViewStyles();
            updateContentAreaStyles();
            // In staffMode, always show stock report directly
            if (staffMode) {
                showStockReportDirect();
                return;
            }
            // Refresh based on which report is currently showing
            switch (currentReport) {
                case STOCK:
                    showStockReport();
                    break;
                case STUDENT:
                    showStudentReport();
                    break;
                case TRANSACTION:
                    showTransactionReport();
                    break;
                case SELECTION:
                default:
                    showReportSelection();
                    break;
            }
        };
        ThemeManager.addThemeChangeListener(themeRefreshListener);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(20));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, #1e3c72 0%, #2a5298 50%, #1e3c72 100%);"
        );
        
        Label titleLabel = new Label(staffMode ? "📦 Stock Availability Report" : "📊 Reports & Analytics");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: white;");
        
        Label descLabel = new Label(staffMode 
            ? "View current stock levels, low stock alerts, and inventory valuations"
            : "Generate comprehensive reports on stock, sales, and student activity");
        descLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9); -fx-font-size: 12px;");
        
        header.getChildren().addAll(titleLabel, descLabel);
        
        return header;
    }
    
    private void showReportSelection() {
        currentReport = CurrentReport.SELECTION;
        contentArea.getChildren().clear();
        
        VBox selectionBox = new VBox(20);
        selectionBox.setPadding(new Insets(40));
        selectionBox.setAlignment(Pos.TOP_CENTER);
        
        Label selectLabel = new Label("Select a Report Type");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        selectLabel.setStyle("-fx-text-fill: " + getTextColor() + ";");
        
        // Report buttons grid
        HBox buttonsRow1 = new HBox(20);
        buttonsRow1.setAlignment(Pos.CENTER);
        
        Button stockBtn = createReportButton(
            "📦 Stock Availability",
            "View current stock levels,\nlow stock alerts, and valuations",
            () -> showStockReport()
        );
        
        Button transactionBtn = createReportButton(
            "💳 Transaction & Sales",
            "View sales summary, order\nhistory, and transaction reports",
            () -> showTransactionReport()
        );
        
        Button studentBtn = createReportButton(
            "👥 Student Activity",
            "View top students and\naccount activity statistics",
            () -> showStudentReport()
        );
        
        buttonsRow1.getChildren().addAll(stockBtn, transactionBtn, studentBtn);
        
        selectionBox.getChildren().addAll(selectLabel, buttonsRow1);
        
        contentArea.getChildren().add(selectionBox);
    }
    
    private String getTextColor() {
        return ThemeManager.isDarkMode() ? "#d0d0e0" : "#1e3c72";
    }
    
    private String getButtonBgColor() {
        // Dark mode: use a deep navy blue instead of violet/gray
        return ThemeManager.isDarkMode() ? "#16233a" : "#ffffff";
    }
    
    private String getButtonHoverBgColor() {
        // Slightly lighter blue for hover in dark mode
        return ThemeManager.isDarkMode() ? "#27344f" : "#f0f0f0";
    }
    
    private String getButtonBorderColor() {
        // Subtle blue border in dark mode
        return ThemeManager.isDarkMode() ? "#24364f" : "#e0e0e0";
    }
    
    private String getButtonHoverBorderColor() {
        return ThemeManager.isDarkMode() ? "#2f4a6b" : "#d0d0d0";
    }
    
    private String getButtonTitleColor() {
        // Use a blue-tinted title color in dark mode
        return ThemeManager.isDarkMode() ? "#9fc5ff" : "#1e3c72";
    }
    
    private String getButtonDescColor() {
        return ThemeManager.isDarkMode() ? "#8fb6d6" : "#666666";
    }
    
    private Button createReportButton(String title, String description, Runnable onClick) {
        VBox buttonContent = new VBox(8);
        buttonContent.setPadding(new Insets(20));
        buttonContent.setAlignment(Pos.CENTER);
        buttonContent.setStyle(
            "-fx-background-color: " + getButtonBgColor() + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + getButtonBorderColor() + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: " + getButtonTitleColor() + ";");
        titleLabel.setWrapText(true);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: " + getButtonDescColor() + "; -fx-font-size: 11px;");
        descLabel.setWrapText(true);
        
        buttonContent.getChildren().addAll(titleLabel, descLabel);
        
        Button btn = new Button();
        btn.setGraphic(buttonContent);
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        btn.setPrefWidth(220);
        btn.setPrefHeight(140);
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        btn.setOnAction(e -> onClick.run());
        
        // Hover effect
        btn.setOnMouseEntered(e -> {
            buttonContent.setStyle(
                "-fx-background-color: " + getButtonHoverBgColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + getButtonHoverBorderColor() + ";" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 12px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);"
            );
        });
        
        btn.setOnMouseExited(e -> {
            buttonContent.setStyle(
                "-fx-background-color: " + getButtonBgColor() + ";" +
                "-fx-background-radius: 12px;" +
                "-fx-border-color: " + getButtonBorderColor() + ";" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 12px;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 2);"
            );
        });
        
        return btn;
    }
    
    private void showStockReport() {
        currentReport = CurrentReport.STOCK;
        contentArea.getChildren().clear();
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(0));
        
        // Back button
        Button backBtn = createBackButton();
        HBox backBar = new HBox(backBtn);
        backBar.setPadding(new Insets(15, 20, 0, 20));
        backBar.setStyle("-fx-background-color: transparent;");
        
        VBox report = controller.createStockAvailabilityReport();
        VBox.setVgrow(report, Priority.ALWAYS);
        
        container.getChildren().addAll(backBar, report);
        contentArea.getChildren().add(container);
    }
    
    /**
     * Show Stock report directly without back button (for Staff mode)
     */
    private void showStockReportDirect() {
        currentReport = CurrentReport.STOCK;
        contentArea.getChildren().clear();
        
        VBox report = controller.createStockAvailabilityReport();
        VBox.setVgrow(report, Priority.ALWAYS);
        
        contentArea.getChildren().add(report);
    }
    
    private void showTransactionReport() {
        currentReport = CurrentReport.TRANSACTION;
        contentArea.getChildren().clear();
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(0));
        
        // Back button
        Button backBtn = createBackButton();
        HBox backBar = new HBox(backBtn);
        backBar.setPadding(new Insets(15, 20, 0, 20));
        backBar.setStyle("-fx-background-color: transparent;");
        
        // Use staff version (without Returns) if in staff mode, otherwise use full version (with Returns)
        VBox report = staffMode ? controller.createTransactionReportStaff() : controller.createTransactionReport();
        VBox.setVgrow(report, Priority.ALWAYS);
        
        container.getChildren().addAll(backBar, report);
        contentArea.getChildren().add(container);
    }
    
    private void showStudentReport() {
        currentReport = CurrentReport.STUDENT;
        contentArea.getChildren().clear();
        
        VBox container = new VBox(10);
        container.setPadding(new Insets(0));
        
        // Back button
        Button backBtn = createBackButton();
        HBox backBar = new HBox(backBtn);
        backBar.setPadding(new Insets(15, 20, 0, 20));
        backBar.setStyle("-fx-background-color: transparent;");
        
        VBox report = controller.createStudentActivityReport();
        VBox.setVgrow(report, Priority.ALWAYS);
        
        container.getChildren().addAll(backBar, report);
        contentArea.getChildren().add(container);
    }
    
    private Button createBackButton() {
        Button backBtn = new Button("← Back to Reports");
        updateBackButtonStyle(backBtn, false);
        
        backBtn.setOnMouseEntered(e -> updateBackButtonStyle(backBtn, true));
        backBtn.setOnMouseExited(e -> updateBackButtonStyle(backBtn, false));
        backBtn.setOnAction(e -> showReportSelection());
        
        return backBtn;
    }
    
    private void updateBackButtonStyle(Button btn, boolean hovered) {
        String bgColor = hovered ? "#0d1f3c" : "#1e3c72";
        btn.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-padding: 8 15; " +
            "-fx-background-color: " + bgColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
    }
    
    public VBox getView() {
        return view;
    }
}
