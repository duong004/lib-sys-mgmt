package database.impl;

import database.config.DatabaseConfig;
import database.dao.BookDAO;
import database.dao.BorrowRecordDAO;
import database.dao.ReaderDAO;
import models.BorrowRecord;
import models.books.Book;
import models.enums.BorrowStatus;
import models.people.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDAOImpl implements BorrowRecordDAO {

    private final BookDAO bookDAO;
    private final ReaderDAO readerDAO;

    public BorrowRecordDAOImpl() {
        this.bookDAO = new BookDAOImpl();
        this.readerDAO = new ReaderDAOImpl();
    }

    @Override
    public void save(BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (record_id, reader_id, isbn, " +
                "borrow_date, due_date, return_date, status, fine, renewal_count) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getRecordId());
            pstmt.setString(2, record.getReader().getId());
            pstmt.setString(3, record.getBook().getISBN());
            pstmt.setDate(4, Date.valueOf(record.getBorrowDate()));
            pstmt.setDate(5, Date.valueOf(record.getDueDate()));
            pstmt.setDate(6, record.getReturnDate() != null ? Date.valueOf(record.getReturnDate()) : null);
            pstmt.setString(7, record.getStatus().name());
            pstmt.setDouble(8, record.getFine());
            pstmt.setInt(9, record.getRenewalCount());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error saving borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(BorrowRecord record) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date = ?, status = ?, " +
                "fine = ?, renewal_count = ?, due_date = ? WHERE record_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, record.getReturnDate() != null ? Date.valueOf(record.getReturnDate()) : null);
            pstmt.setString(2, record.getStatus().name());
            pstmt.setDouble(3, record.getFine());
            pstmt.setInt(4, record.getRenewalCount());
            pstmt.setDate(5, Date.valueOf(record.getDueDate()));
            pstmt.setString(6, record.getRecordId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String recordId) throws SQLException {
        String sql = "DELETE FROM borrow_records WHERE record_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, recordId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new SQLException("Error deleting borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public BorrowRecord findById(String recordId) throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE record_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, recordId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRecord(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new SQLException("Error finding borrow record: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BorrowRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM borrow_records ORDER BY borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching borrow records: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<BorrowRecord> findByReaderId(String readerId) throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE reader_id = ? ORDER BY borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, readerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding records by reader: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<BorrowRecord> findByISBN(String isbn) throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE isbn = ? ORDER BY borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding records by ISBN: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<BorrowRecord> findOverdue() throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE status = 'BORROWED' " +
                "AND due_date < CURRENT_DATE ORDER BY due_date";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding overdue records: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<BorrowRecord> findActive() throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE status = 'BORROWED' ORDER BY due_date";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding active records: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public List<BorrowRecord> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM borrow_records WHERE status = ? ORDER BY borrow_date DESC";
        List<BorrowRecord> records = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding records by status: " + e.getMessage(), e);
        }

        return records;
    }

    @Override
    public int getTotalCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new SQLException("Error counting records: " + e.getMessage(), e);
        }
    }

    @Override
    public int getActiveCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE status = 'BORROWED'";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new SQLException("Error counting active records: " + e.getMessage(), e);
        }
    }

    // ========== HELPER METHODS ==========

    private BorrowRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        String readerId = rs.getString("reader_id");
        String isbn = rs.getString("isbn");

        Reader reader = readerDAO.findById(readerId);
        Book book = bookDAO.findByISBN(isbn);

        if (reader == null || book == null) {
            throw new SQLException("Reader or Book not found for record");
        }

        BorrowRecord record = new BorrowRecord(reader, book);

        record.setRecordId(rs.getString("record_id"));
        record.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
        record.setDueDate(rs.getDate("due_date").toLocalDate());

        Date returnDate = rs.getDate("return_date");
        record.setReturnDate(returnDate != null ? returnDate.toLocalDate() : null);

        record.setStatus(BorrowStatus.valueOf(rs.getString("status")));
        record.setFine(rs.getDouble("fine"));
        record.setRenewalCount(rs.getInt("renewal_count"));

        return record;
    }
}
