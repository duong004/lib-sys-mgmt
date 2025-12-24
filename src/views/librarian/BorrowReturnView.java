package views.librarian;

import database.impl.LibraryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.BorrowRecord;
import models.people.User;

public class BorrowReturnView {

    private LibraryService libraryService;
    private User currentUser;

    public BorrowReturnView(LibraryService libraryService, User currentUser) {
        this.libraryService = libraryService;
        this.currentUser = currentUser;
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Two cards side by side
        HBox cardsRow = new HBox(20);
        cardsRow.setAlignment(Pos.TOP_CENTER);

        // Borrow card
        VBox borrowCard = createBorrowCard();
        HBox.setHgrow(borrowCard, Priority.ALWAYS);

        // Return card
        VBox returnCard = createReturnCard();
        HBox.setHgrow(returnCard, Priority.ALWAYS);

        cardsRow.getChildren().addAll(borrowCard, returnCard);

        // Active borrows table
        VBox activeBorrowsSection = createActiveBorrowsSection();

        view.getChildren().addAll(cardsRow, activeBorrowsSection);

        return view;
    }

    private VBox createBorrowCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("📤 Mượn sách");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        TextField readerIdField = new TextField();
        readerIdField.setPromptText("R001");
        readerIdField.getStyleClass().add("dashboard-input");

        TextField isbnField = new TextField();
        isbnField.setPromptText("978-0-13-468599-1");
        isbnField.getStyleClass().add("dashboard-input");

        grid.add(new Label("Mã độc giả:"), 0, 0);
        grid.add(readerIdField, 1, 0);
        grid.add(new Label("ISBN sách:"), 0, 1);
        grid.add(isbnField, 1, 1);

        Button borrowBtn = new Button("Xác nhận mượn");
        borrowBtn.getStyleClass().add("primary-button");
        borrowBtn.setMaxWidth(Double.MAX_VALUE);
        borrowBtn.setOnAction(e -> {
            String readerId = readerIdField.getText().trim();
            String isbn = isbnField.getText().trim();

            if (readerId.isEmpty() || isbn.isEmpty()) {
                showError("Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            BorrowRecord record = libraryService.borrowBook(readerId, isbn);
            if (record != null) {
                readerIdField.clear();
                isbnField.clear();
                showSuccess("Mượn sách thành công!");
            }
        });

        card.getChildren().addAll(title, grid, borrowBtn);

        return card;
    }

    private VBox createReturnCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.TOP_LEFT);

        Label title = new Label("📥 Trả sách");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        TextField recordIdField = new TextField();
        recordIdField.setPromptText("BR1234567890");
        recordIdField.getStyleClass().add("dashboard-input");

        grid.add(new Label("Mã phiếu mượn:"), 0, 0);
        grid.add(recordIdField, 1, 0);

        Button returnBtn = new Button("Xác nhận trả");
        returnBtn.getStyleClass().add("primary-button");
        returnBtn.setMaxWidth(Double.MAX_VALUE);
        returnBtn.setOnAction(e -> {
            String recordId = recordIdField.getText().trim();

            if (recordId.isEmpty()) {
                showError("Vui lòng nhập mã phiếu mượn!");
                return;
            }

            libraryService.returnBook(recordId);
            recordIdField.clear();
        });

        Button extendBtn = new Button("Gia hạn");
        extendBtn.getStyleClass().add("secondary-button");
        extendBtn.setMaxWidth(Double.MAX_VALUE);
        extendBtn.setOnAction(e -> {
            String recordId = recordIdField.getText().trim();

            if (recordId.isEmpty()) {
                showError("Vui lòng nhập mã phiếu mượn!");
                return;
            }

            TextInputDialog dialog = new TextInputDialog("7");
            dialog.setTitle("Gia hạn sách");
            dialog.setHeaderText("Gia hạn cho phiếu: " + recordId);
            dialog.setContentText("Số ngày gia hạn:");

            dialog.showAndWait().ifPresent(days -> {
                try {
                    libraryService.extendBorrow(recordId, Integer.parseInt(days));
                    recordIdField.clear();
                } catch (NumberFormatException ex) {
                    showError("Số ngày không hợp lệ!");
                }
            });
        });

        card.getChildren().addAll(title, grid, returnBtn, extendBtn);

        return card;
    }

    private VBox createActiveBorrowsSection() {
        VBox section = new VBox(15);
        section.getStyleClass().add("card");

        Label title = new Label("📋 Danh sách đang mượn");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<String> borrowList = new ListView<>();
        borrowList.setPrefHeight(300);

        // Load active borrows
        ObservableList<String> items = FXCollections.observableArrayList();
        libraryService.getBorrowRecords().stream()
                .filter(r -> r.getStatus() == models.enums.BorrowStatus.BORROWED)
                .forEach(r -> items.add(r.getInfo()));
        borrowList.setItems(items);

        section.getChildren().addAll(title, borrowList);

        return section;
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
