package presentation;

import dto.GroupChatMessage;
import dto.ChatMessage;
import dto.User;
import dto.FriendRequest;
import dto.Group;
import java.util.Set;
import java.util.HashSet;
import bus.UserBUS;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;

public class ChatApp implements Runnable{
    private static final int CARD_HEIGHT = 60;
    private static User loggedInUser;
    private static JPanel chatPanel; // Global chat panel for the active chat screen

    //socket
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static String serverAddress = "localhost"; // Server IP (change if needed)
    private static int serverPort = 6000; // Server Port (match the ChatServer)

    public static void main(User user) {

        loggedInUser = user;

        connectToServer();
        // Create the main frame
        JFrame frame = new JFrame("Chat App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // Sidebar
        JPanel sidebar = createSidebar(frame);

        // Initial content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);

        ImageIcon imageIcon = new ImageIcon(ChatApp.class.getResource("/images/speech-bubble-2.png"));
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setPreferredSize(new Dimension(400, 550));
        contentPanel.add(imageLabel, BorderLayout.CENTER);

        // Add sidebar and content to the frame
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (loggedInUser != null) {
                    UserBUS userBUS = new UserBUS();
                    userBUS.setUserOffline(loggedInUser.getUsername());
                }
                System.out.println("Application is closing. User set to offline.");
            }
        });


        frame.setVisible(true);
    }
    private static void connectToServer() {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Notify the server of the connected user
            out.println(loggedInUser.getUsername());
            System.out.println("Connected to the chat server as: " + loggedInUser.getUsername());

            // Listener thread for real-time messages
            new Thread(() -> {
                try {
                    String message;
                    while (true) {
                        // Check for new messages every 1 second
                        if (in.ready()) {  // If there is a message to read
                            message = in.readLine();
                            System.out.println("Incoming message: " + message); // Debugging
                            handleIncomingMessage(message);
                        }
                        // Sleep for 1 second before checking for new messages again
                        Thread.sleep(1000);
                    }
                } catch (IOException e) {
                    System.err.println("Disconnected from server: " + e.getMessage());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to the server!", "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private static void handleIncomingMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (!message.contains(":")) {
                // Handle server announcements (messages without the | delimiter)
                JOptionPane.showMessageDialog(null, message, "Server Message", JOptionPane.INFORMATION_MESSAGE);
                System.out.println("Server message received: " + message);
                return;
            }

            // Split the message into parts based on the | delimiter

            String[] parts = message.split(":");
            if (parts.length == 2) {
                // The message is expected to have 3 parts: sender|recipient|content
                String sender = parts[0].trim(); // Recipient's username
                String content = parts[1].trim();   // The message content
                String recipient = getCurrentUser().getUsername();
                System.out.println("sender: " + sender);
                System.out.println("content: " + content);
                System.out.println("recipient: " + recipient);
                // Check if the message is for the logged-in user (recipient)
                System.out.println("user: " + loggedInUser.getUsername());
                if (recipient.equals(loggedInUser.getUsername())) {
                    System.out.println("User has logged in!");

                        // Display the message on the open chat screen
                        displayMessage(chatPanel, content, true, sender);
                        System.out.println("Message displayed for recipient: " + recipient);

                }
            } else {
                // Handle invalid message format
                System.err.println("Invalid message format: " + message);
            }
        });
    }

    private static Map<String, Boolean> openChatScreens = new HashMap<>();

    // Check if a chat screen with a specific user is open
    private static boolean isChatScreenOpen(String sender) {
        return openChatScreens.getOrDefault(sender, false);
    }

    private static void setChatScreenOpen(String username, boolean isOpen) {
        openChatScreens.put(username, isOpen);
    }

    private static void sendMessage(String recipient, String message) {
        if (socket == null || socket.isClosed()) {
            JOptionPane.showMessageDialog(null, "Not connected to the server!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Xử lý để loại bỏ ký tự đặc biệt như "|"
        String sanitizedMessage = message.replace("|", "");
        String formattedMessage = loggedInUser.getUsername() + "|" + recipient + "|" + sanitizedMessage;

        // Lưu tin nhắn vào cơ sở dữ liệu
        UserBUS userBUS = new UserBUS();
        boolean isSaved = userBUS.sendMessage(loggedInUser.getUsername(), recipient, sanitizedMessage);
        if (!isSaved) {
            JOptionPane.showMessageDialog(null, "Failed to save message to the database!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        // Gửi tin nhắn tới server
        out.println(formattedMessage);
        System.out.println("Message sent: " + formattedMessage);
    }

    private static User getCurrentUser() {
        return loggedInUser;
    }

    private static JPanel createSidebar(JFrame frame) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 30, 30)); // Dark background color

        JLabel titleLabel = new JLabel(loggedInUser.getUsername());
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton chatHistoryButton = createSidebarButton("Chat");
        JButton editProfileButton = createSidebarButton("Edit Profile");
        JButton searchButton = createSidebarButton("Search");
        JButton friendsButton = createSidebarButton("Friends");
        JButton friendRequestButton = createSidebarButton("Friend Request");
        JButton createGroupChatButton = createSidebarButton("Create Group Chat");

        // Add action listener to Friends button
        editProfileButton.addActionListener(e -> openEditProfileScreen(frame));
        searchButton.addActionListener(e -> openSearchScreen(frame));
        friendsButton.addActionListener(e -> openFriendListScreen(frame, getCurrentUser()));
        friendRequestButton.addActionListener(e -> openFriendRequestScreen(frame));
        chatHistoryButton.addActionListener(e -> openChattingScreen(frame));
        createGroupChatButton.addActionListener(e -> openCreateGroupChatScreen(frame));

        sidebar.add(titleLabel);
        sidebar.add(chatHistoryButton);
        sidebar.add(editProfileButton);
        sidebar.add(searchButton);
        sidebar.add(friendsButton);
        sidebar.add(friendRequestButton);
        sidebar.add(createGroupChatButton);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private static JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setBackground(new Color(50, 50, 50)); // Slightly lighter color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return button;
    }

    private static void openSearchScreen(JFrame frame) {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Add title label
        JLabel titleLabel = new JLabel("Search");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        contentPanel.add(titleLabel, BorderLayout.NORTH);

        // Search bar panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel filterLabel = new JLabel("Filter:");
        JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"People", "Message"});
        filterComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(resultsPanel);

        // Search button action listener
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            String selectedFilter = (String) filterComboBox.getSelectedItem();

            if (!searchText.isEmpty()) {
                if ("People".equals(selectedFilter)) {
                    searchPeople(resultsPanel, searchText, getCurrentUser().getUsername());
                } else { // "Message"
                    searchAllMessages(resultsPanel, searchText, getCurrentUser().getUsername());
                }
            } else {
                resultsPanel.removeAll();
                resultsPanel.revalidate();
                resultsPanel.repaint();
            }
        });

        // Add action listener to filterComboBox
        filterComboBox.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            String selectedFilter = (String) filterComboBox.getSelectedItem();

            // Only perform search if there is text in the search field
            if (!searchText.isEmpty()) {
                if ("People".equals(selectedFilter)) {
                    searchPeople(resultsPanel, searchText, getCurrentUser().getUsername());
                } else { // "Message"
                    searchAllMessages(resultsPanel, searchText, getCurrentUser().getUsername());
                }
            } else {
                // Clear the results panel if the search field is empty
                resultsPanel.removeAll();
                resultsPanel.revalidate();
                resultsPanel.repaint();
            }
        });

        // Combine the search bar and filter bar into a fixed top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Add components to the friend list panel below the title
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(topPanel, BorderLayout.NORTH); // Fixed header (search + filter)
        contentWrapper.add(scrollPane, BorderLayout.CENTER); // Scrollable friend list
        contentPanel.add(contentWrapper, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void searchPeople(JPanel resultsPanel, String searchText, String currentUsername) {
        // Clear the current results
        resultsPanel.removeAll();

        // Search for users
        UserBUS userBUS = new UserBUS();
        List<User> searchResults = userBUS.searchUsersByUsername(currentUsername, searchText);

        if (searchResults != null && !searchResults.isEmpty()) {
            for (User resultUser : searchResults) {
                if (currentUsername != resultUser.getUsername()) {
                    boolean isFriend = userBUS.isFriend(currentUsername, resultUser.getUsername());
                    JPanel resultCard = createResultCard(resultUser.getUsername(), isFriend);
                    resultsPanel.add(resultCard);
                }
            }
        } else {
            JLabel noResultsLabel = new JLabel("No users found matching the criteria.");
            noResultsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            resultsPanel.add(noResultsLabel);
        }

        // Update the UI
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private static void searchAllMessages(JPanel resultsPanel, String searchText, String currentUsername) {
        resultsPanel.removeAll();

        UserBUS userBUS = new UserBUS();
        List<ChatMessage> searchResults = userBUS.searchAllMessages(searchText, currentUsername);

        if (searchResults.isEmpty()) {
            resultsPanel.add(new JLabel("No messages found."));
        } else {
            for (ChatMessage message : searchResults) {
                // Display only messages where the current user is the sender or receiver
                if (message.getSender().equals(currentUsername) || message.getReceiver().equals(currentUsername)) {
                    // Get the current timestamp in the desired format
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String timestamp = dateFormat.format(message.getTimestamp());

                    JPanel messageCard = createClickableMessageCard(message.getSender(), message.getReceiver(), message.getMessage(), timestamp, message.getTimestamp());
                    resultsPanel.add(messageCard);
                }
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private static void openEditProfileScreen(JFrame frame) {
        // Create the edit profile panel
        JPanel editProfilePanel = new JPanel();
        editProfilePanel.setLayout(new BorderLayout());
        editProfilePanel.setBackground(Color.WHITE);

        // Title label
        JLabel titleLabel = new JLabel("Edit Profile", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout()); // Flexible layout
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Get the current user's information
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(frame, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Full Name
        gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField fullNameField = new JTextField(currentUser.getFullName(), 20); // Pre-fill with current value
        formPanel.add(fullNameField, gbc);

        // Username (Make it non-editable)
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        JTextField usernameField = new JTextField(currentUser.getUsername(), 20);
        usernameField.setEditable(false); // Disable editing
        formPanel.add(usernameField, gbc);

        // Date of Birth
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        JTextField dobField = new JTextField(20);
        if (currentUser.getDateOfBirth() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dobField.setText(dateFormat.format(currentUser.getDateOfBirth()));
        }
        formPanel.add(dobField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField emailField = new JTextField(currentUser.getEmail(), 20);
        formPanel.add(emailField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        JTextField addressField = new JTextField(currentUser.getAddress(), 20); // Pre-fill
        formPanel.add(addressField, gbc);

        // Gender
        gbc.gridx = 0;
        gbc.gridy++;
        formPanel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        String[] genders = {"Male", "Female"};
        JComboBox<String> genderComboBox = new JComboBox<>(genders);
        genderComboBox.setSelectedItem(currentUser.getGender()); // Pre-select
        formPanel.add(genderComboBox, gbc);

        // Save Button
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton saveButton = new JButton("Save");
        formPanel.add(saveButton, gbc);

        // Add action listener to the Save button
        saveButton.addActionListener(e -> {
            // Get updated values from the fields
            String updatedFullName = fullNameField.getText().trim();
            String updatedDOBString = dobField.getText().trim();
            String updatedEmail = emailField.getText().trim();
            String updatedAddress = addressField.getText().trim();
            String updatedGender = (String) genderComboBox.getSelectedItem();

            // Validate Date of Birth (optional, but recommended)
            Date updatedDOB = null;
            if (!updatedDOBString.isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    updatedDOB = dateFormat.parse(updatedDOBString);
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid date format. Please use yyyy-MM-dd.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Update the currentUser object
            currentUser.setFullName(updatedFullName);
            currentUser.setDateOfBirth(updatedDOB);
            currentUser.setEmail(updatedEmail);
            currentUser.setAddress(updatedAddress);
            currentUser.setGender(updatedGender);

            // Call UserBUS to update the user in the database
            UserBUS userBUS = new UserBUS();
            boolean success = userBUS.updateUser(currentUser); // Implement this in UserBUS

            if (success) {
                JOptionPane.showMessageDialog(frame, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Error updating profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add components to the main panel
        editProfilePanel.add(titleLabel, BorderLayout.NORTH);
        editProfilePanel.add(formPanel, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(editProfilePanel, BorderLayout.CENTER);

        frame.revalidate();
        frame.repaint();
    }

    private static void openFriendListScreen(JFrame frame, User currentUser) {
        // Create the content panel for the friend list
        JPanel friendListPanel = new JPanel();
        friendListPanel.setLayout(new BorderLayout());
        friendListPanel.setBackground(Color.WHITE);

        // Add title label
        JLabel titleLabel = new JLabel("Friend List");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        friendListPanel.add(titleLabel, BorderLayout.NORTH);

        // Search bar panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JLabel filterLabel = new JLabel("Filter:");
        JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"All", "Online"});
        filterComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        filterPanel.add(filterLabel);
        filterPanel.add(filterComboBox);

        // Friend list panel
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));

        // Fetch and display the friend list for the current user
        UserBUS userBUS = new UserBUS(); // Create an instance of UserBUS
        List<User> friends = userBUS.getFriendList(currentUser.getUsername()); // Fetch the friend list
        updateFriendListPanel(friendsPanel, friends);

        // Add action listener to the filter combo box
        filterComboBox.addActionListener(e -> {
            String selectedFilter = (String) filterComboBox.getSelectedItem();
            List<User> filteredFriends;

            if ("Online".equals(selectedFilter)) {
                // Filter for online friends only
                filteredFriends = userBUS.getOnlineFriends(currentUser.getUsername());
            } else {
                // "All" is selected, show all friends
                filteredFriends = userBUS.getFriendList(currentUser.getUsername());
            }

            // Update the friend list panel
            updateFriendListPanel(friendsPanel, filteredFriends);
        });

        // Wrap the friends panel in a scrollable view
        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Combine the search bar and filter bar into a fixed top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Add components to the friend list panel below the title
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(topPanel, BorderLayout.NORTH); // Fixed header (search + filter)
        contentWrapper.add(scrollPane, BorderLayout.CENTER); // Scrollable friend list
        friendListPanel.add(contentWrapper, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(friendListPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();

        // Add action listener to the search button
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase(); // Get search text

            // Clear the current friend list
            friendsPanel.removeAll();

            if (friends != null && !friends.isEmpty()) {
                for (User friend : friends) {
                    // Check if the username matches the search text (case-insensitive)
                    if (friend.getUsername().toLowerCase().contains(searchText)) {
                        friendsPanel.add(createFriendCard(friend.getUsername()));
                    }
                }
            } else {
                JLabel noFriendsLabel = new JLabel("No friends found.");
                noFriendsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                friendsPanel.add(noFriendsLabel);
            }

            // Update the UI
            friendsPanel.revalidate();
            friendsPanel.repaint();
        });
    }

    private static void updateFriendListPanel(JPanel friendsPanel, List<User> friends) {
        friendsPanel.removeAll(); // Clear existing friends

        if (friends != null && !friends.isEmpty()) {
            Collections.sort(friends, Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));

            for (User friend : friends) {
                friendsPanel.add(createFriendCard(friend.getUsername()));
            }
        } else {
            JLabel noFriendsLabel = new JLabel("No friends found.");
            noFriendsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            friendsPanel.add(noFriendsLabel);
        }

        friendsPanel.revalidate();
        friendsPanel.repaint();
    }

    private static void openFriendRequestScreen(JFrame frame) {
        // List to store friend requests
        ArrayList<String> friendRequests = new ArrayList<>();

        // Create the friend request panel
        JPanel friendRequestPanel = new JPanel();
        friendRequestPanel.setLayout(new BorderLayout());
        friendRequestPanel.setBackground(Color.WHITE);

        // Title label
        JLabel titleLabel = new JLabel("Friend Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        friendRequestPanel.add(titleLabel, BorderLayout.NORTH);

        // Friend request cards panel
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));

        // Fetch friend requests
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            UserBUS userBUS = new UserBUS();
            List<FriendRequest> requests = userBUS.getFriendRequests(currentUser.getUsername()); // Implement this method

            // Add friend request cards
            if (requests != null && !requests.isEmpty()) {
                for (FriendRequest request : requests) {
                    JPanel card = createFriendRequestCard(request.getSenderUsername(), cardsPanel, request.getRequestId()); // Modify this method
                    cardsPanel.add(card);
                    cardsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            } else {
                JLabel noRequestsLabel = new JLabel("No pending friend requests.");
                noRequestsLabel.setHorizontalAlignment(SwingConstants.CENTER);
                cardsPanel.add(noRequestsLabel);
            }
        }

        // Wrap the cards in a scroll pane
        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        friendRequestPanel.add(scrollPane, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(friendRequestPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void openChatScreen(JFrame frame, User currentUser, String otherUsername, Timestamp targetTimestamp) {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Panel for the title label and report button
        JPanel topPanel = new JPanel(new BorderLayout());

        // Title label displaying the other user's username
        JLabel titleLabel = new JLabel(otherUsername);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0)); // Add some padding
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Search bar panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JTextField searchField = new JTextField();
        JButton searchButton = new JButton("Search");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        topPanel.add(searchPanel, BorderLayout.SOUTH); // Add search panel to the top

        // Add the top panel to the content panel
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Panel for displaying messages
        chatPanel = new JPanel(); // Initialize global chat panel
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel for message input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        contentPanel.add(inputPanel, BorderLayout.SOUTH);

        // Load and display previous messages
        UserBUS userBUS = new UserBUS();
        List<ChatMessage> previousMessages = userBUS.getChatMessages(currentUser.getUsername(), otherUsername);
        for (ChatMessage message : previousMessages) {

            JPanel messageCard = createMessageCard(message.getSender(), message.getMessage(), message.getTimestamp());
            chatPanel.add(messageCard);
        }

        // Handle auto-scroll if a target timestamp is provided
        if(targetTimestamp!=null){
            // Get the current timestamp in the desired format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            String timestamp = dateFormat.format(targetTimestamp);
            SwingUtilities.invokeLater(() -> {
                for (Component comp : chatPanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel messagePanel = (JPanel) comp;
                        if (messagePanel.getComponentCount() > 0 && messagePanel.getComponent(1) instanceof JLabel) {
                            JLabel timestampLabel = (JLabel) messagePanel.getComponent(1);
                            if (timestampLabel.getText().equals(timestamp)) {
                                Rectangle viewRect = messagePanel.getBounds();
                                scrollPane.getViewport().scrollRectToVisible(viewRect);
                                break;
                            }
                        }
                    }
                }
            });
        }

        // Action listener for the Send button
        sendButton.addActionListener(e -> {
            String messageText = messageField.getText().trim();
            if (!messageText.isEmpty()) {
                sendMessage(otherUsername, messageText); // Use the real-time send method
                displayMessage(chatPanel, messageText, false, otherUsername); // Display locally
                messageField.setText(""); // Clear input
            }
        });

        // Action listener for the Search button
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                searchMessages(chatPanel, searchText, currentUser, otherUsername);
            } else {
                // If search text is empty, redisplay all messages
                chatPanel.removeAll();
                chatPanel.revalidate();
                chatPanel.repaint();
            }
        });

        // Menu button
        JButton menuButton = new JButton("☰"); // Using a "hamburger" icon
        menuButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topPanel.add(menuButton, BorderLayout.EAST);

        // Create the popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Spam Report menu item
        JMenuItem spamReportMenuItem = new JMenuItem("Spam Report");
        spamReportMenuItem.setForeground(Color.RED);
        spamReportMenuItem.addActionListener(e -> {
            // Handle spam report logic
            String reporter = currentUser.getUsername();
            String reported = otherUsername;

            // Prompt for the message to report (or let them choose from recent messages)
            String messageToReport = JOptionPane.showInputDialog(frame, "Enter the message to report (or leave empty to report the user):");

            boolean success = userBUS.createSpamReport(reporter, reported, messageToReport);

            if (success) {
                JOptionPane.showMessageDialog(frame, "User " + otherUsername + " reported for spam.");
            } else {
                JOptionPane.showMessageDialog(frame, "Error reporting user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Spam Report menu item
        JMenuItem removeMessageMenuItem = new JMenuItem("Remove Message");
        removeMessageMenuItem.setForeground(Color.RED);
        removeMessageMenuItem.addActionListener(e -> openRemoveMessageScreen(frame, currentUser, otherUsername));

        // Clear Chat History menu item
        JMenuItem clearChatMenuItem = new JMenuItem("Clear Chat History");
        clearChatMenuItem.setForeground(Color.RED);
        clearChatMenuItem.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(frame,
                    "Are you sure you want to clear the chat history with " + otherUsername + "? This action cannot be undone.",
                    "Confirm Clear Chat History",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                boolean success = userBUS.clearChatHistory(currentUser.getUsername(), otherUsername);

                if (success) {
                    JOptionPane.showMessageDialog(frame, "Chat history cleared successfully.");
                    // Refresh the chat panel to show the empty chat
                    chatPanel.removeAll();
                    chatPanel.revalidate();
                    chatPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(frame, "Error clearing chat history.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        // Add menu items to the popup menu
        popupMenu.add(spamReportMenuItem);
        popupMenu.add(removeMessageMenuItem);
        popupMenu.add(clearChatMenuItem);

        // Add action listener to the menu button
        menuButton.addActionListener(e -> {
            popupMenu.show(menuButton, 0, menuButton.getHeight());
        });

        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void openChatScreen(JFrame frame, User currentUser, String otherUsername) {
        openChatScreen(frame, currentUser, otherUsername, null);
    }
    private static void openRemoveMessageScreen(JFrame frame, User currentUser, String otherUsername) {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Panel for the title label
        JPanel topPanel = new JPanel(new BorderLayout());

        // Title label
        JLabel titleLabel = new JLabel("Remove Message");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Add the top panel to the content panel
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Panel for displaying messages
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Load and display previous messages
        UserBUS userBUS = new UserBUS();
        List<ChatMessage> previousMessages = userBUS.getChatMessages(currentUser.getUsername(), otherUsername);
        for (ChatMessage message : previousMessages) {
            // Use the createRemoveMessageCard function from the previous response
            JPanel messageCard = createRemoveMessageCard(message, message.getTimestamp(), chatPanel);
            chatPanel.add(messageCard);
        }

        // Update the frame
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel createRemoveMessageCard(ChatMessage message, Timestamp timestamp, JPanel chatPanel) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(230, 230, 230));
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(message.getSender());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Use the Timestamp directly, format when displaying if needed.
        JLabel timestampLabel = new JLabel(timestamp.toString());
        timestampLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        timestampLabel.setForeground(Color.GRAY);

        JLabel messageLabel = new JLabel(message.getMessage());
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(usernameLabel, BorderLayout.NORTH);
        textPanel.add(messageLabel, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(timestampLabel, BorderLayout.EAST); // Place timestamp on the WEST side

        JButton removeButton = new JButton("Remove");
        removeButton.addActionListener(e -> {
            UserBUS userBUS = new UserBUS();
            boolean removed = userBUS.removeMessage(message.getSender(), message.getReceiver(), timestamp);
            if (removed) {
                chatPanel.remove(card); // Remove the card from the chat panel
                chatPanel.revalidate();
                chatPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(chatPanel, "Failed to remove message.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Use a panel to hold the remove button and add spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Add horizontal gap
        buttonPanel.add(removeButton);
        card.add(buttonPanel, BorderLayout.WEST);
        return card;
    }

    private static void searchMessages(JPanel resultsPanel, String searchText, User currentUser, String otherUsername) {
        resultsPanel.removeAll();

        UserBUS userBUS = new UserBUS();
        List<ChatMessage> searchResults = userBUS.searchMessages(searchText, currentUser.getUsername(), otherUsername);

        if (searchResults.isEmpty()) {
            resultsPanel.add(new JLabel("No messages found."));
        } else {
            for (ChatMessage message : searchResults) {
                if ((message.getSender().equals(currentUser.getUsername()) && message.getReceiver().equals(otherUsername)) || (message.getSender().equals(otherUsername) && message.getReceiver().equals(currentUser.getUsername()))) {
                    // Get the current timestamp in the desired format
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                    String timestamp = dateFormat.format(message.getTimestamp());
                    JPanel messageCard = createClickableMessageCard(message.getSender(), message.getReceiver(), message.getMessage(), timestamp, message.getTimestamp());
                    resultsPanel.add(messageCard);
                }

            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // For when sending new message
    private static void displayMessage(JPanel chatPanel, String message, boolean isReceived, String otherUsername) {
        String currentUsername = getCurrentUser().getUsername();
        String senderUsername = isReceived ? otherUsername : currentUsername;

        // Get the current timestamp in the desired format
        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        JPanel messageCard = createMessageCard(senderUsername, message, timestamp); // Use createMessageCard

        chatPanel.add(messageCard);

        // Scroll to the bottom
        JScrollBar verticalScrollBar = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());

        chatPanel.revalidate();
        chatPanel.repaint();
    }

    // For search message, show previous message
    private static JPanel createMessageCard(String username, String message, Timestamp timestamp) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(230, 230, 230));
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));


        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String time = dateFormat.format(timestamp);
        JLabel timestampLabel = new JLabel(time);
        timestampLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        timestampLabel.setForeground(Color.GRAY);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(usernameLabel, BorderLayout.NORTH);
        textPanel.add(messageLabel, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(timestampLabel, BorderLayout.EAST);

        return card;
    }

    private static JPanel createChatCard(String otherUsername, JFrame frame) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(240, 240, 240));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(otherUsername);
        card.add(usernameLabel, BorderLayout.CENTER);

        JButton viewButton = new JButton("Chat");
        viewButton.addActionListener(e -> openChatScreen(frame, getCurrentUser(), otherUsername));
        card.add(viewButton, BorderLayout.EAST);

        return card;
    }

    private static JPanel createFriendRequestCard(String username, JPanel parentPanel, int requestID) {
        JPanel reqCard = new JPanel();
        // Use GridBagLayout for better control
        reqCard.setLayout(new GridBagLayout());
        reqCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        reqCard.setBackground(new Color(240, 240, 240));
        reqCard.setPreferredSize(new Dimension(reqCard.getPreferredSize().width, CARD_HEIGHT));
        reqCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();

        // Username label
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0; // Allow username to take available space
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        reqCard.add(usernameLabel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0)); // Add spacing between buttons
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton acceptButton = new JButton("Accept");
        JButton declineButton = new JButton("Decline");
        buttonPanel.add(acceptButton);
        buttonPanel.add(declineButton);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0; // Prevent buttons from expanding
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        reqCard.add(buttonPanel, gbc);

        // Action for Accept button
        acceptButton.addActionListener(e -> {
            UserBUS userBUS = new UserBUS();
            boolean success = userBUS.acceptFriendRequest(requestID);

            if (success) {
                JOptionPane.showMessageDialog(parentPanel, "Friend request from " + username + " accepted!");
                parentPanel.remove(reqCard);
                parentPanel.revalidate();
                parentPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(parentPanel, "Error accepting friend request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action for Decline button
        declineButton.addActionListener(e -> {
            UserBUS userBUS = new UserBUS();
            boolean success = userBUS.declineFriendRequest(requestID);

            if (success) {
                JOptionPane.showMessageDialog(parentPanel, "Friend request from " + username + " declined.");
                parentPanel.remove(reqCard);
                parentPanel.revalidate();
                parentPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(parentPanel, "Error declining friend request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        return reqCard;
    }

    private static JPanel createResultCard(String username, boolean isFriend) {
        JPanel resCard = new JPanel();
        resCard.setLayout(new BorderLayout());
        resCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        resCard.setBackground(new Color(240, 240, 240));
        resCard.setPreferredSize(new Dimension(resCard.getPreferredSize().width, CARD_HEIGHT));
        resCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        // Username label on the left
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        resCard.add(usernameLabel, BorderLayout.WEST);

        // Buttons on the right
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 240, 240));

        // Common buttons: Chat
        JButton chatButton = new JButton("Chat");
        buttonPanel.add(chatButton);
        chatButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(resCard);
            // Open chat screen with the selected user
            User currentUser = getCurrentUser();
            openChatScreen(frame, currentUser, username);
        });

        // Conditional buttons based on friendship status
        if (isFriend) {
            JButton unfriendButton = new JButton("Unfriend");
            buttonPanel.add(unfriendButton);
            // Add action listener for the Unfriend button
            unfriendButton.addActionListener(e -> {
                // Get the currently logged-in user
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(null, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Confirmation dialog before unfriending
                int confirm = JOptionPane.showConfirmDialog(resCard,
                        "Are you sure you want to unfriend " + username + "?",
                        "Confirm Unfriend", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    UserBUS userBUS = new UserBUS();
                    boolean success = userBUS.unfriend(currentUser.getUsername(), username); // Call UserBUS to unfriend

                    if (success) {
                        JOptionPane.showMessageDialog(resCard, "Unfriended " + username + " successfully!");

                        // Remove the friend card from the UI
                        Container parent = resCard.getParent(); // Get the parent container (friendsPanel)
                        parent.remove(resCard);
                        parent.revalidate();
                        parent.repaint();
                    } else {
                        JOptionPane.showMessageDialog(resCard, "Error unfriending " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
        } else {
            JButton addFriendButton = new JButton("Add Friend");
            buttonPanel.add(addFriendButton);
            // Add action listener for Add Friend button
            // Add action listener for Add Friend button
            addFriendButton.addActionListener(e -> {
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(null, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Add friend request logic
                UserBUS userBUS = new UserBUS();
                boolean success = userBUS.sendFriendRequest(currentUser.getUsername(), username);

                if (success) {
                    JOptionPane.showMessageDialog(resCard, "Friend request sent to " + username + "!");
                    addFriendButton.setEnabled(false); // Optionally disable the button after sending a request
                } else {
                    JOptionPane.showMessageDialog(resCard, "Error sending friend request to " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        JButton blockButton = new JButton("Block");
        buttonPanel.add(blockButton);

        // Block button action listener
        blockButton.addActionListener(e -> {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (isFriend) {
                // Confirmation dialog
                int confirm = JOptionPane.showConfirmDialog(resCard,
                        "Are you sure you want to block " + username + "? This will also unfriend them.",
                        "Confirm Block", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    UserBUS userBUS = new UserBUS();
                    boolean success = userBUS.blockUser(currentUser.getUsername(), username); // Call UserBUS to block

                    if (success) {
                        JOptionPane.showMessageDialog(resCard, "Blocked " + username + " successfully!");

                        // Remove the friend card from the UI
                        Container parent = resCard.getParent();
                        parent.remove(resCard);
                        parent.revalidate();
                        parent.repaint();
                    } else {
                        JOptionPane.showMessageDialog(resCard, "Error blocking " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                // Confirmation dialog
                int confirm = JOptionPane.showConfirmDialog(resCard,
                        "Are you sure you want to block " + username + "?",
                        "Confirm Block", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    UserBUS userBUS = new UserBUS();
                    boolean success = userBUS.blockUserWithoutUnfriend(currentUser.getUsername(), username); // Call UserBUS to block

                    if (success) {
                        JOptionPane.showMessageDialog(resCard, "Blocked " + username + " successfully!");

                        // Remove the friend card from the UI
                        Container parent = resCard.getParent();
                        parent.remove(resCard);
                        parent.revalidate();
                        parent.repaint();
                    } else {
                        JOptionPane.showMessageDialog(resCard, "Error blocking " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

        });

        resCard.add(buttonPanel, BorderLayout.EAST);
        return resCard;
    }

    private static JPanel createFriendCard(String username) {
        JPanel friendCard = new JPanel();
        friendCard.setLayout(new BorderLayout());
        friendCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        friendCard.setBackground(new Color(240, 240, 240));
        friendCard.setPreferredSize(new Dimension(friendCard.getPreferredSize().width, CARD_HEIGHT));
        friendCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        // Username label on the left
        JLabel usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        friendCard.add(usernameLabel, BorderLayout.WEST);

        // Buttons on the right
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 240, 240)); // Match the card background

        JButton chatButton = new JButton("Chat");
        JButton unfriendButton = new JButton("Unfriend");
        JButton blockButton = new JButton("Block"); // Add Block button
        buttonPanel.add(chatButton);
        buttonPanel.add(unfriendButton);
        buttonPanel.add(blockButton); // Add Block button to panel

        friendCard.add(buttonPanel, BorderLayout.EAST);
        chatButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(friendCard);
            // Open chat screen with the selected user
            User currentUser = getCurrentUser();
            openChatScreen(frame, currentUser, username);
        });
        unfriendButton.addActionListener(e -> {
            // Get the currently logged-in user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmation dialog before unfriending
            int confirm = JOptionPane.showConfirmDialog(friendCard,
                    "Are you sure you want to unfriend " + username + "?",
                    "Confirm Unfriend", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                UserBUS userBUS = new UserBUS();
                boolean success = userBUS.unfriend(currentUser.getUsername(), username); // Call UserBUS to unfriend

                if (success) {
                    JOptionPane.showMessageDialog(friendCard, "Unfriended " + username + " successfully!");

                    // Remove the friend card from the UI
                    Container parent = friendCard.getParent(); // Get the parent container (friendsPanel)
                    parent.remove(friendCard);
                    parent.revalidate();
                    parent.repaint();
                } else {
                    JOptionPane.showMessageDialog(friendCard, "Error unfriending " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        blockButton.addActionListener(e -> {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                JOptionPane.showMessageDialog(null, "Error: User not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Confirmation dialog
            int confirm = JOptionPane.showConfirmDialog(friendCard,
                    "Are you sure you want to block " + username + "? This will also unfriend them.",
                    "Confirm Block", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                UserBUS userBUS = new UserBUS();
                boolean success = userBUS.blockUser(currentUser.getUsername(), username); // Call UserBUS to block

                if (success) {
                    JOptionPane.showMessageDialog(friendCard, "Blocked " + username + " successfully!");

                    // Remove the friend card from the UI
                    Container parent = friendCard.getParent();
                    parent.remove(friendCard);
                    parent.revalidate();
                    parent.repaint();
                } else {
                    JOptionPane.showMessageDialog(friendCard, "Error blocking " + username + ".", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });


        return friendCard;
    }

    private static void openChatListScreen(JFrame frame) {
        JPanel chatListPanel = new JPanel(new BorderLayout());
        chatListPanel.setBackground(Color.WHITE);

        // Fetch the list of users with whom the current user has chatted
        UserBUS userBUS = new UserBUS();
        List<String> chattedUsers = userBUS.getChattedUsers(loggedInUser.getUsername());

        if (chattedUsers.isEmpty()) {
            JLabel noChatsLabel = new JLabel("No chats available.");
            noChatsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            chatListPanel.add(noChatsLabel, BorderLayout.CENTER);
        } else {
            JPanel chatsListPanel = new JPanel();
            chatsListPanel.setLayout(new BoxLayout(chatsListPanel, BoxLayout.Y_AXIS));

            for (String otherUsername : chattedUsers) {
                JPanel chatCard = createChatCard(otherUsername, frame);
                chatsListPanel.add(chatCard);
            }

            JScrollPane scrollPane = new JScrollPane(chatsListPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            chatListPanel.add(scrollPane, BorderLayout.CENTER);
        }

        // Wrap the content panel in a scrollable view
        JScrollPane scrollPane = new JScrollPane(chatListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Replace the current content panel with the chat list panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(chatListPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void openCreateGroupChatScreen(JFrame frame) {
        // Create the content panel for the create group chat screen
        JPanel createGroupPanel = new JPanel();
        createGroupPanel.setLayout(new BorderLayout());
        createGroupPanel.setBackground(Color.WHITE);

        // Top bar - Group Name
        JPanel topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel groupNameLabel = new JLabel("Group Name:");
        JTextField groupNameField = new JTextField(20);
        topBarPanel.add(groupNameLabel);
        topBarPanel.add(groupNameField);

        // Content panel - Friend list
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Keep track of added friends
        Set<String> addedFriends = new HashSet<>();

        // Fetch and display the friend list for the current user
        UserBUS userBUS = new UserBUS();
        List<User> friends = userBUS.getFriendList(loggedInUser.getUsername());

        // Sort the friends alphabetically by username
        Collections.sort(friends, Comparator.comparing(User::getUsername));


        for (User friend : friends) {
            JPanel friendCard = createAddFriendCard(friend.getUsername(), friendsPanel, addedFriends);
            friendsPanel.add(friendCard);
        }

        // Bottom bar - Create button
        JPanel bottomBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createButton = new JButton("Create");
        createButton.addActionListener(e -> {
            String groupName = groupNameField.getText().trim();
            if (groupName.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please enter a group name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (addedFriends.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please add at least one friend to the group.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Add the creator to the group as an admin
            List<String> groupMembers = new ArrayList<>(addedFriends);

            // Create the group chat
            int groupId = userBUS.createGroupChat(groupName, loggedInUser.getUsername(), groupMembers);
            if (groupId > 0) {
                JOptionPane.showMessageDialog(frame, "Group chat '" + groupName + "' created successfully!");
                // Optionally, navigate to the newly created group chat or refresh the group chat list
                openGroupChatScreen(frame);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to create group chat.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        bottomBarPanel.add(createButton);

        // Add components to the create group panel
        createGroupPanel.add(topBarPanel, BorderLayout.NORTH);
        createGroupPanel.add(scrollPane, BorderLayout.CENTER);
        createGroupPanel.add(bottomBarPanel, BorderLayout.SOUTH);

        // Replace the current content panel with the create group panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(createGroupPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Use to add member (already friends with creator) in creat group chat
    private static JPanel createAddFriendCard(String username, JPanel friendsPanel, Set<String> addedFriends) {
        JPanel friendCard = new JPanel(new BorderLayout());
        friendCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        friendCard.setBackground(new Color(240, 240, 240));
        friendCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(username);
        friendCard.add(usernameLabel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            addedFriends.add(username);
            friendsPanel.remove(friendCard);
            friendsPanel.revalidate();
            friendsPanel.repaint();
        });
        friendCard.add(addButton, BorderLayout.EAST);

        return friendCard;
    }

    private static void openGroupChatScreen(JFrame frame) {
        JPanel groupChatPanel = new JPanel(new BorderLayout());
        groupChatPanel.setBackground(Color.WHITE);

        // Fetch and display the group chats for the current user
        UserBUS userBUS = new UserBUS();
        List<Group> groupChats = userBUS.getGroupChatsForUser(loggedInUser.getUsername());

        // Display group chats or a message if none exist
        if (groupChats.isEmpty()) {
            JLabel noGroupsLabel = new JLabel("No group chats available.");
            noGroupsLabel.setHorizontalAlignment(SwingConstants.CENTER);
            groupChatPanel.add(noGroupsLabel, BorderLayout.CENTER);
        } else {
            JPanel groupsListPanel = new JPanel();
            groupsListPanel.setLayout(new BoxLayout(groupsListPanel, BoxLayout.Y_AXIS));

            for (Group group : groupChats) {
                JPanel groupCard = createGroupChatCard(group, frame);
                groupsListPanel.add(groupCard);
            }

            JScrollPane scrollPane = new JScrollPane(groupsListPanel);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            groupChatPanel.add(scrollPane, BorderLayout.CENTER);
        }

        // Replace the current content panel with the group chat panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(groupChatPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // Method to create a card for each group chat
    private static JPanel createGroupChatCard(Group group, JFrame frame) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(240, 240, 240));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel groupNameLabel = new JLabel(group.getGroupName());
        card.add(groupNameLabel, BorderLayout.CENTER);

        JButton viewButton = new JButton("View");
        viewButton.addActionListener(e -> {
            // Implement action to view the selected group chat
            // This could involve opening a new panel for the group chat
            openGroupChatDetailScreen(frame, group);
        });
        card.add(viewButton, BorderLayout.EAST);

        return card;
    }

    private static void openGroupChatDetailScreen(JFrame frame, Group group) {
        // Create the content panel for the group chat detail screen
        JPanel groupChatDetailPanel = new JPanel();
        groupChatDetailPanel.setLayout(new BorderLayout());
        groupChatDetailPanel.setBackground(Color.WHITE);

        // Top bar - Group Name and options
        JPanel topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel groupNameLabel = new JLabel(group.getGroupName());
        groupNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topBarPanel.add(groupNameLabel);

        // Check if the current user is an admin
        UserBUS userBUS = new UserBUS();
        if (userBUS.isAdmin(group.getGroupId(), loggedInUser.getUsername())) {
            // Add member button
            JButton manageMemberButton = new JButton("Manage Member");
            manageMemberButton.addActionListener(e -> openManageMemberScreen(frame, group));
            topBarPanel.add(manageMemberButton);

            // Edit group name button
            JButton editGroupNameButton = new JButton("Edit Group Name");
            editGroupNameButton.addActionListener(e -> {
                String newGroupName = JOptionPane.showInputDialog(frame, "Enter new group name:", group.getGroupName());
                if (newGroupName != null && !newGroupName.trim().isEmpty()) {
                    boolean success = userBUS.updateGroupName(group.getGroupId(), newGroupName);
                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Group name updated successfully!");
                        groupNameLabel.setText(newGroupName); // Update the label
                        group.setGroupName(newGroupName); // Update the group object
                        openGroupChatScreen(frame);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to update group name.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            topBarPanel.add(editGroupNameButton);
        }

        groupChatDetailPanel.add(topBarPanel, BorderLayout.NORTH);

        // Panel for displaying messages
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        groupChatDetailPanel.add(scrollPane, BorderLayout.CENTER);

        List<GroupChatMessage> previousMessages = userBUS.getGroupChatMessages(group.getGroupId());
        for (GroupChatMessage message : previousMessages) {
            // Get the current timestamp in the desired format
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            String timestamp = dateFormat.format(message.getTimestamp());
            JPanel messageCard = createMessageCard(message.getSender(), message.getMessage(), message.getTimestamp());
            chatPanel.add(messageCard);
        }

        // Bottom bar - Message input
        JPanel bottomBarPanel = new JPanel(new BorderLayout());
        bottomBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JTextField messageField = new JTextField();
        JButton sendMessageButton = new JButton("Send");
        sendMessageButton.addActionListener(e -> {
            String messageText = messageField.getText().trim();
            if (!messageText.isEmpty()) {
                // Call UserBUS to send the message
                boolean sent = userBUS.sendGroupMessage(group.getGroupId(), loggedInUser.getUsername(), messageText);
                displayGroupMessage(chatPanel, messageText);
                if (sent) {
                    messageField.setText(""); // Clear the message field
                    // Optionally, refresh the chat messages
                } else {
                    JOptionPane.showMessageDialog(frame, "Failed to send message.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        bottomBarPanel.add(messageField, BorderLayout.CENTER);
        bottomBarPanel.add(sendMessageButton, BorderLayout.EAST);
        groupChatDetailPanel.add(bottomBarPanel, BorderLayout.SOUTH);

        // Replace the current content panel with the group chat detail panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(groupChatDetailPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    // For when sending new message
    private static void displayGroupMessage(JPanel chatPanel, String message) {
        String currentUsername = getCurrentUser().getUsername();

        // Get the current timestamp in the desired format
        java.util.Date date = new java.util.Date();
        Timestamp timestamp = new Timestamp(date.getTime());

        JPanel messageCard = createMessageCard(currentUsername, message, timestamp); // Use createMessageCard

        chatPanel.add(messageCard);

        // Scroll to the bottom
        JScrollBar verticalScrollBar = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());

        chatPanel.revalidate();
        chatPanel.repaint();
    }

    // promote admin, demote admin, remove member
    private static void openManageMemberScreen(JFrame frame, Group group) {
        // Create the content panel for managing members
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // Top bar - Group Name
        JPanel topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JLabel groupNameLabel = new JLabel("Manage members in " + group.getGroupName());
        groupNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        topBarPanel.add(groupNameLabel);

        contentPanel.add(topBarPanel, BorderLayout.NORTH);

        // Central panel - Member list
        JPanel membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        JScrollPane membersScrollPane = new JScrollPane(membersPanel);
        membersScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(membersScrollPane, BorderLayout.CENTER);

        // Fetch and display the member list
        UserBUS userBUS = new UserBUS();
        List<User> members = userBUS.getGroupMembers(group.getGroupId());
        for (User member : members) {
            JPanel memberCard = createMemberCard(frame, group, member, userBUS);
            membersPanel.add(memberCard);
        }

        // Check if the current user is an admin
        if (userBUS.isAdmin(group.getGroupId(), loggedInUser.getUsername())) {
            // Add member button
            JButton addMemberButton = new JButton("Add Member");
            addMemberButton.addActionListener(e -> openAddMemberScreen(frame, group));
            topBarPanel.add(addMemberButton);
        }

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel createMemberCard(JFrame frame, Group group, User member, UserBUS userBUS) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(240, 240, 240));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(member.getUsername());
        card.add(usernameLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        // Check if the logged-in user is an admin
        if (userBUS.isAdmin(group.getGroupId(), loggedInUser.getUsername())) {
            // Add conditional buttons for admins only
            if (!loggedInUser.getUsername().equals(member.getUsername())) { // Prevent admin from modifying their own status
                if (userBUS.isAdmin(group.getGroupId(), member.getUsername())) {
                    JButton demoteButton = new JButton("Demote");
                    demoteButton.addActionListener(e -> {
                        boolean success = userBUS.demoteAdmin(group.getGroupId(), loggedInUser.getUsername(), member.getUsername());
                        if (success) {
                            JOptionPane.showMessageDialog(frame, "Member demoted to regular member.");
                            openManageMemberScreen(frame, group); // Refresh the screen
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to demote member.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    buttonPanel.add(demoteButton);
                } else {
                    JButton promoteButton = new JButton("Promote");
                    promoteButton.addActionListener(e -> {
                        boolean success = userBUS.promoteMemberToAdmin(group.getGroupId(), loggedInUser.getUsername(), member.getUsername());
                        if (success) {
                            JOptionPane.showMessageDialog(frame, "Member promoted to admin.");
                            openManageMemberScreen(frame, group); // Refresh the screen
                        } else {
                            JOptionPane.showMessageDialog(frame, "Failed to promote member.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    buttonPanel.add(promoteButton);
                }

                JButton removeButton = new JButton("Remove");
                removeButton.addActionListener(e -> {
                    boolean success = userBUS.removeMemberFromGroup(group.getGroupId(), loggedInUser.getUsername(), member.getUsername());
                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Member removed from group.");
                        openManageMemberScreen(frame, group); // Refresh the screen
                    } else {
                        JOptionPane.showMessageDialog(frame, "Failed to remove member.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
                buttonPanel.add(removeButton);
            }
        }

        card.add(buttonPanel, BorderLayout.EAST);
        return card;
    }

    private static void openAddMemberScreen(JFrame frame, Group group) {
        // Create the content panel for adding a member
        JPanel addMemberPanel = new JPanel();
        addMemberPanel.setLayout(new BorderLayout());
        addMemberPanel.setBackground(Color.WHITE);

        // Top bar - Instructions
        JPanel topBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel instructionLabel = new JLabel("Select a friend to add:");
        topBarPanel.add(instructionLabel);
        addMemberPanel.add(topBarPanel, BorderLayout.NORTH);

        // Content panel - Friend list
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(friendsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        addMemberPanel.add(scrollPane, BorderLayout.CENTER);

        // Fetch and display the friend list for the current user
        UserBUS userBUS = new UserBUS();
        List<User> friends = userBUS.getFriendList(loggedInUser.getUsername());

        // Get the list of current group members
        List<User> currentMembers = userBUS.getGroupMembers(group.getGroupId());
        Set<String> currentMemberUsernames = new HashSet<>();
        for (User member : currentMembers) {
            currentMemberUsernames.add(member.getUsername());
        }

        // Sort the friends alphabetically by username
        Collections.sort(friends, Comparator.comparing(User::getUsername));

        for (User friend : friends) {
            // Only display the friend card if the friend is not already a member
            if (!currentMemberUsernames.contains(friend.getUsername())) {
                JPanel friendCard = createAddMemberFriendCard(friend.getUsername(), group, friendsPanel);
                friendsPanel.add(friendCard);
            }
        }

        // Replace the current content panel with the add member panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(addMemberPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel createAddMemberFriendCard(String friendUsername, Group group, JPanel friendsPanel) {
        JPanel friendCard = new JPanel(new BorderLayout());
        friendCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        friendCard.setBackground(new Color(240, 240, 240));
        friendCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(friendUsername);
        friendCard.add(usernameLabel, BorderLayout.CENTER);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> {
            UserBUS userBUS = new UserBUS();
            boolean added = userBUS.addMemberToGroup(group.getGroupId(), loggedInUser.getUsername(), friendUsername);
            if (added) {
                JOptionPane.showMessageDialog(friendsPanel, friendUsername + " added to the group successfully!");
                friendsPanel.remove(friendCard);
                friendsPanel.revalidate();
                friendsPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(friendsPanel, "Failed to add " + friendUsername + " to the group.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        friendCard.add(addButton, BorderLayout.EAST);

        return friendCard;
    }

    // chat in general ->menu(individual/group)
    private static void openChattingScreen(JFrame frame) {
        // Create the content panel for the friend list
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JPanel topBarPanel = new JPanel();
        topBarPanel.setLayout(new BoxLayout(topBarPanel, BoxLayout.X_AXIS));
        topBarPanel.setBackground(Color.WHITE);

        // Add title label
        JLabel titleLabel = new JLabel("Chatting");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        topBarPanel.add(titleLabel);

        // Menu button
        JButton menuButton = new JButton("Choose individual or group chat");
        menuButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        topBarPanel.add(menuButton);

        contentPanel.add(topBarPanel);

        // Create the popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // Spam Report menu item
        JMenuItem oneMenuItem = new JMenuItem("Individual");
        oneMenuItem.setForeground(Color.BLACK);
        oneMenuItem.addActionListener(e -> openChatListScreen(frame));

        // Clear Chat History menu item
        JMenuItem groupMenuItem = new JMenuItem("Group");
        groupMenuItem.setForeground(Color.BLACK);
        groupMenuItem.addActionListener(e -> openGroupChatScreen(frame));

        // Add menu items to the popup menu
        popupMenu.add(oneMenuItem);
        popupMenu.add(groupMenuItem);

        // Add action listener to the menu button
        menuButton.addActionListener(e -> {
            popupMenu.show(menuButton, 0, menuButton.getHeight());
        });

        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(contentPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel createClickableMessageCard(String senderUsername, String receiverUsername, String message, String timestamp, Timestamp messageTimestamp) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(230, 230, 230));
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        JLabel usernameLabel = new JLabel(senderUsername);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel timestampLabel = new JLabel(timestamp);
        timestampLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        timestampLabel.setForeground(Color.GRAY);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(usernameLabel, BorderLayout.NORTH);
        textPanel.add(messageLabel, BorderLayout.CENTER);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(timestampLabel, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(card);
                User currentUser = getCurrentUser();
                // Determine which username to use for the chat screen
                String otherUsername;
                if (senderUsername.equals(currentUser.getUsername())){
                    otherUsername = receiverUsername;
                }else {
                    otherUsername = senderUsername;
                }
                openChatScreen(frame, currentUser, otherUsername, messageTimestamp);
            }
        });

        // Add an underline on hover for visual feedback
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                usernameLabel.setText("<html><u>" + senderUsername + "</u></html>");
                messageLabel.setText("<html><u>" + message + "</u></html>");

                card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                usernameLabel.setText(senderUsername);
                messageLabel.setText(message);
                card.setCursor(Cursor.getDefaultCursor());
            }
        });

        return card;
    }

    @Override
    public void run() {

    }
}