--
-- PostgreSQL init script for Kanban database
--

-- Drop tables if exist (order matters due to foreign keys)
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;

-- Table: roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

-- Table: users
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role_id INT REFERENCES roles(id),
    is_deleted BOOLEAN DEFAULT false
);

-- Table: categories
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN DEFAULT false
);

-- Table: tasks
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status INT DEFAULT 1,
    user_id INT REFERENCES users(id),
    category_id INT REFERENCES categories(id),
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');

-- Insert default categories
INSERT INTO categories (name) VALUES 
    ('Work'), ('Personal'), ('Urgent'), 
    ('Backend Development'), ('Frontend Design'), 
    ('Database Management'), ('DevOps & Cloud'), 
    ('Quality Assurance'), ('CI/CD'), ('Module Test'), 
    ('Component Test '), ('Component Test');

-- Insert default admin user (password: admin)
-- Hash is BCrypt of 'admin' (cost 10)
INSERT INTO users (username, password, full_name, role_id, is_deleted) 
VALUES ('admin', '$2a$10$NkM5C6LxZ9YQ3Z1wR2Q3O.VxVvE3hLqUQlKqGqFqHqJqMqNqOqPq', 'System Administrator', 1, false)
ON CONFLICT (username) DO NOTHING;

-- Update sequences to start from correct values (optional, if you have existing data)
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('tasks_id_seq', (SELECT MAX(id) FROM tasks));