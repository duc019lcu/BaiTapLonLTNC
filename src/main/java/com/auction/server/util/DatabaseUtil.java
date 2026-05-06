package com.auction.server.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseUtil {
    private static DatabaseUtil instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseUtil() {
        try {
            Properties props = new Properties();
            InputStream in = getClass().getClassLoader().getResourceAsStream("config/application.properties");
            if (in != null) {
                props.load(in);
                this.url = props.getProperty("db.url");
                this.username = props.getProperty("db.username");
                this.password = props.getProperty("db.password");
            } else {
                throw new RuntimeException("Cannot find config/application.properties");
            }
            
            // Load driver explicitly (optional for newer JDBC, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DatabaseUtil", e);
        }
    }

    public static synchronized DatabaseUtil getInstance() {
        if (instance == null) {
            instance = new DatabaseUtil();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, username, password);
        }
        return connection;
    }
}
