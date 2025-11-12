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
import java.util.ArrayList;
import java.util.List;

public class HackerNewsSource implements Source {

    // Ensure we are using HTTPS
    private static final String API_URL = "https://hn.algolia.com/api/v1/search?query=";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<Mention> findMentions(Keyword keyword) {
        List<Mention> mentions = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode("\"" + keyword.getPhrase() + "\"", StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + encodedKeyword))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray hits = jsonResponse.getJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                String title = hit.optString("title", null);
                String url = hit.optString("url", null);
                String content = hit.optString("story_text", title); // Fallback to title

                if (content != null && url != null) {
                    mentions.add(new Mention(content, "Hacker News", url));
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