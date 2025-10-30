package gui.views;

import gui.controllers.CashierDashboardController;
import gui.utils.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * CashierDashboard - Main dashboard for cashier users
 * 
 * Features:
 * - Process payments
 * - View approved reservations
 * - Generate receipts
 */
public class CashierDashboard {
    
    private BorderPane view;
    private VBox sidebar;
    private StackPane contentArea;
    private Label titleLabel;
    private CashierDashboardController controller;
    
    private Button paymentsBtn;
    private Button reservationsBtn;
    private Button receiptsBtn;
    private Button logoutBtn;
    
    public CashierDashboard() {
        controller = new CashierDashboardController();
        initializeView();
    }
    
    private void initializeView() {
        view = new BorderPane();
        view.setStyle("-fx-background-color: -color-bg-default;");
        
        createSidebar();
        view.setLeft(sidebar);
        view.setTop(createTopBar());
        
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setStyle("-fx-background-color: -color-bg-default;");
        view.setCenter(contentArea);
        
        showPayments();
    }
    
    private HBox createTopBar() {
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 0 1 0;"
        );
        
        titleLabel = new Label("Process Payments");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button themeBtn = new Button(ThemeManager.isDarkMode() ? "☀" : "🌙");
        themeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: -color-fg-default;" +
            "-fx-font-size: 18px;" +
            "-fx-cursor: hand;"
        );
        themeBtn.setOnAction(e -> {
            ThemeManager.toggleLightDark();
            themeBtn.setText(ThemeManager.isDarkMode() ? "☀" : "🌙");
        });
        
        Label cashierLabel = new Label("👤 Cashier");
        cashierLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        topBar.getChildren().addAll(titleLabel, spacer, themeBtn, cashierLabel);
        return topBar;
    }
    
    private void createSidebar() {
        sidebar = new VBox(10);
        sidebar.setPrefWidth(250);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 1 0 0;"
        );
        
        Label logoLabel = new Label("STI ProWear");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        logoLabel.setStyle("-fx-text-fill: -color-accent-fg;");
        
        Label subtitleLabel = new Label("Cashier Panel");
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        VBox header = new VBox(5, logoLabel, subtitleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        paymentsBtn = createNavButton("💳 Process Payments", true);
        reservationsBtn = createNavButton("📋 View Reservations", false);
        receiptsBtn = createNavButton("🧾 Receipts", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logoutBtn = createNavButton("🚪 Logout", false);
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #CF222E;" +
            "-fx-font-size: 14px;" +
            "-fx-alignment: center-left;" +
            "-fx-padding: 12px;" +
            "-fx-cursor: hand;"
        );
        
        paymentsBtn.setOnAction(e -> {
            setActiveButton(paymentsBtn);
            showPayments();
        });
        
        reservationsBtn.setOnAction(e -> {
            setActiveButton(reservationsBtn);
            showReservations();
        });
        
        receiptsBtn.setOnAction(e -> {
            setActiveButton(receiptsBtn);
            showReceipts();
        });
        
        logoutBtn.setOnAction(e -> controller.handleLogout());
        
        sidebar.getChildren().addAll(
            header,
            new Separator(),
            paymentsBtn,
            reservationsBtn,
            receiptsBtn,
            spacer,
            new Separator(),
            logoutBtn
        );
    }
    
    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPrefHeight(40);
        
        if (active) {
            btn.setStyle(
                "-fx-background-color: -color-accent-subtle;" +
                "-fx-text-fill: -color-accent-fg;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: -color-fg-default;" +
                "-fx-font-size: 14px;" +
                "-fx-cursor: hand;"
            );
        }
        
        return btn;
    }
    
    private void setActiveButton(Button activeBtn) {
        Button[] buttons = {paymentsBtn, reservationsBtn, receiptsBtn};
        
        for (Button btn : buttons) {
            if (btn == activeBtn) {
                btn.setStyle(
                    "-fx-background-color: -color-accent-subtle;" +
                    "-fx-text-fill: -color-accent-fg;" +
                    "-fx-font-size: 14px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: -color-fg-default;" +
                    "-fx-font-size: 14px;" +
                    "-fx-cursor: hand;"
                );
            }
        }
    }
    
    private void showPayments() {
        titleLabel.setText("Process Payments");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createPaymentsView());
    }
    
    private void showReservations() {
        titleLabel.setText("View Reservations");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReservationsView());
    }
    
    private void showReceipts() {
        titleLabel.setText("Receipts");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(controller.createReceiptsView());
    }
    
    public BorderPane getView() {
        return view;
    }
}

