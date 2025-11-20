package tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import utils.DBManager;

public class QueryStudent {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java tools.QueryStudent <studentId>");
            System.exit(1);
        }
        String id = args[0];
        try {
            if (!DBManager.isConfigured()) {
                System.err.println("DB not configured. Check src/database/data/db.properties or environment variables.");
                System.exit(2);
            }
            try (Connection con = DBManager.getConnection()) {
                String sql = "SELECT student_id, first_name, last_name, course_code, is_active FROM students WHERE student_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("Found student:");
                            System.out.println("  ID: " + rs.getString("student_id"));
                            System.out.println("  Name: " + rs.getString("first_name") + " " + rs.getString("last_name"));
                            System.out.println("  Course: " + rs.getString("course_code"));
                            System.out.println("  Active: " + rs.getBoolean("is_active"));
                        } else {
                            System.out.println("No student found with ID: " + id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error querying student: " + e.getMessage());
            e.printStackTrace();
            System.exit(3);
        }
    }
}
