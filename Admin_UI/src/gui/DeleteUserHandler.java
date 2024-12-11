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
            JOptionPane.showMessageDialog(parentPanel, "Select an account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Map view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Get username of the selected row
        String username = (String) tableModel.getValueAt(selectedRow, 0);

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(parentPanel, "Want to delete this account?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Delete from the table
            tableModel.removeRow(selectedRow);

            // Delete from the database
            deleteUserFromDatabase(username);

            // Show success message
            JOptionPane.showMessageDialog(parentPanel, "This account has been deleted!", "Announcement", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteUserFromDatabase(String username) {
        Connection conn = DBConnection.getConnection();

        String sql = "DELETE FROM users WHERE username = ?";  // Adjust table/column names if necessary
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User deleted successfully from the database.");
            } else {
                System.out.println("Error: Unable to delete user from the database.");
            }
        } catch (SQLException e) {
            System.out.println("Error: Unable to delete user from database.");
            e.printStackTrace();
        }
    }
}
