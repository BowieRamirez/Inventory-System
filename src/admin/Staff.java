package admin;

import user.User;

public class Staff extends User {
    private static final String DEFAULT_STAFF_USERNAME = "staff";
    private static final String DEFAULT_STAFF_PASSWORD = "staff123";
    
    public Staff(String username, String password) {
        super(username, password);
    }
    
    @Override
    public boolean authenticate() {
        return username.equals(DEFAULT_STAFF_USERNAME) && password.equals(DEFAULT_STAFF_PASSWORD);
    }
}