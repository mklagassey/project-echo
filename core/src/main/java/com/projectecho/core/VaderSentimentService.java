package com.projectecho.core;

import com.vader.sentiment.analyzer.SentimentAnalyzer;
import com.vader.sentiment.analyzer.SentimentPolarities;

public class VaderSentimentService implements SentimentService {

    @Override
    public Mention.Sentiment analyze(String text) {
        if (text == null || text.isEmpty()) {
            return Mention.Sentiment.NEUTRAL;
        }

        // --- Restore VADER Sentiment Analysis Logic ---
        final SentimentPolarities sentimentPolarities = SentimentAnalyzer.getScoresFor(text);
        float compoundScore = sentimentPolarities.getCompoundPolarity();

        if (compoundScore >= 0.05) {
            return Mention.Sentiment.POSITIVE;
        } else if (compoundScore <= -0.05) {
            return Mention.Sentiment.NEGATIVE;
        } else {
            return Mention.Sentiment.NEUTRAL;
        }
    }
}