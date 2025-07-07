package org.market.movieclubsservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClubSettingsDTO {
    private Long id;

    @NotNull(message = "Club status required.")
    private Boolean isPublic;

    @NotNull(message = "Club max capacity required.")
    @Min(value = 1, message = "A club must have at least one member.")
    private Integer maxMembers;
}
