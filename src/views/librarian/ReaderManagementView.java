package views.librarian;

import database.impl.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.enums.MembershipType;
import models.people.Reader;
import models.people.User;

public class ReaderManagementView {

    private LibraryService libraryService;
    private User currentUser;
    private TableView<Reader> readerTable;
    private ObservableList<Reader> readerData;
    private TextField searchField;

    public ReaderManagementView(LibraryService libraryService, User currentUser) {
        this.libraryService = libraryService;
        this.currentUser = currentUser;
        this.readerData = FXCollections.observableArrayList(libraryService.getReaders());
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Action bar
        HBox actionBar = createActionBar();

        // Reader table
        readerTable = createReaderTable();
        VBox.setVgrow(readerTable, Priority.ALWAYS);

        view.getChildren().addAll(actionBar, readerTable);

        return view;
    }

    private HBox createActionBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        // Search
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 5px 15px;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Tìm kiếm độc giả...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, old, newVal) -> filterReaders(newVal));

        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons
        Button addBtn = new Button("➕ Đăng ký độc giả");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showRegisterDialog());

        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(searchBox, spacer, addBtn, refreshBtn);

        return bar;
    }

    private TableView<Reader> createReaderTable() {
        TableView<Reader> table = new TableView<>();
        table.setItems(readerData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // ID
        TableColumn<Reader, String> idCol = new TableColumn<>("Mã ĐG");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        // Name
        TableColumn<Reader, String> nameCol = new TableColumn<>("Họ tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        // Email
        TableColumn<Reader, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        // Phone
        TableColumn<Reader, String> phoneCol = new TableColumn<>("Số ĐT");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        idCol.setPrefWidth(120);

        // Membership Type
        TableColumn<Reader, MembershipType> typeCol = new TableColumn<>("Loại thẻ");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("membershipType"));
        typeCol.setPrefWidth(120);
        typeCol.setCellFactory(col -> new TableCell<Reader, MembershipType>() {
            @Override
            protected void updateItem(MembershipType item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.name());
                    badge.getStyleClass().add("badge");

                    switch (item) {
                        case PREMIUM: badge.getStyleClass().add("badge-warning"); break;
                        case STUDENT: badge.getStyleClass().add("badge-info"); break;
                        case SENIOR: badge.getStyleClass().add("badge-success"); break;
                        default: badge.getStyleClass().add("badge-info");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Current Borrows
        TableColumn<Reader, String> borrowCol = new TableColumn<>("Đang mượn");
        borrowCol.setCellValueFactory(data -> {
            Reader reader = data.getValue();
            String text = reader.getCurrentBorrows() + "/" + reader.getMembershipType().getBorrowLimit();
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        borrowCol.setPrefWidth(100);

        // Total Borrowed
        TableColumn<Reader, Integer> totalCol = new TableColumn<>("Tổng mượn");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalBorrowed"));
        totalCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, typeCol, borrowCol, totalCol);

        // Double click for details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showReaderDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void filterReaders(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            readerData.setAll(libraryService.getReaders());
        } else {
            String lower = keyword.toLowerCase();
            readerData.setAll(
                    libraryService.getReaders().stream()
                            .filter(reader ->
                                    reader.getName().toLowerCase().contains(lower) ||
                                            reader.getId().toLowerCase().contains(lower) ||
                                            reader.getEmail().toLowerCase().contains(lower)
                            )
                            .toList()
            );
        }
    }

    private void showRegisterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đăng ký độc giả mới");
        dialog.setHeaderText("Thông tin độc giả");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        ComboBox<MembershipType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(MembershipType.values());
        typeCombo.setValue(MembershipType.STANDARD);
        PasswordField passwordField = new PasswordField();
        passwordField.setText("reader123");

        int row = 0;
        grid.add(new Label("Họ tên:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);
        grid.add(new Label("Số điện thoại:"), 0, row);
        grid.add(phoneField, 1, row++);
        grid.add(new Label("Loại thẻ:"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label("Mật khẩu:"), 0, row);
        grid.add(passwordField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String password = passwordField.getText();

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    showError("Vui lòng điền đầy đủ thông tin!");
                    return;
                }

                Reader reader = new Reader(name, email, phone, typeCombo.getValue());

                if (libraryService.registerReaderWithAccount(reader, password)) {
                    refreshTable();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Đăng ký thành công");
                    success.setHeaderText("Thông tin đăng nhập");
                    success.setContentText(
                            "Mã độc giả: " + reader.getId() + "\n" +
                                    "Username: " + email + "\n" +
                                    "Password: " + password + "\n\n" +
                                    "Vui lòng lưu lại thông tin này!"
                    );
                    success.showAndWait();
                } else {
                    showError("Đăng ký thất bại!");
                }
            }
        });
    }

    private void showReaderDetails(Reader reader) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết độc giả");
        alert.setHeaderText(reader.getName());
        alert.setContentText(reader.getInfo());
        alert.showAndWait();
    }

    private void refreshTable() {
        readerData.setAll(libraryService.getReaders());
        searchField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
