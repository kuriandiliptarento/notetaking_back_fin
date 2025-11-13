package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.TagRequestDto;
import com.tarento.notesapp.dto.TagResponseDto;
import com.tarento.notesapp.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@Tag(name = "Tag", description = "Tag management APIs")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Create Tag", description = "Creates a new Tag for the user and returns the created Tag details.")
    @ApiResponse(responseCode = "200", description = "Tag created successfully.")
    @PostMapping
    public ResponseEntity<TagResponseDto> createTag(@Valid @RequestBody TagRequestDto request) {
        TagResponseDto created = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get Tag By ID", description = "Fetches Tag details by its ID.")
    @ApiResponse(responseCode = "200", description = "Tag details fetched successfully.")
    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDto> getTag(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.getTag(id));
    }

    @Operation(summary = "List Tags By User", description = "Fetches a list of Tags for a specific user ID.")
    @ApiResponse(responseCode = "200", description = "User's Tags listed successfully.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TagResponseDto>> listTagsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(tagService.listTagsByUser(userId));
    }

    @Operation(summary = "Update Tag", description = "Updates Tag details (fetched by ID) and returns the updated Tag details.")
    @ApiResponse(responseCode = "200", description = "Tag updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<TagResponseDto> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequestDto request) {
        return ResponseEntity.ok(tagService.updateTag(id, request));
    }

    @Operation(summary = "Delete Tag", description = "Deletes Tag by its ID.")
    @ApiResponse(responseCode = "200", description = "Tag deleted successfully.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
    }
}
