package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.layout.Priority;
import models.Dish;
import models.Review;
import models.Table;
import models.User;
import database.UserManager;

import java.util.ArrayList;

public class Main extends Application {
    private RestaurantController controller;
    private TextArea displayArea;
    private Button profileBtn;
    private TabPane mainTabPane;
    private VBox deliveryPanel;
    private VBox reviewPanel;

    private VBox createHeader(Stage primaryStage) {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(10, 0, 10, 0));

        Label title = new Label("🍽️ РЕСТОРАН УЮТ");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 3, 0, 0, 1);");

        Label subtitle = new Label("Добро пожаловать! Приятного аппетита 🌟");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #ecf0f1; -fx-font-style: italic;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);

        profileBtn = new Button("👤 Войти / Зарегистрироваться");
        profileBtn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 20px; -fx-cursor: hand; -fx-border-color: white; -fx-border-radius: 20px; -fx-border-width: 1px;");

        profileBtn.setOnAction(e -> {
            if (UserManager.getInstance().isLoggedIn()) {
                showProfileDialog(primaryStage);
            } else {
                showLoginDialog(primaryStage);
            }
            updateProfileButton();
        });

        topBar.getChildren().add(profileBtn);

        header.getChildren().addAll(topBar, title, subtitle);
        return header;
    }

    private void updateProfileButton() {
        if (UserManager.getInstance().isLoggedIn()) {
            User user = UserManager.getInstance().getCurrentUser();
            profileBtn.setText("👤 " + user.getFullName() + " ▼");
        } else {
            profileBtn.setText("👤 Войти / Зарегистрироваться");
        }
    }

    // Метод для обновления данных в панели доставки
    private void updateDeliveryPanelData() {
        if (deliveryPanel == null) return;

        for (Node node : deliveryPanel.getChildren()) {
            if (node instanceof GridPane) {
                GridPane grid = (GridPane) node;
                for (Node child : grid.getChildren()) {
                    if (child instanceof TextField) {
                        TextField tf = (TextField) child;
                        if (tf.getPromptText() != null && tf.getPromptText().contains("Улица")) {
                            if (UserManager.getInstance().isLoggedIn()) {
                                User user = UserManager.getInstance().getCurrentUser();
                                if (user != null && user.getAddress() != null && !user.getAddress().isEmpty()) {
                                    tf.setText(user.getAddress());
                                    System.out.println("✅ Адрес подставлен: " + user.getAddress());
                                }
                            }
                        }
                        if (tf.getPromptText() != null && tf.getPromptText().contains("+7")) {
                            if (UserManager.getInstance().isLoggedIn()) {
                                User user = UserManager.getInstance().getCurrentUser();
                                if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                                    tf.setText(user.getPhone());
                                    System.out.println("✅ Телефон подставлен: " + user.getPhone());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Метод для обновления имени в отзывах
    private void updateReviewPanelData() {
        if (reviewPanel == null) return;

        for (Node node : reviewPanel.getChildren()) {
            if (node instanceof SplitPane) {
                SplitPane split = (SplitPane) node;
                if (split.getItems().size() > 0 && split.getItems().get(0) instanceof VBox) {
                    VBox leftPanel = (VBox) split.getItems().get(0);
                    for (Node child : leftPanel.getChildren()) {
                        if (child instanceof TextField && ((TextField) child).getPromptText() != null &&
                                ((TextField) child).getPromptText().contains("имя")) {
                            TextField nameField = (TextField) child;
                            if (UserManager.getInstance().isLoggedIn()) {
                                User user = UserManager.getInstance().getCurrentUser();
                                if (user != null && user.getFullName() != null && !user.getFullName().isEmpty()) {
                                    nameField.setText(user.getFullName());
                                    System.out.println("✅ Имя подставлено: " + user.getFullName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Метод для обновления панели столиков
    private void refreshTablesPanel(Stage owner) {
        Tab tablesTab = mainTabPane.getTabs().get(0);
        tablesTab.setContent(createTablesPanel(owner));
    }

    private void showLoginDialog(Stage owner) {
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.WINDOW_MODAL);
        loginStage.initOwner(owner);
        loginStage.setTitle("🔐 Вход в аккаунт");

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        Label titleLabel = new Label("🔐 Вход в личный кабинет");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab loginTab = new Tab("Вход");
        loginTab.setClosable(false);
        VBox loginBox = createLoginPanel(loginStage, owner);
        loginTab.setContent(loginBox);

        Tab registerTab = new Tab("Регистрация");
        registerTab.setClosable(false);
        VBox registerBox = createRegisterPanel(loginStage, tabPane);
        registerTab.setContent(registerBox);

        tabPane.getTabs().addAll(loginTab, registerTab);

        mainBox.getChildren().addAll(titleLabel, tabPane);

        Scene scene = new Scene(mainBox, 450, 550);
        loginStage.setScene(scene);
        loginStage.showAndWait();
    }

    private VBox createLoginPanel(Stage loginStage, Stage owner) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setAlignment(Pos.CENTER);

        Label userLabel = new Label("👤 Логин:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин");
        usernameField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label passLabel = new Label("🔒 Пароль:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button loginBtn = new Button("✅ Войти");
        loginBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Заполните все поля!");
                return;
            }

            if (UserManager.getInstance().login(username, password)) {
                displayMessage("✅ Добро пожаловать, " + username + "!");
                loginStage.close();
                updateProfileButton();

                // Обновляем данные в панелях доставки и отзывов
                updateDeliveryPanelData();
                updateReviewPanelData();

                // ОБНОВЛЯЕМ СПИСОК СТОЛИКОВ ИЗ БД
                controller.loadTablesFromDB();
                refreshTablesPanel(owner);

                User user = UserManager.getInstance().getCurrentUser();
                int userId = UserManager.getInstance().getCurrentUserId();
                displayMessage("✅ Вы вошли как: " + user.getFullName() + " (ID: " + userId + ")\n" +
                        "📞 Телефон: " + user.getPhone() + "\n" +
                        "📍 Адрес: " + user.getAddress());
            } else {
                errorLabel.setText("Неверный логин или пароль!");
            }
        });

        panel.getChildren().addAll(userLabel, usernameField, passLabel, passwordField, errorLabel, loginBtn);
        return panel;
    }
    private VBox createRegisterPanel(Stage loginStage, TabPane parentTabPane) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));

        Label userLabel = new Label("👤 Логин:");
        userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Придумайте логин");
        usernameField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label passLabel = new Label("🔒 Пароль:");
        passLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Придумайте пароль");
        passwordField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label confirmLabel = new Label("🔒 Подтверждение пароля:");
        confirmLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Повторите пароль");
        confirmField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label nameLabel = new Label("👤 Ваше полное имя:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Иванов Иван");
        fullNameField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label phoneLabel = new Label("📞 Номер телефона:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+7 (XXX) XXX-XX-XX");
        phoneField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label addressLabel = new Label("📍 Адрес доставки:");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Улица, дом, квартира");
        addressArea.setPrefRowCount(2);
        addressArea.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button registerBtn = new Button("📝 Зарегистрироваться");
        registerBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressArea.getText().trim();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                errorLabel.setText("Заполните обязательные поля!");
                return;
            }

            if (!password.equals(confirm)) {
                errorLabel.setText("Пароли не совпадают!");
                return;
            }

            if (UserManager.getInstance().register(username, password, fullName, phone, address)) {
                displayMessage("✅ Регистрация успешна! Теперь войдите в аккаунт.");
                usernameField.clear();
                passwordField.clear();
                confirmField.clear();
                fullNameField.clear();
                phoneField.clear();
                addressArea.clear();
                errorLabel.setText("");

                if (parentTabPane != null && parentTabPane.getTabs().size() > 0) {
                    parentTabPane.getSelectionModel().select(0);
                }
            } else {
                errorLabel.setText("Пользователь с таким логином уже существует!");
            }
        });

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\+?[0-9\\s\\-\\(\\)]*")) {
                phoneField.setText(oldVal);
            }
        });

        panel.getChildren().addAll(
                userLabel, usernameField,
                passLabel, passwordField,
                confirmLabel, confirmField,
                nameLabel, fullNameField,
                phoneLabel, phoneField,
                addressLabel, addressArea,
                errorLabel, registerBtn
        );
        return panel;
    }

    private void showProfileDialog(Stage owner) {
        Stage profileStage = new Stage();
        profileStage.initModality(Modality.WINDOW_MODAL);
        profileStage.initOwner(owner);
        profileStage.setTitle("👤 Мой профиль");

        VBox mainBox = new VBox(15);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        User user = UserManager.getInstance().getCurrentUser();

        Label titleLabel = new Label("👤 Личный кабинет");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label nameLabel = new Label("Имя: " + (user != null ? user.getFullName() : ""));
        nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        Label loginLabel = new Label("Логин: " + (user != null ? user.getUsername() : ""));
        loginLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Separator separator = new Separator();

        Label addressTitle = new Label("📍 Адрес доставки:");
        addressTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextArea addressArea = new TextArea(user != null ? user.getAddress() : "");
        addressArea.setPrefRowCount(2);
        addressArea.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label phoneTitle = new Label("📞 Телефон:");
        phoneTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField phoneField = new TextField(user != null ? user.getPhone() : "");
        phoneField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");

        Button saveBtn = new Button("💾 Сохранить изменения");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        Button logoutBtn = new Button("🚪 Выйти из аккаунта");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveBtn, logoutBtn);

        saveBtn.setOnAction(e -> {
            if (user != null) {
                UserManager.getInstance().updateUserInfo(phoneField.getText(), addressArea.getText());
                errorLabel.setText("✅ Данные сохранены!");
                errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
                updateDeliveryPanelData();
                updateReviewPanelData();
            }
        });

        logoutBtn.setOnAction(e -> {
            UserManager.getInstance().logout();
            profileStage.close();
            displayMessage("👋 Вы вышли из аккаунта");
            updateProfileButton();

            // Очищаем поля после выхода
            if (deliveryPanel != null) {
                for (Node node : deliveryPanel.getChildren()) {
                    if (node instanceof GridPane) {
                        GridPane grid = (GridPane) node;
                        for (Node child : grid.getChildren()) {
                            if (child instanceof TextField) {
                                ((TextField) child).clear();
                            }
                        }
                    }
                }
            }
            if (reviewPanel != null) {
                for (Node node : reviewPanel.getChildren()) {
                    if (node instanceof SplitPane) {
                        SplitPane split = (SplitPane) node;
                        if (split.getItems().size() > 0 && split.getItems().get(0) instanceof VBox) {
                            VBox leftPanel = (VBox) split.getItems().get(0);
                            for (Node child : leftPanel.getChildren()) {
                                if (child instanceof TextField && ((TextField) child).getPromptText() != null &&
                                        ((TextField) child).getPromptText().contains("имя")) {
                                    ((TextField) child).clear();
                                }
                            }
                        }
                    }
                }
            }

            // ОБНОВЛЯЕМ СПИСОК СТОЛИКОВ ПОСЛЕ ВЫХОДА
            controller.loadTablesFromDB();
            refreshTablesPanel(owner);
        });

        mainBox.getChildren().addAll(titleLabel, nameLabel, loginLabel, separator, addressTitle, addressArea, phoneTitle, phoneField, errorLabel, buttonBox);

        Scene scene = new Scene(mainBox, 400, 500);
        profileStage.setScene(scene);
        profileStage.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        controller = new RestaurantController();
        primaryStage.setTitle("🍽️ Ресторан Уют");

        VBox header = createHeader(primaryStage);

        mainTabPane = new TabPane();

        Tab tablesTab = new Tab("📊 Столики");
        tablesTab.setContent(createTablesPanel(primaryStage));
        tablesTab.setClosable(false);

        Tab menuTab = new Tab("📋 Меню");
        menuTab.setContent(createMenuPanel());
        menuTab.setClosable(false);

        Tab deliveryTab = new Tab("🚚 Доставка");
        deliveryPanel = createDeliveryPanel();
        deliveryTab.setContent(deliveryPanel);
        deliveryTab.setClosable(false);

        Tab reviewTab = new Tab("⭐ Отзывы");
        reviewPanel = createReviewPanel();
        reviewTab.setContent(reviewPanel);
        reviewTab.setClosable(false);

        deliveryTab.setOnSelectionChanged(e -> {
            if (deliveryTab.isSelected() && UserManager.getInstance().isLoggedIn()) {
                updateDeliveryPanelData();
            }
        });

        reviewTab.setOnSelectionChanged(e -> {
            if (reviewTab.isSelected() && UserManager.getInstance().isLoggedIn()) {
                updateReviewPanelData();
            }
        });

        mainTabPane.getTabs().addAll(tablesTab, menuTab, deliveryTab, reviewTab);

        displayArea = new TextArea();
        displayArea.setEditable(false);
        displayArea.setPrefHeight(180);
        displayArea.setStyle("-fx-control-inner-background: #2c3e50; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-family: 'Consolas';");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        try {
            String imagePath = getClass().getResource("/images/background.jpg").toExternalForm();
            javafx.scene.image.Image backgroundImage = new javafx.scene.image.Image(imagePath);
            javafx.scene.layout.BackgroundImage background = new javafx.scene.layout.BackgroundImage(
                    backgroundImage,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundRepeat.NO_REPEAT,
                    javafx.scene.layout.BackgroundPosition.CENTER,
                    new javafx.scene.layout.BackgroundSize(
                            javafx.scene.layout.BackgroundSize.AUTO,
                            javafx.scene.layout.BackgroundSize.AUTO,
                            false, false, true, true
                    )
            );
            root.setBackground(new javafx.scene.layout.Background(background));
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #2c3e50;");
        }

        root.getChildren().addAll(header, mainTabPane, displayArea);

        Scene scene = new Scene(root, 1000, 750);

        primaryStage.setScene(scene);
        primaryStage.show();

        displayWelcomeMessage();
    }

    private void showBookingDialog(Table table, Stage owner) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("📝 Бронирование столика №" + table.getNumber());

        VBox dialogVBox = new VBox(15);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setStyle("-fx-background-color: white; -fx-background-radius: 15px;");

        Label titleLabel = new Label("Бронирование столика №" + table.getNumber());
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label infoLabel = new Label("👥 Количество мест: " + table.getSeats());
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label nameLabel = new Label("👤 Ваше имя:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField nameField = new TextField();
        nameField.setPromptText("Введите ваше имя");
        nameField.setPrefWidth(250);
        nameField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label phoneLabel = new Label("📞 Номер телефона:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+7 (XXX) XXX-XX-XX");
        phoneField.setPrefWidth(250);
        phoneField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        if (UserManager.getInstance().isLoggedIn()) {
            User user = UserManager.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getFullName() != null && !user.getFullName().isEmpty()) {
                    nameField.setText(user.getFullName());
                }
                if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                    phoneField.setText(user.getPhone());
                }
            }
        }

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\+?[0-9\\s\\-\\(\\)]*")) {
                phoneField.setText(oldVal);
            }
        });

        Label guestsLabel = new Label("👥 Количество гостей:");
        guestsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ComboBox<Integer> guestsCombo = new ComboBox<>();
        for (int i = 1; i <= table.getSeats(); i++) {
            guestsCombo.getItems().add(i);
        }
        guestsCombo.setValue(1);
        guestsCombo.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label timeLabel = new Label("⏰ Время брони:");
        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ComboBox<String> timeCombo = new ComboBox<>();
        String[] times = {"12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00"};
        timeCombo.getItems().addAll(times);
        timeCombo.setValue("19:00");
        timeCombo.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("✅ Подтвердить бронь");
        confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        Button cancelBtn = new Button("❌ Отмена");
        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        confirmBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty()) {
                errorLabel.setText("Введите ваше имя!");
                return;
            }
            if (phone.isEmpty()) {
                errorLabel.setText("Введите номер телефона!");
                return;
            }

            String digitsOnly = phone.replaceAll("\\D", "");
            if (digitsOnly.length() < 10 || digitsOnly.length() > 11) {
                errorLabel.setText("Введите корректный номер телефона!");
                return;
            }

            int guests = guestsCombo.getValue();
            String time = timeCombo.getValue();

            int userId = UserManager.getInstance().getCurrentUserId();
            System.out.println("🔍 Текущий userId: " + userId);

            controller.saveBookingToDB(table.getNumber(), name, phone, guests, time, userId);
            table.setOccupied(true);

            displayMessage("✅ СТОЛИК ЗАБРОНИРОВАН!\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "🍽️ Столик: №" + table.getNumber() + " (" + table.getSeats() + " места)\n" +
                    "👤 Имя: " + name + "\n" +
                    "📞 Телефон: " + phone + "\n" +
                    "👥 Гостей: " + guests + "\n" +
                    "⏰ Время: " + time + "\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "Ждем вас! Приятного времяпрепровождения! 🌟");

            dialog.close();
        });

        cancelBtn.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(confirmBtn, cancelBtn);

        dialogVBox.getChildren().addAll(
                titleLabel, infoLabel,
                nameLabel, nameField,
                phoneLabel, phoneField,
                guestsLabel, guestsCombo,
                timeLabel, timeCombo,
                errorLabel, buttonBox
        );

        Scene dialogScene = new Scene(dialogVBox, 400, 550);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private VBox createTablesPanel(Stage owner) {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15px;");

        Label title = new Label("🗂️ Бронирование столиков");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane tableGrid = new GridPane();
        tableGrid.setHgap(20);
        tableGrid.setVgap(20);
        tableGrid.setAlignment(Pos.CENTER);

        int currentUserId = UserManager.getInstance().getCurrentUserId();
        boolean isAdmin = false;
        if (UserManager.getInstance().isLoggedIn()) {
            isAdmin = UserManager.getInstance().getCurrentUser().getUsername().equals("admin");
        }
        System.out.println("🔍 Текущий userId в панели столиков: " + currentUserId);

        for (Table table : controller.getTables()) {
            VBox tableCard = new VBox(5);
            tableCard.setAlignment(Pos.CENTER);
            tableCard.setPadding(new Insets(15));
            tableCard.setPrefWidth(140);
            tableCard.setPrefHeight(140);
            tableCard.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");

            Label icon = new Label(table.isOccupied() ? "🔴" : "🟢");
            icon.setStyle("-fx-font-size: 40px;");

            Label tableLabel = new Label("Столик " + table.getNumber());
            tableLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label seatsLabel = new Label("👥 " + table.getSeats() + " места");
            seatsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

            Button actionBtn = new Button();
            actionBtn.setPrefWidth(120);
            actionBtn.setPrefHeight(35);

            boolean canUserFree = false;
            String buttonText = "";
            String buttonStyle = "";

            if (!table.isOccupied()) {
                buttonText = "Забронировать";
                // Если пользователь не авторизован - кнопка серая и неактивная
                if (!UserManager.getInstance().isLoggedIn()) {
                    buttonStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px;";
                    canUserFree = false;
                } else {
                    buttonStyle = "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-cursor: hand;";
                    canUserFree = true;
                }
            }
            else {
                boolean canFree = controller.canUserFreeTable(table.getNumber(), currentUserId, isAdmin);

                if (canFree) {
                    buttonText = "Освободить";
                    buttonStyle = "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-cursor: hand;";
                    canUserFree = true;
                } else {
                    buttonText = "❌ Занят";
                    buttonStyle = "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px;";
                    canUserFree = false;
                }
            }

            actionBtn.setText(buttonText);
            actionBtn.setStyle(buttonStyle);

            final boolean finalCanUserFree = canUserFree;

            actionBtn.setOnAction(e -> {
                // Проверка: только авторизованные пользователи могут бронировать
                if (!table.isOccupied() && !UserManager.getInstance().isLoggedIn()) {
                    displayMessage("⚠️ Только авторизованные пользователи могут бронировать столики!\nПожалуйста, войдите в аккаунт.");
                    return;
                }

                if (!finalCanUserFree) {
                    displayMessage("⚠️ Этот столик забронирован другим пользователем. Вы не можете его освободить.");
                    return;
                }

                if (table.isOccupied()) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Подтверждение");
                    confirmAlert.setHeaderText("Освободить столик №" + table.getNumber());
                    confirmAlert.setContentText("Вы уверены, что хотите освободить этот столик?");

                    confirmAlert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            table.setOccupied(false);
                            icon.setText("🟢");
                            actionBtn.setText("Забронировать");
                            actionBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-cursor: hand;");
                            displayMessage("✅ Столик " + table.getNumber() + " освобожден");
                            controller.updateTableStatus(table.getNumber(), false);
                        }
                    });
                } else {
                    showBookingDialog(table, owner);
                    if (table.isOccupied()) {
                        icon.setText("🔴");
                        actionBtn.setText("Освободить");
                        actionBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 20px; -fx-cursor: hand;");
                    }
                }
            });

            tableCard.getChildren().addAll(icon, tableLabel, seatsLabel, actionBtn);
            tableGrid.add(tableCard, (table.getNumber() - 1) % 3, (table.getNumber() - 1) / 3);
        }

        Button refreshBtn = new Button("🔄 Обновить статус");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> {
            controller.loadTablesFromDB();
            displayTablesStatus();
            refreshTablesPanel(owner);
        });

        panel.getChildren().addAll(title, tableGrid, refreshBtn);
        return panel;
    }

    private VBox createMenuPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15px;");

        Label title = new Label("🍜 Наше меню");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        HBox categoryBox = new HBox(10);
        categoryBox.setAlignment(Pos.CENTER);

        ToggleGroup categoryGroup = new ToggleGroup();
        RadioButton allBtn = createCategoryButton("Все", true);
        RadioButton firstBtn = createCategoryButton("Первые блюда", false);
        RadioButton mainBtn = createCategoryButton("Горячие блюда", false);
        RadioButton saladBtn = createCategoryButton("Салаты", false);
        RadioButton dessertBtn = createCategoryButton("Десерты", false);

        allBtn.setToggleGroup(categoryGroup);
        firstBtn.setToggleGroup(categoryGroup);
        mainBtn.setToggleGroup(categoryGroup);
        saladBtn.setToggleGroup(categoryGroup);
        dessertBtn.setToggleGroup(categoryGroup);

        categoryBox.getChildren().addAll(allBtn, firstBtn, mainBtn, saladBtn, dessertBtn);

        ListView<String> menuList = new ListView<>();
        menuList.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 10px; -fx-background-radius: 10px;");

        Runnable updateMenu = () -> {
            menuList.getItems().clear();
            String selectedCategory = null;
            if (firstBtn.isSelected()) selectedCategory = "Первые блюда";
            else if (mainBtn.isSelected()) selectedCategory = "Горячие блюда";
            else if (saladBtn.isSelected()) selectedCategory = "Салаты";
            else if (dessertBtn.isSelected()) selectedCategory = "Десерты";

            for (Dish item : controller.getMenu()) {
                if (selectedCategory == null || item.getCategory().equals(selectedCategory)) {
                    String emoji = getEmojiForCategory(item.getCategory());
                    menuList.getItems().add(
                            String.format("%s %-25s %-15s 💰 %.2f руб.",
                                    emoji, item.getName(), item.getCategory(), item.getPrice())
                    );
                }
            }
        };

        allBtn.setOnAction(e -> updateMenu.run());
        firstBtn.setOnAction(e -> updateMenu.run());
        mainBtn.setOnAction(e -> updateMenu.run());
        saladBtn.setOnAction(e -> updateMenu.run());
        dessertBtn.setOnAction(e -> updateMenu.run());

        updateMenu.run();

        panel.getChildren().addAll(title, categoryBox, menuList);
        return panel;
    }

    private RadioButton createCategoryButton(String text, boolean selected) {
        RadioButton btn = new RadioButton(text);
        btn.setSelected(selected);
        btn.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        return btn;
    }

    private String getEmojiForCategory(String category) {
        switch (category) {
            case "Первые блюда": return "🥣";
            case "Горячие блюда": return "🍖";
            case "Салаты": return "🥗";
            case "Десерты": return "🍰";
            default: return "🍽️";
        }
    }

    private VBox createDeliveryPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15px;");

        Label title = new Label("🚚 Доставка еды");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Выберите блюда и укажите количество");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        HBox addItemBox = new HBox(10);
        addItemBox.setAlignment(Pos.CENTER_LEFT);

        Label foodLabel = new Label("🍽️ Блюдо:");
        foodLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        ComboBox<String> foodCombo = new ComboBox<>();
        for (Dish item : controller.getMenu()) {
            foodCombo.getItems().add(item.getName() + " - " + item.getPrice() + " руб.");
        }
        foodCombo.setPromptText("Выберите блюдо");
        foodCombo.setPrefWidth(250);
        foodCombo.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label quantityLabel = new Label("Кол-во:");
        quantityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.setPrefWidth(80);
        quantitySpinner.setStyle("-fx-background-radius: 10px;");

        Button addItemBtn = new Button("➕ Добавить в заказ");
        addItemBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 15px; -fx-background-radius: 20px; -fx-cursor: hand;");

        addItemBox.getChildren().addAll(foodLabel, foodCombo, quantityLabel, quantitySpinner, addItemBtn);

        Label cartLabel = new Label("🛒 Ваша корзина:");
        cartLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ListView<String> selectedItemsList = new ListView<>();
        selectedItemsList.setPrefHeight(150);
        selectedItemsList.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #bdc3c7; -fx-border-radius: 10px;");

        HBox cartControlBox = new HBox(10);
        cartControlBox.setAlignment(Pos.CENTER_LEFT);

        Button removeItemBtn = new Button("❌ Удалить выбранное");
        removeItemBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 15px; -fx-background-radius: 20px; -fx-cursor: hand;");

        Button clearCartBtn = new Button("🗑️ Очистить корзину");
        clearCartBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px 15px; -fx-background-radius: 20px; -fx-cursor: hand;");

        cartControlBox.getChildren().addAll(removeItemBtn, clearCartBtn);

        Label totalLabel = new Label("💰 Общая сумма: 0.00 руб.");
        totalLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        class OrderItem {
            String name;
            double price;
            int quantity;

            OrderItem(String name, double price, int quantity) {
                this.name = name;
                this.price = price;
                this.quantity = quantity;
            }

            double getTotal() {
                return price * quantity;
            }

            String getDisplayString() {
                return String.format("%s x%d = %.2f руб.", name, quantity, getTotal());
            }
        }

        ArrayList<OrderItem> cart = new ArrayList<>();

        Runnable updateCartDisplay = () -> {
            selectedItemsList.getItems().clear();
            double total = 0;
            for (OrderItem item : cart) {
                selectedItemsList.getItems().add(item.getDisplayString());
                total += item.getTotal();
            }
            totalLabel.setText("💰 Общая сумма: " + String.format("%.2f", total) + " руб.");
        };

        addItemBtn.setOnAction(e -> {
            String selected = foodCombo.getValue();
            if (selected == null) {
                displayMessage("⚠️ Пожалуйста, выберите блюдо из меню");
                return;
            }

            String name = selected.substring(0, selected.lastIndexOf(" - "));
            String priceStr = selected.substring(selected.lastIndexOf("-") + 1).replace("руб.", "").trim();
            double price = Double.parseDouble(priceStr);
            int quantity = quantitySpinner.getValue();

            boolean found = false;
            for (OrderItem item : cart) {
                if (item.name.equals(name)) {
                    item.quantity += quantity;
                    found = true;
                    displayMessage("✅ Количество блюда \"" + name + "\" увеличено на " + quantity + "\nТеперь: " + item.quantity + " шт.");
                    break;
                }
            }

            if (!found) {
                cart.add(new OrderItem(name, price, quantity));
                displayMessage("✅ Блюдо \"" + name + "\" добавлено в корзину\nКоличество: " + quantity + " шт.");
            }

            updateCartDisplay.run();
            foodCombo.setValue(null);
            quantitySpinner.getValueFactory().setValue(1);
        });

        removeItemBtn.setOnAction(e -> {
            int selectedIndex = selectedItemsList.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < cart.size()) {
                OrderItem removed = cart.remove(selectedIndex);
                displayMessage("🗑️ Блюдо \"" + removed.name + "\" удалено из корзины");
                updateCartDisplay.run();
            } else {
                displayMessage("⚠️ Выберите блюдо для удаления");
            }
        });

        clearCartBtn.setOnAction(e -> {
            if (!cart.isEmpty()) {
                cart.clear();
                updateCartDisplay.run();
                displayMessage("🗑️ Корзина очищена");
            }
        });

        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10));

        int row = 0;

        Label addressLabel = new Label("📍 Адрес доставки:");
        addressLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField addressField = new TextField();
        addressField.setPromptText("Улица, дом, квартира");
        addressField.setPrefWidth(350);
        addressField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");
        formGrid.add(addressLabel, 0, row);
        formGrid.add(addressField, 1, row);

        row++;
        Label phoneLabel = new Label("📞 Номер телефона:");
        phoneLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextField phoneField = new TextField();
        phoneField.setPromptText("+7 (XXX) XXX-XX-XX");
        phoneField.setPrefWidth(350);
        phoneField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");
        formGrid.add(phoneLabel, 0, row);
        formGrid.add(phoneField, 1, row);

        row++;
        Label commentLabel = new Label("💬 Комментарий:");
        commentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Пожелания к заказу...");
        commentArea.setPrefRowCount(3);
        commentArea.setPrefWidth(350);
        commentArea.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");
        formGrid.add(commentLabel, 0, row);
        formGrid.add(commentArea, 1, row);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");
        formGrid.add(errorLabel, 1, row + 1);

        phoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\+?[0-9\\s\\-\\(\\)]*")) {
                phoneField.setText(oldVal);
            }
        });

        Button orderBtn = new Button("✅ Оформить заказ");
        orderBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12px 30px; -fx-background-radius: 25px; -fx-cursor: hand;");
        orderBtn.setPrefWidth(250);

        orderBtn.setOnAction(e -> {
            if (cart.isEmpty()) {
                errorLabel.setText("❌ Добавьте хотя бы одно блюдо в заказ!");
                displayMessage("⚠️ Нельзя оформить пустой заказ. Добавьте блюда!");
                return;
            }

            String address = addressField.getText();
            String phone = phoneField.getText();

            if (address == null || address.trim().isEmpty()) {
                errorLabel.setText("❌ Введите адрес доставки!");
                return;
            }
            if (address.length() < 10) {
                errorLabel.setText("❌ Укажите полный адрес (улица, дом)!");
                return;
            }
            if (phone == null || phone.trim().isEmpty()) {
                errorLabel.setText("❌ Введите номер телефона!");
                return;
            }

            String digitsOnly = phone.replaceAll("\\D", "");
            if (digitsOnly.length() < 10 || digitsOnly.length() > 11) {
                errorLabel.setText("❌ Некорректный номер телефона!");
                return;
            }

            String comment = commentArea.getText().isEmpty() ? "Без комментариев" : commentArea.getText();
            double total = 0;

            StringBuilder itemsList = new StringBuilder();
            for (OrderItem item : cart) {
                itemsList.append(String.format("  • %s x%d = %.2f руб.\n", item.name, item.quantity, item.getTotal()));
                total += item.getTotal();
            }

            displayMessage("✅ ЗАКАЗ ОФОРМЛЕН!\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "🍽️ ЗАКАЗАННЫЕ БЛЮДА:\n" + itemsList.toString() +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "💰 Общая сумма: " + String.format("%.2f", total) + " руб.\n" +
                    "📍 Адрес: " + address + "\n" +
                    "📞 Телефон: " + phone + "\n" +
                    "💬 Комментарий: " + comment + "\n" +
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                    "⏱️ Время доставки: 30-40 минут\n" +
                    "💳 Стоимость доставки: БЕСПЛАТНО\n" +
                    "Спасибо за заказ! ❤️");

            cart.clear();
            updateCartDisplay.run();
            addressField.clear();
            phoneField.clear();
            commentArea.clear();
            errorLabel.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Заказ оформлен");
            alert.setHeaderText("Ваш заказ принят!");
            alert.setContentText("Общая сумма: " + String.format("%.2f", total) + " руб.\nОжидайте доставку 30-40 минут.");
            alert.showAndWait();
        });

        VBox cartSection = new VBox(10);
        cartSection.setPadding(new Insets(10));
        cartSection.setStyle("-fx-background-color: #f0f8ff; -fx-background-radius: 15px; -fx-border-color: #bdc3c7; -fx-border-radius: 15px;");
        cartSection.getChildren().addAll(
                cartLabel,
                selectedItemsList,
                cartControlBox,
                totalLabel
        );

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().add(orderBtn);

        panel.getChildren().addAll(title, subtitle, addItemBox, cartSection, formGrid, buttonBox);
        return panel;
    }

    private VBox createReviewPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 15px;");

        Label title = new Label("⭐ Отзывы гостей");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: transparent;");

        VBox addReviewBox = new VBox(10);
        addReviewBox.setPadding(new Insets(10));
        addReviewBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 15px; -fx-border-color: #bdc3c7; -fx-border-radius: 15px;");

        Label addTitle = new Label("✍️ Оставить отзыв");
        addTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label nameLabel = new Label("👤 Ваше имя:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextField nameField = new TextField();
        nameField.setPromptText("Введите ваше имя");
        nameField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Label ratingLabel = new Label("⭐ Оценка:");
        ratingLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Button[] starButtons = new Button[5];
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            Button starBtn = new Button("☆");
            starBtn.setStyle("-fx-font-size: 24px; -fx-background-color: transparent; -fx-text-fill: #f39c12; -fx-cursor: hand;");
            starBtn.setOnAction(e -> {
                for (int j = 0; j < 5; j++) {
                    if (j < rating) {
                        starButtons[j].setText("★");
                    } else {
                        starButtons[j].setText("☆");
                    }
                }
            });
            starButtons[i] = starBtn;
            ratingBox.getChildren().add(starBtn);
        }

        Label reviewLabel = new Label("💬 Ваш отзыв:");
        reviewLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        TextArea reviewText = new TextArea();
        reviewText.setPromptText("Поделитесь впечатлениями о ресторане...");
        reviewText.setPrefRowCount(4);
        reviewText.setStyle("-fx-background-radius: 10px; -fx-padding: 8px;");

        Button submitBtn = new Button("✍️ Оставить отзыв");
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10px 20px; -fx-background-radius: 25px; -fx-cursor: hand;");

        Label formErrorLabel = new Label();
        formErrorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-font-weight: bold;");

        VBox reviewsListBox = new VBox(10);
        reviewsListBox.setPadding(new Insets(10));
        reviewsListBox.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 15px;");

        Label listTitle = new Label("📖 Отзывы гостей");
        listTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(400);

        VBox cardsContainer = new VBox(10);
        cardsContainer.setPadding(new Insets(10));
        cardsContainer.setStyle("-fx-background-color: transparent;");

        Runnable refreshCards = () -> {
            cardsContainer.getChildren().clear();

            if (controller.getReviews().isEmpty()) {
                Label emptyLabel = new Label("😊 Пока нет отзывов\nБудьте первым!");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-alignment: center;");
                emptyLabel.setAlignment(Pos.CENTER);
                cardsContainer.getChildren().add(emptyLabel);
            } else {
                for (int i = controller.getReviews().size() - 1; i >= 0; i--) {
                    Review review = controller.getReviews().get(i);
                    VBox card = createReviewCard(review);
                    cardsContainer.getChildren().add(card);
                }
            }
        };

        refreshCards.run();

        scrollPane.setContent(cardsContainer);
        reviewsListBox.getChildren().addAll(listTitle, scrollPane);

        submitBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String review = reviewText.getText().trim();

            int rating = 0;
            for (int i = 0; i < starButtons.length; i++) {
                if (starButtons[i].getText().equals("★")) {
                    rating = i + 1;
                }
            }

            if (name.isEmpty()) {
                formErrorLabel.setText("Введите ваше имя!");
                return;
            }
            if (review.isEmpty()) {
                formErrorLabel.setText("Напишите ваш отзыв!");
                return;
            }
            if (rating == 0) {
                formErrorLabel.setText("Поставьте оценку!");
                return;
            }

            controller.addReview(name, rating, review);
            displayMessage("❤️ Спасибо за отзыв!\nОценка: " + rating + "/5");
            reviewText.clear();
            formErrorLabel.setText("");
            for (int i = 0; i < starButtons.length; i++) {
                starButtons[i].setText("☆");
            }

            refreshCards.run();
        });

        addReviewBox.getChildren().addAll(addTitle, nameLabel, nameField, ratingLabel, ratingBox, reviewLabel, reviewText, submitBtn, formErrorLabel);

        splitPane.getItems().addAll(addReviewBox, reviewsListBox);
        splitPane.setDividerPositions(0.35);

        panel.getChildren().addAll(title, splitPane);
        return panel;
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("👤 " + review.getCustomerName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox starsBox = new HBox(2);
        int rating = review.getRating();
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < rating ? "★" : "☆");
            star.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 14px;");
            starsBox.getChildren().add(star);
        }

        headerBox.getChildren().addAll(nameLabel, spacer, starsBox);

        TextArea commentArea = new TextArea(review.getComment());
        commentArea.setEditable(false);
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(3);
        commentArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #ecf0f1; -fx-border-radius: 10px; -fx-background-radius: 10px; -fx-font-size: 12px;");
        commentArea.setPrefHeight(60);

        card.getChildren().addAll(headerBox, commentArea);

        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: #fef9e7; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);");
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 3);");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    private void displayTablesStatus() {
        StringBuilder sb = new StringBuilder("📊 СТАТУС СТОЛИКОВ:\n━━━━━━━━━━━━━━━━━━━━\n");
        for (Table table : controller.getTables()) {
            String status = table.isOccupied() ? "❌ ЗАНЯТ" : "✅ СВОБОДЕН";
            sb.append(String.format("Столик %d (%d мест): %s\n",
                    table.getNumber(), table.getSeats(), status));
        }
        displayMessage(sb.toString());
    }

    private void displayWelcomeMessage() {
        String welcome = "🌟 ДОБРО ПОЖАЛОВАТЬ В РЕСТОРАН 'УЮТ' 🌟\n\n" +
                "📊 Столики - бронирование мест\n" +
                "📋 Меню - просмотр блюд и цен\n" +
                "🚚 Доставка - заказ еды на дом\n" +
                "⭐ Отзывы - оставьте свое мнение\n\n" +
                "Приятного аппетита! 🍽️";
        displayMessage(welcome);
    }

    private void displayMessage(String message) {
        displayArea.setText(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}