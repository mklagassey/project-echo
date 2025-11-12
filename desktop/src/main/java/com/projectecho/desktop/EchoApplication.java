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
        Database.initialize();
        SqliteKeywordDao keywordDao = new SqliteKeywordDao();
        SqliteMentionDao mentionDao = new SqliteMentionDao();
        MainController controller = new MainController(keywordDao, mentionDao);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projectecho/desktop/MainView.fxml"));
        loader.setController(controller);
        Parent root = loader.load();

        pollingService = new BackgroundPollingService(controller, keywordDao, mentionDao);
        controller.setPollingService(pollingService); // Pass service to controller
        pollingService.start();

        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Project Echo");
        stage.setScene(scene);
        
        stage.setOnCloseRequest(e -> shutdown());
        
        stage.show();
        
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