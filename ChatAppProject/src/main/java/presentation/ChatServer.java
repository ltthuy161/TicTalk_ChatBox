package presentation;

import bus.UserBUS;
import java.io.*;
import java.net.*;
import java.util.*;
import dto.User;  // Add this import for the User class


public class ChatServer {
    private static final int PORT = 6500; // Port the server will listen on
    private static final Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>()); // Store connected clients
    private static UserBUS userBUS = new UserBUS(); // Instantiate UserBUS here (static)
    private static final Map<Integer, Set<ClientHandler>> groups = new HashMap<>(); // Lưu trữ các nhóm và thành viên của chúng

    public static void main(String[] args) {
        System.out.println("Chat server is running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Accept client connections
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Create a handler for the new client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);

                // Start a new thread for each client handler
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Send a message to a specific recipient.
     *
     * @param message   The message to send
     * @param sender    The client who sent the message
     * @param recipient The recipient's username
     */
    public static void sendMessageToRecipient(String message, ClientHandler sender, String recipient) {
        synchronized (clientHandlers) {
            boolean found = false;
            for (ClientHandler client : clientHandlers) {
                if (client.getClientName().equals(recipient)) {
                    client.sendMessage(message); // Send the message to the client
                    found = true;
                    break;
                }
            }
            if (!found) {
                sender.sendMessage("Server: User " + recipient + " is not online.");
            }
        }
    }

    /**
     * Send a message to a specific group.
     *
     * @param message  The message to send
     * @param sender   The client who sent the message
     * @param groupId  The group ID
     */
    public static void sendGroupMessage(String message, ClientHandler sender, int groupId) {
        synchronized (groups) {
            List<User> groupMembers = userBUS.getGroupMembers(groupId);  // Lấy danh sách thành viên từ database
            System.out.println(groupMembers);

            if (groupMembers == null || groupMembers.isEmpty()) {
                sender.sendMessage("Server: No members found in group " + groupId); // Nếu nhóm không tồn tại hoặc không có thành viên
                return;
            }

            // Duyệt qua từng thành viên trong nhóm và gửi tin nhắn
            for (User user : groupMembers) {
                // Truy cập tên người dùng (username) của mỗi đối tượng User
                String username = user.getUsername(); // Giả sử User có phương thức getUsername()
                System.out.println(username);

                // Tìm ClientHandler dựa trên username (vì bạn cần gửi tin nhắn tới username)
                for (ClientHandler client : clientHandlers) {
                    if (client.getClientName().equals(username)) {
                        client.sendMessage(message); // Gửi tin nhắn đến client có username tương ứng
                        break;
                    }
                }
            }
            System.out.println("Message sent to group: " + groupId);
        }
    }

    /**
     * Remove a client when they disconnect.
     *
     * @param clientHandler The client to remove
     */
    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(clientHandler); // Remove the client from the connected clients list
        }
        System.out.println("Client disconnected: " + clientHandler.getClientName());
    }
}