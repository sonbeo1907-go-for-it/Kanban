package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.util.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    private static final String SELECT_BASE =
            "SELECT t.*, c.name as category_name FROM tasks t " +
            "JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = ? AND t.is_deleted = false ";

    public List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone) {
        List<Task> list = new ArrayList<>();
        if (userId <= 0) {
            logger.warn("findByUser called with invalid userId: {}", userId);
            return list;
        }

        StringBuilder sql = new StringBuilder(SELECT_BASE);

        if (search != null && !search.trim().isEmpty()) {
            sql.append("AND t.title LIKE ? ");
        }
        if (categoryId > 0) {
            sql.append("AND t.category_id = ? ");
        }
        if (hideOldDone) {
            sql.append("AND NOT (t.status = 3 AND t.created_at < NOW() - INTERVAL '7 days') ");
        }
        sql.append("ORDER BY t.created_at DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            ps.setInt(paramIndex++, userId);

            if (search != null && !search.trim().isEmpty()) {
                ps.setString(paramIndex++, "%" + search.trim() + "%");
            }
            if (categoryId > 0) {
                ps.setInt(paramIndex++, categoryId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding tasks for userId: {}, search: {}, categoryId: {}, hideOldDone: {}",
                    userId, search, categoryId, hideOldDone, e);
            throw new RuntimeException("Database error while fetching tasks", e);
        }
        return list;
    }

    public boolean add(Task task) {
        if (task == null || task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            logger.warn("Attempt to add invalid task (null or empty title)");
            return false;
        }
        if (task.getUser() == null || task.getUser().getId() <= 0) {
            logger.warn("Attempt to add task with invalid user id");
            return false;
        }
        if (task.getCategory() == null || task.getCategory().getId() <= 0) {
            logger.warn("Attempt to add task with invalid category id");
            return false;
        }

        String sql = "INSERT INTO tasks (title, description, status, user_id, category_id, is_deleted, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, false, NOW())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle().trim());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getStatus());
            ps.setInt(4, task.getUser().getId());
            ps.setInt(5, task.getCategory().getId());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Task added: title={}, userId={}", task.getTitle(), task.getUser().getId());
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error adding task: {}", task.getTitle(), e);
            throw new RuntimeException("Database error while adding task", e);
        }
    }

    public boolean update(Task task) {
        if (task == null || task.getId() <= 0) {
            logger.warn("Attempt to update task with invalid id");
            return false;
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            logger.warn("Attempt to update task with empty title, id={}", task.getId());
            return false;
        }
        if (task.getUser() == null || task.getUser().getId() <= 0) {
            logger.warn("Attempt to update task with invalid user id, taskId={}", task.getId());
            return false;
        }
        if (task.getCategory() == null || task.getCategory().getId() <= 0) {
            logger.warn("Attempt to update task with invalid category id, taskId={}", task.getId());
            return false;
        }

        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, category_id = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, task.getTitle().trim());
            ps.setString(2, task.getDescription());
            ps.setInt(3, task.getStatus());
            ps.setInt(4, task.getCategory().getId());
            ps.setInt(5, task.getId());
            ps.setInt(6, task.getUser().getId());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Task updated, id={}, userId={}", task.getId(), task.getUser().getId());
                return true;
            } else {
                logger.warn("No task found to update: id={}, userId={}", task.getId(), task.getUser().getId());
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error updating task id={}", task.getId(), e);
            throw new RuntimeException("Database error while updating task", e);
        }
    }

    public boolean updateStatus(int taskId, int newStatus, int userId) {
        if (taskId <= 0 || userId <= 0) {
            logger.warn("Invalid taskId or userId for status update: taskId={}, userId={}", taskId, userId);
            return false;
        }
        if (newStatus < 1 || newStatus > 3) {
            logger.warn("Invalid status value: {}", newStatus);
            return false;
        }

        String sql = "UPDATE tasks SET status = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatus);
            ps.setInt(2, taskId);
            ps.setInt(3, userId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Task status updated: id={}, newStatus={}, userId={}", taskId, newStatus, userId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error updating status for taskId: {}, userId: {}", taskId, userId, e);
            throw new RuntimeException("Database error while updating task status", e);
        }
    }

    public boolean softDelete(int taskId, int userId) {
        if (taskId <= 0 || userId <= 0) {
            logger.warn("Invalid taskId or userId for soft delete: taskId={}, userId={}", taskId, userId);
            return false;
        }

        String sql = "UPDATE tasks SET is_deleted = true WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.setInt(2, userId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Task soft-deleted: id={}, userId={}", taskId, userId);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error soft-deleting taskId: {}, userId: {}", taskId, userId, e);
            throw new RuntimeException("Database error while deleting task", e);
        }
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