public class Admin extends User {
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    
    public Admin(String username, String password) {
        super(username, password);
    }
    
    @Override
    public boolean authenticate() {
        // Simple authentication - in real system, this would check against a database
        return DEFAULT_ADMIN_USERNAME.equals(username) && DEFAULT_ADMIN_PASSWORD.equals(password);
    }
    
    public boolean hasAdminPrivileges() {
        return authenticate();
    }
}