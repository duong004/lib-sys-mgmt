package database.dao;

import models.books.Book;

import java.sql.SQLException;
import java.util.List;

public interface BookDAO {
    /**
     * Save a new book to database
     * @param book Book to save
     * @throws SQLException if save fails
     */
    void save(Book book) throws SQLException;

    /**
     * Update existing book
     * @param book Book with updated data
     * @throws SQLException if update fails
     */
    void update(Book book) throws SQLException;

    /**
     * Delete book by ISBN
     * @param isbn ISBN of book to delete
     * @return true if deleted, false if not found
     * @throws SQLException if delete fails
     */
    boolean delete(String isbn) throws SQLException;

    /**
     * Find book by ISBN
     * @param isbn ISBN to search
     * @return Book object or null if not found
     * @throws SQLException if query fails
     */
    Book findByISBN(String isbn) throws SQLException;

    /**
     * Get all books
     * @return List of all books
     * @throws SQLException if query fails
     */
    List<Book> findAll() throws SQLException;

    /**
     * Search books by title (partial match)
     * @param title Title to search
     * @return List of matching books
     * @throws SQLException if query fails
     */
    List<Book> searchByTitle(String title) throws SQLException;

    /**
     * Search books by author (partial match)
     * @param author Author to search
     * @return List of matching books
     * @throws SQLException if query fails
     */
    List<Book> searchByAuthor(String author) throws SQLException;

    /**
     * Search books by category
     * @param category Category to search
     * @return List of matching books
     * @throws SQLException if query fails
     */
    List<Book> searchByCategory(String category) throws SQLException;

    /**
     * Get available book count
     * @return Number of available books
     * @throws SQLException if query fails
     */
    int getAvailableCount() throws SQLException;
}
