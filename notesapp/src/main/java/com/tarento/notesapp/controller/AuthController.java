package com.tarento.notesapp.controller;

import com.tarento.notesapp.dto.AuthRequest;
import com.tarento.notesapp.dto.AuthResponse;
import com.tarento.notesapp.dto.RegisterRequest;
import com.tarento.notesapp.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Refister a new user", description = "Registers a new user and returns a JWT token upon successful registration.")
    @ApiResponse(responseCode = "200", description = "User registered successfully.", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }


    @Operation(summary = "Authenticate user", description = "Authenticates a user and returns a JWT token upon successful authentication.")
    @ApiResponse(responseCode = "200", description = "User authenticated successfully.", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))
    })
    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}