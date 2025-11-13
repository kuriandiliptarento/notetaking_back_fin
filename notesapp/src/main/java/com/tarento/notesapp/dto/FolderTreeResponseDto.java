package com.tarento.notesapp.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FolderTreeResponseDto {
    private Long id;
    private String name;
    private List<FolderTreeResponseDto> children = new ArrayList<>();
}
