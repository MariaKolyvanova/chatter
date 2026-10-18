package com.kolyvanova.microblogging.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/comments")
public class CommentRestController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentReadDto create(@Valid @RequestBody CommentCreateDto dto) {
        return commentService.create(dto);
    }

    @GetMapping("/{id}")
    public CommentReadDto findById(@PathVariable UUID id) {
        return commentService.findById(id);
    }

    @GetMapping
    public Page<CommentReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                            Pageable pageable) {
        return commentService.findAll(pageable);
    }

    @GetMapping(params = {"postId", "!authorId"})
    public Page<CommentReadDto> findAllByPostId(@RequestParam UUID postId,
                                                @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                                Pageable pageable) {
        return commentService.findAllByPostId(postId, pageable);
    }

    @GetMapping(params = {"authorId", "!postId"})
    public Page<CommentReadDto> findAllByAuthorId(@RequestParam UUID authorId,
                                                  @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                                  Pageable pageable) {
        return commentService.findAllByAuthorId(authorId, pageable);
    }

    @PutMapping("/{id}")
    public CommentReadDto update(@PathVariable UUID id, @Valid @RequestBody CommentUpdateDto dto) {
        return commentService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        commentService.delete(id);
    }
}
