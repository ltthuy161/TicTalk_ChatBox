package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViewHistoryDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public ViewHistoryDialog(JPanel parentPanel, JTable userTable) {
        this.parentPanel = parentPanel;
        this.userTable = userTable;
    }

    public void execute() {
        int selectedRow = userTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(parentPanel, "Select an account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = (String) userTable.getValueAt(selectedRow, 0);

        // Create a dialog to show login history
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Login History", true);
        dialog.setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("Login History: " + username);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Fetch login history from the database
        Object[][] loginHistory = fetchLoginHistory(username);

        // Define table columns
        String[] columnNames = {"Login time"};

        // Create the table with fetched data
        JTable historyTable = new JTable(loginHistory, columnNames);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        dialog.add(infoLabel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);
    }

    // Method to fetch login history from the database
    private Object[][] fetchLoginHistory(String username) {
        String query = "SELECT logintime FROM loginhistory WHERE username = ?";
        List<Object[]> dataList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Add each login time to the data list
                dataList.add(new Object[]{rs.getString("logintime")});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentPanel, "Cannot access data from database", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        // Convert the list to a 2D array
        return dataList.toArray(new Object[0][0]);
    }
}