package com.tarento.notesapp.service.impl;

import com.tarento.notesapp.dto.TagRequestDto;
import com.tarento.notesapp.dto.TagResponseDto;
import com.tarento.notesapp.entity.Tag;
import com.tarento.notesapp.entity.User;
import com.tarento.notesapp.repository.TagRepository;
import com.tarento.notesapp.repository.UserRepository;
import com.tarento.notesapp.service.TagService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Override
    public TagResponseDto createTag(TagRequestDto request) {
        validateRequest(request);

        Long userId = request.getUserId();

        // ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // uniqueness check
        if (tagRepository.existsByUser_IdAndName(userId, request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag with the same name already exists for this user");
        }

        Tag tag = new Tag();
        tag.setUser(user);
        tag.setName(request.getName());
        tag.setCreatedAt(LocalDateTime.now());

        Tag saved = tagRepository.save(tag);
        return toDto(saved);
    }

    @Override
    public TagResponseDto getTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
        return toDto(tag);
    }

    @Override
    public List<TagResponseDto> listTagsByUser(Long userId) {
        // optional: validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return tagRepository.findByUser_Id(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TagResponseDto updateTag(Long id, TagRequestDto request) {
        validateRequest(request);

        Long userId = request.getUserId();

        // ensure user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));

        // ensure tag belongs to user
        if (!tag.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update tag that belongs to a different user");
        }

        // uniqueness check (exclude same tag)
        boolean exists = tagRepository.existsByUser_IdAndName(userId, request.getName());
        if (exists && !tag.getName().equals(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag with the same name already exists for this user");
        }

        tag.setName(request.getName());
        Tag saved = tagRepository.save(tag);
        return toDto(saved);
    }

    @Override
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
        tagRepository.delete(tag);
    }

    // mapping helper
    private TagResponseDto toDto(Tag tag) {
        TagResponseDto dto = new TagResponseDto();
        dto.setId(tag.getId());
        dto.setUserId(tag.getUser().getId());
        dto.setName(tag.getName());
        dto.setCreatedAt(tag.getCreatedAt());
        return dto;
    }

    private void validateRequest(TagRequestDto request) {
        if (request.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }
    }
}
