package de.haevn.authentification;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank(message = "Username darf nicht leer sein") String username,
                                @NotBlank(message = "Passwort darf nicht leer sein") String password,
                                @NotBlank(message = "Vorname darf nicht leer sein") String firstName,
                                @NotBlank(message = "Nachname darf nicht leer sein") String lastName,
                                String role, String vcsKey, String redmineKey, String avatarUrl) {
}