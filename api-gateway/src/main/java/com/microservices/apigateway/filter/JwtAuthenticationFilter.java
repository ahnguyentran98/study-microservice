package com.microservices.apigateway.filter;

import com.microservices.apigateway.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    @Autowired
    private JwtUtils jwtUtils;
    
    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
            "/api/users/register",
            "/api/users/login",
            "/health",
            "/info",
            "/actuator"
    );
    
    // Admin-only endpoints
    private static final List<String> ADMIN_ENDPOINTS = Arrays.asList(
            "/api/users/admin",
            "/api/products/admin"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.debug("🔐 JWT Filter processing: {} {}", method, path);
        
        // Allow public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("✅ Public endpoint, allowing access: {}", path);
            return chain.filter(exchange);
        }
        
        // Extract JWT token from Authorization header
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            log.warn("❌ No JWT token found in request to protected endpoint: {}", path);
            return handleUnauthorized(exchange, "Missing Authorization header");
        }
        
        // Validate JWT token
        if (!jwtUtils.validateJwtToken(token)) {
            log.warn("❌ Invalid JWT token for endpoint: {}", path);
            return handleUnauthorized(exchange, "Invalid JWT token");
        }
        
        // Extract user information from token
        String email = jwtUtils.getEmailFromJwtToken(token);
        String userId = jwtUtils.getUserIdFromJwtToken(token);
        String role = jwtUtils.getRoleFromJwtToken(token);
        
        if (email == null || userId == null || role == null) {
            log.warn("❌ Invalid JWT token claims for endpoint: {}", path);
            return handleUnauthorized(exchange, "Invalid JWT token claims");
        }
        
        // Check admin-only endpoints
        if (isAdminEndpoint(path) && !"ADMIN".equals(role)) {
            log.warn("❌ User {} attempted to access admin endpoint: {}", email, path);
            return handleForbidden(exchange, "Admin access required");
        }
        
        // Check user-specific endpoints (users can only access their own data)
        if (isUserSpecificEndpoint(path, method) && !hasUserAccess(path, userId, role)) {
            log.warn("❌ User {} attempted to access unauthorized resource: {}", email, path);
            return handleForbidden(exchange, "Access denied to this resource");
        }
        
        log.debug("✅ JWT authentication successful for user: {} ({})", email, role);
        
        // Add user information to headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build();
        
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
    
    private String extractTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private boolean isAdminEndpoint(String path) {
        return ADMIN_ENDPOINTS.stream().anyMatch(path::contains);
    }
    
    private boolean isUserSpecificEndpoint(String path, String method) {
        // Check if the endpoint is user-specific (e.g., /api/users/profile, /api/orders/user/{userId})
        return path.startsWith("/api/users/profile") || 
               path.matches("/api/users/\\d+") || 
               path.matches("/api/orders/user/\\d+");
    }
    
    private boolean hasUserAccess(String path, String userId, String role) {
        // Admin can access everything
        if ("ADMIN".equals(role)) {
            return true;
        }
        
        // Check if user is accessing their own profile
        if (path.startsWith("/api/users/profile")) {
            return true; // Profile endpoint is for the authenticated user
        }
        
        // Check if user is accessing their own data by ID
        if (path.matches("/api/users/\\d+")) {
            String pathUserId = path.substring(path.lastIndexOf('/') + 1);
            return userId.equals(pathUserId);
        }
        
        // Check if user is accessing their own orders
        if (path.matches("/api/orders/user/\\d+")) {
            String pathUserId = path.substring(path.lastIndexOf('/') + 1);
            return userId.equals(pathUserId);
        }
        
        return true; // Allow other endpoints for now
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\",\"status\":401}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    
    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");
        
        String body = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\",\"status\":403}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }
    
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1; // Run after logging filter
    }
}