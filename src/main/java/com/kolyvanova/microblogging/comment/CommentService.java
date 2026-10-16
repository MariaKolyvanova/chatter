package com.kolyvanova.microblogging.comment;

import com.kolyvanova.microblogging.exception.CommentNotFoundException;
import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.Post;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentReadDto create(CommentCreateDto dto) {
        User user = userRepository.findById(dto.authorId())
                .orElseThrow(() -> new UserNotFoundException(dto.authorId()));

        Post post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new PostNotFoundException(dto.postId()));

        Comment comment = commentMapper.toEntity(dto);
        comment.setAuthor(user);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        return commentMapper.toReadDto(savedComment);
    }

    @Transactional(readOnly = true)
    public CommentReadDto findById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        return commentMapper.toReadDto(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentReadDto> findAllByPostId(UUID id) {
        List<Comment> comments = commentRepository.findAllByPostId(id);

        return comments.stream()
                .map(commentMapper::toReadDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentReadDto> findAllByAuthorId(UUID id) {
        List<Comment> comments = commentRepository.findAllByAuthorId(id);

        return comments.stream()
                .map(commentMapper::toReadDto)
                .toList();
    }

    @Transactional
    public CommentReadDto update(UUID id, CommentUpdateDto dto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        commentMapper.updateEntity(dto, comment);
        commentRepository.save(comment);

        return commentMapper.toReadDto(comment);
    }

    @Transactional
    public void delete(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        commentRepository.delete(comment);
    }
}
