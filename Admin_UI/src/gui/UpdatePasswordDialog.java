package gui;

import utils.DBConnection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePasswordDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public UpdatePasswordDialog(JPanel parentPanel, JTable userTable) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Select an account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) userTable.getValueAt(selectedRow, 0);

        // Create a dialog to update password
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Update password", true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("Update password for user: " + username);
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JButton updateButton = new JButton("Update");
        JButton cancelButton = new JButton("Cancel");

        dialog.add(infoLabel);
        dialog.add(new JLabel("New password:"));
        dialog.add(newPasswordField);
        dialog.add(new JLabel("Confirm password:"));
        dialog.add(confirmPasswordField);
        dialog.add(updateButton);
        dialog.add(cancelButton);

        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);

        // Handle update button click
        updateButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter full password!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Wrong password!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Update password in the database
                updatePasswordInDatabase(username, newPassword);
                JOptionPane.showMessageDialog(dialog, "Update password successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });

        // Handle cancel button click
        cancelButton.addActionListener(e -> dialog.dispose());
    }

    private void updatePasswordInDatabase(String username, String newPassword) {
        Connection conn = DBConnection.getConnection();
        String sql = "UPDATE users SET password = ? WHERE username = ?";  // Assuming a 'password' column exists in your table

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Password updated successfully for user: " + username);
            } else {
                System.out.println("Error: Unable to update password for user: " + username);
            }
        } catch (SQLException e) {
            System.out.println("Error: Unable to update password in database.");
            e.printStackTrace();
        }
    }
}
