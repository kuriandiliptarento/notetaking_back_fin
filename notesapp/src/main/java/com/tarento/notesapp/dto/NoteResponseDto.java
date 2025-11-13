package com.tarento.notesapp.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class NoteResponseDto {
    private Long id;
    private String title;
    private String content;
    private Long folderId;
    private List<TagBriefDto> tags = new ArrayList<>();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
