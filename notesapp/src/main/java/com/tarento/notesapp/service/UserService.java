package com.tarento.notesapp.service;

import com.tarento.notesapp.dto.UserUpdateDto;
import com.tarento.notesapp.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto getUser(Long id);

    List<UserResponseDto> listUsers();

    UserResponseDto updateUser(Long id, UserUpdateDto request);

    void deleteUser(Long id);

    UserResponseDto findByUsername(String username);
}
