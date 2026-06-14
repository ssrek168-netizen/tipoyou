package models;

public class User {
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String address;
    private boolean isLoggedIn;
    private String role; // ADMIN, MANAGER, COOK, WAITER, USER
    private int id;

    public User(String username, String password, String fullName, String phone, String address, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.isLoggedIn = false;
    }

    // Геттеры и сеттеры
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isLoggedIn() { return isLoggedIn; }
    public String getRole() { return role; }
    public int getId() { return id; }

    public void setLoggedIn(boolean loggedIn) { isLoggedIn = loggedIn; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setId(int id) { this.id = id; }

    // Проверки ролей
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isManager() { return "MANAGER".equals(role); }
    public boolean isCook() { return "COOK".equals(role); }
    public boolean isWaiter() { return "WAITER".equals(role); }
    public boolean isUser() { return "USER".equals(role); }
}