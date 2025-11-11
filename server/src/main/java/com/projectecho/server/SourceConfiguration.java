package com.projectecho.server;

import com.projectecho.core.HackerNewsSource;
import com.projectecho.core.RedditSource;
import com.projectecho.core.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SourceConfiguration {

    @Bean
    public Source hackerNewsSource() {
        return new HackerNewsSource();
    }

    @Bean
    public Source redditSaasSource() {
        return new RedditSource("saas");
    }
}