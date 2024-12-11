package model;

public class GroupMembers {
    private int groupMemberID;
    private int groupID;
    private int userID;
    private String role;

    // Getters and Setters
    public int getGroupMemberID() {
        return groupMemberID;
    }

    public void setGroupMemberID(int groupMemberID) {
        this.groupMemberID = groupMemberID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
