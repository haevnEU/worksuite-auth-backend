package de.haevn.identity.config;

import de.haevn.identity.common.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that intercepts incoming HTTP requests to extract and validate JSON Web Tokens,
 * establishing authentication state in Spring Security's {@link SecurityContextHolder}.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Inspects the authorization header or query parameters for a JWT and authenticates the principal.
     *
     * @param request the current {@link HttpServletRequest}
     * @param response the current {@link HttpServletResponse}
     * @param filterChain the execution {@link FilterChain}
     * @throws ServletException in case of general servlet processing errors
     * @throws IOException in case of I/O communication errors
     */
    @Override
    protected void doFilterInternal(final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response, final @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String jwt = extractTokenFromHeader(request);

        // Fallback for direct browser URL navigation where tokens might be passed via query params
        if (jwt == null || jwt.isBlank()) {
            final String paramToken = request.getParameter("token");
            if (paramToken != null && !paramToken.isBlank()) {
                // Safeguard against duplicated query parameters (e.g. "token1,token2")
                jwt = paramToken.contains(",") ? paramToken.split(",")[0].trim() : paramToken.trim();
            }
        }

        if (jwt != null && !jwt.isBlank() && jwtService.isTokenValid(jwt)) {
            final String username = jwtService.extractUsername(jwt);
            final List<GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final var authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the raw bearer token from the standard {@code Authorization} header.
     *
     * @param request the {@link HttpServletRequest} to read from
     * @return the token string, or {@code null} if the header is missing/invalid
     */
    private String extractTokenFromHeader(final HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return null;
    }
}