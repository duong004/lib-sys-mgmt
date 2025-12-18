package views;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.people.User;

public abstract class BaseDashboard {

    protected Stage stage;
    protected User currentUser;
    protected BorderPane root;
    protected VBox sidebar;
    protected StackPane contentArea;
    protected Label titleLabel;

    // Colors
    protected static final String PRIMARY_COLOR = "#667eea";
    protected static final String SECONDARY_COLOR = "#764ba2";
    protected static final String SIDEBAR_BG = "#1e293b";
    protected static final String CONTENT_BG = "#f8fafc";

    public BaseDashboard(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    public Scene createScene() {
        root = new BorderPane();

        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);

        // Create content area
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + CONTENT_BG + ";");
        root.setCenter(contentArea);

        // Load default view
        loadDefaultView();

        Scene scene = new Scene(root, 1400, 800);
        scene.getStylesheets().add(getClass().getResource("/resources/styles/login.css").toExternalForm());

        return scene;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(280);
        sidebar.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setPadding(new Insets(0));

        // Header with user info
        VBox header = createSidebarHeader();

        // Navigation menu
        VBox menu = createNavigationMenu();

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Footer with logout
        VBox footer = createSidebarFooter();

        sidebar.getChildren().addAll(header, menu, spacer, footer);

        return sidebar;
    }

    private VBox createSidebarHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(30, 20, 30, 20));
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");

        // Logo
        Label logo = new Label("📚");
        logo.setStyle("-fx-font-size: 48px;");

        // App name
        Label appName = new Label("Library System");
        appName.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Divider
        Separator divider = new Separator();
        divider.setMaxWidth(200);
        divider.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");

        // User info
        VBox userInfo = new VBox(5);
        userInfo.setAlignment(Pos.CENTER);

        // Avatar circle
        Circle avatar = new Circle(30);
        avatar.setFill(Color.web(PRIMARY_COLOR));

        // User initial
        StackPane avatarStack = new StackPane();
        Label initial = new Label(String.valueOf(currentUser.getFullName().charAt(0)).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");
        avatarStack.getChildren().addAll(avatar, initial);

        Label userName = new Label(currentUser.getFullName());
        userName.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 600;");

        Label userRole = new Label(currentUser.getRole().getDisplayName());
        userRole.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        userInfo.getChildren().addAll(avatarStack, userName, userRole);

        header.getChildren().addAll(logo, appName, divider, userInfo);

        return header;
    }

    protected abstract VBox createNavigationMenu();

    protected Button createMenuButton(String icon, String text, Runnable action) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("menu-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(15, 20, 15, 20));
        btn.setOnAction(e -> {
            highlightButton(btn);
            action.run();
        });

        // Hover effect
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyleClass().contains("menu-button-active")) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
                scale.setToX(1.05);
                scale.play();
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyleClass().contains("menu-button-active")) {
                ScaleTransition scale = new ScaleTransition(Duration.millis(100), btn);
                scale.setToX(1.0);
                scale.play();
            }
        });

        return btn;
    }

    private void highlightButton(Button activeButton) {
        // Remove active class from all buttons
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof VBox) {
                for (javafx.scene.Node child : ((VBox) node).getChildren()) {
                    if (child instanceof Button) {
                        child.getStyleClass().remove("menu-button-active");
                    }
                }
            }
        }

        // Add active class to clicked button
        activeButton.getStyleClass().add("menu-button-active");
    }

    private VBox createSidebarFooter() {
        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER);

        Button logoutBtn = new Button("🚪  Đăng xuất");
        logoutBtn.getStyleClass().add("logout-button");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> handleLogout());

        Label version = new Label("v1.0.0");
        version.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

        footer.getChildren().addAll(logoutBtn, version);

        return footer;
    }

    protected void loadView(javafx.scene.Node view, String title) {
        // Fade out old content
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            contentArea.getChildren().clear();

            // Add new content
            VBox container = new VBox(20);
            container.setPadding(new Insets(30));
            container.setAlignment(Pos.TOP_LEFT);

            // Page title
            Label pageTitle = new Label(title);
            pageTitle.getStyleClass().add("page-title");

            container.getChildren().addAll(pageTitle, view);
            contentArea.getChildren().add(container);

            // Fade in new content
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), contentArea);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    protected abstract void loadDefaultView();

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đăng xuất");
        confirm.setHeaderText("Bạn có chắc muốn đăng xuất?");
        confirm.setContentText("Tất cả thay đổi chưa lưu sẽ bị mất.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Return to login screen
                LoginView loginView = new LoginView(stage);
                Scene loginScene = loginView.createScene();
                stage.setScene(loginScene);
            }
        });
    }
}
