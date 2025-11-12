package com.projectecho.desktop;

import com.projectecho.core.*;
import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BackgroundPollingService {

    private final SqliteKeywordDao keywordDao;
    private final SqliteMentionDao mentionDao;
    private final MainController mainController;
    private final List<Source> sources;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private volatile long pollingIntervalMinutes = 30; // Default

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
    }

    public void start() {
        reschedule();
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public void pollNow() {
        // Run the poll task in a new background thread to not block the UI
        scheduler.execute(this::poll);
    }

    public void setPollingInterval(long minutes) {
        this.pollingIntervalMinutes = minutes;
        reschedule();
    }

    private void reschedule() {
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
        scheduledTask = scheduler.scheduleAtFixedRate(this::poll, 0, pollingIntervalMinutes, TimeUnit.MINUTES);
    }

    private void poll() {
        mainController.updateStatus("Polling for mentions...");
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
                mention.setSentiment(new VaderSentimentService().analyze(mention.getContent()));
                mentionDao.save(mention);
                newMentions.add(mention);
            }
        }

        if (!newMentions.isEmpty()) {
            mainController.addMentions(newMentions);
        }
        
        String result = String.format("Poll complete. Found %d new mentions. Last poll: %s", newMentions.size(), TIME_FORMATTER.format(LocalDateTime.now()));
        mainController.updateStatus(result);
    }
}