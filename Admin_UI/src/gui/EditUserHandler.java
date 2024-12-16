package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditUserHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public EditUserHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
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

        // Retrieve current values from the table
        String username = (String) tableModel.getValueAt(selectedRow, 0); // Username (not editable)
        String name = (String) tableModel.getValueAt(selectedRow, 1);    // Full Name
        String address = (String) tableModel.getValueAt(selectedRow, 2); // Address
        String dob = (String) tableModel.getValueAt(selectedRow, 3);     // DOB
        String gender = (String) tableModel.getValueAt(selectedRow, 4);  // Gender
        String email = (String) tableModel.getValueAt(selectedRow, 5);   // Email

        // Open dialog to edit user
        new EditUserDialog(parentPanel, tableModel, selectedRow, username, name, address, dob, gender, email);

        // After dialog closes, retrieve the updated values from the table model
        String updatedName = (String) tableModel.getValueAt(selectedRow, 1);
        String updatedAddress = (String) tableModel.getValueAt(selectedRow, 2);
        String updatedDob = (String) tableModel.getValueAt(selectedRow, 3);
        String updatedGender = (String) tableModel.getValueAt(selectedRow, 4);
        String updatedEmail = (String) tableModel.getValueAt(selectedRow, 5);

        // Update database with the new values
        updateUserInDatabase(username, updatedName, updatedAddress, updatedDob, updatedGender, updatedEmail);
    }

    /**
     * Updates the user information in the database.
     *
     * @param username The username (not editable).
     * @param name     The updated full name.
     * @param address  The updated address.
     * @param dob      The updated date of birth.
     * @param gender   The updated gender.
     * @param email    The updated email.
     */
    private void updateUserInDatabase(String username, String name, String address, String dob, String gender, String email) {
        String query = "UPDATE Users SET FullName = ?, Address = ?, DateOfBirth = ?, Gender = ?, Email = ? WHERE Username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setDate(3, java.sql.Date.valueOf(dob)); // Convert DOB to SQL Date
            stmt.setString(4, gender);
            stmt.setString(5, email);
            stmt.setString(6, username);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("User updated successfully in the database.");
            } else {
                System.out.println("No rows updated. Check the username.");
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentPanel, "Error updating user: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}