package org.market.movieservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating value required.")
    @Min(value = 1, message = "Rating must be at least 1.")
    @Max(value = 5, message = "Rating can't exceed 5 stars.")
    private Integer rating;

    @Size(max = 100, message = "A comment can't exceed 100 characters.")
    private String comment;

    @NotNull(message = "User id required")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "MOVIE_ID")
    @NotNull
    @ToString.Exclude
    private Movie movie;
}
