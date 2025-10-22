package student;
import user.User;

public class Student extends User {
    private String studentId;
    private String course;
    private String firstName;
    private String lastName;
    private String gender;

    // New constructors: username removed. studentId is used as the internal username for User.
    public Student(String studentId, String password, String course, String firstName, String lastName) {
        this(studentId, password, course, firstName, lastName, "");
    }

    public Student(String studentId, String password, String course, String firstName, String lastName, String gender) {
        super(studentId, password); // store studentId in User.username for compatibility
        this.studentId = studentId;
        this.course = course;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
    }
    
    @Override
    public boolean authenticate() {
     // Authentication for students is based on studentId and password. Username may be empty.
     return password != null && !password.trim().isEmpty() &&
         studentId != null && !studentId.trim().isEmpty();
    }
    
    public String getStudentId() { return studentId; }
    public String getCourse() { return course; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender; }
    
    public String getFullName() {
        return lastName + ", " + firstName;
    }
}