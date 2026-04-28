package com.casestudy.kanban.dao;

import com.casestudy.kanban.model.User;
import com.casestudy.kanban.util.DBContext;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDAOTest {

    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("kanban_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withInitScript("init_test.sql");

    @BeforeAll
    static void setup() {
        postgres.start();
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        DBContext.reloadForTest(); 
    }

    @AfterAll
    static void teardown() {
        postgres.stop();
    }

    private UserDAO userDAO;

    @BeforeEach
    void init() {
        userDAO = new UserDAO();
    }

    @Test
    @DisplayName("Login success with correct credentials")
    void testLoginSuccess() {
        User user = userDAO.login("admin", "test");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getRoleId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void testLoginWrongPassword() {
        User user = userDAO.login("admin", "wrong");
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("Login fails with non-existent username")
    void testLoginUserNotFound() {
        User user = userDAO.login("ghost", "anything");
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("Register new user successfully")
    void testRegisterSuccess() {
        boolean registered = userDAO.register("newuser", "password123", "New User");
        assertThat(registered).isTrue();
        assertThat(userDAO.checkUserExist("newuser")).isTrue();

        User user = userDAO.login("newuser", "password123");
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("newuser");
    }

    @Test
    @DisplayName("Register duplicate username returns false")
    void testRegisterDuplicate() {
        boolean registered = userDAO.register("admin", "whatever", "Admin Clone");
        assertThat(registered).isFalse();
    }

    @Test
    @DisplayName("Check existing user returns true")
    void testCheckUserExistTrue() {
        assertThat(userDAO.checkUserExist("admin")).isTrue();
    }

    @Test
    @DisplayName("Check non-existing user returns false")
    void testCheckUserExistFalse() {
        assertThat(userDAO.checkUserExist("ghost")).isFalse();
    }
}