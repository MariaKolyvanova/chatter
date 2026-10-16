package com.kolyvanova.microblogging.like;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeRestController {

    private final LikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeReadDto create(@Valid @RequestBody LikeCreateDto dto) {
        return likeService.create(dto);
    }

    @GetMapping
    public List<LikeReadDto> findAll() {
        return likeService.findAll();
    }

    @GetMapping("/{id}")
    public LikeReadDto findById(@PathVariable UUID id) {
        return likeService.findById(id);
    }

    @GetMapping(params = "postId")
    public List<LikeReadDto> findAllByPostId(@RequestParam UUID postId) {
        return likeService.findAllByPostId(postId);
    }

    @GetMapping(params = "userId")
    public List<LikeReadDto> findAllByUserId(@RequestParam UUID userId) {
        return likeService.findAllByUserId(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        likeService.delete(id);
    }
}
