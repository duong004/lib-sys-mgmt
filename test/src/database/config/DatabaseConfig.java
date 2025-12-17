package database.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseConfig {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Load properties file
            Properties props = new Properties();
            try (InputStream input = new FileInputStream("src/database/config/config.properties")) {
                props.load(input);
                URL = props.getProperty("db.url");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");
            }

        } catch (ClassNotFoundException e) {
            System.err.println(" PostgreSQL JDBC Driver not found!");
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Failed to load database configuration: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println(" Database connected: " + conn.getCatalog());
            return true;
        } catch (SQLException e) {
            System.err.println(" Connection failed: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. PostgreSQL running? Check: pg_ctl status");
            System.err.println("2. Database exists? Run: CREATE DATABASE library_db;");
            System.err.println("3. Password correct? Update PASSWORD in DatabaseConfig.java");
            return false;
        }
    }


    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // Simple test
    public static void main(String[] args) {
        System.out.println("Testing database connection...");
        if (testConnection()) {
            System.out.println("\n Database setup successful!");

//            // Thử query dữ liệu từ bảng books
//            String sql = "SELECT isbn, title, author, publish_year, category, available_copies FROM books";
//
//            try (Connection conn = getConnection();
//                 Statement stmt = conn.createStatement();
//                 ResultSet rs = stmt.executeQuery(sql)) {
//
//                System.out.println("\nDanh sách sách trong bảng books:");
//                while (rs.next()) {
//                    String isbn = rs.getString("isbn");
//                    String title = rs.getString("title");
//                    String author = rs.getString("author");
//                    int year = rs.getInt("publish_year");
//                    String category = rs.getString("category");
//                    int available = rs.getInt("available_copies");
//
//                    System.out.printf("ISBN: %s | Title: %s | Author: %s | Year: %d | Category: %s | Available: %d%n",
//                            isbn, title, author, year, category, available);
//                }
//
//            } catch (SQLException e) {
//                System.err.println("Error querying books: " + e.getMessage());
//            }

        } else {
            System.out.println("\n Database setup failed!");
        }
    }
}
