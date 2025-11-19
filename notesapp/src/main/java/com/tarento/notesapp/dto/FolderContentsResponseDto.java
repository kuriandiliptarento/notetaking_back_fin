package com.tarento.notesapp.dto;

import lombok.Data;
import java.util.List;

@Data
public class FolderContentsResponseDto {
    private List<FolderResponseDto> folders;
    private List<NoteSummaryDto> notes;
}
