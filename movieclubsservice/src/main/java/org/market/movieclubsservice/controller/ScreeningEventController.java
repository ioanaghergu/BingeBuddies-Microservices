package org.market.movieclubsservice.controller;

import jakarta.validation.Valid;
import org.market.movieclubsservice.client.Client;
import org.market.movieclubsservice.dto.ScreeningEventDTO;
import org.market.movieclubsservice.exception.MovieClubNotFoundException;
import org.market.movieclubsservice.exception.MovieNotFoundException;
import org.market.movieclubsservice.service.MovieClubService;
import org.market.movieclubsservice.service.ScreeningEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/events")
public class ScreeningEventController {
    private final ScreeningEventService screeningEventService;
    private final MovieClubService movieClubService;
    private final Client userService;

    public ScreeningEventController(ScreeningEventService screeningEventService, MovieClubService movieClubService, Client userService) {
        this.screeningEventService = screeningEventService;
        this.movieClubService = movieClubService;
        this.userService = userService;
    }

    @GetMapping("/upcoming/{clubId}")
    public ResponseEntity<?> getUpcomingEventsForClub(@PathVariable Long clubId) {
        try {
            if (movieClubService.getMovieClubById(clubId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Movie club with ID " + clubId + " not found.");
            }
            List<ScreeningEventDTO> events = screeningEventService.getUpcomingScreeningEventsForClub(clubId);
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (MovieClubNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error retrieving upcoming events: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScreeningEventDetails(@PathVariable Long id) {
        try {
            Optional<ScreeningEventDTO> eventDTO = screeningEventService.getScreeningEvent(id);
            if (eventDTO.isPresent()) {
                return new ResponseEntity<>(eventDTO.get(), HttpStatus.OK);
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("errorMessage", "Error retrieving screening event: " + id);
                return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error retrieving screening event: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{clubId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> addScreeningEvent(@PathVariable Long clubId, @Valid @RequestBody ScreeningEventDTO screeningEventDTO) {
        try {
            ScreeningEventDTO savedEvent = screeningEventService.addScreeningEvent(clubId, screeningEventDTO);
            return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
        } catch (MovieClubNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Movie club with id " + clubId + " not found");
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (MovieNotFoundException e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Movie with id " + screeningEventDTO.getMovieId() + " not found");
            return new ResponseEntity<>(errors, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error adding screening event: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
