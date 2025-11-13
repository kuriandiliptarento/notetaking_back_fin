package com.tarento.notesapp.service.impl;

import com.tarento.notesapp.dto.UserUpdateDto;
import com.tarento.notesapp.dto.UserResponseDto;
// import com.tarento.notesapp.entity.Role;
import com.tarento.notesapp.entity.User;
import com.tarento.notesapp.repository.UserRepository;
import com.tarento.notesapp.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    @Override
    public UserResponseDto getUser(Long id) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    @Override
    public List<UserResponseDto> listUsers() {
        return repo.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto request) {
        User user = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // check unique username
        if (!user.getUsername().equals(request.getUsername())) {
            if (repo.existsByUsername(request.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(request.getUsername());
        }

        // optional role update
        // if (request.getRole() != null && !request.getRole().isBlank()) {
        //     try {
        //         user.setRole(Role.valueOf(request.getRole()));
        //     } catch (Exception ignored) {
        //         // ignore invalid role input or throw error if needed
        //     }
        // }

        return toDto(repo.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        repo.deleteById(id);
    }

    @Override
    public UserResponseDto findByUsername(String username) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toDto(user);
    }

    private UserResponseDto toDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole().name());
        return dto;
    }
}
