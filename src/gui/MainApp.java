package gui;

import gui.utils.SceneManager;
import gui.utils.ThemeManager;
import gui.views.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;
import utils.StockReturnLogger;

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
        
        // Ensure database table for stock logs exists
        StockReturnLogger.ensureTableExists();
        
        // Configure primary stage
        stage.setTitle("STI ProWear System - Modern Inventory Management");
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        
        // Show login screen
        showLoginScreen();
        
        // Show the stage and open maximized (fullscreen windowed). We only
        // maximize once at startup to avoid toggling/minimize issues when
        // switching scenes later.
        stage.show();
        // Maximize on the next pulse to allow initial layout to stabilize and
        // avoid transient size/decorations glitches when switching scenes.
        javafx.application.Platform.runLater(() -> stage.setMaximized(true));
    }

    
    /**
     * Display the login screen
     */
    private void showLoginScreen() {
        LoginView loginView = new LoginView();
        SceneManager.setRoot(loginView.getView());
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

