package org.market.movieservice.controller;

import jakarta.validation.Valid;
import org.market.movieservice.dto.MovieDTO;
import org.market.movieservice.exceptions.MovieAlreadyExistsException;
import org.market.movieservice.exceptions.MovieNotfoundException;
import org.market.movieservice.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        List<MovieDTO> movies = movieService.getAllMovies();
        return new ResponseEntity<>(movies, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovieDTO> getMovieById(@PathVariable Long id) {
        try{
            MovieDTO movieDTO = movieService.getMovieById(id).orElseThrow(() -> new MovieNotfoundException("Movie with id " + id + " not found"));
            return new ResponseEntity<>(movieDTO, HttpStatus.OK);
        }catch (MovieNotfoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addMovie(@Valid @RequestBody MovieDTO movieDTO) {
        try{
            MovieDTO savedMovie = movieService.addMovie(movieDTO);
            return new ResponseEntity<>(savedMovie, HttpStatus.CREATED);
        }catch (MovieAlreadyExistsException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error adding movie: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @Valid @RequestBody MovieDTO movieDTO) {
        try{
            MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO);
            return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
        }catch (MovieNotfoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }catch (MovieAlreadyExistsException e){
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
        }catch (Exception e) {
            Map<String, String> errors = new HashMap<>();
            errors.put("errorMessage", "Error updating movie: " + e.getMessage());
            return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        try{
            movieService.deleteMovie(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }catch (MovieNotfoundException e) {
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
