package utils;

import inventory.Item;
import inventory.Reservation;
import student.Student;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {

    private static final File ITEMS_FILE = getDataFile("items.txt");
    private static final File STUDENTS_FILE = getDataFile("students.txt");
    private static final File RESERVATIONS_FILE = getDataFile("reservations.txt");
    
    private static File getDataFile(String filename) {
        // Try src/database/data first (when running from project root)
        File file = new File("src/database/data/" + filename);
        if (file.exists()) {
            return file;
        }
        // Otherwise use database/data (when running from src directory)
        return new File("database/data/" + filename);
    }
    
    public static List<Item> loadItems() {
        List<Item> items = new ArrayList<>();

        if (!ITEMS_FILE.exists()) {
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
                        // Skip invalid lines silently
                    }
                }
            }
        } catch (IOException e) {
            // Failed to load items
        }

        return items;
    }
    
    public static boolean saveItems(List<Item> items) {
        try {
            // Ensure parent directory exists
            File parentDir = ITEMS_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // Write items data
            try (FileWriter fw = new FileWriter(ITEMS_FILE, false);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                
                // Write header
                bw.write("ItemCode,ItemName,Course,Size,Quantity,Price");
                bw.newLine();
                
                // Write each item
                for (Item item : items) {
                    String line = String.format("%d,%s,%s,%s,%d,%.2f",
                        item.getCode(),
                        safe(item.getName()),
                        safe(item.getCourse()),
                        safe(item.getSize()),
                        item.getQuantity(),
                        item.getPrice());
                    bw.write(line);
                    bw.newLine();
                }
                
                bw.flush();
                fw.flush(); // Explicit flush to ensure data is written
            }
            
            // Force file timestamp update to trigger VS Code refresh
            ITEMS_FILE.setLastModified(System.currentTimeMillis());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static String safe(String in) {
        if (in == null) return "";
        return in.replace(",", ";");
    }
    
    // ==================== STUDENT DATABASE METHODS ====================
    
    /**
     * Load all students from students.txt file
     * Format: studentId|password|course|firstName|lastName|gender|isActive
     */
    public static List<Student> loadStudents() {
        List<Student> students = new ArrayList<>();

        if (!STUDENTS_FILE.exists()) {
            return students;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(STUDENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines

                Student student = parseStudent(line);
                if (student != null) {
                    students.add(student);
                }
            }
        } catch (IOException e) {
            // Error loading students
        }

        return students;
    }
    
    /**
     * Parse student from file format: studentId|password|course|firstName|lastName|gender|isActive
     */
    private static Student parseStudent(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 7) return null;
        
        try {
            String studentId = parts[0];
            String password = parts[1];
            String course = parts[2];
            String firstName = parts[3];
            String lastName = parts[4];
            String gender = parts[5];
            boolean isActive = Boolean.parseBoolean(parts[6]);
            
            Student student = new Student(studentId, password, course, firstName, lastName, gender);
            student.setActive(isActive);
            return student;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Save all students to students.txt file
     */
    public static boolean saveStudents(List<Student> students) {
        try {
            // Ensure parent directory exists
            File parentDir = STUDENTS_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENTS_FILE))) {
                for (Student student : students) {
                    writer.write(studentToFileFormat(student));
                    writer.newLine();
                }
                writer.flush(); // Ensure data is written to disk
            }
            
            // Force file timestamp update
            STUDENTS_FILE.setLastModified(System.currentTimeMillis());

            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Convert student to file format
     */
    private static String studentToFileFormat(Student student) {
        return student.getStudentId() + "|" +
               student.getPassword() + "|" +
               student.getCourse() + "|" +
               student.getFirstName() + "|" +
               student.getLastName() + "|" +
               student.getGender() + "|" +
               student.isActive();
    }
    
    /**
     * Add a new student and save to database
     */
    public static boolean addStudent(List<Student> students, Student newStudent) {
        students.add(newStudent);
        return saveStudents(students);
    }
    
    /**
     * Find student by ID
     */
    public static Student findStudentById(List<Student> students, String studentId) {
        for (Student student : students) {
            if (student.getStudentId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }
    
    /**
     * Check if student ID exists
     */
    public static boolean studentExists(List<Student> students, String studentId) {
        return findStudentById(students, studentId) != null;
    }
    
    /**
     * Update student in database
     */
    public static boolean updateStudent(List<Student> students, Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getStudentId().equals(updatedStudent.getStudentId())) {
                students.set(i, updatedStudent);
                return saveStudents(students);
            }
        }
        return false;
    }
    
    // ==================== RESERVATION DATABASE METHODS ====================
    
    /**
     * Load all reservations from reservations.txt file
     * Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason
     */
    public static List<Reservation> loadReservations() {
        List<Reservation> reservations = new ArrayList<>();

        if (!RESERVATIONS_FILE.exists()) {
            return reservations;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(RESERVATIONS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines

                Reservation reservation = parseReservation(line);
                if (reservation != null) {
                    reservations.add(reservation);
                }
            }
        } catch (IOException e) {
            // Error loading reservations
        }

        return reservations;
    }
    
    /**
     * Parse reservation from file format
     * Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason
     */
    private static Reservation parseReservation(String line) {
        String[] parts = line.split("\\|", -1); // -1 to keep empty trailing fields
        if (parts.length != 15) {
            return null;
        }

        try {
            int reservationId = Integer.parseInt(parts[0]);
            String studentName = parts[1];
            String studentId = parts[2];
            String course = parts[3];
            int itemCode = Integer.parseInt(parts[4]);
            String itemName = parts[5];
            int quantity = Integer.parseInt(parts[6]);
            double totalPrice = Double.parseDouble(parts[7]);
            String size = parts[8];
            String status = parts[9];
            boolean isPaid = Boolean.parseBoolean(parts[10]);
            String paymentMethod = parts[11];
            String completedDateStr = parts[13];
            String reason = parts[14];

            // Create reservation using reflection to set private fields
            Reservation reservation = new Reservation(reservationId, studentName, studentId, course,
                                                      itemCode, itemName, quantity, totalPrice, size);

            // Set status and payment info
            reservation.setStatus(status);
            reservation.setPaid(isPaid);
            reservation.setPaymentMethod(paymentMethod);

            // Set completed date if exists
            if (!completedDateStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime completedDate = LocalDateTime.parse(completedDateStr, formatter);
                reservation.setCompletedDate(completedDate);
            }

            // Set reason if exists
            if (!reason.isEmpty()) {
                reservation.setReason(reason);
            }

            return reservation;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Save all reservations to reservations.txt file
     */
    public static boolean saveReservations(List<Reservation> reservations) {
        try {
            // Ensure parent directory exists
            File parentDir = RESERVATIONS_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
                for (Reservation reservation : reservations) {
                    writer.write(reservationToFileFormat(reservation));
                    writer.newLine();
                }
                writer.flush(); // Ensure data is written to disk
            }
            
            // Force file timestamp update
            RESERVATIONS_FILE.setLastModified(System.currentTimeMillis());

            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Convert reservation to file format
     * Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason
     */
    private static String reservationToFileFormat(Reservation reservation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String reservationTime = reservation.getReservationTime().format(formatter);
        String completedDate = reservation.getCompletedDate() != null ? 
                               reservation.getCompletedDate().format(formatter) : "";
        String reason = reservation.getReason() != null ? reservation.getReason() : "";
        
        return reservation.getReservationId() + "|" +
               reservation.getStudentName() + "|" +
               reservation.getStudentId() + "|" +
               reservation.getCourse() + "|" +
               reservation.getItemCode() + "|" +
               reservation.getItemName() + "|" +
               reservation.getQuantity() + "|" +
               reservation.getTotalPrice() + "|" +
               reservation.getSize() + "|" +
               reservation.getStatus() + "|" +
               reservation.isPaid() + "|" +
               reservation.getPaymentMethod() + "|" +
               reservationTime + "|" +
               completedDate + "|" +
               reason;
    }
    
    /**
     * Get the next reservation ID from existing reservations
     */
    public static int getNextReservationId(List<Reservation> reservations) {
        int maxId = 5000; // Start from 5001
        for (Reservation r : reservations) {
            if (r.getReservationId() > maxId) {
                maxId = r.getReservationId();
            }
        }
        return maxId + 1;
    }
}
