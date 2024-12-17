package bus;

import dto.GroupChatMessage;
import dao.UserDAO;
import dto.ChatMessage;
import dto.User;
import dto.Group;

import java.sql.Timestamp;
import java.util.stream.Collectors;
import dto.FriendRequest;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import java.util.*;


public class UserBUS {
    private UserDAO userDAO;

    public UserBUS() {
        userDAO = new UserDAO(); // Assuming you have these DAO classes
    }

    // Generate random password
    public String generateRandomPassword() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private boolean sendPasswordToEmail(String email, String newPassword) {
        String fromEmail = "tictalktoday@gmail.com"; // Replace with your email
        String emailPassword = "xzve mvgd gcxt nqnv"; // Replace with App Password
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3"); // Force TLS versions

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Password Reset Notification");
            message.setText(buildEmailContent(newPassword));

            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String buildEmailContent(String newPassword) {
        return "Dear user,\n\n" +
                "Your temporary password is: " + newPassword + "\n\n" +
                "Please use it to change your password.\n\n" +
                "Best regards,\nSupport Team";
    }


    // Add user with generated password and send email
    public boolean addUser(User user) {
        String rawPassword = generateRandomPassword();
        user.setPassword(rawPassword); // Store the raw password temporarily


        // boolean emailSent = sendPasswordToEmail(user.getEmail(), rawPassword);
        boolean emailSent = sendPasswordToEmail(user.getEmail(), rawPassword);

        if (!emailSent) {
            return false; // Return false if the email failed to send
        }
        // Display the raw password on the terminal (NOT RECOMMENDED for production)
//        System.out.println("*********************************************************");
//        System.out.println("User added successfully! Temporary password: " + rawPassword);
//        System.out.println("Please provide this password to the user.");
//        System.out.println("*********************************************************");

        return userDAO.insertUser(user);
    }

    public boolean isLocked(User user) {
        return userDAO.isLocked(user);
    }

    public boolean updateUserPassword(String username, String newPassword) {
        return userDAO.updateUserPassword(username, newPassword);
    }
    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    public User authenticateUser(String username, String password) {
        // Delegate authentication to UserDAO
        return userDAO.authenticateUser(username, password);
    }

    public void setUserOnline(String username) {
        userDAO.setUserOnline(username);
    }

    public void setUserOffline(String username) {
        userDAO.setUserOffline(username);
    }

    public List<User> getFriendList(String username) {
        return userDAO.getFriendList(username);
    }

    public boolean unfriend(String currentUsername, String usernameToUnfriend) {
        User userToUnfriend = userDAO.getUserByUsername(usernameToUnfriend); // Get User object by username
        if (userToUnfriend == null) {
            return false; // Or handle the error as you see fit
        }
        return userDAO.unfriend(currentUsername, userToUnfriend.getUsername());
    }

    public boolean blockUser(String blockerUsername, String usernameToBlock) {
        User userToBlock = userDAO.getUserByUsername(usernameToBlock);
        if (userToBlock == null) {
            return false; // Or handle error as you see fit
        }

        // Unfriend first
        boolean unfriended = unfriend(blockerUsername, usernameToBlock);
        if (!unfriended) {
            // Handle error (maybe log it)
            System.err.println("Error unfriending user before blocking.");
            return false; // Or handle differently, depending on your requirements
        }

        // Then block
        return userDAO.blockUser(blockerUsername, userToBlock.getUsername());
    }

    public boolean blockUserWithoutUnfriend(String blockerUsername, String usernameToBlock) {
        User userToBlock = userDAO.getUserByUsername(usernameToBlock);
        if (userToBlock == null) {
            return false; // Or handle error as you see fit
        }
        return userDAO.blockUser(blockerUsername, userToBlock.getUsername());
    }

    public List<User> searchUsersByUsername(String username, String searchText) {
        return userDAO.searchUsersByUsername(username, searchText);
    }

    public boolean isFriend(String username1, String username2) {
        return userDAO.isFriend(username1, username2);
    }

    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    public boolean sendFriendRequest(String senderUsername, String receiverUsername) {
        return userDAO.insertFriendRequest(senderUsername, receiverUsername);
    }

    public List<FriendRequest> getFriendRequests(String username) {
        return userDAO.getFriendRequestsByReceiver(username);
    }

    public boolean acceptFriendRequest(int requestId) {
        // Get the friend request details
        FriendRequest request = userDAO.getFriendRequestById(requestId);
        if (request == null || !request.getStatus().equals("Pending")) {
            return false; // Request not found or not pending
        }

        // Add to friends table
        boolean addedToFriends = userDAO.addFriend(request.getSenderUsername(), request.getReceiverUsername());
        if (!addedToFriends) {
            return false; // Failed to add to friends table
        }

        // Update the request status to 'Accepted'
        return userDAO.updateFriendRequestStatus(requestId, "Accepted");
    }

    public boolean declineFriendRequest(int requestId) {
        // Update the request status to 'Rejected'
        return userDAO.updateFriendRequestStatus(requestId, "Rejected");
    }

    // In UserBUS.java
    public boolean sendMessage(String sender, String receiver, String message) {
        return userDAO.sendMessage(sender, receiver, message);
    }

    public List<ChatMessage> getChatMessages(String user1, String user2) {
        return userDAO.getChatMessages(user1, user2);
    }

    public List<User> getOnlineFriends(String username) {
            return userDAO.getOnlineFriends(username);
    }

    public List<ChatMessage> searchAllMessages(String searchText, String currentUsername) {
        return userDAO.searchUserMessages(searchText, currentUsername);
    }

    public List<ChatMessage> searchMessages(String searchText, String currentUsername, String otherUsername) {
        return userDAO.searchMessages(searchText, currentUsername, otherUsername);
    }

    public boolean createSpamReport(String reporter, String reported, String reportedMessage) {
        return userDAO.insertSpamReport(reporter, reported, reportedMessage);
    }

    public boolean clearChatHistory(String user1, String user2) {
        return userDAO.deleteChatHistory(user1, user2);
    }

    public int createGroupChat(String groupName, String creatorUsername, List<String> memberUsernames) {
        // 1. Validate friendships
        for (String memberUsername : memberUsernames) {
            if (!isFriend(creatorUsername, memberUsername)) {
                JOptionPane.showMessageDialog(null, "Error: " + memberUsername + " is not a friend of " + creatorUsername, "Error", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        }

        // 2. Create the group using the Group DTO
        Group group = new Group();
        group.setGroupName(groupName);
        group.setCreatedBy(creatorUsername);

        int groupId = userDAO.createGroup(group);
        if (groupId == -1) {
            return -1; // Group creation failed
        }

        // 3. Add participants
        if (!userDAO.addGroupParticipant(groupId, creatorUsername, "Admin")) {
            return -1; // Adding creator as admin failed
        }
        for (String memberUsername : memberUsernames) {
            if (!userDAO.addGroupParticipant(groupId, memberUsername, "Member")) {
                return -1; // Adding member failed
            }
        }

        return groupId;
    }

    public boolean addMemberToGroup(int groupId, String adminUsername, String newMemberUsername) {
        // Verify admin
        if (!isAdmin(groupId, adminUsername)) {
            JOptionPane.showMessageDialog(null, "Error: Only admins can add members.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verify friendship
        if (!isFriend(adminUsername, newMemberUsername)) {
            JOptionPane.showMessageDialog(null, "Error: " + newMemberUsername + " is not a friend of " + adminUsername, "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Add member
        return userDAO.addGroupParticipant(groupId, newMemberUsername, "Member");
    }

    public boolean removeMemberFromGroup(int groupId, String adminUsername, String memberToRemoveUsername) {
        // Verify admin
        if (!isAdmin(groupId, adminUsername)) {
            JOptionPane.showMessageDialog(null, "Error: Only admins can remove members.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Prevent removing self
        if (adminUsername.equals(memberToRemoveUsername)) {
            JOptionPane.showMessageDialog(null, "Error: Admins cannot remove themselves.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verify that the member is not an admin
        if (isAdmin(groupId, memberToRemoveUsername)) {
            JOptionPane.showMessageDialog(null, "Error: Cannot remove an admin. Demote them first.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Remove member
        return userDAO.removeGroupParticipant(groupId, memberToRemoveUsername);
    }

    public boolean promoteMemberToAdmin(int groupId, String adminUsername, String memberToPromoteUsername) {
        // Verify admin
        if (!isAdmin(groupId, adminUsername)) {
            JOptionPane.showMessageDialog(null, "Error: Only admins can promote members.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verify member
        if (!userDAO.isGroupMember(groupId, memberToPromoteUsername)) {
            JOptionPane.showMessageDialog(null, "Error: " + memberToPromoteUsername + " is not a member of this group.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Promote member
        return userDAO.updateGroupParticipantRole(groupId, memberToPromoteUsername, "Admin");
    }

    public boolean demoteAdmin(int groupId, String currentAdmin, String adminToDemote) {
        // Verify admin
        if (!isAdmin(groupId, currentAdmin)) {
            JOptionPane.showMessageDialog(null, "Error: Only admins can demote other admins.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Verify admin to demote
        if (!isAdmin(groupId, adminToDemote)) {
            JOptionPane.showMessageDialog(null, "Error: " + adminToDemote + " is not an admin.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Prevent self-demotion (optional, you might allow it)
        if (currentAdmin.equals(adminToDemote)) {
            JOptionPane.showMessageDialog(null, "Error: You cannot demote yourself.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Demote admin
        return userDAO.updateGroupParticipantRole(groupId, adminToDemote, "Member");
    }

    public List<Group> getGroupChatsForUser(String username) {
        return userDAO.getGroupsByParticipant(username);
    }

    public List<User> getGroupMembers(int groupId) {
        return userDAO.getGroupMembers(groupId);
    }

    public boolean isAdmin(int groupId, String username) {
        return userDAO.isAdmin(groupId, username);
    }

    public boolean sendGroupMessage(int groupId, String senderUsername, String message) {
        return userDAO.sendGroupMessage(groupId, senderUsername, message);
    }

    public List<GroupChatMessage> getGroupChatMessages(int groupId) {
        return userDAO.getGroupChatMessages(groupId);
    }

    public List<String> getChattedUsers(String username) {
        return userDAO.findChattedUsers(username);
    }

    public boolean updateGroupName(int groupId, String newGroupName) {
        return userDAO.updateGroupName(groupId, newGroupName);
    }

    public boolean removeMessage(String currentusername, String otherusername, Timestamp timestamp) {
        return userDAO.removeMessage(currentusername, otherusername, timestamp);
    }

}
