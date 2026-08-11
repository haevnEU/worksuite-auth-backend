package de.haevn.authentification;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Username '" + request.username() + "' ist bereits vergeben.");
        }
        final User user = User.builder().username(request.username()).passwordHash(passwordEncoder.encode(request.password()))
            .firstName(request.firstName()).lastName(request.lastName())
            .role(request.role() != null ? request.role() : "DEVELOPER").vcsKey(request.vcsKey())
            .redmineKey(request.redmineKey()).avatarUrl(request.avatarUrl()).build();

        final User savedUser = userRepository.save(user);
        return UserDTO.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        final User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials.");
        }

        final String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserDTO.fromEntity(user));
    }

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
}