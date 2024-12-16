package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.regex.Pattern;

public class EditUserDialog extends JDialog {
    public EditUserDialog(Component parent, DefaultTableModel tableModel, int rowIndex,
                          String username, String name, String address, String dob, String gender, String email) {
        // Set up dialog properties
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Edit User Information", true);
        setLayout(new GridLayout(7, 2, 10, 10));

        // Form fields
        JTextField usernameField = new JTextField(username);
        usernameField.setEditable(false); // Make the username field non-editable
        JTextField nameField = new JTextField(name);
        JTextField addressField = new JTextField(address);
        JTextField dobField = new JTextField(dob);
        JComboBox<String> genderField = new JComboBox<>(new String[]{"Male", "Female"});
        genderField.setSelectedItem(gender);
        JTextField emailField = new JTextField(email);

        // Add form fields to the dialog
        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Full Name:"));
        add(nameField);
        add(new JLabel("Address:"));
        add(addressField);
        add(new JLabel("Date of Birth (yyyy-mm-dd):"));
        add(dobField);
        add(new JLabel("Gender:"));
        add(genderField);
        add(new JLabel("Email:"));
        add(emailField);

        // Add buttons
        JButton updateBtn = new JButton("Update");
        JButton cancelBtn = new JButton("Cancel");

        add(updateBtn);
        add(cancelBtn);

        // Set dialog size and position
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Handle "Update" button click
        updateBtn.addActionListener(event -> {
            String nameText = nameField.getText().trim();
            String addressText = addressField.getText().trim();
            String dobText = dobField.getText().trim();
            String emailText = emailField.getText().trim();

            // Validate input fields
            if (nameText.isEmpty() || emailText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidEmail(emailText)) {
                JOptionPane.showMessageDialog(this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidDOB(dobText)) {
                JOptionPane.showMessageDialog(this, "Invalid DOB format! Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                // Update table values
                tableModel.setValueAt(nameText, rowIndex, 1);
                tableModel.setValueAt(addressText, rowIndex, 2);
                tableModel.setValueAt(dobText, rowIndex, 3);
                tableModel.setValueAt(genderField.getSelectedItem(), rowIndex, 4);
                tableModel.setValueAt(emailText, rowIndex, 5);

                JOptionPane.showMessageDialog(this, "Update successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        // Handle "Cancel" button click
        cancelBtn.addActionListener(event -> dispose());

        // Show the dialog
        setVisible(true);
    }

    private boolean isValidEmail(String email) {
        // Simple email validation using regular expression
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailPattern, email);
    }

    private boolean isValidDOB(String dob) {
        // Validate date format (yyyy-MM-dd)
        String dobPattern = "^\\d{4}-\\d{2}-\\d{2}$";
        return Pattern.matches(dobPattern, dob);
    }
}