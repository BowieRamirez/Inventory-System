package gui;

import gui.utils.SceneManager;
import gui.utils.ThemeManager;
import gui.views.LoginView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Entry Point for STI ProWear System
 * 
 * This class initializes the GUI application with AtlantaFX theme
 * and sets up the initial login screen.
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        // Initialize theme manager with default theme
        ThemeManager.initialize();
        
        // Set AtlantaFX theme (default: Primer Light)
        Application.setUserAgentStylesheet(ThemeManager.getCurrentTheme().getUserAgentStylesheet());
        
        // Initialize SceneManager
        SceneManager.initialize(stage);
        
        // Configure primary stage
        stage.setTitle("STI ProWear System - Modern Inventory Management");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.setMaximized(false);
        
        // Show login screen
        showLoginScreen();
        
        // Show the stage
        stage.show();
    }
    
    /**
     * Display the login screen
     */
    private void showLoginScreen() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1024, 768);
        SceneManager.setScene(scene);
    }
    
    /**
     * Get the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Main method - launches the JavaFX application
     */
    public static void main(String[] args) {
        launch(args);
    }
}

