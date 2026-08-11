package de.haevn.authentification;

import java.util.UUID;

public record PasswordChange(UUID userId, String currentPassword, String newPassword) {
}
