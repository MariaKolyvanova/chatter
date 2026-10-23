package com.kolyvanova.microblogging.repost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface RepostRepository extends JpaRepository<Repost, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    Page<Repost> findAllByPostId(UUID postId, Pageable pageable);

    Page<Repost> findAllByUserId(UUID userId, Pageable pageable);

    Optional<Repost> findByIdAndUserId(UUID id, UUID userId);
}
