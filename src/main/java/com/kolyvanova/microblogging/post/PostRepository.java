package com.kolyvanova.microblogging.post;

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

    Page<Post> findAllByAuthorIdAndStatusAndReplyToIsNull(UUID authorId, PostStatus postStatus, Pageable pageable);

    @Query("""
            SELECT p FROM Post p
            WHERE p.replyTo IS NULL
                AND p.status = com.kolyvanova.microblogging.post.PostStatus.PUBLISHED
                AND p.author.id IN (
                    SELECT s.author.id FROM Subscription s
                    WHERE s.subscriber.id = :subscriberId
                )
            """)
    Page<Post> findFeed(@Param("subscriberId") UUID subscriberId, Pageable pageable);


    @Query("""
            SELECT p FROM Post p
            WHERE p.replyTo IS NULL
                AND p.status = com.kolyvanova.microblogging.post.PostStatus.PUBLISHED
                AND p.category IN (
                    SELECT t.category FROM TopicSubscriptions t
                    WHERE t.user.id = :userId
                )
            """)
    Page<Post> findAllByInterestsUserId(@Param("userId") UUID userId, Pageable pageable);
}
