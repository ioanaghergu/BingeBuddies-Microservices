package org.market.movieservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.market.movieservice.domain.Review;
import org.market.movieservice.dto.ReviewDTO;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "userId", source = "userId")
    Review toReview(ReviewDTO reviewDTO);

    @Mapping(source = "movie.id", target = "movieId")
    ReviewDTO toReviewDTO(Review review);

}
