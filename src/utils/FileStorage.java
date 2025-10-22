package utils;

import student.Student;
import inventory.Item;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    private static final Path DATA_DIR = Path.of("src", "database", "data");
    private static final File USERS_FILE = DATA_DIR.resolve("users.txt").toFile();
    private static final File ITEMS_FILE = DATA_DIR.resolve("items.txt").toFile();
    
    public static boolean saveStudent(Student s) {
        try {
            // Ensure directory exists
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            
            // Ensure file exists
            if (!USERS_FILE.exists()) {
                USERS_FILE.createNewFile();
            }
            
            // Write student data
            try (FileWriter fw = new FileWriter(USERS_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                String line = String.format("%s,%s,%s,%s,%s,%s",
                    safe(s.getUsername()),
                    safe(s.getPassword()),
                    safe(s.getStudentId()),
                    safe(s.getCourse()),
                    safe(s.getFirstName()),
                    safe(s.getLastName()));
                bw.write(line);
                bw.newLine();
                bw.flush();
                fw.flush(); // Explicit flush to ensure data is written
            }
            
            // Force file timestamp update to trigger VS Code refresh
            USERS_FILE.setLastModified(System.currentTimeMillis());
            
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save student to file: " + e.getMessage());
            return false;
        }
    }
    
    public static List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();
        
        if (!USERS_FILE.exists()) {
            return students; // Return empty list if file doesn't exist
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String studentId = parts[2].trim();
                    String course = parts[3].trim();
                    String firstName = parts[4].trim();
                    String lastName = parts[5].trim();
                    
                    // NEW constructor: Student(studentId, password, course, firstName, lastName)
                    Student student = new Student(studentId, password, course, firstName, lastName);
                    students.add(student);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load students from file: " + e.getMessage());
        }
        
        return students;
    }
    
    public static List<Item> loadItems() {
        List<Item> items = new ArrayList<>();
        
        if (!ITEMS_FILE.exists()) {
            System.out.println("Items file not found. Starting with empty inventory.");
            return items;
        }
        
        try (BufferedReader br = new BufferedReader(new FileReader(ITEMS_FILE))) {
            String line = br.readLine(); // Skip header line
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        int itemCode = Integer.parseInt(parts[0].trim());
                        String itemName = parts[1].trim();
                        String course = parts[2].trim();
                        String size = parts[3].trim();
                        int quantity = Integer.parseInt(parts[4].trim());
                        double price = Double.parseDouble(parts[5].trim());
                        
                        Item item = new Item(itemCode, itemName, course, size, quantity, price);
                        items.add(item);
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid item line: " + line);
                    }
                }
            }
            System.out.println("Loaded " + items.size() + " items from database.");
        } catch (IOException e) {
            System.err.println("Failed to load items from file: " + e.getMessage());
        }
        
        return items;
    }

    private static String safe(String in) {
        if (in == null) return "";
        return in.replace(",", ";");
    }
}
