package com.casestudy.kanban.controller;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.service.ICategoryService;
import com.casestudy.kanban.service.impl.CategoryServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin-category"})
public class CategoryController extends HttpServlet {

    private final ICategoryService categoryService = new CategoryServiceImpl();
    private final int PAGE_SIZE = 5;

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
        if (action.equals("add")) {
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
            try { page = Integer.parseInt(pageStr); } catch (NumberFormatException e) { page = 1; }
        }

        int offset = (page - 1) * PAGE_SIZE;
        
        List<Category> categories = categoryService.search(keyword, PAGE_SIZE, offset);
        int totalRows =  categoryService.getTotalCategories(keyword);
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);

        request.setAttribute("categories", categories);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("searchKeyword", keyword);

        request.getRequestDispatcher("/views/admin/category-list.jsp").forward(request, response);
    }

    private void addCategory(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        String name = request.getParameter("categoryName");
        
        if (name != null && !name.trim().isEmpty()) {
            // Kiểm tra trùng tên
            Category existingCategory = categoryService.findByName(name.trim());
            
            if (existingCategory != null) {
                response.sendRedirect(request.getContextPath() + "/admin-category?action=list&msg=duplicate");
            } else {
                categoryService.save(new Category(0, name.trim(), false));
                response.sendRedirect(request.getContextPath() + "/admin-category?action=list&msg=addSuccess");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/admin-category?action=list");
        }
    }

    private void deleteCategory(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            categoryService.delete(id);
            // Redirect kèm tham số msg
            response.sendRedirect(request.getContextPath() + "/admin-category?action=list&msg=deleteSuccess");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/admin-category?action=list");
        }
    }
}