package views.librarian;

import database.impl.LibraryService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.people.User;
import views.BaseDashboard;

public class LibrarianDashboard extends BaseDashboard {

    private LibraryService libraryService;

    public LibrarianDashboard(Stage stage, User currentUser) {
        super(stage, currentUser);
        this.libraryService = new LibraryService("Thư viện Trung tâm", "123 Đường ABC, Hà Nội");
    }

    @Override
    protected VBox createNavigationMenu() {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(20, 15, 20, 15));

        // Dashboard
        Button dashboardBtn = createMenuButton("📊", "Tổng quan", () -> loadDashboardView());

        // Book Management
        Button booksBtn = createMenuButton("📚", "Quản lý Sách", () -> loadBookManagementView());

        // Reader Management
        Button readersBtn = createMenuButton("👥", "Quản lý Độc giả", () -> loadReaderManagementView());

        // Borrow/Return
        Button borrowBtn = createMenuButton("🔄", "Mượn/Trả sách", () -> loadBorrowReturnView());

        // Reports
        Button reportsBtn = createMenuButton("📈", "Báo cáo", () -> loadReportsView());

        menu.getChildren().addAll(
                dashboardBtn,
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
        DashboardOverview overview = new DashboardOverview(libraryService);
        loadView(overview.createView(), "📊 Tổng quan");
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
 * Dashboard Overview - Stats and quick actions
 */
class DashboardOverview {

    private LibraryService libraryService;

    public DashboardOverview(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public VBox createView() {
        VBox view = new VBox(25);
        view.setPadding(new Insets(0));

        // Stats cards
        HBox statsRow = createStatsCards();

        // Quick actions
        HBox actionsRow = createQuickActions();

        // Recent activities (placeholder)
        VBox recentActivities = createRecentActivities();

        view.getChildren().addAll(statsRow, actionsRow, recentActivities);

        return view;
    }

    private HBox createStatsCards() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);

        // Total Books
        VBox bookCard = createStatCard(
                "📚",
                String.valueOf(libraryService.getBooks().size()),
                "Tổng số sách",
                "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"
        );

        // Total Readers
        VBox readerCard = createStatCard(
                "👥",
                String.valueOf(libraryService.getReaders().size()),
                "Độc giả",
                "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)"
        );

        // Currently Borrowed
        long borrowed = libraryService.getBorrowRecords().stream()
                .filter(r -> r.getStatus() == models.enums.BorrowStatus.BORROWED)
                .count();
        VBox borrowCard = createStatCard(
                "📖",
                String.valueOf(borrowed),
                "Đang mượn",
                "linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)"
        );

        // Overdue
        VBox overdueCard = createStatCard(
                "⚠️",
                String.valueOf(libraryService.getOverdueRecords().size()),
                "Quá hạn",
                "linear-gradient(135deg, #fa709a 0%, #fee140 100%)"
        );

        row.getChildren().addAll(bookCard, readerCard, borrowCard, overdueCard);
        HBox.setHgrow(bookCard, Priority.ALWAYS);
        HBox.setHgrow(readerCard, Priority.ALWAYS);
        HBox.setHgrow(borrowCard, Priority.ALWAYS);
        HBox.setHgrow(overdueCard, Priority.ALWAYS);

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

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setScaleX(1.05);
            card.setScaleY(1.05);
        });
        card.setOnMouseExited(e -> {
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });

        return card;
    }

    private HBox createQuickActions() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("⚡ Thao tác nhanh");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Button addBookBtn = new Button("➕ Thêm sách");
        addBookBtn.getStyleClass().add("primary-button");

        Button registerReaderBtn = new Button("👤 Đăng ký độc giả");
        registerReaderBtn.getStyleClass().add("primary-button");

        Button borrowBtn = new Button("📤 Mượn sách");
        borrowBtn.getStyleClass().add("secondary-button");

        Button returnBtn = new Button("📥 Trả sách");
        returnBtn.getStyleClass().add("secondary-button");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(title, spacer, addBookBtn, registerReaderBtn, borrowBtn, returnBtn);

        return row;
    }

    private VBox createRecentActivities() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label title = new Label("📋 Hoạt động gần đây");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Placeholder for recent activities
        ListView<String> activityList = new ListView<>();
        activityList.getItems().addAll(
                " Nguyễn Văn A đã mượn 'Clean Code'",
                " Trần Thị B đã trả 'Design Patterns'",
                " Lê Văn C đã đăng ký thẻ độc giả",
                " 'Introduction to Algorithms' sắp quá hạn",
                " Phạm Thị D đã gia hạn 'The Pragmatic Programmer'"
        );
        activityList.setPrefHeight(250);
        activityList.setStyle("-fx-background-color: transparent;");

        card.getChildren().addAll(title, activityList);

        return card;
    }
}
