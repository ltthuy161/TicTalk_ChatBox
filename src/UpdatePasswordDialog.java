import javax.swing.*;

public class UpdatePasswordDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public UpdatePasswordDialog(JPanel parentPanel, JTable userTable) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản để cập nhật mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) userTable.getValueAt(selectedRow, 0);

        // Create a dialog to update password
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Cập nhật mật khẩu", true);
        dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));

        JLabel infoLabel = new JLabel("Cập nhật mật khẩu cho: " + username);
        JPasswordField newPasswordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JButton updateButton = new JButton("Cập nhật");
        JButton cancelButton = new JButton("Hủy");

        dialog.add(infoLabel);
        dialog.add(new JLabel("Mật khẩu mới:"));
        dialog.add(newPasswordField);
        dialog.add(new JLabel("Xác nhận mật khẩu:"));
        dialog.add(confirmPasswordField);
        dialog.add(updateButton);
        dialog.add(cancelButton);

        dialog.setSize(300, 200);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);

        // Handle update button click
        updateButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đầy đủ mật khẩu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu xác nhận không khớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "Cập nhật mật khẩu thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            }
        });

        // Handle cancel button click
        cancelButton.addActionListener(e -> dialog.dispose());
    }
}
