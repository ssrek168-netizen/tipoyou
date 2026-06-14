package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseHelper {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=RestaurantDB;encrypt=true;trustServerCertificate=true;loginTimeout=5";
    private static final String USER = "restaurant_user";
    private static final String PASSWORD = "123456";

    private Connection connection;
    private boolean isConnected = false;

    public DatabaseHelper() {
        connect();
    }

    private void connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DriverManager.setLoginTimeout(5);
            connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            isConnected = true;
            System.out.println("✅ ПОДКЛЮЧЕНИЕ К БД УСПЕШНО!");
        } catch (Exception e) {
            System.out.println("❌ Ошибка подключения к БД: " + e.getMessage());
            System.out.println("⚠️ Приложение работает в офлайн-режиме");
            connection = null;
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    // Получить все столики из БД
    public ResultSet getAllTables() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Tables");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Обновить статус столика
    public void updateTableStatus(int tableNumber, boolean occupied) {
        if (!isConnected || connection == null) return;
        String sql = "UPDATE Tables SET IsOccupied = ? WHERE Number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, occupied ? 1 : 0);
            pstmt.setInt(2, tableNumber);
            pstmt.executeUpdate();
            System.out.println("✅ Статус столика " + tableNumber + " обновлён: " + (occupied ? "Занят" : "Свободен"));
        } catch (SQLException e) {
            System.out.println("❌ Ошибка обновления столика: " + e.getMessage());
        }
    }

    // Получить меню
    public ResultSet getAllMenu() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Menu");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Сохранить отзыв
    public void saveReview(String customerName, int rating, String comment) {
        if (!isConnected || connection == null) {
            System.out.println("⚠️ Отзыв не сохранён (БД недоступна)");
            return;
        }
        String sql = "INSERT INTO Reviews (CustomerName, Rating, Comment, ReviewDate) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerName);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("✅ Отзыв сохранён в БД");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения отзыва: " + e.getMessage());
        }
    }

    // Получить все отзывы
    public ResultSet getAllReviews() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Reviews ORDER BY ReviewDate DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Сохранить бронирование
    public void saveBooking(int tableNumber, String customerName, String phone, int guests, String bookingTime, int userId) {
        if (!isConnected || connection == null) {
            System.out.println("⚠️ Бронирование не сохранено (БД недоступна)");
            return;
        }
        String sql = "INSERT INTO Bookings (TableNumber, CustomerName, Phone, Guests, BookingTime, BookingDate, UserId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            pstmt.setString(2, customerName);
            pstmt.setString(3, phone);
            pstmt.setInt(4, guests);
            pstmt.setString(5, bookingTime);
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(7, userId);
            pstmt.executeUpdate();
            updateTableStatus(tableNumber, true);
            System.out.println("✅ Бронирование сохранено в БД: столик " + tableNumber + ", пользователь ID: " + userId);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения брони: " + e.getMessage());
        }
    }

    // Получить ID пользователя, который забронировал столик
    public int getBookingUserId(int tableNumber) {
        if (!isConnected || connection == null) return -1;
        String sql = "SELECT TOP 1 UserId FROM Bookings WHERE TableNumber = ? ORDER BY BookingDate DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("UserId");
                System.out.println("📋 Столик " + tableNumber + " забронирован пользователем ID: " + userId);
                return userId;
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка получения брони: " + e.getMessage());
        }
        return -1;
    }

    // Проверка, может ли пользователь освободить столик
    public boolean canUserFreeTable(int tableNumber, int userId, boolean isAdmin) {
        if (isAdmin) {
            System.out.println("👑 Администратор может освободить столик " + tableNumber);
            return true;
        }
        if (userId == -1) {
            System.out.println("⚠️ Неавторизованный пользователь не может освободить столик " + tableNumber);
            return false;
        }
        int bookingUserId = getBookingUserId(tableNumber);
        boolean canFree = bookingUserId == userId;
        System.out.println("🔍 Проверка столика " + tableNumber + ": userId=" + userId + ", bookingUserId=" + bookingUserId + ", может освободить=" + canFree);
        return canFree;
    }

    // Сохранить пользователя
    public void saveUser(String username, String password, String fullName, String phone, String address) {
        if (!isConnected || connection == null) {
            System.out.println("⚠️ Пользователь не сохранён (БД недоступна)");
            return;
        }
        String sql = "INSERT INTO Users (Username, Password, FullName, Phone, Address) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);
            pstmt.executeUpdate();
            System.out.println("✅ Пользователь сохранён: " + username);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения пользователя: " + e.getMessage());
        }
    }

    // Проверить пользователя
    public boolean checkUser(String username, String password) {
        if (!isConnected || connection == null) {
            return username.equals("user") && password.equals("123");
        }
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // Получить пользователя
    public ResultSet getUser(String username) {
        if (!isConnected || connection == null) return null;
        try {
            PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM Users WHERE Username = ?");
            pstmt.setString(1, username);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    // Сбросить все столики в свободное состояние (для отладки)
    public void resetAllTables() {
        if (!isConnected || connection == null) return;
        try {
            connection.createStatement().executeUpdate("UPDATE Tables SET IsOccupied = 0");
            System.out.println("✅ Все столики сброшены в свободное состояние");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сброса столиков: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            // Игнорируем
        }
    }
    public int getUserIdByUsername(String username) {
        if (connection == null) return -1;
        String sql = "SELECT Id FROM Users WHERE Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("Id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}