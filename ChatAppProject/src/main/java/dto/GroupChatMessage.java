package dto;
import java.sql.Timestamp;

public class GroupChatMessage {
    private String sender;
    private String message;
    private int groupId;
    Timestamp timestamp;

    public GroupChatMessage() {
        super();
    }

    public GroupChatMessage(String sender, String message, int groupId, Timestamp timestamp) {
        this.sender = sender;
        this.message = message;
        this.groupId = groupId;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public int getGroupId() {
        return groupId;
    }
    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
