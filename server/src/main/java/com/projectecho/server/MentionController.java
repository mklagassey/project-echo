package com.projectecho.server;

import com.projectecho.core.Mention;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentions")
public class MentionController {

    private final MentionRepository mentionRepository;

    public MentionController(MentionRepository mentionRepository) {
        this.mentionRepository = mentionRepository;
    }

    @GetMapping
    public Page<Mention> getMentions(Pageable pageable) {
        // TODO: Add user authentication and scoping
        return mentionRepository.findAll(pageable);
    }
}