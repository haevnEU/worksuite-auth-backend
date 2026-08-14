package de.haevn.authentification.license;

import de.haevn.authentification.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final UserService userService;

    /**
     * Prüft, ob die Lizenz für eine bestimmte User-ID abgelaufen ist.
     * Wird direkt im Interceptor aufgerufen.
     */
    @Transactional(readOnly = true)
    public boolean licenseExpired(final UUID userId) {
        if (userId == null) {
            return true;
        }

        return licenseRepository.findById(userId)
            .map(License::isExpired)
            .orElse(true); // Keine Lizenz vorhanden -> als abgelaufen behandeln
    }

    /**
     * Ruft den aktuellen Lizenzstatus des eingeloggten Benutzers ab.
     */
    @Transactional(readOnly = true)
    public LicenseStatusResponse getCurrentLicenseStatus() {
        final UUID currentUserId = userService.getCurrentUser().getId();

        return licenseRepository.findById(currentUserId)
            .map(LicenseStatusResponse::fromEntity)
            .orElseGet(() -> LicenseStatusResponse.notFound(currentUserId));
    }

    /**
     * Erneuert die Lizenz des aktuell eingeloggten Benutzers.
     */
    @Transactional
    public LicenseStatusResponse renew(
        final @NotBlank(message = "License key must not be blank")
        @Pattern(
            regexp = "^WS-[A-Z0-9]{3}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$",
            message = "License key must match the format WS-XXX-XXXX-XXXX-XXXX"
        ) String licenseKey
    ) {
        final UUID currentUserId = userService.getCurrentUser().getId();
        log.info("Renewing license for user {}", currentUserId);

        final String plan = resolvePlanFromKey(licenseKey);

        final License license = licenseRepository.findById(currentUserId)
            .map(existingLicense -> {
                final Instant baseTime = existingLicense.isExpired()
                    ? Instant.now()
                    : existingLicense.getExpiresAt();

                existingLicense.setLicenseKey(licenseKey);
                existingLicense.setPlan(plan);
                existingLicense.setExpiresAt(baseTime.plus(365, ChronoUnit.DAYS));
                existingLicense.setUpdatedAt(Instant.now());
                return existingLicense;
            })
            .orElseGet(() -> License.builder()
                .userId(currentUserId)
                .licenseKey(licenseKey)
                .plan(plan)
                .expiresAt(Instant.now().plus(365, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());

        final License savedLicense = licenseRepository.save(license);
        log.info("License successfully saved for user {}. Valid until: {}", currentUserId, savedLicense.getExpiresAt());

        return LicenseStatusResponse.fromEntity(savedLicense);
    }

    // --- Private Helper ---

    private String resolvePlanFromKey(final String key) {
        if (key.startsWith("WS-ENT-")) return "ENTERPRISE";
        if (key.startsWith("WS-PRO-")) return "PRO";
        return "COMMUNITY";
    }
}