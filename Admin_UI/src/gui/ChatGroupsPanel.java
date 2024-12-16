package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
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
        JPanel headerPanel = createHeaderPanel(mainContainer);

        // Create sidebar panel
        JPanel sidebar = createSidebarPanel();

        // Fetch data from the database
        List<Object[]> dataList = fetchChatGroupsData();

        // Column names for the table
        String[] columnNames = {"Group Name", "Creator", "Creation Time"};

        // Create the table model with fetched data
        tableModel = new DefaultTableModel(dataList.toArray(new Object[0][0]), columnNames);

        // Create the table and set the model
        chatGroupsTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        chatGroupsTable.setRowSorter(sorter);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(chatGroupsTable);

        // Add components to the main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST); // Sidebar on the left
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Table in the center
    }

    private JPanel createHeaderPanel(JPanel mainContainer) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("GroupChat List", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Add action listener to the back button
        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Create buttons for various actions
        JButton sortByNameButton = createSidebarButton("Sorting by Name", this::sortByName);
        JButton sortByDateButton = createSidebarButton("Sorting by Date", this::sortByDate);
        JButton filterByNameButton = createSidebarButton("Filter by Name", this::filterByName);
        JButton viewMembersButton = createSidebarButton("View Members", this::viewGroupMembers);
        JButton viewAdminsButton = createSidebarButton("View Admins", this::viewGroupAdmins);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            // Fetch new data and update table
            List<Object[]> dataList = fetchChatGroupsData();
            updateTableData(dataList);

            // Clear any filters applied
            TableRowSorter<?> sorter = (TableRowSorter<?>) chatGroupsTable.getRowSorter();
            if (sorter != null) {
                sorter.setRowFilter(null);
            }
        });

        // Add buttons to the sidebar
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
        sidebar.add(refreshButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        return sidebar;
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private List<Object[]> fetchChatGroupsData() {
        String sql = "SELECT gr.groupname, creator.username, gr.createdat " +
                "FROM groups gr " +
                "JOIN users creator " +
                "ON gr.createdby = creator.username";
        List<Object[]> data = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String groupName = rs.getString("groupname");
                String creator = rs.getString("username");
                Timestamp createdAt = rs.getTimestamp("createdat");
                data.add(new Object[]{groupName, creator, createdAt});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Error fetching data from the database.", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return data;
    }

    private void updateTableData(List<Object[]> data) {
        tableModel.setRowCount(0); // Clear existing data
        for (Object[] row : data) {
            tableModel.addRow(row); // Add new rows
        }
    }

    private void sortByName() {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) chatGroupsTable.getRowSorter();
        sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
    }

    private void sortByDate() {
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) chatGroupsTable.getRowSorter();
        sorter.setSortKeys(java.util.Arrays.asList(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
    }

    private void filterByName() {
        String filterText = JOptionPane.showInputDialog("Group Name:");
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) chatGroupsTable.getRowSorter();
        if (filterText != null && !filterText.trim().isEmpty()) {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0));
        } else {
            sorter.setRowFilter(null);
        }
    }

    private void viewGroupMembers() {
        int selectedRow = chatGroupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a group first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String groupName = (String) chatGroupsTable.getValueAt(selectedRow, 0);

        List<String[]> members = fetchGroupMembers(groupName);
        if (members.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No members found for this group.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showDialog("Group Members - " + groupName, members, new String[]{"Username", "Full Name"});
        }
    }

    private void viewGroupAdmins() {
        int selectedRow = chatGroupsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a group first!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String groupName = (String) chatGroupsTable.getValueAt(selectedRow, 0);

        List<String[]> admins = fetchGroupAdmins(groupName);
        if (admins.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No admins found for this group.", "Information", JOptionPane.INFORMATION_MESSAGE);
        } else {
            showDialog("Group Admins - " + groupName, admins, new String[]{"Username", "Full Name"});
        }
    }

    private List<String[]> fetchGroupMembers(String groupName) {
        List<String[]> members = new ArrayList<>();
        String query = "SELECT u.username, u.fullname " +
                "FROM GroupParticipants gp " +
                "JOIN users u ON gp.username = u.username AND gp.role = 'Member' " +
                "JOIN groups g ON gp.groupid = g.groupid " +
                "WHERE g.groupname = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String fullName = rs.getString("fullname");
                members.add(new String[]{username, fullName});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return members;
    }

    private List<String[]> fetchGroupAdmins(String groupName) {
        List<String[]> admins = new ArrayList<>();
        String query = "SELECT u.username, u.fullname " +
                "FROM GroupParticipants gp " +
                "JOIN users u ON gp.username = u.username AND gp.role = 'Admin' " +
                "JOIN groups g ON gp.groupid = g.groupid " +
                "WHERE g.groupname = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, groupName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String username = rs.getString("username");
                String fullName = rs.getString("fullname");
                admins.add(new String[]{username, fullName});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return admins;
    }

    private void showDialog(String title, List<String[]> data, String[] columns) {
        JDialog dialog = new JDialog();
        dialog.setTitle(title);
        dialog.setSize(400, 300);

        String[][] dataArray = new String[data.size()][columns.length];
        data.toArray(dataArray);

        JTable table = new JTable(dataArray, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        dialog.getContentPane().add(scrollPane);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}