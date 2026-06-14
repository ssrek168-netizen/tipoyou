package database;

import models.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserManager {
    private static UserManager instance;
    private User currentUser;
    private DatabaseHelper db;
    private int currentUserId = -1;  // Переносим поле в начало

    private UserManager() {
        db = new DatabaseHelper();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    public boolean register(String username, String password, String fullName, String phone, String address) {
        try {
            db.saveUser(username, password, fullName, phone, address);
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (db.checkUser(username, password)) {
            try (ResultSet rs = db.getUser(username)) {
                if (rs != null && rs.next()) {
                    currentUser = new User(
                            rs.getString("Username"),
                            rs.getString("Password"),
                            rs.getString("FullName"),
                            rs.getString("Phone"),
                            rs.getString("Address"),
                            rs.getString("Role")  // Добавляем роль
                    );
                    currentUser.setLoggedIn(true);
                    currentUserId = rs.getInt("Id");
                    currentUser.setId(currentUserId);
                    System.out.println("✅ Пользователь вошёл: " + username + ", ID: " + currentUserId + ", Роль: " + currentUser.getRole());
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
            currentUser = null;
            currentUserId = -1;  // Сбрасываем ID при выходе
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public int getCurrentUserId() {
        return currentUserId;  // Просто возвращаем сохранённый ID
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentUser.isLoggedIn();
    }

    public void updateUserInfo(String phone, String address) {
        if (currentUser != null) {
            currentUser.setPhone(phone);
            currentUser.setAddress(address);
        }
    }
}