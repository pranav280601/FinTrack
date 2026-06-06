// package com.fintrack.api_gateway.config;

// import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
// import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
// import org.springframework.cloud.gateway.route.RouteLocator;
// import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Primary;
// import reactor.core.publisher.Mono;

// @Configuration
// public class GatewayConfig {

//         @Bean
//     public RedisRateLimiter redisRateLimiter() {
//         return new RedisRateLimiter(10, 20, 1);
//     }

//     @Bean
//     @Primary
//     public KeyResolver userKeyResolver() {
//         return exchange -> {
//             String authHeader = exchange.getRequest()
//                 .getHeaders()
//                 .getFirst("Authorization");

//             if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                 return Mono.just(authHeader.substring(7,
//                     Math.min(authHeader.length(), 50)));
//             }

//             return Mono.just(exchange.getRequest()
//                 .getRemoteAddress()
//                 .getAddress()
//                 .getHostAddress());
//         };
//     }

//     @Bean
//     public RouteLocator customRouteLocator(
//             RouteLocatorBuilder builder,
//             RedisRateLimiter redisRateLimiter,
//             KeyResolver userKeyResolver) {

//         return builder.routes()

//             .route("user-service", r -> r
//                 .path("/api/auth/**")
//                 .uri("http://localhost:8081"))

//             .route("transaction-service", r -> r
//                 .path("/api/transactions/**", "/api/categories/**")
//                 .filters(f -> f
//                     .requestRateLimiter(c -> {
//                         c.setRateLimiter(redisRateLimiter);
//                         c.setKeyResolver(userKeyResolver);
//                     }))
//                 .uri("http://localhost:8082"))

//             .route("analytics-service", r -> r
//                 .path("/api/analytics/**")
//                 .filters(f -> f
//                     .requestRateLimiter(c -> {
//                         c.setRateLimiter(redisRateLimiter);
//                         c.setKeyResolver(userKeyResolver);
//                     }))
//                 .uri("http://localhost:8083"))

//             .build();
//     }
// }

package com.fintrack.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/auth/**")
                .uri("http://localhost:8081"))
            .route("transaction-service", r -> r
                .path("/api/transactions/**", "/api/categories/**")
                .uri("http://localhost:8082"))
            .route("analytics-service", r -> r
                .path("/api/analytics/**")
                .uri("http://localhost:8083"))
            .build();
    }
}