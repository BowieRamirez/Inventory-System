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

    public StudentDashboardController(Student student) {
        this.student = student;
        inventoryManager = new InventoryManager();
        reservationManager = new ReservationManager(inventoryManager);
        receiptManager = new ReceiptManager();

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

        Label qtyLabel = new Label("Quantity: " + r.getQuantity() + "x");
        qtyLabel.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 13px;");

        Label priceLabel = new Label("Total: ₱" + String.format("%.2f", r.getTotalPrice()));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        priceLabel.setStyle("-fx-text-fill: #1A7F37;");

        card.getChildren().addAll(header, new Separator(), itemLabel, qtyLabel, priceLabel);

        // Add action buttons based on status
        if ("PAID - READY FOR PICKUP".equals(r.getStatus())) {
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

