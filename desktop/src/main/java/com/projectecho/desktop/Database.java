package com.projectecho.desktop;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

            stmt.execute("CREATE TABLE IF NOT EXISTS mentions (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                         "content TEXT NOT NULL," +
                         "source TEXT NOT NULL," +
                         "url TEXT NOT NULL UNIQUE," +
                         "foundAt TEXT NOT NULL" +
                         ");");

            // Robustly check for and add the sentiment column
            if (!columnExists(conn, "mentions", "sentiment")) {
                stmt.execute("ALTER TABLE mentions ADD COLUMN sentiment TEXT");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }
}