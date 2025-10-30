package gui.controllers;

import inventory.InventoryManager;
import inventory.ReservationManager;
import inventory.Item;
import inventory.Reservation;
import student.Student;
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
import java.util.stream.Collectors;

/**
 * StudentDashboardController - Handles all student dashboard operations
 */
public class StudentDashboardController {
    
    private Student student;
    private InventoryManager inventoryManager;
    private ReservationManager reservationManager;
    
    public StudentDashboardController(Student student) {
        this.student = student;
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
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
        
        Label subtitleLabel = new Label("Browse available items for " + student.getCourse());
        subtitleLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        // Filter bar
        HBox filterBar = new HBox(15);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-weight: bold;");
        
        ComboBox<String> courseFilter = new ComboBox<>();
        courseFilter.getItems().addAll("All Courses", student.getCourse());
        courseFilter.setValue(student.getCourse());
        courseFilter.setPrefWidth(150);
        
        ComboBox<String> sizeFilter = new ComboBox<>();
        sizeFilter.getItems().addAll("All Sizes", "XS", "S", "M", "L", "XL", "XXL", "One Size");
        sizeFilter.setValue("All Sizes");
        sizeFilter.setPrefWidth(150);
        
        Button searchBtn = new Button("🔍 Search");
        styleActionButton(searchBtn, "#0969DA");
        
        filterBar.getChildren().addAll(filterLabel, courseFilter, sizeFilter, searchBtn);
        
        // Items grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        FlowPane itemsGrid = new FlowPane(20, 20);
        itemsGrid.setPadding(new Insets(10));
        
        // Get items for student's course
        List<Item> items = inventoryManager.getItemsByCourse(student.getCourse());
        
        if (items.isEmpty()) {
            Label noItems = new Label("No items available for your course yet.");
            noItems.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            itemsGrid.getChildren().add(noItems);
        } else {
            for (Item item : items) {
                VBox itemCard = createItemCard(item);
                itemsGrid.getChildren().add(itemCard);
            }
        }
        
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
        
        // Reserve button
        Button reserveBtn = new Button("Reserve");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setPrefHeight(35);
        reserveBtn.setDisable(item.getQuantity() == 0);
        
        if (item.getQuantity() > 0) {
            reserveBtn.setStyle(
                "-fx-background-color: #0969DA;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;"
            );
        } else {
            reserveBtn.setStyle(
                "-fx-background-color: #6E7781;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 6px;"
            );
        }
        
        reserveBtn.setOnAction(e -> handleReserveItem(item));
        
        card.getChildren().addAll(
            nameLabel,
            codeLabel,
            sizeLabel,
            new Separator(),
            priceLabel,
            stockLabel,
            reserveBtn
        );
        
        return card;
    }
    
    /**
     * Handle reserve item
     */
    private void handleReserveItem(Item item) {
        AlertHelper.showInfo("Reserve Item", 
            "Reservation feature coming soon!\n\n" +
            "Item: " + item.getName() + "\n" +
            "Price: ₱" + String.format("%.2f", item.getPrice()));
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
        
        // Get student's reservations
        List<Reservation> myReservations = reservationManager.getAllReservations().stream()
            .filter(r -> r.getStudentId().equals(student.getStudentId()))
            .collect(Collectors.toList());
        
        if (myReservations.isEmpty()) {
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
            
            for (Reservation r : myReservations) {
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
        
        Label idLabel = new Label("Reservation #" + r.getReservationId());
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
        
        Label itemLabel = new Label(r.getItemName() + " - " + r.getSize());
        itemLabel.setStyle("-fx-text-fill: -color-fg-default; -fx-font-size: 14px;");
        
        Label priceLabel = new Label("Total: ₱" + String.format("%.2f", r.getTotalPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");
        
        card.getChildren().addAll(header, new Separator(), itemLabel, priceLabel);
        
        return card;
    }
    
    /**
     * Create profile view
     */
    public Node createProfileView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Profile (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
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

