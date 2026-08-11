package de.haevn.identity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request payload used to verify existing credentials and supply a new password for a {@link User}.
 *
 * @param userId the unique identifier of the target user
 * @param currentPassword the user's active plaintext password for identity verification
 * @param newPassword the newly chosen password to replace the active one
 */
@Schema(description = "Payload for resetting or changing an existing user's password")
public record PasswordChange(

    @Schema(description = "Unique user ID", example = "e3ff484b-fab6-4116-997f-23b2ab5c700e",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "User ID must not be null") UUID userId,

    @Schema(description = "Current account password", example = "OldSecretPass123!", format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(
        message = "Current password must not be blank") String currentPassword,

    @Schema(description = "New plaintext password", example = "NewSecretPass456!", format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "New password must not be blank") @Size(
        min = 8, message = "New password must contain at least 8 characters") String newPassword) {
}