package model;

import java.sql.Timestamp;

public class LogOutHistory {
    private int logOutID;
    private int userID;
    private Timestamp logOutTime;

    // Getters and Setters
    public int getLogOutID() {
        return logOutID;
    }

    public void setLogOutID(int logOutID) {
        this.logOutID = logOutID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Timestamp getLogOutTime() {
        return logOutTime;
    }

    public void setLogOutTime(Timestamp logOutTime) {
        this.logOutTime = logOutTime;
    }
}
