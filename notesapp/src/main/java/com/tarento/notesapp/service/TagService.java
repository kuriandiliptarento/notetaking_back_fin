package com.tarento.notesapp.service;

import com.tarento.notesapp.dto.TagRequestDto;
import com.tarento.notesapp.dto.TagResponseDto;

import java.util.List;

public interface TagService {

    TagResponseDto createTag(TagRequestDto request);

    TagResponseDto getTag(Long id);

    List<TagResponseDto> listTagsByUser(Long userId);

    TagResponseDto updateTag(Long id, TagRequestDto request);

    void deleteTag(Long id);
}
