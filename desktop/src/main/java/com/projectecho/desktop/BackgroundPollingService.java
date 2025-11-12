package com.projectecho.desktop;

import com.projectecho.core.HackerNewsSource;
import com.projectecho.core.Keyword;
import com.projectecho.core.Mention;
import com.projectecho.core.RedditSource;
import com.projectecho.core.Source;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class BackgroundPollingService {

    private final SqliteKeywordDao keywordDao;
    private final SqliteMentionDao mentionDao;
    private final MainController mainController;
    private final List<Source> sources;
    private final ScheduledExecutorService scheduler;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public BackgroundPollingService(MainController mainController, SqliteKeywordDao keywordDao, SqliteMentionDao mentionDao) {
        this.mainController = mainController;
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
        this.sources = List.of(new HackerNewsSource(), new RedditSource("saas"));
        
        ThreadFactory daemonThreadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);

        // SystemTray functionality is temporarily disabled to prevent startup hangs.
        // setupSystemTray(); 
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::poll, 0, 30, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Scheduler did not terminate.");
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void poll() {
        mainController.updateStatus("Starting poll at " + TIME_FORMATTER.format(LocalDateTime.now()) + "...");
        List<Keyword> keywords = keywordDao.findAll();
        if (keywords.isEmpty()) {
            mainController.updateStatus("No keywords to search. Add a keyword to begin.");
            return;
        }

        List<Mention> allFoundMentions = new ArrayList<>();
        for (Keyword keyword : keywords) {
            for (Source source : sources) {
                allFoundMentions.addAll(source.findMentions(keyword));
            }
        }
        
        mainController.updateStatus("Found " + allFoundMentions.size() + " potential mentions. Filtering for new ones...");

        List<Mention> newMentions = new ArrayList<>();
        for (Mention mention : allFoundMentions) {
            if (!mentionDao.existsByUrl(mention.getUrl())) {
                mentionDao.save(mention);
                newMentions.add(mention);
            }
        }

        if (!newMentions.isEmpty()) {
            mainController.addMentions(newMentions);
            // showNotification(newMentions.size()); // Disabled
        }
        
        String result = String.format("Poll complete. Found %d new mentions. Last poll: %s", newMentions.size(), TIME_FORMATTER.format(LocalDateTime.now()));
        mainController.updateStatus(result);
    }

    // All SystemTray and notification methods are temporarily disabled.
    /*
    private void setupSystemTray() {
        if (SystemTray.isSupported()) {
            // ...
        }
    }

    private void showNotification(int mentionCount) {
        if (trayIcon != null) {
            // ...
        }
    }
    */
}