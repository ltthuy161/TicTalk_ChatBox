package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class LockUnlockHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public LockUnlockHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Select an account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Map view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Get current username and status
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 7);

        // Toggle status
        String newStatus = "".equals(currentStatus) ? "Locked" : "";

        // Update the status in the table
        tableModel.setValueAt(newStatus, selectedRow, 7);

        // Update the status in the database
        updateUserStatusInDatabase(username, newStatus);

        // Show the result message
        String message = newStatus.isEmpty() ? "Unlock account successfully!" : "This account has been locked!";
        JOptionPane.showMessageDialog(parentPanel, message, "Announcement", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateUserStatusInDatabase(String username, String newStatus) {
        Connection conn = DBConnection.getConnection();

        String sql = "UPDATE users SET status = ? WHERE username = ?";  // Adjust table/column names if necessary
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User status updated successfully.");
            } else {
                System.out.println("Error: Unable to update user status.");
            }
        } catch (SQLException e) {
            System.out.println("Error: Unable to update user status in database.");
            e.printStackTrace();
        }
    }
}
