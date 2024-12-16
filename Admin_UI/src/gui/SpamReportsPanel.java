package gui;

import utils.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

            public class SpamReportsPanel {
                private JPanel mainPanel;
                private JTable spamTable;
                private DefaultTableModel tableModel;
                private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                public SpamReportsPanel(JPanel mainContainer) {
                    // Main panel setup
                    mainPanel = new JPanel(new BorderLayout());

                    // Header panel
                    JPanel headerPanel = createHeaderPanel(mainContainer);

                    // Sidebar panel
                    JPanel sidebar = createSidebarPanel();

                    // Table columns
                    String[] columnNames = {"Time", "Username", "Reason", "Status"};
                    tableModel = new DefaultTableModel(columnNames, 0);

                    // Table setup
                    spamTable = new JTable(tableModel);
                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
                    spamTable.setRowSorter(sorter);

                    // Scroll pane
                    JScrollPane scrollPane = new JScrollPane(spamTable);

                    // Fetch initial data
                    fetchSpamReportsData();

                    // Add components to main panel
                    mainPanel.add(headerPanel, BorderLayout.NORTH);
                    mainPanel.add(sidebar, BorderLayout.WEST);
                    mainPanel.add(scrollPane, BorderLayout.CENTER);
                }

                public JPanel getMainPanel() {
                    return mainPanel;
                }

                private JPanel createHeaderPanel(JPanel mainContainer) {
                    JPanel headerPanel = new JPanel(new BorderLayout());
                    JButton backButton = new JButton("Return Dashboard");
                    JLabel headerLabel = new JLabel("Report Spams List", SwingConstants.CENTER);
                    headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

                    backButton.addActionListener(e -> switchToDashboard(mainContainer));

                    headerPanel.add(backButton, BorderLayout.WEST);
                    headerPanel.add(headerLabel, BorderLayout.CENTER);

                    return headerPanel;
                }

                private JPanel createSidebarPanel() {
                    JPanel sidebar = new JPanel();
                    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
                    sidebar.setBackground(Color.LIGHT_GRAY);

                    // Buttons
                    JButton sortByTimeButton = new JButton("Sort By Time");
                    sortByTimeButton.addActionListener(e -> spamTable.getRowSorter()
                            .setSortKeys(Collections.singletonList(new RowSorter.SortKey(0, SortOrder.ASCENDING))));

                    JButton sortByUsernameButton = new JButton("Sort By Username");
                    sortByUsernameButton.addActionListener(e -> spamTable.getRowSorter()
                            .setSortKeys(Collections.singletonList(new RowSorter.SortKey(1, SortOrder.ASCENDING))));

                    JButton filterByDateRangeButton = new JButton("Filter By Date Range");
                    filterByDateRangeButton.addActionListener(e -> filterByDateRange((TableRowSorter<DefaultTableModel>) spamTable.getRowSorter()));

                    JButton filterByUsernameButton = new JButton("Filter By Username");
                    filterByUsernameButton.addActionListener(e -> {
                        String filterText = JOptionPane.showInputDialog("Enter username:");
                        if (filterText != null && !filterText.trim().isEmpty()) {
                            ((TableRowSorter<DefaultTableModel>) spamTable.getRowSorter()).setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 1));
                        } else {
                            ((TableRowSorter<DefaultTableModel>) spamTable.getRowSorter()).setRowFilter(null);
                        }
                    });

                    JButton lockUserButton = new JButton("Lock/Unlock User");
                    lockUserButton.addActionListener(e -> lockUser());

                    JButton refreshButton = new JButton("Refresh");
                    refreshButton.addActionListener(e -> {
                        fetchSpamReportsData(); // Reload data
                        ((TableRowSorter<DefaultTableModel>) spamTable.getRowSorter()).setRowFilter(null); // Clear all filters
                    });

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
                    sidebar.add(refreshButton);
                    sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

                    return sidebar;
                }

                private void fetchSpamReportsData() {
                    String query = "SELECT sr.reportedat, u.username, sr.message, u.status " +
                            "FROM spamreports sr " +
                            "JOIN users u ON sr.reportedusername = u.username";

                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement stmt = conn.prepareStatement(query);
                         ResultSet rs = stmt.executeQuery()) {

                        List<Object[]> spamReports = new ArrayList<>();
                        while (rs.next()) {
                            spamReports.add(new Object[]{
                                    rs.getString("reportedat"),
                                    rs.getString("username"),
                                    rs.getString("message"),
                                    rs.getString("status")
                            });
                        }
                        updateTableData(spamReports);

                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(mainPanel, "Error loading spam reports data!", "Database Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }

                private void updateTableData(List<Object[]> data) {
                    tableModel.setRowCount(0); // Clear existing rows
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

                    int result = JOptionPane.showConfirmDialog(mainPanel, inputPanel, "Enter Date Range", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        String startDateStr = startDateField.getText().trim();
                        String endDateStr = endDateField.getText().trim();

                        try {
                            // Parse the input dates
                            Date startDate = dateFormat.parse(startDateStr);
                            Date endDate = dateFormat.parse(endDateStr);

                            if (startDate.after(endDate)) {
                                JOptionPane.showMessageDialog(mainPanel, "Start date cannot be after the end date.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // Apply the RowFilter
                            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                                @Override
                                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                                    try {
                                        String dateStr = (String) entry.getValue(0); // Access the "Time" column
                                        Date rowDate = dateFormat.parse(dateStr);
                                        return !rowDate.before(startDate) && !rowDate.after(endDate);
                                    } catch (ParseException e) {
                                        return false;
                                    }
                                }
                            });

                        } catch (ParseException e) {
                            JOptionPane.showMessageDialog(mainPanel, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }

                private void lockUser() {
                    int selectedRow = spamTable.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(mainPanel, "Please select a user to lock!", "Warning", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    selectedRow = spamTable.convertRowIndexToModel(selectedRow);

                    String username = (String) tableModel.getValueAt(selectedRow, 1);
                    String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);

                    if ("Locked".equalsIgnoreCase(currentStatus)) {
                        JOptionPane.showMessageDialog(mainPanel, "The user is already locked!", "Information", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    boolean success = lockUserInDatabase(username);
                    if (success) {
                        tableModel.setValueAt("Locked", selectedRow, 3);
                        JOptionPane.showMessageDialog(mainPanel, "User " + username + " has been locked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

                private boolean lockUserInDatabase(String username) {
                    String query = "UPDATE users SET status = 'Locked' WHERE username = ?";
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(query)) {
                        pstmt.setString(1, username);
                        return pstmt.executeUpdate() > 0;
                    } catch (SQLException e) {
                        JOptionPane.showMessageDialog(mainPanel, "An error occurred while locking the user.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                        return false;
                    }
                }

                private void switchToDashboard(JPanel mainContainer) {
                    CardLayout cl = (CardLayout) mainContainer.getLayout();
                    cl.show(mainContainer, "Dashboard");
                }
            }