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

    private final String apiUrl;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // The constructor now accepts the URL directly, removing any dependency on other modules.
    public HackerNewsSource(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    @Override
    public List<Mention> findMentions(Keyword keyword) {
        List<Mention> mentions = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode("\"" + keyword.getPhrase() + "\"", StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl + encodedKeyword))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONArray hits = jsonResponse.getJSONArray("hits");

            for (int i = 0; i < hits.length(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                String title = hit.optString("title", null);
                String url = hit.optString("url", null);
                String content = hit.optString("story_text", title);

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