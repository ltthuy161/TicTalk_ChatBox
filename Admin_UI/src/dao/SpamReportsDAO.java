package dao;

import model.SpamReports;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpamReportsDAO {
    private Connection connection;

    public SpamReportsDAO(Connection connection) {
        this.connection = connection;
    }

    public void reportSpam(SpamReports report) throws SQLException {
        String sql = "INSERT INTO SpamReports (ReporterID, ReportedID, Message, ReportedAt) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, report.getReporterID());
            stmt.setInt(2, report.getReportedID());
            stmt.setString(3, report.getMessage());
            stmt.setTimestamp(4, report.getReportedAt());
            stmt.executeUpdate();
        }
    }

    public List<SpamReports> getReportsByUserId(int userId) throws SQLException {
        List<SpamReports> reportsList = new ArrayList<>();
        String sql = "SELECT * FROM SpamReports WHERE ReporterID = ? OR ReportedID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SpamReports report = new SpamReports();
                report.setReportID(rs.getInt("ReportID"));
                report.setReporterID(rs.getInt("ReporterID"));
                report.setReportedID(rs.getInt("ReportedID"));
                report.setMessage(rs.getString("Message"));
                report.setReportedAt(rs.getTimestamp("ReportedAt"));
                reportsList.add(report);
            }
        }
        return reportsList;
    }
}
