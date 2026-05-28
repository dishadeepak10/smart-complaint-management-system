import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class Main {

    static Connection con;

    public static void main(String[] args) {

        connectDB();

        JFrame frame = new JFrame("Complaint Management System");
        frame.setSize(700, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // MAIN PANEL
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(224, 242, 241));

        // TITLE
        JLabel title = new JLabel("Complaint Management System", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(15, 10, 15, 10));
        title.setForeground(new Color(0, 77, 64));
        panel.add(title, BorderLayout.NORTH);

        // FORM PANEL
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 15, 15));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(20, 40, 20, 40),
                BorderFactory.createLineBorder(new Color(200, 230, 201), 2)
        ));

        formPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField();

        JLabel complaintLabel = new JLabel("Complaint:");
        JTextField complaintField = new JTextField();

        styleField(nameField);
        styleField(complaintField);

        formPanel.add(nameLabel);
        formPanel.add(nameField);

        formPanel.add(complaintLabel);
        formPanel.add(complaintField);

        panel.add(formPanel, BorderLayout.CENTER);

        // BUTTON PANEL
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        buttonPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        buttonPanel.setBackground(new Color(224, 242, 241));

        JButton submitBtn = createButton("Submit");
        JButton viewBtn = createButton("View All");
        JButton searchBtn = createButton("Search");
        JButton resolveBtn = createButton("Resolve");
        JButton statsBtn = createButton("Stats");

        buttonPanel.add(submitBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(searchBtn);
        buttonPanel.add(resolveBtn);
        buttonPanel.add(statsBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);

        // SUBMIT BUTTON
        submitBtn.addActionListener(e -> {

            if (con == null) {
                JOptionPane.showMessageDialog(frame,
                        "Database not connected!");
                return;
            }

            String name = nameField.getText().trim();
            String complaint = complaintField.getText().trim().toLowerCase();

            if (name.isEmpty() || complaint.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please fill all fields!");
                return;
            }

            String priority;
            String status = "Pending";

            if (complaint.contains("water") ||
                    complaint.contains("electricity")) {

                priority = "Urgent";

            } else if (complaint.contains("road") ||
                    complaint.contains("garbage")) {

                priority = "Normal";

            } else {
                priority = "Low";
            }

            try {

                PreparedStatement pst = con.prepareStatement(
                        "INSERT INTO complaints(name, complaint_text, priority, status) VALUES(?,?,?,?)"
                );

                pst.setString(1, name);
                pst.setString(2, complaint);
                pst.setString(3, priority);
                pst.setString(4, status);

                pst.executeUpdate();

                JOptionPane.showMessageDialog(frame,
                        "Complaint Submitted Successfully!\nPriority: " + priority);

                nameField.setText("");
                complaintField.setText("");

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame,
                        "Error inserting complaint!");
            }
        });

        // VIEW BUTTON
        viewBtn.addActionListener(e -> {

            try {

                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM complaints");

                StringBuilder sb = new StringBuilder();

                while (rs.next()) {

                    sb.append("ID: ")
                            .append(rs.getInt("id"))

                            .append(" | Name: ")
                            .append(rs.getString("name"))

                            .append(" | Complaint: ")
                            .append(rs.getString("complaint_text"))

                            .append(" | Priority: ")
                            .append(rs.getString("priority"))

                            .append(" | Status: ")
                            .append(rs.getString("status"))

                            .append("\n\n");
                }

                JOptionPane.showMessageDialog(frame, sb.toString());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // SEARCH BUTTON
        searchBtn.addActionListener(e -> {

            String name = JOptionPane.showInputDialog("Enter Name:");

            try {

                PreparedStatement pst = con.prepareStatement(
                        "SELECT * FROM complaints WHERE name=?"
                );

                pst.setString(1, name);

                ResultSet rs = pst.executeQuery();

                StringBuilder sb = new StringBuilder();

                while (rs.next()) {

                    sb.append("ID: ")
                            .append(rs.getInt("id"))

                            .append(" | Complaint: ")
                            .append(rs.getString("complaint_text"))

                            .append(" | Priority: ")
                            .append(rs.getString("priority"))

                            .append(" | Status: ")
                            .append(rs.getString("status"))

                            .append("\n\n");
                }

                JOptionPane.showMessageDialog(frame, sb.toString());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // RESOLVE BUTTON
        resolveBtn.addActionListener(e -> {

            String id = JOptionPane.showInputDialog("Enter Complaint ID:");

            try {

                PreparedStatement pst = con.prepareStatement(
                        "UPDATE complaints SET status='Resolved' WHERE id=?"
                );

                pst.setInt(1, Integer.parseInt(id));

                pst.executeUpdate();

                JOptionPane.showMessageDialog(frame,
                        "Complaint Marked as Resolved!");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // STATS BUTTON
        statsBtn.addActionListener(e -> {

            try {

                PreparedStatement pst = con.prepareStatement(
                        "SELECT COUNT(*) AS total, " +
                                "SUM(priority='Urgent') AS urgent, " +
                                "SUM(status='Resolved') AS resolved " +
                                "FROM complaints"
                );

                ResultSet rs = pst.executeQuery();

                rs.next();

                JOptionPane.showMessageDialog(frame,

                        "Complaint Statistics\n\n" +

                                "Total Complaints: "
                                + rs.getInt("total") + "\n\n"

                                + "Urgent Complaints: "
                                + rs.getInt("urgent") + "\n\n"

                                + "Resolved Complaints: "
                                + rs.getInt("resolved")
                );

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // BUTTON STYLE
    static JButton createButton(String text) {

        JButton btn = new JButton(text);

        btn.setFocusPainted(false);
        btn.setBackground(new Color(0, 150, 136));
        btn.setForeground(Color.WHITE);

        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        return btn;
    }

    // TEXT FIELD STYLE
    static void styleField(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        field.setBorder(BorderFactory.createLineBorder(
                new Color(180, 200, 200), 1
        ));
    }

    // DATABASE CONNECTION
    static void connectDB() {

        try {

            System.out.println("Trying to connect database...");

            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/complaints_db",
                    "root",
                    "1234"
            );

            System.out.println("Database Connected Successfully!");

        } catch (Exception e) {

            System.out.println("Database Connection Failed!");

            e.printStackTrace();
        }
    }
}