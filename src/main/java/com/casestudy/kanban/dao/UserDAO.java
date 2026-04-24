package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.User;
import com.casestudy.kanban.util.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {
    Connection conn = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    public User login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ? AND is_deleted = 0";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("full_name"),
                    rs.getInt("role_id"),
                    rs.getBoolean("is_deleted")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkUserExist(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void register(String username, String password, String fullName) {
        String query = "INSERT INTO users (username, password, full_name, role_id, is_deleted) VALUES (?, ?, ?, 2, 0)";
        try {
            conn = new DBContext().getConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}