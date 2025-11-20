package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.NoteRequestDto;
import com.tarento.notesapp.dto.NoteResponseDto;
import com.tarento.notesapp.dto.NoteSummaryDto;
import com.tarento.notesapp.dto.SuccessResponse;
import com.tarento.notesapp.dto.TagSearchRequest;
import com.tarento.notesapp.service.NoteService;

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
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Note", description = "Note management APIs")
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "Create Note", description = "Creates a new note for the user and returns the created note details.")
    @ApiResponse(responseCode = "200", description = "Note created successfully.")
    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(@Valid @RequestBody NoteRequestDto request) {
        NoteResponseDto created = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get Note By ID", description = "Fetches note details by its ID.")
    @ApiResponse(responseCode = "200", description = "Note details fetched successfully.")
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNote(id));
    }

    @Operation(summary = "List Notes By Folder", description = "Fetches a list of notes under a specific folder ID.")
    @ApiResponse(responseCode = "200", description = "Notes in Folder listed successfully.")
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<NoteSummaryDto>> listByFolder(@PathVariable Long folderId) {
        return ResponseEntity.ok(noteService.listNotesByFolder(folderId));
    }

    @Operation(summary = "List Notes By User", description = "Fetches a list of notes for a specific user ID.")
    @ApiResponse(responseCode = "200", description = "User's Notes listed successfully.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteSummaryDto>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(noteService.listNotesByUser(userId));
    }

    @GetMapping("/tag/{tagId}/user/{userId}")
    public ResponseEntity<List<NoteSummaryDto>> listNotesByTagAndUser(
            @PathVariable("tagId") Long tagId,
            @PathVariable("userId") Long userId) {
        List<NoteSummaryDto> result = noteService.listNotesByTagAndUser(tagId, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update Note", description = "Updates note details (fetched by ID) and returns the updated note details.")
    @ApiResponse(responseCode = "200", description = "Note updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDto> updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequestDto request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @Operation(summary = "Delete Note", description = "Deletes note by its ID.")
    @ApiResponse(responseCode = "200", description = "Note deleted successfully.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<SuccessResponse> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok(new SuccessResponse("Note deleted successfully"));
    }


    @Operation(summary = "Search Note by Multiple Tags", description = "Gets note by its Multiple Tags.")
    @ApiResponse(responseCode = "200", description = "Notes Fetched successfully.")
    @PostMapping("/search/by-tags/{userId}")
    public ResponseEntity<List<NoteSummaryDto>> searchByTags(
            @PathVariable("userId") Long userId,
            @RequestBody TagSearchRequest request
    ) {
        // default mode to AND when not provided
        String mode = request.getMode() == null ? "AND" : request.getMode();
        List<NoteSummaryDto> result = noteService.filterNotesByTags(userId, request.getTagIds(), mode);
        return ResponseEntity.ok(result);
    }
}
