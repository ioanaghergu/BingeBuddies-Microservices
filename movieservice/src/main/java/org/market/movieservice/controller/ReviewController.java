package org.market.movieservice.controller;

import jakarta.validation.Valid;
import org.market.movieservice.client.UserIntegrationService;
import org.market.movieservice.client.UserServiceClient;
import org.market.movieservice.dto.ReviewDTO;
import org.market.movieservice.dto.UserResponseDTO;
import org.market.movieservice.exceptions.*;
import org.market.movieservice.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UserServiceClient userServiceClient;
    private final UserIntegrationService userIntegrationService;

    public ReviewController(ReviewService reviewService, UserServiceClient userServiceClient, UserIntegrationService userIntegrationService) {
        this.reviewService = reviewService;
        this.userServiceClient = userServiceClient;
        this.userIntegrationService = userIntegrationService;
    }

    private Long getUserIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            //return userServiceClient.getUserIdByKeycloakId(jwt.getSubject());
            try {
                return userIntegrationService.getUserIdByKeycloakId(jwt.getSubject()).get();
            } catch (UnavailableService e) {
                throw new UnavailableService("User service unavailable. Please try again later.");
            } catch (Exception e) {
                throw new UnavailableService("User service unavailable. Please try again later.");
            }
        }
            throw new IllegalStateException("User not authenticated or JWT principal not found.");

    }

    @GetMapping("/test/{userId}")
    public ResponseEntity<String> testUserLoadBalancing(@PathVariable Long userId) {
        try{
            Optional<UserResponseDTO> userResponseDTO = userServiceClient.getUserById(userId);

            if(userResponseDTO != null) {
                String response = "Movie Service received user " + userResponseDTO.get().getUsername() + " from User Service running on port: " + userResponseDTO.get().getInstancePort();
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        }catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/movies/{movieId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addReview(@PathVariable Long movieId,
                                       @Valid @RequestBody ReviewDTO reviewDTO) {

        Long userId = null;
        try {
             userId= getUserIdFromJwt();
        }catch (UnavailableService e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            reviewDTO.setMovieId(movieId);
            ReviewDTO savedReview = reviewService.addReview(movieId, userId, reviewDTO);
            savedReview.setUsername(userIntegrationService.getUserById(userId).get().get().getUsername());
            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
        } catch (MovieNotfoundException | UserNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (ReviewAlreadyExistsException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        } catch (UnavailableService e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);

        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Error adding review: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> updateReview(@PathVariable Long reviewId,
                                          @Valid @RequestBody ReviewDTO reviewDTO) {
        Long userId = null;
        try {
            userId= getUserIdFromJwt();
        }catch (UnavailableService e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            ReviewDTO updatedReview = reviewService.updateReview(reviewId, userId, reviewDTO);
            return new ResponseEntity<>(updatedReview, HttpStatus.OK);
        } catch (ReviewNotFoundException | PermissionDeniedException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("error", "Error updating review: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        Long userId = null;
        try {
            userId= getUserIdFromJwt();
        }catch (UnavailableService e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }

        try {
            reviewService.deleteReview(reviewId, userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ReviewNotFoundException | PermissionDeniedException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
