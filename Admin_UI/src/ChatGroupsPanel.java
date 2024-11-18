import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChatGroupsPanel {
    private JPanel mainPanel;
    private JTable chatGroupsTable;
    private DefaultTableModel tableModel;

    public ChatGroupsPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Danh sách các nhóm chat", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Add action listener to the back button
        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Create the sidebar panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Add buttons to the sidebar
        JButton sortByNameButton = new JButton("Sắp xếp theo tên");
        JButton sortByDateButton = new JButton("Sắp xếp theo thời gian");
        JButton filterByNameButton = new JButton("Lọc theo tên");
        JButton viewMembersButton = new JButton("Xem thành viên");
        JButton viewAdminsButton = new JButton("Xem admin");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(viewMembersButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(viewAdminsButton);
        sidebar.add(Box.createVerticalGlue());

        // Create column names for the table
        String[] columnNames = {"Tên nhóm", "Người tạo", "Thời gian tạo", "Số thành viên"};

        // Create sample data for the table
        Object[][] data = {
                {"Lập trình Java", "john123", "2024-11-10", 10},
                {"Học Cấu trúc Dữ liệu", "jane456", "2024-11-09", 15},
                {"Câu lạc bộ AI", "alice789", "2024-11-08", 25},
                {"Team Backend", "charlie321", "2024-11-07", 8}
        };

        // Create the table model
        tableModel = new DefaultTableModel(data, columnNames);

        // Create the table and set the model
        chatGroupsTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        chatGroupsTable.setRowSorter(sorter);

        // Add button actions
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(2, SortOrder.ASCENDING))));
        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Nhập tên nhóm để lọc:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0));
            } else {
                sorter.setRowFilter(null);
            }
        });
        viewMembersButton.addActionListener(e -> viewGroupMembers(chatGroupsTable));
        viewAdminsButton.addActionListener(e -> viewGroupAdmins(chatGroupsTable));

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(chatGroupsTable);

        // Add components to the main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST); // Sidebar on the left
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Table in the center
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }

    private void viewGroupMembers(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một nhóm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String groupName = (String) table.getValueAt(selectedRow, 0);
        // Sample data for members
        List<String[]> members = new ArrayList<>();
        members.add(new String[]{"john123", "John Doe"});
        members.add(new String[]{"jane456", "Jane Smith"});
        members.add(new String[]{"alice789", "Alice Brown"});

        showDialog("Danh sách thành viên - " + groupName, members, new String[]{"Tên đăng nhập", "Họ tên"});
    }

    private void viewGroupAdmins(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một nhóm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String groupName = (String) table.getValueAt(selectedRow, 0);
        // Sample data for admins
        List<String[]> admins = new ArrayList<>();
        admins.add(new String[]{"john123", "John Doe"});
        admins.add(new String[]{"alice789", "Alice Brown"});

        showDialog("Danh sách admin - " + groupName, admins, new String[]{"Tên đăng nhập", "Họ tên"});
    }

    private void showDialog(String title, List<String[]> data, String[] columns) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setSize(400, 300);
        JTable table = new JTable(new DefaultTableModel(data.toArray(new Object[0][0]), columns));
        JScrollPane scrollPane = new JScrollPane(table);
        dialog.add(scrollPane);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}
