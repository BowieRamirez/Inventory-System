package gui.utils;

import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * SceneManager - Manages scene navigation and transitions
 * 
 * This utility class handles switching between different views/scenes
 * in the application, maintaining a reference to the primary stage.
 */
public class SceneManager {
    
    private static Stage primaryStage;
    private static Scene currentScene;
    
    /**
     * Initialize the SceneManager with the primary stage
     * 
     * @param stage The primary stage of the application
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
    }
    
    /**
     * Set and display a new scene
     * 
     * @param scene The scene to display
     */
    public static void setScene(Scene scene) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager not initialized. Call initialize() first.");
        }
        
        currentScene = scene;
        primaryStage.setScene(scene);
        
        // Always ensure maximized state after scene change
        primaryStage.setMaximized(true);
    }
    
    /**
     * Get the current scene
     * 
     * @return The current scene
     */
    public static Scene getCurrentScene() {
        return currentScene;
    }
    
    /**
     * Get the primary stage
     * 
     * @return The primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Set the title of the primary stage
     * 
     * @param title The new title
     */
    public static void setTitle(String title) {
        if (primaryStage != null) {
            primaryStage.setTitle(title);
        }
    }
    
    /**
     * Close the application
     */
    public static void closeApplication() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
}

