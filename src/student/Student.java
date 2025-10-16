package student;
import user.User;

public class Student extends User {
    private String studentId;
    private String course;
    private String firstName;
    private String lastName;

    public Student(String username, String password, String studentId, String course, String firstName, String lastName) {
        super(username, password);
        this.studentId = studentId;
        this.course = course;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    
    @Override
    public boolean authenticate() {
        return username != null && !username.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               studentId != null && !studentId.trim().isEmpty() &&
               course != null && !course.trim().isEmpty();
    }
    
    public String getStudentId() { return studentId; }
    public String getCourse() { return course; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    
    public String getFullName() {
        return lastName + ", " + firstName;
    }
}