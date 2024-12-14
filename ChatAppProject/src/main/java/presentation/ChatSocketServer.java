package presentation;

import bus.UserBUS;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatSocketServer {
    private static final int PORT = 8080;
    private static final Map<String, Socket> clients = new ConcurrentHashMap<>();
    private static UserBUS userBUS = new UserBUS();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10); // Thread pool for handling clients

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private String username;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // First message should be IDENTIFY:<username>
                String identifyMessage = reader.readLine();
                if (identifyMessage != null && identifyMessage.startsWith("IDENTIFY:")) {
                    username = identifyMessage.substring("IDENTIFY:".length());
                    clients.put(username, clientSocket);
                    userBUS.setUserOnline(username);
                    System.out.println(username + " connected from " + clientSocket.getInetAddress());
                } else {
                    System.err.println("Invalid initial message from client");
                    return;
                }

                // Send acknowledgment back to the client
                writer.println("IDENTIFIED");

                // Inform all clients about the new user online
                broadcast(username + " has joined the chat.", username);

                // Listen for messages and process them
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received message from " + username + ": " + message);
                    if (message.startsWith("PRIVATE:")) {
                        // Private message format: PRIVATE:<recipient>:<message>
                        String[] parts = message.substring("PRIVATE:".length()).split(":", 2);
                        if (parts.length == 2) {
                            String recipient = parts[0];
                            String privateMessage = parts[1];
                            sendPrivateMessage(username, recipient, privateMessage);
                        }
                    } else {
                        // Broadcast the message to all clients
                        broadcast(username + ": " + message, username);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (username != null) {
                    clients.remove(username);
                    userBUS.setUserOffline(username);
                    System.out.println(username + " disconnected.");
                    broadcast(username + " has left the chat.", username); // Inform other clients
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void sendPrivateMessage(String sender, String recipient, String message) {
            Socket recipientSocket = clients.get(recipient);
            if (recipientSocket != null) {
                try {
                    PrintWriter recipientWriter = new PrintWriter(recipientSocket.getOutputStream(), true);
                    recipientWriter.println("PRIVATE:" + sender + ":" + message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle the case where the recipient is not found
                try {
                    PrintWriter senderWriter = new PrintWriter(clientSocket.getOutputStream(), true);
                    senderWriter.println("SERVER: User " + recipient + " is not online.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        private void broadcast(String message, String sender) {
            for (Map.Entry<String, Socket> entry : clients.entrySet()) {
                if (!entry.getKey().equals(sender)) { // Don't send back to the sender
                    try {
                        PrintWriter writer = new PrintWriter(entry.getValue().getOutputStream(), true);
                        writer.println(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}