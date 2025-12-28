package database.impl;

import database.config.DatabaseConfig;
import database.dao.UserDAO;
import models.enums.UserRole;
import models.people.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAOImpl implements UserDAO {

    @Override
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, role, full_name, email, " +
                "is_active, created_at, linked_entity_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getEmail());
            pstmt.setBoolean(6, user.isActive());
            pstmt.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt()));
            pstmt.setString(8, user.getLinkedEntityId());

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                user.setUserId(rs.getLong(1));
            }

        } catch (SQLException e) {
            throw new SQLException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET password_hash = ?, role = ?, full_name = ?, email = ?, " +
                "is_active = ?, last_login = ?, linked_entity_id = ? WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getPasswordHash());
            pstmt.setString(2, user.getRole().name());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getEmail());
            pstmt.setBoolean(5, user.isActive());
            pstmt.setTimestamp(6, user.getLastLogin() != null ? Timestamp.valueOf(user.getLastLogin()) : null);
            pstmt.setString(7, user.getLinkedEntityId());
            pstmt.setString(8, user.getUsername());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String username) throws SQLException {
        String sql = "UPDATE users SET is_active = false WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new SQLException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new SQLException("Error finding user: " + e.getMessage(), e);
        }
    }

    @Override
    public User findById(Long userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;

        } catch (SQLException e) {
            throw new SQLException("Error finding user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users WHERE is_active = true ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error fetching users: " + e.getMessage(), e);
        }

        return users;
    }

    @Override
    public List<User> findByRole(UserRole role) throws SQLException {
        String sql = "SELECT * FROM users WHERE role = ? AND is_active = true ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new SQLException("Error finding users by role: " + e.getMessage(), e);
        }

        return users;
    }

    @Override
    public User authenticate(String username, String password) throws SQLException {
        User user = findByUsername(username);

        if (user != null && user.isActive() && user.verifyPassword(password)) {
            return user;
        }

        return null;
    }

    @Override
    public void updateLastLogin(String username) throws SQLException {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error updating last login: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new SQLException("Error checking user existence: " + e.getMessage(), e);
        }
    }

    @Override
    public void changePassword(String username, String newPasswordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newPasswordHash);
            pstmt.setString(2, username);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new SQLException("Error changing password: " + e.getMessage(), e);
        }
    }

    // Helper method
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        Long userId = rs.getLong("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        boolean isActive = rs.getBoolean("is_active");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null;

        String linkedEntityId = rs.getString("linked_entity_id");

        return new User(userId, username, passwordHash, role, fullName, email,
                isActive, createdAt, lastLogin, linkedEntityId);
    }
}
