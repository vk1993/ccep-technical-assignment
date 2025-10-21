package com.bayer.healthgoal.config;

import com.bayer.healthgoal.utlity.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ApiFilter extends OncePerRequestFilter {

    @Value("${healthgoal.api.x_api_key}")
    private String expectedApiKey;

    private static final List<String> EXCLUDED_PATHS = List.of(
            "/actuator",              // /actuator/*
            "/swagger-ui",            // /swagger-ui/*
            "/v3/api-docs",           // /v3/api-docs/*
            "/openapi.yaml",          // static YAML spec
            "/error"                  // allow default error endpoint
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // ✅ Skip filter if request URI starts with any whitelisted path
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader(ApiConstants.X_API_KEY);
        String correlationId = request.getHeader(ApiConstants.X_CORRELATION_ID);
        String requestId = request.getHeader(ApiConstants.X_REQUEST_ID);

        MDC.put("correlationId", correlationId);
        MDC.put("requestId", requestId);

        try {
            if (apiKey == null || !apiKey.equals(expectedApiKey)) {
                log.warn("Unauthorized | URI={} | correlationId={} | requestId={}",
                        request.getRequestURI(), correlationId, requestId);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "unauthorized", "Missing or invalid API key", correlationId);
                return;
            }

            log.debug("Authorized | URI={} | correlationId={} | requestId={}",
                    request.getRequestURI(), correlationId, requestId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Unhandled filter error | URI={} | correlationId={} | requestId={}",
                    request.getRequestURI(), correlationId, requestId, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "internal_error", e.getMessage(), correlationId);
        } finally {
            MDC.clear();
        }
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
