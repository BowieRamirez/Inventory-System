package gui.controllers;

import java.util.ArrayList;
import java.util.List;

import admin.Staff;
import gui.utils.AlertHelper;
import gui.utils.ControllerUtils;
import gui.utils.SceneManager;
import gui.views.LoginView;
import inventory.InventoryManager;
import inventory.Item;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.ReservationManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import student.Student;
import utils.FileStorage;
import utils.StockReturnLogger;
import utils.SystemLogger;

/**
 * AdminDashboardController - Handles all admin dashboard operations
 */
@SuppressWarnings("unchecked")
public class AdminDashboardController {
    
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private List<Student> students;
    private List<Staff> staffList;
    
    // Quick action buttons
    private Button approvePendingBtn;
    private Button manageAccountsBtn;

    public AdminDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);

        loadStudents();
        loadStaff();
    }
    
    private void loadStudents() {
        try {
            students = FileStorage.loadStudents();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load students: " + e.getMessage());
            students = List.of();
        }
    }
    
    private void loadStaff() {
        try {
            staffList = FileStorage.loadStaff();
        } catch (Exception e) {
            AlertHelper.showError("Error", "Failed to load staff: " + e.getMessage());
            staffList = List.of();
        }
    }
    
    /**
     * Create dashboard overview
     */
    public Node createDashboardView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        // Statistics cards - First row
        HBox statsBoxRow1 = new HBox(20);
        statsBoxRow1.setAlignment(Pos.CENTER);
        
        // Total Students
        VBox studentsCard = createStatCard("👥 Students", String.valueOf(students.size()), "#1A7F37");
        
        // Total Items in inventory
        int totalItems = inventoryManager.getAllItems().size();
        VBox itemsCard = createStatCard("📦 Items", String.valueOf(totalItems), "#0969DA");
        
        // Total Staff
        VBox staffCard = createStatCard("👔 Staff", String.valueOf(staffList.size()), "#8250DF");
        
        statsBoxRow1.getChildren().addAll(studentsCard, itemsCard, staffCard);
        
        // Statistics cards - Second row
        HBox statsBoxRow2 = new HBox(20);
        statsBoxRow2.setAlignment(Pos.CENTER);
        
        // Pending Stock Approvals
        int pendingApprovals = inventoryManager.getAuditManager().getPendingChanges().size();
        VBox approvalsCard = createStatCard("⏳ Pending Approvals", String.valueOf(pendingApprovals), "#D73A49");
        
        // Active Reservations (PENDING + PAID)
        long activeReservations = reservationManager.getAllReservations().stream()
            .filter(r -> "PENDING".equals(r.getStatus()) || "PAID".equals(r.getStatus()))
            .count();
        VBox reservationsCard = createStatCard("🎫 Active Reservations", String.valueOf(activeReservations), "#F66A0A");
        
        statsBoxRow2.getChildren().addAll(approvalsCard, reservationsCard);
        
        // Quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        actionsLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        HBox actionsBox = new HBox(15);
        actionsBox.setAlignment(Pos.CENTER_LEFT);
        
        approvePendingBtn = createActionButton("✅ Approve Stock Changes");
        manageAccountsBtn = createActionButton("👥 Manage Accounts");
        
        actionsBox.getChildren().addAll(approvePendingBtn, manageAccountsBtn);
        
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
            statsBoxRow1,
            statsBoxRow2,
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
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or code...");
        searchField.setPrefWidth(250);

        styleActionButton(addItemBtn);
        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(addItemBtn, refreshBtn, searchField);

        // Create inventory table
        TableView<Item> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Item, Integer> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCode()));
        codeCol.setPrefWidth(80);

        TableColumn<Item, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(250);

        TableColumn<Item, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourse()));
        courseCol.setPrefWidth(150);

        TableColumn<Item, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(80);

        TableColumn<Item, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(100);

        TableColumn<Item, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getPrice()));
        priceCol.setCellFactory(col -> new TableCell<Item, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f", price));
                }
            }
        });
        priceCol.setPrefWidth(100);

        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Item, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox buttons = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Item currentItem = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> handleEditItem(currentItem, table));
                    deleteBtn.setOnAction(e -> handleDeleteItem(currentItem, table));
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(120);

        table.getColumns().addAll(codeCol, nameCol, courseCol, sizeCol, qtyCol, priceCol, actionsCol);

        // Load data
        ObservableList<Item> items = FXCollections.observableArrayList(inventoryManager.getAllItems());
        table.setItems(items);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            } else {
                List<Item> filtered = inventoryManager.getAllItems().stream()
                    .filter(item -> item.getName().toLowerCase().contains(newVal.toLowerCase()) ||
                                  String.valueOf(item.getCode()).contains(newVal))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Button actions
        addItemBtn.setOnAction(e -> handleAddItem(table));
        refreshBtn.setOnAction(e -> {
            table.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
    }
    
    /**
     * Create reservations view
     */
    public Node createReservationsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Filter buttons
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button allBtn = new Button("All");
        Button pendingBtn = new Button("Pending");
        Button approvedBtn = new Button("Approved");
        Button paidBtn = new Button("Paid");
        Button completedBtn = new Button("Completed");
        Button returnRequestsBtn = new Button("Return Requests");
        Button cancelledBtn = new Button("Cancelled");
        Button refreshBtn = new Button("🔄 Refresh");

        styleActionButton(allBtn);
        styleActionButton(pendingBtn);
        styleActionButton(approvedBtn);
        styleActionButton(paidBtn);
        styleActionButton(completedBtn);
        styleActionButton(returnRequestsBtn);
        styleActionButton(cancelledBtn);
        styleActionButton(refreshBtn);

        filterBar.getChildren().addAll(allBtn, pendingBtn, approvedBtn, paidBtn, completedBtn, returnRequestsBtn, cancelledBtn, refreshBtn);

        // Create reservations table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, show the bundle ID as the order ID
                return new javafx.beans.property.SimpleStringProperty(r.getBundleId());
            }
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getReservationId()));
        });
        idCol.setPrefWidth(180);

        TableColumn<Reservation, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<Reservation, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, show bundle info with item count
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items) - " + r.getItemName());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(200);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // Check if bundle has multiple different sizes
                String bundleId = r.getBundleId();
                long distinctSizes = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .map(Reservation::getSize)
                    .distinct()
                    .count();
                
                if (distinctSizes > 1) {
                    return new javafx.beans.property.SimpleStringProperty("Bundle - Click to see");
                }
                // If all items have the same size, show that size
                return new javafx.beans.property.SimpleStringProperty(r.getSize());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getSize());
        });
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, sum all quantities
                String bundleId = r.getBundleId();
                int totalQty = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .mapToInt(Reservation::getQuantity)
                    .sum();
                return new javafx.beans.property.SimpleObjectProperty<>(totalQty);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(r.getQuantity());
        });
        qtyCol.setPrefWidth(60);

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total");
        priceCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, sum all prices
                String bundleId = r.getBundleId();
                double totalPrice = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .mapToDouble(Reservation::getTotalPrice)
                    .sum();
                return new javafx.beans.property.SimpleObjectProperty<>(totalPrice);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(r.getTotalPrice());
        });
        priceCol.setCellFactory(col -> new TableCell<Reservation, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f", price));
                }
            }
        });
        priceCol.setPrefWidth(100);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(180);

        TableColumn<Reservation, Void> bundleCol = new TableColumn<>("Bundle");
        bundleCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button bundleBtn = new Button("BUNDLE ORDER");
            
            {
                bundleBtn.setStyle(
                    "-fx-background-color: #0969DA; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 10px; " +
                    "-fx-padding: 5 10; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;"
                );
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (reservation.isPartOfBundle()) {
                        bundleBtn.setOnAction(e -> showBundleItemsDialog(reservation));
                        setGraphic(bundleBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        bundleCol.setPrefWidth(130);

        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button approveBtn = new Button("✓");
            private final Button rejectBtn = new Button("✗");
            private final HBox buttons = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if ("PENDING".equals(reservation.getStatus())) {
                        approveBtn.setText("✓ Approve");
                        rejectBtn.setText("✗ Reject");
                        approveBtn.setOnAction(e -> handleApproveReservation(reservation, table));
                        rejectBtn.setOnAction(e -> handleRejectReservation(reservation, table));
                        setGraphic(buttons);
                    } else if ("RETURN REQUESTED".equals(reservation.getStatus())) {
                        approveBtn.setText("✓ Approve Return");
                        rejectBtn.setText("✗ Reject Return");
                        approveBtn.setOnAction(e -> handleApproveReturn(reservation, table));
                        rejectBtn.setOnAction(e -> handleRejectReturn(reservation, table));
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        actionsCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol, bundleCol, actionsCol);

        // Load all reservations (deduplicated for bundles)
        ObservableList<Reservation> allReservations = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations()));
        table.setItems(allReservations);

        // Filter actions
        allBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations()))));
        pendingBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        approvedBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("APPROVED"))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        paidBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("PAID"))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        completedBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        returnRequestsBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getReturnRequests();
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        cancelledBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
        });
        refreshBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations()))));

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(filterBar, table);

        // Add row click handler to show order details
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Reservation> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Reservation clickedReservation = row.getItem();
                    showOrderDetailsDialog(clickedReservation);
                }
            });
            return row;
        });

        return container;
    }

    /**
     * Show detailed order information dialog
     */
    private void showOrderDetailsDialog(Reservation reservation) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Order Details");
        dialog.setHeaderText("Order ID: " + (reservation.isPartOfBundle() ? reservation.getBundleId() : String.valueOf(reservation.getReservationId())));

        javafx.scene.control.ButtonType closeButton = javafx.scene.control.ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");

        // Customer Information Section
        VBox customerSection = new VBox(8);
        customerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label customerHeader = new javafx.scene.control.Label("CUSTOMER INFORMATION");
        customerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label studentName = new javafx.scene.control.Label("Name: " + reservation.getStudentName());
        javafx.scene.control.Label studentId = new javafx.scene.control.Label("Student ID: " + reservation.getStudentId());
        
        customerSection.getChildren().addAll(customerHeader, studentName, studentId);

        // Order Items Section
        VBox itemsSection = new VBox(8);
        itemsSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label itemsHeader = new javafx.scene.control.Label("ORDER ITEMS");
        itemsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        itemsSection.getChildren().add(itemsHeader);

        double totalPrice = 0;
        int totalQuantity = 0;

        if (reservation.isPartOfBundle()) {
            // Get all items in the bundle
            String bundleId = reservation.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .collect(java.util.stream.Collectors.toList());
            
            for (Reservation item : bundleItems) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + item.getItemName());
                itemName.setMinWidth(250);
                
                javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + item.getSize());
                itemSize.setMinWidth(70);
                
                javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + item.getQuantity());
                itemQty.setMinWidth(60);
                
                javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", item.getTotalPrice()));
                itemPrice.setStyle("-fx-font-weight: bold;");
                
                itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
                itemsSection.getChildren().add(itemRow);
                
                totalPrice += item.getTotalPrice();
                totalQuantity += item.getQuantity();
            }
        } else {
            // Single item
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            
            javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + reservation.getItemName());
            itemName.setMinWidth(250);
            
            javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + reservation.getSize());
            itemSize.setMinWidth(70);
            
            javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + reservation.getQuantity());
            itemQty.setMinWidth(60);
            
            javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", reservation.getTotalPrice()));
            itemPrice.setStyle("-fx-font-weight: bold;");
            
            itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
            itemsSection.getChildren().add(itemRow);
            
            totalPrice = reservation.getTotalPrice();
            totalQuantity = reservation.getQuantity();
        }

        // Order Summary Section
        VBox summarySection = new VBox(8);
        summarySection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label summaryHeader = new javafx.scene.control.Label("ORDER SUMMARY");
        summaryHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("Status: " + reservation.getStatus());
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label qtyLabel = new javafx.scene.control.Label("Total Quantity: " + totalQuantity);
        qtyLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label totalLabel = new javafx.scene.control.Label("Total Amount: ₱" + String.format("%.2f", totalPrice));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        javafx.scene.control.Label orderTypeLabel = new javafx.scene.control.Label("Order Type: " + (reservation.isPartOfBundle() ? "Bundle Order" : "Single Item"));
        orderTypeLabel.setStyle("-fx-font-size: 12px;");
        
        summarySection.getChildren().addAll(summaryHeader, statusLabel, orderTypeLabel, qtyLabel, totalLabel);

        content.getChildren().addAll(customerSection, itemsSection, summarySection);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(600);
        dialog.showAndWait();
    }

    /**
     * Show bundle items dialog - displays all items in a bundle order
     */
    private void showBundleItemsDialog(Reservation reservation) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Bundle Order Details");
        dialog.setHeaderText("Bundle ID: " + reservation.getBundleId());

        ButtonType closeButton = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");

        // Customer Information
        VBox customerSection = new VBox(8);
        customerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label customerHeader = new Label("CUSTOMER INFORMATION");
        customerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        Label studentName = new Label("Name: " + reservation.getStudentName());
        Label studentId = new Label("Student ID: " + reservation.getStudentId());
        Label statusLabel = new Label("Status: " + reservation.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        customerSection.getChildren().addAll(customerHeader, studentName, studentId, statusLabel);

        // Bundle Items Section
        VBox itemsSection = new VBox(8);
        itemsSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        Label itemsHeader = new Label("BUNDLE ITEMS");
        itemsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #0969DA;");
        itemsSection.getChildren().add(itemsHeader);

        // Get all items in the bundle
        String bundleId = reservation.getBundleId();
        List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
            .filter(r -> bundleId.equals(r.getBundleId()))
            .collect(java.util.stream.Collectors.toList());
        
        double totalPrice = 0;
        int totalQuantity = 0;
        
        for (Reservation item : bundleItems) {
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.setStyle("-fx-padding: 5 0;");
            
            Label itemName = new Label("• " + item.getItemName());
            itemName.setMinWidth(250);
            itemName.setStyle("-fx-font-size: 12px;");
            
            Label itemSize = new Label("Size: " + item.getSize());
            itemSize.setMinWidth(70);
            itemSize.setStyle("-fx-font-size: 11px;");
            
            Label itemQty = new Label("Qty: " + item.getQuantity());
            itemQty.setMinWidth(60);
            itemQty.setStyle("-fx-font-size: 11px;");
            
            Label itemPrice = new Label("₱" + String.format("%.2f", item.getTotalPrice()));
            itemPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            
            itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
            itemsSection.getChildren().add(itemRow);
            
            totalPrice += item.getTotalPrice();
            totalQuantity += item.getQuantity();
        }

        // Separator
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 10 0;");
        itemsSection.getChildren().add(separator);

        // Bundle Summary
        HBox summaryRow = new HBox(10);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        summaryRow.setStyle("-fx-padding: 10 0 0 0;");
        
        Label summaryLabel = new Label("TOTAL:");
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        summaryLabel.setMinWidth(250);
        
        Label totalItemsLabel = new Label(bundleItems.size() + " item type(s)");
        totalItemsLabel.setMinWidth(70);
        totalItemsLabel.setStyle("-fx-font-size: 12px;");
        
        Label totalQtyLabel = new Label("Qty: " + totalQuantity);
        totalQtyLabel.setMinWidth(60);
        totalQtyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label totalPriceLabel = new Label("₱" + String.format("%.2f", totalPrice));
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0969DA;");
        
        summaryRow.getChildren().addAll(summaryLabel, totalItemsLabel, totalQtyLabel, totalPriceLabel);
        itemsSection.getChildren().add(summaryRow);

        content.getChildren().addAll(customerSection, itemsSection);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(650);
        dialog.showAndWait();
    }

    /**
     * Handle approve reservation
     */
    private void handleApproveReservation(Reservation reservation, TableView<Reservation> table) {
        String message;
        if (reservation.isPartOfBundle()) {
            // Show bundle info
            String bundleId = reservation.getBundleId();
            long itemCount = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .count();
            message = "Approve BUNDLE ORDER for:\n" + 
                     reservation.getStudentName() + "\n" +
                     "Bundle contains " + itemCount + " item type(s)";
        } else {
            message = "Approve reservation for:\n" + 
                     reservation.getStudentName() + "\n" +
                     reservation.getItemName() + " (" + reservation.getSize() + ")";
        }
        
        boolean confirm = AlertHelper.showConfirmation("Approve Reservation", message);

        if (confirm) {
            boolean allSuccess = true;
            
            if (reservation.isPartOfBundle()) {
                // Approve all items in the bundle
                String bundleId = reservation.getBundleId();
                List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                    .filter(r -> bundleId.equals(r.getBundleId()))
                    .collect(java.util.stream.Collectors.toList());
                
                for (Reservation item : bundleItems) {
                    boolean success = reservationManager.approveReservation(item.getReservationId(), item.getSize());
                    if (!success) {
                        allSuccess = false;
                    }
                }
            } else {
                allSuccess = reservationManager.approveReservation(reservation.getReservationId(), reservation.getSize());
            }
            
            if (allSuccess) {
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations())));
                AlertHelper.showSuccess("Success", reservation.isPartOfBundle() ? "Bundle approved!" : "Reservation approved!");
            } else {
                AlertHelper.showError("Error", "Failed to approve reservation");
            }
        }
    }

    /**
     * Handle reject reservation
     */
    private void handleRejectReservation(Reservation reservation, TableView<Reservation> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Reservation");
        
        if (reservation.isPartOfBundle()) {
            String bundleId = reservation.getBundleId();
            long itemCount = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .count();
            dialog.setHeaderText("Reject BUNDLE ORDER for: " + reservation.getStudentName() + 
                               "\nBundle contains " + itemCount + " item type(s)");
        } else {
            dialog.setHeaderText("Reject reservation for: " + reservation.getStudentName());
        }
        
        dialog.setContentText("Reason:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.isEmpty()) {
                boolean allSuccess = true;
                
                if (reservation.isPartOfBundle()) {
                    // Reject all items in the bundle
                    String bundleId = reservation.getBundleId();
                    List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                        .filter(r -> bundleId.equals(r.getBundleId()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    for (Reservation item : bundleItems) {
                        boolean success = reservationManager.cancelReservation(item.getReservationId(), reason);
                        if (!success) {
                            allSuccess = false;
                        }
                    }
                } else {
                    allSuccess = reservationManager.cancelReservation(reservation.getReservationId(), reason);
                }
                
                if (allSuccess) {
                    table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations())));
                    AlertHelper.showSuccess("Success", reservation.isPartOfBundle() ? "Bundle rejected" : "Reservation rejected");
                } else {
                    AlertHelper.showError("Error", "Failed to reject reservation");
                }
            }
        });
    }

    /**
     * Handle approve return request
     */
    private void handleApproveReturn(Reservation reservation, TableView<Reservation> table) {
        // Determine what we're approving
        String itemDescription;
        List<Reservation> itemsToReturn = new java.util.ArrayList<>();
        double totalRefund = 0;
        
        if (reservation.isPartOfBundle()) {
            String bundleId = reservation.getBundleId();
            // Get all items in the bundle with RETURN REQUESTED status
            itemsToReturn = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .filter(res -> "RETURN REQUESTED".equals(res.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            if (itemsToReturn.isEmpty()) {
                AlertHelper.showError("Error", "No items in this bundle have pending return requests.");
                return;
            }
            
            itemDescription = "Bundle Order (" + itemsToReturn.size() + " items)";
            totalRefund = itemsToReturn.stream().mapToDouble(Reservation::getTotalPrice).sum();
        } else {
            itemsToReturn.add(reservation);
            itemDescription = reservation.getItemName() + " (" + reservation.getSize() + ")";
            totalRefund = reservation.getTotalPrice();
        }
        
        // Build confirmation message
        StringBuilder message = new StringBuilder();
        message.append("Approve return request for:\n");
        message.append("Student: ").append(reservation.getStudentName()).append("\n\n");
        
        if (reservation.isPartOfBundle()) {
            message.append("Bundle Items:\n");
            for (Reservation item : itemsToReturn) {
                message.append("• ").append(item.getItemName()).append(" - ").append(item.getSize())
                       .append(" (").append(item.getQuantity()).append("x) - ₱")
                       .append(String.format("%.2f", item.getTotalPrice())).append("\n");
            }
        } else {
            message.append("Item: ").append(itemDescription).append("\n");
            message.append("Quantity: ").append(reservation.getQuantity()).append("x\n");
        }
        
        message.append("\nTotal Refund Amount: ₱").append(String.format("%.2f", totalRefund)).append("\n\n");
        message.append("Reason: ").append(reservation.getReason() != null ? reservation.getReason() : "N/A").append("\n\n");
        message.append("This will restock all items and mark as refunded.");
        
        boolean confirm = AlertHelper.showConfirmation("Approve Return", message.toString());

        if (confirm) {
            // Approve return for all items
            boolean allSuccess = true;
            int successCount = 0;
            
            for (Reservation item : itemsToReturn) {
                boolean success = reservationManager.approveReturn(item.getReservationId());
                if (success) {
                    successCount++;
                } else {
                    allSuccess = false;
                }
            }
            
            if (allSuccess) {
                table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                String successMsg = reservation.isPartOfBundle() ?
                    "Return approved for all " + successCount + " items!\n\n" :
                    "Return approved!\n\n";
                
                AlertHelper.showSuccess("Success",
                    successMsg + "Items have been restocked and marked as refunded.");
            } else if (successCount > 0) {
                table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                AlertHelper.showWarning("Partial Success",
                    "Return approved for " + successCount + " out of " + itemsToReturn.size() + " items.\n" +
                    "Some items may not be restockable.");
            } else {
                AlertHelper.showError("Error", "Failed to approve return. Items may not be restockable.");
            }
        }
    }

    /**
     * Handle reject return request
     */
    private void handleRejectReturn(Reservation reservation, TableView<Reservation> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Return");
        dialog.setHeaderText("Reject return request for: " + reservation.getStudentName());
        dialog.setContentText("Reason for rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.isEmpty()) {
                boolean success = reservationManager.rejectReturn(reservation.getReservationId(), reason);
                if (success) {
                    table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                    AlertHelper.showSuccess("Success", "Return request rejected");
                } else {
                    AlertHelper.showError("Error", "Failed to reject return request");
                }
            }
        });
    }

    /**
     * Create accounts view with tabs for Students and Staff
     */
    public Node createAccountsView() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Students tab
        Tab studentsTab = new Tab("👨‍🎓 Students");
        studentsTab.setContent(createStudentManagementView());
        
        // Staff tab
        Tab staffTab = new Tab("👔 Staff");
        staffTab.setContent(createStaffManagementView());
        
        tabPane.getTabs().addAll(studentsTab, staffTab);
        
        return tabPane;
    }
    
    /**
     * Create student management view
     */
    private Node createStudentManagementView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or student ID...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create students table
        TableView<Student> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Student, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentId()));
        idCol.setPrefWidth(120);

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        nameCol.setPrefWidth(250);

        TableColumn<Student, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourse()));
        courseCol.setPrefWidth(150);

        TableColumn<Student, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getGender()));
        genderCol.setPrefWidth(100);

        TableColumn<Student, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().isActive()));
        activeCol.setCellFactory(col -> new TableCell<Student, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    setText(active ? "✓ Active" : "✗ Inactive");
                    setStyle(active ? "-fx-text-fill: #1A7F37;" : "-fx-text-fill: #CF222E;");
                }
            }
        });
        activeCol.setPrefWidth(100);

        TableColumn<Student, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Student, Void>() {
            private final HBox actionBox = new HBox(8);
            private final Button toggleBtn = new Button();
            private final Button resetPwdBtn = new Button("Reset Password");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Student student = getTableView().getItems().get(getIndex());
                    
                    // Toggle button
                    if (student.isActive()) {
                        toggleBtn.setText("Deactivate");
                        toggleBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    } else {
                        toggleBtn.setText("Activate");
                        toggleBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    }
                    toggleBtn.setOnAction(e -> handleToggleStudent(student, table));
                    
                    // Reset password button
                    resetPwdBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    resetPwdBtn.setOnAction(e -> handleResetStudentPassword(student, table));
                    
                    actionBox.getChildren().setAll(toggleBtn, resetPwdBtn);
                    setGraphic(actionBox);
                }
            }
        });
        actionsCol.setPrefWidth(250);

        table.getColumns().addAll(idCol, nameCol, courseCol, genderCol, activeCol, actionsCol);

        // Load data
        ObservableList<Student> studentList = FXCollections.observableArrayList(students);
        table.setItems(studentList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(students));
            } else {
                List<Student> filtered = students.stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(newVal.toLowerCase()) ||
                               s.getStudentId().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Button actions
        refreshBtn.setOnAction(e -> {
            loadStudents();
            table.setItems(FXCollections.observableArrayList(students));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
    }
    
    /**
     * Create staff management view
     */
    private Node createStaffManagementView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button addStaffBtn = new Button("➕ Add Staff");
        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or staff ID...");
        searchField.setPrefWidth(250);

        styleActionButton(addStaffBtn);
        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(addStaffBtn, refreshBtn, searchField);

        // Create staff table
        TableView<Staff> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Staff, String> idCol = new TableColumn<>("Staff ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStaffId()));
        idCol.setPrefWidth(120);

        TableColumn<Staff, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));
        nameCol.setPrefWidth(250);

        TableColumn<Staff, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRole()));
        roleCol.setPrefWidth(120);

        TableColumn<Staff, Boolean> activeCol = new TableColumn<>("Active");
        activeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().isActive()));
        activeCol.setCellFactory(col -> new TableCell<Staff, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    setText(active ? "✓ Active" : "✗ Inactive");
                    setStyle(active ? "-fx-text-fill: #1A7F37;" : "-fx-text-fill: #CF222E;");
                }
            }
        });
        activeCol.setPrefWidth(100);

        TableColumn<Staff, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Staff, Void>() {
            private final HBox actionBox = new HBox(8);
            private final Button editBtn = new Button("Edit");
            private final Button toggleBtn = new Button();
            private final Button resetPwdBtn = new Button("Reset Password");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Staff staff = getTableView().getItems().get(getIndex());
                    
                    // Edit button
                    editBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    editBtn.setOnAction(e -> handleEditStaff(staff, table));
                    
                    // Toggle button
                    if (staff.isActive()) {
                        toggleBtn.setText("Deactivate");
                        toggleBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    } else {
                        toggleBtn.setText("Activate");
                        toggleBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    }
                    toggleBtn.setOnAction(e -> handleToggleStaff(staff, table));
                    
                    // Reset password button
                    resetPwdBtn.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px;");
                    resetPwdBtn.setOnAction(e -> handleResetStaffPassword(staff, table));
                    
                    actionBox.getChildren().setAll(editBtn, toggleBtn, resetPwdBtn);
                    setGraphic(actionBox);
                }
            }
        });
        actionsCol.setPrefWidth(330);

        table.getColumns().addAll(idCol, nameCol, roleCol, activeCol, actionsCol);

        // Load data
        ObservableList<Staff> staffObsList = FXCollections.observableArrayList(staffList);
        table.setItems(staffObsList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(staffList));
            } else {
                List<Staff> filtered = staffList.stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(newVal.toLowerCase()) ||
                               s.getStaffId().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Button actions
        addStaffBtn.setOnAction(e -> handleAddStaff(table));
        
        refreshBtn.setOnAction(e -> {
            loadStaff();
            table.setItems(FXCollections.observableArrayList(staffList));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
    }

    /**
     * Handle toggle student active status
     */
    private void handleToggleStudent(Student student, TableView<Student> table) {
        String action = student.isActive() ? "deactivate" : "activate";
        boolean confirm = AlertHelper.showConfirmation("Toggle Account",
            "Are you sure you want to " + action + " account for:\n" + student.getFullName() + "?");

        if (confirm) {
            student.setActive(!student.isActive());
            FileStorage.saveStudents(students);
            table.refresh();
            AlertHelper.showSuccess("Success", "Account " + action + "d successfully!");
        }
    }
    
    /**
     * Handle reset student password
     */
    private void handleResetStudentPassword(Student student, TableView<Student> table) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + student.getFullName() + "\nStudent ID: " + student.getStudentId());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        javafx.scene.control.PasswordField newPasswordField = new javafx.scene.control.PasswordField();
        newPasswordField.setPromptText("New Password");
        javafx.scene.control.PasswordField confirmPasswordField = new javafx.scene.control.PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        grid.add(new Label("New Password:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType resetButtonType = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        // Enable/disable reset button based on input
        javafx.scene.Node resetButton = dialog.getDialogPane().lookupButton(resetButtonType);
        resetButton.setDisable(true);

        // Validation listener
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            resetButton.setDisable(newVal.trim().isEmpty() || !newVal.equals(confirmPasswordField.getText()));
        });
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            resetButton.setDisable(newVal.trim().isEmpty() || !newVal.equals(newPasswordField.getText()));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resetButtonType) {
                return newPasswordField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            if (!newPassword.isEmpty()) {
                boolean confirm = AlertHelper.showConfirmation("Confirm Reset",
                    "Are you sure you want to reset the password for:\n" + student.getFullName() + "?");
                
                if (confirm) {
                    student.setPassword(newPassword);
                    FileStorage.saveStudents(students);
                    AlertHelper.showSuccess("Success", 
                        "Password reset successfully for " + student.getFullName() + "!\n" +
                        "Student ID: " + student.getStudentId() + "\n" +
                        "New Password: " + newPassword);
                    SystemLogger.logActivity("Admin reset password for student: " + student.getStudentId());
                }
            }
        });
    }
    
    /**
     * Handle reset staff password
     */
    private void handleResetStaffPassword(Staff staff, TableView<Staff> table) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + staff.getFullName() + "\nStaff ID: " + staff.getStaffId() + "\nRole: " + staff.getRole());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        javafx.scene.control.PasswordField newPasswordField = new javafx.scene.control.PasswordField();
        newPasswordField.setPromptText("New Password");
        javafx.scene.control.PasswordField confirmPasswordField = new javafx.scene.control.PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");

        grid.add(new Label("New Password:"), 0, 0);
        grid.add(newPasswordField, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPasswordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        ButtonType resetButtonType = new ButtonType("Reset Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

        // Enable/disable reset button based on input
        javafx.scene.Node resetButton = dialog.getDialogPane().lookupButton(resetButtonType);
        resetButton.setDisable(true);

        // Validation listener
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            resetButton.setDisable(newVal.trim().isEmpty() || !newVal.equals(confirmPasswordField.getText()));
        });
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            resetButton.setDisable(newVal.trim().isEmpty() || !newVal.equals(newPasswordField.getText()));
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == resetButtonType) {
                return newPasswordField.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            if (!newPassword.isEmpty()) {
                boolean confirm = AlertHelper.showConfirmation("Confirm Reset",
                    "Are you sure you want to reset the password for:\n" + 
                    staff.getFullName() + " (" + staff.getRole() + ")?");
                
                if (confirm) {
                    staff.setPassword(newPassword);
                    FileStorage.saveStaff(staffList);
                    AlertHelper.showSuccess("Success", 
                        "Password reset successfully for " + staff.getFullName() + "!\n" +
                        "Staff ID: " + staff.getStaffId() + "\n" +
                        "Role: " + staff.getRole() + "\n" +
                        "New Password: " + newPassword);
                    SystemLogger.logActivity("Admin reset password for staff: " + staff.getStaffId() + " (" + staff.getRole() + ")");
                }
            }
        });
    }
    
    /**
     * Handle add new staff
     */
    private void handleAddStaff(TableView<Staff> table) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Add New Staff");
        dialog.setHeaderText("Enter staff member details");

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField staffIdField = new TextField();
        staffIdField.setPromptText("Staff ID");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Staff", "Cashier");
        roleCombo.setValue("Staff");

        grid.add(new Label("Staff ID:"), 0, 0);
        grid.add(staffIdField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("First Name:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String staffId = staffIdField.getText().trim();
                String password = passwordField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String role = roleCombo.getValue();

                // Validate
                if (staffId.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    AlertHelper.showError("Validation Error", "All fields are required!");
                    return null;
                }

                // Check if staff ID already exists
                if (FileStorage.staffExists(staffList, staffId)) {
                    AlertHelper.showError("Validation Error", "Staff ID already exists!");
                    return null;
                }

                return new Staff(staffId, password, firstName, lastName, role);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStaff -> {
            if (FileStorage.addStaff(staffList, newStaff)) {
                table.setItems(FXCollections.observableArrayList(staffList));
                AlertHelper.showSuccess("Success", "Staff member added successfully!");
            } else {
                AlertHelper.showError("Error", "Failed to add staff member!");
            }
        });
    }
    
    /**
     * Handle edit staff
     */
    private void handleEditStaff(Staff staff, TableView<Staff> table) {
        Dialog<Staff> dialog = new Dialog<>();
        dialog.setTitle("Edit Staff");
        dialog.setHeaderText("Edit details for: " + staff.getFullName());

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField passwordField = new TextField(staff.getPassword());
        passwordField.setPromptText("Password");
        TextField firstNameField = new TextField(staff.getFirstName());
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField(staff.getLastName());
        lastNameField.setPromptText("Last Name");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("Staff", "Cashier");
        roleCombo.setValue(staff.getRole());

        grid.add(new Label("Staff ID:"), 0, 0);
        grid.add(new Label(staff.getStaffId()), 1, 0); // ID is not editable
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("First Name:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String password = passwordField.getText().trim();
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                String role = roleCombo.getValue();

                // Validate
                if (password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                    AlertHelper.showError("Validation Error", "All fields are required!");
                    return null;
                }

                // Update staff
                staff.setPassword(password);
                staff.setFirstName(firstName);
                staff.setLastName(lastName);
                staff.setRole(role);
                
                return staff;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedStaff -> {
            if (FileStorage.updateStaff(staffList, updatedStaff)) {
                table.refresh();
                AlertHelper.showSuccess("Success", "Staff member updated successfully!");
            } else {
                AlertHelper.showError("Error", "Failed to update staff member!");
            }
        });
    }
    
    /**
     * Handle toggle staff active status
     */
    private void handleToggleStaff(Staff staff, TableView<Staff> table) {
        String action = staff.isActive() ? "deactivate" : "activate";
        boolean confirm = AlertHelper.showConfirmation("Toggle Account",
            "Are you sure you want to " + action + " account for:\n" + staff.getFullName() + " (" + staff.getRole() + ")?");

        if (confirm) {
            staff.setActive(!staff.isActive());
            FileStorage.saveStaff(staffList);
            table.refresh();
            AlertHelper.showSuccess("Success", "Account " + action + "d successfully!");
        }
    }
    
    /**
     * Create stock approvals view (pending stock adjustments from staff)
     */
    public Node createStockApprovalsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        styleActionButton(refreshBtn);
        actionBar.getChildren().add(refreshBtn);

        // Create pending stock adjustments table
        TableView<audit.StockAuditLog> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<audit.StockAuditLog, String> timestampCol = new TableColumn<>("Requested");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getCreatedAt().toString()));
        timestampCol.setPrefWidth(150);

        TableColumn<audit.StockAuditLog, String> staffCol = new TableColumn<>("Staff");
        staffCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getStaffUsername()));
        staffCol.setPrefWidth(100);

        TableColumn<audit.StockAuditLog, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getItemName() + " (" + data.getValue().getItemSize() + ")"));
        itemCol.setPrefWidth(200);

        TableColumn<audit.StockAuditLog, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(data.getValue().getItemCode())));
        codeCol.setPrefWidth(80);

        TableColumn<audit.StockAuditLog, String> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(data -> {
            audit.StockAuditLog log = data.getValue();
            String change = log.getQuantityBefore() + " → " + log.getQuantityAfter() + 
                          " (" + (log.getQuantityChanged() > 0 ? "+" : "") + log.getQuantityChanged() + ")";
            return new javafx.beans.property.SimpleStringProperty(change);
        });
        changeCol.setPrefWidth(150);

        TableColumn<audit.StockAuditLog, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getReason()));
        reasonCol.setPrefWidth(200);

        TableColumn<audit.StockAuditLog, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<audit.StockAuditLog, Void>() {
            private final HBox actionBox = new HBox(8);
            private final Button approveBtn = new Button("✅ Approve");
            private final Button rejectBtn = new Button("❌ Reject");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    audit.StockAuditLog log = getTableView().getItems().get(getIndex());
                    
                    approveBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand;");
                    approveBtn.setOnAction(e -> handleApproveStockChange(log, table));
                    
                    rejectBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand;");
                    rejectBtn.setOnAction(e -> handleRejectStockChange(log, table));
                    
                    actionBox.getChildren().setAll(approveBtn, rejectBtn);
                    setGraphic(actionBox);
                }
            }
        });
        actionsCol.setPrefWidth(200);

        table.getColumns().addAll(timestampCol, staffCol, itemCol, codeCol, changeCol, reasonCol, actionsCol);

        // Load pending stock changes
        List<audit.StockAuditLog> pendingChanges = inventoryManager.getPendingStockChanges();
        table.setItems(FXCollections.observableArrayList(pendingChanges));

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<audit.StockAuditLog> refreshed = inventoryManager.getPendingStockChanges();
            table.setItems(FXCollections.observableArrayList(refreshed));
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        
        // Add info message if no pending changes
        if (pendingChanges.isEmpty()) {
            Label noDataLabel = new Label("No pending stock adjustments");
            noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: -color-fg-muted;");
            VBox emptyState = new VBox(noDataLabel);
            emptyState.setAlignment(Pos.CENTER);
            VBox.setVgrow(emptyState, Priority.ALWAYS);
            container.getChildren().addAll(actionBar, emptyState);
        } else {
            container.getChildren().addAll(actionBar, table);
        }

        return container;
    }
    
    /**
     * Handle approve stock change
     */
    private void handleApproveStockChange(audit.StockAuditLog log, TableView<audit.StockAuditLog> table) {
        boolean confirm = AlertHelper.showConfirmation("Approve Stock Change",
            "Approve this stock adjustment?\n\n" +
            "Item: " + log.getItemName() + " (" + log.getItemSize() + ")\n" +
            "Change: " + log.getQuantityBefore() + " → " + log.getQuantityAfter() + "\n" +
            "Requested by: " + log.getStaffUsername() + "\n" +
            "Reason: " + log.getReason());

        if (confirm) {
            boolean success = inventoryManager.approveAndApplyStockChange(log.getLogId(), "admin");
            
            if (success) {
                // Refresh table
                List<audit.StockAuditLog> refreshed = inventoryManager.getPendingStockChanges();
                table.setItems(FXCollections.observableArrayList(refreshed));
                
                AlertHelper.showSuccess("Approved", "Stock change approved and applied successfully!");
            } else {
                AlertHelper.showError("Error", "Failed to approve stock change!");
            }
        }
    }
    
    /**
     * Handle reject stock change
     */
    private void handleRejectStockChange(audit.StockAuditLog log, TableView<audit.StockAuditLog> table) {
        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Reject Stock Change");
        reasonDialog.setHeaderText("Provide a reason for rejection");
        reasonDialog.setContentText("Reason:");
        
        reasonDialog.showAndWait().ifPresent(reason -> {
            if (reason.trim().isEmpty()) {
                AlertHelper.showError("Validation Error", "Rejection reason is required!");
                return;
            }
            
            boolean success = inventoryManager.rejectStockChange(log.getLogId(), "admin", reason.trim());
            
            if (success) {
                // Refresh table
                List<audit.StockAuditLog> refreshed = inventoryManager.getPendingStockChanges();
                table.setItems(FXCollections.observableArrayList(refreshed));
                
                AlertHelper.showSuccess("Rejected", "Stock change request rejected!");
            } else {
                AlertHelper.showError("Error", "Failed to reject stock change!");
            }
        });
    }
    
    /**
     * Create stock logs view
     */
    public Node createStockLogsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by item name or performer...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create stock logs table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<String[], String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        timestampCol.setPrefWidth(150);

        TableColumn<String[], String> performerCol = new TableColumn<>("Staff/Admin");
        performerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        performerCol.setPrefWidth(200);

        TableColumn<String[], String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        codeCol.setPrefWidth(80);

        TableColumn<String[], String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        itemCol.setPrefWidth(220);

        TableColumn<String[], String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        sizeCol.setPrefWidth(60);

        TableColumn<String[], String> changeCol = new TableColumn<>("Stock Change");
        changeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        changeCol.setCellFactory(col -> new TableCell<String[], String>() {
            @Override
            protected void updateItem(String change, boolean empty) {
                super.updateItem(change, empty);
                if (empty || change == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(change);
                    if (change.startsWith("+")) {
                        setStyle("-fx-text-fill: #1A7F37; -fx-font-weight: bold;");
                    } else if (change.startsWith("-")) {
                        setStyle("-fx-text-fill: #CF222E; -fx-font-weight: bold;");
                    }
                }
            }
        });
        changeCol.setPrefWidth(100);

        TableColumn<String[], String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6]));
        actionCol.setPrefWidth(150);

        TableColumn<String[], String> approvedByCol = new TableColumn<>("Approved By");
        approvedByCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[7]));
        approvedByCol.setPrefWidth(120);

        TableColumn<String[], String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[8]));
        detailsCol.setPrefWidth(300);

        table.getColumns().addAll(timestampCol, performerCol, codeCol, itemCol, sizeCol, changeCol, actionCol, approvedByCol, detailsCol);

        // Load stock logs
        List<String[]> logs = loadStockLogs();
        ObservableList<String[]> logsList = FXCollections.observableArrayList(logs);
        table.setItems(logsList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(logs));
            } else {
                List<String[]> filtered = logs.stream()
                    .filter(log -> log[3].toLowerCase().contains(newVal.toLowerCase()) ||
                                 log[1].toLowerCase().contains(newVal.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Button actions
        refreshBtn.setOnAction(e -> {
            List<String[]> refreshedLogs = loadStockLogs();
            table.setItems(FXCollections.observableArrayList(refreshedLogs));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        // Add row click handler to show log details
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<String[]> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    String[] clickedLog = row.getItem();
                    showStockLogDetailsDialog(clickedLog);
                }
            });
            return row;
        });

        return container;
    }

    /**
     * Show detailed stock log information dialog
     */
    private void showStockLogDetailsDialog(String[] logData) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Stock Log Details");
        dialog.setHeaderText("Stock Change Information");

        javafx.scene.control.ButtonType closeButton = javafx.scene.control.ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");

        // Timestamp Section
        VBox timestampSection = new VBox(8);
        timestampSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label timestampHeader = new javafx.scene.control.Label("📅 TIMESTAMP");
        timestampHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label timestampValue = new javafx.scene.control.Label(logData[0]);
        timestampValue.setStyle("-fx-font-size: 14px;");
        
        timestampSection.getChildren().addAll(timestampHeader, timestampValue);

        // Staff/Admin Information Section
        VBox performerSection = new VBox(8);
        performerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label performerHeader = new javafx.scene.control.Label("👤 STAFF/ADMIN");
        performerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label performerValue = new javafx.scene.control.Label(logData[1]);
        performerValue.setStyle("-fx-font-size: 14px;");
        
        javafx.scene.control.Label approvedByLabel = new javafx.scene.control.Label("Approved By: " + logData[7]);
        approvedByLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");
        
        performerSection.getChildren().addAll(performerHeader, performerValue, approvedByLabel);

        // Item Details Section
        VBox itemSection = new VBox(8);
        itemSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label itemHeader = new javafx.scene.control.Label("📦 ITEM DETAILS");
        itemHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label itemCode = new javafx.scene.control.Label("Code: " + logData[2]);
        javafx.scene.control.Label itemName = new javafx.scene.control.Label("Item: " + logData[3]);
        javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + logData[4]);
        
        itemSection.getChildren().addAll(itemHeader, itemCode, itemName, itemSize);

        // Stock Change Section
        VBox changeSection = new VBox(8);
        changeSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label changeHeader = new javafx.scene.control.Label("📊 STOCK CHANGE");
        changeHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        String stockChange = logData[5];
        javafx.scene.control.Label changeValue = new javafx.scene.control.Label(stockChange);
        changeValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Color code based on increase/decrease
        if (stockChange.startsWith("+")) {
            changeValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1A7F37;");
        } else if (stockChange.startsWith("-")) {
            changeValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #CF222E;");
        }
        
        javafx.scene.control.Label actionLabel = new javafx.scene.control.Label("Action: " + logData[6]);
        actionLabel.setStyle("-fx-font-size: 12px;");
        
        changeSection.getChildren().addAll(changeHeader, changeValue, actionLabel);

        // Details Section
        VBox detailsSection = new VBox(8);
        detailsSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label detailsHeader = new javafx.scene.control.Label("📝 DETAILS");
        detailsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label detailsValue = new javafx.scene.control.Label(logData[8]);
        detailsValue.setWrapText(true);
        detailsValue.setMaxWidth(500);
        detailsValue.setStyle("-fx-font-size: 12px;");
        
        detailsSection.getChildren().addAll(detailsHeader, detailsValue);

        content.getChildren().addAll(timestampSection, performerSection, itemSection, changeSection, detailsSection);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(600);
        dialog.showAndWait();
    }

    /**
     * Load stock logs from file and audit system
     * Admin view: Shows only STAFF/ADMIN activities (returns, additions, deletions, updates)
     */
    private List<String[]> loadStockLogs() {
        List<String[]> logs = new ArrayList<>();
        
        // Admin-relevant actions (staff/admin activities only)
        List<String> adminRelevantActions = java.util.Arrays.asList(
            "STAFF_RETURN", "ITEM_ADDED", "ITEM_DELETED", "ITEM_UPDATED"
        );
        
        // Load from stock_logs.txt (legacy/old format)
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader("src/database/data/stock_logs.txt"))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 8) {
                        String action = parts[6]; // Action column
                        
                        // Only show admin-relevant actions
                        if (adminRelevantActions.contains(action)) {
                            // Add "Approved By" column (empty for old logs from stock_logs.txt)
                            String[] extendedParts = new String[9];
                            System.arraycopy(parts, 0, extendedParts, 0, 7);
                            extendedParts[7] = "-"; // Approved By (not applicable for old logs)
                            extendedParts[8] = parts[7]; // Details
                            logs.add(extendedParts);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Failed to load logs
        }
        
        // Load from audit system (new format with approval tracking)
        audit.StockAuditManager auditManager = new audit.StockAuditManager();
        List<audit.StockAuditLog> auditLogs = auditManager.getAllLogs();
        
        for (audit.StockAuditLog auditLog : auditLogs) {
            String[] logEntry = new String[9];
            logEntry[0] = auditLog.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            logEntry[1] = auditLog.getStaffUsername();
            logEntry[2] = String.valueOf(auditLog.getItemCode());
            logEntry[3] = auditLog.getItemName();
            logEntry[4] = auditLog.getItemSize();
            
            // Format stock change
            int change = auditLog.getQuantityChanged();
            String changeStr = (change >= 0 ? "+" : "") + change + "/" + auditLog.getQuantityAfter();
            logEntry[5] = changeStr;
            
            // Action/Status
            logEntry[6] = auditLog.getStatus() + " - " + auditLog.getChangeType();
            
            // Approved By
            logEntry[7] = auditLog.getApprovedBy() != null ? auditLog.getApprovedBy() : 
                         ("PENDING".equals(auditLog.getStatus()) ? "Pending" : "-");
            
            // Details
            String details = auditLog.getReason();
            if (auditLog.getNotes() != null && !auditLog.getNotes().isEmpty()) {
                details += " | " + auditLog.getNotes();
            }
            logEntry[8] = details;
            
            logs.add(logEntry);
        }
        
        // Sort by timestamp (newest first)
        logs.sort((a, b) -> b[0].compareTo(a[0]));
        
        return logs;
    }
    
    /**
     * Handle add item
     */
    private void handleAddItem(TableView<Item> table) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Add New Item");
        dialog.setHeaderText("Enter item details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeField = new TextField();
        codeField.setPromptText("Item Code");
        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");
        ComboBox<String> courseBox = new ComboBox<>();
        courseBox.getItems().addAll(utils.InputValidator.getAllValidCourses());
        courseBox.setPromptText("Select Course");
        ComboBox<String> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(utils.InputValidator.getValidSizes());
        sizeBox.setPromptText("Select Size");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        grid.add(new Label("Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Course:"), 0, 2);
        grid.add(courseBox, 1, 2);
        grid.add(new Label("Size:"), 0, 3);
        grid.add(sizeBox, 1, 3);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(qtyField, 1, 4);
        grid.add(new Label("Price:"), 0, 5);
        grid.add(priceField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int code = Integer.parseInt(codeField.getText());
                    String name = nameField.getText();
                    String course = courseBox.getValue();
                    String size = sizeBox.getValue();
                    int qty = Integer.parseInt(qtyField.getText());
                    double price = Double.parseDouble(priceField.getText());

                    if (name.isEmpty() || course == null || size == null) {
                        AlertHelper.showError("Error", "Please fill all fields");
                        return null;
                    }

                    Item newItem = new Item(code, name, course, size, qty, price);
                    inventoryManager.addItem(newItem);
                    
                    // Log the item addition for transparency
                    StockReturnLogger.logItemAdded("Admin", code, name, size, qty, price);
                    
                    return newItem;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Error", "Invalid number format");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            table.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            AlertHelper.showSuccess("Success", "Item added successfully!");
        });
    }

    /**
     * Handle edit item
     */
    private void handleEditItem(Item item, TableView<Item> table) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Edit Item");
        dialog.setHeaderText("Edit item: " + item.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField qtyField = new TextField(String.valueOf(item.getQuantity()));
        TextField priceField = new TextField(String.valueOf(item.getPrice()));

        grid.add(new Label("Code:"), 0, 0);
        grid.add(new Label(String.valueOf(item.getCode())), 1, 0);
        grid.add(new Label("Name:"), 0, 1);
        grid.add(new Label(item.getName()), 1, 1);
        grid.add(new Label("Course:"), 0, 2);
        grid.add(new Label(item.getCourse()), 1, 2);
        grid.add(new Label("Size:"), 0, 3);
        grid.add(new Label(item.getSize()), 1, 3);
        grid.add(new Label("Quantity:"), 0, 4);
        grid.add(qtyField, 1, 4);
        grid.add(new Label("Price:"), 0, 5);
        grid.add(priceField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int oldQty = item.getQuantity();
                    double oldPrice = item.getPrice();
                    int newQty = Integer.parseInt(qtyField.getText());
                    double newPrice = Double.parseDouble(priceField.getText());

                    // Build update details
                    StringBuilder updateDetails = new StringBuilder("Updated: ");
                    if (oldQty != newQty) {
                        updateDetails.append(String.format("Qty %d→%d ", oldQty, newQty));
                    }
                    if (oldPrice != newPrice) {
                        updateDetails.append(String.format("Price ₱%.2f→₱%.2f", oldPrice, newPrice));
                    }

                    // Update quantity
                    item.setQuantity(newQty);

                    // For price update, we need to create a new item since Item is immutable for price
                    // Remove old item and add new one with updated price
                    inventoryManager.removeItem(item.getCode());
                    Item updatedItem = new Item(item.getCode(), item.getName(), item.getCourse(),
                                               item.getSize(), newQty, newPrice);
                    inventoryManager.addItem(updatedItem);

                    // Log the item update for transparency
                    StockReturnLogger.logItemUpdated("Admin", item.getCode(), item.getName(), 
                                                     item.getSize(), oldQty, newQty, updateDetails.toString());

                    return updatedItem;
                } catch (NumberFormatException e) {
                    AlertHelper.showError("Error", "Invalid number format");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedItem -> {
            table.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            AlertHelper.showSuccess("Success", "Item updated successfully!");
        });
    }

    /**
     * Handle delete item
     */
    private void handleDeleteItem(Item item, TableView<Item> table) {
        boolean confirm = AlertHelper.showConfirmation("Delete Item",
            "Are you sure you want to delete:\n" + item.getName() + " (" + item.getSize() + ")?");

        if (confirm) {
            // Log the item deletion before removing for transparency
            StockReturnLogger.logItemDeleted("Admin", item.getCode(), item.getName(), 
                                            item.getSize(), item.getQuantity());
            
            // Remove all items with this code (all size variants)
            inventoryManager.removeItem(item.getCode());
            table.setItems(FXCollections.observableArrayList(inventoryManager.getAllItems()));
            AlertHelper.showSuccess("Success", "Item deleted successfully!");
        }
    }

    /**
     * Style action button
     */
    private void styleActionButton(Button btn) {
        btn.setPrefHeight(36);
        btn.setStyle(
            "-fx-background-color: #0969DA;" +
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
            Scene scene = new Scene(loginView.getView(), 1920, 1080);
            SceneManager.setScene(scene);
        }
    }
    
    /**
     * Get quick action buttons for wiring in the view
     */
    public Button getApprovePendingBtn() {
        return approvePendingBtn;
    }
    
    public Button getManageAccountsBtn() {
        return manageAccountsBtn;
    }
    
    /**
     * Create System Settings View
     */
    public Node createSystemSettingsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        // Title
        Label titleLabel = new Label("⚙️ System Configuration");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        
        // Maintenance Mode Card
        VBox maintenanceCard = new VBox(15);
        maintenanceCard.setPadding(new Insets(20));
        maintenanceCard.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        
        Label maintenanceTitleLabel = new Label("🔧 Maintenance Mode");
        maintenanceTitleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        
        Label maintenanceDescLabel = new Label(
            "When maintenance mode is activated, staff, cashiers, and students will be unable to log in to the system.\n" +
            "Administrators can still access the system during maintenance mode."
        );
        maintenanceDescLabel.setWrapText(true);
        maintenanceDescLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");
        
        Separator separator = new Separator();
        
        // Status indicator
        utils.SystemConfigManager configManager = utils.SystemConfigManager.getInstance();
        boolean isActive = configManager.isMaintenanceModeActive();
        
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label statusTitleLabel = new Label("Current Status:");
        statusTitleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        
        Label statusValueLabel = new Label(isActive ? "🔴 ACTIVE" : "🟢 INACTIVE");
        statusValueLabel.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: " + (isActive ? "#CF222E" : "#1A7F37") + ";"
        );
        
        statusBox.getChildren().addAll(statusTitleLabel, statusValueLabel);
        
        // Message display
        Label messageLabel = new Label("Current Message:");
        messageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        
        Label messageValueLabel = new Label("\"" + configManager.getMaintenanceMessage() + "\"");
        messageValueLabel.setWrapText(true);
        messageValueLabel.setStyle(
            "-fx-text-fill: -color-fg-muted; " +
            "-fx-font-style: italic; " +
            "-fx-background-color: -color-bg-default; " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 5;"
        );
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button toggleButton = new Button(isActive ? "✅ Deactivate Maintenance Mode" : "🔧 Activate Maintenance Mode");
        toggleButton.setPrefWidth(250);
        toggleButton.setPrefHeight(40);
        toggleButton.setStyle(
            "-fx-background-color: " + (isActive ? "#1A7F37" : "#CF222E") + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        
        Button editMessageButton = new Button("✏️ Edit Message");
        editMessageButton.setPrefWidth(150);
        editMessageButton.setPrefHeight(40);
        editMessageButton.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;"
        );
        
        buttonBox.getChildren().addAll(toggleButton, editMessageButton);
        
        // Toggle button action
        toggleButton.setOnAction(e -> {
            boolean currentStatus = configManager.isMaintenanceModeActive();
            
            String confirmMessage = currentStatus ? 
                "Are you sure you want to DEACTIVATE maintenance mode?\n\n" +
                "Staff, cashiers, and students will be able to log in again." :
                "Are you sure you want to ACTIVATE maintenance mode?\n\n" +
                "This will prevent staff, cashiers, and students from logging in.\n" +
                "Only administrators will have access to the system.";
            
            boolean confirmed = AlertHelper.showConfirmation(
                currentStatus ? "Deactivate Maintenance Mode" : "Activate Maintenance Mode",
                confirmMessage
            );
            
            if (confirmed) {
                configManager.setMaintenanceMode(!currentStatus);
                
                AlertHelper.showSuccess(
                    "Success",
                    "Maintenance mode has been " + (currentStatus ? "DEACTIVATED" : "ACTIVATED") + ".\n\n" +
                    (currentStatus ? 
                        "Users can now log in to the system." :
                        "Non-admin users will see the maintenance message when attempting to log in.")
                );
                
                // Refresh the view
                container.getChildren().clear();
                Node refreshedView = createSystemSettingsView();
                container.getChildren().addAll(((VBox) refreshedView).getChildren());
            }
        });
        
        // Edit message button action
        editMessageButton.setOnAction(e -> {
            javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog(
                configManager.getMaintenanceMessage()
            );
            dialog.setTitle("Edit Maintenance Message");
            dialog.setHeaderText("Enter the maintenance message:");
            dialog.setContentText("Message:");
            
            dialog.showAndWait().ifPresent(newMessage -> {
                if (newMessage.trim().isEmpty()) {
                    AlertHelper.showError("Error", "Message cannot be empty!");
                    return;
                }
                
                configManager.setMaintenanceMessage(newMessage.trim());
                
                AlertHelper.showSuccess("Success", "Maintenance message updated successfully!");
                
                // Refresh the view
                container.getChildren().clear();
                Node refreshedView = createSystemSettingsView();
                container.getChildren().addAll(((VBox) refreshedView).getChildren());
            });
        });
        
        maintenanceCard.getChildren().addAll(
            maintenanceTitleLabel,
            maintenanceDescLabel,
            separator,
            statusBox,
            messageLabel,
            messageValueLabel,
            buttonBox
        );
        
        container.getChildren().addAll(titleLabel, maintenanceCard);
        
        return container;
    }
}

