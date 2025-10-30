package gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

/**
 * AlertHelper - Utility class for displaying dialogs and alerts
 * 
 * Provides convenient methods for showing information, errors, warnings,
 * confirmations, and input dialogs with consistent styling.
 */
public class AlertHelper {
    
    /**
     * Show an information dialog
     * 
     * @param title The dialog title
     * @param message The message to display
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show an error dialog
     * 
     * @param title The dialog title
     * @param message The error message to display
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show a success dialog
     * 
     * @param title The dialog title
     * @param message The success message to display
     */
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success!");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show a warning dialog
     * 
     * @param title The dialog title
     * @param message The warning message to display
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Show a confirmation dialog
     * 
     * @param title The dialog title
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show a confirmation dialog with custom header
     * 
     * @param title The dialog title
     * @param header The header text
     * @param message The confirmation message
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Show an input dialog
     * 
     * @param title The dialog title
     * @param message The prompt message
     * @param defaultValue The default input value
     * @return The user's input, or null if cancelled
     */
    public static String showInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Show an input dialog without default value
     * 
     * @param title The dialog title
     * @param message The prompt message
     * @return The user's input, or null if cancelled
     */
    public static String showInputDialog(String title, String message) {
        return showInputDialog(title, message, "");
    }
    
    /**
     * Show an exception dialog
     * 
     * @param title The dialog title
     * @param message The error message
     * @param exception The exception that occurred
     */
    public static void showException(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}

