package org.market.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("users-service-route", r -> r.path("/api/v1/users/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://USERSERVICE"))

                .route("auth-service-route", r -> r.path("/api/v1/auth/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://USERSERVICE"))

                .route("movies-service-route", r -> r.path("/api/v1/movies/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://MOVIESERVICE"))

                .route("reviews-service-route", r -> r.path("/api/v1/reviews/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://MOVIESERVICE"))

                .route("clubs-service-route", r -> r.path("/api/v1/clubs/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://MOVIECLUBSSERVICE"))

                .route("events-service-route", r -> r.path("/api/v1/events/**")
                        .filters(f -> f.tokenRelay())
                        .uri("lb://MOVIECLUBSSERVICE"))
                .build();
    }
}
