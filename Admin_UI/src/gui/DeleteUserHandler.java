package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeleteUserHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public DeleteUserHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
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

        // Map view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Get username of the selected row
        String username = (String) tableModel.getValueAt(selectedRow, 0);

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(parentPanel,
                "Are you sure you want to delete this account?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Attempt to delete the user and related data
            if (deleteUserFromDatabase(username)) {
                // Remove from the table if successful
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(parentPanel, "Account deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Notify other components to refresh
                notifyUserDeleted(username);
            } else {
                JOptionPane.showMessageDialog(parentPanel, "Failed to delete the account from the database!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean deleteUserFromDatabase(String username) {
        String sql = "DELETE FROM Users WHERE Username = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User " + username + " deleted successfully from the database.");
                return true;
            } else {
                System.out.println("Error: User " + username + " not found in the database.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Error deleting user from database:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentPanel, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void notifyUserDeleted(String username) {
        // Example: You can add logic to notify other parts of the application here
        System.out.println("User " + username + " deleted. Notify other components to refresh.");
        // If you have observers or listeners, invoke them here
    }
}