public class Student extends User {
    private String studentId;
    private String course;
    
    public Student(String username, String password, String studentId, String course) {
        super(username, password);
        this.studentId = studentId;
        this.course = course;
    }
    
    @Override
    public boolean authenticate() {
        // Simple authentication - check if required fields are not empty
        // In a real system, this would validate against a student database
        return username != null && !username.trim().isEmpty() && 
               password != null && !password.trim().isEmpty() &&
               studentId != null && !studentId.trim().isEmpty() &&
               course != null && !course.trim().isEmpty();
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getCourse() {
        return course;
    }
    
    public void setCourse(String course) {
        this.course = course;
    }
}