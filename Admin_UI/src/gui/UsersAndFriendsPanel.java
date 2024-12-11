package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersAndFriendsPanel {
    private JPanel mainPanel;
    private JTable usersTable;
    private DefaultTableModel tableModel;

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

        sidebar.add(new JLabel("Feature"));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(sortByDateButton);
        sidebar.add(filterByNameButton);
        sidebar.add(filterByDirectFriendsButton);
        sidebar.add(Box.createVerticalGlue());

        // Table data - initially static but can be replaced by dynamic data
        tableModel = new DefaultTableModel(fetchUserDataFromDatabase(), new String[]{"Username", "Number of Direct Friends", "Total Friends"});
        usersTable = new JTable(tableModel);

        // Sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);

        // Add action listeners for sidebar buttons
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))));

        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Enter username:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0));
            } else {
                sorter.setRowFilter(null);
            }
        });

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

    private void filterByDirectFriendsCount(TableRowSorter<DefaultTableModel> sorter) {
        String input = JOptionPane.showInputDialog("Enter condition of number of direct friends(ex: <10, =5, >20):");

        if (input != null && !input.trim().isEmpty()) {
            input = input.trim();
            try {
                int value = Integer.parseInt(input.replaceAll("[^\\d-]", ""));  // Extract integer from input
                char condition = input.charAt(0);

                if (condition != '<' && condition != '>' && condition != '=') {
                    JOptionPane.showMessageDialog(null, "Please enter the correct format of condition (ex: <10, >5, =3)", "Error", JOptionPane.ERROR_MESSAGE);
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
                JOptionPane.showMessageDialog(null, "Please enter the correct format of number!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            sorter.setRowFilter(null);
        }
    }

    private Object[][] fetchUserDataFromDatabase() {
        List<Object[]> data = new ArrayList<>();
        
        // Assuming a database connection is established
        String query = "SELECT username, create_date, direct_friends, total_friends FROM users";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String createDate = rs.getString("create_date");
                int directFriends = rs.getInt("direct_friends");
                int totalFriends = rs.getInt("total_friends");
                data.add(new Object[]{username, createDate, directFriends, totalFriends});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi kết nối cơ sở dữ liệu", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        return data.toArray(new Object[0][0]);
    }
}
