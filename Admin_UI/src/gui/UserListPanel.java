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
        mainPanel = new JPanel(new BorderLayout());

        // Create header and sidebar panels
        JPanel headerPanel = createHeaderPanel(mainContainer);
        JPanel sidebarPanel = createSidebarPanel(mainContainer);

        // Set up table columns
        String[] columnNames = {"Username", "Full Name", "Address", "Date of Birth", "Gender", "Email", "Password", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0);

        // Create and configure the user table
        userTable = new JTable(tableModel);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Fetch initial data for the table
        fetchDataFromDatabase();

        // Add components to the main panel
        JScrollPane scrollPane = new JScrollPane(userTable);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(JPanel mainContainer) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return to Dashboard");
        JLabel titleLabel = new JLabel("Manage Accounts", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createSidebarPanel(JPanel mainContainer) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Create buttons for sidebar
        sidebar.add(createSidebarButton("Add Account", () -> new AddUserDialog(mainPanel, tableModel)));
        sidebar.add(createSidebarButton("Edit Account", () -> new EditUserHandler(mainPanel, userTable, tableModel).execute()));
        sidebar.add(createSidebarButton("Lock/Unlock Account", () -> new LockUnlockHandler(mainPanel, userTable, tableModel).execute()));
        sidebar.add(createSidebarButton("Delete Account", () -> new DeleteUserHandler(mainPanel, userTable, tableModel).execute()));
        sidebar.add(createSidebarButton("Update Password", () -> new UpdatePasswordDialog(mainPanel, userTable, tableModel).execute()));
        sidebar.add(createSidebarButton("View Login History", () -> new ViewHistoryDialog(mainPanel, userTable).execute()));
        sidebar.add(createSidebarButton("Friends List", () -> new FriendListDialog(mainPanel, userTable).execute()));
        sidebar.add(createSidebarButton("Reset Password", () -> new ResetPasswordHandler(mainPanel, userTable, tableModel).execute()));

        sidebar.add(Box.createVerticalGlue());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> fetchDataFromDatabase());
        sidebar.add(refreshButton);
        return sidebar;
    }

    private JButton createSidebarButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout layout = (CardLayout) mainContainer.getLayout();
        layout.show(mainContainer, "Dashboard");
    }

    private void fetchDataFromDatabase() {
        String query = "SELECT username, fullname, address, dateofbirth, gender, email, password, status FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Clear existing data in the table model
            tableModel.setRowCount(0);

            // Populate the table model with data from the database
            while (rs.next()) {
                Object[] row = {
                        rs.getString("username"),
                        rs.getString("fullname"),
                        rs.getString("address"),
                        rs.getString("dateofbirth"),
                        rs.getString("gender"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("status")
                };
                tableModel.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainPanel, "Failed to fetch data from the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}