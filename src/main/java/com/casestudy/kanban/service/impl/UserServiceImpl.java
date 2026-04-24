package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.UserDAO;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements IUserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Login attempt with empty username");
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Login attempt with empty password for username: {}", username);
            return null;
        }

        try {
            User user = userDAO.login(username.trim(), password);
            if (user != null) {
                user.setPassword(null);
                logger.info("User {} logged in successfully", username);
                return user;
            } else {
                logger.warn("Failed login attempt for username: {}", username);
                return null;
            }
        } catch (Exception e) {
            logger.error("Database error during login for user: {}", username, e);
            throw new RuntimeException("Login failed due to system error", e);
        }
    }

    @Override
    public boolean register(String username, String password, String fullName) {
        // Validation
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Registration failed: username is empty");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Registration failed: password is empty for username {}", username);
            return false;
        }
        if (password.length() < 6) {
            logger.warn("Registration failed: password too short (min 6 chars) for username {}", username);
            return false;
        }

        String safeFullName = (fullName == null || fullName.trim().isEmpty()) ? "" : fullName.trim();

        String trimmedUsername = username.trim();
        try {
            if (checkUserExist(trimmedUsername)) {
                logger.warn("Registration failed: username {} already exists", trimmedUsername);
                return false;
            }
            userDAO.register(trimmedUsername, password, safeFullName);
            logger.info("User {} registered successfully", trimmedUsername);
            return true;
        } catch (Exception e) {
            logger.error("Error during registration for user: {}", trimmedUsername, e);
            throw new RuntimeException("Registration failed due to system error", e);
        }
    }

    @Override
    public boolean checkUserExist(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        try {
            return userDAO.checkUserExist(username.trim());
        } catch (Exception e) {
            logger.error("Error checking existence for username: {}", username, e);
            throw new RuntimeException("Database error while checking user existence", e);
        }
    }
}