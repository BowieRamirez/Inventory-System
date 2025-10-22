package student;
import user.User;

public class Student extends User {
    private String studentId;
    private String course;
    private String firstName;
    private String lastName;
    private String gender;

    // REWRITTEN Constructor - NEW ORDER: studentId, password, course, firstName, lastName, gender
    public Student(String studentId, String password, String course, String firstName, String lastName, String gender) {
        super(studentId, password);  // username = studentId
        this.studentId = studentId;
        this.course = course;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
    }
    
    @Override
    public boolean authenticate() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               studentId != null && !studentId.trim().isEmpty() &&
               course != null && !course.trim().isEmpty();
    }
    
    // Getters with DEBUG
    public String getStudentId() { 
        System.out.println("[DEBUG] getStudentId() returning: " + this.studentId);
        return this.studentId; 
    }
    
    public String getCourse() { 
        System.out.println("[DEBUG] getCourse() returning: " + this.course);
        return this.course; 
    }
    
    public String getFirstName() { 
        System.out.println("[DEBUG] getFirstName() returning: " + this.firstName);
        return this.firstName; 
    }
    
    public String getLastName() { 
        System.out.println("[DEBUG] getLastName() returning: " + this.lastName);
        return this.lastName; 
    }
    
    public String getGender() {
        System.out.println("[DEBUG] getGender() returning: " + this.gender);
        return this.gender;
    }
    
    public String getFullName() {
        System.out.println("[DEBUG] getFullName() - lastName=" + this.lastName + ", firstName=" + this.firstName);
        return this.lastName + ", " + this.firstName;
    }
}
