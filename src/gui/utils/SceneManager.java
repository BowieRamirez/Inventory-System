package gui.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
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
    private static StackPane rootContainer;
    
    /**
     * Initialize the SceneManager with the primary stage
     * 
     * @param stage The primary stage of the application
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
        // Create a single root container and attach one Scene for the whole app.
        rootContainer = new StackPane();
        currentScene = new Scene(rootContainer);
        primaryStage.setScene(currentScene);
    }

    /**
     * Replace the application's root content inside the single Scene.
     * This avoids reattaching different Scene objects to the Stage which
     * can trigger layout and decorations recalculation.
     *
     * @param root The new root Node to display
     */
    public static void setRoot(Node root) {
        if (primaryStage == null) {
            throw new IllegalStateException("SceneManager not initialized. Call initialize() first.");
        }

        Runnable change = () -> {
            try {
                // Detach from previous parent if necessary (best-effort).
                if (root.getParent() != null) {
                    if (root.getParent() instanceof Pane) {
                        ((Pane) root.getParent()).getChildren().remove(root);
                    } else if (root.getParent() instanceof Group) {
                        ((Group) root.getParent()).getChildren().remove(root);
                    } else {
                        // Can't safely detach from unknown parent type; proceed and let JavaFX throw if invalid.
                    }
                }

                rootContainer.getChildren().setAll(root);
                System.out.println("[SceneManager] Replaced root inside single Scene.");
            } catch (Exception e) {
                System.err.println("[SceneManager] Error while setting root: " + e.getMessage());
                e.printStackTrace();
            }
        };

        if (Platform.isFxApplicationThread()) {
            change.run();
        } else {
            Platform.runLater(change);
        }
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
        
        // Defer updating currentScene until the Scene is attached to the Stage
        // Ensure scene switching and stage state changes happen on the JavaFX Application Thread.
        // If we're already on the FX thread, perform the change immediately to avoid
        // showing the stage before the scene has been attached (which can cause a
        // small/default window to appear at startup).
        Runnable change = () -> {
            try {
                // Log stage state before change
                System.out.println("[SceneManager] BEFORE setScene: maximized=" + primaryStage.isMaximized()
                        + ", fullScreen=" + primaryStage.isFullScreen()
                        + ", resizable=" + primaryStage.isResizable()
                        + ", width=" + primaryStage.getWidth()
                        + ", height=" + primaryStage.getHeight());

                // Preserve window state so we can re-apply it after swapping scenes.
                boolean wasMaximized = primaryStage.isMaximized();
                boolean wasFullScreen = primaryStage.isFullScreen();

                // Attach the provided Scene to the primary stage. Avoid trying to
                // move a Node between Scene instances (calling setRoot with a
                // root that already belongs to another Scene will throw
                // IllegalArgumentException). Setting the Scene directly is the
                // safest approach.
                primaryStage.setScene(scene);
                System.out.println("[SceneManager] Set Scene on Stage.");

                // Update currentScene now that the Scene is attached.
                currentScene = scene;

                // Re-apply fullscreen/maximized state on the next pulse to avoid
                // transient layout/decorations glitches when replacing the Scene.
                if (wasFullScreen || wasMaximized) {
                    Platform.runLater(() -> {
                        try {
                            if (wasFullScreen) {
                                primaryStage.setFullScreen(true);
                            } else if (wasMaximized) {
                                primaryStage.setMaximized(true);
                            }
                        } catch (Exception e) {
                            // Best-effort; don't let UI navigation fail because of this.
                            System.err.println("[SceneManager] Failed to reapply window state: " + e.getMessage());
                        }
                    });
                }

                // Log stage state after change (may reflect pre-reapply state)
                System.out.println("[SceneManager] AFTER setScene: maximized=" + primaryStage.isMaximized()
                        + ", fullScreen=" + primaryStage.isFullScreen()
                        + ", resizable=" + primaryStage.isResizable()
                        + ", width=" + primaryStage.getWidth()
                        + ", height=" + primaryStage.getHeight());
            } catch (Exception e) {
                System.err.println("[SceneManager] Error while setting scene: " + e.getMessage());
                e.printStackTrace();
            }
        };

        if (Platform.isFxApplicationThread()) {
            change.run();
        } else {
            Platform.runLater(change);
        }
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

