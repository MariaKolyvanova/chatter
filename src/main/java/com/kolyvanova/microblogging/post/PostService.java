package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;

    public PostReadDto create(PostCreateDto dto, UUID userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Post post = postMapper.toEntity(dto);
        post.setAuthor(author);
        post.setStatus(PostStatus.PUBLISHED);

        Post savedPost = postRepository.save(post);
        savedPost.setConversation(savedPost);

        return postMapper.toReadDto(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAll(Pageable pageable) {
        return postRepository.findAllByStatus(PostStatus.PUBLISHED, pageable)
                .map(postMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAllByAuthorId(UUID authorId, Pageable pageable) {
        return postRepository.findAllByAuthorIdAndStatus(authorId, PostStatus.PUBLISHED, pageable)
                .map(postMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findMine(UUID authorId, Pageable pageable) {
        return postRepository.findAllByAuthorId(authorId, pageable)
                .map(postMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public PostReadDto findById(UUID id, UUID viewerId, boolean moderator) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        boolean published = post.getStatus() == PostStatus.PUBLISHED;
        boolean own = post.getAuthor().getId().equals(viewerId);
        if (!published && !own && !moderator) {
            throw new PostNotFoundException(id);
        }

        return postMapper.toReadDto(post);
    }

    public PostReadDto update(UUID id, UUID authorId, PostUpdateDto dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        if (!post.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException("not the author");

        postMapper.updateEntity(dto, post);
        postRepository.save(post);

        return postMapper.toReadDto(post);
    }

    public void delete(UUID id, UUID authorId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        if (!post.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException("not the author");

        postRepository.delete(post);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void hide(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        post.setStatus(PostStatus.HIDDEN);
        postRepository.save(post);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void show(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);
    }
}