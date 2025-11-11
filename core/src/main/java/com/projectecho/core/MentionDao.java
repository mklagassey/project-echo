package com.projectecho.core;

import java.util.List;

public interface MentionDao {
    /**
     * Saves a mention to the database.
     * @param mention The mention to save.
     */
    void save(Mention mention);

    /**
     * Checks if a mention with the given URL already exists.
     * @param url The URL to check.
     * @return true if the mention exists, false otherwise.
     */
    boolean existsByUrl(String url);

    /**
     * Retrieves all mentions.
     * @return A list of all mentions.
     */
    List<Mention> findAll();
}