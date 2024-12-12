package bus;

import dao.UserDAO;
import dto.User;
import dto.FriendRequest;
import java.util.Random;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

public class UserBUS {
    private UserDAO userDAO;

    public UserBUS() {
        userDAO = new UserDAO(); // Assuming you have these DAO classes
    }

    // Generate random password
    public String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private boolean sendPasswordEmail(String toEmail, String password) {
        // Email configuration (replace with your actual email settings)
        String username = "tictalktoday@gmail.com"; // Your email address
        String passwordEmail = "tictalk123"; // Your email password

        //Get properties object
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "578");
        //get Session
        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Create a new message
            MimeMessage message = new MimeMessage(session); // This line should work now
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Temporary Password");
            message.setText("Your temporary password is: " + password);

            // Send the message
            Transport.send(message);

            System.out.println("Email sent successfully to " + toEmail);
            return true; // Email sent successfully

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            return false; // Email sending failed
        }
    }

    // Add user with generated password and send email
    public boolean addUser(User user) {
        String rawPassword = generateRandomPassword();
        user.setPassword(rawPassword); // Store the raw password temporarily

        // Send the raw password to the user via email
//        boolean emailSent = sendPasswordEmail(user.getEmail(), rawPassword);
//
//        if (!emailSent) {
//            return false; // Return false if the email failed to send
//        }
        // Display the raw password on the terminal (NOT RECOMMENDED for production)
        System.out.println("*********************************************************");
        System.out.println("User added successfully! Temporary password: " + rawPassword);
        System.out.println("Please provide this password to the user.");
        System.out.println("*********************************************************");

        return userDAO.insertUser(user);
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

}
