package model;

import java.sql.Timestamp;

public class BlockedUsers {
    private int blockID;
    private int blockerID;
    private int blockedID;
    private Timestamp blockedAt;

    // Getters and Setters
    public int getBlockID() {
        return blockID;
    }

    public void setBlockID(int blockID) {
        this.blockID = blockID;
    }

    public int getBlockerID() {
        return blockerID;
    }

    public void setBlockerID(int blockerID) {
        this.blockerID = blockerID;
    }

    public int getBlockedID() {
        return blockedID;
    }

    public void setBlockedID(int blockedID) {
        this.blockedID = blockedID;
    }

    public Timestamp getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(Timestamp blockedAt) {
        this.blockedAt = blockedAt;
    }
}
