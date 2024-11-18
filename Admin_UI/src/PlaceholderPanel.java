import javax.swing.*;
import java.awt.*;

public class PlaceholderPanel {
    private JPanel panel;

    public PlaceholderPanel(JPanel mainPanel, String title) {
        panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel(title, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(label, BorderLayout.CENTER);

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
