package com.kolyvanova.microblogging.like;

import com.kolyvanova.microblogging.security.CurrentUser;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "likes")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeRestController {

    private final LikeService likeService;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeReadDto create(@RequestParam UUID postId) {
        return likeService.create(postId, currentUser.id());
    }

    @GetMapping
    public Page<LikeReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                         Pageable pageable) {
        return likeService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public LikeReadDto findById(@PathVariable UUID id) {
        return likeService.findById(id, currentUser.id(), currentUser.canModerate());
    }

    @GetMapping(params = "postId")
    public Page<LikeReadDto> findAllByPostId(@RequestParam UUID postId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByPostId(postId, currentUser.id(), currentUser.canModerate(), pageable);
    }

    @GetMapping(params = "userId")
    public Page<LikeReadDto> findAllByUserId(@RequestParam UUID userId,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC)
                                             Pageable pageable) {
        return likeService.findAllByUserId(userId, pageable);
    }

    @DeleteMapping(params = "postId")
    public void delete(@RequestParam UUID postId) {
        likeService.delete(currentUser.id(), postId);
    }
}
