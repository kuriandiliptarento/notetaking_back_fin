package com.tarento.notesapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagSearchRequest {
    private List<Long> tagIds;
    // "AND" or "OR" (default to "AND" if not provided)
    private String mode;
}
