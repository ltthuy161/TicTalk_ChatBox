import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SpamReportsPanel {
    private JPanel mainPanel;
    private JTable spamTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SpamReportsPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Danh sách báo cáo spam", SwingConstants.CENTER);
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
        JButton sortByTimeButton = new JButton("Sắp xếp theo thời gian");
        JButton sortByUsernameButton = new JButton("Sắp xếp theo tên đăng nhập");
        JButton filterByDateRangeButton = new JButton("Lọc theo khoảng thời gian");
        JButton filterByUsernameButton = new JButton("Lọc theo tên đăng nhập");
        JButton lockUserButton = new JButton("Khoá tài khoản");

        // Add buttons to the sidebar
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByTimeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByUsernameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByDateRangeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByUsernameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(lockUserButton);
        sidebar.add(Box.createVerticalGlue());

        // Create column names for the table
        String[] columnNames = {"Thời gian", "Tên đăng nhập", "Lý do báo cáo"};

        // Create sample data for the table
        Object[][] data = {
                {"2024-11-14", "john123", "Spam quảng cáo"},
                {"2024-11-13", "jane456", "Spam tin nhắn"},
                {"2024-11-12", "john123", "Spam liên tục"},
                {"2024-11-11", "alice789", "Spam không phù hợp"}
        };

        // Create the table model
        tableModel = new DefaultTableModel(data, columnNames);

        // Create the table and set the model
        spamTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        spamTable.setRowSorter(sorter);

        // Add button actions
        sortByTimeButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByUsernameButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(1, SortOrder.ASCENDING))));

        filterByDateRangeButton.addActionListener(e -> filterByDateRange(sorter));
        filterByUsernameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Nhập tên đăng nhập để lọc:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 1));
            } else {
                sorter.setRowFilter(null);
            }
        });

        lockUserButton.addActionListener(e -> lockUser(spamTable));

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(spamTable);

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

    private void filterByDateRange(TableRowSorter<DefaultTableModel> sorter) {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        JTextField startDateField = new JTextField();
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("Đến ngày (yyyy-MM-dd):"));
        JTextField endDateField = new JTextField();
        inputPanel.add(endDateField);
    
        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Nhập khoảng thời gian", JOptionPane.OK_CANCEL_OPTION);
    
        if (result == JOptionPane.OK_OPTION) {
            String startDateStr = startDateField.getText().trim();
            String endDateStr = endDateField.getText().trim();
    
            try {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);
    
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        try {
                            String dateStr = entry.getStringValue(0); // Column 0 contains the date
                            Date rowDate = dateFormat.parse(dateStr);
                            return !rowDate.before(startDate) && !rowDate.after(endDate);
                        } catch (ParseException e) {
                            return false;
                        }
                    }
                });
    
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Định dạng ngày không hợp lệ. Vui lòng nhập đúng định dạng yyyy-MM-dd.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    

    private void lockUser(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một tài khoản để khoá!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) table.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn khoá tài khoản " + username + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(null, "Tài khoản " + username + " đã bị khoá!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
