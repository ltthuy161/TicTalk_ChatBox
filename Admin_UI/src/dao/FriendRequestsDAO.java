package dao;

import model.FriendRequests;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestsDAO {
    private Connection connection;

    public FriendRequestsDAO(Connection connection) {
        this.connection = connection;
    }

    public void sendFriendRequest(FriendRequests request) throws SQLException {
        String sql = "INSERT INTO FriendRequests (SenderID, ReceiverID, Status, RequestTime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, request.getSenderID());
            stmt.setInt(2, request.getReceiverID());
            stmt.setString(3, request.getStatus());
            stmt.setTimestamp(4, request.getRequestTime());
            stmt.executeUpdate();
        }
    }

    public List<FriendRequests> getRequestsByUserId(int userId) throws SQLException {
        List<FriendRequests> requestsList = new ArrayList<>();
        String sql = "SELECT * FROM FriendRequests WHERE SenderID = ? OR ReceiverID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FriendRequests request = new FriendRequests();
                request.setRequestID(rs.getInt("RequestID"));
                request.setSenderID(rs.getInt("SenderID"));
                request.setReceiverID(rs.getInt("ReceiverID"));
                request.setStatus(rs.getString("Status"));
                request.setRequestTime(rs.getTimestamp("RequestTime"));
                requestsList.add(request);
            }
        }
        return requestsList;
    }

    public void updateFriendRequestStatus(int requestId, String status) throws SQLException {
        String sql = "UPDATE FriendRequests SET Status = ? WHERE RequestID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, requestId);
            stmt.executeUpdate();
        }
    }
}
