package dto;

import java.sql.Timestamp;

public class Group {
    private int groupId;
    private String groupName;
    private String createdBy;
    private Timestamp createdAt;

    // Constructor
    public Group() {
    }

    public Group(int groupId, String groupName, String createdBy, Timestamp createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}