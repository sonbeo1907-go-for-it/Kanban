package com.casestudy.kanban.controller;

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
import java.io.IOException;
import java.util.List;

@WebServlet(name = "TaskController", value = "/user-dashboard")
public class TaskController extends HttpServlet {

    private ITaskService taskService = new TaskServiceImpl();
    private ICategoryService categoryService = new CategoryServiceImpl();

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
        }
    }

    private void showDashboard(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User currentUser = (User) session.getAttribute("currentUser");

        String search = request.getParameter("search");
        
        int categoryId = 0;
        try {
            String catIdStr = request.getParameter("categoryId");
            if (catIdStr != null) categoryId = Integer.parseInt(catIdStr);
        } catch (NumberFormatException e) {}

        boolean hideOldDone = "true".equals(request.getParameter("hideOldDone"));

        List<Task> tasks = taskService.findByUser(currentUser.getId(), search, categoryId, hideOldDone);
        List<Category> categories = categoryService.findAll();

        request.setAttribute("tasks", tasks);
        request.setAttribute("categories", categories);
        request.setAttribute("searchKeyword", search);
        request.setAttribute("selectedCatId", categoryId);
        request.setAttribute("hideOldDone", hideOldDone);

        request.getRequestDispatcher("/views/user/dashboard.jsp").forward(request, response);
    }

    private void addTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        String title = request.getParameter("title");
        String desc = request.getParameter("description");
        int catId = Integer.parseInt(request.getParameter("categoryId"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(desc);
        task.setStatus(1);
        task.setUser(user);
        
        Category cat = new Category();
        cat.setId(catId);
        task.setCategory(cat);

        taskService.add(task);
        response.sendRedirect("user-dashboard?msg=addSuccess");
    }

    private void updateTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        int id = Integer.parseInt(request.getParameter("id"));
        String title = request.getParameter("title");
        String desc = request.getParameter("description");
        int status = Integer.parseInt(request.getParameter("status"));
        int catId = Integer.parseInt(request.getParameter("categoryId"));

        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(desc);
        task.setStatus(status);
        task.setUser(user);

        Category cat = new Category();
        cat.setId(catId);
        task.setCategory(cat);

        taskService.update(task);
        response.sendRedirect("user-dashboard?msg=updateSuccess");
    }

    private void updateTaskStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        int id = Integer.parseInt(request.getParameter("id"));
        int newStatus = Integer.parseInt(request.getParameter("newStatus"));

        taskService.updateStatus(id, newStatus, user.getId());

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void deleteTask(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = (User) request.getSession().getAttribute("currentUser");
        int id = Integer.parseInt(request.getParameter("id"));

        taskService.delete(id, user.getId());
        response.sendRedirect("user-dashboard?msg=deleteSuccess");
    }
}