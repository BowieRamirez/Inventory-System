package gui.controllers;

import gui.utils.AlertHelper;
import gui.utils.SceneManager;
import gui.views.LoginView;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

/**
 * StaffDashboardController - Handles staff dashboard operations
 */
public class StaffDashboardController {
    
    public Node createReservationsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Reservations Management (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    public Node createInventoryView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Inventory Management (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    public Node createStockLogsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Stock Logs (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
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

