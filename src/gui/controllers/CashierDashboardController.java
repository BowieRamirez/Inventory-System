package gui.controllers;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.Receipt;
import gui.utils.AlertHelper;
import gui.utils.SceneManager;
import gui.views.LoginView;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

/**
 * CashierDashboardController - Handles cashier dashboard operations
 */
public class CashierDashboardController {

    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;

    public CashierDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

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

        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getReservationId()));
        idCol.setPrefWidth(60);

        TableColumn<Reservation, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<Reservation, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        itemCol.setPrefWidth(200);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(60);

        TableColumn<Reservation, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalPrice()));
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

        TableColumn<Reservation, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<Reservation, Void>() {
            private final Button processBtn = new Button("Process Payment");

            {
                processBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand;");
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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, totalCol, actionsCol);

        // Load approved reservations waiting for payment
        List<Reservation> approvedReservations = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStatus().contains("APPROVED") && !r.isPaid())
            .collect(java.util.stream.Collectors.toList());
        ObservableList<Reservation> reservationsList = FXCollections.observableArrayList(approvedReservations);
        table.setItems(reservationsList);

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Reservation> refreshed = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("APPROVED") && !r.isPaid())
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(refreshed));
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Show reservation details
        Label itemLabel = new Label("Item: " + reservation.getItemName() + " (" + reservation.getSize() + ")");
        Label qtyLabel = new Label("Quantity: " + reservation.getQuantity());
        Label totalLabel = new Label("Total: ₱" + String.format("%.2f", reservation.getTotalPrice()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // Payment method selector
        ComboBox<String> paymentMethodBox = new ComboBox<>();
        paymentMethodBox.getItems().addAll("CASH", "GCASH", "CARD", "BANK");
        paymentMethodBox.setValue("CASH");

        grid.add(itemLabel, 0, 0, 2, 1);
        grid.add(qtyLabel, 0, 1, 2, 1);
        grid.add(totalLabel, 0, 2, 2, 1);
        grid.add(new Label("Payment Method:"), 0, 3);
        grid.add(paymentMethodBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == processButtonType) {
                return paymentMethodBox.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(paymentMethod -> {
            boolean success = reservationManager.markAsPaid(reservation.getReservationId(), paymentMethod);
            if (success) {
                // Create receipt
                Receipt receipt = receiptManager.createReceipt(
                    "PAID",
                    reservation.getQuantity(),
                    reservation.getTotalPrice(),
                    reservation.getItemCode(),
                    reservation.getItemName(),
                    reservation.getSize(),
                    reservation.getStudentName()
                );

                // Refresh table
                List<Reservation> refreshed = reservationManager.getAllReservations().stream()
                    .filter(r -> r.getStatus().contains("APPROVED") && !r.isPaid())
                    .collect(java.util.stream.Collectors.toList());
                table.setItems(FXCollections.observableArrayList(refreshed));

                AlertHelper.showSuccess("Success",
                    "Payment processed successfully!\n\n" +
                    "Receipt ID: " + receipt.getReceiptId() + "\n" +
                    "Payment Method: " + paymentMethod);
            } else {
                AlertHelper.showError("Error", "Failed to process payment");
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

        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getReservationId()));
        idCol.setPrefWidth(60);

        TableColumn<Reservation, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setPrefWidth(150);

        TableColumn<Reservation, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getItemName()));
        itemCol.setPrefWidth(200);

        TableColumn<Reservation, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(60);

        TableColumn<Reservation, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(60);

        TableColumn<Reservation, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalPrice()));
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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, totalCol, statusCol);

        // Load all reservations
        ObservableList<Reservation> reservationsList = FXCollections.observableArrayList(reservationManager.getAllReservations());
        table.setItems(reservationsList);

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
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

        table.getColumns().addAll(idCol, dateCol, buyerCol, itemCol, sizeCol, qtyCol, amountCol, statusCol);

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

        return container;
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

