package com.projectecho.server;

import com.projectecho.core.Keyword;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MentionPollingService {

    private final ApiMentionService mentionService;
    private final KeywordRepository keywordRepository;

    public MentionPollingService(ApiMentionService mentionService, KeywordRepository keywordRepository) {
        this.mentionService = mentionService;
        this.keywordRepository = keywordRepository;
    }

    @Scheduled(fixedRate = 1800000) // 30 minutes
    public void pollForMentions() {
        List<Keyword> keywords = keywordRepository.findAll();
        mentionService.findNewMentions(keywords);
    }
}