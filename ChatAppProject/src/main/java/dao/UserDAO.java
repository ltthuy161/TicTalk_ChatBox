package dao;

import dto.User;
import dto.FriendRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String sql = "SELECT * FROM Users u WHERE u.Username LIKE ? AND u.Username NOT IN (SELECT BlockedUsername FROM BlockedUsers WHERE BlockerUsername = ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + searchText + "%"); // Use % for partial matching
            stmt.setString(2, username);
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

}

