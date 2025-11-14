package com.projectecho.desktop;

import com.projectecho.core.KeywordDao;
import com.projectecho.core.MentionDao;
import com.projectecho.core.SentimentService;
import com.projectecho.core.VaderSentimentService;

public class AppContext {

    private final ConfigurationService configService = new ConfigurationService();
    private final KeywordDao keywordDao = new SqliteKeywordDao();
    private final MentionDao mentionDao = new SqliteMentionDao();
    private final SentimentService sentimentService = new VaderSentimentService();
    private final BackgroundPollingService pollingService;
    private final MainController mainController;

    public AppContext() {
        this.mainController = new MainController(keywordDao, mentionDao);
        
        this.pollingService = new BackgroundPollingService(keywordDao, mentionDao, sentimentService, configService);
        
        this.pollingService.addListener(mainController);
        
        // The controller now starts the service, so this call is no longer needed.
        // this.mainController.setPollingService(pollingService); 
    }

    public MainController getMainController() {
        return mainController;
    }

    public BackgroundPollingService getPollingService() {
        return pollingService;
    }
}