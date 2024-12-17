package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database connection details
    private static final String URL = "jdbc:postgresql://localhost:5432/ChatSystem";  // Change if needed
    private static final String USER = "postgres";  // Replace with your DB username
    private static final String PASSWORD = "0803";  // Replace with your DB password
    private static Connection connection;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    // Method to get the connection
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("Opening new database connection...");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } else {
                System.out.println("Reusing existing connection.");
            }
        } catch (SQLException e) {
            System.out.println("Error: Unable to connect to the database.");
            e.printStackTrace();
        }
        return connection;
    }

    // Method to close the connection (optional, for cleanup)
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing the database connection.");
                e.printStackTrace();
            }
        }
    }
}

