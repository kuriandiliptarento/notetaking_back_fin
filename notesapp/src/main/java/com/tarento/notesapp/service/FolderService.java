package com.tarento.notesapp.service;

import com.tarento.notesapp.dto.FolderRequestDto;
import com.tarento.notesapp.dto.FolderResponseDto;
import com.tarento.notesapp.dto.FolderTreeResponseDto;

import java.util.List;

public interface FolderService {
    FolderResponseDto createFolder(FolderRequestDto request);
    FolderResponseDto getFolder(Long id);
    List<FolderResponseDto> getUserRootFolders(Long userId);
    FolderResponseDto updateFolder(Long id, FolderRequestDto request);
    void deleteFolder(Long id);
    FolderTreeResponseDto getFolderTree(Long userId);
    List<FolderResponseDto> listChildren(Long parentId);
}
