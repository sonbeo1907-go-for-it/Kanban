package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.Category;
import com.casestudy.kanban.util.DBContext;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CategoryDAOTest {

    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("kanban_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("init_test.sql");

    private static CategoryDAO categoryDAO;

    @BeforeAll
    static void setup() {
        postgres.start();
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());

        DBContext.reloadForTest();
        categoryDAO = new CategoryDAO();
    }

    @AfterAll
    static void teardown() {
        postgres.stop();
    }

    @BeforeEach
    void cleanUp() {
        List<Category> all = categoryDAO.findAll();
        for (Category cat : all) {
            if (cat.getName().startsWith("TestCat_")) {
                categoryDAO.softDelete(cat.getId());
            }
        }
    }

    @Test
    @DisplayName("Find all categories (non-deleted)")
    void testFindAll() {
        List<Category> categories = categoryDAO.findAll();
        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Work", "Personal", "Urgent");
    }

    @Test
    @DisplayName("Find all categories with pagination")
    void testFindAllPaginated() {
        List<Category> firstPage = categoryDAO.findAll(2, 0);
        assertThat(firstPage).hasSize(2);
        List<Category> secondPage = categoryDAO.findAll(2, 2);
        assertThat(secondPage).hasSize(1);
    }

    @Test
    @DisplayName("Add new category successfully")
    void testAddCategorySuccess() {
        String uniqueName = "TestCat_" + System.currentTimeMillis();
        Category newCat = new Category(0, uniqueName, false);
        boolean added = categoryDAO.add(newCat);
        assertThat(added).isTrue();

        Category found = categoryDAO.findByName(uniqueName);
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo(uniqueName);
        assertThat(found.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("Add duplicate category should fail")
    void testAddDuplicateCategory() {
        String name = "Work";
        Category dup = new Category(0, name, false);
        boolean added = categoryDAO.add(dup);
        assertThat(added).isFalse();
    }

    @Test
    @DisplayName("Add category with invalid name (empty) should fail")
    void testAddCategoryInvalidName() {
        Category emptyName = new Category(0, "", false);
        assertThat(categoryDAO.add(emptyName)).isFalse();

        Category nullName = new Category(0, null, false);
        assertThat(categoryDAO.add(nullName)).isFalse();
    }

    @Test
    @DisplayName("Find category by name (case-sensitive)")
    void testFindByName() {
        Category found = categoryDAO.findByName("Work");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Work");

        Category notFound = categoryDAO.findByName("NonExistent");
        assertThat(notFound).isNull();

        // Chú ý: code hiện tại dùng '=' nên phân biệt hoa thường
        Category lowerCase = categoryDAO.findByName("work");
        assertThat(lowerCase).isNull();
    }

    @Test
    @DisplayName("Search categories by keyword (case-insensitive LIKE)")
    void testSearchCategories() {
        List<Category> result = categoryDAO.search("or", 10, 0);
        assertThat(result).hasSize(1);
        assertThat(result).extracting(Category::getName).containsExactlyInAnyOrder("Work");

        List<Category> resultE = categoryDAO.search("e", 10, 0);
        assertThat(resultE).hasSize(2);
        assertThat(resultE).extracting(Category::getName).containsExactlyInAnyOrder("Personal", "Urgent");
    }

    @Test
    @DisplayName("Soft delete category")
    void testSoftDelete() {
        String name = "TestCat_ToDelete_" + System.currentTimeMillis();
        Category cat = new Category(0, name, false);
        categoryDAO.add(cat);
        Category before = categoryDAO.findByName(name);
        assertThat(before).isNotNull();

        boolean deleted = categoryDAO.softDelete(before.getId());
        assertThat(deleted).isTrue();

        Category after = categoryDAO.findByName(name);
        assertThat(after).isNull();

        // Kiểm tra total count không còn category đó
        List<Category> all = categoryDAO.findAll();
        assertThat(all).extracting(Category::getName).doesNotContain(name);
    }

    @Test
    @DisplayName("Get total number of categories")
    void testGetTotalCategories() {
        int total = categoryDAO.getTotalCategories();
        assertThat(total).isEqualTo(3);

        String newName = "TestCat_Count_" + System.currentTimeMillis();
        categoryDAO.add(new Category(0, newName, false));
        assertThat(categoryDAO.getTotalCategories()).isEqualTo(4);

        // Xóa mềm category đó
        Category added = categoryDAO.findByName(newName);
        categoryDAO.softDelete(added.getId());
        assertThat(categoryDAO.getTotalCategories()).isEqualTo(3);
    }

    @Test
    @DisplayName("Get total categories with search keyword")
    void testGetTotalCategoriesWithKeyword() {
        int count = categoryDAO.getTotalCategories("or");
        assertThat(count).isEqualTo(1);

        count = categoryDAO.getTotalCategories("xyz");
        assertThat(count).isEqualTo(0);
    }
}