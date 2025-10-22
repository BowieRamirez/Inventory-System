package admin;

import user.User;

public class Admin extends User {
    private static final String DEFAULT_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    
    public Admin(String username, String password) {
        super(username, password);
    }
    
    @Override
    public boolean authenticate() {
        return username.equals(DEFAULT_ADMIN_USERNAME) && password.equals(DEFAULT_ADMIN_PASSWORD);
    }
}