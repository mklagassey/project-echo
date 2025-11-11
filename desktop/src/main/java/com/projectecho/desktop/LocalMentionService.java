package com.projectecho.desktop;

import com.projectecho.core.Keyword;
import com.projectecho.core.KeywordDao;
import com.projectecho.core.Mention;
import com.projectecho.core.MentionDao;
import com.projectecho.core.MentionService;
import com.projectecho.core.Source;

import java.util.ArrayList;
import java.util.List;

public class LocalMentionService implements MentionService {

    private final KeywordDao keywordDao;
    private final MentionDao mentionDao;
    private final List<Source> sources;

    public LocalMentionService(KeywordDao keywordDao, MentionDao mentionDao, List<Source> sources) {
        this.keywordDao = keywordDao;
        this.mentionDao = mentionDao;
        this.sources = sources;
    }

    @Override
    public List<Mention> findNewMentions(List<Keyword> keywords) {
        List<Mention> newMentions = new ArrayList<>();
        for (Keyword keyword : keywords) {
            for (Source source : sources) {
                List<Mention> mentions = source.findMentions(keyword);
                for (Mention mention : mentions) {
                    if (!mentionDao.existsByUrl(mention.getUrl())) {
                        mentionDao.save(mention);
                        newMentions.add(mention);
                    }
                }
            }
        }
        return newMentions;
    }
}