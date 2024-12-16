package gui;

import utils.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        String query = "SELECT " +
                "    CASE " +
                "        WHEN f.username1 = ? THEN f.username2 " +
                "        ELSE f.username1 " +
                "    END AS friend_username, " +
                "    u.fullname AS friend_fullname " +
                "FROM " +
                "    friends f " +
                "JOIN " +
                "    users u " +
                "    ON u.username = CASE " +
                "                       WHEN f.username1 = ? THEN f.username2 " +
                "                       ELSE f.username1 " +
                "                   END " +
                "WHERE " +
                "    ? IN (f.username1, f.username2)";
        List<Object[]> dataList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, username);
            stmt.setString(3, username);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String friendUsername = rs.getString("friend_username");
                String friendFullname = rs.getString("friend_fullname");
                dataList.add(new Object[]{friendUsername, friendFullname});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(parentPanel, "Cannot access data from database", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return dataList.toArray(new Object[0][0]);
    }
}