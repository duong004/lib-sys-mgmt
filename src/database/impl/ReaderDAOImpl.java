package database.impl;

import database.config.DatabaseConfig;
import database.dao.ReaderDAO;
import models.enums.MembershipType;
import models.people.Reader;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReaderDAOImpl implements ReaderDAO {

    @Override
    public void save(Reader reader) throws SQLException {
        String sql = "INSERT INTO readers (reader_id, name, email, phone, address, " +
                "membership_type, registration_date, current_borrows, total_borrowed, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reader.getId());
            pstmt.setString(2, reader.getName());
            pstmt.setString(3, reader.getEmail());
            pstmt.setString(4, reader.getPhone());
            pstmt.setString(5, reader.getAddress());
            pstmt.setString(6, reader.getMembershipType().name());
            pstmt.setDate(7, Date.valueOf(reader.getRegistrationDate()));
            pstmt.setInt(8, reader.getCurrentBorrows());
            pstmt.setInt(9, reader.getTotalBorrowed());
            pstmt.setBoolean(10, reader.isActive());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error saving reader: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Reader reader) throws SQLException {
        String sql = "UPDATE readers SET name = ?, email = ?, phone = ?, address = ?, " +
                "membership_type = ?, current_borrows = ?, total_borrowed = ?, is_active = ? " +
                "WHERE reader_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reader.getName());
            pstmt.setString(2, reader.getEmail());
            pstmt.setString(3, reader.getPhone());
            pstmt.setString(4, reader.getAddress());
            pstmt.setString(5, reader.getMembershipType().name());
            pstmt.setInt(6, reader.getCurrentBorrows());
            pstmt.setInt(7, reader.getTotalBorrowed());
            pstmt.setBoolean(8, reader.isActive());
            pstmt.setString(9, reader.getId());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating reader: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String readerId) throws SQLException {
        String sql = "DELETE FROM readers WHERE reader_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, readerId);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new SQLException("Error deleting reader: " + e.getMessage(), e);
        }
    }

    @Override
    public Reader findById(String readerId) throws SQLException {
        String sql = "SELECT * FROM readers WHERE reader_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, readerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToReader(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new SQLException("Error finding reader: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reader> findAll() throws SQLException {
        String sql = "SELECT * FROM readers ORDER BY name";
        List<Reader> readers = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                readers.add(mapResultSetToReader(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching readers: " + e.getMessage(), e);
        }

        return readers;
    }

    @Override
    public List<Reader> findByMembershipType(String membershipType) throws SQLException {
        String sql = "SELECT * FROM readers WHERE membership_type = ? ORDER BY name";
        List<Reader> readers = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, membershipType);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                readers.add(mapResultSetToReader(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding readers by membership: " + e.getMessage(), e);
        }

        return readers;
    }

    @Override
    public void updateBorrowCount(String readerId, int increment) throws SQLException {
        String sql = "UPDATE readers SET current_borrows = current_borrows + ?, " +
                "total_borrowed = total_borrowed + ? WHERE reader_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, increment);
            pstmt.setInt(2, Math.max(0, increment)); // Only increment total if positive
            pstmt.setString(3, readerId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating borrow count: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String readerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM readers WHERE reader_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, readerId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new SQLException("Error checking reader existence: " + e.getMessage(), e);
        }
    }

    @Override
    public int getActiveCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM readers WHERE is_active = true";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new SQLException("Error counting active readers: " + e.getMessage(), e);
        }
    }

    // ========== HELPER METHODS ==========

    private Reader mapResultSetToReader(ResultSet rs) throws SQLException {
        String id = rs.getString("reader_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        MembershipType type = MembershipType.valueOf(rs.getString("membership_type"));

        Reader reader = new Reader(id, name, email, phone, type);

        reader.setAddress(rs.getString("address"));
        reader.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        reader.setCurrentBorrows(rs.getInt("current_borrows"));
        reader.setTotalBorrowed(rs.getInt("total_borrowed"));
        reader.setActive(rs.getBoolean("is_active"));

        return reader;
    }
}
