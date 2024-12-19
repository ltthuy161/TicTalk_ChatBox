package gui;

import utils.DBConnection;

import javax.mail.*;
import javax.mail.internet.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

public class ResetPasswordHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public ResetPasswordHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            showError("Please select an account!");
            return;
        }

        selectedRow = userTable.convertRowIndexToModel(selectedRow);
        String username = getValueAt(selectedRow, 0);
        String email = getValueAt(selectedRow, 5);

        if (email == null || email.trim().isEmpty()) {
            showError("Selected account does not have a valid email.");
            return;
        }

        String newPassword = generateRandomPassword(10);

        if (updatePasswordInDatabase(username, newPassword)) {
            if (sendPasswordToEmail(email, newPassword)) {
                showSuccess("Password reset successfully! New password sent to: " + email);
            } else {
                showError("Failed to send the email. Please try again.");
            }
        } else {
            showError("Failed to reset the password in the database.");
        }
    }

    private String getValueAt(int row, int column) {
        return (String) tableModel.getValueAt(row, column);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(parentPanel, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(parentPanel, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private String generateRandomPassword(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        return password.toString();
    }

    private boolean updatePasswordInDatabase(String username, String newPassword) {
        String query = "UPDATE Users SET Password = ? WHERE Username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, username);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            showError("Database error while updating the password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean sendPasswordToEmail(String email, String newPassword) {
        String fromEmail = "tictalktoday@gmail.com"; // Replace with your email
        String emailPassword = "nind hnpu btkf oztb"; // Replace with App Password
        String host = "smtp.gmail.com";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");

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
                "Your password has been reset successfully.\n" +
                "Your new password is: " + newPassword + "\n\n" +
                "Please change it after logging in.\n\n" +
                "Best regards,\nSupport Team";
    }
}