package org.market.movieclubsservice.client;

import org.market.movieclubsservice.config.FeignClientConfig;
import org.market.movieclubsservice.dto.MovieDTO;
import org.market.movieclubsservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "gatewayserver", configuration = FeignClientConfig.class)
public interface Client {

    @GetMapping("/api/v1/movies/{id}")
    MovieDTO getMovieById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/users/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/users/username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/api/v1/users/by-keycloak-id/{keycloakId}")
    Long getUserIdByKeycloakId(@PathVariable("keycloakId") String keycloakId);

}
