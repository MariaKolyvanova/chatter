package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;

    @Transactional
    public PostReadDto create(PostCreateDto dto) {
        User author = userRepository.findById(dto.authorId())
                .orElseThrow(() -> new UserNotFoundException(dto.authorId()));

        Post post = postMapper.toEntity(dto);
        post.setAuthor(author);
        post.setStatus(PostStatus.PUBLISHED);

        Post savedPost = postRepository.save(post);

        return postMapper.toReadDto(savedPost);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAll(Pageable pageable) {
        return postRepository.findAll(pageable).map(postMapper::toReadDto);
    }

    @Transactional(readOnly = true)
    public PostReadDto findById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        return postMapper.toReadDto(post);
    }

    @Transactional(readOnly = true)
    public Page<PostReadDto> findAllByAuthorId(UUID id, Pageable pageable) {
        return postRepository.findAllByAuthorId(id, pageable).map(postMapper::toReadDto);
    }

    @Transactional
    public PostReadDto update(UUID id, UUID authorId, PostUpdateDto dto) {
        Post post = postRepository.findByIdAndAuthorId(id, authorId)
                .orElseThrow(() -> new PostNotFoundException(id));
        //или? .orElseThrow(() -> new AccessDeniedException("not the author"));

        postMapper.updateEntity(dto, post);
        postRepository.save(post);

        return postMapper.toReadDto(post);
    }

    @Transactional
    public void delete(UUID id, UUID authorId) {
        Post post = postRepository.findByIdAndAuthorId(id,authorId)
                .orElseThrow(() -> new PostNotFoundException(id));
        //или? .orElseThrow(() -> new AccessDeniedException("not the author"));

        postRepository.delete(post);
    }

    @PreAuthorize("hasRole('MODERATOR' || 'ADMIN')")
    public void hide(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        post.setStatus(PostStatus.HIDDEN);
        postRepository.save(post);
    }

}
