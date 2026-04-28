package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.util.DBContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    private static final String SELECT_PAGINATED = "SELECT * FROM categories WHERE is_deleted = false LIMIT ? OFFSET ?";
    private static final String SELECT_ALL = "SELECT * FROM categories WHERE is_deleted = false";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM categories WHERE is_deleted = false";
    private static final String INSERT = "INSERT INTO categories (name, is_deleted) VALUES (?, false)";
    private static final String DELETE = "UPDATE categories SET is_deleted = true WHERE id = ?";
    private static final String SELECT_BY_NAME = "SELECT * FROM categories WHERE name = ? AND is_deleted = false";
    private static final String SELECT_SEARCH_PAGINATED = "SELECT * FROM categories WHERE is_deleted = false AND name LIKE ? LIMIT ? OFFSET ?";
    private static final String COUNT_SEARCH = "SELECT COUNT(*) FROM categories WHERE is_deleted = false AND name LIKE ?";

    public List<Category> findAll(int limit, int offset) {
        List<Category> list = new ArrayList<>();
        String sql = SELECT_PAGINATED;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding categories with limit {} offset {}", limit, offset, e);
            throw new RuntimeException("Database error while fetching categories", e);
        }
        return list;
    }

    public int getTotalCategories() {
        String sql = COUNT_ALL;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting total categories", e);
            throw new RuntimeException("Database error while counting categories", e);
        }
    }

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        String sql = SELECT_ALL;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all categories", e);
            throw new RuntimeException("Database error while fetching all categories", e);
        }
        return list;
    }

    public List<Category> search(String keyword, int limit, int offset) {
        List<Category> list = new ArrayList<>();
        if (keyword == null) keyword = "";
        String sql = SELECT_SEARCH_PAGINATED;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCategory(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching categories with keyword {}", keyword, e);
            throw new RuntimeException("Database error while searching categories", e);
        }
        return list;
    }

    public int getTotalCategories(String keyword) {
        if (keyword == null) keyword = "";
        String sql = COUNT_SEARCH;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            logger.error("Error counting categories with keyword {}", keyword, e);
            throw new RuntimeException("Database error while counting search results", e);
        }
    }

    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        String sql = SELECT_BY_NAME;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding category by name: {}", name, e);
            throw new RuntimeException("Database error while finding category by name", e);
        }
        return null;
    }

    public boolean add(Category category) {
        if (category == null || category.getName() == null || category.getName().trim().isEmpty()) {
            logger.warn("Attempt to add category with invalid name");
            return false;
        }
        String name = category.getName().trim();
        if (name.length() > 100) {
            logger.warn("Category name too long: {}", name);
            return false;
        }

        if (findByName(name) != null) {
            logger.warn("Category with name '{}' already exists and is not deleted", name);
            return false;
        }

        String sql = INSERT;
        try (Connection conn = DBContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Category added: {}", name);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error adding category: {}", name, e);
            throw new RuntimeException("Database error while adding category", e);
        }
    }

    public boolean softDelete(int id) {
        String sql = DELETE;
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                logger.info("Category soft-deleted, id: {}", id);
                return true;
            }
            return false;
        } catch (SQLException e) {
            logger.error("Error soft-deleting category id: {}", id, e);
            throw new RuntimeException("Database error while deleting category", e);
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        return new Category(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getBoolean("is_deleted")
        );
    }
}