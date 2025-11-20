package tools;

import java.util.List;

import dao.StudentDAO;
import student.Student;
import utils.FileStorage;

/**
 * Exports all students from the configured database into the students.txt file(s).
 *
 * Usage: run via Maven exec plugin or from IDE.
 */
public class ExportStudentsFromDB {
    public static void main(String[] args) {
        try {
            System.out.println("[Export] Loading students from database...");
            List<Student> students = StudentDAO.findAll();
            System.out.println("[Export] Retrieved " + students.size() + " students from DB.");

            boolean ok = FileStorage.saveStudents(students);
            if (ok) {
                System.out.println("[Export] Successfully wrote students to students.txt (both src and runtime paths).");
            } else {
                System.err.println("[Export] Failed to write students to students.txt");
            }
        } catch (Exception e) {
            System.err.println("[Export] Error exporting students: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
