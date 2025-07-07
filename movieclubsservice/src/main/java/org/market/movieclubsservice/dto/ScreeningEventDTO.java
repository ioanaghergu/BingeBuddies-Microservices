package org.market.movieclubsservice.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningEventDTO {
    private Long id;

    @NotNull(message = "Date requiered for a screening event.")
    @FutureOrPresent(message = "Event must take place in the present or the future.")
    private LocalDateTime date;

    private Long movieClubId;

    @NotNull(message = "A movie must be selected for the screening event.")
    private Long movieId;
    private String movieTitle;
}
