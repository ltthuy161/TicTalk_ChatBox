import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class RegistrationChartPanel {
    private JPanel mainPanel;

    public RegistrationChartPanel(JPanel mainContainer) {
        mainPanel = new JPanel(new BorderLayout());

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Biểu đồ số lượng người đăng ký mới theo năm", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Back button action listener
        backButton.addActionListener(e -> {
            if (mainContainer.getLayout() instanceof CardLayout) {
                CardLayout cl = (CardLayout) mainContainer.getLayout();
                cl.show(mainContainer, "Dashboard"); // Show Dashboard
            } else {
                System.err.println("Error: mainContainer does not use CardLayout!");
            }
        });

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Select year button
        JButton selectYearButton = new JButton("Chọn năm");
        selectYearButton.addActionListener(e -> showYearSelectionDialog());

        // Add components to mainPanel
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
                Map<Integer, Integer> monthlyData = getMonthlyRegistrationData(selectedYear);
                drawBarChart(selectedYear, monthlyData);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Vui lòng nhập năm hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Map<Integer, Integer> getMonthlyRegistrationData(int year) {
        // Sample data
        String[][] sampleData = {
                {"2024-01-15", "John Doe"},
                {"2024-02-20", "Jane Smith"},
                {"2024-02-25", "Alice Brown"},
                {"2024-03-10", "Charlie Black"},
                {"2024-03-15", "Eve White"},
                {"2024-04-01", "Mallory Gray"},
                {"2024-05-05", "Oscar Blue"},
                {"2024-05-10", "Peggy Green"},
                {"2024-06-18", "Victor Yellow"},
                {"2024-07-22", "Walter Red"}
        };

        Map<Integer, Integer> monthlyData = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthlyData.put(i, 0);
        }

        for (String[] record : sampleData) {
            String date = record[0];
            int recordYear = Integer.parseInt(date.substring(0, 4));
            int recordMonth = Integer.parseInt(date.substring(5, 7));

            if (recordYear == year) {
                monthlyData.put(recordMonth, monthlyData.getOrDefault(recordMonth, 0) + 1);
            }
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
                "Số lượng người đăng ký mới - Năm " + year,
                "Tháng",
                "Số lượng",
                dataset
        );

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        JDialog chartDialog = new JDialog();
        chartDialog.setTitle("Biểu đồ số lượng người đăng ký mới");
        chartDialog.setSize(800, 600);
        chartDialog.add(chartPanel);
        chartDialog.setLocationRelativeTo(null);
        chartDialog.setVisible(true);
    }
}
