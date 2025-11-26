package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import admin.Staff;
import inventory.Item;
import inventory.Reservation;
import student.Student;

public class FileStorage {

    private static final File ITEMS_FILE = getDataFile("items.txt");
    private static final File STUDENTS_FILE = getDataFile("students.txt");
    private static final File STAFF_FILE = getDataFile("staff.txt");
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
                        // Support two formats:
                        // Old format: code,name,course,size,quantity,price
                        // New consolidated format: code,name,course,size1,qty1,size2,qty2,...,price
                        int itemCode = Integer.parseInt(parts[0].trim());
                        String itemName = parts[1].trim();
                        String course = parts[2].trim();

                        // If parts length == 6 -> old single-size line
                        if (parts.length == 6) {
                            String size = parts[3].trim();
                            int quantity = Integer.parseInt(parts[4].trim());
                            double price = Double.parseDouble(parts[5].trim());
                            Item item = new Item(itemCode, itemName, course, size, quantity, price);
                            items.add(item);
                        } else {
                            // Consolidated line: parse size/qty pairs from index 3..n-2, last token is price
                            double price = Double.parseDouble(parts[parts.length - 1].trim());
                            // tokens between 3 and parts.length-2 (inclusive) should be size,qty pairs
                            for (int i = 3; i < parts.length - 1; i += 2) {
                                String size = parts[i].trim();
                                if (i + 1 >= parts.length - 1) break; // malformed
                                String qtyStr = parts[i + 1].trim();
                                try {
                                    int quantity = Integer.parseInt(qtyStr);
                                    Item item = new Item(itemCode, itemName, course, size, quantity, price);
                                    items.add(item);
                                } catch (NumberFormatException nfe) {
                                    // skip this pair if qty invalid
                                }
                            }
                        }
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

                // Consolidate items by code so we write one line per item code with multiple size/qty pairs
                java.util.Map<Integer, java.util.List<Item>> grouped = new java.util.HashMap<>();
                for (Item it : items) {
                    grouped.computeIfAbsent(it.getCode(), k -> new java.util.ArrayList<>()).add(it);
                }

                for (java.util.Map.Entry<Integer, java.util.List<Item>> entry : grouped.entrySet()) {
                    java.util.List<Item> group = entry.getValue();
                    if (group.isEmpty()) continue;
                    // Use first item's name/course as canonical
                    Item first = group.get(0);
                    StringBuilder sb = new StringBuilder();
                    sb.append(first.getCode()).append(",");
                    sb.append(safe(first.getName())).append(",");
                    sb.append(safe(first.getCourse())).append(",");

                    // Append size,quantity pairs
                    for (Item v : group) {
                        sb.append(safe(v.getSize())).append(",");
                        sb.append(v.getQuantity()).append(",");
                    }

                    // Append price at end. If sizes have different prices, prefer first
                    sb.append(String.format("%.2f", first.getPrice()));
                    bw.write(sb.toString());
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
            System.out.println("[FileStorage] Saving " + students.size() + " students...");
            // Write to primary students file (STUDENTS_FILE)
            System.out.println("[FileStorage] Primary students file: " + STUDENTS_FILE.getAbsolutePath());
            File parentDir = STUDENTS_FILE.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STUDENTS_FILE))) {
                for (Student student : students) {
                    writer.write(studentToFileFormat(student));
                    writer.newLine();
                }
                writer.flush(); // Ensure data is written to disk
            }

            // Also mirror to alternative runtime path so file updates are visible
            File alt = new File("database/data/students.txt");
            File altParent = alt.getParentFile();
            if (altParent != null && !altParent.exists()) {
                altParent.mkdirs();
            }
            System.out.println("[FileStorage] Mirroring to alt file: " + alt.getAbsolutePath());
            try (BufferedWriter writer2 = new BufferedWriter(new FileWriter(alt))) {
                for (Student student : students) {
                    writer2.write(studentToFileFormat(student));
                    writer2.newLine();
                }
                writer2.flush();
            }

            // Force file timestamp update
            try { STUDENTS_FILE.setLastModified(System.currentTimeMillis()); } catch (Exception ignored) {}
            try { alt.setLastModified(System.currentTimeMillis()); } catch (Exception ignored) {}

            return true;
        } catch (IOException e) {
            System.err.println("[FileStorage] Failed to save students: " + e.getMessage());
            e.printStackTrace();
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
    
    // ==================== STAFF DATABASE METHODS ====================
    
    /**
     * Load all staff from staff.txt file
     * Format: staffId|password|firstName|lastName|role|isActive
     */
    public static List<Staff> loadStaff() {
        List<Staff> staffList = new ArrayList<>();

        if (!STAFF_FILE.exists()) {
            // Create default staff accounts if file doesn't exist
            createDefaultStaffFile();
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; // Skip empty lines

                Staff staff = parseStaff(line);
                if (staff != null) {
                    staffList.add(staff);
                }
            }
        } catch (IOException e) {
            // Error loading staff
        }

        return staffList;
    }
    
    /**
     * Parse staff from file format: staffId|password|firstName|lastName|role|isActive
     */
    private static Staff parseStaff(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 6) return null;
        
        try {
            String staffId = parts[0];
            String password = parts[1];
            String firstName = parts[2];
            String lastName = parts[3];
            String role = parts[4];
            boolean isActive = Boolean.parseBoolean(parts[5]);
            
            Staff staff = new Staff(staffId, password, firstName, lastName, role);
            staff.setActive(isActive);
            return staff;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Create default staff.txt file with sample accounts
     */
    private static void createDefaultStaffFile() {
        try {
            File parentDir = STAFF_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
                // Default Staff account
                writer.write("staff|staff123|John|Doe|Staff|true");
                writer.newLine();
                // Default Cashier account
                writer.write("cashier|cashier123|Jane|Smith|Cashier|true");
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            // Error creating default staff file
        }
    }
    
    /**
     * Save all staff to staff.txt file
     */
    public static boolean saveStaff(List<Staff> staffList) {
        try {
            // Ensure parent directory exists
            File parentDir = STAFF_FILE.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE))) {
                for (Staff staff : staffList) {
                    writer.write(staffToFileFormat(staff));
                    writer.newLine();
                }
                writer.flush(); // Ensure data is written to disk
            }
            
            // Force file timestamp update
            STAFF_FILE.setLastModified(System.currentTimeMillis());

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ==================== CART PERSISTENCE ====================

    // Single carts storage file for all users
    private static final File CARTS_FILE = resolveCartsFile();

    private static File resolveCartsFile() {
        File srcData = new File("src/database/data");
        File altData = new File("database/data");
        File useDir = null;
        if (srcData.exists() && srcData.isDirectory()) {
            useDir = srcData;
        } else if (altData.exists() && altData.isDirectory()) {
            useDir = altData;
        } else {
            // Prefer creating src/database/data so files live under project structure
            if (!srcData.exists()) srcData.mkdirs();
            useDir = srcData;
        }
        return new File(useDir, "carts.txt");
    }

    /**
     * Save cart lines for a student. Each line format: itemCode|size|quantity|selected
     */
    /**
     * Save cart lines for a student into a single `carts.txt` file.
     * Each stored line format in `carts.txt`: studentId|itemCode|size|quantity|selected
     */
    public static boolean saveCart(String studentId, List<String> cartLines) {
        try {
            File parentDir = CARTS_FILE.getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();

            // Read existing cart entries (if any)
            List<String> existing = new ArrayList<>();
            if (CARTS_FILE.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(CARTS_FILE))) {
                    String l;
                    while ((l = reader.readLine()) != null) {
                        if (l.trim().isEmpty()) continue;
                        existing.add(l);
                    }
                } catch (IOException e) {
                    // ignore read errors and continue with empty list
                }
            }

            // Migrate any legacy per-user cart files (cart_{id}.txt) into carts.txt
            File dataDir = CARTS_FILE.getParentFile();
            if (dataDir != null && dataDir.exists()) {
                File[] files = dataDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        String name = f.getName();
                        if (name.startsWith("cart_") && name.endsWith(".txt") && !f.equals(CARTS_FILE)) {
                            String sid = name.substring(5, name.length() - 4); // between 'cart_' and '.txt'
                            if (sid == null || sid.trim().isEmpty()) sid = "guest";
                            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                                String ln;
                                while ((ln = r.readLine()) != null) {
                                    if (ln.trim().isEmpty()) continue;
                                    String combined = sid + "|" + ln.trim();
                                    if (!existing.contains(combined)) existing.add(combined);
                                }
                            } catch (IOException ex) {
                                // ignore
                            }
                            // Delete legacy file after migration
                            try { f.delete(); } catch (Exception ex) { }
                        }
                    }
                }
            }

            // Filter out previous lines for this student
            String prefix = (studentId == null ? "guest" : studentId) + "|";
            List<String> filtered = new ArrayList<>();
            for (String l : existing) {
                if (!l.startsWith(prefix)) filtered.add(l);
            }

            // Add new lines for this student, prefixing each with studentId|
            for (String line : cartLines) {
                if (line == null || line.trim().isEmpty()) continue;
                filtered.add(prefix + line.trim());
            }

            // Write back all carts
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(CARTS_FILE, false))) {
                for (String l : filtered) {
                    writer.write(l == null ? "" : l);
                    writer.newLine();
                }
                writer.flush();
            }

            CARTS_FILE.setLastModified(System.currentTimeMillis());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Load raw cart lines for a student. Returns list of String[] (split by '|').
     */
    /**
     * Load raw cart lines for a student from the single `carts.txt` file.
     * Returns list of String[] (split by '|') where returned parts exclude the leading studentId.
     */
    public static List<String[]> loadCart(String studentId) {
        List<String[]> result = new ArrayList<>();
        if (!CARTS_FILE.exists()) return result;

        String prefix = (studentId == null ? "guest" : studentId) + "|";
        try (BufferedReader reader = new BufferedReader(new FileReader(CARTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (!line.startsWith(prefix)) continue;
                // remove prefix
                String payload = line.substring(prefix.length());
                String[] parts = payload.split("\\|");
                result.add(parts);
            }
        } catch (IOException e) {
            // ignore on load failure
        }

        return result;
    }

    // ==================== NOTIFICATIONS SEEN PERSISTENCE ====================

    /**
     * Save seen notification ids for a student. Each line contains a reservationId integer.
     */
    public static boolean saveSeenNotifications(String studentId, java.util.Set<Integer> ids) {
        if (studentId == null) return false;
        // Persist only to the primary txt data folder (src/database/data)
        File srcDir = new File("src/database/data");
        if (!srcDir.exists()) srcDir.mkdirs();
        File f = new File(srcDir, "seen_notifications_" + studentId + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, false))) {
            for (Integer id : ids) { writer.write(String.valueOf(id)); writer.newLine(); }
            writer.flush();
            try { f.setLastModified(System.currentTimeMillis()); } catch (Exception ex) {}
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Load seen notification ids for a student. Returns empty set if none.
     */
    public static java.util.Set<Integer> loadSeenNotifications(String studentId) {
        java.util.Set<Integer> out = new java.util.HashSet<>();
        if (studentId == null) return out;
        File srcDir = new File("src/database/data");
        File f = new File(srcDir, "seen_notifications_" + studentId + ".txt");
        if (!f.exists()) return out;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim(); if (line.isEmpty()) continue;
                try { out.add(Integer.parseInt(line)); } catch (NumberFormatException ex) {}
            }
        } catch (IOException e) { }
        return out;
    }
    
    /**
     * Convert staff to file format
     */
    private static String staffToFileFormat(Staff staff) {
        return staff.getStaffId() + "|" +
               staff.getPassword() + "|" +
               staff.getFirstName() + "|" +
               staff.getLastName() + "|" +
               staff.getRole() + "|" +
               staff.isActive();
    }
    
    /**
     * Add a new staff and save to database
     */
    public static boolean addStaff(List<Staff> staffList, Staff newStaff) {
        staffList.add(newStaff);
        return saveStaff(staffList);
    }
    
    /**
     * Find staff by ID
     */
    public static Staff findStaffById(List<Staff> staffList, String staffId) {
        for (Staff staff : staffList) {
            if (staff.getStaffId().equals(staffId)) {
                return staff;
            }
        }
        return null;
    }
    
    /**
     * Check if staff ID exists
     */
    public static boolean staffExists(List<Staff> staffList, String staffId) {
        return findStaffById(staffList, staffId) != null;
    }
    
    /**
     * Update staff in database
     */
    public static boolean updateStaff(List<Staff> staffList, Staff updatedStaff) {
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getStaffId().equals(updatedStaff.getStaffId())) {
                staffList.set(i, updatedStaff);
                return saveStaff(staffList);
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
    * Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason|bundleId|paymentDeadline|scheduledPickup|replacementItemCode|replacementItemName|replacementSize|replacementNote|claimProofImagePath|scheduledPickupEnd
     */
    private static Reservation parseReservation(String line) {
        String[] parts = line.split("\\|", -1); // -1 to keep empty trailing fields
        if (parts.length < 15) {
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
            String bundleId = (parts.length > 15 && !parts[15].isEmpty()) ? parts[15] : null;
            String paymentDeadlineStr = (parts.length > 16 && !parts[16].isEmpty()) ? parts[16] : "";
            String scheduledPickupStr = (parts.length > 17 && !parts[17].isEmpty()) ? parts[17] : "";
            
            // Parse replacement item info (indices shifted by scheduledPickup)
            int replacementItemCode = (parts.length > 18 && !parts[18].isEmpty()) ? Integer.parseInt(parts[18]) : 0;
            String replacementItemName = (parts.length > 19 && !parts[19].isEmpty()) ? parts[19] : "";
            String replacementSize = (parts.length > 20 && !parts[20].isEmpty()) ? parts[20] : "";
            String replacementNote = (parts.length > 21 && !parts[21].isEmpty()) ? parts[21] : "";
            String claimProofImagePath = (parts.length > 22 && !parts[22].isEmpty()) ? parts[22] : "";
            String scheduledPickupEndStr = (parts.length > 23 && !parts[23].isEmpty()) ? parts[23] : "";

            // Create reservation with bundleId
            Reservation reservation = new Reservation(reservationId, studentName, studentId, course,
                                                      itemCode, itemName, quantity, totalPrice, size, bundleId);

            // Migrate old status strings to new ones
            if (status.equals("PICKUP REQUESTED - AWAITING ADMIN APPROVAL")) {
                status = "PICKUP REQUESTED - AWAITING STAFF APPROVAL";
            }
            // Migrate RETURN REQUESTED to REPLACEMENT REQUESTED
            if (status.equals("RETURN REQUESTED")) {
                status = "REPLACEMENT REQUESTED";
            }

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

            // Set scheduled pickup if exists
            if (!scheduledPickupStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime scheduled = LocalDateTime.parse(scheduledPickupStr, formatter);
                reservation.setScheduledPickupDateTime(scheduled);
            }
            
            // Set scheduled pickup end time if exists
            if (!scheduledPickupEndStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime scheduledEnd = LocalDateTime.parse(scheduledPickupEndStr, formatter);
                reservation.setScheduledPickupEndDateTime(scheduledEnd);
            }

            // Set payment deadline if exists
            if (!paymentDeadlineStr.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime paymentDeadline = LocalDateTime.parse(paymentDeadlineStr, formatter);
                reservation.setPaymentDeadline(paymentDeadline);
            }

            // Set reason if exists
            if (!reason.isEmpty()) {
                reservation.setReason(reason);
            }
            
            // Set replacement item if exists
            if (replacementItemCode > 0 && !replacementItemName.isEmpty()) {
                reservation.setReplacementItem(replacementItemCode, replacementItemName, replacementSize);
                if (replacementNote != null && !replacementNote.isEmpty()) {
                    reservation.setReplacementNote(replacementNote);
                }
            }
            
            // Set claim proof image path if exists
            if (!claimProofImagePath.isEmpty()) {
                reservation.setClaimProofImagePath(claimProofImagePath);
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
     * Format: reservationId|studentName|studentId|course|itemCode|itemName|quantity|totalPrice|size|status|isPaid|paymentMethod|reservationTime|completedDate|reason|bundleId|paymentDeadline|replacementItemCode|replacementItemName|replacementSize
     */
    private static String reservationToFileFormat(Reservation reservation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String reservationTime = reservation.getReservationTime().format(formatter);
        String completedDate = reservation.getCompletedDate() != null ? 
                               reservation.getCompletedDate().format(formatter) : "";
        String reason = reservation.getReason() != null ? reservation.getReason() : "";
        String bundleId = reservation.getBundleId() != null ? reservation.getBundleId() : "";
        String paymentDeadline = reservation.getPaymentDeadline() != null ? 
                                 reservation.getPaymentDeadline().format(formatter) : "";
        String replacementItemCode = reservation.getReplacementItemCode() > 0 ? 
                                      String.valueOf(reservation.getReplacementItemCode()) : "";
        String replacementItemName = reservation.getReplacementItemName() != null ? 
                                      reservation.getReplacementItemName() : "";
         String replacementSize = reservation.getReplacementSize() != null ? 
                         reservation.getReplacementSize() : "";
         String replacementNote = reservation.getReplacementNote() != null ? reservation.getReplacementNote() : "";
         String scheduledPickup = reservation.getScheduledPickupDateTime() != null ? reservation.getScheduledPickupDateTime().format(formatter) : "";
         String scheduledPickupEnd = reservation.getScheduledPickupEndDateTime() != null ? reservation.getScheduledPickupEndDateTime().format(formatter) : "";
         String claimProofImagePath = reservation.getClaimProofImagePath() != null ? reservation.getClaimProofImagePath() : "";
        
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
               reason + "|" +
                             bundleId + "|" +
                             paymentDeadline + "|" +
                             scheduledPickup + "|" +
                         replacementItemCode + "|" +
                         replacementItemName + "|" +
                         replacementSize + "|" +
                         replacementNote + "|" +
                         claimProofImagePath + "|" +
                         scheduledPickupEnd;
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
