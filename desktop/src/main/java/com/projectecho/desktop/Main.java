package com.projectecho.desktop;

import javafx.application.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) {
        // Set a global exception handler to catch any uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logError(throwable);
        });

        try {
            Application.launch(EchoApplication.class, args);
        } catch (Throwable t) {
            // This will catch errors during the initial launch phase
            logError(t);
        }
    }

    private static void logError(Throwable throwable) {
        try {
            File errorLog = new File(System.getProperty("user.home"), "project-echo-error.log");
            try (PrintStream ps = new PrintStream(errorLog)) {
                throwable.printStackTrace(ps);
            }
        } catch (FileNotFoundException e) {
            // If logging to the file fails, there's not much more we can do.
            e.printStackTrace();
        }
    }
}