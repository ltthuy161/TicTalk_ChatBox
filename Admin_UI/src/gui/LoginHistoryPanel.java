package gui;

import utils.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginHistoryPanel {
    private JPanel panel;

    public LoginHistoryPanel(JPanel mainPanel) {
        panel = new JPanel(new BorderLayout());

        JLabel headerLabel = new JLabel("Login History", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Fetch login history data from the database
        Object[][] data = fetchLoginHistoryData();

        // Column names for the table
        String[] columnNames = {"Username", "Fullname", "Login Time"};
        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh"); // New Refresh Button
        panel.add(refreshButton, BorderLayout.NORTH);
        refreshButton.addActionListener(e -> fetchLoginHistoryData());

        // Back button to return to the Dashboard
        JButton backButton = new JButton("Return Dashboard");
        backButton.addActionListener(e -> switchToPanel(mainPanel, "Dashboard"));
        panel.add(backButton, BorderLayout.SOUTH);
    }

    private Object[][] fetchLoginHistoryData() {
        String query = "SELECT u.username, u.fullname, lh.logintime " +
                "FROM loginhistory lh " +
                "JOIN users u ON lh.username = u.username " +
                "ORDER BY lh.logintime DESC";
        Object[][] data = new Object[0][0];

        try (Connection conn = DBConnection.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(query)) {
            // Count rows to create a dynamic 2D array
            rs.last(); // Move cursor to the last row to count rows
            int rowCount = rs.getRow(); // Get the current row number (total rows)
            rs.beforeFirst(); // Reset the cursor to the beginning

            data = new Object[rowCount][3];
            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getString("username");
                data[i][1] = rs.getString("fullname");
                data[i][2] = rs.getString("logintime");
                i++;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(panel, "Cannot access data from database (Login History)", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return data;
    }

    private void switchToPanel(JPanel mainPanel, String panelName) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, panelName);
    }

    public JPanel getPanel() {
        return panel;
    }
}
