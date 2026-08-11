package de.haevn.authentification;

import java.time.Instant;

public record UserDTO(
    String id,
    String firstName,
    String lastName,
    String role,
    String vcsKey,
    String redmineKey,
    String createdAt,
    String avatarUrl
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId().toString(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getVcsKey(),
            user.getRedmineKey(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : Instant.now().toString(),
            user.getAvatarUrl()
        );
    }
}