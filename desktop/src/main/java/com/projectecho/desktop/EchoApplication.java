package com.projectecho.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class EchoApplication extends Application {

    private MainController mainController;

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectecho/desktop/MainView.fxml"));
            Parent root = loader.load();
            mainController = loader.getController();

            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Project Echo");
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> shutdown());
            stage.show();

            mainController.startServices();
            
        } catch (Throwable t) {
            logError(t);
            Platform.exit();
        }
    }

    private void shutdown() {
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() {
        shutdown();
    }

    private void logError(Throwable throwable) {
        try {
            File errorLog = new File(System.getProperty("user.home"), "project-echo-error.log");
            try (PrintStream ps = new PrintStream(errorLog)) {
                throwable.printStackTrace(ps);
            }
        } catch (FileNotFoundException e) {
            // If logging fails, do nothing.
        }
    }
}