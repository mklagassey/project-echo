package com.projectecho.desktop;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:" + getDatabasePath();

    private static String getDatabasePath() {
        File appDir = new File(System.getProperty("user.home"), ".project-echo");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        return new File(appDir, "echo.db").getAbsolutePath();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS keywords (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "phrase TEXT NOT NULL UNIQUE" +
                         ");");

            // Add the sentiment column to the mentions table
            stmt.execute("CREATE TABLE IF NOT EXISTS mentions (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "content TEXT NOT NULL," +
                         "source TEXT NOT NULL," +
                         "url TEXT NOT NULL UNIQUE," +
                         "foundAt TEXT NOT NULL," +
                         "sentiment TEXT" + // Storing sentiment as TEXT
                         ");");
                         
            // Use ALTER TABLE to add the column if the table already exists
            if (!stmt.executeQuery("PRAGMA table_info(mentions)").toString().contains("sentiment")) {
                stmt.execute("ALTER TABLE mentions ADD COLUMN sentiment TEXT");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}