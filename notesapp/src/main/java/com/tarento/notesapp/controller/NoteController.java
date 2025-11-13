package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.NoteRequestDto;
import com.tarento.notesapp.dto.NoteResponseDto;
import com.tarento.notesapp.dto.NoteSummaryDto;
import com.tarento.notesapp.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponseDto> createNote(@Valid @RequestBody NoteRequestDto request) {
        NoteResponseDto created = noteService.createNote(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponseDto> getNote(@PathVariable Long id) {
        return ResponseEntity.ok(noteService.getNote(id));
    }

    @GetMapping("/folder/{folderId}")
    public ResponseEntity<List<NoteSummaryDto>> listByFolder(@PathVariable Long folderId) {
        return ResponseEntity.ok(noteService.listNotesByFolder(folderId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteSummaryDto>> listByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(noteService.listNotesByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponseDto> updateNote(@PathVariable Long id, @Valid @RequestBody NoteRequestDto request) {
        return ResponseEntity.ok(noteService.updateNote(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }
}
