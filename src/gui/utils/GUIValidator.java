package gui.utils;

import javafx.scene.control.TextField;
import java.util.Arrays;
import java.util.List;

/**
 * GUIValidator - Input validation utility for GUI forms
 * 
 * Provides validation methods for various input types used in the application.
 * This replaces the console-based InputValidator with GUI-friendly validation.
 */
public class GUIValidator {
    
    // Supported courses
    private static final List<String> SHS_COURSES = Arrays.asList(
        "ABM", "STEM", "HUMSS", "TVL-ICT", "TVL-TO", "TVL-CA"
    );
    
    private static final List<String> TERTIARY_COURSES = Arrays.asList(
        "BSCS", "BSIT", "BSCpE", "BSBA", "BSA", "BSHM", "BMMA", "BSTM"
    );
    
    private static final List<String> ALL_COURSES;
    
    static {
        ALL_COURSES = Arrays.asList(
            "ABM", "STEM", "HUMSS", "TVL-ICT", "TVL-TO", "TVL-CA",
            "BSCS", "BSIT", "BSCpE", "BSBA", "BSA", "BSHM", "BMMA", "BSTM"
        );
    }
    
    // Supported sizes
    private static final List<String> SIZES = Arrays.asList(
        "XS", "S", "M", "L", "XL", "XXL", "One Size"
    );
    
    /**
     * Validate if a string is not empty
     * 
     * @param value The string to validate
     * @return true if not empty, false otherwise
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Validate if a string is a valid integer
     * 
     * @param value The string to validate
     * @return true if valid integer, false otherwise
     */
    public static boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate if a string is a valid integer within range
     * 
     * @param value The string to validate
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if valid integer within range, false otherwise
     */
    public static boolean isValidInteger(String value, int min, int max) {
        try {
            int num = Integer.parseInt(value);
            return num >= min && num <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate if a string is a valid double
     * 
     * @param value The string to validate
     * @return true if valid double, false otherwise
     */
    public static boolean isValidDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate if a string is a valid price
     * 
     * @param value The string to validate
     * @return true if valid price (0-10000), false otherwise
     */
    public static boolean isValidPrice(String value) {
        try {
            double price = Double.parseDouble(value);
            return price >= 0 && price <= 10000;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validate if a string is a valid student ID (10-12 digits)
     * 
     * @param value The string to validate
     * @return true if valid student ID, false otherwise
     */
    public static boolean isValidStudentId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.matches("\\d{10,12}");
    }
    
    /**
     * Validate if a string is a valid course code
     * 
     * @param value The string to validate
     * @return true if valid course, false otherwise
     */
    public static boolean isValidCourse(String value) {
        return value != null && ALL_COURSES.contains(value.toUpperCase());
    }
    
    /**
     * Validate if a string is a valid size
     * 
     * @param value The string to validate
     * @return true if valid size, false otherwise
     */
    public static boolean isValidSize(String value) {
        return value != null && SIZES.contains(value);
    }
    
    /**
     * Validate if a string is a valid item code (1000-9999)
     * 
     * @param value The string to validate
     * @return true if valid item code, false otherwise
     */
    public static boolean isValidItemCode(String value) {
        return isValidInteger(value, 1000, 9999);
    }
    
    /**
     * Validate if a string is a valid quantity (positive integer)
     * 
     * @param value The string to validate
     * @return true if valid quantity, false otherwise
     */
    public static boolean isValidQuantity(String value) {
        return isValidInteger(value, 1, Integer.MAX_VALUE);
    }
    
    /**
     * Get all supported courses
     * 
     * @return List of all course codes
     */
    public static List<String> getAllCourses() {
        return ALL_COURSES;
    }
    
    /**
     * Get all supported sizes
     * 
     * @return List of all sizes
     */
    public static List<String> getAllSizes() {
        return SIZES;
    }
    
    /**
     * Get SHS courses only
     * 
     * @return List of SHS course codes
     */
    public static List<String> getSHSCourses() {
        return SHS_COURSES;
    }
    
    /**
     * Get tertiary courses only
     * 
     * @return List of tertiary course codes
     */
    public static List<String> getTertiaryCourses() {
        return TERTIARY_COURSES;
    }
    
    /**
     * Apply visual validation feedback to a TextField
     * 
     * @param textField The TextField to style
     * @param isValid Whether the input is valid
     */
    public static void applyValidationStyle(TextField textField, boolean isValid) {
        if (isValid) {
            textField.setStyle("-fx-border-color: #1A7F37; -fx-border-width: 2px;");
        } else {
            textField.setStyle("-fx-border-color: #CF222E; -fx-border-width: 2px;");
        }
    }
    
    /**
     * Clear validation style from a TextField
     * 
     * @param textField The TextField to clear
     */
    public static void clearValidationStyle(TextField textField) {
        textField.setStyle("");
    }
    
    /**
     * Validate password strength (minimum 6 characters)
     * 
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Validate if passwords match
     * 
     * @param password The password
     * @param confirmPassword The confirmation password
     * @return true if they match, false otherwise
     */
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
}

