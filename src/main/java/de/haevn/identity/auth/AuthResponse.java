package de.haevn.identity.auth;

import de.haevn.identity.user.UserDTO;

public record AuthResponse(String token, UserDTO user) {
}