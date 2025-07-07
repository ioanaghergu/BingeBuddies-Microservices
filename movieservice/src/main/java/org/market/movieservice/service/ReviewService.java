package org.market.movieservice.service;

import org.market.movieservice.dto.ReviewDTO;

import java.util.Optional;

public interface ReviewService {
    ReviewDTO addReview(Long movieId, Long userId, ReviewDTO reviewDTO);
    Optional<ReviewDTO> getReviewById(Long id);
    ReviewDTO updateReview(Long id, Long userId, ReviewDTO reviewDTO);
    void deleteReview(Long reviewId, Long userId);
    void updateMovieRating(Long movieId);
}
