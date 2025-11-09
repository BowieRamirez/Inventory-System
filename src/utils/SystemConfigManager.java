package utils;

import java.io.*;
import java.util.Properties;

/**
 * SystemConfigManager - Manages system configuration including maintenance mode
 */
public class SystemConfigManager {
    private static final String CONFIG_FILE = "src/database/data/system_config.txt";
    private Properties config;
    
    public SystemConfigManager() {
        config = new Properties();
        loadConfig();
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfig() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            config.load(fis);
        } catch (IOException e) {
            // If file doesn't exist, create with default values
            config.setProperty("maintenanceMode", "false");
            config.setProperty("maintenanceMessage", "System is currently under maintenance. Please try again later.");
            saveConfig();
        }
    }
    
    /**
     * Save configuration to file
     */
    private void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            config.store(fos, "System Configuration");
            SystemLogger.logActivity("System configuration updated");
        } catch (IOException e) {
            SystemLogger.logError("Failed to save system configuration", e);
        }
    }
    
    /**
     * Check if maintenance mode is active
     */
    public boolean isMaintenanceModeActive() {
        return Boolean.parseBoolean(config.getProperty("maintenanceMode", "false"));
    }
    
    /**
     * Set maintenance mode status
     */
    public void setMaintenanceMode(boolean active) {
        config.setProperty("maintenanceMode", String.valueOf(active));
        saveConfig();
        SystemLogger.logActivity("Maintenance mode " + (active ? "ACTIVATED" : "DEACTIVATED"));
    }
    
    /**
     * Get maintenance message
     */
    public String getMaintenanceMessage() {
        return config.getProperty("maintenanceMessage", "System is currently under maintenance. Please try again later.");
    }
    
    /**
     * Set maintenance message
     */
    public void setMaintenanceMessage(String message) {
        config.setProperty("maintenanceMessage", message);
        saveConfig();
    }
    
    /**
     * Get singleton instance
     */
    private static SystemConfigManager instance;
    
    public static SystemConfigManager getInstance() {
        if (instance == null) {
            instance = new SystemConfigManager();
        }
        return instance;
    }
}
