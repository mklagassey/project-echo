package com.projectecho.core;

import java.util.List;

public interface MentionDao {
    void save(Mention mention);
    boolean existsByUrl(String url);
    List<Mention> findAll();
    void deleteAll(); // New method
}