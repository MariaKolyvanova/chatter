package com.kolyvanova.microblogging.post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByAuthorId(UUID authorId, Pageable pageable);

    Optional<Post> findByIdAndAuthorId(UUID id, UUID authorId);
}
