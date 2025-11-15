package gui.controllers;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import utils.StockReturnLogger;
import gui.utils.AlertHelper;
import gui.utils.ThemeManager;
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
 
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 * StaffDashboardController - Handles staff dashboard operations
 */
@SuppressWarnings("unchecked")
public class StaffDashboardController {

    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    
    // Refresh callback for when reservations are modified
    private Runnable refreshCallback;

    public StaffDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);

        // Load data
        inventoryManager.getAllItems().forEach(item -> {});
    }

    /**
     * Handle price change for an item
     */
    private void handleChangePrice(Item item, TableView<Item> table) {
        TextInputDialog priceDialog = new TextInputDialog(String.format("%.2f", item.getPrice()));
        priceDialog.setTitle("Change Price");
        priceDialog.setHeaderText("Change price for: " + item.getName() + " (" + item.getSize() + ")");
        priceDialog.setContentText("Current Price: ₱" + String.format("%.2f", item.getPrice()) + "\nNew Price:");

        priceDialog.showAndWait().ifPresent(input -> {
            try {
                double newPrice = Double.parseDouble(input.trim());
                if (newPrice < 0) {
                    AlertHelper.showError("Invalid Input", "Price cannot be negative!");
                    return;
                }

                double oldPrice = item.getPrice();
                boolean success = inventoryManager.updateItemPriceBySize(item.getCode(), item.getSize(), newPrice);
                if (success) {
                    // Log legacy change
                    StockReturnLogger.logPriceChange("staff", item.getCode(), item.getName(), item.getSize(), oldPrice, newPrice);

                    // Refresh table
                    table.refresh();

                    AlertHelper.showSuccess("Price Updated",
                        "Price updated successfully!\n\n" +
                        "Item: " + item.getName() + " (" + item.getSize() + ")\n" +
                        "Old Price: ₱" + String.format("%.2f", oldPrice) + "\n" +
                        "New Price: ₱" + String.format("%.2f", newPrice));
                } else {
                    AlertHelper.showError("Error", "Failed to update price!");
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Please enter a valid numeric price.");
            }
        });
    }
    
    /**
     * Set the refresh callback - called when reservations are updated
     */
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    public Node createReservationsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        javafx.scene.control.Label searchLabel = new javafx.scene.control.Label("🔍 Search:");
        searchLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Student Name, ID, Order ID, or Item...");
        searchField.setPrefWidth(400);
        searchField.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #d0d7de;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;" +
            "-fx-font-size: 13px;"
        );
        
        Button clearSearchBtn = new Button("✖ Clear");
        clearSearchBtn.setStyle(
            "-fx-background-color: #6c757d;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );
        
        searchBar.getChildren().addAll(searchLabel, searchField, clearSearchBtn);

        // Statistics cards
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Pending Reservations (deduplicated for bundles)
        int pendingCount = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getPendingReservations()
        ).size();
        VBox pendingCard = createStatCard("⏳ Pending", String.valueOf(pendingCount), "#BF8700");
        
        // Pickup Approvals Needed
        int pickupApprovalsCount = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getPickupRequestsAwaitingApproval()
        ).size();
        VBox pickupApprovalsCard = createStatCard("📦 Pickup Approvals", String.valueOf(pickupApprovalsCount), "#0969DA");
        
        // Completed Reservations
        int completedCount = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations()
        ).stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .count();
        VBox completedCard = createStatCard("✅ Completed", String.valueOf(completedCount), "#1A7F37");
        
        statsBox.getChildren().addAll(pendingCard, pickupApprovalsCard, completedCard);

        // Filter buttons
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button allBtn = new Button("All");
        Button pendingBtn = new Button("Pending");
        Button approvedBtn = new Button("Approved");
        Button pickupApprovalsBtn = new Button("📦 Pickup Approvals");
        Button returnRequestsBtn = new Button("Replacement Requests");
        Button refreshBtn = new Button("🔄 Refresh");

        styleActionButton(allBtn);
        styleActionButton(pendingBtn);
        styleActionButton(approvedBtn);
        styleActionButton(pickupApprovalsBtn);
        styleActionButton(returnRequestsBtn);
        styleActionButton(refreshBtn);

        filterBar.getChildren().addAll(allBtn, pendingBtn, approvedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);

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
                    } else if ("REPLACEMENT REQUESTED".equals(reservation.getStatus())) {
                        approveBtn.setText("✓ Approve Replacement");
                        rejectBtn.setText("✗ Reject Return");
                        approveBtn.setOnAction(e -> handleApproveReturn(reservation, table));
                        rejectBtn.setOnAction(e -> handleRejectReturn(reservation, table));
                        setGraphic(buttons);
                    } else if ("PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(reservation.getStatus())) {
                        approveBtn.setText("✓ Approve Pickup");
                        rejectBtn.setText("✗ Reject");
                        approveBtn.setOnAction(e -> handleApprovePickup(reservation, table));
                        rejectBtn.setOnAction(e -> handleRejectPickup(reservation, table));
                        setGraphic(buttons);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        actionsCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol, bundleCol, actionsCol);

        // Load ONLY PENDING reservations (deduplicated for bundles)
        List<Reservation> pendingReservations = reservationManager.getAllReservations().stream()
            .filter(r -> "PENDING".equals(r.getStatus()) || "REPLACEMENT REQUESTED".equals(r.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> allReservations = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(pendingReservations));
        ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList(allReservations);
        table.setItems(filteredReservations);
        
        // Track current filter for search
        final String[] currentFilter = {"ALL"}; // ALL, PENDING, APPROVED, RETURN_REQUESTS

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase().trim();
            
            if (searchText.isEmpty()) {
                filteredReservations.setAll(allReservations);
            } else {
                List<Reservation> filtered = allReservations.stream()
                    .filter(r -> {
                        // Search by Order ID
                        String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                        if (orderId.toLowerCase().contains(searchText)) {
                            return true;
                        }
                        
                        // Search by Student Name
                        if (r.getStudentName().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        
                        // Search by Student ID
                        if (r.getStudentId().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        
                        // Search by Item Name
                        if (r.getItemName().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        
                        // Search by Status
                        if (r.getStatus().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        
                        // For bundle orders, search in all bundle items
                        if (r.isPartOfBundle()) {
                            String bundleId = r.getBundleId();
                            boolean matchInBundle = reservationManager.getAllReservations().stream()
                                .filter(res -> bundleId.equals(res.getBundleId()))
                                .anyMatch(res -> res.getItemName().toLowerCase().contains(searchText));
                            if (matchInBundle) {
                                return true;
                            }
                        }
                        
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                filteredReservations.setAll(filtered);
            }
        });
        
        // Clear search button action
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });

        // Filter actions
        allBtn.setOnAction(e -> {
            currentFilter[0] = "ALL";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()) || "REPLACEMENT REQUESTED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(filtered));
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });
        pendingBtn.setOnAction(e -> {
            currentFilter[0] = "PENDING";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(filtered));
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });
        approvedBtn.setOnAction(e -> {
            currentFilter[0] = "APPROVED";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("APPROVED"))
                .collect(java.util.stream.Collectors.toList());
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(filtered));
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });
        pickupApprovalsBtn.setOnAction(e -> {
            currentFilter[0] = "PICKUP_APPROVALS";
            List<Reservation> filtered = reservationManager.getPickupRequestsAwaitingApproval();
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(filtered));
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });
        returnRequestsBtn.setOnAction(e -> {
            currentFilter[0] = "RETURN_REQUESTS";
            List<Reservation> filtered = reservationManager.getReturnRequests();
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(filtered));
            searchField.clear();
            filteredReservations.setAll(allReservations);
        });
        refreshBtn.setOnAction(e -> {
            performTableRefresh(allReservations, filteredReservations, searchField, table, statsBox, currentFilter);
        });
        
        // Set the refresh callback for when items are approved/rejected
        this.refreshCallback = () -> {
            performTableRefresh(allReservations, filteredReservations, searchField, table, statsBox, currentFilter);
        };

        VBox.setVgrow(table, Priority.ALWAYS);
        // Do not add the statsBox to the UI (hide the top summary boxes)
        container.getChildren().addAll(searchBar, filterBar, table);

        // Make the container resize-friendly and wrap it in a ScrollPane so
        // the dashboard can be scrolled on smaller screens instead of overflowing.
        container.setMaxWidth(Double.MAX_VALUE);
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Give a small padding so scrollbars don't overlap content on narrow windows
        scroll.setStyle("-fx-padding: 8;");

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

        return scroll;
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
                
                // Determine status indicator and color
                String statusIndicator = "";
                String statusColor = "-color-fg-default";
                
                if (item.getStatus().contains("REPLACED")) {
                    statusIndicator = " (REPLACED)";
                    statusColor = "#656D76"; // Gray
                } else if ("COMPLETED".equals(item.getStatus())) {
                    statusIndicator = " (COMPLETED)";
                    statusColor = "#1A7F37"; // Green
                } else if (item.getStatus().contains("REPLACEMENT REQUESTED")) {
                    statusIndicator = " (REPLACEMENT REQUESTED)";
                    statusColor = "#BF8700"; // Orange
                }
                
                javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + item.getItemName() + statusIndicator);
                itemName.setMinWidth(250);
                itemName.setStyle("-fx-text-fill: " + statusColor + ";");
                
                javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + item.getSize());
                itemSize.setMinWidth(70);
                itemSize.setStyle("-fx-text-fill: " + statusColor + ";");
                
                javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + item.getQuantity());
                itemQty.setMinWidth(60);
                itemQty.setStyle("-fx-text-fill: " + statusColor + ";");
                
                javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", item.getTotalPrice()));
                itemPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
                
                itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
                itemsSection.getChildren().add(itemRow);
                
                // Only add to total if not replaced
                if (!item.getStatus().contains("REPLACED")) {
                    totalPrice += item.getTotalPrice();
                    totalQuantity += item.getQuantity();
                }
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

        // Show reason if exists (for returns, cancellations, rejections)
        if (reservation.getReason() != null && !reservation.getReason().isEmpty()) {
            VBox reasonSection = new VBox(8);
            reasonSection.setStyle("-fx-background-color: #FFF8C5; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #9A6700; -fx-border-width: 1px; -fx-border-radius: 5;");
            
            javafx.scene.control.Label reasonHeader = new javafx.scene.control.Label("REASON/NOTE");
            reasonHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #6F4400;");
            
            javafx.scene.control.Label reasonText = new javafx.scene.control.Label(reservation.getReason());
            reasonText.setWrapText(true);
            reasonText.setStyle("-fx-font-size: 12px; -fx-text-fill: #6F4400;");
            
            reasonSection.getChildren().addAll(reasonHeader, reasonText);
            content.getChildren().add(reasonSection);
        }

        content.getChildren().addAll(customerSection, itemsSection, summarySection);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(600);
        dialog.showAndWait();
    }

    /**
     * Show bundle items dialog - displays all items in a bundle order
     */
    private void showBundleItemsDialog(Reservation reservation) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Bundle Order Details");
        dialog.setHeaderText("Bundle ID: " + reservation.getBundleId());

        javafx.scene.control.ButtonType closeButton = javafx.scene.control.ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");

        // Customer Information
        VBox customerSection = new VBox(8);
        customerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label customerHeader = new javafx.scene.control.Label("CUSTOMER INFORMATION");
        customerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label studentName = new javafx.scene.control.Label("Name: " + reservation.getStudentName());
        javafx.scene.control.Label studentId = new javafx.scene.control.Label("Student ID: " + reservation.getStudentId());
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("Status: " + reservation.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        customerSection.getChildren().addAll(customerHeader, studentName, studentId, statusLabel);

        // Bundle Items Section
        VBox itemsSection = new VBox(8);
        itemsSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label itemsHeader = new javafx.scene.control.Label("BUNDLE ITEMS");
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
            
            javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + item.getItemName());
            itemName.setMinWidth(250);
            itemName.setStyle("-fx-font-size: 12px;");
            
            javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + item.getSize());
            itemSize.setMinWidth(70);
            itemSize.setStyle("-fx-font-size: 11px;");
            
            javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + item.getQuantity());
            itemQty.setMinWidth(60);
            itemQty.setStyle("-fx-font-size: 11px;");
            
            javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", item.getTotalPrice()));
            itemPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            
            itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
            itemsSection.getChildren().add(itemRow);
            
            totalPrice += item.getTotalPrice();
            totalQuantity += item.getQuantity();
        }

        // Separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        separator.setStyle("-fx-padding: 10 0;");
        itemsSection.getChildren().add(separator);

        // Bundle Summary
        HBox summaryRow = new HBox(10);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        summaryRow.setStyle("-fx-padding: 10 0 0 0;");
        
        javafx.scene.control.Label summaryLabel = new javafx.scene.control.Label("TOTAL:");
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        summaryLabel.setMinWidth(250);
        
        javafx.scene.control.Label totalItemsLabel = new javafx.scene.control.Label(bundleItems.size() + " item type(s)");
        totalItemsLabel.setMinWidth(70);
        totalItemsLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label totalQtyLabel = new javafx.scene.control.Label("Qty: " + totalQuantity);
        totalQtyLabel.setMinWidth(60);
        totalQtyLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        javafx.scene.control.Label totalPriceLabel = new javafx.scene.control.Label("₱" + String.format("%.2f", totalPrice));
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
            // Show bundle info with all items
            String bundleId = reservation.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .collect(java.util.stream.Collectors.toList());
            
            StringBuilder bundleDetails = new StringBuilder();
            bundleDetails.append("Approve BUNDLE ORDER for:\n");
            bundleDetails.append(reservation.getStudentName()).append("\n\n");
            bundleDetails.append("Items in bundle:\n");
            
            double totalPrice = 0;
            int totalQuantity = 0;
            for (Reservation item : bundleItems) {
                bundleDetails.append("• ").append(item.getItemName())
                    .append(" (").append(item.getSize()).append(")")
                    .append(" - Qty: ").append(item.getQuantity())
                    .append(" - ₱").append(String.format("%.2f", item.getTotalPrice()))
                    .append("\n");
                totalPrice += item.getTotalPrice();
                totalQuantity += item.getQuantity();
            }
            
            bundleDetails.append("\nTotal Items: ").append(bundleItems.size());
            bundleDetails.append("\nTotal Quantity: ").append(totalQuantity);
            bundleDetails.append("\nTotal Price: ₱").append(String.format("%.2f", totalPrice));
            
            message = bundleDetails.toString();
        } else {
            message = "Approve reservation for:\n" + 
                     reservation.getStudentName() + "\n" +
                     reservation.getItemName() + " (" + reservation.getSize() + ")" + "\n" +
                     "Quantity: " + reservation.getQuantity() + "\n" +
                     "Total: ₱" + String.format("%.2f", reservation.getTotalPrice());
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
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                AlertHelper.showSuccess("Success", reservation.isPartOfBundle() ? "Bundle approved! Student can now pay in Cashier to pickup the item." : "Reservation approved! Student can now pay in Cashier to pickup the item.");
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
                    // Call refresh callback to update the display with current filter applied
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
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
            // Get all items in the bundle with REPLACEMENT REQUESTED status
            itemsToReturn = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .filter(res -> "REPLACEMENT REQUESTED".equals(res.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            if (itemsToReturn.isEmpty()) {
                AlertHelper.showError("Error", "No items in this bundle have pending replacement requests.");
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
        message.append("Approve replacement request for:\n");
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
        message.append("Select replacement item for each.");
        
        boolean confirm = AlertHelper.showConfirmation("Approve Replacement", message.toString());

        if (confirm) {
            // Approve replacement for all items - show dialog to select replacement item
            boolean allSuccess = true;
            int successCount = 0;
            
            for (Reservation item : itemsToReturn) {
                // Show item selection dialog for replacement
                Item selectedReplacement = showReplacementItemSelection(item);
                if (selectedReplacement != null) {
                    boolean success = reservationManager.approveReplacementWithItem(
                        item.getReservationId(),
                        selectedReplacement.getCode(),
                        selectedReplacement.getName(),
                        selectedReplacement.getSize()
                    );
                    if (success) {
                        successCount++;
                    } else {
                        allSuccess = false;
                    }
                } else {
                    allSuccess = false; // User cancelled selection
                }
            }
            
            if (allSuccess) {
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                String successMsg = reservation.isPartOfBundle() ?
                    "Replacement approved for all " + successCount + " items!\n\n" :
                    "Replacement approved!\n\n";
                
                AlertHelper.showSuccess("Success",
                    successMsg + "Items have been replaced successfully. previous item is back in inventory.");
            } else if (successCount > 0) {
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                AlertHelper.showWarning("Partial Success",
                    "Replacement approved for " + successCount + " out of " + itemsToReturn.size() + " items.\n" +
                    "Previous items are back in inventory.");
            } else {
                AlertHelper.showError("Error", "Failed to approve replacement. Insufficient stock for replacement items.");
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
                    // Call refresh callback to update the display with current filter applied
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    AlertHelper.showSuccess("Success", "Return request rejected");
                } else {
                    AlertHelper.showError("Error", "Failed to reject return request");
                }
            }
        });
    }

    /**
     * Handle approve pickup request
     */
    private void handleApprovePickup(Reservation reservation, TableView<Reservation> table) {
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Pickup Request");
        confirmAlert.setHeaderText("Approve pickup for: " + reservation.getStudentName());
        
        String itemInfo;
        if (reservation.isPartOfBundle()) {
            String bundleId = reservation.getBundleId();
            long itemCount = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .count();
            itemInfo = "Bundle Order (" + itemCount + " items)";
        } else {
            itemInfo = reservation.getItemName() + " - " + reservation.getSize() + "\nQuantity: " + reservation.getQuantity() + "x";
        }
        
        confirmAlert.setContentText(
            "Item: " + itemInfo + "\n" +
            "Total: ₱" + String.format("%.2f", reservation.getTotalPrice()) + "\n\n" +
            "Approve this pickup request?"
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                boolean allSuccess = true;
                
                if (reservation.isPartOfBundle()) {
                    // Approve all items in the bundle
                    String bundleId = reservation.getBundleId();
                    List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                        .filter(r -> bundleId.equals(r.getBundleId()))
                        .filter(r -> "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
                    
                    for (Reservation item : bundleItems) {
                        boolean success = reservationManager.approvePickupRequest(item.getReservationId());
                        if (!success) {
                            allSuccess = false;
                        }
                    }
                } else {
                    allSuccess = reservationManager.approvePickupRequest(reservation.getReservationId());
                }
                
                if (allSuccess) {
                    // Call refresh callback to update the display with current filter applied
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    AlertHelper.showSuccess("Success", 
                        reservation.isPartOfBundle() ? "Bundle pickup approved! Students now claimed the item." : "Pickup approved! Student now claimed the item.");
                } else {
                    AlertHelper.showError("Error", "Failed to approve pickup request");
                }
            }
        });
    }

    /**
     * Handle reject pickup request
     */
    private void handleRejectPickup(Reservation reservation, TableView<Reservation> table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Pickup Request");
        dialog.setHeaderText("Reject pickup request for: " + reservation.getStudentName());
        dialog.setContentText("Reason for rejection:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.isEmpty()) {
                javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm Rejection");
                confirmAlert.setHeaderText("This will change status back to 'PAID - AWAITING PICKUP APPROVAL'");
                confirmAlert.setContentText("Student will need to request pickup again. Continue?");
                
                confirmAlert.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        boolean allSuccess = true;
                        
                        if (reservation.isPartOfBundle()) {
                            // Reject all items in the bundle
                            String bundleId = reservation.getBundleId();
                            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                                .filter(r -> bundleId.equals(r.getBundleId()))
                                .filter(r -> "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus()))
                                .collect(java.util.stream.Collectors.toList());
                            
                            for (Reservation item : bundleItems) {
                                boolean success = reservationManager.updateReservationStatus(
                                    item.getReservationId(), 
                                    "PAID - AWAITING PICKUP APPROVAL", 
                                    "Pickup request rejected: " + reason
                                );
                                if (!success) {
                                    allSuccess = false;
                                }
                            }
                        } else {
                            allSuccess = reservationManager.updateReservationStatus(
                                reservation.getReservationId(), 
                                "PAID - AWAITING PICKUP APPROVAL", 
                                "Pickup request rejected: " + reason
                            );
                        }
                        
                        if (allSuccess) {
                            // Call refresh callback to update the display with current filter applied
                            if (refreshCallback != null) {
                                refreshCallback.run();
                            }
                            AlertHelper.showSuccess("Success", "Pickup request rejected. Reason: " + reason);
                        } else {
                            AlertHelper.showError("Error", "Failed to reject pickup request");
                        }
                    }
                });
            }
        });
    }

    /**
     * Show pickup approval order details dialog
     */
    private void showPickupApprovalDetailsDialog(Reservation reservation) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Pickup Approval Details");
        dialog.setHeaderText("Order ID: " + (reservation.isPartOfBundle() ? reservation.getBundleId() : String.valueOf(reservation.getReservationId())));

        javafx.scene.control.ButtonType closeButton = javafx.scene.control.ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: -color-bg-default;");

        // Status Info - Highlight that it needs approval
        VBox statusBox = new VBox(8);
        statusBox.setStyle("-fx-background-color: #DDF4FF; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #0969DA; -fx-border-width: 2px; -fx-border-radius: 5;");
        
        javafx.scene.control.Label statusHeader = new javafx.scene.control.Label("⏳ AWAITING APPROVAL");
        statusHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0550AE;");
        
        javafx.scene.control.Label statusNote = new javafx.scene.control.Label("Student has requested to pickup this order. Review the details and approve or reject.");
        statusNote.setWrapText(true);
        statusNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #0550AE;");
        
        statusBox.getChildren().addAll(statusHeader, statusNote);

        // Customer Information Section
        VBox customerSection = new VBox(8);
        customerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label customerHeader = new javafx.scene.control.Label("CUSTOMER INFORMATION");
        customerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label studentName = new javafx.scene.control.Label("Name: " + reservation.getStudentName());
        javafx.scene.control.Label studentId = new javafx.scene.control.Label("Student ID: " + reservation.getStudentId());
        javafx.scene.control.Label studentCourse = new javafx.scene.control.Label("Course: " + reservation.getCourse());
        
        customerSection.getChildren().addAll(customerHeader, studentName, studentId, studentCourse);

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
        
        javafx.scene.control.Label qtyLabel = new javafx.scene.control.Label("Total Quantity: " + totalQuantity);
        qtyLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label totalLabel = new javafx.scene.control.Label("Total Paid: ₱" + String.format("%.2f", totalPrice));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1A7F37;");
        
        javafx.scene.control.Label orderTypeLabel = new javafx.scene.control.Label("Order Type: " + (reservation.isPartOfBundle() ? "Bundle Order" : "Single Item"));
        orderTypeLabel.setStyle("-fx-font-size: 12px;");
        
        summarySection.getChildren().addAll(summaryHeader, orderTypeLabel, qtyLabel, totalLabel);

        content.getChildren().addAll(statusBox, customerSection, itemsSection, summarySection);

        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background: -color-bg-default; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.showAndWait();
    }

    public Node createInventoryView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Statistics cards
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Total Items
        int totalItems = inventoryManager.getAllItems().size();
        VBox itemsCard = createStatCard("📦 Total Items", String.valueOf(totalItems), "#0969DA");
        
        // Low Stock Items
        int lowStockCount = (int) inventoryManager.getAllItems().stream()
            .filter(item -> item.getQuantity() < 10)
            .count();
        VBox lowStockCard = createStatCard("⚠️ Low Stock", String.valueOf(lowStockCount), "#CF222E");
        
        statsBox.getChildren().addAll(itemsCard, lowStockCard);

        // Course filter buttons (All + per-course)
        HBox courseBar = new HBox(8);
        courseBar.setAlignment(Pos.CENTER_LEFT);
        courseBar.setPadding(new Insets(0, 0, 8, 0));

        // Build course buttons
        List<String> availableCourses = inventoryManager.getAvailableCourses();
        availableCourses.removeIf(s -> s == null || s.trim().isEmpty());
        if (!availableCourses.contains("STI Special")) availableCourses.add(0, "STI Special");
        // Ensure stable ordering and include 'All'
        java.util.Collections.sort(availableCourses);
        javafx.scene.control.ToggleGroup courseToggle = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.ToggleButton allBtnCourse = new javafx.scene.control.ToggleButton("All");
        allBtnCourse.setToggleGroup(courseToggle);
        allBtnCourse.setSelected(true);
        allBtnCourse.setStyle(getCourseButtonStyle(true));
        courseBar.getChildren().add(allBtnCourse);
        for (String c : availableCourses) {
            javafx.scene.control.ToggleButton tb = new javafx.scene.control.ToggleButton(c);
            tb.setToggleGroup(courseToggle);
            tb.setStyle(getCourseButtonStyle(false));
            courseBar.getChildren().add(tb);
        }

        // Ensure toggle button styles update when selection changes
        courseToggle.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            for (javafx.scene.control.Toggle t : courseToggle.getToggles()) {
                javafx.scene.control.ToggleButton b = (javafx.scene.control.ToggleButton) t;
                b.setStyle(getCourseButtonStyle(b.isSelected()));
            }
        });

        // Also update course button styles when the application theme changes
        // (ThemeManager notifies registered listeners on setTheme)
        Runnable courseThemeRefresher = () -> {
            try {
                for (javafx.scene.control.Toggle t : courseToggle.getToggles()) {
                    javafx.scene.control.ToggleButton b = (javafx.scene.control.ToggleButton) t;
                    // run on FX thread if needed
                    if (javafx.application.Platform.isFxApplicationThread()) {
                        b.setStyle(getCourseButtonStyle(b.isSelected()));
                    } else {
                        javafx.application.Platform.runLater(() -> b.setStyle(getCourseButtonStyle(b.isSelected())));
                    }
                }
            } catch (Exception ex) {
                // ignore
            }
        };
        ThemeManager.addThemeChangeListener(courseThemeRefresher);

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        Button addItemBtn = new Button("＋ Add Item");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by item name or code...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);
        styleActionButton(addItemBtn);

        actionBar.getChildren().addAll(refreshBtn, addItemBtn, searchField);

        // Add Item button action - open dialog to create new item
        addItemBtn.setOnAction(e -> {
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Add New Item");

            javafx.scene.control.ButtonType addBtnType = new javafx.scene.control.ButtonType("Add", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType cancelBtnType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(addBtnType, cancelBtnType);

            VBox content = new VBox(10);
            content.setPadding(new Insets(12));

            // Generate next item code
            final int[] nextCode = new int[] { 1001 };
            List<Item> existing = inventoryManager.getAllItems();
            for (Item it : existing) {
                if (it.getCode() >= nextCode[0]) nextCode[0] = it.getCode() + 1;
            }

            javafx.scene.control.Label codeLabel = new javafx.scene.control.Label("Item Code: " + nextCode[0]);

            TextField nameField = new TextField();
            nameField.setPromptText("Item Name");

            // Course selection - include existing courses and an "STI Special" option
            javafx.scene.control.ComboBox<String> courseCombo = new javafx.scene.control.ComboBox<>();
            List<String> courses = inventoryManager.getAvailableCourses();
            courses.removeIf(s -> s == null || s.trim().isEmpty());
            if (!courses.contains("STI Special")) {
                courses.add(0, "STI Special");
            }
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            courseCombo.setEditable(true);
            courseCombo.setPromptText("Course or 'STI Special'");

            javafx.scene.control.ComboBox<String> sizeCombo = new javafx.scene.control.ComboBox<>();
            sizeCombo.setItems(FXCollections.observableArrayList("S", "M", "L", "XL", "One Size"));
            sizeCombo.setPromptText("Size");

            TextField qtyField = new TextField();
            qtyField.setPromptText("Quantity");

            TextField priceField = new TextField();
            priceField.setPromptText("Price (e.g. 450.00)");

            content.getChildren().addAll(codeLabel, nameField, courseCombo, sizeCombo, qtyField, priceField);

            dialog.getDialogPane().setContent(content);

            // Enable/disable Add button based on validation
            javafx.scene.control.Button addActionBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(addBtnType);
            addActionBtn.setDisable(true);

            // Simple validation listener
            Runnable validate = () -> {
                boolean ok = !nameField.getText().trim().isEmpty()
                         && courseCombo.getValue() != null && !courseCombo.getValue().trim().isEmpty()
                         && sizeCombo.getValue() != null && !sizeCombo.getValue().trim().isEmpty();
                try {
                    int q = Integer.parseInt(qtyField.getText().trim());
                    double p = Double.parseDouble(priceField.getText().trim());
                    ok = ok && q >= 0 && p >= 0;
                } catch (Exception ex) {
                    ok = false;
                }
                addActionBtn.setDisable(!ok);
            };

            nameField.textProperty().addListener((obs, o, n) -> validate.run());
            courseCombo.valueProperty().addListener((obs, o, n) -> validate.run());
            sizeCombo.valueProperty().addListener((obs, o, n) -> validate.run());
            qtyField.textProperty().addListener((obs, o, n) -> validate.run());
            priceField.textProperty().addListener((obs, o, n) -> validate.run());

            dialog.setResultConverter(button -> {
                if (button == addBtnType) {
                    try {
                        String name = nameField.getText().trim();
                        String course = courseCombo.getValue().trim();
                        String size = sizeCombo.getValue().trim();
                        int qty = Integer.parseInt(qtyField.getText().trim());
                        double price = Double.parseDouble(priceField.getText().trim());

                        Item newItem = new Item(nextCode[0], name, course, size, qty, price);
                        inventoryManager.addItem(newItem);

                        // Log the new item addition to stock logs
                        StockReturnLogger.logItemAdded("staff", nextCode[0], name, size, qty, price);

                        // Refresh table and stats by invoking the refresh button action
                        refreshBtn.fire();

                    } catch (Exception ex) {
                        // ignore - validation prevents this
                    }
                }
                return null;
            });

            dialog.showAndWait();
        });

        // Create inventory table
        TableView<Item> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Item, Integer> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCode()));
        codeCol.setPrefWidth(80);

        TableColumn<Item, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<Item, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourse()));
        courseCol.setPrefWidth(100);

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
        
        // Actions column - Adjust Stock and Change Price buttons
        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Item, Void>() {
            private final Button adjustBtn = new Button("📝 Adjust Stock");
            private final Button priceBtn = new Button("₱ Change Price");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Item currentItem = getTableView().getItems().get(getIndex());
                    adjustBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                    adjustBtn.setOnAction(e -> handleStockAdjustment(currentItem, table));

                    priceBtn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-cursor: hand;");
                    priceBtn.setOnAction(e -> handleChangePrice(currentItem, table));

                    HBox btns = new HBox(8, adjustBtn, priceBtn);
                    btns.setAlignment(Pos.CENTER);
                    setGraphic(btns);
                }
            }
        });
        actionsCol.setPrefWidth(220);

        table.getColumns().addAll(codeCol, nameCol, courseCol, sizeCol, qtyCol, priceCol, actionsCol);

        // Make columns resize to fill the available width of the container
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefWidth(Double.MAX_VALUE);

        // Bind column widths as percentages of the table width so the table fills its box
        codeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.42));
        courseCol.prefWidthProperty().bind(table.widthProperty().multiply(0.14));
        sizeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        qtyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08));
        priceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        actionsCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));

        // Keep table visual size consistent when limiting rows: fix row height and pref height
        // (pref height will be set after itemsPerPage is declared below)

        // Load all items (we'll manage paging/filtering)
        List<Item> allItems = inventoryManager.getAllItems();

        final int[] currentPage = new int[] { 1 };
        final int itemsPerPage = 10;
        final String[] currentCourse = new String[] { "All" };
        // Sliding window start for page numbers (so 1 2 3 ... N can slide to 2 3 4 ... N)
        final int[] pageWindowStart = new int[] { 1 };

        // Pagination controls container
        HBox pageControls = new HBox(6);
        pageControls.setAlignment(Pos.CENTER_LEFT);
        pageControls.setPadding(new Insets(8, 0, 0, 0));

        // pageControls created earlier - we will update it via helper method below

        // Search functionality -> reset to page 1 and update via helper
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
        });

        // Course toggle action -> update via helper
        courseToggle.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                courseToggle.selectToggle((javafx.scene.control.Toggle) courseToggle.getToggles().get(0));
                currentCourse[0] = "All";
            } else {
                javafx.scene.control.ToggleButton tb = (javafx.scene.control.ToggleButton) newT;
                currentCourse[0] = tb.getText();
            }
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Item> refreshed = inventoryManager.getAllItems();
            allItems.clear();
            allItems.addAll(refreshed);
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        // Add course bar, action bar, table and pagination controls
        container.getChildren().addAll(courseBar, actionBar, table, pageControls);

        // Now that itemsPerPage is known, make the rows scale to fill the available table height
        // Bind fixedCellSize so rows expand/shrink to fill the table area and remove empty gap below
        // Use a stable fixed row height so layout stays predictable and table fills the box
        final double headerReserve = 56; // approximate height occupied by headers and paddings
        final double rowHeight = 65; // stable row height
        table.setFixedCellSize(rowHeight);
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        // initial display
        updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);

        return container;
    }

    /**
     * Update inventory table contents and rebuild pagination controls
     */
    private void updateInventoryTable(TableView<Item> table, List<Item> allItems, String[] currentCourse,
                                      int[] currentPage, int itemsPerPage, HBox pageControls, HBox statsBox,
                                      TextField searchField, int[] pageWindowStart) {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Item> filtered = allItems.stream()
            .filter(it -> {
                boolean courseMatch = "All".equalsIgnoreCase(currentCourse[0]) ||
                                      (it.getCourse() != null && it.getCourse().equalsIgnoreCase(currentCourse[0])) ||
                                      ("STI Special".equalsIgnoreCase(it.getCourse()) && "STI Special".equalsIgnoreCase(currentCourse[0]));
                boolean searchMatch = q.isEmpty() || (it.getName() != null && it.getName().toLowerCase().contains(q)) || String.valueOf(it.getCode()).contains(q);
                return courseMatch && searchMatch;
            })
            .collect(java.util.stream.Collectors.toList());

        int totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / itemsPerPage));
        if (currentPage[0] > totalPages) currentPage[0] = totalPages;

        int start = (currentPage[0] - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filtered.size());
        List<Item> pageItems = filtered.isEmpty() ? java.util.Collections.emptyList() : filtered.subList(start, end);

        table.setItems(FXCollections.observableArrayList(pageItems));

        // Build new pagination controls - simple clean design: [Previous] [Page X of Y] [Search Box] [Next]
        pageControls.getChildren().clear();
        pageControls.setSpacing(12);

        // Previous button
        Button prevBtn = new Button("← Previous");
        prevBtn.setDisable(currentPage[0] <= 1);
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        prevBtn.setOnAction(ev -> {
            if (currentPage[0] > 1) {
                currentPage[0]--;
                updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
            }
        });

        // Page info label
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label("Page " + currentPage[0] + " of " + totalPages);
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        // Go to page input field
        TextField goToPageField = new TextField();
        goToPageField.setPromptText("Go to page...");
        goToPageField.setStyle("-fx-padding: 6 8; -fx-font-size: 12; -fx-pref-width: 120;");
        goToPageField.setOnAction(ev -> {
            try {
                String input = goToPageField.getText().trim();
                if (!input.isEmpty()) {
                    int pageNum = Integer.parseInt(input);
                    if (pageNum >= 1 && pageNum <= totalPages) {
                        currentPage[0] = pageNum;
                        goToPageField.clear();
                        updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
                    } else {
                        goToPageField.setStyle("-fx-padding: 6 8; -fx-font-size: 12; -fx-pref-width: 120; -fx-border-color: #ff6b6b;");
                        goToPageField.clear();
                        goToPageField.setPromptText("Invalid page (1-" + totalPages + ")");
                    }
                }
            } catch (NumberFormatException ex) {
                goToPageField.setStyle("-fx-padding: 6 8; -fx-font-size: 12; -fx-pref-width: 120; -fx-border-color: #ff6b6b;");
                goToPageField.clear();
                goToPageField.setPromptText("Enter a valid number");
            }
        });

        // Next button
        Button nextBtn = new Button("Next →");
        nextBtn.setDisable(currentPage[0] >= totalPages);
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        nextBtn.setOnAction(ev -> {
            if (currentPage[0] < totalPages) {
                currentPage[0]++;
                updateInventoryTable(table, allItems, currentCourse, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
            }
        });

        // Add all controls to the container and center them
        pageControls.getChildren().addAll(prevBtn, pageLabel, goToPageField, nextBtn);
        pageControls.setAlignment(Pos.CENTER);

        // Update stats badge for total items (all items in inventory)
        try {
            ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(0)).getChildren().get(1)).setText(String.valueOf(allItems.size()));
        } catch (Exception ex) {
            // ignore if layout differs
        }
    }

    /**
     * Create staff analytics dashboard view
     */
    public Node createStaffDashboardView() {
        VBox container = new VBox(24);
        container.setPadding(new Insets(24));

        List<Reservation> allReservations = reservationManager.getAllReservations();
        List<Item> allItems = inventoryManager.getAllItems();
        List<Reservation> completedReservations = allReservations.stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .collect(Collectors.toList());

        YearMonth currentMonth = YearMonth.now();
        LocalDate today = LocalDate.now();

        double salesThisMonth = calculateSales(allReservations, reservation ->
            "COMPLETED".equals(reservation.getStatus()) &&
            YearMonth.from(getRelevantDate(reservation)).equals(currentMonth)
        );

        double overallSales = calculateSales(allReservations,
            reservation -> "COMPLETED".equals(reservation.getStatus()));

        long ordersToday = countOrders(allReservations, reservation ->
            "COMPLETED".equals(reservation.getStatus()) &&
            getRelevantDate(reservation).equals(today)
        );

        long lowStockCount = allItems.stream()
            .filter(item -> item.getQuantity() <= 15 && item.getQuantity() > 5)
            .count();
        long criticalStockCount = allItems.stream()
            .filter(item -> item.getQuantity() > 0 && item.getQuantity() <= 5)
            .count();
        long outOfStockCount = allItems.stream()
            .filter(item -> item.getQuantity() == 0)
            .count();

        // Stock Status overview: show key metric cards (Net Sales month, Net Sales all-time,
        // Orders today, Completed orders). Low/critical segmented bar removed per request.
        VBox stockStatusBox = new VBox(8);
        stockStatusBox.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 14; -fx-background-radius: 8; -fx-border-radius:8;");

        javafx.scene.control.Label stockTitle = new javafx.scene.control.Label("Stock Status");
        stockTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        int totalProducts = allItems.size();
        int completedOrdersCount = completedReservations.size();

        javafx.scene.control.Label productsLabel = new javafx.scene.control.Label(totalProducts + " Products");
        productsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        // Progress bar made from regions with proportional widths (Stock Status bar)
        HBox barContainer = new HBox();
        barContainer.setStyle("-fx-background-color: #edf2f6; -fx-background-radius: 8; -fx-padding: 0;");
        barContainer.setPrefHeight(18);
        barContainer.setMaxWidth(Double.MAX_VALUE);
        barContainer.setMinHeight(18);
        barContainer.setAlignment(Pos.CENTER_LEFT);
        barContainer.setSpacing(0);
        HBox.setHgrow(barContainer, Priority.ALWAYS);

        int lowCount = (int) lowStockCount;
        int criticalCount = (int) criticalStockCount;
        int outOfStock = (int) outOfStockCount;
        int inStockCount = Math.max(0, totalProducts - lowCount - criticalCount - outOfStock);

        Region inRegion = new Region();
        inRegion.setStyle("-fx-background-color: #1A7F37; -fx-background-radius: 6 0 0 6; -fx-min-width: 20;");
        Region lowRegion = new Region();
        lowRegion.setStyle("-fx-background-color: #FB8C00; -fx-background-radius: 0 0 0 0; -fx-min-width: 1;");
        Region critRegion = new Region();
        critRegion.setStyle("-fx-background-color: #CF222E; -fx-background-radius: 0 0 0 0; -fx-min-width: 1;");
        Region outRegion = new Region();
        outRegion.setStyle("-fx-background-color: #6B5B95; -fx-background-radius: 0 6 6 0; -fx-min-width: 1;");

        // Let the regions expand/shrink correctly inside the HBox
        HBox.setHgrow(inRegion, Priority.ALWAYS);
        HBox.setHgrow(lowRegion, Priority.ALWAYS);
        HBox.setHgrow(critRegion, Priority.ALWAYS);
        HBox.setHgrow(outRegion, Priority.ALWAYS);
        inRegion.setMinHeight(18);
        lowRegion.setMinHeight(18);
        critRegion.setMinHeight(18);
        outRegion.setMinHeight(18);

        double denom = Math.max(1, (double) totalProducts);
        inRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) inStockCount / denom));
        lowRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) lowCount / denom));
        critRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) criticalCount / denom));
        outRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) outOfStock / denom));

        barContainer.getChildren().addAll(inRegion, lowRegion, critRegion, outRegion);

        // Legend
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);

        Region legendIn = new Region(); legendIn.setPrefSize(12,12); legendIn.setStyle("-fx-background-color: #1A7F37; -fx-background-radius:2;");
        Region legendLow = new Region(); legendLow.setPrefSize(12,12); legendLow.setStyle("-fx-background-color: #FB8C00; -fx-background-radius:2;");
        Region legendCrit = new Region(); legendCrit.setPrefSize(12,12); legendCrit.setStyle("-fx-background-color: #CF222E; -fx-background-radius:2;");
        Region legendOut = new Region(); legendOut.setPrefSize(12,12); legendOut.setStyle("-fx-background-color: #6B5B95; -fx-background-radius:2;");

        javafx.scene.control.Label lblIn = new javafx.scene.control.Label("In stock: " + inStockCount);
        javafx.scene.control.Label lblLow = new javafx.scene.control.Label("Low stock: " + lowCount);
        javafx.scene.control.Label lblCrit = new javafx.scene.control.Label("Critical: " + criticalCount);
        javafx.scene.control.Label lblOut = new javafx.scene.control.Label("Out of stock: " + outOfStock);

        HBox inItem = new HBox(6, legendIn, lblIn);
        HBox lowItem = new HBox(6, legendLow, lblLow);
        HBox critItem = new HBox(6, legendCrit, lblCrit);
        HBox outItem = new HBox(6, legendOut, lblOut);

        legend.getChildren().addAll(inItem, lowItem, critItem, outItem);

        // Metric cards row
        HBox metricsRow = new HBox(12);
        metricsRow.setAlignment(Pos.CENTER_LEFT);

        // Helper to create a small card
        java.util.function.BiFunction<String, String, VBox> makeCard = (title, value) -> {
            VBox card = new VBox(6);
            card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 6; -fx-border-radius:6; -fx-border-color: rgba(0,0,0,0.04);");
            javafx.scene.control.Label t = new javafx.scene.control.Label(title);
            t.setStyle("-fx-font-size: 12px; -fx-text-fill: -color-fg-muted;");
            javafx.scene.control.Label v = new javafx.scene.control.Label(value);
            v.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
            card.getChildren().addAll(t, v);
            return card;
        };

        String salesMonthStr = String.format("₱%.2f", salesThisMonth);
        String salesAllStr = String.format("₱%.2f", overallSales);
        String ordersTodayStr = String.valueOf(ordersToday);
        String completedStr = String.valueOf(completedOrdersCount);

        VBox card1 = makeCard.apply("Net Sales (This Month)", salesMonthStr);
        VBox card2 = makeCard.apply("Net Sales (All Time)", salesAllStr);
        VBox card3 = makeCard.apply("Orders Today", ordersTodayStr);
        VBox card4 = makeCard.apply("Completed Orders", completedStr);

        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);
        HBox.setHgrow(card4, Priority.ALWAYS);

        metricsRow.getChildren().addAll(card1, card2, card3, card4);

        stockStatusBox.getChildren().addAll(stockTitle, productsLabel, barContainer, legend, metricsRow);

        javafx.scene.control.Label trendingLabel = new javafx.scene.control.Label("Trending");
        trendingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox trendRow = new HBox(20);
        trendRow.setAlignment(Pos.CENTER);
        trendRow.setPrefHeight(320);
        BarChart<String, Number> weeklySalesChart = buildWeeklySalesChart(completedReservations, allReservations);
        LineChart<String, Number> salesTrendChart = buildSalesTrendChart(completedReservations, allReservations);
        HBox.setHgrow(weeklySalesChart, Priority.ALWAYS);
        HBox.setHgrow(salesTrendChart, Priority.ALWAYS);
        trendRow.getChildren().addAll(weeklySalesChart, salesTrendChart);

        javafx.scene.control.Label breakdownLabel = new javafx.scene.control.Label("Product Breakdown");
        breakdownLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<ProductStat> breakdownTable = buildProductBreakdownTable(allReservations);
        TableView<Item> lowStockTable = buildLowStockTable(allItems);

        VBox alertsSection = new VBox(8,
            new javafx.scene.control.Label("Inventory Alerts"),
            lowStockTable
        );
        ((javafx.scene.control.Label) alertsSection.getChildren().get(0))
            .setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        container.getChildren().addAll(
            stockStatusBox,
            trendingLabel,
            trendRow,
            breakdownLabel,
            breakdownTable,
            alertsSection
        );

        // Wrap the dashboard in a ScrollPane so the content becomes scrollable
        // on smaller windows instead of being clipped.
        container.setMaxWidth(Double.MAX_VALUE);
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-padding: 8;");

        return scroll;
    }
    
    /**
     * Handle stock adjustment request for an item
     */
    private void handleStockAdjustment(Item item, TableView<Item> table) {
        TextInputDialog newQtyDialog = new TextInputDialog(String.valueOf(item.getQuantity()));
        newQtyDialog.setTitle("Adjust Stock");
        newQtyDialog.setHeaderText("Adjust stock for: " + item.getName() + " (" + item.getSize() + ")");
        newQtyDialog.setContentText("Current Quantity: " + item.getQuantity() + "\nNew Quantity:");

        newQtyDialog.showAndWait().ifPresent(input -> {
            try {
                int newQuantity = Integer.parseInt(input.trim());

                if (newQuantity < 0) {
                    AlertHelper.showError("Invalid Input", "Quantity cannot be negative!");
                    return;
                }

                if (newQuantity == item.getQuantity()) {
                    AlertHelper.showInfo("No Change", "New quantity is the same as current quantity.");
                    return;
                }

                // Calculate the difference
                int oldQuantity = item.getQuantity();
                int stockChange = newQuantity - oldQuantity;

                // Apply the change immediately (staff can adjust without admin approval)
                boolean success = inventoryManager.updateItemQuantityBySize(
                    item.getCode(),
                    item.getSize(),
                    newQuantity
                );

                if (success) {
                    // Log the change into the legacy stock logs so Admin can see it in the Admin UI
                    String details = String.format("Adjusted by staff: %s → %s", oldQuantity, newQuantity);
                    StockReturnLogger.logItemUpdated("staff", item.getCode(), item.getName(), item.getSize(), oldQuantity, newQuantity, details);

                    // Refresh the table to show updated stock
                    table.refresh();

                    AlertHelper.showSuccess("Stock Updated",
                        "Stock updated successfully!\n\n" +
                        "Item: " + item.getName() + " (" + item.getSize() + ")\n" +
                        "Old Quantity: " + oldQuantity + "\n" +
                        "New Quantity: " + newQuantity + "\n" +
                        "Change: " + (stockChange > 0 ? "+" : "") + stockChange);
                } else {
                    AlertHelper.showError("Error", "Failed to update stock!");
                }

            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Please enter a valid number!");
            }
        });
    }

    public Node createStockLogsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Create TabPane with two tabs: Student Stock Logs and Staff Stock Logs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Tab 1: Student Stock Logs
        Tab studentLogsTab = new Tab("📚 Student Stock Logs", createStudentStockLogsTab());
        studentLogsTab.setDisable(false);
        
        // Tab 2: Staff Stock Logs (what this staff member updated)
        Tab staffLogsTab = new Tab("👤 Staff Stock Logs", createStaffStockLogsTab());
        staffLogsTab.setDisable(false);
        
        tabPane.getTabs().addAll(studentLogsTab, staffLogsTab);
        
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        container.getChildren().add(tabPane);

        return container;
    }

    /**
     * Create Student Stock Logs tab
     */
    private Node createStudentStockLogsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create stock logs table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");
        table.setPrefHeight(400);

        TableColumn<String[], String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        timestampCol.setPrefWidth(150);

        TableColumn<String[], String> performedByCol = new TableColumn<>("Student/User");
        performedByCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        performedByCol.setPrefWidth(120);

        TableColumn<String[], String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        codeCol.setPrefWidth(80);

        TableColumn<String[], String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        itemCol.setPrefWidth(150);

        TableColumn<String[], String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        sizeCol.setPrefWidth(60);

        TableColumn<String[], String> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        changeCol.setPrefWidth(80);

        TableColumn<String[], String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6]));
        actionCol.setPrefWidth(100);

        TableColumn<String[], String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[7]));
        detailsCol.setPrefWidth(200);

        table.getColumns().addAll(timestampCol, performedByCol, codeCol, itemCol, sizeCol, changeCol, actionCol, detailsCol);

        // Pagination setup
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };
        
        // Load stock logs
        List<String[]> allLogs = loadStockLogs();
        
        // Pagination controls
        HBox pageControls = new HBox(12);
        pageControls.setAlignment(Pos.CENTER);
        pageControls.setPadding(new Insets(12, 0, 0, 0));
        
        Button prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label();
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        
        Button nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        
        pageControls.getChildren().addAll(prevBtn, pageLabel, nextBtn);
        
        // Function to update table with current page
        Runnable updateTable = () -> {
            List<String[]> displayLogs = allLogs;
            
            // Apply search filter if any
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                displayLogs = displayLogs.stream()
                    .filter(log -> String.join(" ", log).toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            int totalPages = Math.max(1, (int) Math.ceil((double) displayLogs.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;
            
            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, displayLogs.size());
            
            List<String[]> pageItems = displayLogs.isEmpty() ? java.util.Collections.emptyList() : displayLogs.subList(start, end);
            table.setItems(FXCollections.observableArrayList(pageItems));
            
            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };
        
        // Previous button action
        prevBtn.setOnAction(e -> {
            if (currentPage[0] > 1) {
                currentPage[0]--;
                updateTable.run();
            }
        });
        
        // Next button action
        nextBtn.setOnAction(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) allLogs.size() / itemsPerPage));
            if (currentPage[0] < totalPages) {
                currentPage[0]++;
                updateTable.run();
            }
        });
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage[0] = 1;
            updateTable.run();
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            allLogs.clear();
            allLogs.addAll(loadStockLogs());
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
        });

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
        
        // Set fixed row height to match inventory table (65px) for consistency
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);
        
        // Initial load
        updateTable.run();
        
        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table, pageControls);

        return container;
    }

    /**
     * Create Staff Stock Logs tab (only logs updated by current staff)
     */
    private Node createStaffStockLogsTab() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search logs...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create stock logs table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");
        table.setPrefHeight(400);

        TableColumn<String[], String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        timestampCol.setPrefWidth(150);

        TableColumn<String[], String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        itemCol.setPrefWidth(150);

        TableColumn<String[], String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        sizeCol.setPrefWidth(60);

        TableColumn<String[], String> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        changeCol.setPrefWidth(80);

        TableColumn<String[], String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[6]));
        actionCol.setPrefWidth(100);

        TableColumn<String[], String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[7]));
        detailsCol.setPrefWidth(200);

        table.getColumns().addAll(timestampCol, itemCol, sizeCol, changeCol, actionCol, detailsCol);

        // Pagination setup
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };
        
        // Load staff stock logs
        List<String[]> staffLogs = loadStaffStockLogs();
        
        // Pagination controls
        HBox pageControls = new HBox(12);
        pageControls.setAlignment(Pos.CENTER);
        pageControls.setPadding(new Insets(12, 0, 0, 0));
        
        Button prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label();
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        
        Button nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        
        pageControls.getChildren().addAll(prevBtn, pageLabel, nextBtn);
        
        // Function to update table with current page
        Runnable updateTable = () -> {
            List<String[]> displayLogs = staffLogs;
            
            // Apply search filter if any
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                displayLogs = displayLogs.stream()
                    .filter(log -> String.join(" ", log).toLowerCase().contains(searchText.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            int totalPages = Math.max(1, (int) Math.ceil((double) displayLogs.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;
            
            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, displayLogs.size());
            
            List<String[]> pageItems = displayLogs.isEmpty() ? java.util.Collections.emptyList() : displayLogs.subList(start, end);
            table.setItems(FXCollections.observableArrayList(pageItems));
            
            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };
        
        // Previous button action
        prevBtn.setOnAction(e -> {
            if (currentPage[0] > 1) {
                currentPage[0]--;
                updateTable.run();
            }
        });
        
        // Next button action
        nextBtn.setOnAction(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) staffLogs.size() / itemsPerPage));
            if (currentPage[0] < totalPages) {
                currentPage[0]++;
                updateTable.run();
            }
        });
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage[0] = 1;
            updateTable.run();
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            staffLogs.clear();
            staffLogs.addAll(loadStaffStockLogs());
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
        });

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
        
        // Set fixed row height to match inventory table (65px) for consistency
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);
        
        // Initial load
        updateTable.run();
        
        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table, pageControls);

        return container;
    }

    /**
     * Show detailed stock log information dialog
     */
    private void showStockLogDetailsDialog(String[] logData) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Stock Log Details");
        dialog.setHeaderText("User Activity Information");

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

        // Student/User Information Section
        VBox performerSection = new VBox(8);
        performerSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label performerHeader = new javafx.scene.control.Label("👤 STUDENT/USER");
        performerHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label performerValue = new javafx.scene.control.Label(logData[1]);
        performerValue.setStyle("-fx-font-size: 14px;");
        
        performerSection.getChildren().addAll(performerHeader, performerValue);

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
        
        javafx.scene.control.Label detailsValue = new javafx.scene.control.Label(logData[7]);
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
     * Load stock logs from file
     * Staff view: Shows only USER activities (pickups, returns)
     */
    private List<String[]> loadStockLogs() {
        List<String[]> logs = new ArrayList<>();
        
        // Only student/user-relevant actions (customer activities only - NOT staff updates)
        List<String> studentOnlyActions = java.util.Arrays.asList(
            "USER_PICKUP", "USER_RETURN", "STAFF_RETURN"
        );
        
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get(StockReturnLogger.getLogFilePath());
            if (java.nio.file.Files.exists(logPath)) {
                // Force read from disk to ensure latest data
                List<String> lines = java.nio.file.Files.readAllLines(logPath, java.nio.charset.StandardCharsets.UTF_8);
                boolean isFirstLine = true;
                
                for (String line : lines) {
                    // Skip empty lines and header
                    if (line == null || line.trim().isEmpty()) {
                        continue;
                    }
                    
                    if (isFirstLine) {
                        isFirstLine = false;
                        if (line.toLowerCase().contains("timestamp")) {
                            continue; // Skip actual header
                        }
                    }
                    
                    String[] parts = line.split("\\|", -1); // Use -1 to include trailing empty strings
                    if (parts.length >= 8) {
                        String action = parts[6].trim(); // Action column
                        
                        // Show only student activities (NOT staff updates)
                        if (studentOnlyActions.contains(action)) {
                            // Trim all parts for cleaner display
                            for (int i = 0; i < parts.length; i++) {
                                parts[i] = parts[i].trim();
                            }
                            logs.add(parts);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue
            System.err.println("Error loading stock logs: " + e.getMessage());
        }
        
        // Sort by timestamp (newest first)
        logs.sort((a, b) -> {
            try {
                java.time.LocalDateTime timeA = java.time.LocalDateTime.parse(a[0], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                java.time.LocalDateTime timeB = java.time.LocalDateTime.parse(b[0], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return timeB.compareTo(timeA);
            } catch (Exception ex) {
                return b[0].compareTo(a[0]); // Fallback to string comparison
            }
        });
        
        return logs;
    }

    /**
     * Load staff stock logs - only logs updated by the current staff member
     */
    private List<String[]> loadStaffStockLogs() {
        List<String[]> logs = new ArrayList<>();
        
        // Get current staff member (assuming there's a way to identify current staff - could use a static/singleton)
        // For now, we'll load all ITEM_UPDATED, ITEM_ADDED, ITEM_DELETED actions (staff-only updates)
        List<String> staffOnlyActions = java.util.Arrays.asList(
            "ITEM_UPDATED", "ITEM_ADDED", "ITEM_DELETED"
        );
        
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get(StockReturnLogger.getLogFilePath());
            if (java.nio.file.Files.exists(logPath)) {
                // Force read from disk to ensure latest data
                List<String> lines = java.nio.file.Files.readAllLines(logPath, java.nio.charset.StandardCharsets.UTF_8);
                boolean isFirstLine = true;
                
                for (String line : lines) {
                    // Skip empty lines and header
                    if (line == null || line.trim().isEmpty()) {
                        continue;
                    }
                    
                    if (isFirstLine) {
                        isFirstLine = false;
                        if (line.toLowerCase().contains("timestamp")) {
                            continue; // Skip actual header
                        }
                    }
                    
                    String[] parts = line.split("\\|", -1); // Use -1 to include trailing empty strings
                    if (parts.length >= 8) {
                        String action = parts[6].trim(); // Action column
                        
                        // Show only staff-updated items
                        if (staffOnlyActions.contains(action)) {
                            // Trim all parts for cleaner display
                            for (int i = 0; i < parts.length; i++) {
                                parts[i] = parts[i].trim();
                            }
                            logs.add(parts);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error but continue
            System.err.println("Error loading staff stock logs: " + e.getMessage());
        }
        
        // Sort by timestamp (newest first)
        logs.sort((a, b) -> {
            try {
                java.time.LocalDateTime timeA = java.time.LocalDateTime.parse(a[0], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                java.time.LocalDateTime timeB = java.time.LocalDateTime.parse(b[0], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                return timeB.compareTo(timeA);
            } catch (Exception ex) {
                return b[0].compareTo(a[0]); // Fallback to string comparison
            }
        });
        
        return logs;
    }

    /**
     * Style action button helper
     */
    private void styleActionButton(Button button) {
        button.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );
    }

    /**
     * Return a style string for course filter buttons honoring dark mode and selection
     */
    private String getCourseButtonStyle(boolean selected) {
        boolean dark = ThemeManager.isDarkMode();
        if (selected) {
            return dark
                ? "-fx-background-color: #2b6fb2; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px;"
                : "-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 6px;";
        } else {
            return dark
                ? "-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: #dbeafe; -fx-cursor: hand; -fx-background-radius: 6px;"
                : "-fx-background-color: #e6eef8; -fx-text-fill: -color-fg-default; -fx-cursor: hand; -fx-background-radius: 6px;";
        }
    }
    
    /**
     * Create statistic card
     */
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        card.setAlignment(Pos.CENTER);

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");

        javafx.scene.control.Label valueLabel = new javafx.scene.control.Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 36px; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    @SuppressWarnings("unused")
    private VBox createMetricCard(String title, String value, String description, String color) {
        VBox card = new VBox(6);
        card.setPadding(new Insets(18));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;"
        );

        javafx.scene.control.Label titleLabel = new javafx.scene.control.Label(title);
        titleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        javafx.scene.control.Label valueLabel = new javafx.scene.control.Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 32px; -fx-font-weight: bold;");

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(description);
        descLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");

        card.getChildren().addAll(titleLabel, valueLabel, descLabel);
        return card;
    }

    private BarChart<String, Number> buildWeeklySalesChart(List<Reservation> completedReservations, List<Reservation> allReservations) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Week");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Net Sales (₱)");
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setTitle("Net Sales Week Trend");
        chart.setAnimated(false);

        Map<String, Double> weeklySales = calculateWeeklySales(completedReservations, allReservations, 5);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        weeklySales.forEach((label, total) -> series.getData().add(new XYChart.Data<>(label, total)));
        chart.getData().add(series);
        chart.setCategoryGap(18);
        chart.setBarGap(6);
        chart.setMinWidth(350);
        chart.setPrefWidth(450);
        return chart;
    }

    private LineChart<String, Number> buildSalesTrendChart(List<Reservation> completedReservations, List<Reservation> allReservations) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Net Sales (₱)");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Total Sales Trend (30 days)");
        chart.setAnimated(false);

        Map<LocalDate, Double> dailySales = calculateDailySales(completedReservations, allReservations, 30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Net Sales");
        dailySales.forEach((date, total) ->
            salesSeries.getData().add(new XYChart.Data<>(date.format(formatter), total))
        );

        chart.getData().add(salesSeries);
        chart.setCreateSymbols(false);
        chart.setPrefWidth(550);
        return chart;
    }

    private TableView<ProductStat> buildProductBreakdownTable(List<Reservation> reservations) {
        TableView<ProductStat> table = new TableView<>();
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<ProductStat, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().itemName));

        TableColumn<ProductStat, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().size));
        sizeCol.setMaxWidth(80);

        TableColumn<ProductStat, Number> ordersCol = new TableColumn<>("Orders");
        ordersCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().orders));

        TableColumn<ProductStat, Number> unitsCol = new TableColumn<>("Units");
        unitsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().quantity));

        TableColumn<ProductStat, String> salesCol = new TableColumn<>("Net Sales");
        salesCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(formatCurrency(data.getValue().sales)));

        table.getColumns().addAll(itemCol, sizeCol, ordersCol, unitsCol, salesCol);

        Map<String, ProductAccumulator> aggregated = new HashMap<>();
        for (Reservation reservation : reservations) {
            if (!"COMPLETED".equals(reservation.getStatus())) {
                continue;
            }
            String key = reservation.getItemName() + "|" + reservation.getSize();
            ProductAccumulator acc = aggregated.computeIfAbsent(key,
                k -> new ProductAccumulator(reservation.getItemName(), reservation.getSize()));
            acc.orders += 1;
            acc.quantity += reservation.getQuantity();
            acc.sales += reservation.getTotalPrice();
        }

        List<ProductStat> stats = aggregated.values().stream()
            .map(ProductAccumulator::toStat)
            .sorted((a, b) -> Double.compare(b.sales, a.sales))
            .limit(10)
            .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(stats));
        return table;
    }

    private TableView<Item> buildLowStockTable(List<Item> items) {
        TableView<Item> table = new TableView<>();
        table.setPrefHeight(260);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: -color-bg-subtle;");
        table.setPlaceholder(new javafx.scene.control.Label("No items between 0-15 units."));

        TableColumn<Item, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        itemCol.setPrefWidth(200);

        TableColumn<Item, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(80);

        TableColumn<Item, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(80);

        TableColumn<Item, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getQuantity() <= 5 ? "CRITICAL" : "LOW";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        statusCol.setCellFactory(col -> new TableCell<Item, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle(null);
                } else {
                    setText(status);
                    String textColor = "CRITICAL".equals(status) ? "#CF222E" : "#C69026";
                    setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(itemCol, sizeCol, qtyCol, statusCol);

        List<Item> lowCriticalItems = items.stream()
            .filter(item -> item.getQuantity() <= 15)
            .sorted(Comparator.comparingInt(Item::getQuantity))
            .collect(Collectors.toList());
        table.setItems(FXCollections.observableArrayList(lowCriticalItems));

        table.setRowFactory(tv -> new TableRow<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle(null);
                } else if (item.getQuantity() <= 5) {
                    setStyle("-fx-background-color: rgba(207,34,46,0.12);");
                } else {
                    setStyle("-fx-background-color: rgba(251,140,0,0.12);");
                }
            }
        });

        return table;
    }

    private double calculateSales(List<Reservation> reservations, Predicate<Reservation> filter) {
        Set<String> processedBundles = new HashSet<>();
        double total = 0;
        for (Reservation reservation : reservations) {
            if (!filter.test(reservation)) {
                continue;
            }
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                if (bundleId != null && processedBundles.add(bundleId)) {
                    total += ControllerUtils.calculateBundleTotal(bundleId, reservations);
                }
            } else {
                total += reservation.getTotalPrice();
            }
        }
        return total;
    }

    private long countOrders(List<Reservation> reservations, Predicate<Reservation> filter) {
        Set<String> processedBundles = new HashSet<>();
        long count = 0;
        for (Reservation reservation : reservations) {
            if (!filter.test(reservation)) {
                continue;
            }
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                if (bundleId != null && processedBundles.add(bundleId)) {
                    count++;
                }
            } else {
                count++;
            }
        }
        return count;
    }

    private Map<String, Double> calculateWeeklySales(List<Reservation> completedReservations,
                                                     List<Reservation> allReservations,
                                                     int weeks) {
        LinkedHashMap<String, Double> weeklyTotals = new LinkedHashMap<>();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        LocalDate now = LocalDate.now();
        for (int i = weeks - 1; i >= 0; i--) {
            LocalDate weekStart = now.minusWeeks(i).with(wf.dayOfWeek(), 1);
            int weekNumber = weekStart.get(wf.weekOfWeekBasedYear());
            int year = weekStart.get(wf.weekBasedYear());
            String label = "W" + weekNumber + "\n" + year;
            weeklyTotals.put(label, 0d);
        }

        List<Reservation> deduped = deduplicateBundles(completedReservations);
        for (Reservation reservation : deduped) {
            LocalDate date = getRelevantDate(reservation);
            int weekNumber = date.get(wf.weekOfWeekBasedYear());
            int year = date.get(wf.weekBasedYear());
            String label = "W" + weekNumber + "\n" + year;
            if (!weeklyTotals.containsKey(label)) {
                continue;
            }
            double amount = getReservationChartAmount(reservation, allReservations);
            weeklyTotals.put(label, weeklyTotals.get(label) + amount);
        }
        return weeklyTotals;
    }

    private Map<LocalDate, Double> calculateDailySales(List<Reservation> completedReservations,
                                                       List<Reservation> allReservations,
                                                       int days) {
        LinkedHashMap<LocalDate, Double> totals = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            totals.put(date, 0d);
        }

        List<Reservation> deduped = deduplicateBundles(completedReservations);
        for (Reservation reservation : deduped) {
            LocalDate date = getRelevantDate(reservation);
            if (!totals.containsKey(date)) {
                continue;
            }
            double amount = getReservationChartAmount(reservation, allReservations);
            totals.put(date, totals.get(date) + amount);
        }

        return totals;
    }

    private List<Reservation> deduplicateBundles(List<Reservation> reservations) {
        Set<String> bundleIds = new HashSet<>();
        List<Reservation> deduped = new ArrayList<>();
        for (Reservation reservation : reservations) {
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                if (bundleId != null && bundleIds.add(bundleId)) {
                    deduped.add(reservation);
                }
            } else {
                deduped.add(reservation);
            }
        }
        return deduped;
    }

    private double getReservationChartAmount(Reservation reservation, List<Reservation> allReservations) {
        if (reservation.isPartOfBundle() && reservation.getBundleId() != null) {
            return ControllerUtils.calculateBundleTotal(reservation.getBundleId(), allReservations);
        }
        return reservation.getTotalPrice();
    }

    private LocalDate getRelevantDate(Reservation reservation) {
        if (reservation.getCompletedDate() != null) {
            return reservation.getCompletedDate().toLocalDate();
        }
        return reservation.getReservationTime() != null
            ? reservation.getReservationTime().toLocalDate()
            : LocalDate.now();
    }

    private String formatCurrency(double amount) {
        return "₱" + String.format("%,.2f", amount);
    }

    @SuppressWarnings("unused")
    private String formatNumber(long value) {
        return String.format("%,d", value);
    }

    private static class ProductStat {
        final String itemName;
        final String size;
        final int orders;
        final int quantity;
        final double sales;

        ProductStat(String itemName, String size, int orders, int quantity, double sales) {
            this.itemName = itemName;
            this.size = size;
            this.orders = orders;
            this.quantity = quantity;
            this.sales = sales;
        }
    }

    private static class ProductAccumulator {
        private final String itemName;
        private final String size;
        private int orders;
        private int quantity;
        private double sales;

        ProductAccumulator(String itemName, String size) {
            this.itemName = itemName;
            this.size = size;
        }

        ProductStat toStat() {
            return new ProductStat(itemName, size, orders, quantity, sales);
        }
    }
    
    /**
     * Create Pickup Approvals View - Shows only orders awaiting pickup approval
     */
    public Node createPickupApprovalsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Statistics card
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        
        // Pickup Approvals Needed
        int pickupApprovalsCount = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getPickupRequestsAwaitingApproval()
        ).size();
        VBox pickupApprovalsCard = createStatCard("📦 Awaiting Approval", String.valueOf(pickupApprovalsCount), "#0969DA");
        
        statsBox.getChildren().add(pickupApprovalsCard);

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Student Name, ID, Order ID, or Item...");
        searchField.setPrefWidth(400);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items) - " + r.getItemName());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(250);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                int totalQty = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .mapToInt(Reservation::getQuantity)
                    .sum();
                return new javafx.beans.property.SimpleObjectProperty<>(totalQty);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(r.getQuantity());
        });
        qtyCol.setPrefWidth(50);

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total");
        priceCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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

        TableColumn<Reservation, Void> bundleCol = new TableColumn<>("Bundle");
        bundleCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button bundleBtn = new Button("📦 View Bundle");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    if (reservation.isPartOfBundle()) {
                        bundleBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5px 10px;");
                        bundleBtn.setOnAction(e -> showBundleItemsDialog(reservation));
                        setGraphic(bundleBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        bundleCol.setPrefWidth(120);

        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button approveBtn = new Button("✓ Approve Pickup");
            private final Button rejectBtn = new Button("✗ Reject");
            private final HBox buttons = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6px 12px;");
                rejectBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 12px; -fx-padding: 6px 12px;");
                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    approveBtn.setOnAction(e -> handleApprovePickup(reservation, table));
                    rejectBtn.setOnAction(e -> handleRejectPickup(reservation, table));
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(180);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, bundleCol, actionsCol);

        // Load pickup requests awaiting approval
        List<Reservation> pickupRequests = reservationManager.getPickupRequestsAwaitingApproval();
        ObservableList<Reservation> allReservations = FXCollections.observableArrayList(
            ControllerUtils.getDeduplicatedReservations(pickupRequests)
        );
        ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList(allReservations);
        table.setItems(filteredReservations);

        // Add row click handler to show order details
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Reservation> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Reservation clickedReservation = row.getItem();
                    showPickupApprovalDetailsDialog(clickedReservation);
                }
            });
            return row;
        });

        // Search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase().trim();
            
            if (searchText.isEmpty()) {
                filteredReservations.setAll(allReservations);
            } else {
                List<Reservation> filtered = allReservations.stream()
                    .filter(r -> {
                        String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                        if (orderId.toLowerCase().contains(searchText)) {
                            return true;
                        }
                        if (r.getStudentName().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        if (r.getStudentId().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        if (r.getItemName().toLowerCase().contains(searchText)) {
                            return true;
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
                filteredReservations.setAll(filtered);
            }
        });

        // Set up refresh callback for action handlers
        this.refreshCallback = () -> {
            List<Reservation> refreshed = reservationManager.getPickupRequestsAwaitingApproval();
            allReservations.setAll(ControllerUtils.getDeduplicatedReservations(refreshed));
            searchField.clear();
            filteredReservations.setAll(allReservations);
            
            // Update stats card
            int updatedCount = (int) ControllerUtils.getDeduplicatedReservations(
                reservationManager.getPickupRequestsAwaitingApproval()
            ).size();
            ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(updatedCount));
        };

        // Refresh button
        refreshBtn.setOnAction(e -> {
            refreshCallback.run();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        // Hide the pickup-approvals summary card from the layout
        container.getChildren().addAll(actionBar, table);

        return container;
    }
    
    /**
     * Create Completed Orders View
     */
    public Node createCompletedView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by student name or order ID...");
        searchField.setPrefWidth(300);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items) - " + r.getItemName());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(250);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                long distinctSizes = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .map(Reservation::getSize)
                    .distinct()
                    .count();
                if (distinctSizes > 1) {
                    return new javafx.beans.property.SimpleStringProperty("Bundle - Click to see");
                }
                return new javafx.beans.property.SimpleStringProperty(r.getSize());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getSize());
        });
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
        statusCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol);

        // Load COMPLETED orders only
        List<Reservation> completedOrders = reservationManager.getAllReservations().stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> completedList = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(completedOrders));
        table.setItems(completedList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                List<Reservation> refreshedCompleted = reservationManager.getAllReservations().stream()
                    .filter(r -> "COMPLETED".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedCompleted)));
            } else {
                List<Reservation> filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "COMPLETED".equals(r.getStatus()))
                    .filter(r -> r.getStudentName().toLowerCase().contains(newVal.toLowerCase()) ||
                               String.valueOf(r.getReservationId()).contains(newVal) ||
                               (r.getBundleId() != null && r.getBundleId().contains(newVal)))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
            }
        });

        // Refresh button
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshedCompleted = reservationManager.getAllReservations().stream()
                .filter(r -> "COMPLETED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedCompleted)));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        // Add row click handler
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
     * Create Returned Orders View
     */
    public Node createReturnedView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by student name or order ID...");
        searchField.setPrefWidth(300);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items) - " + r.getItemName());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(250);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                long distinctSizes = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .map(Reservation::getSize)
                    .distinct()
                    .count();
                if (distinctSizes > 1) {
                    return new javafx.beans.property.SimpleStringProperty("Bundle - Click to see");
                }
                return new javafx.beans.property.SimpleStringProperty(r.getSize());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getSize());
        });
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol);

        // Load REPLACED orders only (items that have been replaced)
        List<Reservation> returnedOrders = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStatus().contains("REPLACED"))
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> returnedList = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(returnedOrders));
        table.setItems(returnedList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                List<Reservation> refreshedReturned = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("REPLACED"))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedReturned)));
            } else {
                List<Reservation> filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("REPLACED"))
                    .filter(r -> r.getStudentName().toLowerCase().contains(newVal.toLowerCase()) ||
                               String.valueOf(r.getReservationId()).contains(newVal) ||
                               (r.getBundleId() != null && r.getBundleId().contains(newVal)))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
            }
        });

        // Refresh button
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshedReturned = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("REPLACED"))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedReturned)));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        // Add row click handler
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
     * Create Cancelled Orders View
     */
    public Node createCancelledView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by student name or order ID...");
        searchField.setPrefWidth(300);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items) - " + r.getItemName());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(250);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                long distinctSizes = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .map(Reservation::getSize)
                    .distinct()
                    .count();
                if (distinctSizes > 1) {
                    return new javafx.beans.property.SimpleStringProperty("Bundle - Click to see");
                }
                return new javafx.beans.property.SimpleStringProperty(r.getSize());
            }
            return new javafx.beans.property.SimpleStringProperty(r.getSize());
        });
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total Price");
        priceCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
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
        statusCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol);

        // Load CANCELLED orders only
        List<Reservation> cancelledOrders = reservationManager.getAllReservations().stream()
            .filter(r -> "CANCELLED".equals(r.getStatus()))
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> cancelledList = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(cancelledOrders));
        table.setItems(cancelledList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                List<Reservation> refreshedCancelled = reservationManager.getAllReservations().stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedCancelled)));
            } else {
                List<Reservation> filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .filter(r -> r.getStudentName().toLowerCase().contains(newVal.toLowerCase()) ||
                               String.valueOf(r.getReservationId()).contains(newVal) ||
                               (r.getBundleId() != null && r.getBundleId().contains(newVal)))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(filtered)));
            }
        });

        // Refresh button
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshedCancelled = reservationManager.getAllReservations().stream()
                .filter(r -> "CANCELLED".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedCancelled)));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        // Add row click handler
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
    
    public void handleLogout() {
        boolean confirm = AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            LoginView loginView = new LoginView();
            SceneManager.setRoot(loginView.getView());
        }
    }
    
    /**
     * Show dialog to select replacement item with search and filter options
     */
    private Item showReplacementItemSelection(Reservation originalItem) {
        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle("Select Replacement Item");
        dialog.setHeaderText("Select replacement item for: " + originalItem.getItemName() + " (Size: " + originalItem.getSize() + ")");
        
        // Make dialog responsive - use 60-70% of screen size
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();
        double dialogWidth = Math.min(900, screenWidth * 0.75);
        double dialogHeight = Math.min(700, screenHeight * 0.80);
        
        dialog.getDialogPane().setPrefSize(dialogWidth, dialogHeight);

        // Get only items with the same name and course as the original item
        List<Item> allItems = inventoryManager.getAllItems();
        List<Item> sameItemVariants = allItems.stream()
            .filter(item -> item.getName().equals(originalItem.getItemName()) && 
                           item.getCourse().equals(originalItem.getCourse()))
            .collect(Collectors.toList());
        ObservableList<Item> itemList = FXCollections.observableArrayList(sameItemVariants);
        ObservableList<Item> filteredList = FXCollections.observableArrayList(sameItemVariants);

        // Create search and filter controls
        HBox searchBox = new HBox(10);
        searchBox.setPadding(new Insets(10));
        searchBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");

        Label sizeLabel = new Label("Size:");
        ComboBox<String> sizeFilter = new ComboBox<>();
        sizeFilter.getItems().addAll("All", "XS", "S", "M", "L", "XL", "XXL");
        sizeFilter.setValue("All");
        sizeFilter.setPrefWidth(100);

        Button clearButton = new Button("Clear");
        clearButton.setPrefWidth(80);

        searchBox.getChildren().addAll(sizeLabel, sizeFilter, clearButton);
        searchBox.setStyle("-fx-alignment: center-left;");

        // Create table for item selection
        TableView<Item> itemTable = new TableView<>(filteredList);
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        itemTable.setPrefHeight(400);

        TableColumn<Item, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Item, Integer> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<Item, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<Item, Integer> qtyCol = new TableColumn<>("Stock");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(80);

        itemTable.getColumns().addAll(nameCol, codeCol, sizeCol, qtyCol);

        // Filtering logic - only filter by size since all items have the same name
        sizeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.clear();
            String sizeValue = newVal;
            
            for (Item item : itemList) {
                boolean matchesSize = "All".equals(sizeValue) || sizeValue.equals(item.getSize());
                
                if (matchesSize && item.getQuantity() > 0) {
                    filteredList.add(item);
                }
            }
        });

        clearButton.setOnAction(e -> {
            sizeFilter.setValue("All");
        });

        // Create container with search box and table
        VBox container = new VBox();
        container.getChildren().addAll(searchBox, itemTable);
        VBox.setVgrow(itemTable, Priority.ALWAYS);

        dialog.getDialogPane().setContent(container);
        
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                Item selected = itemTable.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getQuantity() > 0) {
                    return selected;
                } else {
                    AlertHelper.showError("Error", "Please select an item with available stock.");
                    return null;
                }
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    /**
     * Update the filtered item list based on search and size filter
     */
    private void updateItemFilter(ObservableList<Item> filteredList, 
                                  ObservableList<Item> itemList,
                                  TextField searchField,
                                  ComboBox<String> sizeFilter) {
        String searchText = searchField.getText().toLowerCase().trim();
        String selectedSize = sizeFilter.getValue();

        filteredList.clear();
        
        for (Item item : itemList) {
            // Only show items with stock > 0
            if (item.getQuantity() <= 0) {
                continue;
            }

            // Check search filter (name or code)
            boolean matchesSearch = true;
            if (!searchText.isEmpty()) {
                matchesSearch = item.getName().toLowerCase().contains(searchText) ||
                               String.valueOf(item.getCode()).contains(searchText);
            }

            // Check size filter
            boolean matchesSize = true;
            if (!selectedSize.equals("All")) {
                matchesSize = item.getSize().equals(selectedSize);
            }

            if (matchesSearch && matchesSize) {
                filteredList.add(item);
            }
        }
    }
    
    /**
     * Refresh the reservations table based on current filter
     */
    private void performTableRefresh(ObservableList<Reservation> allReservations, 
                                     ObservableList<Reservation> filteredReservations,
                                     TextField searchField,
                                     TableView<Reservation> table,
                                     HBox statsBox,
                                     String[] currentFilter) {
        // Refresh based on current filter
        List<Reservation> refreshed;
        switch (currentFilter[0]) {
            case "PENDING":
                refreshed = reservationManager.getAllReservations().stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                break;
            case "APPROVED":
                refreshed = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("APPROVED"))
                    .collect(java.util.stream.Collectors.toList());
                break;
            case "PICKUP_APPROVALS":
                refreshed = reservationManager.getPickupRequestsAwaitingApproval();
                break;
            case "RETURN_REQUESTS":
                refreshed = reservationManager.getReturnRequests();
                break;
            default: // ALL
                refreshed = reservationManager.getAllReservations().stream()
                    .filter(r -> "PENDING".equals(r.getStatus()) || "REPLACEMENT REQUESTED".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
        }
        allReservations.setAll(ControllerUtils.getDeduplicatedReservations(refreshed));
        searchField.clear();
        filteredReservations.setAll(allReservations);
        
        // Update stats cards (order: Pending, Pickup Approvals, Completed) - deduplicated for bundles
        int updatedPending = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getPendingReservations()
        ).size();
        ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(0)).getChildren().get(1))
            .setText(String.valueOf(updatedPending));
        
        int updatedPickupApprovals = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getPickupRequestsAwaitingApproval()
        ).size();
        ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(1)).getChildren().get(1))
            .setText(String.valueOf(updatedPickupApprovals));
        
        int updatedCompleted = (int) ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations()
        ).stream()
            .filter(r -> "COMPLETED".equals(r.getStatus()))
            .count();
        ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(2)).getChildren().get(1))
            .setText(String.valueOf(updatedCompleted));
    }
}

