package database.dao;

import models.BookInventoryLog;

import java.sql.SQLException;
import java.util.List;

public interface BookInventoryLogDAO {

    /**
     * Save a new inventory log entry
     */
    void save(BookInventoryLog log) throws SQLException;

    /**
     * Get all logs for a specific ISBN
     */
    List<BookInventoryLog> findByISBN(String isbn) throws SQLException;

    /**
     * Get all logs performed by a specific librarian
     */
    List<BookInventoryLog> findByPerformer(String librarianId) throws SQLException;

    /**
     * Get recent logs (last N entries)
     */
    List<BookInventoryLog> findRecent(int limit) throws SQLException;

    /**
     * Get all logs
     */
    List<BookInventoryLog> findAll() throws SQLException;
}