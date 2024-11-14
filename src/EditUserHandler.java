import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class EditUserHandler {
    private final JPanel parentPanel;
    private final JTable userTable;
    private final DefaultTableModel tableModel;

    public EditUserHandler(JPanel parentPanel, JTable userTable, DefaultTableModel tableModel) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
        this.tableModel = tableModel;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Vui lòng chọn một tài khoản để chỉnh sửa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Map view index to model index
        selectedRow = userTable.convertRowIndexToModel(selectedRow);

        // Retrieve current values
        String username = (String) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);
        String address = (String) tableModel.getValueAt(selectedRow, 2);
        String dob = (String) tableModel.getValueAt(selectedRow, 3);
        String gender = (String) tableModel.getValueAt(selectedRow, 4);
        String email = (String) tableModel.getValueAt(selectedRow, 5);

        // Open a dialog to edit the selected account
        new EditUserDialog(parentPanel, tableModel, selectedRow, username, name, address, dob, gender, email);
    }
}
