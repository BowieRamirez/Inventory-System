package utils;

import student.Student;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class FileStorage {

    private static final Path USERS_DIR = Path.of("src", "database", "data");
    private static final File USERS_FILE = USERS_DIR.resolve("users.txt").toFile();
    public static boolean saveStudent(Student s) {
        try {
            if (!Files.exists(USERS_DIR)) {
                Files.createDirectories(USERS_DIR);
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
                // simple CSV escaping: replace any comma in fields with semicolon
                String password = safe(s.getPassword());
                String studentId = safe(s.getStudentId());
                String course = safe(s.getCourse());
                String first = safe(s.getFirstName());
                String last = safe(s.getLastName());
                String gender = safe(s.getGender());
                // Persist without username (use student ID as primary key for login)
                String line = String.format("Student ID: %s, Password: %s, Course: %s, Gender: %s, Name: %s %s",
                            studentId, password, course, gender, first, last);
                bw.write(line);
                bw.newLine();
                bw.flush();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save student to file: " + e.getMessage());
            return false;
        }
    }

    private static String safe(String in) {
        if (in == null) return "";
        return in.replace(",", ";");
    }
}
