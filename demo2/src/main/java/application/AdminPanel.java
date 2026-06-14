package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import database.DatabaseHelper;
import models.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminPanel {

    public static void showAdminDashboard(Stage owner, User currentUser) {
        if (currentUser == null || !currentUser.isAdmin()) {
            showAlert("Ошибка", "У вас нет прав администратора!");
            return;
        }

        Stage adminStage = new Stage();
        adminStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        adminStage.initOwner(owner);
        adminStage.setTitle("👑 Админ-панель");

        TabPane tabPane = new TabPane();

        // Вкладка статистики
        Tab statsTab = createStatsTab();
        statsTab.setClosable(false);
        tabPane.getTabs().add(statsTab);

        // Вкладка пользователей
        Tab usersTab = createUsersTab();
        usersTab.setClosable(false);
        tabPane.getTabs().add(usersTab);

        // Вкладка заказов
        Tab ordersTab = createOrdersTab();
        ordersTab.setClosable(false);
        tabPane.getTabs().add(ordersTab);

        // Вкладка бронирований
        Tab bookingsTab = createBookingsTab();
        bookingsTab.setClosable(false);
        tabPane.getTabs().add(bookingsTab);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().add(tabPane);

        Scene scene = new Scene(root, 900, 600);
        adminStage.setScene(scene);
        adminStage.show();
    }

    private static Tab createStatsTab() {
        Tab tab = new Tab("📊 Статистика");
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));
        content.setAlignment(Pos.CENTER);

        DatabaseHelper db = new DatabaseHelper();

        Label titleLabel = new Label("Статистика ресторана");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        double totalRevenue = db.getTotalRevenue();
        int todayOrders = db.getTodayOrdersCount();
        int todayBookings = db.getTodayBookingsCount();

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(15);
        statsGrid.setAlignment(Pos.CENTER);

        Label revenueLabel = new Label("💰 Общая выручка:");
        revenueLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label revenueValue = new Label(String.format("%.2f руб.", totalRevenue));
        revenueValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60;");

        Label ordersLabel = new Label("📦 Заказов за сегодня:");
        ordersLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label ordersValue = new Label(String.valueOf(todayOrders));
        ordersValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db;");

        Label bookingsLabel = new Label("📅 Бронирований за сегодня:");
        bookingsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label bookingsValue = new Label(String.valueOf(todayBookings));
        bookingsValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #e67e22;");

        statsGrid.add(revenueLabel, 0, 0);
        statsGrid.add(revenueValue, 1, 0);
        statsGrid.add(ordersLabel, 0, 1);
        statsGrid.add(ordersValue, 1, 1);
        statsGrid.add(bookingsLabel, 0, 2);
        statsGrid.add(bookingsValue, 1, 2);

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            revenueValue.setText(String.format("%.2f руб.", db.getTotalRevenue()));
            ordersValue.setText(String.valueOf(db.getTodayOrdersCount()));
            bookingsValue.setText(String.valueOf(db.getTodayBookingsCount()));
        });

        content.getChildren().addAll(titleLabel, statsGrid, refreshBtn);
        tab.setContent(content);
        return tab;
    }

    private static Tab createUsersTab() {
        Tab tab = new Tab("👥 Пользователи");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ListView<String> userList = new ListView<>();
        DatabaseHelper db = new DatabaseHelper();

        refreshUserList(userList, db);

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshUserList(userList, db));

        content.getChildren().addAll(new Label("Список пользователей:"), userList, refreshBtn);
        tab.setContent(content);
        return tab;
    }

    private static void refreshUserList(ListView<String> userList, DatabaseHelper db) {
        userList.getItems().clear();
        try (ResultSet rs = db.getAllUsers()) {
            if (rs != null) {
                while (rs.next()) {
                    String userInfo = String.format("ID: %d | %s | %s | Роль: %s",
                            rs.getInt("Id"),
                            rs.getString("Username"),
                            rs.getString("FullName"),
                            rs.getString("Role")
                    );
                    userList.getItems().add(userInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Tab createOrdersTab() {
        Tab tab = new Tab("📦 Заказы");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ListView<String> ordersList = new ListView<>();
        DatabaseHelper db = new DatabaseHelper();

        refreshOrdersList(ordersList, db);

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshOrdersList(ordersList, db));

        content.getChildren().addAll(new Label("История заказов:"), ordersList, refreshBtn);
        tab.setContent(content);
        return tab;
    }

    private static void refreshOrdersList(ListView<String> ordersList, DatabaseHelper db) {
        ordersList.getItems().clear();
        try (ResultSet rs = db.getOrderStatistics()) {
            if (rs != null) {
                while (rs.next()) {
                    String orderInfo = String.format("%s | Сумма: %.2f руб. | Блюд: %d",
                            rs.getTimestamp("OrderDate"),
                            rs.getDouble("TotalAmount"),
                            rs.getInt("ItemsCount")
                    );
                    ordersList.getItems().add(orderInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Tab createBookingsTab() {
        Tab tab = new Tab("📅 Бронирования");
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        ListView<String> bookingsList = new ListView<>();
        DatabaseHelper db = new DatabaseHelper();

        refreshBookingsList(bookingsList, db);

        Button refreshBtn = new Button("🔄 Обновить");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> refreshBookingsList(bookingsList, db));

        content.getChildren().addAll(new Label("Все бронирования:"), bookingsList, refreshBtn);
        tab.setContent(content);
        return tab;
    }

    private static void refreshBookingsList(ListView<String> bookingsList, DatabaseHelper db) {
        bookingsList.getItems().clear();
        try (ResultSet rs = db.getAllBookings()) {
            if (rs != null) {
                while (rs.next()) {
                    String bookingInfo = String.format("Столик %d | %s | %s | Гостей: %d | %s | %s",
                            rs.getInt("TableNumber"),
                            rs.getString("CustomerName"),
                            rs.getString("Phone"),
                            rs.getInt("Guests"),
                            rs.getString("BookingTime"),
                            rs.getTimestamp("BookingDate")
                    );
                    bookingsList.getItems().add(bookingInfo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}