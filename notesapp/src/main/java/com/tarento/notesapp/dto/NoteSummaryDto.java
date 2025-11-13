package com.tarento.notesapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteSummaryDto {
    private Long id;
    private String title;
    private Long folderId;
    private LocalDateTime updatedAt;
}
