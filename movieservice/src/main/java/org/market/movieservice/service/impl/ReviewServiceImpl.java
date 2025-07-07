package org.market.movieservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.market.movieservice.client.UserServiceClient;
import org.market.movieservice.domain.Movie;
import org.market.movieservice.domain.Review;
import org.market.movieservice.dto.ReviewDTO;
import org.market.movieservice.dto.UserResponseDTO;
import org.market.movieservice.exceptions.*;
import org.market.movieservice.mapper.ReviewMapper;
import org.market.movieservice.repository.MovieRepository;
import org.market.movieservice.repository.ReviewRepository;
import org.market.movieservice.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final ReviewMapper reviewMapper;
    private final UserServiceClient userServiceClient;

    public ReviewServiceImpl(ReviewRepository reviewRepository, MovieRepository movieRepository, ReviewMapper reviewMapper, UserServiceClient userServiceClient) {
        this.reviewRepository = reviewRepository;
        this.movieRepository = movieRepository;
        this.reviewMapper = reviewMapper;
        this.userServiceClient = userServiceClient;
        log.info("ReviewServiceImpl initialized with ReviewRepository, MovieRepository, ReviewMapper, and UserServiceClient.");
    }

    @Override
    @Transactional
    public ReviewDTO addReview(Long movieId, Long userId, ReviewDTO reviewDTO) {
        log.info("Attempting to add a new review for movie ID: {} by user ID: {}.", movieId, userId);
        Optional<Movie> movie = movieRepository.findById(movieId);

        if(movie.isEmpty()){
            log.warn("Movie with ID: {} not found when adding review. Throwing MovieNotfoundException.", movieId);
            throw new MovieNotfoundException("Movie with id " + movieId + " not found");
        }
        log.debug("Movie with ID: {} found for review.", movieId);

        try {
            log.debug("Attempting to verify user ID: {} with User Service.", userId);
            Optional<UserResponseDTO> user = userServiceClient.getUserById(userId);
            if (user.isEmpty()) {
                log.warn("User with ID: {} not found by User Service.", userId);
                throw new UserNotFoundException("User with id " + userId + " not found from UserService.");
            }
            log.debug("User with ID: {} verified by User Service.", userId);
        }catch (Exception e) {
            log.error("Error verifying user ID: {} with User Service: {}", userId, e.getMessage());
            throw new UserNotFoundException("User with id " + userId + " not found from UserService. Error: " + e.getMessage());
        }

        if(!reviewRepository.findByMovieIdAndUserId(movieId, userId).isEmpty()) {
            log.warn("User ID: {} has already reviewed movie ID: {}. Throwing ReviewAlreadyExistsException.", userId, movieId);
            throw new ReviewAlreadyExistsException("User with id " + userId + " has already reviewed movie with id " + movieId);
        }
        log.debug("No existing review found for user ID: {} on movie ID: {}.", userId, movieId);

        Review review = reviewMapper.toReview(reviewDTO);
        review.setUserId(userId);
        review.setMovie(movie.get());
        log.debug("Review object prepared for saving. Rating: {}, Comment: '{}', User ID: {}, Movie ID: {}.",
                review.getRating(), review.getComment(), review.getUserId(), review.getMovie().getId());


        Review savedReview = reviewRepository.save(review);
        log.info("Review for movie ID: {} by user ID: {} saved successfully with ID: {}. Updating movie rating.",
                movieId, userId, savedReview.getId());
        updateMovieRating(movieId);

        return reviewMapper.toReviewDTO(savedReview);
    }

    @Override
    public Optional<ReviewDTO> getReviewById(Long id){
        log.info("Attempting to retrieve review by ID: {}.", id);
        Optional<Review> review = reviewRepository.findById(id);

        if(review.isEmpty()){
            log.warn("Review with ID: {} not found. Throwing ReviewNotFoundException.", id);
            throw new ReviewNotFoundException("Review with id " + id + " not found");
        }

        ReviewDTO reviewDTO = reviewMapper.toReviewDTO(review.get());
        log.debug("Review with ID: {} found. Mapping to ReviewDTO.", id);

        try {
            log.debug("Attempting to fetch username for user ID: {} for review ID: {}.", reviewDTO.getUserId(), id);
            Optional<UserResponseDTO> userDetailsOptional = userServiceClient.getUserById(reviewDTO.getUserId());
            if (userDetailsOptional.isPresent()) {
                reviewDTO.setUsername(userDetailsOptional.get().getUsername());
                log.debug("Successfully fetched username '{}' for user ID: {}.", userDetailsOptional.get().getUsername(), reviewDTO.getUserId());
            } else {
                log.warn("User not found for ID: {}. Setting username to '[User Not Found]' for review ID: {}.", reviewDTO.getUserId(), id);
                reviewDTO.setUsername("[User Not Found]");
            }
        } catch (Exception e) {
            log.error("Error fetching user details for ID: {} for review ID: {}: {}", reviewDTO.getUserId(), id, e.getMessage());
            reviewDTO.setUsername("[User Not Found]");
        }
        log.info("Finished retrieving and mapping review ID: {}.", id);
        return Optional.of(reviewDTO);
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(Long id, Long userId, ReviewDTO reviewDTO){
        log.info("Attempting to update review ID: {} by user ID: {}.", id, userId);
        Optional<Review> review = reviewRepository.findById(id);

        if(review.isEmpty()){
            log.warn("Review with ID: {} not found for update. Throwing ReviewNotFoundException.", id);
            throw new ReviewNotFoundException("Review with id " + id + " not found");
        }

        if(!review.get().getUserId().equals(userId)){
            log.warn("User ID: {} attempted to update review ID: {}, but does not have permission (owner ID: {}). Throwing PermissionDeniedException.",
                    userId, id, review.get().getUserId());
            throw new PermissionDeniedException("You don't have permission to update this review");
        }
        log.debug("User ID: {} has permission to update review ID: {}.", userId, id);

        Review existingReview = review.get();
        log.debug("Existing review content: Rating: {}, Comment: '{}'. New content: Rating: {}, Comment: '{}'.",
                existingReview.getRating(), existingReview.getComment(), reviewDTO.getRating(), reviewDTO.getComment());
        existingReview.setRating(reviewDTO.getRating());
        existingReview.setComment(reviewDTO.getComment());

        Review updatedReview = reviewRepository.save(existingReview);
        log.info("Review ID: {} updated successfully. Updating movie rating for movie ID: {}.", id, updatedReview.getMovie().getId());
        updateMovieRating(updatedReview.getMovie().getId());

        ReviewDTO updatedReviewDTO = reviewMapper.toReviewDTO(updatedReview);

        try {
            log.debug("Attempting to fetch username for user ID: {} for updated review ID: {}.", updatedReviewDTO.getUserId(), id);
            Optional<UserResponseDTO> userDetailsOptional = userServiceClient.getUserById(updatedReviewDTO.getUserId());
            if (userDetailsOptional.isPresent()) {
                updatedReviewDTO.setUsername(userDetailsOptional.get().getUsername());
                log.debug("Successfully fetched username '{}' for user ID: {}.", userDetailsOptional.get().getUsername(), updatedReviewDTO.getUserId());
            } else {
                log.warn("User not found for ID: {}. Setting username to '[User Not Found]' for updated review ID: {}.", updatedReviewDTO.getUserId(), id);
                updatedReviewDTO.setUsername("[User Not Found]");
            }
        } catch (Exception e) {
            log.error("Error fetching user details for ID: {} for updated review ID: {}: {}", updatedReviewDTO.getUserId(), id, e.getMessage());
            updatedReviewDTO.setUsername("[User Not Found]");
        }
        log.info("Finished updating and mapping review ID: {}.", id);
        return updatedReviewDTO;
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Attempting to delete review ID: {} by user ID: {}.", reviewId, userId);
        Optional<Review> review = reviewRepository.findById(reviewId);

        if(review.isEmpty()){
            log.warn("Review with ID: {} not found for deletion. Throwing ReviewNotFoundException.", reviewId);
            throw new ReviewNotFoundException("Review with id " + reviewId + " not found");
        }

        if(!review.get().getUserId().equals(userId)){
            log.warn("User ID: {} attempted to delete review ID: {}, but does not have permission (owner ID: {}). Throwing PermissionDeniedException.",
                    userId, reviewId, review.get().getUserId());
            throw new PermissionDeniedException("You don't have permission to delete this review");
        }
        log.debug("User ID: {} has permission to delete review ID: {}.", userId, reviewId);

        Long movieId = review.get().getMovie().getId();
        reviewRepository.deleteById(reviewId);
        log.info("Review ID: {} deleted successfully. Updating movie rating for movie ID: {}.", reviewId, movieId);
        updateMovieRating(movieId);
    }

    @Override
    @Transactional
    public void updateMovieRating(Long movieId){
        log.info("Calculating and updating average rating for movie ID: {}.", movieId);
        Optional<Movie> movie = movieRepository.findById(movieId);

        if(movie.isEmpty()){
            log.warn("Movie with ID: {} not found when updating rating. Throwing MovieNotfoundException.", movieId);
            throw new MovieNotfoundException("Movie with id " + movieId + " not found");
        }
        log.debug("Movie with ID: {} found for rating update.", movieId);

        List<Review> reviews = reviewRepository.findByMovieId(movieId);
        log.debug("Found {} reviews for movie ID: {}.", reviews.size(), movieId);

        if(reviews.isEmpty()){
            movie.get().setAvgRating(0.0);
            log.debug("No reviews found for movie ID: {}. Setting average rating to 0.0.", movieId);
        }
        else {
            Integer sumRating = reviews.stream().mapToInt(Review::getRating).sum();
            double avgRating = (double) sumRating / reviews.size();
            double roundedAvgRating = Math.round(avgRating * 10.0) / 10.0;
            movie.get().setAvgRating(roundedAvgRating);
            log.debug("Calculated average rating for movie ID: {}: Sum={}, Count={}, Raw Avg={}, Rounded Avg={}.",
                    movieId, sumRating, reviews.size(), avgRating, roundedAvgRating);
        }

        movieRepository.save(movie.get());
        log.info("Average rating for movie ID: {} updated to {}.", movieId, movie.get().getAvgRating());
    }
}