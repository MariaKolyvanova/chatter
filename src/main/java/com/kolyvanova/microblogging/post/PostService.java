package com.kolyvanova.microblogging.post;

import com.kolyvanova.microblogging.exception.PostNotFoundException;
import com.kolyvanova.microblogging.exception.UserNotFoundException;
import com.kolyvanova.microblogging.user.User;
import com.kolyvanova.microblogging.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public PostReadDto findById(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        return postMapper.toReadDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostReadDto> findAll() {
        List<Post> posts = postRepository.findAll();

        return posts.stream()
                .map(postMapper::toReadDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostReadDto> findAllByAuthorId(UUID id) {
        List<Post> posts = postRepository.findAllByAuthor_Id(id);

        return posts.stream()
                .map(postMapper::toReadDto)
                .toList();
    }

    @Transactional
    public PostReadDto update(UUID id, PostUpdateDto dto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        postMapper.updateEntity(dto, post);
        postRepository.save(post);

        return postMapper.toReadDto(post);
    }

    @Transactional
    public void delete(UUID id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        postRepository.delete(post);
    }
}
