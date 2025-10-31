package gui.controllers;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Item;
import inventory.Reservation;
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
import java.util.ArrayList;

/**
 * StaffDashboardController - Handles staff dashboard operations
 */
public class StaffDashboardController {

    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;

    public StaffDashboardController() {
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);

        // Load data
        inventoryManager.getAllItems().forEach(item -> {});
    }

    public Node createReservationsView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Filter buttons
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button allBtn = new Button("All");
        Button pendingBtn = new Button("Pending");
        Button approvedBtn = new Button("Approved");
        Button returnRequestsBtn = new Button("Return Requests");
        Button refreshBtn = new Button("🔄 Refresh");

        styleActionButton(allBtn, "#0969DA");
        styleActionButton(pendingBtn, "#BF8700");
        styleActionButton(approvedBtn, "#1A7F37");
        styleActionButton(returnRequestsBtn, "#BF8700");
        styleActionButton(refreshBtn, "#6E7781");

        filterBar.getChildren().addAll(allBtn, pendingBtn, approvedBtn, returnRequestsBtn, refreshBtn);

        // Create reservations table
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

        TableColumn<Reservation, Double> priceCol = new TableColumn<>("Total");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalPrice()));
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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol, actionsCol);

        // Load all reservations
        ObservableList<Reservation> allReservations = FXCollections.observableArrayList(reservationManager.getAllReservations());
        table.setItems(allReservations);

        // Filter actions
        allBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations())));
        pendingBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(filtered));
        });
        approvedBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> r.getStatus().contains("APPROVED"))
                .collect(java.util.stream.Collectors.toList());
            table.setItems(FXCollections.observableArrayList(filtered));
        });
        returnRequestsBtn.setOnAction(e -> {
            List<Reservation> filtered = reservationManager.getReturnRequests();
            table.setItems(FXCollections.observableArrayList(filtered));
        });
        refreshBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations())));

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(filterBar, table);

        return container;
    }

    /**
     * Handle approve reservation
     */
    private void handleApproveReservation(Reservation reservation, TableView<Reservation> table) {
        boolean confirm = AlertHelper.showConfirmation("Approve Reservation",
            "Approve reservation for:\n" + reservation.getStudentName() + "\n" +
            reservation.getItemName() + " (" + reservation.getSize() + ")");

        if (confirm) {
            boolean success = reservationManager.approveReservation(reservation.getReservationId(), reservation.getSize());
            if (success) {
                table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                AlertHelper.showSuccess("Success", "Reservation approved!");
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
        dialog.setHeaderText("Reject reservation for: " + reservation.getStudentName());
        dialog.setContentText("Reason:");

        dialog.showAndWait().ifPresent(reason -> {
            if (!reason.isEmpty()) {
                boolean success = reservationManager.cancelReservation(reservation.getReservationId(), reason);
                if (success) {
                    table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                    AlertHelper.showSuccess("Success", "Reservation rejected");
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
        boolean confirm = AlertHelper.showConfirmation("Approve Return",
            "Approve return request for:\n" +
            "Student: " + reservation.getStudentName() + "\n" +
            "Item: " + reservation.getItemName() + " (" + reservation.getSize() + ")\n" +
            "Quantity: " + reservation.getQuantity() + "x\n" +
            "Refund Amount: ₱" + String.format("%.2f", reservation.getTotalPrice()) + "\n\n" +
            "Reason: " + (reservation.getReason() != null ? reservation.getReason() : "N/A") + "\n\n" +
            "This will restock the item and mark as refunded.");

        if (confirm) {
            boolean success = reservationManager.approveReturn(reservation.getReservationId());
            if (success) {
                table.setItems(FXCollections.observableArrayList(reservationManager.getAllReservations()));
                AlertHelper.showSuccess("Success",
                    "Return approved!\n\n" +
                    "Item has been restocked and marked as refunded.");
            } else {
                AlertHelper.showError("Error", "Failed to approve return. Item may not be restockable.");
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

    public Node createInventoryView() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Action buttons
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Refresh");
        TextField searchField = new TextField();
        searchField.setPromptText("Search by item name or code...");
        searchField.setPrefWidth(250);

        styleActionButton(refreshBtn, "#0969DA");

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

        table.getColumns().addAll(codeCol, nameCol, courseCol, sizeCol, qtyCol, priceCol);

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
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table);

        return container;
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

        styleActionButton(refreshBtn, "#0969DA");

        actionBar.getChildren().addAll(refreshBtn, searchField);

        // Create stock logs table
        TableView<String[]> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<String[], String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        timestampCol.setPrefWidth(150);

        TableColumn<String[], String> performedByCol = new TableColumn<>("Performed By");
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

        return container;
    }

    /**
     * Load stock logs from file
     */
    private List<String[]> loadStockLogs() {
        List<String[]> logs = new ArrayList<>();
        try {
            java.nio.file.Path logPath = java.nio.file.Paths.get("src/database/data/stock_logs.txt");
            if (java.nio.file.Files.exists(logPath)) {
                List<String> lines = java.nio.file.Files.readAllLines(logPath);
                for (String line : lines) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 8) {
                        logs.add(parts);
                    }
                }
            }
        } catch (Exception e) {
            // Silently handle error
        }
        return logs;
    }

    /**
     * Style action button helper
     */
    private void styleActionButton(Button button, String color) {
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-pref-height: 36px;"
        );
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

