package de.haevn.authentification.license;

import java.time.Instant;
import java.util.UUID;

public record LicenseStatusResponse(
    UUID userId,
    boolean valid,
    String licenseKey,
    String plan,
    Instant expiresAt,
    Instant createdAt,
    Instant updatedAt,
    String message
) {
    /**
     * Mappt direkt aus der License-Entity.
     */
    public static LicenseStatusResponse fromEntity(final License license) {
        final boolean isValid = !license.isExpired();
        return new LicenseStatusResponse(
            license.getUserId(),
            isValid,
            license.getLicenseKey(),
            license.getPlan(),
            license.getExpiresAt(),
            license.getCreatedAt(),
            license.getUpdatedAt(),
            isValid ? "License is active and valid." : "License has expired."
        );
    }

    public static LicenseStatusResponse notFound(final UUID userId) {
        return new LicenseStatusResponse(
            userId,
            false,
            null,
            "NONE",
            null,
            null,
            null,
            "No license assigned to this user."
        );
    }
}