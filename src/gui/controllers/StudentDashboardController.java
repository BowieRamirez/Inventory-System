package gui.controllers;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.ReceiptManager;
import inventory.Item;
import inventory.Reservation;
import student.Student;
import utils.FileStorage;
import gui.utils.AlertHelper;
import gui.utils.SceneManager;
import gui.views.LoginView;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
        
        public CartItem(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
        
        public Item getItem() { return item; }
        public int getQuantity() { return quantity; }
        @SuppressWarnings("unused")
        public void setQuantity(int quantity) { this.quantity = quantity; }
        
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
        
        // Get items for student's course (uniforms) and STI Special items
        List<Item> allItems = inventoryManager.getItemsByCourse(student.getCourse());
        List<Item> specialItems = inventoryManager.getItemsByCourse("STI Special");
        allItems.addAll(specialItems);
        
        // Display all items initially
        List<Item> displayItems = allItems;
        
        if (displayItems.isEmpty()) {
            Label noItems = new Label("No items available for your course yet.");
            noItems.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            itemsGrid.getChildren().add(noItems);
        } else {
            for (Item item : displayItems) {
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
                    // Category filter
                    boolean categoryMatch = true;
                    if (selectedCategory != null && selectedCategory.equals("Uniforms")) {
                        categoryMatch = item.getCourse().equals(student.getCourse());
                    } else if (selectedCategory != null && selectedCategory.equals("Specials")) {
                        categoryMatch = item.getCourse().equals("STI Special");
                    }
                    
                    // Gender filter
                    boolean genderMatch = true;
                    if (selectedGender != null && !selectedGender.equals("All Genders")) {
                        if (selectedGender.equals("Male")) {
                            genderMatch = item.getName().contains("(Male)");
                        } else if (selectedGender.equals("Female")) {
                            genderMatch = item.getName().contains("(Female)");
                        } else if (selectedGender.equals("Unisex")) {
                            genderMatch = !item.getName().contains("(Male)") && !item.getName().contains("(Female)");
                        }
                    }
                    
                    // Size filter
                    boolean sizeMatch = selectedSize == null || selectedSize.equals("All Sizes") || item.getSize().equals(selectedSize);
                    
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
        boolean alreadyInCart = cart.stream()
            .anyMatch(cartItem -> cartItem.getItem().getCode() == item.getCode() && 
                                 cartItem.getItem().getSize().equals(item.getSize()));
        
        if (alreadyInCart) {
            AlertHelper.showWarning("Already in Cart", 
                "This item is already in your cart!");
            return;
        }
        
        // Show quantity selection dialog
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Add to Cart: " + item.getName() + " (" + item.getSize() + ")");

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
            CartItem cartItem = new CartItem(item, quantity);
            cart.add(cartItem);
            AlertHelper.showSuccess("Added to Cart", 
                quantity + "x " + item.getName() + " (" + item.getSize() + ") added to cart!\n" +
                "Cart items: " + cart.size());
            
            // Update cart badge if callback is set
            if (cartUpdateCallback != null) {
                cartUpdateCallback.run();
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
            // Cart items list
            VBox cartItemsList = new VBox(15);
            double totalPrice = 0;
            int totalQuantity = 0;
            
            for (CartItem cartItem : cart) {
                HBox itemRow = createCartItemRow(cartItem);
                cartItemsList.getChildren().add(itemRow);
                totalPrice += cartItem.getTotalPrice();
                totalQuantity += cartItem.getQuantity();
            }
            
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
            Label itemsLabel = new Label("Items:");
            itemsLabel.setStyle("-fx-text-fill: -color-fg-default;");
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Label itemsValue = new Label(cart.size() + " type(s), " + totalQuantity + " item(s) total");
            itemsValue.setStyle("-fx-text-fill: -color-fg-muted;");
            itemsCount.getChildren().addAll(itemsLabel, spacer1, itemsValue);
            
            HBox totalRow = new HBox();
            totalRow.setAlignment(Pos.CENTER_LEFT);
            Label totalLabel = new Label("Total:");
            totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            totalLabel.setStyle("-fx-text-fill: -color-fg-default;");
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            Label totalValue = new Label("₱" + String.format("%.2f", totalPrice));
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
            
            Button reserveAllBtn = new Button("Reserve Bundle");
            reserveAllBtn.setPrefWidth(200);
            reserveAllBtn.setPrefHeight(40);
            reserveAllBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            reserveAllBtn.setOnAction(e -> handleReserveBundle());
            
            buttonBox.getChildren().addAll(clearBtn, reserveAllBtn);
            
            summaryPanel.getChildren().addAll(
                summaryTitle,
                new Separator(),
                itemsCount,
                totalRow,
                new Separator(),
                buttonBox
            );
            
            container.getChildren().addAll(titleLabel, scrollPane, summaryPanel);
        }
        
        return container;
    }
    
    /**
     * Create cart item row
     */
    private HBox createCartItemRow(CartItem cartItem) {
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
        
        VBox itemInfo = new VBox(5);
        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label detailsLabel = new Label("Code: " + item.getCode() + " | Size: " + item.getSize());
        detailsLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");
        
        Label qtyLabel = new Label("Quantity: " + cartItem.getQuantity() + "x");
        qtyLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        itemInfo.getChildren().addAll(nameLabel, detailsLabel, qtyLabel);
        HBox.setHgrow(itemInfo, Priority.ALWAYS);
        
        VBox priceBox = new VBox(3);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label unitPriceLabel = new Label("₱" + String.format("%.2f", item.getPrice()) + " each");
        unitPriceLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        
        Label totalPriceLabel = new Label("₱" + String.format("%.2f", cartItem.getTotalPrice()));
        totalPriceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        totalPriceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        priceBox.getChildren().addAll(unitPriceLabel, totalPriceLabel);
        
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
        
        row.getChildren().addAll(itemInfo, priceBox, removeBtn);
        return row;
    }
    
    /**
     * Handle reserve bundle (all cart items)
     */
    private void handleReserveBundle() {
        if (cart.isEmpty()) {
            AlertHelper.showWarning("Empty Cart", "Your cart is empty!");
            return;
        }
        
        // Check if all items are still in stock with requested quantities
        for (CartItem cartItem : cart) {
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
                    "Please adjust the quantity or remove it from your cart.");
                return;
            }
        }
        
        // Generate unique bundle ID for this bundle purchase
        String bundleId = "BUNDLE-" + student.getStudentId() + "-" + System.currentTimeMillis();
        
        // Create reservations for all cart items with the same bundleId
        int successCount = 0;
        int totalItems = 0;
        StringBuilder results = new StringBuilder();
        results.append("Bundle Reservation Results:\n\n");
        
        for (CartItem cartItem : cart) {
            Item item = cartItem.getItem();
            int quantity = cartItem.getQuantity();
            double totalPrice = cartItem.getTotalPrice();
            
            Reservation reservation = reservationManager.createReservation(
                student.getFullName(),
                student.getStudentId(),
                student.getCourse(),
                item.getCode(),
                item.getName(),
                item.getSize(),
                quantity,
                totalPrice,
                bundleId  // Pass the bundleId
            );
            
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
            AlertHelper.showSuccess("Bundle Reserved!", 
                results.toString() + "\n" +
                successCount + " type(s) of items, " + totalItems + " total items reserved!\n" +
                "Bundle ID: " + bundleId + "\n" +
                "Status: Pending Approval\n\n" +
                "Please wait for admin approval.");
            
            // Clear cart after successful reservation
            clearCart();
            
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
        List<Reservation> deduplicatedReservations = getDeduplicatedReservations(myReservations);
        
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
     * Deduplicate bundle reservations - show only one card per bundle
     */
    private List<Reservation> getDeduplicatedReservations(List<Reservation> reservations) {
        List<Reservation> deduplicated = new ArrayList<>();
        java.util.Set<String> seenBundles = new java.util.HashSet<>();
        
        for (Reservation r : reservations) {
            if (r.isPartOfBundle()) {
                String bundleId = r.getBundleId();
                if (!seenBundles.contains(bundleId)) {
                    seenBundles.add(bundleId);
                    deduplicated.add(r); // Add only first occurrence of bundle
                }
            } else {
                deduplicated.add(r); // Add non-bundle reservations
            }
        }
        
        return deduplicated;
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

        Label priceLabel = new Label("Total: ₱" + String.format("%.2f", totalPrice));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");

        card.getChildren().addAll(header, new Separator(), itemsBox, qtyLabel, priceLabel);

        // Add action buttons based on status
        if ("PENDING".equals(r.getStatus()) || "APPROVED - WAITING FOR PAYMENT".equals(r.getStatus())) {
            // Add cancel button for unpaid reservations
            Button cancelBtn = new Button("✕ Cancel Reservation");
            cancelBtn.setMaxWidth(Double.MAX_VALUE);
            cancelBtn.setPrefHeight(35);
            cancelBtn.setStyle(
                "-fx-background-color: #CF222E;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            cancelBtn.setOnAction(e -> handleCancelReservation(r));
            card.getChildren().add(cancelBtn);
        } else if ("PAID - READY FOR PICKUP".equals(r.getStatus())) {
            Button pickupBtn = new Button("✓ Confirm Pickup");
            pickupBtn.setMaxWidth(Double.MAX_VALUE);
            pickupBtn.setPrefHeight(35);
            pickupBtn.setStyle(
                "-fx-background-color: #1A7F37;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            pickupBtn.setOnAction(e -> handlePickup(r));
            card.getChildren().add(pickupBtn);
        } else if ("COMPLETED".equals(r.getStatus()) && r.isEligibleForReturn()) {
            VBox returnBox = new VBox(5);

            Label returnInfoLabel = new Label("✓ Item picked up. Return available for " + r.getDaysUntilReturnExpires() + " more days.");
            returnInfoLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");

            Button returnBtn = new Button("↩ Request Return");
            returnBtn.setMaxWidth(Double.MAX_VALUE);
            returnBtn.setPrefHeight(35);
            returnBtn.setStyle(
                "-fx-background-color: #BF8700;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
            returnBtn.setOnAction(e -> handleReturnRequest(r));

            returnBox.getChildren().addAll(returnInfoLabel, returnBtn);
            card.getChildren().add(returnBox);
        } else if ("RETURN REQUESTED".equals(r.getStatus())) {
            Label waitingLabel = new Label("⏳ Return request pending approval from admin/staff");
            waitingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().add(waitingLabel);
        } else if ("RETURNED - REFUNDED".equals(r.getStatus())) {
            Label refundedLabel = new Label("✓ Item returned and refunded successfully");
            refundedLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px; -fx-font-weight: bold;");
            card.getChildren().add(refundedLabel);
        } else if ("CANCELLED".equals(r.getStatus())) {
            Label cancelledLabel = new Label("✕ Reservation cancelled");
            cancelledLabel.setStyle("-fx-text-fill: #CF222E; -fx-font-size: 12px; -fx-font-weight: bold;");
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
     * Handle pickup confirmation
     */
    private void handlePickup(Reservation r) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Pickup");
        confirmAlert.setHeaderText("Confirm Item Pickup");
        confirmAlert.setContentText(
            "Are you sure you want to confirm pickup for:\n\n" +
            r.getItemName() + " - " + r.getSize() + "\n" +
            "Quantity: " + r.getQuantity() + "x\n\n" +
            "You will have 10 days to request a return if the item is damaged."
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = reservationManager.markAsPickedUp(r.getReservationId());
                if (success) {
                    AlertHelper.showSuccess("Success",
                        "Item picked up successfully!\n\n" +
                        "You have 10 days to request a return if needed.");
                    // Refresh the view
                    refreshReservationsView();
                } else {
                    AlertHelper.showError("Error", "Failed to confirm pickup. Please try again.");
                }
            }
        });
    }

    /**
     * Handle return request
     */
    private void handleReturnRequest(Reservation r) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Request Return");
        dialog.setHeaderText("Request Return for: " + r.getItemName());

        ButtonType submitButtonType = new ButtonType("Submit Request", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label infoLabel = new Label("Please provide a reason for the return:");
        infoLabel.setStyle("-fx-font-weight: bold;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("e.g., Item is damaged, wrong size, defective, etc.");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);

        Label noteLabel = new Label("Note: Return requests must be approved by admin/staff.");
        noteLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        noteLabel.setWrapText(true);

        grid.add(infoLabel, 0, 0);
        grid.add(reasonArea, 0, 1);
        grid.add(noteLabel, 0, 2);

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

        dialog.showAndWait().ifPresent(reason -> {
            if (reason != null) {
                boolean success = reservationManager.requestReturn(r.getReservationId(), reason);
                if (success) {
                    AlertHelper.showSuccess("Success",
                        "Return request submitted successfully!\n\n" +
                        "Please wait for admin/staff approval.");
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
            Scene scene = new Scene(loginView.getView(), 1024, 768);
            SceneManager.setScene(scene);
        }
    }
}

