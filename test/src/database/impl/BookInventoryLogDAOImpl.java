package database.impl;

import database.config.DatabaseConfig;
import database.dao.BookInventoryLogDAO;
import models.BookInventoryLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookInventoryLogDAOImpl implements BookInventoryLogDAO {

    @Override
    public void save(BookInventoryLog log) throws SQLException {
        String sql = "INSERT INTO book_inventory_logs (isbn, quantity_change, total_copies_after, " +
                "action_type, performed_by, timestamp, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, log.getIsbn());
            pstmt.setInt(2, log.getQuantityChange());
            pstmt.setInt(3, log.getTotalCopiesAfter());
            pstmt.setString(4, log.getActionType());
            pstmt.setString(5, log.getPerformedBy());
            pstmt.setTimestamp(6, Timestamp.valueOf(log.getTimestamp()));
            pstmt.setString(7, log.getNotes());

            pstmt.executeUpdate();

            // Get generated log_id
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                log.setLogId(rs.getLong(1));
            }

        } catch (SQLException e) {
            throw new SQLException("Error saving inventory log: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BookInventoryLog> findByISBN(String isbn) throws SQLException {
        String sql = "SELECT * FROM book_inventory_logs WHERE isbn = ? ORDER BY timestamp DESC";
        List<BookInventoryLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isbn);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching logs by ISBN: " + e.getMessage(), e);
        }

        return logs;
    }

    @Override
    public List<BookInventoryLog> findByPerformer(String librarianId) throws SQLException {
        String sql = "SELECT * FROM book_inventory_logs WHERE performed_by = ? ORDER BY timestamp DESC";
        List<BookInventoryLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, librarianId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching logs by performer: " + e.getMessage(), e);
        }

        return logs;
    }

    @Override
    public List<BookInventoryLog> findRecent(int limit) throws SQLException {
        String sql = "SELECT * FROM book_inventory_logs ORDER BY timestamp DESC LIMIT ?";
        List<BookInventoryLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching recent logs: " + e.getMessage(), e);
        }

        return logs;
    }

    @Override
    public List<BookInventoryLog> findAll() throws SQLException {
        String sql = "SELECT * FROM book_inventory_logs ORDER BY timestamp DESC";
        List<BookInventoryLog> logs = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToLog(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching all logs: " + e.getMessage(), e);
        }

        return logs;
    }

    // Helper method
    private BookInventoryLog mapResultSetToLog(ResultSet rs) throws SQLException {
        return new BookInventoryLog(
                rs.getLong("log_id"),
                rs.getString("isbn"),
                rs.getInt("quantity_change"),
                rs.getInt("total_copies_after"),
                rs.getString("action_type"),
                rs.getString("performed_by"),
                rs.getTimestamp("timestamp").toLocalDateTime(),
                rs.getString("notes")
        );
    }
}