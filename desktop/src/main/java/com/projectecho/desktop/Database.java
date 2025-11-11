package com.projectecho.desktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:echo.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create keywords table
            stmt.execute("CREATE TABLE IF NOT EXISTS keywords (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "phrase TEXT NOT NULL UNIQUE" +
                         ");");

            // Create mentions table
            stmt.execute("CREATE TABLE IF NOT EXISTS mentions (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "content TEXT NOT NULL," +
                         "source TEXT NOT NULL," +
                         "url TEXT NOT NULL UNIQUE," +
                         "foundAt TEXT NOT NULL" +
                         ");");

        } catch (SQLException e) {
            e.printStackTrace(); // Handle this more gracefully
        }
    }
}