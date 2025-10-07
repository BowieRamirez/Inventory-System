package utils;

import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class InputValidator {
    private Scanner scanner;
    
    private static final String[] SHS_COURSES = {
        "ABM", "STEM", "HUMSS", "TVL-ICT", "TVL-TO", "TVL-CA"
    };
    
    private static final String[] TERTIARY_COURSES = {
        "BSCS", "BSIT", "BSCpE", "BSBA", "BSA", "BSHM", "BMMA", "BSTM"
    };
    
    private static final String[] VALID_SIZES = {
        "XS", "S", "M", "L", "XL", "XXL", "One Size"
    };
    
    public InputValidator(Scanner scanner) {
        this.scanner = scanner;
    }
    
    public int getValidInteger(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(" Error: Input cannot be empty.");
                    continue;
                }
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println(" Error: Number must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println(" Error: Please enter a valid number.");
            }
        }
    }
    
    public double getValidPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println(" Error: Price cannot be empty.");
                    continue;
                }
                double price = Double.parseDouble(input);
                if (price >= 0 && price <= 10000) {
                    return price;
                }
                System.out.println(" Error: Price must be between 0 and 10000.");
            } catch (NumberFormatException e) {
                System.out.println(" Error: Please enter a valid price.");
            }
        }
    }
    
    public String getValidCourse(String prompt) {
        while (true) {
            System.out.println("\n📚 Valid Course Codes:");
            System.out.println("=== Senior High School ===");
            System.out.println("ABM    - Accountancy, Business, and Management");
            System.out.println("STEM   - Science, Technology, Engineering, and Mathematics");
            System.out.println("HUMSS  - Humanities and Social Sciences");
            System.out.println("TVL-ICT - IT in Mobile App and Web Development");
            System.out.println("TVL-TO  - Tourism Operations");
            System.out.println("TVL-CA  - Culinary Arts");
            
            System.out.println("\n=== Tertiary Programs ===");
            System.out.println("BSCS   - Bachelor of Science in Computer Science");
            System.out.println("BSIT   - Bachelor of Science in Information Technology");
            System.out.println("BSCpE  - Bachelor of Science in Computer Engineering");
            System.out.println("BSBA   - Bachelor of Science in Business Administration");
            System.out.println("BSA    - Bachelor of Science in Accountancy");
            System.out.println("BSHM   - Bachelor of Science in Hospitality Management");
            System.out.println("BMMA   - Bachelor of Multimedia Arts");
            System.out.println("BSTM   - Bachelor of Science in Tourism Management");
            
            System.out.print(prompt);
            
            String course = scanner.nextLine().trim().toUpperCase();
            if (course.isEmpty()) {
                System.out.println(" Error: Course cannot be empty.");
                continue;
            }
            
            if (isValidCourse(course)) {
                return course;
            }
            System.out.println(" Error: Invalid course code.");
        }
    }
    
    public String getValidSize(String prompt) {
        while (true) {
            System.out.println("\n Valid Sizes: " + String.join(", ", VALID_SIZES));
            System.out.print(prompt);
            
            String size = scanner.nextLine().trim().toUpperCase();
            if (size.isEmpty()) {
                System.out.println(" Error: Size cannot be empty.");
                continue;
            }
            
            if (size.equals("ONE SIZE") || size.equals("ONESIZE")) {
                return "One Size";
            }
            
            List<String> validSizes = Arrays.asList(VALID_SIZES);
            if (validSizes.contains(size)) {
                return size;
            }
            System.out.println(" Error: Invalid size.");
        }
    }
    
    public String getValidNonEmptyString(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty() && input.length() <= 50) {
                return input;
            }
            if (input.isEmpty()) {
                System.out.println(" Error: " + fieldName + " cannot be empty.");
            } else {
                System.out.println(" Error: " + fieldName + " too long (max 50 characters).");
            }
        }
    }
    
    public String getValidStudentId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String id = scanner.nextLine().trim();
            if (id.matches("\\d{6,12}")) {
                return id;
            }
            System.out.println(" Error: Student ID must be 6-12 digits.");
        }
    }
    
    public boolean getValidYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            }
            if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println(" Error: Please enter 'y' or 'n'.");
        }
    }
    
    public static boolean isValidCourse(String course) {
        if (course == null) return false;
        String c = course.trim().toUpperCase();
        for (String s : SHS_COURSES) {
            if (s.equals(c)) return true;
        }
        for (String s : TERTIARY_COURSES) {
            if (s.equals(c)) return true;
        }
        return false;
    }
    
    public static String[] getAllValidCourses() {
        String[] all = new String[SHS_COURSES.length + TERTIARY_COURSES.length];
        System.arraycopy(SHS_COURSES, 0, all, 0, SHS_COURSES.length);
        System.arraycopy(TERTIARY_COURSES, 0, all, SHS_COURSES.length, TERTIARY_COURSES.length);
        return all;
    }
    
    public static String[] getSHSCourses() {
        return SHS_COURSES.clone();
    }
    
    public static String[] getTertiaryCourses() {
        return TERTIARY_COURSES.clone();
    }
}