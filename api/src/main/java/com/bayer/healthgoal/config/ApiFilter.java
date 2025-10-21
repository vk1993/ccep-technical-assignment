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
        MDC.put("correlationId",correlationId);
        try {
            if (!x_api_key.equals(xapi)) {
                log.error("missing or invalid API key | URI={} | x-correlation-id={} | x-request-id={}",
                        request.getRequestURI(), correlationId, requestId);

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("""
                        {
                          "error": "unauthorized",
                          "message": "Missing or invalid API key",
                          "traceId": "%s"
                        }
                        """.formatted(correlationId != null ? correlationId : "N/A"));
                return;
            }
            log.info("Authorized request | URI={} | x-correlation-id={} | x-request-id={}",
                    request.getRequestURI(), correlationId, requestId);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            MDC.clear();
        }
    }

    // jsut to exclude /health and /info
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return excludedUrl.stream().anyMatch(url -> url.equals(request.getRequestURI()));
    }
}
