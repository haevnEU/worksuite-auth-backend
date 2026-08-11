package de.haevn.identity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload containing all parameters necessary to register a new {@link User}.
 *
 * @param username the unique username identifier
 * @param password the raw password to be hashed and stored
 * @param firstname the user's given first name
 * @param lastName the user's family name
 * @param role the assigned application role (e.g., "DEVELOPER", "ADMIN")
 * @param vcsKey personal access token or API key for Git VCS integrations
 * @param redmineKey API access token for issue tracking integrations
 * @param avatarUrl public URL pointing to the user's profile image
 */
@Schema(description = "Payload required to register and create a new platform user")
public record CreateUserRequest(

    @Schema(description = "Unique user account handle", example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Username must not be blank") @Size(min = 3,
        max = 50, message = "Username must be between 3 and 50 characters") String username,

    @Schema(description = "Plaintext password", example = "P@ssw0rd2026!", format = "password",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Password must not be blank") @Size(min = 8,
        message = "Password must contain at least 8 characters") String password,

    @Schema(description = "First name", example = "John") String firstname,

    @Schema(description = "Last name", example = "Doe") String lastName,

    @Schema(description = "Assigned security role", example = "DEVELOPER", defaultValue = "DEVELOPER") String role,

    @Schema(description = "VCS provider API token", example = "glpat-xxxxxxxxxxxxxxxxxxxx") String vcsKey,

    @Schema(description = "Redmine access token", example = "9f8e7d6c5b4a3210") String redmineKey,

    @Schema(description = "Direct URL to user avatar image", example = "https://picsum.photos/200") String avatarUrl) {
}