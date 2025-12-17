package database.dao;

import models.BorrowRecord;

import java.sql.SQLException;
import java.util.List;

public interface BorrowRecordDAO {
    /**
     * Save a new borrow record to database
     * @param record BorrowRecord to save
     * @throws SQLException if save fails
     */
    void save(BorrowRecord record) throws SQLException;

    /**
     * Update existing borrow record
     * @param record BorrowRecord with updated data
     * @throws SQLException if update fails
     */
    void update(BorrowRecord record) throws SQLException;

    /**
     * Delete borrow record by ID
     * @param recordId ID of record to delete
     * @return true if deleted, false if not found
     * @throws SQLException if delete fails
     */
    boolean delete(String recordId) throws SQLException;

    /**
     * Find borrow record by ID
     * @param recordId ID to search
     * @return BorrowRecord object or null if not found
     * @throws SQLException if query fails
     */
    BorrowRecord findById(String recordId) throws SQLException;

    /**
     * Get all borrow records
     * @return List of all records
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findAll() throws SQLException;

    /**
     * Find all borrow records for a reader
     * @param readerId Reader ID
     * @return List of reader's borrow records
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findByReaderId(String readerId) throws SQLException;

    /**
     * Find all borrow records for a book
     * @param isbn Book ISBN
     * @return List of book's borrow records
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findByISBN(String isbn) throws SQLException;

    /**
     * Get all overdue records
     * @return List of overdue records
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findOverdue() throws SQLException;

    /**
     * Get all active (borrowed) records
     * @return List of active borrow records
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findActive() throws SQLException;

    /**
     * Get records by status
     * @param status Status to filter (BORROWED, RETURNED, etc.)
     * @return List of records with given status
     * @throws SQLException if query fails
     */
    List<BorrowRecord> findByStatus(String status) throws SQLException;

    /**
     * Get total borrow count
     * @return Total number of borrow records
     * @throws SQLException if query fails
     */
    int getTotalCount() throws SQLException;

    /**
     * Get active borrow count
     * @return Number of currently borrowed books
     * @throws SQLException if query fails
     */
    int getActiveCount() throws SQLException;
}
