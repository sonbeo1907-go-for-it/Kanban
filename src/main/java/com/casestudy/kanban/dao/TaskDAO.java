package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.util.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private DBContext dbContext = new DBContext();

    private static final String SELECT_BASE = 
        "SELECT t.*, c.name as category_name FROM tasks t " +
        "JOIN categories c ON t.category_id = c.id " +
        "WHERE t.user_id = ? AND t.is_deleted = 0 ";

    public List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone) {
        List<Task> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SELECT_BASE);
        
        if (search != null && !search.isEmpty()) {
            sql.append("AND t.title LIKE ? ");
        }
        if (categoryId > 0) {
            sql.append("AND t.category_id = ? ");
        }

        if (hideOldDone) {
            sql.append("AND NOT (t.status = 3 AND t.created_at < DATE_SUB(NOW(), INTERVAL 7 DAY)) ");
        }
        
        sql.append("ORDER BY t.created_at DESC");

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            ps.setInt(paramIndex++, userId);
            
            if (search != null && !search.isEmpty()) {
                ps.setString(paramIndex++, "%" + search + "%");
            }
            if (categoryId > 0) {
                ps.setInt(paramIndex++, categoryId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToTask(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    public void add(Task task) {
        String sql = "INSERT INTO tasks (title, description, status, user_id, category_id, is_deleted, created_at) VALUES (?, ?, ?, ?, ?, 0, NOW())";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getStatus());
            ps.setInt(4, task.getUser().getId());
            ps.setInt(5, task.getCategory().getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, category_id = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getStatus());
            ps.setInt(4, task.getCategory().getId());
            ps.setInt(5, task.getId());
            ps.setInt(6, task.getUser().getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void updateStatus(int taskId, int newStatus, int userId) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void softDelete(int taskId, int userId) {
        String sql = "UPDATE tasks SET is_deleted = 1 WHERE id = ? AND user_id = ?";
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Category cat = new Category();
        cat.setId(rs.getInt("category_id"));
        cat.setName(rs.getString("category_name"));

        User user = new User();
        user.setId(rs.getInt("user_id"));

        return new Task(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getInt("status"),
            user,
            cat,
            rs.getBoolean("is_deleted"),
            rs.getTimestamp("created_at")
        );
    }
}