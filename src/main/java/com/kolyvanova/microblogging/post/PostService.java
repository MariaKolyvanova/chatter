package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
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

        if (dto.replyToId() == null) {
            Post savedPost = postRepository.save(post);
            savedPost.setConversation(savedPost);
            log.info("Created post {} by user {}", savedPost.getId(), userId);
            return postMapper.toReadDto(savedPost);
        }

        Post parent = postRepository.findById(dto.replyToId())
                .orElseThrow(() -> new PostNotFoundException(dto.replyToId()));
        if (!parent.getStatus().equals(PostStatus.PUBLISHED)) {
            throw new PostNotFoundException(dto.replyToId());
        }

        post.setReplyTo(parent);
        post.setConversation(parent.getConversation());
        Post savedPost = postRepository.save(post);
        log.info("Created post {} by user {} in reply to {}", savedPost.getId(), userId, dto.replyToId());

        return postMapper.toReadDto(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAll(Pageable pageable) {
        return postRepository.findAllByStatusAndReplyToIsNull(PostStatus.PUBLISHED, pageable)
                .map(postMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAllByAuthorId(UUID authorId, Pageable pageable) {
        return postRepository.findAllByAuthorIdAndStatusAndReplyToIsNull(authorId, PostStatus.PUBLISHED, pageable)
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

    @Transactional(readOnly = true)
    public Page<PostReadDto> findFeed(UUID userId, Pageable pageable) {
        return postRepository.findFeed(userId, pageable)
                .map(postMapper::toReadDto);
    }

    public PostReadDto update(UUID id, UUID authorId, PostUpdateDto dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        if (!post.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException("not the author");

        postMapper.updateEntity(dto, post);
        postRepository.save(post);
        log.info("Updated post {} by user {}", id, authorId);

        return postMapper.toReadDto(post);
    }

    public void delete(UUID id, UUID authorId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        if (!post.getAuthor().getId().equals(authorId))
            throw new AccessDeniedException("not the author");

        postRepository.delete(post);
        log.info("Deleted post {} by user {}", id, authorId);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void hide(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        post.setStatus(PostStatus.HIDDEN);
        postRepository.save(post);
        log.info("Hid post {}", id);
    }

    @PreAuthorize("hasAnyRole('MODERATOR', 'ADMIN')")
    public void show(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.setStatus(PostStatus.PUBLISHED);
        postRepository.save(post);
        log.info("Showed post {}", id);
    }

    public Page<PostReadDto> findAllByInterests(UUID userId, Pageable pageable) {
        return postRepository.findAllByInterestsUserId(userId, pageable)
                .map(postMapper::toReadDto);
    }
}