import javax.swing.SwingUtilities;

import gui.AdminDashboard;

public class Main {
    public static void main(String[] args) {
        // Start the Admin Dashboard
        SwingUtilities.invokeLater(AdminDashboard::new);
    }
}
