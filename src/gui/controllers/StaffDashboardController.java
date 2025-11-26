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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import gui.utils.AlertHelper;
import gui.utils.ControllerUtils;
import gui.utils.SceneManager;
import gui.utils.ThemeManager;
import gui.views.LoginView;
import inventory.InventoryManager;
import inventory.Item;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.ReservationManager;
import javafx.application.Platform;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import utils.DamagedStockTracker;
import utils.ReplacementTracker;
import utils.ReplacementTracker.ReplacementReason;
import utils.ReplacementTracker.ReplacementSummary;
import utils.StockReturnLogger;

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
     * Normalize course names for display (combine SHS variants)
     */
    private String normalizeCourseForDisplay(String course) {
        if (course == null || course.trim().isEmpty()) return "";
        String c = course.trim().toUpperCase();
        
        // Normalize SHS-related courses to "SHS"
        if (c.equals("ABM") || c.equals("STEM") || c.equals("HUMSS") || 
            c.equals("IT") || c.equals("T.O") || c.equals("TO") || 
            c.startsWith("TVL") || c.contains("TVL-")) {
            return "SHS";
        }
        
        // Group CS-related courses: BSCS, BSIT, BSCpE → BSCS/BSIT/BSCpE
        if (c.equals("BSCS") || c.equals("BSIT") || c.equals("BSCPE")) {
            return "BSCS/BSIT/BSCpE";
        }
        
        // Group business courses: BSA, BSBA → BSBA/BSA
        if (c.equals("BSA") || c.equals("BSBA")) {
            return "BSBA/BSA";
        }
        
        // Return original if already combined or other courses
        return course;
    }

    /**
     * Handle price change for an item
     */
    private void handleChangePriceForItem(Item item, Runnable refreshAction) {
        // Use a custom Dialog with explicit TextField so we can control focus and behavior.
        Dialog<Double> priceDialog = new Dialog<>();
        priceDialog.setTitle("Change Price");
        priceDialog.setHeaderText("Change price for: " + item.getName() + " (applies to all sizes)");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        priceDialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label currentLbl = new Label("Current Price: ₱" + String.format("%.2f", item.getPrice()));
        TextField priceField = new TextField(String.format("%.2f", item.getPrice()));
        priceField.setPromptText("New Price");

        grid.add(currentLbl, 0, 0);
        grid.add(priceField, 0, 1);

        priceDialog.getDialogPane().setContent(grid);

        // Request focus on the price field when the dialog is shown
        priceDialog.setOnShown(e -> Platform.runLater(() -> priceField.requestFocus()));

        priceDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okBtn) {
                try {
                    return Double.parseDouble(priceField.getText().trim());
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });

        priceDialog.showAndWait().ifPresent(newPrice -> {
            if (newPrice == null) {
                AlertHelper.showError("Invalid Input", "Please enter a valid numeric price.");
                return;
            }
            if (newPrice < 0) {
                AlertHelper.showError("Invalid Input", "Price cannot be negative!");
                return;
            }

            double oldPrice = item.getPrice();
            boolean success = inventoryManager.updateItemPriceByCode(item.getCode(), newPrice);
            if (success) {
                refreshAction.run();
                AlertHelper.showSuccess("Price Updated",
                    "Price updated successfully for all sizes!\n\n" +
                    "Item: " + item.getName() + "\n" +
                    "Old Price (example): ₱" + String.format("%.2f", oldPrice) + "\n" +
                    "New Price: ₱" + String.format("%.2f", newPrice));
            } else {
                AlertHelper.showError("Error", "Failed to update price!");
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
            "-fx-border-radius: 3px;" +
            "-fx-background-radius: 3px;" +
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
            "-fx-background-radius: 3px;" +
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

        // Filter Bar with Dropdowns
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setPadding(new Insets(8, 0, 8, 0));
        filterBar.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 12; -fx-background-radius: 3px;");

        // Status Filter
        // We'll append live counts to the Pickup/Replacement items but match by prefix when applying filters.
        int initialPickupBadge = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()).size();
        int initialReplacementBadge = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getReturnRequests()).size();
        ComboBox<String> statusFilter = new ComboBox<>(FXCollections.observableArrayList(
            "All Statuses",
            "Pending",
            "Approved",
            "Awaiting Pickup Request",
            "Awaiting Replacement Claim",
            "Replaced",
            "Cancelled",
            "Pickup Approvals (" + initialPickupBadge + ")",
            "Replacement Requests (" + initialReplacementBadge + ")"
        ));
        statusFilter.setValue("Pending");
        statusFilter.setPrefWidth(180);
        statusFilter.setPrefHeight(45);
        // Theme-aware styling matching inventory tab
        String fieldBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#000000ff";
        String fieldText = ThemeManager.isDarkMode() ? "white" : "#111827";
        // Use the same highlighted border style as the Inventory course filter so focus/border remains consistent
        String baseComboStyle =
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-control-inner-background: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: -color-accent-emphasis;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 0px 8px;" +
            "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
        statusFilter.setStyle(baseComboStyle);

        // Mark the control for dialog-like styling (global stylesheet is loaded by SceneManager)
        statusFilter.getStyleClass().add("dialog-combo");
        if (ThemeManager.isDarkMode()) statusFilter.getStyleClass().add("dark");

        // Listen for theme changes so the ComboBox updates immediately when user toggles dark mode
        ThemeManager.addThemeChangeListener(() -> Platform.runLater(() -> {
            String fb = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
            String ftext = ThemeManager.isDarkMode() ? "white" : "#111827";
            String updatedStyle =
                "-fx-font-size: 14px;" +
                "-fx-background-color: " + fb + ";" +
                "-fx-control-inner-background: " + fb + ";" +
                "-fx-text-fill: " + ftext + ";" +
                "-fx-border-color: -color-accent-emphasis;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 4px;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 0px 8px;" +
                "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
            statusFilter.setStyle(updatedStyle);
            if (ThemeManager.isDarkMode()) {
                if (!statusFilter.getStyleClass().contains("dark")) statusFilter.getStyleClass().add("dark");
            } else {
                statusFilter.getStyleClass().remove("dark");
            }
            // Force-reload the combobox stylesheet so CSS rules re-evaluate (fixes transient border removal)
            try {
                String cssPath = getClass().getResource("/gui/styles/combobox-dark.css").toExternalForm();
                if (statusFilter.getStylesheets().contains(cssPath)) {
                    statusFilter.getStylesheets().remove(cssPath);
                    statusFilter.getStylesheets().add(cssPath);
                }
            } catch (Exception ex) {
                // ignore
            }
            // Re-apply CSS and layout to ensure visual state updates immediately
            try {
                statusFilter.applyCss();
                statusFilter.requestLayout();
                if (statusFilter.getScene() != null && statusFilter.getScene().getRoot() != null) {
                    statusFilter.getScene().getRoot().applyCss();
                }
            } catch (Exception ex) {
                // ignore
            }
        }));

        // When the popup list is shown, ensure its background/text are updated immediately
        statusFilter.showingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(() -> {
                    try {
                        Node listView = statusFilter.lookup(".list-view");
                        if (listView != null) {
                            String lvBg = ThemeManager.isDarkMode() ? "-fx-background-color: rgba(255,255,255,0.04); -fx-text-fill: white;" : "-fx-background-color: -color-bg-default; -fx-text-fill: -color-fg-default;";
                            listView.setStyle(lvBg);
                        }
                    } catch (Exception ex) {
                        // ignore reflection/lookup issues
                    }
                });
            }
        });

        Button clearFilterBtn = new Button("Clear Filters");
        styleActionButton(clearFilterBtn);

        Button refreshBtn = new Button("🔄 Refresh");
        styleActionButton(refreshBtn);

        // Create small red badge labels for pickup and replacement counts
        int initialPickupCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()).size();
        int initialReplacementCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getReturnRequests()).size();
        
        // Badges for the Type filter to indicate pending actions
        Label pickupBadge = new Label(String.valueOf(initialPickupCount));
        pickupBadge.setVisible(initialPickupCount > 0);
        pickupBadge.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 10;");
        
        Label returnBadge = new Label(String.valueOf(initialReplacementCount));
        returnBadge.setVisible(initialReplacementCount > 0);
        returnBadge.setStyle("-fx-background-color: #CF222E; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 5; -fx-background-radius: 10;");

        // We can't easily attach badges to ComboBox items directly in standard JavaFX without a custom cell factory.
        // For now, we'll just show the counts in the stats cards at the top, which is already done.
        // Or we can append the count to the text in the ComboBox items dynamically.

        filterBar.getChildren().addAll(new Label("Status:"), statusFilter, clearFilterBtn, refreshBtn);

        // Create reservations table
        TableView<Reservation> table = new TableView<>();
        table.setStyle("-fx-background-color: -color-bg-subtle;");
        // Use constrained resize policy so columns behave predictably, then
        // pin a reasonable width for the actions column so both buttons fit.
        // Use the newer flex-last-column policy to avoid deprecated API warnings.
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Reservation, String> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, show the bundle ID as the order ID
                return new javafx.beans.property.SimpleStringProperty(r.getBundleId());
            }
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(r.getReservationId()));
        });
        idCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String orderId, boolean empty) {
                super.updateItem(orderId, empty);
                if (empty || orderId == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("📋 " + orderId);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        idCol.setPrefWidth(180);

        TableColumn<Reservation, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStudentName()));
        studentCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String studentName, boolean empty) {
                super.updateItem(studentName, empty);
                if (empty || studentName == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("👤 " + studentName);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        studentCol.setPrefWidth(160);

        TableColumn<Reservation, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.isPartOfBundle()) {
                // For bundles, show only bundle info without item name
                String bundleId = r.getBundleId();
                long itemCount = reservationManager.getAllReservations().stream()
                    .filter(res -> bundleId.equals(res.getBundleId()))
                    .count();
                return new javafx.beans.property.SimpleStringProperty(
                    "BUNDLE ORDER (" + itemCount + " items)");
            }
            return new javafx.beans.property.SimpleStringProperty(r.getItemName());
        });
        itemCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String itemName, boolean empty) {
                super.updateItem(itemName, empty);
                if (empty || itemName == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Reservation r = (getTableRow() != null) ? (Reservation) getTableRow().getItem() : null;
                    String icon = "👕";
                    if (r != null && r.isPartOfBundle()) {
                        icon = "📦";
                    }
                    setText(icon + " " + itemName);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        itemCol.setPrefWidth(220);

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
        sizeCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String size, boolean empty) {
                super.updateItem(size, empty);
                if (empty || size == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(size);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        sizeCol.setPrefWidth(80);

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
        qtyCol.setCellFactory(col -> new TableCell<Reservation, Integer>() {
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(qty));
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
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
                    setStyle("");
                } else {
                    setText(String.format("₱%.2f", price));
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        priceCol.setPrefWidth(100);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusCol.setPrefWidth(140);
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
                badge.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 6 10; -fx-background-radius: 3px;");

                if (s.contains("REPLACED")) {
                    badge.setText("REPLACED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #656D76; -fx-text-fill: white;");
                } else if (s.contains("COMPLETED")) {
                    badge.setText("COMPLETED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #1A7F37; -fx-text-fill: white;");
                } else if (s.contains("PICKUP REQUESTED") || (s.contains("REQUESTED") && s.contains("PICKUP"))) {
                    // Only treat explicit pickup-requested states as "PICKUP REQUESTED".
                    // This avoids matching awaiting-request states (e.g. "AWAITING PICKUP REQUEST").
                    badge.setText("PICKUP REQUESTED");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #0969DA; -fx-text-fill: white;");
                } else if (s.contains("AWAITING PICKUP REQUEST")) {
                    // Status when payment is complete but student hasn't requested pickup yet
                    badge.setText("AWAITING PICKUP REQUEST");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #6c757d; -fx-text-fill: white;");
                } else if (s.contains("PAID")) {
                    badge.setText("PAID");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #6c757d; -fx-text-fill: white;");
                } else if (s.contains("APPROVED FOR REPLACEMENT")) {
                    // Replacement approved but student hasn't claimed yet
                    badge.setText("AWAITING CLAIM");
                    badge.setStyle(badge.getStyle() + " -fx-background-color: #8250DF; -fx-text-fill: white;");
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

        TableColumn<Reservation, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(data -> {
            Reservation r = data.getValue();
            if (r.getReservationTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
                return new javafx.beans.property.SimpleStringProperty(r.getReservationTime().format(formatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        dateCol.setCellFactory(col -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(date);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        dateCol.setPrefWidth(140);

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
                // Exception: APPROVED FOR REPLACEMENT needs a "Confirm Claim" action
                boolean isAwaitingClaim = status.contains("APPROVED FOR REPLACEMENT");
                boolean isFinalState = (status.contains("APPROVED") || status.contains("PAID") || status.contains("COMPLETED") || status.contains("REPLACED")) && !isAwaitingClaim;
                if (isFinalState) {
                    setGraphic(null);
                    return;
                }
                
                // Handle APPROVED FOR REPLACEMENT - show Confirm Claim button
                if (isAwaitingClaim) {
                    approveBtn.setText("📦");
                    approveBtn.setTooltip(new javafx.scene.control.Tooltip("Confirm Student Claimed Replacement"));
                    approveBtn.setStyle("-fx-background-color: #8250DF; -fx-text-fill: white; -fx-cursor: hand;");
                    rejectBtn.setText("✗");
                    rejectBtn.setTooltip(new javafx.scene.control.Tooltip("Cancel Replacement"));
                    approveBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleConfirmReplacementClaim(current, table);
                    });
                    rejectBtn.setOnAction(e -> {
                        Reservation current = (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size())
                            ? getTableView().getItems().get(getIndex())
                            : (getTableRow() != null ? getTableRow().getItem() : null);
                        if (current != null) handleCancelReplacementClaim(current, table);
                    });
                    setGraphic(buttons);
                    return;
                }

                boolean showPending = status.contains("PENDING") || (isBundle && bundleHasPending);
                boolean showReplacement = status.contains("REPLACEMENT") || (isBundle && bundleHasReplacement);
                boolean showPickup = status.contains("PICKUP") || (isBundle && bundleHasPickup);

                LOGGER.fine("[StaffDashboard] actions.updateForReservation resId=" + res.getReservationId() + " status='" + res.getStatus() + "' isBundle=" + isBundle);

                if (showPending) {
                    approveBtn.setText("✓");
                    approveBtn.setTooltip(new javafx.scene.control.Tooltip("Approve Reservation"));
                    rejectBtn.setText("✗");
                    rejectBtn.setTooltip(new javafx.scene.control.Tooltip("Reject Reservation"));
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
                    approveBtn.setText("✓");
                    approveBtn.setTooltip(new javafx.scene.control.Tooltip("Approve Replacement"));
                    rejectBtn.setText("✗");
                    rejectBtn.setTooltip(new javafx.scene.control.Tooltip("Reject Return"));
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
                    approveBtn.setText("✓");
                    approveBtn.setTooltip(new javafx.scene.control.Tooltip("Approve Pickup"));
                    rejectBtn.setText("✗");
                    rejectBtn.setTooltip(new javafx.scene.control.Tooltip("Reject Pickup"));
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
        actionsCol.setPrefWidth(120);
        actionsCol.setMinWidth(110);
        actionsCol.setMaxWidth(160);

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, statusCol, dateCol, actionsCol);
        

        // Pagination + search setup (10 items per page, prev/next, page label visible when pages > 2)
        final int itemsPerPage = 10;
        final int[] currentPage = new int[] { 1 };

        // Default to showing pending reservations
        List<Reservation> pendingReservations = new ArrayList<>(reservationManager.getPendingReservations());

        List<Reservation> allReservations = new ArrayList<>(ControllerUtils.getDeduplicatedReservations(pendingReservations));
        List<Reservation> workingFiltered = new ArrayList<>(allReservations); // current filtered set from status buttons

        // Track current filter for refresh logic (default to PENDING)
        // final String[] currentFilter = {"PENDING"}; // Removed as we use ComboBoxes now

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
            // Always show the page indicator so staff know which page they're on
            pageLabel.setVisible(true);
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
        Runnable applyFilters = () -> {
            // Normalize status selection: remove any appended counts in parentheses
            String rawStatus = statusFilter.getValue();
            String status = rawStatus == null ? "All Statuses" : rawStatus.split(" \\(")[0].trim();

            // Highlight active filters - blue border with consistent styling
            String activeBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
            String activeText = ThemeManager.isDarkMode() ? "white" : "#111827";
            String activeComboStyle =
                "-fx-font-size: 13px;" +
                "-fx-background-color: " + activeBg + ";" +
                "-fx-control-inner-background: " + activeBg + ";" +
                "-fx-text-fill: " + activeText + ";" +
                "-fx-border-color: -color-accent-emphasis;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 4px;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 0px 8px;" +
                "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
            statusFilter.setStyle(activeComboStyle);
            
            List<Reservation> filtered = new ArrayList<>();
            
            // Filter by Status (including the new "Type" options which are now part of status)
            if ("All Statuses".equals(status)) {
                filtered = reservationManager.getAllReservations();
            } else if ("Awaiting Pickup Request".equals(status)) {
                // Show items where payment is complete but student hasn't requested pickup
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "AWAITING PICKUP REQUEST".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Pending".equals(status)) {
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Approved".equals(status)) {
                // Show reservations that are explicitly in an APPROVED state (e.g. "APPROVED - WAITING FOR PAYMENT",
                // "APPROVED FOR PICKUP"). Do NOT include all paid or completed items here (those are their own states).
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> {
                        String s = r.getStatus();
                        if (s == null) return false;
                        s = s.toUpperCase();
                        return s.contains("APPROVED") && !s.contains("PAID");
                    })
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Awaiting Replacement Claim".equals(status)) {
                // Show replacements approved but not yet claimed by student
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "APPROVED FOR REPLACEMENT".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Returned".equals(status) || "Replaced".equals(status)) {
                // legacy label support: treat "Returned" as "Replaced"
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> {
                        String s = r.getStatus();
                        return s != null && s.toUpperCase().contains("REPLACED");
                    })
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Cancelled".equals(status)) {
                filtered = reservationManager.getAllReservations().stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            } else if ("Pickup Approvals".equals(status)) {
                filtered = reservationManager.getPickupRequestsAwaitingApproval();
            } else if ("Replacement Requests".equals(status)) {
                filtered = reservationManager.getReturnRequests();
            } else {
                filtered = reservationManager.getAllReservations();
            }
            
            allReservations.clear(); 
            allReservations.addAll(ControllerUtils.getDeduplicatedReservations(filtered));
            workingFiltered.clear(); 
            workingFiltered.addAll(allReservations);
            
            currentPage[0] = 1;
            searchField.clear();
            
            // Show actions column only if we are in a state that allows actions
            // (Pending, Pickup Approvals, Replacement Requests, Awaiting Replacement Claim)
            boolean actionsVisible = "Pending".equals(status) || 
                                     "Pickup Approvals".equals(status) || 
                                     "Replacement Requests".equals(status) ||
                                     "Awaiting Replacement Claim".equals(status);
            
            actionsCol.setVisible(actionsVisible);
            
            updateTable.run();
            
            // Update badges
            int updatedPickupCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getPickupRequestsAwaitingApproval()).size();
            pickupBadge.setText(String.valueOf(updatedPickupCount));
            pickupBadge.setVisible(updatedPickupCount > 0);
            
            int updatedReturnCount = (int) ControllerUtils.getDeduplicatedReservations(reservationManager.getReturnRequests()).size();
            returnBadge.setText(String.valueOf(updatedReturnCount));
            returnBadge.setVisible(updatedReturnCount > 0);

                // Refresh the ComboBox items so the Pickup/Replacement entries show live counts.
                // Do this asynchronously to avoid modifying the ComboBox items while its selection event is being processed.
                final String prevSelection = rawStatus == null ? "All Statuses" : rawStatus;
                javafx.application.Platform.runLater(() -> {
                    javafx.collections.ObservableList<String> comboItems = FXCollections.observableArrayList(
                        "All Statuses",
                        "Pending",
                        "Approved",
                        "Awaiting Pickup Request",
                        "Awaiting Replacement Claim",
                        "Replaced",
                        "Cancelled",
                        "Pickup Approvals (" + updatedPickupCount + ")",
                        "Replacement Requests (" + updatedReturnCount + ")"
                    );
                    // Temporarily detach the action handler to avoid re-entrancy while we replace items and restore selection.
                    javafx.event.EventHandler<javafx.event.ActionEvent> oldHandler = statusFilter.getOnAction();
                    try {
                        statusFilter.setOnAction(null);
                        statusFilter.setItems(comboItems);
                        // Preserve previous selection (match by prefix before '(' ) without firing the handler.
                        String prevBase = prevSelection.split(" \\(")[0].trim();
                        boolean matched = false;
                        for (String it : comboItems) {
                            if (it.startsWith(prevBase)) {
                                statusFilter.getSelectionModel().select(it);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            statusFilter.getSelectionModel().selectFirst();
                        }
                    } finally {
                        // Restore the original handler after selection is set
                        statusFilter.setOnAction(oldHandler);
                    }
                });
        };

        statusFilter.setOnAction(e -> applyFilters.run());
        
        clearFilterBtn.setOnAction(e -> {
            statusFilter.setValue("All Statuses");
            applyFilters.run();
        });

        refreshBtn.setOnAction(e -> {
            // Reload reservations from persistent storage in case external edits were made
            try { reservationManager.refresh(); } catch (Exception ex) { /* ignore refresh errors */ }
            applyFilters.run();
        });

        // Set the refresh callback for when items are approved/rejected
        this.refreshCallback = () -> {
            // trigger a refresh equivalent to clicking refresh
            refreshBtn.fire();
        };

        // Initially load pending reservations into the table (so view shows Pending by default)
        applyFilters.run();

        // Set fixed row height to match stock logs
        final double rowHeight = 65;
        table.setFixedCellSize(rowHeight);
        final double headerReserve = 56;
        table.setPrefHeight(itemsPerPage * rowHeight + headerReserve);

        VBox.setVgrow(table, Priority.ALWAYS);
        // Do not add the statsBox to the UI (hide the top summary boxes)
        // Wrap page controls in a full-width HBox so the pagination buttons are centered
        HBox pageControlsWrapper = new HBox(pageControls);
        pageControlsWrapper.setAlignment(Pos.CENTER);
        pageControlsWrapper.setMaxWidth(Double.MAX_VALUE);
        container.getChildren().addAll(searchBar, filterBar, table, pageControlsWrapper);

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

        // Dialog styling is provided globally by SceneManager; just add semantic classes
        dialog.getDialogPane().getStyleClass().add("dialog-root");
        if (ThemeManager.isDarkMode()) dialog.getDialogPane().getStyleClass().add("dark");

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


        // If student included a reschedule note, show it to staff for context
        if (reservation.getRescheduleNote() != null && !reservation.getRescheduleNote().trim().isEmpty()) {
            VBox noteBox = new VBox(6);
            noteBox.setStyle("-fx-background-color: #FFF7E6; -fx-padding: 10; -fx-background-radius: 6;");
            javafx.scene.control.Label noteHeader = new javafx.scene.control.Label("Reschedule Note (from student)");
            noteHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            javafx.scene.control.Label noteLabel = new javafx.scene.control.Label(reservation.getRescheduleNote());
            noteLabel.setWrapText(true);
            noteBox.getChildren().addAll(noteHeader, noteLabel);
            content.getChildren().add(noteBox);
        }
            // Show replacement note if exists
            if (reservation.getReplacementNote() != null && !reservation.getReplacementNote().isEmpty()) {
                VBox noteBox = new VBox(6);
                noteBox.setStyle("-fx-background-color: #FFF8C5; -fx-padding: 8; -fx-background-radius: 4; -fx-border-color: #E6C07A; -fx-border-width: 1;");
                javafx.scene.control.Label noteHeader = new javafx.scene.control.Label("Replacement Note");
                noteHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #6F4400;");
                javafx.scene.control.Label noteText = new javafx.scene.control.Label(reservation.getReplacementNote());
                noteText.setWrapText(true);
                noteBox.getChildren().addAll(noteHeader, noteText);
                itemsSection.getChildren().add(noteBox);
            }
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
    @SuppressWarnings("unused")
    private void showBundleItemsDialog(Reservation reservation) {
        javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Bundle Order Details");
        dialog.setHeaderText("Bundle ID: " + reservation.getBundleId());

        javafx.scene.control.ButtonType closeButton = javafx.scene.control.ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        // Dialog styling is provided globally by SceneManager; just add semantic classes
        dialog.getDialogPane().getStyleClass().add("dialog-root");
        if (ThemeManager.isDarkMode()) dialog.getDialogPane().getStyleClass().add("dark");

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
        String header;
        if (reservation.isPartOfBundle()) {
            String bundleId = reservation.getBundleId();
            long itemCount = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId.equals(r.getBundleId()))
                .count();
            header = "Reject BUNDLE ORDER for: " + reservation.getStudentName() +
                     "\nBundle contains " + itemCount + " item type(s)";
        } else {
            header = "Reject reservation for: " + reservation.getStudentName();
        }

        String reason = AlertHelper.showInputDialog("Reject Reservation", header, "Reason:");

        if (reason != null && !reason.isEmpty()) {
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
            
            // Calculate correct item count and total refund based on partial replacements
            int totalItemsToReplace = 0;
            totalRefund = 0.0;
            for (Reservation item : itemsToReturn) {
                // Check if this is a partial replacement request
                String reasonText = item.getReason() != null ? item.getReason() : "";
                int qtyToReplace = item.getQuantity(); // default to full quantity
                
                if (reasonText.startsWith("Partial Replacement (")) {
                    try {
                        int start = reasonText.indexOf("(") + 1;
                        int end = reasonText.indexOf(" of ");
                        String qtyStr = reasonText.substring(start, end).trim();
                        qtyToReplace = Integer.parseInt(qtyStr);
                    } catch (Exception e) {
                        // If parsing fails, use full quantity
                        qtyToReplace = item.getQuantity();
                    }
                }
                
                totalItemsToReplace += qtyToReplace;
                // Calculate refund based on quantity being replaced
                double pricePerUnit = item.getTotalPrice() / item.getQuantity();
                totalRefund += pricePerUnit * qtyToReplace;
            }
            
            itemDescription = "Bundle Order (" + totalItemsToReplace + " item(s) from " + itemsToReturn.size() + " reservation(s))";
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
                // Parse quantity to replace for partial replacements
                int qtyToReplace = item.getQuantity();
                String reasonText = item.getReason() != null ? item.getReason() : "";
                if (reasonText.startsWith("Partial Replacement (")) {
                    try {
                        int start = reasonText.indexOf("(") + 1;
                        int end = reasonText.indexOf(" of ");
                        String qtyStr = reasonText.substring(start, end).trim();
                        qtyToReplace = Integer.parseInt(qtyStr);
                    } catch (Exception e) {
                        qtyToReplace = item.getQuantity();
                    }
                }
                
                double pricePerUnit = item.getTotalPrice() / item.getQuantity();
                double refundForThisItem = pricePerUnit * qtyToReplace;
                
                message.append("• ").append(item.getItemName()).append(" - ").append(item.getSize())
                       .append(" (").append(qtyToReplace);
                if (qtyToReplace < item.getQuantity()) {
                    message.append(" of ").append(item.getQuantity());
                }
                message.append("x) - ₱").append(String.format("%.2f", refundForThisItem)).append("\n");
            }
        } else {
            message.append("Item: ").append(itemDescription).append("\n");
            message.append("Quantity: ").append(reservation.getQuantity()).append("x\n");
        }
        
        message.append("\nTotal Refund Amount: ₱").append(String.format("%.2f", totalRefund)).append("\n\n");
        message.append("Reason: ").append(reservation.getReason() != null ? reservation.getReason() : "N/A").append("\n\n");
        message.append("Select replacement item for each.");

        // Build a richer confirmation dialog with boxed information and colored labels
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Approve Replacement");
        confirmDialog.setHeaderText(null);

        ButtonType okType = ButtonType.OK;
        ButtonType cancelType = ButtonType.CANCEL;
        confirmDialog.getDialogPane().getButtonTypes().addAll(cancelType, okType);

        // Content layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.setStyle("-fx-background-color: transparent;");

        // Boxed info area
        VBox box = new VBox(8);
        box.setPadding(new Insets(12));
        String boxBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.03)" : "#F8FAFF";
        box.setStyle("-fx-background-color: " + boxBg + "; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: rgba(0,0,0,0.06); -fx-border-width: 1;");

        Label titleLbl = new Label("Replacement Request");
        titleLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");

        Label studentLbl = new Label("Student: " + reservation.getStudentName());
        studentLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");

        // Top card: only show student name here; student ID/order/date are in the topInfo block below.
        Label refundLbl = new Label("Total Refund Amount: ₱" + String.format("%.2f", totalRefund));
        refundLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");

        // Simplify reason text: strip any leading 'Replacement requested' phrasing and show concise reason.
        String rawReason = reservation.getReason() != null ? reservation.getReason().trim() : "N/A";
        String simpleReason = rawReason;
        if (rawReason.toLowerCase().startsWith("replacement requested")) {
            int idx = rawReason.lastIndexOf("Reason:");
            if (idx >= 0) {
                simpleReason = rawReason.substring(idx + "Reason:".length()).trim();
            } else {
                // remove the prefix
                int dash = rawReason.indexOf('-');
                if (dash >= 0 && dash + 1 < rawReason.length()) simpleReason = rawReason.substring(dash + 1).trim();
                else simpleReason = rawReason;
            }
        }
        // Capitalize first letter
        if (simpleReason != null && !simpleReason.isEmpty()) {
            simpleReason = simpleReason.substring(0, 1).toUpperCase() + simpleReason.substring(1);
        }

        Label reasonLbl = new Label("Reason: " + (simpleReason != null ? simpleReason : "N/A"));
        reasonLbl.setWrapText(true);
        reasonLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        
        // Extract and display student's preferred pickup time if provided
        String preferredPickupTime = null;
        if (rawReason != null && rawReason.contains("[Preferred pickup:")) {
            int startIdx = rawReason.indexOf("[Preferred pickup:");
            int endIdx = rawReason.indexOf("]", startIdx);
            if (startIdx >= 0 && endIdx > startIdx) {
                preferredPickupTime = rawReason.substring(startIdx + "[Preferred pickup:".length(), endIdx).trim();
            }
        }
        
        VBox preferredTimeBox = null;
        if (preferredPickupTime != null && !preferredPickupTime.isEmpty()) {
            preferredTimeBox = new VBox(4);
            preferredTimeBox.setStyle(
                "-fx-background-color: rgba(9, 105, 218, 0.1);" +
                "-fx-border-color: #0969DA;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 8;"
            );
            Label prefTimeTitle = new Label("🕐 Student's Preferred Pickup Time:");
            prefTimeTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969DA; -fx-font-size: 12px;");
            Label prefTimeContent = new Label(preferredPickupTime);
            prefTimeContent.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px;");
            prefTimeContent.setWrapText(true);
            preferredTimeBox.getChildren().addAll(prefTimeTitle, prefTimeContent);
        }
        
        // Extract and display image proof if attached
        String imagePath = null;
        if (rawReason != null && rawReason.contains("[Image proof attached:")) {
            int startIdx = rawReason.indexOf("[Image proof attached:");
            int endIdx = rawReason.indexOf("]", startIdx);
            if (startIdx >= 0 && endIdx > startIdx) {
                imagePath = rawReason.substring(startIdx + "[Image proof attached:".length(), endIdx).trim();
            }
        }
        
        VBox imageProofBox = null;
        final String finalImagePath = imagePath;
        if (imagePath != null && !imagePath.isEmpty()) {
            imageProofBox = new VBox(8);
            imageProofBox.setPadding(new Insets(8, 0, 0, 0));
            
            Label imageProofLabel = new Label("📷 Proof of Damage/Issue:");
            imageProofLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #666666;");
            
            try {
                java.io.File imgFile = new java.io.File(imagePath);
                if (imgFile.exists()) {
                    HBox imageControls = new HBox(10);
                    imageControls.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    imageControls.setPadding(new Insets(4, 0, 0, 0));
                    
                    Label imagePathLabel = new Label("📁 " + imgFile.getName());
                    imagePathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
                    imagePathLabel.setWrapText(true);
                    
                    javafx.scene.control.Button showImageBtn = new javafx.scene.control.Button("🔍 Show Image");
                    showImageBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #0969DA; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;");
                    showImageBtn.setOnMouseEntered(e -> showImageBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #0860CA; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;"));
                    showImageBtn.setOnMouseExited(e -> showImageBtn.setStyle("-fx-font-size: 11px; -fx-background-color: #0969DA; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;"));
                    showImageBtn.setOnAction(e -> {
                        openImageModal(finalImagePath, imgFile.getName());
                    });
                    
                    imageControls.getChildren().addAll(imagePathLabel, showImageBtn);
                    
                    imageProofBox.getChildren().addAll(imageProofLabel, imageControls);
                } else {
                    Label notFoundLabel = new Label("⚠️ Image file not found: " + imgFile.getName());
                    notFoundLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CF222E;");
                    notFoundLabel.setWrapText(true);
                    imageProofBox.getChildren().addAll(imageProofLabel, notFoundLabel);
                }
            } catch (Exception ex) {
                Label errorLabel = new Label("⚠️ Error loading image");
                errorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #CF222E;");
                imageProofBox.getChildren().addAll(imageProofLabel, errorLabel);
            }
        }

        box.getChildren().addAll(titleLbl, studentLbl, new javafx.scene.control.Separator(), refundLbl, reasonLbl);
        if (preferredTimeBox != null) {
            box.getChildren().add(preferredTimeBox);
        }
        if (imageProofBox != null) {
            box.getChildren().add(imageProofBox);
        }

        // Dialog styling is provided globally by SceneManager; just add semantic classes
        confirmDialog.getDialogPane().getStyleClass().add("dialog-root");
        if (ThemeManager.isDarkMode()) confirmDialog.getDialogPane().getStyleClass().add("dark");

        // Add semantic style classes to top card elements
        box.getStyleClass().add("top-card");
        titleLbl.getStyleClass().add("top-title");
        studentLbl.getStyleClass().add("top-title");
        refundLbl.getStyleClass().add("top-muted");
        reasonLbl.getStyleClass().add("small-muted");
        grid.getStyleClass().add("scroll-area");

        // Instruction
        Label instr = new Label("Select replacement item for each.");
        instr.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");
        instr.getStyleClass().add("section-label");

        grid.add(box, 0, 0);
        grid.add(instr, 0, 1);

        confirmDialog.getDialogPane().setContent(grid);

        // Create selection dialog so staff can pick all replacement items in one place
        confirmDialog.getDialogPane().getButtonTypes().clear();
        ButtonType approveType = new ButtonType("Approve Replacement", ButtonBar.ButtonData.OK_DONE);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, approveType);

        // Top info: student, student id, order id/date
        VBox topInfo = new VBox(6);
        topInfo.setPadding(new Insets(6, 0, 6, 0));
        Label studentIdLbl = new Label("Student ID: " + (reservation.getStudentId() != null ? reservation.getStudentId() : "N/A"));
        studentIdLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        Label orderRefLbl = new Label("Order: " + (reservation.isPartOfBundle() ? (reservation.getBundleId() != null ? reservation.getBundleId() : "Bundle") : String.valueOf(reservation.getReservationId())));
        orderRefLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
        String orderTime = reservation.getFormattedTime();
        // If bundle, try to show earliest time across items
        if (reservation.isPartOfBundle()) {
            try {
                java.time.LocalDateTime earliest = itemsToReturn.stream()
                    .map(Reservation::getReservationTime)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder())
                    .orElse(reservation.getReservationTime());
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
                orderTime = earliest.format(fmt);
            } catch (Exception ex) {
                // fall back to reservation.getFormattedTime()
            }
        }
        Label orderTimeLbl = new Label("Ordered: " + orderTime);
        orderTimeLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");

        topInfo.getChildren().addAll(studentIdLbl, orderRefLbl, orderTimeLbl);

        // Make dialog larger and responsive
        javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
        double screenWidth = screen.getVisualBounds().getWidth();
        double screenHeight = screen.getVisualBounds().getHeight();
        double dialogWidth = Math.min(1000, screenWidth * 0.72);
        double dialogHeight = Math.min(720, screenHeight * 0.78);
        confirmDialog.getDialogPane().setPrefSize(dialogWidth, dialogHeight);

        // Create replacement selector area (stack original above replacement)
        VBox selectors = new VBox(14);
        selectors.setPadding(new Insets(8, 0, 8, 0));

        // Ensure damaged/priority items appear first
        try {
            itemsToReturn.sort((a, b) -> {
                boolean ad = a.getReason() != null && a.getReason().toLowerCase().contains("damaged");
                boolean bd = b.getReason() != null && b.getReason().toLowerCase().contains("damaged");
                if (ad == bd) return 0;
                return ad ? -1 : 1; // damaged first
            });
        } catch (Exception ex) {
            // ignore sort errors
        }

        // Map each reservation to its toggle-group of replacement size options
        Map<Reservation, javafx.scene.control.ToggleGroup> selectionMap = new java.util.LinkedHashMap<>();
        // Keep a parallel map of variants per reservation to retrieve Item objects from selected toggles
        Map<Reservation, List<Item>> variantsMap = new java.util.HashMap<>();
        // Keep optional notes per reservation when staff choose a different size
        Map<Reservation, javafx.scene.control.TextArea> notesMap = new java.util.HashMap<>();

        for (Reservation it : itemsToReturn) {
            VBox itemRow = new VBox(8);
            itemRow.setAlignment(Pos.CENTER_LEFT);
            itemRow.setPadding(new Insets(10));
            itemRow.setStyle("-fx-background-color: " + (ThemeManager.isDarkMode() ? "rgba(255,255,255,0.05)" : "#F8FAFF") + "; -fx-border-color: rgba(0,0,0,0.1); -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

            VBox origBox = new VBox(6);
            Label origLabel = new Label("📦 Original Item:");
            origLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666666;");
            
            // Parse quantity to replace for partial replacements
            int qtyToReplace = it.getQuantity();
            String reasonText = it.getReason() != null ? it.getReason() : "";
            if (reasonText.startsWith("Partial Replacement (")) {
                try {
                    int start = reasonText.indexOf("(") + 1;
                    int end = reasonText.indexOf(" of ");
                    String qtyStr = reasonText.substring(start, end).trim();
                    qtyToReplace = Integer.parseInt(qtyStr);
                } catch (Exception e) {
                    qtyToReplace = it.getQuantity();
                }
            }
            
            String quantityDisplay = "x" + qtyToReplace;
            if (qtyToReplace < it.getQuantity()) {
                quantityDisplay = "x" + qtyToReplace + " of " + it.getQuantity();
            }
            
            // Display item name and details in separate lines for better readability
            Label origName = new Label(it.getItemName());
            origName.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B5FFF;");
            
            Label origDetails = new Label("Size: " + it.getSize() + "  •  Quantity: " + quantityDisplay);
            origDetails.setStyle("-fx-font-size: 12px; -fx-font-weight: normal; -fx-text-fill: #333333;");
            
            origBox.getChildren().addAll(origLabel, origName, origDetails);

            // Replacement label
            Label replLabel = new Label("🔄 Replacement Item:");
            replLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666666;");

            // Instead of a repetitive dropdown per size, show the same item name and provide
            // per-size radio buttons so staff select one size for replacement.
            List<Item> allItems = inventoryManager.getAllItems();
            
            // Debug: Show what we're searching for
            System.out.println("DEBUG: Looking for replacement for: " + it.getItemName());
            System.out.println("DEBUG: Original course: " + it.getCourse());
            System.out.println("DEBUG: Original size: " + it.getSize());
            System.out.println("DEBUG: Total items in inventory: " + allItems.size());
            
            // First, find items with matching name
            List<Item> nameMatches = allItems.stream()
                .filter(item -> item.getName().equals(it.getItemName()))
                .collect(Collectors.toList());
            System.out.println("DEBUG: Items with matching name: " + nameMatches.size());
            
            // Then filter by course and stock
            List<Item> rawCandidates = allItems.stream()
                .filter(item -> item.getName().equals(it.getItemName()))
                .filter(item -> {
                    // Special case: SHS items are available to all SHS courses (STEM, HUMSS, ABM, etc.)
                    // Normalize reservation/item course and allow prefix matching so values like
                    // "TVL-CA" or "CUL ART" are treated as SHS-related courses.
                    String courseRes = it.getCourse() != null ? it.getCourse().trim().toUpperCase() : "";
                    String itemCourse = item.getCourse() != null ? item.getCourse().trim().toUpperCase() : "";
                    boolean isSHSItem = "SHS".equals(itemCourse);
                    boolean isSHSReservation = !courseRes.isEmpty() && (
                        courseRes.startsWith("SHS") ||
                        courseRes.startsWith("STEM") ||
                        courseRes.startsWith("HUMSS") ||
                        courseRes.startsWith("ABM") ||
                        courseRes.startsWith("GAS") ||
                        courseRes.startsWith("TVL") ||
                        courseRes.startsWith("CUL")
                    );

                    if (isSHSItem && isSHSReservation) {
                        return true; // SHS items match all SHS-related courses
                    }
                    
                    // Course matching: allow "STI Special", exact match, or when either side is a combined label
                    boolean courseMatch = it.getCourse().equals("STI Special") ||
                        item.getCourse().equals("STI Special") ||
                        // allow combined labels like "BSBA/BSA" to match items labeled "BSBA" or "BSA"
                        (it.getCourse() != null && item.getCourse() != null && (
                            it.getCourse().equalsIgnoreCase(item.getCourse()) ||
                            (it.getCourse().contains("/") && java.util.Arrays.stream(it.getCourse().split("/")).anyMatch(p -> p.trim().equalsIgnoreCase(item.getCourse()))) ||
                            (item.getCourse().contains("/") && java.util.Arrays.stream(item.getCourse().split("/")).anyMatch(p -> p.trim().equalsIgnoreCase(it.getCourse())))
                        ));
                    if (!courseMatch) {
                        System.out.println("DEBUG: Rejected due to course mismatch - Item course: " + item.getCourse() + " vs Reservation course: " + it.getCourse());
                    }
                    return courseMatch;
                })
                .filter(item -> {
                    boolean hasStock = item.getQuantity() > 0;
                    if (!hasStock) {
                        System.out.println("DEBUG: Rejected due to no stock - " + item.getName() + " (" + item.getSize() + ") - Stock: " + item.getQuantity());
                    }
                    return hasStock;
                })
                .collect(Collectors.toList());
            
            System.out.println("DEBUG: Final candidates after filtering: " + rawCandidates.size());

            // Group candidates by size to avoid repetitive radio entries (aggregate stock per size)
            java.util.Map<String, java.util.List<Item>> groupedBySize = rawCandidates.stream()
                .collect(Collectors.groupingBy(Item::getSize, LinkedHashMap::new, Collectors.toList()));

            // Build a deduplicated candidate list where each size is represented by the variant with highest stock
            List<Item> candidates = new ArrayList<>();
            Map<String, Integer> aggregatedStockBySize = new HashMap<>();
            for (Map.Entry<String, java.util.List<Item>> entry : groupedBySize.entrySet()) {
                String sizeKey = entry.getKey();
                List<Item> listForSize = entry.getValue();
                int sumQty = listForSize.stream().mapToInt(Item::getQuantity).sum();
                aggregatedStockBySize.put(sizeKey, sumQty);
                // choose representative item (highest quantity) as the item used for replacement
                Item rep = listForSize.stream().max(Comparator.comparingInt(Item::getQuantity)).orElse(listForSize.get(0));
                candidates.add(rep);
            }

            // Keep variants for lookup when approving (store deduplicated candidates)
            variantsMap.put(it, candidates);

            VBox sizesBox = new VBox(6);
            sizesBox.setPadding(new Insets(6, 0, 6, 8));
            sizesBox.setStyle("-fx-background-color: transparent;");
            javafx.scene.control.ToggleGroup tg = new javafx.scene.control.ToggleGroup();

            if (candidates.isEmpty()) {
                Label none = new Label("No replacement available");
                none.setStyle("-fx-font-size:12px; -fx-text-fill:#999999;");
                sizesBox.getChildren().add(none);
            } else {
                // Create a radio button per available size. Preselect same-size when possible.
                for (Item cand : candidates) {
                    int totalStock = aggregatedStockBySize.getOrDefault(cand.getSize(), cand.getQuantity());
                    javafx.scene.control.RadioButton rb = new javafx.scene.control.RadioButton(cand.getSize() + " — Stock: " + totalStock);
                    rb.setToggleGroup(tg);
                    rb.setUserData(cand);
                    String textColor = ThemeManager.isDarkMode() ? "#E6E6E6" : "#333333";
                    rb.setStyle("-fx-font-size:12px; -fx-text-fill:" + textColor + "; -fx-background-color: transparent;");
                    if (cand.getSize().equals(it.getSize())) rb.setSelected(true);
                    sizesBox.getChildren().add(rb);
                }

                // Add a mismatch indicator and an optional note field
                Label mismatchLbl = new Label("");
                mismatchLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#C86900; -fx-font-weight:bold;");
                javafx.scene.control.TextArea noteArea = new javafx.scene.control.TextArea();
                noteArea.setPromptText("Note (optional): explain why a different size was chosen");
                noteArea.setPrefRowCount(2);
                noteArea.setWrapText(true);
                noteArea.setMaxWidth(420);

                // update mismatch label when selection changes
                tg.selectedToggleProperty().addListener((obs, oldT, newT) -> {
                    if (newT != null && newT.getUserData() instanceof Item) {
                        Item sel = (Item) newT.getUserData();
                        if (!sel.getSize().equals(it.getSize())) {
                            mismatchLbl.setText("⚠ Selected size differs from original (" + it.getSize() + ")");
                            mismatchLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#C86900; -fx-font-weight:bold; -fx-background-color:#FFF4E5; -fx-padding:4; -fx-border-radius:4; -fx-background-radius:4;");
                        } else {
                            mismatchLbl.setText("");
                            mismatchLbl.setStyle("-fx-font-size:11px; -fx-text-fill:#C86900; -fx-font-weight:bold;");
                        }
                    } else {
                        mismatchLbl.setText("");
                    }
                });

                // 'Add note' quick link to focus the note area
                javafx.scene.control.Hyperlink addNoteLink = new javafx.scene.control.Hyperlink("Add note");
                addNoteLink.setOnAction(ae -> Platform.runLater(() -> noteArea.requestFocus()));

                sizesBox.getChildren().addAll(mismatchLbl, noteArea, addNoteLink);
                notesMap.put(it, noteArea);
            }

            selectionMap.put(it, tg);

            itemRow.getChildren().addAll(origBox, replLabel, sizesBox);
            selectors.getChildren().add(itemRow);
        }

        // Instruction and actions row
        Label confirmNote = new Label("Selected replacements will be applied when you click Approve Replacement.");
        confirmNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        Button viewOrderBtn = new Button("View Full Order");
        viewOrderBtn.setOnAction(evt -> showOrderDetailsDialog(reservation));

        // Combine selectors + notes + date/time into a single scrollable section
        VBox middleContent = new VBox(12);
        middleContent.setPadding(new Insets(4, 0, 4, 0));
        // we'll add selectors and the notes/date controls into middleContent below

        // --- Pickup date controls (placed after replacement selectors) ---
        // Pickup Date (global) - allowed: today .. today + N days
        final int N = 3; // configurable window (default 3 days)
        java.time.LocalDate minDate = java.time.LocalDate.now();
        java.time.LocalDate maxDate = minDate.plusDays(N);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        Label pickupLabel = new Label("Pickup Date (Today - " + maxDate.format(dateFmt) + "): ");
        pickupLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        DatePicker pickupDatePicker = new DatePicker(minDate);
        pickupDatePicker.setPrefWidth(240);
        pickupDatePicker.setEditable(false);

        // --- Time picker controls (editable spinners + AM/PM) ---
        Label timeLabel = new Label("Pickup Time:");
        timeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        javafx.collections.ObservableList<Integer> allowedHours = FXCollections.observableArrayList(8,9,10,11,12,1,2,3,4,5);
        javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<Integer> hourFactory =
            new javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<>(allowedHours);
        javafx.scene.control.Spinner<Integer> hourSpinner = new javafx.scene.control.Spinner<>();
        hourSpinner.setValueFactory(hourFactory);
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(70);
        hourSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.valueOf(object);
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) return hourFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    if (allowedHours.contains(parsed)) return parsed;
                    if (parsed >= 8 && parsed <= 17) {
                        int mapped = parsed > 12 ? parsed - 12 : parsed;
                        if (allowedHours.contains(mapped)) return mapped;
                    }
                } catch (NumberFormatException ignored) {}
                return hourFactory.getValue();
            }
        });

        javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory =
            new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15);
        javafx.scene.control.Spinner<Integer> minuteSpinner = new javafx.scene.control.Spinner<>();
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(70);
        minuteSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) return minuteFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    int snapped = Math.max(0, Math.min(45, ((parsed + 7) / 15) * 15));
                    if (snapped == 60) snapped = 45;
                    return snapped;
                } catch (NumberFormatException ignored) {
                    return minuteFactory.getValue();
                }
            }
        });

        ComboBox<String> amPmPicker = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        amPmPicker.setEditable(true);
        amPmPicker.setValue("AM");

        Label colonLabel = new Label(":");
        colonLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox timeBox = new HBox(6, hourSpinner, colonLabel, minuteSpinner, amPmPicker);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        // === END TIME PICKER FOR REPLACEMENT ===
        Label endTimeLabel = new Label("Pickup Time (Until):");
        endTimeLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        
        // End time spinners - default to 5 PM (closing time)
        javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<Integer> endHourFactory =
            new javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<>(FXCollections.observableArrayList(8,9,10,11,12,1,2,3,4,5));
        endHourFactory.setValue(5); // Default to 5 PM
        javafx.scene.control.Spinner<Integer> endHourSpinner = new javafx.scene.control.Spinner<>();
        endHourSpinner.setValueFactory(endHourFactory);
        endHourSpinner.setEditable(true);
        endHourSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.valueOf(object);
            }
            @Override
            public Integer fromString(String string) {
                if (string == null) return endHourFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    if (allowedHours.contains(parsed)) return parsed;
                    if (parsed >= 8 && parsed <= 17) {
                        int mapped = parsed > 12 ? parsed - 12 : parsed;
                        if (allowedHours.contains(mapped)) return mapped;
                    }
                } catch (NumberFormatException ignored) {}
                return endHourFactory.getValue();
            }
        });
        endHourSpinner.setPrefWidth(70);

        javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory endMinuteFactory =
            new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15);
        javafx.scene.control.Spinner<Integer> endMinuteSpinner = new javafx.scene.control.Spinner<>();
        endMinuteSpinner.setValueFactory(endMinuteFactory);
        endMinuteSpinner.setEditable(true);
        endMinuteSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }
            @Override
            public Integer fromString(String string) {
                if (string == null) return endMinuteFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    int snapped = Math.max(0, Math.min(45, ((parsed + 7) / 15) * 15));
                    if (snapped == 60) snapped = 45;
                    return snapped;
                } catch (NumberFormatException ignored) {
                    return endMinuteFactory.getValue();
                }
            }
        });
        endMinuteSpinner.setPrefWidth(70);

        ComboBox<String> endAmPmPicker = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        endAmPmPicker.setEditable(true);
        endAmPmPicker.setValue("PM"); // Default to PM for end time

        Label endColonLabel = new Label(":");
        endColonLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox endTimeBox = new HBox(6, endHourSpinner, endColonLabel, endMinuteSpinner, endAmPmPicker);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        // Auto-adjust AM/PM based on selected hour so resulting 24-hour time falls within 08:00-17:00
        hourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int h = newVal;
            if (h >= 8 && h <= 11) {
                amPmPicker.setValue("AM");
            } else {
                amPmPicker.setValue("PM");
            }
        });

        // If the hour is 5 PM, clamp minutes to 00 to enforce the 5:00 PM cutoff
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Integer m = newVal;
                Integer h = hourSpinner.getValue();
                String ap = amPmPicker.getValue();
                if (h != null && ap != null && h == 5 && "PM".equalsIgnoreCase(ap) && m != null && m > 0) {
                    minuteSpinner.getValueFactory().setValue(0);
                }
            } catch (Exception ignored) {}
        });

        amPmPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String ap = newVal.trim().toUpperCase();
            int h = hourSpinner.getValue();
            if (ap.equals("AM") && (h < 8 || h > 11)) {
                amPmPicker.setValue("PM");
            } else if (ap.equals("PM") && !(h == 12 || (h >= 1 && h <= 5))) {
                if (h >= 8 && h <= 11) amPmPicker.setValue("AM");
            }
        });

        // End time listeners - auto-adjust AM/PM based on hour
        endHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int h = newVal;
            if (h >= 8 && h <= 11) {
                endAmPmPicker.setValue("AM");
            } else {
                endAmPmPicker.setValue("PM");
            }
        });
        endMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Integer m = newVal;
                Integer h = endHourSpinner.getValue();
                String ap = endAmPmPicker.getValue();
                if (h != null && ap != null && h == 5 && "PM".equalsIgnoreCase(ap) && m != null && m > 0) {
                    endMinuteSpinner.getValueFactory().setValue(0);
                }
            } catch (Exception ignored) {}
        });
        endAmPmPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String ap = newVal.trim().toUpperCase();
            int h = endHourSpinner.getValue();
            if (ap.equals("AM") && (h < 8 || h > 11)) {
                endAmPmPicker.setValue("PM");
            } else if (ap.equals("PM") && !(h == 12 || (h >= 1 && h <= 5))) {
                if (h >= 8 && h <= 11) endAmPmPicker.setValue("AM");
            }
        });

        // Store hours label
        Label hoursInfoLabel = new Label("⏰ Store Hours: 8:00 AM - 5:00 PM");
        hoursInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        // Helper text under date picker
        Label dateHelper = new Label("Allowed: Today - " + maxDate.format(dateFmt));
        dateHelper.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        // Inline error label (hidden by default)
        Label dateErrorLabel = new Label();
        dateErrorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #CF222E;");
        dateErrorLabel.setVisible(false);

        // Disable days outside the allowed window in the calendar UI
        pickupDatePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) return;
                if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-text-fill: #bdbdbd;");
                    setTooltip(new javafx.scene.control.Tooltip("Not available: pickups allowed only through " + maxDate.format(dateFmt)));
                }
            }
        });

        VBox notesAndDateBox = new VBox(8, pickupLabel, pickupDatePicker, 
            timeLabel, timeBox, endTimeLabel, endTimeBox, hoursInfoLabel, dateHelper, dateErrorLabel);
        notesAndDateBox.setPadding(new Insets(8, 0, 8, 0));

        VBox contentBox = new VBox(16);
        // Add selectors and the notes/date into the single middle scroll area
        middleContent.getChildren().addAll(selectors, notesAndDateBox, confirmNote, viewOrderBtn);
        ScrollPane middleScroll = new ScrollPane(middleContent);
        middleScroll.setFitToWidth(true);
        middleScroll.setPrefViewportHeight(Math.min(420, dialogHeight * 0.55));
        middleScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        middleScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        middleScroll.setPannable(true);
        middleScroll.getStyleClass().add("scroll-section");

        contentBox.getChildren().addAll(box, topInfo, new javafx.scene.control.Separator(), middleScroll);
        contentBox.setPadding(new Insets(12));

        confirmDialog.getDialogPane().setContent(contentBox);

        // Show dialog and process selection
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == approveType) {
            // Validate global pickup date chosen and ensure it lies within allowed window
            java.time.LocalDate selectedDate = pickupDatePicker.getValue();
            if (selectedDate == null) {
                dateErrorLabel.setText("Please select a pickup date.");
                dateErrorLabel.setVisible(true);
                return;
            }
            if (selectedDate.isBefore(minDate) || selectedDate.isAfter(maxDate)) {
                dateErrorLabel.setText("Please select a pickup date between " + minDate.format(dateFmt) + " and " + maxDate.format(dateFmt) + ".");
                dateErrorLabel.setVisible(true);
                return;
            }
            dateErrorLabel.setVisible(false);

            // Ensure every replacement item has a selected replacement before proceeding
            boolean allSelected = true;
            for (Map.Entry<Reservation, javafx.scene.control.ToggleGroup> entry : selectionMap.entrySet()) {
                if (entry.getValue().getSelectedToggle() == null) {
                    allSelected = false;
                    break;
                }
            }
            if (!allSelected) {
                AlertHelper.showError("Missing selection", "Please select replacement item for all listed items.");
                return;
            }

            // Read selected time controls and validate business hours (08:00 - 17:00)
            int selHour12 = hourSpinner.getValue();
            int selMinute = minuteSpinner.getValue();
            String selAp = amPmPicker.getValue() == null ? "AM" : amPmPicker.getValue().trim().toUpperCase();
            int selHour24 = "AM".equals(selAp) ? (selHour12 == 12 ? 0 : selHour12) : (selHour12 == 12 ? 12 : selHour12 + 12);

            if (selHour24 < 8 || selHour24 > 17 || (selHour24 == 17 && selMinute > 0)) {
                AlertHelper.showError("Invalid Time", "Start time is outside business hours (8:00 AM - 5:00 PM). Please choose 5:00 PM or earlier.");
                return;
            }

            java.time.LocalDateTime scheduledStart = java.time.LocalDateTime.of(selectedDate, java.time.LocalTime.of(selHour24, selMinute));
            if (scheduledStart.isBefore(java.time.LocalDateTime.now())) {
                AlertHelper.showError("Invalid Time", "Selected start date/time is in the past.");
                return;
            }
            
            // Read end time controls and validate
            int endHour12 = endHourSpinner.getValue();
            int endMinuteVal = endMinuteSpinner.getValue();
            String endAp = endAmPmPicker.getValue() == null ? "PM" : endAmPmPicker.getValue().trim().toUpperCase();
            int endHour24 = "AM".equals(endAp) ? (endHour12 == 12 ? 0 : endHour12) : (endHour12 == 12 ? 12 : endHour12 + 12);

            if (endHour24 < 8 || endHour24 > 17 || (endHour24 == 17 && endMinuteVal > 0)) {
                AlertHelper.showError("Invalid Time", "End time is outside business hours (8:00 AM - 5:00 PM). Please choose 5:00 PM or earlier.");
                return;
            }

            java.time.LocalDateTime scheduledEnd = java.time.LocalDateTime.of(selectedDate, java.time.LocalTime.of(endHour24, endMinuteVal));
            if (!scheduledEnd.isAfter(scheduledStart)) {
                AlertHelper.showError("Invalid Time Range", "End time must be after start time.");
                return;
            }
            
            // If any selected replacement changes size and no note was provided, ask for confirmation
            List<Reservation> mismatchesNoNote = new ArrayList<>();
            for (Map.Entry<Reservation, javafx.scene.control.ToggleGroup> entry : selectionMap.entrySet()) {
                Reservation item = entry.getKey();
                javafx.scene.control.ToggleGroup tg = entry.getValue();
                javafx.scene.control.Toggle selected = tg.getSelectedToggle();
                if (selected != null && selected.getUserData() instanceof Item) {
                    Item sel = (Item) selected.getUserData();
                    if (!sel.getSize().equals(item.getSize())) {
                        javafx.scene.control.TextArea note = notesMap.get(item);
                        if (note == null || note.getText().trim().isEmpty()) {
                            mismatchesNoNote.add(item);
                        }
                    }
                }
            }

            if (!mismatchesNoNote.isEmpty()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Confirm size changes");
                confirmAlert.setHeaderText("Some replacements change size");
                confirmAlert.setContentText("There are " + mismatchesNoNote.size() + " replacement(s) where the selected size differs from the original and no note was provided. Continue anyway?");
                Optional<ButtonType> r = confirmAlert.showAndWait();
                if (!r.isPresent() || r.get() != ButtonType.OK) {
                    return; // abort approval so staff can add notes or change selection
                }
            }

            boolean allSuccess = true;
            int successCount = 0;

            for (Map.Entry<Reservation, javafx.scene.control.ToggleGroup> entry : selectionMap.entrySet()) {
                Reservation item = entry.getKey();
                javafx.scene.control.ToggleGroup tg = entry.getValue();
                javafx.scene.control.Toggle selected = tg.getSelectedToggle();
                if (selected != null && selected.getUserData() instanceof Item) {
                    Item selectedReplacement = (Item) selected.getUserData();
                    // Retrieve optional note provided by staff for this item
                    javafx.scene.control.TextArea noteArea = notesMap.get(item);
                    String noteTxt = (noteArea != null) ? noteArea.getText().trim() : "";

                    boolean success = reservationManager.approveReplacementWithItem(
                        item.getReservationId(),
                        selectedReplacement.getCode(),
                        selectedReplacement.getName(),
                        selectedReplacement.getSize(),
                        noteTxt,
                        scheduledStart,
                        scheduledEnd
                    );
                    if (success) {
                        successCount++;
                        
                        // Log ALL replacements to ReplacementTracker for tracking by reason category
                        String reasonText = noteTxt.isEmpty() ? item.getReason() : noteTxt;
                        String imagePathFromReason = extractImagePathFromReason(item.getReason());
                        
                        // Log to new ReplacementTracker (tracks all replacement reasons)
                        ReplacementTracker.logReplacement(
                            item.getReservationId(),
                            item.getStudentName(),
                            item.getStudentId(),
                            item.getItemCode(),
                            item.getItemName(),
                            item.getSize(),
                            selectedReplacement.getCode(),
                            selectedReplacement.getName(),
                            selectedReplacement.getSize(),
                            reasonText,
                            imagePathFromReason,
                            "Staff"
                        );
                        
                        // Also log to legacy DamagedStockTracker if damage-related (for backward compatibility)
                        if (isDamageReason(reasonText)) {
                            DamagedStockTracker.logDamagedReplacement(
                                item.getReservationId(),
                                item.getStudentName(),
                                item.getStudentId(),
                                item.getItemCode(),
                                item.getItemName(),
                                item.getSize(),
                                selectedReplacement.getCode(),
                                selectedReplacement.getName(),
                                selectedReplacement.getSize(),
                                reasonText,
                                imagePathFromReason,
                                "Staff"
                            );
                        }
                    } else {
                        allSuccess = false;
                    }
                } else {
                    allSuccess = false; // missing selection
                }
            }

            if (allSuccess) {
                if (refreshCallback != null) refreshCallback.run();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Replacement Approved");
                successAlert.setHeaderText(reservation.isPartOfBundle() ? 
                    "Replacement approved for all " + successCount + " items!" : 
                    "Replacement approved!");
                DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
                DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");
                successAlert.setContentText(
                    "Pickup scheduled for:\n" +
                    scheduledStart.format(dateTimeFmt) + "\n" +
                    "From " + scheduledStart.format(timeFmt) + " to " + scheduledEnd.format(timeFmt) + "\n\n" +
                    "⏳ Awaiting student to claim the replacement.\n" +
                    "Stock will be updated when student picks up the item.\n\n" +
                    "The student has been notified and will see this in their notification bell (🔔)."
                );
                successAlert.showAndWait();
            } else if (successCount > 0) {
                if (refreshCallback != null) refreshCallback.run();
                AlertHelper.showWarning("Partial Success", "Replacement approved for " + successCount + " out of " + itemsToReturn.size() + " items.\nPrevious items are back in inventory.");
            } else {
                AlertHelper.showError("Error", "Failed to approve replacement. Please ensure replacement items are selected and have available stock.");
            }
        }
    }

    /**
     * Handle reject return request
     */
    private void handleRejectReturn(Reservation reservation, TableView<Reservation> table) {
        String reason = AlertHelper.showInputDialog("Reject Return", "Reject return request for: " + reservation.getStudentName(), "Reason for rejection:");
        if (reason != null && !reason.isEmpty()) {
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
    }

    /**
     * Handle confirm replacement claim - when student picks up their approved replacement
     */
    private void handleConfirmReplacementClaim(Reservation reservation, TableView<Reservation> table) {
        // Build confirmation message
        StringBuilder message = new StringBuilder();
        message.append("Confirm replacement claim for:\n\n");
        message.append("Student: ").append(reservation.getStudentName()).append("\n");
        message.append("Student ID: ").append(reservation.getStudentId()).append("\n\n");
        
        if (reservation.isPartOfBundle()) {
            String bundleId = reservation.getBundleId();
            java.util.List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(r -> bundleId != null && bundleId.equals(r.getBundleId()))
                .filter(r -> "APPROVED FOR REPLACEMENT".equals(r.getStatus()))
                .collect(java.util.stream.Collectors.toList());
            
            message.append("Items being claimed:\n");
            for (Reservation item : bundleItems) {
                message.append("  • ").append(item.getItemName())
                       .append(" (").append(item.getSize()).append(")\n");
                if (item.getReplacementItemName() != null) {
                    message.append("    → Replacement: ").append(item.getReplacementItemName())
                           .append(" (").append(item.getReplacementSize()).append(")\n");
                }
            }
        } else {
            message.append("Original Item: ").append(reservation.getItemName())
                   .append(" (").append(reservation.getSize()).append(")\n");
            if (reservation.getReplacementItemName() != null) {
                message.append("Replacement Item: ").append(reservation.getReplacementItemName())
                       .append(" (").append(reservation.getReplacementSize()).append(")\n");
            }
        }
        
        message.append("\n⚠️ This will:\n");
        message.append("  • Deduct replacement item from stock\n");
        message.append("  • Mark reservation as REPLACED (completed)\n");
        message.append("  • Log to replacement tracker\n");
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Replacement Claim");
        confirmAlert.setHeaderText("Student picking up replacement item");
        confirmAlert.setContentText(message.toString());
        
        ButtonType confirmBtn = new ButtonType("Confirm Claim", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmAlert.getButtonTypes().setAll(confirmBtn, cancelBtn);
        
        java.util.Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == confirmBtn) {
            boolean allSuccess = true;
            int successCount = 0;
            
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                java.util.List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                    .filter(r -> bundleId != null && bundleId.equals(r.getBundleId()))
                    .filter(r -> "APPROVED FOR REPLACEMENT".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                
                for (Reservation item : bundleItems) {
                    boolean success = reservationManager.completeReplacementClaim(item.getReservationId(), null);
                    if (success) {
                        successCount++;
                    } else {
                        allSuccess = false;
                    }
                }
            } else {
                allSuccess = reservationManager.completeReplacementClaim(reservation.getReservationId(), null);
                if (allSuccess) successCount = 1;
            }
            
            if (allSuccess && successCount > 0) {
                if (refreshCallback != null) refreshCallback.run();
                AlertHelper.showSuccess("Success", 
                    "✅ Replacement claim confirmed!\n\n" +
                    successCount + " item(s) marked as REPLACED.\n" +
                    "Stock has been updated."
                );
            } else if (successCount > 0) {
                if (refreshCallback != null) refreshCallback.run();
                AlertHelper.showWarning("Partial Success", 
                    "Claim confirmed for " + successCount + " items.\n" +
                    "Some items failed to process."
                );
            } else {
                AlertHelper.showError("Error", "Failed to confirm replacement claim. Please try again.");
            }
        }
    }

    /**
     * Handle cancel replacement claim - when staff needs to cancel an approved replacement
     */
    private void handleCancelReplacementClaim(Reservation reservation, TableView<Reservation> table) {
        String reason = AlertHelper.showInputDialog(
            "Cancel Replacement", 
            "Cancel approved replacement for: " + reservation.getStudentName(), 
            "Reason for cancellation:"
        );
        
        if (reason != null && !reason.isEmpty()) {
            boolean allSuccess = true;
            
            if (reservation.isPartOfBundle()) {
                String bundleId = reservation.getBundleId();
                java.util.List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                    .filter(r -> bundleId != null && bundleId.equals(r.getBundleId()))
                    .filter(r -> "APPROVED FOR REPLACEMENT".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
                
                for (Reservation item : bundleItems) {
                    // Use cancelApprovedReplacement which handles status change and save
                    reservationManager.cancelApprovedReplacement(item.getReservationId(), reason);
                }
            } else {
                reservationManager.cancelApprovedReplacement(reservation.getReservationId(), reason);
            }
            
            if (allSuccess) {
                if (refreshCallback != null) refreshCallback.run();
                AlertHelper.showSuccess("Success", 
                    "Replacement cancelled.\n" +
                    "Status reverted to REPLACEMENT REQUESTED.\n" +
                    "Staff can re-process the request."
                );
            } else {
                AlertHelper.showError("Error", "Failed to cancel replacement.");
            }
        }
    }

    /**
     * Handle approve pickup request
     */
    private void handleApprovePickup(Reservation reservation, TableView<Reservation> table) {
        // Create date picker dialog
        Dialog<LocalDate> dateDialog = new Dialog<>();
        dateDialog.setTitle("Set Pickup Date");
        dateDialog.setHeaderText("Approve pickup for: " + reservation.getStudentName());
        
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        dateDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
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
        
        Label infoLabel = new Label(
            "Item: " + itemInfo + "\n" +
            "Total: ₱" + String.format("%.2f", reservation.getTotalPrice())
        );
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 13px;");
        
        Label dateLabel = new Label("Pickup Date:");
        dateLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        // Limit pickup dates to a 3-day window (today .. today + 3) for consistency
        java.time.LocalDate minDate = java.time.LocalDate.now();
        final int N_window = 3;
        java.time.LocalDate maxDate = minDate.plusDays(N_window);
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        DatePicker datePicker = new DatePicker(minDate);
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) return;
                if (date.isBefore(minDate) || date.isAfter(maxDate)) {
                    setDisable(true);
                    setStyle("-fx-text-fill: #bdbdbd;");
                    setTooltip(new javafx.scene.control.Tooltip("Not available: pickups allowed only through " + maxDate.format(dateFmt)));
                }
            }
        });
        datePicker.setPrefWidth(200);
        
        Label timeLabel = new Label("Pickup Time (Start):");
        timeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        // Time picker: editable Spinners for hour (1-12) and minute (00,15,30,45), plus editable AM/PM ComboBox
        // Allowed hour values mapped to business hours: 8,9,10,11,12,1,2,3,4,5
        javafx.collections.ObservableList<Integer> allowedHours = FXCollections.observableArrayList(8,9,10,11,12,1,2,3,4,5);
        javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<Integer> hourFactory =
            new javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<>(allowedHours);
        javafx.scene.control.Spinner<Integer> hourSpinner = new javafx.scene.control.Spinner<>();
        hourSpinner.setValueFactory(hourFactory);
        hourSpinner.setEditable(true);
        // Converter to safely parse typed input into an allowed hour value
        hourSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.valueOf(object);
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) return hourFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    if (allowedHours.contains(parsed)) return parsed;
                    // map 24-hour-like input into 12-hour range when reasonable
                    if (parsed >= 8 && parsed <= 17) {
                        int mapped = parsed > 12 ? parsed - 12 : parsed;
                        if (allowedHours.contains(mapped)) return mapped;
                    }
                } catch (NumberFormatException ignored) {}
                return hourFactory.getValue();
            }
        });
        hourSpinner.setPrefWidth(70);

        javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory =
            new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15);
        javafx.scene.control.Spinner<Integer> minuteSpinner = new javafx.scene.control.Spinner<>();
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setEditable(true);
        // Converter to safely parse typed input into a minute value (0,15,30,45)
        minuteSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }

            @Override
            public Integer fromString(String string) {
                if (string == null) return minuteFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    // snap to nearest 15-minute increment within 0-45
                    int snapped = Math.max(0, Math.min(45, ((parsed + 7) / 15) * 15));
                    if (snapped == 60) snapped = 45;
                    return snapped;
                } catch (NumberFormatException ignored) {
                    return minuteFactory.getValue();
                }
            }
        });
        minuteSpinner.setPrefWidth(70);

        ComboBox<String> amPmPicker = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        amPmPicker.setEditable(true);
        amPmPicker.setValue("AM");

        Label colonLabel = new Label(":");
        colonLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox timeBox = new HBox(5, hourSpinner, colonLabel, minuteSpinner, amPmPicker);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        // === END TIME PICKER ===
        Label endTimeLabel = new Label("Pickup Time (Until):");
        endTimeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
        
        // End time spinners - default to 5 PM (closing time)
        javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<Integer> endHourFactory =
            new javafx.scene.control.SpinnerValueFactory.ListSpinnerValueFactory<>(FXCollections.observableArrayList(8,9,10,11,12,1,2,3,4,5));
        endHourFactory.setValue(5); // Default to 5 PM
        javafx.scene.control.Spinner<Integer> endHourSpinner = new javafx.scene.control.Spinner<>();
        endHourSpinner.setValueFactory(endHourFactory);
        endHourSpinner.setEditable(true);
        endHourSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.valueOf(object);
            }
            @Override
            public Integer fromString(String string) {
                if (string == null) return endHourFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    if (allowedHours.contains(parsed)) return parsed;
                    if (parsed >= 8 && parsed <= 17) {
                        int mapped = parsed > 12 ? parsed - 12 : parsed;
                        if (allowedHours.contains(mapped)) return mapped;
                    }
                } catch (NumberFormatException ignored) {}
                return endHourFactory.getValue();
            }
        });
        endHourSpinner.setPrefWidth(70);

        javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory endMinuteFactory =
            new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15);
        javafx.scene.control.Spinner<Integer> endMinuteSpinner = new javafx.scene.control.Spinner<>();
        endMinuteSpinner.setValueFactory(endMinuteFactory);
        endMinuteSpinner.setEditable(true);
        endMinuteSpinner.getValueFactory().setConverter(new javafx.util.StringConverter<Integer>() {
            @Override
            public String toString(Integer object) {
                return object == null ? "" : String.format("%02d", object);
            }
            @Override
            public Integer fromString(String string) {
                if (string == null) return endMinuteFactory.getValue();
                try {
                    int parsed = Integer.parseInt(string.trim());
                    int snapped = Math.max(0, Math.min(45, ((parsed + 7) / 15) * 15));
                    if (snapped == 60) snapped = 45;
                    return snapped;
                } catch (NumberFormatException ignored) {
                    return endMinuteFactory.getValue();
                }
            }
        });
        endMinuteSpinner.setPrefWidth(70);

        ComboBox<String> endAmPmPicker = new ComboBox<>(FXCollections.observableArrayList("AM", "PM"));
        endAmPmPicker.setEditable(true);
        endAmPmPicker.setValue("PM"); // Default to PM for end time

        Label endColonLabel = new Label(":");
        endColonLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox endTimeBox = new HBox(5, endHourSpinner, endColonLabel, endMinuteSpinner, endAmPmPicker);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        // Store hours label to display business hours info
        Label hoursInfoLabel = new Label("⏰ Store Hours: 8:00 AM - 5:00 PM");
        hoursInfoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-fg-muted;");
        
        // Auto-adjust AM/PM based on selected hour so resulting 24-hour time falls within 08:00-17:00
        hourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int h = newVal;
            if (h >= 8 && h <= 11) {
                amPmPicker.setValue("AM");
            } else {
                amPmPicker.setValue("PM");
            }
            // If hour is 5 PM, ensure minutes are 0 (store closes at 5:00 PM sharp)
            if (h == 5 && "PM".equalsIgnoreCase(amPmPicker.getValue())) {
                minuteSpinner.getValueFactory().setValue(0);
            }
        });
        // If the hour is 5 PM, clamp minutes to 00 to enforce the 5:00 PM cutoff (store closes at 5:00 PM)
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Integer m = newVal;
                Integer h = hourSpinner.getValue();
                String ap = amPmPicker.getValue();
                if (h != null && ap != null && h == 5 && "PM".equalsIgnoreCase(ap) && m != null && m > 0) {
                    // Store closes at 5:00 PM - snap back to 00 minutes
                    Platform.runLater(() -> minuteSpinner.getValueFactory().setValue(0));
                }
            } catch (Exception ignored) {
            }
        });
        // If user edits AM/PM manually, ensure the combination remains valid
        amPmPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String ap = newVal.trim().toUpperCase();
            int h = hourSpinner.getValue();
            if (ap.equals("AM") && (h < 8 || h > 11)) {
                amPmPicker.setValue("PM");
            } else if (ap.equals("PM") && !(h == 12 || (h >= 1 && h <= 5))) {
                if (h >= 8 && h <= 11) amPmPicker.setValue("AM");
            }
            // If switching to PM with hour 5, ensure minutes are 0
            if (ap.equals("PM") && h == 5) {
                minuteSpinner.getValueFactory().setValue(0);
            }
        });
        
        // End time listeners - auto-adjust AM/PM based on hour
        endHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            int h = newVal;
            if (h >= 8 && h <= 11) {
                endAmPmPicker.setValue("AM");
            } else {
                endAmPmPicker.setValue("PM");
            }
            // If hour is 5 PM, ensure minutes are 0 (store closes at 5:00 PM sharp)
            if (h == 5 && "PM".equalsIgnoreCase(endAmPmPicker.getValue())) {
                endMinuteSpinner.getValueFactory().setValue(0);
            }
        });
        endMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                Integer m = newVal;
                Integer h = endHourSpinner.getValue();
                String ap = endAmPmPicker.getValue();
                if (h != null && ap != null && h == 5 && "PM".equalsIgnoreCase(ap) && m != null && m > 0) {
                    Platform.runLater(() -> endMinuteSpinner.getValueFactory().setValue(0));
                }
            } catch (Exception ignored) {
            }
        });
        endAmPmPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String ap = newVal.trim().toUpperCase();
            int h = endHourSpinner.getValue();
            if (ap.equals("AM") && (h < 8 || h > 11)) {
                endAmPmPicker.setValue("PM");
            } else if (ap.equals("PM") && !(h == 12 || (h >= 1 && h <= 5))) {
                if (h >= 8 && h <= 11) endAmPmPicker.setValue("AM");
            }
            if (ap.equals("PM") && h == 5) {
                endMinuteSpinner.getValueFactory().setValue(0);
            }
        });
        
        grid.add(infoLabel, 0, 0, 2, 1);
        
        // Show student's preferred pickup time note if available
        int nextRow = 1;
        String studentNote = reservation.getReason();
        if (studentNote != null && studentNote.contains("Preferred pickup time:")) {
            String preferredTime = studentNote.replace("Preferred pickup time:", "").trim();
            VBox noteBox = new VBox(4);
            noteBox.setStyle(
                "-fx-background-color: rgba(9, 105, 218, 0.1);" +
                "-fx-border-color: #0969DA;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 8;"
            );
            Label noteTitle = new Label("📝 Student's Preferred Time:");
            noteTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969DA; -fx-font-size: 12px;");
            Label noteContent = new Label(preferredTime);
            noteContent.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px;");
            noteContent.setWrapText(true);
            noteBox.getChildren().addAll(noteTitle, noteContent);
            grid.add(noteBox, 0, nextRow, 2, 1);
            nextRow++;
        }
        
        grid.add(new javafx.scene.control.Separator(), 0, nextRow, 2, 1);
        nextRow++;
        grid.add(dateLabel, 0, nextRow);
        grid.add(datePicker, 1, nextRow);
        nextRow++;
        grid.add(timeLabel, 0, nextRow);
        grid.add(timeBox, 1, nextRow);
        nextRow++;
        grid.add(endTimeLabel, 0, nextRow);
        grid.add(endTimeBox, 1, nextRow);
        nextRow++;
        grid.add(hoursInfoLabel, 0, nextRow, 2, 1);
        
        dateDialog.getDialogPane().setContent(grid);
        
        final String[] selectedTime = new String[1];
        final String[] selectedEndTime = new String[1];
        dateDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                String h = String.valueOf(hourSpinner.getValue());
                String m = String.format("%02d", minuteSpinner.getValue());
                String ap = amPmPicker.getValue();
                selectedTime[0] = h + ":" + m + " " + ap;
                
                String eh = String.valueOf(endHourSpinner.getValue());
                String em = String.format("%02d", endMinuteSpinner.getValue());
                String eap = endAmPmPicker.getValue();
                selectedEndTime[0] = eh + ":" + em + " " + eap;
                return datePicker.getValue();
            }
            return null;
        });

        dateDialog.showAndWait().ifPresent(pickupDate -> {
            // Validate selected times are within business hours and not in the past
            if (selectedTime[0] == null || selectedEndTime[0] == null) {
                AlertHelper.showError("Invalid Time", "Please select valid start and end times.");
                return;
            }
            
            int startHour24, startMinute, endHour24, endMinute;
            try {
                // Parse start time
                String[] parts = selectedTime[0].split("[: ]"); // [HH, mm, AM/PM]
                int hour12 = Integer.parseInt(parts[0]);
                startMinute = Integer.parseInt(parts[1]);
                String ampm = parts[2];
                startHour24 = "AM".equals(ampm) ? (hour12 == 12 ? 0 : hour12) : (hour12 == 12 ? 12 : hour12 + 12);
                
                // Parse end time
                String[] endParts = selectedEndTime[0].split("[: ]");
                int endHour12 = Integer.parseInt(endParts[0]);
                endMinute = Integer.parseInt(endParts[1]);
                String endAmpm = endParts[2];
                endHour24 = "AM".equals(endAmpm) ? (endHour12 == 12 ? 0 : endHour12) : (endHour12 == 12 ? 12 : endHour12 + 12);
                
                // Business hours: 8:00 AM to 5:00 PM
                if (startHour24 < 8 || startHour24 > 17 || (startHour24 == 17 && startMinute > 0)) {
                    AlertHelper.showError("Invalid Start Time", "Start time is outside store hours.\n\n⏰ Store Hours: 8:00 AM - 5:00 PM");
                    return;
                }
                if (endHour24 < 8 || endHour24 > 17 || (endHour24 == 17 && endMinute > 0)) {
                    AlertHelper.showError("Invalid End Time", "End time is outside store hours.\n\n⏰ Store Hours: 8:00 AM - 5:00 PM");
                    return;
                }
                
                // Validate end time is after start time
                int startTotalMinutes = startHour24 * 60 + startMinute;
                int endTotalMinutes = endHour24 * 60 + endMinute;
                if (endTotalMinutes <= startTotalMinutes) {
                    AlertHelper.showError("Invalid Time Range", "End time must be after start time.");
                    return;
                }
                
                java.time.LocalDateTime scheduledStart = java.time.LocalDateTime.of(pickupDate, java.time.LocalTime.of(startHour24, startMinute));
                if (scheduledStart.isBefore(java.time.LocalDateTime.now())) {
                    AlertHelper.showError("Invalid Time", "Selected date/time is in the past.");
                    return;
                }
            } catch (Exception ex) {
                AlertHelper.showError("Invalid Time", "Unable to parse selected time.");
                return;
            }
            boolean allSuccess = true;
            
            java.time.LocalDateTime scheduledStart = null;
            java.time.LocalDateTime scheduledEnd = null;
            try {
                // Parse start time
                String[] startParts = selectedTime[0].split("[: ]"); // [HH, mm, AM/PM]
                int startHr12 = Integer.parseInt(startParts[0]);
                int startMin = Integer.parseInt(startParts[1]);
                String startAmpm = startParts[2];
                int startHr24 = "AM".equals(startAmpm) ? (startHr12 == 12 ? 0 : startHr12) : (startHr12 == 12 ? 12 : startHr12 + 12);
                scheduledStart = java.time.LocalDateTime.of(pickupDate, java.time.LocalTime.of(startHr24, startMin));
                
                // Parse end time
                String[] endPts = selectedEndTime[0].split("[: ]");
                int endHr12 = Integer.parseInt(endPts[0]);
                int endMin = Integer.parseInt(endPts[1]);
                String endAp = endPts[2];
                int endHr24 = "AM".equals(endAp) ? (endHr12 == 12 ? 0 : endHr12) : (endHr12 == 12 ? 12 : endHr12 + 12);
                scheduledEnd = java.time.LocalDateTime.of(pickupDate, java.time.LocalTime.of(endHr24, endMin));
            } catch (Exception ex) {
                // fallback: use 9 AM - 5 PM if parsing failed
                scheduledStart = java.time.LocalDateTime.of(pickupDate, java.time.LocalTime.of(9, 0));
                scheduledEnd = java.time.LocalDateTime.of(pickupDate, java.time.LocalTime.of(17, 0));
            }

            if (reservation.isPartOfBundle()) {
                // Approve all items in the bundle with scheduled pickup
                String bundleId = reservation.getBundleId();
                List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                    .filter(r -> bundleId.equals(r.getBundleId()))
                    .filter(r -> "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus()))
                    .collect(java.util.stream.Collectors.toList());

                for (Reservation item : bundleItems) {
                    boolean success = reservationManager.approvePickupRequest(item.getReservationId(), scheduledStart, scheduledEnd);
                    if (!success) {
                        allSuccess = false;
                    }
                }
            } else {
                allSuccess = reservationManager.approvePickupRequest(reservation.getReservationId(), scheduledStart, scheduledEnd);
            }
            
            if (allSuccess) {
                // Call refresh callback to update the display with current filter applied
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Pickup Approved");
                successAlert.setHeaderText(reservation.isPartOfBundle() ? "Bundle pickup approved!" : "Pickup approved!");
                successAlert.setContentText(
                    "Pickup scheduled for:\n" +
                    pickupDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "\n" +
                    "⏰ " + selectedTime[0] + " - " + selectedEndTime[0] + "\n\n" +
                    "The student has been notified and can now claim the item.\n" +
                    "They will see this in their notification bell (🔔)."
                );
                successAlert.showAndWait();
            } else {
                AlertHelper.showError("Error", "Failed to approve pickup request");
            }
        });
    }

    /**
     * Handle reject pickup request
     */
    private void handleRejectPickup(Reservation reservation, TableView<Reservation> table) {
        String reason = AlertHelper.showInputDialog("Reject Pickup Request", "Reject pickup request for: " + reservation.getStudentName(), "Reason for rejection:");
        if (reason != null && !reason.isEmpty()) {
            javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Rejection");
            confirmAlert.setHeaderText("This will change status back to 'AWAITING PICKUP REQUEST'");
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
                                "AWAITING PICKUP REQUEST", 
                                "Pickup request rejected: " + reason
                            );
                            if (!success) {
                                allSuccess = false;
                            }
                        }
                    } else {
                        allSuccess = reservationManager.updateReservationStatus(
                            reservation.getReservationId(), 
                            "AWAITING PICKUP REQUEST", 
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

        // Dialog styling is provided globally by SceneManager; just add semantic classes
        dialog.getDialogPane().getStyleClass().add("dialog-root");
        if (ThemeManager.isDarkMode()) dialog.getDialogPane().getStyleClass().add("dark");

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
        
        // Damaged Stock Items
        int damagedStockCount = inventoryManager.getTotalDamagedStock();
        VBox damagedStockCard = createStatCard("🔨 Damaged", String.valueOf(damagedStockCount), "#9B59B6");
        
        statsBox.getChildren().addAll(itemsCard, lowStockCard, damagedStockCard);

        // Course filter dropdown (All + per-course)
        HBox courseBar = new HBox(8);
        courseBar.setAlignment(Pos.CENTER_LEFT);
        courseBar.setPadding(new Insets(0, 0, 8, 0));

        // Build course ComboBox - normalize SHS courses and remove duplicates
        List<String> rawCourses = inventoryManager.getAvailableCourses();
        rawCourses.removeIf(s -> s == null || s.trim().isEmpty());
        
        // Normalize and deduplicate courses
        Set<String> uniqueCourses = new LinkedHashSet<>();
        for (String course : rawCourses) {
            String normalized = normalizeCourseForDisplay(course);
            uniqueCourses.add(normalized);
        }
        
        List<String> availableCourses = new ArrayList<>(uniqueCourses);
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
        String fieldText = ThemeManager.isDarkMode() ? "white" : "#111827";
        // Apply highlighted styling from the start (consistent with reservations filter)
        String comboStyle =
            "-fx-font-size: 14px;" +
            "-fx-background-color: " + fieldBg + ";" +
            "-fx-control-inner-background: " + fieldBg + ";" +
            "-fx-text-fill: " + fieldText + ";" +
            "-fx-border-color: -color-accent-emphasis;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 4px;" +
            "-fx-background-radius: 4px;" +
            "-fx-padding: 0px 8px;" +
            "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
        courseCombo.setStyle(comboStyle);

        // Gender Toggle (modern radio buttons) - placed next to course filter
        final ToggleGroup genderToggle = new ToggleGroup();
        final RadioButton rbAllGender = new RadioButton("All");
        final RadioButton rbMale = new RadioButton("Male");
        final RadioButton rbFemale = new RadioButton("Female");
        rbAllGender.setToggleGroup(genderToggle);
        rbMale.setToggleGroup(genderToggle);
        rbFemale.setToggleGroup(genderToggle);
        rbAllGender.setSelected(true);

        HBox genderBox = new HBox(6, rbAllGender, rbMale, rbFemale);
        genderBox.setAlignment(Pos.CENTER_LEFT);
        String rbBase = "-fx-cursor: hand; -fx-padding: 6 12; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-font-size:13px;";
        rbAllGender.setStyle(rbBase + " -fx-background-color: transparent;");
        rbMale.setStyle(rbBase + " -fx-background-color: transparent;");
        rbFemale.setStyle(rbBase + " -fx-background-color: transparent;");

        courseBar.getChildren().addAll(courseCombo, genderBox);

        // Update ComboBox style when the application theme changes
        Runnable courseThemeRefresher = () -> {
            try {
                String fieldBg2 = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
                String fieldText2 = ThemeManager.isDarkMode() ? "white" : "#111827";
                String comboStyle2 =
                    "-fx-font-size: 14px;" +
                    "-fx-background-color: " + fieldBg2 + ";" +
                    "-fx-control-inner-background: " + fieldBg2 + ";" +
                    "-fx-text-fill: " + fieldText2 + ";" +
                    "-fx-border-color: -color-accent-emphasis;" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 4px;" +
                    "-fx-background-radius: 4px;" +
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

            // Size selection: allow multiple sizes (S, M, L, XL) each with a checkbox + qty field.
            // One Size is exclusive and cannot be combined with S/M/L/XL.
            GridPane sizesGrid = new GridPane();
            sizesGrid.setHgap(8);
            sizesGrid.setVgap(8);

            javafx.scene.control.CheckBox cbS = new javafx.scene.control.CheckBox("S");
            TextField qtyS = new TextField(); qtyS.setPromptText("Qty"); qtyS.setPrefWidth(70); qtyS.setDisable(true);
            javafx.scene.control.CheckBox cbM = new javafx.scene.control.CheckBox("M");
            TextField qtyM = new TextField(); qtyM.setPromptText("Qty"); qtyM.setPrefWidth(70); qtyM.setDisable(true);
            javafx.scene.control.CheckBox cbL = new javafx.scene.control.CheckBox("L");
            TextField qtyL = new TextField(); qtyL.setPromptText("Qty"); qtyL.setPrefWidth(70); qtyL.setDisable(true);
            javafx.scene.control.CheckBox cbXL = new javafx.scene.control.CheckBox("XL");
            TextField qtyXL = new TextField(); qtyXL.setPromptText("Qty"); qtyXL.setPrefWidth(70); qtyXL.setDisable(true);

            // One Size (exclusive)
            javafx.scene.control.CheckBox cbOne = new javafx.scene.control.CheckBox("One Size");
            TextField qtyOne = new TextField(); qtyOne.setPromptText("Qty"); qtyOne.setPrefWidth(70); qtyOne.setDisable(true);

            // Wire up enable/disable behavior
            cbS.selectedProperty().addListener((obs,o,n) -> qtyS.setDisable(!n));
            cbM.selectedProperty().addListener((obs,o,n) -> qtyM.setDisable(!n));
            cbL.selectedProperty().addListener((obs,o,n) -> qtyL.setDisable(!n));
            cbXL.selectedProperty().addListener((obs,o,n) -> qtyXL.setDisable(!n));
            cbOne.selectedProperty().addListener((obs,o,n) -> {
                qtyOne.setDisable(!n);
                // if one-size selected, disable others
                if (n) {
                    cbS.setDisable(true); cbM.setDisable(true); cbL.setDisable(true); cbXL.setDisable(true);
                    qtyS.setDisable(true); qtyM.setDisable(true); qtyL.setDisable(true); qtyXL.setDisable(true);
                } else {
                    cbS.setDisable(false); cbM.setDisable(false); cbL.setDisable(false); cbXL.setDisable(false);
                }
            });
            // If any regular size selected, disable one-size
            java.util.List<javafx.scene.control.CheckBox> regularCbs = java.util.List.of(cbS, cbM, cbL, cbXL);
            for (javafx.scene.control.CheckBox cb : regularCbs) {
                cb.selectedProperty().addListener((obs,o,n) -> {
                    if (n) {
                        cbOne.setDisable(true); cbOne.setSelected(false); qtyOne.setDisable(true);
                    } else {
                        boolean any = regularCbs.stream().anyMatch(c -> c.isSelected());
                        if (!any) cbOne.setDisable(false);
                    }
                });
            }

            // Layout sizes grid
            sizesGrid.add(cbS, 0, 0); sizesGrid.add(qtyS, 1, 0);
            sizesGrid.add(cbM, 2, 0); sizesGrid.add(qtyM, 3, 0);
            sizesGrid.add(cbL, 0, 1); sizesGrid.add(qtyL, 1, 1);
            sizesGrid.add(cbXL, 2, 1); sizesGrid.add(qtyXL, 3, 1);
            sizesGrid.add(cbOne, 0, 2); sizesGrid.add(qtyOne, 1, 2);

            TextField priceField = new TextField();
            priceField.setPromptText("Price (e.g. 450.00)");

            content.getChildren().addAll(codeLabel, nameField, courseComboDialog, sizesGrid, priceField);

            dialog.getDialogPane().setContent(content);

            // Enable/disable Add button based on validation
            javafx.scene.control.Button addActionBtn = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(addBtnType);
            addActionBtn.setDisable(true);

            // Simple validation listener for multi-size inputs
            Runnable validate = () -> {
                boolean ok = !nameField.getText().trim().isEmpty()
                         && courseComboDialog.getValue() != null && !courseComboDialog.getValue().trim().isEmpty();
                try {
                    double p = Double.parseDouble(priceField.getText().trim());
                    ok = ok && p >= 0;
                } catch (Exception ex) {
                    ok = false;
                }

                // At least one size must be selected with a valid quantity
                boolean anySizeValid = false;
                try {
                    if (cbS.isSelected()) { int qs = Integer.parseInt(qtyS.getText().trim()); if (qs >= 0) anySizeValid = true; }
                    if (cbM.isSelected()) { int qm = Integer.parseInt(qtyM.getText().trim()); if (qm >= 0) anySizeValid = true; }
                    if (cbL.isSelected()) { int ql = Integer.parseInt(qtyL.getText().trim()); if (ql >= 0) anySizeValid = true; }
                    if (cbXL.isSelected()) { int qxl = Integer.parseInt(qtyXL.getText().trim()); if (qxl >= 0) anySizeValid = true; }
                    if (cbOne.isSelected()) { int qo = Integer.parseInt(qtyOne.getText().trim()); if (qo >= 0) anySizeValid = true; }
                } catch (Exception ex) { anySizeValid = false; }

                addActionBtn.setDisable(!(ok && anySizeValid));
            };

            // Attach listeners
            nameField.textProperty().addListener((obs, o, n) -> validate.run());
            courseComboDialog.valueProperty().addListener((obs, o, n) -> validate.run());
            priceField.textProperty().addListener((obs, o, n) -> validate.run());
            qtyS.textProperty().addListener((obs, o, n) -> validate.run());
            qtyM.textProperty().addListener((obs, o, n) -> validate.run());
            qtyL.textProperty().addListener((obs, o, n) -> validate.run());
            qtyXL.textProperty().addListener((obs, o, n) -> validate.run());
            qtyOne.textProperty().addListener((obs, o, n) -> validate.run());
            cbS.selectedProperty().addListener((obs, o, n) -> validate.run());
            cbM.selectedProperty().addListener((obs, o, n) -> validate.run());
            cbL.selectedProperty().addListener((obs, o, n) -> validate.run());
            cbXL.selectedProperty().addListener((obs, o, n) -> validate.run());
            cbOne.selectedProperty().addListener((obs, o, n) -> validate.run());

            dialog.setResultConverter(button -> {
                if (button == addBtnType) {
                    try {
                        String name = nameField.getText().trim();
                        String course = courseComboDialog.getValue().trim();
                        double price = Double.parseDouble(priceField.getText().trim());

                        // Add an item for each selected size
                        if (cbS.isSelected()) {
                            int qs = Integer.parseInt(qtyS.getText().trim());
                            Item it = new Item(nextCode[0], name, course, "S", qs, price);
                            inventoryManager.addItem(it);
                            StockReturnLogger.logItemAdded("staff", nextCode[0], name, "S", qs, price);
                        }
                        if (cbM.isSelected()) {
                            int qm = Integer.parseInt(qtyM.getText().trim());
                            Item it = new Item(nextCode[0], name, course, "M", qm, price);
                            inventoryManager.addItem(it);
                            StockReturnLogger.logItemAdded("staff", nextCode[0], name, "M", qm, price);
                        }
                        if (cbL.isSelected()) {
                            int ql = Integer.parseInt(qtyL.getText().trim());
                            Item it = new Item(nextCode[0], name, course, "L", ql, price);
                            inventoryManager.addItem(it);
                            StockReturnLogger.logItemAdded("staff", nextCode[0], name, "L", ql, price);
                        }
                        if (cbXL.isSelected()) {
                            int qxl = Integer.parseInt(qtyXL.getText().trim());
                            Item it = new Item(nextCode[0], name, course, "XL", qxl, price);
                            inventoryManager.addItem(it);
                            StockReturnLogger.logItemAdded("staff", nextCode[0], name, "XL", qxl, price);
                        }
                        if (cbOne.isSelected()) {
                            int qo = Integer.parseInt(qtyOne.getText().trim());
                            Item it = new Item(nextCode[0], name, course, "One Size", qo, price);
                            inventoryManager.addItem(it);
                            StockReturnLogger.logItemAdded("staff", nextCode[0], name, "One Size", qo, price);
                        }

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
        sizesCol.setPrefWidth(130);

        TableColumn<InventoryRow, String> qtyCol = new TableColumn<>("Stock");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStockDisplay()));
        qtyCol.setCellFactory(col -> new TableCell<InventoryRow, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(value);
                    // Highlight rows with damaged stock
                    if (value.contains("dmg")) {
                        setStyle("-fx-text-fill: #CF222E;"); // Red for damaged
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        qtyCol.setPrefWidth(120);

        TableColumn<InventoryRow, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPriceDisplay()));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<InventoryRow, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : value);
                setAlignment(Pos.CENTER_LEFT);
                // ensure the text is left-aligned and vertically centered
                setTextAlignment(javafx.scene.text.TextAlignment.LEFT);
                if (!getStyleClass().contains("price-cell")) getStyleClass().add("price-cell");
            }
        });
        
        // Actions column - single Manage button that opens a small modal with actions
        TableColumn<InventoryRow, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<InventoryRow, Void>() {
            private final Button addStockBtn = new Button("➕ Add Stock");
            private final Button priceBtn = new Button("₱ Change Price");

            {
                addStockBtn.getStyleClass().add("primary-btn");
                addStockBtn.setMaxWidth(Double.MAX_VALUE);
                addStockBtn.setPrefHeight(28);
                priceBtn.getStyleClass().add("secondary-btn");
                priceBtn.setMaxWidth(Double.MAX_VALUE);
                priceBtn.setPrefHeight(28);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty) {
                    setGraphic(null);
                } else {
                    InventoryRow currentRow = getTableView().getItems().get(getIndex());
                    // When adding stock, prompt for variant selection if multiple sizes exist
                    addStockBtn.setOnAction(e -> showVariantSelectionDialog(currentRow, "Adjust Stock", selected -> {
                        if (selected != null) {
                            handleStockAdjustmentForItem(selected, refreshBtn::fire);
                        }
                    }));

                    priceBtn.setOnAction(e -> {
                        List<Item> vs = currentRow.getVariants();
                        if (vs != null && !vs.isEmpty()) {
                            handleChangePriceForItem(vs.get(0), refreshBtn::fire);
                        } else {
                            AlertHelper.showError("No Variants", "No variants available to change price.");
                        }
                    });
                    
                    // Create a More Actions menu button for additional options
                    javafx.scene.control.MenuButton moreBtn = new javafx.scene.control.MenuButton("⋮");
                    moreBtn.setStyle("-fx-font-size: 14px; -fx-padding: 4 8;");
                    
                    javafx.scene.control.MenuItem markDamagedItem = new javafx.scene.control.MenuItem("🔨 Mark Damaged");
                    markDamagedItem.setOnAction(ev -> showVariantSelectionDialog(currentRow, "Mark as Damaged", selected -> {
                        if (selected != null) {
                            handleMarkAsDamaged(selected, refreshBtn::fire);
                        }
                    }));
                    
                    javafx.scene.control.MenuItem restoreDamagedItem = new javafx.scene.control.MenuItem("♻️ Restore Damaged");
                    restoreDamagedItem.setOnAction(ev -> showVariantSelectionDialog(currentRow, "Restore from Damaged", selected -> {
                        if (selected != null && selected.getDamagedStock() > 0) {
                            handleRestoreDamaged(selected, refreshBtn::fire);
                        } else {
                            AlertHelper.showInfo("No Damaged Stock", "This item has no damaged stock to restore.");
                        }
                    }));
                    
                    moreBtn.getItems().addAll(markDamagedItem, restoreDamagedItem);

                    HBox wrapper = new HBox(8, addStockBtn, priceBtn, moreBtn);
                    wrapper.getStyleClass().add("action-gap");
                    // left-align the action buttons inside the Actions column
                    wrapper.setAlignment(Pos.CENTER_LEFT);
                    setAlignment(Pos.CENTER_LEFT);
                    wrapper.setPadding(new Insets(0, 12, 0, 8));
                    wrapper.setPrefHeight(Region.USE_COMPUTED_SIZE);
                    setGraphic(wrapper);
                }
            }
        });
        actionsCol.setPrefWidth(180);
        actionsCol.setMinWidth(160);
        
        // Place actions column on the right (end) as requested
        table.getColumns().addAll(codeCol, nameCol, courseCol, sizesCol, qtyCol, priceCol, actionsCol);

        // Make columns resize to fill the available width of the container
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefWidth(Double.MAX_VALUE);

        // Bind column widths as percentages of the table width so the table fills its box
        // Bind column widths as percentages of the table width so the table fills its box
        // Reserve more space for the Actions column so buttons fit comfortably
        actionsCol.prefWidthProperty().bind(table.widthProperty().multiply(0.16));
        actionsCol.setMinWidth(140);
        codeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));

        // Make table rows clickable (double-click) to show full item details
        table.setRowFactory(tv -> {
            TableRow<InventoryRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    InventoryRow rowData = row.getItem();
                    // Show comprehensive details dialog
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Item Details");
                    info.setHeaderText(rowData.getName() + " (Code: " + rowData.getCode() + ")");
                    StringBuilder sb = new StringBuilder();
                    sb.append("Course: ").append(rowData.getCourse()).append("\n\n");
                    sb.append("Variants:\n");
                    for (Item it : rowData.getVariants()) {
                        sb.append(" - Size: ").append(it.getSize())
                          .append(" | Available: ").append(it.getQuantity());
                        if (it.getDamagedStock() > 0) {
                            sb.append(" | Damaged: ").append(it.getDamagedStock());
                        }
                        sb.append(" | Price: ₱").append(String.format("%.2f", it.getPrice()))
                          .append("\n");
                    }
                    sb.append("\nTotal Available: ").append(rowData.getTotalQuantity());
                    if (rowData.getTotalDamagedStock() > 0) {
                        sb.append(" | Total Damaged: ").append(rowData.getTotalDamagedStock());
                    }
                    sb.append("\nPrice (example): ").append(rowData.getPriceDisplay()).append("\n");
                    info.setContentText(sb.toString());
                    info.showAndWait();
                }
            });
            return row;
        });
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.26));
        courseCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08));
        sizesCol.prefWidthProperty().bind(table.widthProperty().multiply(0.12));
        qtyCol.prefWidthProperty().bind(table.widthProperty().multiply(0.06));
        priceCol.prefWidthProperty().bind(table.widthProperty().multiply(0.08));

        // Keep table visual size consistent when limiting rows: fix row height and pref height
        // (pref height will be set after itemsPerPage is declared below)

        // Load all items (we'll manage paging/filtering)
        List<Item> allItems = inventoryManager.getAllItems();

        final int[] currentPage = new int[] { 1 };
        final int itemsPerPage = 10;
        final String[] currentCourse = new String[] { "All" };
        final String[] currentGender = new String[] { "All" };
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
            updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
        });

        // Course combo action -> update via helper
        courseCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                courseCombo.setValue("All");
                currentCourse[0] = "All";
            } else {
                currentCourse[0] = newV;
            }
            
            // Add highlighting for all filter selections (consistent with reservations filter)
            String activeBg = ThemeManager.isDarkMode() ? "rgba(255,255,255,0.12)" : "#f6f7f8";
            String activeText = ThemeManager.isDarkMode() ? "white" : "#111827";
            String highlightStyle =
                "-fx-font-size: 14px;" +
                "-fx-background-color: " + activeBg + ";" +
                "-fx-control-inner-background: " + activeBg + ";" +
                "-fx-text-fill: " + activeText + ";" +
                "-fx-border-color: -color-accent-emphasis;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 4px;" +
                "-fx-background-radius: 4px;" +
                "-fx-padding: 0px 8px;" +
                "-fx-prompt-text-fill: rgba(0,0,0,0.45);" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.03), 4, 0, 0, 1);";
            courseCombo.setStyle(highlightStyle);
            
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
        });

        // Gender toggle change -> update via helper
        genderToggle.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                currentGender[0] = "All";
            } else {
                RadioButton sel = (RadioButton) newT;
                currentGender[0] = sel.getText();
            }
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
        });

        // Refresh button action
        refreshBtn.setOnAction(e -> {
            List<Item> refreshed = inventoryManager.getAllItems();
            allItems.clear();
            allItems.addAll(refreshed);
            currentPage[0] = 1;
            pageWindowStart[0] = 1;
            updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
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
        updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);

        return container;
    }

    /**
     * Reload inventory data from disk. Useful when external tools modify the
     * underlying `items.txt` file and the UI should reflect changes.
     */
    public void reloadInventory() {
        inventoryManager.reloadItems();
    }

    /**
     * Update inventory table contents and rebuild pagination controls
     */
    private void updateInventoryTable(TableView<InventoryRow> table, List<Item> allItems, String[] currentCourse,
                                      String[] currentGender, int[] currentPage, int itemsPerPage, HBox pageControls, HBox statsBox,
                                      TextField searchField, int[] pageWindowStart) {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        List<Item> filtered = allItems.stream()
            .filter(it -> {
                String sel = currentCourse[0] == null ? "All" : currentCourse[0];
                String itemCourse = it.getCourse() == null ? "" : it.getCourse();

                String gsel = currentGender[0] == null ? "All" : currentGender[0];

                boolean courseMatch;
                if ("All".equalsIgnoreCase(sel)) {
                    courseMatch = true;
                } else if ("STI Special".equalsIgnoreCase(sel)) {
                    courseMatch = "STI Special".equalsIgnoreCase(itemCourse);
                } else {
                    // If selected contains multiple course codes (e.g. "BSBA/BSA"), match if itemCourse matches any part
                    if (sel.contains("/")) {
                        String[] selParts = sel.split("/");
                        courseMatch = false;
                        for (String sp : selParts) {
                            if (sp.trim().equalsIgnoreCase(itemCourse)) {
                                courseMatch = true; break;
                            }
                            // if itemCourse is combined, check its parts too
                            if (itemCourse.contains("/")) {
                                for (String ip : itemCourse.split("/")) {
                                    if (sp.trim().equalsIgnoreCase(ip.trim())) { courseMatch = true; break; }
                                }
                                if (courseMatch) break;
                            }
                        }
                    } else {
                        // sel is single code; itemCourse may be combined or single
                        if (itemCourse.contains("/")) {
                            courseMatch = false;
                            for (String ip : itemCourse.split("/")) {
                                if (ip.trim().equalsIgnoreCase(sel)) { courseMatch = true; break; }
                            }
                        } else {
                            courseMatch = itemCourse.equalsIgnoreCase(sel);
                        }
                    }
                }

                // Ensure STEM Lab Coat shows when SHS or STEM is selected (since STEM is now grouped under SHS)
                String nameLower = it.getName() == null ? "" : it.getName().toLowerCase();
                if (nameLower.contains("lab coat")) {
                    // Show when selection is "All", "SHS", or contains "STEM"
                    String selUpper = sel.toUpperCase();
                    if (!("All".equalsIgnoreCase(sel) || selUpper.equals("SHS") || selUpper.contains("STEM") || itemCourse.toUpperCase().equals("STEM"))) {
                        courseMatch = false;
                    }
                }

                // Special-case: TVL Chef / Culinary items should show when SHS or TVL-CA is selected
                boolean isCulArtItem = nameLower.contains("chef") || nameLower.contains("apron") || nameLower.contains("cul art") || nameLower.contains("culinary") || nameLower.contains("tvl chef");
                if (isCulArtItem) {
                    // Show when selection is "All", "SHS", or explicitly TVL-CA
                    String selUpper = sel == null ? "" : sel.toUpperCase();
                    if (!("All".equalsIgnoreCase(sel) || selUpper.equals("SHS") || selUpper.equals("TVL-CA") || selUpper.equals("CUL ART") || selUpper.equals("TVL CA") || itemCourse.toUpperCase().startsWith("TVL"))) {
                        courseMatch = false;
                    }
                }

                boolean searchMatch = q.isEmpty() || (it.getName() != null && it.getName().toLowerCase().contains(q)) || String.valueOf(it.getCode()).contains(q);

                boolean genderMatch = true;
                if (!"All".equalsIgnoreCase(gsel)) {
                    String name = it.getName() == null ? "" : it.getName().toLowerCase();
                    if ("Male".equalsIgnoreCase(gsel)) {
                        genderMatch = name.contains("(male)");
                    } else if ("Female".equalsIgnoreCase(gsel)) {
                        genderMatch = name.contains("(female)");
                    }
                }

                return courseMatch && genderMatch && searchMatch;
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
                updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
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
                        updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
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
                updateInventoryTable(table, allItems, currentCourse, currentGender, currentPage, itemsPerPage, pageControls, statsBox, searchField, pageWindowStart);
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
        // Deduplicate exact duplicate item entries (same code + size) which may appear in
        // the underlying data source. Keep insertion order.
        Map<String, Item> uniqueByKey = new LinkedHashMap<>();
        for (Item it : source) {
            String key = it.getCode() + "::" + (it.getSize() == null ? "" : it.getSize()).trim().toLowerCase();
            if (!uniqueByKey.containsKey(key)) {
                uniqueByKey.put(key, it);
            } else {
                // If the same code+size appears multiple times, merge quantities to preserve totals
                Item existing = uniqueByKey.get(key);
                try {
                    int mergedQty = existing.getQuantity() + it.getQuantity();
                    existing.setQuantity(mergedQty);
                } catch (Exception ex) {
                    // if mutation isn't supported, ignore and keep the first one
                }
            }
        }

        Map<Integer, List<Item>> grouped = new LinkedHashMap<>();
        for (Item item : uniqueByKey.values()) {
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

    @SuppressWarnings("unused")
    private void showManageItemDialog(InventoryRow row, Runnable refreshAction) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Item");
        dialog.setHeaderText("Manage: " + row.getName() + " (Code: " + row.getCode() + ")");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        // Variant selector (single dialog contains variant + actions)
        List<Item> variants = row.getVariants();
        ComboBox<Item> variantCombo = new ComboBox<>(FXCollections.observableArrayList(variants));
        variantCombo.setPrefWidth(320);
        variantCombo.setPrefHeight(36);
        variantCombo.setPromptText("Select size / variant");
        if (!variants.isEmpty()) variantCombo.getSelectionModel().selectFirst();

        // Display meaningful text for each variant
        variantCombo.setCellFactory(cb -> new javafx.scene.control.ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getSize() + " — Qty: " + item.getQuantity() + " — ₱" + String.format("%.2f", item.getPrice()));
                }
            }
        });
        variantCombo.setButtonCell(new javafx.scene.control.ListCell<Item>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getSize() + " — Qty: " + item.getQuantity() + " — ₱" + String.format("%.2f", item.getPrice()));
            }
        });

        Button adjustBtn = new Button("📝 Adjust Stock");
        adjustBtn.getStyleClass().add("primary-btn");
        adjustBtn.setMaxWidth(Double.MAX_VALUE);
        adjustBtn.setOnAction(e -> {
            Item selected = variantCombo.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertHelper.showError("No Variant Selected", "Please select a size/variant first.");
                return;
            }
            dialog.close();
            handleStockAdjustmentForItem(selected, refreshAction);
        });

        Button priceBtn = new Button("₱ Change Price");
        priceBtn.getStyleClass().add("primary-btn");
        priceBtn.setMaxWidth(Double.MAX_VALUE);
        priceBtn.setOnAction(e -> {
            dialog.close();
            // Change price applies to all sizes; use first variant as representative
            List<Item> vs = row.getVariants();
            if (vs != null && !vs.isEmpty()) {
                handleChangePriceForItem(vs.get(0), refreshAction);
            }
        });

        Button detailsBtn = new Button("🔍 View Details");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> {
            Item selected = variantCombo.getSelectionModel().getSelectedItem();
            // Show details for the selected variant or the row summary
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Item Details");
            info.setHeaderText(row.getName());
            StringBuilder sb = new StringBuilder();
            sb.append("Code: ").append(row.getCode()).append("\n");
            sb.append("Course: ").append(row.getCourse()).append("\n");
            if (selected != null) {
                sb.append("Size: ").append(selected.getSize()).append("\n");
                sb.append("Qty: ").append(selected.getQuantity()).append("\n");
                sb.append("Price: ₱").append(String.format("%.2f", selected.getPrice())).append("\n");
            } else {
                sb.append("Sizes: ").append(row.getSizesDisplay()).append("\n");
                sb.append("Total Qty: ").append(row.getTotalQuantity()).append("\n");
                sb.append("Price (example): ").append(row.getPriceDisplay()).append("\n");
            }
            info.setContentText(sb.toString());
            info.showAndWait();
        });

        VBox content = new VBox(10);
        content.setPrefWidth(360);
        content.getChildren().addAll(new Label("Variant:"), variantCombo, adjustBtn, priceBtn, detailsBtn);
        dialog.getDialogPane().setContent(content);

        // Request focus on variant selector when shown so user can immediately interact
        dialog.setOnShown(ev -> Platform.runLater(() -> {
            if (!variants.isEmpty()) variantCombo.requestFocus();
        }));

        dialog.showAndWait();
    }

    private static class InventoryRow {
        private final int code;
        private final String name;
        private final String course;
        private final List<Item> variants;
        private final int totalQuantity;      // Available stock only
        private final int totalDamagedStock;  // Damaged stock
        private final String sizesDisplay;
        private final String priceDisplay;
        private final String stockDisplay;    // Shows available/damaged breakdown

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
            this.totalDamagedStock = variants.stream().mapToInt(Item::getDamagedStock).sum();
            this.sizesDisplay = buildSizesDisplay();
            this.priceDisplay = buildPriceDisplay();
            this.stockDisplay = buildStockDisplay();
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
        
        /**
         * Build display showing available and damaged stock
         */
        private String buildStockDisplay() {
            if (totalDamagedStock > 0) {
                return totalQuantity + " (+" + totalDamagedStock + " dmg)";
            }
            return String.valueOf(totalQuantity);
        }

        public int getCode() { return code; }
        public String getName() { return name; }
        public String getCourse() { return course; }
        public List<Item> getVariants() { return java.util.Collections.unmodifiableList(variants); }
        public int getTotalQuantity() { return totalQuantity; }
        public int getTotalDamagedStock() { return totalDamagedStock; }
        public String getSizesDisplay() { return sizesDisplay; }
        public String getPriceDisplay() { return priceDisplay; }
        public String getStockDisplay() { return stockDisplay; }
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

        // Public helper to allow external callers (e.g., view focus listeners)
        // to ask the controller to reload inventory from disk.

        long ordersToday = countOrders(allReservations, reservation ->
            "COMPLETED".equals(reservation.getStatus()) &&
            getRelevantDate(reservation).equals(today)
        );

        

        // Stock Status overview: show key metric cards (Net Sales month, Net Sales all-time,
        // Orders today, Completed orders). Stock bar shows inventory levels only.
        VBox stockStatusBox = new VBox(8);
        stockStatusBox.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 14; -fx-background-radius: 8; -fx-border-radius:8;");

        javafx.scene.control.Label stockTitle = new javafx.scene.control.Label("Stock Status");
        stockTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        int completedOrdersCount = completedReservations.size();

        // Compute stock metrics per item *variant* (size) so the status bar reflects
        // individual sizes rather than collapsing counts to a single product entry.
        // Total products (unique codes) still shown, but the bar is based on variant counts.
        java.util.Map<Integer, java.util.List<Item>> groupedByCode = allItems.stream()
            .collect(Collectors.groupingBy(Item::getCode));

        int totalProducts = groupedByCode.size();

        // Count across all variants (sizes)
        int totalVariants = allItems.size();
        long lowStockCount = allItems.stream().filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 15 && i.getQuantity() > 5).count();
        long criticalStockCount = allItems.stream().filter(i -> i.getQuantity() > 0 && i.getQuantity() <= 5).count();
        long outOfStockCount = allItems.stream().filter(i -> i.getQuantity() == 0).count();

        javafx.scene.control.Label productsLabel = new javafx.scene.control.Label(totalProducts + " Products • " + totalVariants + " Variants");
        productsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        // Progress bar made from regions with proportional widths (Stock Status bar)
        // Note: This bar only shows inventory levels, NOT replacement/damaged items
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
        
        // Stock bar only shows inventory levels (not replacements)
        int inStockCount = Math.max(0, totalVariants - lowCount - criticalCount - outOfStock);

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

        // Use total variant count as the denominator so proportions reflect variants, not product groups.
        double denom = Math.max(1, (double) totalVariants);
        inRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) inStockCount / denom));
        lowRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) lowCount / denom));
        critRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) criticalCount / denom));
        outRegion.prefWidthProperty().bind(barContainer.widthProperty().multiply((double) outOfStock / denom));

        barContainer.getChildren().addAll(inRegion, lowRegion, critRegion, outRegion);

        // Legend for stock status
        HBox legend = new HBox(12);
        legend.setAlignment(Pos.CENTER_LEFT);

        Region legendIn = new Region(); legendIn.setPrefSize(12,12); legendIn.setStyle("-fx-background-color: #1A7F37; -fx-background-radius:2;");
        Region legendLow = new Region(); legendLow.setPrefSize(12,12); legendLow.setStyle("-fx-background-color: #FB8C00; -fx-background-radius:2;");
        Region legendCrit = new Region(); legendCrit.setPrefSize(12,12); legendCrit.setStyle("-fx-background-color: #CF222E; -fx-background-radius:2;");
        Region legendOut = new Region(); legendOut.setPrefSize(12,12); legendOut.setStyle("-fx-background-color: #6B5B95; -fx-background-radius:2;");

        javafx.scene.control.Label lblIn = new javafx.scene.control.Label("In stock (variants): " + inStockCount);
        javafx.scene.control.Label lblLow = new javafx.scene.control.Label("Low stock (variants): " + lowCount);
        javafx.scene.control.Label lblCrit = new javafx.scene.control.Label("Critical (variants): " + criticalCount);
        javafx.scene.control.Label lblOut = new javafx.scene.control.Label("Out of stock (variants): " + outOfStock);

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

        // ========== REPLACEMENT REASONS SECTION ==========
        // Separate tracking for replacement requests with categorized reasons
        VBox replacementBox = new VBox(12);
        replacementBox.setStyle("-fx-background-color: -color-bg-subtle; -fx-padding: 16; -fx-background-radius: 8; -fx-border-radius:8;");

        javafx.scene.control.Label replacementTitle = new javafx.scene.control.Label("Replacement Reasons");
        replacementTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Get replacement summary (including data from reservations)
        java.util.List<Reservation> allReservationsForSummary = reservationManager.getAllReservations();
        ReplacementSummary replacementSummary = ReplacementTracker.getSummaryWithReservations(allReservationsForSummary);
        int totalReplacements = replacementSummary.getTotalReplacements();

        javafx.scene.control.Label replacementCountLabel = new javafx.scene.control.Label("Total Replacements: " + totalReplacements);
        replacementCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: -color-fg-default;");

        // Get counts for each reason
        int wrongSizeCount = replacementSummary.getCount(ReplacementReason.WRONG_SIZE);
        int damagedDefectiveCount = replacementSummary.getCount(ReplacementReason.DAMAGED_DEFECTIVE);
        int wrongItemCount = replacementSummary.getCount(ReplacementReason.WRONG_ITEM);
        int poorQualityCount = replacementSummary.getCount(ReplacementReason.POOR_QUALITY);
        int colorDesignCount = replacementSummary.getCount(ReplacementReason.COLOR_DESIGN);
        int sizeFitCount = replacementSummary.getCount(ReplacementReason.SIZE_FIT);
        int otherCount = replacementSummary.getCount(ReplacementReason.OTHER);

        // Build replacement reasons pie chart
        javafx.scene.chart.PieChart replacementPieChart = new javafx.scene.chart.PieChart();
        replacementPieChart.setTitle(null);
        replacementPieChart.setLegendVisible(false);
        replacementPieChart.setLabelsVisible(false);
        replacementPieChart.setPrefSize(200, 200);
        replacementPieChart.setMinSize(180, 180);
        replacementPieChart.setMaxSize(220, 220);

        // Add pie chart slices for each reason (only if count > 0)
        javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections.observableArrayList();
        if (wrongSizeCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Wrong Size", wrongSizeCount));
        if (damagedDefectiveCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Damaged/Defective", damagedDefectiveCount));
        if (wrongItemCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Wrong Item", wrongItemCount));
        if (poorQualityCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Poor Quality", poorQualityCount));
        if (colorDesignCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Color/Design", colorDesignCount));
        if (sizeFitCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Size Fit", sizeFitCount));
        if (otherCount > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Other", otherCount));

        replacementPieChart.setData(pieData);

        // Apply colors to pie slices after data is set
        Platform.runLater(() -> {
            String[] colors = {
                ReplacementReason.WRONG_SIZE.getColor(),
                ReplacementReason.DAMAGED_DEFECTIVE.getColor(),
                ReplacementReason.WRONG_ITEM.getColor(),
                ReplacementReason.POOR_QUALITY.getColor(),
                ReplacementReason.COLOR_DESIGN.getColor(),
                ReplacementReason.SIZE_FIT.getColor(),
                ReplacementReason.OTHER.getColor()
            };
            int[] counts = {wrongSizeCount, damagedDefectiveCount, wrongItemCount, poorQualityCount, colorDesignCount, sizeFitCount, otherCount};
            int colorIndex = 0;
            for (int i = 0; i < counts.length; i++) {
                if (counts[i] > 0 && colorIndex < replacementPieChart.getData().size()) {
                    javafx.scene.chart.PieChart.Data slice = replacementPieChart.getData().get(colorIndex);
                    slice.getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
                    colorIndex++;
                }
            }
        });

        // Style the pie chart
        replacementPieChart.setStyle("-fx-background-color: transparent;");

        // Replacement reasons legend - grid layout for better space usage
        javafx.scene.layout.GridPane replacementLegend = new javafx.scene.layout.GridPane();
        replacementLegend.setHgap(20);
        replacementLegend.setVgap(10);
        replacementLegend.setAlignment(Pos.CENTER_LEFT);

        // Helper to create legend item with larger text and icons
        java.util.function.BiFunction<ReplacementReason, Integer, HBox> makeLegendItem = (reason, count) -> {
            Region dot = new Region();
            dot.setPrefSize(14, 14);
            dot.setMinSize(14, 14);
            dot.setStyle("-fx-background-color: " + reason.getColor() + "; -fx-background-radius: 3;");
            javafx.scene.control.Label lbl = new javafx.scene.control.Label(reason.getIcon() + " " + reason.getDisplayName() + ": " + count);
            lbl.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 14px; -fx-font-weight: normal;");
            HBox item = new HBox(8, dot, lbl);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setMinWidth(180);
            return item;
        };

        // Grid layout: 2 columns, 4 rows for better distribution
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.WRONG_SIZE, wrongSizeCount), 0, 0);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.DAMAGED_DEFECTIVE, damagedDefectiveCount), 1, 0);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.WRONG_ITEM, wrongItemCount), 0, 1);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.POOR_QUALITY, poorQualityCount), 1, 1);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.COLOR_DESIGN, colorDesignCount), 0, 2);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.SIZE_FIT, sizeFitCount), 1, 2);
        replacementLegend.add(makeLegendItem.apply(ReplacementReason.OTHER, otherCount), 0, 3);

        // Layout: pie chart on left, legend on right with proper spacing
        HBox replacementContent = new HBox(24);
        replacementContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(replacementContent, Priority.ALWAYS);
        
        VBox legendBox = new VBox(12);
        legendBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(legendBox, Priority.ALWAYS);
        legendBox.getChildren().addAll(replacementCountLabel, replacementLegend);
        
        replacementContent.getChildren().addAll(replacementPieChart, legendBox);

        replacementBox.getChildren().addAll(replacementTitle, replacementContent);

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
            replacementBox,
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
        // Use a custom Dialog with an explicit TextField so focus and input work reliably.
        Dialog<Integer> qtyDialog = new Dialog<>();
        qtyDialog.setTitle("Adjust Stock");
        qtyDialog.setHeaderText("Adjust stock for: " + item.getName() + " (" + item.getSize() + ")");
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        qtyDialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label currentQty = new Label("Current Quantity: " + item.getQuantity());
        TextField addField = new TextField("0");
        addField.setPromptText("Add Quantity");
        addField.setPrefWidth(120);

        grid.add(currentQty, 0, 0);
        grid.add(new Label("Add Quantity:"), 0, 1);
        grid.add(addField, 1, 1);

        qtyDialog.getDialogPane().setContent(grid);

        // Ensure the text field gains focus when shown
        qtyDialog.setOnShown(e -> Platform.runLater(() -> addField.requestFocus()));

        qtyDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okBtn) {
                try {
                    return Integer.parseInt(addField.getText().trim());
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });

        qtyDialog.showAndWait().ifPresent(addQuantity -> {
            if (addQuantity == null) {
                AlertHelper.showError("Invalid Input", "Please enter a valid whole number!");
                return;
            }
            if (addQuantity < 0) {
                AlertHelper.showError("Invalid Input", "Added quantity cannot be negative!");
                return;
            }
            if (addQuantity == 0) {
                AlertHelper.showInfo("No Change", "No quantity added.");
                return;
            }

            int oldQuantity = item.getQuantity();
            int newQuantity = oldQuantity + addQuantity;

            boolean success = inventoryManager.updateItemQuantityBySize(
                item.getCode(),
                item.getSize(),
                newQuantity
            );

            if (success) {
                String details = String.format("Added by staff: +%d (manual restock)", addQuantity);
                StockReturnLogger.logItemUpdated("staff", item.getCode(), item.getName(), item.getSize(), oldQuantity, newQuantity, details);
                refreshAction.run();
                AlertHelper.showSuccess("Stock Updated",
                    "Stock increased successfully!\n\n" +
                    "Item: " + item.getName() + " (" + item.getSize() + ")\n" +
                    "Old Quantity: " + oldQuantity + "\n" +
                    "Added: " + addQuantity + "\n" +
                    "New Quantity: " + newQuantity + "\n");
            } else {
                AlertHelper.showError("Error", "Failed to update stock!");
            }
        });
    }
    
    /**
     * Handle marking items as damaged - moves from available stock to damaged stock.
     */
    private void handleMarkAsDamaged(Item item, Runnable refreshAction) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Mark as Damaged");
        dialog.setHeaderText("Mark damaged stock for: " + item.getName() + " (" + item.getSize() + ")");
        ButtonType okBtn = new ButtonType("Mark Damaged", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label availableLabel = new Label("Available Stock: " + item.getQuantity());
        Label damagedLabel = new Label("Current Damaged: " + item.getDamagedStock());
        TextField qtyField = new TextField("1");
        qtyField.setPromptText("Quantity to mark damaged");
        qtyField.setPrefWidth(120);
        TextField reasonField = new TextField();
        reasonField.setPromptText("Reason (e.g., torn, stained)");
        reasonField.setPrefWidth(200);

        grid.add(availableLabel, 0, 0, 2, 1);
        grid.add(damagedLabel, 0, 1, 2, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(qtyField, 1, 2);
        grid.add(new Label("Reason:"), 0, 3);
        grid.add(reasonField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setOnShown(e -> Platform.runLater(() -> qtyField.requestFocus()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okBtn) {
                try {
                    return Integer.parseInt(qtyField.getText().trim());
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(qty -> {
            if (qty == null || qty <= 0) {
                AlertHelper.showError("Invalid Input", "Please enter a valid positive quantity!");
                return;
            }
            if (qty > item.getQuantity()) {
                AlertHelper.showError("Insufficient Stock", 
                    "Cannot mark " + qty + " as damaged. Only " + item.getQuantity() + " available.");
                return;
            }

            String reason = reasonField.getText().trim();
            if (reason.isEmpty()) {
                reason = "No reason specified";
            }

            boolean success = inventoryManager.markItemAsDamaged(
                item.getCode(), item.getSize(), qty, reason, "Staff"
            );

            if (success) {
                String details = String.format("Marked as damaged: %d units - %s", qty, reason);
                StockReturnLogger.logItemUpdated("staff", item.getCode(), item.getName(), item.getSize(), 
                    item.getQuantity() + qty, item.getQuantity(), details);
                refreshAction.run();
                AlertHelper.showSuccess("Marked as Damaged",
                    "Successfully marked " + qty + " unit(s) as damaged.\n\n" +
                    "Item: " + item.getName() + " (" + item.getSize() + ")\n" +
                    "Reason: " + reason + "\n" +
                    "New Available: " + item.getQuantity() + "\n" +
                    "New Damaged: " + item.getDamagedStock());
            } else {
                AlertHelper.showError("Error", "Failed to mark items as damaged!");
            }
        });
    }
    
    /**
     * Handle restoring damaged items back to available stock.
     */
    private void handleRestoreDamaged(Item item, Runnable refreshAction) {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Restore Damaged Stock");
        dialog.setHeaderText("Restore damaged stock for: " + item.getName() + " (" + item.getSize() + ")");
        ButtonType okBtn = new ButtonType("Restore", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Label availableLabel = new Label("Available Stock: " + item.getQuantity());
        Label damagedLabel = new Label("Damaged Stock: " + item.getDamagedStock());
        TextField qtyField = new TextField("1");
        qtyField.setPromptText("Quantity to restore");
        qtyField.setPrefWidth(120);

        grid.add(availableLabel, 0, 0, 2, 1);
        grid.add(damagedLabel, 0, 1, 2, 1);
        grid.add(new Label("Quantity to restore:"), 0, 2);
        grid.add(qtyField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setOnShown(e -> Platform.runLater(() -> qtyField.requestFocus()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okBtn) {
                try {
                    return Integer.parseInt(qtyField.getText().trim());
                } catch (Exception ex) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(qty -> {
            if (qty == null || qty <= 0) {
                AlertHelper.showError("Invalid Input", "Please enter a valid positive quantity!");
                return;
            }
            if (qty > item.getDamagedStock()) {
                AlertHelper.showError("Insufficient Damaged Stock", 
                    "Cannot restore " + qty + ". Only " + item.getDamagedStock() + " damaged.");
                return;
            }

            boolean success = inventoryManager.restoreDamagedItem(
                item.getCode(), item.getSize(), qty, "Staff"
            );

            if (success) {
                String details = String.format("Restored from damaged: %d units", qty);
                StockReturnLogger.logItemUpdated("staff", item.getCode(), item.getName(), item.getSize(), 
                    item.getQuantity() - qty, item.getQuantity(), details);
                refreshAction.run();
                AlertHelper.showSuccess("Restored from Damaged",
                    "Successfully restored " + qty + " unit(s) to available stock.\n\n" +
                    "Item: " + item.getName() + " (" + item.getSize() + ")\n" +
                    "New Available: " + item.getQuantity() + "\n" +
                    "New Damaged: " + item.getDamagedStock());
            } else {
                AlertHelper.showError("Error", "Failed to restore damaged items!");
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
     * Extract image path from replacement reason string
     * Reason may contain: [Image proof attached: path/to/image.jpg]
     */
    private String extractImagePathFromReason(String reason) {
        if (reason == null || !reason.contains("[Image proof attached:")) {
            return null;
        }
        int startIdx = reason.indexOf("[Image proof attached:");
        int endIdx = reason.indexOf("]", startIdx);
        if (startIdx >= 0 && endIdx > startIdx) {
            return reason.substring(startIdx + "[Image proof attached:".length(), endIdx).trim();
        }
        return null;
    }

    /**
     * Determine whether a replacement reason indicates damage/defect.
     */
    private boolean isDamageReason(String reason) {
        if (reason == null) return false;
        String lower = reason.toLowerCase();
        // match 'damag' to cover 'damage' and 'damaged', and 'defect'/'defective'
        return lower.contains("damag") || lower.contains("defect");
    }



    /**
     * Return a style string for course filter buttons honoring dark mode and selection
     */
    @SuppressWarnings("unused")
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
        // Show 5 rows by default: use a fixed cell size and compute preferred height
        table.setFixedCellSize(42);
        double _rowsToShow = 5;
        double _headerHeight = 36;
        double _totalHeight = table.getFixedCellSize() * _rowsToShow + _headerHeight;
        table.setPrefHeight(_totalHeight); // 5 rows + header
        table.setMinHeight(_totalHeight);
        table.setMaxHeight(_totalHeight);
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
        // Show 5 rows by default: use a fixed cell size and compute preferred height
        table.setFixedCellSize(42);
        double _rowsToShow2 = 5;
        double _headerHeight2 = 36;
        double _totalHeight2 = table.getFixedCellSize() * _rowsToShow2 + _headerHeight2;
        table.setPrefHeight(_totalHeight2); // 5 rows + header
        table.setMinHeight(_totalHeight2);
        table.setMaxHeight(_totalHeight2);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle("-fx-background-color: -color-bg-subtle;");
        table.setPlaceholder(new javafx.scene.control.Label("No items between 0-15 units."));

        TableColumn<Item, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        itemCol.setCellFactory(col -> new TableCell<Item, String>() {
            @Override
            protected void updateItem(String itemName, boolean empty) {
                super.updateItem(itemName, empty);
                if (empty || itemName == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("👕 " + itemName);
                    setStyle("-fx-padding: 8 12; -fx-alignment: center-left;");
                }
            }
        });
        itemCol.setPrefWidth(280);

        TableColumn<Item, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSize()));
        sizeCol.setPrefWidth(80);

        TableColumn<Item, Number> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getQuantity()));
        qtyCol.setPrefWidth(80);

        TableColumn<Item, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> {
            int q = data.getValue().getQuantity();
            String status;
            if (q == 0) {
                status = "OUT OF STOCK";
            } else if (q <= 5) {
                status = "CRITICAL";
            } else {
                status = "LOW";
            }
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
                    // For CRITICAL, rely on the row background (light red) and keep text default.
                    if ("CRITICAL".equals(status)) {
                        setStyle("-fx-font-weight: bold;");
                    } else if ("OUT OF STOCK".equals(status)) {
                        // Keep OUT OF STOCK prominent with red text
                        setStyle("-fx-text-fill: #CF222E; -fx-font-weight: bold;");
                    } else {
                        // LOW
                        setStyle("-fx-text-fill: #C69026; -fx-font-weight: bold;");
                    }
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

        table.getColumns().addAll(idCol, studentCol, itemCol, sizeCol, qtyCol, priceCol, actionsCol);

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
    @SuppressWarnings("unused")
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
     * Opens an image in a modal window with zoom and pan capabilities
     */
    private void openImageModal(String imagePath, String fileName) {
        try {
            javafx.stage.Stage imageStage = new javafx.stage.Stage();
            imageStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            imageStage.setTitle("Image Proof - " + fileName);
            
            java.io.File imgFile = new java.io.File(imagePath);
            if (!imgFile.exists()) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Image Not Found");
                alert.setHeaderText(null);
                alert.setContentText("Image file not found.");
                alert.showAndWait();
                return;
            }
            
            javafx.scene.image.Image image = new javafx.scene.image.Image(imgFile.toURI().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            // Scale image to fit within reasonable window size
            double imgWidth = image.getWidth();
            double imgHeight = image.getHeight();
            double maxWidth = 1000;
            double maxHeight = 700;
            
            double scale = Math.min(maxWidth / imgWidth, maxHeight / imgHeight);
            if (scale < 1.0) {
                imageView.setFitWidth(imgWidth * scale);
                imageView.setFitHeight(imgHeight * scale);
            }
            
            // Enable zooming and panning
            javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane();
            scrollPane.setContent(imageView);
            scrollPane.setPannable(true);
            scrollPane.setStyle("-fx-background: #1F1F1F; -fx-background-color: #1F1F1F;");
            
            // Zoom controls
            double[] zoomLevel = {1.0};
            
            HBox controls = new HBox(10);
            controls.setAlignment(javafx.geometry.Pos.CENTER);
            controls.setPadding(new Insets(10));
            controls.setStyle("-fx-background-color: #F6F8FA; -fx-border-color: #D0D7DE; -fx-border-width: 1 0 0 0;");
            
            javafx.scene.control.Button zoomInBtn = new javafx.scene.control.Button("➕ Zoom In");
            javafx.scene.control.Button zoomOutBtn = new javafx.scene.control.Button("➖ Zoom Out");
            javafx.scene.control.Button resetBtn = new javafx.scene.control.Button("🔄 Reset");
            javafx.scene.control.Button closeBtn = new javafx.scene.control.Button("✖️ Close");
            
            String btnStyle = "-fx-font-size: 12px; -fx-background-color: #0969DA; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;";
            String btnHoverStyle = "-fx-font-size: 12px; -fx-background-color: #0860CA; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;";
            String closeBtnStyle = "-fx-font-size: 12px; -fx-background-color: #CF222E; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;";
            String closeBtnHoverStyle = "-fx-font-size: 12px; -fx-background-color: #A40E26; -fx-text-fill: white; -fx-padding: 6 16; -fx-cursor: hand; -fx-background-radius: 4;";
            
            zoomInBtn.setStyle(btnStyle);
            zoomInBtn.setOnMouseEntered(e -> zoomInBtn.setStyle(btnHoverStyle));
            zoomInBtn.setOnMouseExited(e -> zoomInBtn.setStyle(btnStyle));
            zoomInBtn.setOnAction(e -> {
                zoomLevel[0] += 0.2;
                imageView.setScaleX(zoomLevel[0]);
                imageView.setScaleY(zoomLevel[0]);
            });
            
            zoomOutBtn.setStyle(btnStyle);
            zoomOutBtn.setOnMouseEntered(e -> zoomOutBtn.setStyle(btnHoverStyle));
            zoomOutBtn.setOnMouseExited(e -> zoomOutBtn.setStyle(btnStyle));
            zoomOutBtn.setOnAction(e -> {
                if (zoomLevel[0] > 0.4) {
                    zoomLevel[0] -= 0.2;
                    imageView.setScaleX(zoomLevel[0]);
                    imageView.setScaleY(zoomLevel[0]);
                }
            });
            
            resetBtn.setStyle(btnStyle);
            resetBtn.setOnMouseEntered(e -> resetBtn.setStyle(btnHoverStyle));
            resetBtn.setOnMouseExited(e -> resetBtn.setStyle(btnStyle));
            resetBtn.setOnAction(e -> {
                zoomLevel[0] = 1.0;
                imageView.setScaleX(1.0);
                imageView.setScaleY(1.0);
            });
            
            closeBtn.setStyle(closeBtnStyle);
            closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtnHoverStyle));
            closeBtn.setOnMouseExited(e -> closeBtn.setStyle(closeBtnStyle));
            closeBtn.setOnAction(e -> imageStage.close());
            
            controls.getChildren().addAll(zoomInBtn, zoomOutBtn, resetBtn, closeBtn);
            
            javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
            root.setCenter(scrollPane);
            root.setBottom(controls);
            
            // Calculate window size based on image size
            double windowWidth = Math.min(image.getWidth() + 50, 1100);
            double windowHeight = Math.min(image.getHeight() + 100, 800);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, windowWidth, windowHeight);
            imageStage.setScene(scene);
            imageStage.showAndWait();
            
        } catch (Exception ex) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to open image: " + ex.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Getter methods for managers (used by reporting and other features)
     */
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    public ReservationManager getReservationManager() {
        return reservationManager;
    }
    
    public ReceiptManager getReceiptManager() {
        return receiptManager;
    }

}

