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
    private final UserRepository userRepository; // <-- injected to validate users

    @Override
    public FolderResponseDto createFolder(FolderRequestDto request) {
        validateCreateRequest(request);

        Long userId = request.getUserId();
        Long parentId = request.getParentFolderId();

        // --- NEW: ensure user exists ---
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // uniqueness check
        boolean nameExists;
        if (parentId == null) {
            nameExists = folderRepository.existsByUserIdAndNameAndParentFolderIsNull(userId, request.getName());
        } else {
            nameExists = folderRepository.existsByUserIdAndNameAndParentFolder_Id(userId, request.getName(), parentId);
        }
        if (nameExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder with the same name already exists in the parent scope");
        }

        Folder folder = new Folder();
        // folder.setUserId(userId);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        folder.setUser(user);
        folder.setName(request.getName());
        folder.setCreatedAt(LocalDateTime.now());

        if (parentId != null) {
            Folder parent = folderRepository.findById(parentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent folder not found"));
            if (!Objects.equals(parent.getUser().getId(), userId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent folder belongs to a different user");
            }
            folder.setParentFolder(parent);
            parent.getSubFolders().add(folder); // maintain both sides
        }

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
        // Optional: validate the user exists before returning an empty list for a non-existent user
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Folder> roots = folderRepository.findByUserIdAndParentFolderIsNull(userId);
        return roots.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public FolderResponseDto updateFolder(Long id, FolderRequestDto request) {
        validateCreateRequest(request);

        Long userId = request.getUserId();
        Long newParentId = request.getParentFolderId();

        // --- NEW: ensure user exists ---
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));

        // ensure same user (cannot reassign someone else's folder)
        if (!Objects.equals(folder.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot change folder of different user");
        }

        // check for name uniqueness in new parent scope
        boolean nameExists;
        if (newParentId == null) {
            nameExists = folderRepository.existsByUserIdAndNameAndParentFolderIsNull(userId, request.getName());
        } else {
            nameExists = folderRepository.existsByUserIdAndNameAndParentFolder_Id(userId, request.getName(), newParentId);
        }

        if (nameExists) {
            // fetch candidate siblings to check if it is the same folder
            List<Folder> siblings = (newParentId == null)
                    ? folderRepository.findByUserIdAndParentFolderIsNull(userId)
                    : folderRepository.findByParentFolder_Id(newParentId);
            boolean conflict = siblings.stream().anyMatch(s -> !Objects.equals(s.getId(), id) && s.getName().equals(request.getName()));
            if (conflict) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Folder with the same name already exists in the parent scope");
            }
        }

        // Update parent relation if changed
        Long currentParentId = folder.getParentFolder() == null ? null : folder.getParentFolder().getId();
        if (!Objects.equals(currentParentId, newParentId)) {
            // remove from old parent
            if (folder.getParentFolder() != null) {
                folder.getParentFolder().getSubFolders().remove(folder);
            }

            if (newParentId != null) {
                Folder newParent = folderRepository.findById(newParentId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New parent folder not found"));
                if (!Objects.equals(newParent.getUser().getId(), userId)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New parent belongs to different user");
                }
                folder.setParentFolder(newParent);
                newParent.getSubFolders().add(folder);
            } else {
                folder.setParentFolder(null);
            }
        }

        folder.setName(request.getName());
        Folder saved = folderRepository.save(folder);
        return toDto(saved);
    }

    @Override
    public void deleteFolder(Long id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Folder not found"));
        // cascade and orphanRemoval will remove children
        folderRepository.delete(folder);
    }

    @Override
    public FolderTreeResponseDto getFolderTree(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Folder> roots = folderRepository.findByUserIdAndParentFolderIsNull(userId);
        FolderTreeResponseDto root = new FolderTreeResponseDto();
        root.setId(null);
        root.setName("root");
        for (Folder r : roots) {
            root.getChildren().add(toTreeDto(r));
        }
        return root;
    }

    // ------ Mapping helpers ------

    private FolderResponseDto toDto(Folder folder) {
        FolderResponseDto dto = new FolderResponseDto();
        dto.setId(folder.getId());
        dto.setUserId(folder.getUser().getId());
        dto.setName(folder.getName());
        dto.setParentFolderId(folder.getParentFolder() == null ? null : folder.getParentFolder().getId());
        dto.setCreatedAt(folder.getCreatedAt());
        // map children
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
}
