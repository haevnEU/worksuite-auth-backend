package de.haevn.identity.common;

import de.haevn.identity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Service component handling the creation, parsing, and cryptographic verification
 * of JSON Web Tokens (JWT) using modern {@link Instant} timestamps.
 */
@Service
public class JwtService {

    @Value("${jwt.secret:SuperSecretKeyWhichIsAtLeast32BytesLongForHS256Algorithm!}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    /**
     * Computes the HMAC signing key based on the configured secret bytes.
     *
     * @return a cryptographic {@link SecretKey}
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT containing basic user claims and expiration timestamps.
     *
     * @param user the {@link User} entity for whom the token is generated
     * @return the serialized JWT string
     */
    public String generateToken(final User user) {
        final Instant now = Instant.now();
        final Instant expiryInstant = now.plus(expirationMs, ChronoUnit.MILLIS);

        return Jwts.builder().subject(user.getId().toString()).claim("username", user.getUsername())
            .claim("role", user.getRole()).issuedAt(Date.from(now)).expiration(Date.from(expiryInstant))
            .signWith(getSigningKey()).compact();
    }

    /**
     * Extracts the username claim from a token.
     *
     * @param jwt the raw JWT string
     * @return the associated username, or {@code null}
     */
    public String extractUsername(final String jwt) {
        return extractClaim(jwt, claims -> claims.get("username", String.class));
    }

    /**
     * Extracts the unique user identifier from the token's subject claim.
     *
     * @param jwt the raw JWT string
     * @return the subject as {@link UUID}, or {@code null} if parsing fails
     */
    public UUID extractUserId(final String jwt) {
        final String subject = extractClaim(jwt, Claims::getSubject);
        return subject != null ? UUID.fromString(subject) : null;
    }

    /**
     * Extracts the expiration instant directly from the token payload.
     *
     * @param jwt the raw JWT string
     * @return the expiration {@link Instant}, or {@code null}
     */
    public Instant extractExpiration(final String jwt) {
        return extractClaim(jwt, claims -> claims.getExpiration().toInstant());
    }

    /**
     * Extracts granted authorities from the role claim.
     *
     * @param jwt the raw JWT string
     * @return a list of {@link GrantedAuthority} objects
     */
    public List<GrantedAuthority> extractAuthorities(final String jwt) {
        final String role = extractClaim(jwt, claims -> claims.get("role", String.class));
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        final String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(formattedRole));
    }

    /**
     * Verifies that the token has a valid signature, contains a username, and is not expired.
     *
     * @param jwt the token to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean isTokenValid(final String jwt) {
        try {
            return !isTokenExpired(jwt) && extractUsername(jwt) != null;
        } catch (final JwtException | IllegalArgumentException _) {
            return false;
        }
    }

    /**
     * Checks if the token's expiration timestamp is in the past compared to the current UTC clock.
     *
     * @param jwt the raw token string
     * @return {@code true} if expired, {@code false} otherwise
     */
    public boolean isTokenExpired(final String jwt) {
        final Instant expiration = extractExpiration(jwt);
        return expiration != null && expiration.isBefore(Instant.now());
    }

    /**
     * Resolves an arbitrary claim from the token payload using a resolver function.
     *
     * @param jwt the token string
     * @param claimsResolver a function extracting the required value from {@link Claims}
     * @param <T> the type of the resolved value
     * @return the resolved claim value
     */
    public <T> T extractClaim(final String jwt, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and cryptographically validates the token against the HMAC secret.
     *
     * @param jwt the raw token string
     * @return the verified payload {@link Claims}
     */
    private Claims extractAllClaims(final String jwt) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(jwt).getPayload();
    }
}