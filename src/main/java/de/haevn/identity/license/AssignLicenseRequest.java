package de.haevn.identity.license;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for assigning or switching a user's subscription tier.
 *
 * <p>Example instantiation:
 * <pre>{@code
 * AssignLicenseRequest request = new AssignLicenseRequest("PRO");
 * }</pre>
 *
 * @param plan the target subscription tier (e.g. COMMUNITY, PRO, ENTERPRISE)
 */
@Schema(description = "Payload for assigning or switching a workspace license plan")
public record AssignLicenseRequest(

    @NotBlank(message = "Plan must not be blank")
    @Pattern(
        regexp = "^(COMMUNITY|PRO|ENTERPRISE)$",
        message = "Plan must be one of: COMMUNITY, PRO, ENTERPRISE"
    )
    @Schema(
        description = "Target subscription plan tier",
        example = "PRO",
        allowableValues = {"COMMUNITY", "PRO", "ENTERPRISE"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String plan
) {}