package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.util.DBContext;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TaskDAOTest {

    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("kanban_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("init_test.sql");

    private static TaskDAO taskDAO;
    private static User testUser;
    private static Category anyCategory;

    @BeforeAll
    static void setup() {
        postgres.start();
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        DBContext.reloadForTest();

        UserDAO userDAO = new UserDAO();
        String testUsername = "testuser_" + System.currentTimeMillis();
        boolean registered = userDAO.register(testUsername, "test", "Test User");
        assertThat(registered).isTrue();
        testUser = userDAO.login(testUsername, "test");
        assertThat(testUser).isNotNull();
        assertThat(testUser.getRoleId()).isEqualTo(2); 

        CategoryDAO categoryDAO = new CategoryDAO();
        List<Category> categories = categoryDAO.findAll();
        assertThat(categories).isNotEmpty();
        anyCategory = categories.get(0);

        taskDAO = new TaskDAO();
    }

    @AfterAll
    static void teardown() {
        postgres.stop();
    }

    @BeforeEach
    void cleanTasks() {
        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        for (Task t : tasks) {
            taskDAO.softDelete(t.getId(), testUser.getId());
        }
    }

    @Test
    @Order(1)
    @DisplayName("Add new task successfully")
    void testAddTask_Success() {
        Task task = newTask("Test Task", 1);
        boolean added = taskDAO.add(task);
        assertThat(added).isTrue();

        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("Add task fails with empty title")
    void testAddTask_EmptyTitle() {
        Task task = newTask("", 1);
        boolean added = taskDAO.add(task);
        assertThat(added).isFalse();
    }

    @Test
    @DisplayName("Find tasks by user – no filters")
    void testFindByUser_NoFilters() {
        taskDAO.add(newTask("Task A", 1));
        taskDAO.add(newTask("Task B", 2));

        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting(Task::getTitle).containsExactlyInAnyOrder("Task A", "Task B");
    }

    @Test
    @DisplayName("Search tasks by title (LIKE)")
    void testFindByUser_WithSearch() {
        taskDAO.add(newTask("Alpha Task", 1));
        taskDAO.add(newTask("Beta Task", 2));
        taskDAO.add(newTask("Gamma Task", 3));

        List<Task> result = taskDAO.findByUser(testUser.getId(), "Alpha", 0, false);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Alpha Task");
    }

    @Test
    @DisplayName("Filter tasks by category")
    void testFindByUser_WithCategory() {
        CategoryDAO catDAO = new CategoryDAO();
        List<Category> cats = catDAO.findAll();
        Category secondCat = cats.size() > 1 ? cats.get(1) : anyCategory;

        Task task1 = newTask("Task Cat1", 1, anyCategory);
        Task task2 = newTask("Task Cat2", 2, secondCat);
        taskDAO.add(task1);
        taskDAO.add(task2);

        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, anyCategory.getId(), false);
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Task Cat1");
    }

    @Test
    @DisplayName("Hide old done tasks (status=3 and older than 7 days)")
    void testFindByUser_HideOldDone() {
        Task oldDone = newTask("Old Done", 3);
        taskDAO.add(oldDone);

        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        long oldTaskId = tasks.stream()
                .filter(t -> t.getTitle().equals("Old Done"))
                .findFirst().get().getId();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET created_at = ? WHERE id = ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(10)));
            ps.setLong(2, oldTaskId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Task recentDone = newTask("Recent Done", 3);
        taskDAO.add(recentDone);

        Task pending = newTask("Pending", 1);
        taskDAO.add(pending);

        List<Task> filtered = taskDAO.findByUser(testUser.getId(), null, 0, true);
        assertThat(filtered).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Recent Done", "Pending");
    }

    @Test
    @DisplayName("Update task details")
    void testUpdateTask() {
        Task task = newTask("Original", 1);
        taskDAO.add(task);
        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        Task toUpdate = tasks.get(0);

        toUpdate.setTitle("Updated Title");
        toUpdate.setDescription("New desc");
        toUpdate.setStatus(2);
        toUpdate.setCategory(anyCategory);

        boolean updated = taskDAO.update(toUpdate);
        assertThat(updated).isTrue();

        Task updatedTask = taskDAO.findByUser(testUser.getId(), null, 0, false).get(0);
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.getStatus()).isEqualTo(2);
    }

    @Test
    @DisplayName("Update task status")
    void testUpdateStatus() {
        Task task = newTask("Status Task", 1);
        taskDAO.add(task);
        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        int taskId = tasks.get(0).getId();

        boolean statusUpdated = taskDAO.updateStatus(taskId, 3, testUser.getId());
        assertThat(statusUpdated).isTrue();

        Task updated = taskDAO.findByUser(testUser.getId(), null, 0, false).get(0);
        assertThat(updated.getStatus()).isEqualTo(3);
    }

    @Test
    @DisplayName("Soft delete task")
    void testSoftDelete() {
        Task task = newTask("To Delete", 1);
        taskDAO.add(task);
        List<Task> tasks = taskDAO.findByUser(testUser.getId(), null, 0, false);
        assertThat(tasks).hasSize(1);
        int taskId = tasks.get(0).getId();

        boolean deleted = taskDAO.softDelete(taskId, testUser.getId());
        assertThat(deleted).isTrue();

        List<Task> remaining = taskDAO.findByUser(testUser.getId(), null, 0, false);
        assertThat(remaining).isEmpty();
    }

    private Task newTask(String title, int status) {
        return newTask(title, status, anyCategory);
    }

    private Task newTask(String title, int status, Category category) {
        Task t = new Task();
        t.setTitle(title);
        t.setDescription("Sample desc");
        t.setStatus(status);
        t.setUser(testUser);
        t.setCategory(category);
        t.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        return t;
    }
}