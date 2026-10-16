package com.kolyvanova.microblogging.repost;

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
@RequestMapping("/api/v1/reposts")
public class RepostRestController {

    private final RepostService repostService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RepostReadDto create(@Valid @RequestBody RepostCreateDto dto) {
        return repostService.create(dto);
    }

    @GetMapping
    public List<RepostReadDto> findAll() {
        return repostService.findAll();
    }

    @GetMapping("/{id}")
    public RepostReadDto findById(@PathVariable UUID id) {
        return repostService.findById(id);
    }

    @GetMapping(params = "postId")
    public List<RepostReadDto> findAllByPostId(@RequestParam UUID postId) {
        return repostService.findAllByPostId(postId);
    }

    @GetMapping(params = "userId")
    public List<RepostReadDto> findAllByUserId(@RequestParam UUID userId) {
        return repostService.findAllByUserId(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        repostService.delete(id);
    }
}
