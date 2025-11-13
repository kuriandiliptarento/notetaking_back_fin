// AuthResponse.java
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
// @Schema(description = "Authentication response with JWT token")
public class AuthResponse {

    // @Schema(
    //     description = "JWT access token",
    //     example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    // )
    private String token;
}