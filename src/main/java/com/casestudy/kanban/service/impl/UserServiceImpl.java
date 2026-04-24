package com.casestudy.kanban.service.impl;

import com.casestudy.kanban.dao.UserDAO;
import com.casestudy.kanban.model.User;
import com.casestudy.kanban.service.IUserService;

public class UserServiceImpl implements IUserService {
    private UserDAO userDAO = new UserDAO();

    @Override
    public User login(String username, String password) {
        return userDAO.login(username, password);
    }

    @Override
    public boolean register(String username, String password, String fullName) {
        if(checkUserExist(username)) return false;
        userDAO.register(username, password, fullName);
        return true;
    }

    @Override
    public boolean checkUserExist(String username) {
        return userDAO.checkUserExist(username);
    }
}