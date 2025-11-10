package gui.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gui.utils.AlertHelper;
import gui.utils.ControllerUtils;
import gui.utils.SceneManager;
import gui.views.LoginView;
import inventory.InventoryManager;
import inventory.Item;
import inventory.ReceiptManager;
import inventory.Reservation;
import inventory.ReservationManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import student.Student;
import utils.FileStorage;

/**
 * StudentDashboardController - Handles all student dashboard operations
 */
public class StudentDashboardController {

    private Student student;
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    private ReceiptManager receiptManager;
    private Runnable refreshCallback;
    private List<CartItem> cart;  // Shopping cart for bundle purchases with quantities
    private Runnable cartUpdateCallback;  // Callback to update cart badge

    /**
     * Inner class to represent an item in the cart with its quantity
     */
    private static class CartItem {
        private Item item;
        private int quantity;
        private boolean selected;
        
        public CartItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
            this.selected = true; // Default to selected
        }
        
        public Item getItem() { return item; }
        public int getQuantity() { return quantity; }
        public boolean isSelected() { return selected; }
        @SuppressWarnings("unused")
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public void setSelected(boolean selected) { this.selected = selected; }
        
        public double getTotalPrice() {
            return item.getPrice() * quantity;
        }
    }

    public StudentDashboardController(Student student) {
        this.student = student;
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();
        cart = new java.util.ArrayList<>();

        // Link receipt manager to reservation manager for synchronization
        reservationManager.setReceiptManager(receiptManager);
    }

    /**
     * Set callback to refresh the reservations view
     */
    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }
    
    /**
     * Create shop view
     */
    public Node createShopView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        // Welcome message
        Label welcomeLabel = new Label("Welcome, " + student.getFirstName() + "! 👋");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        welcomeLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label subtitleLabel = new Label("Browse available uniforms and specials for " + student.getCourse());
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        // Filter bar
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        
        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("All Items", "Uniforms", "Specials");
        categoryFilter.setValue("All Items");
        categoryFilter.setPrefWidth(150);
        
        ComboBox<String> genderFilter = new ComboBox<>();
        genderFilter.getItems().addAll("All Genders", "Male", "Female", "Unisex");
        genderFilter.setValue("All Genders");
        genderFilter.setPrefWidth(150);
        
        ComboBox<String> sizeFilter = new ComboBox<>();
        sizeFilter.getItems().addAll("All Sizes", "XS", "S", "M", "L", "XL", "XXL", "One Size");
        sizeFilter.setValue("All Sizes");
        sizeFilter.setPrefWidth(150);
        
        Button searchBtn = new Button("🔍 Search");
        styleActionButton(searchBtn, "#0969DA");
        
        filterBar.getChildren().addAll(filterLabel, categoryFilter, genderFilter, sizeFilter, searchBtn);
        
        // Items grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane itemsGrid = new FlowPane(20, 20);
        itemsGrid.setPadding(new Insets(10));
        
        // Get items for student's course (uniforms) and STI Special items ONLY
        List<Item> allItems = inventoryManager.getItemsByCourse(student.getCourse());
        List<Item> specialItems = inventoryManager.getItemsByCourse("STI Special");
        allItems.addAll(specialItems);
        
        // Display items with available stock initially
        List<Item> initialDisplayItems = allItems.stream()
            .filter(item -> item.getQuantity() > 0)
            .collect(Collectors.toList());
        
        if (initialDisplayItems.isEmpty()) {
            Label noItems = new Label("No items available for your course yet.");
            noItems.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            itemsGrid.getChildren().add(noItems);
        } else {
            for (Item item : initialDisplayItems) {
                VBox itemCard = createItemCard(item);
                itemsGrid.getChildren().add(itemCard);
            }
        }
        
        // Filter method
        Runnable applyFilters = () -> {
            String selectedCategory = categoryFilter.getValue();
            String selectedGender = genderFilter.getValue();
            String selectedSize = sizeFilter.getValue();
            
            List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    // Category filter - only show items from student's course or STI Special
                    boolean categoryMatch = true;
                    if (selectedCategory != null) {
                        if (selectedCategory.equals("Uniforms")) {
                            // Show only student's course uniforms
                            categoryMatch = item.getCourse().equals(student.getCourse());
                        } else if (selectedCategory.equals("Specials")) {
                            categoryMatch = item.getCourse().equals("STI Special");
                        }
                        // "All Items" shows student's course + STI Special (already in allItems)
                    }
                    
                    // Gender filter
                    boolean genderMatch = true;
                    if (selectedGender != null && !selectedGender.equals("All Genders")) {
                        String itemName = item.getName().toLowerCase();
                        if (selectedGender.equals("Male")) {
                            genderMatch = itemName.contains("(male)");
                        } else if (selectedGender.equals("Female")) {
                            genderMatch = itemName.contains("(female)");
                        } else if (selectedGender.equals("Unisex")) {
                            genderMatch = !itemName.contains("(male)") && !itemName.contains("(female)");
                        }
                    }
                    
                    // Size filter
                    boolean sizeMatch = true;
                    if (selectedSize != null && !selectedSize.equals("All Sizes")) {
                        sizeMatch = item.getSize().equalsIgnoreCase(selectedSize);
                    }
                    
                    return categoryMatch && genderMatch && sizeMatch;
                })
                .collect(Collectors.toList());
            
            // Update grid
            itemsGrid.getChildren().clear();
            if (filteredItems.isEmpty()) {
                Label noItems = new Label("No items found matching your filters.");
                noItems.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
                itemsGrid.getChildren().add(noItems);
            } else {
                for (Item item : filteredItems) {
                    VBox itemCard = createItemCard(item);
                    itemsGrid.getChildren().add(itemCard);
                }
            }
        };
        
        // Search/Filter button action
        searchBtn.setOnAction(e -> applyFilters.run());
        
        // Real-time filtering when ComboBox selection changes
        categoryFilter.setOnAction(e -> applyFilters.run());
        genderFilter.setOnAction(e -> applyFilters.run());
        sizeFilter.setOnAction(e -> applyFilters.run());
        
        scrollPane.setContent(itemsGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        container.getChildren().addAll(
            welcomeLabel,
            subtitleLabel,
            new Separator(),
            filterBar,
            scrollPane
        );
        
        return container;
    }
    
    /**
     * Create item card
     */
    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setPrefWidth(220);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        // Item name
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        nameLabel.setWrapText(true);
        
        // Item details
        Label codeLabel = new Label("Code: " + item.getCode());
        codeLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        // Category badge
        Label categoryLabel = new Label(item.getCourse().equals("STI Special") ? "🎉 Special" : "👕 Uniform");
        categoryLabel.setStyle(
            "-fx-background-color: " + (item.getCourse().equals("STI Special") ? "#DDF4FF" : "#DAFBE1") + ";" +
            "-fx-text-fill: " + (item.getCourse().equals("STI Special") ? "#0969DA" : "#1A7F37") + ";" +
            "-fx-padding: 4 8 4 8;" +
            "-fx-background-radius: 12px;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );
        
        Label sizeLabel = new Label("Size: " + item.getSize());
        sizeLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        Label priceLabel = new Label("₱" + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        // Stock status
        Label stockLabel;
        if (item.getQuantity() > 10) {
            stockLabel = new Label("✓ In Stock (" + item.getQuantity() + ")");
            stockLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px;");
        } else if (item.getQuantity() > 0) {
            stockLabel = new Label("⚠ Low Stock (" + item.getQuantity() + ")");
            stockLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px;");
        } else {
            stockLabel = new Label("✗ Out of Stock");
            stockLabel.setStyle("-fx-text-fill: #CF222E; -fx-font-size: 12px;");
        }
        
        // Buttons
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        
        // Add to Cart button
        Button addToCartBtn = new Button("🛒 Add");
        addToCartBtn.setPrefWidth(100);
        addToCartBtn.setPrefHeight(35);
        addToCartBtn.setDisable(item.getQuantity() == 0);
        
        if (item.getQuantity() > 0) {
            addToCartBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            addToCartBtn.setStyle(
                "-fx-background-color: #6E7781;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 6px;"
            );
        }
        
        addToCartBtn.setOnAction(e -> handleAddToCart(item));
        
        // Reserve Now button (single item)
        Button reserveBtn = new Button("Reserve");
        reserveBtn.setPrefWidth(100);
        reserveBtn.setPrefHeight(35);
        reserveBtn.setDisable(item.getQuantity() == 0);
        
        if (item.getQuantity() > 0) {
            reserveBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            reserveBtn.setStyle(
                "-fx-background-color: #6E7781;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 6px;"
            );
        }
        
        reserveBtn.setOnAction(e -> handleReserveItem(item));
        
        buttonBox.getChildren().addAll(addToCartBtn, reserveBtn);
        
        card.getChildren().addAll(
            nameLabel,
            categoryLabel,
            codeLabel,
            sizeLabel,
            new Separator(),
            priceLabel,
            stockLabel,
            buttonBox
        );
        
        return card;
    }
    
    /**
     * Handle add to cart
     */
    private void handleAddToCart(Item item) {
        if (item.getQuantity() == 0) {
            AlertHelper.showError("Out of Stock", "This item is currently out of stock.");
            return;
        }
        
        // Check if item already in cart
        CartItem existingCartItem = cart.stream()
            .filter(cartItem -> cartItem.getItem().getCode() == item.getCode() && 
                               cartItem.getItem().getSize().equals(item.getSize()))
            .findFirst()
            .orElse(null);
        
        // Show quantity selection dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");
        
        if (existingCartItem != null) {
            dialog.setHeaderText("Add More: " + item.getName() + " (" + item.getSize() + ")\n" +
                                "Currently in cart: " + existingCartItem.getQuantity());
        } else {
            dialog.setHeaderText("Add to Cart: " + item.getName() + " (" + item.getSize() + ")");
        }

        ButtonType addButtonType = new ButtonType("Add to Cart", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Show item details
        Label priceLabel = new Label("Price: ₱" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 13px;");
        Label stockLabel = new Label("Available: " + item.getQuantity());
        stockLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: -color-fg-muted;");

        // Quantity spinner
        Spinner<Integer> qtySpinner = new Spinner<>(1, item.getQuantity(), 1);
        qtySpinner.setEditable(true);
        qtySpinner.setPrefWidth(100);

        // Total price label
        Label totalLabel = new Label("Total: ₱" + String.format("%.2f", item.getPrice()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Update total when quantity changes
        qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            double total = item.getPrice() * newVal;
            totalLabel.setText("Total: ₱" + String.format("%.2f", total));
        });

        grid.add(priceLabel, 0, 0);
        grid.add(stockLabel, 0, 1);
        grid.add(new Label("Quantity:"), 0, 2);
        grid.add(qtySpinner, 1, 2);
        grid.add(totalLabel, 0, 3, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return qtySpinner.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(quantity -> {
            if (existingCartItem != null) {
                // Item already in cart - increment quantity
                int newQuantity = existingCartItem.getQuantity() + quantity;
                existingCartItem.setQuantity(newQuantity);
                AlertHelper.showSuccess("Cart Updated", 
                    "Added " + quantity + " more " + item.getName() + " (" + item.getSize() + ")\n" +
                    "New quantity in cart: " + newQuantity);
            } else {
                // New item - add to cart
                CartItem cartItem = new CartItem(item, quantity);
                cart.add(cartItem);
                AlertHelper.showSuccess("Added to Cart", 
                    quantity + "x " + item.getName() + " (" + item.getSize() + ") added to cart!\n" +
                    "Cart items: " + cart.size());
            }
            
            // Update cart badge if callback is set
            if (cartUpdateCallback != null) {
                cartUpdateCallback.run();
            }
            
            // Refresh cart view to show updated quantities
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
    }
    
    /**
     * Get cart size
     */
    public int getCartSize() {
        return cart.size();
    }
    
    /**
     * Set cart update callback
     */
    public void setCartUpdateCallback(Runnable callback) {
        this.cartUpdateCallback = callback;
    }
    
    /**
     * Clear cart
     */
    public void clearCart() {
        cart.clear();
        if (cartUpdateCallback != null) {
            cartUpdateCallback.run();
        }
    }
    
    /**
     * Handle reserve item
     */
    private void handleReserveItem(Item item) {
        // Create reservation dialog
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Reserve Item");
        dialog.setHeaderText("Reserve: " + item.getName());

        ButtonType reserveButtonType = new ButtonType("Reserve", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reserveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Show item details
        Label itemLabel = new Label(item.getName());
        itemLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label priceLabel = new Label("Price: ₱" + String.format("%.2f", item.getPrice()));
        Label sizeLabel = new Label("Size: " + item.getSize());
        Label stockLabel = new Label("Available: " + item.getQuantity());

        // Quantity selector
        Spinner<Integer> qtySpinner = new Spinner<>(1, item.getQuantity(), 1);
        qtySpinner.setEditable(true);

        // Total price label
        Label totalLabel = new Label("Total: ₱" + String.format("%.2f", item.getPrice()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Update total when quantity changes
        qtySpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            double total = item.getPrice() * newVal;
            totalLabel.setText("Total: ₱" + String.format("%.2f", total));
        });

        grid.add(itemLabel, 0, 0, 2, 1);
        grid.add(priceLabel, 0, 1);
        grid.add(sizeLabel, 1, 1);
        grid.add(stockLabel, 0, 2, 2, 1);
        grid.add(new Label("Quantity:"), 0, 3);
        grid.add(qtySpinner, 1, 3);
        grid.add(totalLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reserveButtonType) {
                int quantity = qtySpinner.getValue();
                double totalPrice = item.getPrice() * quantity;

                // Create reservation
                Reservation reservation = reservationManager.createReservation(
                    student.getFullName(),
                    student.getStudentId(),
                    student.getCourse(),
                    item.getCode(),
                    item.getName(),
                    item.getSize(),
                    quantity,
                    totalPrice
                );

                return reservation;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reservation -> {
            if (reservation != null) {
                AlertHelper.showSuccess("Success",
                    "Reservation created successfully!\n\n" +
                    "Reservation ID: " + reservation.getReservationId() + "\n" +
                    "Status: " + reservation.getStatus() + "\n\n" +
                    "Please wait for admin approval.");
            } else {
                AlertHelper.showError("Error", "Failed to create reservation. Item may be out of stock.");
            }
        });
    }
    
    /**
     * Create cart view - shows all items in cart for bundle purchase
     */
    public Node createCartView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("🛒 Shopping Cart");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        if (cart.isEmpty()) {
            VBox emptyBox = new VBox(15);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("🛒 Your cart is empty");
            emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            emptyLabel.setStyle("-fx-text-fill: -color-fg-muted;");
            
            Label hintLabel = new Label("Add items to your cart from the Shop to reserve multiple items at once!");
            hintLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            hintLabel.setWrapText(true);
            hintLabel.setMaxWidth(400);
            hintLabel.setAlignment(Pos.CENTER);
            
            emptyBox.getChildren().addAll(emptyLabel, hintLabel);
            container.getChildren().addAll(titleLabel, emptyBox);
        } else {
            // Select All checkbox
            HBox selectAllBox = new HBox(10);
            selectAllBox.setAlignment(Pos.CENTER_LEFT);
            CheckBox selectAllCheckbox = new CheckBox("Select All");
            selectAllCheckbox.setSelected(true); // Default all selected
            selectAllCheckbox.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold; -fx-font-size: 13px;");
            selectAllBox.getChildren().add(selectAllCheckbox);
            
            // Cart items list
            VBox cartItemsList = new VBox(15);
            
            // Calculate selected items totals
            double selectedTotalPrice = 0;
            int selectedTotalQuantity = 0;
            int selectedItemCount = 0;
            
            for (CartItem cartItem : cart) {
                if (cartItem.isSelected()) {
                    selectedTotalPrice += cartItem.getTotalPrice();
                    selectedTotalQuantity += cartItem.getQuantity();
                    selectedItemCount++;
                }
            }
            
            for (CartItem cartItem : cart) {
                HBox itemRow = createCartItemRow(cartItem, () -> {
                    // Refresh callback when item selection changes
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                });
                cartItemsList.getChildren().add(itemRow);
            }
            
            // Select All checkbox action
            selectAllCheckbox.setOnAction(e -> {
                boolean selectAll = selectAllCheckbox.isSelected();
                for (CartItem cartItem : cart) {
                    cartItem.setSelected(selectAll);
                }
                // Refresh cart view
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });
            
            ScrollPane scrollPane = new ScrollPane(cartItemsList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            // Summary panel
            VBox summaryPanel = new VBox(15);
            summaryPanel.setPadding(new Insets(20));
            summaryPanel.setStyle(
                "-fx-background-color: -color-bg-subtle;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;"
            );
            
            Label summaryTitle = new Label("Order Summary");
            summaryTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            summaryTitle.setStyle("-fx-text-fill: -color-fg-default;");
            
            HBox itemsCount = new HBox();
            itemsCount.setAlignment(Pos.CENTER_LEFT);
            Label itemsLabel = new Label("Selected Items:");
            itemsLabel.setStyle("-fx-text-fill: -color-fg-default;");
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Label itemsValue = new Label(selectedItemCount + " type(s), " + selectedTotalQuantity + " item(s) total");
            itemsValue.setStyle("-fx-text-fill: -color-fg-muted;");
            itemsCount.getChildren().addAll(itemsLabel, spacer1, itemsValue);
            
            HBox totalRow = new HBox();
            totalRow.setAlignment(Pos.CENTER_LEFT);
            Label totalLabel = new Label("Total:");
            totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            totalLabel.setStyle("-fx-text-fill: -color-fg-default;");
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            Label totalValue = new Label("₱" + String.format("%.2f", selectedTotalPrice));
            totalValue.setFont(Font.font("System", FontWeight.BOLD, 20));
            totalValue.setStyle("-fx-text-fill: #1A7F37;");
            totalRow.getChildren().addAll(totalLabel, spacer2, totalValue);
            
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER);
            
            Button clearBtn = new Button("Clear Cart");
            clearBtn.setPrefWidth(150);
            clearBtn.setPrefHeight(40);
            clearBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: #CF222E;" +
                "-fx-border-width: 2px;" +
                "-fx-text-fill: #CF222E;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-border-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            clearBtn.setOnAction(e -> {
                clearCart();
                // Refresh cart view
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });
            
            Button reserveSelectedBtn = new Button("Reserve Selected");
            reserveSelectedBtn.setPrefWidth(200);
            reserveSelectedBtn.setPrefHeight(40);
            reserveSelectedBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            reserveSelectedBtn.setOnAction(e -> handleReserveSelected());
            
            // Disable reserve button if no items selected
            reserveSelectedBtn.setDisable(selectedItemCount == 0);
            
            buttonBox.getChildren().addAll(clearBtn, reserveSelectedBtn);
            
            summaryPanel.getChildren().addAll(
                summaryTitle,
                new Separator(),
                itemsCount,
                totalRow,
                new Separator(),
                buttonBox
            );
            
            container.getChildren().addAll(titleLabel, selectAllBox, scrollPane, summaryPanel);
        }
        
        return container;
    }
    
    /**
     * Create cart item row with selection checkbox
     */
    private HBox createCartItemRow(CartItem cartItem, Runnable selectionCallback) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15));
        row.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;"
        );
        
        Item item = cartItem.getItem();
        
        // Selection checkbox
        CheckBox selectCheckbox = new CheckBox();
        selectCheckbox.setSelected(cartItem.isSelected());
        selectCheckbox.setOnAction(e -> {
            cartItem.setSelected(selectCheckbox.isSelected());
            if (selectionCallback != null) {
                selectionCallback.run();
            }
        });
        
        VBox itemInfo = new VBox(5);
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label detailsLabel = new Label("Code: " + item.getCode() + " | Size: " + item.getSize());
        detailsLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        // Quantity control with +/- buttons
        HBox qtyBox = new HBox(8);
        qtyBox.setAlignment(Pos.CENTER_LEFT);
        
        Label qtyTextLabel = new Label("Quantity:");
        qtyTextLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 12px;");
        
        Button decreaseBtn = new Button("−");
        decreaseBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4px;" +
            "-fx-min-width: 28px;" +
            "-fx-min-height: 28px;" +
            "-fx-cursor: hand;"
        );
        
        Label qtyLabel = new Label(cartItem.getQuantity() + "x");
        qtyLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 40px; -fx-alignment: center;");
        
        Button increaseBtn = new Button("+");
        increaseBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4px;" +
            "-fx-min-width: 28px;" +
            "-fx-min-height: 28px;" +
            "-fx-cursor: hand;"
        );
        
        qtyBox.getChildren().addAll(qtyTextLabel, decreaseBtn, qtyLabel, increaseBtn);
        
        itemInfo.getChildren().addAll(nameLabel, detailsLabel, qtyBox);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        VBox priceBox = new VBox(3);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label unitPriceLabel = new Label("₱" + String.format("%.2f", item.getPrice()) + " each");
        unitPriceLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        
        Label totalPriceLabel = new Label("₱" + String.format("%.2f", cartItem.getTotalPrice()));
        totalPriceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        totalPriceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        priceBox.getChildren().addAll(unitPriceLabel, totalPriceLabel);
        
        // Decrease quantity button action
        decreaseBtn.setOnAction(e -> {
            int currentQty = cartItem.getQuantity();
            if (currentQty > 1) {
                cartItem.setQuantity(currentQty - 1);
                qtyLabel.setText(cartItem.getQuantity() + "x");
                totalPriceLabel.setText("₱" + String.format("%.2f", cartItem.getTotalPrice()));
                
                // Update cart badge and summary
                if (cartUpdateCallback != null) {
                    cartUpdateCallback.run();
                }
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            }
        });
        
        // Increase quantity button action
        increaseBtn.setOnAction(e -> {
            int currentQty = cartItem.getQuantity();
            // Check if item has enough stock
            if (currentQty < item.getQuantity()) {
                cartItem.setQuantity(currentQty + 1);
                qtyLabel.setText(cartItem.getQuantity() + "x");
                totalPriceLabel.setText("₱" + String.format("%.2f", cartItem.getTotalPrice()));
                
                // Update cart badge and summary
                if (cartUpdateCallback != null) {
                    cartUpdateCallback.run();
                }
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } else {
                AlertHelper.showWarning("Stock Limit", 
                    "Cannot add more. Available stock: " + item.getQuantity());
            }
        });
        
        Button removeBtn = new Button("✕");
        removeBtn.setStyle(
            "-fx-background-color: #CF222E;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 15px;" +
            "-fx-min-width: 30px;" +
            "-fx-min-height: 30px;" +
            "-fx-cursor: hand;"
        );
        removeBtn.setOnAction(e -> {
            cart.remove(cartItem);
            if (cartUpdateCallback != null) {
                cartUpdateCallback.run();
            }
            // Refresh cart view
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });
        
        row.getChildren().addAll(selectCheckbox, itemInfo, priceBox, removeBtn);
        return row;
    }
    
    /**
     * Handle reserve selected items only
     */
    private void handleReserveSelected() {
        // Get only selected items
        List<CartItem> selectedItems = cart.stream()
            .filter(CartItem::isSelected)
            .collect(java.util.stream.Collectors.toList());
        
        if (selectedItems.isEmpty()) {
            AlertHelper.showWarning("No Items Selected", "Please select at least one item to reserve.");
            return;
        }
        
        // Check if all selected items are still in stock with requested quantities
        for (CartItem cartItem : selectedItems) {
            Item item = cartItem.getItem();
            int requestedQty = cartItem.getQuantity();
            
            // Check by finding item in inventory list
            List<Item> courseItems = inventoryManager.getItemsByCourse(item.getCourse());
            boolean found = false;
            int availableQty = 0;
            
            for (Item invItem : courseItems) {
                if (invItem.getCode() == item.getCode() && 
                    invItem.getSize().equals(item.getSize())) {
                    availableQty = invItem.getQuantity();
                    if (availableQty >= requestedQty) {
                        found = true;
                    }
                    break;
                }
            }
            
            if (!found) {
                AlertHelper.showError("Insufficient Stock", 
                    item.getName() + " (Size: " + item.getSize() + ")\n" +
                    "Requested: " + requestedQty + "x\n" +
                    "Available: " + availableQty + "x\n\n" +
                    "Please adjust the quantity or deselect the item.");
                return;
            }
        }
        
        // Generate unique bundle ID only if there are multiple selected items (2+)
        String bundleId = null;
        if (selectedItems.size() > 1) {
            bundleId = "BUNDLE-" + student.getStudentId() + "-" + System.currentTimeMillis();
        }
        
        // Create reservations for selected cart items
        int successCount = 0;
        int totalItems = 0;
        StringBuilder results = new StringBuilder();
        
        if (bundleId != null) {
            results.append("Bundle Reservation Results:\n\n");
        } else {
            results.append("Reservation Results:\n\n");
        }
        
        for (CartItem cartItem : selectedItems) {
            Item item = cartItem.getItem();
            int quantity = cartItem.getQuantity();
            double totalPrice = cartItem.getTotalPrice();
            
            Reservation reservation;
            if (bundleId != null) {
                // Multiple items - create with bundleId
                reservation = reservationManager.createReservation(
                    student.getFullName(),
                    student.getStudentId(),
                    student.getCourse(),
                    item.getCode(),
                    item.getName(),
                    item.getSize(),
                    quantity,
                    totalPrice,
                    bundleId
                );
            } else {
                // Single item - create without bundleId
                reservation = reservationManager.createReservation(
                    student.getFullName(),
                    student.getStudentId(),
                    student.getCourse(),
                    item.getCode(),
                    item.getName(),
                    item.getSize(),
                    quantity,
                    totalPrice
                );
            }
            
            if (reservation != null) {
                successCount++;
                totalItems += quantity;
                results.append("✓ ").append(quantity).append("x ").append(item.getName())
                       .append(" (").append(item.getSize()).append(")")
                       .append(" - ID: ").append(reservation.getReservationId())
                       .append("\n");
            } else {
                results.append("✗ ").append(quantity).append("x ").append(item.getName())
                       .append(" - Failed\n");
            }
        }
        
        if (successCount > 0) {
            String title = bundleId != null ? "Bundle Reserved!" : "Item Reserved!";
            String message = results.toString() + "\n";
            
            if (bundleId != null) {
                message += successCount + " type(s) of items, " + totalItems + " total items reserved!\n" +
                          "Bundle ID: " + bundleId + "\n";
            } else {
                message += totalItems + " item(s) reserved!\n" +
                          "Reservation ID: " + results.toString().split("ID: ")[1].split("\n")[0].trim() + "\n";
            }
            
            message += "Status: Pending Approval\n\n" +
                      "Please wait for admin approval.";
            
            AlertHelper.showSuccess(title, message);
            
            // Remove only selected items from cart
            cart.removeAll(selectedItems);
            
            // Update cart badge
            if (cartUpdateCallback != null) {
                cartUpdateCallback.run();
            }
            
            // Refresh views
            if (refreshCallback != null) {
                refreshCallback.run();
            }
        } else {
            AlertHelper.showError("Reservation Failed", 
                "Failed to reserve any items. Please try again.");
        }
    }
    
    /**
     * Create my reservations view
     */
    public Node createMyReservationsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("My Reservations");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        // Reload reservations from file to ensure we have latest data
        List<Reservation> allReservations = FileStorage.loadReservations();
        
        // Get student's reservations and deduplicate bundles
        List<Reservation> myReservations = allReservations.stream()
            .filter(r -> r.getStudentId().equals(student.getStudentId()))
            .collect(Collectors.toList());
        
        // Deduplicate bundles - show only one card per bundle
        List<Reservation> deduplicatedReservations = ControllerUtils.getDeduplicatedReservations(myReservations);
        
        if (deduplicatedReservations.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("📋 No reservations yet");
            emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            emptyLabel.setStyle("-fx-text-fill: -color-fg-muted;");
            
            Label hintLabel = new Label("Start shopping to create your first reservation!");
            hintLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            
            emptyBox.getChildren().addAll(emptyLabel, hintLabel);
            container.getChildren().addAll(titleLabel, emptyBox);
        } else {
            VBox reservationsList = new VBox(15);
            
            for (Reservation r : deduplicatedReservations) {
                VBox reservationCard = createReservationCard(r);
                reservationsList.getChildren().add(reservationCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(reservationsList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            container.getChildren().addAll(titleLabel, scrollPane);
        }
        
        return container;
    }
    
    /**
     * Create claim items view - Shows items that need pickup request or are ready for pickup
     */
    public Node createClaimItemsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Claim Items");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label subtitleLabel = new Label("Request pickup approval or claim approved items");
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        subtitleLabel.setWrapText(true);
        
        // Reload reservations from file to ensure we have latest data
        List<Reservation> allReservations = FileStorage.loadReservations();
        
        // Get student's reservations in various pickup-related statuses
        List<Reservation> pickupItems = allReservations.stream()
            .filter(r -> r.getStudentId().equals(student.getStudentId()))
            .filter(r -> "PAID - AWAITING PICKUP APPROVAL".equals(r.getStatus()) || 
                        "PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(r.getStatus()) ||
                        "APPROVED FOR PICKUP".equals(r.getStatus()))
            .filter(r -> {
                // For bundles, verify all items in the bundle have the same status
                if (r.isPartOfBundle()) {
                    String bundleId = r.getBundleId();
                    String expectedStatus = r.getStatus();
                    return allReservations.stream()
                        .filter(res -> bundleId.equals(res.getBundleId()))
                        .allMatch(res -> expectedStatus.equals(res.getStatus()));
                }
                return true;
            })
            .collect(Collectors.toList());
        
        // Deduplicate bundles - show only one card per bundle
        List<Reservation> deduplicatedReservations = ControllerUtils.getDeduplicatedReservations(pickupItems);
        
        if (deduplicatedReservations.isEmpty()) {
            VBox emptyBox = new VBox(20);
            emptyBox.setAlignment(Pos.CENTER);
            emptyBox.setPadding(new Insets(50));
            
            Label emptyLabel = new Label("📦 No items ready for pickup");
            emptyLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            emptyLabel.setStyle("-fx-text-fill: -color-fg-muted;");
            
            Label hintLabel = new Label("Complete payment for your reservations first, then items will appear here for claiming.");
            hintLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            hintLabel.setWrapText(true);
            
            emptyBox.getChildren().addAll(emptyLabel, hintLabel);
            container.getChildren().addAll(titleLabel, subtitleLabel, new Separator(), emptyBox);
        } else {
            VBox itemsList = new VBox(15);
            
            for (Reservation r : deduplicatedReservations) {
                VBox claimCard = createClaimItemCard(r);
                itemsList.getChildren().add(claimCard);
            }
            
            ScrollPane scrollPane = new ScrollPane(itemsList);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            VBox.setVgrow(scrollPane, Priority.ALWAYS);
            
            container.getChildren().addAll(titleLabel, subtitleLabel, new Separator(), scrollPane);
        }
        
        return container;
    }
    
    /**
     * Create claim item card - simplified card for claiming items
     */
    private VBox createClaimItemCard(Reservation r) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: #1A7F37;" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 8px;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Show bundle ID or reservation ID
        String orderId = r.isPartOfBundle() ? r.getBundleId() : "Reservation #" + r.getReservationId();
        Label idLabel = new Label(orderId);
        idLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        idLabel.setStyle("-fx-text-fill: -color-fg-default;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label("✓ READY FOR PICKUP");
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle(
            "-fx-background-color: #1A7F37;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 4px;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(idLabel, spacer, statusLabel);

        // Show bundle info or single item info
        VBox itemsBox = new VBox(5);
        if (r.isPartOfBundle()) {
            // For bundles, show all items in the bundle
            String bundleId = r.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .collect(Collectors.toList());
            
            Label bundleLabel = new Label("📦 Bundle Order (" + bundleItems.size() + " items)");
            bundleLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-weight: bold; -fx-font-size: 14px;");
            itemsBox.getChildren().add(bundleLabel);
            
            for (Reservation item : bundleItems) {
                Label itemLabel = new Label("• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x)");
                itemLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px;");
                itemsBox.getChildren().add(itemLabel);
            }
        } else {
            Label itemLabel = new Label(r.getItemName() + " - " + r.getSize());
            itemLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 14px;");
            itemsBox.getChildren().add(itemLabel);
        }

        // Calculate total quantity and price for bundles
        int totalQty = r.getQuantity();
        double totalPrice = r.getTotalPrice();
        
        if (r.isPartOfBundle()) {
            String bundleId = r.getBundleId();
            totalQty = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .mapToInt(Reservation::getQuantity)
                .sum();
            totalPrice = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .mapToDouble(Reservation::getTotalPrice)
                .sum();
        }

        Label qtyLabel = new Label("Total Quantity: " + totalQty + "x");
        qtyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        Label priceLabel = new Label("Total Paid: ₱" + String.format("%.2f", totalPrice));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        // Status and action button based on current status
        String status = r.getStatus();
        VBox actionBox = new VBox(10);
        
        if ("PAID - AWAITING PICKUP APPROVAL".equals(status)) {
            // Student needs to request pickup
            Label awaitingLabel = new Label("📋 Status: Payment Completed");
            awaitingLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Button requestBtn = new Button("📦 Request Pickup");
            requestBtn.setMaxWidth(Double.MAX_VALUE);
            requestBtn.setPrefHeight(40);
            requestBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            requestBtn.setOnAction(e -> handleRequestPickup(r));
            
            Label awaitingNote = new Label("💡 Click to request admin approval for pickup");
            awaitingNote.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            awaitingNote.setWrapText(true);
            
            actionBox.getChildren().addAll(awaitingLabel, requestBtn, awaitingNote);
            
        } else if ("PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(status)) {
            // Waiting for admin approval
            Label pendingLabel = new Label("⏳ Status: Waiting for Admin Approval");
            pendingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Button pendingBtn = new Button("⏳ Pending Admin Approval");
            pendingBtn.setMaxWidth(Double.MAX_VALUE);
            pendingBtn.setPrefHeight(40);
            pendingBtn.setStyle(
                "-fx-background-color: #BF8700;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.7;"
            );
            pendingBtn.setDisable(true);
            
            Label pendingNote = new Label("⏱️ Please wait for admin to approve your pickup request");
            pendingNote.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            pendingNote.setWrapText(true);
            
            actionBox.getChildren().addAll(pendingLabel, pendingBtn, pendingNote);
            
        } else if ("APPROVED FOR PICKUP".equals(status)) {
            // Approved - student can claim
            Label approvedLabel = new Label("✅ Status: Approved for Pickup");
            approvedLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Button claimBtn = new Button("✓ Claim Item");
            claimBtn.setMaxWidth(Double.MAX_VALUE);
            claimBtn.setPrefHeight(40);
            claimBtn.setStyle(
                "-fx-background-color: #1A7F37;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            claimBtn.setOnAction(e -> handleClaimItem(r));
            
            Label approvedNote = new Label("💡 Confirm you've received the item from admin");
            approvedNote.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            approvedNote.setWrapText(true);
            
            actionBox.getChildren().addAll(approvedLabel, claimBtn, approvedNote);
        }

        card.getChildren().addAll(header, new Separator(), itemsBox, qtyLabel, priceLabel, actionBox);

        return card;
    }
    
    /**
     * Handle pickup request (student requests admin approval)
     */
    private void handleRequestPickup(Reservation r) {
        String itemDescription = r.isPartOfBundle() ? 
            "bundle order (" + r.getBundleId() + ")" : 
            r.getItemName() + " - " + r.getSize();
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Request Pickup");
        confirmAlert.setHeaderText("Request Pickup Approval");
        confirmAlert.setContentText(
            "Request admin approval to pickup this " + itemDescription + "?\n\n" +
            "After approval, you'll be able to claim your item.\n\n" +
            "Total Paid: ₱" + String.format("%.2f", r.getTotalPrice())
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success;
                if (r.isPartOfBundle()) {
                    // Request pickup for all items in the bundle
                    String bundleId = r.getBundleId();
                    List<Reservation> bundleItems = FileStorage.loadReservations().stream()
                        .filter(res -> bundleId.equals(res.getBundleId()))
                        .collect(Collectors.toList());
                    
                    success = true;
                    for (Reservation item : bundleItems) {
                        if (!reservationManager.requestPickup(item.getReservationId())) {
                            success = false;
                            break;
                        }
                    }
                } else {
                    success = reservationManager.requestPickup(r.getReservationId());
                }
                
                if (success) {
                    // Refresh the claim items view
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                    
                    AlertHelper.showSuccess("Request Sent", 
                        "Pickup request submitted successfully!\n\n" +
                        "Please wait for admin approval.");
                } else {
                    AlertHelper.showError("Error", "Failed to send pickup request. Please try again.");
                }
            }
        });
    }
    
    /**
     * Handle claiming an item (marking as picked up)
     */
    private void handleClaimItem(Reservation r) {
        String itemDescription = r.isPartOfBundle() ? 
            "bundle order (" + r.getBundleId() + ")" : 
            r.getItemName() + " - " + r.getSize();
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Claim Item");
        confirmAlert.setHeaderText("Confirm Item Claim");
        confirmAlert.setContentText(
            "Are you sure you want to claim this " + itemDescription + "?\n\n" +
            "By claiming, you confirm that:\n" +
            "• You have received the item(s)\n" +
            "• The item(s) are in good condition\n" +
            "• You have 10 days to request a return if there are any issues\n\n" +
            "Total: ₱" + String.format("%.2f", r.getTotalPrice())
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // If it's a bundle, mark all items in the bundle as picked up
                boolean success;
                if (r.isPartOfBundle()) {
                    String bundleId = r.getBundleId();
                    List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                        .filter(res -> bundleId.equals(res.getBundleId()))
                        .collect(Collectors.toList());
                    
                    success = true;
                    for (Reservation item : bundleItems) {
                        if (!reservationManager.markAsPickedUp(item.getReservationId())) {
                            success = false;
                            break;
                        }
                    }
                } else {
                    success = reservationManager.markAsPickedUp(r.getReservationId());
                }
                
                if (success) {
                    AlertHelper.showSuccess("Success",
                        "Item claimed successfully! ✓\n\n" +
                        "The item has been marked as picked up.\n" +
                        "You have 10 days to request a return if there are any issues.\n\n" +
                        "Check 'My Reservations' to view status or request a return.");
                    // Refresh the claim items view
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }
                } else {
                    AlertHelper.showError("Error", "Failed to claim item. Please try again or contact support.");
                }
            }
        });
    }
    
    /**
     * Create reservation card
     */
    private VBox createReservationCard(Reservation r) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-cursor: hand;"
        );
        
        // Make card clickable to open details/return dialog
        card.setOnMouseClicked(event -> handleCardClick(r));
        
        // Add hover effect
        card.setOnMouseEntered(event -> {
            card.setStyle(
                "-fx-background-color: -color-bg-subtle;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: -color-accent-emphasis;" +
                "-fx-border-width: 2px;" +
                "-fx-border-radius: 8px;" +
                "-fx-cursor: hand;"
            );
        });
        
        card.setOnMouseExited(event -> {
            card.setStyle(
                "-fx-background-color: -color-bg-subtle;" +
                "-fx-background-radius: 8px;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 8px;" +
                "-fx-cursor: hand;"
            );
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Show bundle ID or reservation ID
        String orderId = r.isPartOfBundle() ? r.getBundleId() : "Reservation #" + r.getReservationId();
        Label idLabel = new Label(orderId);
        idLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        idLabel.setStyle("-fx-text-fill: -color-fg-default;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label(r.getStatus());
        statusLabel.setPadding(new Insets(5, 10, 5, 10));
        statusLabel.setStyle(
            "-fx-background-color: -color-accent-subtle;" +
            "-fx-text-fill: -color-accent-fg;" +
            "-fx-background-radius: 4px;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(idLabel, spacer, statusLabel);

        // Show bundle info or single item info
        VBox itemsBox = new VBox(5);
        if (r.isPartOfBundle()) {
            // For bundles, show ALL items including returned ones with status
            String bundleId = r.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .collect(Collectors.toList());
            
            Label bundleLabel = new Label("📦 Bundle Order (" + bundleItems.size() + " items)");
            bundleLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-weight: bold; -fx-font-size: 14px;");
            itemsBox.getChildren().add(bundleLabel);
            
            for (Reservation item : bundleItems) {
                String statusTag = "";
                String statusColor = "-color-fg-default";
                
                if (item.getStatus().contains("RETURNED")) {
                    statusTag = " (Refunded)";
                    statusColor = "#656D76"; // Gray for returned
                } else if ("COMPLETED".equals(item.getStatus())) {
                    statusTag = " (Completed)";
                    statusColor = "#1A7F37"; // Green for completed
                } else if (item.getStatus().contains("RETURN REQUESTED")) {
                    statusTag = " (Return Requested)";
                    statusColor = "#BF8700"; // Orange for pending return
                }
                
                Label itemLabel = new Label("• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x)" + statusTag);
                itemLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 13px;");
                itemsBox.getChildren().add(itemLabel);
            }
        } else {
            Label itemLabel = new Label(r.getItemName() + " - " + r.getSize());
            itemLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 14px;");
            itemsBox.getChildren().add(itemLabel);
        }

        // Calculate total quantity and price for bundles (exclude returned items from totals)
        int totalQty = r.getQuantity();
        double totalPrice = r.getTotalPrice();
        int activeItemCount = 0; // Count of non-returned items
        
        if (r.isPartOfBundle()) {
            String bundleId = r.getBundleId();
            List<Reservation> activeItems = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .filter(res -> !res.getStatus().contains("RETURNED")) // Only count non-returned
                .collect(Collectors.toList());
            
            activeItemCount = activeItems.size();
            totalQty = activeItems.stream().mapToInt(Reservation::getQuantity).sum();
            totalPrice = activeItems.stream().mapToDouble(Reservation::getTotalPrice).sum();
        }

        Label qtyLabel = new Label("Total Quantity: " + totalQty + "x");
        qtyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        Label priceLabel = new Label("Total: ₱" + String.format("%.2f", totalPrice));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        // Add note about returned items if applicable
        VBox cardContent = new VBox(5);
        if (r.isPartOfBundle() && activeItemCount == 0) {
            Label allReturnedNote = new Label("All items in this bundle have been refunded");
            allReturnedNote.setStyle("-fx-text-fill: #656D76; -fx-font-size: 12px; -fx-font-style: italic;");
            cardContent.getChildren().addAll(header, new Separator(), itemsBox, allReturnedNote);
        } else {
            cardContent.getChildren().addAll(header, new Separator(), itemsBox, qtyLabel, priceLabel);
        }

        card.getChildren().addAll(cardContent.getChildren());

        // Add status indicators based on reservation state
        if ("PENDING".equals(r.getStatus()) || "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
            Label pendingLabel = new Label("⏳ Click to view details or cancel");
            pendingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(pendingLabel);
        } else if ("PAID - AWAITING PICKUP APPROVAL".equals(r.getStatus())) {
            Label pickupLabel = new Label("📋 Payment completed! Go to 'Claim Items' to request pickup");
            pickupLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
            card.getChildren().add(pickupLabel);
        } else if ("PICKUP REQUESTED - AWAITING ADMIN APPROVAL".equals(r.getStatus())) {
            Label awaitingLabel = new Label("⏳ Pickup request sent - Waiting for admin approval");
            awaitingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(awaitingLabel);
        } else if ("APPROVED FOR PICKUP".equals(r.getStatus())) {
            Label approvedLabel = new Label("✅ Approved! Go to 'Claim Items' to pick up");
            approvedLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
            card.getChildren().add(approvedLabel);
        } else if ("COMPLETED".equals(r.getStatus()) && r.isEligibleForReturn()) {
            Label returnLabel = new Label("↩ Click to request return (" + r.getDaysUntilReturnExpires() + " days left)");
            returnLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
            card.getChildren().add(returnLabel);
        } else if (r.isPartOfBundle() && hasCompletedItems(r.getBundleId())) {
            // Bundle with some completed items that can be returned
            Label returnLabel = new Label("↩ Click to view and return eligible items");
            returnLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
            card.getChildren().add(returnLabel);
        } else if ("RETURN REQUESTED".equals(r.getStatus())) {
            Label waitingLabel = new Label("⏳ Return request pending approval");
            waitingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(waitingLabel);
        } else if ("RETURNED - REFUNDED".equals(r.getStatus())) {
            Label refundedLabel = new Label("✓ Returned and refunded successfully");
            refundedLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(refundedLabel);
        } else if ("CANCELLED".equals(r.getStatus())) {
            Label cancelledLabel = new Label("✕ Reservation cancelled");
            cancelledLabel.setStyle("-fx-text-fill: #CF222E; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(cancelledLabel);
        }

        // Show reason if exists
        if (r.getReason() != null && !r.getReason().isEmpty()) {
            Label reasonLabel = new Label("Note: " + r.getReason());
            reasonLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            reasonLabel.setWrapText(true);
            card.getChildren().add(reasonLabel);
        }

        return card;
    }

    /**
     * Handle return request - with selective item return for bundles
     */
    private void handleReturnRequest(Reservation r) {
        // Determine what we're returning
        String itemDescription;
        List<Reservation> availableItems = new java.util.ArrayList<>();
        
        if (r.isPartOfBundle()) {
            String bundleId = r.getBundleId();
            // Get all items in the bundle that are eligible for return
            availableItems = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .filter(res -> "COMPLETED".equals(res.getStatus()))
                .collect(Collectors.toList());
            
            if (availableItems.isEmpty()) {
                AlertHelper.showError("Error", "No items in this bundle are eligible for return.");
                return;
            }
            
            itemDescription = "Bundle Order (" + availableItems.size() + " items)";
        } else {
            availableItems.add(r);
            itemDescription = r.getItemName();
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Request Return");
        dialog.setHeaderText("Request Return for: " + itemDescription);

        ButtonType submitButtonType = new ButtonType("Submit Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Create checkboxes and quantity spinners for selecting items to return (for bundles only)
        Map<CheckBox, Reservation> itemCheckBoxMap = new HashMap<>();
        Map<Reservation, Spinner<Integer>> quantitySpinners = new HashMap<>();
        
        if (r.isPartOfBundle()) {
            Label selectLabel = new Label("Select items to return:");
            selectLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-fg-default; -fx-font-size: 14px;");
            
            VBox itemsList = new VBox(8);
            itemsList.setPadding(new Insets(10));
            itemsList.setStyle(
                "-fx-background-color: -color-bg-default;" +
                "-fx-border-color: -color-border-default;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;"
            );
            
            // Select All / Deselect All buttons
            HBox selectAllBox = new HBox(10);
            selectAllBox.setPadding(new Insets(0, 0, 8, 0));
            
            Button selectAllBtn = new Button("Select All");
            selectAllBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
            Button deselectAllBtn = new Button("Deselect All");
            deselectAllBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
            
            selectAllBox.getChildren().addAll(selectAllBtn, deselectAllBtn);
            itemsList.getChildren().add(selectAllBox);
            
            // Create checkbox with quantity spinner for each item
            for (Reservation item : availableItems) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                
                CheckBox checkBox = new CheckBox(
                    item.getItemName() + " - " + item.getSize() +
                    " - ₱" + String.format("%.2f", item.getTotalPrice())
                );
                checkBox.setSelected(true); // Select all by default
                checkBox.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 13px;");
                itemCheckBoxMap.put(checkBox, item);
                
                // Add quantity spinner
                Label qtyLabel = new Label("Qty:");
                qtyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
                
                Spinner<Integer> qtySpinner = new Spinner<>(1, item.getQuantity(), item.getQuantity());
                qtySpinner.setEditable(true);
                qtySpinner.setPrefWidth(70);
                qtySpinner.setStyle("-fx-font-size: 12px;");
                quantitySpinners.put(item, qtySpinner);
                
                // Disable spinner when checkbox is unchecked
                qtySpinner.setDisable(!checkBox.isSelected());
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    qtySpinner.setDisable(!newVal);
                });
                
                itemRow.getChildren().addAll(checkBox, qtyLabel, qtySpinner);
                itemsList.getChildren().add(itemRow);
            }
            
            // Select/Deselect All functionality
            selectAllBtn.setOnAction(e -> {
                for (CheckBox cb : itemCheckBoxMap.keySet()) {
                    cb.setSelected(true);
                }
            });
            
            deselectAllBtn.setOnAction(e -> {
                for (CheckBox cb : itemCheckBoxMap.keySet()) {
                    cb.setSelected(false);
                }
            });
            
            grid.add(selectLabel, 0, 0);
            grid.add(itemsList, 0, 1);
        }

        Label infoLabel = new Label("Please provide a reason for the return:");
        infoLabel.setStyle("-fx-font-weight: bold;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("e.g., Item is damaged, wrong size, defective, etc.");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);

        Label noteLabel = new Label("Note: Return requests must be approved by admin/staff.");
        noteLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        noteLabel.setWrapText(true);

        int currentRow = r.isPartOfBundle() ? 2 : 0;
        grid.add(infoLabel, 0, currentRow);
        grid.add(reasonArea, 0, currentRow + 1);
        grid.add(noteLabel, 0, currentRow + 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                String reason = reasonArea.getText().trim();
                if (reason.isEmpty()) {
                    AlertHelper.showError("Error", "Please provide a reason for the return.");
                    return null;
                }
                return reason;
            }
            return null;
        });

        final List<Reservation> finalAvailableItems = availableItems;
        final Map<CheckBox, Reservation> finalItemCheckBoxMap = itemCheckBoxMap;
        final Map<Reservation, Spinner<Integer>> finalQuantitySpinners = quantitySpinners;
        
        dialog.showAndWait().ifPresent(reason -> {
            if (reason != null) {
                // Get selected items to return with their quantities
                Map<Reservation, Integer> itemsToReturnWithQty = new HashMap<>();
                
                if (r.isPartOfBundle()) {
                    // Get only selected items from checkboxes with their quantities
                    for (Map.Entry<CheckBox, Reservation> entry : finalItemCheckBoxMap.entrySet()) {
                        if (entry.getKey().isSelected()) {
                            Reservation item = entry.getValue();
                            int qtyToReturn = finalQuantitySpinners.get(item).getValue();
                            itemsToReturnWithQty.put(item, qtyToReturn);
                        }
                    }
                    
                    // Validate that at least one item is selected
                    if (itemsToReturnWithQty.isEmpty()) {
                        AlertHelper.showError("Error", "Please select at least one item to return.");
                        return;
                    }
                } else {
                    // For single items, add the full quantity
                    itemsToReturnWithQty.put(finalAvailableItems.get(0), finalAvailableItems.get(0).getQuantity());
                }
                
                // Request return for selected items with quantities
                boolean allSuccess = true;
                int successCount = 0;
                int totalItemsToReturn = itemsToReturnWithQty.values().stream().mapToInt(Integer::intValue).sum();
                
                for (Map.Entry<Reservation, Integer> entry : itemsToReturnWithQty.entrySet()) {
                    Reservation item = entry.getKey();
                    int qtyToReturn = entry.getValue();
                    int originalQty = item.getQuantity();
                    
                    if (qtyToReturn == originalQty) {
                        // Return all items in this reservation
                        boolean success = reservationManager.requestReturn(item.getReservationId(), reason);
                        if (success) {
                            successCount += qtyToReturn;
                        } else {
                            allSuccess = false;
                        }
                    } else if (qtyToReturn < originalQty) {
                        // Partial return - need to split the reservation
                        boolean success = reservationManager.requestPartialReturn(
                            item.getReservationId(), qtyToReturn, reason);
                        if (success) {
                            successCount += qtyToReturn;
                        } else {
                            allSuccess = false;
                        }
                    }
                }
                
                if (allSuccess) {
                    String message;
                    if (r.isPartOfBundle()) {
                        message = "Return request submitted successfully for " + successCount + " item(s)!\n\n";
                    } else {
                        message = "Return request submitted successfully!\n\n";
                    }
                    
                    AlertHelper.showSuccess("Success",
                        message + "Please wait for admin/staff approval.");
                    refreshReservationsView();
                } else if (successCount > 0) {
                    AlertHelper.showWarning("Partial Success",
                        "Return request submitted for " + successCount + " out of " + totalItemsToReturn + " items.\n" +
                        "Some items may have exceeded the return period (10 days limit).");
                    refreshReservationsView();
                } else {
                    AlertHelper.showError("Error",
                        "Failed to submit return request.\n" +
                        "Return period may have expired (10 days limit).");
                }
            }
        });
    }

    /**
     * Handle cancel reservation
     */
    private void handleCancelReservation(Reservation r) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cancel Reservation");
        dialog.setHeaderText("Cancel Reservation #" + r.getReservationId());

        ButtonType confirmButtonType = new ButtonType("Confirm Cancel", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label warningLabel = new Label("⚠ Are you sure you want to cancel this reservation?");
        warningLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox itemInfo = new VBox(5);
        itemInfo.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-padding: 10;" +
            "-fx-background-radius: 6px;"
        );
        
        Label itemLabel = new Label("Item: " + r.getItemName() + " - " + r.getSize());
        Label qtyLabel = new Label("Quantity: " + r.getQuantity() + "x");
        Label priceLabel = new Label("Total: ₱" + String.format("%.2f", r.getTotalPrice()));
        itemInfo.getChildren().addAll(itemLabel, qtyLabel, priceLabel);

        Label reasonLabel = new Label("Reason for cancellation (optional):");
        reasonLabel.setStyle("-fx-font-weight: bold;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("e.g., Changed my mind, wrong item selected, no longer needed, etc.");
        reasonArea.setPrefRowCount(3);
        reasonArea.setWrapText(true);

        Label noteLabel = new Label("Note: If this reservation is part of a bundle, only this specific item will be cancelled.");
        noteLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        noteLabel.setWrapText(true);

        content.getChildren().addAll(warningLabel, itemInfo, reasonLabel, reasonArea, noteLabel);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return reasonArea.getText().trim();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reason -> {
            String cancellationReason = reason.isEmpty() ? "Student cancelled reservation" : reason;
            boolean success = reservationManager.cancelReservation(r.getReservationId(), cancellationReason);
            
            if (success) {
                // Restore stock when cancelling
                inventoryManager.restockItem(r.getItemCode(), r.getSize(), r.getQuantity());
                
                AlertHelper.showSuccess("Reservation Cancelled",
                    "Your reservation has been cancelled successfully.\n\n" +
                    "The reserved stock has been returned to inventory.");
                refreshReservationsView();
            } else {
                AlertHelper.showError("Error",
                    "Failed to cancel reservation.\n" +
                    "Please contact staff if the issue persists.");
            }
        });
    }

    /**
     * Refresh reservations view
     */
    private void refreshReservationsView() {
        if (refreshCallback != null) {
            refreshCallback.run();
        }
    }
    
    /**
     * Check if bundle has any completed items that can be returned
     */
    private boolean hasCompletedItems(String bundleId) {
        return reservationManager.getAllReservations().stream()
            .anyMatch(res -> bundleId.equals(res.getBundleId()) && 
                            "COMPLETED".equals(res.getStatus()) && 
                            res.isEligibleForReturn());
    }
    
    /**
     * Handle card click - show details or return dialog based on status
     */
    private void handleCardClick(Reservation r) {
        if ("PENDING".equals(r.getStatus()) || "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
            // Show cancel option
            handleCancelReservation(r);
        } else if ("COMPLETED".equals(r.getStatus()) && r.isEligibleForReturn()) {
            // Show return dialog
            handleReturnRequest(r);
        } else if (r.isPartOfBundle() && hasCompletedItems(r.getBundleId())) {
            // Bundle with some completed items - show return dialog
            handleReturnRequest(r);
        } else {
            // Just show details
            showReservationDetails(r);
        }
    }
    
    /**
     * Show reservation details dialog (read-only view)
     */
    private void showReservationDetails(Reservation r) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details");
        dialog.setHeaderText(r.isPartOfBundle() ? r.getBundleId() : "Reservation #" + r.getReservationId());
        
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-min-width: 400px;");
        
        // Status
        Label statusLabel = new Label("Status: " + r.getStatus());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Items list
        VBox itemsBox = new VBox(8);
        itemsBox.setPadding(new Insets(10));
        itemsBox.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;"
        );
        
        Label itemsTitle = new Label("Items:");
        itemsTitle.setStyle("-fx-font-weight: bold;");
        itemsBox.getChildren().add(itemsTitle);
        
        if (r.isPartOfBundle()) {
            String bundleId = r.getBundleId();
            List<Reservation> bundleItems = reservationManager.getAllReservations().stream()
                .filter(res -> bundleId.equals(res.getBundleId()))
                .collect(Collectors.toList());
            
            for (Reservation item : bundleItems) {
                String statusTag = "";
                String statusColor = "-color-fg-default";
                
                if (item.getStatus().contains("RETURNED")) {
                    statusTag = " (Refunded)";
                    statusColor = "#656D76";
                } else if ("COMPLETED".equals(item.getStatus())) {
                    statusTag = " (Completed)";
                    statusColor = "#1A7F37";
                } else if (item.getStatus().contains("RETURN REQUESTED")) {
                    statusTag = " (Return Requested)";
                    statusColor = "#BF8700";
                }
                
                Label itemLabel = new Label("• " + item.getItemName() + " - " + item.getSize() + 
                                           " (" + item.getQuantity() + "x) - ₱" + 
                                           String.format("%.2f", item.getTotalPrice()) + statusTag);
                itemLabel.setStyle("-fx-text-fill: " + statusColor + ";");
                itemsBox.getChildren().add(itemLabel);
            }
        } else {
            Label itemLabel = new Label("• " + r.getItemName() + " - " + r.getSize() + 
                                       " (" + r.getQuantity() + "x) - ₱" + 
                                       String.format("%.2f", r.getTotalPrice()));
            itemsBox.getChildren().add(itemLabel);
        }
        
        // Reason if exists
        if (r.getReason() != null && !r.getReason().isEmpty()) {
            VBox reasonBox = new VBox(5);
            reasonBox.setPadding(new Insets(10));
            reasonBox.setStyle(
                "-fx-background-color: #FFF8C5;" +
                "-fx-border-color: #9A6700;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;"
            );
            
            Label reasonTitle = new Label("Reason/Note:");
            reasonTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #9A6700;");
            
            Label reasonText = new Label(r.getReason());
            reasonText.setWrapText(true);
            reasonText.setStyle("-fx-text-fill: #9A6700;");
            
            reasonBox.getChildren().addAll(reasonTitle, reasonText);
            content.getChildren().addAll(statusLabel, itemsBox, reasonBox);
        } else {
            content.getChildren().addAll(statusLabel, itemsBox);
        }
        
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    /**
     * Create profile view
     */
    public Node createProfileView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));

        // Profile card
        VBox profileCard = new VBox(15);
        profileCard.setPadding(new Insets(30));
        profileCard.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 1px;"
        );
        profileCard.setMaxWidth(600);

        // Title
        Label titleLabel = new Label("My Profile");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Profile info
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(15);
        infoGrid.setPadding(new Insets(20, 0, 0, 0));

        Label idLabel = new Label("Student ID:");
        idLabel.setStyle("-fx-font-weight: bold;");
        Label idValue = new Label(student.getStudentId());

        Label nameLabel = new Label("Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        Label nameValue = new Label(student.getFullName());

        Label courseLabel = new Label("Course:");
        courseLabel.setStyle("-fx-font-weight: bold;");
        Label courseValue = new Label(student.getCourse());

        Label genderLabel = new Label("Gender:");
        genderLabel.setStyle("-fx-font-weight: bold;");
        Label genderValue = new Label(student.getGender());

        Label statusLabel = new Label("Status:");
        statusLabel.setStyle("-fx-font-weight: bold;");
        Label statusValue = new Label(student.getAccountStatus());
        statusValue.setStyle(student.isActive() ? "-fx-text-fill: #1A7F37;" : "-fx-text-fill: #CF222E;");

        infoGrid.add(idLabel, 0, 0);
        infoGrid.add(idValue, 1, 0);
        infoGrid.add(nameLabel, 0, 1);
        infoGrid.add(nameValue, 1, 1);
        infoGrid.add(courseLabel, 0, 2);
        infoGrid.add(courseValue, 1, 2);
        infoGrid.add(genderLabel, 0, 3);
        infoGrid.add(genderValue, 1, 3);
        infoGrid.add(statusLabel, 0, 4);
        infoGrid.add(statusValue, 1, 4);

        // Change password button
        Button changePasswordBtn = new Button("Change Password");
        changePasswordBtn.setStyle(
            "-fx-background-color: #0969DA;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20;"
        );
        changePasswordBtn.setOnAction(e -> handleChangePassword());

        HBox buttonBox = new HBox(changePasswordBtn);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        profileCard.getChildren().addAll(titleLabel, infoGrid, buttonBox);

        container.getChildren().add(profileCard);
        return container;
    }

    /**
     * Handle change password
     */
    private void handleChangePassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your new password");

        ButtonType changeButtonType = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");

        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(currentPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                String current = currentPasswordField.getText();
                String newPass = newPasswordField.getText();
                String confirm = confirmPasswordField.getText();

                if (!current.equals(student.getPassword())) {
                    AlertHelper.showError("Error", "Current password is incorrect");
                    return null;
                }

                if (newPass.isEmpty() || newPass.length() < 6) {
                    AlertHelper.showError("Error", "New password must be at least 6 characters");
                    return null;
                }

                if (newPass.equals(current)) {
                    AlertHelper.showError("Error", "New password must be different from current password");
                    return null;
                }

                if (!newPass.equals(confirm)) {
                    AlertHelper.showError("Error", "Passwords do not match");
                    return null;
                }

                return newPass;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPassword -> {
            student.setPassword(newPassword);
            List<Student> students = FileStorage.loadStudents();
            FileStorage.updateStudent(students, student);
            AlertHelper.showSuccess("Success", "Password changed successfully!");
        });
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
            Scene scene = new Scene(loginView.getView(), 1920, 1025);
            SceneManager.setScene(scene);
        }
    }
}

