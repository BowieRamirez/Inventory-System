package gui.utils;

import java.util.function.Supplier;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Utility helpers to keep table styling and footer information consistent across dashboards.
 */
public final class TableViewUtils {

    private TableViewUtils() {
        // helper class
    }

    public static void applyConsistentStyle(TableView<?> table) {
        // Use inline CSS for table styling
        table.setStyle("-fx-background-color: -color-bg-subtle; "
            + "-fx-border-color: -color-border-default; "
            + "-fx-border-radius: 12px; "
            + "-fx-background-radius: 12px; "
            + "-fx-padding: 0;");
        
        // Style placeholder label
        Label placeholder = new Label("No content in table");
        placeholder.setStyle("-fx-text-fill: -color-fg-muted;");
        table.setPlaceholder(placeholder);
    }

    public static <T> VBox wrapWithFooter(TableView<T> table, Supplier<Integer> totalSupplier, Node paginationControls) {
        Label footer = createFooter(table, totalSupplier);
        VBox wrapper = new VBox(6);
        wrapper.setFillWidth(true);
        wrapper.getChildren().addAll(table, footer);
        if (paginationControls != null) {
            wrapper.getChildren().add(paginationControls);
        }
        VBox.setVgrow(table, Priority.ALWAYS);
        wrapper.setPadding(new Insets(0));
        return wrapper;
    }

    public static <T> Label createFooter(TableView<T> table, Supplier<Integer> totalSupplier) {
        Label footer = new Label();
        String footerColor = ThemeManager.isDarkMode() ? "rgba(200, 200, 220, 0.7)" : "rgba(42, 51, 65, 0.7)";
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: " + footerColor + "; -fx-padding: 4 0 6 8;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setWrapText(true);

        ListChangeListener<T> listener = change -> updateFooterText(table, totalSupplier, footer);
        ObservableList<T> items = table.getItems();
        if (items != null) {
            items.addListener(listener);
        }
        table.itemsProperty().addListener((obs, oldList, newList) -> {
            if (oldList != null) {
                oldList.removeListener(listener);
            }
            if (newList != null) {
                newList.addListener(listener);
            }
            updateFooterText(table, totalSupplier, footer);
        });

        updateFooterText(table, totalSupplier, footer);
        return footer;
    }

    private static <T> void updateFooterText(TableView<T> table, Supplier<Integer> totalSupplier, Label footer) {
        int current = table.getItems() == null ? 0 : table.getItems().size();
        Integer total = totalSupplier == null ? null : totalSupplier.get();
        String text = "Showing " + current + (current == 1 ? " result" : " results");
        if (total != null && total > current) {
            text += " of " + total + " total";
        }
        footer.setText(text);
    }
}
