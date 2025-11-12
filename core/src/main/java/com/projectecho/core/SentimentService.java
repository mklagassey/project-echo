package com.projectecho.core;

public interface SentimentService {
    /**
     * Analyzes the given text and returns the sentiment.
     * @param text The text to analyze.
     * @return The sentiment of the text.
     */
    Mention.Sentiment analyze(String text);
}