package views.librarian;

import database.impl.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.books.Book;
import models.books.Magazine;
import models.books.ReferenceBook;
import models.books.TextBook;
import models.people.User;

public class BookManagementView {

    private LibraryService libraryService;
    private User currentUser;
    private TableView<Book> bookTable;
    private ObservableList<Book> bookData;
    private TextField searchField;

    public BookManagementView(LibraryService libraryService, User currentUser) {
        this.libraryService = libraryService;
        this.currentUser = currentUser;
        this.bookData = FXCollections.observableArrayList(libraryService.getBooks());
    }

    public VBox createView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(0));

        // Search and action bar
        HBox actionBar = createActionBar();

        // Books table
        bookTable = createBookTable();
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        view.getChildren().addAll(actionBar, bookTable);

        return view;
    }

    private HBox createActionBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0));

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 5px 15px;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm sách theo tên, tác giả, ISBN...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, old, newVal) -> filterBooks(newVal));

        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        Button addBtn = new Button("➕ Nhập sách");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showAddBookDialog());

        Button deleteBtn = new Button("🗑️ Xóa");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> deleteSelectedBook());

        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setTooltip(new Tooltip("Làm mới"));
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(searchBox, spacer, addBtn, deleteBtn, refreshBtn);

        return bar;
    }

    private TableView<Book> createBookTable() {
        TableView<Book> table = new TableView<>();
        table.setItems(bookData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ISBN column
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        isbnCol.setPrefWidth(130);

        // Title column
        TableColumn<Book, String> titleCol = new TableColumn<>("Tên sách");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(250);

        // Author column
        TableColumn<Book, String> authorCol = new TableColumn<>("Tác giả");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorCol.setPrefWidth(180);

        // Category column
        TableColumn<Book, String> categoryCol = new TableColumn<>("Thể loại");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(150);

        // Available column
        TableColumn<Book, String> availableCol = new TableColumn<>("Có sẵn");
        availableCol.setCellValueFactory(data -> {
            Book book = data.getValue();
            String text = book.getAvailableCopies() + "/" + book.getTotalCopies();
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        availableCol.setPrefWidth(100);

        // Status badge column
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
                    } else if (book.getAvailableCopies() < 3) {
                        badge.setText("Sắp hết");
                        badge.getStyleClass().add("badge-warning");
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

        table.getColumns().addAll(isbnCol, titleCol, authorCol, categoryCol, availableCol, statusCol);

        // Double click to view details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showBookDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void filterBooks(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            bookData.setAll(libraryService.getBooks());
        } else {
            String lower = keyword.toLowerCase();
            bookData.setAll(
                    libraryService.getBooks().stream()
                            .filter(book ->
                                    book.getTitle().toLowerCase().contains(lower) ||
                                            book.getAuthor().toLowerCase().contains(lower) ||
                                            book.getISBN().toLowerCase().contains(lower) ||
                                            (book.getCategory() != null && book.getCategory().toLowerCase().contains(lower))
                            )
                            .toList()
            );
        }
    }

    private void showAddBookDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Nhập sách vào kho");
        dialog.setHeaderText("Thêm sách mới hoặc cập nhật số lượng");

        // Content
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField isbnField = new TextField();
        isbnField.setPromptText("978-0-13-468599-1");

        TextField quantityField = new TextField();
        quantityField.setPromptText("5");

        grid.add(new Label("ISBN:"), 0, 0);
        grid.add(isbnField, 1, 0);
        grid.add(new Label("Số lượng:"), 0, 1);
        grid.add(quantityField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String isbn = isbnField.getText().trim();
                    int quantity = Integer.parseInt(quantityField.getText().trim());

                    String performedBy = currentUser.getLinkedEntityId() != null ?
                            currentUser.getLinkedEntityId() : "SYSTEM";

                    Book existing = libraryService.addOrUpdateBookInventory(isbn, quantity, performedBy);

                    if (existing != null) {
                        // Updated existing book
                        refreshTable();
                        showSuccess("Đã cập nhật số lượng sách!");
                    } else {
                        // Need full info for new book
                        showAddNewBookDialog(isbn, quantity, performedBy);
                    }

                } catch (NumberFormatException ex) {
                    showError("Số lượng không hợp lệ!");
                }
            }
        });
    }

    private void showAddNewBookDialog(String isbn, int quantity, String performedBy) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm sách mới");
        dialog.setHeaderText("ISBN chưa tồn tại - Nhập thông tin đầy đủ");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Book type
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Sách giáo khoa", "Sách tham khảo", "Tạp chí");
        typeCombo.setValue("Sách tham khảo");

        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField publisherField = new TextField();
        TextField yearField = new TextField();
        TextField priceField = new TextField();

        // Type-specific fields
        TextField specificField1 = new TextField();
        TextField specificField2 = new TextField();
        Label specific1Label = new Label("Chủ đề:");
        Label specific2Label = new Label();

        typeCombo.setOnAction(e -> {
            String type = typeCombo.getValue();
            if ("Sách giáo khoa".equals(type)) {
                specific1Label.setText("Môn học:");
                specific2Label.setText("Lớp:");
                specificField2.setVisible(true);
                specific2Label.setVisible(true);
            } else if ("Tạp chí".equals(type)) {
                specific1Label.setText("Số phát hành:");
                specificField2.setVisible(false);
                specific2Label.setVisible(false);
            } else {
                specific1Label.setText("Chủ đề:");
                specificField2.setVisible(false);
                specific2Label.setVisible(false);
            }
        });

        int row = 0;
        grid.add(new Label("Loại sách:"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label("Tên sách:"), 0, row);
        grid.add(titleField, 1, row++);
        grid.add(new Label("Tác giả:"), 0, row);
        grid.add(authorField, 1, row++);
        grid.add(specific1Label, 0, row);
        grid.add(specificField1, 1, row++);
        grid.add(specific2Label, 0, row);
        grid.add(specificField2, 1, row++);
        grid.add(new Label("Nhà XB:"), 0, row);
        grid.add(publisherField, 1, row++);
        grid.add(new Label("Năm XB:"), 0, row);
        grid.add(yearField, 1, row++);
        grid.add(new Label("Giá:"), 0, row);
        grid.add(priceField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String type = typeCombo.getValue();
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();

                    Book book = null;

                    if ("Sách giáo khoa".equals(type)) {
                        String subject = specificField1.getText().trim();
                        int grade = Integer.parseInt(specificField2.getText().trim());
                        book = new TextBook(isbn, title, author, quantity, subject, grade);
                    } else if ("Tạp chí".equals(type)) {
                        int issueNumber = Integer.parseInt(specificField1.getText().trim());
                        book = new Magazine(isbn, title, author, quantity, issueNumber);
                    } else {
                        String topic = specificField1.getText().trim();
                        book = new ReferenceBook(isbn, title, author, quantity, topic);
                    }

                    // Optional fields
                    if (!publisherField.getText().isEmpty()) {
                        book.setPublisher(publisherField.getText().trim());
                    }
                    if (!yearField.getText().isEmpty()) {
                        book.setPublishYear(Integer.parseInt(yearField.getText().trim()));
                    }
                    if (!priceField.getText().isEmpty()) {
                        book.setPrice(Double.parseDouble(priceField.getText().trim()));
                    }

                    libraryService.addBook(book, performedBy);
                    refreshTable();
                    showSuccess("Đã thêm sách mới thành công!");

                } catch (Exception ex) {
                    showError("Lỗi: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteSelectedBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn sách cần xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa sách: " + selected.getTitle());
        confirm.setContentText("Bạn có chắc muốn xóa sách này?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (libraryService.removeBook(selected.getISBN())) {
                    refreshTable();
                    showSuccess("Đã xóa sách!");
                } else {
                    showError("Không thể xóa sách (có thể đang được mượn)!");
                }
            }
        });
    }

    private void showBookDetails(Book book) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết sách");
        alert.setHeaderText(book.getTitle());
        alert.setContentText(book.getInfo());
        alert.showAndWait();
    }

    private void refreshTable() {
        bookData.setAll(libraryService.getBooks());
        searchField.clear();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
