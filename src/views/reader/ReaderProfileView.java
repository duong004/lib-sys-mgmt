package views.reader;

import database.impl.LibraryService;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.people.Reader;

public class ReaderProfileView {
    private LibraryService libraryService;
    private Reader currentReader;

    public ReaderProfileView(LibraryService libraryService, Reader currentReader) {
        this.libraryService = libraryService;
        this.currentReader = currentReader;
    }

    public VBox createView() {
        VBox view = new VBox(25);

        // Profile card
        VBox profileCard = createProfileCard();

        // Statistics card
        VBox statsCard = createStatisticsCard();

        // Membership info
        VBox membershipCard = createMembershipCard();

        view.getChildren().addAll(profileCard, statsCard, membershipCard);

        return view;
    }

    private VBox createProfileCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label title = new Label("👤 Thông tin cá nhân");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        addInfoRow(grid, 0, "Mã độc giả:", currentReader.getId());
        addInfoRow(grid, 1, "Họ tên:", currentReader.getName());
        addInfoRow(grid, 2, "Email:", currentReader.getEmail());
        addInfoRow(grid, 3, "Số điện thoại:", currentReader.getPhone());
        addInfoRow(grid, 4, "Địa chỉ:", currentReader.getAddress() != null ? currentReader.getAddress() : "Chưa cập nhật");
        addInfoRow(grid, 5, "Ngày đăng ký:", currentReader.getRegistrationDate().toString());
        addInfoRow(grid, 6, "Trạng thái:", currentReader.getStatus());

        card.getChildren().addAll(title, grid);

        return card;
    }

    private VBox createStatisticsCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label title = new Label("📊 Thống kê hoạt động");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox statsRow = new HBox(30);
        statsRow.setAlignment(Pos.CENTER);

        VBox currentBox = createStatBox("Đang mượn",
                String.valueOf(currentReader.getCurrentBorrows()), "#3b82f6");
        VBox totalBox = createStatBox("Tổng đã mượn",
                String.valueOf(currentReader.getTotalBorrowed()), "#8b5cf6");
        VBox limitBox = createStatBox("Giới hạn",
                String.valueOf(currentReader.getMembershipType().getBorrowLimit()), "#06b6d4");

        statsRow.getChildren().addAll(currentBox, totalBox, limitBox);

        card.getChildren().addAll(title, statsRow);

        return card;
    }

    private VBox createStatBox(String label, String value, String color) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 10px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        box.getChildren().addAll(valueLabel, labelText);

        return box;
    }

    private VBox createMembershipCard() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");

        Label title = new Label("⭐ Thông tin thẻ thành viên");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10));

        addInfoRow(grid, 0, "Loại thẻ:", currentReader.getMembershipType().name());
        addInfoRow(grid, 1, "Giới hạn mượn:", currentReader.getMembershipType().getBorrowLimit() + " sách");
        addInfoRow(grid, 2, "Giảm giá:", (currentReader.getMembershipType().getDiscount() * 100) + "%");

        // Membership benefits
        String benefits = getMembershipBenefits();
        TextArea benefitsArea = new TextArea(benefits);
        benefitsArea.setEditable(false);
        benefitsArea.setWrapText(true);
        benefitsArea.setPrefRowCount(5);
        benefitsArea.setStyle("-fx-control-inner-background: #f8fafc;");

        VBox benefitsBox = new VBox(10);
        Label benefitsLabel = new Label("Quyền lợi:");
        benefitsLabel.setStyle("-fx-font-weight: bold;");
        benefitsBox.getChildren().addAll(benefitsLabel, benefitsArea);

        card.getChildren().addAll(title, grid, benefitsBox);

        return card;
    }

    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-text-fill: #1e293b;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private String getMembershipBenefits() {
        switch (currentReader.getMembershipType()) {
            case STANDARD:
                return "• Mượn tối đa 3 sách cùng lúc\n" +
                        "• Thời gian mượn: 14 ngày\n" +
                        "• Gia hạn tối đa 2 lần";
            case PREMIUM:
                return "• Mượn tối đa 10 sách cùng lúc\n" +
                        "• Thời gian mượn: 14 ngày\n" +
                        "• Gia hạn tối đa 2 lần\n" +
                        "• Giảm 15% phí phạt trễ hạn\n" +
                        "• Ưu tiên đặt sách mới";
            case STUDENT:
                return "• Mượn tối đa 5 sách cùng lúc\n" +
                        "• Thời gian mượn: 14 ngày\n" +
                        "• Gia hạn tối đa 2 lần\n" +
                        "• Giảm 10% phí phạt trễ hạn\n" +
                        "• Miễn phí thẻ hàng năm";
            case SENIOR:
                return "• Mượn tối đa 5 sách cùng lúc\n" +
                        "• Thời gian mượn: 14 ngày\n" +
                        "• Gia hạn tối đa 2 lần\n" +
                        "• Giảm 20% phí phạt trễ hạn\n" +
                        "• Miễn phí thẻ hàng năm\n" +
                        "• Dịch vụ giao sách tận nơi";
            default:
                return "Không có thông tin";
        }
    }
}
