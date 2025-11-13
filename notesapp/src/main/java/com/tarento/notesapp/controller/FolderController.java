package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.FolderRequestDto;
import com.tarento.notesapp.dto.FolderResponseDto;
import com.tarento.notesapp.dto.FolderTreeResponseDto;
import com.tarento.notesapp.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<FolderResponseDto> createFolder(@Valid @RequestBody FolderRequestDto request) {
        FolderResponseDto created = folderService.createFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FolderResponseDto> getFolder(@PathVariable Long id) {
        return ResponseEntity.ok(folderService.getFolder(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FolderResponseDto>> getUserRootFolders(@PathVariable Long userId) {
        return ResponseEntity.ok(folderService.getUserRootFolders(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FolderResponseDto> updateFolder(@PathVariable Long id, @Valid @RequestBody FolderRequestDto request) {
        return ResponseEntity.ok(folderService.updateFolder(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
    }

    @GetMapping("/tree/{userId}")
    public ResponseEntity<FolderTreeResponseDto> getTree(@PathVariable Long userId) {
        return ResponseEntity.ok(folderService.getFolderTree(userId));
    }
}
