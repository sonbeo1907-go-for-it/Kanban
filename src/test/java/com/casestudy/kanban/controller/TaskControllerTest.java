package com.casestudy.kanban.controller;

import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.service.ICategoryService;
import com.casestudy.kanban.service.ITaskService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private ITaskService taskService;
    @Mock
    private ICategoryService categoryService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;
    @Mock
    private PrintWriter printWriter;

    private TaskController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new TaskController();
        // Inject mocks
        java.lang.reflect.Field taskField = TaskController.class.getDeclaredField("taskService");
        taskField.setAccessible(true);
        taskField.set(controller, taskService);
        java.lang.reflect.Field categoryField = TaskController.class.getDeclaredField("categoryService");
        categoryField.setAccessible(true);
        categoryField.set(controller, categoryService);
    }

    private void mockLoggedInUser(User user) {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(user);
    }

    // ================== showDashboard ==================
    @Test
    @DisplayName("showDashboard - user not logged in -> redirect to login")
    void showDashboard_UserNotLoggedIn_RedirectsToLogin() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        // action không cần stub, vì nếu null sẽ dùng default "list"
        controller.doGet(request, response);
        verify(response).sendRedirect("/user-auth?action=login-page");
        verifyNoInteractions(taskService, categoryService);
    }

    @Test
    @DisplayName("showDashboard - success with default parameters")
    void showDashboard_Success_ForwardsToDashboard() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("list");
        when(request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp")).thenReturn(dispatcher);
        when(taskService.findByUser(eq(1), eq(""), eq(0), eq(false))).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of());

        controller.doGet(request, response);

        verify(taskService).findByUser(1, "", 0, false);
        verify(categoryService).findAll();
        verify(request).setAttribute("tasks", List.of());
        verify(request).setAttribute("categories", List.of());
        verify(request).setAttribute("searchKeyword", "");
        verify(request).setAttribute("selectedCatId", 0);
        verify(request).setAttribute("hideOldDone", false);
        verify(request).setAttribute("msg", null);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("showDashboard - with filters and msg")
    void showDashboard_WithFilters_Success() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        // Chỉ stub các parameter có giá trị khác default
        when(request.getParameter("action")).thenReturn("list");
        when(request.getParameter("search")).thenReturn("test");
        when(request.getParameter("categoryId")).thenReturn("5");
        when(request.getParameter("hideOldDone")).thenReturn("true");
        when(request.getParameter("msg")).thenReturn("addSuccess");
        // Không cần stub contextPath, action
        when(request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp")).thenReturn(dispatcher);
        when(taskService.findByUser(eq(1), eq("test"), eq(5), eq(true))).thenReturn(List.of());
        when(categoryService.findAll()).thenReturn(List.of());

        controller.doGet(request, response);

        verify(request).setAttribute("searchKeyword", "test");
        verify(request).setAttribute("selectedCatId", 5);
        verify(request).setAttribute("hideOldDone", true);
        verify(request).setAttribute("msg", "addSuccess");
    }

    @Test
    @DisplayName("showDashboard - service throws exception -> forward with error")
    void showDashboard_ServiceThrowsException_ForwardsWithError() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        // Chỉ cần stub requestDispatcher và service throw
        when(request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp")).thenReturn(dispatcher);
        when(taskService.findByUser(anyInt(), anyString(), anyInt(), anyBoolean()))
                .thenThrow(new RuntimeException("DB error"));

        controller.doGet(request, response);

        verify(request).setAttribute(eq("error"), anyString());
        verify(dispatcher).forward(request, response);
    }

    // ================== addTask ==================
    private void stubAddParams(String title, String catId, String desc) {
        lenient().when(request.getParameter("title")).thenReturn(title);
        lenient().when(request.getParameter("categoryId")).thenReturn(catId);
        lenient().when(request.getParameter("description")).thenReturn(desc);
    }

    @Test
    @DisplayName("addTask - user not logged in -> redirect login")
    void addTask_UserNotLoggedIn_RedirectsToLogin() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-auth?action=login-page");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("addTask - empty title -> redirect emptyTitle")
    void addTask_EmptyTitle_RedirectsWithMsg() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("", "1", null);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=emptyTitle");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("addTask - missing category -> redirect missingCategory")
    void addTask_MissingCategory_RedirectsWithMsg() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("Task", "", null);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=missingCategory");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("addTask - successful add -> redirect addSuccess")
    void addTask_Success_RedirectsWithAddSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("New Task", "2", "desc");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");
        when(taskService.add(any(Task.class))).thenReturn(true);

        controller.doPost(request, response);

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).add(taskCaptor.capture());
        Task task = taskCaptor.getValue();
        assertThat(task.getTitle()).isEqualTo("New Task");
        assertThat(task.getCategory().getId()).isEqualTo(2);
        assertThat(task.getStatus()).isEqualTo(1);
        verify(response).sendRedirect("/user-dashboard?action=list&msg=addSuccess");
    }

    @Test
    @DisplayName("addTask - service returns false -> redirect addFailed")
    void addTask_ServiceReturnsFalse_RedirectsWithAddFailed() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("Task", "1", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");
        when(taskService.add(any(Task.class))).thenReturn(false);

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=addFailed");
    }

    @Test
    @DisplayName("addTask - invalid categoryId format -> redirect invalidCategory")
    void addTask_InvalidCategoryId_RedirectsWithInvalidCategory() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("Task", "abc", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=invalidCategory");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("addTask - service throws exception -> redirect error")
    void addTask_ServiceThrowsException_RedirectsWithError() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubAddParams("Task", "1", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("add");
        when(taskService.add(any(Task.class))).thenThrow(new RuntimeException("DB error"));

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=error");
    }

    // ================== updateTask ==================
    private void stubUpdateParams(String id, String title, String status, String catId, String desc) {
        lenient().when(request.getParameter("id")).thenReturn(id);
        lenient().when(request.getParameter("title")).thenReturn(title);
        lenient().when(request.getParameter("status")).thenReturn(status);
        lenient().when(request.getParameter("categoryId")).thenReturn(catId);
        lenient().when(request.getParameter("description")).thenReturn(desc);
    }

    @Test
    @DisplayName("updateTask - user not logged in -> redirect login")
    void updateTask_UserNotLoggedIn_RedirectsToLogin() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("update");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-auth?action=login-page");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("updateTask - missing fields -> redirect missingFields")
    void updateTask_MissingFields_RedirectsWithMissingFields() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubUpdateParams("1", "", "1", "1", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("update");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=missingFields");
        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("updateTask - success -> redirect updateSuccess")
    void updateTask_Success_RedirectsWithUpdateSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubUpdateParams("10", "Updated", "2", "3", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("update");
        when(taskService.update(any(Task.class))).thenReturn(true);

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=updateSuccess");
    }

    @Test
    @DisplayName("updateTask - service returns false -> redirect updateFailed")
    void updateTask_ServiceReturnsFalse_RedirectsWithUpdateFailed() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubUpdateParams("10", "Updated", "2", "3", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("update");
        when(taskService.update(any(Task.class))).thenReturn(false);

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=updateFailed");
    }

    @Test
    @DisplayName("updateTask - invalid number format -> redirect invalidData")
    void updateTask_NumberFormatException_RedirectsWithInvalidData() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        stubUpdateParams("abc", "Title", "1", "1", "");
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("update");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=invalidData");
    }

    // ================== updateTaskStatus (AJAX) ==================
    @Test
    @DisplayName("updateTaskStatus - user not logged in -> 401")
    void updateTaskStatus_UserNotLoggedIn_ReturnsUnauthorized() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(response.getWriter()).thenReturn(printWriter);

        controller.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("updateTaskStatus - missing params -> 400")
    void updateTaskStatus_MissingParameters_ReturnsBadRequest() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(request.getParameter("id")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        controller.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("updateTaskStatus - success -> 200 OK")
    void updateTaskStatus_Success_ReturnsOK() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("newStatus")).thenReturn("3");
        when(response.getWriter()).thenReturn(printWriter);
        when(taskService.updateStatus(5, 3, 1)).thenReturn(true);

        controller.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(printWriter).write("OK");
    }

    @Test
    @DisplayName("updateTaskStatus - service returns false -> 404")
    void updateTaskStatus_ServiceReturnsFalse_ReturnsNotFound() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("updateStatus");
        when(request.getParameter("id")).thenReturn("5");
        when(request.getParameter("newStatus")).thenReturn("3");
        when(response.getWriter()).thenReturn(printWriter);
        when(taskService.updateStatus(5, 3, 1)).thenReturn(false);

        controller.doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    // ================== deleteTask ==================
    @Test
    @DisplayName("deleteTask - user not logged in -> redirect login")
    void deleteTask_UserNotLoggedIn_RedirectsToLogin() throws Exception {
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("currentUser")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");
        when(request.getParameter("action")).thenReturn("delete");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-auth?action=login-page");
    }

    @Test
    @DisplayName("deleteTask - missing id -> redirect missingId")
    void deleteTask_MissingId_RedirectsWithMissingId() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn(null);
        when(request.getContextPath()).thenReturn("");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=missingId");
    }

    @Test
    @DisplayName("deleteTask - success -> redirect deleteSuccess")
    void deleteTask_Success_RedirectsWithDeleteSuccess() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("10");
        when(request.getContextPath()).thenReturn("");
        when(taskService.delete(10, 1)).thenReturn(true);

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=deleteSuccess");
    }

    @Test
    @DisplayName("deleteTask - invalid id format -> redirect invalidId")
    void deleteTask_InvalidIdFormat_RedirectsWithInvalidId() throws Exception {
        User user = new User();
        user.setId(1);
        mockLoggedInUser(user);
        when(request.getParameter("action")).thenReturn("delete");
        when(request.getParameter("id")).thenReturn("abc");
        when(request.getContextPath()).thenReturn("");

        controller.doPost(request, response);

        verify(response).sendRedirect("/user-dashboard?action=list&msg=invalidId");
    }
}