package com.tarento.notesapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderRequestDto {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "name is required")
    @Size(max = 255, message = "name must be at most 255 characters")
    private String name;
    
    private Long parentFolderId; // null => root folder
}
