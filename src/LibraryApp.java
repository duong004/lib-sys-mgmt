import database.config.DatabaseConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import views.LoginView;

public class LibraryApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Test database connection
        System.out.println("Testing database connection...");
        if (!DatabaseConfig.testConnection()) {
            showDatabaseError(primaryStage);
            return;
        }
        System.out.println(" Database connected successfully!\n");

        // Configure primary stage
        primaryStage.setTitle("Library Management System - Hệ thống Quản lý Thư viện");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(700);

        // Set application icon (optional)
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }

        // Create and show login screen
        LoginView loginView = new LoginView(primaryStage);
        Scene loginScene = loginView.createScene();

        primaryStage.setScene(loginScene);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Prevent resizing during login (optional)
        primaryStage.setResizable(true);
    }

    private void showDatabaseError(Stage stage) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Không thể kết nối đến database");
        alert.setContentText(
                "Vui lòng kiểm tra:\n" +
                        "1. PostgreSQL đang chạy\n" +
                        "2. Database 'library_db' đã được tạo\n" +
                        "3. Thông tin trong config.properties đúng"
        );
        alert.showAndWait();
        System.exit(1);
    }

    @Override
    public void stop() {
        // Cleanup when application closes
        System.out.println("Application closing...");
        // Close database connections, etc.
    }

    public static void main(String[] args) {
        launch(args);
    }
}
