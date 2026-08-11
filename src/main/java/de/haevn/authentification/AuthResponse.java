package de.haevn.authentification;
public record AuthResponse(
    String token,
    UserDTO user
) {}