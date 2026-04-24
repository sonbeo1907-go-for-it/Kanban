package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.TaskDAO;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.service.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TaskServiceImpl implements ITaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
    private final TaskDAO taskDAO;

    public TaskServiceImpl(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    @Override
    public List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone) {
        if (userId <= 0) {
            logger.warn("findByUser called with invalid userId: {}", userId);
            return List.of();
        }
        String cleanSearch = (search == null) ? "" : search.trim();
        try {
            return taskDAO.findByUser(userId, cleanSearch, categoryId, hideOldDone);
        } catch (Exception e) {
            logger.error("Error finding tasks for userId: {}", userId, e);
            throw new RuntimeException("Failed to fetch tasks", e);
        }
    }

    @Override
    public boolean add(Task task) {
        // Validation
        if (task == null) {
            logger.warn("Attempt to add null task");
            return false;
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            logger.warn("Attempt to add task with empty title");
            return false;
        }
        if (task.getUser() == null || task.getUser().getId() <= 0) {
            logger.warn("Attempt to add task with invalid user");
            return false;
        }
        if (task.getCategory() == null || task.getCategory().getId() <= 0) {
            logger.warn("Attempt to add task with invalid category");
            return false;
        }
        try {
            boolean success = taskDAO.add(task);
            if (success) {
                logger.info("Task added: {}", task.getTitle());
            } else {
                logger.warn("Failed to add task: {}", task.getTitle());
            }
            return success;
        } catch (Exception e) {
            logger.error("Database error while adding task", e);
            throw new RuntimeException("Failed to add task", e);
        }
    }

    @Override
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
            logger.warn("Attempt to update task with invalid user, id={}", task.getId());
            return false;
        }
        if (task.getCategory() == null || task.getCategory().getId() <= 0) {
            logger.warn("Attempt to update task with invalid category, id={}", task.getId());
            return false;
        }
        try {
            boolean success = taskDAO.update(task);
            if (success) {
                logger.info("Task updated: id={}", task.getId());
            } else {
                logger.warn("No task found to update: id={}", task.getId());
            }
            return success;
        } catch (Exception e) {
            logger.error("Database error while updating task id={}", task.getId(), e);
            throw new RuntimeException("Failed to update task", e);
        }
    }

    @Override
    public boolean updateStatus(int taskId, int newStatus, int userId) {
        if (taskId <= 0 || userId <= 0) {
            logger.warn("Invalid parameters for updateStatus: taskId={}, userId={}", taskId, userId);
            return false;
        }
        if (newStatus < 1 || newStatus > 3) {
            logger.warn("Invalid status value: {}", newStatus);
            return false;
        }
        try {
            boolean success = taskDAO.updateStatus(taskId, newStatus, userId);
            if (success) {
                logger.info("Status updated for task {}, new status: {}, user: {}", taskId, newStatus, userId);
            } else {
                logger.warn("Status update failed: task {} not found for user {}", taskId, userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error updating status for taskId={}, userId={}", taskId, userId, e);
            throw new RuntimeException("Failed to update task status", e);
        }
    }

    @Override
    public boolean delete(int taskId, int userId) {
        if (taskId <= 0 || userId <= 0) {
            logger.warn("Invalid parameters for delete: taskId={}, userId={}", taskId, userId);
            return false;
        }
        try {
            boolean success = taskDAO.softDelete(taskId, userId);
            if (success) {
                logger.info("Task deleted: id={}, user={}", taskId, userId);
            } else {
                logger.warn("Delete failed: task {} not found for user {}", taskId, userId);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error deleting taskId={}, userId={}", taskId, userId, e);
            throw new RuntimeException("Failed to delete task", e);
        }
    }
}