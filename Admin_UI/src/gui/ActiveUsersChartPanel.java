package gui;

import utils.DBConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ActiveUsersChartPanel {
    private JPanel mainPanel;

    public ActiveUsersChartPanel(JPanel mainContainer) {
        mainPanel = new JPanel(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Return Dashboard");
        JLabel headerLabel = new JLabel("Chart of Active Users Yearly", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Back button action
        backButton.addActionListener(e -> {
            if (mainContainer.getLayout() instanceof CardLayout) {
                CardLayout cl = (CardLayout) mainContainer.getLayout();
                cl.show(mainContainer, "Dashboard");
            } else {
                System.err.println("Error: mainContainer does not use CardLayout!");
            }
        });

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Select year button
        JButton selectYearButton = new JButton("Select Year");
        selectYearButton.addActionListener(e -> showYearSelectionDialog());

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(selectYearButton, BorderLayout.CENTER);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void showYearSelectionDialog() {
        String year = JOptionPane.showInputDialog("Enter year (ex: 2024):");

        if (year != null && !year.trim().isEmpty()) {
            try {
                int selectedYear = Integer.parseInt(year.trim());
                Map<Integer, Integer> monthlyData = getMonthlyActiveUsersData(selectedYear);
                drawBarChart(selectedYear, monthlyData);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter correct year!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Map<Integer, Integer> getMonthlyActiveUsersData(int year) {
        Map<Integer, Integer> monthlyData = new HashMap<>();
        // Initialize months to 0
        for (int i = 1; i <= 12; i++) {
            monthlyData.put(i, 0);
        }

        String query = "SELECT EXTRACT(MONTH FROM lh.LoginTime) AS month, " +
                "COUNT(*) AS count " +
                "FROM LoginHistory lh " +
                "WHERE EXTRACT(YEAR FROM lh.LoginTime) = ? " +
                "GROUP BY EXTRACT(MONTH FROM lh.LoginTime) " +
                "ORDER BY month";

        try (Connection conn = DBConnection.getConnection(); // Ensure DBConnection is properly set up
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int month = rs.getInt("month");
                    int count = rs.getInt("count");
                    monthlyData.put(month, count);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database connection error!", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return monthlyData;
    }

    private void drawBarChart(int year, Map<Integer, Integer> monthlyData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Integer> entry : monthlyData.entrySet()) {
            int month = entry.getKey();
            int count = entry.getValue();
            dataset.addValue(count, "Number of Active User", "Month " + month);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Number of Active Users - Year " + year,
                "Month",
                "Number of Active Users",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        // Show the chart in a new dialog
        JDialog chartDialog = new JDialog();
        chartDialog.setTitle("Chart of Active Users");
        chartDialog.setSize(800, 600);
        chartDialog.add(chartPanel);
        chartDialog.setLocationRelativeTo(null);
        chartDialog.setVisible(true);
    }
}
