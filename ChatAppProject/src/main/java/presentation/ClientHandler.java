package presentation;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize input/output streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Read the first message as the client name
            clientName = in.readLine();
            if (clientName == null || clientName.trim().isEmpty()) {
                System.out.println("Client connected without a valid name. Disconnecting...");
                close();
                return;
            }

            System.out.println("Client connected: " + clientName);

            // Listen for messages from the client
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Message from " + clientName + ": " + message);

                // Ensure the message is in the expected format `recipient|message`
                String[] parts = message.split("\\|", 3);
                System.out.println(parts[0] + " " + parts[1] + " " + parts[2]);

                if (parts.length == 3) {
                    String recipient = parts[1].trim();
                    String content = parts[2].trim();

                    // Send the message to the recipient
                    ChatServer.sendMessageToRecipient(clientName + ": " + content, this, recipient);
                } else {
                    // Handle invalid message format
                    System.err.println("Invalid message format received from client " + clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Connection error with client " + clientName + ": " + e.getMessage());
        } finally {
            // Cleanup and disconnect when the client disconnects
            ChatServer.removeClient(this);
            try {
                close();
            } catch (IOException e) {
                System.err.println("Error closing connection for client " + clientName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Send a message to this client.
     *
     * @param message The message to send
     * @return `true` if the message was sent successfully, `false` if there was an error
     */
    public boolean sendMessage(String message) {
        try {
            out.println(message);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send message to client " + clientName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Close the socket and associated resources.
     *
     * @throws IOException if there is an error closing the resources
     */
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }

    /**
     * Get the client's name (username).
     *
     * @return The client's name
     */
    public String getClientName() {
        return clientName;
    }
}