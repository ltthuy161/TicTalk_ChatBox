package presentation;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 6000; // Cổng mà server sẽ lắng nghe
    private static final Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat server is running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                // Tạo một handler để xử lý client mới
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);

                // Chạy handler trên một thread riêng
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn đến một người nhận cụ thể.
     *
     * @param message   Tin nhắn cần gửi
     * @param sender    Client gửi tin nhắn
     * @param recipient Người nhận tin nhắn
     */
    public static void sendMessageToRecipient(String message, ClientHandler sender, String recipient) {
        synchronized (clientHandlers) {
            boolean found = false;
            for (ClientHandler client : clientHandlers) {
                if (client.getClientName().equals(recipient)) {
                    client.sendMessage(message);
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
     * Loại bỏ client khi ngắt kết nối.
     *
     * @param clientHandler Client cần loại bỏ
     */
    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client disconnected: " + clientHandler.getClientName());
    }
}