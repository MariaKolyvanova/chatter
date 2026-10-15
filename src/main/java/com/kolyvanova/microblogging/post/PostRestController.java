package com.kolyvanova.microblogging.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostRestController {
    private final PostService postService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostReadDto create(@Valid @RequestBody PostCreateDto dto) {
        return postService.create(dto);
    }

    @GetMapping()
    public List<PostReadDto> findAll() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public PostReadDto findById(@PathVariable UUID id) {
        return postService.findById(id);
    }

    @PutMapping("/{id}")
    public PostReadDto update(@PathVariable UUID id, @Valid @RequestBody PostUpdateDto dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        postService.delete(id);
    }
}
