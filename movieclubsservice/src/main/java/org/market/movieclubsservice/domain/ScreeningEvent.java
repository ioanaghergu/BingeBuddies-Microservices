package org.market.movieclubsservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreeningEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Date requiered for a screening event.")
    @FutureOrPresent(message = "Event must take place in the present or the future.")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "CLUB_ID")
    @NotNull(message = "A screening event can only be created inside a movie club.")
    @ToString.Exclude
    private MovieClub movieClub;

    @NotNull(message = "A movie must be selected for the screening event.")
    private Long movieId;
}
