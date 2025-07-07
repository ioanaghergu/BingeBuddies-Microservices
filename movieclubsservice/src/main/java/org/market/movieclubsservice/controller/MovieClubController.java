package org.market.movieclubsservice.controller;

import jakarta.validation.Valid;
import org.market.movieclubsservice.client.UserClient;
import org.market.movieclubsservice.dto.MovieClubDTO;
import org.market.movieclubsservice.dto.ScreeningEventDTO;
import org.market.movieclubsservice.exception.MovieClubAlreadyExistsException;
import org.market.movieclubsservice.exception.MovieClubNotFoundException;
import org.market.movieclubsservice.exception.PermissionDeniedException;
import org.market.movieclubsservice.service.MovieClubService;
import org.market.movieclubsservice.service.ScreeningEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clubs")
public class MovieClubController {
    private final MovieClubService movieClubService;
    private final ScreeningEventService eventService;
    private final UserClient userClient;

    public MovieClubController(MovieClubService movieClubService, ScreeningEventService eventService, UserClient userClient) {
        this.movieClubService = movieClubService;
        this.eventService = eventService;
        this.userClient = userClient;
    }

    private Long getUserIdFromJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return userClient.getUserIdByKeycloakId(jwt.getSubject());
        }
        throw new IllegalStateException("User not authenticated or JWT principal not found.");
    }

    @GetMapping
    public ResponseEntity<List<MovieClubDTO>> getAllClubs() {
        List<MovieClubDTO> clubs = movieClubService.getPublicMovieClubs();
        return new ResponseEntity<>(clubs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieClubDTO> getClubById(@PathVariable Long id) {
        try{
            MovieClubDTO clubDTO = movieClubService.getMovieClubById(id).orElseThrow(() -> new MovieClubNotFoundException("Movie club with id " + id + " not found."));
            return new ResponseEntity<>(clubDTO, HttpStatus.OK);
        }catch (MovieClubNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> addClub(@Valid @RequestBody MovieClubDTO clubDTO) {
        try{
            MovieClubDTO savedMovieClub = movieClubService.createMovieClub(clubDTO, getUserIdFromJwt());
            return new ResponseEntity<>(savedMovieClub, HttpStatus.CREATED);
        }catch (MovieClubAlreadyExistsException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (Exception e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error adding movie club: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateClub(@PathVariable Long id, @Valid @RequestBody MovieClubDTO clubDTO) {
        try{
            MovieClubDTO updatedMovieClub = movieClubService.updateMovieClub(id, clubDTO, getUserIdFromJwt());
            return new ResponseEntity<>(updatedMovieClub, HttpStatus.OK);
        }catch (MovieClubNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (MovieClubAlreadyExistsException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (PermissionDeniedException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
        } catch (Exception e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error updating movie club: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> deleteClub(@PathVariable Long id) {
        try{
            movieClubService.deleteMovieClub(id, getUserIdFromJwt());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (MovieClubNotFoundException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (PermissionDeniedException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
        }catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/join/{clubId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> joinClub(@PathVariable Long clubId) {
        try {
            boolean joined = movieClubService.joinMovieClub(clubId, getUserIdFromJwt());
            if (joined) {
                return new ResponseEntity<>("Succesfully joined club " + clubId, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Failed to join club " + clubId, HttpStatus.BAD_REQUEST);
            }
        }catch (MovieClubNotFoundException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error joining movie club: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/remove/{clubId}/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> removeClub(@PathVariable Long clubId, @PathVariable Long memberId) {
        try {
            boolean removed = movieClubService.removeMemberFromClub(clubId, memberId, getUserIdFromJwt());

            if (removed) {
                return new ResponseEntity<>("Succesfully removed member " + memberId, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>("Failed to remove member " + memberId, HttpStatus.BAD_REQUEST);
            }
        }catch (MovieClubNotFoundException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (PermissionDeniedException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.FORBIDDEN);
        }catch (Exception e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error removing member from movie club: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clubId}/events")
    public ResponseEntity<?> getUpcomingEvents(@PathVariable Long clubId) {
        try {
            List<ScreeningEventDTO> events = eventService.getUpcomingScreeningEventsForClub(clubId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        }catch (MovieClubNotFoundException e){
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
