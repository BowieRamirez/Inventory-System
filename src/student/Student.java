package student;
import user.User;

public class Student extends User {
    private String studentId;
    private String course;
    private String firstName;
    private String lastName;
    private String gender;
    private boolean isActive;  // Track if account is active or deactivated

    // REWRITTEN Constructor - NEW ORDER: studentId, password, course, firstName, lastName, gender
    public Student(String studentId, String password, String course, String firstName, String lastName, String gender) {
        super(studentId, password);  // username = studentId
        this.studentId = studentId;
        this.course = course;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.isActive = true;  // Account is active by default
    }
    
    @Override
    public boolean authenticate() {
        // Check if account is active first
        if (!isActive) {
            return false;  // Deactivated accounts cannot login
        }
        
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               studentId != null && !studentId.trim().isEmpty() &&
               course != null && !course.trim().isEmpty();
    }
    
    // Getters
    public String getStudentId() { 
        return this.studentId; 
    }
    
    public String getCourse() { 
        return this.course; 
    }
    
    public String getFirstName() { 
        return this.firstName; 
    }
    
    public String getLastName() { 
        return this.lastName; 
    }
    
    public String getGender() {
        return this.gender;
    }
    
    public String getFullName() {
        return this.lastName + ", " + this.firstName;
    }
    
    public boolean isActive() {
        return this.isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public String getAccountStatus() {
        return isActive ? "Active" : "Deactivated";
    }
}
