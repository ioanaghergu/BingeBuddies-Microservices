package org.market.movieservice.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.market.movieservice.client.UserServiceClient;
import org.market.movieservice.domain.Movie;
import org.market.movieservice.dto.MovieDTO;
import org.market.movieservice.dto.ReviewDTO;
import org.market.movieservice.dto.UserResponseDTO;
import org.market.movieservice.exceptions.MovieAlreadyExistsException;
import org.market.movieservice.exceptions.MovieNotfoundException;
import org.market.movieservice.exceptions.UnavailableService;
import org.market.movieservice.mapper.MovieMapper;
import org.market.movieservice.mapper.ReviewMapper;
import org.market.movieservice.repository.MovieRepository;
import org.market.movieservice.service.MovieService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;
    private final ReviewMapper reviewMapper;
    private final UserServiceClient userServiceClient;

    public MovieServiceImpl(MovieRepository movieRepository, MovieMapper movieMapper, ReviewMapper reviewMapper, UserServiceClient userServiceClient) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
        this.reviewMapper = reviewMapper;
        this.userServiceClient = userServiceClient;
        log.info("MovieServiceImpl initialized with MovieRepository, MovieMapper, ReviewMapper, and UserServiceClient.");
    }

    @Override
    public List<MovieDTO> getAllMovies() {
        log.info("Attempting to retrieve all movies.");
        List<Movie> movies = movieRepository.findAll();
        log.debug("Found {} movies in the database.", movies.size());

        List<MovieDTO> movieDTOs = movies.stream()
                .map(movie -> {
                    MovieDTO movieDTO = movieMapper.toMovieDTO(movie);
                    log.debug("Mapping movie with ID: {} and title: {} to MovieDTO.", movie.getId(), movie.getTitle());

                    if (movie.getReviews() != null && !movie.getReviews().isEmpty()) {
                        log.debug("Processing {} reviews for movie ID: {}.", movie.getReviews().size(), movie.getId());
                        List<ReviewDTO> reviews = movie.getReviews().stream()
                                .map(review -> {
                                    ReviewDTO reviewDTO = reviewMapper.toReviewDTO(review);
                                    log.debug("Mapping review ID: {} for user ID: {}.", review.getId(), review.getUserId());

                                    try {
                                        log.debug("Calling User Service for user ID: {} for review ID: {}.", review.getUserId(), review.getId());
                                        Optional<UserResponseDTO> userOptional = userServiceClient.getUserById(review.getUserId());
                                        if (userOptional.isPresent()) {
                                            reviewDTO.setUsername(userOptional.get().getUsername());
                                            log.debug("Successfully fetched username '{}' for user ID: {}.", userOptional.get().getUsername(), review.getUserId());
                                        } else {
                                            log.warn("User not found for ID: {}. Setting username to '[User Not Found]'.", review.getUserId());
                                            reviewDTO.setUsername("[User Not Found]");
                                        }
                                    } catch (Exception e) {
                                        log.error("Error calling User Service for user ID: {} for review ID: {}: {}", review.getUserId(), review.getId(), e.getMessage());
                                        throw new UnavailableService("User Service unavailable: " + e.getMessage());
                                    }
                                    return reviewDTO;
                                })
                                .collect(Collectors.toList());
                        movieDTO.setReviews(reviews);
                    } else {
                        log.debug("No reviews found for movie ID: {}.", movie.getId());
                    }
                    return movieDTO;
                })
                .collect(Collectors.toList());
        log.info("Finished retrieving and mapping all movies.");
        return movieDTOs;
    }

    @Override
    public Optional<MovieDTO> getMovieById(Long id) {
        log.info("Attempting to retrieve movie by ID: {}.", id);
        Optional<Movie> movie = movieRepository.findById(id);

        if(movie.isEmpty()) {
            log.warn("Movie with ID: {} not found. Throwing MovieNotfoundException.", id);
            throw new MovieNotfoundException("Movie with id " + id + " not found");
        }

        MovieDTO movieDTO = movieMapper.toMovieDTO(movie.get());
        log.debug("Movie with ID: {} found. Mapping to MovieDTO.", id);

        if(movie.get().getReviews() != null && !movie.get().getReviews().isEmpty()) {
            log.debug("Processing {} reviews for movie ID: {}.", movie.get().getReviews().size(), id);
            List<ReviewDTO> reviews = movie.get().getReviews().stream()
                    .map(review -> {
                        ReviewDTO reviewDTO = reviewMapper.toReviewDTO(review);
                        log.debug("Mapping review ID: {} for user ID: {}.", review.getId(), review.getUserId());
                        try{
                            log.debug("Calling User Service for user ID: {} for review ID: {}.", review.getUserId(), review.getId());
                            Optional<UserResponseDTO> user = userServiceClient.getUserById(review.getUserId());
                            if(user.isPresent()){
                                reviewDTO.setUsername(user.get().getUsername());
                                log.debug("Successfully fetched username '{}' for user ID: {}.", user.get().getUsername(), review.getUserId());
                            } else {
                                log.warn("User not found for ID: {}. Setting username to '[User Not Found]'.", review.getUserId());
                                reviewDTO.setUsername("[User Not Found]");
                            }
                        }catch (Exception e) {
                            log.error("Error calling User Service for user ID: {} for review ID: {}: {}", review.getUserId(), review.getId(), e.getMessage());
                            reviewDTO.setUsername("[User Not Found]");
                        }
                        return reviewDTO;
                    })
                    .collect(Collectors.toList());
            movieDTO.setReviews(reviews);
        } else {
            log.debug("No reviews found for movie ID: {}.", id);
        }

        log.info("Finished retrieving and mapping movie ID: {}.", id);
        return Optional.of(movieDTO);
    }

    @Override
    @Transactional
    public MovieDTO addMovie(MovieDTO movieDTO) {
        log.info("Attempting to add new movie with title: '{}' and release year: {}.", movieDTO.getTitle(), movieDTO.getReleaseYear());
        Optional<Movie> movie = movieRepository.findByTitleAndAndReleaseYear(movieDTO.getTitle(), movieDTO.getReleaseYear());

        if(movie.isPresent()){
            log.warn("Movie with title '{}' and release year {} already exists. Throwing MovieAlreadyExistsException.", movieDTO.getTitle(), movieDTO.getReleaseYear());
            throw new MovieAlreadyExistsException("Movie with title " + movieDTO.getTitle() + " already exists");
        }

        Movie newMovie = movieMapper.toMovie(movieDTO);
        Movie savedMovie = movieRepository.save(newMovie);
        log.info("New movie '{}' (ID: {}) saved successfully.", savedMovie.getTitle(), savedMovie.getId());
        return movieMapper.toMovieDTO(savedMovie);
    }

    @Override
    public MovieDTO updateMovie(Long id, MovieDTO movieDTO) {
        log.info("Attempting to update movie with ID: {} to title: '{}' and release year: {}.", id, movieDTO.getTitle(), movieDTO.getReleaseYear());
        Optional<Movie> movieOptional = movieRepository.findById(id);

        if(movieOptional.isEmpty()){
            log.warn("Movie with ID: {} not found for update. Throwing MovieNotfoundException.", id);
            throw new MovieNotfoundException("Movie with id " + id + " not found");
        }

        Movie movie = movieOptional.get();
        log.debug("Original movie found with ID: {}. Checking for title/year conflict.", id);

        Optional<Movie> conflictMovie = movieRepository.findByTitleAndAndReleaseYear(movieDTO.getTitle(), movieDTO.getReleaseYear());
        if(conflictMovie.isPresent() && !conflictMovie.get().getId().equals(id)){
            log.warn("Update conflict: Another movie with title '{}' and release year {} already exists (ID: {}). Throwing MovieAlreadyExistsException.",
                    movieDTO.getTitle(), movieDTO.getReleaseYear(), conflictMovie.get().getId());
            throw new MovieAlreadyExistsException("Movie with title " + movieDTO.getTitle() + " already exists");
        }

        movie.setTitle(movieDTO.getTitle());
        movie.setReleaseYear(movieDTO.getReleaseYear());
        movie.setGenre(movieDTO.getGenre());
        log.debug("Movie with ID: {} details updated in memory. Title: {}, Year: {}, Genre: {}.", id, movie.getTitle(), movie.getReleaseYear(), movie.getGenre());

        Movie updatedMovie = movieRepository.save(movie);
        log.info("Movie with ID: {} updated and saved successfully.", updatedMovie.getId());
        return movieMapper.toMovieDTO(updatedMovie);
    }

    @Override
    public void deleteMovie(Long id) {
        log.info("Attempting to delete movie with ID: {}.", id);
        Optional<Movie> movieOptional = movieRepository.findById(id);

        if(movieOptional.isEmpty()){
            log.warn("Movie with ID: {} not found for deletion. Throwing MovieNotfoundException.", id);
            throw new MovieNotfoundException("Movie with id " + id + " not found");
        }

        movieRepository.deleteById(id);
        log.info("Movie with ID: {} deleted successfully.", id);
    }
}