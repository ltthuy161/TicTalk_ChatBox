package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
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
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("List of Active Users", SwingConstants.CENTER);
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
        JButton sortByNameButton = new JButton("Sort By Name");
        JButton sortByDateButton = new JButton("Sort By Date");
        JButton filterByDateRangeButton = new JButton("Filter By Date Range");
        JButton filterByNameButton = new JButton("Filter By Name");
        JButton filterByActivityButton = new JButton("Filter By Number of Activities");

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
        String[] columnNames = {"Username", "Full Name", "Actived time", "Number of times TicTalk is opened", "Number of people to chat with", "Number of groups to chat with"};

        // Create the table model (empty for now)
        tableModel = new DefaultTableModel(columnNames, 0);

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
            String filterText = JOptionPane.showInputDialog("Enter username:");
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

        // Fetch data from the database and populate the table
        fetchActiveUsersData();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }

    private void fetchActiveUsersData() {
        String query = "SELECT username, full_name, last_active_date, app_open_count, chat_count, group_chat_count FROM active_users"; // Modify with your actual query
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Clear previous data in the table
            tableModel.setRowCount(0);

            // Populate the table with the data from the database
            while (rs.next()) {
                String username = rs.getString("username");
                String fullName = rs.getString("full_name");
                String lastActiveDate = rs.getString("last_active_date");
                int appOpenCount = rs.getInt("app_open_count");
                int chatCount = rs.getInt("chat_count");
                int groupChatCount = rs.getInt("group_chat_count");

                // Add a row to the table
                tableModel.addRow(new Object[]{
                    username, fullName, lastActiveDate, appOpenCount, chatCount, groupChatCount
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error loading active user data!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterByDateRange(TableRowSorter<DefaultTableModel> sorter) {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("From (yyyy-MM-dd):"));
        JTextField startDateField = new JTextField();
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("To (yyyy-MM-dd):"));
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
