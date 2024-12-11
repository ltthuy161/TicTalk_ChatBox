package gui;
import utils.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FriendListDialog {
    private final JPanel parentPanel;
    private final JTable userTable;

    public FriendListDialog(JPanel parentPanel, JTable userTable) {
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

        // Create a dialog to show friend list
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(parentPanel), "Friends list", true);
        dialog.setLayout(new BorderLayout());

        JLabel infoLabel = new JLabel("Friends list of " + username);
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Fetch friend list from the database
        Object[][] friendList = fetchFriendList(username);

        // Define table columns
        String[] columnNames = {"Username", "Full name"};

        // Create the table with fetched data
        JTable friendTable = new JTable(friendList, columnNames);
        JScrollPane scrollPane = new JScrollPane(friendTable);

        dialog.add(infoLabel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);

        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentPanel);
        dialog.setVisible(true);
    }

    // Method to fetch friend list from the database
    private Object[][] fetchFriendList(String username) {
        String query = "SELECT f.username, u.full_name " +
                       "FROM friends f " +
                       "JOIN users u ON f.friend_username = u.username " +
                       "WHERE f.username = ?";
        Object[][] data = new Object[0][0];

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            // Count rows to initialize the 2D array with the correct size
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
            }
            rs.beforeFirst(); // Reset the cursor to the beginning

            data = new Object[rowCount][2];
            int i = 0;
            while (rs.next()) {
                data[i][0] = rs.getString("username");
                data[i][1] = rs.getString("full_name");
                i++;
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentPanel, "Cannot access data from database", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return data;
    }
}
