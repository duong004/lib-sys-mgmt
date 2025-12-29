package views;

import database.dao.UserDAO;
import database.impl.UserDAOImpl;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.people.User;
import views.admin.AdminDashboard;
import views.librarian.LibrarianDashboard;
import views.reader.ReaderDashboard;

public class LoginView {
    private Stage stage;
    private UserDAO userDAO;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label errorLabel;
    private Button loginButton;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.userDAO = new UserDAOImpl();
    }

    public Scene createScene() {
        // Root container with gradient background
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");

        // Add animated background circles
        root.getChildren().add(createAnimatedBackground());

        // Main login card
        VBox loginCard = createLoginCard();
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1200, 700);

        // Add CSS stylesheet
        scene.getStylesheets().add(getClass().getResource("/resources/styles/dashboard.css").toExternalForm());

        return scene;
    }

    private Pane createAnimatedBackground() {
        Pane bgPane = new Pane();
        bgPane.setMouseTransparent(true);

        // Create floating circles with different sizes and animations
        for (int i = 0; i < 6; i++) {
            Circle circle = new Circle();
            circle.setRadius(80 + Math.random() * 120);
            circle.setFill(Color.rgb(255, 255, 255, 0.05 /*+ Math.random() * 0.1*/));
            circle.setEffect(new GaussianBlur(20));

            // Random position
            circle.setLayoutX(Math.random() * 1200);
            circle.setLayoutY(Math.random() * 700);

            // Floating animation
            TranslateTransition tt = new TranslateTransition(Duration.seconds(15 + Math.random() * 10), circle);
            tt.setByX(-100 + Math.random() * 200);
            tt.setByY(-100 + Math.random() * 200);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setAutoReverse(true);
            tt.play();

            bgPane.getChildren().add(circle);
        }

        return bgPane;
    }

    private VBox createLoginCard() {
        VBox card = new VBox(25);
        card.setMaxWidth(450);
        card.setMaxHeight(550);
        card.setPadding(new Insets(50, 40, 50, 40));
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("login-card");

        // Drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(30);
        shadow.setSpread(0.1);
        card.setEffect(shadow);

        // Logo/Icon area
        VBox logoArea = createLogoArea();

        // Title
        Label titleLabel = new Label("Đăng nhập");
        titleLabel.getStyleClass().add("title-label");

        Label subtitleLabel = new Label("Hệ thống Quản lý Thư viện");
        subtitleLabel.getStyleClass().add("subtitle-label");

        // Input fields
        VBox inputsBox = createInputFields();

        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Login button
        loginButton = new Button("ĐĂNG NHẬP");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin());

        // Footer text
        Label footerLabel = new Label("Developed by Group 23");
        footerLabel.getStyleClass().add("footer-label");

        card.getChildren().addAll(
                logoArea,
                titleLabel,
                subtitleLabel,
                inputsBox,
                errorLabel,
                loginButton,
                footerLabel
        );

        // Entrance animation
        card.setOpacity(0);
        card.setTranslateY(30);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(800), card);
        slideIn.setFromY(30);
        slideIn.setToY(0);

        ParallelTransition entrance = new ParallelTransition(fadeIn, slideIn);
        entrance.setDelay(Duration.millis(200));
        entrance.play();

        return card;
    }

    private VBox createLogoArea() {
        VBox logoBox = new VBox(10);
        logoBox.setAlignment(Pos.CENTER);

        // Book icon using Unicode
        Label iconLabel = new Label("📚");
        iconLabel.setStyle("-fx-font-size: 64px;");

        logoBox.getChildren().add(iconLabel);
        return logoBox;
    }

    private VBox createInputFields() {
        VBox inputsBox = new VBox(20);
        inputsBox.setAlignment(Pos.CENTER);

        // Username field
        VBox usernameBox = createInputField("👤", "Tên đăng nhập", false);
        usernameField = (TextField) usernameBox.getChildren().get(1);

        // Password field
        VBox passwordBox = createInputField("🔒", "Mật khẩu", true);
        passwordField = (PasswordField) passwordBox.getChildren().get(1);

        // Enter key to login
        passwordField.setOnAction(e -> handleLogin());

        inputsBox.getChildren().addAll(usernameBox, passwordBox);
        return inputsBox;
    }

    private VBox createInputField(String icon, String placeholder, boolean isPassword) {
        VBox fieldBox = new VBox(8);

        // Icon + Label
        HBox labelBox = new HBox(8);
        labelBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 18px;");
        Label textLabel = new Label(placeholder);
        textLabel.getStyleClass().add("field-label");
        labelBox.getChildren().addAll(iconLabel, textLabel);

        // Input field
        TextField field;
        if (isPassword) {
            field = new PasswordField();
        } else {
            field = new TextField();
        }
        field.setPromptText(placeholder);
        field.getStyleClass().add("input-field");
        field.setPrefHeight(45);

        // Focus animation
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), field);
                scale.setToX(1.02);
                scale.setToY(1.02);
                scale.play();
            } else {
                ScaleTransition scale = new ScaleTransition(Duration.millis(200), field);
                scale.setToX(1.0);
                scale.setToY(1.0);
                scale.play();
            }
        });

        fieldBox.getChildren().addAll(labelBox, field);
        return fieldBox;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            shakeAnimation(loginButton);
            return;
        }

        // Disable button and show loading
        loginButton.setDisable(true);
        loginButton.setText("Đang xử lý...");

        // Simulate async operation (in real app, use Task)
        new Thread(() -> {
            try {
                User user = userDAO.authenticate(username, password);

                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        // Update last login
                        try {
                            userDAO.updateLastLogin(username);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        // Success animation then navigate
                        successAnimation(() -> navigateToDashboard(user));
                    } else {
                        loginButton.setDisable(false);
                        loginButton.setText("ĐĂNG NHẬP");
                        showError("Sai tên đăng nhập hoặc mật khẩu!");
                        shakeAnimation(errorLabel);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("ĐĂNG NHẬP");
                    showError("Lỗi kết nối: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message); //⚠️
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Fade in error
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void shakeAnimation(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), node);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void successAnimation(Runnable onComplete) {
        // Green checkmark effect
        loginButton.setText(" Thành công!");
        loginButton.setStyle(loginButton.getStyle() + "-fx-background-color: #10b981;");

        // Scale animation
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), loginButton);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.setOnFinished(e -> {
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(ev -> onComplete.run());
            pause.play();
        });
        scale.play();
    }

    private void navigateToDashboard(User user) {
        Scene dashboardScene = null;

        switch (user.getRole()) {
            case ADMIN:
                AdminDashboard adminDashboard = new AdminDashboard(stage, user);
                dashboardScene = adminDashboard.createScene();
                break;

            case LIBRARIAN:
                LibrarianDashboard librarianDashboard = new LibrarianDashboard(stage, user);
                dashboardScene = librarianDashboard.createScene();
                break;

            case READER:
                // NEW: Điều hướng đến ReaderDashboard
                ReaderDashboard readerDashboard = new ReaderDashboard(stage, user);
                dashboardScene = readerDashboard.createScene();
                break;
        }

        if (dashboardScene != null) {
            stage.setScene(dashboardScene);
            stage.setMaximized(true);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}