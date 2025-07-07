package org.market.userservice.service.impl;

import org.market.userservice.domain.security.User;
import org.market.userservice.exceptions.UserAlreadyExists;
import org.market.userservice.exceptions.UserNotFoundException;
import org.market.userservice.repository.security.UserRepository;
import org.market.userservice.service.UserService;
import org.market.userservice.service.security.AuthorityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final AuthorityService authorityService;

    public UserServiceImpl(UserRepository userRepository, AuthorityService authorityService) {
        this.userRepository = userRepository;
        this.authorityService = authorityService;
        logger.info("UserServiceImpl initialized with UserRepository and AuthorityService.");
    }

    @Override
    @Transactional
    public User findOrCreateUser(String keycloakId, String username) {
        logger.info("Attempting to find or create user with Keycloak ID: {} and username: {}", keycloakId, username);
        Optional<User> existingUser = userRepository.findByUsername(username);

        if (existingUser.isPresent()) {
            logger.debug("User with username {} found. Updating Keycloak ID.", username);
            existingUser.get().setKeycloakId(keycloakId);
            User updatedUser = userRepository.save(existingUser.get());
            logger.info("Existing user {} updated and saved.", username);
            return updatedUser;
        }
        else {
            logger.debug("User with username {} not found. Creating new user.", username);
            User newUser = User.builder().keycloakId(keycloakId).username(username).authorities(Set.of(authorityService.getAuthorityByRole("ROLE_USER"))).build();
            User savedUser = userRepository.save(newUser);
            logger.info("New user {} created and saved successfully.", username);
            return savedUser;
        }
    }

    @Override
    public Optional<User> findByKeycloakId(String keycloakId) {
        logger.info("Attempting to find user by Keycloak ID: {}", keycloakId);
        Optional<User> user = userRepository.findByKeycloakId(keycloakId);
        if (user.isPresent()) {
            logger.debug("User found for Keycloak ID: {}.", keycloakId);
        } else {
            logger.debug("No user found for Keycloak ID: {}.", keycloakId);
        }
        return user;
    }

    @Override
    public User findById(Long id) {
        logger.info("Attempting to find user by ID: {}", id);
        Optional<User> user = userRepository.findById(id);

        if(user.isEmpty()){
            logger.warn("User with ID {} not found. Throwing UserNotFoundException.", id);
            throw new UserNotFoundException("User with id " + id + " not found");
        }

        logger.debug("User with ID {} found.", id);
        return user.get();
    }

    @Override
    public User findByUsername(String username) {
        logger.info("Attempting to find user by username: {}", username);
        Optional<User> user = userRepository.findByUsername(username);

        if(user.isEmpty()) {
            logger.warn("User with username '{}' not found. Throwing UserNotFoundException.", username);
            throw new UserNotFoundException("User " + username + " not found");
        }
        logger.debug("User with username '{}' found.", username);
        return user.get();
    }

    @Override
    @Transactional
    public User save(User user) {
        logger.info("Attempting to save new user with username: {}", user.getUsername());
        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        if (dbUser.isPresent()) {
            logger.warn("User with username '{}' already exists. Throwing UserAlreadyExists exception.", user.getUsername());
            throw new UserAlreadyExists("User " + user.getUsername() + " already exists.");
        }

        User savedUser = userRepository.save(user);
        logger.info("User '{}' saved successfully with ID: {}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    @Override
    public List<User> findAll() {
        logger.info("Fetching all users.");
        List<User> users = userRepository.findAll();
        logger.debug("Found {} users in total.", users.size());
        return users;
    }
}