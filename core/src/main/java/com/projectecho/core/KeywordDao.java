package com.projectecho.core;

import java.util.List;

public interface KeywordDao {
    void save(Keyword keyword);
    void delete(Keyword keyword);
    List<Keyword> findAll();
    void deleteAll(); // New method
}