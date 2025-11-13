package com.projectecho.desktop;

import com.projectecho.core.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class BackgroundPollingService {

    private final KeywordDao keywordDao;
    private final MentionDao mentionDao;
    private final SentimentService sentimentService;
    private final List<Source> sources;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> scheduledTask;
    private volatile long pollingIntervalMinutes;

    private final List<MentionListener> listeners = new CopyOnWriteArrayList<>();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public BackgroundPollingService(KeywordDao keywordDao, MentionDao mentionDao, SentimentService sentimentService, ConfigurationService config) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
        this.sentimentService = sentimentService;
        this.pollingIntervalMinutes = config.getIntProperty("default.polling.interval", 30);
        
        // Read configuration in the desktop module and pass simple values to the core module.
        String hnApiUrl = config.getProperty("source.hackernews.url");
        String redditSubreddit = config.getProperty("source.reddit.subreddit");
        this.sources = List.of(new HackerNewsSource(hnApiUrl), new RedditSource(redditSubreddit));
        
        ThreadFactory daemonThreadFactory = r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(daemonThreadFactory);
    }

    public void addListener(MentionListener listener) { listeners.add(listener); }
    public void removeListener(MentionListener listener) { listeners.remove(listener); }
    private void notifyListeners(Consumer<MentionListener> action) { listeners.forEach(action); }

    public void start() { reschedule(); }
    public void stop() { scheduler.shutdownNow(); }
    public void pollNow() { scheduler.execute(this::poll); }

    public void setPollingInterval(long minutes) {
        this.pollingIntervalMinutes = minutes;
        reschedule();
    }

    private void reschedule() {
        if (scheduledTask != null) scheduledTask.cancel(false);
        scheduledTask = scheduler.scheduleAtFixedRate(this::poll, 0, pollingIntervalMinutes, TimeUnit.MINUTES);
    }

    private void poll() {
        notifyListeners(l -> l.onStatusUpdate("Polling for mentions..."));
        List<Keyword> keywords = keywordDao.findAll();
        if (keywords.isEmpty()) {
            notifyListeners(l -> l.onStatusUpdate("No keywords to search. Add a keyword to begin."));
            return;
        }

        List<Mention> newMentions = new ArrayList<>();
        for (Keyword keyword : keywords) {
            for (Source source : sources) {
                for (Mention mention : source.findMentions(keyword)) {
                    if (!mentionDao.existsByUrl(mention.getUrl())) {
                        mention.setSentiment(sentimentService.analyze(mention.getContent()));
                        mentionDao.save(mention);
                        newMentions.add(mention);
                    }
                }
            }
        }
        
        if (!newMentions.isEmpty()) notifyListeners(l -> l.onMentionsFound(newMentions));
        
        String result = String.format("Poll complete. Found %d new mentions. Last poll: %s", newMentions.size(), TIME_FORMATTER.format(LocalDateTime.now()));
        notifyListeners(l -> l.onStatusUpdate(result));
    }
}