package com.casestudy.kanban.service;

import com.casestudy.kanban.model.Task;
import java.util.List;

public interface ITaskService {
    List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone);
    
    void add(Task task);
    
    void update(Task task);
    
    void updateStatus(int taskId, int newStatus, int userId);
    
    void delete(int taskId, int userId);
}