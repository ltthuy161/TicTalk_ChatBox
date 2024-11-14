import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class UserListPanel {
    private JPanel mainPanel;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserListPanel(JPanel mainContainer) {
        // Set up the main panel with a BorderLayout
        mainPanel = new JPanel(new BorderLayout());

        // Create the header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("Quay lại Dashboard");
        JLabel headerLabel = new JLabel("Quản lý người dùng", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // Add action listener to the back button
        backButton.addActionListener(e -> switchToDashboard(mainContainer));

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        // Create the sidebar panel
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.LIGHT_GRAY);

        // Add buttons to the sidebar
        JButton addButton = new JButton("Thêm tài khoản");
        JButton editButton = new JButton("Chỉnh sửa tài khoản");
        JButton lockButton = new JButton("Khoá/Mở khoá tài khoản");
        JButton deleteButton = new JButton("Xóa tài khoản");
        JButton updatePasswordButton = new JButton("Cập nhật mật khẩu");
        JButton viewHistoryButton = new JButton("Xem lịch sử đăng nhập");
        JButton friendListButton = new JButton("Danh sách bạn bè");

        // Add buttons to the sidebar
        sidebar.add(Box.createRigidArea(new Dimension(0, 10))); // Add spacing
        sidebar.add(addButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(editButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(lockButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(deleteButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(updatePasswordButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(viewHistoryButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(friendListButton);
        sidebar.add(Box.createVerticalGlue());

        // Add action listeners for buttons
        addButton.addActionListener(e -> new AddUserDialog(mainPanel, tableModel));
        editButton.addActionListener(e -> new EditUserHandler(mainPanel, userTable, tableModel).execute());
        lockButton.addActionListener(e -> new LockUnlockHandler(mainPanel, userTable, tableModel).execute());
        deleteButton.addActionListener(e -> new DeleteUserHandler(mainPanel, userTable, tableModel).execute());
        updatePasswordButton.addActionListener(e -> new UpdatePasswordDialog(mainPanel, userTable).execute());
        viewHistoryButton.addActionListener(e -> new ViewHistoryDialog(mainPanel, userTable).execute());
        friendListButton.addActionListener(e -> new FriendListDialog(mainPanel, userTable).execute());

        // Create column names for the table
        String[] columnNames = {"Tên đăng nhập", "Họ tên", "Địa chỉ", "Ngày sinh", "Giới tính", "Email", "Trạng thái"};

        // Create sample data for the table
        Object[][] data = {
                {"john123", "John Doe", "123 Main St, City A", "1990-05-15", "Nam", "john@example.com", "Mở khoá"},
                {"jane456", "Jane Smith", "456 Elm St, City B", "1992-08-22", "Nữ", "jane@example.com", "Mở khoá"}
        };

        // Create the table model
        tableModel = new DefaultTableModel(data, columnNames);

        // Create the table and set the model
        userTable = new JTable(tableModel);

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Add the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);

        // Add components to the main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST); // Sidebar on the left
        mainPanel.add(scrollPane, BorderLayout.CENTER); // Table in the center
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void switchToDashboard(JPanel mainContainer) {
        CardLayout cl = (CardLayout) mainContainer.getLayout();
        cl.show(mainContainer, "Dashboard");
    }
}
