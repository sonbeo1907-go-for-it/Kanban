package com.casestudy.kanban.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }

        String server = requireEnv("DB_SERVER");
        String port = requireEnv("DB_PORT");
        String dbName = requireEnv("DB_NAME");
        String user = requireEnv("DB_USER");
        String password = readPasswordFromEnv();

        String jdbcUrl = "jdbc:mysql://" + server + ":" + port + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setPoolName("KanbanHikariPool");

        dataSource = new HikariDataSource(config);
    }

    private static String requireEnv(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required environment variable: " + key);
        }
        return value;
    }

    private static String readPasswordFromEnv() {
        String passwordFilePath = System.getenv("DB_PASSWORD_FILE");
        if (passwordFilePath != null && !passwordFilePath.trim().isEmpty()) {
            try {
                return Files.readString(Paths.get(passwordFilePath)).trim();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read password file: " + passwordFilePath, e);
            }
        }
        String plainPassword = System.getenv("DB_PASS");
        if (plainPassword != null && !plainPassword.trim().isEmpty()) {
            return plainPassword;
        }
        throw new IllegalStateException("Neither DB_PASSWORD_FILE nor DB_PASS is set");
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}