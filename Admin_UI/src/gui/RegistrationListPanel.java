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
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

public class RegistrationListPanel {
    private JPanel mainPanel;
    private JTable registrationTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public RegistrationListPanel(JPanel mainContainer) {
        // Set up the main panel
        mainPanel = new JPanel(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("Registration List", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        JButton sortByNameButton = new JButton("Sort By Name");
        JButton sortByDateButton = new JButton("Sort By Date");
        JButton filterByDateRangeButton = new JButton("Filter By Date Range");
        JButton filterByNameButton = new JButton("Filter By Name");
        JButton refreshButton = new JButton("Refresh");

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByDateRangeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(refreshButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // Table and model
        String[] columnNames = {"Username", "Full name", "Registration Time"};
        tableModel = new DefaultTableModel(columnNames, 0);
        registrationTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        registrationTable.setRowSorter(sorter);

        sortByNameButton.addActionListener(e -> {
            sorter.setSortKeys(Collections.singletonList(new SortKey(1, SortOrder.ASCENDING)));
            System.out.println("Sort By Name clicked!");
        });

        sortByDateButton.addActionListener(e -> {
            sorter.setSortKeys(Collections.singletonList(new SortKey(2, SortOrder.ASCENDING)));
            System.out.println("Sort By Date clicked!");
        });

        filterByDateRangeButton.addActionListener(e -> filterByDateRange(sorter));
        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Enter Username:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 0)); // Column 0 is Username
                System.out.println("Filter By Name clicked with input: " + filterText);
            } else {
                sorter.setRowFilter(null);
                System.out.println("Filter By Name reset.");
            }
        });

        // Fix for Refresh Button
        refreshButton.addActionListener(e -> {
            sorter.setRowFilter(null); // Xóa mọi bộ lọc hiện tại
            fetchRegistrationData();  // Làm mới bảng
            System.out.println("Refresh button clicked. Table refreshed.");
        });

        // Fetch initial data
        fetchRegistrationData();

        // Scrollable table
        JScrollPane scrollPane = new JScrollPane(registrationTable);

        // Add components
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }

    private void fetchRegistrationData() {
        String query = "SELECT username, fullname, createdat FROM users";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<Object[]> registrationData = new ArrayList<>();
            while (rs.next()) {
                registrationData.add(new Object[]{
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("createdat")
                });
            }

            updateTableData(registrationData);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error loading registration data!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableData(List<Object[]> data) {
        tableModel.setRowCount(0); // Clear old rows
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        System.out.println("Table updated with " + data.size() + " rows.");
    }

    private void filterByDateRange(TableRowSorter<DefaultTableModel> sorter) {
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("From (yyyy-MM-dd):"));
        JTextField startDateField = new JTextField();
        inputPanel.add(startDateField);
        inputPanel.add(new JLabel("To (yyyy-MM-dd):"));
        JTextField endDateField = new JTextField();
        inputPanel.add(endDateField);

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Enter the Date Range", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Date startDate = dateFormat.parse(startDateField.getText().trim());
                Date endDate = dateFormat.parse(endDateField.getText().trim());

                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        try {
                            Date rowDate = dateFormat.parse((String) entry.getValue(2));
                            return !rowDate.before(startDate) && !rowDate.after(endDate);
                        } catch (ParseException e) {
                            return false;
                        }
                    }
                });
                System.out.println("Filter By Date Range applied: " + startDate + " to " + endDate);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "Invalid date format. Use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}