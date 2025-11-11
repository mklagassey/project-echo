package com.projectecho.core;

import java.util.List;

public interface KeywordDao {
    /**
     * Saves a keyword to the database.
     * @param keyword The keyword to save.
     */
    void save(Keyword keyword);

    /**
     * Deletes a keyword from the database.
     * @param keyword The keyword to delete.
     */
    void delete(Keyword keyword);

    /**
     * Retrieves all keywords.
     * @return A list of all keywords.
     */
    List<Keyword> findAll();
}