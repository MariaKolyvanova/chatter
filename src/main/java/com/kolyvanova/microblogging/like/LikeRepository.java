package com.kolyvanova.microblogging.like;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    List<Like> findAllByPostId(UUID postId);

    List<Like> findAllByUserId(UUID userId);
}
