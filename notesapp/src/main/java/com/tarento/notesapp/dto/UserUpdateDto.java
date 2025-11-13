package com.tarento.notesapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateDto {

    @NotBlank(message = "username is required")
    @Email(message = "username must be a valid email")
    private String username;

    // private String role; 
    // optional, depending on your system
}
