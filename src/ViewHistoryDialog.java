import javax.swing.*;
import java.awt.*;

public class ViewHistoryDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public ViewHistoryDialog(JPanel parentPanel, JTable userTable) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản để xem lịch sử đăng nhập!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) userTable.getValueAt(selectedRow, 0);

        // Create a dialog to show login history
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Lịch sử đăng nhập", true);
        dialog.setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("Lịch sử đăng nhập của: " + username);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Example login history data
        String[] columnNames = {"Thời gian đăng nhập", "Địa chỉ IP"};
        Object[][] loginHistory = {
                {"2024-11-14 10:00", "192.168.1.1"},
                {"2024-11-13 15:45", "192.168.1.2"},
                {"2024-11-12 08:30", "192.168.1.3"}
        };

        JTable historyTable = new JTable(loginHistory, columnNames);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        dialog.add(infoLabel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);
    }
}
