package com.tarento.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class NoteRequestDto {

    @NotNull(message = "folderId is required")
    private Long folderId;

    @NotBlank(message = "title is required")
    @Size(max = 255, message = "title must be at most 255 characters")
    private String title;

    private String content;

    // Optional list of tag ids to attach
    private List<Long> tagIds;
}
