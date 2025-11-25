package gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * AlertHelper - Utility class for displaying dialogs and alerts
 * 
 * Provides convenient methods for showing information, errors, warnings,
 * confirmations, and input dialogs with consistent styling.
 */
public class AlertHelper {
    private static final Logger LOGGER = Logger.getLogger(AlertHelper.class.getName());
    
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
     * Show a detailed receipt in a scrollable, fixed-height dialog.
     * @param title dialog title
     * @param receiptText the receipt text (monospaced)
     */
    public static void showReceiptDialog(String title, String receiptText) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Success!");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        javafx.scene.control.TextArea area = new javafx.scene.control.TextArea(receiptText);
        area.setEditable(false);
        area.setWrapText(false);
        area.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        area.setPrefWidth(520);
        area.setPrefHeight(380);

        dialog.getDialogPane().setContent(area);
        // reduce overall dialog height by constraining the pane
        dialog.getDialogPane().setPrefHeight(420);
        dialog.getDialogPane().setPrefWidth(540);

        dialog.showAndWait();
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
        return showInputDialog(title, null, message, defaultValue);
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
     * Show an input dialog with optional header.
     * Adds logging when dialog is shown and when user input is null/cancelled.
     */
    public static String showInputDialog(String title, String header, String message, String defaultValue) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField inputField = new TextField(defaultValue == null ? "" : defaultValue);
        inputField.setPromptText(message);
        grid.add(inputField, 0, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setOnShown(e -> LOGGER.fine(() -> "Input dialog shown: " + title + " / " + header));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okBtn) {
                String v = inputField.getText();
                if (v == null || v.trim().isEmpty()) {
                    LOGGER.fine(() -> "Input dialog returned empty for: " + title + " / " + header);
                    return v == null ? null : v.trim();
                }
                return v.trim();
            }
            LOGGER.fine(() -> "Input dialog cancelled for: " + title + " / " + header);
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            LOGGER.fine(() -> "Input dialog result empty (user cancelled): " + title + " / " + header);
        }
        return result.orElse(null);
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

