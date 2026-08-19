package de.haevn.identity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Data Transfer Object representing the payload required to re-authenticate a user by their unique identifier.
 *
 * @param userId   the unique {@link UUID} of the user attempting re-authentication; must not be null
 * @param password the raw plaintext password to verify against the stored hash; must not be blank
 */
@Schema(description = "Payload required to re-authenticate an existing user via their unique identifier.")
public record ReauthDTO(
    @NotNull(message = "User ID must not be null.")
    @Schema(
        description = "Unique identifier of the user.",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID userId,

    @NotBlank(message = "Password must not be blank.")
    @Schema(
        description = "Plaintext password for credential verification.",
        example = "SecretP@ssw0rd!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "password"
    )
    String password
) {
}