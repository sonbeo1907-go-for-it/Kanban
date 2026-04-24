package com.casestudy.kanban.listener;

import com.casestudy.kanban.util.DBContext;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebListener
public class DatabaseInitializer implements ServletContextListener {

    static {
        System.out.println("DatabaseInitializer class loaded");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        System.out.println("=== DatabaseInitializer started, checking users table...");
        String checkQuery = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                String adminUsername = "admin";
                String adminPassword = System.getenv("DEFAULT_ADMIN_PASSWORD");
                if (adminPassword == null || adminPassword.trim().isEmpty()) {
                    adminPassword = "admin123"; // fallback, nhưng nên cảnh báo log
                    System.err.println("WARNING: Using default admin password 'admin123'. Please set env DEFAULT_ADMIN_PASSWORD for production.");
                }
                String hashedPassword = BCrypt.hashpw(adminPassword, BCrypt.gensalt());
                String fullName = "System Administrator";
                int roleId = 1; // ADMIN role
                
                String insertQuery = "INSERT INTO users (username, password, full_name, role_id, is_deleted) VALUES (?, ?, ?, ?, 0)";
                try (Connection conn2 = DBContext.getConnection();
                     PreparedStatement ps2 = conn2.prepareStatement(insertQuery)) {
                    ps2.setString(1, adminUsername);
                    ps2.setString(2, hashedPassword);
                    ps2.setString(3, fullName);
                    ps2.setInt(4, roleId);
                    int rows = ps2.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Default admin user created successfully. Username: admin");
                    }
                } catch (SQLException e) {
                    System.err.println("Failed to insert default admin: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("Users table already has data, skipping admin creation.");
            }
        } catch (SQLException e) {
            System.err.println("Error checking users table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}