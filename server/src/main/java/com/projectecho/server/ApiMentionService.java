package com.projectecho.server;

import com.projectecho.core.Keyword;
import com.projectecho.core.Mention;
import com.projectecho.core.MentionService;
import com.projectecho.core.Source;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ApiMentionService implements MentionService {

    private final KeywordRepository keywordRepository;
    private final MentionRepository mentionRepository;
    private final List<Source> sources;

    public ApiMentionService(KeywordRepository keywordRepository, MentionRepository mentionRepository, List<Source> sources) {
        this.keywordRepository = keywordRepository;
        this.mentionRepository = mentionRepository;
        this.sources = sources;
    }

    @Override
    public List<Mention> findNewMentions(List<Keyword> keywords) {
        List<Mention> newMentions = new ArrayList<>();
        for (Keyword keyword : keywords) {
            for (Source source : sources) {
                List<Mention> mentions = source.findMentions(keyword);
                for (Mention mention : mentions) {
                    if (!mentionRepository.existsByUrl(mention.getUrl())) {
                        mentionRepository.save(mention);
                        newMentions.add(mention);
                    }
                }
            }
        }
        return newMentions;
    }
}