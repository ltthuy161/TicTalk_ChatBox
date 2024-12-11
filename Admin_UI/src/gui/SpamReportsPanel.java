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

public class SpamReportsPanel {
    private JPanel mainPanel;
    private JTable spamTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public SpamReportsPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("Report Spams List", SwingConstants.CENTER);
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
        JButton sortByTimeButton = new JButton("Sort By Time");
        JButton sortByUsernameButton = new JButton("Sort By Username");
        JButton filterByDateRangeButton = new JButton("Filter By Date Range");
        JButton filterByUsernameButton = new JButton("Filter By Username");
        JButton lockUserButton = new JButton("Lock User");

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

        // Create column names for the table
        String[] columnNames = {"Time", "Username", "Reason"};

        // Create the table model
        tableModel = new DefaultTableModel(columnNames, 0);
        spamTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        sortByTimeButton.addActionListener(e -> sorter.setSortKeys(Collections.singletonList(new SortKey(0, SortOrder.ASCENDING))));
        sortByUsernameButton.addActionListener(e -> sorter.setSortKeys(Collections.singletonList(new SortKey(1, SortOrder.ASCENDING))));

        filterByDateRangeButton.addActionListener(e -> filterByDateRange(sorter));
        filterByUsernameButton.addActionListener(e -> {
            String filterText = JOptionPane.showInputDialog("Enter username:");
            if (filterText != null && !filterText.trim().isEmpty()) {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filterText, 1));
            } else {
                sorter.setRowFilter(null);
            }
        });

        lockUserButton.addActionListener(e -> lockUser(spamTable));

        // Fetch the spam reports data from the database
        fetchSpamReportsData();

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(spamTable);

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

    private void fetchSpamReportsData() {
        String query = "SELECT sr.reportedat, u.username, sr.message " +
                "FROM spamreports sr " +
                "JOIN users u " +
                "ON sr.reportedid = u.userid"; // Modify with your actual query
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

                List<Object[]> spamReports = new ArrayList<>();
                while (rs.next()) {
                    spamReports.add(new Object[]{
                        rs.getString("reportedat"),
                        rs.getString("username"),
                        rs.getString("message")
                    });
                }

            updateTableData(spamReports);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Error loading spam reports data!", "Database Error", JOptionPane.ERROR_MESSAGE);
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

                if (startDate.after(endDate)) {
                    JOptionPane.showMessageDialog(null, "Ngày bắt đầu không thể sau ngày kết thúc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        try {
                            String dateStr = entry.getStringValue(0); // Column 0 contains the date
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

    private void lockUser(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Vui lòng chọn một tài khoản để khoá!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) table.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(null, "Bạn có chắc muốn khoá tài khoản " + username + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            lockUserInDatabase(username);
        }
    }

    private void lockUserInDatabase(String username) {
        String query = "UPDATE users SET status = 'locked' WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(null, "Tài khoản " + username + " đã bị khoá!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Không tìm thấy tài khoản " + username, "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi khoá tài khoản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
