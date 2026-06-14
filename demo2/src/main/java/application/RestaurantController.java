package application;

import models.Dish;
import models.Table;
import models.Review;
import database.DatabaseHelper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RestaurantController {
    private ArrayList<Table> tables;
    private ArrayList<Dish> menu;
    private ArrayList<Review> reviews;
    private DatabaseHelper db;

    public RestaurantController() {
        db = new DatabaseHelper();
        tables = new ArrayList<>();
        menu = new ArrayList<>();
        reviews = new ArrayList<>();
        loadTables();
        loadMenu();
        loadReviews();
    }

    public void loadTables() {
        tables.clear();
        try (ResultSet rs = db.getAllTables()) {
            if (rs != null) {
                while (rs.next()) {
                    Table table = new Table(rs.getInt("Number"), rs.getInt("Seats"));
                    table.setOccupied(rs.getInt("IsOccupied") == 1);
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTablesFromDB() {
        loadTables();
    }

    private void loadMenu() {
        menu.clear();
        try (ResultSet rs = db.getAllMenu()) {
            if (rs != null) {
                while (rs.next()) {
                    menu.add(new Dish(rs.getString("Name"), rs.getString("Category"), rs.getDouble("Price")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadReviews() {
        reviews.clear();
        try (ResultSet rs = db.getAllReviews()) {
            if (rs != null) {
                while (rs.next()) {
                    reviews.add(new Review(rs.getString("CustomerName"), rs.getInt("Rating"), rs.getString("Comment")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTableStatus(int tableNumber, boolean occupied) {
        db.updateTableStatus(tableNumber, occupied);
        for (Table table : tables) {
            if (table.getNumber() == tableNumber) {
                table.setOccupied(occupied);
                break;
            }
        }
    }

    public void saveBookingToDB(int tableNumber, String customerName, String phone, int guests, String bookingTime, int userId) {
        db.saveBooking(tableNumber, customerName, phone, guests, bookingTime, userId);
        updateTableStatus(tableNumber, true);
    }

    public boolean canUserFreeTable(int tableNumber, int userId, boolean isAdmin) {
        return db.canUserFreeTable(tableNumber, userId, isAdmin);
    }

    public void addReview(String name, int rating, String comment) {
        db.saveReview(name, rating, comment);
        reviews.add(new Review(name, rating, comment));
    }

    public ArrayList<Table> getTables() {
        loadTables();
        return tables;
    }

    public ArrayList<Dish> getMenu() {
        return menu;
    }

    public ArrayList<Review> getReviews() {
        loadReviews();
        return reviews;
    }

    public void resetAllTables() {
        db.resetAllTables();
        loadTables();
    }

    // ========== СТАТИСТИКА ==========
    public ResultSet getOrderStatistics() {
        return db.getOrderStatistics();
    }

    public double getTotalRevenue() {
        return db.getTotalRevenue();
    }

    public int getTodayOrdersCount() {
        return db.getTodayOrdersCount();
    }

    public int getTodayBookingsCount() {
        return db.getTodayBookingsCount();
    }

    public void saveOrderStatistics(double totalAmount, int itemsCount, String itemsDetails) {
        db.saveOrderStatistics(totalAmount, itemsCount, itemsDetails);
    }
}