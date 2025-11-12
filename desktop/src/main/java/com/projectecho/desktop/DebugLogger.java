package com.projectecho.desktop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DebugLogger {
    private static final String LOG_FILE = "project-echo-debug.log";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public static synchronized void log(String message) {
        try {
            File logFile = new File(System.getProperty("user.home"), LOG_FILE);
            try (PrintWriter out = new PrintWriter(new FileWriter(logFile, true))) {
                String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
                out.println(timestamp + " - " + message);
            }
        } catch (IOException e) {
            // If the logger itself fails, we can't do much.
            e.printStackTrace();
        }
    }
}