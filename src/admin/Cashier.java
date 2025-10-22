package admin;

import user.User;

public class Cashier extends User {
    private static final String DEFAULT_CASHIER_USERNAME = "cashier";
    private static final String DEFAULT_CASHIER_PASSWORD = "cashier123";
    
    public Cashier(String username, String password) {
        super(username, password);
    }
    
    @Override
    public boolean authenticate() {
        return username.equals(DEFAULT_CASHIER_USERNAME) && password.equals(DEFAULT_CASHIER_PASSWORD);
    }
}