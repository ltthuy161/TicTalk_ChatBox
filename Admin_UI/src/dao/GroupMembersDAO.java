package dao;

import model.GroupMembers;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMembersDAO {
    private Connection connection;

    public GroupMembersDAO(Connection connection) {
        this.connection = connection;
    }

    public void addMemberToGroup(GroupMembers member) throws SQLException {
        String sql = "INSERT INTO GroupMembers (GroupID, UserID, Role) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, member.getGroupID());
            stmt.setInt(2, member.getUserID());
            stmt.setString(3, member.getRole());
            stmt.executeUpdate();
        }
    }

    public List<GroupMembers> getMembersByGroupId(int groupId) throws SQLException {
        List<GroupMembers> membersList = new ArrayList<>();
        String sql = "SELECT * FROM GroupMembers WHERE GroupID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GroupMembers member = new GroupMembers();
                member.setGroupMemberID(rs.getInt("GroupMemberID"));
                member.setGroupID(rs.getInt("GroupID"));
                member.setUserID(rs.getInt("UserID"));
                member.setRole(rs.getString("Role"));
                membersList.add(member);
            }
        }
        return membersList;
    }
}
