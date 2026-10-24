package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.post.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeRepository extends JpaRepository<Like, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    Page<Like> findAllByPostId(UUID postId, Pageable pageable);

    Page<Like> findAllByPostStatus(PostStatus status, Pageable pageable);

    Page<Like> findAllByUserIdAndPostStatus(UUID userId, PostStatus status, Pageable pageable);

    Optional<Like> findByUserIdAndPostId(UUID userId, UUID postId);
}
