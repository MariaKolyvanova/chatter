package com.kolyvanova.chatter.like;

import com.kolyvanova.chatter.exception.LikeAlreadyExistsException;
import com.kolyvanova.chatter.exception.LikeNotFoundException;
import com.kolyvanova.chatter.exception.PostNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.post.Post;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.post.PostStatus;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeMapper likeMapper;

    public LikeReadDto create(UUID postId, UUID userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostNotFoundException(postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new LikeAlreadyExistsException();
        }

        Like savedLike = likeRepository.save(Like.builder()
                .post(post)
                .user(user)
                .build());
        log.info("Created like {} by user {} on post {}", savedLike.getId(), userId, postId);

        return likeMapper.toReadDto(savedLike);
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAll(Pageable pageable) {
        return likeRepository.findAllByPostStatus(PostStatus.PUBLISHED, pageable)
                .map(likeMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public LikeReadDto findById(UUID id, UUID viewerId, boolean moderator) {
        Like like = likeRepository.findById(id)
                .orElseThrow(() -> new LikeNotFoundException(id));
        if (!isVisible(like.getPost(), viewerId, moderator)) {
            throw new LikeNotFoundException(id);
        }

        return likeMapper.toReadDto(like);
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAllByPostId(UUID postId, UUID viewerId, boolean moderator, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        if (!isVisible(post, viewerId, moderator)) {
            throw new PostNotFoundException(postId);
        }

        return likeRepository.findAllByPostId(postId, pageable)
                .map(likeMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAllByUserId(UUID userId, Pageable pageable) {
        return likeRepository.findAllByUserIdAndPostStatus(userId, PostStatus.PUBLISHED, pageable)
                .map(likeMapper::toReadDto);
    }

    private boolean isVisible(Post post, UUID viewerId, boolean moderator) {
        return post.getStatus() == PostStatus.PUBLISHED
                || post.getAuthor().getId().equals(viewerId)
                || moderator;
    }

    public void delete(UUID userId, UUID postId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new LikeNotFoundException(userId, postId));

        likeRepository.delete(like);
        log.info("Deleted like by user {} on post {}", userId, postId);
    }
}
