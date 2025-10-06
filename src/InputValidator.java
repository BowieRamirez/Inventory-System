import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class InputValidator {
    private Scanner scanner;
    
    // Valid course codes
    private static final String[] SHS_COURSES = {
        "STEM", "ABM", "HUMSS", "GAS", "TVL-ICT", "TVL-HE", "TVL-IA", "ARTS"
    };
    
    private static final String[] TERTIARY_COURSES = {
        "BSCS", "BSIT", "BSCpE", "BSBA", "BSA", "BSHM", "BSTM", "BMMA"
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
                    System.out.println("❌ Error: Input cannot be empty. Please enter a number between " + min + " and " + max + ".");
                    continue;
                }
                
                int value = Integer.parseInt(input);
                if (value < min || value > max) {
                    System.out.println("❌ Error: Number must be between " + min + " and " + max + ". You entered: " + value);
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Error: Please enter a valid number (integers only). No letters or special characters allowed.");
            }
        }
    }
    
    public double getValidPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("❌ Error: Price cannot be empty. Please enter a valid price (e.g., 450.00).");
                    continue;
                }
                
                double price = Double.parseDouble(input);
                if (price < 0) {
                    System.out.println("❌ Error: Price cannot be negative. Please enter a positive number.");
                    continue;
                }
                if (price > 10000) {
                    System.out.println("❌ Error: Price seems too high. Maximum allowed is ₱10,000. You entered: ₱" + price);
                    continue;
                }
                return price;
            } catch (NumberFormatException e) {
                System.out.println("❌ Error: Please enter a valid price (numbers only, e.g., 450.00).");
            }
        }
    }
    
    public String getValidCourse(String prompt) {
        while (true) {
            System.out.println("\n📚 Valid Course Codes:");
            System.out.println("Senior High School: " + String.join(", ", SHS_COURSES));
            System.out.println("Tertiary Programs: " + String.join(", ", TERTIARY_COURSES));
            System.out.print(prompt);
            
            String course = scanner.nextLine().trim().toUpperCase();
            if (course.isEmpty()) {
                System.out.println("❌ Error: Course cannot be empty. Please enter a valid course code.");
                continue;
            }
            
            List<String> allCourses = Arrays.asList(SHS_COURSES);
            List<String> tertiaryCourses = Arrays.asList(TERTIARY_COURSES);
            
            if (allCourses.contains(course) || tertiaryCourses.contains(course)) {
                return course;
            } else {
                System.out.println("❌ Error: Invalid course code '" + course + "'. Please use only the codes listed above.");
                System.out.println("Example: BSIT, BSCS, STEM, ABM, etc.");
            }
        }
    }
    
    public String getValidSize(String prompt) {
        while (true) {
            System.out.println("\n👕 Valid Sizes: " + String.join(", ", VALID_SIZES));
            System.out.print(prompt);
            
            String size = scanner.nextLine().trim().toUpperCase();
            if (size.isEmpty()) {
                System.out.println("❌ Error: Size cannot be empty. Please enter a valid size.");
                continue;
            }
            
            // Handle "ONE SIZE" input
            if (size.equals("ONE SIZE") || size.equals("ONESIZE")) {
                return "One Size";
            }
            
            List<String> validSizes = Arrays.asList(VALID_SIZES);
            if (validSizes.contains(size)) {
                return size;
            } else {
                System.out.println("❌ Error: Invalid size '" + size + "'. Please use only: " + String.join(", ", VALID_SIZES));
            }
        }
    }
    
    public String getValidNonEmptyString(String prompt, String fieldName) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("❌ Error: " + fieldName + " cannot be empty. Please enter a valid " + fieldName.toLowerCase() + ".");
                continue;
            }
            if (input.length() > 50) {
                System.out.println("❌ Error: " + fieldName + " is too long. Maximum 50 characters allowed.");
                continue;
            }
            return input;
        }
    }
    
    public String getValidStudentId(String prompt) {
        while (true) {
            System.out.print(prompt);
            String studentId = scanner.nextLine().trim();
            if (studentId.isEmpty()) {
                System.out.println("❌ Error: Student ID cannot be empty.");
                continue;
            }
            if (studentId.length() < 6 || studentId.length() > 12) {
                System.out.println("❌ Error: Student ID must be between 6-12 characters. You entered: " + studentId.length() + " characters.");
                continue;
            }
            if (!studentId.matches("\\d+")) {
                System.out.println("❌ Error: Student ID must contain only numbers. You entered: " + studentId);
                continue;
            }
            return studentId;
        }
    }
    
    public boolean getValidYesNo(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            } else {
                System.out.println("❌ Error: Please enter 'y' for yes or 'n' for no. You entered: " + input);
            }
        }
    }
    
    public String getValidPassword(String prompt) {
        while (true) {
            System.out.print(prompt);
            String password = scanner.nextLine().trim();
            if (password.isEmpty()) {
                System.out.println("❌ Error: Password cannot be empty.");
                continue;
            }
            if (password.length() < 6) {
                System.out.println("❌ Error: Password must be at least 6 characters long.");
                continue;
            }
            if (password.length() > 20) {
                System.out.println("❌ Error: Password cannot be longer than 20 characters.");
                continue;
            }
            return password;
        }
    }
    
    public boolean confirmPassword(String password) {
        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine().trim();
        if (!password.equals(confirmPassword)) {
            System.out.println("❌ Error: Passwords do not match. Please try again.");
            return false;
        }
        System.out.println("✅ Password confirmed successfully!");
        return true;
    }
    
    public static boolean isValidCourse(String course) {
        List<String> allCourses = Arrays.asList(SHS_COURSES);
        List<String> tertiaryCourses = Arrays.asList(TERTIARY_COURSES);
        return allCourses.contains(course.toUpperCase()) || tertiaryCourses.contains(course.toUpperCase());
    }
    
    public static String[] getAllValidCourses() {
        String[] allCourses = new String[SHS_COURSES.length + TERTIARY_COURSES.length];
        System.arraycopy(SHS_COURSES, 0, allCourses, 0, SHS_COURSES.length);
        System.arraycopy(TERTIARY_COURSES, 0, allCourses, SHS_COURSES.length, TERTIARY_COURSES.length);
        return allCourses;
    }
    
    public static String[] getSHSCourses() {
        return SHS_COURSES.clone();
    }
    
    public static String[] getTertiaryCourses() {
        return TERTIARY_COURSES.clone();
    }
}