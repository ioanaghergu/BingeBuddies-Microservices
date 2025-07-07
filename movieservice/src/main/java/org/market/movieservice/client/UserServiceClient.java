package org.market.movieservice.client;

import org.market.movieservice.config.FeignClientConfig;
import org.market.movieservice.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "userservice", configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    Optional<UserResponseDTO> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/users/by-keycloak-id/{keycloakId}")
    Long getUserIdByKeycloakId(@PathVariable("keycloakId") String keycloakId);
}

/*

import jakarta.ws.rs.core.HttpHeaders;
import org.market.movieservice.dto.UserResponseDTO;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServletBearerExchangeFilterFunction;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserServiceClient {
    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.filter(new ServletBearerExchangeFilterFunction()).build();
    }

    public Mono<UserResponseDTO> getUserById(Long userId) {
        return webClient
                .get()
                .uri("lb://USERSERVICE/api/v1/users/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION)
                .retrieve()
                .bodyToMono(UserResponseDTO.class);
    }

    public Mono<Long> getUserIdByKeycloakId(String keycloakId) {
        return webClient
                .get()
                .uri("lb://USERSERVICE/api/v1/users/get-by-keycloak-id/{keycloakId}", keycloakId)
                .header(HttpHeaders.AUTHORIZATION)
                .retrieve()
                .bodyToMono(Long.class);
    }

}*/
