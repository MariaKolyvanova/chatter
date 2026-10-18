package com.kolyvanova.microblogging.like;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    Page<Like> findAllByPostId(UUID postId, Pageable pageable);

    Page<Like> findAllByUserId(UUID userId, Pageable pageable);
}
