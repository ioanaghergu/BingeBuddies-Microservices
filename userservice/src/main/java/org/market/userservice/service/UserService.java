package org.market.userservice.service;

import org.market.userservice.domain.security.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User findByUsername(String username);
    User findById(Long id);
    User save(User user);
    List<User> findAll();
    Optional<User> findByKeycloakId(String keycloakId);
    public User findOrCreateUser(String keycloakId, String username);
}

