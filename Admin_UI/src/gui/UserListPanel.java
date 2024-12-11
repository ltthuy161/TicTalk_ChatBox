package gui;

import utils.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserListPanel {
    private JPanel mainPanel;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserListPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = createHeaderPanel(mainContainer);

        // Create sidebar panel
        JPanel sidebar = createSidebarPanel(mainContainer);

        // Create column names for the table
        String[] columnNames = {"Username", "Full name", "Address", "Date of birth", "Gender", "Email", "Password", "Status"};

        // Fetch data from the database
        List<Object[]> data = fetchDataFromDatabase();

        // Create the table model with fetched data
        tableModel = new DefaultTableModel(data.toArray(new Object[0][]), columnNames);

        // Create the table and set the model
        userTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Add components to the main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST); // Sidebar on the left
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Table in the center
    }

    private JPanel createHeaderPanel(JPanel mainContainer) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("Manage account", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Add action listener to the back button
        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        return headerPanel;
    }

    private JPanel createSidebarPanel(JPanel mainContainer) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Create buttons
        JButton addButton = createSidebarButton("Add account", () -> new AddUserDialog(mainPanel, tableModel));
        JButton editButton = createSidebarButton("Edit account", () -> new EditUserHandler(mainPanel, userTable, tableModel).execute());
        JButton lockButton = createSidebarButton("Lock/Unlock account", () -> new LockUnlockHandler(mainPanel, userTable, tableModel).execute());
        JButton deleteButton = createSidebarButton("Delete account", () -> new DeleteUserHandler(mainPanel, userTable, tableModel).execute());
        JButton updatePasswordButton = createSidebarButton("Update password", () -> new UpdatePasswordDialog(mainPanel, userTable).execute());
        JButton viewHistoryButton = createSidebarButton("Login history", () -> new ViewHistoryDialog(mainPanel, userTable).execute());
        JButton friendListButton = createSidebarButton("Friends list", () -> new FriendListDialog(mainPanel, userTable).execute());

        // Add buttons to the sidebar
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(addButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(editButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(lockButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(deleteButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(updatePasswordButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(viewHistoryButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(friendListButton);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JButton createSidebarButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private List<Object[]> fetchDataFromDatabase() {
        List<Object[]> data = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        String sql = "SELECT username, fullname, address, dateofbirth, gender, email, password, status FROM users";  // Adjust query as per your schema
        try (PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                String fullname = rs.getString("fullname");
                String address = rs.getString("address");
                String dob = rs.getString("dateofbirth");
                String gender = rs.getString("gender");
                String email = rs.getString("email");
                String password = rs.getString("password");
                String status = rs.getString("status");
                data.add(new Object[]{username, fullname, address, dob, gender, email, password, status});
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Cannot access data from database (User List)", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        return data;
    }
}
