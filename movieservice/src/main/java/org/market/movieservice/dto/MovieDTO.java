package org.market.movieservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieDTO {
    private Long id;

    @NotBlank(message = "Movie title required.")
    @Size(max = 255, message = "Title must not exceed 255 characters.")
    private String title;

    @NotBlank(message = "Movie genre required.")
    @Size(max = 255, message = "Genre must not exceed 255 characters.")
    private String genre;

    @NotNull(message = "Release year required.")
    @Min(value = 2000, message = "Release year must be after 2000.")
    @Max(value = 2030, message = "Release year can't be in the distant future")
    private Integer releaseYear;

    @Min(value = 0, message = "Movie rating can't be negative.")
    @Max(value = 5, message = "Movie rating can't exceed 5 stars.")
    private Double avgRating;

    private List<ReviewDTO> reviews;
}
