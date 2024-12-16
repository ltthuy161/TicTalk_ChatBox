package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.Arrays;

public class ActiveUsersPanel {
    private JPanel mainPanel;
    private JTable activeUsersTable;
    private DefaultTableModel tableModel;

    public ActiveUsersPanel(JPanel mainContainer) {
        // Main panel layout
        mainPanel = new JPanel(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("List of Active Users", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Sidebar panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Sidebar buttons
        JButton sortByNameButton = new JButton("Sort By Name");
        JButton sortByDateButton = new JButton("Sort By Date");
        JButton filterByNameButton = new JButton("Filter By Username");
        JButton filterByActivityButton = new JButton("Filter By Total Activities");
        JButton refreshButton = new JButton("Refresh");

        // Add button actions
        sortByNameButton.addActionListener(e -> sortByName());
        sortByDateButton.addActionListener(e -> sortByDate());
        filterByNameButton.addActionListener(e -> filterByName());
        filterByActivityButton.addActionListener(e -> filterByActivity());
        refreshButton.addActionListener(e -> {
            // Clear filters and refresh data
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
            sorter.setRowFilter(null);
            fetchActiveUsersData();
            System.out.println("Refresh button clicked. Data refreshed.");
        });

        // Add buttons to sidebar
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByActivityButton);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(refreshButton);

        // Column names for the table
        String[] columnNames = {
                "Username",
                "Full Name",
                "Number of times TicTalk is opened",
                "Number of people to chat with",
                "Number of groups to chat with",
                "Total Activities",
                "Last Active Time"
        };

        // Table setup
        tableModel = new DefaultTableModel(columnNames, 0);
        activeUsersTable = new JTable(tableModel);

        // Sorting functionality
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        activeUsersTable.setRowSorter(sorter);

        // Scrollable table
        JScrollPane scrollPane = new JScrollPane(activeUsersTable);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Fetch initial data
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
        String query = "SELECT " +
                "    u.Username AS username, " +
                "    u.FullName AS fullname, " +
                "    COUNT(lh.LoginTime) AS app_open_count, " +
                "    COUNT(DISTINCT pc.ReceiverUsername) AS chat_count, " +
                "    COUNT(DISTINCT gc.GroupID) AS group_chat_count, " +
                "    (COUNT(lh.LoginTime) + COUNT(DISTINCT pc.ReceiverUsername) + COUNT(DISTINCT gc.GroupID)) AS total_activity, " +
                "    MAX(lh.LoginTime) AS last_active_time " +
                "FROM " +
                "    Users u " +
                "LEFT JOIN " +
                "    LoginHistory lh ON u.Username = lh.Username " +
                "LEFT JOIN " +
                "    PeopleChat pc ON u.Username = pc.SenderUsername " +
                "LEFT JOIN " +
                "    GroupChat gc ON u.Username = gc.SenderUsername " +
                "GROUP BY " +
                "    u.Username, u.FullName;";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            // Clear existing data
            tableModel.setRowCount(0);

            // Populate the table with data from the database
            while (rs.next()) {
                String username = rs.getString("username");
                String fullName = rs.getString("fullname");
                int appOpenCount = rs.getInt("app_open_count");
                int chatCount = rs.getInt("chat_count");
                int groupChatCount = rs.getInt("group_chat_count");
                int totalActivity = rs.getInt("total_activity");
                String lastActiveTime = rs.getString("last_active_time");

                tableModel.addRow(new Object[]{username, fullName, appOpenCount, chatCount, groupChatCount, totalActivity, lastActiveTime});
            }

            System.out.println("Data successfully loaded and table updated.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error loading active user data!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sortByName() {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING))); // Column 0 is Username
    }

    private void sortByDate() {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(6, SortOrder.ASCENDING))); // Column 6 is Last Active Time
    }

    private void filterByName() {
        String filterText = JOptionPane.showInputDialog("Enter username:");
        if (filterText != null && !filterText.trim().isEmpty()) {
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0)); // Column 0 is Username
        } else {
            TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
            sorter.setRowFilter(null); // Clear filter
        }
    }

    private void filterByActivity() {
        JPanel inputPanel = new JPanel(new GridLayout(1, 2));
        JComboBox<String> comparisonOptions = new JComboBox<>(new String[]{"=", ">", "<"});
        JTextField activityField = new JTextField();

        inputPanel.add(comparisonOptions);
        inputPanel.add(activityField);

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Filter by Total Activities", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String comparison = (String) comparisonOptions.getSelectedItem();
            String activityStr = activityField.getText().trim();

            try {
                int activityValue = Integer.parseInt(activityStr);

                TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) activeUsersTable.getRowSorter();
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        int totalActivity = Integer.parseInt(entry.getStringValue(5)); // Column 5 is Total Activities
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
                JOptionPane.showMessageDialog(null, "Invalid number format. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}