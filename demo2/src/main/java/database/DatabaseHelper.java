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
            connection = null;
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    // ========== РАБОТА СО СТОЛИКАМИ ==========
    public ResultSet getAllTables() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Tables");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updateTableStatus(int tableNumber, boolean occupied) {
        if (!isConnected || connection == null) return;
        String sql = "UPDATE Tables SET IsOccupied = ? WHERE Number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, occupied ? 1 : 0);
            pstmt.setInt(2, tableNumber);
            pstmt.executeUpdate();
            System.out.println("✅ Статус столика " + tableNumber + " обновлён");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка обновления столика: " + e.getMessage());
        }
    }

    public void resetAllTables() {
        if (!isConnected || connection == null) return;
        try {
            connection.createStatement().executeUpdate("UPDATE Tables SET IsOccupied = 0");
            System.out.println("✅ Все столики сброшены");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сброса: " + e.getMessage());
        }
    }

    // ========== РАБОТА С МЕНЮ ==========
    public ResultSet getAllMenu() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Menu");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========== РАБОТА С ОТЗЫВАМИ ==========
    public void saveReview(String customerName, int rating, String comment) {
        if (!isConnected || connection == null) {
            System.out.println("⚠️ Отзыв не сохранён");
            return;
        }
        String sql = "INSERT INTO Reviews (CustomerName, Rating, Comment, ReviewDate) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, customerName);
            pstmt.setInt(2, rating);
            pstmt.setString(3, comment);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
            System.out.println("✅ Отзыв сохранён");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения отзыва: " + e.getMessage());
        }
    }

    public ResultSet getAllReviews() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Reviews ORDER BY ReviewDate DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ========== РАБОТА С БРОНИРОВАНИЯМИ ==========
    public void saveBooking(int tableNumber, String customerName, String phone, int guests, String bookingTime, int userId) {
        if (!isConnected || connection == null) {
            System.out.println("⚠️ Бронирование не сохранено");
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
            System.out.println("✅ Бронирование сохранено, столик " + tableNumber);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка сохранения брони: " + e.getMessage());
        }
    }

    public ResultSet getAllBookings() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM Bookings ORDER BY BookingDate DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getBookingUserId(int tableNumber) {
        if (!isConnected || connection == null) return -1;
        String sql = "SELECT TOP 1 UserId FROM Bookings WHERE TableNumber = ? ORDER BY BookingDate DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("UserId");
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
        return -1;
    }

    public boolean canUserFreeTable(int tableNumber, int userId, boolean isAdmin) {
        if (isAdmin) return true;
        if (userId == -1) return false;
        int bookingUserId = getBookingUserId(tableNumber);
        return bookingUserId == userId;
    }

    // ========== РАБОТА С ПОЛЬЗОВАТЕЛЯМИ ==========
    public void saveUser(String username, String password, String fullName, String phone, String address) {
        if (!isConnected || connection == null) return;
        String sql = "INSERT INTO Users (Username, Password, FullName, Phone, Address, Role) VALUES (?, ?, ?, ?, ?, 'USER')";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, fullName);
            pstmt.setString(4, phone);
            pstmt.setString(5, address);
            pstmt.executeUpdate();
            System.out.println("✅ Пользователь сохранён: " + username);
        } catch (SQLException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

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

    public ResultSet getAllUsers() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT Id, Username, FullName, Phone, Address, Role FROM Users");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getUserIdByUsername(String username) {
        if (!isConnected || connection == null) return -1;
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

    // ========== СТАТИСТИКА ==========
    public ResultSet getOrderStatistics() {
        if (!isConnected || connection == null) return null;
        try {
            return connection.createStatement().executeQuery("SELECT * FROM OrderStatistics ORDER BY OrderDate DESC");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getTotalRevenue() {
        if (!isConnected || connection == null) return 0;
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) as Total FROM OrderStatistics";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getDouble("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTodayOrdersCount() {
        if (!isConnected || connection == null) return 0;
        String sql = "SELECT ISNULL(COUNT(*), 0) as Count FROM OrderStatistics WHERE CAST(OrderDate AS DATE) = CAST(GETDATE() AS DATE)";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("Count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTodayBookingsCount() {
        if (!isConnected || connection == null) return 0;
        String sql = "SELECT ISNULL(COUNT(*), 0) as Count FROM Bookings WHERE CAST(BookingDate AS DATE) = CAST(GETDATE() AS DATE)";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt("Count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void saveOrderStatistics(double totalAmount, int itemsCount, String itemsDetails) {
        if (!isConnected || connection == null) return;
        String sql = "INSERT INTO OrderStatistics (OrderDate, TotalAmount, ItemsCount, Status, ItemsDetails) VALUES (?, ?, ?, 'NEW', ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setDouble(2, totalAmount);
            pstmt.setInt(3, itemsCount);
            pstmt.setString(4, itemsDetails);
            pstmt.executeUpdate();
            System.out.println("✅ Статистика заказа сохранена");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            // Игнорируем
        }
    }
}