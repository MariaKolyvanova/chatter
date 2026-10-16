package com.kolyvanova.microblogging.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByPostId(UUID id);

    List<Comment> findAllByAuthorId(UUID id);
}
