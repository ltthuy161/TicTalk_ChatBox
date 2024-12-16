package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class UsersAndFriendsPanel {
    private JPanel mainPanel;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    public UsersAndFriendsPanel(JPanel mainContainer) {
        mainPanel = new JPanel(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("List of Users and Friends", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        backButton.addActionListener(e -> {
            if (mainContainer.getLayout() instanceof CardLayout) {
                CardLayout cl = (CardLayout) mainContainer.getLayout();
                cl.show(mainContainer, "Dashboard");
            } else {
                System.err.println("Error: mainContainer does not use CardLayout!");
            }
        });

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Sidebar panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        JButton sortByNameButton = new JButton("Sort By Name");
        JButton sortByDateButton = new JButton("Sort By Date");
        JButton filterByNameButton = new JButton("Filter By Name");
        JButton filterByDirectFriendsButton = new JButton("Filter By Direct Friends");

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            sorter.setRowFilter(null); // Xóa bộ lọc hiện tại (nếu có)
            refreshTableData();       // Làm mới dữ liệu bảng
        });

        sidebar.add(new JLabel("Features"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(sortByDateButton);
        sidebar.add(filterByNameButton);
        sidebar.add(filterByDirectFriendsButton);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(refreshButton);

        // Table data setup
        tableModel = new DefaultTableModel(fetchUserDataFromDatabase(), new String[]{
                "Username", "Created At", "Number of Direct Friends", "Total Friends of Friends"});
        usersTable = new JTable(tableModel);

        // Sorting setup
        sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);

        // Add action listeners for sorting
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))));

        // Add action listener for filtering by name
        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Enter username:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0)); // Lọc theo tên
            } else {
                sorter.setRowFilter(null); // Xóa bộ lọc nếu đầu vào rỗng
            }
        });

        // Add action listener for filtering by direct friends count
        filterByDirectFriendsButton.addActionListener(e -> filterByDirectFriendsCount(sorter));

        // Scroll pane for table
        JScrollPane scrollPane = new JScrollPane(usersTable);

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void refreshTableData() {
        System.out.println("Refreshing table data...");

        // Lấy dữ liệu mới từ cơ sở dữ liệu
        Object[][] newData = fetchUserDataFromDatabase();

        // Kiểm tra dữ liệu mới
        if (newData == null || newData.length == 0) {
            System.out.println("No data found.");
            JOptionPane.showMessageDialog(null, "No data available!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Xóa dữ liệu cũ và cập nhật dữ liệu mới
        tableModel.setRowCount(0); // Xóa toàn bộ dữ liệu cũ
        for (Object[] row : newData) {
            tableModel.addRow(row); // Thêm từng dòng mới
        }

        System.out.println("Table refreshed successfully!"); // Debug log
    }

    private void filterByDirectFriendsCount(TableRowSorter<DefaultTableModel> sorter) {
        String input = JOptionPane.showInputDialog("Enter condition of number of direct friends (e.g., <10, =5, >20):");

        if (input != null && !input.trim().isEmpty()) {
            input = input.trim();
            try {
                int value = Integer.parseInt(input.replaceAll("[^\\d-]", ""));
                char condition = input.charAt(0);

                if (condition != '<' && condition != '>' && condition != '=') {
                    JOptionPane.showMessageDialog(null, "Please enter a valid condition (e.g., <10, >5, =3)", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                final int filterValue = value;
                final char conditionOperator = condition;

                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        int directFriends = Integer.parseInt(entry.getStringValue(2));
                        switch (conditionOperator) {
                            case '<':
                                return directFriends < filterValue;
                            case '>':
                                return directFriends > filterValue;
                            case '=':
                                return directFriends == filterValue;
                            default:
                                return false;
                        }
                    }
                });
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            sorter.setRowFilter(null);
        }
    }

    private Object[][] fetchUserDataFromDatabase() {
        List<Object[]> data = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String query = "SELECT " +
                "    u.Username AS username, " +
                "    u.CreatedAt AS created_at, " +
                "    COUNT(DISTINCT CASE " +
                "                      WHEN f.Username1 = u.Username THEN f.Username2 " +
                "                      ELSE f.Username1 " +
                "                  END) AS direct_friends, " +
                "    COUNT(DISTINCT CASE " +
                "                      WHEN fof.Username1 = u.Username THEN fof.Username2 " +
                "                      WHEN fof.Username2 = u.Username THEN fof.Username1 " +
                "                  END) AS total_friends " +
                "FROM " +
                "    Users u " +
                "LEFT JOIN Friends f ON (u.Username = f.Username1 OR u.Username = f.Username2) " +
                "LEFT JOIN Friends fof ON (fof.Username1 = f.Username2 OR fof.Username2 = f.Username1) " +
                "GROUP BY " +
                "    u.Username, u.CreatedAt;";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String createdAt = dateFormat.format(rs.getDate("created_at"));
                int directFriends = rs.getInt("direct_friends");
                int totalFriends = rs.getInt("total_friends");

                data.add(new Object[]{username, createdAt, directFriends, totalFriends});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return data.toArray(new Object[0][0]);
    }
}