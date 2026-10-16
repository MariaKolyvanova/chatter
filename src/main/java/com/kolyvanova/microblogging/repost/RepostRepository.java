package com.kolyvanova.microblogging.repost;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RepostRepository extends JpaRepository<Repost, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    List<Repost> findAllByPostId(UUID postId);

    List<Repost> findAllByUserId(UUID userId);
}
