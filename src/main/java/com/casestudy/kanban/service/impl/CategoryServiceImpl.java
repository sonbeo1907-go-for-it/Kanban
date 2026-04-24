package com.casestudy.kanban.service.impl;
import com.casestudy.kanban.dao.CategoryDAO;
import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.service.ICategoryService;
import java.util.List;

public class CategoryServiceImpl implements ICategoryService {
    private CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    public List<Category> findAll() { return categoryDAO.findAll(); }

    @Override
    public List<Category> findAll(int page, int size) {
        int offset = (page - 1) * size;
        return categoryDAO.findAll(size, offset);
    }

    @Override
    public int getTotalPages(int size) {
        int totalRows = categoryDAO.getTotalCategories();
        return (int) Math.ceil((double) totalRows / size);
    }

    @Override
    public void save(Category category) { categoryDAO.add(category); }

    @Override
    public void delete(int id) { categoryDAO.softDelete(id); }

    @Override
    public Category findByName(String name) {
        return categoryDAO.findByName(name);
    }

    @Override
    public List<Category> search(String keyword, int limit, int offset) {
        return categoryDAO.search(keyword, limit, offset);
    }

    @Override
    public int getTotalCategories(String keyword) {
        return categoryDAO.getTotalCategories(keyword);
    }
}