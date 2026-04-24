package com.casestudy.kanban.controller;

import com.casestudy.kanban.dao.CategoryDAO;
import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.service.ICategoryService;
import com.casestudy.kanban.service.impl.CategoryServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin-category"})
public class CategoryController extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private ICategoryService categoryService;
    private static final int PAGE_SIZE = 5;

    @Override
    public void init() throws ServletException {
        CategoryDAO categoryDAO = new CategoryDAO();
        categoryService = new CategoryServiceImpl(categoryDAO);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":
                listCategories(request, response);
                break;
            case "delete":
                deleteCategory(request, response);
                break;
            default:
                listCategories(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("add".equals(action)) {
            addCategory(request, response);
        }
    }

    private void listCategories(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String keyword = request.getParameter("search");
        if (keyword == null) keyword = "";

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isEmpty()) {
            try { 
                page = Integer.parseInt(pageStr); 
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        if (page < 1) page = 1;

        int offset = (page - 1) * PAGE_SIZE;
        
        try {
            List<Category> categories = categoryService.search(keyword, PAGE_SIZE, offset);
            int totalRows = categoryService.getTotalCategories(keyword);
            int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);

            request.setAttribute("categories", categories);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("searchKeyword", keyword);
            request.setAttribute("msg", request.getParameter("msg"));

            request.getRequestDispatcher("/WEB-INF/views/admin/category-list.jsp").forward(request, response);
        } catch (Exception e) {
            logger.error("Error listing categories", e);
            request.setAttribute("error", "Lỗi tải danh sách danh mục, vui lòng thử lại sau.");
            request.getRequestDispatcher("/WEB-INF/views/admin/category-list.jsp").forward(request, response);
        }
    }

    private void addCategory(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        String name = request.getParameter("categoryName");
        String contextPath = request.getContextPath();

        if (name == null || name.trim().isEmpty()) {
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=emptyName");
            return;
        }
        String trimmedName = name.trim();
        if (trimmedName.length() > 100) {
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=nameTooLong");
            return;
        }

        try {
            Category existing = categoryService.findByName(trimmedName);
            if (existing != null) {
                response.sendRedirect(contextPath + "/admin-category?action=list&msg=duplicate");
                return;
            }

            boolean saved = categoryService.save(new Category(0, trimmedName, false));
            if (saved) {
                response.sendRedirect(contextPath + "/admin-category?action=list&msg=addSuccess");
            } else {
                response.sendRedirect(contextPath + "/admin-category?action=list&msg=addFailed");
            }
        } catch (Exception e) {
            logger.error("Error adding category: {}", trimmedName, e);
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=error");
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String idParam = request.getParameter("id");
        String contextPath = request.getContextPath();

        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=invalidId");
            return;
        }
        try {
            int id = Integer.parseInt(idParam);
            boolean deleted = categoryService.delete(id);
            if (deleted) {
                response.sendRedirect(contextPath + "/admin-category?action=list&msg=deleteSuccess");
            } else {
                response.sendRedirect(contextPath + "/admin-category?action=list&msg=deleteFailed");
            }
        } catch (NumberFormatException e) {
            logger.warn("Invalid id format: {}", idParam);
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=invalidId");
        } catch (Exception e) {
            logger.error("Error deleting category id: {}", idParam, e);
            response.sendRedirect(contextPath + "/admin-category?action=list&msg=error");
        }
    }
}