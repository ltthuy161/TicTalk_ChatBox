package dao;

import model.LoginHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoginHistoryDAO {
    private Connection connection;

    public LoginHistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public void createLoginHistory(LoginHistory history) throws SQLException {
        String sql = "INSERT INTO LoginHistory (UserID, LoginTime) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, history.getUserID());
            stmt.setTimestamp(2, history.getLoginTime());
            stmt.executeUpdate();
        }
    }

    public List<LoginHistory> getLoginHistoryByUserId(int userId) throws SQLException {
        List<LoginHistory> histories = new ArrayList<>();
        String sql = "SELECT * FROM LoginHistory WHERE UserID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LoginHistory history = new LoginHistory();
                history.setLoginID(rs.getInt("LoginID"));
                history.setUserID(rs.getInt("UserID"));
                history.setLoginTime(rs.getTimestamp("LoginTime"));
                histories.add(history);
            }
        }
        return histories;
    }
}
