package model;

import java.sql.Timestamp;

public class SpamReports {
    private int reportID;
    private int reporterID;
    private int reportedID;
    private String message;
    private Timestamp reportedAt;

    // Getters and Setters
    public int getReportID() {
        return reportID;
    }

    public void setReportID(int reportID) {
        this.reportID = reportID;
    }

    public int getReporterID() {
        return reporterID;
    }

    public void setReporterID(int reporterID) {
        this.reporterID = reporterID;
    }

    public int getReportedID() {
        return reportedID;
    }

    public void setReportedID(int reportedID) {
        this.reportedID = reportedID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(Timestamp reportedAt) {
        this.reportedAt = reportedAt;
    }
}
