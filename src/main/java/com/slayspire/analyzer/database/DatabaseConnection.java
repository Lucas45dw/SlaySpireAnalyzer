package com.slayspire.analyzer.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String path = findDatabase();
            if (path == null) {
                throw new SQLException("Cannot find data.db. Looked in: " +
                        "current dir, ../../data.db, and db.path system property");
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        }
        return connection;
    }

    private static String findDatabase() {
        String propPath = System.getProperty("db.path");
        if (propPath != null && new File(propPath).exists()) {
            return propPath;
        }
        String[] candidates = {
            "data.db",
            "SlaySpireAnalyzer/data.db",
            "../SlaySpireAnalyzer/data.db",
            "../../SlaySpireAnalyzer/data.db"
        };
        for (String c : candidates) {
            if (new File(c).exists()) {
                return c;
            }
        }
        return null;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
