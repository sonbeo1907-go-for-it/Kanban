package com.casestudy.kanban.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBContext {
    private final String serverName = getEnvOrDefault("DB_SERVER", "localhost");
    private final String dbName = getEnvOrDefault("DB_NAME", "kanban");
    private final String portNumber = getEnvOrDefault("DB_PORT", "3306");
    private final String userID = getEnvOrDefault("DB_USER", "root");
    private final String password = getEnvOrDefault("DB_PASS", "rootpassword");


    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    public Connection getConnection() throws Exception {
        String url = "jdbc:mysql://" + serverName + ":" + portNumber + "/" + dbName 
                     + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, userID, password);
    }

    public static void main(String[] args) {
        try {
            DBContext db = new DBContext();
            Connection conn = db.getConnection();
            if (conn != null) {
                System.out.println("--- KẾT NỐI DATABASE THÀNH CÔNG ---");
                System.out.println("URL: " + conn.getMetaData().getURL());
                System.out.println("User: " + conn.getMetaData().getUserName());
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("!!! LỖI KẾT NỐI: " + e.getMessage());
            e.printStackTrace();
        }
    }
}