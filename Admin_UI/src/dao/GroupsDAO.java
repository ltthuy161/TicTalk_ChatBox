package dao;

import model.Groups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupsDAO {
    private Connection connection;

    public GroupsDAO(Connection connection) {
        this.connection = connection;
    }

    public void createGroup(Groups group) throws SQLException {
        String sql = "INSERT INTO Groups (GroupName, CreatedBy, CreatedAt) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, group.getGroupName());
            stmt.setInt(2, group.getCreatedBy());
            stmt.setTimestamp(3, group.getCreatedAt());
            stmt.executeUpdate();
        }
    }

    public Groups getGroupById(int groupId) throws SQLException {
        String sql = "SELECT * FROM Groups WHERE GroupID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Groups group = new Groups();
                group.setGroupID(rs.getInt("GroupID"));
                group.setGroupName(rs.getString("GroupName"));
                group.setCreatedBy(rs.getInt("CreatedBy"));
                group.setCreatedAt(rs.getTimestamp("CreatedAt"));
                return group;
            }
        }
        return null;
    }

    public List<Groups> getAllGroups() throws SQLException {
        List<Groups> groupsList = new ArrayList<>();
        String sql = "SELECT * FROM Groups";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Groups group = new Groups();
                group.setGroupID(rs.getInt("GroupID"));
                group.setGroupName(rs.getString("GroupName"));
                group.setCreatedBy(rs.getInt("CreatedBy"));
                group.setCreatedAt(rs.getTimestamp("CreatedAt"));
                groupsList.add(group);
            }
        }
        return groupsList;
    }
}
