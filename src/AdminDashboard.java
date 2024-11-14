import javax.swing.*;
import java.awt.*;

public class AdminDashboard {
    private JFrame frame;

    public AdminDashboard() {
        frame = new JFrame("Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Main panel with card layout
        JPanel mainPanel = new JPanel(new CardLayout());

        // Add pages to main panel
        JPanel dashboardPanel = createDashboardPanel(mainPanel);
        JPanel userListPanel = new UserListPanel(mainPanel).getMainPanel();
        JPanel loginHistoryPanel = createPlaceholderPanel("Lịch sử đăng nhập");
        JPanel chatGroupsPanel = createPlaceholderPanel("Các nhóm chat");
        JPanel spamPanel = createPlaceholderPanel("Spams");
        JPanel newRegistrationPanel = createPlaceholderPanel("Đăng ký mới");
        JPanel activeUsersPanel = createPlaceholderPanel("Đang hoạt động");

        mainPanel.add(dashboardPanel, "Dashboard");
        mainPanel.add(userListPanel, "UserList");
        mainPanel.add(loginHistoryPanel, "LoginHistory");
        mainPanel.add(chatGroupsPanel, "ChatGroups");
        mainPanel.add(spamPanel, "Spams");
        mainPanel.add(newRegistrationPanel, "NewRegistration");
        mainPanel.add(activeUsersPanel, "ActiveUsers");

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

        // Buttons for each page
        JButton userListButton = new JButton("Quản lý người dùng");
        JButton loginHistoryButton = new JButton("Lịch sử đăng nhập");
        JButton chatGroupsButton = new JButton("Các nhóm chat");
        JButton spamButton = new JButton("Spams");
        JButton newRegistrationButton = new JButton("Đăng ký mới");
        JButton activeUsersButton = new JButton("Đang hoạt động");

        // Set button actions
        userListButton.addActionListener(e -> switchToPanel(mainPanel, "UserList"));
        loginHistoryButton.addActionListener(e -> switchToPanel(mainPanel, "LoginHistory"));
        chatGroupsButton.addActionListener(e -> switchToPanel(mainPanel, "ChatGroups"));
        spamButton.addActionListener(e -> switchToPanel(mainPanel, "Spams"));
        newRegistrationButton.addActionListener(e -> switchToPanel(mainPanel, "NewRegistration"));
        activeUsersButton.addActionListener(e -> switchToPanel(mainPanel, "ActiveUsers"));

        // Add components to the dashboard panel
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

        return dashboardPanel;
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.CENTER);

        JButton backButton = new JButton("Quay lại Dashboard");
        backButton.addActionListener(e -> switchToPanel((JPanel) panel.getParent(), "Dashboard"));
        panel.add(backButton, BorderLayout.SOUTH);

        return panel;
    }

    private void switchToPanel(JPanel mainPanel, String panelName) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
