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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
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
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
 
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
import javafx.scene.control.ListView;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Tab;
import java.util.logging.Logger;
import javafx.scene.control.TabPane;

/**
 * StaffDashboardController - Handles staff dashboard operations
 */
@SuppressWarnings("unchecked")
public class StaffDashboardController {

    private static final Logger LOGGER = Logger.getLogger(StaffDashboardController.class.getName());

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
    private void handleChangePriceForItem(Item item, Runnable refreshAction) {
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
                    refreshAction.run();

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
            "-fx-background-color: -color-bg-default;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-padding: 8px;" +
            "-fx-font-size: 13px;" +
            "-fx-text-fill: -color-fg-default;"
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
        Button replacedBtn = new Button("Replaced");
        Button pickupApprovalsBtn = new Button("📦 Pickup Approvals");
        Button returnRequestsBtn = new Button("Replacement Requests");
        Button refreshBtn = new Button("🔄 Refresh");

        styleActionButton(allBtn);
        styleActionButton(pendingBtn);
        styleActionButton(approvedBtn);
        styleActionButton(replacedBtn);
        styleActionButton(pickupApprovalsBtn);
        styleActionButton(returnRequestsBtn);
        styleActionButton(refreshBtn);

        // Create small red badge labels for pickup and replacement counts
        int initialPickupCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()).size();
        int initialReplacementCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getReturnRequests()).size();
        int initialPendingCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPendingReservations()).size();

        final Label pickupBadge = new Label(String.valueOf(initialPickupCount));
        pickupBadge.setVisible(initialPickupCount > 0);
        pickupBadge.setStyle(
            "-fx-background-color: #CF222E; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 2 6; " +
            "-fx-background-radius: 999px; " +
            "-fx-min-width: 20px; " +
            "-fx-alignment: center;"
        );

        final Label returnBadge = new Label(String.valueOf(initialReplacementCount));
        returnBadge.setVisible(initialReplacementCount > 0);

        final Label pendingBadge = new Label(String.valueOf(initialPendingCount));
        pendingBadge.setVisible(initialPendingCount > 0);
        pendingBadge.setStyle(
            "-fx-background-color: #CF222E; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 2 6; " +
            "-fx-background-radius: 999px; " +
            "-fx-min-width: 20px; " +
            "-fx-alignment: center;"
        );
        returnBadge.setStyle(
            "-fx-background-color: #CF222E; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-padding: 2 6; " +
            "-fx-background-radius: 999px; " +
            "-fx-min-width: 20px; " +
            "-fx-alignment: center;"
        );

        // Stack the badge on top-right of the button
        StackPane pickupStack = new StackPane(pickupApprovalsBtn, pickupBadge);
        StackPane.setAlignment(pickupBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(pickupBadge, new Insets(0, -6, 20, 0));

        StackPane returnStack = new StackPane(returnRequestsBtn, returnBadge);
        StackPane.setAlignment(returnBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(returnBadge, new Insets(0, -6, 20, 0));

        StackPane pendingStack = new StackPane(pendingBtn, pendingBadge);
        StackPane.setAlignment(pendingBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(pendingBadge, new Insets(0, -6, 20, 0));

        filterBar.getChildren().addAll(allBtn, pendingStack, approvedBtn, replacedBtn, pickupStack, returnStack, refreshBtn);

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
        // Render a colored badge so approved/completed/replaced/pending states are obvious
        statusCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Reservation r = (getTableRow() != null) ? (Reservation) getTableRow().getItem() : null;
                if (r == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                String s = status.toUpperCase();

                javafx.scene.control.Label badge = new javafx.scene.control.Label();
                badge.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 10; -fx-background-radius: 6;");

                if (s.contains("REPLACED")) {
                    badge.setText("REPLACED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #656D76; -fx-text-fill: white;");
                } else if (s.contains("COMPLETED")) {
                    badge.setText("COMPLETED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #1A7F37; -fx-text-fill: white;");
                } else if (s.contains("PICKUP") || s.contains("REQUESTED") && s.contains("PICKUP")) {
                    // Any pickup-related status (student requested pickup / awaiting staff approval)
                    badge.setText("PICKUP REQUESTED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #0969DA; -fx-text-fill: white;");
                } else if (s.contains("PAID") || r.isPaid()) {
                    // Paid reservations - indicate approved+paid (ready for pickup / awaiting pickup approval)
                    if (s.contains("AWAITING") || s.contains("PICKUP") || s.contains("AWAITING PICKUP")) {
                        badge.setText("APPROVED (PAID) - AWAITING PICKUP");
                    } else {
                        badge.setText("APPROVED (PAID)");
                    }
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #1A7F37; -fx-text-fill: white;");
                } else if (s.contains("APPROVED")) {
                    // Approved but not yet paid
                    if (s.contains("WAITING FOR PAYMENT") || s.contains("WAITING")) {
                        badge.setText("WAITING FOR PAYMENT");
                    } else {
                        badge.setText("APPROVED");
                    }
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #BF8700; -fx-text-fill: white;");
                } else if (s.contains("REPLACEMENT")) {
                    badge.setText("REPLACEMENT REQUESTED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #BF8700; -fx-text-fill: white;");
                } else if (s.contains("PENDING")) {
                    badge.setText("PENDING");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #BF8700; -fx-text-fill: white;");
                } else if (s.contains("CANCEL")) {
                    badge.setText("CANCELLED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #CF222E; -fx-text-fill: white;");
                } else {
                    // Fallback: show raw status
                    badge.setText(status);
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #E6E6E6; -fx-text-fill: #222;");
                }

                setGraphic(badge);
                setText(null);
            }
        });

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
                    Reservation reservation = (getTableRow() != null) ? (Reservation) getTableRow().getItem() : null;
                    if (reservation == null) {
                        setGraphic(null);
                        return;
                    }
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

            private final javafx.beans.value.ChangeListener<Reservation> rowItemListener = (obs, oldItem, newItem) -> {
                // When the row's item changes (due to virtualization or setItems), refresh UI
                updateForReservation(newItem);
            };

            {
                approveBtn.setStyle("-fx-background-color: #1A7F37; -fx-text-fill: white; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-cursor: hand;");
                buttons.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                // Ensure we listen to row item changes so the cell updates when the row is recycled
                TableRow<Reservation> row = getTableRow();
                if (row != null) {
                    // Remove previous listener (defensive)
                    try { row.itemProperty().removeListener(rowItemListener); } catch (Exception ex) { }
                    row.itemProperty().addListener(rowItemListener);
                }

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Reservation current = (row != null) ? row.getItem() : null;
                updateForReservation(current);
            }

            private void updateForReservation(Reservation reservation) {
                if (reservation == null) {
                    setGraphic(null);
                    return;
                }

                // Make a final reference for use inside lambda handlers
                final Reservation res = reservation;

                String status = res.getStatus() != null ? res.getStatus().toUpperCase() : "";

                boolean isBundle = res.isPartOfBundle();
                boolean bundleHasPending = false, bundleHasReplacement = false, bundleHasPickup = false;
                if (isBundle) {
                    String bundleId = res.getBundleId();
                    java.util.List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                        .filter(r -> bundleId != null && bundleId.equals(r.getBundleId()))
                        .collect(java.util.stream.Collectors.toList());
                    for (Reservation bi : bundleItems) {
                        String s = bi.getStatus() != null ? bi.getStatus().toUpperCase() : "";
                        if (s.contains("PENDING")) bundleHasPending = true;
                        if (s.contains("REPLACEMENT")) bundleHasReplacement = true;
                        if (s.contains("PICKUP")) bundleHasPickup = true;
                    }
                }

                // If the representative status indicates a final/approved state, do not show actions
                boolean isFinalState = status.contains("APPROVED") || status.contains("PAID") || status.contains("COMPLETED") || status.contains("REPLACED");
                if (isFinalState) {
                    setGraphic(null);
                    return;
                }

                boolean showPending = status.contains("PENDING") || (isBundle && bundleHasPending);
                boolean showReplacement = status.contains("REPLACEMENT") || (isBundle && bundleHasReplacement);
                boolean showPickup = status.contains("PICKUP") || (isBundle && bundleHasPickup);

                LOGGER.fine("[StaffDashboard] actions.updateForReservation resId=" + res.getReservationId() + " status='" + res.getStatus() + "' isBundle=" + isBundle);

                if (showPending) {
                    approveBtn.setText("✓ Approve");
                    rejectBtn.setText("✗ Reject");
                    approveBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleApproveReservation(current, table);
                    });
                    rejectBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleRejectReservation(current, table);
                    });
                    setGraphic(buttons);
                } else if (showReplacement) {
                    approveBtn.setText("✓ Approve Replacement");
                    rejectBtn.setText("✗ Reject Return");
                    approveBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleApproveReturn(current, table);
                    });
                    rejectBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleRejectReturn(current, table);
                    });
                    setGraphic(buttons);
                } else if (showPickup) {
                    approveBtn.setText("✓ Approve Pickup");
                    rejectBtn.setText("✗ Reject");
                    approveBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleApprovePickup(current, table);
                    });
                    rejectBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleRejectPickup(current, table);
                    });
                    setGraphic(buttons);
                } else {
                    setGraphic(null);
                }
            }
        });
        actionsCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol, bundleCol, actionsCol);
        

        // Pagination + search setup (10 items per page, prev/next, page label visible when pages > 2)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        // Default to showing PENDING reservations on view load
        List<Reservation> pendingReservations = new ArrayList<>(reservationManager.getPendingReservations());

        List<Reservation> allReservations = new ArrayList<>(ControllerUtils.getDeduplicatedReservations(pendingReservations));
        List<Reservation> workingFiltered = new ArrayList<>(allReservations); // current filtered set from status buttons

        // Track current filter for refresh logic (default to PENDING)
        final String[] currentFilter = {"PENDING"}; // ALL, PENDING, APPROVED, PICKUP_APPROVALS, RETURN_REQUESTS

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

        

        // Function to update table with current page and applied search
        Runnable updateTable = () -> {
            List<Reservation> display = new ArrayList<>(workingFiltered);

            // Apply search
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String st = searchText.toLowerCase().trim();
                display = display.stream()
                    .filter(r -> {
                        String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                        if (orderId != null && orderId.toLowerCase().contains(st)) return true;
                        if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(st)) return true;
                        if (r.getStudentId() != null && r.getStudentId().toLowerCase().contains(st)) return true;
                        if (r.getItemName() != null && r.getItemName().toLowerCase().contains(st)) return true;
                        if (r.getStatus() != null && r.getStatus().toLowerCase().contains(st)) return true;
                        if (r.isPartOfBundle() && r.getBundleId() != null) {
                            boolean matchInBundle = reservationManager.getAllReservations().stream()
                                .filter(res -> r.getBundleId().equals(res.getBundleId()))
                                .anyMatch(res -> res.getItemName() != null && res.getItemName().toLowerCase().contains(st));
                            if (matchInBundle) return true;
                        }
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) display.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;

            

            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, display.size());
            List<Reservation> pageItems = display.isEmpty() ? java.util.Collections.emptyList() : display.subList(start, end);

            table.setItems(FXCollections.observableArrayList(pageItems));

            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            pageLabel.setVisible(totalPages > 2);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };

        // Prev/Next actions
        prevBtn.setOnAction(e -> {
            if (currentPage[0] > 1) {
                currentPage[0]--;
                updateTable.run();
            }
        });
        nextBtn.setOnAction(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) workingFiltered.size() / itemsPerPage));
            if (currentPage[0] < totalPages) {
                currentPage[0]++;
                updateTable.run();
            }
        });

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage[0] = 1;
            updateTable.run();
        });

        // Clear search
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            currentPage[0] = 1;
            updateTable.run();
        });

        // Filter actions - update workingFiltered and reset page
        allBtn.setOnAction(e -> {
            currentFilter[0] = "ALL";
            List<Reservation> filtered = reservationManager.getAllReservations();
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Ensure actions column is visible for non-approved filters
            actionsCol.setVisible(true);
            updateTable.run();
            activateFilterButton(allBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        pendingBtn.setOnAction(e -> {
            currentFilter[0] = "PENDING";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Show actions for pending
            actionsCol.setVisible(true);
            updateTable.run();
            activateFilterButton(pendingBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        approvedBtn.setOnAction(e -> {
            currentFilter[0] = "APPROVED";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> {
                    String s = r.getStatus();
                    if (s == null) return false;
                    s = s.toUpperCase();
                    // Accept any of these that represent approved/completed/paid flows
                    if (s.contains("APPROVED")) return true;
                    if (s.contains("PAID")) return true;
                    if (s.contains("COMPLETED")) return true;
                    // Also include reservations that are marked paid even if status text differs
                    if (r.isPaid()) return true;
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Hide actions column for approved view
            actionsCol.setVisible(false);
            updateTable.run();
            activateFilterButton(approvedBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        replacedBtn.setOnAction(e -> {
            currentFilter[0] = "REPLACED";
            List<Reservation> filtered = reservationManager.getAllReservations().stream()
                .filter(r -> {
                    String s = r.getStatus();
                    return s != null && s.toUpperCase().contains("REPLACED");
                })
                .collect(java.util.stream.Collectors.toList());
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Hide actions column for replaced view
            actionsCol.setVisible(false);
            updateTable.run();
            activateFilterButton(replacedBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        pickupApprovalsBtn.setOnAction(e -> {
            currentFilter[0] = "PICKUP_APPROVALS";
            List<Reservation> filtered = reservationManager.getPickupRequestsAwaitingApproval();
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Show actions for pickup approvals
            actionsCol.setVisible(true);
            updateTable.run();
            activateFilterButton(pickupApprovalsBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        returnRequestsBtn.setOnAction(e -> {
            currentFilter[0] = "RETURN_REQUESTS";
            List<Reservation> filtered = reservationManager.getReturnRequests();
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            // Show actions for return requests
            actionsCol.setVisible(true);
            updateTable.run();
            activateFilterButton(returnRequestsBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
        });

        refreshBtn.setOnAction(e -> {
            // Rebuild based on currentFilter
            List<Reservation> refreshed;
            switch (currentFilter[0]) {
                case "PENDING":
                    refreshed = reservationManager.getAllReservations().stream().filter(r -> "PENDING".equals(r.getStatus())).collect(java.util.stream.Collectors.toList());
                    break;
                case "APPROVED":
                    refreshed = reservationManager.getAllReservations().stream().filter(r -> {
                        String s = r.getStatus();
                        if (s == null) return false;
                        s = s.toUpperCase();
                        if (s.contains("APPROVED")) return true;
                        if (s.contains("PAID")) return true;
                        if (s.contains("COMPLETED")) return true;
                        if (r.isPaid()) return true;
                        return false;
                    }).collect(java.util.stream.Collectors.toList());
                    break;
                case "REPLACED":
                    refreshed = reservationManager.getAllReservations().stream().filter(r -> {
                        String s = r.getStatus();
                        return s != null && s.toUpperCase().contains("REPLACED");
                    }).collect(java.util.stream.Collectors.toList());
                    break;
                case "PICKUP_APPROVALS":
                    refreshed = reservationManager.getPickupRequestsAwaitingApproval();
                    break;
                case "RETURN_REQUESTS":
                    refreshed = reservationManager.getReturnRequests();
                    break;
                default:
                    refreshed = reservationManager.getAllReservations().stream().filter(r -> "PENDING".equals(r.getStatus()) || "REPLACEMENT REQUESTED".equals(r.getStatus())).collect(java.util.stream.Collectors.toList());
            }
            allReservations.clear(); allReservations.addAll(ControllerUtils.getDeduplicatedReservations(refreshed));
            workingFiltered.clear(); workingFiltered.addAll(allReservations);
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
            // Refresh badge counts after table update
            int updatedPickupCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()).size();
            pickupBadge.setText(String.valueOf(updatedPickupCount));
            pickupBadge.setVisible(updatedPickupCount > 0);
            int updatedReturnCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getReturnRequests()).size();
            returnBadge.setText(String.valueOf(updatedReturnCount));
            returnBadge.setVisible(updatedReturnCount > 0);
            int updatedPendingCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPendingReservations()).size();
            pendingBadge.setText(String.valueOf(updatedPendingCount));
            pendingBadge.setVisible(updatedPendingCount > 0);
            // Keep the currently selected filter highlighted after a refresh
            switch (currentFilter[0]) {
                case "PENDING":
                    activateFilterButton(pendingBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
                case "APPROVED":
                    activateFilterButton(approvedBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
                case "REPLACED":
                    activateFilterButton(replacedBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
                case "PICKUP_APPROVALS":
                    activateFilterButton(pickupApprovalsBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
                case "RETURN_REQUESTS":
                    activateFilterButton(returnRequestsBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
                case "ALL":
                default:
                    activateFilterButton(allBtn, allBtn, pendingBtn, approvedBtn, replacedBtn, pickupApprovalsBtn, returnRequestsBtn, refreshBtn);
                    break;
            }
        });

        // Set the refresh callback for when items are approved/rejected
        this.refreshCallback = () -> {
            // trigger a refresh equivalent to clicking refresh
            refreshBtn.fire();
        };

        // Initially load pending reservations into the table (so view shows Pending by default)
        // This mirrors clicking the Pending button on open
        pendingBtn.fire();

        // Set fixed row height to match stock logs
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        VBox.setVgrow(table, Priority.ALWAYS);
        // Do not add the statsBox to the UI (hide the top summary boxes)
        container.getChildren().addAll(searchBar, filterBar, table, pageControls);

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

        // Course filter dropdown (All + per-course)
        HBox courseBar = new HBox(8);
        courseBar.setAlignment(Pos.CENTER_LEFT);
        courseBar.setPadding(new Insets(0, 0, 8, 0));

        // Build course ComboBox
        List<String> availableCourses = inventoryManager.getAvailableCourses();
        availableCourses.removeIf(s -> s == null || s.trim().isEmpty());
        if (!availableCourses.contains("STI Special")) availableCourses.add(0, "STI Special");
        java.util.Collections.sort(availableCourses);

        ObservableList<String> courseItems = FXCollections.observableArrayList();
        courseItems.add("All");
        courseItems.addAll(availableCourses);

        javafx.scene.control.ComboBox<String> courseCombo = new javafx.scene.control.ComboBox<>(courseItems);
        courseCombo.setValue("All");
        courseCombo.setPromptText("Select course");
        courseCombo.setPrefWidth(240);
        courseCombo.setPrefHeight(45);
        // Theme-aware styling: slightly darker white for light theme to reduce plainness
        String fieldBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
        String fieldBorder = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(0,0,0,0.06)";
        String fieldText = ThemeManager.isDarkMode() ? "white" : "#111827";
        String comboStyle =
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-control-inner-background: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: " + fieldBorder + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 10px;" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 0px 8px;" +
            "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
        courseCombo.setStyle(comboStyle);

        courseBar.getChildren().add(courseCombo);

        // Update ComboBox style when the application theme changes
        Runnable courseThemeRefresher = () -> {
            try {
                String fieldBg2 = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
                String fieldBorder2 = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.06)" : "rgba(0,0,0,0.06)";
                String fieldText2 = ThemeManager.isDarkMode() ? "white" : "#111827";
                String comboStyle2 =
                    "-fx-font-size: 14px;" +
                    "-fx-background-color: " + fieldBg2 + ";" +
                    "-fx-control-inner-background: " + fieldBg2 + ";" +
                    "-fx-text-fill: " + fieldText2 + ";" +
                    "-fx-border-color: " + fieldBorder2 + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-padding: 0px 8px;" +
                    "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
                if (javafx.application.Platform.isFxApplicationThread()) {
                    courseCombo.setStyle(comboStyle2);
                } else {
                    javafx.application.Platform.runLater(() -> courseCombo.setStyle(comboStyle2));
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
            javafx.scene.control.ComboBox<String> courseComboDialog = new javafx.scene.control.ComboBox<>();
            List<String> courses = inventoryManager.getAvailableCourses();
            courses.removeIf(s -> s == null || s.trim().isEmpty());
            if (!courses.contains("STI Special")) {
                courses.add(0, "STI Special");
            }
            courseComboDialog.setItems(FXCollections.observableArrayList(courses));
            courseComboDialog.setEditable(true);
            courseComboDialog.setPromptText("Course or 'STI Special'");

            javafx.scene.control.ComboBox<String> sizeCombo = new javafx.scene.control.ComboBox<>();
            sizeCombo.setItems(FXCollections.observableArrayList("S", "M", "L", "XL", "One Size"));
            sizeCombo.setPromptText("Size");

            TextField qtyField = new TextField();
            qtyField.setPromptText("Quantity");

            TextField priceField = new TextField();
            priceField.setPromptText("Price (e.g. 450.00)");

            content.getChildren().addAll(codeLabel, nameField, courseComboDialog, sizeCombo, qtyField, priceField);

            dialog.getDialogPane().setContent(content);

            // Enable/disable Add button based on validation
            javafx.scene.control.Button addActionBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(addBtnType);
            addActionBtn.setDisable(true);

            // Simple validation listener
            Runnable validate = () -> {
                boolean ok = !nameField.getText().trim().isEmpty()
                         && courseComboDialog.getValue() != null && !courseComboDialog.getValue().trim().isEmpty()
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
            courseComboDialog.valueProperty().addListener((obs, o, n) -> validate.run());
            sizeCombo.valueProperty().addListener((obs, o, n) -> validate.run());
            qtyField.textProperty().addListener((obs, o, n) -> validate.run());
            priceField.textProperty().addListener((obs, o, n) -> validate.run());

            dialog.setResultConverter(button -> {
                if (button == addBtnType) {
                    try {
                        String name = nameField.getText().trim();
                        String course = courseComboDialog.getValue().trim();
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
        TableView<InventoryRow> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");

        TableColumn<InventoryRow, Integer> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getCode()));
        codeCol.setPrefWidth(80);

        TableColumn<InventoryRow, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);

        TableColumn<InventoryRow, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCourse()));
        courseCol.setPrefWidth(100);

        TableColumn<InventoryRow, String> sizesCol = new TableColumn<>("Sizes");
        sizesCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSizesDisplay()));
        sizesCol.setPrefWidth(160);

        TableColumn<InventoryRow, Integer> qtyCol = new TableColumn<>("Total Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getTotalQuantity()));
        qtyCol.setPrefWidth(120);

        TableColumn<InventoryRow, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPriceDisplay()));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<InventoryRow, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
            }
        });
        
        // Actions column - Adjust Stock and Change Price buttons
        TableColumn<InventoryRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<InventoryRow, Void>() {
            private final Button adjustBtn = new Button("📝 Adjust Stock");
            private final Button priceBtn = new Button("₱ Change Price");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    InventoryRow currentRow = getTableView().getItems().get(getIndex());
                    adjustBtn.setStyle("-fx-background-color: #0969DA; -fx-text-fill: white; -fx-cursor: hand;");
                    adjustBtn.setOnAction(e -> showVariantSelectionDialog(currentRow, "Adjust Stock", selected -> handleStockAdjustmentForItem(selected, refreshBtn::fire)));

                    priceBtn.setStyle("-fx-background-color: #0A84FF; -fx-text-fill: white; -fx-cursor: hand;");
                    priceBtn.setOnAction(e -> showVariantSelectionDialog(currentRow, "Change Price", selected -> handleChangePriceForItem(selected, refreshBtn::fire)));

                    HBox btns = new HBox(8, adjustBtn, priceBtn);
                    btns.setAlignment(Pos.CENTER);
                    setGraphic(btns);
                }
            }
        });
        actionsCol.setPrefWidth(220);
        
        table.getColumns().addAll(codeCol, nameCol, courseCol, sizesCol, qtyCol, priceCol, actionsCol);

        // Make columns resize to fill the available width of the container
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefWidth(Double.MAX_VALUE);

        // Bind column widths as percentages of the table width so the table fills its box
        codeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.36));
        courseCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        sizesCol.prefWidthProperty().bind(table.widthProperty().multiply(0.18));
        qtyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.10));
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

        // Course combo action -> update via helper
        courseCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                courseCombo.setValue("All");
                currentCourse[0] = "All";
            } else {
                currentCourse[0] = newV;
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
    private void updateInventoryTable(TableView<InventoryRow> table, List<Item> allItems, String[] currentCourse,
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

        List<InventoryRow> aggregated = buildInventoryRows(filtered);

        int totalPages = Math.max(1, (int) Math.ceil((double) aggregated.size() / itemsPerPage));
        if (currentPage[0] > totalPages) currentPage[0] = totalPages;

        int start = (currentPage[0] - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, aggregated.size());
        List<InventoryRow> pageItems = aggregated.isEmpty() ? java.util.Collections.emptyList() : aggregated.subList(start, end);

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

    private List<InventoryRow> buildInventoryRows(List<Item> source) {
        Map<Integer, List<Item>> grouped = new LinkedHashMap<>();
        for (Item item : source) {
            grouped.computeIfAbsent(item.getCode(), k -> new ArrayList<>()).add(item);
        }
        return grouped.values().stream().map(InventoryRow::new).collect(Collectors.toList());
    }

    private void showVariantSelectionDialog(InventoryRow row, String title, Consumer<Item> onVariantSelected) {
        List<Item> variants = row.getVariants();
        if (variants.isEmpty()) return;

        Dialog<Item> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Choose a size for " + row.getName());
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        ListView<Item> sizeList = new ListView<>(FXCollections.observableArrayList(variants));
        sizeList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Item variant, boolean empty) {
                super.updateItem(variant, empty);
                if (empty || variant == null) {
                    setText(null);
                } else {
                    setText(variant.getSize() + "  |  Qty: " + variant.getQuantity());
                }
            }
        });
        sizeList.setPrefHeight(Math.min(variants.size() * 42 + 20, 220));
        sizeList.getSelectionModel().selectFirst();

        Button selectBtn = (Button) dialog.getDialogPane().lookupButton(selectButtonType);
        if (selectBtn != null) {
            selectBtn.setDisable(variants.isEmpty());
        }
        sizeList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (selectBtn != null) {
                selectBtn.setDisable(newVal == null);
            }
        });

        dialog.getDialogPane().setContent(sizeList);
        dialog.setResultConverter(btn -> btn == selectButtonType ? sizeList.getSelectionModel().getSelectedItem() : null);
        dialog.showAndWait().ifPresent(onVariantSelected);
    }

    private static class InventoryRow {
        private final int code;
        private final String name;
        private final String course;
        private final List<Item> variants;
        private final int totalQuantity;
        private final String sizesDisplay;
        private final String priceDisplay;

        InventoryRow(List<Item> variants) {
            Objects.requireNonNull(variants, "Variant list cannot be null");
            if (variants.isEmpty()) {
                throw new IllegalArgumentException("InventoryRow requires at least one variant");
            }
            this.variants = new ArrayList<>(variants);
            this.code = variants.get(0).getCode();
            this.name = variants.get(0).getName();
            this.course = variants.get(0).getCourse();
            this.totalQuantity = variants.stream().mapToInt(Item::getQuantity).sum();
            this.sizesDisplay = buildSizesDisplay();
            this.priceDisplay = buildPriceDisplay();
        }

        private String buildSizesDisplay() {
            Set<String> uniqueSizes = new LinkedHashSet<>();
            for (Item variant : variants) {
                String size = variant.getSize() == null ? "" : variant.getSize().trim();
                if (!size.isEmpty()) {
                    uniqueSizes.add(size);
                }
            }
            return uniqueSizes.isEmpty() ? "N/A" : String.join(" | ", uniqueSizes);
        }

        private String buildPriceDisplay() {
            Set<Double> uniquePrices = variants.stream()
                .map(Item::getPrice)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            if (uniquePrices.size() == 1) {
                return String.format("₱%.2f", uniquePrices.iterator().next());
            }
            return "Varies";
        }

        public int getCode() { return code; }
        public String getName() { return name; }
        public String getCourse() { return course; }
        public List<Item> getVariants() { return java.util.Collections.unmodifiableList(variants); }
        public int getTotalQuantity() { return totalQuantity; }
        public String getSizesDisplay() { return sizesDisplay; }
        public String getPriceDisplay() { return priceDisplay; }
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
            card.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 10; -fx-background-radius: 6; -fx-border-radius:6; -fx-border-color: -color-border-default;");
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
    private void handleStockAdjustmentForItem(Item item, Runnable refreshAction) {
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
                    refreshAction.run();

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
        
        // Only student/user-relevant actions (customer activities only - NOT staff/admin updates)
        // 'STAFF_RETURN' is an administrative/staff-level action and must not be shown
        // in the general student/user activity view for regular staff users.
        List<String> studentOnlyActions = java.util.Arrays.asList(
            "USER_PICKUP", "USER_RETURN"
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
     * Activate a filter button visually and animate it briefly. Resets other buttons.
     */
    private void activateFilterButton(Button active, Button... allButtons) {
        for (Button b : allButtons) {
            if (b == active) {
                // Slightly darker blue for the active state
                b.setStyle(
                    "-fx-background-color: #002c6eff;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-cursor: hand;" +
                    "-fx-pref-height: 36px;"
                );

                try {
                    ScaleTransition st = new ScaleTransition(Duration.millis(140), b);
                    st.setFromX(1.0);
                    st.setFromY(1.0);
                    st.setToX(1.03);
                    st.setToY(1.03);
                    st.setCycleCount(2);
                    st.setAutoReverse(true);
                    st.play();
                } catch (Exception ex) {
                    // If animation fails for any reason, ignore and keep the active style
                }
            } else {
                // Reset style for non-active buttons
                styleActionButton(b);
            }
        }
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
                    Reservation reservation = (getTableRow() != null) ? (Reservation) getTableRow().getItem() : null;
                    if (reservation == null) {
                        setGraphic(null);
                        return;
                    }
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
                    Reservation reservation = (getTableRow() != null) ? (Reservation) getTableRow().getItem() : null;
                    if (reservation == null) {
                        setGraphic(null);
                        return;
                    }
                    approveBtn.setOnAction(e -> handleApprovePickup(reservation, table));
                    rejectBtn.setOnAction(e -> handleRejectPickup(reservation, table));
                    setGraphic(buttons);
                }
            }
        });
        actionsCol.setPrefWidth(180);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, bundleCol, actionsCol);

        // Pagination + search setup for pickup approvals (10 items per page)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        List<Reservation> sourceList = ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval());
        List<Reservation> workingFiltered = new ArrayList<>(sourceList);

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

        // Row click handler
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

        // Function to update table with pagination and search
        Runnable updateTable = () -> {
            List<Reservation> display = new ArrayList<>(workingFiltered);

            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String st = searchText.toLowerCase().trim();
                display = display.stream()
                    .filter(r -> {
                        String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                        if (orderId != null && orderId.toLowerCase().contains(st)) return true;
                        if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(st)) return true;
                        if (r.getStudentId() != null && r.getStudentId().toLowerCase().contains(st)) return true;
                        if (r.getItemName() != null && r.getItemName().toLowerCase().contains(st)) return true;
                        return false;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) display.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;

            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, display.size());
            List<Reservation> pageItems = display.isEmpty() ? java.util.Collections.emptyList() : display.subList(start, end);

            table.setItems(FXCollections.observableArrayList(pageItems));

            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            pageLabel.setVisible(totalPages > 2);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };

        prevBtn.setOnAction(e -> {
            if (currentPage[0] > 1) { currentPage[0]--; updateTable.run(); }
        });
        nextBtn.setOnAction(e -> {
            int totalPages = Math.max(1, (int) Math.ceil((double) workingFiltered.size() / itemsPerPage));
            if (currentPage[0] < totalPages) { currentPage[0]++; updateTable.run(); }
        });

        // Search listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            currentPage[0] = 1; updateTable.run();
        });

        // Refresh callback and button
        Runnable doRefresh = () -> {
            sourceList.clear();
            sourceList.addAll(ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()));
            workingFiltered.clear(); workingFiltered.addAll(sourceList);
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();

            // update stats
            int updatedCount = (int) ControllerUtils.getDeduplicatedReservations(
                reservationManager.getPickupRequestsAwaitingApproval()).size();
            try {
                ((javafx.scene.control.Label) ((VBox) statsBox.getChildren().get(0)).getChildren().get(1))
                    .setText(String.valueOf(updatedCount));
            } catch (Exception ex) {
                // ignore layout differences
            }
        };

        refreshBtn.setOnAction(e -> doRefresh.run());
        this.refreshCallback = () -> doRefresh.run();

        // Fixed row height to match stock logs
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        // Initial load
        workingFiltered.clear(); workingFiltered.addAll(sourceList);
        updateTable.run();

        VBox.setVgrow(table, Priority.ALWAYS);
        // Hide the pickup-approvals summary card from the layout
        container.getChildren().addAll(actionBar, table, pageControls);

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

        // Pagination + search setup for completed orders (10 items per page)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        List<Reservation> sourceList = ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations().stream().filter(r -> "COMPLETED".equals(r.getStatus())).collect(java.util.stream.Collectors.toList())
        );
        List<Reservation> workingFiltered = new ArrayList<>(sourceList);

        HBox pageControls = new HBox(12);
        pageControls.setAlignment(Pos.CENTER);
        pageControls.setPadding(new Insets(12,0,0,0));

        Button prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label();
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        Button nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        pageControls.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // Row click handler
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

        // Update table func
        Runnable updateTable = () -> {
            List<Reservation> display = new ArrayList<>(workingFiltered);
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String st = searchText.toLowerCase().trim();
                display = display.stream().filter(r -> {
                    String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                    if (orderId != null && orderId.toLowerCase().contains(st)) return true;
                    if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(st)) return true;
                    if (r.getBundleId() != null && r.getBundleId().toLowerCase().contains(st)) return true;
                    return false;
                }).collect(java.util.stream.Collectors.toList());
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) display.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;
            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, display.size());
            List<Reservation> pageItems = display.isEmpty() ? java.util.Collections.emptyList() : display.subList(start, end);
            table.setItems(FXCollections.observableArrayList(pageItems));

            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            pageLabel.setVisible(totalPages > 2);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };

        prevBtn.setOnAction(e -> { if (currentPage[0] > 1) { currentPage[0]--; updateTable.run(); } });
        nextBtn.setOnAction(e -> { int totalPages = Math.max(1, (int) Math.ceil((double) workingFiltered.size() / itemsPerPage)); if (currentPage[0] < totalPages) { currentPage[0]++; updateTable.run(); } });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage[0] = 1; updateTable.run(); });

        // Refresh button behavior
        Runnable doRefresh = () -> {
            sourceList.clear();
            sourceList.addAll(ControllerUtils.getDeduplicatedReservations(
                reservationManager.getAllReservations().stream().filter(r -> "COMPLETED".equals(r.getStatus())).collect(java.util.stream.Collectors.toList())
            ));
            workingFiltered.clear(); workingFiltered.addAll(sourceList);
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
        };

        refreshBtn.setOnAction(e -> doRefresh.run());

        // Fixed row height
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        // Initial load
        workingFiltered.clear(); workingFiltered.addAll(sourceList);
        updateTable.run();

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table, pageControls);

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

        // Pagination + search setup for returned/replaced orders (10 items per page)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        List<Reservation> sourceList = ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations().stream().filter(r -> r.getStatus().contains("REPLACED")).collect(java.util.stream.Collectors.toList())
        );
        List<Reservation> workingFiltered = new ArrayList<>(sourceList);

        HBox pageControls = new HBox(12);
        pageControls.setAlignment(Pos.CENTER);
        pageControls.setPadding(new Insets(12,0,0,0));

        Button prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label();
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        Button nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        pageControls.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // Row click handler
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

        // Update table with pagination and search
        Runnable updateTable = () -> {
            List<Reservation> display = new ArrayList<>(workingFiltered);
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String st = searchText.toLowerCase().trim();
                display = display.stream().filter(r -> {
                    String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                    if (orderId != null && orderId.toLowerCase().contains(st)) return true;
                    if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(st)) return true;
                    if (r.getBundleId() != null && r.getBundleId().toLowerCase().contains(st)) return true;
                    return false;
                }).collect(java.util.stream.Collectors.toList());
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) display.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;
            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, display.size());
            List<Reservation> pageItems = display.isEmpty() ? java.util.Collections.emptyList() : display.subList(start, end);
            table.setItems(FXCollections.observableArrayList(pageItems));

            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            pageLabel.setVisible(totalPages > 2);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };

        prevBtn.setOnAction(e -> { if (currentPage[0] > 1) { currentPage[0]--; updateTable.run(); } });
        nextBtn.setOnAction(e -> { int totalPages = Math.max(1, (int) Math.ceil((double) workingFiltered.size() / itemsPerPage)); if (currentPage[0] < totalPages) { currentPage[0]++; updateTable.run(); } });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage[0] = 1; updateTable.run(); });

        Runnable doRefresh = () -> {
            sourceList.clear();
            sourceList.addAll(ControllerUtils.getDeduplicatedReservations(
                reservationManager.getAllReservations().stream().filter(r -> r.getStatus().contains("REPLACED")).collect(java.util.stream.Collectors.toList())
            ));
            workingFiltered.clear(); workingFiltered.addAll(sourceList);
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
        };

        refreshBtn.setOnAction(e -> doRefresh.run());

        // Fixed row height to match stock logs
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        // Initial load
        workingFiltered.clear(); workingFiltered.addAll(sourceList);
        updateTable.run();

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table, pageControls);

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

        // Pagination + search setup for cancelled orders (10 items per page)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        List<Reservation> sourceList = ControllerUtils.getDeduplicatedReservations(
            reservationManager.getAllReservations().stream().filter(r -> "CANCELLED".equals(r.getStatus())).collect(java.util.stream.Collectors.toList())
        );
        List<Reservation> workingFiltered = new ArrayList<>(sourceList);

        HBox pageControls = new HBox(12);
        pageControls.setAlignment(Pos.CENTER);
        pageControls.setPadding(new Insets(12,0,0,0));

        Button prevBtn = new Button("← Previous");
        prevBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        javafx.scene.control.Label pageLabel = new javafx.scene.control.Label();
        pageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
        Button nextBtn = new Button("Next →");
        nextBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 12; -fx-cursor: hand;");
        pageControls.getChildren().addAll(prevBtn, pageLabel, nextBtn);

        // Row click handler
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

        // Update function
        Runnable updateTable = () -> {
            List<Reservation> display = new ArrayList<>(workingFiltered);
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String st = searchText.toLowerCase().trim();
                display = display.stream().filter(r -> {
                    String orderId = r.isPartOfBundle() ? r.getBundleId() : String.valueOf(r.getReservationId());
                    if (orderId != null && orderId.toLowerCase().contains(st)) return true;
                    if (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(st)) return true;
                    if (r.getBundleId() != null && r.getBundleId().toLowerCase().contains(st)) return true;
                    return false;
                }).collect(java.util.stream.Collectors.toList());
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) display.size() / itemsPerPage));
            if (currentPage[0] > totalPages) currentPage[0] = totalPages;
            int start = (currentPage[0] - 1) * itemsPerPage;
            int end = Math.min(start + itemsPerPage, display.size());
            List<Reservation> pageItems = display.isEmpty() ? java.util.Collections.emptyList() : display.subList(start, end);
            table.setItems(FXCollections.observableArrayList(pageItems));

            pageLabel.setText("Page " + currentPage[0] + " of " + totalPages);
            pageLabel.setVisible(totalPages > 2);
            prevBtn.setDisable(currentPage[0] <= 1);
            nextBtn.setDisable(currentPage[0] >= totalPages);
        };

        prevBtn.setOnAction(e -> { if (currentPage[0] > 1) { currentPage[0]--; updateTable.run(); } });
        nextBtn.setOnAction(e -> { int totalPages = Math.max(1, (int) Math.ceil((double) workingFiltered.size() / itemsPerPage)); if (currentPage[0] < totalPages) { currentPage[0]++; updateTable.run(); } });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> { currentPage[0] = 1; updateTable.run(); });

        Runnable doRefresh = () -> {
            sourceList.clear();
            sourceList.addAll(ControllerUtils.getDeduplicatedReservations(
                reservationManager.getAllReservations().stream().filter(r -> "CANCELLED".equals(r.getStatus())).collect(java.util.stream.Collectors.toList())
            ));
            workingFiltered.clear(); workingFiltered.addAll(sourceList);
            currentPage[0] = 1;
            searchField.clear();
            updateTable.run();
        };

        refreshBtn.setOnAction(e -> doRefresh.run());

        // Fixed row height
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        // Initial load
        workingFiltered.clear(); workingFiltered.addAll(sourceList);
        updateTable.run();

        VBox.setVgrow(table, Priority.ALWAYS);
        container.getChildren().addAll(actionBar, table, pageControls);

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

        // Get only items with the same name. Include 'STI Special' variants or cross-course variants when needed
        List<Item> allItems = inventoryManager.getAllItems();
        List<Item> sameItemVariants = allItems.stream()
            .filter(item -> item.getName().equals(originalItem.getItemName()) && (
                originalItem.getCourse().equals("STI Special") ||
                item.getCourse().equals(originalItem.getCourse()) ||
                item.getCourse().equals("STI Special")
            ))
            .collect(Collectors.toList());
        ObservableList<Item> itemList = FXCollections.observableArrayList(sameItemVariants);
        // Initialize filtered list with only in-stock variants
        ObservableList<Item> filteredList = FXCollections.observableArrayList();
        for (Item it : sameItemVariants) if (it.getQuantity() > 0) filteredList.add(it);

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

        // Header showing original return details (item being replaced and reason)
        VBox headerBox = new VBox(6);
        headerBox.setPadding(new Insets(6, 10, 6, 10));
        Label replacingLabel = new Label("Replacing: " + originalItem.getItemName() + " (" + originalItem.getSize() + ")");
        replacingLabel.setStyle("-fx-font-weight: bold;");
        Label reasonLabel = new Label("Reason: " + (originalItem.getReason() != null ? originalItem.getReason() : "N/A"));
        reasonLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        headerBox.getChildren().addAll(replacingLabel, reasonLabel);

        // Create container with header, search box and table
        VBox container = new VBox();
        container.getChildren().addAll(headerBox, searchBox, itemTable);
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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

