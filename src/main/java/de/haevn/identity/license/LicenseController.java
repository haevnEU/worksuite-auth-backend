package de.haevn.identity.license;
import de.haevn.identity.common.RestApiController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestApiController("/api/v1/user-service/license")
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping("/status")
    public ResponseEntity<LicenseStatusResponse> getStatus() {
        final LicenseStatusResponse status = licenseService.getCurrentLicenseStatus();
        return ResponseEntity.ok(status);
    }

    @PostMapping("/renew")
    public ResponseEntity<LicenseStatusResponse> renewLicense(
        @Valid @RequestBody final RenewLicenseRequest request
    ) {
        final LicenseStatusResponse renewedLicense = licenseService.renew(request.licenseKey());
        return ResponseEntity.ok(renewedLicense);
    }
}