package org.market.movieclubsservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieClubDTO {
    private Long id;

    //@NotNull(message = "A movie club must have an admin.")
    private Long adminId;

    @NotBlank(message = "Club name required")
    @Size(min = 3, max = 100, message = "Club name must be between 3 and 100 characters.")
    private String name;

    @Size(max = 100, message = "Club description must not exceed 100 characters.")
    private String description;

    @NotNull(message = "Club settings required.")
    @Valid
    private ClubSettingsDTO settings;

    private Set<UserDTO> members = new HashSet<>();
    private List<ScreeningEventDTO> screeningEvents;
}
