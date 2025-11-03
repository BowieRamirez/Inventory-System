package admin;

import user.User;

public class Staff extends User {
    private String staffId;
    private String firstName;
    private String lastName;
    private String role;  // "Staff" or "Cashier"
    private boolean isActive;  // Track if account is active or deactivated
    
    /**
     * Constructor for creating Staff/Cashier accounts
     * 
     * @param staffId The staff's unique ID (also used as username)
     * @param password The password
     * @param firstName The first name
     * @param lastName The last name
     * @param role The role: "Staff" or "Cashier"
     */
    public Staff(String staffId, String password, String firstName, String lastName, String role) {
        super(staffId, password);  // username = staffId
        this.staffId = staffId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
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
               staffId != null && !staffId.trim().isEmpty();
    }
    
    // Getters
    public String getStaffId() {
        return this.staffId;
    }
    
    public String getFirstName() {
        return this.firstName;
    }
    
    public String getLastName() {
        return this.lastName;
    }
    
    public String getFullName() {
        return this.lastName + ", " + this.firstName;
    }
    
    public String getRole() {
        return this.role;
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
    
    // Setters for editing
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}