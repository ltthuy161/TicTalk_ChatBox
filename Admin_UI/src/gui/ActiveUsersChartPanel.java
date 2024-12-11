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
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Biểu đồ số lượng người hoạt động theo năm", SwingConstants.CENTER);
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
        JButton selectYearButton = new JButton("Chọn năm");
        selectYearButton.addActionListener(e -> showYearSelectionDialog());

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(selectYearButton, BorderLayout.CENTER);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void showYearSelectionDialog() {
        String year = JOptionPane.showInputDialog("Nhập năm (ví dụ: 2024):");

        if (year != null && !year.trim().isEmpty()) {
            try {
                int selectedYear = Integer.parseInt(year.trim());
                Map<Integer, Integer> monthlyData = getMonthlyActiveUsersData(selectedYear);
                drawBarChart(selectedYear, monthlyData);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập năm hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Map<Integer, Integer> getMonthlyActiveUsersData(int year) {
        Map<Integer, Integer> monthlyData = new HashMap<>();
        // Initialize months to 0
        for (int i = 1; i <= 12; i++) {
            monthlyData.put(i, 0);
        }

        String query = "SELECT EXTRACT(MONTH FROM activity_date) AS month, COUNT(*) AS count " +
                       "FROM user_activity WHERE EXTRACT(YEAR FROM activity_date) = ? " +
                       "GROUP BY month ORDER BY month";

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
            JOptionPane.showMessageDialog(null, "Lỗi kết nối cơ sở dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        return monthlyData;
    }

    private void drawBarChart(int year, Map<Integer, Integer> monthlyData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<Integer, Integer> entry : monthlyData.entrySet()) {
            int month = entry.getKey();
            int count = entry.getValue();
            dataset.addValue(count, "Số lượng", "Tháng " + month);
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Số lượng người hoạt động - Năm " + year,
                "Tháng",
                "Số lượng người mở ứng dụng",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        // Show the chart in a new dialog
        JDialog chartDialog = new JDialog();
        chartDialog.setTitle("Biểu đồ số lượng người hoạt động");
        chartDialog.setSize(800, 600);
        chartDialog.add(chartPanel);
        chartDialog.setLocationRelativeTo(null);
        chartDialog.setVisible(true);
    }
}
