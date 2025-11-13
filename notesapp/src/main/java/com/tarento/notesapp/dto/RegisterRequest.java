// RegisterRequest.java
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
// @Schema(description = "Registration request payload")
public class RegisterRequest {
    // private String firstname;
    // private String lastname;

    // @Schema(
    //     description = "User email address",
    //     example = "user@example.com",
    //     required = true
    // )
    private String username;

    // @Schema(
    //     description = "User password (minimum 8 characters)",
    //     example = "password123",
    //     required = true,
    //     minLength = 8
    // )
    private String password;
}