package gui.controllers;

import java.util.ArrayList;
import java.util.List;

import audit.StockAuditManager;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * StaffDashboardController - Handles staff dashboard operations
 */
@SuppressWarnings("unchecked")
public class StaffDashboardController {

    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private StockAuditManager auditManager;
    
    // Refresh callback for when reservations are modified
    private Runnable refreshCallback;

    public StaffDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();
        auditManager = new StockAuditManager();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);

        // Load data
        inventoryManager.getAllItems().forEach(item -> {});
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
        Button returnRequestsBtn = new Button("Return Requests");
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
                    } else if ("RETURN REQUESTED".equals(reservation.getStatus())) {
                        approveBtn.setText("✓ Approve Return");
                        rejectBtn.setText("✗ Reject Return");
                        approveBtn.setOnAction(e -> handleApproveReturn(reservation, table));
                        rejectBtn.setOnAction(e -> handleRejectReturn(reservation, table));
                        setGraphic(buttons);
                    } else if ("PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(reservation.getStatus())) {
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
            .filter(r -> "PENDING".equals(r.getStatus()) || "RETURN REQUESTED".equals(r.getStatus()))
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
                .filter(r -> "PENDING".equals(r.getStatus()) || "RETURN REQUESTED".equals(r.getStatus()))
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
        container.getChildren().addAll(searchBar, statsBox, filterBar, table);

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
                
                // Determine status indicator and color
                String statusIndicator = "";
                String statusColor = "-color-fg-default";
                
                if (item.getStatus().contains("RETURNED")) {
                    statusIndicator = " (RETURNED)";
                    statusColor = "#656D76"; // Gray
                } else if ("COMPLETED".equals(item.getStatus())) {
                    statusIndicator = " (COMPLETED)";
                    statusColor = "#1A7F37"; // Green
                } else if (item.getStatus().contains("RETURN REQUESTED")) {
                    statusIndicator = " (RETURN REQUESTED)";
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
                
                // Only add to total if not returned
                if (!item.getStatus().contains("RETURNED")) {
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
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                String successMsg = reservation.isPartOfBundle() ?
                    "Return approved for all " + successCount + " items!\n\n" :
                    "Return approved!\n\n";
                
                AlertHelper.showSuccess("Success",
                    successMsg + "Items have been restocked and marked as refunded.");
            } else if (successCount > 0) {
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
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
                        .filter(r -> "PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(r.getStatus()))
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
                        reservation.isPartOfBundle() ? "Bundle pickup approved! Student can now claim items." : "Pickup approved! Student can now claim item.");
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
                                .filter(r -> "PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(r.getStatus()))
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

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by item name or code...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn);

        actionBar.getChildren().addAll(refreshBtn, searchField);

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
        
        // Actions column - Adjust Stock button
        TableColumn<Item, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Item, Void>() {
            private final Button adjustBtn = new Button("📝 Adjust Stock");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Item currentItem = getTableView().getItems().get(getIndex());
                    adjustBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                    adjustBtn.setOnAction(e -> handleStockAdjustment(currentItem, table));
                    setGraphic(adjustBtn);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        table.getColumns().addAll(codeCol, nameCol, courseCol, sizeCol, qtyCol, priceCol, actionsCol);

        // Load all items
        List<Item> allItems = inventoryManager.getAllItems();
        ObservableList<Item> itemsList = FXCollections.observableArrayList(allItems);
        table.setItems(itemsList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(allItems));
            } else {
                List<Item> filtered = allItems.stream()
                    .filter(item -> item.getName().toLowerCase().contains(newVal.toLowerCase()) ||
                                  String.valueOf(item.getCode()).contains(newVal))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Item> refreshed = inventoryManager.getAllItems();
            table.setItems(FXCollections.observableArrayList(refreshed));
            searchField.clear();
            
            // Update stats cards
            int updatedTotal = inventoryManager.getAllItems().size();
            ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(0)).getChildren().get(1))
                .setText(String.valueOf(updatedTotal));
            
            int updatedLowStock = (int) inventoryManager.getAllItems().stream()
                .filter(item -> item.getQuantity() < 10)
                .count();
            ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(1)).getChildren().get(1))
                .setText(String.valueOf(updatedLowStock));
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(statsBox, actionBar, table);

        return container;
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
                
                // Ask for reason
                TextInputDialog reasonDialog = new TextInputDialog();
                reasonDialog.setTitle("Stock Adjustment Reason");
                reasonDialog.setHeaderText("Provide a reason for this stock change");
                reasonDialog.setContentText("Reason:");
                
                reasonDialog.showAndWait().ifPresent(reason -> {
                    if (reason.trim().isEmpty()) {
                        AlertHelper.showError("Validation Error", "Reason is required!");
                        return;
                    }
                    
                    // Calculate the difference
                    int oldQuantity = item.getQuantity();
                    int stockChange = newQuantity - oldQuantity;
                    String action = stockChange > 0 ? "ADD" : (stockChange < 0 ? "REMOVE" : "ADJUST");
                    
                    // Log the stock change to audit system
                    auditManager.logStockChange(
                        "staff", // Performed by staff
                        item.getName(),
                        item.getCode(),
                        item.getSize(),
                        oldQuantity,
                        newQuantity,
                        reason.trim(),
                        action
                    );
                    
                    // Update the stock directly using InventoryManager
                    boolean success = inventoryManager.updateItemQuantityBySize(
                        item.getCode(),
                        item.getSize(),
                        newQuantity
                    );
                    
                    if (success) {
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
                });
                
            } catch (NumberFormatException e) {
                AlertHelper.showError("Invalid Input", "Please enter a valid number!");
            }
        });
    }

    public Node createStockLogsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

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

        // Load stock logs
        List<String[]> allLogs = loadStockLogs();
        ObservableList<String[]> logsList = FXCollections.observableArrayList(allLogs);
        table.setItems(logsList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(allLogs));
            } else {
                List<String[]> filtered = allLogs.stream()
                    .filter(log -> String.join(" ", log).toLowerCase().contains(newVal.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<String[]> refreshed = loadStockLogs();
            table.setItems(FXCollections.observableArrayList(refreshed));
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
        
        // Staff-relevant actions (user/customer activities only)
        List<String> staffRelevantActions = java.util.Arrays.asList(
            "USER_PICKUP", "USER_RETURN"
        );
        
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get("src/database/data/stock_logs.txt");
            if (java.nio.file.Files.exists(logPath)) {
                List<String> lines = java.nio.file.Files.readAllLines(logPath);
                boolean isFirstLine = true;
                
                for (String line : lines) {
                    // Skip header
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    
                    String[] parts = line.split("\\|");
                    if (parts.length >= 8) {
                        String action = parts[6]; // Action column
                        
                        // Only show staff-relevant actions (user activities)
                        if (staffRelevantActions.contains(action)) {
                            logs.add(parts);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle error
        }
        
        // Sort by timestamp (newest first)
        logs.sort((a, b) -> b[0].compareTo(a[0]));
        
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
        container.getChildren().addAll(statsBox, actionBar, table);

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

        // Load RETURNED orders only (RETURNED - REFUNDED)
        List<Reservation> returnedOrders = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStatus().contains("RETURNED"))
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> returnedList = FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(returnedOrders));
        table.setItems(returnedList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                List<Reservation> refreshedReturned = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("RETURNED"))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(ControllerUtils.getDeduplicatedReservations(refreshedReturned)));
            } else {
                List<Reservation> filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("RETURNED"))
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
                .filter(r -> r.getStatus().contains("RETURNED"))
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
            Scene scene = new Scene(loginView.getView(), 1920, 1025);
            SceneManager.setScene(scene);
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
                    .filter(r -> "PENDING".equals(r.getStatus()) || "RETURN REQUESTED".equals(r.getStatus()))
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

