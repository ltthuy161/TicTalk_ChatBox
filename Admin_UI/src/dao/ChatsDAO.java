package dao;

import model.Chats;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatsDAO {
    private Connection connection;

    public ChatsDAO(Connection connection) {
        this.connection = connection;
    }

    public void createChat(Chats chat) throws SQLException {
        String sql = "INSERT INTO Chats (SenderID, ReceiverID, Message, Timestamp) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, chat.getSenderID());
            stmt.setInt(2, chat.getReceiverID());
            stmt.setString(3, chat.getMessage());
            stmt.setTimestamp(4, chat.getTimestamp());
            stmt.executeUpdate();
        }
    }

    public List<Chats> getChatsByUserId(int userId) throws SQLException {
        List<Chats> chatsList = new ArrayList<>();
        String sql = "SELECT * FROM Chats WHERE SenderID = ? OR ReceiverID = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Chats chat = new Chats();
                chat.setChatID(rs.getInt("ChatID"));
                chat.setSenderID(rs.getInt("SenderID"));
                chat.setReceiverID(rs.getInt("ReceiverID"));
                chat.setMessage(rs.getString("Message"));
                chat.setTimestamp(rs.getTimestamp("Timestamp"));
                chatsList.add(chat);
            }
        }
        return chatsList;
    }
}
