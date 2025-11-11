package com.projectecho.server;

import com.projectecho.core.Keyword;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordRepository keywordRepository;

    public KeywordController(KeywordRepository keywordRepository) {
        this.keywordRepository = keywordRepository;
    }

    @GetMapping
    public List<Keyword> getKeywords() {
        // TODO: Add user authentication and scoping
        return keywordRepository.findAll();
    }

    @PostMapping
    public Keyword createKeyword(@RequestBody Keyword keyword) {
        // TODO: Add user authentication and scoping
        return keywordRepository.save(keyword);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyword(@PathVariable Long id) {
        // TODO: Add user authentication and scoping
        keywordRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}