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
 * CashierDashboardController - Handles cashier dashboard operations
 */
public class CashierDashboardController {
    
    public Node createPaymentsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Payment Processing (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    public Node createReservationsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Reservations View (Implementation coming next)");
        label.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
        
        container.getChildren().add(label);
        return container;
    }
    
    public Node createReceiptsView() {
        VBox container = new VBox(20);
        container.setPadding(new Insets(20));
        
        Label label = new Label("Receipts View (Implementation coming next)");
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

