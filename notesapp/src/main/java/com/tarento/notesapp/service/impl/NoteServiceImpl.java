package com.tarento.notesapp.service.impl;

import com.tarento.notesapp.dto.*;
import com.tarento.notesapp.entity.Folder;
import com.tarento.notesapp.entity.Note;
import com.tarento.notesapp.entity.Tag;
// import com.tarento.notesapp.entity.User;
import com.tarento.notesapp.repository.FolderRepository;
import com.tarento.notesapp.repository.NoteRepository;
import com.tarento.notesapp.repository.TagRepository;
import com.tarento.notesapp.repository.UserRepository;
import com.tarento.notesapp.service.NoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    public NoteResponseDto createNote(NoteRequestDto request) {
        validateRequest(request);

        Long folderId = request.getFolderId();
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // ensure folder's user exists (defensive) and get userId
        Long userId = folder.getUser() == null ? null : folder.getUser().getId();
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for folder");
        }

        // Resolve tags (if provided) and ensure they belong to same user
        Set<Tag> resolvedTags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found: " + tagId));
                if (!Objects.equals(tag.getUser().getId(), userId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag " + tagId + " does not belong to folder user");
                }
                resolvedTags.add(tag);
            }
        }

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setFolder(folder);
        note.setTags(resolvedTags);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        Note saved = noteRepository.save(note);
        return toDto(saved);
    }

    @Override
    public NoteResponseDto getNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        return toDto(note);
    }

    @Override
    public List<NoteSummaryDto> listNotesByFolder(Long folderId) {
        // validate folder exists and belongs to a user
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        return noteRepository.findByFolder_Id(folderId)
                .stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteSummaryDto> listNotesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return noteRepository.findByFolder_User_Id(userId)
                .stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public NoteResponseDto updateNote(Long id, NoteRequestDto request) {
        validateRequest(request);

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));

        Folder newFolder = folderRepository.findById(request.getFolderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // Ensure the folder change maintains same owner as note's current owner (or allow move if same user)
        Long currentUserId = note.getFolder().getUser() == null ? null : note.getFolder().getUser().getId();
        Long newUserId = newFolder.getUser() == null ? null : newFolder.getUser().getId();

        if (currentUserId != null && newUserId != null && !Objects.equals(currentUserId, newUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move note to a folder of different user");
        }

        // Resolve tags (if provided) and ensure they belong to same user
        Set<Tag> resolvedTags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Long userId = newUserId != null ? newUserId : currentUserId;
            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot resolve user for tags");
            }
            for (Long tagId : request.getTagIds()) {
                Tag tag = tagRepository.findById(tagId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found: " + tagId));
                if (!Objects.equals(tag.getUser().getId(), userId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag " + tagId + " does not belong to note user");
                }
                resolvedTags.add(tag);
            }
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setFolder(newFolder);
        note.setTags(resolvedTags);
        note.setUpdatedAt(LocalDateTime.now());

        Note saved = noteRepository.save(note);
        return toDto(saved);
    }

    @Override
    public void deleteNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
        noteRepository.delete(note);
    }

    // --- mapping helpers ---

    private NoteResponseDto toDto(Note note) {
        NoteResponseDto dto = new NoteResponseDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setContent(note.getContent());
        dto.setFolderId(note.getFolder() == null ? null : note.getFolder().getId());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());

        if (note.getTags() != null) {
            List<TagBriefDto> tags = note.getTags().stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .map(t -> {
                        TagBriefDto tb = new TagBriefDto();
                        tb.setId(t.getId());
                        tb.setName(t.getName());
                        return tb;
                    })
                    .collect(Collectors.toList());
            dto.setTags(tags);
        }
        return dto;
    }

    private NoteSummaryDto toSummaryDto(Note note) {
        NoteSummaryDto s = new NoteSummaryDto();
        s.setId(note.getId());
        s.setTitle(note.getTitle());
        s.setFolderId(note.getFolder() == null ? null : note.getFolder().getId());
        s.setUpdatedAt(note.getUpdatedAt());
        return s;
    }

    private void validateRequest(NoteRequestDto request) {
        if (request.getFolderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "folderId is required");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
    }
}
