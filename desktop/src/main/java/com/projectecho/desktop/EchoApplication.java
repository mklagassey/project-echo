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

    private AppContext appContext;

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();

            // Create the application context, which manages all dependencies.
            appContext = new AppContext();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectecho/desktop/MainView.fxml"));
            
            // Set the controller from the AppContext.
            loader.setController(appContext.getMainController());
            
            Parent root = loader.load();

            Scene scene = new Scene(root, 800, 600);
            stage.setTitle("Project Echo");
            stage.setScene(scene);

            stage.setOnCloseRequest(e -> shutdown());
            stage.show();

            // Start services after the UI is fully visible.
            appContext.getMainController().startServices();
            
        } catch (Throwable t) {
            logError(t);
            Platform.exit();
        }
    }

    private void shutdown() {
        if (appContext != null && appContext.getPollingService() != null) {
            appContext.getPollingService().stop();
        }
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