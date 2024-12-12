package presentation;

import dto.User;
import dto.FriendRequest;
import bus.UserBUS;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class ChatApp {
    private static final int CARD_HEIGHT = 60;
    private static User loggedInUser;

    public static void main(User user) {
        loggedInUser = user;
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

        frame.setVisible(true);
    }

    private static User getCurrentUser() {
        return loggedInUser;
    }

    private static JPanel createSidebar(JFrame frame) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 30, 30)); // Dark background color

        JLabel titleLabel = new JLabel("ChatApp");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JButton editProfileButton = createSidebarButton("Edit Profile");
        JButton searchButton = createSidebarButton("Search");
        JButton friendsButton = createSidebarButton("Friends");
        JButton friendRequestButton = createSidebarButton("Friend Request");
        JButton chatHistoryButton = createSidebarButton("Chat History");
        JButton createGroupChatButton = createSidebarButton("Create Group Chat");

        // Add action listener to Friends button
        editProfileButton.addActionListener(e -> openEditProfileScreen(frame));
        searchButton.addActionListener(e -> openSearchScreen(frame));
        friendsButton.addActionListener(e -> openFriendListScreen(frame, getCurrentUser()));
        friendRequestButton.addActionListener(e -> openFriendRequestScreen(frame));
        chatHistoryButton.addActionListener(e -> openChatHistoryScreen(frame));

        sidebar.add(titleLabel);
        sidebar.add(editProfileButton);
        sidebar.add(searchButton);
        sidebar.add(friendsButton);
        sidebar.add(friendRequestButton);
        sidebar.add(chatHistoryButton);
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
        // Create the content panel for the search results
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultPanel.setBackground(Color.WHITE);

        // Add title label
        JLabel titleLabel = new JLabel("Search Results");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        resultPanel.add(titleLabel, BorderLayout.NORTH);

        // Search bar panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton searchButton = new JButton("Search");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // Filter panel (Not implemented yet, can be added later)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        // Result list panel
        JPanel resPanel = new JPanel();
        resPanel.setLayout(new BoxLayout(resPanel, BoxLayout.Y_AXIS));

        // Wrap the results panel in a scrollable view
        JScrollPane scrollPane = new JScrollPane(resPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove default border

        // Combine the search bar and filter bar into a fixed top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Add components to the result panel, below the title label
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(topPanel, BorderLayout.NORTH); // Fixed header (search + filter)
        contentWrapper.add(scrollPane, BorderLayout.CENTER); // Scrollable result list
        resultPanel.add(contentWrapper, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(resultPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();

        // Add action listener to the search button
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            User currentUser = getCurrentUser();

            // Clear the current results
            resPanel.removeAll();

            // Search for users
            UserBUS userBUS = new UserBUS();
            List<User> searchResults = userBUS.searchUsersByUsername(currentUser.getUsername(), searchText);

            if (searchResults != null && !searchResults.isEmpty()) {
                for (User resultUser : searchResults) {
                    if (currentUser.getUsername() != resultUser.getUsername()) {
                        boolean isFriend = userBUS.isFriend(currentUser.getUsername(), resultUser.getUsername());
                        JPanel resultCard = createResultCard(resultUser.getUsername(), isFriend);
                        resPanel.add(resultCard);
                    }
                }
            } else {
                JLabel noResultsLabel = new JLabel("No users found matching the criteria.");
                noResultsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                resPanel.add(noResultsLabel);
            }

            // Update the UI
            resPanel.revalidate();
            resPanel.repaint();
        });

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

        // Add friends to the panel
        if (friends != null && !friends.isEmpty()) {
            Collections.sort(friends, new Comparator<User>() {
                @Override
                public int compare(User user1, User user2) {
                    return user1.getUsername().compareToIgnoreCase(user2.getUsername());
                }
            });

            for (User friend : friends) {
                friendsPanel.add(createFriendCard(friend.getUsername())); // Use friend's username
            }
        } else {
            // Handle the case where the user has no friends
            JLabel noFriendsLabel = new JLabel("No friends found.");
            noFriendsLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            friendsPanel.add(noFriendsLabel);
        }

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
    private static void openChatHistoryScreen(JFrame frame) {
        // Create the chat history panel
        JPanel chatHistoryPanel = new JPanel();
        chatHistoryPanel.setLayout(new BorderLayout());
        chatHistoryPanel.setBackground(Color.WHITE);

        // Title label
        JLabel titleLabel = new JLabel("Chat History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat history list
        JPanel chatListPanel = new JPanel();
        chatListPanel.setLayout(new BoxLayout(chatListPanel, BoxLayout.Y_AXIS));

        // Add dummy conversations
        for (int i = 1; i <= 20; i++) {
            chatListPanel.add(createChatCard("Conversation " + i, frame));
        }

        // Wrap the list in a scroll pane
        JScrollPane scrollPane = new JScrollPane(chatListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        chatHistoryPanel.add(titleLabel, BorderLayout.NORTH);
        chatHistoryPanel.add(scrollPane, BorderLayout.CENTER);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(chatHistoryPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void openConversationDetailScreen(JFrame frame, String conversationName) {
        // Create the conversation detail panel
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);

        // Title label
        JLabel titleLabel = new JLabel("Conversation Detail: " + conversationName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Conversation detail area
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));

        // Add dummy messages
        for (int i = 1; i <= 4; i++) {
            messagePanel.add(createMessageCard("User" + i, "This is message " + i, "10:0" + i + " AM"));
        }

        // Wrap messages in a scroll pane
        JScrollPane scrollPane = new JScrollPane(messagePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> openChatHistoryScreen(frame));

        detailPanel.add(titleLabel, BorderLayout.NORTH);
        detailPanel.add(scrollPane, BorderLayout.CENTER);
        detailPanel.add(backButton, BorderLayout.SOUTH);

        // Replace the current content panel
        frame.getContentPane().removeAll();
        frame.add(createSidebar(frame), BorderLayout.WEST);
        frame.add(detailPanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel createChatCard(String conversationName, JFrame frame) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(240, 240, 240));
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));

        // Conversation name label
        JLabel nameLabel = new JLabel(conversationName);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        card.add(nameLabel, BorderLayout.WEST);

        // View button
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(e -> openConversationDetailScreen(frame, conversationName));
        card.add(viewButton, BorderLayout.EAST);

        return card;
    }

    private static JPanel createMessageCard(String username, String message, String timestamp) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setBackground(new Color(230, 230, 230));
        card.setPreferredSize(new Dimension(card.getPreferredSize().width, CARD_HEIGHT));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));


        JLabel usernameLabel = new JLabel(username);
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

}
