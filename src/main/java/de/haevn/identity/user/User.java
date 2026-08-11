package de.haevn.identity.user;

import de.haevn.identity.license.License;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Persistent JPA entity representing a user account within the platform.
 *
 * <p>Contains account credentials, profile attributes, integration credentials (such as VCS and
 * issue tracking tokens), and initial license validity timestamps. Associated subscription details
 * are managed via {@link License}.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user account.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Unique username used for authentication.
     */
    @Column(name = "username", nullable = false, unique = true)
    private String username;

    /**
     * Cryptographically hashed account password.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Given first name of the user.
     */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * Family last name of the user.
     */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * Assigned security role governing system authorization (e.g., "DEVELOPER", "ADMIN").
     */
    @Column(name = "role", nullable = false)
    private String role;

    /**
     * API token or personal access key for Git VCS integrations (e.g., GitLab).
     */
    @Column(name = "vcs_key")
    private String vcsKey;

    /**
     * API key for Redmine issue tracking integration.
     */
    @Column(name = "redmine_key")
    private String redmineKey;

    /**
     * Resource URL pointing to the user's avatar image.
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    /**
     * Initial trial expiration timestamp assigned upon account creation.
     */
    @Column(name = "license_expiration")
    private Instant licenseExpiration;

    /**
     * Timestamp indicating when the user record was initially created in the database.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Lifecycle callback establishing initial creation and default 30-day trial expiration dates.
     */
    @PrePersist
    protected void onCreate() {
        final Instant now = Instant.now();
        // Initialize explicit fallback timestamps prior to first database insert.
        this.createdAt = now;
        this.licenseExpiration = now.plus(30, ChronoUnit.DAYS);
    }
}