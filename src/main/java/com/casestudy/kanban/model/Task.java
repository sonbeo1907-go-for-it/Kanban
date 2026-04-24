package com.casestudy.kanban.model;

import java.sql.Timestamp;

public class Task {
    private int id;
    private String title;
    private String description;
    private int status;
    private User user;
    private Category category;
    private boolean isDeleted;
    private Timestamp createdAt;

    public Task() {}

    public Task (int id, String title, String description, int status, User user, Category category, boolean isDeleted, Timestamp createdAt){
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.user = user;
        this.category = category;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}