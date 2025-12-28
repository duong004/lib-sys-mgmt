package database.dao;

import models.people.Reader;

import java.sql.SQLException;
import java.util.List;

public interface ReaderDAO {
    /**
     * Save a new reader to database
     * @param reader Reader to save
     * @throws SQLException if save fails
     */
    void save(Reader reader) throws SQLException;

    /**
     * Update existing reader
     * @param reader Reader with updated data
     * @throws SQLException if update fails
     */
    void update(Reader reader) throws SQLException;

    /**
     * Delete reader by ID
     * @param readerId ID of reader to delete
     * @return true if deleted, false if not found
     * @throws SQLException if delete fails
     */
    boolean delete(String readerId) throws SQLException;

    /**
     * Find reader by ID
     * @param readerId ID to search
     * @return Reader object or null if not found
     * @throws SQLException if query fails
     */
    Reader findById(String readerId) throws SQLException;

    /**
     * Get all readers
     * @return List of all readers
     * @throws SQLException if query fails
     */
    List<Reader> findAll() throws SQLException;

    /**
     * Find readers by membership type
     * @param membershipType Membership type to search
     * @return List of matching readers
     * @throws SQLException if query fails
     */
    List<Reader> findByMembershipType(String membershipType) throws SQLException;

    /**
     * Update reader's borrow count
     * @param readerId Reader ID
     * @param increment Amount to increment (can be negative)
     * @throws SQLException if update fails
     */
    void updateBorrowCount(String readerId, int increment) throws SQLException;

    /**
     * Check if reader exists
     * @param readerId Reader ID
     * @return true if exists
     * @throws SQLException if query fails
     */
    boolean exists(String readerId) throws SQLException;

    /**
     * Get active readers count
     * @return Number of active readers
     * @throws SQLException if query fails
     */
    int getActiveCount() throws SQLException;
}
