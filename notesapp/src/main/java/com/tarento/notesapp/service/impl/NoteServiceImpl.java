package com.tarento.notesapp.service.impl;

import com.tarento.notesapp.dto.*;
import com.tarento.notesapp.entity.Folder;
import com.tarento.notesapp.entity.Note;
import com.tarento.notesapp.entity.NoteTag;
import com.tarento.notesapp.entity.Tag;
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
import java.util.stream.Collector;
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
                if (tag.getUser() == null || !Objects.equals(tag.getUser().getId(), userId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag " + tagId + " does not belong to folder user");
                }
                resolvedTags.add(tag);
            }
        }

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setFolder(folder);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        // Attach tags via NoteTag join entities
        if (!resolvedTags.isEmpty()) {
            for (Tag tag : resolvedTags) {
                NoteTag nt = new NoteTag();
                nt.setNote(note);
                nt.setTag(tag);
                // ensure both sides
                note.getNoteTags().add(nt);
                tag.getNoteTags().add(nt);
            }
        }

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
    public List<NoteResponseDto> listFullNotesByUser(Long userId){
        if(!userRepository.existsById(userId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return noteRepository.findFullByFolder_User_Id(userId)
                .stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(this::toDto)
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
        Long currentUserId = note.getFolder() == null || note.getFolder().getUser() == null ? null : note.getFolder().getUser().getId();
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
                if (tag.getUser() == null || !Objects.equals(tag.getUser().getId(), userId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag " + tagId + " does not belong to note user");
                }
                resolvedTags.add(tag);
            }
        }

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setFolder(newFolder);
        note.setUpdatedAt(LocalDateTime.now());

        // Update NoteTag associations: clear existing and attach new ones
        // Clear both sides for existing NoteTag entries
        if (!note.getNoteTags().isEmpty()) {
            // remove from Tag side as well
            for (Iterator<NoteTag> it = note.getNoteTags().iterator(); it.hasNext(); ) {
                NoteTag nt = it.next();
                Tag tag = nt.getTag();
                if (tag != null) {
                    tag.getNoteTags().remove(nt);
                }
                it.remove();
            }
        }

        if (!resolvedTags.isEmpty()) {
            for (Tag tag : resolvedTags) {
                NoteTag nt = new NoteTag();
                nt.setNote(note);
                nt.setTag(tag);
                note.getNoteTags().add(nt);
                tag.getNoteTags().add(nt);
            }
        }

        Note saved = noteRepository.save(note);
        return toDto(saved);
    }

    @Override
    public List<NoteSummaryDto> listNotesByTagAndUser(Long tagId, Long userId) {
        // validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // validate tag exists and belongs to the user (defensive)
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        // If tags are per-user, ensure ownership
        if (tag.getUser() == null || !Objects.equals(tag.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag does not belong to user");
        }

        List<Note> notes = noteRepository.findByNoteTags_Tag_IdAndFolder_User_Id(tagId, userId);

        return notes.stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NoteSummaryDto> filterNotesByTags(Long userId, List<Long> tagIds, String mode) {
    // validate user
    if (!userRepository.existsById(userId)) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    if (tagIds == null || tagIds.isEmpty()) {
        // return all notes for user (existing behavior)
        return noteRepository.findByFolder_User_Id(userId)
                .stream()
                .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    List<Note> notes;
    if ("OR".equalsIgnoreCase(mode)) {
        notes = noteRepository.findNotesByAnyTag(userId, tagIds);
    } else {
        // AND mode: use tag count as parameter
        notes = noteRepository.findNotesByAllTags(userId, tagIds, tagIds.size());
    }

    return notes.stream()
            .sorted(Comparator.comparing(Note::getUpdatedAt).reversed())
            .map(this::toSummaryDto)
            .collect(Collectors.toList());
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

        if (note.getNoteTags() != null && !note.getNoteTags().isEmpty()) {
            List<TagBriefDto> tags = note.getNoteTags().stream()
                    .map(NoteTag::getTag)
                    .filter(Objects::nonNull)
                    .map(t -> {
                        TagBriefDto tb = new TagBriefDto();
                        tb.setId(t.getId());
                        tb.setName(t.getName());
                        return tb;
                    })
                    .sorted(Comparator.comparing(TagBriefDto::getName, Comparator.nullsLast(String::compareTo)))
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
