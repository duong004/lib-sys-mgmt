package views.librarian;

import database.impl.LibraryService;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ReportsView {

    private LibraryService libraryService;
    private TextArea reportArea;

    public ReportsView(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public VBox createView() {
        VBox view = new VBox(20);

        // Report buttons
        HBox buttonsRow = new HBox(15);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        Button popularBooksBtn = new Button("📊 Top sách được mượn");
        popularBooksBtn.getStyleClass().add("primary-button");
        popularBooksBtn.setOnAction(e -> showPopularBooks());

        Button activeReadersBtn = new Button("👑 Top độc giả tích cực");
        activeReadersBtn.getStyleClass().add("primary-button");
        activeReadersBtn.setOnAction(e -> showActiveReaders());

        Button overdueBtn = new Button("⚠ Sách quá hạn");
        overdueBtn.getStyleClass().add("danger-button");
        overdueBtn.setOnAction(e -> showOverdueBooks());

        Button statsBtn = new Button("📈 Thống kê tổng quan");
        statsBtn.getStyleClass().add("secondary-button");
        statsBtn.setOnAction(e -> showMonthlyStats());

        buttonsRow.getChildren().addAll(popularBooksBtn, activeReadersBtn, overdueBtn, statsBtn);

        // Report display area
        reportArea = new TextArea();
        reportArea.setEditable(false);
        reportArea.setWrapText(true);
        reportArea.setPrefHeight(500);
        reportArea.setStyle(
                "-fx-font-family: 'Consolas', 'Monaco', monospace; " +
                        "-fx-font-size: 13px; " +
                        "-fx-control-inner-background: #1e293b; " +
                        "-fx-text-fill: #e2e8f0; " +
                        "-fx-display-caret: false; " +
                        "-fx-opacity: 1.0; " +
                        "-fx-padding: 20px;"
        );
        VBox.setVgrow(reportArea, Priority.ALWAYS);

        view.getChildren().addAll(buttonsRow, reportArea);

        // Initial report
        reportArea.setText("═══════════════════════════════════════\n" +
                "     📊 HỆ THỐNG BÁO CÁO & THỐNG KÊ\n" +
                "═══════════════════════════════════════\n\n" +
                "Chọn một loại báo cáo bên trên để xem chi tiết...\n\n" +
                "📊 Top sách được mượn - Xem sách phổ biến nhất\n" +
                "👑 Top độc giả tích cực - Xem độc giả mượn nhiều\n" +
                "⚠  Sách quá hạn - Danh sách cần thu hồi\n" +
                "📈 Thống kê tổng quan - Số liệu hệ thống");

        return view;
    }

    private void showPopularBooks() {
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════════╗\n");
        report.append("║   📊 TOP 5 SÁCH ĐƯỢC MƯỢN NHIỀU NHẤT   ║\n");
        report.append("╚════════════════════════════════════════╝\n\n");

        try {
            java.sql.Connection conn = database.config.DatabaseConfig.getConnection();
            String sql = "SELECT b.isbn, b.title, b.author, COUNT(*) as borrow_count " +
                    "FROM borrow_records br " +
                    "JOIN books b ON br.isbn = b.isbn " +
                    "GROUP BY b.isbn, b.title, b.author " +
                    "ORDER BY borrow_count DESC " +
                    "LIMIT 5";

            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            int rank = 1;
            while (rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                int count = rs.getInt("borrow_count");

                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                report.append(String.format("%s #%d. %s\n", medal, rank, title));
                report.append(String.format("      Tác giả: %s\n", author));
                report.append(String.format("      Số lần mượn: %d lần\n\n", count));
                rank++;
            }

            if (rank == 1) {
                report.append("   Chưa có dữ liệu mượn sách.\n");
            }

            conn.close();

        } catch (Exception e) {
            report.append("❌ Lỗi: ").append(e.getMessage()).append("\n");
        }

        report.append("\n════════════════════════════════════════\n");
        report.append("Báo cáo tạo lúc: ").append(java.time.LocalDateTime.now()).append("\n");

        reportArea.setText(report.toString());
    }

    private void showActiveReaders() {
        StringBuilder report = new StringBuilder();
        report.append("╔════════════════════════════════════╗\n");
        report.append("║   👑 TOP 5 ĐỘC GIẢ TÍCH CỰC NHẤT   ║\n");
        report.append("╚════════════════════════════════════╝\n\n");

        try {
            java.sql.Connection conn = database.config.DatabaseConfig.getConnection();
            String sql = "SELECT reader_id, name, email, membership_type, total_borrowed " +
                    "FROM readers " +
                    "WHERE status = 'ACTIVE' " +
                    "ORDER BY total_borrowed DESC " +
                    "LIMIT 5";

            java.sql.Statement stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sql);

            int rank = 1;
            while (rs.next()) {
                String id = rs.getString("reader_id");
                String name = rs.getString("name");
                String type = rs.getString("membership_type");
                int total = rs.getInt("total_borrowed");

                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                report.append(String.format("%s #%d. %s (%s)\n", medal, rank, name, id));
                report.append(String.format("      Loại thẻ: %s\n", type));
                report.append(String.format("      Tổng đã mượn: %d lần\n\n", total));
                rank++;
            }

            if (rank == 1) {
                report.append("   Chưa có độc giả nào.\n");
            }

            conn.close();

        } catch (Exception e) {
            report.append("❌ Lỗi: ").append(e.getMessage()).append("\n");
        }

        report.append("\n════════════════════════════════════════\n");
        report.append("Báo cáo tạo lúc: ").append(java.time.LocalDateTime.now()).append("\n");

        reportArea.setText(report.toString());
    }

    private void showOverdueBooks() {
        StringBuilder report = new StringBuilder();
        report.append("╔═════════════════════════════════════╗\n");
        report.append("║      ⚠  DANH SÁCH SÁCH QUÁ HẠN      ║\n");
        report.append("╚═════════════════════════════════════╝\n\n");

        var overdueList = libraryService.getOverdueRecords();

        if (overdueList.isEmpty()) {
            report.append("✅ Không có sách quá hạn!\n\n");
            report.append("Tất cả độc giả đều trả sách đúng hạn.\n");
        } else {
            report.append(String.format("Tổng số: %d phiếu mượn quá hạn\n\n", overdueList.size()));

            int count = 1;
            for (var record : overdueList) {
                int daysLate = record.getDaysLate();
                double fine = record.calculateFine();

                report.append(String.format("%d. Phiếu: %s\n", count++, record.getRecordId()));
                report.append(String.format("   Độc giả: %s\n", record.getReader().getName()));
                report.append(String.format("   Sách: %s\n", record.getBook().getTitle()));
                report.append(String.format("   Hạn trả: %s\n", record.getDueDate()));
                report.append(String.format("   ⏰ Trễ: %d ngày\n", daysLate));
                report.append(String.format("   💰 Phí phạt: %,.0f VND\n\n", fine));
            }

            double totalFine = overdueList.stream()
                    .mapToDouble(r -> r.calculateFine())
                    .sum();
            report.append("────────────────────────────────────────\n");
            report.append(String.format("💰 Tổng phí phạt: %,.0f VND\n", totalFine));
        }

        report.append("\n════════════════════════════════════════\n");
        report.append("Báo cáo tạo lúc: ").append(java.time.LocalDateTime.now()).append("\n");

        reportArea.setText(report.toString());
    }

    private void showMonthlyStats() {
        StringBuilder report = new StringBuilder();
        report.append("╔═══════════════════════════════════╗\n");
        report.append("║       📈 THỐNG KÊ TỔNG QUAN       ║\n");
        report.append("╚═══════════════════════════════════╝\n\n");

        try {
            java.sql.Connection conn = database.config.DatabaseConfig.getConnection();

            // Total books
            String sql1 = "SELECT COUNT(*) as count, SUM(available_copies) as available FROM books";
            java.sql.Statement stmt1 = conn.createStatement();
            java.sql.ResultSet rs1 = stmt1.executeQuery(sql1);
            if (rs1.next()) {
                report.append("📚 SÁCH\n");
                report.append(String.format("   Tổng đầu sách: %d\n", rs1.getInt("count")));
                report.append(String.format("   Sách có sẵn: %d\n\n", rs1.getInt("available")));
            }

            // Total readers
            String sql2 = "SELECT COUNT(*) as total, " +
                    "COUNT(*) FILTER (WHERE status = 'ACTIVE') as active FROM readers";
            java.sql.Statement stmt2 = conn.createStatement();
            java.sql.ResultSet rs2 = stmt2.executeQuery(sql2);
            if (rs2.next()) {
                report.append("👥 ĐỘC GIẢ\n");
                report.append(String.format("   Tổng số: %d\n", rs2.getInt("total")));
                report.append(String.format("   Đang hoạt động: %d\n\n", rs2.getInt("active")));
            }

            // Borrow statistics
            String sql3 = "SELECT COUNT(*) as total, " +
                    "COUNT(*) FILTER (WHERE status = 'BORROWED') as borrowed, " +
                    "COUNT(*) FILTER (WHERE status = 'RETURNED') as returned FROM borrow_records";
            java.sql.Statement stmt3 = conn.createStatement();
            java.sql.ResultSet rs3 = stmt3.executeQuery(sql3);
            if (rs3.next()) {
                report.append("🔄 MƯỢN/TRẢ\n");
                report.append(String.format("   Tổng giao dịch: %d\n", rs3.getInt("total")));
                report.append(String.format("   Đang mượn: %d\n", rs3.getInt("borrowed")));
                report.append(String.format("   Đã trả: %d\n\n", rs3.getInt("returned")));
            }

            // Overdue
            String sql4 = "SELECT COUNT(*) as count FROM borrow_records " +
                    "WHERE status = 'BORROWED' AND due_date < CURRENT_DATE";
            java.sql.Statement stmt4 = conn.createStatement();
            java.sql.ResultSet rs4 = stmt4.executeQuery(sql4);
            if (rs4.next()) {
                int overdueCount = rs4.getInt("count");
                report.append("⚠  QUÁ HẠN\n");
                report.append(String.format("   Số sách quá hạn: %d\n\n", overdueCount));
            }

            // Most active day
            String sql5 = "SELECT DATE(borrow_date) as day, COUNT(*) as count " +
                    "FROM borrow_records " +
                    "WHERE borrow_date >= CURRENT_DATE - INTERVAL '30 days' " +
                    "GROUP BY DATE(borrow_date) " +
                    "ORDER BY count DESC LIMIT 1";
            java.sql.Statement stmt5 = conn.createStatement();
            java.sql.ResultSet rs5 = stmt5.executeQuery(sql5);
            if (rs5.next()) {
                report.append("📊 THỐNG KÊ BỔ SUNG\n");
                report.append(String.format("   Ngày có nhiều giao dịch nhất: %s (%d giao dịch)\n",
                        rs5.getDate("day"), rs5.getInt("count")));
            }

            conn.close();

        } catch (Exception e) {
            report.append("❌ Lỗi: ").append(e.getMessage()).append("\n");
        }

        report.append("\n════════════════════════════════════════\n");
        report.append("Báo cáo tạo lúc: ").append(java.time.LocalDateTime.now()).append("\n");

        reportArea.setText(report.toString());
    }
}
