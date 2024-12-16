package dao;

import dto.GroupChatMessage;
import dto.ChatMessage;
import dto.User;
import dto.Group;
import dto.FriendRequest;

import java.sql.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;


public class UserDAO {
    public boolean insertUser(User user) {
        String sql = "INSERT INTO Users (Username, FullName, Address, DateOfBirth, Gender, Email, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getAddress());
            stmt.setDate(4, user.getDateOfBirth() != null ? new java.sql.Date(user.getDateOfBirth().getTime()) : null);
            stmt.setString(5, user.getGender());
            stmt.setString(6, user.getEmail());
            stmt.setString(7, user.getPassword());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserPassword(String username, String newPassword) {
        String sql = "UPDATE Users SET Password = ? WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword); // Storing plain text password
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM Users WHERE Email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setAddress(rs.getString("Address"));
                    user.setDateOfBirth(rs.getDate("DateOfBirth"));
                    user.setGender(rs.getString("Gender"));
                    user.setEmail(rs.getString("Email"));
                    user.setPassword(rs.getString("Password"));
                    user.setStatus(rs.getString("Status"));
                    user.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // User found, create and return User object
                    User user = new User();
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setAddress(rs.getString("Address"));
                    user.setDateOfBirth(rs.getDate("DateOfBirth"));
                    user.setGender(rs.getString("Gender"));
                    user.setEmail(rs.getString("Email"));
                    user.setPassword(rs.getString("Password"));
                    user.setStatus(rs.getString("Status"));
                    user.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    return user;
                } else {
                    // User not found or incorrect password
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception
            return null;
        }
    }

    public boolean setUserOnline(String username) {
        String sql = "UPDATE Users SET IsOnline = TRUE WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately (log, rethrow, etc.)
            return false;
        }
    }

    public boolean setUserOffline(String username) {
        String sql = "UPDATE Users SET IsOnline = FALSE WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately (log, rethrow, etc.)
            return false;
        }
    }

    // In UserDAO class:
    public List<User> getFriendList(String username) {
        List<User> friends = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                "INNER JOIN Friends f ON (u.Username = f.Username1 AND f.Username2 = ?) " +
                "OR (u.Username = f.Username2 AND f.Username1 = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User friend = new User();
                    friend.setUsername(rs.getString("Username"));
                    friend.setFullName(rs.getString("FullName"));
                    friends.add(friend);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return friends;
    }

    public boolean unfriend(String username1, String username2) {
        String sql = "DELETE FROM Friends WHERE (Username1 = ? AND Username2 = ?) OR (Username1 = ? AND Username2 = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username1);
            stmt.setString(2, username2);
            stmt.setString(3, username2);
            stmt.setString(4, username1);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately (log it, possibly rethrow a custom exception)
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM Users WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setAddress(rs.getString("Address"));
                    user.setDateOfBirth(rs.getDate("DateOfBirth"));
                    user.setGender(rs.getString("Gender"));
                    user.setEmail(rs.getString("Email"));
                    user.setPassword(rs.getString("Password"));
                    user.setStatus(rs.getString("Status"));
                    user.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
        return null;
    }

    public boolean blockUser(String blockerUsername, String blockedUsername) {
        String sql = "INSERT INTO BlockedUsers (BlockerUsername, BlockedUsername) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, blockerUsername);
            stmt.setString(2, blockedUsername);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately (log, rethrow, etc.)
            return false;
        }
    }

    public List<User> searchUsersByUsername(String username, String searchText) {
        List<User> results = new ArrayList<>();
        String sql = "SELECT * FROM Users u WHERE u.Username LIKE ? AND (u.Username NOT IN (SELECT BlockedUsername FROM BlockedUsers WHERE BlockerUsername = ?)" +
                "AND u.Username NOT IN (SELECT BlockerUsername FROM BlockedUsers WHERE BlockedUsername = ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%"); // Use % for partial matching
            stmt.setString(2, username);
            stmt.setString(3, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setAddress(rs.getString("Address"));
                    user.setDateOfBirth(rs.getDate("DateOfBirth"));
                    user.setGender(rs.getString("Gender"));
                    user.setEmail(rs.getString("Email"));
                    user.setPassword(rs.getString("Password"));
                    user.setStatus(rs.getString("Status"));
                    user.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    results.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
        return results;
    }

    public boolean isFriend(String username1, String username2) {
        String sql = "SELECT 1 FROM Friends WHERE " +
                "(Username1 = ? AND Username2 = ?) OR (Username1 = ? AND Username2 = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username1);
            stmt.setString(2, username2);
            stmt.setString(3, username2);
            stmt.setString(4, username1);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // Returns true if a row is found (friendship exists)
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
        return false; // Default to false if no friendship or error
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET FullName = ?, Address = ?, DateOfBirth = ?, Gender = ?, Email = ? WHERE Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getAddress());
            stmt.setDate(3, user.getDateOfBirth() != null ? new java.sql.Date(user.getDateOfBirth().getTime()) : null);
            stmt.setString(4, user.getGender());
            stmt.setString(5, user.getEmail());
            stmt.setString(6, user.getUsername());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
            return false;
        }
    }

    public boolean insertFriendRequest(String senderUsername, String receiverUsername) {
        String sql = "INSERT INTO FriendRequests (SenderUsername, ReceiverUsername) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, senderUsername);
            stmt.setString(2, receiverUsername);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception (log, rethrow, etc.)
            return false;
        }
    }

    public List<FriendRequest> getFriendRequestsByReceiver(String receiverUsername) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM FriendRequests WHERE ReceiverUsername = ? AND Status = 'Pending' ORDER BY SenderUsername ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, receiverUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FriendRequest request = new FriendRequest(
                            rs.getInt("RequestID"),
                            rs.getString("SenderUsername"),
                            rs.getString("ReceiverUsername"),
                            rs.getString("Status"),
                            rs.getTimestamp("RequestTime")
                    );
                    requests.add(request);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
        return requests;
    }

    public boolean updateFriendRequestStatus(int requestId, String status) {
        String sql = "UPDATE FriendRequests SET Status = ? WHERE RequestID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, requestId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
            return false;
        }
    }

    public boolean addFriend(String username1, String username2) {
        String sql = "INSERT INTO Friends (Username1, Username2) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username1);
            stmt.setString(2, username2);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
            return false;
        }
    }

    public FriendRequest getFriendRequestById(int requestId) {
        String sql = "SELECT * FROM FriendRequests WHERE RequestID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FriendRequest(
                            rs.getInt("RequestID"),
                            rs.getString("SenderUsername"),
                            rs.getString("ReceiverUsername"),
                            rs.getString("Status"),
                            rs.getTimestamp("RequestTime")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }
        return null;
    }

    public boolean sendMessage(String sender, String receiver, String message) {
        String sql = "INSERT INTO PeopleChat (SenderUsername, ReceiverUsername, Message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, message);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception (log, inform user, etc.)
            return false;
        }
    }

    public List<ChatMessage> getChatMessages(String user1, String user2) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT SenderUsername, ReceiverUsername, Message, Timestamp FROM PeopleChat " +
                "WHERE (SenderUsername = ? AND ReceiverUsername = ?) OR (SenderUsername = ? AND ReceiverUsername = ?) " +
                "ORDER BY Timestamp ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage message = new ChatMessage(
                            rs.getString("SenderUsername"),
                            rs.getString("ReceiverUsername"), // Set the receiver
                            rs.getString("Message"),
                            rs.getTimestamp("Timestamp")
                    );
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }
        return messages;
    }

    public List<User> getOnlineFriends(String username) {
        List<User> onlineFriends = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                "INNER JOIN Friends f ON (u.Username = f.Username1 AND f.Username2 = ?) " +
                "OR (u.Username = f.Username2 AND f.Username1 = ?) " +
                "WHERE u.IsOnline = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User friend = new User();
                    friend.setUsername(rs.getString("Username"));
                    friend.setFullName(rs.getString("FullName"));
                    // ... Set other properties if needed ...
                    onlineFriends.add(friend);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return onlineFriends;
    }

    public List<ChatMessage> searchUserMessages(String searchText, String currentUsername) {
        List<ChatMessage> results = new ArrayList<>();
        String sql = "SELECT * FROM PeopleChat WHERE Message LIKE ? AND (SenderUsername = ? OR ReceiverUsername = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, currentUsername);
            stmt.setString(3, currentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage message = new ChatMessage(
                            rs.getString("SenderUsername"),
                            rs.getString("ReceiverUsername"),
                            rs.getString("Message"),
                            rs.getTimestamp("Timestamp")
                    );
                    results.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return results;
    }

    public List<ChatMessage> searchMessages(String searchText, String currentUsername, String otherUsername) {
        List<ChatMessage> results = new ArrayList<>();
        String sql = "SELECT * FROM PeopleChat WHERE Message LIKE ? AND" +
                " ((SenderUsername = ? AND ReceiverUsername = ?) OR (SenderUsername = ? AND ReceiverUsername = ?))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%");
            stmt.setString(2, currentUsername);
            stmt.setString(3, otherUsername);
            stmt.setString(4, otherUsername);
            stmt.setString(5, currentUsername);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChatMessage message = new ChatMessage(
                            rs.getString("SenderUsername"),
                            rs.getString("ReceiverUsername"),
                            rs.getString("Message"),
                            rs.getTimestamp("Timestamp")
                    );
                    results.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
        return results;
    }

    public boolean insertSpamReport(String reporter, String reported, String reportedMessage) {
        String sql = "INSERT INTO SpamReports (ReporterUsername, ReportedUsername, Message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reporter);
            stmt.setString(2, reported);
            stmt.setString(3, reportedMessage); // Get message text from ChatMessage

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Handle exception (log, inform user, etc.)
            return false;
        }
    }

    public boolean deleteChatHistory(String user1, String user2) {
        String sql = "DELETE FROM PeopleChat WHERE (SenderUsername = ? AND ReceiverUsername = ?) OR (SenderUsername = ? AND ReceiverUsername = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user1);
            stmt.setString(2, user2);
            stmt.setString(3, user2);
            stmt.setString(4, user1);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // Returns true if at least one row was deleted (can be improved)

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (log it, perhaps show an error to the user)
            return false;
        }
    }

    public int createGroup(Group group) {
        String sql = "INSERT INTO `Groups` (GroupName, CreatedBy) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, group.getGroupName());
            stmt.setString(2, group.getCreatedBy());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int groupId = generatedKeys.getInt(1);
                    group.setGroupId(groupId);
                    return groupId;
                } else {
                    throw new SQLException("Creating group failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error creating group: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return -1;
        }
    }

    public boolean addGroupParticipant(int groupId, String username, String role) {
        String sql = "INSERT INTO GroupParticipants (GroupID, Username, Role) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            stmt.setString(3, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error adding group participant: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean removeGroupParticipant(int groupId, String username) {
        String sql = "DELETE FROM GroupParticipants WHERE GroupID = ? AND Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error removing group participant: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean updateGroupParticipantRole(int groupId, String username, String role) {
        String sql = "UPDATE GroupParticipants SET Role = ? WHERE GroupID = ? AND Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setInt(2, groupId);
            stmt.setString(3, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error updating group participant role: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<Group> getGroupsByParticipant(String username) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM `Groups` g INNER JOIN GroupParticipants gp ON g.GroupID = gp.GroupID WHERE gp.Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Group group = new Group();
                    group.setGroupId(rs.getInt("GroupID"));
                    group.setGroupName(rs.getString("GroupName"));
                    group.setCreatedBy(rs.getString("CreatedBy"));
                    group.setCreatedAt(rs.getTimestamp("CreatedAt"));
                    groups.add(group);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error getting groups by participant: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return groups;
    }

    public List<User> getGroupMembers(int groupId) {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u INNER JOIN GroupParticipants gp ON u.Username = gp.Username WHERE gp.GroupID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    // Set user attributes (username, etc.)
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    // ... set other attributes as needed ...
                    members.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error getting group members: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return members;
    }

    public boolean isAdmin(int groupId, String username) {
        String sql = "SELECT 1 FROM GroupParticipants WHERE GroupID = ? AND Username = ? AND Role = 'Admin'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error checking if user is admin: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean isGroupMember(int groupId, String username) {
        String sql = "SELECT 1 FROM GroupParticipants WHERE GroupID = ? AND Username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setString(2, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider logging the error or displaying a generic error message
            return false;
        }
    }

    public boolean sendGroupMessage(int groupId, String senderUsername, String message) {
        String sql = "INSERT INTO GroupChat (GroupID, SenderUsername, Message) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);
            stmt.setString(2, senderUsername);
            stmt.setString(3, message);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error sending group message: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<GroupChatMessage> getGroupChatMessages(int groupId) {
        List<GroupChatMessage> messages = new ArrayList<>();
        String sql = "SELECT SenderUsername, Message, GroupId, Timestamp FROM GroupChat " +
                "WHERE (GroupID = ?) ORDER BY Timestamp ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, groupId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GroupChatMessage message = new GroupChatMessage(
                            rs.getString("SenderUsername"),
                            rs.getString("Message"),
                            rs.getInt("GroupID"),
                            rs.getTimestamp("Timestamp")
                    );
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle exception
        }
        return messages;
    }

    public List<String> findChattedUsers(String username) {
        List<String> chattedUsers = new ArrayList<>();
        String sql = "SELECT DISTINCT CASE WHEN SenderUsername = ? THEN ReceiverUsername ELSE SenderUsername END AS OtherUser " +
                "FROM PeopleChat " +
                "WHERE SenderUsername = ? OR ReceiverUsername = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chattedUsers.add(rs.getString("OtherUser"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error finding chatted users: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return chattedUsers;
    }

    public boolean updateGroupName(int groupId, String newGroupName) {
        String sql = "UPDATE `Groups` SET GroupName = ? WHERE GroupID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newGroupName);
            stmt.setInt(2, groupId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error updating group name: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean removeMessage(String currentusername, String otherusername, Timestamp timestamp) {
        String sql = "DELETE FROM PeopleChat WHERE ((SenderUsername = ? AND ReceiverUsername = ?) OR (SenderUsername = ? AND ReceiverUsername = ?)) AND (Timestamp = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, currentusername);
            stmt.setString(2, otherusername);
            stmt.setString(3, otherusername);
            stmt.setString(4, currentusername);
            stmt.setTimestamp(5, timestamp);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0; // Returns true if at least one row was deleted (can be improved)

        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception (log it, perhaps show an error to the user)
            return false;
        }
    }


}

