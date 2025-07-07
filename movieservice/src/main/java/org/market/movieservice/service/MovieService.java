package org.market.movieservice.service;

import org.market.movieservice.dto.MovieDTO;

import java.util.List;
import java.util.Optional;

public interface MovieService {
    Optional<MovieDTO> getMovieById(Long id);
    List<MovieDTO> getAllMovies();
    MovieDTO addMovie(MovieDTO movieDTO);
    MovieDTO updateMovie(Long id, MovieDTO movieDTO);
    void deleteMovie(Long id);
}
