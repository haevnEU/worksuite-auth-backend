package de.haevn.identity.user;

import java.util.UUID;

public record PasswordChange(UUID userId, String currentPassword, String newPassword) {
}
