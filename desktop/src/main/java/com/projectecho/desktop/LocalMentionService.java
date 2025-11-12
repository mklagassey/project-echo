package com.projectecho.desktop;

import com.projectecho.core.*;

import java.util.ArrayList;
import java.util.List;

public class LocalMentionService implements MentionService {

    private final KeywordDao keywordDao;
    private final MentionDao mentionDao;
    private final List<Source> sources;
    private final SentimentService sentimentService;

    public LocalMentionService(KeywordDao keywordDao, MentionDao mentionDao, List<Source> sources) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
        this.sources = sources;
        this.sentimentService = new VaderSentimentService(); // Using the new service
    }

    @Override
    public List<Mention> findNewMentions(List<Keyword> keywords) {
        List<Mention> newMentions = new ArrayList<>();
        for (Keyword keyword : keywords) {
            for (Source source : sources) {
                List<Mention> mentions = source.findMentions(keyword);
                for (Mention mention : mentions) {
                    if (!mentionDao.existsByUrl(mention.getUrl())) {
                        // Analyze and set sentiment using the service
                        mention.setSentiment(sentimentService.analyze(mention.getContent()));
                        mentionDao.save(mention);
                        newMentions.add(mention);
                    }
                }
            }
        }
        return newMentions;
    }
}