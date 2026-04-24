package com.casestudy.kanban.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }

        String server = requireEnv("DB_SERVER");
        String port = requireEnv("DB_PORT");
        String dbName = requireEnv("DB_NAME");
        String user = requireEnv("DB_USER");
        String password = requireEnv("DB_PASSWORD");

        String jdbcUrl = "jdbc:postgresql://" + server + ":" + port + "/" + dbName
                + "?ssl=true&sslmode=require";

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
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

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}