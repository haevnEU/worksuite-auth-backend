package de.haevn.identity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object encapsulating user authentication credentials.
 *
 * @param username the unique login handle of the user
 * @param password the plaintext password submitted for verification
 */
@Schema(description = "Credentials payload for user login authentication")
public record LoginRequest(

    @Schema(description = "Unique username", example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Username must not be blank") String username,

    @Schema(description = "Plaintext account password", example = "SecretPass123!", format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(
        message = "Password must not be blank") String password) {
}