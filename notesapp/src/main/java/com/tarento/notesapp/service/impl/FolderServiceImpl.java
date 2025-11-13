package com.tarento.notesapp.service.impl;

import com.tarento.notesapp.dto.FolderRequestDto;
import com.tarento.notesapp.dto.FolderResponseDto;
import com.tarento.notesapp.dto.FolderTreeResponseDto;
import com.tarento.notesapp.entity.Folder;
import com.tarento.notesapp.entity.User;
import com.tarento.notesapp.repository.FolderRepository;
import com.tarento.notesapp.repository.UserRepository;
import com.tarento.notesapp.service.FolderService;
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
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Override
    public FolderResponseDto createFolder(FolderRequestDto request) {
        validateCreateRequest(request);

        Long userId = request.getUserId();
        Long parentId = request.getParentFolderId();

        // Ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // If parentId is null => attach to user's root folder (create root if missing)
        Folder parent = null;
        if (parentId == null) {
            parent = getOrCreateRootFolder(user);
            parentId = parent.getId();
        } else {
            parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));
            if (!Objects.equals(parent.getUser().getId(), userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent folder belongs to a different user");
            }
        }

        // Uniqueness check scoped to parent
        boolean nameExists = folderRepository.existsByUser_IdAndNameAndParentFolder_Id(userId, request.getName(), parentId);
        if (nameExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder with the same name already exists in the parent scope");
        }

        Folder folder = new Folder();
        folder.setUser(user);
        folder.setName(request.getName());
        folder.setParentFolder(parent);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setRoot(false);

        // maintain both sides
        parent.getSubFolders().add(folder);

        Folder saved = folderRepository.save(folder);
        return toDto(saved);
    }

    @Override
    public FolderResponseDto getFolder(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        return toDto(folder);
    }

    @Override
    public List<FolderResponseDto> getUserRootFolders(Long userId) {
        // returns children of the user's root
        Folder root = folderRepository.findByUser_IdAndRootTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root folder not found for user"));

        List<Folder> children = folderRepository.findByParentFolder_Id(root.getId());
        return children.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public FolderResponseDto updateFolder(Long id, FolderRequestDto request) {
        validateCreateRequest(request);

        Long userId = request.getUserId();
        Long newParentId = request.getParentFolderId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // Prevent updating root folder properties
        if (folder.isRoot()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify root folder");
        }

        // ensure same user
        if (!Objects.equals(folder.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change folder of different user");
        }

        // resolve new parent (if null attach to user's root)
        Folder newParent = null;
        if (newParentId == null) {
            newParent = getOrCreateRootFolder(user);
            newParentId = newParent.getId();
        } else {
            newParent = folderRepository.findById(newParentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New parent folder not found"));
            if (!Objects.equals(newParent.getUser().getId(), userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New parent belongs to different user");
            }
        }

        // ---- NEW: cycle detection ----
        // If newParent is a descendant of folder, moving would create a cycle.
        // Walk up from newParent to root and ensure we never hit the folder being moved.
        assertNotMovingIntoDescendant(folder, newParent);

        // check uniqueness in new parent scope (excluding self)
        boolean nameExists = folderRepository.existsByUser_IdAndNameAndParentFolder_Id(userId, request.getName(), newParentId);
        if (nameExists) {
            // fetch candidate siblings to check if it is the same folder
            List<Folder> siblings = folderRepository.findByParentFolder_Id(newParentId);
            boolean conflict = siblings.stream().anyMatch(s -> !Objects.equals(s.getId(), id) && s.getName().equals(request.getName()));
            if (conflict) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder with the same name already exists in the parent scope");
            }
        }

        // move folder if parent changed
        Long currentParentId = folder.getParentFolder() == null ? null : folder.getParentFolder().getId();
        if (!Objects.equals(currentParentId, newParentId)) {
            if (folder.getParentFolder() != null) {
                folder.getParentFolder().getSubFolders().remove(folder);
            }
            folder.setParentFolder(newParent);
            newParent.getSubFolders().add(folder);
        }

        folder.setName(request.getName());
        Folder saved = folderRepository.save(folder);
        return toDto(saved);
    }

    @Override
    public void deleteFolder(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        if (folder.isRoot()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete root folder");
        }

        folderRepository.delete(folder);
    }

    @Override
    public FolderTreeResponseDto getFolderTree(Long userId) {
        Folder root = folderRepository.findByUser_IdAndRootTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Root folder not found for user"));

        FolderTreeResponseDto rootDto = new FolderTreeResponseDto();
        rootDto.setId(root.getId());
        rootDto.setName("root");
        root.getSubFolders().stream()
                .sorted(Comparator.comparing(Folder::getName))
                .forEach(child -> rootDto.getChildren().add(toTreeDto(child)));

        return rootDto;
    }

    @Override
    public List<FolderResponseDto> listChildren(Long parentId) {
        // Ensure parent exists (friendly 404)
        Folder parent = folderRepository.findById(parentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));

        List<Folder> children = folderRepository.findByParentFolder_Id(parentId);
        return children.stream()
                .sorted(Comparator.comparing(Folder::getName))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Helper: find or create the user's root folder
    private Folder getOrCreateRootFolder(User user) {
        return folderRepository.findByUser_IdAndRootTrue(user.getId())
                .orElseGet(() -> {
                    Folder root = new Folder();
                    root.setUser(user);
                    root.setName("root");
                    root.setParentFolder(null);
                    root.setRoot(true);
                    root.setCreatedAt(LocalDateTime.now());
                    return folderRepository.save(root);
                });
    }

    // mapping helpers (unchanged except userId retrieval)
    private FolderResponseDto toDto(Folder folder) {
        FolderResponseDto dto = new FolderResponseDto();
        dto.setId(folder.getId());
        dto.setUserId(folder.getUser() == null ? null : folder.getUser().getId());
        dto.setName(folder.getName());
        dto.setParentFolderId(folder.getParentFolder() == null ? null : folder.getParentFolder().getId());
        dto.setCreatedAt(folder.getCreatedAt());
        folder.getSubFolders().stream()
                .sorted(Comparator.comparing(Folder::getName))
                .forEach(child -> dto.getSubFolders().add(toDto(child)));
        return dto;
    }

    private FolderTreeResponseDto toTreeDto(Folder folder) {
        FolderTreeResponseDto dto = new FolderTreeResponseDto();
        dto.setId(folder.getId());
        dto.setName(folder.getName());
        folder.getSubFolders().stream()
                .sorted(Comparator.comparing(Folder::getName))
                .forEach(child -> dto.getChildren().add(toTreeDto(child)));
        return dto;
    }

    private void validateCreateRequest(FolderRequestDto request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
    }

    /**
     * Throws ResponseStatusException if newParent is a descendant of folder (i.e., moving folder into its own subtree).
     */
    private void assertNotMovingIntoDescendant(Folder folder, Folder newParent) {
        Folder p = newParent;
        while (p != null) {
            // Defensive: if either id is null (shouldn't happen for persisted folders), stop checking
            Long pId = p.getId();
            Long folderId = folder.getId();
            if (pId != null && folderId != null && pId.equals(folderId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move folder into its own descendant");
            }
            p = p.getParentFolder();
        }
    }
}
