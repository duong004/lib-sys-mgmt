package database.impl;

import database.config.DatabaseConfig;
import database.dao.LibrarianDAO;
import models.people.Librarian;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibrarianDAOImpl implements LibrarianDAO {

    @Override
    public void save(Librarian librarian) throws SQLException {
        if (librarian.getEmployeeId() == null || librarian.getEmployeeId().isEmpty()) {
            librarian.setEmployeeId(generateNextLibrarianId());
        }

        String sql = "INSERT INTO librarians (employee_id, name, email, phone, address, position, hire_date, salary, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_DATE, ?, TRUE)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, librarian.getEmployeeId());
            pstmt.setString(2, librarian.getName());
            pstmt.setString(3, librarian.getEmail());
            pstmt.setString(4, librarian.getPhone());
            pstmt.setString(5, librarian.getAddress());
            pstmt.setString(6, librarian.getPosition());
            pstmt.setDouble(7, 0.0); // Mặc định lương 0, có thể cập nhật sau

            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Librarian librarian) throws SQLException {
        String sql = "UPDATE librarians SET name = ?, email = ?, phone = ?, address = ?, position = ?, updated_at = CURRENT_TIMESTAMP " +
                "WHERE employee_id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, librarian.getName());
            pstmt.setString(2, librarian.getEmail());
            pstmt.setString(3, librarian.getPhone());
            pstmt.setString(4, librarian.getAddress());
            pstmt.setString(5, librarian.getPosition());
            pstmt.setString(6, librarian.getEmployeeId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public boolean delete(String employeeId) throws SQLException {
        // Xóa mềm: Set is_active = false
        String sql = "UPDATE librarians SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public Librarian findById(String employeeId) throws SQLException {
        String sql = "SELECT * FROM librarians WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToLibrarian(rs);
        }
        return null;
    }

    @Override
    public Librarian findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM librarians WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToLibrarian(rs);
        }
        return null;
    }

    @Override
    public List<Librarian> findAll() throws SQLException {
        String sql = "SELECT * FROM librarians WHERE is_active = true ORDER BY employee_id";
        List<Librarian> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapResultSetToLibrarian(rs));
        }
        return list;
    }

    @Override
    public String generateNextLibrarianId() throws SQLException {
        String sql = "SELECT employee_id FROM librarians WHERE employee_id LIKE 'LIB%' ORDER BY employee_id DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                String lastId = rs.getString("employee_id");
                int nextNum = Integer.parseInt(lastId.substring(3)) + 1;
                return String.format("LIB%03d", nextNum);
            }
            return "LIB001";
        }
    }

    @Override
    public boolean exists(String employeeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM librarians WHERE employee_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private Librarian mapResultSetToLibrarian(ResultSet rs) throws SQLException {
        Librarian lib = new Librarian(
                rs.getString("employee_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("position")
        );
        lib.setAddress(rs.getString("address"));
        // Librarian model của bạn có thể cần bổ sung thêm field salary/hireDate nếu muốn hiển thị
        return lib;
    }
}
