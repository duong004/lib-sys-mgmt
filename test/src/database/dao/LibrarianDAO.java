package database.dao;

import models.people.Librarian;

import java.sql.SQLException;
import java.util.List;

public interface LibrarianDAO {
    void save(Librarian librarian) throws SQLException;
    void update(Librarian librarian) throws SQLException;
    boolean delete(String employeeId) throws SQLException; // Xóa mềm (set is_active = false)
    Librarian findById(String employeeId) throws SQLException;
    Librarian findByEmail(String email) throws SQLException;
    List<Librarian> findAll() throws SQLException;
    String generateNextLibrarianId() throws SQLException;
    boolean exists(String employeeId) throws SQLException;
}
