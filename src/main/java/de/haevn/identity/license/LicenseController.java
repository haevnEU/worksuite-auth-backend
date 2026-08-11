package de.haevn.identity.license;

import de.haevn.identity.common.RestApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST API controller providing endpoints to query licensing tiers, assign plans, and apply renewal keys.
 *
 * <p>Delegates business operations such as status resolution and validation checks
 * to {@link LicenseService}.
 */
@Tag(name = "License & Subscription", description = "Endpoints for managing workspace subscriptions and license keys")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@RestApiController("/api/v1/user-service/license")
public class LicenseController {

    private final LicenseService licenseService;

    /**
     * Queries the license and subscription tier for the currently authenticated principal.
     *
     * @return a {@link ResponseEntity} wrapping the resolved {@link LicenseStatusResponse}
     */
    @Operation(summary = "Get current license status",
        description = "Resolves the active subscription tier, expiration timestamp, and license key of the calling user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "License status retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = LicenseStatusResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT bearer token",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Authenticated user context not found in storage",
            content = @Content)})
    @GetMapping("/status")
    public ResponseEntity<LicenseStatusResponse> getStatus() {
        final LicenseStatusResponse status = licenseService.getCurrentLicenseStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * Directly assigns or switches the subscription plan for the authenticated user.
     *
     * @param request the validated {@link AssignLicenseRequest} containing the target plan
     * @return a {@link ResponseEntity} containing the updated {@link LicenseStatusResponse}
     */
    @Operation(summary = "Assign license plan",
        description = "Directly assigns a new plan tier (COMMUNITY, PRO, or ENTERPRISE) to the active user account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plan successfully assigned",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = LicenseStatusResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid or unsupported plan tier provided",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Bearer token missing or invalid",
            content = @Content)})
    @PostMapping("/assign")
    public ResponseEntity<LicenseStatusResponse> assignPlan(@Valid @RequestBody final AssignLicenseRequest request) {
        final LicenseStatusResponse updatedLicense = licenseService.assignPlan(request.plan());
        return ResponseEntity.ok(updatedLicense);
    }

    /**
     * Applies a valid license key to renew or upgrade the user's workspace plan.
     *
     * @param request the validated {@link RenewLicenseRequest} containing the key string
     * @return a {@link ResponseEntity} containing the updated {@link LicenseStatusResponse}
     */
    @Operation(summary = "Renew license key",
        description = "Validates the provided license key format (WS-XXX-XXXX-XXXX-XXXX) and applies it to the active user account.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "License successfully renewed/applied",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = LicenseStatusResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid key format or malformed request payload",
            content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Bearer token missing or invalid",
            content = @Content)})
    @PostMapping("/renew")
    public ResponseEntity<LicenseStatusResponse> renewLicense(@Valid @RequestBody final RenewLicenseRequest request) {
        final LicenseStatusResponse renewedLicense = licenseService.renew(request.licenseKey());
        return ResponseEntity.ok(renewedLicense);
    }
}