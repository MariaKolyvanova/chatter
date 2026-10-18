package com.kolyvanova.microblogging.repost;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.RepostAlreadyExistsException;
import com.kolyvanova.microblogging.exception.RepostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.post.Post;
import com.kolyvanova.microblogging.post.PostRepository;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RepostService {

    private final RepostRepository repostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RepostMapper repostMapper;

    @Transactional
    public RepostReadDto create(RepostCreateDto dto) {
        Post post = postRepository.findById(dto.postId())
                .orElseThrow(() -> new PostNotFoundException(dto.postId()));
        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException(dto.userId()));

        if (repostRepository.existsByPostIdAndUserId(dto.postId(), dto.userId())) {
            throw new RepostAlreadyExistsException();
        }

        Repost repost = repostMapper.toEntity(dto);
        repost.setPost(post);
        repost.setUser(user);

        return repostMapper.toReadDto(repostRepository.save(repost));
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAll(Pageable pageable) {
        return repostRepository.findAll(pageable).map(repostMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public RepostReadDto findById(UUID id) {
        Repost repost = repostRepository.findById(id)
                .orElseThrow(() -> new RepostNotFoundException(id));

        return repostMapper.toReadDto(repost);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAllByPostId(UUID postId, Pageable pageable) {
        return repostRepository.findAllByPostId(postId, pageable).map(repostMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public Page<RepostReadDto> findAllByUserId(UUID userId, Pageable pageable) {
        return repostRepository.findAllByUserId(userId, pageable).map(repostMapper::toReadDto);
    }

    @Transactional
    public void delete(UUID id) {
        Repost repost = repostRepository.findById(id)
                .orElseThrow(() -> new RepostNotFoundException(id));

        repostRepository.delete(repost);
    }
}
