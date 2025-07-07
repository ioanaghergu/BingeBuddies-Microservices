package org.market.movieservice.mapper;

import org.mapstruct.Mapper;
import org.market.movieservice.domain.Movie;
import org.market.movieservice.dto.MovieDTO;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ReviewMapper.class})
public interface MovieMapper {
    Movie toMovie(MovieDTO movieDTO);
    MovieDTO toMovieDTO(Movie movie);
    List<MovieDTO> toMovieDTOList(List<Movie> movies);
}
