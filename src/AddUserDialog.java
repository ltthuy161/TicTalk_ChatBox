import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AddUserDialog extends JDialog {
    public AddUserDialog(Component parent, DefaultTableModel tableModel) {
        // Set up dialog properties
        super((Frame) SwingUtilities.getWindowAncestor(parent), "Thêm tài khoản", true);
        setLayout(new GridLayout(7, 2, 10, 10));

        // Form fields
        JTextField usernameField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField dobField = new JTextField();
        JComboBox<String> genderField = new JComboBox<>(new String[]{"Nam", "Nữ"});
        JTextField emailField = new JTextField();

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
        JButton addBtn = new JButton("Thêm");
        JButton cancelBtn = new JButton("Hủy");

        add(addBtn);
        add(cancelBtn);

        // Set dialog size and position
        setSize(400, 300);
        setLocationRelativeTo(parent);

        // Handle "Thêm" button click
        addBtn.addActionListener(event -> {
            String username = usernameField.getText();
            String name = nameField.getText();
            String address = addressField.getText();
            String dob = dobField.getText();
            String gender = (String) genderField.getSelectedItem();
            String email = emailField.getText();

            if (username.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                tableModel.addRow(new Object[]{username, name, address, dob, gender, email});
                JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            }
        });

        // Handle "Hủy" button click
        cancelBtn.addActionListener(event -> dispose());

        // Show the dialog
        setVisible(true);
    }
}
