package com.casestudy.kanban.service;

import com.casestudy.kanban.model.User;

public interface IUserService {
    User login(String username, String password);
    boolean register(String username, String password, String fullName);
    boolean checkUserExist(String username);
}