package dao;

import model.GroupAdmins;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupAdminsDAO {
    private Connection connection;

    public GroupAdminsDAO(Connection connection) {
        this.connection = connection;
    }

    public void createGroupAdmin(GroupAdmins admin) throws SQLException {
        String sql = "INSERT INTO GroupAdmins (GroupID, AdminID) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, admin.getGroupID());   // Set GroupID
            stmt.setInt(2, admin.getAdminID());   // Set AdminID
            stmt.executeUpdate();
        }
    }

    public List<GroupAdmins> getGroupAdminsByGroupId(int groupId) throws SQLException {
        List<GroupAdmins> adminsList = new ArrayList<>();
        String sql = "SELECT * FROM GroupAdmins WHERE GroupID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GroupAdmins admin = new GroupAdmins();
                admin.setGroupAdminID(rs.getInt("GroupAdminID"));
                admin.setGroupID(rs.getInt("GroupID"));
                admin.setAdminID(rs.getInt("AdminID"));
                adminsList.add(admin);
            }
        }
        return adminsList;
    }
}
