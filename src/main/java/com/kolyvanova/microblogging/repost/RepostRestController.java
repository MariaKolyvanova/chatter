package com.kolyvanova.microblogging.repost;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api/v1/reposts")
public class RepostRestController {

    private final RepostService repostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepostReadDto create(@Valid @RequestBody RepostCreateDto dto) {
        return repostService.create(dto);
    }

    @GetMapping
    public Page<RepostReadDto> findAll(@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public RepostReadDto findById(@PathVariable UUID id) {
        return repostService.findById(id);
    }

    @GetMapping(params = "postId")
    public Page<RepostReadDto> findAllByPostId(@RequestParam UUID postId,
                                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAllByPostId(postId, pageable);
    }

    @GetMapping(params = "userId")
    public Page<RepostReadDto> findAllByUserId(@RequestParam UUID userId,
                                               @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        return repostService.findAllByUserId(userId, pageable);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        repostService.delete(id);
    }
}
