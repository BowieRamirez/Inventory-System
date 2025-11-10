package gui.controllers;

import java.util.List;

import gui.utils.AlertHelper;
import gui.utils.ControllerUtils;
import gui.utils.SceneManager;
import gui.views.LoginView;
import inventory.InventoryManager;
import inventory.Receipt;
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
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import utils.SystemLogger;

/**
 * CashierDashboardController - Handles cashier dashboard operations
 */
@SuppressWarnings("unchecked")
public class CashierDashboardController {

    private final InventoryManager inventoryManager;
    private final ReservationManager reservationManager;
    private final ReceiptManager receiptManager;

    public CashierDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);

        // Load data
        inventoryManager.getAllItems().forEach(item -> {});
    }

    public Node createPaymentsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("🔍 Search:");
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

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );

        actionBar.getChildren().add(refreshBtn);

        // Create approved reservations table (waiting for payment)
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

        TableColumn<Reservation, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> {
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
        totalCol.setCellFactory(col -> new TableCell<Reservation, Double>() {
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
        totalCol.setPrefWidth(100);

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
            private final Button processBtn = new Button("Process Payment");

            {
                processBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reservation reservation = getTableView().getItems().get(getIndex());
                    processBtn.setOnAction(e -> handleProcessPayment(reservation, table));
                    setGraphic(processBtn);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, totalCol, bundleCol, actionsCol);

        // Load ONLY approved reservations waiting for payment (deduplicated for bundles)
        // Filter out: CANCELLED, RETURNED, PAID, COMPLETED - only show unpaid approved orders
        List<Reservation> pendingPaymentReservations = ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations().stream()
                .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()) && !r.isPaid())
                .collect(java.util.stream.Collectors.toList())
        );
        ObservableList<Reservation> allReservations = FXCollections.observableArrayList(pendingPaymentReservations);
        ObservableList<Reservation> filteredReservations = FXCollections.observableArrayList(allReservations);
        table.setItems(filteredReservations);

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

        // Refresh button action - reload only approved unpaid reservations
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshed = ControllerUtils.getDeduplicatedReservations(
                reservationManager.getAllReservations().stream()
                    .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()) && !r.isPaid())
                    .collect(java.util.stream.Collectors.toList())
            );
            allReservations.setAll(refreshed);
            searchField.clear(); // Clear search when refreshing
            filteredReservations.setAll(allReservations);
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(searchBar, actionBar, table);

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
        
        // Payment deadline display
        VBox deadlineBox = new VBox(3);
        if (reservation.getPaymentDeadline() != null && "APPROVED - WAITING FOR PAYMENT".equals(reservation.getStatus())) {
            javafx.scene.control.Label deadlineLabel = new javafx.scene.control.Label("Payment Deadline: " + reservation.getFormattedPaymentDeadline());
            long hoursRemaining = reservation.getHoursUntilPaymentDeadline();
            
            if (hoursRemaining <= 0) {
                deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff0000; -fx-font-weight: bold;");
            } else if (hoursRemaining <= 6) {
                deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ff6b6b; -fx-font-weight: bold;");
            } else if (hoursRemaining <= 24) {
                deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ffa500; -fx-font-weight: bold;");
            } else {
                deadlineLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4CAF50;");
            }
            
            javafx.scene.control.Label timeRemainingLabel = new javafx.scene.control.Label(
                String.format("Time Remaining: %d hours", hoursRemaining)
            );
            
            if (hoursRemaining <= 0) {
                timeRemainingLabel.setText("⚠️ OVERDUE - Payment deadline has passed!");
                timeRemainingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff0000; -fx-font-weight: bold;");
            } else if (hoursRemaining <= 6) {
                timeRemainingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ff6b6b;");
            } else if (hoursRemaining <= 24) {
                timeRemainingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #ffa500;");
            } else {
                timeRemainingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
            }
            
            deadlineBox.getChildren().addAll(deadlineLabel, timeRemainingLabel);
        }
        
        javafx.scene.control.Label qtyLabel = new javafx.scene.control.Label("Total Quantity: " + totalQuantity);
        qtyLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label totalLabel = new javafx.scene.control.Label("Total Amount: ₱" + String.format("%.2f", totalPrice));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        javafx.scene.control.Label orderTypeLabel = new javafx.scene.control.Label("Order Type: " + (reservation.isPartOfBundle() ? "Bundle Order" : "Single Item"));
        orderTypeLabel.setStyle("-fx-font-size: 12px;");
        
        // Add deadline box if it has content
        if (deadlineBox.getChildren().isEmpty()) {
            summarySection.getChildren().addAll(summaryHeader, statusLabel, orderTypeLabel, qtyLabel, totalLabel);
        } else {
            summarySection.getChildren().addAll(summaryHeader, statusLabel, deadlineBox, orderTypeLabel, qtyLabel, totalLabel);
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
     * Handle process payment
     */
    private void handleProcessPayment(Reservation reservation, TableView<Reservation> table) {
        // Create payment dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Process Payment");
        dialog.setHeaderText("Process payment for: " + reservation.getStudentName());

        ButtonType processButtonType = new ButtonType("Process", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButtonType, ButtonType.CANCEL);

        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));

        // Calculate total price (for bundles, sum all items)
        double totalPrice = reservation.getTotalPrice();
        int totalQuantity = reservation.getQuantity();
        
        if (reservation.isPartOfBundle()) {
            // Get all items in the bundle
            String bundleId = reservation.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .collect(java.util.stream.Collectors.toList());
            
            // Show bundle header
            Label bundleLabel = new Label("BUNDLE ORDER - " + bundleItems.size() + " item type(s)");
            bundleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            mainContainer.getChildren().add(bundleLabel);
            
            // Show all items in the bundle
            VBox itemsList = new VBox(5);
            itemsList.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 10; -fx-background-radius: 5;");
            
            totalPrice = 0;
            totalQuantity = 0;
            
            for (Reservation item : bundleItems) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                Label itemName = new Label("• " + item.getItemName() + " (" + item.getSize() + ")");
                itemName.setMinWidth(250);
                
                Label itemQty = new Label("Qty: " + item.getQuantity());
                itemQty.setMinWidth(70);
                
                Label itemPrice = new Label("₱" + String.format("%.2f", item.getTotalPrice()));
                itemPrice.setStyle("-fx-font-weight: bold;");
                
                itemRow.getChildren().addAll(itemName, itemQty, itemPrice);
                itemsList.getChildren().add(itemRow);
                
                totalPrice += item.getTotalPrice();
                totalQuantity += item.getQuantity();
            }
            
            mainContainer.getChildren().add(itemsList);
        } else {
            // Single item
            Label itemLabel = new Label("Item: " + reservation.getItemName() + " (" + reservation.getSize() + ")");
            itemLabel.setStyle("-fx-font-size: 13px;");
            mainContainer.getChildren().add(itemLabel);
        }

        // Show totals
        VBox totalsBox = new VBox(5);
        totalsBox.setStyle("-fx-background-color: -color-bg-default; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label qtyLabel = new Label("Total Quantity: " + totalQuantity);
        qtyLabel.setStyle("-fx-font-size: 13px;");
        
        Label totalLabel = new Label("Total Amount: ₱" + String.format("%.2f", totalPrice));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        totalsBox.getChildren().addAll(qtyLabel, totalLabel);
        mainContainer.getChildren().add(totalsBox);

        // Payment method selector
        HBox paymentBox = new HBox(10);
        paymentBox.setAlignment(Pos.CENTER_LEFT);
        
        Label paymentLabel = new Label("Payment Method:");
        ComboBox<String> paymentMethodBox = new ComboBox<>();
        paymentMethodBox.getItems().addAll("CASH", "GCASH", "CARD", "BANK");
        paymentMethodBox.setValue("CASH");
        
        paymentBox.getChildren().addAll(paymentLabel, paymentMethodBox);
        mainContainer.getChildren().add(paymentBox);

        dialog.getDialogPane().setContent(mainContainer);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == processButtonType) {
                return paymentMethodBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(paymentMethod -> {
            // If it's a bundle, process all items in the bundle
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                    .filter(r -> bundleId.equals(r.getBundleId()))
                    .collect(java.util.stream.Collectors.toList());
                
                // Mark all items in the bundle as paid
                boolean allSuccess = true;
                for (Reservation bundleItem : bundleItems) {
                    boolean success = reservationManager.markAsPaid(bundleItem.getReservationId(), paymentMethod);
                    if (!success) {
                        allSuccess = false;
                        break;
                    }
                }
                
                if (allSuccess) {
                    // Calculate bundle totals for receipt
                    double bundleTotalPrice = bundleItems.stream()
                        .mapToDouble(Reservation::getTotalPrice)
                        .sum();
                    
                    // Create individual receipts for EACH item in the bundle
                    // All receipts will share the same bundleId for grouping
                    for (Reservation bundleItem : bundleItems) {
                        receiptManager.createReceipt(
                            "COMPLETED",
                            bundleItem.getQuantity(),
                            bundleItem.getTotalPrice(),
                            bundleItem.getItemCode(),
                            bundleItem.getItemName(),
                            bundleItem.getSize(),
                            bundleItem.getStudentName(),
                            bundleId  // Link all items with the same bundle ID
                        );
                    }
                    
                    // Get the first receipt ID for display (any receipt from the bundle)
                    Receipt firstReceipt = receiptManager.getAllReceipts().stream()
                        .filter(r -> bundleId.equals(r.getBundleId()))
                        .findFirst()
                        .orElse(null);
                    
                    int displayReceiptId = firstReceipt != null ? firstReceipt.getReceiptId() : 0;
                    
                    // Log each item in the bundle
                    for (Reservation bundleItem : bundleItems) {
                        SystemLogger.logPurchase(
                            bundleItem.getStudentName(),
                            bundleItem.getItemName() + " (" + bundleItem.getSize() + ")",
                            bundleItem.getQuantity(),
                            bundleItem.getTotalPrice()
                        );
                        
                        SystemLogger.logStockUpdate(
                            bundleItem.getItemName(),
                            bundleItem.getQuantity(),
                            inventoryManager.findItemByCode(bundleItem.getItemCode()).getQuantity()
                        );
                    }
                    
                    // Refresh table - show only unpaid approved reservations
                    List<Reservation> refreshed = ControllerUtils.getDeduplicatedReservations(
                        reservationManager.getAllReservations().stream()
                            .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()) && !r.isPaid())
                            .collect(java.util.stream.Collectors.toList())
                    );
                    table.setItems(FXCollections.observableArrayList(refreshed));
                    
                    AlertHelper.showSuccess("Success",
                        "Bundle payment processed successfully!\n\n" +
                        "Receipt ID: " + displayReceiptId + "\n" +
                        "Payment Method: " + paymentMethod + "\n" +
                        "Bundle ID: " + bundleId + "\n" +
                        "Total Items: " + bundleItems.size() + "\n" +
                        "Total Amount: ₱" + String.format("%.2f", bundleTotalPrice));
                } else {
                    AlertHelper.showError("Error", "Failed to process bundle payment");
                    SystemLogger.logError("Bundle payment processing failed for bundle: " + bundleId, 
                        new Exception("Payment failed"));
                }
            } else {
                // Single item payment (not a bundle)
                boolean success = reservationManager.markAsPaid(reservation.getReservationId(), paymentMethod);
                if (success) {
                    Receipt receipt = receiptManager.createReceipt(
                        "PAID",
                        reservation.getQuantity(),
                        reservation.getTotalPrice(),
                        reservation.getItemCode(),
                        reservation.getItemName(),
                        reservation.getSize(),
                        reservation.getStudentName()
                    );

                    // Log purchase transaction
                    SystemLogger.logPurchase(
                        reservation.getStudentName(),
                        reservation.getItemName() + " (" + reservation.getSize() + ")",
                        reservation.getQuantity(),
                        reservation.getTotalPrice()
                    );
                    
                    // Log stock update
                    SystemLogger.logStockUpdate(
                        reservation.getItemName(),
                        reservation.getQuantity(),
                        inventoryManager.findItemByCode(reservation.getItemCode()).getQuantity()
                    );

                    // Refresh table - show only unpaid approved reservations
                    List<Reservation> refreshed = ControllerUtils.getDeduplicatedReservations(
                        reservationManager.getAllReservations().stream()
                            .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()) && !r.isPaid())
                            .collect(java.util.stream.Collectors.toList())
                    );
                    table.setItems(FXCollections.observableArrayList(refreshed));

                    AlertHelper.showSuccess("Success",
                        "Payment processed successfully!\n\n" +
                        "Receipt ID: " + receipt.getReceiptId() + "\n" +
                        "Payment Method: " + paymentMethod);
                } else {
                    AlertHelper.showError("Error", "Failed to process payment");
                    SystemLogger.logError("Payment processing failed for reservation: " + reservation.getReservationId(), 
                        new Exception("Payment failed"));
                }
            }
        });
    }
    
    public Node createReservationsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );

        actionBar.getChildren().add(refreshBtn);

        // Create all reservations table
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

        TableColumn<Reservation, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> {
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
        totalCol.setCellFactory(col -> new TableCell<Reservation, Double>() {
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
        totalCol.setPrefWidth(100);

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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, totalCol, statusCol, bundleCol);

        // Load all reservations (deduplicated for bundles)
        List<Reservation> allReservations = ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations());
        ObservableList<Reservation> reservationsList = FXCollections.observableArrayList(allReservations);
        table.setItems(reservationsList);

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshed = ControllerUtils.getDeduplicatedReservations(reservationManager.getAllReservations());
            table.setItems(FXCollections.observableArrayList(refreshed));
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
    }

    public Node createReceiptsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by buyer name...");
        searchField.setPrefWidth(250);

        refreshBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create receipts table
        TableView<Receipt> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<Receipt, Integer> idCol = new TableColumn<>("Receipt ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getReceiptId()));
        idCol.setPrefWidth(100);

        TableColumn<Receipt, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDateOrdered()));
        dateCol.setPrefWidth(150);

        TableColumn<Receipt, String> buyerCol = new TableColumn<>("Buyer");
        buyerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBuyerName()));
        buyerCol.setPrefWidth(150);

        TableColumn<Receipt, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> {
            Receipt r = data.getValue();
            if (r.isPartOfBundle()) {
                // Count items in this bundle
                String bundleId = r.getBundleId();
                long itemCount = receiptManager.getAllReceipts().stream()
                    .filter(receipt -> bundleId.equals(receipt.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items)"
                );
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setPrefWidth(200);

        TableColumn<Receipt, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> {
            Receipt r = data.getValue();
            if (r.isPartOfBundle()) {
                return new javafx.beans.property.SimpleStringProperty("Bundle");
            }
            return new javafx.beans.property.SimpleStringProperty(r.getSize());
        });
        sizeCol.setPrefWidth(60);

        TableColumn<Receipt, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> {
            Receipt r = data.getValue();
            if (r.isPartOfBundle()) {
                // Sum quantities for all items in bundle
                String bundleId = r.getBundleId();
                int totalQty = receiptManager.getAllReceipts().stream()
                    .filter(receipt -> bundleId.equals(receipt.getBundleId()))
                    .mapToInt(Receipt::getQuantity)
                    .sum();
                return new javafx.beans.property.SimpleObjectProperty<>(totalQty);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(r.getQuantity());
        });
        qtyCol.setPrefWidth(60);

        TableColumn<Receipt, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> {
            Receipt r = data.getValue();
            if (r.isPartOfBundle()) {
                // Sum amounts for all items in bundle
                String bundleId = r.getBundleId();
                double totalAmount = receiptManager.getAllReceipts().stream()
                    .filter(receipt -> bundleId.equals(receipt.getBundleId()))
                    .mapToDouble(Receipt::getAmount)
                    .sum();
                return new javafx.beans.property.SimpleObjectProperty<>(totalAmount);
            }
            return new javafx.beans.property.SimpleObjectProperty<>(r.getAmount());
        });
        amountCol.setCellFactory(col -> new TableCell<Receipt, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f", amount));
                }
            }
        });
        amountCol.setPrefWidth(100);

        TableColumn<Receipt, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPaymentStatus()));
        statusCol.setPrefWidth(100);

        TableColumn<Receipt, String> bundleCol = new TableColumn<>("Bundle");
        bundleCol.setCellValueFactory(data -> {
            Receipt r = data.getValue();
            String bundleInfo = r.isPartOfBundle() ? "✓ BUNDLE" : "";
            return new javafx.beans.property.SimpleStringProperty(bundleInfo);
        });
        bundleCol.setCellFactory(col -> new TableCell<Receipt, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #0969DA; -fx-font-weight: bold;");
                }
            }
        });
        bundleCol.setPrefWidth(80);

        table.getColumns().addAll(idCol, dateCol, buyerCol, itemCol, sizeCol, qtyCol, amountCol, statusCol, bundleCol);

        // Load all receipts and deduplicate bundles
        List<Receipt> allReceipts = deduplicateBundleReceipts(receiptManager.getAllReceipts());
        ObservableList<Receipt> receiptsList = FXCollections.observableArrayList(allReceipts);
        table.setItems(receiptsList);

        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(FXCollections.observableArrayList(allReceipts));
            } else {
                List<Receipt> filtered = allReceipts.stream()
                    .filter(r -> r.getBuyerName().toLowerCase().contains(newVal.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(filtered));
            }
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Receipt> refreshed = deduplicateBundleReceipts(receiptManager.getAllReceipts());
            table.setItems(FXCollections.observableArrayList(refreshed));
            searchField.clear();
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        // Add row click handler to show receipt details
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Receipt> row = new javafx.scene.control.TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    Receipt clickedReceipt = row.getItem();
                    showReceiptDetailsDialog(clickedReceipt);
                }
            });
            return row;
        });

        return container;
    }

    /**
     * Show detailed receipt information dialog
     */
    private void showReceiptDetailsDialog(Receipt receipt) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Receipt Details");
        dialog.setHeaderText("Receipt ID: " + receipt.getReceiptId());

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
        
        javafx.scene.control.Label buyerName = new javafx.scene.control.Label("Name: " + receipt.getBuyerName());
        javafx.scene.control.Label purchaseDate = new javafx.scene.control.Label("Purchase Date: " + receipt.getDateOrdered());
        
        customerSection.getChildren().addAll(customerHeader, buyerName, purchaseDate);

        // Order Items Section
        VBox itemsSection = new VBox(8);
        itemsSection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label itemsHeader = new javafx.scene.control.Label("PURCHASE ITEMS");
        itemsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        itemsSection.getChildren().add(itemsHeader);

        double totalAmount = 0;
        int totalQuantity = 0;

        if (receipt.isPartOfBundle()) {
            // Get all items in the bundle
            String bundleId = receipt.getBundleId();
            List<Receipt> bundleItems = receiptManager.getAllReceipts().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .collect(java.util.stream.Collectors.toList());
            
            // Check if this is an old-style bundle (only one receipt entry)
            if (bundleItems.size() == 1 && receipt.getItemName().contains("BUNDLE ORDER")) {
                // Old-style bundle - show what we have
                VBox itemBox = new VBox(5);
                itemBox.setStyle("-fx-padding: 10; -fx-border-color: #FFA500; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-color: #FFF3CD; -fx-background-radius: 5;");
                
                javafx.scene.control.Label warningLabel = new javafx.scene.control.Label("⚠ Legacy Bundle Receipt");
                warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404;");
                
                javafx.scene.control.Label itemName = new javafx.scene.control.Label(receipt.getItemName());
                itemName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                
                HBox detailsRow = new HBox(15);
                detailsRow.setAlignment(Pos.CENTER_LEFT);
                
                javafx.scene.control.Label itemCode = new javafx.scene.control.Label("Code: " + receipt.getItemCode());
                javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + receipt.getSize());
                javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + receipt.getQuantity());
                javafx.scene.control.Label itemTotalPrice = new javafx.scene.control.Label("Total: ₱" + String.format("%.2f", receipt.getAmount()));
                itemTotalPrice.setStyle("-fx-font-weight: bold;");
                
                detailsRow.getChildren().addAll(itemCode, itemSize, itemQty, itemTotalPrice);
                
                javafx.scene.control.Label infoLabel = new javafx.scene.control.Label("Note: Individual item details not available for this older receipt format.");
                infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #856404; -fx-font-style: italic;");
                
                itemBox.getChildren().addAll(warningLabel, itemName, detailsRow, infoLabel);
                itemsSection.getChildren().add(itemBox);
                
                totalAmount = receipt.getAmount();
                totalQuantity = receipt.getQuantity();
            } else {
                // New-style bundle - show all individual items
                for (Receipt item : bundleItems) {
                    // Find corresponding reservation to check status
                    String itemStatus = getItemStatusFromReservation(item);
                    String statusTag = "";
                    String statusColor = "-color-fg-default";
                    String borderColor = "-color-border-default";
                    
                    if (itemStatus.contains("RETURNED")) {
                        statusTag = " (REFUNDED)";
                        statusColor = "#656D76"; // Gray
                        borderColor = "#656D76";
                    } else if ("COMPLETED".equals(itemStatus)) {
                        statusTag = " (COMPLETED)";
                        statusColor = "#1A7F37"; // Green
                        borderColor = "#1A7F37";
                    } else if (itemStatus.contains("RETURN REQUESTED")) {
                        statusTag = " (RETURN REQUESTED)";
                        statusColor = "#BF8700"; // Orange
                        borderColor = "#BF8700";
                    }
                    
                    VBox itemBox = new VBox(5);
                    itemBox.setStyle("-fx-padding: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-color: -color-bg-default; -fx-background-radius: 3;");
                    
                    javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + item.getItemName() + statusTag);
                    itemName.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
                    
                    HBox detailsRow = new HBox(15);
                    detailsRow.setAlignment(Pos.CENTER_LEFT);
                    
                    javafx.scene.control.Label itemCode = new javafx.scene.control.Label("Code: " + item.getItemCode());
                    itemCode.setStyle("-fx-text-fill: " + statusColor + ";");
                    javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + item.getSize());
                    itemSize.setStyle("-fx-text-fill: " + statusColor + ";");
                    javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + item.getQuantity());
                    itemQty.setStyle("-fx-text-fill: " + statusColor + ";");
                    
                    double unitPrice = item.getAmount() / item.getQuantity();
                    javafx.scene.control.Label itemUnitPrice = new javafx.scene.control.Label("Unit Price: ₱" + String.format("%.2f", unitPrice));
                    itemUnitPrice.setStyle("-fx-text-fill: " + statusColor + ";");
                    javafx.scene.control.Label itemTotalPrice = new javafx.scene.control.Label("Total: ₱" + String.format("%.2f", item.getAmount()));
                    itemTotalPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
                    
                    detailsRow.getChildren().addAll(itemCode, itemSize, itemQty, itemUnitPrice, itemTotalPrice);
                    itemBox.getChildren().addAll(itemName, detailsRow);
                    itemsSection.getChildren().add(itemBox);
                    
                    // Only add to total if not returned
                    if (!itemStatus.contains("RETURNED")) {
                        totalAmount += item.getAmount();
                        totalQuantity += item.getQuantity();
                    }
                }
            }
        } else {
            // Single item
            // Find corresponding reservation to check status
            String itemStatus = getItemStatusFromReservation(receipt);
            String statusTag = "";
            String statusColor = "-color-fg-default";
            String borderColor = "-color-border-default";
            
            if (itemStatus.contains("RETURNED")) {
                statusTag = " (REFUNDED)";
                statusColor = "#656D76"; // Gray
                borderColor = "#656D76";
            } else if ("COMPLETED".equals(itemStatus)) {
                statusTag = " (COMPLETED)";
                statusColor = "#1A7F37"; // Green
                borderColor = "#1A7F37";
            } else if (itemStatus.contains("RETURN REQUESTED")) {
                statusTag = " (RETURN REQUESTED)";
                statusColor = "#BF8700"; // Orange
                borderColor = "#BF8700";
            }
            
            VBox itemBox = new VBox(5);
            itemBox.setStyle("-fx-padding: 5; -fx-border-color: " + borderColor + "; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-color: -color-bg-default; -fx-background-radius: 3;");
            
            javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + receipt.getItemName() + statusTag);
            itemName.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
            
            HBox detailsRow = new HBox(15);
            detailsRow.setAlignment(Pos.CENTER_LEFT);
            
            javafx.scene.control.Label itemCode = new javafx.scene.control.Label("Code: " + receipt.getItemCode());
            itemCode.setStyle("-fx-text-fill: " + statusColor + ";");
            javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + receipt.getSize());
            itemSize.setStyle("-fx-text-fill: " + statusColor + ";");
            javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + receipt.getQuantity());
            itemQty.setStyle("-fx-text-fill: " + statusColor + ";");
            
            double unitPrice = receipt.getAmount() / receipt.getQuantity();
            javafx.scene.control.Label itemUnitPrice = new javafx.scene.control.Label("Unit Price: ₱" + String.format("%.2f", unitPrice));
            itemUnitPrice.setStyle("-fx-text-fill: " + statusColor + ";");
            javafx.scene.control.Label itemTotalPrice = new javafx.scene.control.Label("Total: ₱" + String.format("%.2f", receipt.getAmount()));
            itemTotalPrice.setStyle("-fx-font-weight: bold; -fx-text-fill: " + statusColor + ";");
            
            detailsRow.getChildren().addAll(itemCode, itemSize, itemQty, itemUnitPrice, itemTotalPrice);
            itemBox.getChildren().addAll(itemName, detailsRow);
            itemsSection.getChildren().add(itemBox);
            
            // Only add to total if not returned
            if (!itemStatus.contains("RETURNED")) {
                totalAmount = receipt.getAmount();
                totalQuantity = receipt.getQuantity();
            }
        }

        // Payment Summary Section
        VBox summarySection = new VBox(8);
        summarySection.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 15; -fx-background-radius: 5;");
        
        javafx.scene.control.Label summaryHeader = new javafx.scene.control.Label("PAYMENT SUMMARY");
        summaryHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("Payment Status: " + receipt.getPaymentStatus());
        statusLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label qtyLabel = new javafx.scene.control.Label("Total Quantity: " + totalQuantity);
        qtyLabel.setStyle("-fx-font-size: 12px;");
        
        javafx.scene.control.Label totalLabel = new javafx.scene.control.Label("Total Amount: ₱" + String.format("%.2f", totalAmount));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        javafx.scene.control.Label orderTypeLabel = new javafx.scene.control.Label("Purchase Type: " + (receipt.isPartOfBundle() ? "Bundle Order" : "Single Item"));
        orderTypeLabel.setStyle("-fx-font-size: 12px;");
        
        summarySection.getChildren().addAll(summaryHeader, statusLabel, orderTypeLabel, qtyLabel, totalLabel);

        content.getChildren().addAll(customerSection, itemsSection, summarySection);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(600);
        dialog.showAndWait();
    }
    
    /**
     * Deduplicate bundle receipts - show only one entry per bundle
     */
    private List<Receipt> deduplicateBundleReceipts(List<Receipt> receipts) {
        List<Receipt> deduplicated = new java.util.ArrayList<>();
        java.util.Set<String> processedBundles = new java.util.HashSet<>();
        
        for (Receipt receipt : receipts) {
            if (receipt.isPartOfBundle()) {
                String bundleId = receipt.getBundleId();
                // Only add the first receipt from each bundle
                if (!processedBundles.contains(bundleId)) {
                    deduplicated.add(receipt);
                    processedBundles.add(bundleId);
                }
            } else {
                // Not a bundle, always add
                deduplicated.add(receipt);
            }
        }
        
        return deduplicated;
    }
    
    /**
     * Get item status from corresponding reservation
     */
    private String getItemStatusFromReservation(Receipt receipt) {
        // Try to find the corresponding reservation
        List<Reservation> allReservations = reservationManager.getAllReservations();
        
        // Match by buyer name, item code, size, and quantity
        for (Reservation reservation : allReservations) {
            if (reservation.getStudentName().equals(receipt.getBuyerName()) &&
                reservation.getItemCode() == receipt.getItemCode() &&
                reservation.getSize().equals(receipt.getSize()) &&
                reservation.getQuantity() == receipt.getQuantity()) {
                return reservation.getStatus();
            }
        }
        
        // If bundle, try to match by bundle ID
        if (receipt.isPartOfBundle()) {
            String bundleId = receipt.getBundleId();
            for (Reservation reservation : allReservations) {
                if (bundleId.equals(reservation.getBundleId()) &&
                    reservation.getItemCode() == receipt.getItemCode() &&
                    reservation.getSize().equals(receipt.getSize())) {
                    return reservation.getStatus();
                }
            }
        }
        
        // Default to COMPLETED if no reservation found (item already picked up)
        return "COMPLETED";
    }
    
    public void handleLogout() {
        boolean confirm = AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 1920, 1080);
            SceneManager.setScene(scene);
        }
    }
}

