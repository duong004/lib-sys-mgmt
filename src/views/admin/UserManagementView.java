package views.admin;

import database.dao.UserDAO;
import database.impl.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.enums.MembershipType;
import models.enums.UserRole;
import models.people.*;

public class UserManagementView {

    private LibraryService libraryService;
    private UserDAO userDAO;
    private User currentUser;
    private TableView<User> userTable;
    private ObservableList<User> userData;
    private TextField searchField;

    public UserManagementView(LibraryService libraryService, UserDAO userDAO, User currentUser) {
        this.libraryService = libraryService;
        this.userDAO = userDAO;
        this.currentUser = currentUser;
        try {
            this.userData = FXCollections.observableArrayList(userDAO.findAll());
        } catch (Exception e) {
            this.userData = FXCollections.observableArrayList();
        }
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Action bar
        HBox actionBar = createActionBar();

        // User table
        userTable = createUserTable();
        VBox.setVgrow(userTable, Priority.ALWAYS);

        view.getChildren().addAll(actionBar, userTable);

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
        searchField.setPromptText("Tìm kiếm người dùng...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, old, newVal) -> filterUsers(newVal));

        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons
        Button addBtn = new Button("➕ Tạo tài khoản");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> showCreateUserDialog());

        Button toggleBtn = new Button("🔒 Khóa/Mở");
        toggleBtn.getStyleClass().add("secondary-button");
        toggleBtn.setOnAction(e -> toggleUserStatus());

        Button passwordBtn = new Button("🔑 Đổi mật khẩu");
        passwordBtn.getStyleClass().add("secondary-button");
        passwordBtn.setOnAction(e -> changePassword());

        Button refreshBtn = new Button("🔄");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> refreshTable());

        bar.getChildren().addAll(searchBox, spacer, addBtn, toggleBtn, passwordBtn, refreshBtn);

        return bar;
    }

    private TableView<User> createUserTable() {
        TableView<User> table = new TableView<>();
        table.setItems(userData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Username
        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(180);

        // Full Name
        TableColumn<User, String> nameCol = new TableColumn<>("Họ tên");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);

        // Email
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);

        // Role
        TableColumn<User, UserRole> roleCol = new TableColumn<>("Vai trò");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(col -> new TableCell<User, UserRole>() {
            @Override
            protected void updateItem(UserRole item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item.getDisplayName());
                    badge.getStyleClass().add("badge");

                    switch (item) {
                        case ADMIN: badge.getStyleClass().add("badge-danger"); break;
                        case LIBRARIAN: badge.getStyleClass().add("badge-warning"); break;
                        case READER: badge.getStyleClass().add("badge-info"); break;
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Status
        TableColumn<User, Boolean> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");

                    if (item) {
                        badge.setText("Hoạt động");
                        badge.getStyleClass().add("badge-success");
                    } else {
                        badge.setText("Khóa");
                        badge.getStyleClass().add("badge-danger");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Last Login
        TableColumn<User, String> lastLoginCol = new TableColumn<>("Đăng nhập cuối");
        lastLoginCol.setCellValueFactory(data -> {
            User user = data.getValue();
            String text = user.getLastLogin() != null ?
                    user.getLastLogin().toString() : "Chưa đăng nhập";
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        lastLoginCol.setPrefWidth(180);

        table.getColumns().addAll(usernameCol, nameCol, emailCol, roleCol, statusCol, lastLoginCol);

        // Double click for details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showUserDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void filterUsers(String keyword) {
        try {
            if (keyword == null || keyword.isEmpty()) {
                userData.setAll(userDAO.findAll());
            } else {
                String lower = keyword.toLowerCase();
                userData.setAll(
                        userDAO.findAll().stream()
                                .filter(user ->
                                        user.getUsername().toLowerCase().contains(lower) ||
                                                user.getFullName().toLowerCase().contains(lower) ||
                                                user.getEmail().toLowerCase().contains(lower)
                                )
                                .toList()
                );
            }
        } catch (Exception e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void showCreateUserDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tạo tài khoản mới");
        dialog.setHeaderText("Thông tin người dùng");

        // Tab pane for role selection
        TabPane tabPane = new TabPane();

        // Admin/Librarian tab
        Tab staffTab = new Tab("Admin/Librarian");
        staffTab.setClosable(false);
        staffTab.setContent(createStaffForm());

        // Reader tab
        Tab readerTab = new Tab("Reader (Độc giả)");
        readerTab.setClosable(false);
        readerTab.setContent(createReaderForm());

        tabPane.getTabs().addAll(staffTab, readerTab);

        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
                if (selectedIndex == 0) {
                    createStaffUser((VBox) staffTab.getContent());
                } else {
                    createReaderUser((VBox) readerTab.getContent());
                }
            }
        });
    }

    private VBox createStaffForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        ComboBox<UserRole> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll(UserRole.ADMIN, UserRole.LIBRARIAN);
        roleCombo.setValue(UserRole.LIBRARIAN);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField positionField = new TextField();
        positionField.setPromptText("Vd: Thủ thư trưởng, Quản lý kho...");
        positionField.setText("Thủ thư");

        Label positionLabel = new Label("Chức vụ:");

        // Show/hide position based on role
        positionLabel.setVisible(roleCombo.getValue() == UserRole.LIBRARIAN);
        positionField.setVisible(roleCombo.getValue() == UserRole.LIBRARIAN);
        positionField.setManaged(roleCombo.getValue() == UserRole.LIBRARIAN);

        roleCombo.setOnAction(e -> {
            boolean isLibrarian = roleCombo.getValue() == UserRole.LIBRARIAN;
            positionLabel.setVisible(isLibrarian);
            positionField.setVisible(isLibrarian);
            positionField.setManaged(isLibrarian);
        });

        form.getChildren().addAll(
                new Label("Vai trò:"), roleCombo,
                new Label("Username:"), usernameField,
                new Label("Mật khẩu:"), passwordField,
                new Label("Họ tên:"), nameField,
                new Label("Email:"), emailField,
                positionLabel, positionField
        );

        // Store all fields including position
        form.setUserData(new Object[]{roleCombo, usernameField, passwordField, nameField, emailField, positionField});

        return form;
    }

    private VBox createReaderForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));

        Label info = new Label("Tạo tài khoản Reader sẽ tự động đăng ký độc giả mới");
        info.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic;");

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        TextField phoneField = new TextField();
        PasswordField passwordField = new PasswordField();
        passwordField.setText("reader123");

        ComboBox<MembershipType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(MembershipType.values());
        typeCombo.setValue(MembershipType.STANDARD);

        form.getChildren().addAll(
                info,
                new Label("Họ tên:"), nameField,
                new Label("Email:"), emailField,
                new Label("Số điện thoại:"), phoneField,
                new Label("Loại thẻ:"), typeCombo,
                new Label("Mật khẩu:"), passwordField
        );

        form.setUserData(new Object[]{nameField, emailField, phoneField, typeCombo, passwordField});

        return form;
    }

    private void createStaffUser(VBox form) {
        Object[] fields = (Object[]) form.getUserData();
        UserRole role = ((ComboBox<UserRole>) fields[0]).getValue();
        String username = ((TextField) fields[1]).getText().trim();
        String password = ((PasswordField) fields[2]).getText();
        String name = ((TextField) fields[3]).getText().trim();
        String email = ((TextField) fields[4]).getText().trim();
        String position = ((TextField) fields[5]).getText().trim();  // NEW

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        try {
            if (role == UserRole.LIBRARIAN) {
                // Validate position for Librarian
                if (position.isEmpty()) {
                    position = "Thủ thư";
                }

                // Tạo Librarian object
                Librarian librarian = new Librarian(null, name, email, "0000000000", position);

                // Sử dụng LibraryService để tạo đồng bộ (Librarian + User)
                if (libraryService.registerLibrarianWithAccount(librarian, username, password)) {
                    refreshTable();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Tạo tài khoản thành công");
                    success.setHeaderText("Thông tin Librarian");
                    success.setContentText(
                            "✅ Đã tạo tài khoản và hồ sơ thủ thư\n\n" +
                                    "Mã thủ thư: " + librarian.getEmployeeId() + "\n" +
                                    "Username: " + username + "\n" +
                                    "Họ tên: " + name + "\n" +
                                    "Chức vụ: " + position + "\n\n" +
                                    "Vui lòng lưu lại thông tin này!"
                    );
                    success.showAndWait();
                } else {
                    showError("Tạo tài khoản Librarian thất bại!");
                }

            } else if (role == UserRole.ADMIN) {
                // Admin chỉ tạo User account, không cần entity
                User newUser = new User(username, password, UserRole.ADMIN, name, email);
                userDAO.save(newUser);
                refreshTable();
                showSuccess("Đã tạo tài khoản Admin: " + username);
            }

        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createReaderUser(VBox form) {
        Object[] fields = (Object[]) form.getUserData();
        String name = ((TextField) fields[0]).getText().trim();
        String email = ((TextField) fields[1]).getText().trim();
        String phone = ((TextField) fields[2]).getText().trim();
        MembershipType type = ((ComboBox<MembershipType>) fields[3]).getValue();
        String password = ((PasswordField) fields[4]).getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        Reader reader = new Reader(name, email, phone, type);

        if (libraryService.registerReaderWithAccount(reader, password)) {
            refreshTable();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Đăng ký thành công");
            success.setHeaderText("Thông tin đăng nhập");
            success.setContentText(
                    "Mã độc giả: " + reader.getId() + "\n" +
                            "Username: " + email + "\n" +
                            "Password: " + password
            );
            success.showAndWait();
        } else {
            showError("Đăng ký thất bại!");
        }
    }

    private void toggleUserStatus() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn người dùng!");
            return;
        }

        if (selected.getUsername().equals(currentUser.getUsername())) {
            showError("Không thể khóa tài khoản của chính mình!");
            return;
        }

        try {
            selected.setActive(!selected.isActive());
            userDAO.update(selected);
            refreshTable();
            showSuccess("Đã " + (selected.isActive() ? "mở khóa" : "khóa") + " tài khoản!");
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void changePassword() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn người dùng!");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đổi mật khẩu");
        dialog.setHeaderText("Đổi mật khẩu cho: " + selected.getUsername());
        dialog.setContentText("Mật khẩu mới:");

        dialog.showAndWait().ifPresent(newPassword -> {
            try {
                String hash = User.hashPassword(newPassword);
                userDAO.changePassword(selected.getUsername(), hash);
                showSuccess("Đã đổi mật khẩu!");
            } catch (Exception e) {
                showError("Lỗi: " + e.getMessage());
            }
        });
    }

    private void showUserDetails(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết người dùng");
        alert.setHeaderText(user.getFullName());
        alert.setContentText(user.getInfo());
        alert.showAndWait();
    }

    private void refreshTable() {
        try {
            userData.setAll(userDAO.findAll());
            searchField.clear();
        } catch (Exception e) {
            showError("Lỗi làm mới: " + e.getMessage());
        }
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
