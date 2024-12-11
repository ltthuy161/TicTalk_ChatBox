package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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

        // Add buttons
        JButton addBtn = new JButton("Add");
        JButton cancelBtn = new JButton("Cancel");

        add(addBtn);
        add(cancelBtn);

        // Set dialog size and position
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Handle "Thêm" button click
        addBtn.addActionListener(event -> {
            String username = usernameField.getText().trim();
            String name = nameField.getText().trim();
            String address = addressField.getText().trim();
            String dob = dobField.getText().trim();
            String gender = (String) genderField.getSelectedItem();
            String email = emailField.getText().trim();

            // Validate input fields
            if (username.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete the information!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Email wrong formated!", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidDOB(dob)) {
                JOptionPane.showMessageDialog(this, "DOB wrong formated! Format: yyyy-mm-dd", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                tableModel.addRow(new Object[]{username, name, address, dob, gender, email});
                JOptionPane.showMessageDialog(this, "Add account successfully!", "Completed", JOptionPane.INFORMATION_MESSAGE);
                clearForm(usernameField, nameField, addressField, dobField, emailField); // Clear form after submission
                dispose();
            }
        });

        // Handle "Hủy" button click
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
        // Validate date format (yyyy-mm-dd)
        String dobPattern = "^\\d{4}-\\d{2}-\\d{2}$";
        return Pattern.matches(dobPattern, dob);
    }

    private void clearForm(JTextField usernameField, JTextField nameField, JTextField addressField, JTextField dobField, JTextField emailField) {
        // Clear the form fields after successful submission
        usernameField.setText("");
        nameField.setText("");
        addressField.setText("");
        dobField.setText("");
        emailField.setText("");
    }
}
