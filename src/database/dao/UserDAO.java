package database.dao;

import models.enums.UserRole;
import models.people.User;

import java.sql.SQLException;
import java.util.List;

public interface UserDAO {

    /**
     * Save a new user
     */
    void save(User user) throws SQLException;

    /**
     * Update existing user
     */
    void update(User user) throws SQLException;

    /**
     * Delete user (soft delete - set inactive)
     */
    boolean delete(String username) throws SQLException;

    /**
     * Find user by username
     */
    User findByUsername(String username) throws SQLException;

    /**
     * Find user by ID
     */
    User findById(Long userId) throws SQLException;

    /**
     * Get all users
     */
    List<User> findAll() throws SQLException;

    /**
     * Find users by role
     */
    List<User> findByRole(UserRole role) throws SQLException;

    /**
     * Authenticate user (check username and password)
     */
    User authenticate(String username, String password) throws SQLException;

    /**
     * Update last login timestamp
     */
    void updateLastLogin(String username) throws SQLException;

    /**
     * Check if username exists
     */
    boolean exists(String username) throws SQLException;

    /**
     * Change user password
     */
    void changePassword(String username, String newPasswordHash) throws SQLException;
}
