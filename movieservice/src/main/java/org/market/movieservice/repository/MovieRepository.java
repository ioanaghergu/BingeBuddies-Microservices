package org.market.movieservice.repository;

import org.market.movieservice.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByTitleAndAndReleaseYear(String title, Integer releaseYear);
    Optional<Movie> findByTitleAndAndReleaseYearAndGenre(String title, Integer releaseYear, String genre);
}
