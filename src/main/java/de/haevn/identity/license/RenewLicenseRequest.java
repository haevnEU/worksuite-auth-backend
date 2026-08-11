package de.haevn.identity.license;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data transfer object representing a request payload to renew or assign a subscription key.
 *
 * @param licenseKey the formatted license key string adhering to standard workspace key prefixes
 */
@Schema(description = "Payload required to renew or apply a license key")
public record RenewLicenseRequest(

    @Schema(description = "Formatted workspace license key (e.g., WS-PRO-XXXX-XXXX-XXXX)",
        example = "WS-PRO-A9F2-K891-B421", pattern = "^WS-[A-Z0-9]{3,4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$",
        requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "License key must not be blank") @Pattern(
        regexp = "^WS-[A-Z0-9]{3,4}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$",
        message = "License key must match the format WS-XXX-XXXX-XXXX-XXXX") String licenseKey) {
}