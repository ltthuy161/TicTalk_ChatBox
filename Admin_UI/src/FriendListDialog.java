import javax.swing.*;
import java.awt.*;

public class FriendListDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public FriendListDialog(JPanel parentPanel, JTable userTable) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản để xem danh sách bạn bè!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) userTable.getValueAt(selectedRow, 0);

        // Create a dialog to show friend list
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Danh sách bạn bè", true);
        dialog.setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("Danh sách bạn bè của: " + username);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Example friend list data
        String[] columnNames = {"Tên đăng nhập", "Họ tên"};
        Object[][] friendList = {
                {"friend1", "Alice Brown"},
                {"friend2", "Bob White"},
                {"friend3", "Charlie Black"}
        };

        JTable friendTable = new JTable(friendList, columnNames);
        JScrollPane scrollPane = new JScrollPane(friendTable);

        dialog.add(infoLabel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);
    }
}
