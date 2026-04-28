package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.TaskDAO;
import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskDAO taskDAO;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private Category testCategory;
    private Task validTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");

        testCategory = new Category();
        testCategory.setId(1);
        testCategory.setName("Work");

        validTask = new Task();
        validTask.setId(100);
        validTask.setTitle("Test Task");
        validTask.setDescription("Description");
        validTask.setStatus(1);
        validTask.setUser(testUser);
        validTask.setCategory(testCategory);
    }

    @Test
    @DisplayName("findByUser should return empty list when userId <= 0")
    void findByUser_InvalidUserId_ReturnsEmptyList() {
        List<Task> result = taskService.findByUser(0, null, 0, false);
        assertThat(result).isEmpty();
        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("findByUser should delegate to DAO with trimmed search")
    void findByUser_ValidUserId_DelegatesToDAO() {
        List<Task> expected = List.of(validTask);
        when(taskDAO.findByUser(eq(1), eq("search"), eq(2), eq(true))).thenReturn(expected);

        List<Task> result = taskService.findByUser(1, " search ", 2, true);
        assertThat(result).isSameAs(expected);
        verify(taskDAO).findByUser(1, "search", 2, true);
    }

    @Test
    @DisplayName("findByUser should handle null search as empty string")
    void findByUser_NullSearch_DelegatesWithEmpty() {
        List<Task> expected = List.of(validTask);
        when(taskDAO.findByUser(eq(1), eq(""), eq(0), eq(false))).thenReturn(expected);

        List<Task> result = taskService.findByUser(1, null, 0, false);
        assertThat(result).isSameAs(expected);
        verify(taskDAO).findByUser(1, "", 0, false);
    }

    @Test
    @DisplayName("findByUser should propagate RuntimeException from DAO")
    void findByUser_DAOThrowsException_ThrowsRuntimeException() {
        when(taskDAO.findByUser(anyInt(), anyString(), anyInt(), anyBoolean()))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> taskService.findByUser(1, "test", 0, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch tasks");
    }

    @Test
    @DisplayName("add should return false when task is null")
    void add_NullTask_ReturnsFalse() {
        boolean result = taskService.add(null);
        assertThat(result).isFalse();
        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("add should return false when title is null or empty")
    void add_InvalidTitle_ReturnsFalse() {
        validTask.setTitle(null);
        assertThat(taskService.add(validTask)).isFalse();

        validTask.setTitle(" ");
        assertThat(taskService.add(validTask)).isFalse();

        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("add should return false when user is invalid")
    void add_InvalidUser_ReturnsFalse() {
        validTask.setUser(null);
        assertThat(taskService.add(validTask)).isFalse();

        User invalidUser = new User();
        invalidUser.setId(0);
        validTask.setUser(invalidUser);
        assertThat(taskService.add(validTask)).isFalse();

        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("add should return false when category is invalid")
    void add_InvalidCategory_ReturnsFalse() {
        validTask.setCategory(null);
        assertThat(taskService.add(validTask)).isFalse();

        Category invalidCat = new Category();
        invalidCat.setId(0);
        validTask.setCategory(invalidCat);
        assertThat(taskService.add(validTask)).isFalse();

        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("add should return true and call DAO when valid")
    void add_ValidTask_ReturnsTrueAndDelegates() {
        when(taskDAO.add(any(Task.class))).thenReturn(true);

        boolean result = taskService.add(validTask);
        assertThat(result).isTrue();
        verify(taskDAO).add(validTask);
    }

    @Test
    @DisplayName("add should return false when DAO returns false")
    void add_DAOReturnsFalse_ReturnsFalse() {
        when(taskDAO.add(any(Task.class))).thenReturn(false);

        boolean result = taskService.add(validTask);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("add should propagate RuntimeException from DAO")
    void add_DAOThrowsException_ThrowsRuntimeException() {
        when(taskDAO.add(any(Task.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> taskService.add(validTask))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to add task");
    }

    @Test
    @DisplayName("update should return false when task is null or id invalid")
    void update_InvalidTask_ReturnsFalse() {
        assertThat(taskService.update(null)).isFalse();

        validTask.setId(0);
        assertThat(taskService.update(validTask)).isFalse();

        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("update should validate title, user, category")
    void update_InvalidFields_ReturnsFalse() {
        validTask.setTitle(null);
        assertThat(taskService.update(validTask)).isFalse();
        validTask.setTitle(" ");
        assertThat(taskService.update(validTask)).isFalse();

        validTask.setTitle("Valid");
        validTask.setUser(null);
        assertThat(taskService.update(validTask)).isFalse();

        User invalidUser = new User();
        invalidUser.setId(0);
        validTask.setUser(invalidUser);
        assertThat(taskService.update(validTask)).isFalse();

        validTask.setUser(testUser);
        validTask.setCategory(null);
        assertThat(taskService.update(validTask)).isFalse();

        Category invalidCat = new Category();
        invalidCat.setId(0);
        validTask.setCategory(invalidCat);
        assertThat(taskService.update(validTask)).isFalse();

        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("update should return true and delegate to DAO")
    void update_ValidTask_ReturnsTrue() {
        when(taskDAO.update(any(Task.class))).thenReturn(true);

        boolean result = taskService.update(validTask);
        assertThat(result).isTrue();
        verify(taskDAO).update(validTask);
    }

    @Test
    @DisplayName("update should return false when DAO returns false")
    void update_DAOReturnsFalse_ReturnsFalse() {
        when(taskDAO.update(any(Task.class))).thenReturn(false);

        boolean result = taskService.update(validTask);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("update should propagate RuntimeException from DAO")
    void update_DAOThrowsException_ThrowsRuntimeException() {
        when(taskDAO.update(any(Task.class))).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> taskService.update(validTask))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update task");
    }

    @Test
    @DisplayName("updateStatus should return false for invalid parameters")
    void updateStatus_InvalidParams_ReturnsFalse() {
        assertThat(taskService.updateStatus(0, 1, 1)).isFalse();
        assertThat(taskService.updateStatus(1, 1, 0)).isFalse();
        assertThat(taskService.updateStatus(1, 0, 1)).isFalse();
        assertThat(taskService.updateStatus(1, 4, 1)).isFalse();
        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("updateStatus should return true and delegate to DAO")
    void updateStatus_ValidParams_ReturnsTrue() {
        when(taskDAO.updateStatus(1, 2, 1)).thenReturn(true);

        boolean result = taskService.updateStatus(1, 2, 1);
        assertThat(result).isTrue();
        verify(taskDAO).updateStatus(1, 2, 1);
    }

    @Test
    @DisplayName("updateStatus should return false when DAO returns false")
    void updateStatus_DAOReturnsFalse_ReturnsFalse() {
        when(taskDAO.updateStatus(1, 2, 1)).thenReturn(false);

        boolean result = taskService.updateStatus(1, 2, 1);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("updateStatus should propagate RuntimeException from DAO")
    void updateStatus_DAOThrowsException_ThrowsRuntimeException() {
        when(taskDAO.updateStatus(1, 2, 1)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> taskService.updateStatus(1, 2, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update task status");
    }

    @Test
    @DisplayName("delete should return false for invalid parameters")
    void delete_InvalidParams_ReturnsFalse() {
        assertThat(taskService.delete(0, 1)).isFalse();
        assertThat(taskService.delete(1, 0)).isFalse();
        verifyNoInteractions(taskDAO);
    }

    @Test
    @DisplayName("delete should return true and delegate to DAO")
    void delete_ValidParams_ReturnsTrue() {
        when(taskDAO.softDelete(1, 1)).thenReturn(true);

        boolean result = taskService.delete(1, 1);
        assertThat(result).isTrue();
        verify(taskDAO).softDelete(1, 1);
    }

    @Test
    @DisplayName("delete should return false when DAO returns false")
    void delete_DAOReturnsFalse_ReturnsFalse() {
        when(taskDAO.softDelete(1, 1)).thenReturn(false);

        boolean result = taskService.delete(1, 1);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("delete should propagate RuntimeException from DAO")
    void delete_DAOThrowsException_ThrowsRuntimeException() {
        when(taskDAO.softDelete(1, 1)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> taskService.delete(1, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to delete task");
    }
}