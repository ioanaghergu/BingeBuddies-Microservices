package org.market.movieservice.client;

import org.market.movieservice.dto.UserResponseDTO;
import org.market.movieservice.exceptions.UnavailableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class UserIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(UserIntegrationService.class);
    private final UserServiceClient userServiceClient;
    private final Executor applicationTaskExecutor;

    public UserIntegrationService(UserServiceClient userServiceClient, @Qualifier("applicationTaskExecutor") Executor applicationTaskExecutor) {
        this.userServiceClient = userServiceClient;
        this.applicationTaskExecutor = applicationTaskExecutor;
    }

    @CircuitBreaker(name = "userByIdCircuitBreaker", fallbackMethod = "getUserByIdFallback")
    @TimeLimiter(name = "userByIdTimeLimiter")
    public CompletableFuture<Optional<UserResponseDTO>> getUserById(Long userId) {
        log.info("Attempting to fetch user {} from UserService", userId);
        return CompletableFuture.supplyAsync(() -> userServiceClient.getUserById(userId), applicationTaskExecutor);
    }

    private CompletableFuture<Optional<UserResponseDTO>> getUserByIdFallback(Long userId, Throwable t) {
        log.error("Fallback for getUserById triggered for userId: {}. Reason: {}", userId, t.getMessage());
        throw new UnavailableService("User service is currently unavailable. Please try again later.");
    }

    @CircuitBreaker(name = "userIdByKeycloakCircuitBreaker", fallbackMethod = "getUserIdByKeycloakIdFallback")
    @TimeLimiter(name = "userIdByKeycloakTimeLimiter")
    public CompletableFuture<Long> getUserIdByKeycloakId(String keycloakId) {
        log.info("Attempting to fetch user ID for Keycloak ID {} from UserService", keycloakId);
        return CompletableFuture.supplyAsync(() -> userServiceClient.getUserIdByKeycloakId(keycloakId), applicationTaskExecutor);
    }

    private CompletableFuture<Long> getUserIdByKeycloakIdFallback(String keycloakId, Throwable t) {
        log.error("Fallback for getUserIdByKeycloakId triggered for keycloakId: {}. Reason: {}", keycloakId, t.getMessage());
        throw new UnavailableService("User service is currently unavailable. Please try again later.");
    }
}