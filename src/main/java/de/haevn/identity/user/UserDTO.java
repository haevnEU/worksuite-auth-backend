package de.haevn.identity.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Data transfer object representing the public profile of a {@link User}, omitting sensitive credential hashes.
 *
 * @param id stringified unique user identifier (UUID)
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @param role the assigned platform authority
 * @param vcsKey masked or raw VCS access integration key
 * @param redmineKey Redmine issue tracker API key
 * @param createdAt ISO-8601 timestamp string representing account creation
 * @param avatarUrl URL to the profile picture
 */
@Schema(description = "Public representation of a platform user profile")
public record UserDTO(

    @Schema(description = "Stringified user UUID", example = "e3ff484b-fab6-4116-997f-23b2ab5c700e") String id,

    @Schema(description = "First name", example = "John") String firstName,

    @Schema(description = "Last name", example = "Doe") String lastName,

    @Schema(description = "Assigned platform role", example = "DEVELOPER") String role,

    @Schema(description = "Configured VCS API key", example = "glpat-xxxxxxxxxxxx") String vcsKey,

    @Schema(description = "Configured Redmine API key", example = "redmine-xxxxxxxxx") String redmineKey,

    @Schema(description = "Creation timestamp in ISO-8601 format", example = "2026-08-17T01:00:00Z") String createdAt,

    @Schema(description = "Avatar image resource URL", example = "https://picsum.photos/200") String avatarUrl) {

    /**
     * Converts an internal {@link User} entity to its external {@link UserDTO} representation.
     *
     * @param user the persistent entity to map
     * @return a sanitized {@link UserDTO} instance
     */
    public static UserDTO fromEntity(final User user) {
        return new UserDTO(user.getId().toString(), user.getFirstName(), user.getLastName(), user.getRole(),
            user.getVcsKey(), user.getRedmineKey(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : Instant.now().toString(),
            user.getAvatarUrl());
    }
}