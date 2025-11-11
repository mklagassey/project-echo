package com.projectecho.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HackerNewsSource implements Source {

    private static final String API_URL = "http://hn.algolia.com/api/v1/search?query=";

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
        } catch (IOException | InterruptedException e) {
            // In a real app, log this error
            e.printStackTrace();
        }
        return mentions;
    }
}