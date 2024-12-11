package dao;

import model.Friends;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendsDAO {
    private Connection connection;

    public FriendsDAO(Connection connection) {
        this.connection = connection;
    }

    public void createFriendship(Friends friendship) throws SQLException {
        String sql = "INSERT INTO Friends (UserID1, UserID2, Status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, friendship.getUserID1());
            stmt.setInt(2, friendship.getUserID2());
            stmt.setString(3, friendship.getStatus());
            stmt.executeUpdate();
        }
    }

    public List<Friends> getFriendsByUserId(int userId) throws SQLException {
        List<Friends> friendsList = new ArrayList<>();
        String sql = "SELECT * FROM Friends WHERE UserID1 = ? OR UserID2 = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Friends friend = new Friends();
                friend.setFriendshipID(rs.getInt("FriendshipID"));
                friend.setUserID1(rs.getInt("UserID1"));
                friend.setUserID2(rs.getInt("UserID2"));
                friend.setStatus(rs.getString("Status"));
                friendsList.add(friend);
            }
        }
        return friendsList;
    }
}
