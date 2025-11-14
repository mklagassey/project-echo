package com.projectecho.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Mention {

    public enum Sentiment {
        POSITIVE, NEUTRAL, NEGATIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String source;
    private String url;
    private Instant foundAt;
    private Instant authoredAt; // New field for the creation date
    private Sentiment sentiment;

    public Mention() {
    }

    // Updated constructor
    public Mention(String content, String source, String url, Instant authoredAt) {
        this.content = content;
        this.source = source;
        this.url = url;
        this.authoredAt = authoredAt;
        this.foundAt = Instant.now(); // The "found" date is always now
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getFoundAt() {
        return foundAt;
    }

    public void setFoundAt(Instant foundAt) {
        this.foundAt = foundAt;
    }

    public Instant getAuthoredAt() {
        return authoredAt;
    }

    public void setAuthoredAt(Instant authoredAt) {
        this.authoredAt = authoredAt;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }
}