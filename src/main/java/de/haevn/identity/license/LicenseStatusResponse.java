package de.haevn.identity.license;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/**
 * Comprehensive response model representing the current validity and tier status of a {@link License}.
 *
 * @param userId unique identifier of the owning user
 * @param valid flag indicating if the subscription is actively within its valid term
 * @param licenseKey the currently assigned license key
 * @param plan the active subscription tier (e.g., "COMMUNITY", "PRO", "ENTERPRISE", "NONE")
 * @param expiresAt timestamp when the subscription period ends
 * @param createdAt timestamp when the license was initially created
 * @param updatedAt timestamp of the most recent renewal or tier change
 * @param message human-readable status or explanatory notification
 */
@Schema(description = "Detailed subscription status and active workspace license information")
public record LicenseStatusResponse(

    @Schema(description = "Unique user identifier", example = "e3ff484b-fab6-4116-997f-23b2ab5c700e") UUID userId,

    @Schema(description = "Indicates whether the license is currently active and unexpired",
        example = "true") boolean valid,

    @Schema(description = "Active license key string", example = "WS-PRO-A9F2-K891-B421") String licenseKey,

    @Schema(description = "Assigned workspace subscription plan", example = "PRO",
        allowableValues = {"COMMUNITY", "PRO", "ENTERPRISE", "NONE"}) String plan,

    @Schema(description = "Subscription expiration timestamp") Instant expiresAt,

    @Schema(description = "Initial issuance timestamp") Instant createdAt,

    @Schema(description = "Last update or renewal timestamp") Instant updatedAt,

    @Schema(description = "Human-readable status summary message",
        example = "License is active and valid.") String message) {

    /**
     * Constructs a {@link LicenseStatusResponse} directly from a persistent {@link License} entity.
     *
     * @param license the source entity
     * @return a populated response instance
     */
    public static LicenseStatusResponse fromEntity(final License license) {
        final boolean isValid = !license.isExpired(); // Calculate status against current system clock.
        return new LicenseStatusResponse(license.getUserId(), isValid, license.getLicenseKey(), license.getPlan(),
            license.getExpiresAt(), license.getCreatedAt(), license.getUpdatedAt(),
            isValid ? "License is active and valid." : "License has expired.");
    }

    /**
     * Factory method creating a default unassigned state response for users without an active license.
     *
     * @param userId the target user identifier
     * @return an inactive placeholder {@link LicenseStatusResponse}
     */
    public static LicenseStatusResponse notFound(final UUID userId) {
        return new LicenseStatusResponse(userId, false, null, "NONE", null, null, null,
            "No license assigned to this user.");
    }
}