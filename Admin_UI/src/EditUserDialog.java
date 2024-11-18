import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EditUserDialog extends JDialog {
    public EditUserDialog(Component parent, DefaultTableModel tableModel, int rowIndex,
                          String username, String name, String address, String dob, String gender, String email) {
        // Set up dialog properties
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Chỉnh sửa tài khoản", true);
        setLayout(new GridLayout(7, 2, 10, 10));

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
            // Update table values
            tableModel.setValueAt(usernameField.getText(), rowIndex, 0);
            tableModel.setValueAt(nameField.getText(), rowIndex, 1);
            tableModel.setValueAt(addressField.getText(), rowIndex, 2);
            tableModel.setValueAt(dobField.getText(), rowIndex, 3);
            tableModel.setValueAt(genderField.getSelectedItem(), rowIndex, 4);
            tableModel.setValueAt(emailField.getText(), rowIndex, 5);

            JOptionPane.showMessageDialog(this, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        // Handle "Hủy" button click
        cancelBtn.addActionListener(event -> dispose());

        // Show the dialog
        setVisible(true);
    }
}
