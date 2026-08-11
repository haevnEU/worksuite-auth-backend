package de.haevn.identity.license;

import de.haevn.identity.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapping the persistent {@code licenses} table.
 *
 * <p>Contains subscription tier metadata, expiration timestamps, and cryptographic keys
 * associated with a specific {@link User} identifier.
 */
@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    /**
     * Primary key directly corresponding to the owning {@link User#getId()}.
     */
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    /**
     * Formatted license key string (e.g., {@code WS-PRO-XXXX-XXXX-XXXX}).
     */
    @Column(name = "license_key", nullable = false)
    private String licenseKey;

    /**
     * Expiration timestamp beyond which features assigned to {@link #plan} are locked.
     */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * The active subscription tier name (e.g., "COMMUNITY", "PRO", "ENTERPRISE").
     */
    @Column(name = "plan", nullable = false, length = 50)
    private String plan;

    /**
     * Timestamp when the record was initially created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    /**
     * Timestamp of the most recent plan switch or license renewal.
     */
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Evaluates whether the license has passed its expiration timestamp against current UTC time.
     *
     * @return {@code true} if expired or unconfigured; {@code false} if active
     */
    public boolean isExpired() {
        return this.expiresAt == null || Instant.now().isAfter(this.expiresAt);
    }
}