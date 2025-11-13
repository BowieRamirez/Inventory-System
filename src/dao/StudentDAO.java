package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import student.Student;
import utils.DBManager;

public class StudentDAO {

    public static void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students ("+
                     "student_id VARCHAR(20) PRIMARY KEY, " +
                     "password VARCHAR(255) NOT NULL, " +
                     "course_code VARCHAR(128) NOT NULL, " +
                     "first_name VARCHAR(100) NOT NULL, " +
                     "last_name VARCHAR(100) NOT NULL, " +
                     "gender VARCHAR(10) NOT NULL, " +
                     "is_active BOOLEAN NOT NULL DEFAULT TRUE" +
                     ")";
        try (Connection con = DBManager.getConnection(); Statement st = con.createStatement()) {
            st.execute(sql);
            try (Statement alter = con.createStatement()) {
                alter.execute("ALTER TABLE students MODIFY course_code VARCHAR(128)");
            } catch (SQLException ignored) {}
        }
    }

    public static boolean exists(String studentId) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE student_id=?";
        try (Connection con = DBManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void insert(Student s) throws SQLException {
        String sql = "INSERT INTO students(student_id,password,course_code,first_name,last_name,gender,is_active) VALUES (?,?,?,?,?,?,?)";
        try (Connection con = DBManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ps.setString(2, s.getPassword());
            ps.setString(3, s.getCourse());
            ps.setString(4, s.getFirstName());
            ps.setString(5, s.getLastName());
            ps.setString(6, s.getGender());
            ps.setBoolean(7, s.isActive());
            ps.executeUpdate();
        }
    }

    public static List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT student_id,password,course_code,first_name,last_name,gender,is_active FROM students";
        try (Connection con = DBManager.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = new Student(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6)
                );
                s.setActive(rs.getBoolean(7));
                list.add(s);
            }
        }
        return list;
    }
}
