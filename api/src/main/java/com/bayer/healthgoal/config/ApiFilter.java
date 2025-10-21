package com.bayer.healthgoal.config;

import com.bayer.healthgoal.utlity.ApiConstants;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class ApiFilter extends OncePerRequestFilter {

    @Value("${healthgoal.api.x_api_key}")
    private String x_api_key;

    private final List<String> excludedUrl = List.of("/health","/info","/actuator");

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
//        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
//        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        String xapi = request.getHeader(ApiConstants.X_API_KEY);
        String correlationId = request.getHeader(ApiConstants.X_CORRELATION_ID);
        String requestId = request.getHeader(ApiConstants.X_REQUEST_ID);

        MDC.put("correlationId", correlationId);
        MDC.put("requestId", requestId);
        try {
            if (!x_api_key.equals(xapi)) {
                log.error("missing or invalid API key | URI={} | x-correlation-id={} | x-request-id={}",
                        request.getRequestURI(), correlationId, requestId);

                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "unauthorized", "Missing or invalid API key", correlationId);
                return;
            }
            log.info("Authorized request | URI={} | x-correlation-id={} | x-request-id={}",
                    request.getRequestURI(), correlationId, requestId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Unhandled error in filter | URI={} | correlationId={} | requestId={}",
                    request.getRequestURI(), correlationId, requestId, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "internal_error", e.getMessage(), correlationId);
        } finally {
            MDC.clear();
        }
    }

    // jsut to exclude /health and /info
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return excludedUrl.stream().anyMatch(url -> url.equals(request.getRequestURI()));
    }

    private void sendErrorResponse(HttpServletResponse response, int status,
                                   String error, String message, String correlationId) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                  "error": "%s",
                  "message": "%s",
                  "traceId": "%s"
                }
                """.formatted(error, message, correlationId != null ? correlationId : "missing_correlationId"));
    }
}
