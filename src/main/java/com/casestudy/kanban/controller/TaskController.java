package com.casestudy.kanban.controller;

import com.casestudy.kanban.dao.CategoryDAO;
import com.casestudy.kanban.dao.TaskDAO;
import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.model.Task;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.service.ICategoryService;
import com.casestudy.kanban.service.ITaskService;
import com.casestudy.kanban.service.impl.CategoryServiceImpl;
import com.casestudy.kanban.service.impl.TaskServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "TaskController", value = "/user-dashboard")
public class TaskController extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private ITaskService taskService;
    private ICategoryService categoryService;

    @Override
    public void init() {
        TaskDAO taskDAO = new TaskDAO();
        taskService = new TaskServiceImpl(taskDAO);
        CategoryDAO categoryDAO = new CategoryDAO();
        categoryService = new CategoryServiceImpl(categoryDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":
                showDashboard(request, response);
                break;
            default:
                showDashboard(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        switch (action) {
            case "add":
                addTask(request, response);
                break;
            case "update":
                updateTask(request, response);
                break;
            case "updateStatus":
                updateTaskStatus(request, response);
                break;
            case "delete":
                deleteTask(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/user-auth?action=login-page");
            return;
        }

        String search = request.getParameter("search");
        if (search == null) search = "";

        int categoryId = 0;
        try {
            String catIdStr = request.getParameter("categoryId");
            if (catIdStr != null && !catIdStr.isEmpty()) {
                categoryId = Integer.parseInt(catIdStr);
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid categoryId parameter: {}", request.getParameter("categoryId"));
        }

        boolean hideOldDone = "true".equals(request.getParameter("hideOldDone"));

        try {
            List<Task> tasks = taskService.findByUser(currentUser.getId(), search, categoryId, hideOldDone);
            List<Category> categories = categoryService.findAll();

            request.setAttribute("tasks", tasks);
            request.setAttribute("categories", categories);
            request.setAttribute("searchKeyword", search);
            request.setAttribute("selectedCatId", categoryId);
            request.setAttribute("hideOldDone", hideOldDone);
            request.setAttribute("msg", request.getParameter("msg"));

            request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error loading dashboard for user {}", currentUser.getId(), e);
            request.setAttribute("error", "Không thể tải dữ liệu, vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/user/dashboard.jsp").forward(request, response);
        }
    }

    private void addTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/user-auth?action=login-page");
            return;
        }

        String title = request.getParameter("title");
        String desc = request.getParameter("description");
        String catIdStr = request.getParameter("categoryId");

        if (title == null || title.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=emptyTitle");
            return;
        }
        if (catIdStr == null || catIdStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=missingCategory");
            return;
        }

        try {
            int catId = Integer.parseInt(catIdStr);
            Category cat = new Category();
            cat.setId(catId);

            Task task = new Task();
            task.setTitle(title.trim());
            task.setDescription(desc != null ? desc : "");
            task.setStatus(1);
            task.setUser(user);
            task.setCategory(cat);

            boolean added = taskService.add(task);
            if (added) {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=addSuccess");
            } else {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=addFailed");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid categoryId: {}", catIdStr);
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=invalidCategory");
        } catch (Exception e) {
            logger.error("Error adding task for user {}", user.getId(), e);
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=error");
        }
    }

    private void updateTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/user-auth?action=login-page");
            return;
        }

        String idStr = request.getParameter("id");
        String title = request.getParameter("title");
        String desc = request.getParameter("description");
        String statusStr = request.getParameter("status");
        String catIdStr = request.getParameter("categoryId");

        if (idStr == null || title == null || title.trim().isEmpty() || statusStr == null || catIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=missingFields");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            int status = Integer.parseInt(statusStr);
            int catId = Integer.parseInt(catIdStr);

            Category cat = new Category();
            cat.setId(catId);

            Task task = new Task();
            task.setId(id);
            task.setTitle(title.trim());
            task.setDescription(desc != null ? desc : "");
            task.setStatus(status);
            task.setUser(user);
            task.setCategory(cat);

            boolean updated = taskService.update(task);
            if (updated) {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=updateSuccess");
            } else {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=updateFailed");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid number format in updateTask: id={}, status={}, catId={}", idStr, statusStr, catIdStr);
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=invalidData");
        } catch (Exception e) {
            logger.error("Error updating task for user {}", user.getId(), e);
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=error");
        }
    }

    private void updateTaskStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("User not logged in");
            return;
        }

        String idStr = request.getParameter("id");
        String newStatusStr = request.getParameter("newStatus");

        if (idStr == null || newStatusStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing parameters");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            int newStatus = Integer.parseInt(newStatusStr);

            boolean updated = taskService.updateStatus(id, newStatus, user.getId());
            if (updated) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("OK");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Task not found or not owned by user");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid number format");
        } catch (Exception e) {
            logger.error("Error updating task status", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Database error");
        }
    }

    private void deleteTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/user-auth?action=login-page");
            return;
        }

        String idStr = request.getParameter("id");
        if (idStr == null) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=missingId");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            boolean deleted = taskService.delete(id, user.getId());
            if (deleted) {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=deleteSuccess");
            } else {
                response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=deleteFailed");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=invalidId");
        } catch (Exception e) {
            logger.error("Error deleting task for user {}", user.getId(), e);
            response.sendRedirect(request.getContextPath() + "/user-dashboard?action=list&msg=error");
        }
    }
}