package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.TagRequestDto;
import com.tarento.notesapp.dto.TagResponseDto;
import com.tarento.notesapp.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(@Valid @RequestBody TagRequestDto request) {
        TagResponseDto created = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTag(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TagResponseDto>> listTagsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(tagService.listTagsByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDto request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }
}
