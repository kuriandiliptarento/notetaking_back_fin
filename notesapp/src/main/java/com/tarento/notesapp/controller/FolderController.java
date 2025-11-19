package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.FolderContentsResponseDto;
// import com.tarento.notesapp.dto.AuthResponse;
import com.tarento.notesapp.dto.FolderRequestDto;
import com.tarento.notesapp.dto.FolderResponseDto;
import com.tarento.notesapp.dto.FolderTreeResponseDto;
import com.tarento.notesapp.dto.SuccessResponse;
import com.tarento.notesapp.service.FolderService;

import io.swagger.v3.oas.annotations.Operation;
// import io.swagger.v3.oas.annotations.media.Content;
// import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
@RequiredArgsConstructor
@Tag(name = "Folder", description = "Folder management APIs")
public class FolderController {

    private final FolderService folderService;

    @Operation(summary = "Create Folder", description = "Creates a new folder for the user and returns the created folder details.")
    @ApiResponse(responseCode = "200", description = "Folder created successfully.")
    @PostMapping
    public ResponseEntity<FolderResponseDto> createFolder(@Valid @RequestBody FolderRequestDto request) {
        SuccessResponse response = new SuccessResponse("Folder created successfully");
        FolderResponseDto created = folderService.createFolder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header("Message", response.getMessage())
            .body(created);
    }

    @Operation(summary = "Get Folder By ID", description = "Fetches folder details by its ID.")
    @ApiResponse(responseCode = "200", description = "Folder details fetched successfully.")
    @GetMapping("/{id}")
    public ResponseEntity<FolderResponseDto> getFolder(@PathVariable Long id) {
        return ResponseEntity.ok(folderService.getFolder(id));
    }

    @Operation(summary = "Get User root folders", description = "Fetches all root folders for a user.")
    @ApiResponse(responseCode = "200", description = "Root folders fetched successfully.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FolderResponseDto>> getUserRootFolders(@PathVariable Long userId) {
        return ResponseEntity.ok(folderService.getUserRootFolders(userId));
    }

    @Operation(summary = "Update Folder", description = "Updates folder details (fetched by ID) and returns the updated folder details.")
    @ApiResponse(responseCode = "200", description = "Folder updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<FolderResponseDto> updateFolder(@PathVariable Long id, @Valid @RequestBody FolderRequestDto request) {
        return ResponseEntity.ok(folderService.updateFolder(id, request));
    }

    @Operation(summary = "Delete Folder", description = "Deletes folder by its ID.")
    @ApiResponse(responseCode = "200", description = "User registered successfully.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<SuccessResponse> deleteFolder(@PathVariable Long id) {
        folderService.deleteFolder(id);
        return ResponseEntity.ok(new SuccessResponse("Folder deleted successfully"));
    }

    @Operation(summary = "Get Folder Tree", description = "Fetches the entire folder tree structure for a user.")
    @ApiResponse(responseCode = "200", description = "Folder tree fetched successfully.")
    @GetMapping("/tree/{userId}")
    public ResponseEntity<FolderTreeResponseDto> getTree(@PathVariable Long userId) {
        return ResponseEntity.ok(folderService.getFolderTree(userId));
    }

    @Operation(summary = "Get Child Folders", description = "Fetches all child folders of a given folder ID.")
    @ApiResponse(responseCode = "200", description = "Child folders fetched successfully.")
    @GetMapping("/children/{id}")
    public ResponseEntity<List<FolderResponseDto>> getChildren(@PathVariable("id") Long id) {
        return ResponseEntity.ok(folderService.listChildren(id));
    }

    @Operation(summary = "Get Child Folders and Notes", description = "Fetches all child folders and child notes of a given folder ID.")
    @ApiResponse(responseCode = "200", description = "Children fetched successfully.")
    @GetMapping("/contents/{id}")
    public ResponseEntity<FolderContentsResponseDto> getFolderContents(@PathVariable Long id) {
        FolderContentsResponseDto contents = folderService.listChildrenWithNotes(id);
        return ResponseEntity.ok(contents);
    }

}
