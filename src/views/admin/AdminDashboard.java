package views.admin;

import database.dao.UserDAO;
import database.impl.LibraryService;
import database.impl.UserDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.people.User;
import views.BaseDashboard;
import views.librarian.BookManagementView;
import views.librarian.BorrowReturnView;
import views.librarian.ReaderManagementView;
import views.librarian.ReportsView;

public class AdminDashboard extends BaseDashboard {

    private LibraryService libraryService;
    private UserDAO userDAO;

    public AdminDashboard(Stage stage, User currentUser) {
        super(stage, currentUser);
        this.libraryService = new LibraryService("Thư viện Trung tâm", "123 Đường ABC, Hà Nội");
        this.userDAO = new UserDAOImpl();
    }

    @Override
    protected VBox createNavigationMenu() {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(20, 15, 20, 15));

        // Dashboard
        Button dashboardBtn = createMenuButton("📊", "Tổng quan", () -> loadDashboardView());

        // User Management (ADMIN ONLY)
        Button usersBtn = createMenuButton("👤", "Quản lý Người dùng", () -> loadUserManagementView());

        // Separator
        Separator sep = new Separator();
        sep.setMaxWidth(200);
        sep.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2);");

        // All librarian features
        Button booksBtn = createMenuButton("📚", "Quản lý Sách", () -> loadBookManagementView());
        Button readersBtn = createMenuButton("👥", "Quản lý Độc giả", () -> loadReaderManagementView());
        Button borrowBtn = createMenuButton("🔄", "Mượn/Trả sách", () -> loadBorrowReturnView());
        Button reportsBtn = createMenuButton("📈", "Báo cáo", () -> loadReportsView());

        menu.getChildren().addAll(
                dashboardBtn,
                usersBtn,
                sep,
                booksBtn,
                readersBtn,
                borrowBtn,
                reportsBtn
        );

        return menu;
    }

    @Override
    protected void loadDefaultView() {
        loadDashboardView();
    }

    private void loadDashboardView() {
        AdminOverview overview = new AdminOverview(libraryService, userDAO);
        loadView(overview.createView(), "👑 Admin Dashboard");
    }

    private void loadUserManagementView() {
        UserManagementView userView = new UserManagementView(libraryService, userDAO, currentUser);
        loadView(userView.createView(), "👤 Quản lý Người dùng");
    }

    private void loadBookManagementView() {
        BookManagementView bookView = new BookManagementView(libraryService, currentUser);
        loadView(bookView.createView(), "📚 Quản lý Sách");
    }

    private void loadReaderManagementView() {
        ReaderManagementView readerView = new ReaderManagementView(libraryService, currentUser);
        loadView(readerView.createView(), "👥 Quản lý Độc giả");
    }

    private void loadBorrowReturnView() {
        BorrowReturnView borrowView = new BorrowReturnView(libraryService, currentUser);
        loadView(borrowView.createView(), "🔄 Mượn/Trả sách");
    }

    private void loadReportsView() {
        ReportsView reportsView = new ReportsView(libraryService);
        loadView(reportsView.createView(), "📈 Báo cáo & Thống kê");
    }
}

/**
 * Admin Overview - System-wide statistics
 */
class AdminOverview {

    private LibraryService libraryService;
    private UserDAO userDAO;

    public AdminOverview(LibraryService libraryService, UserDAO userDAO) {
        this.libraryService = libraryService;
        this.userDAO = userDAO;
    }

    public VBox createView() {
        VBox view = new VBox(25);

        // Stats cards - Extended for admin
        HBox statsRow = createStatsCards();

        // System health
        VBox healthSection = createSystemHealth();

        // Recent activities
        VBox activitiesSection = createRecentActivities();

        view.getChildren().addAll(statsRow, healthSection, activitiesSection);

        return view;
    }

    private HBox createStatsCards() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);

        // Books
        VBox bookCard = createStatCard(
                "📚",
                String.valueOf(libraryService.getBooks().size()),
                "Tổng sách",
                "linear-gradient(to bottom right, #667eea 0%, #764ba2 100%)"
        );

        // Readers
        VBox readerCard = createStatCard(
                "👥",
                String.valueOf(libraryService.getReaders().size()),
                "Độc giả",
                "linear-gradient(to bottom right, #f093fb 0%, #f5576c 100%)"
        );

        // Users
        try {
            int userCount = userDAO.findAll().size();
            VBox userCard = createStatCard(
                    "👤",
                    String.valueOf(userCount),
                    "Người dùng",
                    "linear-gradient(to bottom right, #4facfe 0%, #00f2fe 100%)"
            );
            row.getChildren().add(userCard);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Borrowed
        long borrowed = libraryService.getBorrowRecords().stream()
                .filter(r -> r.getStatus() == models.enums.BorrowStatus.BORROWED)
                .count();
        VBox borrowCard = createStatCard(
                "📖",
                String.valueOf(borrowed),
                "Đang mượn",
                "linear-gradient(to bottom right, #43e97b 0%, #38f9d7 100%)"
        );

        row.getChildren().addAll(bookCard, readerCard, borrowCard);

        HBox.setHgrow(bookCard, Priority.ALWAYS);
        HBox.setHgrow(readerCard, Priority.ALWAYS);
        HBox.setHgrow(borrowCard, Priority.ALWAYS);

        return row;
    }

    private VBox createStatCard(String icon, String number, String label, String gradient) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setStyle(
                "-fx-background-radius: 15px; " +
                        "-fx-background-color: " + gradient + "; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);"
        );
        card.setMaxWidth(Double.MAX_VALUE);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 40px;");

        Label numberLabel = new Label(number);
        numberLabel.getStyleClass().add("stats-number");

        Label textLabel = new Label(label);
        textLabel.getStyleClass().add("stats-label");

        card.getChildren().addAll(iconLabel, numberLabel, textLabel);

        return card;
    }

    private VBox createSystemHealth() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");

        Label title = new Label("🏥 Tình trạng Hệ thống");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        // Database
        addHealthItem(grid, 0, "💾 Database", "Hoạt động", "✅");

        // Active Users
        try {
            long activeUsers = userDAO.findAll().stream()
                    .filter(User::isActive)
                    .count();
            addHealthItem(grid, 1, "👤 Người dùng hoạt động", String.valueOf(activeUsers), "✅");
        } catch (Exception e) {
            addHealthItem(grid, 1, "👤 Người dùng hoạt động", "N/A", "⚠️");
        }

        // Overdue books
        int overdueCount = libraryService.getOverdueRecords().size();
        String overdueStatus = overdueCount == 0 ? "✅" : "⚠️";
        addHealthItem(grid, 2, "⏰ Sách quá hạn", String.valueOf(overdueCount), overdueStatus);

        section.getChildren().addAll(title, grid);

        return section;
    }

    private void addHealthItem(GridPane grid, int row, String label, String value, String status) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label statusNode = new Label(status);
        statusNode.setStyle("-fx-font-size: 18px;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
        grid.add(statusNode, 2, row);
    }

    private VBox createRecentActivities() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");

        Label title = new Label("📋 Hoạt động Gần đây");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> activityList = new ListView<>();
        activityList.setPrefHeight(250);

        ObservableList<String> activities = FXCollections.observableArrayList();

        // Get recent borrows
        libraryService.getBorrowRecords().stream()
                .sorted((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()))
                .limit(10)
                .forEach(r -> {
                    String activity = String.format("🔵 %s - %s mượn '%s'",
                            r.getBorrowDate(),
                            r.getReader().getName(),
                            r.getBook().getTitle()
                    );
                    activities.add(activity);
                });

        activityList.setItems(activities);

        section.getChildren().addAll(title, activityList);

        return section;
    }
}
