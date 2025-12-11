package database.impl;

import database.config.DatabaseConfig;
import database.dao.BookDAO;
import models.books.Book;
import models.books.Magazine;
import models.books.ReferenceBook;
import models.books.TextBook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAOImpl implements BookDAO {

    @Override
    public void save(Book book) throws SQLException {
        String sql = "INSERT INTO books (isbn, title, author, publisher, publish_year, " +
                "category, total_copies, available_copies, price, book_type, extra_info) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setBookParameters(pstmt, book);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error saving book: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Book book) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, " +
                "publish_year = ?, category = ?, total_copies = ?, " +
                "available_copies = ?, price = ?, extra_info = ?::jsonb " +
                "WHERE isbn = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getPublisher());
            pstmt.setInt(4, book.getPublishYear());
            pstmt.setString(5, book.getCategory());
            pstmt.setInt(6, book.getTotalCopies());
            pstmt.setInt(7, book.getAvailableCopies());
            pstmt.setDouble(8, book.getPrice());
            pstmt.setString(9, getExtraInfo(book));
            pstmt.setString(10, book.getISBN());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating book: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String isbn) throws SQLException {
        String sql = "DELETE FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new SQLException("Error deleting book: " + e.getMessage(), e);
        }
    }

    @Override
    public Book findByISBN(String isbn) throws SQLException {
        String sql = "SELECT * FROM books WHERE isbn = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBook(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new SQLException("Error finding book: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> findAll() throws SQLException {
        String sql = "SELECT * FROM books ORDER BY title";
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching books: " + e.getMessage(), e);
        }

        return books;
    }

    @Override
    public List<Book> searchByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?) ORDER BY title";
        return searchBooks(sql, "%" + title + "%");
    }

    @Override
    public List<Book> searchByAuthor(String author) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(author) LIKE LOWER(?) ORDER BY title";
        return searchBooks(sql, "%" + author + "%");
    }

    @Override
    public List<Book> searchByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(category) LIKE LOWER(?) ORDER BY title";
        return searchBooks(sql, "%" + category + "%");
    }

    @Override
    public int getAvailableCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM books WHERE available_copies > 0";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new SQLException("Error counting available books: " + e.getMessage(), e);
        }
    }

    // ========== HELPER METHODS ==========

    private List<Book> searchBooks(String sql, String param) throws SQLException {
        List<Book> books = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, param);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error searching books: " + e.getMessage(), e);
        }

        return books;
    }

    private void setBookParameters(PreparedStatement pstmt, Book book) throws SQLException {
        pstmt.setString(1, book.getISBN());
        pstmt.setString(2, book.getTitle());
        pstmt.setString(3, book.getAuthor());
        pstmt.setString(4, book.getPublisher());
        pstmt.setInt(5, book.getPublishYear());
        pstmt.setString(6, book.getCategory());
        pstmt.setInt(7, book.getTotalCopies());
        pstmt.setInt(8, book.getAvailableCopies());
        pstmt.setDouble(9, book.getPrice());
        pstmt.setString(10, getBookType(book));
        pstmt.setString(11, getExtraInfo(book));
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        String bookType = rs.getString("book_type");
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        String author = rs.getString("author");
        int totalCopies = rs.getInt("total_copies");

        Book book;
        String extraInfo = rs.getString("extra_info");

        switch (bookType) {
            case "TextBook":
                String subject = extractFromJSON(extraInfo, "subject");
                int grade = Integer.parseInt(extractFromJSON(extraInfo, "grade"));
                book = new TextBook(isbn, title, author, totalCopies, subject, grade);
                break;

            case "ReferenceBook":
                String topic = extractFromJSON(extraInfo, "topic");
                book = new ReferenceBook(isbn, title, author, totalCopies, topic);
                break;

            case "Magazine":
                int issueNumber = Integer.parseInt(extractFromJSON(extraInfo, "issueNumber"));
                book = new Magazine(isbn, title, author, totalCopies, issueNumber);
                break;

            default:
                throw new SQLException("Unknown book type: " + bookType);
        }

        // Set common properties
        book.setPublisher(rs.getString("publisher"));
        book.setPublishYear(rs.getInt("publish_year"));
        book.setPrice(rs.getDouble("price"));

        // Set available copies directly (package access needed or use reflection)
        int availableCopies = rs.getInt("available_copies");
        // Adjust available copies by borrowing/returning
        int diff = totalCopies - availableCopies;
        for (int i = 0; i < diff; i++) {
            book.borrowBook();
        }

        return book;
    }

    private String getBookType(Book book) {
        if (book instanceof TextBook) return "TextBook";
        if (book instanceof ReferenceBook) return "ReferenceBook";
        if (book instanceof Magazine) return "Magazine";
        return "Book";
    }

    private String getExtraInfo(Book book) {
        if (book instanceof TextBook) {
            TextBook tb = (TextBook) book;
            return String.format("{\"subject\":\"%s\",\"grade\":%d}",
                    tb.getSubject(), tb.getGrade());
        } else if (book instanceof ReferenceBook) {
            ReferenceBook rb = (ReferenceBook) book;
            return String.format("{\"topic\":\"%s\",\"canBorrow\":%b}",
                    rb.getTopic(), rb.canBeBorrowed());
        } else if (book instanceof Magazine) {
            Magazine m = (Magazine) book;
            return String.format("{\"issueNumber\":%d}", m.getIssueNumber());
        }
        return "{}";
    }

    private String extractFromJSON(String json, String key) {
        if (json == null) return "";
        String pattern = "\"" + key + "\":\"?([^,}\"]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
