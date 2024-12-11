package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.regex.Pattern;

public class EditUserDialog extends JDialog {
    public EditUserDialog(Component parent, DefaultTableModel tableModel, int rowIndex,
                          String username, String name, String address, String dob, String gender, String email) {
        // Set up dialog properties
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Chỉnh sửa tài khoản", true);
        setLayout(new GridLayout(8, 2, 10, 10));

        // Form fields
        JTextField usernameField = new JTextField(username);
        JTextField nameField = new JTextField(name);
        JTextField addressField = new JTextField(address);
        JTextField dobField = new JTextField(dob);
        JComboBox<String> genderField = new JComboBox<>(new String[]{"Nam", "Nữ"});
        genderField.setSelectedItem(gender);
        JTextField emailField = new JTextField(email);

        // Add form fields to the dialog
        add(new JLabel("Tên đăng nhập:"));
        add(usernameField);
        add(new JLabel("Họ tên:"));
        add(nameField);
        add(new JLabel("Địa chỉ:"));
        add(addressField);
        add(new JLabel("Ngày sinh (yyyy-mm-dd):"));
        add(dobField);
        add(new JLabel("Giới tính:"));
        add(genderField);
        add(new JLabel("Email:"));
        add(emailField);

        // Add buttons
        JButton updateBtn = new JButton("Cập nhật");
        JButton cancelBtn = new JButton("Hủy");

        add(updateBtn);
        add(cancelBtn);

        // Set dialog size and position
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Handle "Cập nhật" button click
        updateBtn.addActionListener(event -> {
            String usernameText = usernameField.getText().trim();
            String nameText = nameField.getText().trim();
            String addressText = addressField.getText().trim();
            String dobText = dobField.getText().trim();
            String emailText = emailField.getText().trim();

            // Validate the input fields
            if (usernameText.isEmpty() || nameText.isEmpty() || emailText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidEmail(emailText)) {
                JOptionPane.showMessageDialog(this, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else if (!isValidDOB(dobText)) {
                JOptionPane.showMessageDialog(this, "Ngày sinh không hợp lệ! Định dạng: yyyy-mm-dd", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                // Update table values
                tableModel.setValueAt(usernameText, rowIndex, 0);
                tableModel.setValueAt(nameText, rowIndex, 1);
                tableModel.setValueAt(addressText, rowIndex, 2);
                tableModel.setValueAt(dobText, rowIndex, 3);
                tableModel.setValueAt(genderField.getSelectedItem(), rowIndex, 4);
                tableModel.setValueAt(emailText, rowIndex, 5);

                JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        // Handle "Hủy" button click
        cancelBtn.addActionListener(event -> {
            // Optionally clear fields or just close
            dispose();
        });

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
}
