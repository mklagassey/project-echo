package com.projectecho.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RedditSource implements Source {

    private static final String API_URL_TEMPLATE = "https://www.reddit.com/r/%s/search.json?q=%s&restrict_sr=on";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String subreddit;

    public RedditSource(String subreddit) {
        this.subreddit = subreddit;
    }

    @Override
    public List<Mention> findMentions(Keyword keyword) {
        List<Mention> mentions = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode("\"" + keyword.getPhrase() + "\"", StandardCharsets.UTF_8);
            String apiUrl = String.format(API_URL_TEMPLATE, subreddit, encodedKeyword);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("User-Agent", "ProjectEcho/1.0")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray children = jsonResponse.getJSONObject("data").getJSONArray("children");

            for (int i = 0; i < children.length(); i++) {
                JSONObject post = children.getJSONObject(i).getJSONObject("data");
                String title = post.optString("title", "");
                String selftext = post.optString("selftext", "");
                String url = "https://www.reddit.com" + post.optString("permalink", "");
                String content = title + "\n" + selftext;
                long createdAt = post.optLong("created_utc");

                if (createdAt > 0) {
                    Instant authoredAt = Instant.ofEpochSecond(createdAt);
                    mentions.add(new Mention(content.trim(), "Reddit r/" + subreddit, url, authoredAt));
                }
            }
        } catch (Exception e) {
            logError(e);
        }
        return mentions;
    }

    private void logError(Throwable t) {
        try {
            File errorLog = new File(System.getProperty("user.home"), "project-echo-error.log");
            try (PrintStream ps = new PrintStream(errorLog)) {
                t.printStackTrace(ps);
            }
        } catch (Exception e) {
            // If logging fails, do nothing.
        }
    }
}