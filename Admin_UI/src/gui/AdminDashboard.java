package gui;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard {
    private JFrame frame;
    private JPanel mainPanel;

    public AdminDashboard() {
        // Frame setup
        frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Main panel with CardLayout
        mainPanel = new JPanel(new CardLayout());

        // Create and add panels to mainPanel
        addPanelsToMainPanel();

        // Show the main dashboard
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "Dashboard");

        // Add the mainPanel to the frame
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void addPanelsToMainPanel() {
        // Create the individual panels
        JPanel dashboardPanel = createDashboardPanel();
        JPanel userListPanel = new UserListPanel(mainPanel).getMainPanel();
        JPanel loginHistoryPanel = new LoginHistoryPanel(mainPanel).getPanel();
        JPanel chatGroupsPanel = new ChatGroupsPanel(mainPanel).getMainPanel();
        JPanel spamPanel = new SpamReportsPanel(mainPanel).getMainPanel();
        JPanel newRegistrationPanel = new RegistrationListPanel(mainPanel).getMainPanel();
        JPanel activeUsersPanel = new ActiveUsersPanel(mainPanel).getMainPanel();
        JPanel registrationChartPanel = new RegistrationChartPanel(mainPanel).getMainPanel();
        JPanel activeUsersChartPanel = new ActiveUsersChartPanel(mainPanel).getMainPanel();
        JPanel usersAndFriendsPanel = new UsersAndFriendsPanel(mainPanel).getMainPanel();

        // Add all panels to the mainPanel with respective names
        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(userListPanel, "UserList");
        mainPanel.add(loginHistoryPanel, "LoginHistory");
        mainPanel.add(chatGroupsPanel, "ChatGroups");
        mainPanel.add(spamPanel, "Spams");
        mainPanel.add(newRegistrationPanel, "NewRegistration");
        mainPanel.add(activeUsersPanel, "ActiveUsers");
        mainPanel.add(registrationChartPanel, "RegistrationChart");
        mainPanel.add(activeUsersChartPanel, "ActiveUsersChart");
        mainPanel.add(usersAndFriendsPanel, "UsersAndFriends");
    }

    private JPanel createDashboardPanel() {
        // Dashboard panel setup
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));

        // Header Label
        JLabel headerLabel = new JLabel("Admin Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button setup
        JButton userListButton = createButton("Manage Account", "UserList");
        JButton loginHistoryButton = createButton("Login History", "LoginHistory");
        JButton chatGroupsButton = createButton("Groups Chat", "ChatGroups");
        JButton spamButton = createButton("Spams", "Spams");
        JButton newRegistrationButton = createButton("New Registration", "NewRegistration");
        JButton activeUsersButton = createButton("Active Users", "ActiveUsers");
        JButton registrationChartButton = createButton("Registration Chart", "RegistrationChart");
        JButton activeUsersChartButton = createButton("Active Users Chart", "ActiveUsersChart");
        JButton usersAndFriendsButton = createButton("Friends List", "UsersAndFriends");

        // Adding components to the dashboard panel
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(headerLabel);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(userListButton);
        dashboardPanel.add(loginHistoryButton);
        dashboardPanel.add(chatGroupsButton);
        dashboardPanel.add(spamButton);
        dashboardPanel.add(newRegistrationButton);
        dashboardPanel.add(activeUsersButton);
        dashboardPanel.add(registrationChartButton);
        dashboardPanel.add(activeUsersChartButton);
        dashboardPanel.add(usersAndFriendsButton);

        return dashboardPanel;
    }

    private JButton createButton(String text, String panelName) {
        JButton button = new JButton(text);
        button.addActionListener(e -> switchToPanel(panelName));
        return button;
    }

    private void switchToPanel(String panelName) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
