import java.sql.*;

public class DBConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/ChatSystem";
        String username = "postgres";
        String password = "0803";  // Your password

        try {
            // Try to connect
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connection established successfully!");

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
