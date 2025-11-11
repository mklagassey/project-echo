package com.projectecho.core;

import java.util.List;

public interface MentionService {
    /**
     * Finds all mentions for the given keywords.
     *
     * @param keywords The keywords to search for.
     * @return A list of new mentions found.
     */
    List<Mention> findNewMentions(List<Keyword> keywords);
}