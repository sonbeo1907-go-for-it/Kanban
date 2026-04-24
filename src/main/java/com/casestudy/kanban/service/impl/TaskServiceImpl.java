package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.TaskDAO;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.service.ITaskService;
import java.util.List;

public class TaskServiceImpl implements ITaskService {
    private TaskDAO taskDAO = new TaskDAO();

    @Override
    public List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone) {
        String cleanSearch = (search == null) ? "" : search.trim();
        return taskDAO.findByUser(userId, cleanSearch, categoryId, hideOldDone);
    }

    @Override
    public void add(Task task) {
        if (task.getTitle() != null && !task.getTitle().trim().isEmpty()) {
            taskDAO.add(task);
        }
    }

    @Override
    public void update(Task task) {
        if (task.getId() > 0 && task.getTitle() != null) {
            taskDAO.update(task);
        }
    }

    @Override
    public void updateStatus(int taskId, int newStatus, int userId) {
        if (newStatus >= 1 && newStatus <= 3) {
            taskDAO.updateStatus(taskId, newStatus, userId);
        }
    }

    @Override
    public void delete(int taskId, int userId) {
            taskDAO.softDelete(taskId, userId);
    }
}