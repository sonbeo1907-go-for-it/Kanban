package com.casestudy.kanban.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBContext {
    private static HikariDataSource dataSource;

    static {
        reloadDataSource();
    }

    private static void reloadDataSource() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC Driver not found", e);
        }

        String jdbcUrl = System.getProperty("DB_URL");
        if (jdbcUrl == null || jdbcUrl.trim().isEmpty()) {
            String server = requireEnv("DB_SERVER");
            String port = requireEnv("DB_PORT");
            String dbName = requireEnv("DB_NAME");
            jdbcUrl = "jdbc:postgresql://" + server + ":" + port + "/" + dbName;

            if (!"localhost".equals(server)) {
                jdbcUrl += "?ssl=true&sslmode=require";
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(requireEnv("DB_USER"));
        config.setPassword(requireEnv("DB_PASSWORD"));
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setPoolName("KanbanHikariPool");

        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        dataSource = new HikariDataSource(config);
    }

    private static String requireEnv(String key) {
        String value = System.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(key);
        }
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Missing required configuration: " + key);
        }
        return value;
    }

    public static void reloadForTest() {
        reloadDataSource();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            reloadDataSource();
        }
        return dataSource.getConnection();
    }
}