package com.kolyvanova.microblogging.post;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByStatus(PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorIdAndStatus(UUID authorId, PostStatus status, Pageable pageable);

    Page<Post> findAllByAuthorId(UUID authorId, Pageable pageable);

    Optional<Post> findByIdAndStatus(@NotNull UUID uuid, PostStatus status);

    Page<Post> findAllByStatusAndReplyToIsNull(PostStatus postStatus, Pageable pageable);

    Page<Post> findAllByAuthorIdAndStatusAndReplyToIsNull(UUID authorId, PostStatus postStatus, Pageable pageable);
}
