package com.casestudy.kanban.service;
import com.casestudy.kanban.model.Category;
import java.util.List;

public interface ICategoryService {
    List<Category> findAll();
    List<Category> findAll(int page, int size);
    int getTotalPages(int size);
    void save(Category category);
    void delete(int id);
    Category findByName(String name);
    List<Category> search(String keyword, int limit, int offset);
    int getTotalCategories(String keyword);
}