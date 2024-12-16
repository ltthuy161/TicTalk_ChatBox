package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class AddUserDialog extends JDialog {

    public AddUserDialog(Component parent, DefaultTableModel tableModel) {
        // Set up dialog properties
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Add Account", true);
        setLayout(new GridLayout(8, 2, 10, 10));

        // Form fields
        JTextField usernameField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField dobField = new JTextField();
        JComboBox<String> genderField = new JComboBox<>(new String[]{"Male", "Female"});
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Add form fields to the dialog
        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Full name:"));
        add(nameField);
        add(new JLabel("Address:"));
        add(addressField);
        add(new JLabel("DOB (yyyy-mm-dd):"));
        add(dobField);
        add(new JLabel("Gender:"));
        add(genderField);
        add(new JLabel("Email:"));
        add(emailField);
        add(new JLabel("Password:"));
        add(passwordField);

        // Add buttons
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");

        add(addBtn);
        add(cancelBtn);

        // Set dialog size and position
        setSize(400, 400);
        setLocationRelativeTo(parent);

        // Handle "Add" button click
        addBtn.addActionListener(event -> {
            String username = usernameField.getText().trim();
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String dob = dobField.getText().trim();
            String gender = (String) genderField.getSelectedItem();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Validate input fields
            if (username.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please complete all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
                focusFirstEmptyField(usernameField, nameField, emailField, passwordField);
            } else if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                emailField.requestFocus();
            } else if (!isValidDOB(dob)) {
                JOptionPane.showMessageDialog(this, "Invalid DOB format! Use yyyy-mm-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                dobField.requestFocus();
            } else {
                if (addUserToDatabase(username, name, address, dob, gender, email, password)) {
                    tableModel.addRow(new Object[]{username, name, address, dob, gender, email, password});
                    JOptionPane.showMessageDialog(this, "Account added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearForm(usernameField, nameField, addressField, dobField, emailField, passwordField); // Clear form after submission
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add account to the database!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Handle "Cancel" button click
        cancelBtn.addActionListener(event -> dispose());

        // Show the dialog
        setVisible(true);
    }

    private boolean isValidEmail(String email) {
        // Improved email validation regex
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return Pattern.matches(emailPattern, email);
    }

    private boolean isValidDOB(String dob) {
        // Validate date format (yyyy-mm-dd) and ensure it's not in the future
        try {
            LocalDate parsedDate = LocalDate.parse(dob);
            return !parsedDate.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void focusFirstEmptyField(JTextField... fields) {
        for (JTextField field : fields) {
            if (field.getText().trim().isEmpty()) {
                field.requestFocus();
                break;
            }
        }
    }

    private void clearForm(JTextField usernameField, JTextField nameField, JTextField addressField, JTextField dobField, JTextField emailField, JPasswordField passwordField) {
        // Clear the form fields after successful submission
        usernameField.setText("");
        nameField.setText("");
        addressField.setText("");
        dobField.setText("");
        emailField.setText("");
        passwordField.setText("");
    }

    private boolean addUserToDatabase(String username, String name, String address, String dob, String gender, String email, String password) {
        String sql = "INSERT INTO Users (Username, FullName, Address, DateOfBirth, Gender, Email, Password) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, address);

            // Convert the date string to SQL Date format
            stmt.setDate(4, java.sql.Date.valueOf(dob));

            stmt.setString(5, gender);
            stmt.setString(6, email);
            stmt.setString(7, password); // Ideally, hash the password before storing it

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting user into database:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}