package models;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private boolean isLoggedIn;

    public User(String username, String password, String fullName, String phone, String address) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.isLoggedIn = false;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isLoggedIn() { return isLoggedIn; }

    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
}