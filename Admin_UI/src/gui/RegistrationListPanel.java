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
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("Registration List", SwingConstants.CENTER);
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

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByNameButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(sortByDateButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByDateRangeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(filterByNameButton);
        sidebar.add(Box.createVerticalGlue());

        // Create column names for the table
        String[] columnNames = {"Username", "Full name", "Registration Time"};

        // Create the table model
        tableModel = new DefaultTableModel(columnNames, 0);
        registrationTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sortByNameButton.addActionListener(e -> sorter.setSortKeys(Collections.singletonList(new SortKey(1, SortOrder.ASCENDING))));
        sortByDateButton.addActionListener(e -> sorter.setSortKeys(Collections.singletonList(new SortKey(2, SortOrder.ASCENDING))));

        filterByDateRangeButton.addActionListener(e -> filterByDateRange(sorter));
        filterByNameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Enter Username:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 1));
            } else {
                sorter.setRowFilter(null);
            }
        });

        // Fetch the registration data from the database
        fetchRegistrationData();

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(registrationTable);

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

    private void fetchRegistrationData() {
        String query = "SELECT username, fullname, createdat FROM users"; // Modify with your actual query
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
        tableModel.setRowCount(0);  // Clear existing rows
        for (Object[] row : data) {
            tableModel.addRow(row);
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

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Enter the Date Range", JOptionPane.OK_CANCEL_OPTION);

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
                            String dateStr = (String) entry.getValue(2); // Column 2 contains the registration date
                            Date rowDate = dateFormat.parse(dateStr);
                            return !rowDate.before(startDate) && !rowDate.after(endDate);
                        } catch (ParseException e) {
                            return false;
                        }
                    }
                });

            } catch (ParseException e) {
                JOptionPane.showMessageDialog(null, "The date format is invalid. Please enter the correct format yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
