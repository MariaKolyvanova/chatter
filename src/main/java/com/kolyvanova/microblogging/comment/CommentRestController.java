package com.kolyvanova.microblogging.comment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;
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
    public List<CommentReadDto> findAll() {
        return commentService.findAll();
    }

    @GetMapping(params = {"postId", "!authorId"})
    public List<CommentReadDto> findAllByPostId(@RequestParam("postId") UUID postId) {
        return commentService.findAllByPostId(postId);
    }

    @GetMapping(params = {"authorId", "!postId"})
    public List<CommentReadDto> findAllByAuthorId(@RequestParam("authorId") UUID authorId) {
        return commentService.findAllByAuthorId(authorId);
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
