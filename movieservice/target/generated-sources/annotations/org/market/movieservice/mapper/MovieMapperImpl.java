package org.market.movieservice.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.market.movieservice.domain.Movie;
import org.market.movieservice.domain.Review;
import org.market.movieservice.dto.MovieDTO;
import org.market.movieservice.dto.ReviewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:47:53+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class MovieMapperImpl implements MovieMapper {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public Movie toMovie(MovieDTO movieDTO) {
        if ( movieDTO == null ) {
            return null;
        }

        Movie movie = new Movie();

        movie.setId( movieDTO.getId() );
        movie.setTitle( movieDTO.getTitle() );
        movie.setGenre( movieDTO.getGenre() );
        movie.setReleaseYear( movieDTO.getReleaseYear() );
        movie.setAvgRating( movieDTO.getAvgRating() );
        movie.setReviews( reviewDTOListToReviewList( movieDTO.getReviews() ) );

        return movie;
    }

    @Override
    public MovieDTO toMovieDTO(Movie movie) {
        if ( movie == null ) {
            return null;
        }

        MovieDTO.MovieDTOBuilder movieDTO = MovieDTO.builder();

        movieDTO.id( movie.getId() );
        movieDTO.title( movie.getTitle() );
        movieDTO.genre( movie.getGenre() );
        movieDTO.releaseYear( movie.getReleaseYear() );
        movieDTO.avgRating( movie.getAvgRating() );
        movieDTO.reviews( reviewListToReviewDTOList( movie.getReviews() ) );

        return movieDTO.build();
    }

    @Override
    public List<MovieDTO> toMovieDTOList(List<Movie> movies) {
        if ( movies == null ) {
            return null;
        }

        List<MovieDTO> list = new ArrayList<MovieDTO>( movies.size() );
        for ( Movie movie : movies ) {
            list.add( toMovieDTO( movie ) );
        }

        return list;
    }

    protected List<Review> reviewDTOListToReviewList(List<ReviewDTO> list) {
        if ( list == null ) {
            return null;
        }

        List<Review> list1 = new ArrayList<Review>( list.size() );
        for ( ReviewDTO reviewDTO : list ) {
            list1.add( reviewMapper.toReview( reviewDTO ) );
        }

        return list1;
    }

    protected List<ReviewDTO> reviewListToReviewDTOList(List<Review> list) {
        if ( list == null ) {
            return null;
        }

        List<ReviewDTO> list1 = new ArrayList<ReviewDTO>( list.size() );
        for ( Review review : list ) {
            list1.add( reviewMapper.toReviewDTO( review ) );
        }

        return list1;
    }
}
