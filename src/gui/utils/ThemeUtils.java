package gui.utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;

/**
 * ThemeUtils - small helper for refreshing scene styles when themes change
 */
public class ThemeUtils {

    /**
     * Ensure scene root has global stylesheets loaded and reapply CSS/layout.
     */
    public static void refreshScene(Scene scene) {
        if (scene == null) return;
        Runnable work = () -> {
            try {
                Parent root = scene.getRoot();
                if (root == null) return;

                // Ensure our shared stylesheets are attached to the root so popups inherit them
                try {
                    String dialogCss = ThemeUtils.class.getResource("/gui/styles/dialog-compact.css").toExternalForm();
                    if (dialogCss != null && !root.getStylesheets().contains(dialogCss)) root.getStylesheets().add(dialogCss);
                } catch (Exception ex) { /* ignore */ }
                try {
                    String comboCss = ThemeUtils.class.getResource("/gui/styles/combobox-dark.css").toExternalForm();
                    if (comboCss != null && !root.getStylesheets().contains(comboCss)) root.getStylesheets().add(comboCss);
                } catch (Exception ex) { /* ignore */ }

                // Re-apply CSS to the root and request layout
                root.applyCss();
                root.requestLayout();

                // For commonly problematic controls (ComboBox), force skin/css update
                try {
                    for (Node n : root.lookupAll(".combo-box")) {
                        if (n instanceof ComboBox) {
                            ((ComboBox<?>) n).applyCss();
                        } else {
                            n.applyCss();
                        }
                    }
                } catch (Exception ex) {
                    // ignore per-control failures
                }
            } catch (Exception ex) {
                // best-effort
            }
        };

        if (Platform.isFxApplicationThread()) work.run(); else Platform.runLater(work);
    }
}
