package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePasswordDialog {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public UpdatePasswordDialog(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Please select an account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Convert view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Get current username and password
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String currentPassword = (String) tableModel.getValueAt(selectedRow, 6);

        // Prompt user for the new password
        String newPassword = showPasswordInputDialog(username);

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // Update the password in the table
            tableModel.setValueAt(newPassword, selectedRow, 6);

            // Update the password in the database
            if (updatePasswordInDatabase(username, newPassword)) {
                JOptionPane.showMessageDialog(parentPanel, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(parentPanel, "Failed to update the password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String showPasswordInputDialog(String username) {
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("Update password for user: " + username);
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        dialogPanel.add(infoLabel);
        dialogPanel.add(new JLabel("New Password:"));
        dialogPanel.add(newPasswordField);
        dialogPanel.add(new JLabel("Confirm Password:"));
        dialogPanel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(parentPanel, dialogPanel, "Update Password", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(parentPanel, "Both fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(parentPanel, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return newPassword;
        }

        return null;
    }

    private boolean updatePasswordInDatabase(String username, String newPassword) {
        Connection conn = DBConnection.getConnection();

        if (conn == null) {
            JOptionPane.showMessageDialog(parentPanel, "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String sql = "UPDATE users SET password = ? WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword); // Use hashing if needed
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Password updated successfully for user: " + username);
                return true;
            } else {
                System.out.println("Failed to update password for user: " + username);
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error updating password in the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}