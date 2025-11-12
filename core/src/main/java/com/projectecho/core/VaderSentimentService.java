package com.projectecho.core;

// Temporarily disabling VADER imports for diagnostics
// import com.vader.sentiment.analyzer.SentimentAnalyzer;
// import com.vader.sentiment.analyzer.SentimentPolarities;

public class VaderSentimentService implements SentimentService {

    @Override
    public Mention.Sentiment analyze(String text) {
        if (text == null || text.isEmpty()) {
            return Mention.Sentiment.NEUTRAL;
        }

        // --- Temporary Diagnostic Logic ---
        // This will help us confirm that the sentiment saving/displaying mechanism works.
        String lowerText = text.toLowerCase();
        if (lowerText.contains("good") || lowerText.contains("great") || lowerText.contains("excellent") || lowerText.contains("love")) {
            return Mention.Sentiment.POSITIVE;
        } else if (lowerText.contains("bad") || lowerText.contains("terrible") || lowerText.contains("awful") || lowerText.contains("hate")) {
            return Mention.Sentiment.NEGATIVE;
        } else {
            return Mention.Sentiment.NEUTRAL;
        }
    }
}