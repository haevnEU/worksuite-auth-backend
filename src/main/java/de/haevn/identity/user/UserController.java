package de.haevn.identity.user;

import de.haevn.identity.auth.AuthResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST API controller managing user profiles, account actions, and credentials.
 *
 * <p>Interacts with {@link UserService} to perform persistent updates and lookups.
 */
@Tag(name = "User Management", description = "Endpoints for user profile operations and credential updates")
@RequiredArgsConstructor
@RestApiController("/api/v1/user-service")
public class UserController {

    private final UserService userService;

    /**
     * Registers a new user account.
     *
     * @param request the validated {@link CreateUserRequest} entity creation payload
     * @return a {@link ResponseEntity} containing the created {@link UserDTO} and HTTP status {@code 201 Created}
     */
    @Operation(summary = "Create user account",
        description = "Registers a new user record within the central database.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "User created successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid payload constraints", content = @Content),
        @ApiResponse(responseCode = "409", description = "Username already in use", content = @Content)})
    @PostMapping("/register")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody final CreateUserRequest request) {
        final UserDTO createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    /**
     * Authenticates credentials and returns a JWT response.
     *
     * @param request the validated {@link LoginRequest} payload
     * @return a {@link ResponseEntity} containing the {@link AuthResponse}
     */
    @Operation(summary = "Log in user",
        description = "Verifies user credentials and issues a fresh JWT authentication token.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Authentication successful",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials provided", content = @Content)})
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody final LoginRequest request) {
        final AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
    /**
     * Re-authenticates an existing user via their unique user identifier.
     * <p>
     * Validates the provided credentials against the database and issues a fresh JWT access token
     * upon successful verification.
     * </p>
     *
     * @param request the {@link ReauthDTO} containing the user ID and password
     * @return a {@link ResponseEntity} containing the {@link AuthResponse} with the JWT token and user profile
     */
    @Operation(
        summary = "Re-authenticate by user ID",
        description = "Authenticates a user using their UUID and password to issue a new JWT access token."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User successfully authenticated. Fresh token and user profile returned.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = AuthResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation failed for the request payload (e.g., missing ID or empty password).",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials. User ID does not exist or password mismatch.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
        )
    })
    @PostMapping("/login/id")
    public ResponseEntity<AuthResponse> loginById(@Valid @RequestBody final ReauthDTO request) {
        final AuthResponse response = userService.loginById(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Verifies the existing password and sets a newly specified password for the target user.
     *
     * @param request the validated {@link PasswordChange} payload
     * @return a {@link ResponseEntity} containing the updated {@link AuthResponse} with a refreshed token
     */
    @Operation(summary = "Change user password",
        description = "Updates the account password after confirming current credentials and returns an updated token.",
        security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Password changed successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid password change payload", content = @Content),
        @ApiResponse(responseCode = "401", description = "Current password verification failed", content = @Content),
        @ApiResponse(responseCode = "404", description = "Target user ID not found", content = @Content)})
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody final PasswordChange request) {
        final AuthResponse response = userService.changePassword(request);
        return ResponseEntity.ok(response);
    }
}