CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100),
    role_id INT REFERENCES roles(id),
    is_deleted BOOLEAN DEFAULT false
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    is_deleted BOOLEAN DEFAULT false
);

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

INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');
INSERT INTO users (username, password, full_name, role_id, is_deleted)
VALUES ('admin', '$2a$12$er9xdAI/U92NRS4EK9isSu7FM9OU1JOitYzGl70k6e9qKZQnwcwju', 'Admin User', 1, false);
INSERT INTO categories (name) VALUES ('Work'), ('Personal'), ('Urgent');