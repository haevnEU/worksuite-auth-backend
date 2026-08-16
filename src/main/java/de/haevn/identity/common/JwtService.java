package de.haevn.identity.common;

import de.haevn.identity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret:SuperSecretKeyWhichIsAtLeast32BytesLongForHS256Algorithm!}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}") // 24 Stunden
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(final User user) {
        final Date now = new Date();
        final Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder().subject(user.getId().toString()).claim("username", user.getUsername())
            .claim("role", user.getRole()).issuedAt(now).expiration(expiryDate).signWith(getSigningKey()).compact();
    }

    public String extractUsername(final String jwt) {
        return extractClaim(jwt, claims -> claims.get("username", String.class));
    }

    public UUID extractUserId(final String jwt) {
        final String subject = extractClaim(jwt, Claims::getSubject);
        return subject != null ? UUID.fromString(subject) : null;
    }

    public boolean isTokenValid(final String jwt, final UserDetails userDetails) {
        try {
            final String username = extractUsername(jwt);
            return (username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(jwt));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isTokenExpired(final String jwt) {
        final Date expiration = extractClaim(jwt, Claims::getExpiration);
        return expiration != null && expiration.before(new Date());
    }

    public <T> T extractClaim(final String jwt, final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(jwt);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(final String jwt) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(jwt).getPayload();
    }
}