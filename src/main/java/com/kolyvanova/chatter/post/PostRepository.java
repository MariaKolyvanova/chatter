package com.kolyvanova.chatter.post;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorId(UUID authorId, Pageable pageable);

    Optional<Post> findByIdAndStatus(@NotNull UUID uuid, PostStatus status);

    Page<Post> findAllByStatusAndReplyToIsNull(PostStatus postStatus, Pageable pageable);

    Page<Post> findAllByStatusAndReplyToIsNullAndCategory(PostStatus postStatus, PostCategory category, Pageable pageable);

    Page<Post> findAllByReplyToIdAndStatus(UUID replyToId, PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorIdAndReplyToIsNull(UUID authorId, Pageable pageable);

    Page<Post> findAllByAuthorIdAndStatusAndReplyToIsNull(UUID authorId, PostStatus postStatus, Pageable pageable);

    Page<Post> findAllByAuthorIdAndStatusAndReplyToIsNotNull(UUID authorId, PostStatus postStatus, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.replyTo IS NULL
                AND p.status = com.kolyvanova.chatter.post.PostStatus.PUBLISHED
                AND p.author.id IN (
                    SELECT s.author.id FROM Subscription s
                    WHERE s.subscriber.id = :subscriberId
                )
            """)
    Page<Post> findFeed(@Param("subscriberId") UUID subscriberId, Pageable pageable);
}
