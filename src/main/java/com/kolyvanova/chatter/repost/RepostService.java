package com.kolyvanova.chatter.repost;

import com.kolyvanova.chatter.exception.PostNotFoundException;
import com.kolyvanova.chatter.exception.RepostAlreadyExistsException;
import com.kolyvanova.chatter.exception.RepostNotFoundException;
import com.kolyvanova.chatter.exception.UserNotFoundException;
import com.kolyvanova.chatter.post.Post;
import com.kolyvanova.chatter.post.PostRepository;
import com.kolyvanova.chatter.post.PostStatus;
import com.kolyvanova.chatter.user.User;
import com.kolyvanova.chatter.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class RepostService {

    private final RepostRepository repostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RepostMapper repostMapper;

    @Transactional
    public RepostReadDto create(UUID postId, UUID userId) {
        Post post = postRepository.findByIdAndStatus(postId, PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostNotFoundException());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        if (repostRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new RepostAlreadyExistsException();
        }

        Repost savedRepost = repostRepository.save(Repost.builder()
                .post(post)
                .user(user)
                .build());
        log.info("Created repost {} by user {} on post {}", savedRepost.getId(), userId, postId);

        return repostMapper.toReadDto(savedRepost);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAll(Pageable pageable) {
        return repostRepository.findAllByPostStatus(PostStatus.PUBLISHED, pageable)
                .map(repostMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public RepostReadDto findById(UUID id, UUID viewerId, boolean moderator) {
        Repost repost = repostRepository.findById(id)
                .orElseThrow(() -> new RepostNotFoundException());
        if (!isVisible(repost.getPost(), viewerId, moderator)) {
            throw new RepostNotFoundException();
        }

        return repostMapper.toReadDto(repost);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAllByPostId(UUID postId, UUID viewerId, boolean moderator, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException());
        if (!isVisible(post, viewerId, moderator)) {
            throw new PostNotFoundException();
        }

        return repostRepository.findAllByPostId(postId, pageable).map(repostMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAllByUserId(UUID userId, Pageable pageable) {
        return repostRepository.findAllByUserIdAndPostStatus(userId, PostStatus.PUBLISHED, pageable)
                .map(repostMapper::toReadDto);
    }

    private boolean isVisible(Post post, UUID viewerId, boolean moderator) {
        return post.getStatus() == PostStatus.PUBLISHED
                || post.getAuthor().getId().equals(viewerId)
                || moderator;
    }

    @Transactional
    public void delete(UUID id, UUID userId) {
        Repost repost = repostRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RepostNotFoundException());

        repostRepository.delete(repost);
        log.info("Deleted repost {} by user {}", id, userId);
    }
}
