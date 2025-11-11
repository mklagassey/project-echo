package com.projectecho.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Mention {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String source;
    private String url;
    private Instant foundAt;

    public Mention() {
    }

    public Mention(String content, String source, String url) {
        this.content = content;
        this.source = source;
        this.url = url;
        this.foundAt = Instant.now();
    }

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
}