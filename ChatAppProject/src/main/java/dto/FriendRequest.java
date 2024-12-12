// In dto.FriendRequest.java
package dto;

import java.sql.Timestamp;

public class FriendRequest {
    private int requestId;
    private String senderUsername;
    private String receiverUsername;
    private String status;
    private Timestamp requestTime;

    // Constructor
    public FriendRequest(int requestId, String senderUsername, String receiverUsername, String status, Timestamp requestTime) {
        this.requestId = requestId;
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.status = status;
        this.requestTime = requestTime;
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Timestamp requestTime) {
        this.requestTime = requestTime;
    }
}