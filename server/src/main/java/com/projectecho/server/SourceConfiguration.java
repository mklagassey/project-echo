package com.projectecho.server;

import com.projectecho.core.HackerNewsSource;
import com.projectecho.core.RedditSource;
import com.projectecho.core.Source;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SourceConfiguration {

    @Bean
    public List<Source> sources(
            @Value("${source.hackernews.url}") String hnApiUrl,
            @Value("${source.reddit.subreddit}") String redditSubreddit
    ) {
        // Correctly instantiate sources with required configuration
        Source hackerNews = new HackerNewsSource(hnApiUrl);
        Source reddit = new RedditSource(redditSubreddit);
        return List.of(hackerNews, reddit);
    }
}