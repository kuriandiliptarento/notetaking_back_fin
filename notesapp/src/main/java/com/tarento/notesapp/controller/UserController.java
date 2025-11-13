package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.UserUpdateDto;
import com.tarento.notesapp.dto.UserResponseDto;
import com.tarento.notesapp.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get User By ID", description = "Fetches user details by its ID.")
    @ApiResponse(responseCode = "200", description = "User details fetched successfully.")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @Operation(summary = "List Users", description = "Fetches a list of all users.(Debug Function)")
    @ApiResponse(responseCode = "200", description = "Users listed successfully.")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> listUsers() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @Operation(summary = "Update User", description = "Updates user details (fetched by ID) and returns the updated user details.")
    @ApiResponse(responseCode = "200", description = "User updated successfully.")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto request) {

        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Delete User", description = "Deletes user by its ID.")
    @ApiResponse(responseCode = "200", description = "User deleted successfully.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
