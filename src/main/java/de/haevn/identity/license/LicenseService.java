package de.haevn.identity.license;

import de.haevn.identity.user.User;
import de.haevn.identity.user.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user subscription tiers, plan assignments, license renewals, and key generation.
 *
 * <p>Example usage:
 * <pre>{@code
 * @Autowired
 * private LicenseService licenseService;
 *
 * LicenseStatusResponse status = licenseService.assignPlan("PRO");
 * boolean isExpired = licenseService.licenseExpired(userId);
 * }</pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_SUBSCRIPTION_DAYS = 365;

    private final LicenseRepository licenseRepository;
    private final UserService userService;

    /**
     * Checks whether a license associated with the given user identifier is expired or non-existent.
     *
     * @param userId the unique identifier of the {@link User}
     * @return {@code true} if the license is expired or missing; {@code false} otherwise
     */
    @Transactional(readOnly = true)
    public boolean licenseExpired(final UUID userId) {
        if (userId == null) {
            return true;
        }

        return licenseRepository.findById(userId)
            .map(License::isExpired)
            .orElse(true);
    }

    /**
     * Retrieves the license status for the currently authenticated user context.
     *
     * @return the {@link LicenseStatusResponse} model
     */
    @Transactional(readOnly = true)
    public LicenseStatusResponse getCurrentLicenseStatus() {
        final UUID currentUserId = userService.getCurrentUser().getId();

        return licenseRepository.findById(currentUserId)
            .map(LicenseStatusResponse::fromEntity)
            .orElseGet(() -> LicenseStatusResponse.notFound(currentUserId));
    }

    /**
     * Directly assigns or switches the subscription plan for the currently authenticated user.
     *
     * <p>Generates a corresponding license key with the appropriate plan prefix if a new plan is assigned.
     *
     * <p>Example usage:
     * <pre>{@code
     * LicenseStatusResponse response = licenseService.assignPlan("PRO");
     * }</pre>
     *
     * @param plan the target plan identifier (e.g. {@code "COMMUNITY"}, {@code "PRO"}, {@code "ENTERPRISE"})
     * @return the updated {@link LicenseStatusResponse}
     */
    @Transactional
    public LicenseStatusResponse assignPlan(
        @NotBlank(message = "Plan must not be blank")
        @Pattern(
            regexp = "^(COMMUNITY|PRO|ENTERPRISE)$",
            message = "Plan must be one of: COMMUNITY, PRO, ENTERPRISE"
        ) final String plan
    ) {
        final UUID currentUserId = userService.getCurrentUser().getId();
        final String normalizedPlan = plan.trim().toUpperCase();
        log.info("Assigning plan '{}' to user ID {}", normalizedPlan, currentUserId);

        final Instant now = Instant.now();
        final String generatedKey = generateLicenseKey(normalizedPlan);

        final License license = licenseRepository.findById(currentUserId).map(existingLicense -> {
            existingLicense.setPlan(normalizedPlan);
            existingLicense.setLicenseKey(generatedKey);
            existingLicense.setExpiresAt(now.plus(DEFAULT_SUBSCRIPTION_DAYS, ChronoUnit.DAYS));
            existingLicense.setUpdatedAt(now);
            return existingLicense;
        }).orElseGet(() -> License.builder()
            .userId(currentUserId)
            .licenseKey(generatedKey)
            .plan(normalizedPlan)
            .expiresAt(now.plus(DEFAULT_SUBSCRIPTION_DAYS, ChronoUnit.DAYS))
            .createdAt(now)
            .updatedAt(now)
            .build());

        final License savedLicense = licenseRepository.save(license);
        log.info("Plan '{}' successfully assigned to user ID {}. Valid until: {}",
            normalizedPlan, currentUserId, savedLicense.getExpiresAt());

        return LicenseStatusResponse.fromEntity(savedLicense);
    }

    /**
     * Activates or renews an existing license key for the current user.
     *
     * @param licenseKey the formatted key string (format: {@code WS-XXX-XXXX-XXXX-XXXX})
     * @return the renewed {@link LicenseStatusResponse}
     */
    @Transactional
    public LicenseStatusResponse renew(
        @NotBlank(message = "License key must not be blank")
        @Pattern(
            regexp = "^WS-[A-Z0-9]{3}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$",
            message = "License key must match the format WS-XXX-XXXX-XXXX-XXXX"
        ) final String licenseKey
    ) {
        final UUID currentUserId = userService.getCurrentUser().getId();
        log.info("Renewing license for user ID {}", currentUserId);

        final String plan = resolvePlanFromKey(licenseKey);
        final Instant now = Instant.now();

        final License license = licenseRepository.findById(currentUserId).map(existingLicense -> {
            final Instant baseTime = existingLicense.isExpired() ? now : existingLicense.getExpiresAt();

            existingLicense.setLicenseKey(licenseKey);
            existingLicense.setPlan(plan);
            existingLicense.setExpiresAt(baseTime.plus(DEFAULT_SUBSCRIPTION_DAYS, ChronoUnit.DAYS));
            existingLicense.setUpdatedAt(now);
            return existingLicense;
        }).orElseGet(() -> License.builder()
            .userId(currentUserId)
            .licenseKey(licenseKey)
            .plan(plan)
            .expiresAt(now.plus(DEFAULT_SUBSCRIPTION_DAYS, ChronoUnit.DAYS))
            .createdAt(now)
            .updatedAt(now)
            .build());

        final License savedLicense = licenseRepository.save(license);
        log.info("License successfully saved for user ID {}. Valid until: {}",
            currentUserId, savedLicense.getExpiresAt());

        return LicenseStatusResponse.fromEntity(savedLicense);
    }

    /**
     * Generates a cryptographically secure license key matching the format {@code WS-XXX-XXXX-XXXX-XXXX}.
     *
     * <p>Example output:
     * <pre>{@code
     * generateLicenseKey("PRO"); // "WS-PRO-A9F2-4KC1-89PQ"
     * }</pre>
     *
     * @param plan target subscription tier name
     * @return formatted license key string
     */
    private String generateLicenseKey(final String plan) {
        final String prefix = switch (Objects.requireNonNullElse(plan, "").toUpperCase()) {
            case "ENTERPRISE" -> "ENT";
            case "PRO" -> "PRO";
            case "COMMUNITY" -> "COM";
            default -> "NON";
        };

        return String.format("WS-%s-%s-%s-%s", prefix, randomBlock(4), randomBlock(4), randomBlock(4));
    }

    /**
     * Determines the subscription plan based on key prefixes.
     *
     * @param key the license key string
     * @return the resolved plan name
     */
    private String resolvePlanFromKey(final String key) {
        if (key == null) {
            return "NONE";
        }
        if (key.startsWith("WS-ENT-")) {
            return "ENTERPRISE";
        }
        if (key.startsWith("WS-PRO-")) {
            return "PRO";
        }
        if (key.startsWith("WS-COM-")) {
            return "COMMUNITY";
        }
        return "NONE";
    }

    /**
     * Produces an alphanumeric random string block of specified length.
     *
     * @param length the target block size
     * @return random alphanumeric string
     */
    private String randomBlock(final int length) {
        final StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}