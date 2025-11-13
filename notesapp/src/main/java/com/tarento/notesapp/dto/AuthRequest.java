// AuthRequest.java
package com.tarento.notesapp.dto;

// import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
// @Schema(description = "Authentication request payload")
public class AuthRequest {

    // @Schema(
    //     description = "User email address",
    //     example = "user@example.com",
    //     required = true
    // )
    private String username;

    // @Schema(
    //     description = "User password (minimum 8 characters)",
    //     example = "password123",
    //     required = true
    // )
    private String password;
}