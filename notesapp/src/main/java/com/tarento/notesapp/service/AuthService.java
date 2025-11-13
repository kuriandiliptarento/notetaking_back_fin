package com.tarento.notesapp.service;

import com.tarento.notesapp.dto.AuthRequest;
import com.tarento.notesapp.dto.AuthResponse;
import com.tarento.notesapp.dto.RegisterRequest;
import com.tarento.notesapp.entity.Folder;
import com.tarento.notesapp.entity.Role;
import com.tarento.notesapp.entity.User;
import com.tarento.notesapp.repository.FolderRepository;
import com.tarento.notesapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                // .firstname(request.getFirstname())
                // .lastname(request.getLastname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Default role
                .build();
        
        User savedUser = userRepository.save(user);
        // Create root folder
        Folder root = new Folder();
        root.setName("root");
        root.setUser(savedUser);
        root.setParentFolder(null);
        root.setCreatedAt(LocalDateTime.now());
        root.setRoot(true);
        folderRepository.save(root);
        
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        // If authentication is successful, find the user and generate token
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(); // Should not happen if auth was successful
        
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }
}