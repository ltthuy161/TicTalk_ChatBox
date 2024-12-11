package dao;

import model.BlockedUsers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlockedUsersDAO {
    private Connection connection;

    public BlockedUsersDAO(Connection connection) {
        this.connection = connection;
    }

    public void blockUser(BlockedUsers block) throws SQLException {
        String sql = "INSERT INTO BlockedUsers (BlockerID, BlockedID, BlockedAt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, block.getBlockerID());
            stmt.setInt(2, block.getBlockedID());
            stmt.setTimestamp(3, block.getBlockedAt());
            stmt.executeUpdate();
        }
    }

    public List<BlockedUsers> getBlockedUsersByUserId(int userId) throws SQLException {
        List<BlockedUsers> blockedUsersList = new ArrayList<>();
        String sql = "SELECT * FROM BlockedUsers WHERE BlockerID = ? OR BlockedID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BlockedUsers block = new BlockedUsers();
                block.setBlockID(rs.getInt("BlockID"));
                block.setBlockerID(rs.getInt("BlockerID"));
                block.setBlockedID(rs.getInt("BlockedID"));
                block.setBlockedAt(rs.getTimestamp("BlockedAt"));
                blockedUsersList.add(block);
            }
        }
        return blockedUsersList;
    }
}
