package views.reader;

import database.impl.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.BorrowRecord;
import models.books.Book;
import models.enums.BorrowStatus;
import models.people.*;
import views.BaseDashboard;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class ReaderDashboard extends BaseDashboard {

    private LibraryService libraryService;
    private Reader currentReader;

    public ReaderDashboard(Stage stage, User currentUser) {
        super(stage, currentUser);
        this.libraryService = new LibraryService("Thư viện Trung tâm", "123 Đường ABC, Hà Nội");

        // Load reader info
        if (currentUser.getLinkedEntityId() != null) {
            this.currentReader = libraryService.findReaderById(currentUser.getLinkedEntityId());
        }
    }

    @Override
    protected VBox createNavigationMenu() {
        VBox menu = new VBox(5);
        menu.setPadding(new Insets(20, 15, 20, 15));

        // Dashboard
        Button dashboardBtn = createMenuButton("📊", "Tổng quan", () -> loadDashboardView());

        // Search Books
        Button searchBtn = createMenuButton("🔍", "Tìm sách", () -> loadSearchView());

        // My Books (Borrowed)
        Button myBooksBtn = createMenuButton("📚", "Sách đang mượn", () -> loadMyBooksView());

        // Borrow History
        Button historyBtn = createMenuButton("📖", "Lịch sử mượn", () -> loadHistoryView());

        // Profile
        Button profileBtn = createMenuButton("👤", "Thông tin cá nhân", () -> loadProfileView());

        menu.getChildren().addAll(
                dashboardBtn,
                searchBtn,
                myBooksBtn,
                historyBtn,
                profileBtn
        );

        return menu;
    }

    @Override
    protected void loadDefaultView() {
        loadDashboardView();
    }

    private void loadDashboardView() {
        ReaderOverview overview = new ReaderOverview(libraryService, currentReader, this);
        loadView(overview.createView(), "📊 Tổng quan");
    }

    public void loadSearchView() {
        BookSearchView searchView = new BookSearchView(libraryService);
        loadView(searchView.createView(), "🔍 Tìm kiếm sách");
    }

    public void loadMyBooksView() {
        MyBorrowedBooksView myBooksView = new MyBorrowedBooksView(libraryService, currentReader);
        loadView(myBooksView.createView(), "📚 Sách đang mượn");
    }

    public void loadHistoryView() {
        BorrowHistoryView historyView = new BorrowHistoryView(libraryService, currentReader);
        loadView(historyView.createView(), "📖 Lịch sử mượn");
    }

    public void loadProfileView() {
        ReaderProfileView profileView = new ReaderProfileView(libraryService, currentReader);
        loadView(profileView.createView(), "👤 Thông tin cá nhân");
    }
}

// Reader Dashboard Overview
class ReaderOverview {
    private LibraryService libraryService;
    private Reader currentReader;
    private ReaderDashboard parentDashboard;

    public ReaderOverview(LibraryService libraryService, Reader currentReader, ReaderDashboard parentDashboard) {
        this.libraryService = libraryService;
        this.currentReader = currentReader;
        this.parentDashboard = parentDashboard;
    }

    public VBox createView() {
        VBox view = new VBox(25);
        view.setPadding(new Insets(0));

        // Welcome message
        Label welcomeLabel = new Label("Xin chào, " + currentReader.getName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Stats cards
        HBox statsRow = createStatsCards();

        // Quick actions
        VBox actionsCard = createQuickActions();

        // Recent borrows
        VBox recentBorrowsCard = createRecentBorrows();

        view.getChildren().addAll(welcomeLabel, statsRow, actionsCard, recentBorrowsCard);

        return view;
    }

    private HBox createStatsCards() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER);

        List<BorrowRecord> myRecords = libraryService.getBorrowHistory(currentReader.getId());
        long activeBorrows = myRecords.stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                .count();

        long overdue = myRecords.stream()
                .filter(BorrowRecord::isOverdue)
                .count();

        // Currently Borrowing
        VBox borrowingCard = createStatCard(
                "📚",
                String.valueOf(activeBorrows) + "/" + currentReader.getMembershipType().getBorrowLimit(),
                "Đang mượn",
                "linear-gradient(to bottom right, #667eea 0%, #764ba2 100%)"
        );

        // Total Borrowed
        VBox totalCard = createStatCard(
                "📖",
                String.valueOf(currentReader.getTotalBorrowed()),
                "Tổng đã mượn",
                "linear-gradient(to bottom right, #4facfe 0%, #00f2fe 100%)"
        );

        // Overdue
        VBox overdueCard = createStatCard(
                "⚠",
                String.valueOf(overdue),
                "Quá hạn",
                overdue > 0 ?
                        "linear-gradient(to bottom right, #fa709a 0%, #fee140 100%)" :
                        "linear-gradient(to bottom right, #43e97b 0%, #38f9d7 100%)"
        );

        // Membership
        VBox memberCard = createStatCard(
                "⭐",
                currentReader.getMembershipType().name(),
                "Loại thẻ",
                "linear-gradient(to bottom right, #f093fb 0%, #f5576c 100%)"
        );

        row.getChildren().addAll(borrowingCard, totalCard, overdueCard, memberCard);
        HBox.setHgrow(borrowingCard, Priority.ALWAYS);
        HBox.setHgrow(totalCard, Priority.ALWAYS);
        HBox.setHgrow(overdueCard, Priority.ALWAYS);
        HBox.setHgrow(memberCard, Priority.ALWAYS);

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

    private VBox createQuickActions() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label title = new Label("⚡ Thao tác nhanh");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        Button searchBtn = new Button("🔍 Tìm sách");
        searchBtn.getStyleClass().add("primary-button");
        searchBtn.setOnAction(e -> {
            if (parentDashboard != null) {
                parentDashboard.loadSearchView();
            }
        });

        Button myBooksBtn = new Button("📚 Sách đang mượn");
        myBooksBtn.getStyleClass().add("secondary-button");
        myBooksBtn.setOnAction(e -> {
            if (parentDashboard != null) {
                parentDashboard.loadMyBooksView();
            }
        });

        Button historyBtn = new Button("📖 Lịch sử");
        historyBtn.getStyleClass().add("secondary-button");
        historyBtn.setOnAction(e -> {
            if (parentDashboard != null) {
                parentDashboard.loadHistoryView();
            }
        });

        buttonsRow.getChildren().addAll(searchBtn, myBooksBtn, historyBtn);

        card.getChildren().addAll(title, buttonsRow);

        return card;
    }

    private VBox createRecentBorrows() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label title = new Label("📋 Hoạt động gần đây");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> activityList = new ListView<>();
        activityList.setPrefHeight(250);

        ObservableList<String> activities = FXCollections.observableArrayList();

        List<BorrowRecord> recentRecords = libraryService.getBorrowHistory(currentReader.getId())
                .stream()
                .sorted((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()))
                .limit(10)
                .collect(Collectors.toList());

        for (BorrowRecord record : recentRecords) {
            String status = record.getStatus() == BorrowStatus.BORROWED ? "Đang mượn" : "Đã trả";
            String activity = String.format("📖 %s - '%s' - %s",
                    record.getBorrowDate(),
                    record.getBook().getTitle(),
                    status
            );
            activities.add(activity);
        }

        if (activities.isEmpty()) {
            activities.add("Chưa có hoạt động nào");
        }

        activityList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                int selectedIndex = activityList.getSelectionModel().getSelectedIndex();
                if (selectedIndex >= 0 && selectedIndex < recentRecords.size()) {
                    BorrowRecord selectedRecord = recentRecords.get(selectedIndex);
                    showRecordDetails(selectedRecord);
                }
            }
        });

        activityList.setItems(activities);

        activityList.setItems(activities);

        card.getChildren().addAll(title, activityList);

        return card;
    }

    private void showRecordDetails(BorrowRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết phiếu mượn");
        alert.setHeaderText(record.getBook().getTitle());
        alert.setContentText(
                "Mã phiếu: " + record.getRecordId() + "\n" +
                        "ISBN: " + record.getBook().getISBN() + "\n" +
                        "Tác giả: " + record.getBook().getAuthor() + "\n" +
                        "Ngày mượn: " + record.getBorrowDate() + "\n" +
                        "Hạn trả: " + record.getDueDate() + "\n" +
                        "Ngày trả: " + (record.getReturnDate() != null ? record.getReturnDate() : "Chưa trả") + "\n" +
                        "Trạng thái: " + record.getStatus().getDescription() + "\n" +
                        "Đã gia hạn: " + record.getRenewalCount() + " lần"
        );
        alert.showAndWait();
    }
}

// Book Search View for Readers
class BookSearchView {
    private LibraryService libraryService;
    private TableView<Book> bookTable;
    private ObservableList<Book> bookData;
    private TextField searchField;
    private ComboBox<String> searchTypeCombo;

    public BookSearchView(LibraryService libraryService) {
        this.libraryService = libraryService;
        this.bookData = FXCollections.observableArrayList();
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Search bar
        HBox searchBar = createSearchBar();

        // Results table
        bookTable = createBookTable();
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        view.getChildren().addAll(searchBar, bookTable);

        return view;
    }

    private HBox createSearchBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Tìm kiếm:");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        searchTypeCombo = new ComboBox<>();
        searchTypeCombo.getItems().addAll("Tên sách", "Tác giả", "ISBN", "Thể loại");
        searchTypeCombo.setValue("Tên sách");
        searchTypeCombo.setPrefWidth(120);

        searchField = new TextField();
        searchField.setPromptText("Nhập từ khóa tìm kiếm...");
        searchField.setPrefWidth(400);

        Button searchBtn = new Button("🔍 Tìm");
        searchBtn.getStyleClass().add("primary-button");
        searchBtn.setOnAction(e -> performSearch());

        Button showAllBtn = new Button("📚 Hiển thị tất cả");
        showAllBtn.getStyleClass().add("secondary-button");
        showAllBtn.setOnAction(e -> showAllBooks());

        bar.getChildren().addAll(label, searchTypeCombo, searchField, searchBtn, showAllBtn);

        // Enter to search
        searchField.setOnAction(e -> performSearch());

        return bar;
    }

    private TableView<Book> createBookTable() {
        TableView<Book> table = new TableView<>();
        table.setItems(bookData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Title
        TableColumn<Book, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        // Author
        TableColumn<Book, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(180);

        // Category
        TableColumn<Book, String> categoryCol = new TableColumn<>("Thể loại");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        // Available
        TableColumn<Book, String> availableCol = new TableColumn<>("Có sẵn");
        availableCol.setCellValueFactory(data -> {
            Book book = data.getValue();
            String text = book.getAvailableCopies() + "/" + book.getTotalCopies();
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        availableCol.setPrefWidth(100);

        // Status
        TableColumn<Book, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellFactory(col -> new TableCell<Book, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Book book = getTableRow().getItem();
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");

                    if (book.getAvailableCopies() == 0) {
                        badge.setText("Hết sách");
                        badge.getStyleClass().add("badge-danger");
                    } else {
                        badge.setText("Còn sách");
                        badge.getStyleClass().add("badge-success");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(120);

        table.getColumns().addAll(titleCol, authorCol, categoryCol, availableCol, statusCol);

        // Double click to view details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showBookDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            showError("Vui lòng nhập từ khóa!");
            return;
        }

        String searchType = searchTypeCombo.getValue();
        List<Book> results = null;

        switch (searchType) {
            case "Tên sách":
                results = libraryService.searchByTitle(keyword);
                break;
            case "Tác giả":
                results = libraryService.searchByAuthor(keyword);
                break;
            case "ISBN":
                Book book = libraryService.searchByISBN(keyword);
                results = book != null ? List.of(book) : List.of();
                break;
            case "Thể loại":
                results = libraryService.searchByCategory(keyword);
                break;
        }

        bookData.setAll(results);

        if (results.isEmpty()) {
            showInfo("Không tìm thấy sách nào!");
        }
    }

    private void showAllBooks() {
        bookData.setAll(libraryService.getBooks());
    }

    private void showBookDetails(Book book) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết sách");
        alert.setHeaderText(book.getTitle());
        alert.setContentText(
                "ISBN: " + book.getISBN() + "\n" +
                        "Tác giả: " + book.getAuthor() + "\n" +
                        "Thể loại: " + book.getCategory() + "\n" +
                        "Nhà xuất bản: " + (book.getPublisher() != null ? book.getPublisher() : "N/A") + "\n" +
                        "Năm xuất bản: " + (book.getPublishYear() > 0 ? book.getPublishYear() : "N/A") + "\n" +
                        "Tổng số: " + book.getTotalCopies() + "\n" +
                        "Còn lại: " + book.getAvailableCopies()
        );
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

// My Borrowed Books View
class MyBorrowedBooksView {
    private LibraryService libraryService;
    private Reader currentReader;
    private TableView<BorrowRecord> borrowTable;
    private ObservableList<BorrowRecord> borrowData;

    public MyBorrowedBooksView(LibraryService libraryService, Reader currentReader) {
        this.libraryService = libraryService;
        this.currentReader = currentReader;

        List<BorrowRecord> activeBorrows = libraryService.getBorrowHistory(currentReader.getId())
                .stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                .collect(Collectors.toList());

        this.borrowData = FXCollections.observableArrayList(activeBorrows);
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Info card
        HBox infoCard = createInfoCard();

        // Borrowed books table
        borrowTable = createBorrowTable();
        VBox.setVgrow(borrowTable, Priority.ALWAYS);

        // Action buttons
        HBox actionBar = createActionBar();

        view.getChildren().addAll(infoCard, borrowTable, actionBar);

        return view;
    }

    private HBox createInfoCard() {
        HBox card = new HBox(30);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label borrowingLabel = new Label("Đang mượn: " + borrowData.size() + "/" +
                currentReader.getMembershipType().getBorrowLimit());
        borrowingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        long overdue = borrowData.stream().filter(BorrowRecord::isOverdue).count();
        Label overdueLabel = new Label("Quá hạn: " + overdue);
        overdueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; " +
                (overdue > 0 ? "-fx-text-fill: #ef4444;" : "-fx-text-fill: #22c55e;"));

        card.getChildren().addAll(borrowingLabel, overdueLabel);

        return card;
    }

    private TableView<BorrowRecord> createBorrowTable() {
        TableView<BorrowRecord> table = new TableView<>();
        table.setItems(borrowData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Book Title
        TableColumn<BorrowRecord, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getBook().getTitle()));
        titleCol.setPrefWidth(250);

        // Borrow Date
        TableColumn<BorrowRecord, LocalDate> borrowCol = new TableColumn<>("Ngày mượn");
        borrowCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowCol.setPrefWidth(120);

        // Due Date
        TableColumn<BorrowRecord, LocalDate> dueCol = new TableColumn<>("Hạn trả");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueCol.setPrefWidth(120);

        // Days Left
        TableColumn<BorrowRecord, String> daysLeftCol = new TableColumn<>("Còn lại");
        daysLeftCol.setCellValueFactory(data -> {
            BorrowRecord record = data.getValue();
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), record.getDueDate());
            return new javafx.beans.property.SimpleStringProperty(daysLeft + " ngày");
        });
        daysLeftCol.setPrefWidth(100);

        // Renewal Count
        TableColumn<BorrowRecord, Integer> renewalCol = new TableColumn<>("Đã gia hạn");
        renewalCol.setCellValueFactory(new PropertyValueFactory<>("renewalCount"));
        renewalCol.setPrefWidth(100);

        // Status
        TableColumn<BorrowRecord, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellFactory(col -> new TableCell<BorrowRecord, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    BorrowRecord record = getTableRow().getItem();
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");

                    if (record.isOverdue()) {
                        badge.setText("Quá hạn");
                        badge.getStyleClass().add("badge-danger");
                    } else if (record.canRenew()) {
                        badge.setText("Có thể gia hạn");
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.setText("Không thể gia hạn");
                        badge.getStyleClass().add("badge-warning");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        statusCol.setPrefWidth(150);

        table.getColumns().addAll(titleCol, borrowCol, dueCol, daysLeftCol, renewalCol, statusCol);

        return table;
    }

    private HBox createActionBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_RIGHT);

        Button renewBtn = new Button("🔄 Gia hạn");
        renewBtn.getStyleClass().add("primary-button");
        renewBtn.setOnAction(e -> renewSelected());

        Button refreshBtn = new Button("↻ Làm mới");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(renewBtn, refreshBtn);

        return bar;
    }

    private void renewSelected() {
        BorrowRecord selected = borrowTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn sách cần gia hạn!");
            return;
        }

        if (!selected.canRenew()) {
            showError("Sách này không thể gia hạn (đã quá hạn hoặc đã gia hạn tối đa 2 lần)!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog("7");
        dialog.setTitle("Gia hạn sách");
        dialog.setHeaderText("Gia hạn: " + selected.getBook().getTitle());
        dialog.setContentText("Số ngày gia hạn (1-14):");

        dialog.showAndWait().ifPresent(days -> {
            try {
                int numDays = Integer.parseInt(days);
                if (numDays < 1 || numDays > 14) {
                    showError("Số ngày phải từ 1-14!");
                    return;
                }

                if (libraryService.extendBorrow(selected.getRecordId(), numDays)) {
                    showSuccess("Gia hạn thành công!");
                    refreshTable();
                }
            } catch (NumberFormatException ex) {
                showError("Số ngày không hợp lệ!");
            }
        });
    }

    private void refreshTable() {
        List<BorrowRecord> activeBorrows = libraryService.getBorrowHistory(currentReader.getId())
                .stream()
                .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                .collect(Collectors.toList());
        borrowData.setAll(activeBorrows);
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }
}

// Borrow History View
class BorrowHistoryView {
    private LibraryService libraryService;
    private Reader currentReader;
    private TableView<BorrowRecord> historyTable;
    private ObservableList<BorrowRecord> historyData;
    private List<BorrowRecord> allRecords;

    public BorrowHistoryView(LibraryService libraryService, Reader currentReader) {
        this.libraryService = libraryService;
        this.currentReader = currentReader;
        this.allRecords = libraryService.getBorrowHistory(currentReader.getId());
        this.historyData = FXCollections.observableArrayList(allRecords);
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Filter bar
        HBox filterBar = createFilterBar();

        // History table
        historyTable = createHistoryTable();
        VBox.setVgrow(historyTable, Priority.ALWAYS);

        view.getChildren().addAll(filterBar, historyTable);

        return view;
    }

    private HBox createFilterBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("Lọc:");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("Tất cả", "Đang mượn", "Đã trả", "Quá hạn");
        filterCombo.setValue("Tất cả");
        filterCombo.setOnAction(e -> filterHistory(filterCombo.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label totalLabel = new Label("Tổng: " + historyData.size() + " lượt");
        totalLabel.setStyle("-fx-font-size: 14px;");

        bar.getChildren().addAll(label, filterCombo, spacer, totalLabel);

        return bar;
    }

    private TableView<BorrowRecord> createHistoryTable() {
        TableView<BorrowRecord> table = new TableView<>();
        table.setItems(historyData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Book Title
        TableColumn<BorrowRecord, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getBook().getTitle()));
        titleCol.setPrefWidth(250);

        // Borrow Date
        TableColumn<BorrowRecord, LocalDate> borrowCol = new TableColumn<>("Ngày mượn");
        borrowCol.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowCol.setPrefWidth(120);

        // Due Date
        TableColumn<BorrowRecord, LocalDate> dueCol = new TableColumn<>("Hạn trả");
        dueCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        dueCol.setPrefWidth(120);

        // Return Date
        TableColumn<BorrowRecord, String> returnCol = new TableColumn<>("Ngày trả");
        returnCol.setCellValueFactory(data -> {
            LocalDate returnDate = data.getValue().getReturnDate();
            String text = returnDate != null ? returnDate.toString() : "-";
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        returnCol.setPrefWidth(120);

        // Fine
        TableColumn<BorrowRecord, String> fineCol = new TableColumn<>("Phí phạt");
        fineCol.setCellValueFactory(data -> {
            double fine = data.getValue().getFine();
            String text = fine > 0 ? String.format("%,.0f VND", fine) : "-";
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        fineCol.setPrefWidth(120);

        // Status
        TableColumn<BorrowRecord, BorrowStatus> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<BorrowRecord, BorrowStatus>() {
            @Override
            protected void updateItem(BorrowStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");

                    switch (item) {
                        case BORROWED:
                            badge.setText("Đang mượn");
                            badge.getStyleClass().add("badge-info");
                            break;
                        case RETURNED:
                            badge.setText("Đã trả");
                            badge.getStyleClass().add("badge-success");
                            break;
                        case OVERDUE:
                            badge.setText("Quá hạn");
                            badge.getStyleClass().add("badge-danger");
                            break;
                        case LOST:
                            badge.setText("Mất sách");
                            badge.getStyleClass().add("badge-warning");
                            break;
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        table.getColumns().addAll(titleCol, borrowCol, dueCol, returnCol, fineCol, statusCol);

        // Double click for details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showRecordDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void filterHistory(String filter) {
        switch (filter) {
            case "Tất cả":
                historyData.setAll(allRecords);
                break;
            case "Đang mượn":
                historyData.setAll(allRecords.stream()
                        .filter(r -> r.getStatus() == BorrowStatus.BORROWED)
                        .collect(Collectors.toList()));
                break;
            case "Đã trả":
                historyData.setAll(allRecords.stream()
                        .filter(r -> r.getStatus() == BorrowStatus.RETURNED)
                        .collect(Collectors.toList()));
                break;
            case "Quá hạn":
                historyData.setAll(allRecords.stream()
                        .filter(BorrowRecord::isOverdue)
                        .collect(Collectors.toList()));
                break;
        }
    }

    private void showRecordDetails(BorrowRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết phiếu mượn");
        alert.setHeaderText(record.getBook().getTitle());
        alert.setContentText(
                "Mã phiếu: " + record.getRecordId() + "\n" +
                        "ISBN: " + record.getBook().getISBN() + "\n" +
                        "Ngày mượn: " + record.getBorrowDate() + "\n" +
                        "Hạn trả: " + record.getDueDate() + "\n" +
                        "Ngày trả: " + (record.getReturnDate() != null ? record.getReturnDate() : "Chưa trả") + "\n" +
                        "Trạng thái: " + record.getStatus().getDescription() + "\n" +
                        "Đã gia hạn: " + record.getRenewalCount() + " lần\n" +
                        "Phí phạt: " + (record.getFine() > 0 ? String.format("%,.0f VND", record.getFine()) : "Không")
        );
        alert.showAndWait();
    }
}
