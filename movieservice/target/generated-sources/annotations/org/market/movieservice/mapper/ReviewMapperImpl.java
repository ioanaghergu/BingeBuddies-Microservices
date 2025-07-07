package org.market.movieservice.mapper;

import javax.annotation.processing.Generated;
import org.market.movieservice.domain.Movie;
import org.market.movieservice.domain.Review;
import org.market.movieservice.dto.ReviewDTO;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-07-07T21:47:53+0300",
    comments = "version: 1.6.0.Beta1, compiler: javac, environment: Java 23.0.2 (Azul Systems, Inc.)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public Review toReview(ReviewDTO reviewDTO) {
        if ( reviewDTO == null ) {
            return null;
        }

        Review review = new Review();

        review.setUserId( reviewDTO.getUserId() );
        review.setId( reviewDTO.getId() );
        review.setRating( reviewDTO.getRating() );
        review.setComment( reviewDTO.getComment() );

        return review;
    }

    @Override
    public ReviewDTO toReviewDTO(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewDTO reviewDTO = new ReviewDTO();

        reviewDTO.setMovieId( reviewMovieId( review ) );
        reviewDTO.setId( review.getId() );
        reviewDTO.setRating( review.getRating() );
        reviewDTO.setComment( review.getComment() );
        reviewDTO.setUserId( review.getUserId() );

        return reviewDTO;
    }

    private Long reviewMovieId(Review review) {
        Movie movie = review.getMovie();
        if ( movie == null ) {
            return null;
        }
        return movie.getId();
    }
}
