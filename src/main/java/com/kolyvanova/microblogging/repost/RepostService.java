package com.kolyvanova.microblogging.repost;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.RepostAlreadyExistsException;
import com.kolyvanova.microblogging.exception.RepostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.Post;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.post.PostStatus;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
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
    public RepostReadDto create(RepostCreateDto dto, UUID userId) {
        Post post = postRepository.findByIdAndStatus(dto.postId(), PostStatus.PUBLISHED)
                .orElseThrow(() -> new PostNotFoundException(dto.postId()));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (repostRepository.existsByPostIdAndUserId(dto.postId(), userId)) {
            throw new RepostAlreadyExistsException();
        }

        Repost repost = repostMapper.toEntity(dto);
        repost.setPost(post);
        repost.setUser(user);
        Repost savedRepost = repostRepository.save(repost);
        log.info("Created repost {} by user {} on post {}", savedRepost.getId(), userId, dto.postId());

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
                .orElseThrow(() -> new RepostNotFoundException(id));
        if (!isVisible(repost.getPost(), viewerId, moderator)) {
            throw new RepostNotFoundException(id);
        }

        return repostMapper.toReadDto(repost);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAllByPostId(UUID postId, UUID viewerId, boolean moderator, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        if (!isVisible(post, viewerId, moderator)) {
            throw new PostNotFoundException(postId);
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
                .orElseThrow(() -> new RepostNotFoundException(id));

        repostRepository.delete(repost);
        log.info("Deleted repost {} by user {}", id, userId);
    }
}
