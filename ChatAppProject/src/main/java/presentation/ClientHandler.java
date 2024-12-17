package presentation;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;  // Tên của client kết nối
    private Set<Integer> groups;  // Danh sách nhóm mà client tham gia (dùng Set để lưu trữ nhóm)

    // Constructor khởi tạo
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.groups = new HashSet<>();  // Khởi tạo Set để lưu trữ nhóm mà client tham gia
    }

    // Kiểm tra xem đây có phải là tin nhắn nhóm (group ID).
    private boolean isGroupMessage(String recipientOrGroupIdStr) {
        try {
            // Thử chuyển recipientOrGroupIdStr thành số nguyên (group ID)
            Integer.parseInt(recipientOrGroupIdStr);
            return true;  // Nếu chuyển được, là group ID
        } catch (NumberFormatException e) {
            return false;  // Nếu không chuyển được, là recipient (username)
        }
    }


    @Override
    public void run() {
        try {
            // Khởi tạo luồng vào/ra cho socket
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Đọc tên client gửi khi kết nối
            clientName = in.readLine();
            if (clientName == null || clientName.trim().isEmpty()) {
                System.out.println("Client kết nối không có tên hợp lệ. Ngắt kết nối...");
                close();
                return;
            }

            System.out.println("Client kết nối: " + clientName);

            // Lắng nghe các tin nhắn từ client
            // Lắng nghe các tin nhắn từ client
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Tin nhắn từ " + clientName + ": " + message);

                // Đảm bảo tin nhắn có định dạng đúng: sender|recipientOrGroupId|message
                String[] parts = message.split("\\|", 3);
                if (parts.length == 3) {
                    String recipientOrGroupIdStr = parts[1].trim();  // Người nhận hoặc group ID
                    String content = parts[2].trim();  // Nội dung tin nhắn

                    // Kiểm tra nếu recipientOrGroupIdStr là group ID
                    // Kiểm tra nếu recipientOrGroupIdStr là group ID
                    if (isGroupMessage(recipientOrGroupIdStr)) {
                        // Định dạng đúng, gửi đến nhóm
                        int groupId = Integer.parseInt(recipientOrGroupIdStr); // Convert to groupId (int)
                        String messageToSend = clientName + "|" + recipientOrGroupIdStr + "|" + content;  // Tạo tin nhắn gửi từ client

                        System.out.println("Nek" + messageToSend);
                        // Gọi sendGroupMessage với các tham số
                        ChatServer.sendGroupMessage(messageToSend, this, groupId);
                    } else {
                        // Định dạng sai, gửi cho người nhận
                        ChatServer.sendMessageToRecipient(clientName + ": " + content, this, recipientOrGroupIdStr);
                    }
                } else {
                    // Nếu tin nhắn không đúng định dạng, in ra lỗi
                    System.err.println("Định dạng tin nhắn không hợp lệ từ client " + clientName + ": " + message);
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi kết nối với client " + clientName + ": " + e.getMessage());
        } finally {
            // Khi client ngắt kết nối, thực hiện dọn dẹp
            ChatServer.removeClient(this);
            try {
                close();
            } catch (IOException e) {
                System.err.println("Lỗi khi đóng kết nối cho client " + clientName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gửi tin nhắn đến client này.
     *
     * @param message Tin nhắn cần gửi
     * @return `true` nếu gửi thành công, `false` nếu có lỗi
     */
    public boolean sendMessage(String message) {
        try {
            out.println(message);  // Gửi tin nhắn qua luồng ra
            return true;
        } catch (Exception e) {
            System.err.println("Gửi tin nhắn thất bại cho client " + clientName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Đóng socket và các tài nguyên liên quan khi client ngắt kết nối.
     *
     * @throws IOException nếu có lỗi khi đóng tài nguyên
     */
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();  // Đóng socket
        }
        if (in != null) {
            in.close();  // Đóng luồng vào
        }
        if (out != null) {
            out.close();  // Đóng luồng ra
        }
    }

    /**
     * Lấy tên của client (username).
     *
     * @return Tên client
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Thêm client vào một nhóm.
     *
     * @param groupId ID nhóm cần thêm client vào
     */
    public void addToGroup(int groupId) {
        groups.add(groupId);  // Thêm nhóm vào danh sách nhóm mà client tham gia
    }

    /**
     * Kiểm tra xem client có tham gia nhóm này hay không.
     *
     * @param groupId ID nhóm cần kiểm tra
     * @return `true` nếu client tham gia nhóm, `false` nếu không
     */

    public boolean isInGroup(String groupId) {
        return groups.contains(groupId);  // Kiểm tra nếu nhóm đã có trong danh sách
    }
}