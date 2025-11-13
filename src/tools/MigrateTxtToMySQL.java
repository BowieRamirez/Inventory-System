package tools;

import java.io.*;
import java.sql.*;
// Removed DateTimeFormatter import (not used after refactor)
import java.util.ArrayList;
import java.util.List;

import admin.Staff;
import dao.StudentDAO;
import inventory.Item;
import inventory.Reservation;
import student.Student;
import utils.DBManager;
import utils.FileStorage;
import gui.utils.GUIValidator;

public class MigrateTxtToMySQL {

    public static void main(String[] args) throws Exception {
        if (!DBManager.isConfigured()) {
            System.err.println("Database not configured. Set env DB_URL, DB_USER, DB_PASSWORD or create src/database/data/db.properties");
            System.exit(1);
        }
        try (Connection con = DBManager.getConnection()) {
            con.setAutoCommit(false);
            createTables(con);
            migrateStudents(con);
            migrateStaff(con);
            migrateItems(con);
            migrateReservations(con);
            migrateReceipts(con);
            migrateStockLogs(con);
            migrateSystemConfig(con);
            con.commit();
            System.out.println("Migration completed successfully.");
        }
    }

    private static void createTables(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS students ("+
                "student_id VARCHAR(20) PRIMARY KEY, password VARCHAR(255) NOT NULL, course_code VARCHAR(128) NOT NULL, " +
                "first_name VARCHAR(100) NOT NULL, last_name VARCHAR(100) NOT NULL, gender VARCHAR(10) NOT NULL, is_active BOOLEAN NOT NULL DEFAULT TRUE)");

            st.execute("CREATE TABLE IF NOT EXISTS staff ("+
                "staff_id VARCHAR(50) PRIMARY KEY, password VARCHAR(255) NOT NULL, first_name VARCHAR(100), last_name VARCHAR(100), role VARCHAR(30), is_active BOOLEAN NOT NULL DEFAULT TRUE)");

            st.execute("CREATE TABLE IF NOT EXISTS items ("+
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, item_code INT, item_name VARCHAR(255), course_code VARCHAR(128), size VARCHAR(20), quantity INT, price DECIMAL(10,2), " +
                "UNIQUE KEY uk_item (item_code, course_code, size))");

            st.execute("CREATE TABLE IF NOT EXISTS reservations ("+
                "reservation_id INT PRIMARY KEY, student_name VARCHAR(200), student_id VARCHAR(20), course_code VARCHAR(128), " +
                "item_code INT, item_name VARCHAR(255), quantity INT, total_price DECIMAL(10,2), size VARCHAR(20), status VARCHAR(50), is_paid BOOLEAN, payment_method VARCHAR(30), reservation_time DATETIME, completed_date DATETIME NULL, reason TEXT, bundle_id VARCHAR(50) NULL, payment_deadline DATETIME NULL)");

            st.execute("CREATE TABLE IF NOT EXISTS receipts ("+
                "receipt_no BIGINT PRIMARY KEY, ts DATETIME, status VARCHAR(50), quantity INT, price DECIMAL(10,2), item_code INT, item_name VARCHAR(255), size VARCHAR(20), customer_name VARCHAR(200))");

            st.execute("CREATE TABLE IF NOT EXISTS stock_logs ("+
                "ts DATETIME, actor VARCHAR(200), item_code INT, item_name VARCHAR(255), size VARCHAR(20), delta VARCHAR(20), action_type VARCHAR(50), details VARCHAR(400), INDEX idx_ts (ts))");

            st.execute("CREATE TABLE IF NOT EXISTS system_config (cfg_key VARCHAR(100) PRIMARY KEY, cfg_value VARCHAR(500))");
        }
    }

    private static void migrateStudents(Connection con) throws Exception {
        List<Student> list = FileStorage.loadStudents();
        StudentDAO.createTableIfNotExists();
        try (Statement alter = con.createStatement()) {
            alter.execute("ALTER TABLE students MODIFY course_code VARCHAR(128)");
            alter.execute("ALTER TABLE items MODIFY course_code VARCHAR(128)");
            alter.execute("ALTER TABLE reservations MODIFY course_code VARCHAR(128)");
        } catch (SQLException ignored) {}
        String upsert = "INSERT INTO students(student_id,password,course_code,first_name,last_name,gender,is_active) VALUES (?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE password=VALUES(password), course_code=VALUES(course_code), first_name=VALUES(first_name), last_name=VALUES(last_name), gender=VALUES(gender), is_active=VALUES(is_active)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (Student s : list) {
                String code = GUIValidator.normalizeCourse(s.getCourse());
                if (code == null && s.getCourse() != null && s.getCourse().length() > 128) {
                    System.err.println("WARNING: course value too long, truncating: " + s.getCourse());
                }
                ps.setString(1, s.getStudentId());
                ps.setString(2, s.getPassword());
                ps.setString(3, code == null ? truncate(s.getCourse(),128) : code);
                ps.setString(4, s.getFirstName());
                ps.setString(5, s.getLastName());
                ps.setString(6, s.getGender());
                ps.setBoolean(7, s.isActive());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Students migrated: " + list.size());
    }

    private static void migrateStaff(Connection con) throws Exception {
        List<Staff> list = FileStorage.loadStaff();
        String upsert = "INSERT INTO staff(staff_id,password,first_name,last_name,role,is_active) VALUES (?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE password=VALUES(password), first_name=VALUES(first_name), last_name=VALUES(last_name), role=VALUES(role), is_active=VALUES(is_active)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (Staff s : list) {
                ps.setString(1, s.getStaffId());
                ps.setString(2, s.getPassword());
                ps.setString(3, s.getFirstName());
                ps.setString(4, s.getLastName());
                ps.setString(5, s.getRole());
                ps.setBoolean(6, s.isActive());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Staff migrated: " + list.size());
    }

    private static void migrateItems(Connection con) throws Exception {
        List<Item> list = FileStorage.loadItems();
        String upsert = "INSERT INTO items(item_code,item_name,course_code,size,quantity,price) VALUES (?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), quantity=VALUES(quantity), price=VALUES(price)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (Item i : list) {
                ps.setInt(1, i.getCode());
                ps.setString(2, i.getName());
                // Normalize course code if long name accidentally present
                String courseCode = gui.utils.GUIValidator.normalizeCourse(i.getCourse());
                ps.setString(3, courseCode == null ? truncate(i.getCourse(),128) : courseCode);
                ps.setString(4, i.getSize());
                ps.setInt(5, i.getQuantity());
                ps.setDouble(6, i.getPrice());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Items migrated: " + list.size());
    }

    private static void migrateReservations(Connection con) throws Exception {
        List<Reservation> list = FileStorage.loadReservations();
        // DateTimeFormatter retained (if future transformations require formatting)
        String upsert = "INSERT INTO reservations(reservation_id,student_name,student_id,course_code,item_code,item_name,quantity,total_price,size,status,is_paid,payment_method,reservation_time,completed_date,reason,bundle_id,payment_deadline) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE student_name=VALUES(student_name), status=VALUES(status), is_paid=VALUES(is_paid), completed_date=VALUES(completed_date), reason=VALUES(reason), bundle_id=VALUES(bundle_id), payment_deadline=VALUES(payment_deadline)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (Reservation r : list) {
                ps.setInt(1, r.getReservationId());
                ps.setString(2, r.getStudentName());
                ps.setString(3, r.getStudentId());
                String courseCode = gui.utils.GUIValidator.normalizeCourse(r.getCourse());
                ps.setString(4, courseCode == null ? truncate(r.getCourse(),128) : courseCode);
                ps.setInt(5, r.getItemCode());
                ps.setString(6, r.getItemName());
                ps.setInt(7, r.getQuantity());
                ps.setDouble(8, r.getTotalPrice());
                ps.setString(9, r.getSize());
                ps.setString(10, r.getStatus());
                ps.setBoolean(11, r.isPaid());
                ps.setString(12, r.getPaymentMethod());
                ps.setTimestamp(13, Timestamp.valueOf(r.getReservationTime()));
                ps.setTimestamp(14, r.getCompletedDate() == null ? null : Timestamp.valueOf(r.getCompletedDate()));
                ps.setString(15, r.getReason());
                ps.setString(16, r.getBundleId());
                ps.setTimestamp(17, r.getPaymentDeadline() == null ? null : Timestamp.valueOf(r.getPaymentDeadline()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Reservations migrated: " + list.size());
    }

    private static void migrateReceipts(Connection con) throws Exception {
        File file = new File("src/database/data/receipts.txt");
        if (!file.exists()) return;
        List<String> lines = readAll(file);
        String upsert = "INSERT INTO receipts(receipt_no,ts,status,quantity,price,item_code,item_name,size,customer_name) VALUES (?,?,?,?,?,?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE status=VALUES(status)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length < 9) continue;
                ps.setLong(1, Long.parseLong(p[0]));
                ps.setTimestamp(2, Timestamp.valueOf(p[1]));
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setDouble(5, Double.parseDouble(p[4]));
                ps.setInt(6, Integer.parseInt(p[5]));
                ps.setString(7, p[6]);
                ps.setString(8, p[7]);
                ps.setString(9, p[8]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Receipts migrated: " + lines.size());
    }

    private static void migrateStockLogs(Connection con) throws Exception {
        File file = new File("src/database/data/stock_logs.txt");
        if (!file.exists()) return;
        List<String> lines = readAll(file);
        String insert = "INSERT INTO stock_logs(ts,actor,item_code,item_name,size,delta,action_type,details) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(insert)) {
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length < 8) continue;
                ps.setTimestamp(1, Timestamp.valueOf(p[0]));
                ps.setString(2, p[1]);
                ps.setInt(3, Integer.parseInt(p[2]));
                ps.setString(4, p[3]);
                ps.setString(5, p[4]);
                ps.setString(6, p[5]);
                ps.setString(7, p[6]);
                ps.setString(8, p[7]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("Stock logs migrated: " + lines.size());
    }

    private static void migrateSystemConfig(Connection con) throws Exception {
        File file = new File("src/database/data/system_config.txt");
        if (!file.exists()) return;
        List<String> lines = readAll(file);
        String upsert = "INSERT INTO system_config(cfg_key,cfg_value) VALUES (?,?) ON DUPLICATE KEY UPDATE cfg_value=VALUES(cfg_value)";
        try (PreparedStatement ps = con.prepareStatement(upsert)) {
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String k = line.substring(0, eq);
                String v = line.substring(eq + 1);
                ps.setString(1, k);
                ps.setString(2, v);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        System.out.println("System config migrated.");
    }

    private static List<String> readAll(File f) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                list.add(line);
            }
        }
        return list;
    }

    // Utility to safely truncate overly long strings for columns
    private static String truncate(String v, int max) {
        if (v == null) return null;
        return v.length() <= max ? v : v.substring(0, max);
    }
}
