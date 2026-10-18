package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.exception.LikeAlreadyExistsException;
import com.kolyvanova.microblogging.exception.LikeNotFoundException;
import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.Post;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeMapper likeMapper;

    @Transactional
    public LikeReadDto create(LikeCreateDto dto) {
        Post post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new PostNotFoundException(dto.postId()));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if (likeRepository.existsByPostIdAndUserId(dto.postId(), dto.userId())) {
            throw new LikeAlreadyExistsException();
        }

        Like like = likeMapper.toEntity(dto);
        like.setPost(post);
        like.setUser(user);

        return likeMapper.toReadDto(likeRepository.save(like));
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAll(Pageable pageable) {
        return likeRepository.findAll(pageable)
                .map(likeMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public LikeReadDto findById(UUID id) {
        Like like = likeRepository.findById(id)
                .orElseThrow(() -> new LikeNotFoundException(id));

        return likeMapper.toReadDto(like);
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAllByPostId(UUID postId, Pageable pageable) {
        return likeRepository.findAllByPostId(postId, pageable)
                .map(likeMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<LikeReadDto> findAllByUserId(UUID userId, Pageable pageable) {
        return likeRepository.findAllByUserId(userId, pageable)
                .map(likeMapper::toReadDto);
    }

    @Transactional
    public void delete(UUID id) {
        Like like = likeRepository.findById(id)
                .orElseThrow(() -> new LikeNotFoundException(id));

        likeRepository.delete(like);
    }
}
