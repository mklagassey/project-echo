package com.projectecho.core;

import java.util.List;

/**
 * Represents a source to be monitored for keyword mentions, e.g., Hacker News or a specific subreddit.
 */
public interface Source {
    /**
     * Finds all mentions of a given keyword from this source.
     *
     * @param keyword The keyword to search for.
     * @return A list of mentions found.
     */
    List<Mention> findMentions(Keyword keyword);
}