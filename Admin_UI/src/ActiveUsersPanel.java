import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActiveUsersPanel {
    private JPanel mainPanel;
    private JTable activeUsersTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public ActiveUsersPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Danh sách người dùng hoạt động", SwingConstants.CENTER);
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
        JButton filterByDateRangeButton = new JButton("Lọc theo khoảng thời gian");
        JButton filterByNameButton = new JButton("Lọc theo tên");
        JButton filterByActivityButton = new JButton("Lọc theo số lượng hoạt động");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByDateRangeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByActivityButton);
        sidebar.add(Box.createVerticalGlue());

        // Create column names for the table
        String[] columnNames = {"Tên đăng nhập", "Họ tên", "Thời gian hoạt động", "Số lần mở ứng dụng", "Chat với bao nhiêu người", "Chat bao nhiêu nhóm"};

        // Create sample data for the table
        Object[][] data = {
                {"john123", "John Doe", "2024-11-14", 5, 3, 2},
                {"jane456", "Jane Smith", "2024-11-13", 8, 5, 3},
                {"alice789", "Alice Brown", "2024-11-12", 2, 1, 1},
                {"charlie321", "Charlie Black", "2024-11-11", 10, 7, 4}
        };

        // Create the table model
        tableModel = new DefaultTableModel(data, columnNames);

        // Create the table and set the model
        activeUsersTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        activeUsersTable.setRowSorter(sorter);

        // Add button actions
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(1, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(2, SortOrder.ASCENDING))));
        filterByDateRangeButton.addActionListener(e -> filterByDateRange(sorter));
        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Nhập tên để lọc:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 1));
            } else {
                sorter.setRowFilter(null);
            }
        });
        filterByActivityButton.addActionListener(e -> filterByActivityMetrics(sorter));

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(activeUsersTable);

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
                            String dateStr = (String) entry.getValue(2); // Column 2 contains the activity date
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

    private void filterByActivityMetrics(TableRowSorter<DefaultTableModel> sorter) {
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        JComboBox<String> comparisonOptions = new JComboBox<>(new String[]{"=", ">", "<"});
        JTextField activityField = new JTextField();

        inputPanel.add(comparisonOptions);
        inputPanel.add(activityField);

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Nhập điều kiện lọc hoạt động", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String comparison = (String) comparisonOptions.getSelectedItem();
            String activityStr = activityField.getText().trim();

            try {
                int activityValue = Integer.parseInt(activityStr);

                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        int totalActivity = (int) entry.getValue(3) + (int) entry.getValue(4) + (int) entry.getValue(5);
                        switch (comparison) {
                            case "=":
                                return totalActivity == activityValue;
                            case ">":
                                return totalActivity > activityValue;
                            case "<":
                                return totalActivity < activityValue;
                            default:
                                return false;
                        }
                    }
                });

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Giá trị số lượng hoạt động không hợp lệ. Vui lòng nhập số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
