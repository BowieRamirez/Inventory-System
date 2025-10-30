package gui.controllers;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Item;
import inventory.Reservation;
import student.Student;
import gui.utils.AlertHelper;
import gui.utils.SceneManager;
import gui.views.LoginView;
import utils.FileStorage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;

/**
 * AdminDashboardController - Handles all admin dashboard operations
 */
public class AdminDashboardController {
    
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private List<Student> students;
    
    public AdminDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        loadStudents();
    }
    
    private void loadStudents() {
        try {
            students = FileStorage.loadStudents();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load students: " + e.getMessage());
            students = List.of();
        }
    }
    
    /**
     * Create dashboard overview
     */
    public Node createDashboardView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        // Statistics cards
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        // Total Items
        int totalItems = inventoryManager.getAllItems().size();
        VBox itemsCard = createStatCard("📦 Total Items", String.valueOf(totalItems), "#0969DA");
        
        // Pending Reservations
        int pendingCount = reservationManager.getPendingReservations().size();
        VBox pendingCard = createStatCard("⏳ Pending", String.valueOf(pendingCount), "#BF8700");
        
        // Total Students
        VBox studentsCard = createStatCard("👥 Students", String.valueOf(students.size()), "#1A7F37");
        
        // Low Stock Items
        int lowStockCount = (int) inventoryManager.getAllItems().stream()
            .filter(item -> item.getQuantity() < 10)
            .count();
        VBox lowStockCard = createStatCard("⚠️ Low Stock", String.valueOf(lowStockCount), "#CF222E");
        
        statsBox.getChildren().addAll(itemsCard, pendingCard, studentsCard, lowStockCard);
        
        // Quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        actionsLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        Button viewInventoryBtn = createActionButton("📦 View Inventory");
        Button approvePendingBtn = createActionButton("✅ Approve Pending");
        Button addItemBtn = createActionButton("➕ Add Item");
        Button manageAccountsBtn = createActionButton("👥 Manage Accounts");
        
        actionsBox.getChildren().addAll(viewInventoryBtn, approvePendingBtn, addItemBtn, manageAccountsBtn);
        
        // Recent activity
        Label activityLabel = new Label("Recent Activity");
        activityLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        activityLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        VBox activityBox = new VBox(10);
        activityBox.setPadding(new Insets(15));
        activityBox.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-radius: 8px;"
        );
        
        List<Reservation> recentReservations = reservationManager.getAllReservations();
        if (recentReservations.isEmpty()) {
            Label noActivity = new Label("No recent activity");
            noActivity.setStyle("-fx-text-fill: -color-fg-muted;");
            activityBox.getChildren().add(noActivity);
        } else {
            int count = Math.min(5, recentReservations.size());
            for (int i = 0; i < count; i++) {
                Reservation r = recentReservations.get(i);
                Label activityItem = new Label(
                    String.format("Reservation #%d - %s - %s", 
                        r.getReservationId(), r.getStudentName(), r.getStatus())
                );
                activityItem.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px;");
                activityBox.getChildren().add(activityItem);
            }
        }
        
        container.getChildren().addAll(
            statsBox,
            new Separator(),
            actionsLabel,
            actionsBox,
            new Separator(),
            activityLabel,
            activityBox
        );
        
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: -color-bg-default; -fx-background-color: transparent;");
        
        return scrollPane;
    }
    
    /**
     * Create statistics card
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    /**
     * Create action button
     */
    private Button createActionButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(40);
        btn.setPrefWidth(180);
        btn.setStyle(
            "-fx-background-color: -color-accent-emphasis;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }
    
    /**
     * Create inventory view
     */
    public Node createInventoryView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button addItemBtn = new Button("➕ Add Item");
        Button refreshBtn = new Button("🔄 Refresh");
        Button exportBtn = new Button("📥 Export");
        
        styleActionButton(addItemBtn, "#1A7F37");
        styleActionButton(refreshBtn, "#0969DA");
        styleActionButton(exportBtn, "#6E7781");
        
        actionBar.getChildren().addAll(addItemBtn, refreshBtn, exportBtn);
        
        // Inventory table placeholder
        Label tableLabel = new Label("Inventory Table (TableView implementation coming next)");
        tableLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        VBox tableBox = new VBox(20);
        tableBox.setPadding(new Insets(30));
        tableBox.setAlignment(Pos.CENTER);
        tableBox.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-radius: 8px;"
        );
        tableBox.getChildren().add(tableLabel);
        
        container.getChildren().addAll(actionBar, tableBox);
        
        return container;
    }
    
    /**
     * Create reservations view
     */
    public Node createReservationsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Reservations Management (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    /**
     * Create accounts view
     */
    public Node createAccountsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Account Management (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    /**
     * Create stock logs view
     */
    public Node createStockLogsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Stock Logs (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    /**
     * Style action button
     */
    private void styleActionButton(Button btn, String color) {
        btn.setPrefHeight(36);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
    }
    
    /**
     * Handle logout
     */
    public void handleLogout() {
        boolean confirm = AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 1024, 768);
            SceneManager.setScene(scene);
        }
    }
}

