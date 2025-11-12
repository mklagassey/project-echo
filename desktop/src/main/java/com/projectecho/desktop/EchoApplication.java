package com.projectecho.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EchoApplication extends Application {

    private BackgroundPollingService pollingService;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Initialize Database
        Database.initialize();

        // 2. Create Dependencies
        SqliteKeywordDao keywordDao = new SqliteKeywordDao();
        SqliteMentionDao mentionDao = new SqliteMentionDao();

        // 3. Create Controller and pass dependencies
        MainController controller = new MainController(keywordDao, mentionDao);

        // 4. Load FXML and set the controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectecho/desktop/MainView.fxml"));
        loader.setController(controller);
        Parent root = loader.load();

        // 5. Create and start the background service
        pollingService = new BackgroundPollingService(controller, keywordDao, mentionDao);
        pollingService.start();

        // 6. Setup and show the stage
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Project Echo");
        stage.setScene(scene);
        
        stage.setOnCloseRequest(e -> {
            shutdown();
        });
        
        stage.show();
        
        // 7. Load initial data AFTER the stage is shown
        controller.loadInitialData();
    }

    private void shutdown() {
        if (pollingService != null) {
            pollingService.stop();
        }
        Platform.exit();
        System.exit(0);
    }

    @Override
    public void stop() {
        shutdown();
    }
}