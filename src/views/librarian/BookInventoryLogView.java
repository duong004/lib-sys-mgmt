package views.librarian;

import database.dao.BookInventoryLogDAO;
import database.impl.BookInventoryLogDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import models.BookInventoryLog;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class BookInventoryLogView {
    private BookInventoryLogDAO logDAO;
    private TableView<BookInventoryLog> logTable;
    private ObservableList<BookInventoryLog> logData;
    private TextField searchField;
    private ComboBox<String> filterCombo;

    public BookInventoryLogView() {
        this.logDAO = new BookInventoryLogDAOImpl();
        this.logData = FXCollections.observableArrayList();
        loadAllLogs();
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Action bar
        HBox actionBar = createActionBar();

        // Logs table
        logTable = createLogTable();
        VBox.setVgrow(logTable, Priority.ALWAYS);

        // Statistics card
        HBox statsCard = createStatisticsCard();

        view.getChildren().addAll(actionBar, statsCard, logTable);

        return view;
    }

    private HBox createActionBar() {
        HBox bar = new HBox(15);
        bar.setAlignment(Pos.CENTER_LEFT);

        // Filter dropdown
        Label filterLabel = new Label("Lọc:");
        filterLabel.setStyle("-fx-font-weight: bold;");

        filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll(
                "Tất cả",
                "Thêm mới",
                "Nhập thêm",
                "Xuất giảm"
        );
        filterCombo.setValue("Tất cả");
        filterCombo.setOnAction(e -> filterLogs());

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 5px 15px;");

        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size: 18px;");

        searchField = new TextField();
        searchField.setPromptText("Tìm theo ISBN...");
        searchField.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, old, newVal) -> searchByISBN(newVal));

        searchBox.getChildren().addAll(searchIcon, searchField);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Refresh button
        Button refreshBtn = new Button("🔄 Làm mới");
        refreshBtn.getStyleClass().add("secondary-button");
        refreshBtn.setOnAction(e -> loadAllLogs());

        // Export button (placeholder)
        Button exportBtn = new Button("📊 Xuất báo cáo");
        exportBtn.getStyleClass().add("primary-button");
        exportBtn.setOnAction(e -> exportLogs());

        bar.getChildren().addAll(filterLabel, filterCombo, searchBox, spacer, refreshBtn, exportBtn);

        return bar;
    }

    private HBox createStatisticsCard() {
        HBox card = new HBox(30);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));

        // Total logs
        VBox totalBox = createStatBox("📝", "Tổng giao dịch", String.valueOf(logData.size()));

        // Today's logs
        long todayCount = logData.stream()
                .filter(log -> log.getTimestamp().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();
        VBox todayBox = createStatBox("📅", "Hôm nay", String.valueOf(todayCount));

        // This month
        long monthCount = logData.stream()
                .filter(log -> log.getTimestamp().getMonth() == LocalDateTime.now().getMonth())
                .count();
        VBox monthBox = createStatBox("📆", "Tháng này", String.valueOf(monthCount));

        card.getChildren().addAll(totalBox, todayBox, monthBox);

        return card;
    }

    private VBox createStatBox(String icon, String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        topRow.getChildren().addAll(iconLabel, valueLabel);

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        box.getChildren().addAll(topRow, labelText);

        return box;
    }

    private TableView<BookInventoryLog> createLogTable() {
        TableView<BookInventoryLog> table = new TableView<>();
        table.setItems(logData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Timestamp
        TableColumn<BookInventoryLog, LocalDateTime> timeCol = new TableColumn<>("Thời gian");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(180);
        timeCol.setCellFactory(col -> new TableCell<BookInventoryLog, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                }
            }
        });

        // ISBN
        TableColumn<BookInventoryLog, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(150);

        // Action Type
        TableColumn<BookInventoryLog, String> actionCol = new TableColumn<>("Loại giao dịch");
        actionCol.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        actionCol.setPrefWidth(130);
        actionCol.setCellFactory(col -> new TableCell<BookInventoryLog, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");

                    switch (item) {
                        case "ADD_NEW":
                            badge.setText("Thêm mới");
                            badge.getStyleClass().add("badge-success");
                            break;
                        case "INCREASE_STOCK":
                            badge.setText("Nhập thêm");
                            badge.getStyleClass().add("badge-info");
                            break;
                        case "DECREASE_STOCK":
                            badge.setText("Xuất giảm");
                            badge.getStyleClass().add("badge-warning");
                            break;
                        default:
                            badge.setText(item);
                            badge.getStyleClass().add("badge-secondary");
                    }

                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Quantity Change
        TableColumn<BookInventoryLog, Integer> changeCol = new TableColumn<>("Thay đổi");
        changeCol.setCellValueFactory(new PropertyValueFactory<>("quantityChange"));
        changeCol.setPrefWidth(100);
        changeCol.setCellFactory(col -> new TableCell<BookInventoryLog, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = item > 0 ? "+" + item : String.valueOf(item);
                    setText(text);
                    String color = item > 0 ? "#22c55e" : "#ef4444";
                    setStyle("-fx-font-weight: bold; -fx-text-fill: " + color + ";");
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Total After
        TableColumn<BookInventoryLog, Integer> totalCol = new TableColumn<>("Tổng sau");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalCopiesAfter"));
        totalCol.setPrefWidth(100);

        // Performed By
        TableColumn<BookInventoryLog, String> performerCol = new TableColumn<>("Người thực hiện");
        performerCol.setCellValueFactory(new PropertyValueFactory<>("performedBy"));
        performerCol.setPrefWidth(130);

        // Notes
        TableColumn<BookInventoryLog, String> notesCol = new TableColumn<>("Ghi chú");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(250);

        table.getColumns().addAll(timeCol, isbnCol, actionCol, changeCol, totalCol, performerCol, notesCol);

        // Double click for details
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null) {
                showLogDetails(table.getSelectionModel().getSelectedItem());
            }
        });

        return table;
    }

    private void loadAllLogs() {
        try {
            List<BookInventoryLog> logs = logDAO.findAll();
            logData.setAll(logs);
        } catch (SQLException e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
        }
    }

    private void filterLogs() {
        String filter = filterCombo.getValue();

        if ("Tất cả".equals(filter)) {
            loadAllLogs();
            return;
        }

        try {
            String actionType = switch (filter) {
                case "Thêm mới" -> "ADD_NEW";
                case "Nhập thêm" -> "INCREASE_STOCK";
                case "Xuất giảm" -> "DECREASE_STOCK";
                default -> null;
            };

            if (actionType != null) {
                List<BookInventoryLog> logs = logDAO.findAll();
                logData.setAll(logs.stream()
                        .filter(log -> log.getActionType().equals(actionType))
                        .toList());
            }
        } catch (SQLException e) {
            showError("Lỗi lọc dữ liệu: " + e.getMessage());
        }
    }

    private void searchByISBN(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            loadAllLogs();
            return;
        }

        try {
            List<BookInventoryLog> logs = logDAO.findByISBN(isbn.trim());
            logData.setAll(logs);
        } catch (SQLException e) {
            showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    private void showLogDetails(BookInventoryLog log) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiết giao dịch");
        alert.setHeaderText("Mã giao dịch: " + log.getLogId());
        alert.setContentText(
                "ISBN: " + log.getIsbn() + "\n" +
                        "Loại: " + log.getActionType() + "\n" +
                        "Thay đổi: " + (log.getQuantityChange() > 0 ? "+" : "") + log.getQuantityChange() + "\n" +
                        "Tổng sau: " + log.getTotalCopiesAfter() + "\n" +
                        "Người thực hiện: " + log.getPerformedBy() + "\n" +
                        "Thời gian: " + log.getTimestamp() + "\n" +
                        "Ghi chú: " + log.getNotes()
        );
        alert.showAndWait();
    }

    private void exportLogs() {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Xuất báo cáo");
//        alert.setHeaderText("Tính năng đang phát triển");
//        alert.setContentText("Chức năng xuất báo cáo Excel/PDF sẽ được bổ sung trong phiên bản sau.");
//        alert.showAndWait();
        // Simple text export
        try {
            StringBuilder report = new StringBuilder();
            report.append("╔══════════════════════════════════════════════════╗\n");
            report.append("║          BÁO CÁO LỊCH SỬ NHẬP XUẤT SÁCH          ║\n");
            report.append("╚══════════════════════════════════════════════════╝\n\n");
            report.append("Thời gian tạo báo cáo: ").append(java.time.LocalDateTime.now()).append("\n");
            report.append("Tổng số giao dịch: ").append(logData.size()).append("\n\n");
            report.append("═══════════════════════════════════════════════════════\n\n");

            for (BookInventoryLog log : logData) {
                report.append("ID: ").append(log.getLogId()).append("\n");
                report.append("Thời gian: ").append(log.getTimestamp()).append("\n");
                report.append("ISBN: ").append(log.getIsbn()).append("\n");
                report.append("Loại: ").append(log.getActionType()).append("\n");
                report.append("Thay đổi: ").append(log.getQuantityChange()).append("\n");
                report.append("Tổng sau: ").append(log.getTotalCopiesAfter()).append("\n");
                report.append("Người thực hiện: ").append(log.getPerformedBy()).append("\n");
                report.append("Ghi chú: ").append(log.getNotes()).append("\n");
                report.append("-----------------------------------------------------------\n");
            }

            // Show in dialog
            TextArea textArea = new TextArea(report.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(800);
            textArea.setPrefHeight(600);
            textArea.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 12px;");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Báo cáo Nhập xuất Sách");
            alert.setHeaderText("Xem và sao chép báo cáo");
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefWidth(850);
            alert.showAndWait();

        } catch (Exception e) {
            showError("Lỗi tạo báo cáo: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
