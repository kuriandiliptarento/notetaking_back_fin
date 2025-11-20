package com.tarento.notesapp.service;

import com.tarento.notesapp.dto.NoteRequestDto;
import com.tarento.notesapp.dto.NoteResponseDto;
import com.tarento.notesapp.dto.NoteSummaryDto;

import java.util.List;

public interface NoteService {
    NoteResponseDto createNote(NoteRequestDto request);
    NoteResponseDto getNote(Long id);
    List<NoteSummaryDto> listNotesByFolder(Long folderId);
    List<NoteSummaryDto> listNotesByUser(Long userId);
    NoteResponseDto updateNote(Long id, NoteRequestDto request);
    void deleteNote(Long id);
    List<NoteSummaryDto> listNotesByTagAndUser(Long tagId, Long userId);
    List<NoteSummaryDto> filterNotesByTags(Long userId, List<Long> tagIds, String mode);
    List<NoteResponseDto> listFullNotesByUser(Long userId);

}
