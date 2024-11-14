import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DeleteUserHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public DeleteUserHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản để xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        selectedRow = userTable.convertRowIndexToModel(selectedRow); // Map view index to model index

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(parentPanel, "Bạn có chắc chắn muốn xóa tài khoản này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(parentPanel, "Tài khoản đã được xóa!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
