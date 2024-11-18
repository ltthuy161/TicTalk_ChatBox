import javax.swing.*;
import java.awt.*;


public class LoginHistoryPanel {
    private JPanel panel;

    public LoginHistoryPanel(JPanel mainPanel) {
        panel = new JPanel(new BorderLayout());

        JLabel headerLabel = new JLabel("Lịch sử đăng nhập", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(headerLabel, BorderLayout.NORTH);

        // Table with login history
        String[] columnNames = {"Tên đăng nhập", "Họ tên", "Thời gian đăng nhập"};
        Object[][] data = {
            {"user0", "Nguyễn Văn A", "2021-05-01 08:00:00"},
            {"user1", "Trần Thị B", "2021-05-01 08:30:00"},
            {"user2", "Lê Văn C", "2021-05-01 09:00:00"},
            {"user3", "Phạm Thị D", "2021-05-01 09:30:00"},
            {"user4", "Hoàng Văn E", "2021-05-01 10:00:00"},
        };
        JTable table = new JTable(data, columnNames);
        table.setFillsViewportHeight(true);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Quay lại Dashboard");
        backButton.addActionListener(e -> switchToPanel(mainPanel, "Dashboard"));
        panel.add(backButton, BorderLayout.SOUTH);
    }

    private void switchToPanel(JPanel mainPanel, String panelName) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, panelName);
    }

    public JPanel getPanel() {
        return panel;
    }    
}
