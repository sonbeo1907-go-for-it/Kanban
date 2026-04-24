package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.CategoryDAO;
import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.service.ICategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class CategoryServiceImpl implements ICategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    private final CategoryDAO categoryDAO;

    // Constructor injection
    public CategoryServiceImpl(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @Override
    public List<Category> findAll() {
        try {
            return categoryDAO.findAll();
        } catch (Exception e) {
            logger.error("Error fetching all categories", e);
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }

    @Override
    public List<Category> findAll(int page, int size) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        try {
            return categoryDAO.findAll(size, offset);
        } catch (Exception e) {
            logger.error("Error fetching categories page {} size {}", page, size, e);
            throw new RuntimeException("Failed to fetch categories", e);
        }
    }

    @Override
    public int getTotalPages(int size) {
        if (size < 1) size = 10;
        try {
            int totalRows = categoryDAO.getTotalCategories();
            return (int) Math.ceil((double) totalRows / size);
        } catch (Exception e) {
            logger.error("Error calculating total pages", e);
            throw new RuntimeException("Failed to calculate total pages", e);
        }
    }

    @Override
    public boolean save(Category category) {
        // Validation
        if (category == null) {
            logger.warn("Attempt to save null category");
            return false;
        }
        String name = category.getName();
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Attempt to save category with empty name");
            return false;
        }
        if (name.length() > 100) {
            logger.warn("Category name too long: {}", name);
            return false;
        }
        String trimmedName = name.trim();
        // Kiểm tra trùng tên (tránh duplicate)
        Category existing = findByName(trimmedName);
        if (existing != null) {
            logger.warn("Category with name '{}' already exists", trimmedName);
            return false;
        }
        try {
            category.setName(trimmedName);
            categoryDAO.add(category);
            logger.info("Category saved: {}", trimmedName);
            return true;
        } catch (Exception e) {
            logger.error("Database error while saving category: {}", trimmedName, e);
            throw new RuntimeException("Failed to save category due to database error", e);
        }
    }

    @Override
    public boolean delete(int id) {
        if (id <= 0) {
            logger.warn("Attempt to delete category with invalid id: {}", id);
            return false;
        }
        try {
            categoryDAO.softDelete(id);
            logger.info("Category soft-deleted with id: {}", id);
            return true;
        } catch (Exception e) {
            logger.error("Database error while deleting category id: {}", id, e);
            throw new RuntimeException("Failed to delete category due to database error", e);
        }
    }

    @Override
    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try {
            return categoryDAO.findByName(name.trim());
        } catch (Exception e) {
            logger.error("Error finding category by name: {}", name, e);
            throw new RuntimeException("Failed to find category by name", e);
        }
    }

    @Override
    public List<Category> search(String keyword, int limit, int offset) {
        if (keyword == null) keyword = "";
        try {
            return categoryDAO.search(keyword, limit, offset);
        } catch (Exception e) {
            logger.error("Error searching categories with keyword: {}", keyword, e);
            throw new RuntimeException("Failed to search categories", e);
        }
    }

    @Override
    public int getTotalCategories(String keyword) {
        if (keyword == null) keyword = "";
        try {
            return categoryDAO.getTotalCategories(keyword);
        } catch (Exception e) {
            logger.error("Error counting search results for keyword: {}", keyword, e);
            throw new RuntimeException("Failed to count categories", e);
        }
    }
}