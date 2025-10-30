package gui.utils;

import atlantafx.base.theme.*;
import javafx.application.Application;

/**
 * ThemeManager - Manages application themes using AtlantaFX
 * 
 * Provides methods to switch between different AtlantaFX themes
 * (Light/Dark modes with various color schemes)
 */
public class ThemeManager {
    
    /**
     * Available theme options
     */
    public enum Theme {
        PRIMER_LIGHT("Primer Light", new PrimerLight()),
        PRIMER_DARK("Primer Dark", new PrimerDark()),
        NORD_LIGHT("Nord Light", new NordLight()),
        NORD_DARK("Nord Dark", new NordDark()),
        CUPERTINO_LIGHT("Cupertino Light", new CupertinoLight()),
        CUPERTINO_DARK("Cupertino Dark", new CupertinoDark()),
        DRACULA("Dracula", new Dracula());
        
        private final String displayName;
        private final atlantafx.base.theme.Theme theme;
        
        Theme(String displayName, atlantafx.base.theme.Theme theme) {
            this.displayName = displayName;
            this.theme = theme;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public atlantafx.base.theme.Theme getTheme() {
            return theme;
        }
    }
    
    private static Theme currentTheme = Theme.PRIMER_LIGHT;
    
    /**
     * Initialize the theme manager with default theme
     */
    public static void initialize() {
        setTheme(currentTheme);
    }
    
    /**
     * Set the application theme
     * 
     * @param theme The theme to apply
     */
    public static void setTheme(Theme theme) {
        currentTheme = theme;
        Application.setUserAgentStylesheet(theme.getTheme().getUserAgentStylesheet());
    }
    
    /**
     * Get the current theme
     * 
     * @return The current theme
     */
    public static atlantafx.base.theme.Theme getCurrentTheme() {
        return currentTheme.getTheme();
    }
    
    /**
     * Get the current theme enum
     * 
     * @return The current theme enum value
     */
    public static Theme getCurrentThemeEnum() {
        return currentTheme;
    }
    
    /**
     * Toggle between light and dark mode (Primer themes)
     */
    public static void toggleLightDark() {
        if (currentTheme == Theme.PRIMER_LIGHT) {
            setTheme(Theme.PRIMER_DARK);
        } else if (currentTheme == Theme.PRIMER_DARK) {
            setTheme(Theme.PRIMER_LIGHT);
        } else if (currentTheme == Theme.NORD_LIGHT) {
            setTheme(Theme.NORD_DARK);
        } else if (currentTheme == Theme.NORD_DARK) {
            setTheme(Theme.NORD_LIGHT);
        } else if (currentTheme == Theme.CUPERTINO_LIGHT) {
            setTheme(Theme.CUPERTINO_DARK);
        } else if (currentTheme == Theme.CUPERTINO_DARK) {
            setTheme(Theme.CUPERTINO_LIGHT);
        } else {
            // Dracula is always dark, switch to Primer Light
            setTheme(Theme.PRIMER_LIGHT);
        }
    }
    
    /**
     * Check if current theme is dark mode
     * 
     * @return true if dark mode, false otherwise
     */
    public static boolean isDarkMode() {
        return currentTheme == Theme.PRIMER_DARK || 
               currentTheme == Theme.NORD_DARK || 
               currentTheme == Theme.CUPERTINO_DARK || 
               currentTheme == Theme.DRACULA;
    }
    
    /**
     * Get all available themes
     * 
     * @return Array of all theme options
     */
    public static Theme[] getAllThemes() {
        return Theme.values();
    }
}

