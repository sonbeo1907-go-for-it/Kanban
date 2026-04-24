package com.casestudy.kanban.service;

import com.casestudy.kanban.model.Task;
import java.util.List;

public interface ITaskService {
    List<Task> findByUser(int userId, String search, int categoryId, boolean hideOldDone);
    
    boolean add(Task task);
    
    boolean update(Task task);
    
    boolean updateStatus(int taskId, int newStatus, int userId);
    
    boolean delete(int taskId, int userId);
}