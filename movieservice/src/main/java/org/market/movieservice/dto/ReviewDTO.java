package org.market.movieservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;

    @NotNull(message = "Rating value required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating can't exceed 5 stars.")
    private Integer rating;

    @Size(max = 100, message = "A comment can't exceed 100 characters.")
    private String comment;

    private Long userId;
    private Long movieId;
    private String username;
}
