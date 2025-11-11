package com.projectecho.server;

import com.projectecho.core.Mention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionRepository extends JpaRepository<Mention, Long> {
    boolean existsByUrl(String url);
}