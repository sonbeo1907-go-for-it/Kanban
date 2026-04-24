package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.util.DBContext; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DBContext dbContext = new DBContext();

    private static final String SELECT_PAGINATED = "SELECT * FROM categories WHERE is_deleted = 0 LIMIT ? OFFSET ?";
    private static final String SELECT_ALL = "SELECT * FROM categories WHERE is_deleted = 0";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM categories WHERE is_deleted = 0";
    private static final String INSERT = "INSERT INTO categories (name, is_deleted) VALUES (?, 0)";
    private static final String DELETE = "UPDATE categories SET is_deleted = 1 WHERE id = ?";
    private static final String SELECT_BY_NAME = "SELECT * FROM categories WHERE name = ? AND is_deleted = 0";
    private static final String SELECT_SEARCH_PAGINATED =  "SELECT * FROM categories WHERE is_deleted = 0 AND name LIKE ? LIMIT ? OFFSET ?";
    private static final String COUNT_SEARCH = "SELECT COUNT(*) FROM categories WHERE is_deleted = 0 AND name LIKE ?";

    public List<Category> findAll(int limit, int offset) {
        List<Category> list = new ArrayList<>();
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PAGINATED)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getBoolean("is_deleted")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getTotalCategories() {
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ALL)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Category> findAll() {
        List<Category> list = new ArrayList<>();
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"), 
                    rs.getString("name"), 
                    rs.getBoolean("is_deleted")
                ));
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
        return list;
    }

    public List<Category> search(String keyword, int limit, int offset) {
    List<Category> list = new ArrayList<>();
    try (Connection conn = dbContext.getConnection();
         PreparedStatement ps = conn.prepareStatement(SELECT_SEARCH_PAGINATED)) {
        ps.setString(1, "%" + keyword + "%");
        ps.setInt(2, limit);
        ps.setInt(3, offset);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getBoolean("is_deleted")));
        }
    } catch (Exception e) { e.printStackTrace(); }
    return list;
    }

    public int getTotalCategories(String keyword) {
        try (Connection conn = dbContext.getConnection();
            PreparedStatement ps = conn.prepareStatement(COUNT_SEARCH)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public Category findByName(String name) {
    try (Connection conn = dbContext.getConnection();
         PreparedStatement ps = conn.prepareStatement(SELECT_BY_NAME)) {
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Category(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getBoolean("is_deleted")
            );
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
    }

    public void add(Category category) {
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT)) {
            ps.setString(1, category.getName());
            ps.executeUpdate();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    public void softDelete(int id) {
        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
}