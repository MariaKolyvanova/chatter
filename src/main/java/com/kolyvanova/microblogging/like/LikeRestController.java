package com.kolyvanova.microblogging.like;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
    public Page<LikeReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                         Pageable pageable) {
        return likeService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public LikeReadDto findById(@PathVariable UUID id) {
        return likeService.findById(id);
    }

    @GetMapping(params = "postId")
    public Page<LikeReadDto> findAllByPostId(@RequestParam UUID postId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByPostId(postId, pageable);
    }

    @GetMapping(params = "userId")
    public Page<LikeReadDto> findAllByUserId(@RequestParam UUID userId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByUserId(userId, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        likeService.delete(id);
    }
}
