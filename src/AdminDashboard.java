import javax.swing.*;
import java.awt.*;

public class AdminDashboard {
    private JFrame frame;
    private JPanel mainPanel;

    public AdminDashboard() {
        frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Main panel with card layout
        mainPanel = new JPanel(new CardLayout());

        // Add pages to main panel
        JPanel dashboardPanel = createDashboardPanel(mainPanel);
        JPanel userListPanel = new UserListPanel(mainPanel).getMainPanel();
        JPanel loginHistoryPanel = new LoginHistoryPanel(mainPanel).getPanel();
        JPanel chatGroupsPanel = new ChatGroupsPanel(mainPanel).getMainPanel();
        JPanel spamPanel = new SpamReportsPanel(mainPanel).getMainPanel();
        JPanel newRegistrationPanel = new RegistrationListPanel(mainPanel).getMainPanel();
        JPanel activeUsersPanel = new ActiveUsersPanel(mainPanel).getMainPanel();
        JPanel registrationChartPanel = new RegistrationChartPanel(mainPanel).getMainPanel();
        JPanel activeUsersChartPanel = new ActiveUsersChartPanel(mainPanel).getMainPanel();
        JPanel usersAndFriendsPanel = new UsersAndFriendsPanel(mainPanel).getMainPanel();

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

        // Show the main dashboard
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "Dashboard");

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private JPanel createDashboardPanel(JPanel mainPanel) {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BoxLayout(dashboardPanel, BoxLayout.Y_AXIS));

        JLabel headerLabel = new JLabel("Admin Dashboard");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton userListButton = new JButton("Quản lý người dùng");
        JButton loginHistoryButton = new JButton("Lịch sử đăng nhập");
        JButton chatGroupsButton = new JButton("Các nhóm chat");
        JButton spamButton = new JButton("Spams");
        JButton newRegistrationButton = new JButton("Đăng ký mới");
        JButton activeUsersButton = new JButton("Đang hoạt động");
        JButton registrationChartButton = new JButton("Biểu đồ người dùng đăng kí mới");
        JButton activeUsersChartButton = new JButton("Biểu đồ người dùng hoạt động");
        JButton usersAndFriendsButton = new JButton("Danh sách bạn bè người dùng");

        // Set button actions
        userListButton.addActionListener(e -> switchToPanel(mainPanel, "UserList"));
        loginHistoryButton.addActionListener(e -> switchToPanel(mainPanel, "LoginHistory"));
        chatGroupsButton.addActionListener(e -> switchToPanel(mainPanel, "ChatGroups"));
        spamButton.addActionListener(e -> switchToPanel(mainPanel, "Spams"));
        newRegistrationButton.addActionListener(e -> switchToPanel(mainPanel, "NewRegistration"));
        activeUsersButton.addActionListener(e -> switchToPanel(mainPanel, "ActiveUsers"));
        registrationChartButton.addActionListener(e -> switchToPanel(mainPanel, "RegistrationChart"));
        activeUsersChartButton.addActionListener(e -> switchToPanel(mainPanel, "ActiveUsersChart"));
        usersAndFriendsButton.addActionListener(e -> switchToPanel(mainPanel, "UsersAndFriends"));

        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(headerLabel);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        dashboardPanel.add(userListButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(loginHistoryButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(chatGroupsButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(spamButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(newRegistrationButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(activeUsersButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(registrationChartButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(activeUsersChartButton);
        dashboardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        dashboardPanel.add(usersAndFriendsButton);

        return dashboardPanel;
    }

    private void switchToPanel(JPanel mainPanel, String panelName) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
