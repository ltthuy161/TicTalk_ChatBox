import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
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
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Danh sách người dùng và số lượng bạn bè", SwingConstants.CENTER);
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

        JButton sortByNameButton = new JButton("Sắp xếp theo tên");
        JButton sortByDateButton = new JButton("Sắp xếp theo thời gian tạo");
        JButton filterByNameButton = new JButton("Lọc theo tên");
        JButton filterByDirectFriendsButton = new JButton("Lọc theo số bạn trực tiếp");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByDirectFriendsButton);
        sidebar.add(Box.createVerticalGlue());

        // Table data
        String[] columnNames = {"Tên người dùng", "Thời gian tạo", "Số bạn trực tiếp", "Tổng số bạn"};
        Object[][] data = {
                {"John Doe", "2024-01-15", 10, 25},
                {"Jane Smith", "2024-01-10", 5, 15},
                {"Alice Brown", "2024-02-01", 12, 30},
                {"Charlie Black", "2024-03-20", 8, 20},
                {"Eve White", "2024-04-10", 3, 10}
        };

        tableModel = new DefaultTableModel(data, columnNames);
        usersTable = new JTable(tableModel);

        // Sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        usersTable.setRowSorter(sorter);

        // Add action listeners for sidebar buttons
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING))));

        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Nhập tên người dùng để lọc:");
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
        String input = JOptionPane.showInputDialog("Nhập điều kiện lọc (vd: <10, =5, >20):");

        if (input != null && !input.trim().isEmpty()) {
            char[] conditionHolder = new char[1];
            int value;

            try {
                if (input.charAt(0) == '<' || input.charAt(0) == '>') {
                    conditionHolder[0] = input.charAt(0);
                    value = Integer.parseInt(input.substring(1).trim());
                } else if (input.charAt(0) == '=') {
                    conditionHolder[0] = input.charAt(0);
                    value = Integer.parseInt(input.substring(1).trim());
                } else {
                    conditionHolder[0] = '=';
                    value = Integer.parseInt(input.trim());
                }

                final int filterValue = value;
                final char condition = conditionHolder[0];

                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        int directFriends = Integer.parseInt(entry.getStringValue(2));
                        return switch (condition) {
                            case '<' -> directFriends < filterValue;
                            case '>' -> directFriends > filterValue;
                            case '=' -> directFriends == filterValue;
                            default -> false;
                        };
                    }
                });
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            sorter.setRowFilter(null);
        }
    }

}
