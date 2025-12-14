package database.impl;

import database.config.DatabaseConfig;
import database.dao.*;
import interfaces.Reportable;
import interfaces.Searchable;
import models.BookInventoryLog;
import models.BorrowRecord;
import models.books.Book;
import models.enums.BorrowStatus;
import models.enums.UserRole;
import models.people.Reader;
import models.people.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class LibraryService implements Searchable, Reportable {
    private final String libraryName;
    private final String address;

    // DAO instances
    private final BookDAO bookDAO;
    private final ReaderDAO readerDAO;
    private final BorrowRecordDAO borrowRecordDAO;
    private final UserDAO userDAO;

    private final int maxBorrowDays = 14;
    private final double finePerDay = 5000;

    public LibraryService(String name, String address) {
        this.libraryName = name;
        this.address = address;
        this.bookDAO = new BookDAOImpl();
        this.readerDAO = new ReaderDAOImpl();
        this.borrowRecordDAO = new BorrowRecordDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    // ========== BOOK MANAGEMENT ==========

    public Book addOrUpdateBookInventory(String isbn, int quantityToAdd, String performedBy) {
        try {
            Book existingBook = bookDAO.findByISBN(isbn);

            if (existingBook != null) {
                // ISBN exists - UPDATE quantity
                int oldTotal = existingBook.getTotalCopies();
                int oldAvailable = existingBook.getAvailableCopies();
                int newTotal = oldTotal + quantityToAdd;
                int newAvailable = oldAvailable + quantityToAdd;

                existingBook.setTotalCopies(newTotal);
                existingBook.setAvailableCopies(newAvailable);

                // Update both total and available copies
//                for (int i = 0; i < quantityToAdd; i++) {
//                    existingBook.returnBook(); // Increases available copies
//                }

                // Save to database
                bookDAO.update(existingBook);

                // Log the change
                logInventoryChange(isbn, quantityToAdd, newTotal, "INCREASE_STOCK",
                        performedBy, "Nhập thêm sách vào kho");

                System.out.println(" Đã cập nhật số lượng cho ISBN: " + isbn);
                System.out.println(" Số lượng cũ: " + oldTotal);
                System.out.println(" Số lượng mới: " + newTotal);

                return existingBook;

            } else {
                // ISBN doesn't exist - return null to signal need for full info
                return null;
            }

        } catch (SQLException e) {
            System.err.println(" Lỗi khi xử lý sách: " + e.getMessage());
            return null;
        }
    }

    public void addBook(Book book) {
        addBook(book, "SYSTEM");
    }

    public void addBook(Book book, String performedBy) {
        try {
            bookDAO.save(book);

            // Log the addition
            logInventoryChange(book.getISBN(), book.getTotalCopies(), book.getTotalCopies(),
                    "ADD_NEW", performedBy, "Thêm sách mới: " + book.getTitle());

            System.out.println(" Đã thêm sách mới: " + book.getTitle());
        } catch (SQLException e) {
            System.err.println(" Lỗi khi thêm sách: " + e.getMessage());
        }
    }

    private void logInventoryChange(String isbn, int quantityChange, int totalAfter,
                                    String actionType, String performedBy, String notes) {
        try {
            BookInventoryLogDAO logDAO = new BookInventoryLogDAOImpl();
            BookInventoryLog log = new BookInventoryLog(isbn, quantityChange, totalAfter,
                    actionType, performedBy, notes);
            logDAO.save(log);
        } catch (SQLException e) {
            System.err.println("  Không thể ghi log: " + e.getMessage());
        }
    }

    public boolean removeBook(String isbn) {
        try {
            Book book = bookDAO.findByISBN(isbn);
            if (book != null && book.getAvailableCopies() == book.getTotalCopies()) {
                bookDAO.delete(isbn);
                System.out.println(" Đã xóa sách: " + book.getTitle());
                return true;
            }
            System.out.println(" Không thể xóa sách (đang được mượn hoặc không tồn tại)");
            return false;
        } catch (SQLException e) {
            System.err.println(" Lỗi khi xóa sách: " + e.getMessage());
            return false;
        }
    }

    public void updateBook(Book book) {
        try {
            bookDAO.update(book);
            System.out.println(" Đã cập nhật sách: " + book.getTitle());
        } catch (SQLException e) {
            System.err.println(" Lỗi khi cập nhật sách: " + e.getMessage());
        }
    }

    public void displayAllBooks() {
        try {
            System.out.println("\n=== DANH SÁCH SÁCH TRONG THƯ VIỆN ===");
            List<Book> books = bookDAO.findAll();
            if (books.isEmpty()) {
                System.out.println("Chưa có sách nào.");
                return;
            }
            for (Book book : books) {
                System.out.println(book.getInfo());
            }
        } catch (SQLException e) {
            System.err.println(" Lỗi khi hiển thị sách: " + e.getMessage());
        }
    }

    // ========== SEARCH OPERATIONS (Implementation of Searchable) ==========

    @Override
    public List<Book> searchByTitle(String title) {
        try {
            return bookDAO.searchByTitle(title);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi tìm kiếm: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        try {
            return bookDAO.searchByAuthor(author);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi tìm kiếm: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Book searchByISBN(String isbn) {
        try {
            return bookDAO.findByISBN(isbn);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi tìm kiếm: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Book> searchByCategory(String category) {
        try {
            return bookDAO.searchByCategory(category);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi tìm kiếm: " + e.getMessage());
            return List.of();
        }
    }

    // ========== READER MANAGEMENT ==========

    public void registerReader(Reader reader) {
        try {
            readerDAO.save(reader);
            System.out.println(" Đã đăng ký độc giả: " + reader.getName());
        } catch (SQLException e) {
            System.err.println(" Lỗi khi đăng ký độc giả: " + e.getMessage());
        }
    }

    public boolean registerReaderWithAccount(Reader reader, String password) {
        try {
            readerDAO.save(reader);
            String readerId = reader.getId();

            User newUser = new User(reader.getEmail(), password, UserRole.READER, reader.getName(), reader.getEmail());
            newUser.setLinkedEntityId(readerId);

            userDAO.save(newUser);
            return true;
        } catch (SQLException e) {
            System.err.println(" Lỗi đăng ký hệ thống: " + e.getMessage());
            return false;
        }
    }

    public boolean removeReader(String readerId) {
        try {
            Reader reader = readerDAO.findById(readerId);
            if (reader != null && reader.getCurrentBorrows() == 0) {
                readerDAO.delete(readerId);

                List<User> allUsers = userDAO.findAll(); // Hoặc viết thêm hàm findByLinkedId trong UserDAO
                for (User u : allUsers) {
                    if (readerId.equals(u.getLinkedEntityId())) {
                        u.setActive(false);
                        userDAO.update(u);
                        System.out.println("  Đã khóa tài khoản đăng nhập: " + u.getUsername());
                        break;
                    }
                }

                System.out.println(" Đã xóa độc giả: " + reader.getName());
                return true;
            }
            System.out.println(" Không thể xóa (độc giả đang mượn sách hoặc không tồn tại)");
            return false;
        } catch (SQLException e) {
            System.err.println(" Lỗi khi xóa độc giả: " + e.getMessage());
            return false;
        }
    }

    public Reader findReaderById(String readerId) {
        try {
            return readerDAO.findById(readerId);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi tìm độc giả: " + e.getMessage());
            return null;
        }
    }

    public void displayAllReaders() {
        try {
            System.out.println("\n=== DANH SÁCH ĐỘC GIẢ ===");
            List<Reader> readers = readerDAO.findAll();
            if (readers.isEmpty()) {
                System.out.println("Chưa có độc giả nào.");
                return;
            }
            for (Reader reader : readers) {
                System.out.println(reader.getInfo());
            }
        } catch (SQLException e) {
            System.err.println(" Lỗi khi hiển thị độc giả: " + e.getMessage());
        }
    }

    // ========== BORROW/RETURN OPERATIONS ==========

    public BorrowRecord borrowBook(String readerId, String isbn) {
        try {
            Reader reader = readerDAO.findById(readerId);
            Book book = bookDAO.findByISBN(isbn);

            if (reader == null) {
                System.out.println(" Không tìm thấy độc giả");
                return null;
            }

            if (book == null) {
                System.out.println(" Không tìm thấy sách");
                return null;
            }

            if (!reader.canBorrow()) {
                System.out.println(" Độc giả đã đạt giới hạn mượn sách");
                return null;
            }

            if (!book.isAvailable()) {
                System.out.println(" Sách không có sẵn");
                return null;
            }

            // Execute borrow transaction
            if (book.borrowBook()) {
                reader.incrementBorrowCount();
                BorrowRecord record = new BorrowRecord(reader, book);

                // Save to database
                borrowRecordDAO.save(record);
                bookDAO.update(book);
                readerDAO.update(reader);

                System.out.println(" Mượn sách thành công!");
                System.out.println("  Hạn trả: " + record.getDueDate());
                return record;
            }

            return null;

        } catch (SQLException e) {
            System.err.println(" Lỗi khi mượn sách: " + e.getMessage());
            return null;
        }
    }

    public void returnBook(String recordId) {
        try {
            BorrowRecord record = borrowRecordDAO.findById(recordId);

            if (record != null && record.getStatus() == BorrowStatus.BORROWED) {
                record.markAsReturned();
                record.getBook().returnBook();
                record.getReader().decrementBorrowCount();

                // Update database
                borrowRecordDAO.update(record);
                bookDAO.update(record.getBook());
                readerDAO.update(record.getReader());

                System.out.println(" Trả sách thành công!");
                if (record.getFine() > 0) {
                    System.out.println("  Phí phạt: " + String.format("%,.0f VND", record.getFine()));
                }
                return;
            }
            System.out.println(" Không tìm thấy phiếu mượn");

        } catch (SQLException e) {
            System.err.println(" Lỗi khi trả sách: " + e.getMessage());
        }
    }

    public boolean extendBorrow(String recordId, int days) {
        try {
            BorrowRecord record = borrowRecordDAO.findById(recordId);

            if (record != null) {
                if (record.extendDueDate(days)) {
                    borrowRecordDAO.update(record);
                    System.out.println(" Đã gia hạn thêm " + days + " ngày");
                    System.out.println("  Hạn mới: " + record.getDueDate());
                    return true;
                } else {
                    System.out.println(" Không thể gia hạn (quá hạn hoặc đã gia hạn tối đa)");
                    return false;
                }
            }
            System.out.println(" Không tìm thấy phiếu mượn");
            return false;

        } catch (SQLException e) {
            System.err.println(" Lỗi khi gia hạn: " + e.getMessage());
            return false;
        }
    }

    public List<BorrowRecord> getBorrowHistory(String readerId) {
        try {
            return borrowRecordDAO.findByReaderId(readerId);
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy lịch sử: " + e.getMessage());
            return List.of();
        }
    }

    public List<BorrowRecord> getOverdueRecords() {
        try {
            return borrowRecordDAO.findOverdue();
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy danh sách quá hạn: " + e.getMessage());
            return List.of();
        }
    }

    // ========== REPORT GENERATION (Implementation of Reportable) ==========

    @Override
    public void generatePopularBooksReport() {
        System.out.println("\n=== TOP 5 SÁCH ĐƯỢC MƯỢN NHIỀU NHẤT ===");

        String sql = "SELECT b.isbn, b.title, COUNT(*) as borrow_count " +
                "FROM borrow_records br " +
                "JOIN books b ON br.isbn = b.isbn " +
                "GROUP BY b.isbn, b.title " +
                "ORDER BY borrow_count DESC " +
                "LIMIT 5";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getString("title") + " - " +
                        rs.getInt("borrow_count") + " lần");
            }

        } catch (SQLException e) {
            System.err.println(" Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }

    @Override
    public void generateActiveReadersReport() {
        System.out.println("\n=== TOP 5 ĐỘC GIẢ TÍCH CỰC NHẤT ===");

        String sql = "SELECT reader_id, name, total_borrowed " +
                "FROM readers " +
                "ORDER BY total_borrowed DESC " +
                "LIMIT 5";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " +
                        rs.getInt("total_borrowed") + " lần");
            }

        } catch (SQLException e) {
            System.err.println(" Lỗi khi tạo báo cáo: " + e.getMessage());
        }
    }

    @Override
    public void generateOverdueReport() {
        System.out.println("\n=== DANH SÁCH SÁCH QUÁ HẠN ===");
        List<BorrowRecord> overdueList = getOverdueRecords();

        if (overdueList.isEmpty()) {
            System.out.println("Không có sách quá hạn.");
            return;
        }

        for (BorrowRecord record : overdueList) {
            System.out.println(record.getInfo() + " - Trễ: " + record.getDaysLate() + " ngày");
        }
    }

    @Override
    public void generateMonthlyStatistics() {
        System.out.println("\n=== THỐNG KÊ THÁNG NÀY ===");

        try (Connection conn = DatabaseConfig.getConnection()) {

            // Total books
            String sql1 = "SELECT COUNT(*) as count FROM books";
            Statement stmt1 = conn.createStatement();
            ResultSet rs1 = stmt1.executeQuery(sql1);
            if (rs1.next()) {
                System.out.println("Tổng số sách: " + rs1.getInt("count"));
            }

            // Total readers
            String sql2 = "SELECT COUNT(*) as count FROM readers";
            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery(sql2);
            if (rs2.next()) {
                System.out.println("Tổng số độc giả: " + rs2.getInt("count"));
            }

            // Total borrow records
            String sql3 = "SELECT COUNT(*) as count FROM borrow_records";
            Statement stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery(sql3);
            if (rs3.next()) {
                System.out.println("Số lần mượn: " + rs3.getInt("count"));
            }

            // Currently borrowed
            String sql4 = "SELECT COUNT(*) as count FROM borrow_records WHERE status = 'BORROWED'";
            Statement stmt4 = conn.createStatement();
            ResultSet rs4 = stmt4.executeQuery(sql4);
            if (rs4.next()) {
                System.out.println("Đang được mượn: " + rs4.getInt("count"));
            }

            // Overdue
            String sql5 = "SELECT COUNT(*) as count FROM borrow_records " +
                    "WHERE status = 'BORROWED' AND due_date < CURRENT_DATE";
            Statement stmt5 = conn.createStatement();
            ResultSet rs5 = stmt5.executeQuery(sql5);
            if (rs5.next()) {
                System.out.println("Sách quá hạn: " + rs5.getInt("count"));
            }

        } catch (SQLException e) {
            System.err.println(" Lỗi khi tạo thống kê: " + e.getMessage());
        }
    }

    // ========== GETTERS ==========

    public String getLibraryName() {
        return libraryName;
    }

    public String getAddress() {
        return address;
    }

    public List<Book> getBooks() {
        try {
            return bookDAO.findAll();
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy danh sách sách: " + e.getMessage());
            return List.of();
        }
    }

    public List<Reader> getReaders() {
        try {
            return readerDAO.findAll();
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy danh sách độc giả: " + e.getMessage());
            return List.of();
        }
    }

    public List<BorrowRecord> getBorrowRecords() {
        try {
            return borrowRecordDAO.findAll();
        } catch (SQLException e) {
            System.err.println(" Lỗi khi lấy danh sách phiếu mượn: " + e.getMessage());
            return List.of();
        }
    }
}
