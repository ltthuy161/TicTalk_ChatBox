package dao;

import model.LogOutHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogOutHistoryDAO {
    private Connection connection;

    public LogOutHistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public void recordLogOut(LogOutHistory logOut) throws SQLException {
        String sql = "INSERT INTO LogOutHistory (UserID, LogOutTime) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, logOut.getUserID());
            stmt.setTimestamp(2, logOut.getLogOutTime());
            stmt.executeUpdate();
        }
    }

    public List<LogOutHistory> getLogOutHistoryByUserId(int userId) throws SQLException {
        List<LogOutHistory> logOutHistoryList = new ArrayList<>();
        String sql = "SELECT * FROM LogOutHistory WHERE UserID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LogOutHistory logOut = new LogOutHistory();
                logOut.setLogOutID(rs.getInt("LogOutID"));
                logOut.setUserID(rs.getInt("UserID"));
                logOut.setLogOutTime(rs.getTimestamp("LogOutTime"));
                logOutHistoryList.add(logOut);
            }
        }
        return logOutHistoryList;
    }
}
