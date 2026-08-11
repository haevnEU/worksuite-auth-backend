package de.haevn.identity.user;

import de.haevn.identity.auth.AuthResponse;
import de.haevn.identity.common.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service managing user lifecycle, credential verification, and authentication context lookups.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Creates and persists a new {@link User} entity.
     *
     * @param request registration payload
     * @return the created {@link UserDTO}
     * @throws ResponseStatusException if the username is already in use (HTTP 409)
     */
    @Transactional
    public UserDTO createUser(final CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Username '" + request.username() + "' is already taken.");
        }

        final User user =
            User.builder().username(request.username()).passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstname()).lastName(request.lastName())
                .role(request.role() != null ? request.role() : "DEVELOPER").vcsKey(request.vcsKey())
                .redmineKey(request.redmineKey()).avatarUrl(request.avatarUrl()).build();

        final User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    /**
     * Validates user credentials and issues an authentication token.
     *
     * @param request the login payload
     * @return {@link AuthResponse} containing the JWT and user information
     * @throws ResponseStatusException if credentials do not match (HTTP 401)
     */
    @Transactional(readOnly = true)
    public AuthResponse login(final LoginRequest request) {
        final User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        final String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserDTO.fromEntity(user));
    }

    /**
     * Changes a user password after verifying current credentials.
     *
     * @param request payload containing old and new passwords
     * @return refreshed {@link AuthResponse}
     * @throws ResponseStatusException if user is not found (HTTP 404) or password invalid (HTTP 401)
     */
    @Transactional
    public AuthResponse changePassword(final @Valid PasswordChange request) {
        final User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        final String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserDTO.fromEntity(user));
    }

    /**
     * Extracts and retrieves the {@link User} entity belonging to the current security context.
     *
     * @return the authenticated {@link User}
     * @throws ResponseStatusException if no authenticated user is present (HTTP 401) or user not found (HTTP 404)
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check for absent authentication or anonymous principal placeholder
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(
            authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user in context.");
        }

        final String username = authentication.getName();

        return userRepository.findByUsername(username).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found: " + username));
    }

    /**
     * Retrieves a {@link User} entity by username.
     *
     * @param username the target username
     * @return the {@link User} entity
     * @throws ResponseStatusException if the user does not exist (HTTP 404)
     */
    @Transactional(readOnly = true)
    public User getByUsername(final String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }
}