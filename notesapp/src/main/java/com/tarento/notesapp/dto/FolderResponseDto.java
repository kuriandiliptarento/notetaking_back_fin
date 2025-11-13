package com.tarento.notesapp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class FolderResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private Long parentFolderId;
    private LocalDateTime createdAt;
    private List<FolderResponseDto> subFolders = new ArrayList<>();
}
