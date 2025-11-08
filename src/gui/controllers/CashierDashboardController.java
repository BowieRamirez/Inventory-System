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

        TableColumn<Reservation, String> bundleCol = new TableColumn<>("Bundle");
        bundleCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            String bundleInfo = r.isPartOfBundle() ? "✓ BUNDLE" : "";
            return new javafx.beans.property.SimpleStringProperty(bundleInfo);
        });
        bundleCol.setCellFactory(col -> new TableCell<Reservation, String>() {
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
        table.setItems(FXCollections.observableArrayList(pendingPaymentReservations));

        // Refresh button action - reload only approved unpaid reservations
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshed = ControllerUtils.getDeduplicatedReservations(
                reservationManager.getAllReservations().stream()
                    .filter(r -> "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus()) && !r.isPaid())
                    .collect(java.util.stream.Collectors.toList())
            );
            table.setItems(FXCollections.observableArrayList(refreshed));
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

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
                    int bundleTotalQuantity = bundleItems.stream()
                        .mapToInt(Reservation::getQuantity)
                        .sum();
                    
                    // Create a single receipt for the entire bundle
                    Receipt receipt = receiptManager.createReceipt(
                        "PAID",
                        bundleTotalQuantity,
                        bundleTotalPrice,
                        reservation.getItemCode(),
                        "BUNDLE ORDER (" + bundleItems.size() + " items)",
                        "Bundle",
                        reservation.getStudentName(),
                        bundleId
                    );
                    
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
                        "Receipt ID: " + receipt.getReceiptId() + "\n" +
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

        TableColumn<Reservation, String> bundleCol = new TableColumn<>("Bundle");
        bundleCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            String bundleInfo = r.isPartOfBundle() ? "✓ BUNDLE" : "";
            return new javafx.beans.property.SimpleStringProperty(bundleInfo);
        });
        bundleCol.setCellFactory(col -> new TableCell<Reservation, String>() {
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
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        itemCol.setPrefWidth(200);

        TableColumn<Receipt, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(60);

        TableColumn<Receipt, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(60);

        TableColumn<Receipt, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getAmount()));
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

        // Load all receipts
        List<Receipt> allReceipts = receiptManager.getAllReceipts();
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
            List<Receipt> refreshed = receiptManager.getAllReceipts();
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
            
            for (Receipt item : bundleItems) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + item.getItemName());
                itemName.setMinWidth(250);
                
                javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + item.getSize());
                itemSize.setMinWidth(70);
                
                javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + item.getQuantity());
                itemQty.setMinWidth(60);
                
                javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", item.getAmount()));
                itemPrice.setStyle("-fx-font-weight: bold;");
                
                itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
                itemsSection.getChildren().add(itemRow);
                
                totalAmount += item.getAmount();
                totalQuantity += item.getQuantity();
            }
        } else {
            // Single item
            HBox itemRow = new HBox(10);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            
            javafx.scene.control.Label itemName = new javafx.scene.control.Label("• " + receipt.getItemName());
            itemName.setMinWidth(250);
            
            javafx.scene.control.Label itemSize = new javafx.scene.control.Label("Size: " + receipt.getSize());
            itemSize.setMinWidth(70);
            
            javafx.scene.control.Label itemQty = new javafx.scene.control.Label("Qty: " + receipt.getQuantity());
            itemQty.setMinWidth(60);
            
            javafx.scene.control.Label itemPrice = new javafx.scene.control.Label("₱" + String.format("%.2f", receipt.getAmount()));
            itemPrice.setStyle("-fx-font-weight: bold;");
            
            itemRow.getChildren().addAll(itemName, itemSize, itemQty, itemPrice);
            itemsSection.getChildren().add(itemRow);
            
            totalAmount = receipt.getAmount();
            totalQuantity = receipt.getQuantity();
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
    
    public void handleLogout() {
        boolean confirm = AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            LoginView loginView = new LoginView();
            Scene scene = new Scene(loginView.getView(), 1024, 768);
            SceneManager.setScene(scene);
        }
    }
}

