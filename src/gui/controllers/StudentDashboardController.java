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
 
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
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
    private Runnable navigateToShopCallback;  // Callback to navigate to shop view

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
     * Set callback to navigate to shop view
     */
    public void setNavigateToShopCallback(Runnable callback) {
        this.navigateToShopCallback = callback;
    }
    
    /**
     * Create home view with best sellers, course items, and special items
     */
    public Node createHomeView() {
        VBox homeContainer = new VBox(40);
        homeContainer.setPadding(new Insets(60, 40, 40, 40));
        homeContainer.setAlignment(Pos.TOP_CENTER);
        homeContainer.setStyle("-fx-background-color: -color-bg-default;");
        
        ScrollPane scrollPane = new ScrollPane(homeContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: -color-bg-default; -fx-control-inner-background: -color-bg-default;");
        
        // Logo section
          try {
            java.io.File logoFile = new java.io.File("src/database/data/images/NewLogo.png");
            if (logoFile.exists()) {
                javafx.scene.image.Image logoImage = new javafx.scene.image.Image(logoFile.toURI().toString());
                javafx.scene.image.ImageView logoView = new javafx.scene.image.ImageView(logoImage);
                logoView.setFitHeight(300);
                logoView.setFitWidth(300);
                logoView.setPreserveRatio(true);

                // Clip to rounded rectangle so the logo image itself has rounded corners
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(300, 300);
                clip.setArcWidth(40);
                clip.setArcHeight(40);
                logoView.setClip(clip);

                homeContainer.getChildren().add(logoView);
            }
        } catch (Exception e) {
            // Logo loading failed, continue without it
        }
        
        // Welcome section
        Label welcomeLabel = new Label("Welcome to STI ProWear Novaliches");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 32));
        welcomeLabel.setStyle("-fx-text-fill: -color-fg-default;");
        welcomeLabel.setAlignment(Pos.CENTER);
        
        Label subtitleLabel = new Label("Your one-stop destination for official STI uniforms and gearsets. Browse our collection of\nquality apparel designed specifically for " + "STI" + " students.");
        subtitleLabel.setFont(Font.font("System", 15));
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        subtitleLabel.setAlignment(Pos.CENTER);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(800);
        
        // Start Shopping Button
        Button startShoppingBtn = new Button("Start Shopping");
        startShoppingBtn.setPrefWidth(200);
        startShoppingBtn.setPrefHeight(50);
        startShoppingBtn.setStyle(
            "-fx-background-color: #3E4C96;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 8px;"
        );
        startShoppingBtn.setOnAction(e -> {
            // Trigger navigation to Shop view
            if (navigateToShopCallback != null) {
                navigateToShopCallback.run();
            }
        });
        
        // Feature cards section
        HBox featuresContainer = new HBox(30);
        featuresContainer.setAlignment(Pos.CENTER);
        featuresContainer.setPadding(new Insets(40, 0, 0, 0));
        featuresContainer.setMaxWidth(1000);
        
        VBox qualityCard = createFeatureCard("✓", "#3E4C96", "Quality Uniforms", 
            "Official STI apparel made with\npremium materials");
        VBox reservationCard = createFeatureCard("⚡", "#F59E0B", "Quick Reservations", 
            "Reserve items and pick them\nup at your convenience");
        VBox claimCard = createFeatureCard("📦", "#EF4444", "Easy Claim", 
            "Track and claim your reserved\nitems hassle-free");
        
        HBox.setHgrow(qualityCard, Priority.ALWAYS);
        HBox.setHgrow(reservationCard, Priority.ALWAYS);
        HBox.setHgrow(claimCard, Priority.ALWAYS);
        
        featuresContainer.getChildren().addAll(qualityCard, reservationCard, claimCard);
        
        homeContainer.getChildren().addAll(welcomeLabel, subtitleLabel, startShoppingBtn, featuresContainer);
        
        // Add thin separator line between welcome and featured items
        VBox separatorBox = new VBox();
        separatorBox.setPadding(new Insets(40, 0, 40, 0));
        separatorBox.setMaxWidth(Double.MAX_VALUE);
        separatorBox.setAlignment(Pos.CENTER);
        
        // Create a thin custom separator line
        Region separatorLine = new Region();
        separatorLine.setStyle("-fx-background-color: -color-border-default;");
        separatorLine.setPrefHeight(1);
        separatorLine.setMaxWidth(1200);
        separatorLine.setMinHeight(1);
        separatorBox.getChildren().add(separatorLine);
        homeContainer.getChildren().add(separatorBox);
        
        // Get items
        List<Item> courseItems = inventoryManager.getItemsByCourse(student.getCourse());
        List<Item> specialItems = inventoryManager.getItemsByCourse("STI Special");
        List<Item> bestSellers = courseItems.stream()
            .filter(item -> item.getQuantity() < 50)  // Simulate best sellers (low stock/high demand)
            .limit(6)
            .collect(Collectors.toList());
        
        // Featured Items Title
        Label featuredTitle = new Label("Featured Items");
        featuredTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        featuredTitle.setStyle("-fx-text-fill: -color-fg-default;");
        featuredTitle.setAlignment(Pos.CENTER);
        VBox titleBox = new VBox(featuredTitle);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 30, 0));
        homeContainer.getChildren().add(titleBox);
        
        // Best Sellers Section
        if (!bestSellers.isEmpty()) {
            VBox bestSellersSection = createHomeSection("⭐ Best Sellers", bestSellers);
            homeContainer.getChildren().add(bestSellersSection);
        }
        
        // Your Course Items Section
        if (!courseItems.isEmpty()) {
            VBox courseSection = createHomeSection("👕 Items for " + student.getCourse(), courseItems.stream().limit(6).collect(Collectors.toList()));
            homeContainer.getChildren().add(courseSection);
        }
        
        // Special Items Section
        if (!specialItems.isEmpty()) {
            VBox specialSection = createHomeSection("✨ Special Items", specialItems.stream().limit(6).collect(Collectors.toList()));
            homeContainer.getChildren().add(specialSection);
        }
        
        return scrollPane;
    }
    
    /**
     * Create a feature card for the home page
     */
    private VBox createFeatureCard(String icon, String iconColor, String title, String description) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30, 20, 30, 20));
        card.setMaxWidth(300);
        card.setStyle(
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8;"
        );
        
        // Icon circle
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        iconLabel.setStyle(
            "-fx-text-fill: white;" +
            "-fx-background-color: " + iconColor + ";" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 70px;" +
            "-fx-min-height: 70px;" +
            "-fx-max-width: 70px;" +
            "-fx-max-height: 70px;" +
            "-fx-alignment: center;"
        );
        iconLabel.setAlignment(Pos.CENTER);
        
        // Title
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: -color-fg-default;");
        titleLabel.setAlignment(Pos.CENTER);
        
        // Description
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("System", 13));
        descLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        descLabel.setAlignment(Pos.CENTER);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(250);
        
        card.getChildren().addAll(iconLabel, titleLabel, descLabel);
        return card;
    }
    
    /**
     * Create a home section with title and items
     */
    private VBox createHomeSection(String title, List<Item> items) {
        VBox section = new VBox(20);
        section.setPadding(new Insets(0, 0, 30, 0));
        section.setMaxWidth(1200);
        
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        sectionTitle.setStyle("-fx-text-fill: -color-fg-default;");
        
        // Configure so 6 cards fit in a single row on desktop widths
        FlowPane itemsGrid = new FlowPane(20, 0);
        itemsGrid.setPadding(new Insets(0, 0, 0, 0)); // more side padding, no vertical gap
        itemsGrid.setPrefWrapLength(1920); // effectively one row at common desktop width
        
        for (Item item : items) {
            itemsGrid.getChildren().add(createHomeItemCard(item));
        }
        
        section.getChildren().addAll(sectionTitle, itemsGrid);
        return section;
    }
    
    /**
     * Create a simplified item card for home view
     */
    private VBox createHomeItemCard(Item item) {
        // Slightly narrower so 6 cards fit in one row at 1920px while keeping full button labels
        VBox card = new VBox(10);
        card.setPrefWidth(180);
        card.setMaxWidth(180);
        card.setMinHeight(230);
        card.setStyle(
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 15;" +
            "-fx-border-radius: 6;" +
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 6;"
        );
        card.setAlignment(Pos.TOP_LEFT);
        
        // Item name
        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        // Item code
        Label codeLabel = new Label("Code: " + item.getCode());
        codeLabel.setFont(Font.font("System", 11));
        codeLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        
        // Item size
        Label sizeLabel = new Label("Size: " + item.getSize());
        sizeLabel.setFont(Font.font("System", 11));
        sizeLabel.setStyle("-fx-text-fill: -color-fg-muted;");
        
        // Flexible spacer so that price/stock/buttons align at the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Price
        Label priceLabel = new Label("₱" + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #16A34A;");
        
        // Stock status
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER_LEFT);
        Label stockLabel = new Label(item.getQuantity() > 0 ? "✓ In Stock" : "✗ Out of Stock");
        stockLabel.setFont(Font.font("System", 10));
        String stockColor = item.getQuantity() > 0 ? "#16A34A" : "#DC2626";
        stockLabel.setStyle("-fx-text-fill: " + stockColor + ";");
        stockBox.getChildren().add(stockLabel);
        
        // Bottom button row for consistency across all cards
        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER);
        
        Button addBtn = new Button("Add");
        addBtn.setPrefWidth(90);
        addBtn.setPrefHeight(34);
        addBtn.setStyle(
            "-fx-background-color: #3E4C96;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        );
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
            "-fx-background-color: #2E3C86;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
            "-fx-background-color: #3E4C96;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        addBtn.setOnAction(e -> handleAddToCart(item));
        
        Button reserveBtn = new Button("Reserve");
        reserveBtn.setPrefWidth(90);
        reserveBtn.setPrefHeight(34);
        reserveBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #3E4C96;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-border-color: #D0D7DE;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4;" +
            "-fx-cursor: hand;"
        );
        reserveBtn.setOnMouseEntered(e -> reserveBtn.setStyle(
            "-fx-background-color: #F6F8FA;" +
            "-fx-text-fill: #3E4C96;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-border-color: #D0D7DE;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        reserveBtn.setOnMouseExited(e -> reserveBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #3E4C96;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 4;" +
            "-fx-border-color: #D0D7DE;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 4;" +
            "-fx-cursor: hand;"
        ));
        reserveBtn.setOnAction(e -> handleReserveItem(item));
        
        buttonRow.getChildren().addAll(addBtn, reserveBtn);
        
        card.getChildren().addAll(
            nameLabel,
            codeLabel,
            sizeLabel,
            spacer,
            priceLabel,
            stockBox,
            buttonRow
        );
        return card;
    }
    
    /**
     * Create shop view
     */
    public Node createShopView() {
        HBox mainContainer = new HBox(0);
        mainContainer.setPadding(new Insets(0));
        
        // Left sidebar for filters
        VBox filterSidebar = new VBox(20);
        filterSidebar.setPadding(new Insets(20));
        filterSidebar.setPrefWidth(260);
        filterSidebar.setMinWidth(260);
        filterSidebar.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 0 1 0 0;" +
            "-fx-border-radius: 0;"
        );
        
        // Get items for student's course (uniforms) and STI Special items ONLY
        List<Item> allItems = inventoryManager.getItemsByCourse(student.getCourse());
        List<Item> specialItems = inventoryManager.getItemsByCourse("STI Special");
        allItems.addAll(specialItems);
        
        Label filtersTitle = new Label("Filters");
        filtersTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        filtersTitle.setStyle("-fx-text-fill: -color-fg-default;");
        
        // Search box
        javafx.scene.control.TextField searchField = new javafx.scene.control.TextField();
        searchField.setPromptText("Search items...");
        searchField.setPrefWidth(220);
        searchField.setStyle("-fx-padding: 8px; -fx-font-size: 12px;");
        
        // Categories section
        VBox categoriesBox = new VBox(10);
        Label categoriesLabel = new Label("Categories");
        categoriesLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        categoriesLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        CheckBox allItemsCheck = new CheckBox("All Items");
        CheckBox topsCheck = new CheckBox("Tops");
        CheckBox bottomsCheck = new CheckBox("Bottoms");
        CheckBox accessoriesCheck = new CheckBox("Accessories");
        
        allItemsCheck.setSelected(true);
        topsCheck.setSelected(true);
        bottomsCheck.setSelected(true);
        
        categoriesBox.getChildren().addAll(categoriesLabel, allItemsCheck, topsCheck, bottomsCheck, accessoriesCheck);
        
        // Gender section
        VBox genderBox = new VBox(10);
        Label genderLabel = new Label("Gender");
        genderLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        genderLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        CheckBox maleCheck = new CheckBox("Male");
        CheckBox femaleCheck = new CheckBox("Female");
        CheckBox unisexCheck = new CheckBox("Unisex");
        
        genderBox.getChildren().addAll(genderLabel, maleCheck, femaleCheck, unisexCheck);
        
        // Size section
        VBox sizeBox = new VBox(10);
        Label sizeLabel = new Label("Size");
        sizeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        sizeLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        CheckBox xsCheck = new CheckBox("XS");
        CheckBox sCheck = new CheckBox("S");
        CheckBox mCheck = new CheckBox("M");
        CheckBox lCheck = new CheckBox("L");
        CheckBox xlCheck = new CheckBox("XL");
        CheckBox xxlCheck = new CheckBox("XXL");
        
        sizeBox.getChildren().addAll(sizeLabel, xsCheck, sCheck, mCheck, lCheck, xlCheck, xxlCheck);
        
        filterSidebar.getChildren().addAll(filtersTitle, new Separator(), searchField, categoriesBox, genderBox, sizeBox);
        
        // Right content area
        VBox contentArea = new VBox(20);
        contentArea.setPadding(new Insets(0, 0, 0, 20));
        HBox.setHgrow(contentArea, Priority.ALWAYS);
        
        // Welcome message
        Label welcomeLabel = new Label("Welcome, " + student.getFirstName() + "! 👋");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        welcomeLabel.setStyle("-fx-text-fill: -color-fg-default;");
        
        Label subtitleLabel = new Label("Browse available uniforms and gearsets for " + student.getCourse());
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        // Items grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane itemsGrid = new FlowPane(20, 20);
        itemsGrid.setPadding(new Insets(10));
        
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
        
        scrollPane.setContent(itemsGrid);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Apply filters method
        Runnable applyFilters = () -> {
            String searchText = searchField.getText().toLowerCase().trim();
            
            List<Item> filteredItems = allItems.stream()
                .filter(item -> {
                    // Search filter
                    if (!searchText.isEmpty()) {
                        boolean matchesSearch = item.getName().toLowerCase().contains(searchText) ||
                                              String.valueOf(item.getCode()).contains(searchText);
                        if (!matchesSearch) return false;
                    }
                    
                    // Category filter
                    boolean categoryMatch = false;
                    boolean anyCheckboxSelected = topsCheck.isSelected() || bottomsCheck.isSelected() || accessoriesCheck.isSelected();
                    
                    if (!anyCheckboxSelected || allItemsCheck.isSelected()) {
                        // If no category is selected or "All Items" is checked, show everything
                        categoryMatch = true;
                    } else {
                        // Check which categories are selected
                        String itemName = item.getName().toLowerCase();
                        if (topsCheck.isSelected() && (itemName.contains("blouse") || itemName.contains("polo"))) {
                            categoryMatch = true;
                        }
                        if (bottomsCheck.isSelected() && (itemName.contains("skirt") || itemName.contains("pants"))) {
                            categoryMatch = true;
                        }
                        if (accessoriesCheck.isSelected() && !itemName.contains("blouse") && !itemName.contains("polo") &&
                            !itemName.contains("skirt") && !itemName.contains("pants")) {
                            categoryMatch = true;
                        }
                    }
                    
                    // Gender filter
                    boolean genderMatch = true;
                    if (maleCheck.isSelected() || femaleCheck.isSelected() || unisexCheck.isSelected()) {
                        genderMatch = false;
                        String itemName = item.getName().toLowerCase();
                        
                        if (maleCheck.isSelected() && itemName.contains("(male)")) {
                            genderMatch = true;
                        }
                        if (femaleCheck.isSelected() && itemName.contains("(female)")) {
                            genderMatch = true;
                        }
                        if (unisexCheck.isSelected() && !itemName.contains("(male)") && !itemName.contains("(female)")) {
                            genderMatch = true;
                        }
                    }
                    
                    // Size filter
                    boolean sizeMatch = true;
                    if (xsCheck.isSelected() || sCheck.isSelected() || mCheck.isSelected() || 
                        lCheck.isSelected() || xlCheck.isSelected() || xxlCheck.isSelected()) {
                        sizeMatch = false;
                        String itemSize = item.getSize().toUpperCase();
                        
                        if (xsCheck.isSelected() && itemSize.equals("XS")) sizeMatch = true;
                        if (sCheck.isSelected() && itemSize.equals("S")) sizeMatch = true;
                        if (mCheck.isSelected() && itemSize.equals("M")) sizeMatch = true;
                        if (lCheck.isSelected() && itemSize.equals("L")) sizeMatch = true;
                        if (xlCheck.isSelected() && itemSize.equals("XL")) sizeMatch = true;
                        if (xxlCheck.isSelected() && itemSize.equals("XXL")) sizeMatch = true;
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
        
        // Real-time filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        allItemsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        topsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        bottomsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        accessoriesCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        maleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        femaleCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        unisexCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        xsCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        sCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        mCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        lCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        xlCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        xxlCheck.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        
        contentArea.getChildren().addAll(
            welcomeLabel,
            subtitleLabel,
            scrollPane
        );
        
        mainContainer.getChildren().addAll(filterSidebar, contentArea);
        return mainContainer;
    }
    
    /**
     * Create item card
     */
    private VBox createItemCard(Item item) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: -color-bg-subtle;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: -color-border-default;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);"
        );

        // Title row
        HBox titleRow = new HBox(8);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(item.getName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        nameLabel.setStyle("-fx-text-fill: -color-fg-default;");
        nameLabel.setWrapText(true);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        String badgeEmoji = item.getCourse().equals("STI Special") ? "🎉" : "";
        if (!badgeEmoji.isEmpty()) {
            Label badgeLabel = new Label(badgeEmoji);
            badgeLabel.setStyle("-fx-font-size: 14px;");
            titleRow.getChildren().addAll(nameLabel, badgeLabel);
        } else {
            titleRow.getChildren().add(nameLabel);
        }

        Label categoryBadge = new Label(item.getCourse().equals("STI Special") ? "✨ Special" : "✓ Uniform");
        categoryBadge.setStyle(
            "-fx-background-color: " + (item.getCourse().equals("STI Special") ? "#DDF4FF" : "#DAFBE1") + ";" +
            "-fx-text-fill: " + (item.getCourse().equals("STI Special") ? "#0969DA" : "#1A7F37") + ";" +
            "-fx-padding: 3 8 3 8;" +
            "-fx-background-radius: 10px;" +
            "-fx-font-size: 11px;" +
            "-fx-font-weight: bold;"
        );

        Label codeLabel = new Label("Code: " + item.getCode());
        codeLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");

        Label sizeLabel = new Label("Size: " + item.getSize());
        sizeLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 12px;");

        // Spacer so price/stock/buttons sit at bottom for all cards
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label("₱" + String.format("%.2f", item.getPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");

        Label stockLabel;
        if (item.getQuantity() > 10) {
            stockLabel = new Label("✓ In Stock (" + item.getQuantity() + ")");
            stockLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 11px;");
        } else if (item.getQuantity() > 0) {
            stockLabel = new Label("✓ In Stock (" + item.getQuantity() + ")");
            stockLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 11px;");
        } else {
            stockLabel = new Label("Out of Stock");
            stockLabel.setStyle("-fx-text-fill: #CF222E; -fx-font-size: 11px;");
        }

        // Bottom-aligned buttons (always present, just disabled when no stock)
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);

        Button addBtn = new Button("Add");
        addBtn.setPrefWidth(110);
        addBtn.setPrefHeight(34);

        Button reserveBtn = new Button("Reserve");
        reserveBtn.setPrefWidth(110);
        reserveBtn.setPrefHeight(34);

        if (item.getQuantity() > 0) {
            addBtn.setDisable(false);
            addBtn.setStyle(
                "-fx-background-color: #3E4C96;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;"
            );
            addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: #2D3A7A;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;"
            ));
            addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: #3E4C96;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;"
            ));

            reserveBtn.setDisable(false);
            reserveBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #3E4C96;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-color: #D0D7DE;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;" +
                "-fx-cursor: hand;"
            );
            reserveBtn.setOnMouseEntered(e -> reserveBtn.setStyle(
                "-fx-background-color: #F6F8FA;" +
                "-fx-text-fill: #3E4C96;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-color: #D0D7DE;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;" +
                "-fx-cursor: hand;"
            ));
            reserveBtn.setOnMouseExited(e -> reserveBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #3E4C96;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-color: #D0D7DE;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;" +
                "-fx-cursor: hand;"
            ));
        } else {
            // Disabled styles but same size & position to keep alignment identical
            addBtn.setDisable(true);
            addBtn.setStyle(
                "-fx-background-color: #D0D7DE;" +
                "-fx-text-fill: #57606A;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 4px;"
            );

            reserveBtn.setDisable(true);
            reserveBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #57606A;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 4px;" +
                "-fx-border-color: #D0D7DE;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 4px;"
            );
        }

        addBtn.setOnAction(e -> handleAddToCart(item));
        reserveBtn.setOnAction(e -> handleReserveItem(item));

        buttonBox.getChildren().addAll(addBtn, reserveBtn);

        card.getChildren().addAll(
            titleRow,
            categoryBadge,
            codeLabel,
            sizeLabel,
            spacer,
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
                    "Please wait for staff approval.");
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
                      "Please wait for staff approval.";
            
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
        
        // Get student's reservations
        List<Reservation> myReservations = allReservations.stream()
            .filter(r -> r.getStudentId().equals(student.getStudentId()))
            .collect(Collectors.toList());
        
        // Sort by reservation ID descending (newest reservations have higher IDs)
        myReservations.sort((r1, r2) -> Integer.compare(r2.getReservationId(), r1.getReservationId()));
        
        // Deduplicate bundles while preserving order: newest IDs stay on top
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
                        "PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus()) ||
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
        
        // Sort by reservation time (most recent first) BEFORE deduplicating
        pickupItems.sort((r1, r2) -> r2.getReservationTime().compareTo(r1.getReservationTime()));
        
        // Deduplicate bundles - show only one card per bundle (will keep first, which is newest due to sort above)
        List<Reservation> deduplicatedReservations = ControllerUtils.getDeduplicatedReservations(pickupItems);
        
        // Sort by reservation time (most recent first)
        deduplicatedReservations.sort((r1, r2) -> r2.getReservationTime().compareTo(r1.getReservationTime()));
        
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
            
            Label awaitingNote = new Label("💡 Click to request Staff approval for pickup");
            awaitingNote.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            awaitingNote.setWrapText(true);
            
            actionBox.getChildren().addAll(awaitingLabel, requestBtn, awaitingNote);
            
        } else if ("PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(status)) {
            // Waiting for staff approval
            Label pendingLabel = new Label("⏳ Status: Waiting for Staff Approval");
            pendingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Button pendingBtn = new Button("⏳ Pending Staff Approval");
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
            
            Label pendingNote = new Label("⏱️ Please wait for Staff to approve your pickup request");
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
            
            Label approvedNote = new Label("💡 Confirm you've received the item from Staff");
            approvedNote.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px; -fx-font-style: italic;");
            approvedNote.setWrapText(true);
            
            actionBox.getChildren().addAll(approvedLabel, claimBtn, approvedNote);
        }

        card.getChildren().addAll(header, new Separator(), itemsBox, qtyLabel, priceLabel, actionBox);

        return card;
    }
    
    /**
     * Handle pickup request (student requests staff approval)
     */
    private void handleRequestPickup(Reservation r) {
        String itemDescription = r.isPartOfBundle() ? 
            "bundle order (" + r.getBundleId() + ")" : 
            r.getItemName() + " - " + r.getSize();
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Request Pickup");
        confirmAlert.setHeaderText("Request Pickup Approval");
        confirmAlert.setContentText(
            "Request Staff approval to pickup this " + itemDescription + "?\n\n" +
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
                        "Please wait for Staff approval.");
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
                String displayText;
                String statusColor = "-color-fg-default";
                
                if (item.getStatus().contains("REPLACED")) {
                    // Show replacement info: Original → Replaced into: New Item
                    displayText = "• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x) → Replaced into: " + item.getReplacementItemName() + " (" + item.getReplacementSize() + ")";
                    statusColor = "#656D76"; // Gray for replaced
                } else if ("COMPLETED".equals(item.getStatus())) {
                    displayText = "• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x) (Completed)";
                    statusColor = "#1A7F37"; // Green for completed
                } else if (item.getStatus().contains("REPLACEMENT REQUESTED")) {
                    displayText = "• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x) (Replacement Requested)";
                    statusColor = "#BF8700"; // Orange for pending replacement
                } else {
                    // For any other status, show it explicitly
                    displayText = "• " + item.getItemName() + " - " + item.getSize() + " (" + item.getQuantity() + "x) (" + item.getStatus() + ")";
                }
                
                Label itemLabel = new Label(displayText);
                itemLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 13px;");
                itemLabel.setWrapText(true);
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
        } else if ("PICKUP REQUESTED - AWAITING STAFF APPROVAL".equals(r.getStatus())) {
            Label awaitingLabel = new Label("⏳ Pickup request sent - Waiting for staff approval");
            awaitingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(awaitingLabel);
        } else if ("APPROVED FOR PICKUP".equals(r.getStatus())) {
            Label approvedLabel = new Label("✅ Approved! Go to 'Claim Items' to pick up");
            approvedLabel.setStyle("-fx-text-fill: #1A7F37; -fx-font-size: 12px; -fx-font-style: italic; -fx-font-weight: bold;");
            card.getChildren().add(approvedLabel);
        } else if ("COMPLETED".equals(r.getStatus()) && r.isEligibleForReturn()) {
            Label returnLabel = new Label("↩ Click to request replacement (" + r.getDaysUntilReturnExpires() + " days left)");
            returnLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
            card.getChildren().add(returnLabel);
        } else if (r.isPartOfBundle() && hasCompletedItems(r.getBundleId())) {
            // Bundle with some completed items that can be returned
            Label returnLabel = new Label("↩ Click to view and replace eligible items");
            returnLabel.setStyle("-fx-text-fill: #0969DA; -fx-font-size: 12px; -fx-font-weight: bold; -fx-font-style: italic;");
            card.getChildren().add(returnLabel);
        } else if ("REPLACEMENT REQUESTED".equals(r.getStatus())) {
            Label waitingLabel = new Label("⏳ Replacement request pending approval");
            waitingLabel.setStyle("-fx-text-fill: #BF8700; -fx-font-size: 12px; -fx-font-style: italic;");
            card.getChildren().add(waitingLabel);
        } else if ("REPLACED".equals(r.getStatus())) {
            Label refundedLabel = new Label("✓ Item replaced successfully (items can only be replaced once)");
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
        dialog.setTitle("Request Replacement");
        dialog.setHeaderText("Request Replacement for: " + itemDescription);

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

        Label infoLabel = new Label("Please provide a reason for the replacement:");
        infoLabel.setStyle("-fx-font-weight: bold;");

        TextArea reasonArea = new TextArea();
        reasonArea.setPromptText("e.g., Item is damaged, wrong size, defective, etc.");
        reasonArea.setPrefRowCount(4);
        reasonArea.setWrapText(true);

        Label noteLabel = new Label("⚠ Important: Replacement requests must be approved by staff. Items can only be replaced once.");
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
                    AlertHelper.showError("Error", "Please provide a reason for the replacement.");
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
                        message + "Please wait for staff approval.");
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
                
                if (item.getStatus().contains("REPLACED")) {
                    statusTag = " (Replaced)";
                    statusColor = "#656D76";
                } else if ("COMPLETED".equals(item.getStatus())) {
                    statusTag = " (Completed)";
                    statusColor = "#1A7F37";
                } else if (item.getStatus().contains("REPLACEMENT REQUESTED")) {
                    statusTag = " (Replacement Requested)";
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
        
        // Replacement item if exists
        if (r.getReplacementItemName() != null && !r.getReplacementItemName().isEmpty()) {
            VBox replacementBox = new VBox(8);
            replacementBox.setPadding(new Insets(10));
            replacementBox.setStyle(
                "-fx-background-color: #E6F7FF;" +
                "-fx-border-color: #0969DA;" +
                "-fx-border-width: 1px;" +
                "-fx-border-radius: 6px;" +
                "-fx-background-radius: 6px;"
            );
            
            Label replacementTitle = new Label("Item Replacement Summary:");
            replacementTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #0969DA; -fx-font-size: 13px;");
            
            // Original item returned
            VBox returnedBox = new VBox(3);
            Label returnedLabel = new Label("↩ Returned Item:");
            returnedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #CF222E; -fx-font-size: 12px;");
            Label returnedText = new Label("• " + r.getItemName() + " - " + r.getSize());
            returnedText.setWrapText(true);
            returnedText.setStyle("-fx-text-fill: #CF222E;");
            returnedBox.getChildren().addAll(returnedLabel, returnedText);
            
            // Replacement item received
            VBox receivedBox = new VBox(3);
            Label receivedLabel = new Label("↪ Received Replacement:");
            receivedLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A7F37; -fx-font-size: 12px;");
            Label receivedText = new Label("• " + r.getReplacementItemName() + " - " + r.getReplacementSize());
            receivedText.setWrapText(true);
            receivedText.setStyle("-fx-text-fill: #1A7F37;");
            receivedBox.getChildren().addAll(receivedLabel, receivedText);
            
            replacementBox.getChildren().addAll(replacementTitle, returnedBox, receivedBox);
            content.getChildren().add(replacementBox);
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
        changePasswordBtn.setOnAction(e -> openChangePasswordDialog());

        HBox buttonBox = new HBox(changePasswordBtn);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        profileCard.getChildren().addAll(titleLabel, infoGrid, buttonBox);

        container.getChildren().add(profileCard);
        return container;
    }

    /**
     * Expose change password dialog for Account menu
     * (also used by the Profile view button)
     */
    public void openChangePasswordDialog() {
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
    @SuppressWarnings("unused")
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
            SceneManager.setRoot(loginView.getView());
        }
    }
}

