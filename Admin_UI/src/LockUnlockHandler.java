import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LockUnlockHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public LockUnlockHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Map view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Get current status
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 6);

        // Toggle status
        if ("Mở khoá".equals(currentStatus)) {
            tableModel.setValueAt("Khoá", selectedRow, 6);
            JOptionPane.showMessageDialog(parentPanel, "Tài khoản đã được khoá!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        } else {
            tableModel.setValueAt("Mở khoá", selectedRow, 6);
            JOptionPane.showMessageDialog(parentPanel, "Tài khoản đã được mở khoá!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
