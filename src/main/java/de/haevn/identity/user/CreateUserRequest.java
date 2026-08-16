package de.haevn.identity.user;

public record CreateUserRequest(String username, String password, String firstname, String lastName, String role,
                                String vcsKey, String redmineKey, String avatarUrl) {
}