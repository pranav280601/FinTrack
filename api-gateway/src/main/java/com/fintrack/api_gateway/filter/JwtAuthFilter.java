package com.fintrack.api_gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fintrack.api_gateway.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered{
    
    private final JwtUtil jwtUtil;

    //public endpoints that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){

        String path = exchange.getRequest().getPath().toString();
        log.info("Incoming request to path: {}", path);

        //allow public endpoints without authentication
        if(isPublicPath(path)){
            return chain.filter(exchange);
        }

        //Check for Authorization header 
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        //Validate JWT token

        String token = authHeader.substring(7); //remove "Bearer " prefix

        if(!jwtUtil.isTokenValid(token)){
            log.warn("Invalid JWT token for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Token is valid, proceed with the request
        
        String userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        log.info("Authenticated userId: {} accessing: {}", userId, path);

        //Add user context to headers for downstream services
        log.info("Forwarding request to downstream service: {}", path);
        ServerWebExchange modifiedExchange = exchange.mutate().request(
            exchange.getRequest().mutate()
            .header("X_User-Id", userId)
            .header("X_User-Role", role)
            .build()
        ).build();  
        
        return chain.filter(modifiedExchange);
    }

    @Override
    public int getOrder() {
        return -1; // Run before all other filters
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
            .anyMatch(path::startsWith);
    }
}
