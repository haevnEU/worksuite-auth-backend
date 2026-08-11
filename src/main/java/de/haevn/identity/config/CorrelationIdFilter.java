package de.haevn.identity.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter responsible for extracting or generating a unique correlation ID
 * for distributed tracing, binding it to the logging {@link MDC} and outgoing headers.
 */
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    /**
     * Processes each HTTP request by initializing and attaching the correlation identifier.
     *
     * @param request the incoming {@link HttpServletRequest}
     * @param response the outgoing {@link HttpServletResponse}
     * @param filterChain the servlet {@link FilterChain}
     * @throws ServletException if a servlet exception occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(final @NonNull HttpServletRequest request,
        final @NonNull HttpServletResponse response, final @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        try {
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            MDC.put(MDC_KEY, correlationId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}