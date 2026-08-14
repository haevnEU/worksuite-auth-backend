package de.haevn.authentification.license;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RenewLicenseRequest(
    @NotBlank(message = "License key must not be blank")
    @Pattern(
        regexp = "^WS-[A-Z0-9]{3}-[A-Z0-9]{4}-[A-Z0-9]{4}-[A-Z0-9]{4}$",
        message = "License key must match the format WS-XXX-XXXX-XXXX-XXXX"
    )
    String licenseKey
) {}