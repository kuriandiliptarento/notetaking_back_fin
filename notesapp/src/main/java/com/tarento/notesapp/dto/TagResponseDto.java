package com.tarento.notesapp.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagResponseDto {
    private Long id;
    private Long userId;
    private String name;
    private LocalDateTime createdAt;
}
